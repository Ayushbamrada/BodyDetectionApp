//package com.example.bodydetectionapp.ui.components
//
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.graphics.PointF
//import android.view.View
//import com.example.bodydetectionapp.data.models.ExercisePhase
//import com.example.bodydetectionapp.utils.AngleCalculator
//import com.example.bodydetectionapp.utils.AngleCalculator.LandmarkIndices
//import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
//import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
//
//class PoseOverlay(context: Context) : View(context) {
//
//    var poseResult: PoseLandmarkerResult? = null
//        set(value) {
//            field = value
//            invalidate()
//        }
//
//    var highlightedJointIndices: Set<Int> = emptySet()
//        set(value) {
//            field = value
//            invalidate()
//        }
//
//    var anglesToDisplay: Map<String, Double> = emptyMap()
//        set(value) {
//            field = value
//            invalidate()
//        }
//
//    var currentPhaseInfo: ExercisePhase? = null
//        set(value) {
//            field = value
//            invalidate()
//        }
//
//    var feedbackMessages: List<String> = emptyList()
//        set(value) {
//            field = value
//            invalidate()
//        }
//
//    private val pointPaint = Paint().apply {
//        color = Color.GREEN
//        strokeWidth = 10f
//        style = Paint.Style.FILL
//        isAntiAlias = true
//    }
//
//    private val highlightPaint = Paint().apply {
//        color = Color.RED
//        strokeWidth = 12f
//        style = Paint.Style.FILL
//        isAntiAlias = true
//    }
//
//    private val linePaint = Paint().apply {
//        color = Color.WHITE
//        strokeWidth = 6f
//        style = Paint.Style.STROKE
//        isAntiAlias = true
//    }
//
//    private val angleTextGoodPaint = Paint().apply {
//        color = Color.GREEN
//        textSize = 40f
//        textAlign = Paint.Align.CENTER
//        isFakeBoldText = true
//        style = Paint.Style.FILL
//        isAntiAlias = true
//        setShadowLayer(3f, 1f, 1f, Color.BLACK)
//    }
//    private val angleTextWarningPaint = Paint().apply {
//        color = Color.YELLOW
//        textSize = 40f
//        textAlign = Paint.Align.CENTER
//        isFakeBoldText = true
//        style = Paint.Style.FILL
//        isAntiAlias = true
//        setShadowLayer(3f, 1f, 1f, Color.BLACK)
//    }
//    private val angleTextBadPaint = Paint().apply {
//        color = Color.RED
//        textSize = 40f
//        textAlign = Paint.Align.CENTER
//        isFakeBoldText = true
//        style = Paint.Style.FILL
//        isAntiAlias = true
//        setShadowLayer(3f, 1f, 1f, Color.BLACK)
//    }
//    private val angleTextDefaultPaint = Paint().apply {
//        color = Color.WHITE
//        textSize = 40f
//        textAlign = Paint.Align.CENTER
//        isFakeBoldText = true
//        style = Paint.Style.FILL
//        isAntiAlias = true
//        setShadowLayer(3f, 1f, 1f, Color.BLACK)
//    }
//
//    private val feedbackTextPaint = Paint().apply {
//        color = Color.CYAN
//        textSize = 30f
//        textAlign = Paint.Align.CENTER
//        isFakeBoldText = true
//        style = Paint.Style.FILL
//        isAntiAlias = true
//        setShadowLayer(3f, 1f, 1f, Color.BLACK)
//    }
//
//    private val phaseTextPaint = Paint().apply {
//        color = Color.WHITE
//        textSize = 80f
//        textAlign = Paint.Align.CENTER
//        isFakeBoldText = true
//        style = Paint.Style.FILL
//        isAntiAlias = true
//        setShadowLayer(5f, 0f, 0f, Color.BLACK)
//    }
//
//    private fun drawPoint(canvas: Canvas, point: PointF, landmarkIndex: Int) {
//        val currentPaint = if (highlightedJointIndices.contains(landmarkIndex)) highlightPaint else pointPaint
//        canvas.drawCircle(point.x, point.y, 15f, currentPaint)
//    }
//
//    private fun drawLine(canvas: Canvas, startPoint: PointF, endPoint: PointF) {
//        canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, linePaint)
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
//        val viewWidth = width.toFloat()
//        val viewHeight = height.toFloat()
//
//        poseResult?.landmarks()?.firstOrNull()?.let { landmarkList ->
//            val scaledLandmarks = landmarkList.map { landmark ->
//                PointF(landmark.x() * viewWidth, landmark.y() * viewHeight)
//            }
//
//            scaledLandmarks.forEachIndexed { index, point ->
//                drawPoint(canvas, point, index)
//            }
//
//            val connections = listOf(
//                Pair(11, 13), Pair(13, 15), Pair(15, 17), Pair(15, 19), Pair(15, 21), // Left Arm
//                Pair(12, 14), Pair(14, 16), Pair(16, 18), Pair(16, 20), Pair(16, 22), // Right Arm
//                Pair(11, 12), // Shoulders
//                Pair(23, 24), // Hips
//                Pair(11, 23), Pair(12, 24), // Torso
//                Pair(23, 25), Pair(25, 27), Pair(27, 29), Pair(29, 31), // Left Leg
//                Pair(24, 26), Pair(26, 28), Pair(28, 30), Pair(30, 32),  // Right Leg
//                Pair(27, 31), Pair(28, 32) // Feet
//            )
//
//            val headConnections = listOf(
//                Pair(LandmarkIndices.NOSE, LandmarkIndices.LEFT_EYE_INNER),
//                Pair(LandmarkIndices.LEFT_EYE_INNER, LandmarkIndices.LEFT_EYE),
//                Pair(LandmarkIndices.LEFT_EYE, LandmarkIndices.LEFT_EYE_OUTER),
//                Pair(LandmarkIndices.LEFT_EYE_OUTER, LandmarkIndices.LEFT_EAR),
//                Pair(LandmarkIndices.NOSE, LandmarkIndices.RIGHT_EYE_INNER),
//                Pair(LandmarkIndices.RIGHT_EYE_INNER, LandmarkIndices.RIGHT_EYE),
//                Pair(LandmarkIndices.RIGHT_EYE, LandmarkIndices.RIGHT_EYE_OUTER),
//                Pair(LandmarkIndices.RIGHT_EYE_OUTER, LandmarkIndices.RIGHT_EAR),
//                Pair(LandmarkIndices.LEFT_EAR, LandmarkIndices.RIGHT_EAR), // Connect ears
//                Pair(LandmarkIndices.MOUTH_LEFT, LandmarkIndices.MOUTH_RIGHT) // Mouth
//            )
//
//            val allConnections = connections + headConnections
//
//            allConnections.forEach { (startIndex, endIndex) ->
//                if (startIndex < scaledLandmarks.size && endIndex < scaledLandmarks.size) {
//                    drawLine(canvas, scaledLandmarks[startIndex], scaledLandmarks[endIndex])
//                }
//            }
//
//            drawJointInformation(canvas, scaledLandmarks)
//        }
//    }
//
//    private fun drawJointInformation(canvas: Canvas, scaledLandmarks: List<PointF>) {
//        val angleJointMap = mapOf(
//            "Left Elbow Angle" to LandmarkIndices.LEFT_ELBOW,
//            "Right Elbow Angle" to LandmarkIndices.RIGHT_ELBOW,
//            "Left Knee Angle" to LandmarkIndices.LEFT_KNEE,
//            "Right Knee Angle" to LandmarkIndices.RIGHT_KNEE,
//            "Left Shoulder Angle" to LandmarkIndices.LEFT_SHOULDER,
//            "Right Shoulder Angle" to LandmarkIndices.RIGHT_SHOULDER,
//            "Left Hip Angle" to LandmarkIndices.LEFT_HIP,
//            "Right Hip Angle" to LandmarkIndices.RIGHT_HIP,
//            "Left Ankle Angle" to LandmarkIndices.LEFT_ANKLE,
//            "Right Ankle Angle" to LandmarkIndices.RIGHT_ANKLE,
//            "Torso Angle" to LandmarkIndices.LEFT_HIP // Example, adjust if a better central point exists
//        )
//
//        val targetAnglesForCurrentPhase = currentPhaseInfo?.targetAngles ?: emptyMap()
//
//        val jointFeedbackMap = mutableMapOf<Int, String>()
//        feedbackMessages.forEach { msg ->
//            when {
//                msg.contains("Knee Angle", ignoreCase = true) -> {
//                    if (msg.contains("Left", ignoreCase = true)) jointFeedbackMap[LandmarkIndices.LEFT_KNEE] = msg
//                    else if (msg.contains("Right", ignoreCase = true)) jointFeedbackMap[LandmarkIndices.RIGHT_KNEE] = msg
//                }
//                msg.contains("Hip Angle", ignoreCase = true) -> {
//                    if (msg.contains("Left", ignoreCase = true)) jointFeedbackMap[LandmarkIndices.LEFT_HIP] = msg
//                    else if (msg.contains("Right", ignoreCase = true)) jointFeedbackMap[LandmarkIndices.RIGHT_HIP] = msg
//                }
//                msg.contains("Elbow Angle", ignoreCase = true) -> {
//                    if (msg.contains("Left", ignoreCase = true)) jointFeedbackMap[LandmarkIndices.LEFT_ELBOW] = msg
//                    else if (msg.contains("Right", ignoreCase = true)) jointFeedbackMap[LandmarkIndices.RIGHT_ELBOW] = msg
//                }
//                msg.contains("Shoulder Angle", ignoreCase = true) -> {
//                    if (msg.contains("Left", ignoreCase = true)) jointFeedbackMap[LandmarkIndices.LEFT_SHOULDER] = msg
//                    else if (msg.contains("Right", ignoreCase = true)) jointFeedbackMap[LandmarkIndices.RIGHT_SHOULDER] = msg
//                }
//                msg.contains("Ankle Angle", ignoreCase = true) -> {
//                    if (msg.contains("Left", ignoreCase = true)) jointFeedbackMap[LandmarkIndices.LEFT_ANKLE] = msg
//                    else if (msg.contains("Right", ignoreCase = true)) jointFeedbackMap[LandmarkIndices.RIGHT_ANKLE] = msg
//                }
//                msg.contains("Torso Angle", ignoreCase = true) -> {
//                    jointFeedbackMap[LandmarkIndices.LEFT_HIP] = msg
//                }
//            }
//        }
//
//        // Define vertical offsets
//        val angleTextOffsetY = -40f // Position angle text 40 pixels above the joint
//        val feedbackTextOffsetY = 40f // Position feedback text 40 pixels below the joint
//
//        anglesToDisplay.forEach { (angleName, angleValue) ->
//            val jointIndex = angleJointMap[angleName]
//            if (jointIndex != null && jointIndex < scaledLandmarks.size) {
//                val jointPoint = scaledLandmarks[jointIndex]
//
//                val angleText = "%.0f°".format(angleValue)
//                var currentAngleTextPaint = angleTextDefaultPaint
//
//                // Determine angle text color based on range
//                targetAnglesForCurrentPhase[angleName]?.let { targetRange ->
//                    currentAngleTextPaint = when {
//                        // Using a small buffer for warning
//                        angleValue >= targetRange.min - 5 && angleValue <= targetRange.min ||
//                                angleValue <= targetRange.max + 5 && angleValue >= targetRange.max -> angleTextWarningPaint // Close to range
//                        angleValue >= targetRange.min && angleValue <= targetRange.max -> angleTextGoodPaint // In range
//                        else -> angleTextBadPaint // Out of range
//                    }
//                }
//
//                // Draw current angle value
//                canvas.drawText(
//                    angleText,
//                    jointPoint.x,
//                    jointPoint.y + angleTextOffsetY, // Use the dedicated offset
//                    currentAngleTextPaint
//                )
//
//                // Draw specific feedback message near the joint, if available
//                val feedback = jointFeedbackMap[jointIndex]
//                if (!feedback.isNullOrBlank()) {
//                    val cleanFeedback = feedback
//                        .split("(Target:")[0].trim()
//                        .replace("Current: .*".toRegex(), "")
//                        .trim()
//
//                    canvas.drawText(
//                        cleanFeedback,
//                        jointPoint.x,
//                        jointPoint.y + feedbackTextOffsetY, // Use the dedicated offset
//                        feedbackTextPaint
//                    )
//                }
//            }
//        }
//    }
//}

package com.example.bodydetectionapp.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.view.View
import com.example.bodydetectionapp.data.models.ExercisePhase
import com.example.bodydetectionapp.utils.AngleCalculator.LandmarkIndices
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseOverlay(context: Context) : View(context) {

    var poseResult: PoseLandmarkerResult? = null
        set(value) {
            field = value
            invalidate() // Request a redraw when data changes
        }

    var highlightedJointIndices: Set<Int> = emptySet()
        set(value) {
            field = value
            invalidate()
        }

    // Now directly receives the filtered angles from ViewModel
    var anglesToDisplay: Map<String, Double> = emptyMap()
        set(value) {
            field = value
            invalidate()
        }

    var currentPhaseInfo: ExercisePhase? = null
        set(value) {
            field = value
            invalidate()
        }

    var feedbackMessages: List<String> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val pointPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 10f
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val highlightPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 12f
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val linePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val angleTextGoodPaint = Paint().apply {
        color = Color.GREEN
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        style = Paint.Style.FILL
        isAntiAlias = true
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }
    private val angleTextWarningPaint = Paint().apply {
        color = Color.YELLOW
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        style = Paint.Style.FILL
        isAntiAlias = true
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }
    private val angleTextBadPaint = Paint().apply {
        color = Color.RED
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        style = Paint.Style.FILL
        isAntiAlias = true
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }
    private val angleTextDefaultPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        style = Paint.Style.FILL
        isAntiAlias = true
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    // Updated feedback text paint for better visibility
    private val feedbackTextPaint = Paint().apply {
        color = Color.CYAN
        textSize = 36f // Slightly larger
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        style = Paint.Style.FILL
        isAntiAlias = true
        setShadowLayer(5f, 2f, 2f, Color.BLACK) // Stronger shadow
    }

    private fun drawPoint(canvas: Canvas, point: PointF, landmarkIndex: Int) {
        val currentPaint = if (highlightedJointIndices.contains(landmarkIndex)) highlightPaint else pointPaint
        canvas.drawCircle(point.x, point.y, 15f, currentPaint)
    }

    private fun drawLine(canvas: Canvas, startPoint: PointF, endPoint: PointF) {
        canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, linePaint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        poseResult?.landmarks()?.firstOrNull()?.let { landmarkList ->
            val scaledLandmarks = landmarkList.map { landmark ->
                PointF(landmark.x() * viewWidth, landmark.y() * viewHeight)
            }

            // Draw connections between landmarks
            val connections = listOf(
                Pair(11, 13), Pair(13, 15), Pair(15, 17), Pair(15, 19), Pair(15, 21), // Left Arm
                Pair(12, 14), Pair(14, 16), Pair(16, 18), Pair(16, 20), Pair(16, 22), // Right Arm
                Pair(11, 12), // Shoulders
                Pair(23, 24), // Hips
                Pair(11, 23), Pair(12, 24), // Torso
                Pair(23, 25), Pair(25, 27), Pair(27, 29), Pair(29, 31), // Left Leg
                Pair(24, 26), Pair(26, 28), Pair(28, 30), Pair(30, 32),  // Right Leg
                Pair(27, 31), Pair(28, 32) // Feet
            )

            val headConnections = listOf(
                Pair(LandmarkIndices.NOSE, LandmarkIndices.LEFT_EYE_INNER),
                Pair(LandmarkIndices.LEFT_EYE_INNER, LandmarkIndices.LEFT_EYE),
                Pair(LandmarkIndices.LEFT_EYE, LandmarkIndices.LEFT_EYE_OUTER),
                Pair(LandmarkIndices.LEFT_EYE_OUTER, LandmarkIndices.LEFT_EAR),
                Pair(LandmarkIndices.NOSE, LandmarkIndices.RIGHT_EYE_INNER),
                Pair(LandmarkIndices.RIGHT_EYE_INNER, LandmarkIndices.RIGHT_EYE),
                Pair(LandmarkIndices.RIGHT_EYE, LandmarkIndices.RIGHT_EYE_OUTER),
                Pair(LandmarkIndices.RIGHT_EYE_OUTER, LandmarkIndices.RIGHT_EAR),
                Pair(LandmarkIndices.LEFT_EAR, LandmarkIndices.RIGHT_EAR), // Connect ears
                Pair(LandmarkIndices.MOUTH_LEFT, LandmarkIndices.MOUTH_RIGHT) // Mouth
            )

            val allConnections = connections + headConnections

            allConnections.forEach { (startIndex, endIndex) ->
                if (startIndex < scaledLandmarks.size && endIndex < scaledLandmarks.size) {
                    drawLine(canvas, scaledLandmarks[startIndex], scaledLandmarks[endIndex])
                }
            }

            // Draw individual points (landmarks)
            scaledLandmarks.forEachIndexed { index, point ->
                drawPoint(canvas, point, index)
            }

            // Draw angle information and feedback
            drawJointInformation(canvas, scaledLandmarks)

            // Draw general feedback messages at the bottom
            drawGeneralFeedback(canvas, viewWidth, viewHeight)
        }
    }

    private fun drawJointInformation(canvas: Canvas, scaledLandmarks: List<PointF>) {
        // Map angle names to a representative joint index for drawing purposes
        val angleJointMap = mapOf(
            "Left Elbow Angle" to LandmarkIndices.LEFT_ELBOW,
            "Right Elbow Angle" to LandmarkIndices.RIGHT_ELBOW,
            "Left Knee Angle" to LandmarkIndices.LEFT_KNEE,
            "Right Knee Angle" to LandmarkIndices.RIGHT_KNEE,
            "Left Shoulder Angle" to LandmarkIndices.LEFT_SHOULDER,
            "Right Shoulder Angle" to LandmarkIndices.RIGHT_SHOULDER,
            "Left Hip Angle" to LandmarkIndices.LEFT_HIP,
            "Right Hip Angle" to LandmarkIndices.RIGHT_HIP,
            "Left Ankle Angle" to LandmarkIndices.LEFT_ANKLE,
            "Right Ankle Angle" to LandmarkIndices.RIGHT_ANKLE,
            "Torso Angle" to LandmarkIndices.LEFT_HIP // Use Left Hip as a proxy for torso
        )

        // Pre-process feedback messages to associate with joints
        val jointFeedbackMap = mutableMapOf<Int, MutableList<String>>()
        feedbackMessages.forEach { msg ->
            when {
                msg.contains("Knee Angle", ignoreCase = true) -> {
                    if (msg.contains("Left", ignoreCase = true)) jointFeedbackMap.getOrPut(LandmarkIndices.LEFT_KNEE) { mutableListOf() }.add(msg)
                    else if (msg.contains("Right", ignoreCase = true)) jointFeedbackMap.getOrPut(LandmarkIndices.RIGHT_KNEE) { mutableListOf() }.add(msg)
                }
                msg.contains("Hip Angle", ignoreCase = true) -> {
                    if (msg.contains("Left", ignoreCase = true)) jointFeedbackMap.getOrPut(LandmarkIndices.LEFT_HIP) { mutableListOf() }.add(msg)
                    else if (msg.contains("Right", ignoreCase = true)) jointFeedbackMap.getOrPut(LandmarkIndices.RIGHT_HIP) { mutableListOf() }.add(msg)
                }
                msg.contains("Elbow Angle", ignoreCase = true) -> {
                    if (msg.contains("Left", ignoreCase = true)) jointFeedbackMap.getOrPut(LandmarkIndices.LEFT_ELBOW) { mutableListOf() }.add(msg)
                    else if (msg.contains("Right", ignoreCase = true)) jointFeedbackMap.getOrPut(LandmarkIndices.RIGHT_ELBOW) { mutableListOf() }.add(msg)
                }
                msg.contains("Shoulder Angle", ignoreCase = true) -> {
                    if (msg.contains("Left", ignoreCase = true)) jointFeedbackMap.getOrPut(LandmarkIndices.LEFT_SHOULDER) { mutableListOf() }.add(msg)
                    else if (msg.contains("Right", ignoreCase = true)) jointFeedbackMap.getOrPut(LandmarkIndices.RIGHT_SHOULDER) { mutableListOf() }.add(msg)
                }
                msg.contains("Ankle Angle", ignoreCase = true) -> {
                    if (msg.contains("Left", ignoreCase = true)) jointFeedbackMap.getOrPut(LandmarkIndices.LEFT_ANKLE) { mutableListOf() }.add(msg)
                    else if (msg.contains("Right", ignoreCase = true)) jointFeedbackMap.getOrPut(LandmarkIndices.RIGHT_ANKLE) { mutableListOf() }.add(msg)
                }
                msg.contains("Torso Angle", ignoreCase = true) -> {
                    jointFeedbackMap.getOrPut(LandmarkIndices.LEFT_HIP) { mutableListOf() }.add(msg)
                }
            }
        }

        val angleTextOffsetY = -40f // Position angle text above the joint
        val feedbackTextOffsetY = 40f // Position feedback text below the joint

        anglesToDisplay.forEach { (angleName, angleValue) ->
            val jointIndex = angleJointMap[angleName]
            if (jointIndex != null && jointIndex < scaledLandmarks.size) {
                val jointPoint = scaledLandmarks[jointIndex]

                val angleText = "%.0f°".format(angleValue)
                var currentAngleTextPaint = angleTextDefaultPaint

                // Determine angle text color based on current phase's absolute target ranges
                currentPhaseInfo?.targetAngles?.get(angleName)?.let { targetRange ->
                    currentAngleTextPaint = when {
                        // Check if angle is within the target range
                        angleValue >= targetRange.min && angleValue <= targetRange.max -> angleTextGoodPaint
                        // Check if angle is close to the target range (within 5 degrees)
                        angleValue >= targetRange.min - 5 && angleValue < targetRange.min ||
                                angleValue > targetRange.max && angleValue <= targetRange.max + 5 -> angleTextWarningPaint
                        // Otherwise, out of range
                        else -> angleTextBadPaint
                    }
                }
                // If there are relative target angles, you might need more complex logic to color.
                // For simplicity, we prioritize absolute targets first.
                // You could add logic here for relative targets if needed.

                // Draw current angle value
                canvas.drawText(
                    angleText,
                    jointPoint.x,
                    jointPoint.y + angleTextOffsetY,
                    currentAngleTextPaint
                )

                // Draw specific feedback messages near the joint, if available
                val specificJointFeedbacks = jointFeedbackMap[jointIndex]
                if (!specificJointFeedbacks.isNullOrEmpty()) {
                    var currentFeedbackY = jointPoint.y + feedbackTextOffsetY
                    specificJointFeedbacks.forEach { feedback ->
                        // Clean the feedback message to remove angle values/targets
                        val cleanFeedback = feedback
                            .split("(Target:")[0].trim()
                            .replace("Current: .*".toRegex(), "")
                            .trim()

                        canvas.drawText(
                            cleanFeedback,
                            jointPoint.x,
                            currentFeedbackY,
                            feedbackTextPaint
                        )
                        currentFeedbackY += feedbackTextPaint.textSize * 1.2f // Add line spacing
                    }
                }
            }
        }
    }

    // Draws general feedback messages at the bottom center
    private fun drawGeneralFeedback(canvas: Canvas, viewWidth: Float, viewHeight: Float) {
        val generalFeedback = feedbackMessages.filter { msg ->
            // Filter out messages that were already handled and drawn near specific joints
            !msg.contains("Knee Angle", ignoreCase = true) &&
                    !msg.contains("Hip Angle", ignoreCase = true) &&
                    !msg.contains("Elbow Angle", ignoreCase = true) &&
                    !msg.contains("Shoulder Angle", ignoreCase = true) &&
                    !msg.contains("Ankle Angle", ignoreCase = true) &&
                    !msg.contains("Torso Angle", ignoreCase = true)
        }

        var textY = viewHeight - 200f // Start drawing feedback from bottom, adjust as needed

        // Only draw the first few general messages to avoid clutter
        generalFeedback.take(3).forEachIndexed { index, message ->
            // For general messages, we'll draw the full message.
            canvas.drawText(
                message,
                viewWidth / 2,
                textY + (index * feedbackTextPaint.textSize * 1.2f),
                feedbackTextPaint
            )
        }
    }
}