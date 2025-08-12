package com.example.bodydetectionapp.ui.screens


import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bodydetectionapp.R
import com.example.bodydetectionapp.navigation.Screen
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember { Animatable(0f) }
    val fullText = "Your AI Exercise App"
    var visibleText by remember { mutableStateOf("") }


    LaunchedEffect(Unit) {
        // Animate the logo scaling up
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )


        // A brief pause after the logo appears
        delay(300)


        // Animate the text character by character
        fullText.forEach { char ->
            visibleText += char
            delay(50) // Delay between each character appearing
        }


        // Hold the full screen for a moment before navigating
        delay(1000)


        // Navigate to the next screen
        navController.navigate(Screen.ExerciseSelection.route) {
            popUpTo(Screen.Splash.route) {
                inclusive = true
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp) // Add padding for text
        ) {
            // Your app's logo
            Image(
                painter = painterResource(id = R.drawable.ripple_healthcare_logo),
                contentDescription = "Company Logo",
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale.value)
            )


            Spacer(modifier = Modifier.height(32.dp))


            // The text that appears character by character
            Text(
                text = visibleText,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(70.dp) // Set a fixed height to prevent layout jumps
            )
        }
    }
}