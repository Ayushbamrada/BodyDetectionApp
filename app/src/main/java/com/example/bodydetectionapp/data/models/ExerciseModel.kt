package com.example.bodydetectionapp.data.models

data class Landmark(val x: Float, val y: Float, val z: Float)

enum class ExerciseType {
    SYMMETRICAL,
    ALTERNATING
}

enum class CameraView {
    FRONT,
    SIDE
}

enum class BodyPart {
    UPPER_BODY,
    LOWER_BODY,
    CORE,
    FULL_BODY,
    YOGA
}

sealed interface FormRule {
    val feedbackMessage: String
}

data class AngleRule(
    val angleName: String,
    val minAngle: Double,
    val maxAngle: Double,
    override val feedbackMessage: String
) : FormRule

data class HorizontalAlignmentRule(
    val landmark1: String,
    val landmark2: String,
    val maxDistanceRatio: Float,
    override val feedbackMessage: String
) : FormRule

data class DistanceRule(
    val landmark1: String,
    val landmark2: String,
    val maxDistanceRatio: Float,
    override val feedbackMessage: String
) : FormRule

sealed interface PrimaryMovement {
    val entryThreshold: Double
    val exitThreshold: Double
}

data class AngleMovement(
    val keyJointsToTrack: List<String>,
    override val entryThreshold: Double,
    override val exitThreshold: Double
) : PrimaryMovement

data class DistanceMovement(
    val landmark1: String,
    val landmark2: String,
    override val entryThreshold: Double,
    override val exitThreshold: Double
) : PrimaryMovement

data class Exercise(
    val name: String,
    val description: String,
    val primaryMovement: PrimaryMovement,
    val metValue: Double,
    val type: ExerciseType,
    val cameraView: CameraView,
    val bodyPart: BodyPart,
    val formRules: List<FormRule>,
    val videoResId: Int? = null
)
