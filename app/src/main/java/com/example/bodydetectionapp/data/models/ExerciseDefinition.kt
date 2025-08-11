package com.example.bodydetectionapp.data.models

// Define all your exercises here
object ExerciseDefinitions {

    val SQUAT = Exercise(
        name = "Squat",
        description = "A full-body exercise that trains the hips, thighs, and glutes.",
        phases = listOf(
            ExercisePhase(
                name = "Starting Position",
                targetAngles = mapOf(
                    "Left Knee Angle" to AngleRange(160.0, 180.0), // Legs straight
                    "Right Knee Angle" to AngleRange(160.0, 180.0),
                    "Left Hip Angle" to AngleRange(160.0, 180.0), // Torso upright
                    "Right Hip Angle" to AngleRange(160.0, 180.0)
                ),
                feedbackMessage = "Stand tall, chest up, core engaged."
            ),
            ExercisePhase(
                name = "Lowering Phase",
                targetAngles = mapOf(
                    "Left Knee Angle" to AngleRange(80.0, 110.0), // Knees bent for squat depth
                    "Right Knee Angle" to AngleRange(80.0, 110.0),
                    "Left Hip Angle" to AngleRange(70.0, 100.0), // Hips dropping
                    "Right Hip Angle" to AngleRange(70.0, 100.0)
                ),
                feedbackMessage = "Lower hips as if sitting back into a chair."
            ),
            ExercisePhase(
                name = "Bottom Position",
                targetAngles = mapOf(
                    "Left Knee Angle" to AngleRange(70.0, 95.0), // Deepest point
                    "Right Knee Angle" to AngleRange(70.0, 95.0),
                    "Left Hip Angle" to AngleRange(60.0, 90.0),
                    "Right Hip Angle" to AngleRange(60.0, 90.0)
                ),
                feedbackMessage = "Keep your back straight and chest up at the bottom."
            ),
            ExercisePhase(
                name = "Ascending Phase",
                targetAngles = mapOf( // Angles will be increasing towards starting position
                    "Left Knee Angle" to AngleRange(110.0, 150.0), // Indicative range
                    "Right Knee Angle" to AngleRange(110.0, 150.0)
                ),
                feedbackMessage = "Push through your heels to stand up."
            )
        )
    )

    val HAND_RAISING = Exercise(
        name = "Hand Raising",
        description = "Raise arms forward or to the side.",
        phases = listOf(
            ExercisePhase(
                name = "Arms Down",
                targetAngles = mapOf(
                    "Left Elbow Angle" to AngleRange(160.0, 180.0), // Straight arm
                    "Right Elbow Angle" to AngleRange(160.0, 180.0),
                    "Left Shoulder Angle" to AngleRange(160.0, 180.0), // Arm down by side
                    "Right Shoulder Angle" to AngleRange(160.0, 180.0)
                ),
                feedbackMessage = "Arms by your side, ready to raise."
            ),
            ExercisePhase(
                name = "Arms Up",
                targetAngles = mapOf(
                    "Left Shoulder Angle" to AngleRange(70.0, 90.0), // Arm raised to 90 degrees (or overhead)
                    "Right Shoulder Angle" to AngleRange(70.0, 90.0)
                    // Elbows should still be straight (160-180) for a straight-arm raise
                ),
                feedbackMessage = "Raise arms directly in front or to the side, maintaining straight elbows."
            )
        )
    )

    // Add more exercises here as you create them
    val ALL_EXERCISES = listOf(SQUAT, HAND_RAISING)
}