package com.example.bodydetectionapp.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.bodydetectionapp.utils.AngleCalculator // Import AngleCalculator
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.io.File
import kotlin.math.sqrt
import kotlin.math.pow

class PoseDetectionHelper(
    context: Context,
    // Modified callback: now also provides a map of angles
    private val onResult: (PoseLandmarkerResult, Set<Int>, Map<String, Double>) -> Unit
) {
    private val poseLandmarker: PoseLandmarker
    private var lastPoseResult: PoseLandmarkerResult? = null
    private val MOVEMENT_THRESHOLD: Double = 0.005 // Changed to Double for consistency with distance calculation

    init {
        val modelAssetName = "pose_landmarker_lite.task"
        val modelFile = File(context.filesDir, modelAssetName)

        // Only copy the model if it doesn't already exist in the files directory
        if (!modelFile.exists()) {
            try {
                context.assets.open(modelAssetName).use { input ->
                    modelFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("PoseDetectionHelper", "Model copied from assets to ${modelFile.absolutePath}")
            } catch (e: Exception) {
                Log.e("PoseDetectionHelper", "Failed to copy model asset: $modelAssetName", e)
                // Handle the error (e.g., disable pose detection or show error to user)
            }
        } else {
            Log.d("PoseDetectionHelper", "Model already exists at ${modelFile.absolutePath}")
        }

        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(modelFile.absolutePath)
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result, _ ->
                val highlightedJoints = calculateMovingJoints(result, lastPoseResult)
                val currentAngles = result.landmarks().firstOrNull()?.let { landmarks ->
                    AngleCalculator.getExerciseAngles(landmarks) // Use your new AngleCalculator
                } ?: emptyMap()

                onResult(result, highlightedJoints, currentAngles) // Pass angles here
                lastPoseResult = result
            }
            .setErrorListener { Log.e("PoseDetectionHelper", "Pose detection error", it) }
            .build()

        // Create the PoseLandmarker instance
        poseLandmarker = try {
            PoseLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            Log.e("PoseDetectionHelper", "Failed to create PoseLandmarker: ${e.message}", e)
            // Provide a fallback or re-throw a custom exception if critical
            // For now, throw a runtime exception to crash early if setup fails
            throw RuntimeException("Failed to initialize PoseLandmarker", e)
        }
    }

    fun detect(bitmap: Bitmap) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        poseLandmarker.detectAsync(mpImage, System.currentTimeMillis())
    }

    private fun calculateMovingJoints(
        currentResult: PoseLandmarkerResult,
        previousResult: PoseLandmarkerResult?
    ): Set<Int> {
        val movingJoints = mutableSetOf<Int>()

        val currentLandmarks = currentResult.landmarks().firstOrNull()
        val previousLandmarks = previousResult?.landmarks()?.firstOrNull()

        if (currentLandmarks != null && previousLandmarks != null && currentLandmarks.size == previousLandmarks.size) {
            for (i in currentLandmarks.indices) {
                val current = currentLandmarks[i]
                val previous = previousLandmarks[i]

                // Calculate Euclidean distance between the current and previous landmark positions
                val distance = sqrt(
                    (current.x() - previous.x()).toDouble().pow(2) +
                            (current.y() - previous.y()).toDouble().pow(2)
                )

                if (distance > MOVEMENT_THRESHOLD) {
                    movingJoints.add(i)
                }
            }
        }
        return movingJoints
    }

    fun close() {
        poseLandmarker.close()
    }
}