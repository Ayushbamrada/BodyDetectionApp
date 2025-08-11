package com.example.bodydetectionapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bodydetectionapp.R // Make sure this R points to your app's resources
import com.example.bodydetectionapp.navigation.Screen // Your navigation routes
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Scale animation state
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animate from 0x scale to 1x scale
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1500, // Animation duration
                easing = FastOutSlowInEasing
            )
        )
        delay(500) // Hold full scale for a moment

        // Corrected navigation:
        // Pop the Splash screen from the back stack and then navigate to ExerciseSelection.
        // `inclusive = true` ensures the Splash screen is removed completely.
        navController.navigate(Screen.ExerciseSelection.route) {
            popUpTo(Screen.Splash.route) {
                inclusive = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        //
        Image(
            painter = painterResource(id = R.drawable.ripple_healthcare_logo), // Replace with your logo's drawable name
            contentDescription = "Company Logo",
            modifier = Modifier
                .size(200.dp)
                .scale(scale.value)
        )
    }
}