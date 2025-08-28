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

//package com.example.bodydetectionapp.ui.components
//
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.LinearEasing
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.gestures.detectTapGestures
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.drawscope.DrawScope
//import androidx.compose.ui.input.pointer.pointerInput
//import kotlinx.coroutines.launch
//import kotlin.math.pow
//import kotlin.math.sqrt
//import kotlin.random.Random
//
//// Data class for the static background nodes
//private data class Node(val position: Offset)
//
//// Data class for each interactive touch ripple
//private data class Ripple(val center: Offset, val progress: Animatable<Float, *>)
//
//@Composable
//private fun NeuralNetworkCanvas(ripples: List<Ripple>) {
//    val nodes = remember {
//        List(150) {
//            Node(position = Offset(Random.nextFloat(), Random.nextFloat()))
//        }
//    }
//
//    Canvas(modifier = Modifier.fillMaxSize()) {
//        drawStaticNetwork(nodes)
//
//        val ripplesCopy = ripples.toList()
//        ripplesCopy.forEach { ripple ->
//            drawRippleEffect(ripple, nodes)
//        }
//    }
//}
//
//private fun DrawScope.drawStaticNetwork(nodes: List<Node>) {
//    nodes.forEach { node ->
//        drawCircle(
//            color = Color.Cyan,
//            radius = 1.5f,
//            center = Offset(node.position.x * size.width, node.position.y * size.height),
//            alpha = 0.2f
//        )
//    }
//}
//
//private fun DrawScope.drawRippleEffect(ripple: Ripple, nodes: List<Node>) {
//    val rippleProgress = ripple.progress.value
//    val rippleCenter = Offset(ripple.center.x, ripple.center.y)
//
//    val boomRadius = 250f * rippleProgress
//    val boomAlpha = 1f - rippleProgress
//
//    // --- THE CRITICAL FIX IS HERE ---
//    // Only draw the circle if its radius is a positive number.
//    if (boomRadius > 0f && boomAlpha > 0f) {
//        drawCircle(
//            brush = Brush.radialGradient(
//                colors = listOf(
//                    Color.Cyan.copy(alpha = 0.5f * boomAlpha),
//                    Color.Transparent
//                ),
//                center = rippleCenter,
//                radius = boomRadius
//            ),
//            radius = boomRadius,
//            center = rippleCenter,
//
//        )
//    }
//
//    val affectedNodes = nodes.filter {
//        val nodePos = Offset(it.position.x * size.width, it.position.y * size.height)
//        val distance = sqrt((nodePos.x - rippleCenter.x).pow(2) + (nodePos.y - rippleCenter.y).pow(2))
//        distance < 400f
//    }
//
//    affectedNodes.forEach { node ->
//        val startPoint = Offset(node.position.x * size.width, node.position.y * size.height)
//        val endPoint = rippleCenter
//        val lineEndProgress = (rippleProgress * 2f).coerceAtMost(1f)
//        val animatedEndPoint = Offset(
//            x = startPoint.x + (endPoint.x - startPoint.x) * lineEndProgress,
//            y = startPoint.y + (endPoint.y - startPoint.y) * lineEndProgress
//        )
//        val lineAlpha = (1f - kotlin.math.abs(rippleProgress - 0.5f) * 2) * 0.7f
//        if (lineAlpha > 0) {
//            drawLine(
//                color = Color.Cyan,
//                start = startPoint,
//                end = animatedEndPoint,
//                strokeWidth = 1.5f,
//                alpha = lineAlpha,
//
//            )
//        }
//    }
//}
//
//@Composable
//fun AppBackground(content: @Composable () -> Unit) {
//    val coroutineScope = rememberCoroutineScope()
//    val ripples = remember { mutableStateListOf<Ripple>() }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(
//                        Color(0xFF00080B),
//                        Color.Black
//                    )
//                )
//            )
//            .pointerInput(Unit) {
//                detectTapGestures { offset ->
//                    coroutineScope.launch {
//                        val newRipple = Ripple(
//                            center = offset,
//                            progress = Animatable(0f)
//                        )
//                        ripples.add(newRipple)
//
//                        newRipple.progress.animateTo(
//                            targetValue = 1f,
//                            animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
//                        )
//
//                        ripples.remove(newRipple)
//                    }
//                }
//            }
//    ) {
//        NeuralNetworkCanvas(ripples = ripples)
//        content()
//    }
//}
