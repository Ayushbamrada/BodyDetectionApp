package com.example.bodydetectionapp.ui.screens

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bodydetectionapp.data.models.AngleRange
import com.example.bodydetectionapp.data.models.Exercise
import com.example.bodydetectionapp.data.models.ExercisePhase
import com.example.bodydetectionapp.ml.ExerciseEvaluator
import com.example.bodydetectionapp.ml.PoseDetectionHelper
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExerciseTrackingViewModel : ViewModel() {

    private val _poseResult = MutableStateFlow<PoseLandmarkerResult?>(null)
    val poseResult: StateFlow<PoseLandmarkerResult?> = _poseResult.asStateFlow()

    private val _highlightedJoints = MutableStateFlow<Set<Int>>(emptySet())
    val highlightedJoints: StateFlow<Set<Int>> = _highlightedJoints.asStateFlow()

    private val _currentAngles = MutableStateFlow<Map<String, Double>>(emptyMap())
    val currentAngles: StateFlow<Map<String, Double>> = _currentAngles.asStateFlow()

    private val _feedbackMessages = MutableStateFlow<List<String>>(emptyList())
    val feedbackMessages: StateFlow<List<String>> = _feedbackMessages.asStateFlow()

    private val _repCount = MutableStateFlow(0)
    val repCount: StateFlow<Int> = _repCount.asStateFlow()

    private val _exerciseSummary = MutableStateFlow("Loading exercise...")
    val exerciseSummary: StateFlow<String> = _exerciseSummary.asStateFlow()

    private val _currentExerciseModel = MutableStateFlow<Exercise?>(null) // New: Holds the current exercise model
    val currentExerciseModel: StateFlow<Exercise?> = _currentExerciseModel.asStateFlow()

    private val _currentPhaseInfo = MutableStateFlow<ExercisePhase?>(null) // New: Holds current phase info
    val currentPhaseInfo: StateFlow<ExercisePhase?> = _currentPhaseInfo.asStateFlow()


    private lateinit var poseDetectionHelper: PoseDetectionHelper
    private val exerciseEvaluator = ExerciseEvaluator()

    init {
        exerciseEvaluator.onRepCompleted = { count ->
            _repCount.value = count
            _exerciseSummary.value = exerciseEvaluator.getEvaluationSummary()
        }
        exerciseEvaluator.onFeedbackUpdate = { messages ->
            _feedbackMessages.value = messages
            _exerciseSummary.value = exerciseEvaluator.getEvaluationSummary()
            // Ensure this update happens after evaluation, so currentPhaseIndex is accurate
            _currentPhaseInfo.value = exerciseEvaluator.currentExercise?.phases?.getOrNull(exerciseEvaluator.currentPhaseIndex)
        }
    }

    fun initializePoseDetectionHelper(context: Context) {
        if (!this::poseDetectionHelper.isInitialized) {
            poseDetectionHelper = PoseDetectionHelper(context) { result, newHighlightedJoints, newAngles ->
                _poseResult.value = result
                _highlightedJoints.value = newHighlightedJoints
                _currentAngles.value = newAngles
                viewModelScope.launch {
                    exerciseEvaluator.evaluate(newAngles)
                }
            }
        }
    }

    fun setExercise(exercise: Exercise?) {
        _currentExerciseModel.value = exercise // Set the exercise model in ViewModel state
        exerciseEvaluator.setExercise(exercise)
        _repCount.value = exerciseEvaluator.repCount
        _feedbackMessages.value = emptyList()
        _exerciseSummary.value = exerciseEvaluator.getEvaluationSummary()
        // Initialize current phase info when exercise is set
        _currentPhaseInfo.value = exerciseEvaluator.currentExercise?.phases?.getOrNull(exerciseEvaluator.currentPhaseIndex)
    }

    fun detectPose(bitmap: Bitmap) {
        if (this::poseDetectionHelper.isInitialized) {
            poseDetectionHelper.detect(bitmap)
        }
    }

    /**
     * Helper function to get the angles specific to the current exercise
     * for drawing on the PoseOverlay.
     */
    fun getRequiredAnglesForDisplay(exercise: Exercise): Map<String, Double> {
        val requiredAngles = mutableMapOf<String, Double>()
        val currentAnglesMap = currentAngles.value

        // Collect all angle names that are targeted across all phases of the exercise
        // This ensures only angles relevant to the current exercise are passed to the overlay
        val angleNames = exercise.phases.flatMap { it.targetAngles.keys }.toSet()

        angleNames.forEach { angleName ->
            currentAnglesMap[angleName]?.let { angleValue ->
                requiredAngles[angleName] = angleValue
            }
        }
        return requiredAngles
    }
}