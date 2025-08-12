package com.example.bodydetectionapp.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Boy
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.navigation.NavController
import com.example.bodydetectionapp.data.models.ExerciseDefinitions
import com.example.bodydetectionapp.navigation.Screen

@Composable
fun ExerciseSelectionScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 48.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Welcome to Ripple!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Let's start your workout.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // FIX: Create a special, hardcoded card for "Free Movement"
            // This ensures the correct "free_movement" ID is always passed.
            item {
                AnimatedExerciseCard(
                    title = "Free Movement",
                    description = "Track your pose without specific exercise guidance.",
                    animationDelay = 0,
                    onClick = {
                        navController.navigate(Screen.ExerciseTracking.createRoute("free_movement"))
                    }
                )
            }

            // FIX: Loop only through the exercises from ExerciseDefinitions.
            itemsIndexed(ExerciseDefinitions.ALL_EXERCISES) { index, exercise ->
                AnimatedExerciseCard(
                    title = exercise.name,
                    description = exercise.description ?: "",
                    // Add 1 to index for animation delay because "Free Movement" is at index 0
                    animationDelay = (index + 1) * 100,
                    onClick = {
                        navController.navigate(Screen.ExerciseTracking.createRoute(exercise.name))
                    }
                )
            }
        }
    }
}

// Overloaded the card to accept raw strings for the special "Free Movement" case
@Composable
fun AnimatedExerciseCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    animationDelay: Int = 0
) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        animatedValue.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        )
    }

    val scale = lerp(0.8f.dp, 1f.dp, animatedValue.value).value
    val alpha = lerp(0f.dp, 1f.dp, animatedValue.value).value

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .scale(scale)
            .alpha(alpha)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getIconForExercise(title), // Use title to get the icon
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun getIconForExercise(exerciseName: String): ImageVector {
    return when (exerciseName.lowercase()) {
        "free movement" -> Icons.Default.Gesture
        "squat" -> Icons.Default.AccessibilityNew
        "push-up" -> Icons.Default.FitnessCenter
        "jumping jack" -> Icons.Default.Boy
        else -> Icons.Default.SportsGymnastics
    }
}