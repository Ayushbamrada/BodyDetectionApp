package com.example.bodydetectionapp.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.view.View
import com.example.bodydetectionapp.utils.AngleCalculator.LandmarkIndices
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseOverlay(context: Context) : View(context) {

    var poseResult: PoseLandmarkerResult? = null
        set(value) {
            field = value
            invalidate() // Request a redraw when data changes
        }

    var anglesToDisplay: Map<String, Double> = emptyMap()
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

    private val linePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val angleTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        style = Paint.Style.FILL
        isAntiAlias = true
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    private fun drawPoint(canvas: Canvas, point: PointF) {
        // Reduced the point size slightly for a cleaner look with more points
        canvas.drawCircle(point.x, point.y, 12f, pointPaint)
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

            // --- NEW: Expanded list of all connections for a full skeleton ---
            val connections = listOf(
                // Face
                Pair(LandmarkIndices.NOSE, LandmarkIndices.LEFT_EYE_INNER),
                Pair(LandmarkIndices.LEFT_EYE_INNER, LandmarkIndices.LEFT_EYE),
                Pair(LandmarkIndices.LEFT_EYE, LandmarkIndices.LEFT_EYE_OUTER),
                Pair(LandmarkIndices.LEFT_EYE_OUTER, LandmarkIndices.LEFT_EAR),
                Pair(LandmarkIndices.NOSE, LandmarkIndices.RIGHT_EYE_INNER),
                Pair(LandmarkIndices.RIGHT_EYE_INNER, LandmarkIndices.RIGHT_EYE),
                Pair(LandmarkIndices.RIGHT_EYE, LandmarkIndices.RIGHT_EYE_OUTER),
                Pair(LandmarkIndices.RIGHT_EYE_OUTER, LandmarkIndices.RIGHT_EAR),
                Pair(LandmarkIndices.MOUTH_LEFT, LandmarkIndices.MOUTH_RIGHT),

                // Body
                Pair(LandmarkIndices.LEFT_SHOULDER, LandmarkIndices.RIGHT_SHOULDER),
                Pair(LandmarkIndices.LEFT_HIP, LandmarkIndices.RIGHT_HIP),
                Pair(LandmarkIndices.LEFT_SHOULDER, LandmarkIndices.LEFT_HIP),
                Pair(LandmarkIndices.RIGHT_SHOULDER, LandmarkIndices.RIGHT_HIP),

                // Arms
                Pair(LandmarkIndices.LEFT_SHOULDER, LandmarkIndices.LEFT_ELBOW),
                Pair(LandmarkIndices.LEFT_ELBOW, LandmarkIndices.LEFT_WRIST),
                Pair(LandmarkIndices.RIGHT_SHOULDER, LandmarkIndices.RIGHT_ELBOW),
                Pair(LandmarkIndices.RIGHT_ELBOW, LandmarkIndices.RIGHT_WRIST),

                // Hands (simplified)
                Pair(LandmarkIndices.LEFT_WRIST, LandmarkIndices.LEFT_THUMB_CMC),
                Pair(LandmarkIndices.LEFT_WRIST, LandmarkIndices.LEFT_INDEX_MCP),
                Pair(LandmarkIndices.LEFT_WRIST, LandmarkIndices.LEFT_PINKY_MCP),
                Pair(LandmarkIndices.RIGHT_WRIST, LandmarkIndices.RIGHT_THUMB_CMC),
                Pair(LandmarkIndices.RIGHT_WRIST, LandmarkIndices.RIGHT_INDEX_MCP),
                Pair(LandmarkIndices.RIGHT_WRIST, LandmarkIndices.RIGHT_PINKY_MCP),


                // Legs
                Pair(LandmarkIndices.LEFT_HIP, LandmarkIndices.LEFT_KNEE),
                Pair(LandmarkIndices.LEFT_KNEE, LandmarkIndices.LEFT_ANKLE),
                Pair(LandmarkIndices.RIGHT_HIP, LandmarkIndices.RIGHT_KNEE),
                Pair(LandmarkIndices.RIGHT_KNEE, LandmarkIndices.RIGHT_ANKLE),

                // Feet
                Pair(LandmarkIndices.LEFT_ANKLE, LandmarkIndices.LEFT_HEEL),
                Pair(LandmarkIndices.LEFT_HEEL, LandmarkIndices.LEFT_FOOT_INDEX),
                Pair(LandmarkIndices.LEFT_ANKLE, LandmarkIndices.LEFT_FOOT_INDEX),
                Pair(LandmarkIndices.RIGHT_ANKLE, LandmarkIndices.RIGHT_HEEL),
                Pair(LandmarkIndices.RIGHT_HEEL, LandmarkIndices.RIGHT_FOOT_INDEX),
                Pair(LandmarkIndices.RIGHT_ANKLE, LandmarkIndices.RIGHT_FOOT_INDEX)
            )

            connections.forEach { (startIndex, endIndex) ->
                if (startIndex < scaledLandmarks.size && endIndex < scaledLandmarks.size) {
                    // Only draw if both points are reasonably visible
                    if (landmarkList[startIndex].visibility().orElse(0f) > 0.5f &&
                        landmarkList[endIndex].visibility().orElse(0f) > 0.5f) {
                        drawLine(canvas, scaledLandmarks[startIndex], scaledLandmarks[endIndex])
                    }
                }
            }

            // --- NEW: Draw all visible landmarks ---
            scaledLandmarks.forEachIndexed { index, point ->
                // Check the visibility score from the model.
                // A score of > 0.5 is generally considered "visible".
                if (landmarkList[index].visibility().orElse(0f) > 0.5f) {
                    drawPoint(canvas, point)
                }
            }

            // Draw angle information (this logic remains the same)
            drawAngleInformation(canvas, scaledLandmarks)
        }
    }

    private fun drawAngleInformation(canvas: Canvas, scaledLandmarks: List<PointF>) {
        val angleJointMap = mapOf(
            "Left Elbow Angle" to LandmarkIndices.LEFT_ELBOW,
            "Right Elbow Angle" to LandmarkIndices.RIGHT_ELBOW,
            "Left Knee Angle" to LandmarkIndices.LEFT_KNEE,
            "Right Knee Angle" to LandmarkIndices.RIGHT_KNEE,
            "Left Shoulder Angle" to LandmarkIndices.LEFT_SHOULDER,
            "Right Shoulder Angle" to LandmarkIndices.RIGHT_SHOULDER,
            "Left Hip Angle" to LandmarkIndices.LEFT_HIP,
            "Right Hip Angle" to LandmarkIndices.RIGHT_HIP
        )

        val angleTextOffsetY = -40f

        anglesToDisplay.forEach { (angleName, angleValue) ->
            val jointIndex = angleJointMap[angleName]
            if (jointIndex != null && jointIndex < scaledLandmarks.size) {
                val jointPoint = scaledLandmarks[jointIndex]
                val angleText = "%.0f°".format(angleValue)

                canvas.drawText(
                    angleText,
                    jointPoint.x,
                    jointPoint.y + angleTextOffsetY,
                    angleTextPaint
                )
            }
        }
    }
}
