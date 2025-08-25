package com.example.bodydetectionapp.ui.exercise

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.bodydetectionapp.ui.components.AppBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseName: String,
    exerciseDetailViewModel: ExerciseDetailViewModel,
    onStartClicked: (String, Int, Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(exerciseName) {
        exerciseDetailViewModel.loadExercise(exerciseName)
    }
    val exercise by exerciseDetailViewModel.exercise.collectAsState()
    var selectedReps by remember { mutableStateOf(10) }
    var selectedTime by remember { mutableStateOf(100) } // You might want to remove this if it's derived from reps

    if (exercise == null) {
        AppBackground {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        return
    }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(exercise!!.name, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                Button(
                    onClick = { onStartClicked(exercise!!.name, selectedReps, selectedTime) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(50.dp)
                ) {
                    Text("Start Exercise", fontSize = 18.sp)
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                exercise!!.videoResId?.let { gifId ->
                    GifPlayer(
                        gifId = gifId,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = exercise!!.description,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Set Your Goal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    GoalChip(text = "10 Reps", isSelected = selectedReps == 10) { selectedReps = 10 }
                    GoalChip(text = "20 Reps", isSelected = selectedReps == 20) { selectedReps = 20 }
                    GoalChip(text = "30 Reps", isSelected = selectedReps == 30) { selectedReps = 30 }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    // We use a Surface with a clickable modifier to create our own chip.
    Surface(
        modifier = Modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isSelected) Color.White else Color.White.copy(alpha = 0.2f)),
        color = if (isSelected) Color.White else Color.Transparent,
        contentColor = if (isSelected) Color.Black else Color.White
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun GifPlayer(gifId: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
    Image(
        painter = rememberAsyncImagePainter(gifId, imageLoader),
        contentDescription = "Exercise Demo",
        modifier = modifier
    )
}