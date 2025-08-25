//package com.example.bodydetectionapp.ui.screens
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.*
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.slideInVertically
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.example.bodydetectionapp.R
//import com.example.bodydetectionapp.data.repository.UserRepository
//import com.example.bodydetectionapp.navigation.Screen
//import com.example.bodydetectionapp.ui.theme.RippleTeal
//import kotlinx.coroutines.delay
//import kotlin.math.pow
//import kotlin.math.sqrt
//import kotlin.random.Random
//
//// --- A "pulsing" nerve effect background ---
//@Composable
//private fun NeuralNetworkBackground() {
//    val particles = remember {
//        List(70) {
//            Particle(
//                x = Random.nextFloat(),
//                y = Random.nextFloat(),
//                radius = Random.nextFloat() * 1.5f + 1f
//            )
//        }
//    }
//
//    val infiniteTransition = rememberInfiniteTransition(label = "background_animation")
//
//    // Animate a pulse wave that expands from the center and restarts
//    val pulse by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 1.5f, // Pulse expands beyond the screen edges
//        animationSpec = infiniteRepeatable(
//            animation = tween(durationMillis = 3000, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ), label = "pulse_progress"
//    )
//
//    Canvas(modifier = Modifier.fillMaxSize()) {
//        val centerX = size.width / 2
//        val centerY = size.height / 2
//        val maxDist = sqrt(centerX.pow(2) + centerY.pow(2))
//
//        particles.forEach { particle ->
//            val particlePos = Offset(particle.x * size.width, particle.y * size.height)
//            val distFromCenter = sqrt((particlePos.x - centerX).pow(2) + (particlePos.y - centerY).pow(2)) / maxDist
//
//            // Brightness depends on the particle's distance to the expanding pulse wave
//            val pulseProximity = 1f - (kotlin.math.abs(pulse - distFromCenter))
//            val brightness = (pulseProximity.pow(10) * 0.8f).coerceIn(0.05f, 0.8f)
//
//            // Draw the nodes (particles)
//            drawCircle(
//                color = Color.White,
//                center = particlePos,
//                radius = particle.radius,
//                alpha = brightness
//            )
//
//            // Draw lines to nearby particles
//            particles.forEach { otherParticle ->
//                val distance = particle.distanceTo(otherParticle, size.width, size.height)
//                if (distance < size.width * 0.2f) {
//                    val otherPos = Offset(otherParticle.x * size.width, otherParticle.y * size.height)
//                    drawLine(
//                        color = Color.White,
//                        start = particlePos,
//                        end = otherPos,
//                        strokeWidth = 0.5f,
//                        alpha = brightness * 0.5f // Line brightness matches node brightness
//                    )
//                }
//            }
//        }
//    }
//}
//
//private data class Particle(val x: Float, val y: Float, val radius: Float) {
//    fun distanceTo(other: Particle, width: Float, height: Float): Float {
//        val dx = (x - other.x) * width
//        val dy = (y - other.y) * height
//        return sqrt(dx * dx + dy * dy)
//    }
//}
//
//
//@Composable
//fun SplashScreen(navController: NavController, userRepository: UserRepository) {
//    var topTextVisible by remember { mutableStateOf(false) }
//    var bottomContentVisible by remember { mutableStateOf(false) }
//
//    LaunchedEffect(Unit) {
//        delay(500) // Initial delay before animations start
//        topTextVisible = true
//        delay(500) // Stagger the animation of the bottom content
//        bottomContentVisible = true
//        delay(3000) // How long the content stays on screen
//
//        val destination = if (userRepository.hasCompletedOnboarding()) {
//            Screen.Home.route
//        } else {
//            Screen.Onboarding.route
//        }
//        navController.navigate(destination) {
//            popUpTo(Screen.Splash.route) { inclusive = true }
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                brush = Brush.linearGradient(
//                    colors = listOf(
//                        RippleTeal.copy(alpha = 0.8f),
//                        Color(0xFF003D4D) // A darker, richer teal
//                    ),
//                    start = Offset(0f, 0f),
//                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
//                )
//            ),
//        contentAlignment = Alignment.Center
//    ) {
//        // --- Animated Neural Network Background ---
//        NeuralNetworkBackground()
//
//        // --- Animated Content ---
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 24.dp, vertical = 60.dp),
//            verticalArrangement = Arrangement.SpaceBetween
//        ) {
//            // Animate top text sliding in from the top
//            AnimatedVisibility(
//                visible = topTextVisible,
//                enter = fadeIn(animationSpec = tween(1000)) +
//                        slideInVertically(
//                            initialOffsetY = { -it / 2 },
//                            animationSpec = tween(1000, easing = EaseOutCubic)
//                        )
//            ) {
//                Text(
//                    text = "Your AI Exercise App",
//                    color = Color.White,
//                    fontSize = 28.sp,
//                    fontWeight = FontWeight.Bold,
//                    textAlign = TextAlign.Center,
//                )
//            }
//
//            // Animate bottom content sliding in from the bottom
//            AnimatedVisibility(
//                visible = bottomContentVisible,
//                enter = fadeIn(animationSpec = tween(1000)) +
//                        slideInVertically(
//                            initialOffsetY = { it / 2 },
//                            animationSpec = tween(1000, easing = EaseOutCubic)
//                        )
//            ) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text(
//                        text = "Powered by",
//                        color = Color.White.copy(alpha = 0.8f),
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Medium,
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier.padding(bottom = 8.dp)
//                    )
//                    Image(
//                        painter = painterResource(id = R.drawable.ripple_healthcare_logo),
//                        contentDescription = "Company Logo",
//                        modifier = Modifier.fillMaxWidth(0.7f)
//                    )
//                }
//            }
//        }
//    }
//}
package com.example.bodydetectionapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bodydetectionapp.R
import com.example.bodydetectionapp.data.repository.UserRepository
import com.example.bodydetectionapp.navigation.Screen
import com.example.bodydetectionapp.ui.components.AppBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, userRepository: UserRepository) {
    var topTextVisible by remember { mutableStateOf(false) }
    var bottomContentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        topTextVisible = true
        delay(500)
        bottomContentVisible = true
        delay(3000)

        // Using your original, stable navigation logic
        val destination = if (userRepository.hasCompletedOnboarding()) {
            Screen.Home.route
        } else {
            Screen.Onboarding.route
        }
        navController.navigate(destination) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    // Use the single, consistent AppBackground for the entire screen
    AppBackground {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 60.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedVisibility(
                visible = topTextVisible,
                enter = fadeIn(animationSpec = tween(1000)) +
                        slideInVertically(
                            initialOffsetY = { -it / 2 },
                            animationSpec = tween(1000, easing = EaseOutCubic)
                        )
            ) {
                Text(
                    text = "Your AI Exercise App",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }

            AnimatedVisibility(
                visible = bottomContentVisible,
                enter = fadeIn(animationSpec = tween(1000)) +
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(1000, easing = EaseOutCubic)
                        )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Powered by",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Company Logo",
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                }
            }
        }
    }
}