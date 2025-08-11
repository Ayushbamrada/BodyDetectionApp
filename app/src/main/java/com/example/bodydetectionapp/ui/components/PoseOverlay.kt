package com.example.bodydetectionapp.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.view.View
import com.example.bodydetectionapp.utils.AngleCalculator // Ensure AngleCalculator is imported for LandmarkIndices
import com.example.bodydetectionapp.utils.AngleCalculator.LandmarkIndices // Ensure AngleCalculator is imported for LandmarkIndices
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseOverlay(context: Context) : View(context) {

    // Store the pose detection result. Invalidate view to trigger redraw.
    var poseResult: PoseLandmarkerResult? = null
        set(value) {
            field = value
            invalidate() // Triggers onDraw
        }

    // A set of landmark indices that should be highlighted (e.g., if they are moving)
    var highlightedJointIndices: Set<Int> = emptySet()
        set(value) {
            field = value
            invalidate() // Redraw when highlighted joints change
        }

    // Angles to display on the body (provided externally)
    var anglesToDisplay: Map<String, Double> = emptyMap()
        set(value) {
            field = value
            invalidate() // Redraw when angles change
        }

    // Paint for drawing joint points (default green)
    private val pointPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 10f
        style = Paint.Style.FILL
        isAntiAlias = true // Smooth circles
    }

    // Paint for drawing highlighted joint points (red)
    private val highlightPaint = Paint().apply {
        color = Color.RED // Red for highlighted joints
        strokeWidth = 12f // Slightly thicker for emphasis
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Paint for drawing lines connecting joints (white)
    private val linePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 6f // Slightly thicker lines for better visibility
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    // Paint for drawing angle text
    private val angleTextPaint = Paint().apply {
        color = Color.YELLOW // Bright color for visibility
        textSize = 40f // Larger text for readability
        textAlign = Paint.Align.CENTER // Center text over the joint
        isFakeBoldText = true // Make text bolder
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Paint for phase text at top (if you want to draw this in the overlay)
    // Note: It might be better to display phase info directly in Compose UI rather than this View overlay.
    private val phaseTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 80f // Very large for prominent display
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        style = Paint.Style.FILL
        isAntiAlias = true
        setShadowLayer(5f, 0f, 0f, Color.BLACK) // Optional: Add shadow for better contrast
    }

    // Helper to draw a single point, choosing color based on highlight status
    private fun drawPoint(canvas: Canvas, point: PointF, landmarkIndex: Int) {
        val currentPaint = if (highlightedJointIndices.contains(landmarkIndex)) highlightPaint else pointPaint
        // Increased circle radius to 15f for better visibility
        canvas.drawCircle(point.x, point.y, 15f, currentPaint)
    }

    // Helper to draw a line between two points
    private fun drawLine(canvas: Canvas, startPoint: PointF, endPoint: PointF) {
        canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, linePaint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        // Use poseResult to get landmarks
        poseResult?.landmarks()?.firstOrNull()?.let { landmarkList ->
            // Scale landmarks to view dimensions
            val scaledLandmarks = landmarkList.map { landmark ->
                PointF(landmark.x() * viewWidth, landmark.y() * viewHeight)
            }

            // Draw all joint points (circles) first
            scaledLandmarks.forEachIndexed { index, point ->
                drawPoint(canvas, point, index)
            }

            // Define connections between landmarks to form the skeleton.
            // These indices correspond to the MediaPipe Pose landmarks (0-32).
            // Reference: https://developers.google.com/mediapipe/solutions/vision/pose_landmarker/android#pose_landmarks
            val connections = listOf(
                // Torso
                Pair(11, 13), Pair(13, 15), // Left Arm (Shoulder-Elbow, Elbow-Wrist)
                Pair(12, 14), Pair(14, 16), // Right Arm (Shoulder-Elbow, Elbow-Wrist)
                Pair(11, 12), // Shoulders (Left-Right)
                Pair(23, 24), // Hips (Left-Right)
                Pair(11, 23), // Left Torso (Shoulder-Hip)
                Pair(12, 24), // Right Torso (Shoulder-Hip)

                // Head (simplified, using points around the head)
                Pair(LandmarkIndices.NOSE, LandmarkIndices.LEFT_EYE_INNER),
                Pair(LandmarkIndices.LEFT_EYE_INNER, LandmarkIndices.LEFT_EYE),
                Pair(LandmarkIndices.LEFT_EYE, LandmarkIndices.LEFT_EYE_OUTER),
                Pair(LandmarkIndices.LEFT_EYE_OUTER, LandmarkIndices.LEFT_EAR),
                Pair(LandmarkIndices.NOSE, LandmarkIndices.RIGHT_EYE_INNER),
                Pair(LandmarkIndices.RIGHT_EYE_INNER, LandmarkIndices.RIGHT_EYE),
                Pair(LandmarkIndices.RIGHT_EYE, LandmarkIndices.RIGHT_EYE_OUTER),
                Pair(LandmarkIndices.RIGHT_EYE_OUTER, LandmarkIndices.RIGHT_EAR),
                Pair(LandmarkIndices.LEFT_EAR, LandmarkIndices.RIGHT_EAR), // Connect ears
                Pair(LandmarkIndices.MOUTH_LEFT, LandmarkIndices.MOUTH_RIGHT), // Mouth

                // Hands (Simplified for visualization, connecting wrist to key finger base points)
                // These are more for visual connectivity than precise anatomy
                Pair(LandmarkIndices.LEFT_WRIST, LandmarkIndices.LEFT_THUMB_CMC),
                Pair(LandmarkIndices.LEFT_WRIST, LandmarkIndices.LEFT_INDEX_MCP),
                Pair(LandmarkIndices.LEFT_WRIST, LandmarkIndices.LEFT_PINKY_MCP),

                Pair(LandmarkIndices.RIGHT_WRIST, LandmarkIndices.RIGHT_THUMB_CMC),
                Pair(LandmarkIndices.RIGHT_WRIST, LandmarkIndices.RIGHT_INDEX_MCP),
                Pair(LandmarkIndices.RIGHT_WRIST, LandmarkIndices.RIGHT_PINKY_MCP),

                // Left Leg
                Pair(23, 25), // Left Hip to Left Knee
                Pair(25, 27), // Left Knee to Left Ankle
                Pair(27, 29), // Left Ankle to Left Heel
                Pair(29, 31), // Left Heel to Left Foot Index (Ball of foot)
                Pair(27, 31), // Left Ankle to Left Foot Index (for foot outline)

                // Right Leg
                Pair(24, 26), // Right Hip to Right Knee
                Pair(26, 28), // Right Knee to Right Ankle
                Pair(28, 30), // Right Ankle to Right Heel
                Pair(30, 32), // Right Heel to Right Foot Index (Ball of foot)
                Pair(28, 32)  // Right Ankle to Right Foot Index (for foot outline)
            )

            // Draw lines between connected joints
            connections.forEach { (startIndex, endIndex) ->
                // Ensure indices are within bounds of the scaledLandmarks list
                if (startIndex < scaledLandmarks.size && endIndex < scaledLandmarks.size) {
                    drawLine(canvas, scaledLandmarks[startIndex], scaledLandmarks[endIndex])
                }
            }

            // Draw angles on the body
            drawAnglesOnBody(canvas, scaledLandmarks)
        }
    }

    // Helper function to draw angles at specific joint locations
    private fun drawAnglesOnBody(canvas: Canvas, scaledLandmarks: List<PointF>) {
        // Map angle names to the landmark index of the *joint* where the angle is formed
        val angleJointMap = mapOf(
            "Left Elbow Angle" to AngleCalculator.LandmarkIndices.LEFT_ELBOW,
            "Right Elbow Angle" to AngleCalculator.LandmarkIndices.RIGHT_ELBOW,
            "Left Knee Angle" to AngleCalculator.LandmarkIndices.LEFT_KNEE,
            "Right Knee Angle" to AngleCalculator.LandmarkIndices.RIGHT_KNEE,
            "Left Shoulder Angle" to AngleCalculator.LandmarkIndices.LEFT_SHOULDER,
            "Right Shoulder Angle" to AngleCalculator.LandmarkIndices.RIGHT_SHOULDER,
            "Left Hip Angle" to AngleCalculator.LandmarkIndices.LEFT_HIP,
            "Right Hip Angle" to AngleCalculator.LandmarkIndices.RIGHT_HIP,
            "Left Ankle Angle" to AngleCalculator.LandmarkIndices.LEFT_ANKLE,
            "Right Ankle Angle" to AngleCalculator.LandmarkIndices.RIGHT_ANKLE
            // "Torso Angle" needs careful placement, perhaps an average of hips/shoulders.
            // For now, let's keep it simple at the hip, or consider a dedicated torso line.
            // For the example, I'll place it near the left hip as before.
            // If it's a "general" torso angle, it might not be tied to a single joint.
        )

        anglesToDisplay.forEach { (angleName, angleValue) ->
            val jointIndex = angleJointMap[angleName]
            if (jointIndex != null && jointIndex < scaledLandmarks.size) {
                val jointPoint = scaledLandmarks[jointIndex]

                // Determine text position to avoid overlap and place near the joint
                // Offset slightly from the joint to not cover it completely
                val offsetX = 30f // pixels to the right
                val offsetY = -30f // pixels above

                val text = "%.0f°".format(angleValue) // Format to whole number degrees

                // Draw the text
                canvas.drawText(text, jointPoint.x + offsetX, jointPoint.y + offsetY, angleTextPaint)
            }
        }
    }
}