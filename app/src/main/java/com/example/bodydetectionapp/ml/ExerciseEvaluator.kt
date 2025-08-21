package com.example.bodydetectionapp.ml

import android.os.CountDownTimer
import android.util.Log
import com.example.bodydetectionapp.data.models.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

enum class ExerciseState {
    NOT_STARTED,
    WAITING_TO_START,
    COUNTDOWN,
    IN_PROGRESS,
    FINISHED
}

sealed class RepetitionState {
    object IDLE : RepetitionState()
    object IN_PROGRESS : RepetitionState()
    object SIDE_VIEW_IN_PROGRESS : RepetitionState() // For single-side tracking
    object LEFT_SIDE_IN_PROGRESS : RepetitionState()
    object RIGHT_SIDE_IN_PROGRESS : RepetitionState()
}

class ExerciseEvaluator {

    var currentExercise: Exercise? = null
        private set
    var repCount: Int = 0
        private set
    var exerciseState: ExerciseState = ExerciseState.NOT_STARTED
        private set

    var onRepCompleted: ((Int) -> Unit)? = null
    var onFeedbackUpdate: ((String) -> Unit)? = null
    var onStateChanged: ((ExerciseState) -> Unit)? = null
    var onCountdownTick: ((Int) -> Unit)? = null

    private var repState: RepetitionState = RepetitionState.IDLE
    private var startGestureTimer: CountDownTimer? = null
    private val START_GESTURE_DURATION_MS = 1500L
    private var stepCount = 0
    private var visibleSide: String? = null // To store "LEFT" or "RIGHT"

    fun setExercise(exercise: Exercise) {
        currentExercise = exercise
        reset()
    }

    fun evaluate(landmarks: Map<String, Landmark>?, angles: Map<String, Double>?) {
        when (exerciseState) {
            ExerciseState.NOT_STARTED -> {
                landmarks?.let {
                    updateState(ExerciseState.WAITING_TO_START)
                    onFeedbackUpdate?.invoke("Raise both hands to start")
                }
            }
            ExerciseState.WAITING_TO_START -> {
                landmarks?.let { checkForStartGesture(it) } ?: cancelStartGestureTimer()
            }
            ExerciseState.IN_PROGRESS -> {
                if (landmarks != null && angles != null) {
                    trackRepetition(landmarks, angles)
                }
            }
            else -> { /* No action needed */ }
        }
    }

    private fun trackRepetition(landmarks: Map<String, Landmark>, angles: Map<String, Double>) {
        val exercise = currentExercise ?: return
        // --- NEW: Route to different logic based on camera view ---
        when (exercise.cameraView) {
            CameraView.FRONT -> trackFrontViewRep(landmarks, angles)
            CameraView.SIDE -> trackSideViewRep(landmarks, angles)
        }
    }

    private fun trackFrontViewRep(landmarks: Map<String, Landmark>, angles: Map<String, Double>) {
        val exercise = currentExercise!!
        when (exercise.type) {
            ExerciseType.SYMMETRICAL -> trackSymmetricalRep(landmarks, angles)
            ExerciseType.ALTERNATING -> trackAlternatingRep(landmarks, angles)
        }
    }

    // --- NEW: Logic specifically for side-view exercises ---
    private fun trackSideViewRep(landmarks: Map<String, Landmark>, angles: Map<String, Double>) {
        val exercise = currentExercise!!
        val movement = exercise.primaryMovement as? AngleMovement ?: return

        // Determine which side is visible if we don't know yet
        if (visibleSide == null) {
            visibleSide = getVisibleSide(landmarks)
        }
        val visibleSideNotNull = visibleSide ?: return // Exit if we can't determine the side

        // Get the angle for the visible side only
        val angleToTrack = angles["${visibleSideNotNull}_${movement.keyJointsToTrack[0].split(" ")[1]} Angle"] ?: return
        val isBending = movement.entryThreshold < movement.exitThreshold

        when (repState) {
            RepetitionState.IDLE -> {
                onFeedbackUpdate?.invoke("Begin ${exercise.name}")
                if ((isBending && angleToTrack < movement.entryThreshold) ||
                    (!isBending && angleToTrack > movement.entryThreshold)) {
                    repState = RepetitionState.SIDE_VIEW_IN_PROGRESS
                }
            }
            RepetitionState.SIDE_VIEW_IN_PROGRESS -> {
                onFeedbackUpdate?.invoke("Finish the movement")
                if ((isBending && angleToTrack > movement.exitThreshold) ||
                    (!isBending && angleToTrack < movement.exitThreshold)) {
                    stepCount++
                    repState = RepetitionState.IDLE
                }
            }
            else -> {}
        }

        // For side view, every 2 steps (assumed left/right) is one rep
        if (stepCount >= 2) {
            repCount++
            onRepCompleted?.invoke(repCount)
            stepCount = 0
        }
    }

    private fun getVisibleSide(landmarks: Map<String, Landmark>): String? {
        val leftHip = landmarks["LEFT_HIP"]?.z ?: return null
        val rightHip = landmarks["RIGHT_HIP"]?.z ?: return null
        // The side with the smaller Z value is closer to the camera
        return if (leftHip < rightHip) "LEFT" else "RIGHT"
    }


    private fun trackSymmetricalRep(landmarks: Map<String, Landmark>, angles: Map<String, Double>) {
        // This function remains the same
        val exercise = currentExercise!!
        val movement = exercise.primaryMovement as? AngleMovement ?: return
        val relevantAngles = movement.keyJointsToTrack.mapNotNull { angles[it] }
        if (relevantAngles.isEmpty()) return
        val currentAngle = relevantAngles.average()
        val isBendingMovement = movement.entryThreshold < movement.exitThreshold

        when (repState) {
            is RepetitionState.IDLE -> {
                if ((isBendingMovement && currentAngle < movement.entryThreshold) ||
                    (!isBendingMovement && currentAngle > movement.exitThreshold)) {
                    repState = RepetitionState.IN_PROGRESS
                }
            }
            is RepetitionState.IN_PROGRESS -> {
                if ((isBendingMovement && currentAngle > movement.exitThreshold) ||
                    (!isBendingMovement && currentAngle < movement.exitThreshold)) {
                    val formFeedback = checkFormRules(landmarks, angles)
                    if (formFeedback != null) {
                        onFeedbackUpdate?.invoke(formFeedback)
                        repState = RepetitionState.IDLE
                        return
                    }
                    repCount++
                    onRepCompleted?.invoke(repCount)
                    repState = RepetitionState.IDLE
                }
            }
            else -> {}
        }
    }

    private fun trackAlternatingRep(landmarks: Map<String, Landmark>, angles: Map<String, Double>) {
        // This function now only handles FRONT view alternating exercises
        val exercise = currentExercise!!
        val formFeedback = checkFormRules(landmarks, angles)
        if (formFeedback != null) {
            onFeedbackUpdate?.invoke(formFeedback)
            return
        }

        when (val movement = exercise.primaryMovement) {
            is AngleMovement -> trackAlternatingAngleRep(angles, movement)
            is DistanceMovement -> trackAlternatingDistanceRep(landmarks, movement)
        }
    }

    private fun trackAlternatingAngleRep(angles: Map<String, Double>, movement: AngleMovement) {
        // This function remains the same
        val leftAngle = angles[movement.keyJointsToTrack[0]] ?: return
        val rightAngle = angles[movement.keyJointsToTrack[1]] ?: return
        val isBending = movement.entryThreshold < movement.exitThreshold

        when (repState) {
            RepetitionState.IDLE -> {
                if (isBending && leftAngle < movement.entryThreshold) repState = RepetitionState.LEFT_SIDE_IN_PROGRESS
                else if (isBending && rightAngle < movement.entryThreshold) repState = RepetitionState.RIGHT_SIDE_IN_PROGRESS
            }
            RepetitionState.LEFT_SIDE_IN_PROGRESS -> {
                if (isBending && leftAngle > movement.exitThreshold) {
                    stepCount++
                    repState = RepetitionState.IDLE
                }
            }
            RepetitionState.RIGHT_SIDE_IN_PROGRESS -> {
                if (isBending && rightAngle > movement.exitThreshold) {
                    stepCount++
                    repState = RepetitionState.IDLE
                }
            }
            else -> {}
        }

        if (stepCount >= 2) {
            repCount++
            onRepCompleted?.invoke(repCount)
            stepCount = 0
        }
    }

    private fun trackAlternatingDistanceRep(landmarks: Map<String, Landmark>, movement: DistanceMovement) {
        // This function remains the same
        val shoulderWidth = abs((landmarks["LEFT_SHOULDER"]?.x ?: 0f) - (landmarks["RIGHT_SHOULDER"]?.x ?: 1f))
        if (shoulderWidth == 0f) return

        val leftCrunchDist = getDistance(landmarks["RIGHT_ELBOW"], landmarks["LEFT_KNEE"]) / shoulderWidth
        val rightCrunchDist = getDistance(landmarks["LEFT_ELBOW"], landmarks["RIGHT_KNEE"]) / shoulderWidth
        val leftObliqueDist = getDistance(landmarks["LEFT_ELBOW"], landmarks["LEFT_KNEE"]) / shoulderWidth
        val rightObliqueDist = getDistance(landmarks["RIGHT_ELBOW"], landmarks["RIGHT_KNEE"]) / shoulderWidth

        val (leftDist, rightDist) = when (currentExercise?.name) {
            "Bicycle Crunches" -> Pair(leftCrunchDist, rightCrunchDist)
            "Standing Oblique Crunches" -> Pair(leftObliqueDist, rightObliqueDist)
            else -> Pair(Float.MAX_VALUE, Float.MAX_VALUE)
        }

        when (repState) {
            RepetitionState.IDLE -> {
                if (leftDist < movement.entryThreshold) repState = RepetitionState.LEFT_SIDE_IN_PROGRESS
                else if (rightDist < movement.entryThreshold) repState = RepetitionState.RIGHT_SIDE_IN_PROGRESS
            }
            RepetitionState.LEFT_SIDE_IN_PROGRESS -> {
                if (leftDist > movement.exitThreshold) {
                    stepCount++
                    repState = RepetitionState.IDLE
                }
            }
            RepetitionState.RIGHT_SIDE_IN_PROGRESS -> {
                if (rightDist > movement.exitThreshold) {
                    stepCount++
                    repState = RepetitionState.IDLE
                }
            }
            else -> {}
        }

        if (stepCount >= 2) {
            repCount++
            onRepCompleted?.invoke(repCount)
            stepCount = 0
        }
    }

    private fun getDistance(lm1: Landmark?, lm2: Landmark?): Float {
        if (lm1 == null || lm2 == null) return Float.MAX_VALUE
        val dx = lm1.x - lm2.x
        val dy = lm1.y - lm2.y
        return sqrt(dx.pow(2) + dy.pow(2))
    }

    private fun checkFormRules(landmarks: Map<String, Landmark>, angles: Map<String, Double>): String? {
        // This function remains mostly the same
        val exercise = currentExercise ?: return null
        if (repState == RepetitionState.IDLE) return null

        for (rule in exercise.formRules) {
            when (rule) {
                is AngleRule -> {
                    val angle = angles[rule.angleName] ?: continue
                    if (angle < rule.minAngle || angle > rule.maxAngle) {
                        return rule.feedbackMessage
                    }
                }
                is HorizontalAlignmentRule -> {
                    val lm1 = landmarks[rule.landmark1] ?: continue
                    val lm2 = landmarks[rule.landmark2] ?: continue
                    val shoulderWidth = abs((landmarks["LEFT_SHOULDER"]?.x ?: 0f) - (landmarks["RIGHT_SHOULDER"]?.x ?: 1f))
                    if (shoulderWidth == 0f) continue
                    val horizontalDistance = abs(lm1.x - lm2.x)
                    if (horizontalDistance / shoulderWidth > rule.maxDistanceRatio) {
                        return rule.feedbackMessage
                    }
                }
                is DistanceRule -> {
                    val shoulderWidth = abs((landmarks["LEFT_SHOULDER"]?.x ?: 0f) - (landmarks["RIGHT_SHOULDER"]?.x ?: 1f))
                    if (shoulderWidth == 0f) continue
                    val checkLeft = (repState == RepetitionState.LEFT_SIDE_IN_PROGRESS)
                    val checkRight = (repState == RepetitionState.RIGHT_SIDE_IN_PROGRESS)
                    if (checkLeft) {
                        val lm1 = landmarks["LEFT_${rule.landmark1}"]
                        val lm2 = landmarks["LEFT_${rule.landmark2}"]
                        val distance = getDistance(lm1, lm2) / shoulderWidth
                        if (distance > rule.maxDistanceRatio) {
                            return rule.feedbackMessage
                        }
                    }
                    if (checkRight) {
                        val lm1 = landmarks["RIGHT_${rule.landmark1}"]
                        val lm2 = landmarks["RIGHT_${rule.landmark2}"]
                        val distance = getDistance(lm1, lm2) / shoulderWidth
                        if (distance > rule.maxDistanceRatio) {
                            return rule.feedbackMessage
                        }
                    }
                }
            }
        }
        return null
    }

    // --- Other helper functions (checkForStartGesture, etc.) are unchanged ---
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
        }
    }

    private fun cancelStartGestureTimer() {
        if (startGestureTimer != null) {
            startGestureTimer?.cancel()
            startGestureTimer = null
            onFeedbackUpdate?.invoke("Raise both hands to start")
        }
    }

    fun reset() {
        cancelStartGestureTimer()
        repCount = 0
        stepCount = 0
        visibleSide = null // Reset visible side
        repState = RepetitionState.IDLE
        updateState(ExerciseState.NOT_STARTED)
        onRepCompleted?.invoke(repCount)
    }
}
