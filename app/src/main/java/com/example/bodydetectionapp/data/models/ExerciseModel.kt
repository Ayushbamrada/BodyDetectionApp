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
 * A simple data class to hold 3D landmark coordinates.
 * This will be needed by the evaluator for gesture detection.
 * MediaPipe provides these coordinates.
 */
data class Landmark(val x: Float, val y: Float, val z: Float)

/**
 * An enum to represent the state of a repetition counter.
 * It tracks whether the user is in the starting position, performing the
 * primary movement (e.g., going down in a squat), or returning.
 */
enum class RepetitionState {
    IDLE,       // Neutral state, ready for a rep
    IN_PROGRESS // User is in the middle of a rep (e.g., down in a squat)
}

/**
 * Defines the core mechanics of an exercise for the cyclical rep counter.
 *
 * @param keyJointsToTrack A list of joint angle names (e.g., "Left Knee Angle") to monitor.
 * The evaluator will average them or use the most prominent one.
 * @param entryThreshold The angle threshold (in degrees) to start a repetition.
 * For a squat, this is the angle you must go *below* to start the rep.
 * @param exitThreshold The angle threshold (in degrees) to complete a repetition.
 * For a squat, this is the angle you must go *above* to finish the rep.
 */
data class RepCounter(
    val keyJointsToTrack: List<String>,
    val entryThreshold: Double,
    val exitThreshold: Double
)

/**
 * Represents a complete exercise with its name, description, and the new RepCounter logic.
 *
 * @param name The name of the exercise.
 * @param description A brief description of the exercise.
 * @param repCounter The RepCounter object that defines how to count reps for this exercise.
 * @param videoResId Optional: For linking to a demo video.
 */
data class Exercise(
    val name: String,
    val description: String,
    val repCounter: RepCounter,
    val metValue: Double, // NEW: Added for calorie calculation
    val videoResId: Int? = null
)