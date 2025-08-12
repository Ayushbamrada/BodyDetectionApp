//package com.example.bodydetectionapp.data.models
//
///**
// * Represents a range for an angle, with a minimum and maximum value.
// */
//data class AngleRange(val min: Double, val max: Double)
//
///**
// * Defines a specific phase within an exercise (e.g., "Starting Position", "Bottom of Squat").
// *
// * @param name The name of the phase.
// * @param targetAngles A map of angle names (e.g., "Left Knee Angle") to their target AngleRange.
// * @param feedbackMessage An optional message to display when the user is in this phase.
// */
//data class ExercisePhase(
//    val name: String,
//    val targetAngles: Map<String, AngleRange>,
//    val feedbackMessage: String? = null
//)
//
///**
// * Represents a complete exercise with its name, description, and sequence of phases.
// *
// * @param name The name of the exercise.
// * @param description A brief description of the exercise.
// * @param phases The ordered list of ExercisePhase objects that define the exercise.
// */
//data class Exercise(
//    val name: String,
//    val description: String,
//    val phases: List<ExercisePhase>
//)
package com.example.bodydetectionapp.data.models

/**
 * Represents a range for an angle, with a minimum and maximum value.
 */
data class AngleRange(val min: Double, val max: Double)

/**
 * Defines a target for an angle based on its change relative to an initial angle.
 * This is used for tracking movements where the start position is dynamic.
 *
 * @param angleName The name of the angle (e.g., "Left Knee Angle").
 * @param minRelativeAngle The minimum acceptable change (in degrees) from the initial angle.
 * Can be negative for a decrease (e.g., bending).
 * @param maxRelativeAngle The maximum acceptable change (in degrees) from the initial angle.
 */
data class RelativeAngleTarget(
    val angleName: String,
    val minRelativeAngle: Double,
    val maxRelativeAngle: Double
)

/**
 * Defines a specific phase within an exercise (e.g., "Starting Position", "Bottom of Squat").
 *
 * @param name The name of the phase.
 * @param targetAngles A map of angle names (e.g., "Left Knee Angle") to their **absolute** target AngleRange.
 * Used for fixed positions (e.g., "Torso straight", or to *guide* initial position).
 * @param relativeTargetAngles A list of [RelativeAngleTarget]s, defining required *changes* from the
 * user's dynamically captured initial angles. Used for actual movement tracking.
 * @param feedbackMessage An optional message to display when the user is in this phase.
 */
data class ExercisePhase(
    val name: String,
    val targetAngles: Map<String, AngleRange> = emptyMap(), // Default to emptyMap for phases that primarily use relative targets
    val relativeTargetAngles: List<RelativeAngleTarget> = emptyList(), // New field
    val feedbackMessage: String? = null
)

/**
 * Represents a complete exercise with its name, description, and sequence of phases.
 *
 * @param name The name of the exercise.
 * @param description A brief description of the exercise.
 * @param phases The ordered list of ExercisePhase objects that define the exercise.
 */
data class Exercise(
    val name: String,
    val description: String,
    val phases: List<ExercisePhase>,
    val videoResId: Int? = null // Optional: For linking to a demo video
)