package com.example.bodydetectionapp.ui.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bodydetectionapp.data.models.ExerciseDefinitions
import com.example.bodydetectionapp.navigation.Screen
import com.example.bodydetectionapp.ui.components.CameraPreview
import com.example.bodydetectionapp.ui.components.PoseOverlay
import com.google.gson.Gson
import java.net.URLEncoder // Ensure this import is present
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
    val isInitialPoseCaptured by viewModel.isInitialPoseCaptured.collectAsState()
    val anglesToDisplay by viewModel.anglesToDisplay.collectAsState()
    val exerciseStartTime by viewModel.exerciseStartTime.collectAsState() // NEW: Observe exerciseStartTime from ViewModel

    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var overlayView: PoseOverlay? by remember { mutableStateOf(null) }
    var showInitialInstructionsDialogInternally by remember { mutableStateOf(true) }

    LaunchedEffect(context, exerciseName) {
        viewModel.initializePoseDetectionHelper(context)
        val selectedExercise = when (exerciseName) {
            "free_movement" -> null
            else -> ExerciseDefinitions.ALL_EXERCISES.find { it.name == exerciseName }
        }
        viewModel.setExercise(selectedExercise)

        if (exerciseName == "free_movement") {
            showInitialInstructionsDialogInternally = false
            viewModel.triggerInitialAngleCaptureFromUI()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* Title is intentionally empty */ },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(
                        onClick = {
                            // Ensure initial pose is captured OR it's free movement mode
                            if (isInitialPoseCaptured || exerciseName == "free_movement") {
                                val timestampsJson = Gson().toJson(viewModel.repTimestamps)

                                // **MODIFIED**: Use the actual exerciseStartTime from the ViewModel.
                                // If for some reason it's null (e.g., exiting free movement immediately),
                                // use current time as a fallback, though it should ideally be set.
                                val actualExerciseStartTime = exerciseStartTime ?: System.currentTimeMillis()

                                navController.navigate(
                                    Screen.ExerciseReport.createRoute(
                                        exerciseName,
                                        repCount,
                                        timestampsJson,
                                        actualExerciseStartTime // Pass the actual start time
                                    )
                                )
                            } else {
                                Log.d("ExerciseTrackingScreen", "Cannot finish yet, initial pose not captured.")
                                // Optionally show a SnackBar or Toast to the user here.
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
                onPreviewReady = { previewView = it }
            )

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    PoseOverlay(it).also { overlay ->
                        overlayView = overlay
                        overlay.poseResult = poseResult
                        overlay.highlightedJointIndices = highlightedJoints
                        overlay.anglesToDisplay = anglesToDisplay
                        overlay.currentPhaseInfo = currentPhaseInfo
                        overlay.feedbackMessages = feedbackMessages
                    }
                },
                update = { overlay ->
                    overlay.poseResult = poseResult
                    overlay.highlightedJointIndices = highlightedJoints
                    overlay.anglesToDisplay = anglesToDisplay
                    overlay.currentPhaseInfo = currentPhaseInfo
                    overlay.feedbackMessages = feedbackMessages
                }
            )

            // --- Your existing UI elements are unchanged ---
            AnimatedVisibility(
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

            AnimatedVisibility(
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

            if (showInitialInstructionsDialogInternally && currentExerciseModel != null && exerciseName != "free_movement") {
                AlertDialog(
                    onDismissRequest = { /* Cannot dismiss */ },
                    title = { Text("Exercise Instructions: ${currentExerciseModel?.name}") },
                    text = {
                        val scrollState = rememberScrollState()
                        Column(modifier = Modifier.verticalScroll(scrollState)) {
                            Text(currentExerciseModel?.description ?: "No description available.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Phases:", fontWeight = FontWeight.Bold)
                            currentExerciseModel?.phases?.forEach { phase ->
                                Text("- ${phase.name}: ${phase.feedbackMessage ?: "No specific guidance."}")
                                if (phase.targetAngles.isNotEmpty()) {
                                    Text("  Target Angles (Absolute):", fontSize = 12.sp, color = Color.Gray)
                                    phase.targetAngles.forEach { (angleName, range) ->
                                        Text("    -> $angleName: ${"%.0f-%.0f°".format(range.min, range.max)}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                if (phase.relativeTargetAngles.isNotEmpty()) {
                                    Text("  Target Angles (Relative):", fontSize = 12.sp, color = Color.Gray)
                                    phase.relativeTargetAngles.forEach { relativeTarget ->
                                        Text("    -> ${relativeTarget.angleName}: ${"%.0f-%.0f° (change)".format(relativeTarget.minRelativeAngle, relativeTarget.maxRelativeAngle)}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Please get into your **starting position** and hold still. The exercise will begin when your pose is stable.", fontWeight = FontWeight.Bold)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showInitialInstructionsDialogInternally = false
                                viewModel.triggerInitialAngleCaptureFromUI()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("I'm Ready", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                )
            }

            LaunchedEffect(previewView, showInitialInstructionsDialogInternally, isInitialPoseCaptured) {
                if (previewView == null || (showInitialInstructionsDialogInternally && !isInitialPoseCaptured && exerciseName != "free_movement")) {
                    return@LaunchedEffect
                }
                while (isActive) {
                    previewView?.bitmap?.let { frameBitmap ->
                        val copyBitmap = frameBitmap.copy(Bitmap.Config.ARGB_8888, true)
                        viewModel.detectPose(copyBitmap)
                    }
                    delay(33) // ~30 FPS
                }
            }
        }
    }
}