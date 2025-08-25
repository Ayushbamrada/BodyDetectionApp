//package com.example.bodydetectionapp.ui.exercise_list
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.ChevronRight
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.bodydetectionapp.data.models.BodyPart
//import com.example.bodydetectionapp.data.models.ExerciseDefinitions
//import com.example.bodydetectionapp.ui.components.AppBackground
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ExerciseListScreen(
//    bodyPartName: String,
//    onExerciseClicked: (String) -> Unit,
//    onNavigateBack: () -> Unit
//) {
//    val bodyPart = BodyPart.valueOf(bodyPartName)
//    val exercises = ExerciseDefinitions.ALL_EXERCISES.filter { it.bodyPart == bodyPart }
//
//    AppBackground {
//        Scaffold(
//            containerColor = Color.Transparent,
//            topBar = {
//                TopAppBar(
//                    title = { Text(formatBodyPartName(bodyPart), fontWeight = FontWeight.Bold) },
//                    navigationIcon = {
//                        IconButton(onClick = onNavigateBack) {
//                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
//                        }
//                    },
//                    colors = TopAppBarDefaults.topAppBarColors(
//                        containerColor = Color.Transparent,
//                        titleContentColor = Color.White
//                    )
//                )
//            }
//        ) { paddingValues ->
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//                    .padding(horizontal = 16.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp),
//                contentPadding = PaddingValues(
//                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
//                )
//            ) {
//                items(exercises) { exercise ->
//                    ExerciseListItem(
//                        exerciseName = exercise.name,
//                        onClicked = { onExerciseClicked(exercise.name) }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ExerciseListItem(
//    exerciseName: String,
//    onClicked: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onClicked),
//        shape = RoundedCornerShape(16.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 20.dp, vertical = 24.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text(text = exerciseName, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White)
//            Icon(Icons.Default.ChevronRight, contentDescription = "Start Exercise", tint = Color.White)
//        }
//    }
//}
//
//private fun formatBodyPartName(bodyPart: BodyPart): String {
//    return bodyPart.name.replace('_', ' ').split(' ')
//        .joinToString(" ") { it.capitalize() } + " Exercises"
//}
package com.example.bodydetectionapp.ui.exercise_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bodydetectionapp.data.models.BodyPart
import com.example.bodydetectionapp.data.models.ExerciseDefinitions
import com.example.bodydetectionapp.ui.components.AppBackground
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    bodyPartName: String,
    onExerciseClicked: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val bodyPart = BodyPart.valueOf(bodyPartName)
    val exercises = ExerciseDefinitions.ALL_EXERCISES.filter { it.bodyPart == bodyPart }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(formatBodyPartName(bodyPart), fontWeight = FontWeight.Bold) },
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
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 16.dp
                )
            ) {
                itemsIndexed(exercises) { index, exercise ->
                    ExerciseListItem(
                        exerciseName = exercise.name,
                        // Determine alignment based on whether the index is even or odd
                        isImageOnLeft = index % 2 == 0,
                        onClicked = { onExerciseClicked(exercise.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseListItem(
    exerciseName: String,
    isImageOnLeft: Boolean,
    onClicked: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100) // Small delay for the animation to be noticeable
        isVisible = true
    }

    // Animate each item sliding in from the side
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                slideInHorizontally(
                    initialOffsetX = { if (isImageOnLeft) -it else it },
                    animationSpec = tween(durationMillis = 500)
                )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable(onClick = onClicked),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Min), // Ensures row children can fill height
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isImageOnLeft) {
                    ImagePlaceholder()
                    Spacer(modifier = Modifier.width(16.dp))
                    TextContent(exerciseName)
                } else {
                    TextContent(exerciseName)
                    Spacer(modifier = Modifier.width(16.dp))
                    ImagePlaceholder()
                }
            }
        }
    }
}

@Composable
private fun RowScope.ImagePlaceholder() {
    // This is the placeholder for your future exercise images
    Box(
        modifier = Modifier
            .weight(0.4f) // Takes up 40% of the row width
            .aspectRatio(1f) // Makes it a perfect square
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Image,
            contentDescription = "Exercise Image Placeholder",
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun RowScope.TextContent(exerciseName: String) {
    Column(
        modifier = Modifier
            .weight(0.6f) // Takes up 60% of the row width
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Text(
            text = exerciseName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Start Now",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.7f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatBodyPartName(bodyPart: BodyPart): String {
    return bodyPart.name.replace('_', ' ').split(' ')
        .joinToString(" ") { it.capitalize() } + " Exercises"
}