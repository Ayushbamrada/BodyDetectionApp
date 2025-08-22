package com.example.bodydetectionapp.ui.exercise

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bodydetectionapp.data.models.*
import com.example.bodydetectionapp.ml.ExerciseEvaluator
import com.example.bodydetectionapp.ml.ExerciseState
import com.example.bodydetectionapp.ml.PoseDetectionHelper
import com.example.bodydetectionapp.utils.VoiceAssistant
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExerciseTrackingViewModel(application: Application) : AndroidViewModel(application) {

    private var poseDetectionHelper: PoseDetectionHelper? = null
    private val exerciseEvaluator = ExerciseEvaluator()
    private val voiceAssistant = VoiceAssistant(application.applicationContext)

    private var lastSpokenMessage: String = ""
    private var lastSpokenTime: Long = 0L
    private val REMINDER_DELAY_MS = 3000L

    private var repGoal: Int = 0
    private var timeGoal: Int = 0
    private var exerciseTimer: CountDownTimer? = null

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
        exerciseEvaluator.onRepCompleted = { count ->
            _repCount.value = count
            if (_exerciseStartTime.value != null) {
                repTimestamps.add(RepTimestamp(count, System.currentTimeMillis()))
            }
            if (repGoal > 0 && count >= repGoal) {
                finishExercise("Goal Reached!")
            }
        }
        exerciseEvaluator.onFeedbackUpdate = { message ->
            speakMessage(message)
        }
        exerciseEvaluator.onStateChanged = { newState ->
            _exerciseState.value = newState
            if (newState == ExerciseState.IN_PROGRESS && _exerciseStartTime.value == null) {
                _exerciseStartTime.value = System.currentTimeMillis()
                startExerciseTimer()
            } else if (newState == ExerciseState.WAITING_TO_START) {
                speakMessage("Raise both hands to start", forceSpeak = true)
            }
        }
        exerciseEvaluator.onCountdownTick = { tick ->
            _countdownValue.value = tick
            speakMessage(tick.toString(), forceSpeak = true)
        }
    }

    private fun speakMessage(message: String, forceSpeak: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        _feedbackMessage.value = message

        if (message == "Keep Going!") return

        if (message.contains("Raise both hands") && _exerciseState.value != ExerciseState.WAITING_TO_START) {
            return
        }

        if (forceSpeak || message != lastSpokenMessage || (currentTime - lastSpokenTime) > REMINDER_DELAY_MS) {
            voiceAssistant.speak(message)
            lastSpokenMessage = message
            lastSpokenTime = currentTime
        }
    }

    fun initialize(exerciseName: String, repGoal: Int, timeGoal: Int) {
        this.repGoal = repGoal
        this.timeGoal = timeGoal
        val exercise = ExerciseDefinitions.ALL_EXERCISES.find { it.name == exerciseName }
        exercise?.let {
            _currentExercise.value = it
            exerciseEvaluator.setExercise(it)
            _feedbackMessage.value = "Get in position."
        }
        initializePoseDetectionHelper(getApplication())
    }

    private fun initializePoseDetectionHelper(context: Context) {
        if (poseDetectionHelper == null) {
            poseDetectionHelper = PoseDetectionHelper(context) { result, landmarks, angles ->
                processPoseResult(result, landmarks, angles)
            }
        }
    }

    fun detectPose(bitmap: Bitmap) {
        poseDetectionHelper?.detect(bitmap)
    }

    private fun processPoseResult(
        result: PoseLandmarkerResult,
        landmarks: Map<String, Landmark>?,
        angles: Map<String, Double>?
    ) {
        viewModelScope.launch {
            _poseResult.value = result
            exerciseEvaluator.evaluate(landmarks, angles)
            angles?.let {
                _anglesToDisplay.value = filterAnglesForDisplay(it)
            }
        }
    }

    private fun startExerciseTimer() {
        if (timeGoal > 0) {
            exerciseTimer = object : CountDownTimer(timeGoal * 1000L, 1000) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    finishExercise("Time's Up!")
                }
            }.start()
        }
    }

    fun finishExercise(reason: String) {
        if (_exerciseState.value != ExerciseState.FINISHED) {
            updateState(ExerciseState.FINISHED)
            voiceAssistant.speak(reason)
            exerciseTimer?.cancel()
            Log.d("ViewModel", "Exercise finished: $reason")
        }
    }

    private fun updateState(newState: ExerciseState) {
        _exerciseState.value = newState
    }

    private fun filterAnglesForDisplay(allAngles: Map<String, Double>): Map<String, Double> {
        val exercise = _currentExercise.value ?: return allAngles
        return when (val movement = exercise.primaryMovement) {
            is AngleMovement -> allAngles.filterKeys { it in movement.keyJointsToTrack }
            else -> emptyMap()
        }
    }

    override fun onCleared() {
        super.onCleared()
        poseDetectionHelper?.close()
        voiceAssistant.shutdown()
        exerciseTimer?.cancel()
    }
}
