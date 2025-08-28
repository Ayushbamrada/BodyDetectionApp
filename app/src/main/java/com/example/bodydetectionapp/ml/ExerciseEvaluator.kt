//package com.example.bodydetectionapp.ml
//
//import android.os.CountDownTimer
//import android.util.Log
//import com.example.bodydetectionapp.data.models.*
//import kotlin.math.abs
//import kotlin.math.sqrt
//
//enum class ExerciseState {
//    NOT_STARTED,
//    WAITING_TO_START,
//    COUNTDOWN,
//    IN_PROGRESS,
//    FINISHED
//}
//
//sealed class RepetitionState {
//    object IDLE : RepetitionState()
//    object IN_PROGRESS : RepetitionState()
//    object SIDE_VIEW_IN_PROGRESS : RepetitionState()
//    object LEFT_SIDE_IN_PROGRESS : RepetitionState()
//    object RIGHT_SIDE_IN_PROGRESS : RepetitionState()
//}
//
//class ExerciseEvaluator {
//
//    var currentExercise: Exercise? = null
//        private set
//    var repCount: Int = 0
//        private set
//    var exerciseState: ExerciseState = ExerciseState.NOT_STARTED
//        private set
//
//    // Callbacks to communicate with the ViewModel
//    var onRepCompleted: ((Int) -> Unit)? = null
//    var onFeedbackUpdate: ((String) -> Unit)? = null
//    var onStateChanged: ((ExerciseState) -> Unit)? = null
//    var onCountdownTick: ((Int) -> Unit)? = null
//
//    private var repState: RepetitionState = RepetitionState.IDLE
//
//    // --- SMART FEEDBACK LOGIC ---
//    private val spokenFormErrors = mutableSetOf<String>()
//    private var idleTimer: CountDownTimer? = null
//    private val IDLE_THRESHOLD_MS = 4000L
//    private val VISIBILITY_THRESHOLD = 0.4f
//
//    // --- OTHER PROPERTIES ---
//    private var startGestureTimer: CountDownTimer? = null
//    private val START_GESTURE_DURATION_MS = 1500L
//    private var stepCount = 0
//    private var visibleSide: String? = null
//
//    fun setExercise(exercise: Exercise) {
//        currentExercise = exercise
//        reset()
//    }
//
//    fun evaluate(landmarks: Map<String, Landmark>?, angles: Map<String, Double>?) {
//        when (exerciseState) {
//            ExerciseState.NOT_STARTED -> landmarks?.let { updateState(ExerciseState.WAITING_TO_START) }
//            ExerciseState.WAITING_TO_START -> landmarks?.let { checkForStartGesture(it) } ?: cancelStartGestureTimer()
//            ExerciseState.IN_PROGRESS -> if (landmarks != null && angles != null) trackRepetition(landmarks, angles)
//            else -> { /* No action needed */ }
//        }
//    }
//
//    private fun trackRepetition(landmarks: Map<String, Landmark>, angles: Map<String, Double>) {
//        val exercise = currentExercise ?: return
//
////        if (!checkLandmarkVisibility(landmarks)) {
////            onFeedbackUpdate?.invoke(exercise.visibilityFeedbackMessage)
////            resetRepState()
////            return
////        }
//        //new logic
//        val isVisible = if (exercise.cameraView == CameraView.SIDE) {
//            checkSideViewLandmarkVisibility(landmarks)
//        } else {
//            checkFrontViewLandmarkVisibility(landmarks)
//        }
//
//        if (!isVisible) {
//            onFeedbackUpdate?.invoke(exercise.visibilityFeedbackMessage)
//            resetRepState()
//            return
//        }
//
//        val newFormErrors = checkFormRules(landmarks, angles)
//        if (newFormErrors.isNotEmpty()) {
//            onFeedbackUpdate?.invoke(newFormErrors.joinToString(". "))
//            startIdleTimer()
//            return
//        }
//
//        val repWasCompleted = processRepetitionMovement(landmarks, angles)
//        if (repWasCompleted) {
//            return
//        }
//
//        if (repState != RepetitionState.IDLE) {
//            startIdleTimer()
//        } else {
//            cancelIdleTimer()
//        }
//    }
//
////    private fun checkLandmarkVisibility(landmarks: Map<String, Landmark>): Boolean {
////        val required = currentExercise?.requiredLandmarks ?: return true
////        if (required.isEmpty()) return true
////
////        // Log the start of the check for a new frame
////        Log.d("VISIBILITY_DEBUG", "--- New Frame Check ---")
////
////        for (landmarkName in required) {
////            val landmark = landmarks[landmarkName]
////            val visibility = landmark?.visibility
////
////            if (landmark == null) {
////                // Log if a required landmark is completely missing from the data
////                Log.e("VISIBILITY_DEBUG", "Landmark '$landmarkName' is MISSING from the map.")
////                return false
////            }
////
////            if (visibility == null) {
////                // Log if the landmark exists but has no visibility score
////                Log.e("VISIBILITY_DEBUG", "Landmark '$landmarkName' has NULL visibility.")
////                return false
////            }
////
////            // Log the visibility score for every required landmark
////            Log.d("VISIBILITY_DEBUG", "Checking Landmark: '$landmarkName', Visibility: $visibility")
////
////            if (visibility < VISIBILITY_THRESHOLD) {
////                // Log which specific landmark failed the check
////                Log.w("VISIBILITY_DEBUG", "FAILED CHECK: '$landmarkName' with visibility $visibility is below threshold $VISIBILITY_THRESHOLD")
////                return false
////            }
////        }
////
////        // Log if all checks passed for this frame
////        Log.i("VISIBILITY_DEBUG", "SUCCESS: All required landmarks are visible.")
////        return true
////    }
////new functions here
//// This is the standard, strict check for front-facing exercises
//private fun checkFrontViewLandmarkVisibility(landmarks: Map<String, Landmark>): Boolean {
//    val required = currentExercise?.requiredLandmarks ?: return true
//    if (required.isEmpty()) return true
//
//    for (landmarkName in required) {
//        val landmark = landmarks[landmarkName]
//        if (landmark == null || (landmark.visibility ?: 0f) < VISIBILITY_THRESHOLD) {
//            Log.w("VISIBILITY_DEBUG", "FRONT VIEW FAILED: '$landmarkName' visibility is too low.")
//            return false
//        }
//    }
//    return true
//}
//
//    // --- NEW: A more forgiving check specifically for side-view exercises ---
//    private fun checkSideViewLandmarkVisibility(landmarks: Map<String, Landmark>): Boolean {
//        val required = currentExercise?.requiredLandmarks ?: return true
//        if (required.isEmpty()) return true
//
//        // For side view, we only need EITHER the left OR the right side to be visible.
//        val leftLandmarks = required.filter { it.startsWith("LEFT_") }
//        val rightLandmarks = required.filter { it.startsWith("RIGHT_") }
//
//        val isLeftVisible = leftLandmarks.all { landmarkName ->
//            val landmark = landmarks[landmarkName]
//            (landmark?.visibility ?: 0f) >= VISIBILITY_THRESHOLD
//        }
//
//        val isRightVisible = rightLandmarks.all { landmarkName ->
//            val landmark = landmarks[landmarkName]
//            (landmark?.visibility ?: 0f) >= VISIBILITY_THRESHOLD
//        }
//
//        if (!isLeftVisible && !isRightVisible) {
//            Log.w("VISIBILITY_DEBUG", "SIDE VIEW FAILED: Neither left nor right side landmarks are visible.")
//        }
//
//        return isLeftVisible || isRightVisible
//    }
//
//    private fun processRepetitionMovement(landmarks: Map<String, Landmark>, angles: Map<String, Double>): Boolean {
//        val previousRepCount = repCount
//        when (currentExercise!!.cameraView) {
//            CameraView.FRONT -> trackFrontViewRep(landmarks, angles)
//            CameraView.SIDE -> trackSideViewRep(landmarks, angles)
//        }
//        return repCount > previousRepCount
//    }
//
//    private fun checkFormRules(landmarks: Map<String, Landmark>, angles: Map<String, Double>): List<String> {
//        val exercise = currentExercise ?: return emptyList()
//        if (repState == RepetitionState.IDLE) return emptyList()
//
//        val newFeedbackMessages = mutableListOf<String>()
//
//        for (rule in exercise.formRules) {
//            var isBroken = false
//            when (rule) {
//                is AngleRule -> {
//                    val angle = angles[rule.angleName]
//                    if (angle != null && (angle < rule.minAngle || angle > rule.maxAngle)) {
//                        isBroken = true
//                    }
//                }
//                is HorizontalAlignmentRule -> {
//                    val lm1 = landmarks[rule.landmark1] ?: continue
//                    val lm2 = landmarks[rule.landmark2] ?: continue
//                    val shoulderWidth = abs((landmarks["LEFT_SHOULDER"]?.x ?: 0f) - (landmarks["RIGHT_SHOULDER"]?.x ?: 1f))
//                    if (shoulderWidth == 0f) continue
//                    val horizontalDistance = abs(lm1.x - lm2.x)
//                    if (horizontalDistance / shoulderWidth > rule.maxDistanceRatio) {
//                        isBroken = true
//                    }
//                }
//                is DistanceRule -> {
//                    val shoulderWidth = abs((landmarks["LEFT_SHOULDER"]?.x ?: 0f) - (landmarks["RIGHT_SHOULDER"]?.x ?: 1f))
//                    if (shoulderWidth == 0f) continue
//                    val lm1 = landmarks[rule.landmark1] ?: continue
//                    val lm2 = landmarks[rule.landmark2] ?: continue
//                    val distance = getDistance(lm1, lm2) / shoulderWidth
//                    if (distance > rule.maxDistanceRatio) {
//                        isBroken = true
//                    }
//                }
//            }
//
//            if (isBroken && !spokenFormErrors.contains(rule.feedbackMessage)) {
//                newFeedbackMessages.add(rule.feedbackMessage)
//                spokenFormErrors.add(rule.feedbackMessage)
//            }
//        }
//        return newFeedbackMessages
//    }
//
//    private fun startIdleTimer() {
//        idleTimer?.cancel()
//        idleTimer = object : CountDownTimer(IDLE_THRESHOLD_MS, 1000) {
//            override fun onTick(millisUntilFinished: Long) {}
//            override fun onFinish() {
//                currentExercise?.primaryMovementInstruction?.let {
//                    onFeedbackUpdate?.invoke(it)
//                }
//            }
//        }.start()
//    }
//
//    private fun cancelIdleTimer() {
//        idleTimer?.cancel()
//        idleTimer = null
//    }
//
//    private fun resetRepState() {
//        repState = RepetitionState.IDLE
//        spokenFormErrors.clear()
//        cancelIdleTimer()
//    }
//
//    private fun trackFrontViewRep(landmarks: Map<String, Landmark>, angles: Map<String, Double>) {
//        val exercise = currentExercise!!
//        when (exercise.type) {
//            ExerciseType.SYMMETRICAL -> trackSymmetricalRep(landmarks, angles)
//            ExerciseType.ALTERNATING -> trackAlternatingRep(landmarks, angles)
//        }
//    }
//
//    // In ExerciseEvaluator.kt, replace the old trackSymmetricalRep with this new one.
//
//    private fun trackSymmetricalRep(landmarks: Map<String, Landmark>, angles: Map<String, Double>) {
//        val exercise = currentExercise!!
//        val movement = exercise.primaryMovement as? AngleMovement ?: return
//        val relevantAngles = movement.keyJointsToTrack.mapNotNull { angles[it] }
//        if (relevantAngles.size != movement.keyJointsToTrack.size) return // Ensure all joints are tracked
//
//        // For an arm raise or sit-to-stand, entry > exit. This is an "extending" movement.
//        // For a squat, entry < exit. This is a "bending" movement.
//        val isExtendingMovement = movement.entryThreshold > movement.exitThreshold
//
//        if (isExtendingMovement) {
//            // LOGIC FOR MOVEMENTS LIKE ARM RAISES (going from low angle to high angle)
//            when (repState) {
//                is RepetitionState.IDLE -> {
//                    // To start a rep, both arms must be in the "down" position (below exitThreshold).
//                    if (relevantAngles.all { it < movement.exitThreshold }) {
//                        repState = RepetitionState.IN_PROGRESS
//                    }
//                }
//                is RepetitionState.IN_PROGRESS -> {
//                    // To complete the rep, both arms must now go to the "up" position (above entryThreshold).
//                    if (relevantAngles.all { it > movement.entryThreshold }) {
//                        repCount++
//                        onRepCompleted?.invoke(repCount)
//                        resetRepState()
//                    }
//                }
//                else -> {}
//            }
//        } else {
//            // LOGIC FOR MOVEMENTS LIKE SQUATS (going from high angle to low angle)
//            when (repState) {
//                is RepetitionState.IDLE -> {
//                    // To start a rep, both legs must be in the "up" position (above exitThreshold).
//                    if (relevantAngles.all { it > movement.exitThreshold }) {
//                        repState = RepetitionState.IN_PROGRESS
//                    }
//                }
//                is RepetitionState.IN_PROGRESS -> {
//                    // To complete the rep, both legs must now go to the "down" position (below entryThreshold).
//                    if (relevantAngles.all { it < movement.entryThreshold }) {
//                        repCount++
//                        onRepCompleted?.invoke(repCount)
//                        resetRepState()
//                    }
//                }
//                else -> {}
//            }
//        }
//    }
//
//    // --- ADDED BACK THE MISSING FUNCTIONS ---
//
//    private fun trackSideViewRep(landmarks: Map<String, Landmark>, angles: Map<String, Double>) {
//        val exercise = currentExercise!!
//        val movement = exercise.primaryMovement as? AngleMovement ?: return
//
//        if (visibleSide == null) {
//            visibleSide = getVisibleSide(landmarks)
//        }
//        val visibleSideNotNull = visibleSide ?: return
//
//        val angleNameToTrack = movement.keyJointsToTrack.firstOrNull { it.startsWith(visibleSideNotNull, ignoreCase = true) } ?: return
//        val angleToTrack = angles[angleNameToTrack] ?: return
//        val isBending = movement.entryThreshold < movement.exitThreshold
//
//        when (repState) {
//            RepetitionState.IDLE -> {
//                if ((isBending && angleToTrack < movement.entryThreshold) ||
//                    (!isBending && angleToTrack > movement.exitThreshold)) {
//                    repState = RepetitionState.SIDE_VIEW_IN_PROGRESS
//                }
//            }
//            RepetitionState.SIDE_VIEW_IN_PROGRESS -> {
//                if ((isBending && angleToTrack > movement.exitThreshold) ||
//                    (!isBending && angleToTrack < movement.exitThreshold)) {
//                    repCount++
//                    onRepCompleted?.invoke(repCount)
//                    resetRepState()
//                }
//            }
//            else -> {}
//        }
//    }
//
//    private fun trackAlternatingRep(landmarks: Map<String, Landmark>, angles: Map<String, Double>) {
//        when (val movement = currentExercise!!.primaryMovement) {
//            is AngleMovement -> trackAlternatingAngleRep(angles, movement)
//            is DistanceMovement -> trackAlternatingDistanceRep(landmarks, movement)
//        }
//    }
//
//    private fun trackAlternatingAngleRep(angles: Map<String, Double>, movement: AngleMovement) {
//        val leftAngle = angles[movement.keyJointsToTrack[0]] ?: return
//        val rightAngle = angles[movement.keyJointsToTrack[1]] ?: return
//        val isBending = movement.entryThreshold < movement.exitThreshold
//
//        when (repState) {
//            RepetitionState.IDLE -> {
//                if (isBending && leftAngle < movement.entryThreshold) repState = RepetitionState.LEFT_SIDE_IN_PROGRESS
//                else if (isBending && rightAngle < movement.entryThreshold) repState = RepetitionState.RIGHT_SIDE_IN_PROGRESS
//            }
//            RepetitionState.LEFT_SIDE_IN_PROGRESS -> {
//                if (isBending && leftAngle > movement.exitThreshold) {
//                    stepCount++
//                    resetRepState()
//                }
//            }
//            RepetitionState.RIGHT_SIDE_IN_PROGRESS -> {
//                if (isBending && rightAngle > movement.exitThreshold) {
//                    stepCount++
//                    resetRepState()
//                }
//            }
//            else -> {}
//        }
//
//        if (stepCount >= 2) {
//            repCount++
//            onRepCompleted?.invoke(repCount)
//            stepCount = 0
//        }
//    }
//
//    private fun trackAlternatingDistanceRep(landmarks: Map<String, Landmark>, movement: DistanceMovement) {
//        val shoulderWidth = abs((landmarks["LEFT_SHOULDER"]?.x ?: 0f) - (landmarks["RIGHT_SHOULDER"]?.x ?: 1f))
//        if (shoulderWidth == 0f) return
//
//        val leftCrunchDist = getDistance(landmarks["RIGHT_ELBOW"], landmarks["LEFT_KNEE"]) / shoulderWidth
//        val rightCrunchDist = getDistance(landmarks["LEFT_ELBOW"], landmarks["RIGHT_KNEE"]) / shoulderWidth
//        val leftObliqueDist = getDistance(landmarks["LEFT_ELBOW"], landmarks["LEFT_KNEE"]) / shoulderWidth
//        val rightObliqueDist = getDistance(landmarks["RIGHT_ELBOW"], landmarks["RIGHT_KNEE"]) / shoulderWidth
//
//        val (leftDist, rightDist) = when (currentExercise?.name) {
//            "Bicycle Crunches" -> Pair(leftCrunchDist, rightCrunchDist)
//            "Standing Oblique Crunches" -> Pair(leftObliqueDist, rightObliqueDist)
//            else -> Pair(Float.MAX_VALUE, Float.MAX_VALUE)
//        }
//
//        when (repState) {
//            RepetitionState.IDLE -> {
//                if (leftDist < movement.entryThreshold) repState = RepetitionState.LEFT_SIDE_IN_PROGRESS
//                else if (rightDist < movement.entryThreshold) repState = RepetitionState.RIGHT_SIDE_IN_PROGRESS
//            }
//            RepetitionState.LEFT_SIDE_IN_PROGRESS -> {
//                if (leftDist > movement.exitThreshold) {
//                    stepCount++
//                    resetRepState()
//                }
//            }
//            RepetitionState.RIGHT_SIDE_IN_PROGRESS -> {
//                if (rightDist > movement.exitThreshold) {
//                    stepCount++
//                    resetRepState()
//                }
//            }
//            else -> {}
//        }
//
//        if (stepCount >= 2) {
//            repCount++
//            onRepCompleted?.invoke(repCount)
//            stepCount = 0
//        }
//    }
//
//    private fun getDistance(lm1: Landmark?, lm2: Landmark?): Float {
//        if (lm1 == null || lm2 == null) return Float.MAX_VALUE
//        val dx = lm1.x - lm2.x
//        val dy = lm1.y - lm2.y
//        return sqrt((dx * dx) + (dy * dy))
//    }
//
//    private fun getVisibleSide(landmarks: Map<String, Landmark>): String? {
//        val leftHipZ = landmarks["LEFT_HIP"]?.z ?: return null
//        val rightHipZ = landmarks["RIGHT_HIP"]?.z ?: return null
//        return if (leftHipZ < rightHipZ) "LEFT" else "RIGHT"
//    }
//
//    // --- GESTURE AND STATE FUNCTIONS ---
//
//    private fun checkForStartGesture(landmarks: Map<String, Landmark>) {
//        val leftWrist = landmarks["LEFT_WRIST"]
//        val rightWrist = landmarks["RIGHT_WRIST"]
//        val leftShoulder = landmarks["LEFT_SHOULDER"]
//        val rightShoulder = landmarks["RIGHT_SHOULDER"]
//
//        if (leftWrist != null && rightWrist != null && leftShoulder != null && rightShoulder != null) {
//            if (leftWrist.y < leftShoulder.y && rightWrist.y < rightShoulder.y) {
//                if (startGestureTimer == null) {
//                    startGestureTimer = object : CountDownTimer(START_GESTURE_DURATION_MS, 1000) {
//                        override fun onTick(millisUntilFinished: Long) {}
//                        override fun onFinish() {
//                            startExerciseCountdown()
//                            startGestureTimer = null
//                        }
//                    }.start()
//                }
//            } else {
//                cancelStartGestureTimer()
//            }
//        }
//    }
//
//    private fun cancelStartGestureTimer() {
//        startGestureTimer?.cancel()
//        startGestureTimer = null
//    }
//
//    private fun startExerciseCountdown() {
//        updateState(ExerciseState.COUNTDOWN)
//        object : CountDownTimer(3500, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                val second = (millisUntilFinished / 1000).toInt()
//                if (second > 0) onCountdownTick?.invoke(second)
//            }
//            override fun onFinish() {
//                updateState(ExerciseState.IN_PROGRESS)
//            }
//        }.start()
//    }
//
//    private fun updateState(newState: ExerciseState) {
//        if (exerciseState != newState) {
//            exerciseState = newState
//            onStateChanged?.invoke(newState)
//        }
//    }
//
//    fun reset() {
//        cancelStartGestureTimer()
//        cancelIdleTimer()
//        repCount = 0
//        stepCount = 0
//        visibleSide = null
//        repState = RepetitionState.IDLE
//        spokenFormErrors.clear()
//        updateState(ExerciseState.NOT_STARTED)
////        onRepCompleted?.invoke(repCount)
//    }
//}


package com.example.bodydetectionapp.ml

import android.os.CountDownTimer
import android.util.Log
import com.example.bodydetectionapp.data.models.*
import kotlin.math.abs
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
    private val spokenFormErrors = mutableSetOf<String>()
    private var idleTimer: CountDownTimer? = null
    private val IDLE_THRESHOLD_MS = 4000L
    private val VISIBILITY_THRESHOLD = 0.4f

    private var startGestureTimer: CountDownTimer? = null
    private val START_GESTURE_DURATION_MS = 1500L
    private var stepCount = 0

    fun setExercise(exercise: Exercise) {
        currentExercise = exercise
        reset()
    }

    fun evaluate(landmarks: Map<String, Landmark>?, angles: Map<String, Double>?) {
        when (exerciseState) {
            ExerciseState.NOT_STARTED -> landmarks?.let { updateState(ExerciseState.WAITING_TO_START) }
            ExerciseState.WAITING_TO_START -> landmarks?.let { checkForStartGesture(it) } ?: cancelStartGestureTimer()
            ExerciseState.IN_PROGRESS -> if (landmarks != null && angles != null) trackRepetition(landmarks, angles)
            else -> { /* No action needed */ }
        }
    }

    private fun trackRepetition(landmarks: Map<String, Landmark>, angles: Map<String, Double>) {
        val repWasCompleted = processRepetitionMovement(landmarks, angles)
        if (repWasCompleted) {
            return
        }

        val newFormErrors = checkFormRules(landmarks, angles)
        if (newFormErrors.isNotEmpty()) {
            onFeedbackUpdate?.invoke(newFormErrors.joinToString(". "))
            startIdleTimer()
            return
        }

        if (repState != RepetitionState.IDLE) {
            startIdleTimer()
        } else {
            cancelIdleTimer()
        }
    }

    private fun processRepetitionMovement(landmarks: Map<String, Landmark>, angles: Map<String, Double>): Boolean {
        val exercise = currentExercise!!
        val previousRepCount = repCount

        if (exercise.cameraView == CameraView.FRONT) {
            if (exercise.type == ExerciseType.SYMMETRICAL) {
                trackSymmetricalFrontViewRep(landmarks, angles)
            } else {
                trackAlternatingFrontViewRep(landmarks, angles)
            }
        } else { // Side View
            if (exercise.type == ExerciseType.SYMMETRICAL) {
                trackSymmetricalSideViewRep(landmarks, angles)
            } else {
                trackAlternatingSideViewRep(landmarks, angles)
            }
        }
        return repCount > previousRepCount
    }

    // --- 1. Symmetrical Front View (e.g., Squats, Hand Raises) ---
    private fun trackSymmetricalFrontViewRep(landmarks: Map<String, Landmark>, angles: Map<String, Double>) {
        if (!isSideVisible("LEFT_", landmarks) || !isSideVisible("RIGHT_", landmarks)) {
            onFeedbackUpdate?.invoke(currentExercise!!.visibilityFeedbackMessage)
            resetRepState()
            return
        }
        val movement = currentExercise!!.primaryMovement as? AngleMovement ?: return
        val relevantAngles = movement.keyJointsToTrack.mapNotNull { angles[it] }
        if (relevantAngles.size != movement.keyJointsToTrack.size) return

        val isExtendingMovement = movement.entryThreshold > movement.exitThreshold

        if (isExtendingMovement) {
            when (repState) {
                is RepetitionState.IDLE -> if (relevantAngles.all { it < movement.exitThreshold }) repState = RepetitionState.IN_PROGRESS
                is RepetitionState.IN_PROGRESS -> if (relevantAngles.all { it > movement.entryThreshold }) {
                    repCount++
                    onRepCompleted?.invoke(repCount)
                    resetRepState()
                }
                else -> {}
            }
        } else {
            when (repState) {
                is RepetitionState.IDLE -> if (relevantAngles.all { it > movement.exitThreshold }) repState = RepetitionState.IN_PROGRESS
                is RepetitionState.IN_PROGRESS -> if (relevantAngles.all { it < movement.entryThreshold }) {
                    repCount++
                    onRepCompleted?.invoke(repCount)
                    resetRepState()
                }
                else -> {}
            }
        }
    }

    // --- 2. Symmetrical Side View (e.g., Calf Raises) ---
    private fun trackSymmetricalSideViewRep(landmarks: Map<String, Landmark>, angles: Map<String, Double>) {
        val exercise = currentExercise!!
        val movement = exercise.primaryMovement as? AngleMovement ?: return

        val isLeftVisible = isSideVisible("LEFT_", landmarks)
        val isRightVisible = isSideVisible("RIGHT_", landmarks)

        val angleToTrack: Double? = when {
            isLeftVisible -> angles[movement.keyJointsToTrack.firstOrNull { it.startsWith("Left") }]
            isRightVisible -> angles[movement.keyJointsToTrack.firstOrNull { it.startsWith("Right") }]
            else -> {
                onFeedbackUpdate?.invoke(exercise.visibilityFeedbackMessage)
                resetRepState()
                return
            }
        }
        if (angleToTrack == null) return

        val isExtendingMovement = movement.entryThreshold > movement.exitThreshold

        if (isExtendingMovement) {
            when (repState) {
                is RepetitionState.IDLE -> if (angleToTrack < movement.exitThreshold) repState = RepetitionState.IN_PROGRESS
                is RepetitionState.IN_PROGRESS -> if (angleToTrack > movement.entryThreshold) {
                    repCount++
                    onRepCompleted?.invoke(repCount)
                    resetRepState()
                }
                else -> {}
            }
        } else {
            when (repState) {
                is RepetitionState.IDLE -> if (angleToTrack > movement.exitThreshold) repState = RepetitionState.IN_PROGRESS
                is RepetitionState.IN_PROGRESS -> if (angleToTrack < movement.entryThreshold) {
                    repCount++
                    onRepCompleted?.invoke(repCount)
                    resetRepState()
                }
                else -> {}
            }
        }
    }

    // --- 3. Alternating Side View (e.g., Flutter Kicks) ---
    private fun trackAlternatingSideViewRep(landmarks: Map<String, Landmark>, angles: Map<String, Double>) {
        val isLeftVisible = isSideVisible("LEFT_", landmarks)
        val isRightVisible = isSideVisible("RIGHT_", landmarks)

        if (!isLeftVisible && !isRightVisible) {
            onFeedbackUpdate?.invoke(currentExercise!!.visibilityFeedbackMessage)
            resetRepState()
            return
        }
        when (val movement = currentExercise!!.primaryMovement) {
            is AngleMovement -> trackAlternatingAngleRep(angles, movement, isLeftVisible, isRightVisible)
            is DistanceMovement -> trackAlternatingDistanceRep(landmarks, movement)
        }
    }

    // --- 4. Alternating Front View (e.g., Marching) ---
    private fun trackAlternatingFrontViewRep(landmarks: Map<String, Landmark>, angles: Map<String, Double>) {
        if (!isSideVisible("LEFT_", landmarks) || !isSideVisible("RIGHT_", landmarks)) {
            onFeedbackUpdate?.invoke(currentExercise!!.visibilityFeedbackMessage)
            resetRepState()
            return
        }
        when (val movement = currentExercise!!.primaryMovement) {
            is AngleMovement -> trackAlternatingAngleRep(angles, movement)
            is DistanceMovement -> trackAlternatingDistanceRep(landmarks, movement)
        }
    }

    // --- CORE REP COUNTING LOGIC (REUSABLE) ---

    private fun trackAlternatingAngleRep(angles: Map<String, Double>, movement: AngleMovement, isLeftVisible: Boolean = true, isRightVisible: Boolean = true) {
        val leftAngle = if (isLeftVisible) angles[movement.keyJointsToTrack[0]] else null
        val rightAngle = if (isRightVisible) angles[movement.keyJointsToTrack[1]] else null
        if (leftAngle == null && rightAngle == null) return

        val isBending = movement.entryThreshold < movement.exitThreshold

        when (repState) {
            RepetitionState.IDLE -> {
                if (isBending && leftAngle != null && leftAngle < movement.entryThreshold) repState = RepetitionState.LEFT_SIDE_IN_PROGRESS
                else if (isBending && rightAngle != null && rightAngle < movement.entryThreshold) repState = RepetitionState.RIGHT_SIDE_IN_PROGRESS
            }
            RepetitionState.LEFT_SIDE_IN_PROGRESS -> {
                if (isBending && leftAngle != null && leftAngle > movement.exitThreshold) {
                    stepCount++
                    repState = RepetitionState.IDLE
                }
            }
            RepetitionState.RIGHT_SIDE_IN_PROGRESS -> {
                if (isBending && rightAngle != null && rightAngle > movement.exitThreshold) {
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

    // --- HELPER FUNCTIONS ---

    private fun isSideVisible(sidePrefix: String, landmarks: Map<String, Landmark>): Boolean {
        val required = currentExercise?.requiredLandmarks ?: return false
        val sideLandmarks = required.filter { it.startsWith(sidePrefix) }
        if (sideLandmarks.isEmpty()) return true
        return sideLandmarks.all { (landmarks[it]?.visibility ?: 0f) >= VISIBILITY_THRESHOLD }
    }

    private fun checkFormRules(landmarks: Map<String, Landmark>, angles: Map<String, Double>): List<String> {
        val exercise = currentExercise ?: return emptyList()
        if (repState == RepetitionState.IDLE) return emptyList()

        val newFeedbackMessages = mutableListOf<String>()

        for (rule in exercise.formRules) {
            var isBroken = false
            when (rule) {
                is AngleRule -> {
                    val angle = angles[rule.angleName]
                    if (angle != null && (angle < rule.minAngle || angle > rule.maxAngle)) {
                        isBroken = true
                    }
                }
                is HorizontalAlignmentRule -> {
                    val lm1 = landmarks[rule.landmark1] ?: continue
                    val lm2 = landmarks[rule.landmark2] ?: continue
                    val shoulderWidth = abs((landmarks["LEFT_SHOULDER"]?.x ?: 0f) - (landmarks["RIGHT_SHOULDER"]?.x ?: 1f))
                    if (shoulderWidth == 0f) continue
                    val horizontalDistance = abs(lm1.x - lm2.x)
                    if (horizontalDistance / shoulderWidth > rule.maxDistanceRatio) {
                        isBroken = true
                    }
                }
                is DistanceRule -> {
                    val shoulderWidth = abs((landmarks["LEFT_SHOULDER"]?.x ?: 0f) - (landmarks["RIGHT_SHOULDER"]?.x ?: 1f))
                    if (shoulderWidth == 0f) continue
                    val lm1 = landmarks[rule.landmark1] ?: continue
                    val lm2 = landmarks[rule.landmark2] ?: continue
                    val distance = getDistance(lm1, lm2) / shoulderWidth
                    if (distance > rule.maxDistanceRatio) {
                        isBroken = true
                    }
                }
            }

            if (isBroken && !spokenFormErrors.contains(rule.feedbackMessage)) {
                newFeedbackMessages.add(rule.feedbackMessage)
                spokenFormErrors.add(rule.feedbackMessage)
            }
        }
        return newFeedbackMessages
    }

    private fun getDistance(lm1: Landmark?, lm2: Landmark?): Float {
        if (lm1 == null || lm2 == null) return Float.MAX_VALUE
        val dx = lm1.x - lm2.x
        val dy = lm1.y - lm2.y
        return sqrt((dx * dx) + (dy * dy))
    }

    private fun startIdleTimer() {
        idleTimer?.cancel()
        idleTimer = object : CountDownTimer(IDLE_THRESHOLD_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                currentExercise?.primaryMovementInstruction?.let {
                    onFeedbackUpdate?.invoke(it)
                }
            }
        }.start()
    }

    private fun cancelIdleTimer() {
        idleTimer?.cancel()
        idleTimer = null
    }

    private fun resetRepState() {
        repState = RepetitionState.IDLE
        spokenFormErrors.clear()
        cancelIdleTimer()
    }

    private fun checkForStartGesture(landmarks: Map<String, Landmark>) {
        val leftWrist = landmarks["LEFT_WRIST"]
        val rightWrist = landmarks["RIGHT_WRIST"]
        val leftShoulder = landmarks["LEFT_SHOULDER"]
        val rightShoulder = landmarks["RIGHT_SHOULDER"]

        if (leftWrist != null && rightWrist != null && leftShoulder != null && rightShoulder != null) {
            if (leftWrist.y < leftShoulder.y && rightWrist.y < rightShoulder.y) {
                if (startGestureTimer == null) {
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

    private fun cancelStartGestureTimer() {
        startGestureTimer?.cancel()
        startGestureTimer = null
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

    fun reset() {
        cancelStartGestureTimer()
        cancelIdleTimer()
        repCount = 0
        stepCount = 0
        repState = RepetitionState.IDLE
        spokenFormErrors.clear()
        updateState(ExerciseState.NOT_STARTED)
    }
}

