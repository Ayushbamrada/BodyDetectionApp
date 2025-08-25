package com.example.bodydetectionapp.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

// This helper function finds the current Activity from the context
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

// This is the main composable we will use to control the screen orientation
@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current

    // --- CORRECTED DisposableEffect LOGIC ---
    DisposableEffect(orientation) {
        val activity = context.findActivity()
        // If the activity is null, we do nothing.
        if (activity != null) {
            val originalOrientation = activity.requestedOrientation
            activity.requestedOrientation = orientation

            // The onDispose block is the return value of the DisposableEffect lambda.
            onDispose {
                // When the screen is disposed, restore the original orientation.
                activity.requestedOrientation = originalOrientation
            }
        } else {
            // If we can't find the activity, we still need to return an onDispose block.
            // In this case, it does nothing.
            onDispose { }
        }
    }
}