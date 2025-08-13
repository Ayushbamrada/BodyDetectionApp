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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bodydetectionapp.navigation.Screen
import com.example.bodydetectionapp.ui.screens.RepTimestamp // Make sure this data class is accessible
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URLDecoder
import kotlin.math.ceil
import kotlin.math.floor

// Assuming RepTimestamp is defined like this somewhere:
// data class RepTimestamp(val repCount: Int, val timestamp: Long)

// A data class to hold the duration of a single rep.
data class RepDuration(val repCount: Int, val durationSeconds: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseReportScreen(
    navController: NavController,
    exerciseName: String,
    finalRepCount: Int,
    repTimestampsJson: String?
) {
    // --- Data Processing ---
    val (repDurations, totalWorkoutTimeSeconds) = remember(repTimestampsJson) {
        val timestampsList: List<RepTimestamp> = if (!repTimestampsJson.isNullOrEmpty()) {
            val decodedJson = URLDecoder.decode(repTimestampsJson, "UTF-8")
            val type = object : TypeToken<List<RepTimestamp>>() {}.type
            try {
                Gson().fromJson<List<RepTimestamp>>(decodedJson, type) ?: emptyList()
            } catch (e: Exception) {
                // Log the error for debugging
                // Log.e("ExerciseReportScreen", "Error parsing repTimestampsJson: ${e.message}")
                emptyList()
            }
        } else {
            emptyList()
        }

        val calculatedRepDurations = mutableListOf<RepDuration>()
        var totalTime: Long = 0L

        if (timestampsList.isNotEmpty()) {
            val exerciseStartTime = timestampsList.first().timestamp // Assume first timestamp is effectively exercise start

            for (i in timestampsList.indices) {
                val currentTimestampData = timestampsList[i]
                val durationMillis = if (i == 0) {
                    // For the first rep, duration is from exercise start to its completion
                    // If your actual exercise starts AT the first rep, this is just its timestamp value
                    // relative to a theoretical 0. Otherwise, if you have a separate start time, use that.
                    // For now, assuming first rep's timestamp implies its duration from start.
                    currentTimestampData.timestamp - exerciseStartTime // If exerciseStartTime is true start of session
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

            // Calculate total workout time: Last rep's timestamp minus the first rep's timestamp
            if (timestampsList.size > 1) {
                totalTime = (timestampsList.last().timestamp - timestampsList.first().timestamp) / 1000L
            } else if (timestampsList.size == 1) {
                // If only one rep, total time is its duration from the start
                totalTime = (timestampsList.first().timestamp - exerciseStartTime) / 1000L
            }
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text("Finish Workout", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// RepDurationLineGraph remains mostly the same as the previous version,
// but I've included it again for completeness and any minor tweaks.
@Composable
fun RepDurationLineGraph(repDurations: List<RepDuration>) {
    val lineColor = MaterialTheme.colorScheme.primary
    val pointColor = MaterialTheme.colorScheme.secondary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp) // Overall padding for the graph area
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Find max/min durations, ensuring minDuration doesn't go below 0
            val maxDuration = repDurations.maxOfOrNull { it.durationSeconds }?.let { ceil(it * 1.2f) } ?: 1f // Add more buffer
            val minDuration = repDurations.minOfOrNull { it.durationSeconds }?.let { floor(it * 0.8f).coerceAtLeast(0f) } ?: 0f

            // Adjust effective drawing area for labels
            val yAxisLabelWidth = with(density) { 40.dp.toPx() } // Space for Y-axis labels
            val xAxisLabelHeight = with(density) { 30.dp.toPx() } // Space for X-axis labels

            val effectiveHeight = size.height - xAxisLabelHeight
            val effectiveWidth = size.width - yAxisLabelWidth

            val yAxisDrawStart = 0f
            val yAxisDrawEnd = effectiveHeight
            val xAxisDrawStart = yAxisLabelWidth
            val xAxisDrawEnd = size.width

            val repCount = repDurations.size

            // Draw Y-axis (Time) labels and grid lines
            val numYLabels = 5 // Example: 5 labels on Y-axis
            val yLabelRange = maxDuration - minDuration
            val yPixelInterval = if (numYLabels > 1) effectiveHeight / (numYLabels - 1) else effectiveHeight

            for (i in 0 until numYLabels) {
                val labelValue = minDuration + (yLabelRange / (numYLabels - 1).coerceAtLeast(1)) * i
                val y = yAxisDrawEnd - ((labelValue - minDuration) / yLabelRange) * effectiveHeight

                // Y-axis grid lines
                drawLine(
                    color = gridColor,
                    start = Offset(xAxisDrawStart, y),
                    end = Offset(xAxisDrawEnd, y),
                    strokeWidth = 1f
                )
                // Duration labels
                drawContext.canvas.nativeCanvas.drawText(
                    "%.1f".format(labelValue),
                    xAxisDrawStart - 8.dp.toPx(), // Position to the left of Y-axis line
                    y + (with(density) { 6.sp.toPx() / 2 }), // Adjust for vertical centering
                    Paint().apply {
                        color = android.graphics.Color.BLACK
                        textAlign = Paint.Align.RIGHT
                        textSize = with(density) { 12.sp.toPx() }
                    }
                )
            }
            // Y-axis title
            drawContext.canvas.nativeCanvas.save()
            drawContext.canvas.nativeCanvas.rotate(-90f)
            drawContext.canvas.nativeCanvas.drawText(
                "Duration (s)",
                -(effectiveHeight / 2 + yAxisDrawStart), // Center of the effective height
                yAxisLabelWidth / 2 - 8.dp.toPx(), // Roughly centered in the label width area
                Paint().apply {
                    color = android.graphics.Color.BLACK
                    textAlign = Paint.Align.CENTER
                    textSize = with(density) { 14.sp.toPx() }
                    isFakeBoldText = true
                }
            )
            drawContext.canvas.nativeCanvas.restore()

            // Draw X-axis (Reps) labels and grid lines
            val repLabelSpacing = if (repCount > 1) effectiveWidth / (repCount - 1).toFloat() else effectiveWidth
            for (i in 0 until repCount) {
                val x = xAxisDrawStart + i * repLabelSpacing

                // X-axis grid lines
                drawLine(
                    color = gridColor,
                    start = Offset(x, yAxisDrawEnd),
                    end = Offset(x, yAxisDrawStart),
                    strokeWidth = 1f
                )
                // Rep number labels
                drawContext.canvas.nativeCanvas.drawText(
                    "${repDurations[i].repCount}",
                    x,
                    size.height - 5.dp.toPx(), // Position below X-axis
                    Paint().apply {
                        color = android.graphics.Color.BLACK
                        textAlign = Paint.Align.CENTER
                        textSize = with(density) { 12.sp.toPx() }
                    }
                )
            }
            drawContext.canvas.nativeCanvas.drawText(
                "Repetition",
                xAxisDrawStart + effectiveWidth / 2, // Center of the effective width
                size.height + 15.dp.toPx(), // X-axis title position
                Paint().apply {
                    color = android.graphics.Color.BLACK
                    textAlign = Paint.Align.CENTER
                    textSize = with(density) { 14.sp.toPx() }
                    isFakeBoldText = true
                }
            )


            // Draw the line graph
            if (repCount > 0) {
                val path = Path()

                // Calculate the first point
                val firstRep = repDurations.first()
                val firstX = xAxisDrawStart
                val firstY = yAxisDrawEnd - ((firstRep.durationSeconds - minDuration) / yLabelRange) * effectiveHeight
                path.moveTo(firstX, firstY)

                // Draw lines to subsequent points
                repDurations.forEachIndexed { index, repData ->
                    if (index > 0) {
                        val x = xAxisDrawStart + index * repLabelSpacing
                        val y = yAxisDrawEnd - ((repData.durationSeconds - minDuration) / yLabelRange) * effectiveHeight
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw points on the line
                repDurations.forEachIndexed { index, repData ->
                    val x = xAxisDrawStart + index * repLabelSpacing
                    val y = yAxisDrawEnd - ((repData.durationSeconds - minDuration) / yLabelRange) * effectiveHeight
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
                }
            }
        }
    }
}