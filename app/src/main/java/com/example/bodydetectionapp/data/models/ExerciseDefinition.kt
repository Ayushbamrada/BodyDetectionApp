package com.example.bodydetectionapp.data.models

/**
 * An object that holds the definitions for all exercises available in the app,
 * using the new cyclical rep counting model.
 */
object ExerciseDefinitions {

    val SQUAT = Exercise(
        name = "Squat",
        description = "A full-body exercise that trains the hips, thighs, and glutes.",
        repCounter = RepCounter(
            keyJointsToTrack = listOf("Left Knee Angle", "Right Knee Angle"),
            entryThreshold = 120.0,
            exitThreshold = 160.0
        ),
        metValue = 8.0 // Vigorous calisthenics
    )

    val OVERHEAD_HAND_RAISING = Exercise(
        name = "Overhead Hand Raising",
        description = "Raise arms vertically overhead, maintaining straight elbows.",
        repCounter = RepCounter(
            keyJointsToTrack = listOf("Left Shoulder Angle", "Right Shoulder Angle"),
            entryThreshold = 140.0,
            exitThreshold = 70.0
        ),
        metValue = 2.5 // Light stretching
    )

    val SIT_TO_STAND = Exercise(
        name = "Sit to Stand",
        description = "Transition from a seated to a standing position.",
        repCounter = RepCounter(
            keyJointsToTrack = listOf("Left Knee Angle", "Right Knee Angle", "Left Hip Angle", "Right Hip Angle"),
            entryThreshold = 160.0,
            exitThreshold = 120.0
        ),
        metValue = 3.5 // Light calisthenics
    )

    val MARCHING_IN_PLACE = Exercise(
        name = "Marching in Place",
        description = "Lift your knees alternately to warm up your body. A full rep consists of one lift for each leg.",
        repCounter = RepCounter(
            keyJointsToTrack = listOf("Left Knee Angle", "Right Knee Angle", "Left Hip Angle", "Right Hip Angle"),
            entryThreshold = 130.0,
            exitThreshold = 160.0
        ),
        metValue = 4.0 // Moderate calisthenics / aerobics
    )

    val ALL_EXERCISES = listOf(SQUAT, OVERHEAD_HAND_RAISING, SIT_TO_STAND, MARCHING_IN_PLACE)
}
