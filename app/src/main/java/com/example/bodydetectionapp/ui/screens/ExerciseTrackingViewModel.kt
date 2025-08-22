//package com.example.bodydetectionapp.ui.screens
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.bodydetectionapp.data.models.*
//import com.example.bodydetectionapp.ml.ExerciseEvaluator
//import com.example.bodydetectionapp.ml.ExerciseState
//import com.example.bodydetectionapp.ml.PoseDetectionHelper
//import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//
//class ExerciseTrackingViewModel : ViewModel() {
//
//    private var poseDetectionHelper: PoseDetectionHelper? = null
//    private val exerciseEvaluator = ExerciseEvaluator()
//
//    private val _poseResult = MutableStateFlow<PoseLandmarkerResult?>(null)
//    val poseResult: StateFlow<PoseLandmarkerResult?> = _poseResult.asStateFlow()
//
//    private val _anglesToDisplay = MutableStateFlow<Map<String, Double>>(emptyMap())
//    val anglesToDisplay: StateFlow<Map<String, Double>> = _anglesToDisplay.asStateFlow()
//
//    private val _currentExercise = MutableStateFlow<Exercise?>(null)
//    val currentExercise: StateFlow<Exercise?> = _currentExercise.asStateFlow()
//
//    private val _exerciseState = MutableStateFlow(ExerciseState.NOT_STARTED)
//    val exerciseState: StateFlow<ExerciseState> = _exerciseState.asStateFlow()
//
//    private val _feedbackMessage = MutableStateFlow("Initializing...")
//    val feedbackMessage: StateFlow<String> = _feedbackMessage.asStateFlow()
//
//    private val _repCount = MutableStateFlow(0)
//    val repCount: StateFlow<Int> = _repCount.asStateFlow()
//
//    private val _countdownValue = MutableStateFlow(0)
//    val countdownValue: StateFlow<Int> = _countdownValue.asStateFlow()
//
//    val repTimestamps = mutableListOf<RepTimestamp>()
//    private val _exerciseStartTime = MutableStateFlow<Long?>(null)
//    val exerciseStartTime: StateFlow<Long?> = _exerciseStartTime.asStateFlow()
//
//    init {
//        exerciseEvaluator.onRepCompleted = { count ->
//            _repCount.value = count
//            if (_exerciseStartTime.value != null) {
//                repTimestamps.add(RepTimestamp(count, System.currentTimeMillis()))
//            }
//        }
//        exerciseEvaluator.onFeedbackUpdate = { message ->
//            _feedbackMessage.value = message
//        }
//        exerciseEvaluator.onStateChanged = { newState ->
//            _exerciseState.value = newState
//            if (newState == ExerciseState.IN_PROGRESS && _exerciseStartTime.value == null) {
//                _exerciseStartTime.value = System.currentTimeMillis()
//                Log.d("ViewModel", "Exercise started at: ${_exerciseStartTime.value} ms")
//            }
//        }
//        exerciseEvaluator.onCountdownTick = { tick ->
//            _countdownValue.value = tick
//        }
//    }
//
//    fun initializePoseDetectionHelper(context: Context) {
//        if (poseDetectionHelper == null) {
//            poseDetectionHelper = PoseDetectionHelper(context) { result, landmarks, angles ->
//                processPoseResult(result, landmarks, angles)
//            }
//        }
//    }
//
//    fun setExercise(exercise: Exercise) {
//        _currentExercise.value = exercise
//        exerciseEvaluator.setExercise(exercise)
//        _repCount.value = 0
//        repTimestamps.clear()
//        _exerciseStartTime.value = null
//        _feedbackMessage.value = "Waiting for user to get in position."
//    }
//
//    fun detectPose(bitmap: Bitmap) {
//        viewModelScope.launch {
//            poseDetectionHelper?.detect(bitmap)
//        }
//    }
//
//    private fun processPoseResult(
//        result: PoseLandmarkerResult,
//        landmarks: Map<String, Landmark>?,
//        angles: Map<String, Double>?
//    ) {
//        viewModelScope.launch {
//            _poseResult.value = result
//            exerciseEvaluator.evaluate(landmarks, angles)
//            angles?.let {
//                _anglesToDisplay.value = filterAnglesForDisplay(it)
//            }
//        }
//    }
//
//    private fun filterAnglesForDisplay(allAngles: Map<String, Double>): Map<String, Double> {
//        val exercise = _currentExercise.value ?: return allAngles
//        // --- FIX: Access keyJointsToTrack from the primaryMovement property ---
//        val relevantAngleNames = when (val movement = exercise.primaryMovement) {
//            is AngleMovement -> movement.keyJointsToTrack
//            else -> emptyList() // Distance-based movements don't have key angles to display
//        }
//        return allAngles.filterKeys { it in relevantAngleNames }
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        poseDetectionHelper?.close()
//    }
//}
