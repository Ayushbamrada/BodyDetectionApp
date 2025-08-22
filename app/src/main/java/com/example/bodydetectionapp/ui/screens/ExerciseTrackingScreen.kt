//package com.example.bodydetectionapp.ui.screens
//
//import android.graphics.Bitmap
//import android.os.Build.VERSION.SDK_INT
//import androidx.camera.view.PreviewView
//import androidx.compose.animation.*
//import androidx.compose.foundation.Image
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
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import coil.ImageLoader
//import coil.compose.rememberAsyncImagePainter
//import coil.decode.GifDecoder
//import coil.decode.ImageDecoderDecoder
//import com.example.bodydetectionapp.data.models.ExerciseDefinitions
//import com.example.bodydetectionapp.ml.ExerciseState
//import com.example.bodydetectionapp.navigation.Screen
//import com.example.bodydetectionapp.ui.components.CameraPreview
//import com.example.bodydetectionapp.ui.components.PoseOverlay
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
//    modifier: Modifier = Modifier,
//    viewModel: ExerciseTrackingViewModel = viewModel()
//) {
//    val context = LocalContext.current
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
//    var showInstructionsDialog by remember { mutableStateOf(true) }
//
//
//    LaunchedEffect(Unit) {
//        viewModel.initializePoseDetectionHelper(context)
//        val selectedExercise = ExerciseDefinitions.ALL_EXERCISES.find { it.name == exerciseName }
//        selectedExercise?.let {
//            viewModel.setExercise(it)
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(currentExercise?.name ?: "Loading...") },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.Transparent),
//                actions = {
//                    IconButton(onClick = {
//                        val timestampsJson = Gson().toJson(viewModel.repTimestamps)
//                        val encodedJson = URLEncoder.encode(timestampsJson, "UTF-8")
//                        val startTime = exerciseStartTime ?: System.currentTimeMillis()
//
//                        navController.navigate(
//                            Screen.ExerciseReport.createRoute(
//                                exerciseName,
//                                repCount,
//                                encodedJson,
//                                startTime
//                            )
//                        )
//                    }) {
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
//                            contentDescription = "Finish Exercise",
//                            tint = Color.Black
//                        )
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
//            CameraPreview(
//                modifier = Modifier.fillMaxSize(),
//                onPreviewReady = { previewView = it }
//            )
//
//            AndroidView(
//                modifier = Modifier.fillMaxSize(),
//                factory = { PoseOverlay(it) },
//                update = { overlay ->
//                    overlay.poseResult = poseResult
//                    overlay.anglesToDisplay = anglesToDisplay
//                }
//            )
//
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                AnimatedVisibility(
//                    visible = exerciseState == ExerciseState.COUNTDOWN,
//                    enter = scaleIn() + fadeIn(),
//                    exit = scaleOut() + fadeOut()
//                ) {
//                    Text(
//                        text = countdownValue.toString(),
//                        fontSize = 200.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.White.copy(alpha = 0.8f)
//                    )
//                }
//            }
//
//            AnimatedVisibility(
//                visible = exerciseState == ExerciseState.IN_PROGRESS,
//                enter = fadeIn(),
//                exit = fadeOut()
//            ) {
//                Column(
//                    modifier = Modifier
//                        .align(Alignment.TopCenter)
//                        .padding(top = 16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text("Reps", fontSize = 24.sp, color = Color.White.copy(alpha = 0.8f))
//                    Text(repCount.toString(), fontSize = 80.sp, fontWeight = FontWeight.Bold, color = Color.White)
//                }
//            }
//
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//                    .padding(32.dp)
//                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
//                    .padding(16.dp)
//            ) {
//                Text(
//                    text = feedbackMessage,
//                    color = Color.White,
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//
//            if (showInstructionsDialog && currentExercise != null) {
//                AlertDialog(
//                    onDismissRequest = { /* Prevent dismissing by clicking outside */ },
//                    title = { Text(text = currentExercise!!.name, fontWeight = FontWeight.Bold) },
//                    text = {
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            currentExercise!!.videoResId?.let { gifId ->
//                                // --- FIX: Replaced VideoPlayer with GifPlayer ---
//                                GifPlayer(gifId = gifId, modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(200.dp)
//                                    .clip(RoundedCornerShape(12.dp))
//                                )
//                            }
//                            Spacer(modifier = Modifier.height(16.dp))
//                            Text(text = currentExercise!!.description)
//                        }
//                    },
//                    confirmButton = {
//                        Button(onClick = { showInstructionsDialog = false }) {
//                            Text("I'm Ready")
//                        }
//                    }
//                )
//            }
//
//            LaunchedEffect(previewView, showInstructionsDialog) {
//                if (previewView == null || showInstructionsDialog) {
//                    return@LaunchedEffect
//                }
//                while (isActive) {
//                    previewView?.bitmap?.let { frameBitmap ->
//                        val copyBitmap = frameBitmap.copy(Bitmap.Config.ARGB_8888, true)
//                        viewModel.detectPose(copyBitmap)
//                    }
//                    delay(33)
//                }
//            }
//        }
//    }
//}
//
//// --- NEW: Reusable Composable for playing GIFs from the raw resource folder using Coil ---
//@Composable
//fun GifPlayer(gifId: Int, modifier: Modifier = Modifier) {
//    val context = LocalContext.current
//    // Create an ImageLoader that can decode GIFs
//    val imageLoader = ImageLoader.Builder(context)
//        .components {
//            if (SDK_INT >= 28) {
//                add(ImageDecoderDecoder.Factory())
//            } else {
//                add(GifDecoder.Factory())
//            }
//        }
//        .build()
//
//    Image(
//        painter = rememberAsyncImagePainter(gifId, imageLoader),
//        contentDescription = "Exercise Demo",
//        modifier = modifier
//    )
//}
