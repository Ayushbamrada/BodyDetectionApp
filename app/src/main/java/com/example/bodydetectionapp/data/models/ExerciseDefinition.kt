//package com.example.bodydetectionapp.data.models
//
//// Define all your exercises here
//object ExerciseDefinitions {
//
//    val SQUAT = Exercise(
//        name = "Squat",
//        description = "A full-body exercise that trains the hips, thighs, and glutes.",
//        phases = listOf(
//            ExercisePhase(
//                name = "Starting Position",
//                targetAngles = mapOf(
//                    "Left Knee Angle" to AngleRange(160.0, 180.0), // Legs straight
//                    "Right Knee Angle" to AngleRange(160.0, 180.0),
//                    "Left Hip Angle" to AngleRange(160.0, 180.0), // Torso upright
//                    "Right Hip Angle" to AngleRange(160.0, 180.0)
//                ),
//                feedbackMessage = "Stand tall, chest up, core engaged."
//            ),
//            ExercisePhase(
//                name = "Lowering Phase",
//                targetAngles = mapOf(
//                    "Left Knee Angle" to AngleRange(80.0, 110.0), // Knees bent for squat depth
//                    "Right Knee Angle" to AngleRange(80.0, 110.0),
//                    "Left Hip Angle" to AngleRange(70.0, 100.0), // Hips dropping
//                    "Right Hip Angle" to AngleRange(70.0, 100.0)
//                ),
//                feedbackMessage = "Lower hips as if sitting back into a chair."
//            ),
//            ExercisePhase(
//                name = "Bottom Position",
//                targetAngles = mapOf(
//                    "Left Knee Angle" to AngleRange(70.0, 95.0), // Deepest point
//                    "Right Knee Angle" to AngleRange(70.0, 95.0),
//                    "Left Hip Angle" to AngleRange(60.0, 90.0),
//                    "Right Hip Angle" to AngleRange(60.0, 90.0)
//                ),
//                feedbackMessage = "Keep your back straight and chest up at the bottom."
//            ),
//            ExercisePhase(
//                name = "Ascending Phase",
//                targetAngles = mapOf( // Angles will be increasing towards starting position
//                    "Left Knee Angle" to AngleRange(110.0, 150.0), // Indicative range
//                    "Right Knee Angle" to AngleRange(110.0, 150.0)
//                ),
//                feedbackMessage = "Push through your heels to stand up."
//            )
//        )
//    )
//
//    val HAND_RAISING = Exercise(
//        name = "Hand Raising",
//        description = "Raise arms forward or to the side.",
//        phases = listOf(
//            ExercisePhase(
//                name = "Arms Down",
//                targetAngles = mapOf(
//                    "Left Elbow Angle" to AngleRange(160.0, 180.0), // Straight arm
//                    "Right Elbow Angle" to AngleRange(160.0, 180.0),
//                    "Left Shoulder Angle" to AngleRange(160.0, 180.0), // Arm down by side
//                    "Right Shoulder Angle" to AngleRange(160.0, 180.0)
//                ),
//                feedbackMessage = "Arms by your side, ready to raise."
//            ),
//            ExercisePhase(
//                name = "Arms Up",
//                targetAngles = mapOf(
//                    "Left Shoulder Angle" to AngleRange(70.0, 90.0), // Arm raised to 90 degrees (or overhead)
//                    "Right Shoulder Angle" to AngleRange(70.0, 90.0)
//                    // Elbows should still be straight (160-180) for a straight-arm raise
//                ),
//                feedbackMessage = "Raise arms directly in front or to the side, maintaining straight elbows."
//            )
//        )
//    )
//
//    // Add more exercises here as you create them
//    val ALL_EXERCISES = listOf(SQUAT, HAND_RAISING)
//}
package com.example.bodydetectionapp.data.models

import com.example.bodydetectionapp.R

/**
 * An object that holds the definitions for all exercises available in the app.
 *
 * Angle Definitions (Assumed Landmarks):
 * - Torso Angle: Formed by the average of shoulder, hip, and knee landmarks. A straight torso is ~180°.
 * - Hip Angle: Formed by Shoulder-Hip-Knee. A straight leg/torso alignment is ~180°.
 * - Knee Angle: Formed by Hip-Knee-Ankle. A straight leg is ~180°.
 * - Shoulder Angle: Formed by Hip-Shoulder-Elbow. Arms by the side is ~180° or ~0° depending on calculation.
 * We assume ~180° for arms down, ~90° for arms parallel to floor.
 * - Elbow Angle: Formed by Shoulder-Elbow-Wrist. A straight arm is ~180°.
 */
object ExerciseDefinitions {

    val SQUAT = Exercise(
        name = "Squat",
        description = "A full-body exercise that trains the hips, thighs, and glutes. Focus on controlled descent and ascent.",
        // videoResId = R.raw.placeholder_video, // Placeholder, replace with actual video
        phases = listOf(
            ExercisePhase(
                name = "Starting Position", // Phase 0: Initial standing position
                targetAngles = mapOf(
                    // Absolute check for a good starting posture
                    "Torso Angle" to AngleRange(160.0, 180.0) // Torso should be relatively straight
                ),
                relativeTargetAngles = emptyList(), // No relative movement in the starting phase
                feedbackMessage = "Stand tall, feet shoulder-width apart. Prepare to squat."
            ),
            ExercisePhase(
                name = "Descent (Bottom)", // Phase 1: Squatting down
                targetAngles = emptyMap(), // Depth is relative to the user's initial stance
                relativeTargetAngles = listOf(
                    // Knee angles should decrease as the user bends their knees
                    RelativeAngleTarget("Left Knee Angle", minRelativeAngle = -110.0, maxRelativeAngle = -90.0),
                    RelativeAngleTarget("Right Knee Angle", minRelativeAngle = -110.0, maxRelativeAngle = -90.0),
                    // Hip angles should also decrease as the user hinges at the hips
                    RelativeAngleTarget("Left Hip Angle", minRelativeAngle = -100.0, maxRelativeAngle = -80.0),
                    RelativeAngleTarget("Right Hip Angle", minRelativeAngle = -100.0, maxRelativeAngle = -80.0)
                ),
                feedbackMessage = "Lower hips as if sitting back into a chair. Keep your chest up."
            ),
            ExercisePhase(
                name = "Ascent (Top)", // Phase 2: Standing back up to complete the rep
                targetAngles = emptyMap(), // Return is relative to the starting position
                relativeTargetAngles = listOf(
                    // All angles should return to their initial state, with a small tolerance
                    RelativeAngleTarget("Left Knee Angle", minRelativeAngle = -15.0, maxRelativeAngle = 15.0),
                    RelativeAngleTarget("Right Knee Angle", minRelativeAngle = -15.0, maxRelativeAngle = 15.0),
                    RelativeAngleTarget("Left Hip Angle", minRelativeAngle = -15.0, maxRelativeAngle = 15.0),
                    RelativeAngleTarget("Right Hip Angle", minRelativeAngle = -15.0, maxRelativeAngle = 15.0)
                ),
                feedbackMessage = "Push through your heels to stand up fully."
            )
        )
    )

    val HAND_RAISING = Exercise(
        name = "Hand Raising",
        description = "Raise arms forward or to the side, maintaining straight elbows.",
        // videoResId = R.raw.placeholder_video,
        phases = listOf(
            ExercisePhase(
                name = "Starting Position", // Phase 0: Arms by side, ready to start.
                targetAngles = mapOf(
                    // Absolute check: Ensure arms are straight and by the user's side.
                    "Left Elbow Angle" to AngleRange(100.0, 180.0),
                    "Right Elbow Angle" to AngleRange(100.0, 180.0),
                    "Left Shoulder Angle" to AngleRange(160.0, 180.0),
                    "Right Shoulder Angle" to AngleRange(160.0, 180.0)
                ),
                relativeTargetAngles = emptyList(), // No movement yet
                feedbackMessage = "Arms by your side, ready to raise."
            ),
            ExercisePhase(
                name = "Arms Up (Top)", // Phase 1: Peak of the movement.
                targetAngles = mapOf(
                    // Absolute check: Ensure elbows remain straight at the top.
                    "Left Elbow Angle" to AngleRange(160.0, 180.0),
                    "Right Elbow Angle" to AngleRange(160.0, 180.0)
                ),
                relativeTargetAngles = listOf(
                    // Relative check: Shoulder angle should decrease as arms are raised.
                    // A change of -90° means arms are parallel to the floor (180° -> 90°).
                    RelativeAngleTarget("Left Shoulder Angle", minRelativeAngle = -110.0, maxRelativeAngle = -80.0),
                    RelativeAngleTarget("Right Shoulder Angle", minRelativeAngle = -110.0, maxRelativeAngle = -80.0)
                ),
                feedbackMessage = "Raise arms until they are parallel to the floor."
            ),
            ExercisePhase(
                name = "Arms Down (Return)", // Phase 2: Return to the starting position.
                targetAngles = mapOf(
                    // Absolute check: Ensure elbows are still straight on the way down.
                    "Left Elbow Angle" to AngleRange(160.0, 180.0),
                    "Right Elbow Angle" to AngleRange(160.0, 180.0)
                ),
                relativeTargetAngles = listOf(
                    // Relative check: Shoulder angle should return to its starting value (+/- 15° tolerance).
                    RelativeAngleTarget("Left Shoulder Angle", minRelativeAngle = -15.0, maxRelativeAngle = 15.0),
                    RelativeAngleTarget("Right Shoulder Angle", minRelativeAngle = -15.0, maxRelativeAngle = 15.0)
                ),
                feedbackMessage = "Lower your arms with control."
            )
        )
    )

    // The list containing all defined exercises for the app to use.
    val ALL_EXERCISES = listOf(SQUAT, HAND_RAISING)
}