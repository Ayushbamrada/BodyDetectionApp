package com.example.bodydetectionapp.ml

import android.os.CountDownTimer
import android.util.Log
import com.example.bodydetectionapp.data.models.Exercise
import com.example.bodydetectionapp.data.models.Landmark
import com.example.bodydetectionapp.data.models.RepetitionState

/**
 * An enum to manage the overall state of the exercise session.
 */
enum class ExerciseState {
    NOT_STARTED,      // No exercise is active, waiting for user to get in frame.
    WAITING_TO_START, // User is visible, waiting for the "hands up" start gesture.
    COUNTDOWN,        // The 3-2-1 countdown is in progress.
    IN_PROGRESS,      // The exercise is actively being tracked.
    FINISHED          // The session is over.
}

/**
 * A new, intelligent evaluator that uses gesture detection to start and
 * cyclical motion tracking to count repetitions.
 */
class ExerciseEvaluator {

    // MARK: - Public State
    var currentExercise: Exercise? = null
        private set

    var repCount: Int = 0
        private set

    var exerciseState: ExerciseState = ExerciseState.NOT_STARTED
        private set

    // MARK: - Callbacks
    var onRepCompleted: ((Int) -> Unit)? = null
    var onFeedbackUpdate: ((String) -> Unit)? = null
    var onStateChanged: ((ExerciseState) -> Unit)? = null // For UI to react to state changes
    var onCountdownTick: ((Int) -> Unit)? = null // For the countdown UI

    // MARK: - Private State
    private var repState: RepetitionState = RepetitionState.IDLE
    private var startGestureTimer: CountDownTimer? = null
    private val START_GESTURE_DURATION_MS = 1500L // Hold hands up for 1.5 seconds

    /**
     * Sets the exercise and resets the entire state machine.
     */
    fun setExercise(exercise: Exercise) {
        currentExercise = exercise
        reset()
        Log.d("ExerciseEvaluator", "Exercise set to: ${exercise.name}. Waiting for user.")
    }

    /**
     * The main evaluation function, called for every frame from the camera.
     *
     * @param landmarks A map of landmark names to their 3D coordinates.
     * @param angles A map of calculated joint angles for the current frame.
     */
    fun evaluate(landmarks: Map<String, Landmark>?, angles: Map<String, Double>?) {
        // If landmarks or angles are null, the user is not fully visible.
        // We handle this inside the state machine now.
        when (exerciseState) {
            ExerciseState.NOT_STARTED -> {
                // Once the user is visible (landmarks are not null), we can move to the next state.
                landmarks?.let {
                    updateState(ExerciseState.WAITING_TO_START)
                    onFeedbackUpdate?.invoke("Raise both hands to start")
                }
            }
            ExerciseState.WAITING_TO_START -> {
                // Use a safe call. Only check for gesture if landmarks are available.
                landmarks?.let {
                    checkForStartGesture(it)
                } ?: cancelStartGestureTimer() // If user disappears, cancel the timer.
            }
            ExerciseState.IN_PROGRESS -> {
                // Use a safe call. Only track reps if angles are available.
                angles?.let {
                    trackRepetition(it)
                }
            }
            ExerciseState.COUNTDOWN, ExerciseState.FINISHED -> {
                // No action needed in these states, they are managed by timers/events.
            }
        }
    }

    /**
     * Checks if the user is performing the "hands up" gesture.
     */
    private fun checkForStartGesture(landmarks: Map<String, Landmark>) {
        val leftWrist = landmarks["LEFT_WRIST"]
        val rightWrist = landmarks["RIGHT_WRIST"]
        val leftShoulder = landmarks["LEFT_SHOULDER"]
        val rightShoulder = landmarks["RIGHT_SHOULDER"]

        if (leftWrist != null && rightWrist != null && leftShoulder != null && rightShoulder != null) {
            val handsAreUp = leftWrist.y < leftShoulder.y && rightWrist.y < rightShoulder.y
            if (handsAreUp) {
                if (startGestureTimer == null) {
                    onFeedbackUpdate?.invoke("Hold it...")
                    startGestureTimer = object : CountDownTimer(START_GESTURE_DURATION_MS, 1000) {
                        override fun onTick(millisUntilFinished: Long) {}
                        override fun onFinish() {
                            startExerciseCountdown()
                            startGestureTimer = null
                        }
                    }.start()
                }
            } else {
                cancelStartGestureTimer()
            }
        }
    }

    /**
     * Tracks the cyclical motion of an exercise to count reps.
     */
    private fun trackRepetition(angles: Map<String, Double>) {
        val exercise = currentExercise ?: return
        val repCounter = exercise.repCounter

        val relevantAngles = repCounter.keyJointsToTrack.mapNotNull { angles[it] }
        if (relevantAngles.isEmpty()) return
        val currentAngle = relevantAngles.average()

        val isBendingMovement = repCounter.entryThreshold < repCounter.exitThreshold

        when (repState) {
            RepetitionState.IDLE -> {
                onFeedbackUpdate?.invoke("Begin ${exercise.name}")
                if ((isBendingMovement && currentAngle < repCounter.entryThreshold) ||
                    (!isBendingMovement && currentAngle > repCounter.entryThreshold)) {
                    repState = RepetitionState.IN_PROGRESS
                }
            }
            RepetitionState.IN_PROGRESS -> {
                onFeedbackUpdate?.invoke("Finish the movement")
                if ((isBendingMovement && currentAngle > repCounter.exitThreshold) ||
                    (!isBendingMovement && currentAngle < repCounter.exitThreshold)) {
                    repCount++
                    onRepCompleted?.invoke(repCount)
                    repState = RepetitionState.IDLE
                }
            }
        }
    }

    private fun startExerciseCountdown() {
        updateState(ExerciseState.COUNTDOWN)
        object : CountDownTimer(3500, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val second = (millisUntilFinished / 1000).toInt()
                if (second > 0) onCountdownTick?.invoke(second)
            }
            override fun onFinish() {
                updateState(ExerciseState.IN_PROGRESS)
            }
        }.start()
    }

    private fun updateState(newState: ExerciseState) {
        if (exerciseState != newState) {
            exerciseState = newState
            onStateChanged?.invoke(newState)
            Log.d("ExerciseEvaluator", "State changed to: $newState")
        }
    }

    private fun cancelStartGestureTimer() {
        if (startGestureTimer != null) {
            startGestureTimer?.cancel()
            startGestureTimer = null
            onFeedbackUpdate?.invoke("Raise both hands to start")
        }
    }

    /**
     * Resets the evaluator to its initial state.
     */
    fun reset() {
        cancelStartGestureTimer()
        repCount = 0
        repState = RepetitionState.IDLE
        updateState(ExerciseState.NOT_STARTED)
        onRepCompleted?.invoke(repCount)
    }
}
