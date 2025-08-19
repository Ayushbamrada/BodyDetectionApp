package com.example.bodydetectionapp.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll // <-- ADD THIS IMPORT
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

// --- NEW HELPER FUNCTION FOR CALORIE CALCULATION ---
fun calculateCaloriesBurned(exerciseName: String, totalTimeSeconds: Long): String {
    val exercise = ExerciseDefinitions.ALL_EXERCISES.find { it.name == exerciseName }
    val metValue = exercise?.metValue ?: 3.5 // Default to light exercise MET value
    val averageWeightKg = 70 // Assume an average user weight of 70kg for this calculation
    val totalTimeHours = totalTimeSeconds / 3600.0

    // Formula: METs * 3.5 * (body weight in kg) / 200 * (duration in minutes)
    // Simplified: METs * weight * time_in_hours
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

    // --- NEW: Calculate calories ---
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
                .verticalScroll(rememberScrollState()), // Make screen scrollable
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                // --- MODIFIED: Changed to a Column to better fit 3 items ---
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
                    .height(350.dp), // Increased height for better graph visibility
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (repDurations.isNotEmpty()) {
                    RepDurationBarGraph(repDurations = repDurations)
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
                onClick = { navController.popBackStack(Screen.ExerciseSelection.route, inclusive = false) },
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 40.dp) // Increased padding
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxDuration = repDurations.maxOfOrNull { it.durationSeconds }?.let { ceil(it * 1.2f) }?.coerceAtLeast(5f) ?: 5f
            val minDuration = 0f
            val durationRange = maxDuration - minDuration
            val safeDurationRange = if (durationRange == 0f) 1f else durationRange

            val yAxisLabelWidth = with(density) { 45.dp.toPx() }
            val xAxisLabelHeight = with(density) { 30.dp.toPx() }
            val effectiveHeight = size.height - xAxisLabelHeight
            val effectiveWidth = size.width - yAxisLabelWidth
            val yAxisDrawEnd = effectiveHeight
            val xAxisDrawStart = yAxisLabelWidth

            // --- RESPONSIVE BAR WIDTH AND SPACING LOGIC ---
            val numReps = repDurations.size
            val maxBarWidth = with(density) { 30.dp.toPx() }
            val minSpacing = with(density) { 4.dp.toPx() }

            // Calculate width based on available space
            var barWidth = (effectiveWidth - (numReps - 1) * minSpacing) / numReps
            if (barWidth > maxBarWidth) {
                barWidth = maxBarWidth
            }
            val spacingBetweenBars = (effectiveWidth - numReps * barWidth) / (numReps - 1).coerceAtLeast(1)

            // Draw Y-axis labels and grid lines (logic is fine)
            val desiredNumYLabels = 5
            val rawStep = safeDurationRange / (desiredNumYLabels - 1).coerceAtLeast(1)
            val stepSize = if (rawStep > 0) {
                val exponent = floor(log10(rawStep)).toInt()
                val factor = when {
                    rawStep / (10.0.pow(exponent)) < 2.0 -> 1.0
                    rawStep / (10.0.pow(exponent)) < 5.0 -> 2.0
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
                currentYLabelValue += stepSize
            }

            // --- DYNAMIC LABEL DRAWING LOGIC ---
            // Determine how many labels we can fit without them overlapping
            val repLabelPaint = Paint().apply { color = onSurfaceColor.toArgb(); textAlign = Paint.Align.CENTER; textSize = with(density) { 12.sp.toPx() } }
            val labelWidth = repLabelPaint.measureText("15") // Measure width of a typical label
            val labelFrequency = if (barWidth + spacingBetweenBars > 0) ceil(labelWidth * 2.5 / (barWidth + spacingBetweenBars)).toInt() else 1

            var currentXPosition = xAxisDrawStart
            repDurations.forEachIndexed { index, repData ->
                val barHeight = ((repData.durationSeconds - minDuration) / safeDurationRange) * effectiveHeight
                val barTopLeftY = yAxisDrawEnd - barHeight

                drawRect(color = barColor, topLeft = Offset(currentXPosition, barTopLeftY), size = Size(barWidth, barHeight))

                // Only draw rep number label if it fits
                if (index % labelFrequency == 0) {
                    drawContext.canvas.nativeCanvas.drawText("${repData.repCount}", currentXPosition + barWidth / 2, size.height - (xAxisLabelHeight / 2) + 8.dp.toPx(), repLabelPaint)
                }

                // Only draw duration text if the bar is wide enough
                if (barWidth > with(density) { 15.dp.toPx() }) {
                    drawContext.canvas.nativeCanvas.drawText("%.1fs".format(repData.durationSeconds), currentXPosition + barWidth / 2, barTopLeftY - 5.dp.toPx(), Paint().apply { color = onSurfaceColor.toArgb(); textAlign = Paint.Align.CENTER; textSize = with(density) { 11.sp.toPx() }; isFakeBoldText = true })
                }
                currentXPosition += barWidth + spacingBetweenBars
            }
        }
    }
}
