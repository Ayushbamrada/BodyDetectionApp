package com.example.bodydetectionapp.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.bodydetectionapp.data.models.Landmark
import com.example.bodydetectionapp.utils.AngleCalculator
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
//import com.google.mediapipe.tasks.vision.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.io.File

class PoseDetectionHelper(
    context: Context,
    private val onResult: (
        result: PoseLandmarkerResult,
        landmarks: Map<String, Landmark>?,
        angles: Map<String, Double>?
    ) -> Unit
) {
    private val poseLandmarker: PoseLandmarker

    init {
        Log.d("PoseDetectionHelper", "Initializing...")
        val modelAssetName = "pose_landmarker_lite.task"
        val modelFile = File(context.filesDir, modelAssetName)
        if (!modelFile.exists()) {
            try {
                context.assets.open(modelAssetName).use { input ->
                    modelFile.outputStream().use { output -> input.copyTo(output) }
                }
                Log.d("PoseDetectionHelper", "Model copied successfully.")
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
                Log.d("PoseDetectionHelper", "ResultListener invoked.")
                if (result.landmarks().isEmpty()) {
                    Log.d("PoseDetectionHelper", "No landmarks detected in this frame.")
                    onResult(result, null, null)
                    return@setResultListener
                }

                Log.d("PoseDetectionHelper", "Landmarks detected. Processing...")
                val landmarksList = result.landmarks().first()

                val landmarkMap = mutableMapOf<String, Landmark>()
                AngleCalculator.LandmarkIndices.landmarkNameMapping.forEach { (index, name) ->
                    if (index < landmarksList.size) {
                        val mpLandmark = landmarksList[index]
                        landmarkMap[name] = Landmark(mpLandmark.x(), mpLandmark.y(), mpLandmark.z())
                    }
                }

                val currentAngles = AngleCalculator.calculateAllAngles(landmarksList)
                Log.d("PoseDetectionHelper", "Calculated ${currentAngles.size} angles. Sending to ViewModel.")
                onResult(result, landmarkMap, currentAngles)
            }
            .setErrorListener { error ->
                // --- ADDED DETAILED ERROR LOGGING ---
                Log.e("PoseDetectionHelper", "MediaPipe Error: ${error.message}", error)
            }
            .build()

        try {
            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
            Log.d("PoseDetectionHelper", "PoseLandmarker created successfully.")
        } catch (e: Exception) {
            Log.e("PoseDetectionHelper", "Failed to create PoseLandmarker", e)
            // Re-throw to ensure the app doesn't continue in a broken state
            throw e
        }
    }

    fun detect(bitmap: Bitmap) {
        Log.d("PoseDetectionHelper", "detect() called. Preparing image for detection.")
        val mpImage = BitmapImageBuilder(bitmap).build()
        poseLandmarker.detectAsync(mpImage, System.currentTimeMillis())
        Log.d("PoseDetectionHelper", "detectAsync() submitted.")
    }

    fun close() {
        Log.d("PoseDetectionHelper", "Closing PoseLandmarker.")
        poseLandmarker.close()
    }
}
