package com.example.bodydetectionapp.data.models

data class Landmark(val x: Float, val y: Float, val z: Float, val visibility: Float? = null)

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

/**
 * Defines a single exercise.
 *
 * @param name The display name of the exercise.
 * @param description A brief description of the exercise.
 * @param startInstruction The initial voice prompt to guide the user into the starting position.
 * @param primaryMovementInstruction A corrective voice prompt given if the user gets stuck mid-rep.
 * @param requiredLandmarks A list of landmark names that MUST be visible for the exercise to be tracked.
 * @param visibilityFeedbackMessage The feedback message to display if any required landmarks are not visible.
 * @param primaryMovement Defines the core movement for counting a repetition.
 * @param metValue The Metabolic Equivalent of Task value for calorie calculation.
 * @param type Whether the exercise is symmetrical (e.g., squat) or alternating (e.g., lunges).
 * @param cameraView The recommended camera view for tracking this exercise.
 * @param bodyPart The main body part targeted.
 * @param formRules A list of rules to check for proper form during the exercise.
 * @param videoResId Optional resource ID for a demonstration video or GIF.
 */
data class Exercise(
    val name: String,
    val description: String,
    val startInstruction: String,
    val primaryMovementInstruction: String,
    // --- NEW PROPERTIES TO PREVENT PHANTOM REPS ---
    val requiredLandmarks: List<String>,
    val visibilityFeedbackMessage: String,
    val requiresHighPrecision: Boolean = false, // Default to false for high speed

    // ---
    val primaryMovement: PrimaryMovement,
    val metValue: Double,
    val type: ExerciseType,
    val cameraView: CameraView,
    val bodyPart: BodyPart,
    val formRules: List<FormRule>,
    val videoResId: Int? = null
)