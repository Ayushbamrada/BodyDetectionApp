package com.example.bodydetectionapp.utils

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.acos
import kotlin.math.sqrt

object AngleCalculator {

    /**
     * Calculates the angle in degrees between three NormalizedLandmark points.
     */
    fun calculateAngle(p1: NormalizedLandmark, p2: NormalizedLandmark, p3: NormalizedLandmark): Double {
        val v1x = p1.x() - p2.x()
        val v1y = p1.y() - p2.y()
        val v2x = p3.x() - p2.x()
        val v2y = p3.y() - p2.y()

        val dotProduct = (v1x * v2x + v1y * v2y).toDouble()
        val magV1 = sqrt((v1x * v1x + v1y * v1y).toDouble())
        val magV2 = sqrt((v2x * v2x + v2y * v2y).toDouble())

        if (magV1 == 0.0 || magV2 == 0.0) {
            return Double.NaN
        }

        var cosTheta = dotProduct / (magV1 * magV2)
        cosTheta = cosTheta.coerceIn(-1.0, 1.0)

        val angleRad = acos(cosTheta)
        return Math.toDegrees(angleRad)
    }

    object LandmarkIndices {
        const val NOSE = 0
        const val LEFT_EYE_INNER = 1
        const val LEFT_EYE = 2
        const val LEFT_EYE_OUTER = 3
        const val RIGHT_EYE_INNER = 4
        const val RIGHT_EYE = 5
        const val RIGHT_EYE_OUTER = 6
        const val LEFT_EAR = 7
        const val RIGHT_EAR = 8
        const val MOUTH_LEFT = 9
        const val MOUTH_RIGHT = 10
        const val LEFT_SHOULDER = 11
        const val RIGHT_SHOULDER = 12
        const val LEFT_ELBOW = 13
        const val RIGHT_ELBOW = 14
        const val LEFT_WRIST = 15
        const val RIGHT_WRIST = 16
        const val LEFT_THUMB_CMC = 17
        const val RIGHT_THUMB_CMC = 18
        const val LEFT_INDEX_MCP = 19
        const val RIGHT_INDEX_MCP = 20
        const val LEFT_PINKY_MCP = 21
        const val RIGHT_PINKY_MCP = 22
        const val LEFT_HIP = 23
        const val RIGHT_HIP = 24
        const val LEFT_KNEE = 25
        const val RIGHT_KNEE = 26
        const val LEFT_ANKLE = 27
        const val RIGHT_ANKLE = 28
        const val LEFT_HEEL = 29
        const val RIGHT_HEEL = 30
        const val LEFT_FOOT_INDEX = 31
        const val RIGHT_FOOT_INDEX = 32

        // --- NEW: This map is crucial for the PoseDetectionHelper ---
        val landmarkNameMapping = mapOf(
            LEFT_SHOULDER to "LEFT_SHOULDER",
            RIGHT_SHOULDER to "RIGHT_SHOULDER",
            LEFT_ELBOW to "LEFT_ELBOW",
            RIGHT_ELBOW to "RIGHT_ELBOW",
            LEFT_WRIST to "LEFT_WRIST",
            RIGHT_WRIST to "RIGHT_WRIST",
            LEFT_HIP to "LEFT_HIP",
            RIGHT_HIP to "RIGHT_HIP",
            LEFT_KNEE to "LEFT_KNEE",
            RIGHT_KNEE to "RIGHT_KNEE",
            LEFT_ANKLE to "LEFT_ANKLE",
            RIGHT_ANKLE to "RIGHT_ANKLE"
        )
    }

    /**
     * Calculates all relevant exercise angles from a list of landmarks.
     */
    fun calculateAllAngles(landmarks: List<NormalizedLandmark>): Map<String, Double> {
        val angles = mutableMapOf<String, Double>()

        if (landmarks.size < 33) {
            return emptyMap()
        }

        with(LandmarkIndices) {
            angles["Left Elbow Angle"] = calculateAngle(landmarks[LEFT_SHOULDER], landmarks[LEFT_ELBOW], landmarks[LEFT_WRIST])
            angles["Right Elbow Angle"] = calculateAngle(landmarks[RIGHT_SHOULDER], landmarks[RIGHT_ELBOW], landmarks[RIGHT_WRIST])
            angles["Left Knee Angle"] = calculateAngle(landmarks[LEFT_HIP], landmarks[LEFT_KNEE], landmarks[LEFT_ANKLE])
            angles["Right Knee Angle"] = calculateAngle(landmarks[RIGHT_HIP], landmarks[RIGHT_KNEE], landmarks[RIGHT_ANKLE])
            angles["Left Shoulder Angle"] = calculateAngle(landmarks[LEFT_HIP], landmarks[LEFT_SHOULDER], landmarks[LEFT_ELBOW])
            angles["Right Shoulder Angle"] = calculateAngle(landmarks[RIGHT_HIP], landmarks[RIGHT_SHOULDER], landmarks[RIGHT_ELBOW])
            angles["Left Hip Angle"] = calculateAngle(landmarks[LEFT_SHOULDER], landmarks[LEFT_HIP], landmarks[LEFT_KNEE])
            angles["Right Hip Angle"] = calculateAngle(landmarks[RIGHT_SHOULDER], landmarks[RIGHT_HIP], landmarks[RIGHT_KNEE])
            angles["Left Ankle Angle"] = calculateAngle(landmarks[LEFT_KNEE], landmarks[LEFT_ANKLE], landmarks[LEFT_FOOT_INDEX])
            angles["Right Ankle Angle"] = calculateAngle(landmarks[RIGHT_KNEE], landmarks[RIGHT_ANKLE], landmarks[RIGHT_FOOT_INDEX])
            angles["Torso Angle"] = calculateAngle(landmarks[LEFT_SHOULDER], landmarks[LEFT_HIP], landmarks[LEFT_KNEE])
        }

        return angles
    }
}
