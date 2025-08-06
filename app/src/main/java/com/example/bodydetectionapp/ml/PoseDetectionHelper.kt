package com.example.bodydetectionapp.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.io.File
import kotlin.math.pow
import kotlin.math.sqrt

class PoseDetectionHelper(
    context: Context,
    private val onResult: (PoseLandmarkerResult, Set<Int>) -> Unit // Modified callback
) {
    private val poseLandmarker: PoseLandmarker
    private var lastPoseResult: PoseLandmarkerResult? = null // To store the previous result
    private val MOVEMENT_THRESHOLD = 0.005f // Adjust this value to sensitivity of movement detection

    init {
        val modelAssetName = "pose_landmarker_lite.task"
        val modelFile = File(context.filesDir, modelAssetName)
        if (!modelFile.exists()) {
            try {
                context.assets.open(modelAssetName).use { input ->
                    modelFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.e("PoseDetectionHelper", "Failed to copy model asset: $modelAssetName", e)
            }
        }

        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(modelFile.absolutePath)
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result, _ ->
                val highlightedJoints = calculateMovingJoints(result, lastPoseResult)
                onResult(result, highlightedJoints) // Pass both result and highlighted joints
                lastPoseResult = result // Update last result for next frame
            }
            .setErrorListener { Log.e("PoseDetectionHelper", "Pose detection error", it) }
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    fun detect(bitmap: Bitmap) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        poseLandmarker.detectAsync(mpImage, System.currentTimeMillis())
    }

    /**
     * Calculates which joints have moved significantly between the current and previous pose results.
     * Returns a set of indices for moving joints.
     */
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
}