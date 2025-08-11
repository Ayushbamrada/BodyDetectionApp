package com.example.bodydetectionapp.data.models

/**
 * Represents a range for an angle, with a minimum and maximum value.
 */
data class AngleRange(val min: Double, val max: Double)

/**
 * Defines a specific phase within an exercise (e.g., "Starting Position", "Bottom of Squat").
 *
 * @param name The name of the phase.
 * @param targetAngles A map of angle names (e.g., "Left Knee Angle") to their target AngleRange.
 * @param feedbackMessage An optional message to display when the user is in this phase.
 */
data class ExercisePhase(
    val name: String,
    val targetAngles: Map<String, AngleRange>,
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
    val phases: List<ExercisePhase>
)