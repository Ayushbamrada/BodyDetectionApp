package com.example.bodydetectionapp.ui.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column // Removed duplicate
import androidx.compose.foundation.layout.Spacer // Removed duplicate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme // Removed duplicate
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text // Removed duplicate
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState // Keep this one for Compose state observation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bodydetectionapp.data.models.ExerciseDefinitions
import com.example.bodydetectionapp.navigation.Screen
import com.example.bodydetectionapp.ui.components.CameraPreview
import com.example.bodydetectionapp.ui.components.PoseOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive // Keep this one for coroutine scope

// REMOVED: import kotlinx.coroutines.flow.collectAsState // This is usually redundant with androidx.compose.runtime.collectAsState for Compose UI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseTrackingScreen(
    navController: NavController,
    exerciseName: String, // Receive the exercise name as an argument
    modifier: Modifier = Modifier,
    viewModel: ExerciseTrackingViewModel = viewModel() // Use a new ViewModel for this screen
) {
    val context = LocalContext.current

    // Observe ViewModel states
    val poseResult by viewModel.poseResult.collectAsState()
    val highlightedJoints by viewModel.highlightedJoints.collectAsState()
    val feedbackMessages by viewModel.feedbackMessages.collectAsState()
    val repCount by viewModel.repCount.collectAsState()
    val exerciseSummary by viewModel.exerciseSummary.collectAsState() // Renamed for clarity
    val currentExerciseModel by viewModel.currentExerciseModel.collectAsState() // New state for exercise model
    val currentPhaseInfo by viewModel.currentPhaseInfo.collectAsState() // New state for phase info

    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var overlayView: PoseOverlay? by remember { mutableStateOf(null) }

    // State to control instruction overlay visibility
    var showInstructions by remember { mutableStateOf(true) }

    // Initialize ViewModel's helper and set the selected exercise
    LaunchedEffect(context, exerciseName) { // Re-initialize if exerciseName changes
        viewModel.initializePoseDetectionHelper(context)

        // Find and set the selected exercise based on the name
        val selectedExercise = when (exerciseName) {
            "free_movement" -> null // Or define a "Free Movement" exercise model with no specific phases/targets
            else -> ExerciseDefinitions.ALL_EXERCISES.find { it.name == exerciseName }
        }
        viewModel.setExercise(selectedExercise)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (exerciseName == "free_movement") "Free Movement" else exerciseName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        currentPhaseInfo?.let { phase ->
                            Text(
                                text = "Phase: ${phase.name}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Finish Exercise Button
                    IconButton(
                        onClick = {
                            // Navigate to Exercise Report screen, passing exercise name and final rep count
                            navController.navigate(Screen.ExerciseReport.createRoute(exerciseName, repCount))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Finish Exercise",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
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
                        // Initial setup for the overlay
                        it.poseResult = poseResult
                        it.highlightedJointIndices = highlightedJoints
                        it.anglesToDisplay = currentExerciseModel?.let { model ->
                            viewModel.getRequiredAnglesForDisplay(model)
                        } ?: emptyMap()
                        // REMOVED this line as PoseOverlay doesn't have a 'landmarks' property directly
                        // it.landmarks = poseResult?.landmarks()?.firstOrNull()
                    }
                },
                update = {
                    // Update overlay with latest data from ViewModel
                    it.poseResult = poseResult
                    it.highlightedJointIndices = highlightedJoints
                    // Pass specific angles to draw on overlay
                    // Only call getRequiredAnglesForDisplay if currentExerciseModel is not null
                    it.anglesToDisplay = currentExerciseModel?.let { model ->
                        viewModel.getRequiredAnglesForDisplay(model)
                    } ?: emptyMap() // If model is null (free movement), pass an empty map
                    // REMOVED this line as PoseOverlay doesn't have a 'landmarks' property directly
                    // it.landmarks = poseResult?.landmarks()?.firstOrNull()
                }
            )

            // Display feedback and summary
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter) // Place feedback at the bottom
                    .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                    .padding(8.dp)
            ) {
                Text(text = "Reps: $repCount", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                feedbackMessages.forEach { message ->
                    Text(text = message, color = Color.LightGray, fontSize = 16.sp)
                }
            }

            // Instruction Overlay
            if (showInstructions && currentExerciseModel != null) {
                AlertDialog(
                    onDismissRequest = { /* Cannot dismiss without accepting */ }, // User must click "Start Exercise"
                    title = { Text("Exercise Instructions: ${currentExerciseModel?.name}") },
                    text = {
                        Column {
                            Text(currentExerciseModel?.description ?: "No description available.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Phases:", fontWeight = FontWeight.Bold)
                            currentExerciseModel?.phases?.forEach { phase ->
                                Text("- ${phase.name}: ${phase.feedbackMessage ?: "No specific guidance."}")
                                phase.targetAngles.forEach { (angleName, range) ->
                                    Text("  -> $angleName: ${"%.0f-%.0f°".format(range.min, range.max)}", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showInstructions = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Start Exercise", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                )
            }


            // Real-time pose detection loop using ViewModel
            LaunchedEffect(previewView, showInstructions) { // Depend on showInstructions to restart when instructions are dismissed
                if (previewView == null || showInstructions) {
                    // Log.d("ExerciseTrackingScreen", "Detection paused: previewView null or instructions showing.")
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
                    delay(66) // ~15 FPS processing rate
                }
                Log.d("ExerciseTrackingScreen", "Pose detection loop stopped.")
            }
        }
    }
}