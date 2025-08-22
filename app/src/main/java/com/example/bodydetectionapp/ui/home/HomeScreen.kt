package com.example.bodydetectionapp.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bodydetectionapp.data.models.BodyPart
import com.example.bodydetectionapp.data.models.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onExerciseClicked: (String) -> Unit // Callback to navigate to the detail screen
) {
    val userName by homeViewModel.userName.collectAsState()
    val exerciseModules by homeViewModel.exerciseModules.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Dynamic Welcome Message ---
        item {
            Text(
                text = "Welcome back, ${userName ?: "User"}!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // --- Exercise Modules ---
        items(exerciseModules.entries.toList()) { (bodyPart, exercises) ->
            ExerciseModuleCard(
                bodyPart = bodyPart,
                exercises = exercises,
                onExerciseClicked = onExerciseClicked
            )
        }
    }
}

@Composable
fun ExerciseModuleCard(
    bodyPart: BodyPart,
    exercises: List<Exercise>,
    onExerciseClicked: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = formatBodyPartName(bodyPart),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            exercises.forEach { exercise ->
                Text(
                    text = exercise.name,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExerciseClicked(exercise.name) }
                        .padding(vertical = 8.dp)
                )
                Divider()
            }
        }
    }
}

// Helper function to make the enum names look nice on the screen
private fun formatBodyPartName(bodyPart: BodyPart): String {
    return bodyPart.name.replace('_', ' ').split(' ')
        .joinToString(" ") { it.capitalize() }
}
