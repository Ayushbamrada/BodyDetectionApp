package com.example.bodydetectionapp.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bodydetectionapp.navigation.Screen
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URLDecoder
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

// In ExerciseReportScreen.kt and ExerciseTrackingViewModel.kt
import com.example.bodydetectionapp.data.models.RepTimestamp // Adjust package if you put it elsewhere

// Ensure RepTimestamp and RepDuration data classes are here or accessible
data class RepDuration(val repCount: Int, val durationSeconds: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseReportScreen(
    navController: NavController,
    exerciseName: String,
    finalRepCount: Int,
    repTimestampsJson: String?,
    exerciseSessionStartTime: Long // NEW: Accept the actual session start time
) {
    // --- Data Processing ---
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
            // Use the passed exerciseSessionStartTime as the base for all calculations
            val baseTime = exerciseSessionStartTime

            for (i in timestampsList.indices) {
                val currentTimestampData = timestampsList[i]
                val durationMillis = if (i == 0) {
                    // For the very first rep, its duration is from the provided session start time
                    currentTimestampData.timestamp - baseTime
                } else {
                    // For subsequent reps, duration is difference from the previous rep's completion time
                    currentTimestampData.timestamp - timestampsList[i - 1].timestamp
                }
                calculatedRepDurations.add(
                    RepDuration(
                        repCount = currentTimestampData.repCount,
                        durationSeconds = durationMillis / 1000f
                    )
                )
            }

            // Total workout time: Last rep's timestamp minus the actual exercise session start time
            totalTime = (timestampsList.lastOrNull()?.timestamp ?: baseTime) - baseTime
            totalTime = (totalTime / 1000L).coerceAtLeast(0L) // Ensure non-negative seconds
        }
        calculatedRepDurations to totalTime
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$exerciseName Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Prominent Total Reps and Total Time
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total Reps",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = finalRepCount.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total Time",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${totalWorkoutTimeSeconds}s",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
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
                    .height(300.dp), // Increased height for better graph visibility
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (repDurations.isNotEmpty()) {
                    RepDurationLineGraph(repDurations = repDurations)
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

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.popBackStack(Screen.ExerciseSelection.route, inclusive = false) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp)), // More rounded corners
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Finish Workout", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun RepDurationLineGraph(repDurations: List<RepDuration>) {
    val lineColor = MaterialTheme.colorScheme.primary
    val pointColor = MaterialTheme.colorScheme.secondary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val density = LocalDensity.current

    // --- Generate Heartbeat Graph Points ---
    val graphPoints = remember(repDurations) {
        val points = mutableListOf<PointF>()
        if (repDurations.isNotEmpty()) {
            // Add a starting point at (0, 0) to represent the baseline before the first rep
            points.add(PointF(0f, 0f))

            repDurations.forEachIndexed { index, repData ->
                val repNumber = repData.repCount.toFloat()
                val duration = repData.durationSeconds

                // Point for the peak of the current rep
                points.add(PointF(repNumber, duration))

                // Point to bring the line back to 0 (baseline) after the rep
                // We use a small offset on the X-axis for visual separation between reps
                // The X-value here could be repNumber + 0.5f to denote "after this rep"
                points.add(PointF(repNumber + 0.5f, 0f))
            }
        }
        points
    }

    val maxDuration = remember(repDurations) {
        // Find max duration from actual repDurations, ensure it's at least 5f for graph scaling
        repDurations.maxOfOrNull { it.durationSeconds }?.let { ceil(it * 1.2f) }?.coerceAtLeast(5f) ?: 5f
    }
    val minDuration = 0f // Baseline is always 0 for heartbeat graph

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 30.dp) // Increased bottom padding for X-axis label
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val durationRange = maxDuration - minDuration
            val safeDurationRange = if (durationRange == 0f) 1f else durationRange

            // Adjust effective drawing area for labels
            val yAxisLabelWidth = with(density) { 45.dp.toPx() } // Space for Y-axis labels
            val xAxisLabelHeight = with(density) { 30.dp.toPx() } // Space for X-axis labels
            val valueLabelOffset = with(density) { 10.dp.toPx() } // Offset for duration value text above points

            val effectiveHeight = size.height - xAxisLabelHeight // Height for graph drawing area
            val effectiveWidth = size.width - yAxisLabelWidth // Width for graph drawing area

            val yAxisDrawStart = 0f // Top of the graph area for drawing
            val yAxisDrawEnd = effectiveHeight // Bottom of the graph area for drawing
            val xAxisDrawStart = yAxisLabelWidth // Left edge of graph area for drawing
            val xAxisDrawEnd = size.width // Right edge of graph area for drawing

            val numRepsForXAxis = repDurations.size
            // X-axis scale: Need space for rep 0, rep 1, rep 1.5, rep 2, rep 2.5 etc.
            // If 1 rep, points at 0, 1, 1.5
            // If 3 reps, points at 0, 1, 1.5, 2, 2.5, 3, 3.5
            val maxGraphX = if (numRepsForXAxis > 0) repDurations.last().repCount.toFloat() + 0.5f else 1f
            val xAxisStep = if (maxGraphX > 0) effectiveWidth / maxGraphX else effectiveWidth // Calculate spacing for X points


            // Draw Y-axis (Duration) labels and grid lines
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
            } else {
                1f
            }

            var currentYLabelValue = floor(minDuration / stepSize) * stepSize
            while (currentYLabelValue <= maxDuration + stepSize / 2) {
                val y = yAxisDrawEnd - ((currentYLabelValue - minDuration) / safeDurationRange) * effectiveHeight

                if (y.isFinite() && y >= yAxisDrawStart && y <= yAxisDrawEnd + 10.dp.toPx()) {
                    drawLine(
                        color = gridColor,
                        start = Offset(xAxisDrawStart, y),
                        end = Offset(xAxisDrawEnd, y),
                        strokeWidth = 1f
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        "%.1f".format(currentYLabelValue),
                        xAxisDrawStart - 8.dp.toPx(),
                        y + (with(density) { 12.sp.toPx() / 3 }),
                        Paint().apply {
                            color = onSurfaceColor.toArgb() // Use Compose Color
                            textAlign = Paint.Align.RIGHT
                            textSize = with(density) { 12.sp.toPx() }
                        }
                    )
                }
                currentYLabelValue += stepSize
            }

            // Y-axis title
            drawContext.canvas.nativeCanvas.save()
            drawContext.canvas.nativeCanvas.rotate(-90f)
            drawContext.canvas.nativeCanvas.drawText(
                "Duration (s)",
                -(effectiveHeight / 2 + yAxisDrawStart + 20.dp.toPx()),
                yAxisLabelWidth / 2 - 20.dp.toPx(),
                Paint().apply {
                    color = onSurfaceColor.toArgb() // Use Compose Color
                    textAlign = Paint.Align.CENTER
                    textSize = with(density) { 14.sp.toPx() }
                    isFakeBoldText = true
                }
            )
            drawContext.canvas.nativeCanvas.restore()

            // Draw X-axis (Repetition) labels and grid lines
            // We only want to label integer rep numbers (1, 2, 3...)
            for (i in 1..numRepsForXAxis) { // Loop from 1 to total rep count
                val x = xAxisDrawStart + (i.toFloat() * xAxisStep) // Calculate X for rep number

                // Draw vertical grid line for each actual rep number
                drawLine(
                    color = gridColor,
                    start = Offset(x, yAxisDrawEnd),
                    end = Offset(x, yAxisDrawStart),
                    strokeWidth = 1f
                )
                // Rep number labels
                drawContext.canvas.nativeCanvas.drawText(
                    "$i",
                    x,
                    size.height - xAxisLabelHeight + (with(density) { 12.sp.toPx() * 0.7f }), // Position below X-axis line, adjusted
                    Paint().apply {
                        color = onSurfaceColor.toArgb() // Use Compose Color
                        textAlign = Paint.Align.CENTER
                        textSize = with(density) { 12.sp.toPx() }
                    }
                )
            }
            // X-axis title
            drawContext.canvas.nativeCanvas.drawText(
                "Repetition",
                xAxisDrawStart + effectiveWidth / 2, // Center of the effective width
                size.height - 5.dp.toPx(), // Adjusted position: further down to prevent cutoff
                Paint().apply {
                    color = onSurfaceColor.toArgb() // Use Compose Color
                    textAlign = Paint.Align.CENTER
                    textSize = with(density) { 14.sp.toPx() }
                    isFakeBoldText = true
                }
            )

            // Draw the line graph and point values
            if (graphPoints.isNotEmpty()) {
                val path = Path()

                // Move to the first point (which will be 0,0)
                val firstPoint = graphPoints.first()
                val startX = xAxisDrawStart + firstPoint.x * xAxisStep
                val startY = yAxisDrawEnd - ((firstPoint.y - minDuration) / safeDurationRange) * effectiveHeight
                path.moveTo(startX, startY)

                // Draw lines to subsequent points
                graphPoints.forEach { point ->
                    val x = xAxisDrawStart + point.x * xAxisStep
                    val y = yAxisDrawEnd - ((point.y - minDuration) / safeDurationRange) * effectiveHeight
                    path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw circles and duration text only for actual rep duration points (not the 0-points)
                repDurations.forEach { repData ->
                    val x = xAxisDrawStart + repData.repCount.toFloat() * xAxisStep
                    val y = yAxisDrawEnd - ((repData.durationSeconds - minDuration) / safeDurationRange) * effectiveHeight

                    // Draw point
                    drawCircle(
                        color = pointColor,
                        center = Offset(x, y),
                        radius = 6.dp.toPx()
                    )
                    drawCircle( // Inner circle for better visibility
                        color = Color.White,
                        center = Offset(x, y),
                        radius = 3.dp.toPx()
                    )

                    // Draw duration text above the point
                    val textToDisplay = "%.1fs".format(repData.durationSeconds)
                    val textPaint = Paint().apply {
                        color = onSurfaceColor.toArgb() // Use Compose Color
                        textAlign = Paint.Align.CENTER
                        textSize = with(density) { 12.sp.toPx() }
                        isFakeBoldText = true
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        textToDisplay,
                        x,
                        y - valueLabelOffset, // Position above the point
                        textPaint
                    )
                }
            }
        }
    }
}

// Helper data class for graph points (can be internal to the file)
private data class PointF(val x: Float, val y: Float)