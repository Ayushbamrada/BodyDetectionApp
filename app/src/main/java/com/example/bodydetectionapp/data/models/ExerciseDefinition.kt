//package com.example.bodydetectionapp.data.models
//import com.example.bodydetectionapp.R
//
//object ExerciseDefinitions {
//
//    val SQUAT = Exercise(
//        name = "Squat",
//        description = "A full-body exercise that trains the hips, thighs, and glutes.",
//        type = ExerciseType.SYMMETRICAL,
//        cameraView = CameraView.FRONT,
//        bodyPart = BodyPart.LOWER_BODY,
//        primaryMovement = AngleMovement(
//            keyJointsToTrack = listOf("Left Knee Angle", "Right Knee Angle"),
//            entryThreshold = 120.0,
//            exitThreshold = 160.0
//        ),
//        metValue = 8.0,
//        formRules = listOf(
//            AngleRule("Torso Angle", 110.0, 180.0, "Keep your chest up!")
//        ),
//        videoResId = R.raw.squat_demo
//    )
//
//    val OVERHEAD_HAND_RAISING = Exercise(
//        name = "Overhead Hand Raising",
//        description = "Raise arms vertically overhead, maintaining straight elbows.",
//        type = ExerciseType.SYMMETRICAL,
//        cameraView = CameraView.FRONT,
//        bodyPart = BodyPart.UPPER_BODY,
//        primaryMovement = AngleMovement(
//            keyJointsToTrack = listOf("Left Shoulder Angle", "Right Shoulder Angle"),
//            entryThreshold = 120.0,
//            exitThreshold = 70.0
//        ),
//        metValue = 2.5,
//        formRules = listOf(
//            AngleRule("Left Elbow Angle", 150.0, 180.0, "Keep left arm straight!"),
//            AngleRule("Right Elbow Angle", 150.0, 180.0, "Keep right arm straight!")
//        ),
//        videoResId = null
//    )
//
//    val MARCHING_IN_PLACE = Exercise(
//        name = "Marching in Place",
//        description = "Lift your knees alternately to warm up your body.",
//        type = ExerciseType.ALTERNATING,
//        cameraView = CameraView.FRONT,
//        bodyPart = BodyPart.FULL_BODY,
//        primaryMovement = AngleMovement(
//            keyJointsToTrack = listOf("Left Hip Angle", "Right Hip Angle"),
//            entryThreshold = 130.0,
//            exitThreshold = 160.0
//        ),
//        metValue = 4.0,
//        formRules = listOf(
//            HorizontalAlignmentRule("LEFT_KNEE", "LEFT_HIP", 0.2f, "Lift left knee forward!"),
//            HorizontalAlignmentRule("RIGHT_KNEE", "RIGHT_HIP", 0.2f, "Lift right knee forward!")
//        ),
//        videoResId = R.raw.marching_in_place_demo
//    )
//
//    val FLUTTER_KICKS = Exercise(
//        name = "Flutter Kicks",
//        description = "Lie on your back and make small, rapid kicking motions.",
//        type = ExerciseType.ALTERNATING,
//        cameraView = CameraView.SIDE,
//        bodyPart = BodyPart.CORE,
//        primaryMovement = AngleMovement(
//            keyJointsToTrack = listOf("Left Hip Angle", "Right Hip Angle"),
//            entryThreshold = 165.0,
//            exitThreshold = 175.0
//        ),
//        metValue = 3.0,
//        formRules = listOf(
//            AngleRule("Left Knee Angle", 140.0, 180.0, "Keep left leg straight!"),
//            AngleRule("Right Knee Angle", 140.0, 180.0, "Keep right leg straight!")
//        ),
//        videoResId = R.raw.flutter_kicks_demo
//    )
//
//    val BICYCLE_CRUNCHES = Exercise(
//        name = "Bicycle Crunches",
//        description = "Bring your opposite knee to your opposite elbow in an alternating motion.",
//        type = ExerciseType.ALTERNATING,
//        cameraView = CameraView.SIDE,
//        bodyPart = BodyPart.CORE,
//        primaryMovement = AngleMovement(
//            keyJointsToTrack = listOf("Left Torso Angle", "Right Torso Angle"),
//            entryThreshold = 130.0,
//            exitThreshold = 150.0
//        ),
//        metValue = 5.0,
//        formRules = listOf(),
//        videoResId = R.raw.bicycle_crunches_demo
//    )
//
//    val STANDING_OBLIQUE_CRUNCHES = Exercise(
//        name = "Standing Oblique Crunches",
//        description = "Bring your knee up towards your elbow on the same side.",
//        type = ExerciseType.ALTERNATING,
//        cameraView = CameraView.FRONT,
//        bodyPart = BodyPart.CORE,
//        primaryMovement = DistanceMovement(
//            landmark1 = "ELBOW",
//            landmark2 = "KNEE",
//            entryThreshold = 0.2,
//            exitThreshold = 0.4
//        ),
//        metValue = 4.5,
//        formRules = listOf(
//            AngleRule("Torso Angle", 150.0, 180.0, "Stay upright!")
//        ),
//        videoResId = R.raw.standing_oblique_crunches_demo
//    )
//
//    val SIT_TO_STAND = Exercise(
//        name = "Sit to Stand",
//        description = "Transition from a seated to a standing position.",
//        type = ExerciseType.SYMMETRICAL,
//        cameraView = CameraView.FRONT,
//        bodyPart = BodyPart.LOWER_BODY,
//        primaryMovement = AngleMovement(
//            keyJointsToTrack = listOf("Left Knee Angle", "Right Knee Angle", "Left Hip Angle", "Right Hip Angle"),
//            entryThreshold = 160.0,
//            exitThreshold = 120.0
//        ),
//        metValue = 3.5,
//        formRules = emptyList(),
//        videoResId = R.raw.sit_to_stand_demo
//    )
//
//
//    val ALL_EXERCISES = listOf(
//        SQUAT,
//        OVERHEAD_HAND_RAISING,
//        MARCHING_IN_PLACE,
//        FLUTTER_KICKS,
//        BICYCLE_CRUNCHES,
//        STANDING_OBLIQUE_CRUNCHES,
//        SIT_TO_STAND
//    )
//}
package com.example.bodydetectionapp.data.models
import com.example.bodydetectionapp.R

object ExerciseDefinitions {

    val SQUAT = Exercise(
        name = "Squat",
        description = "A full-body exercise that trains the hips, thighs, and glutes.",
        startInstruction = "Stand with your feet shoulder-width apart.",
        primaryMovementInstruction = "Go lower to complete the squat.",
        requiredLandmarks = listOf("LEFT_HIP", "RIGHT_HIP", "LEFT_KNEE", "RIGHT_KNEE", "LEFT_ANKLE", "RIGHT_ANKLE"),
        visibilityFeedbackMessage = "Cannot see your legs. Please step back.",
        type = ExerciseType.SYMMETRICAL,
        cameraView = CameraView.FRONT,
        bodyPart = BodyPart.LOWER_BODY,
        primaryMovement = AngleMovement(
            keyJointsToTrack = listOf("Left Knee Angle", "Right Knee Angle"),
            entryThreshold = 120.0,
            exitThreshold = 160.0
        ),
        metValue = 8.0,
        formRules = listOf(
            AngleRule("Torso Angle", 110.0, 180.0, "Keep your chest up!")
        ),
        videoResId = R.raw.squat_demo
    )

    val OVERHEAD_HAND_RAISING = Exercise(
        name = "Overhead Hand Raising",
        description = "Raise arms vertically overhead, maintaining straight elbows.",
        startInstruction = "Stand with your arms by your side.",
        primaryMovementInstruction = "Raise your arms higher.",
        requiredLandmarks = listOf("LEFT_SHOULDER", "RIGHT_SHOULDER", "LEFT_ELBOW", "RIGHT_ELBOW", "LEFT_WRIST", "RIGHT_WRIST"),
        visibilityFeedbackMessage = "Please make sure your arms and shoulders are visible.",
        type = ExerciseType.SYMMETRICAL,
        cameraView = CameraView.FRONT,
        bodyPart = BodyPart.UPPER_BODY,
        primaryMovement = AngleMovement(
            keyJointsToTrack = listOf("Left Shoulder Angle", "Right Shoulder Angle"),
            entryThreshold = 165.0,
            exitThreshold = 80.0
        ),
        metValue = 2.5,
        formRules = listOf(
            AngleRule("Left Elbow Angle", 150.0, 180.0, "Keep your left arm straight!"),
            AngleRule("Right Elbow Angle", 150.0, 180.0, "Keep your right arm straight!")
        ),
        videoResId = null
    )

    val MARCHING_IN_PLACE = Exercise(
        name = "Marching in Place",
        description = "Lift your knees alternately to warm up your body.",
        startInstruction = "Stand tall to begin.",
        primaryMovementInstruction = "Lift your knees higher.",
        requiredLandmarks = listOf("LEFT_HIP", "RIGHT_HIP", "LEFT_KNEE", "RIGHT_KNEE"),
        visibilityFeedbackMessage = "Please make sure your hips and knees are visible.",
        type = ExerciseType.ALTERNATING,
        cameraView = CameraView.FRONT,
        bodyPart = BodyPart.FULL_BODY,
        primaryMovement = AngleMovement(
            keyJointsToTrack = listOf("Left Hip Angle", "Right Hip Angle"),
            entryThreshold = 130.0,
            exitThreshold = 160.0
        ),
        metValue = 4.0,
        formRules = listOf(
            HorizontalAlignmentRule("LEFT_KNEE", "LEFT_HIP", 0.2f, "Lift your left knee forward!"),
            HorizontalAlignmentRule("RIGHT_KNEE", "RIGHT_HIP", 0.2f, "Lift your right knee forward!")
        ),
        videoResId = R.raw.marching_in_place_demo
    )

    val FLUTTER_KICKS = Exercise(
        name = "Flutter Kicks",
        description = "Lie on your back and make small, rapid kicking motions.",
        startInstruction = "Lie on your back with legs straight.",
        primaryMovementInstruction = "Keep kicking.",
        requiredLandmarks = listOf("LEFT_HIP", "RIGHT_HIP", "LEFT_KNEE", "RIGHT_KNEE", "LEFT_ANKLE", "RIGHT_ANKLE"),
        visibilityFeedbackMessage = "Cannot see your legs. Please make sure your full body is in frame.",
        type = ExerciseType.ALTERNATING,
        cameraView = CameraView.SIDE,
        bodyPart = BodyPart.CORE,
        primaryMovement = AngleMovement(
            keyJointsToTrack = listOf("Left Hip Angle", "Right Hip Angle"),
            entryThreshold = 165.0,
            exitThreshold = 175.0
        ),
        metValue = 3.0,
        formRules = listOf(
            AngleRule("Left Knee Angle", 140.0, 180.0, "Keep your left leg straight!"),
            AngleRule("Right Knee Angle", 140.0, 180.0, "Keep your right leg straight!")
        ),
        videoResId = R.raw.flutter_kicks_demo
    )

    val BICYCLE_CRUNCHES = Exercise(
        name = "Bicycle Crunches",
        description = "Bring your opposite knee to your opposite elbow in an alternating motion.",
        startInstruction = "Lie on your back with knees bent.",
        primaryMovementInstruction = "Bring your elbow to your knee.",
        requiredLandmarks = listOf("LEFT_SHOULDER", "RIGHT_SHOULDER", "LEFT_ELBOW", "RIGHT_ELBOW", "LEFT_HIP", "RIGHT_HIP", "LEFT_KNEE", "RIGHT_KNEE"),
        visibilityFeedbackMessage = "Please make sure your full body is visible.",
        type = ExerciseType.ALTERNATING,
        cameraView = CameraView.SIDE,
        bodyPart = BodyPart.CORE,
        primaryMovement = AngleMovement(
            keyJointsToTrack = listOf("Left Torso Angle", "Right Torso Angle"),
            entryThreshold = 130.0,
            exitThreshold = 150.0
        ),
        metValue = 5.0,
        formRules = listOf(),
        videoResId = R.raw.bicycle_crunches_demo
    )

    val STANDING_OBLIQUE_CRUNCHES = Exercise(
        name = "Standing Oblique Crunches",
        description = "Bring your knee up towards your elbow on the same side.",
        startInstruction = "Stand with your hands behind your head.",
        primaryMovementInstruction = "Bring your knee to your elbow.",
        requiredLandmarks = listOf("LEFT_ELBOW", "RIGHT_ELBOW", "LEFT_KNEE", "RIGHT_KNEE", "LEFT_HIP", "RIGHT_HIP"),
        visibilityFeedbackMessage = "Please make sure your full body is visible.",
        type = ExerciseType.ALTERNATING,
        cameraView = CameraView.FRONT,
        bodyPart = BodyPart.CORE,
        primaryMovement = DistanceMovement(
            landmark1 = "ELBOW",
            landmark2 = "KNEE",
            entryThreshold = 0.2,
            exitThreshold = 0.4
        ),
        metValue = 4.5,
        formRules = listOf(
            AngleRule("Torso Angle", 150.0, 180.0, "Stay upright!")
        ),
        videoResId = R.raw.standing_oblique_crunches_demo
    )

    val SIT_TO_STAND = Exercise(
        name = "Sit to Stand",
        description = "Transition from a seated to a standing position.",
        startInstruction = "Start from a seated position.",
        primaryMovementInstruction = "Stand up completely.",
        requiredLandmarks = listOf("LEFT_HIP", "RIGHT_HIP", "LEFT_KNEE", "RIGHT_KNEE"),
        visibilityFeedbackMessage = "Cannot see your lower body. Please adjust your camera.",
        type = ExerciseType.SYMMETRICAL,
        cameraView = CameraView.FRONT,
        bodyPart = BodyPart.LOWER_BODY,
        primaryMovement = AngleMovement(
            keyJointsToTrack = listOf("Left Knee Angle", "Right Knee Angle", "Left Hip Angle", "Right Hip Angle"),
            entryThreshold = 160.0,
            exitThreshold = 120.0
        ),
        metValue = 3.5,
        formRules = emptyList(),
        videoResId = R.raw.sit_to_stand_demo
    )


    val ALL_EXERCISES = listOf(
        SQUAT,
        OVERHEAD_HAND_RAISING,
        MARCHING_IN_PLACE,
        FLUTTER_KICKS,
        BICYCLE_CRUNCHES,
        STANDING_OBLIQUE_CRUNCHES,
        SIT_TO_STAND
    )
}