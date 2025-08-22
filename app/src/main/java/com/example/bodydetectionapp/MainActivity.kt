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
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.bodydetectionapp.data.repository.UserRepository
import com.example.bodydetectionapp.navigation.AppNavGraph
import com.example.bodydetectionapp.ui.theme.BodyDetectionAppTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setAppContent()
        } else {
            // Handle permission denial gracefully
            setContent {
                BodyDetectionAppTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        // In a real app, you'd show a proper message explaining why the permission is needed.
                    }
                }
            }
        }
    }

    // --- NEW: Create an instance of our UserRepository "librarian" ---
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the UserRepository
        userRepository = UserRepository(applicationContext)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setAppContent()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun setAppContent() {
        setContent {
            BodyDetectionAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    // --- UPDATED: Pass the userRepository to the AppNavGraph ---
                    AppNavGraph(navController = navController, userRepository = userRepository)
                }
            }
        }
    }
}
