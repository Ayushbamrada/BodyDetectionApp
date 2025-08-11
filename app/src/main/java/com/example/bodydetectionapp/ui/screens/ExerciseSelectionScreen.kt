package com.example.bodydetectionapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bodydetectionapp.data.models.Exercise
import com.example.bodydetectionapp.data.models.ExerciseDefinitions
import com.example.bodydetectionapp.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelectionScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Fitness Dashboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Brush.verticalGradient(
                    colors = listOf(Color(0xFFE0F7FA), Color(0xFFB3E5FC)) // Light blue gradient
                ))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose Your Workout!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // Two columns for grid view
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Free Movement Tracking Card
                item {
                    ExerciseCard(
                        title = "Free Movement",
                        description = "Track your pose without specific exercise guidance.",
                        onClick = { navController.navigate(Screen.ExerciseTracking.createRoute("free_movement")) }
                    )
                }

                // Dynamic Exercise Cards from ExerciseDefinitions
                items(ExerciseDefinitions.ALL_EXERCISES) { exercise ->
                    ExerciseCard(
                        title = exercise.name,
                        description = exercise.description,
                        onClick = { navController.navigate(Screen.ExerciseTracking.createRoute(exercise.name)) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Fixed height for consistent grid
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center,
                maxLines = 3 // Limit description lines
            )
            // Optional: Add an icon related to the exercise
            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                imageVector = Icons.Default.Info, // Placeholder icon
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}