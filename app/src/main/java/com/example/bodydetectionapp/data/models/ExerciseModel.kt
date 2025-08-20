//package com.example.bodydetectionapp.data.models
//
///**
// * A simple data class to hold 3D landmark coordinates.
// */
//data class Landmark(val x: Float, val y: Float, val z: Float)
//
///**
// * Defines the different types of exercises.
// * SYMMETRICAL: Both sides of the body do the same thing (e.g., Squat).
// * ALTERNATING: Left and right sides alternate (e.g., Marching).
// */
//enum class ExerciseType {
//    SYMMETRICAL,
//    ALTERNATING
//}
//
///**
// * A sealed interface representing a rule that must be followed during an exercise
// * for a repetition to be considered valid.
// */
//sealed interface FormRule {
//    val feedbackMessage: String
//}
//
///**
// * A rule that checks if a specific joint angle stays within a valid range.
// * @param angleName The name of the angle to check (e.g., "Left Elbow Angle").
// * @param minAngle The minimum acceptable angle.
// * @param maxAngle The maximum acceptable angle.
// */
//data class AngleRule(
//    val angleName: String,
//    val minAngle: Double,
//    val maxAngle: Double,
//    override val feedbackMessage: String
//) : FormRule
//
///**
// * A rule that checks if two landmarks stay horizontally aligned.
// * @param landmark1 The name of the first landmark (e.g., "LEFT_KNEE").
// * @param landmark2 The name of the second landmark (e.g., "LEFT_HIP").
// * @param maxDistanceRatio The maximum allowed horizontal distance, as a fraction of shoulder width.
// */
//data class HorizontalAlignmentRule(
//    val landmark1: String,
//    val landmark2: String,
//    val maxDistanceRatio: Float,
//    override val feedbackMessage: String
//) : FormRule
//
///**
// * Defines the core mechanics of the primary movement for an exercise.
// */
//data class RepCounter(
//    val keyJointsToTrack: List<String>,
//    val entryThreshold: Double,
//    val exitThreshold: Double
//)
//
///**
// * The complete "Exercise Blueprint".
// * @param formRules A list of FormRules that must be maintained during the rep.
// */
//data class Exercise(
//    val name: String,
//    val description: String,
//    val repCounter: RepCounter,
//    val metValue: Double,
//    val type: ExerciseType,
//    val formRules: List<FormRule>,
//    val videoResId: Int? = null
//)
package com.example.bodydetectionapp.data.models

/**
 * A simple data class to hold 3D landmark coordinates.
 */
data class Landmark(val x: Float, val y: Float, val z: Float)

/**
 * Defines the different types of exercises.
 * SYMMETRICAL: Both sides of the body do the same thing (e.g., Squat).
 * ALTERNATING: Left and right sides alternate (e.g., Marching).
 */
enum class ExerciseType {
    SYMMETRICAL,
    ALTERNATING
}

/**
 * A sealed interface representing a rule that must be followed during an exercise
 * for a repetition to be considered valid.
 */
sealed interface FormRule {
    val feedbackMessage: String
}

/**
 * A rule that checks if a specific joint angle stays within a valid range.
 */
data class AngleRule(
    val angleName: String,
    val minAngle: Double,
    val maxAngle: Double,
    override val feedbackMessage: String
) : FormRule

/**
 * A rule that checks if two landmarks stay horizontally aligned.
 */
data class HorizontalAlignmentRule(
    val landmark1: String,
    val landmark2: String,
    val maxDistanceRatio: Float,
    override val feedbackMessage: String
) : FormRule

/**
 * NEW: A rule that checks the distance between two landmarks.
 * @param maxDistanceRatio The maximum allowed distance, as a fraction of shoulder width.
 */
data class DistanceRule(
    val landmark1: String,
    val landmark2: String,
    val maxDistanceRatio: Float,
    override val feedbackMessage: String
) : FormRule


/**
 * NEW: A sealed interface to define the primary movement of an exercise.
 * This replaces the old RepCounter.
 */
sealed interface PrimaryMovement {
    val entryThreshold: Double
    val exitThreshold: Double
}

/**
 * Defines an angle-based primary movement.
 */
data class AngleMovement(
    val keyJointsToTrack: List<String>,
    override val entryThreshold: Double,
    override val exitThreshold: Double
) : PrimaryMovement

/**
 * Defines a distance-based primary movement (for crunches).
 */
data class DistanceMovement(
    val landmark1: String,
    val landmark2: String,
    override val entryThreshold: Double, // For distance, this is the "close enough" threshold
    override val exitThreshold: Double  // And this is the "far enough away" threshold
) : PrimaryMovement

/**
 * The complete "Exercise Blueprint".
 */
data class Exercise(
    val name: String,
    val description: String,
    val primaryMovement: PrimaryMovement, // UPDATED: Replaced RepCounter
    val metValue: Double,
    val type: ExerciseType,
    val formRules: List<FormRule>,
    val videoResId: Int? = null
)
