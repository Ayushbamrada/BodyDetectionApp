

package com.example.bodydetectionapp.ui.report

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bodydetectionapp.data.models.ExerciseDefinitions
import com.example.bodydetectionapp.data.models.RepTimestamp
import com.example.bodydetectionapp.navigation.Screen
import com.example.bodydetectionapp.ui.components.AppBackground
import com.example.bodydetectionapp.ui.theme.RippleTeal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLDecoder
import kotlin.math.ceil

data class RepDuration(val repCount: Int, val durationSeconds: Float)

fun calculateCaloriesBurned(exerciseName: String, totalTimeSeconds: Long): String {
    val exercise = ExerciseDefinitions.ALL_EXERCISES.find { it.name == exerciseName }
    val metValue = exercise?.metValue ?: 3.5
    val averageWeightKg = 70
    val totalTimeHours = totalTimeSeconds / 3600.0
    val calories = metValue * averageWeightKg * totalTimeHours
    return "%.1f".format(calories)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseReportScreen(
    navController: NavController,
    exerciseName: String,
    finalRepCount: Int,
    repTimestampsJson: String?,
    exerciseSessionStartTime: Long
) {
    val (repDurations, totalWorkoutTimeSeconds) = remember(repTimestampsJson, exerciseSessionStartTime) {
        val timestampsList: List<RepTimestamp> = if (!repTimestampsJson.isNullOrEmpty()) {
            val decodedJson = URLDecoder.decode(repTimestampsJson, "UTF-8")
            val type = object : TypeToken<List<RepTimestamp>>() {}.type
            try {
                Gson().fromJson<List<RepTimestamp>>(decodedJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        val calculatedRepDurations = mutableListOf<RepDuration>()
        var totalTime: Long = 0L

        if (timestampsList.isNotEmpty()) {
            val sortedTimestamps = timestampsList.sortedBy { it.repCount }
            calculatedRepDurations.add(
                RepDuration(
                    repCount = sortedTimestamps[0].repCount,
                    durationSeconds = (sortedTimestamps[0].timestamp - exerciseSessionStartTime) / 1000f
                )
            )
            for (i in 1 until sortedTimestamps.size) {
                val durationMillis = sortedTimestamps[i].timestamp - sortedTimestamps[i - 1].timestamp
                calculatedRepDurations.add(
                    RepDuration(
                        repCount = sortedTimestamps[i].repCount,
                        durationSeconds = durationMillis / 1000f
                    )
                )
            }
            totalTime = (sortedTimestamps.lastOrNull()?.timestamp ?: exerciseSessionStartTime) - exerciseSessionStartTime
        }
        val finalDurations = calculatedRepDurations.filter { it.repCount <= finalRepCount }
        finalDurations to (totalTime / 1000L).coerceAtLeast(0L)
    }

    val caloriesBurned = remember(exerciseName, totalWorkoutTimeSeconds) {
        calculateCaloriesBurned(exerciseName, totalWorkoutTimeSeconds)
    }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Workout Summary", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = exerciseName,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetricDisplay(title = "Total Reps", value = finalRepCount.toString())
                        MetricDisplay(title = "Total Time", value = "${totalWorkoutTimeSeconds}s")
                        MetricDisplay(title = "Calories", value = caloriesBurned)
                    }
                }

                Text(
                    text = "Repetition Performance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .align(Alignment.Start)
                )
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    if (repDurations.isNotEmpty()) {
                        Box(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(16.dp)) {
                            RepDurationBarGraph(repDurations = repDurations)
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Not enough data for a graph.", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { navController.popBackStack(Screen.Home.route, inclusive = false) },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    Text("Finish Workout", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MetricDisplay(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun RepDurationBarGraph(repDurations: List<RepDuration>) {
    val barColor = RippleTeal
    val textColor = Color.White
    val density = LocalDensity.current

    // --- FIX: Animation state is now managed here, outside the Canvas ---
    val animatables = remember {
        repDurations.map { Animatable(0f) }
    }

    LaunchedEffect(repDurations) {
        // Launch a separate coroutine for each bar to animate them in sequence
        animatables.forEachIndexed { index, animatable ->
            launch {
                delay(index * 75L) // Staggered animation delay
                animatable.animateTo(
                    targetValue = 1f, // Animate from 0 (invisible) to 1 (fully visible)
                    animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic)
                )
            }
        }
    }

    val barWidthDp = 50.dp
    val spacingDp = 24.dp
    val numReps = repDurations.size
    val totalWidth = (barWidthDp * numReps) + (spacingDp * (numReps + 1))

    Box(
        modifier = Modifier
            .width(totalWidth)
            .fillMaxHeight()
            .padding(top = 20.dp, bottom = 30.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxDuration = repDurations.maxOfOrNull { it.durationSeconds }?.coerceAtLeast(1f) ?: 1f
            val xAxisLabelHeight = with(density) { 30.dp.toPx() }
            val effectiveHeight = size.height - xAxisLabelHeight

            val barWidthPx = with(density) { barWidthDp.toPx() }
            val spacingPx = with(density) { spacingDp.toPx() }

            val repLabelPaint = Paint().apply {
                color = textColor.toArgb()
                textAlign = Paint.Align.CENTER
                textSize = with(density) { 14.sp.toPx() }
            }
            val durationLabelPaint = Paint().apply {
                color = textColor.toArgb()
                textAlign = Paint.Align.CENTER
                textSize = with(density) { 12.sp.toPx() }
            }

            var currentXPosition = spacingPx

            repDurations.forEachIndexed { index, repData ->
                // Calculate the final height of the bar
                val barTargetHeight = (repData.durationSeconds / maxDuration) * effectiveHeight
                // The current animated height is the target height multiplied by the animation progress (0f to 1f)
                val barHeight = barTargetHeight * animatables[index].value
                val barTopLeftY = effectiveHeight - barHeight

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(currentXPosition, barTopLeftY),
                    size = Size(barWidthPx, barHeight),
                    cornerRadius = CornerRadius(16f, 16f)
                )

                drawContext.canvas.nativeCanvas.drawText(
                    "%.1fs".format(repData.durationSeconds),
                    currentXPosition + barWidthPx / 2,
                    barTopLeftY - 8.dp.toPx(),
                    durationLabelPaint
                )

                drawContext.canvas.nativeCanvas.drawText(
                    "${repData.repCount}",
                    currentXPosition + barWidthPx / 2,
                    size.height - (xAxisLabelHeight / 2) + 10.dp.toPx(),
                    repLabelPaint
                )

                currentXPosition += barWidthPx + spacingPx
            }
        }
    }
}
