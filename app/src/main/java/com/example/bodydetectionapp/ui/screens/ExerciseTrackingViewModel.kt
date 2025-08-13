package com.example.bodydetectionapp.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bodydetectionapp.data.models.Exercise
import com.example.bodydetectionapp.data.models.ExercisePhase
import com.example.bodydetectionapp.ml.ExerciseEvaluator
import com.example.bodydetectionapp.ml.PoseDetectionHelper
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update


data class RepTimestamp(val repCount: Int, val timestamp: Long)

class ExerciseTrackingViewModel : ViewModel() {

    private var poseDetectionHelper: PoseDetectionHelper? = null
    private val exerciseEvaluator = ExerciseEvaluator()

    private val _poseResult = MutableStateFlow<PoseLandmarkerResult?>(null)
    val poseResult: StateFlow<PoseLandmarkerResult?> = _poseResult.asStateFlow()

    private val _highlightedJoints = MutableStateFlow<Set<Int>>(emptySet())
    val highlightedJoints: StateFlow<Set<Int>> = _highlightedJoints.asStateFlow()

    private val _allCalculatedAngles = MutableStateFlow<Map<String, Double>>(emptyMap())
    // This will hold the angles to display on the overlay, filtered by relevance
    private val _anglesToDisplay = MutableStateFlow<Map<String, Double>>(emptyMap())
    val anglesToDisplay: StateFlow<Map<String, Double>> = _anglesToDisplay.asStateFlow()

    private val _feedbackMessages = MutableStateFlow<List<String>>(emptyList())
    val feedbackMessages: StateFlow<List<String>> = _feedbackMessages.asStateFlow()

    private val _repCount = MutableStateFlow(0)
    val repCount: StateFlow<Int> = _repCount.asStateFlow()

    private val _currentExerciseModel = MutableStateFlow<Exercise?>(null)
    val currentExerciseModel: StateFlow<Exercise?> = _currentExerciseModel.asStateFlow()

    private val _currentPhaseInfo = MutableStateFlow<ExercisePhase?>(null)
    val currentPhaseInfo: StateFlow<ExercisePhase?> = _currentPhaseInfo.asStateFlow()

    // --- New State for Initial Pose Auto-Detection ---
    private val _isInitialPoseCaptured = MutableStateFlow(false)
    val isInitialPoseCaptured: StateFlow<Boolean> = _isInitialPoseCaptured.asStateFlow()

    private val _initialAngles = MutableStateFlow<Map<String, Double>>(emptyMap())
    // This buffer helps check for stability before capturing initial angles
    private val angleBuffer = mutableMapOf<String, MutableList<Double>>()
    private val BUFFER_SIZE = 30 // Number of frames to check for stability (~1 second at 30 FPS)
    private val STABILITY_THRESHOLD_FOR_AUTO_START = 10.0 // Max degrees difference allowed for "stable" auto-start
    private val INSTRUCTION_MESSAGE_DURATION = 3000L // 3 seconds to show instruction before auto-start check begins
    private var instructionDisplayStartTime: Long = 0L

    // NEW: A list to store the timestamp of each completed rep.
    val repTimestamps = mutableListOf<RepTimestamp>()

    init {
        // Set up evaluator callbacks
        exerciseEvaluator.onRepCompleted = { count ->
            _repCount.value = count
            // NEW: Every time a rep is completed, add a new entry to our list.
            repTimestamps.add(RepTimestamp(count, System.currentTimeMillis()))
        }
        exerciseEvaluator.onFeedbackUpdate = { messages ->
            _feedbackMessages.value = messages
        }
        exerciseEvaluator.onPhaseChanged = { phase ->
            _currentPhaseInfo.value = phase
        }
    }

    fun initializePoseDetectionHelper(context: Context) {
        if (poseDetectionHelper == null) {
            poseDetectionHelper = PoseDetectionHelper(context) { result, highlighted, angles ->
                processPoseResult(result, highlighted, angles)
            }
        }
    }

    fun setExercise(exercise: Exercise?) {
        _currentExerciseModel.value = exercise
        exerciseEvaluator.setExercise(exercise)
        // Reset initial pose capture state when a new exercise is set
        _isInitialPoseCaptured.value = false
        _initialAngles.value = emptyMap()
        angleBuffer.clear()
        _repCount.value = 0 // Reset rep count for new exercise
        instructionDisplayStartTime = System.currentTimeMillis() // Reset timer for new exercise

        // NEW: Clear the list for a new session and add the starting point (Rep 0).
        repTimestamps.clear()
        repTimestamps.add(RepTimestamp(0, System.currentTimeMillis()))
    }

    fun detectPose(bitmap: Bitmap) {
        viewModelScope.launch {
            poseDetectionHelper?.detect(bitmap)
        }
    }

    // Call this from UI to start the initial pose detection process
    fun triggerInitialAngleCaptureFromUI() {
        if (!_isInitialPoseCaptured.value && _currentExerciseModel.value != null && _currentExerciseModel.value?.name != "free_movement") {
            angleBuffer.clear() // Clear any old data
            _feedbackMessages.value = listOf("Get into your starting position and hold still. Exercise will begin shortly.")
            instructionDisplayStartTime = System.currentTimeMillis() // Start timer for instruction message
            Log.d("ViewModel", "Triggered initial angle capture. Waiting for stable pose.")
        } else if (_currentExerciseModel.value?.name == "free_movement") {
            // For free movement, capture immediately as there's no specific starting pose
            captureInitialAngles(_allCalculatedAngles.value)
            Log.d("ViewModel", "Free movement: Initial angles captured immediately.")
        }
    }


    private fun processPoseResult(
        result: PoseLandmarkerResult,
        highlighted: Set<Int>,
        currentAngles: Map<String, Double>
    ) {
        _poseResult.value = result
        _highlightedJoints.value = highlighted
        _allCalculatedAngles.value = currentAngles // Store all calculated angles

        val exerciseModel = _currentExerciseModel.value

        // --- Initial Pose Auto-Detection Logic ---
        if (!_isInitialPoseCaptured.value && exerciseModel != null && exerciseModel.name != "free_movement") {
            // Give user a moment to read instructions/get into position before auto-detecting
            if (System.currentTimeMillis() - instructionDisplayStartTime < INSTRUCTION_MESSAGE_DURATION) {
                // Keep showing initial instruction message and wait
                _feedbackMessages.value = listOf("Get into your starting position and hold still.")
                return
            }

            val firstPhase = exerciseModel.phases.firstOrNull() ?: run {
                Log.w("ViewModel", "No phases defined for exercise: ${exerciseModel.name}")
                _feedbackMessages.value = listOf("Exercise definition error: No phases found.")
                return
            }

            var isInCorrectPredefinedPose = true
            val tempFeedback = mutableListOf<String>()

            // 1. Check if current angles match any ABSOLUTE targets for the first phase
            // This is for exercises like Sit-to-Stand where the start *must* be sitting.
            // For Squat/Hand Raise, the first phase might not have strict absolute targets,
            // or they might be loose (e.g., "Torso Angle" for standing straight).
            if (firstPhase.targetAngles.isNotEmpty()) {

                firstPhase.targetAngles.forEach { (angleName, range) ->
                    val angleValue = currentAngles[angleName]
                    if (angleValue == null || angleValue.isNaN() || angleValue < range.min || angleValue > range.max) {
                        isInCorrectPredefinedPose = false
                        tempFeedback.add("Adjust $angleName. Current: %.0f°, Target: %.0f-%.0f°.".format(angleValue ?: Double.NaN, range.min, range.max))
                    }
                }
            } else {
                // If the first phase has no absolute targetAngles, assume any generally stable pose is potentially valid.
                // This means exercises like Squat don't *require* specific hardcoded angles for their standing start,
                // just that the user is stable.
                isInCorrectPredefinedPose = true
            }

            // 2. Check for Pose Stability for the relevant angles
            var isPoseStable = true
            // Change to mutable set
            val anglesToCheckForStability: MutableSet<String> = if (firstPhase.targetAngles.isNotEmpty()) {
                firstPhase.targetAngles.keys.toMutableSet() // If specific absolute targets, check those for stability
            } else {
                // For a generic standing start (like Squat, Hand Raise), check key angles for overall body stability
                mutableSetOf("Left Knee Angle", "Right Knee Angle", "Left Hip Angle", "Right Hip Angle", "Torso Angle", "Left Shoulder Angle", "Right Shoulder Angle")
            }

            if (anglesToCheckForStability.isEmpty()) { // Fallback for exercises with no specific angle targets (e.g., simple free movement)
                anglesToCheckForStability.addAll(currentAngles.keys) // Corrected: addAll is now possible
            }

            if (currentAngles.isEmpty() || anglesToCheckForStability.any { !currentAngles.containsKey(it) || currentAngles[it]!!.isNaN() }) {
                isPoseStable = false
                tempFeedback.add("Waiting for full pose detection. Ensure all joints are visible.")
            } else {
                anglesToCheckForStability.forEach { angleName ->
                    val angleValue = currentAngles[angleName]
                    if (angleValue != null && !angleValue.isNaN()) {
                        angleBuffer.getOrPut(angleName) { mutableListOf() }.add(angleValue)
                        if (angleBuffer[angleName]!!.size > BUFFER_SIZE) {
                            angleBuffer[angleName]!!.removeAt(0)
                        }

                        val history = angleBuffer[angleName]
                        if (history != null && history.size == BUFFER_SIZE) {
                            val maxDiff = history.maxOrNull()!! - history.minOrNull()!!
                            if (maxDiff > STABILITY_THRESHOLD_FOR_AUTO_START) {
//                                Log.d("ETVModel",history.toString())
//                                Log.d("ETVModel",maxDiff.toString())

                                isPoseStable = false
                                tempFeedback.add("Hold still. ${angleName.replace(" Angle", "")} changing by %.1f°".format(maxDiff))
                            }
                        } else {
//                            Log.d("ETVModel","PoseStable: $isPoseStable")

                            isPoseStable = false // Not enough data yet to check stability
                            tempFeedback.add("Hold still to detect starting position.")
                        }
                    } else {
                        isPoseStable = false // Angle not detected, cannot be stable
                        tempFeedback.add("Waiting for pose detection. Adjust position.")
                    }
                }
            }

//            Log.d("ETVModel","InitialPosecaptured: $isInCorrectPredefinedPose, isPoseStable: $isPoseStable ")


            if (isInCorrectPredefinedPose && isPoseStable) {

//                Log.d("ETVModel","Initial angles captured.")
                captureInitialAngles(currentAngles)
            } else {
                if (tempFeedback.isEmpty()) { // Generic message if no specific angle issues, but not stable/correct
                    _feedbackMessages.value = listOf("Get into your starting position and hold still.")
                } else {
                    _feedbackMessages.value = tempFeedback
                }
                _currentPhaseInfo.value = firstPhase // Keep showing the first phase's info during guidance
            }
            return // Stop further processing until initial pose is captured
        }


        // --- Normal Exercise Tracking (after initial pose is captured or for free movement) ---
        if (exerciseModel != null) {
            val initialAnglesSnapshot = _initialAngles.value
            // Pass the current rep count to the evaluator for its internal logic
            val (newPhase, feedbackList) = exerciseEvaluator.evaluate(
                currentAngles,
                initialAnglesSnapshot
            )
            _currentPhaseInfo.value = newPhase
            _feedbackMessages.value = feedbackList

            // Update angles to display on the overlay based on the current phase's targets
            _anglesToDisplay.value = getRequiredAnglesForDisplay(exerciseModel, currentAngles)
        } else {
            // Free movement mode: just display all angles
            _anglesToDisplay.value = currentAngles
            _feedbackMessages.value = listOf("Free Movement Mode")
            _currentPhaseInfo.value = null // No specific phase for free movement
        }
    }

    private fun captureInitialAngles(allCurrentAngles: Map<String, Double>) {
        if (!_isInitialPoseCaptured.value) {
            _initialAngles.value = allCurrentAngles
            _isInitialPoseCaptured.value = true
            angleBuffer.clear() // Clear buffer once captured
            _feedbackMessages.value = listOf("Starting Position Detected! Begin exercise.")
            Log.d("ViewModel", "Initial angles captured: ${_initialAngles.value}")
            exerciseEvaluator.currentPhaseIndex = 0 // Corrected: This line assumes currentPhaseIndex is a mutable property in ExerciseEvaluator
            // Also notify evaluator of the new initial angles for its internal calculations
            _currentExerciseModel.value?.let {
                // Trigger re-evaluation with captured initial angles
                processPoseResult(_poseResult.value!!, _highlightedJoints.value, _allCalculatedAngles.value)
            }
        }
    }

    /**
     * Provides all currently calculated angles. Used primarily for "free_movement" mode.
     */
    fun getAllCalculatedAngles(): Map<String, Double> {
        return _allCalculatedAngles.value
    }

    /**
     * Provides a filtered map of angles relevant to the current exercise's active phase,
     * including both absolute and relative targets.
     */
    fun getRequiredAnglesForDisplay(exercise: Exercise, allCurrentAngles: Map<String, Double>): Map<String, Double> {
        val relevantAngles = mutableMapOf<String, Double>()
        val currentPhase = _currentPhaseInfo.value ?: return emptyMap()

        // Add angles from absolute targets
        currentPhase.targetAngles.keys.forEach { angleName ->
            allCurrentAngles[angleName]?.let { relevantAngles[angleName] = it }
        }

        // Add angles from relative targets
        currentPhase.relativeTargetAngles.forEach { relativeTarget ->
            allCurrentAngles[relativeTarget.angleName]?.let { relevantAngles[relativeTarget.angleName] = it }
        }

        return relevantAngles
    }


    override fun onCleared() {
        super.onCleared()
        poseDetectionHelper?.close()
    }
}