package com.example.bodydetectionapp.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bodydetectionapp.data.models.Exercise
import com.example.bodydetectionapp.data.models.Landmark
import com.example.bodydetectionapp.data.models.RepTimestamp
import com.example.bodydetectionapp.ml.ExerciseEvaluator
import com.example.bodydetectionapp.ml.ExerciseState
import com.example.bodydetectionapp.ml.PoseDetectionHelper
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExerciseTrackingViewModel : ViewModel() {

    private var poseDetectionHelper: PoseDetectionHelper? = null
    private val exerciseEvaluator = ExerciseEvaluator()

    private val _poseResult = MutableStateFlow<PoseLandmarkerResult?>(null)
    val poseResult: StateFlow<PoseLandmarkerResult?> = _poseResult.asStateFlow()

    private val _anglesToDisplay = MutableStateFlow<Map<String, Double>>(emptyMap())
    val anglesToDisplay: StateFlow<Map<String, Double>> = _anglesToDisplay.asStateFlow()

    private val _currentExercise = MutableStateFlow<Exercise?>(null)
    val currentExercise: StateFlow<Exercise?> = _currentExercise.asStateFlow()

    private val _exerciseState = MutableStateFlow(ExerciseState.NOT_STARTED)
    val exerciseState: StateFlow<ExerciseState> = _exerciseState.asStateFlow()

    private val _feedbackMessage = MutableStateFlow("Initializing...")
    val feedbackMessage: StateFlow<String> = _feedbackMessage.asStateFlow()

    private val _repCount = MutableStateFlow(0)
    val repCount: StateFlow<Int> = _repCount.asStateFlow()

    private val _countdownValue = MutableStateFlow(0)
    val countdownValue: StateFlow<Int> = _countdownValue.asStateFlow()

    val repTimestamps = mutableListOf<RepTimestamp>()
    private val _exerciseStartTime = MutableStateFlow<Long?>(null)
    val exerciseStartTime: StateFlow<Long?> = _exerciseStartTime.asStateFlow()

    init {
        Log.d("ViewModel", "ViewModel Initialized.")
        exerciseEvaluator.onRepCompleted = { count ->
            Log.d("ViewModelCallback", "onRepCompleted: $count")
            _repCount.value = count
            if (_exerciseStartTime.value != null) {
                repTimestamps.add(RepTimestamp(count, System.currentTimeMillis()))
            }
        }
        exerciseEvaluator.onFeedbackUpdate = { message ->
            Log.d("ViewModelCallback", "onFeedbackUpdate: '$message'")
            _feedbackMessage.value = message
        }
        exerciseEvaluator.onStateChanged = { newState ->
            Log.d("ViewModelCallback", "onStateChanged: $newState")
            _exerciseState.value = newState
            if (newState == ExerciseState.IN_PROGRESS && _exerciseStartTime.value == null) {
                _exerciseStartTime.value = System.currentTimeMillis()
                Log.d("ViewModel", "Exercise started at: ${_exerciseStartTime.value} ms")
            }
        }
        exerciseEvaluator.onCountdownTick = { tick ->
            Log.d("ViewModelCallback", "onCountdownTick: $tick")
            _countdownValue.value = tick
        }
    }

    fun initializePoseDetectionHelper(context: Context) {
        if (poseDetectionHelper == null) {
            Log.d("ViewModel", "Initializing PoseDetectionHelper...")
            poseDetectionHelper = PoseDetectionHelper(context) { result, landmarks, angles ->
                processPoseResult(result, landmarks, angles)
            }
            Log.d("ViewModel", "PoseDetectionHelper Initialized.")
        }
    }

    fun setExercise(exercise: Exercise) {
        Log.d("ViewModel", "Setting exercise to: ${exercise.name}")
        _currentExercise.value = exercise
        exerciseEvaluator.setExercise(exercise)
        _repCount.value = 0
        repTimestamps.clear()
        _exerciseStartTime.value = null
        _feedbackMessage.value = "Waiting for user to get in position."
        Log.d("ViewModel", "State has been reset for new exercise.")
    }

    fun detectPose(bitmap: Bitmap) {
        // This log can be spammy, so we keep it commented out unless needed.
        // Log.d("ViewModel", "Submitting frame for detection.")
        poseDetectionHelper?.detect(bitmap)
    }

    private fun processPoseResult(
        result: PoseLandmarkerResult,
        landmarks: Map<String, Landmark>?,
        angles: Map<String, Double>?
    ) {
        // This is the fix from before, ensuring UI updates happen on the main thread.
        viewModelScope.launch {
            Log.d("ViewModel", "processPoseResult: Received data from helper. Landmarks detected: ${landmarks != null}, Angles calculated: ${angles != null}")
            _poseResult.value = result

            Log.d("ViewModel", "Calling evaluator.evaluate() with current state: ${_exerciseState.value}")
            exerciseEvaluator.evaluate(landmarks, angles)
            Log.d("ViewModel", "Finished evaluator.evaluate()")

            angles?.let {
                _anglesToDisplay.value = filterAnglesForDisplay(it)
            }
        }
    }

    private fun filterAnglesForDisplay(allAngles: Map<String, Double>): Map<String, Double> {
        val exercise = _currentExercise.value ?: return allAngles
        val relevantAngleNames = exercise.repCounter.keyJointsToTrack
        return allAngles.filterKeys { it in relevantAngleNames }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("ViewModel", "ViewModel cleared. Closing PoseDetectionHelper.")
        poseDetectionHelper?.close()
    }
}
