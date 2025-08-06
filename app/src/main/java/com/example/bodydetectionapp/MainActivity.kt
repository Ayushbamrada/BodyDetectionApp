package com.example.bodydetectionapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.bodydetectionapp.navigation.AppNavGraph
import com.example.bodydetectionapp.ui.theme.BodyDetectionAppTheme

class MainActivity : ComponentActivity() {

    // 1. Register for permission request result
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, set your app content
            setAppContent()
        } else {
            // Permission denied.
            // You should inform the user why the permission is needed and
            // gracefully handle the case where functionality is limited.
            // For example, display a message or disable camera-dependent features.
            // For now, we'll just set an empty content to prevent a crash,
            // but in a real app, you'd provide user feedback.
            setContent {
                BodyDetectionAppTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        // You could display a message here:
                        // Text("Camera permission is required to use this app.", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Check and request camera permission on app start
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            setAppContent()
        } else {
            // Request permission from the user
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // 3. Encapsulate content setting logic, including AppNavGraph
    private fun setAppContent() {
        setContent {
            BodyDetectionAppTheme { // Apply your app's theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController) // Your main navigation graph is placed here
                }
            }
        }
    }
}