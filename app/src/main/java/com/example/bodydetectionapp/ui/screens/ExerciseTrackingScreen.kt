//package com.example.bodydetectionapp.ui.screens
//
//import android.graphics.Bitmap
//import android.util.Log
//import androidx.camera.view.PreviewView
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.slideInVertically
//import androidx.compose.animation.slideOutVertically
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowForward // Import for the new icon
//import androidx.compose.material.icons.filled.Close // Keep for now in case of issues, but will replace
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.runtime.collectAsState
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import androidx.navigation.compose.rememberNavController
//import com.example.bodydetectionapp.data.models.ExerciseDefinitions
//import com.example.bodydetectionapp.navigation.Screen
//import com.example.bodydetectionapp.ui.components.CameraPreview
//import com.example.bodydetectionapp.ui.components.PoseOverlay
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.isActive
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ExerciseTrackingScreen(
//    navController: NavController,
//    exerciseName: String,
//    modifier: Modifier = Modifier,
//    viewModel: ExerciseTrackingViewModel = viewModel()
//) {
//    val context = LocalContext.current
//
//    val poseResult by viewModel.poseResult.collectAsState()
//    val highlightedJoints by viewModel.highlightedJoints.collectAsState()
//    val feedbackMessages by viewModel.feedbackMessages.collectAsState() // We'll pass this to PoseOverlay now
//    val repCount by viewModel.repCount.collectAsState()
//    val currentExerciseModel by viewModel.currentExerciseModel.collectAsState()
//    val currentPhaseInfo by viewModel.currentPhaseInfo.collectAsState()
//
//    var previewView: PreviewView? by remember { mutableStateOf(null) }
//    var overlayView: PoseOverlay? by remember { mutableStateOf(null) }
//
//    var showInitialInstructionsDialog by remember { mutableStateOf(true) }
//
//    LaunchedEffect(context, exerciseName) {
//        viewModel.initializePoseDetectionHelper(context)
//
//        val selectedExercise = when (exerciseName) {
//            "free_movement" -> null
//            else -> ExerciseDefinitions.ALL_EXERCISES.find { it.name == exerciseName }
//        }
//        viewModel.setExercise(selectedExercise)
//
//        // For "free_movement", always skip initial instructions to start immediately.
//        if (exerciseName == "free_movement") {
//            showInitialInstructionsDialog = false
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { /* Exercise name removed from TopAppBar */ },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Color.Transparent, // Make TopAppBar background transparent
//                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
//                ),
//                actions = {
//                    // Arrow Forward icon in top right, less padding
//                    IconButton(
//                        onClick = {
//                            navController.navigate(Screen.ExerciseReport.createRoute(exerciseName, repCount))
//                        },
//                        modifier = Modifier.padding(4.dp) // Smaller padding
//                    ) {
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
//                            contentDescription = "Finish Exercise",
//                            tint = MaterialTheme.colorScheme.primary // Use primary color for visibility
//                        )
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
//            CameraPreview(
//                modifier = Modifier.fillMaxSize(),
//                onPreviewReady = {
//                    previewView = it
//                }
//            )
//
//            AndroidView(
//                modifier = Modifier.fillMaxSize(),
//                factory = {
//                    PoseOverlay(it).also {
//                        overlayView = it
//                        it.poseResult = poseResult
//                        it.highlightedJointIndices = highlightedJoints
//                        it.anglesToDisplay = if (exerciseName == "free_movement") {
//                            viewModel.getAllCalculatedAngles()
//                        } else {
//                            currentExerciseModel?.let { model ->
//                                viewModel.getRequiredAnglesForDisplay(model)
//                            } ?: emptyMap()
//                        }
//                        it.currentPhaseInfo = currentPhaseInfo
//                        it.feedbackMessages = feedbackMessages // Pass feedback messages to overlay
//                    }
//                },
//                update = {
//                    it.poseResult = poseResult
//                    it.highlightedJointIndices = highlightedJoints
//                    it.anglesToDisplay = if (exerciseName == "free_movement") {
//                        viewModel.getAllCalculatedAngles()
//                    } else {
//                        currentExerciseModel?.let { model ->
//                            viewModel.getRequiredAnglesForDisplay(model)
//                        } ?: emptyMap()
//                    }
//                    it.currentPhaseInfo = currentPhaseInfo
//                    it.feedbackMessages = feedbackMessages // Update feedback messages in overlay
//                }
//            )
//
//            // TOP LEFT: Phase Name & Exercise Name
//            AnimatedVisibility(
//                visible = !showInitialInstructionsDialog && exerciseName != "free_movement",
//                enter = fadeIn(),
//                exit = fadeOut(),
//                modifier = Modifier
//                    .align(Alignment.TopStart) // Align to top-start
//                    .padding(top = 16.dp, start = 16.dp) // Adjust padding as needed
//            ) {
//                Column(horizontalAlignment = Alignment.Start) { // Align text left within column
//                    Text(
//                        text = currentExerciseModel?.name ?: "",
//                        color = Color.White,
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Bold,
//                        modifier = Modifier
//                            .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.small)
//                            .padding(horizontal = 8.dp, vertical = 4.dp)
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    currentPhaseInfo?.let { phase ->
//                        Text(
//                            text = "Phase: ${phase.name}",
//                            color = MaterialTheme.colorScheme.onPrimary,
//                            fontSize = 24.sp,
//                            fontWeight = FontWeight.ExtraBold,
//                            modifier = Modifier
//                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), MaterialTheme.shapes.small)
//                                .padding(horizontal = 8.dp, vertical = 4.dp)
//                        )
//                    }
//                }
//            }
//
//            // TOP RIGHT: Rep Count
//            AnimatedVisibility(
//                visible = !showInitialInstructionsDialog && exerciseName != "free_movement",
//                enter = fadeIn(),
//                exit = fadeOut(),
//                modifier = Modifier
//                    .align(Alignment.TopEnd) // Align to top-end
//                    .padding(top = 16.dp, end = 16.dp) // Adjust padding as needed
//            ) {
//                Text(
//                    text = "Reps: $repCount",
//                    color = Color.White,
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier
//                        .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.small)
//                        .padding(horizontal = 8.dp, vertical = 4.dp)
//                )
//            }
//
//
//            // REMOVED: Old feedback/angle display column at the bottom
//
//
//            // FREE MOVEMENT MODE INDICATOR (Bottom Center) - remains the same
//            if (exerciseName == "free_movement" && !showInitialInstructionsDialog) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .align(Alignment.BottomCenter)
//                        .padding(bottom = 16.dp)
//                ) {
//                    Text(
//                        text = "Free Movement Mode",
//                        color = Color.White.copy(alpha = 0.7f),
//                        fontSize = 20.sp,
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier.align(Alignment.Center)
//                    )
//                }
//            }
//
//            // Initial Instruction Dialog (unchanged, still a blocking dialog)
//            if (showInitialInstructionsDialog && currentExerciseModel != null && exerciseName != "free_movement") {
//                AlertDialog(
//                    onDismissRequest = { /* Cannot dismiss without accepting */ },
//                    title = { Text("Exercise Instructions: ${currentExerciseModel?.name}") },
//                    text = {
//                        Column {
//                            Text(currentExerciseModel?.description ?: "No description available.")
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text("Phases:", fontWeight = FontWeight.Bold)
//                            currentExerciseModel?.phases?.forEach { phase ->
//                                Text("- ${phase.name}: ${phase.feedbackMessage ?: "No specific guidance."}")
//                                phase.targetAngles.forEach { (angleName, range) ->
//                                    Text("  -> $angleName: ${"%.0f-%.0f°".format(range.min, range.max)}", fontSize = 12.sp, color = Color.Gray)
//                                }
//                            }
//                        }
//                    },
//                    confirmButton = {
//                        Button(
//                            onClick = { showInitialInstructionsDialog = false },
//                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
//                        ) {
//                            Text("Start Exercise", color = MaterialTheme.colorScheme.onPrimary)
//                        }
//                    }
//                )
//            }
//
//
//            // Real-time pose detection loop using ViewModel
//            LaunchedEffect(previewView, showInitialInstructionsDialog) {
//                if (previewView == null || (showInitialInstructionsDialog && exerciseName != "free_movement")) {
//                    Log.d("ExerciseTrackingScreen", "Detection paused: previewView null or initial instructions showing.")
//                    return@LaunchedEffect
//                }
//
//                Log.d("ExerciseTrackingScreen", "Starting/Resuming pose detection loop.")
//                while (isActive) {
//                    previewView?.bitmap?.let { frameBitmap ->
//                        val copyBitmap = frameBitmap.copy(Bitmap.Config.ARGB_8888, true)
//                        if (copyBitmap != null) {
//                            viewModel.detectPose(copyBitmap)
//                        } else {
//                            Log.w("ExerciseTrackingScreen", "Failed to get bitmap from PreviewView or copy it.")
//                        }
//                    } ?: run {
//                        Log.d("ExerciseTrackingScreen", "PreviewView bitmap is null. Skipping detection.")
//                    }
//                    delay(66) // ~15 FPS processing rate
//                }
//                Log.d("ExerciseTrackingScreen", "Pose detection loop stopped.")
//            }
//        }
//    }
//}
package com.example.bodydetectionapp.ui.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bodydetectionapp.data.models.ExerciseDefinitions
import com.example.bodydetectionapp.navigation.Screen
import com.example.bodydetectionapp.ui.components.CameraPreview
import com.example.bodydetectionapp.ui.components.PoseOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseTrackingScreen(
    navController: NavController,
    exerciseName: String,
    modifier: Modifier = Modifier,
    viewModel: ExerciseTrackingViewModel = viewModel()
) {
    val context = LocalContext.current

    val poseResult by viewModel.poseResult.collectAsState()
    val highlightedJoints by viewModel.highlightedJoints.collectAsState()
    val feedbackMessages by viewModel.feedbackMessages.collectAsState()
    val repCount by viewModel.repCount.collectAsState()
    val currentExerciseModel by viewModel.currentExerciseModel.collectAsState()
    val currentPhaseInfo by viewModel.currentPhaseInfo.collectAsState()
    val isInitialPoseCaptured by viewModel.isInitialPoseCaptured.collectAsState() // NEW
    val anglesToDisplay by viewModel.anglesToDisplay.collectAsState() // NEW

    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var overlayView: PoseOverlay? by remember { mutableStateOf(null) }

    // This state controls the *visibility* of the instruction dialog.
    // The ViewModel now manages the *actual* initial pose detection process.
    var showInitialInstructionsDialogInternally by remember { mutableStateOf(true) }


    LaunchedEffect(context, exerciseName) {
        viewModel.initializePoseDetectionHelper(context)

        val selectedExercise = when (exerciseName) {
            "free_movement" -> null
            else -> ExerciseDefinitions.ALL_EXERCISES.find { it.name == exerciseName }
        }
        viewModel.setExercise(selectedExercise)

        // For "free_movement", always skip initial instructions to start immediately.
        if (exerciseName == "free_movement") {
            showInitialInstructionsDialogInternally = false
            viewModel.triggerInitialAngleCaptureFromUI() // Immediately start free movement detection
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* Exercise name removed from TopAppBar */ },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, // Make TopAppBar background transparent
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(
                        onClick = {
                            // Only navigate if initial pose is captured or it's free movement
                            if (isInitialPoseCaptured || exerciseName == "free_movement") {
                                navController.navigate(Screen.ExerciseReport.createRoute(exerciseName, repCount))
                            } else {
                                // Optionally show a toast or message if they try to finish too early
                                Log.d("ExerciseTrackingScreen", "Cannot finish yet, initial pose not captured.")
                            }
                        },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Finish Exercise",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onPreviewReady = {
                    previewView = it
                }
            )

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    PoseOverlay(it).also {
                        overlayView = it
                        it.poseResult = poseResult
                        it.highlightedJointIndices = highlightedJoints
                        it.anglesToDisplay = anglesToDisplay // Use anglesToDisplay from ViewModel
                        it.currentPhaseInfo = currentPhaseInfo
                        it.feedbackMessages = feedbackMessages
                    }
                },
                update = {
                    it.poseResult = poseResult
                    it.highlightedJointIndices = highlightedJoints
                    it.anglesToDisplay = anglesToDisplay // Update anglesToDisplay
                    it.currentPhaseInfo = currentPhaseInfo
                    it.feedbackMessages = feedbackMessages
                }
            )

            // TOP LEFT: Phase Name & Exercise Name
            AnimatedVisibility(
                // Visible only when initial instructions are gone AND initial pose is captured
                visible = !showInitialInstructionsDialogInternally && isInitialPoseCaptured && exerciseName != "free_movement",
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 16.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = currentExerciseModel?.name ?: "",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    currentPhaseInfo?.let { phase ->
                        Text(
                            text = "Phase: ${phase.name}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), MaterialTheme.shapes.small)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // TOP RIGHT: Rep Count
            AnimatedVisibility(
                // Visible only when initial instructions are gone AND initial pose is captured
                visible = !showInitialInstructionsDialogInternally && isInitialPoseCaptured && exerciseName != "free_movement",
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = "Reps: $repCount",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }


            // FREE MOVEMENT MODE INDICATOR (Bottom Center)
            if (exerciseName == "free_movement" && !showInitialInstructionsDialogInternally) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Free Movement Mode",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Initial Instruction Dialog
            if (showInitialInstructionsDialogInternally && currentExerciseModel != null && exerciseName != "free_movement") {
                AlertDialog(
                    onDismissRequest = { /* Cannot dismiss without accepting */ },
                    title = { Text("Exercise Instructions: ${currentExerciseModel?.name}") },
                    text = {
                        Column {
                            Text(currentExerciseModel?.description ?: "No description available.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Phases:", fontWeight = FontWeight.Bold)
                            currentExerciseModel?.phases?.forEach { phase ->
                                Text("- ${phase.name}: ${phase.feedbackMessage ?: "No specific guidance."}")
                                // Displaying absolute target ranges in instructions
                                if (phase.targetAngles.isNotEmpty()) {
                                    Text("  Target Angles (Absolute):", fontSize = 12.sp, color = Color.Gray)
                                    phase.targetAngles.forEach { (angleName, range) ->
                                        Text("    -> $angleName: ${"%.0f-%.0f°".format(range.min, range.max)}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                // Displaying relative target ranges in instructions
                                if (phase.relativeTargetAngles.isNotEmpty()) {
                                    Text("  Target Angles (Relative):", fontSize = 12.sp, color = Color.Gray)
                                    phase.relativeTargetAngles.forEach { relativeTarget ->
                                        Text("    -> ${relativeTarget.angleName}: ${"%.0f-%.0f° (change)".format(relativeTarget.minRelativeAngle, relativeTarget.maxRelativeAngle)}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text("Please get into your **starting position** and hold still. The exercise will begin when your pose is stable.", fontWeight = FontWeight.Bold)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showInitialInstructionsDialogInternally = false // Dismiss UI dialog
                                viewModel.triggerInitialAngleCaptureFromUI() // Start ViewModel's auto-detection
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("I'm Ready", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                )
            }


            // Real-time pose detection loop using ViewModel
            LaunchedEffect(previewView, showInitialInstructionsDialogInternally, isInitialPoseCaptured) { // Added isInitialPoseCaptured
                // Pause detection if preview is null OR if instructions are showing AND initial pose isn't captured yet for an exercise
                if (previewView == null || (showInitialInstructionsDialogInternally && !isInitialPoseCaptured && exerciseName != "free_movement")) {
                    Log.d("ExerciseTrackingScreen", "Detection paused: Waiting for initial pose detection completion.")
                    return@LaunchedEffect
                }

                Log.d("ExerciseTrackingScreen", "Starting/Resuming pose detection loop.")
                while (isActive) {
                    previewView?.bitmap?.let { frameBitmap ->
                        val copyBitmap = frameBitmap.copy(Bitmap.Config.ARGB_8888, true)
                        if (copyBitmap != null) {
                            viewModel.detectPose(copyBitmap)
                        } else {
                            Log.w("ExerciseTrackingScreen", "Failed to get bitmap from PreviewView or copy it.")
                        }
                    } ?: run {
                        Log.d("ExerciseTrackingScreen", "PreviewView bitmap is null. Skipping detection.")
                    }
                    delay(33) // ~ 30 FPS processing rate
                }
                Log.d("ExerciseTrackingScreen", "Pose detection loop stopped.")
            }
        }
    }
}