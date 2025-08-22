package com.example.bodydetectionapp.ui.report

import android.graphics.Paint
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URLDecoder
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

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
            val baseTime = exerciseSessionStartTime
            for (i in timestampsList.indices) {
                val currentTimestampData = timestampsList[i]
                val durationMillis = if (i == 0) {
                    currentTimestampData.timestamp - baseTime
                } else {
                    currentTimestampData.timestamp - timestampsList[i - 1].timestamp
                }
                calculatedRepDurations.add(
                    RepDuration(
                        repCount = currentTimestampData.repCount,
                        durationSeconds = durationMillis / 1000f
                    )
                )
            }
            totalTime = (timestampsList.lastOrNull()?.timestamp ?: baseTime) - baseTime
        }
        calculatedRepDurations to (totalTime / 1000L).coerceAtLeast(0L)
    }

    val caloriesBurned = remember(exerciseName, totalWorkoutTimeSeconds) {
        calculateCaloriesBurned(exerciseName, totalWorkoutTimeSeconds)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$exerciseName Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricDisplay(title = "Total Reps", value = finalRepCount.toString())
                        MetricDisplay(title = "Total Time", value = "${totalWorkoutTimeSeconds}s")
                    }
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    MetricDisplay(title = "Est. Calories Burned", value = "$caloriesBurned kcal")
                }
            }

            Text(
                text = "Repetition Durations (seconds)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .align(Alignment.Start)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (repDurations.isNotEmpty()) {
                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        RepDurationBarGraph(repDurations = repDurations)
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Not enough data for a graph.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.popBackStack(Screen.Home.route, inclusive = false) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Finish Workout", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MetricDisplay(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun RepDurationBarGraph(repDurations: List<RepDuration>) {
    val barColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val density = LocalDensity.current

    val barWidthDp = 40.dp
    val spacingDp = 16.dp
    val numReps = repDurations.size
    val totalWidth = (barWidthDp * numReps) + (spacingDp * (numReps - 1))

    Box(
        modifier = Modifier
            .width(totalWidth)
            .fillMaxHeight()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 40.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxDuration = repDurations.maxOfOrNull { it.durationSeconds }?.let { ceil(it * 1.2f) }?.coerceAtLeast(5f) ?: 5f
            val minDuration = 0f
            val durationRange = maxDuration - minDuration
            val safeDurationRange = if (durationRange == 0f) 1f else durationRange

            val yAxisLabelWidth = with(density) { 45.dp.toPx() }
            val xAxisLabelHeight = with(density) { 30.dp.toPx() }
            val effectiveHeight = size.height - xAxisLabelHeight
            val yAxisDrawEnd = effectiveHeight
            val xAxisDrawStart = yAxisLabelWidth

            val desiredNumYLabels = 5
            val rawStepY = safeDurationRange / (desiredNumYLabels - 1).coerceAtLeast(1)
            val stepSizeY = if (rawStepY > 0) {
                val exponent = floor(log10(rawStepY)).toInt()
                val factor = when {
                    rawStepY / (10.0.pow(exponent)) < 2.0 -> 1.0
                    rawStepY / (10.0.pow(exponent)) < 5.0 -> 2.0
                    else -> 5.0
                }
                (factor * (10.0.pow(exponent))).toFloat()
            } else { 1f }

            var currentYLabelValue = 0f
            while (currentYLabelValue <= maxDuration) {
                val y = yAxisDrawEnd - ((currentYLabelValue - minDuration) / safeDurationRange) * effectiveHeight
                if (y.isFinite()) {
                    drawLine(color = gridColor, start = Offset(xAxisDrawStart, y), end = Offset(size.width, y), strokeWidth = 1f)
                    drawContext.canvas.nativeCanvas.drawText("%.1f".format(currentYLabelValue), xAxisDrawStart - 8.dp.toPx(), y + 4.dp.toPx(), Paint().apply { color = onSurfaceColor.toArgb(); textAlign = Paint.Align.RIGHT; textSize = with(density) { 12.sp.toPx() } })
                }
                currentYLabelValue += stepSizeY
            }

            val barWidthPx = with(density) { barWidthDp.toPx() }
            val spacingPx = with(density) { spacingDp.toPx() }
            val repLabelPaint = Paint().apply { color = onSurfaceColor.toArgb(); textAlign = Paint.Align.CENTER; textSize = with(density) { 14.sp.toPx() }; isFakeBoldText = true }
            val durationLabelPaint = Paint().apply { color = onSurfaceColor.toArgb(); textAlign = Paint.Align.CENTER; textSize = with(density) { 12.sp.toPx() } }

            var currentXPosition = xAxisDrawStart
            repDurations.forEach { repData ->
                val barHeight = ((repData.durationSeconds - minDuration) / safeDurationRange) * effectiveHeight
                val barTopLeftY = yAxisDrawEnd - barHeight

                drawRect(
                    color = barColor,
                    topLeft = Offset(currentXPosition, barTopLeftY),
                    size = Size(barWidthPx, barHeight)
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
