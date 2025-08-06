package com.example.bodydetectionapp.ui.screens.freepose

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bodydetectionapp.ml.PoseDetectionHelper
import com.example.bodydetectionapp.ui.components.CameraPreview
import com.example.bodydetectionapp.ui.components.PoseOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun FreePoseScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var overlayView: PoseOverlay? by remember { mutableStateOf(null) }
    var highlightedJoints by remember { mutableStateOf(emptySet<Int>()) } // State for highlighted joints

    // Initialize PoseDetectionHelper, it will copy the model if needed and set up the detector.
    val poseHelper = remember {
        // Modified callback to receive highlighted joints
        PoseDetectionHelper(context) { result, newHighlightedJoints ->
            overlayView?.poseResult = result
            highlightedJoints = newHighlightedJoints // Update the state
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onPreviewReady = {
                previewView = it
            }
        )

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                // Pass the highlightedJoints state to the PoseOverlay
                PoseOverlay(it).also {
                    overlayView = it
                    it.highlightedJointIndices = highlightedJoints // Initial set
                }
            },
            update = {
                // This 'update' block ensures the PoseOverlay's highlightedJointIndices is updated
                // whenever the 'highlightedJoints' state changes.
                it.highlightedJointIndices = highlightedJoints
            }
        )

        LaunchedEffect(previewView) {
            if (previewView == null) return@LaunchedEffect

            while (isActive) {
                previewView?.bitmap?.let { frameBitmap ->
                    val copyBitmap = frameBitmap.copy(Bitmap.Config.ARGB_8888, true)
                    if (copyBitmap != null) {
                        poseHelper.detect(copyBitmap)
                    } else {
                        Log.w("FreePoseScreen", "Failed to get bitmap from PreviewView or copy it.")
                    }
                } ?: run {
                    Log.d("FreePoseScreen", "PreviewView bitmap is null.")
                }
                delay(66)
            }
        }
    }
}