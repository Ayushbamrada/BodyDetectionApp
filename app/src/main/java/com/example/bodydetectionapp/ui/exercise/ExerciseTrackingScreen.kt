//
//package com.example.bodydetectionapp.ui.exercise
//
//import android.content.pm.ActivityInfo
//import android.graphics.Bitmap
//import androidx.camera.view.PreviewView
//import androidx.compose.animation.*
//import androidx.compose.animation.core.EaseInOutCubic
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowForward
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.example.bodydetectionapp.ml.ExerciseState
//import com.example.bodydetectionapp.navigation.Screen
//import com.example.bodydetectionapp.ui.components.CameraPreview
//import com.example.bodydetectionapp.ui.components.PoseOverlay
//import com.example.bodydetectionapp.ui.theme.RippleTeal
//import com.example.bodydetectionapp.utils.LockScreenOrientation
//import com.google.gson.Gson
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.isActive
//import java.net.URLEncoder
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ExerciseTrackingScreen(
//    navController: NavController,
//    exerciseName: String,
//    repGoal: Int,
//    timeGoal: Int,
//    modifier: Modifier = Modifier,
//    viewModel: ExerciseTrackingViewModel = viewModel()
//) {
//    // This unlocks the screen for rotation. When you navigate away, it locks back to portrait.
//    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR)
//
//    val poseResult by viewModel.poseResult.collectAsState()
//    val anglesToDisplay by viewModel.anglesToDisplay.collectAsState()
//    val feedbackMessage by viewModel.feedbackMessage.collectAsState()
//    val repCount by viewModel.repCount.collectAsState()
//    val currentExercise by viewModel.currentExercise.collectAsState()
//    val exerciseState by viewModel.exerciseState.collectAsState()
//    val countdownValue by viewModel.countdownValue.collectAsState()
//    val exerciseStartTime by viewModel.exerciseStartTime.collectAsState()
//
//    var previewView: PreviewView? by remember { mutableStateOf(null) }
//
//    LaunchedEffect(Unit) {
//        viewModel.initialize(exerciseName, repGoal, timeGoal)
//    }
//
//    LaunchedEffect(exerciseState) {
//        if (exerciseState == ExerciseState.FINISHED) {
//            delay(1500)
//            val timestampsJson = Gson().toJson(viewModel.repTimestamps)
//            val encodedJson = URLEncoder.encode(timestampsJson, "UTF-8")
//            val startTime = exerciseStartTime ?: System.currentTimeMillis()
//
//            navController.navigate(
//                Screen.ExerciseReport.createRoute(
//                    exerciseName,
//                    repCount,
//                    encodedJson,
//                    startTime
//                )
//            ) {
//                popUpTo(Screen.Home.route)
//            }
//        }
//    }
//
//    // The Box is the root layer for the camera feed
//    Box(modifier = modifier.fillMaxSize()) {
//        CameraPreview(
//            modifier = Modifier.fillMaxSize(),
//            onPreviewReady = { previewView = it }
//        )
//
//        AndroidView(
//            modifier = Modifier.fillMaxSize(),
//            factory = { PoseOverlay(it) },
//            update = { overlay ->
//                overlay.poseResult = poseResult
//                overlay.anglesToDisplay = anglesToDisplay
//            }
//        )
//
//        // The Scaffold provides the structure for UI elements on top of the camera
//        Scaffold(
//            containerColor = Color.Transparent, // Makes Scaffold transparent
//            topBar = {
//                TopAppBar(
//                    title = { Text(currentExercise?.name ?: "Loading...") },
//                    colors = TopAppBarDefaults.topAppBarColors(
//                        containerColor = Color.Transparent,
//                        titleContentColor = Color.White // White provides the best contrast
//                    ),
//                    actions = {
//                        IconButton(onClick = { viewModel.finishExercise("Workout stopped.") }) {
//                            Icon(
//                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
//                                contentDescription = "Finish Exercise",
//                                tint = Color.White // White provides the best contrast
//                            )
//                        }
//                    }
//                )
//            }
//        ) { paddingValues ->
//            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
//                // Countdown Timer UI
//                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    AnimatedVisibility(
//                        visible = exerciseState == ExerciseState.COUNTDOWN,
//                        enter = scaleIn(animationSpec = tween(500, easing = EaseInOutCubic)) + fadeIn(),
//                        exit = scaleOut(animationSpec = tween(500, easing = EaseInOutCubic)) + fadeOut()
//                    ) {
//                        Text(
//                            text = countdownValue.toString(),
//                            fontSize = 250.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.White.copy(alpha = 0.8f)
//                        )
//                    }
//                }
//
//                // Rep Counter and Progress Indicator UI
//                AnimatedVisibility(
//                    visible = exerciseState == ExerciseState.IN_PROGRESS || exerciseState == ExerciseState.FINISHED,
//                    enter = fadeIn(animationSpec = tween(500)),
//                    exit = fadeOut(animationSpec = tween(500))
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .align(Alignment.TopCenter)
//                            .padding(top = 16.dp)
//                            .clip(RoundedCornerShape(24.dp))
//                            .background(Color.Black.copy(alpha = 0.2f)) // Glassmorphism background
//                            .padding(horizontal = 24.dp, vertical = 12.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        val progress = if (repGoal > 0) (repCount.toFloat() / repGoal.toFloat()) else 0f
//                        val animatedProgress by animateFloatAsState(
//                            targetValue = progress,
//                            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
//                            label = "RepProgressAnimation"
//                        )
//
//                        CircularProgressIndicator(
//                            progress = { animatedProgress },
//                            modifier = Modifier.size(120.dp),
//                            color = RippleTeal,
//                            trackColor = Color.White.copy(alpha = 0.3f),
//                            strokeWidth = 8.dp,
//                            strokeCap = StrokeCap.Round
//                        )
//
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            Text("Reps", fontSize = 20.sp, color = Color.White.copy(alpha = 0.8f))
//                            Text(
//                                text = repCount.toString(),
//                                fontSize = 48.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color.White
//                            )
//                        }
//                    }
//                }
//
//                // Feedback Message UI
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .align(Alignment.BottomCenter)
//                        .padding(horizontal = 32.dp, vertical = 48.dp)
//                ) {
//                    AnimatedVisibility(
//                        visible = feedbackMessage.isNotBlank() && exerciseState != ExerciseState.COUNTDOWN,
//                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
//                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
//                    ) {
//                        Text(
//                            text = feedbackMessage,
//                            color = Color.White,
//                            fontSize = 20.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            textAlign = TextAlign.Center,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
//                                .padding(16.dp)
//                        )
//                    }
//                }
//            }
//        }
//
//
////        LaunchedEffect(previewView) {
////            if (previewView == null) return@LaunchedEffect
////            while (isActive && exerciseState != ExerciseState.FINISHED) {
////                previewView?.bitmap?.let { frameBitmap ->
////                    val copyBitmap = frameBitmap.copy(Bitmap.Config.ARGB_8888, true)
////                    viewModel.detectPose(copyBitmap)
////                }
////                delay(33)
////            }
////        }
//        LaunchedEffect(previewView) {
//            if (previewView == null) return@LaunchedEffect
//
//            var lastFrameTime = 0L
//            // We will process a frame every 66ms, which is roughly 15 FPS.
//            // This is smooth enough for tracking but light enough for most processors.
//            val frameIntervalMs = 66L
//
//            while (isActive && exerciseState != ExerciseState.FINISHED) {
//                val currentTime = System.currentTimeMillis()
//                if (currentTime - lastFrameTime >= frameIntervalMs) {
//                    previewView?.bitmap?.let { frameBitmap ->
//                        // --- THIS IS THE NEW CODE ---
//                        // 1. Define the new, smaller dimensions (e.g., 50% of the original).
//                        val newWidth = frameBitmap.width / 2
//                        val newHeight = frameBitmap.height / 2
//
//                        // 2. Create a scaled-down version of the bitmap.
//                        val scaledBitmap = Bitmap.createScaledBitmap(frameBitmap, newWidth, newHeight, true)
//                        viewModel.detectPose(scaledBitmap)
//
//                        // 3. Send the SMALLER bitmap to the ViewModel for processing.
////This is my original code for better tracking above 3 lines of code is for optimise the performance by reducing dimensions
////                        val copyBitmap = frameBitmap.copy(Bitmap.Config.ARGB_8888, true)
////                        viewModel.detectPose(copyBitmap)
//
//                    }
//                    lastFrameTime = currentTime
//                }
//                // A small delay to prevent this loop from running too hot while waiting
//                delay(16)
//            }
//        }
//
//    }
//}
package com.example.bodydetectionapp.ui.exercise

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bodydetectionapp.ml.ExerciseState
import com.example.bodydetectionapp.navigation.Screen
import com.example.bodydetectionapp.ui.components.CameraPreview
import com.example.bodydetectionapp.ui.components.PoseOverlay
import com.example.bodydetectionapp.ui.theme.RippleTeal
import com.example.bodydetectionapp.utils.LockScreenOrientation
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseTrackingScreen(
    navController: NavController,
    exerciseName: String,
    repGoal: Int,
    timeGoal: Int,
    modifier: Modifier = Modifier,
    viewModel: ExerciseTrackingViewModel = viewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR)

    val poseResult by viewModel.poseResult.collectAsState()
    val anglesToDisplay by viewModel.anglesToDisplay.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()
    val repCount by viewModel.repCount.collectAsState()
    val currentExercise by viewModel.currentExercise.collectAsState()
    val exerciseState by viewModel.exerciseState.collectAsState()
    val countdownValue by viewModel.countdownValue.collectAsState()
    val exerciseStartTime by viewModel.exerciseStartTime.collectAsState()

    var previewView: PreviewView? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        viewModel.initialize(exerciseName, repGoal, timeGoal)
    }

    LaunchedEffect(exerciseState) {
        if (exerciseState == ExerciseState.FINISHED) {
            delay(1500)
            val timestampsJson = Gson().toJson(viewModel.repTimestamps)
            val encodedJson = URLEncoder.encode(timestampsJson, "UTF-8")
            val startTime = exerciseStartTime ?: System.currentTimeMillis()

            navController.navigate(
                Screen.ExerciseReport.createRoute(
                    exerciseName,
                    repCount,
                    encodedJson,
                    startTime
                )
            ) {
                popUpTo(Screen.Home.route)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onPreviewReady = { previewView = it }
        )

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { PoseOverlay(it) },
            update = { overlay ->
                overlay.poseResult = poseResult
                overlay.anglesToDisplay = anglesToDisplay
            }
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(currentExercise?.name ?: "Loading...") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.finishExercise("Workout stopped.") }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Finish Exercise",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // (Your existing UI for countdown, rep counter, and feedback message goes here)
                // Countdown Timer UI
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AnimatedVisibility(
                        visible = exerciseState == ExerciseState.COUNTDOWN,
                        enter = scaleIn(animationSpec = tween(500, easing = EaseInOutCubic)) + fadeIn(),
                        exit = scaleOut(animationSpec = tween(500, easing = EaseInOutCubic)) + fadeOut()
                    ) {
                        Text(
                            text = countdownValue.toString(),
                            fontSize = 250.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Rep Counter and Progress Indicator UI
                AnimatedVisibility(
                    visible = exerciseState == ExerciseState.IN_PROGRESS || exerciseState == ExerciseState.FINISHED,
                    enter = fadeIn(animationSpec = tween(500)),
                    exit = fadeOut(animationSpec = tween(500))
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.Black.copy(alpha = 0.2f)) // Glassmorphism background
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val progress = if (repGoal > 0) (repCount.toFloat() / repGoal.toFloat()) else 0f
                        val animatedProgress by animateFloatAsState(
                            targetValue = progress,
                            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                            label = "RepProgressAnimation"
                        )

                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(120.dp),
                            color = RippleTeal,
                            trackColor = Color.White.copy(alpha = 0.3f),
                            strokeWidth = 8.dp,
                            strokeCap = StrokeCap.Round
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Reps", fontSize = 20.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                text = repCount.toString(),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Feedback Message UI
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 32.dp, vertical = 48.dp)
                ) {
                    AnimatedVisibility(
                        visible = feedbackMessage.isNotBlank() && exerciseState != ExerciseState.COUNTDOWN,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        Text(
                            text = feedbackMessage,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        )
                    }
                }
            }
        }

        // --- THE ADAPTIVE PERFORMANCE LOGIC ---
        LaunchedEffect(previewView, currentExercise) { // React to changes in the exercise
            if (previewView == null || currentExercise == null) return@LaunchedEffect

            var lastFrameTime = 0L
            val frameIntervalMs = 66L // Throttle to ~15 FPS

            while (isActive && exerciseState != ExerciseState.FINISHED) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFrameTime >= frameIntervalMs) {
                    previewView?.bitmap?.let { frameBitmap ->

                        // Check the flag for the current exercise
                        val bitmapToProcess = if (currentExercise!!.requiresHighPrecision) {
                            // For yoga/calf raises, use the original, high-quality image
                            frameBitmap.copy(Bitmap.Config.ARGB_8888, true)
                        } else {
                            // For squats/high knees, use the faster, scaled-down image
                            val newWidth = frameBitmap.width / 2
                            val newHeight = frameBitmap.height / 2
                            Bitmap.createScaledBitmap(frameBitmap, newWidth, newHeight, true)
                        }

                        viewModel.detectPose(bitmapToProcess)
                    }
                    lastFrameTime = currentTime
                }
                delay(16)
            }
        }
    }
}