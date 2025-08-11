package com.example.bodydetectionapp.utils

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.acos
import kotlin.math.sqrt

object AngleCalculator {

    /**
     * Calculates the angle in degrees between three NormalizedLandmark points.
     * The angle is formed at the 'mid' landmark (p2).
     *
     * @param p1 The first landmark.
     * @param p2 The middle landmark (joint).
     * @param p3 The third landmark.
     * @return The angle in degrees (0 to 180), or NaN if any points are too close.
     */
    fun calculateAngle(p1: NormalizedLandmark, p2: NormalizedLandmark, p3: NormalizedLandmark): Double {
        // Vectors from mid point (p2) to p1 and p3
        val v1x = p1.x() - p2.x()
        val v1y = p1.y() - p2.y()
        val v2x = p3.x() - p2.x()
        val v2y = p3.y() - p2.y()

        // Dot product
        val dotProduct = (v1x * v2x + v1y * v2y).toDouble()

        // Magnitudes (lengths of vectors)
        val magV1 = sqrt((v1x * v1x + v1y * v1y).toDouble())
        val magV2 = sqrt((v2x * v2x + v2y * v2y).toDouble())

        // Avoid division by zero if points are identical or extremely close
        if (magV1 == 0.0 || magV2 == 0.0) {
            return Double.NaN
        }

        // Cosine of the angle
        var cosTheta = dotProduct / (magV1 * magV2)

        // Clamp cosTheta to [-1, 1] to avoid NaN from acos due to floating point inaccuracies
        cosTheta = cosTheta.coerceIn(-1.0, 1.0)

        // Angle in radians and then degrees
        val angleRad = acos(cosTheta)
        return Math.toDegrees(angleRad)
    }

    // You can define constants for landmark indices for better readability
    // These are based on MediaPipe Pose 33 landmarks, as used in your PoseOverlay.
    object LandmarkIndices {
        // Face landmarks (0-10) - usually not used for exercise angles, but good to know
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

        // Upper body
        const val LEFT_SHOULDER = 11
        const val RIGHT_SHOULDER = 12
        const val LEFT_ELBOW = 13
        const val RIGHT_ELBOW = 14
        const val LEFT_WRIST = 15
        const val RIGHT_WRIST = 16

        // Hand landmarks (simplified, typically for 33 landmark model)
        const val LEFT_THUMB_CMC = 17
        const val RIGHT_THUMB_CMC = 18
        const val LEFT_INDEX_MCP = 19
        const val RIGHT_INDEX_MCP = 20
        const val LEFT_PINKY_MCP = 21
        const val RIGHT_PINKY_MCP = 22

        // Lower body
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
    }

    /**
     * Calculates a predefined set of common exercise-relevant angles from a PoseLandmarkerResult.
     * This method uses the LandmarkIndices constants for clarity.
     *
     * @param landmarks The list of NormalizedLandmark points from a single pose.
     * @return A Map of angle names (String) to their calculated degree values (Double).
     */
    fun getExerciseAngles(landmarks: List<NormalizedLandmark>): Map<String, Double> {
        val angles = mutableMapOf<String, Double>()

        // Ensure we have enough landmarks before attempting to calculate angles
        if (landmarks.size < 33) {
            return emptyMap()
        }

        with(LandmarkIndices) {
            // Elbow Angles
            angles["Left Elbow Angle"] = calculateAngle(landmarks[LEFT_SHOULDER], landmarks[LEFT_ELBOW], landmarks[LEFT_WRIST])
            angles["Right Elbow Angle"] = calculateAngle(landmarks[RIGHT_SHOULDER], landmarks[RIGHT_ELBOW], landmarks[RIGHT_WRIST])

            // Knee Angles
            angles["Left Knee Angle"] = calculateAngle(landmarks[LEFT_HIP], landmarks[LEFT_KNEE], landmarks[LEFT_ANKLE])
            angles["Right Knee Angle"] = calculateAngle(landmarks[RIGHT_HIP], landmarks[RIGHT_KNEE], landmarks[RIGHT_ANKLE])

            // Shoulder Angles (often involves hip as a reference for torso line)
            // This is the angle formed at the shoulder, between the torso (hip-shoulder line) and the upper arm (shoulder-elbow line).
            angles["Left Shoulder Angle"] = calculateAngle(landmarks[LEFT_HIP], landmarks[LEFT_SHOULDER], landmarks[LEFT_ELBOW])
            angles["Right Shoulder Angle"] = calculateAngle(landmarks[RIGHT_HIP], landmarks[RIGHT_SHOULDER], landmarks[RIGHT_ELBOW])

            // Hip Angles (for squat, deadlift, etc. - connecting shoulder, hip, and knee)
            // This is the angle formed at the hip, between the torso (shoulder-hip line) and the thigh (hip-knee line).
            angles["Left Hip Angle"] = calculateAngle(landmarks[LEFT_SHOULDER], landmarks[LEFT_HIP], landmarks[LEFT_KNEE])
            angles["Right Hip Angle"] = calculateAngle(landmarks[RIGHT_SHOULDER], landmarks[RIGHT_HIP], landmarks[RIGHT_KNEE])

            // Ankle Angles (for squats, calf raises) - connecting knee, ankle, foot index/heel
            // This is the angle formed at the ankle, between the lower leg (knee-ankle line) and the foot (ankle-foot_index line).
            angles["Left Ankle Angle"] = calculateAngle(landmarks[LEFT_KNEE], landmarks[LEFT_ANKLE], landmarks[LEFT_FOOT_INDEX])
            angles["Right Ankle Angle"] = calculateAngle(landmarks[RIGHT_KNEE], landmarks[RIGHT_ANKLE], landmarks[RIGHT_FOOT_INDEX])

            // Torso Angle: This can be interpreted in several ways.
            // A common interpretation for a "torso angle" related to posture or lean is
            // the angle between the line formed by (shoulder-hip) and a vertical axis.
            // In 2D, a simpler approximation is the angle formed by (shoulder, hip, knee)
            // which indicates forward lean. Let's keep your current definition as it aligns
            // with detecting torso lean in squats.
            angles["Torso Angle"] = calculateAngle(landmarks[LEFT_SHOULDER], landmarks[LEFT_HIP], landmarks[LEFT_KNEE])
            // Note: For a truly "vertical" torso angle, you'd usually use a fixed point in the camera frame
            // as the third point (e.g., (hip.x, hip.y - 1) for a point directly above the hip),
            // or rely on 3D landmarks if available.
        }

        return angles
    }
}