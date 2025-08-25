//package com.example.bodydetectionapp.ui.components
//
//import androidx.compose.animation.core.*
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import com.example.bodydetectionapp.ui.theme.RippleTeal
//import kotlin.math.pow
//import kotlin.math.sqrt
//import kotlin.random.Random
//
//// Data class to manage each particle's state for the background
//private data class Particle(val x: Float, val y: Float, val radius: Float) {
//    fun distanceTo(other: Particle, width: Float, height: Float): Float {
//        val dx = (x - other.x) * width
//        val dy = (y - other.y) * height
//        return sqrt(dx * dx + dy * dy)
//    }
//}
//
//// The "pulsing" nerve effect background
//@Composable
//private fun NeuralNetworkBackground() {
//    val particles = remember {
//        List(70) { // Increased particle count for a denser network
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
//// This is the main composable we will use on every screen
//@Composable
//fun AppBackground(content: @Composable () -> Unit) {
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
//            )
//    ) {
//        NeuralNetworkBackground()
//        content()
//    }
//}
package com.example.bodydetectionapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

// Data class from your old structure to manage each particle's state
private data class Particle(val x: Float, val y: Float, val radius: Float) {
    fun distanceTo(other: Particle, width: Float, height: Float): Float {
        val dx = (x - other.x) * width
        val dy = (y - other.y) * height
        return sqrt(dx * dx + dy * dy)
    }
}

// The "pulsing" nerve effect background with the new glow effect
@Composable
private fun NeuralNetworkBackground() {
    val particles = remember {
        List(70) { // Particle count from the old code
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 2f + 1.5f // Slightly larger radius for a better glow
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "background_animation")

    // Animate a pulse wave that expands from the center and restarts
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1.5f, // Pulse expands beyond the screen edges
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "pulse_progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxDist = sqrt(centerX.pow(2) + centerY.pow(2))

        particles.forEach { particle ->
            val particlePos = Offset(particle.x * size.width, particle.y * size.height)
            val distFromCenter = sqrt((particlePos.x - centerX).pow(2) + (particlePos.y - centerY).pow(2)) / maxDist

            // Brightness depends on the particle's distance to the expanding pulse wave
            val pulseProximity = 1f - (kotlin.math.abs(pulse - distFromCenter))
            val brightness = (pulseProximity.pow(10) * 0.9f).coerceIn(0.1f, 0.9f)

            // --- NEW: Create the "boom" light effect with a RadialGradient ---
            val glowRadius = particle.radius * 3f // The glow extends beyond the particle's core
            val glowBrush = Brush.radialGradient(
                colors = listOf(
                    Color.Cyan.copy(alpha = brightness * 0.8f), // Bright, glowing center
                    Color.Transparent // Fades to nothing
                ),
                center = particlePos,
                radius = glowRadius
            )

            // Draw the glow effect
            drawCircle(
                brush = glowBrush,
                radius = glowRadius,
                center = particlePos
            )

            // Draw the solid core of the node on top of the glow
            drawCircle(
                color = Color.White, // A solid white core like the image
                center = particlePos,
                radius = particle.radius / 2, // Core is smaller than the particle radius
                alpha = brightness
            )


            // Draw lines to nearby particles, creating the web-like structure
            particles.forEach { otherParticle ->
                val distance = particle.distanceTo(otherParticle, size.width, size.height)
                if (distance < size.width * 0.2f) {
                    val otherPos = Offset(otherParticle.x * size.width, otherParticle.y * size.height)
                    drawLine(
                        color = Color.Cyan,
                        start = particlePos,
                        end = otherPos,
                        strokeWidth = 1.5f, // Slightly thicker lines
                        alpha = brightness * 0.4f // Line brightness matches node brightness
                    )
                }
            }
        }
    }
}

// This is the main composable with the dark background
@Composable
fun AppBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF111111), // Dark Charcoal Grey
                        Color.Black
                    )
                )
            )
    ) {
        NeuralNetworkBackground()
        content()
    }
}

