package com.example.bodydetectionapp.ui.exercise

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseName: String,
    exerciseDetailViewModel: ExerciseDetailViewModel,
    onStartClicked: (String, Int, Int) -> Unit // name, reps, time
) {
    LaunchedEffect(exerciseName) {
        exerciseDetailViewModel.loadExercise(exerciseName)
    }

    val exercise by exerciseDetailViewModel.exercise.collectAsState()
    var selectedReps by remember { mutableStateOf(10) }
    var selectedTime by remember { mutableStateOf(100) }

    if (exercise == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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

        Text(text = exercise!!.name, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = exercise!!.description, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Set Your Goal", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GoalChip(text = "10 Reps", isSelected = selectedReps == 10) {
                selectedReps = 10
                selectedTime = 100
            }
            GoalChip(text = "20 Reps", isSelected = selectedReps == 20) {
                selectedReps = 20
                selectedTime = 200
            }
            GoalChip(text = "30 Reps", isSelected = selectedReps == 30) {
                selectedReps = 30
                selectedTime = 300
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onStartClicked(exercise!!.name, selectedReps, selectedTime) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Start Exercise", fontSize = 18.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text) },
        shape = RoundedCornerShape(16.dp)
    )
}

// --- FIX: Moved GifPlayer here to be accessible ---
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
