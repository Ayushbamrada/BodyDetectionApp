package com.example.bodydetectionapp.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.view.View
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

class PoseOverlay(context: Context) : View(context) {

    // Store the pose detection result. Invalidate view to trigger redraw.
    var poseResult: PoseLandmarkerResult? = null
        set(value) {
            field = value
            invalidate()
        }

    // A set of landmark indices that should be highlighted (e.g., if they are moving)
    var highlightedJointIndices: Set<Int> = emptySet()
        set(value) {
            field = value
            invalidate() // Redraw when highlighted joints change
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

        poseResult?.landmarks()?.firstOrNull()?.let { landmarkList ->
            // Scale landmarks to view dimensions
            val scaledLandmarks = landmarkList.mapIndexed { index, landmark ->
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
                Pair(11, 13), // Left Shoulder to Left Elbow
                Pair(13, 15), // Left Elbow to Left Wrist
                Pair(12, 14), // Right Shoulder to Right Elbow
                Pair(14, 16), // Right Elbow to Right Wrist
                Pair(11, 12), // Shoulders (connects top of torso)
                Pair(23, 24), // Hips (connects bottom of torso)
                Pair(11, 23), // Left Shoulder to Left Hip
                Pair(12, 24), // Right Shoulder to Right Hip

                // Head/Face (Basic connections for a skeleton head)
                Pair(0, 1), Pair(1, 2), Pair(2, 3), Pair(3, 7), // Left side of face/head
                Pair(0, 4), Pair(4, 5), Pair(5, 6), Pair(6, 8), // Right side of face/head
                Pair(9, 10), // Mouth/jaw area
                Pair(7, 8), // Connect ears (or general head width)
                Pair(0, 7), Pair(0, 8), // Connect nose to ears/head points
//                Pair(7, 11), Pair(8, 12), // Connect head to shoulders (e.g., ear to shoulder)

                // Left Hand (simplified for basic skeleton, connecting wrist to finger tips)
                Pair(15, 17), // Left Wrist to Left Thumb CMC
                Pair(17, 19), // Left Thumb CMC to Left Index Finger MCP
                Pair(19, 21), // Left Index Finger MCP to Left Pinky Finger MCP
                // More precise finger segments would require distinct PIP/DIP landmarks if available,
                // but for a general skeleton, these create a good hand shape.
                // Reconnecting individual fingers to wrist
                Pair(15, 17), // Left wrist to thumb base
                Pair(15, 19), // Left wrist to index base
                Pair(15, 21), // Left wrist to pinky base


                // Right Hand (simplified for basic skeleton, connecting wrist to finger tips)
                Pair(16, 18), // Right Wrist to Right Thumb CMC
                Pair(18, 20), // Right Thumb CMC to Right Index Finger MCP
                Pair(20, 22), // Right Index Finger MCP to Right Pinky Finger MCP
                // Reconnecting individual fingers to wrist
                Pair(16, 18), // Right wrist to thumb base
                Pair(16, 20), // Right wrist to index base
                Pair(16, 22), // Right wrist to pinky base

                // Left Leg
                Pair(23, 25), // Left Hip to Left Knee
                Pair(25, 27), // Left Knee to Left Ankle
                Pair(27, 29), // Left Ankle to Left Heel
                Pair(29, 31), // Left Heel to Left Foot Index (Ball of foot)
                Pair(27, 31), // Left Ankle to Left Foot Index (for foot outline)
                Pair(31, 29), // Left Foot Index to Left Heel (closing foot outline)

                // Right Leg
                Pair(24, 26), // Right Hip to Right Knee
                Pair(26, 28), // Right Knee to Right Ankle
                Pair(28, 30), // Right Ankle to Right Heel
                Pair(30, 32), // Right Heel to Right Foot Index (Ball of foot)
                Pair(28, 32), // Right Ankle to Right Foot Index (for foot outline)
                Pair(32, 30) // Right Foot Index to Right Heel (closing foot outline)
            )

            // Draw lines between connected joints
            connections.forEach { (startIndex, endIndex) ->
                // Ensure indices are within bounds of the scaledLandmarks list
                if (startIndex < scaledLandmarks.size && endIndex < scaledLandmarks.size) {
                    drawLine(canvas, scaledLandmarks[startIndex], scaledLandmarks[endIndex])
                }
            }
        }
    }
}