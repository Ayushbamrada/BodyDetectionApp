//package com.example.bodydetectionapp
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.ui.Modifier
//import androidx.core.content.ContextCompat
//import androidx.core.view.WindowCompat
//import androidx.navigation.compose.rememberNavController
//import com.example.bodydetectionapp.data.repository.UserRepository
//import com.example.bodydetectionapp.navigation.AppNavGraph
//import com.example.bodydetectionapp.ui.theme.BodyDetectionAppTheme
//
//class MainActivity : ComponentActivity() {
//
//    private val requestPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted: Boolean ->
//        if (isGranted) {
//            setAppContent()
//        } else {
//            // Handle permission denial gracefully
//            setContent {
//                BodyDetectionAppTheme {
//                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
//                        // In a real app, you'd show a proper message explaining why the permission is needed.
//                    }
//                }
//            }
//        }
//    }
//
//    // --- NEW: Create an instance of our UserRepository "librarian" ---
//    private lateinit var userRepository: UserRepository
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//// This tells the system that your app will handle drawing behind the system bars.
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//
//
//        // Initialize the UserRepository
//        userRepository = UserRepository(applicationContext)
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//            setAppContent()
//        } else {
//            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
//        }
//    }
//
//    private fun setAppContent() {
//        setContent {
//            BodyDetectionAppTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    val navController = rememberNavController()
//                    // --- UPDATED: Pass the userRepository to the AppNavGraph ---
//                    AppNavGraph(navController = navController, userRepository = userRepository)
//                }
//            }
//        }
//    }
//}
package com.example.bodydetectionapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.bodydetectionapp.data.repository.UserRepository
import com.example.bodydetectionapp.navigation.AppNavGraph
import com.example.bodydetectionapp.ui.theme.BodyDetectionAppTheme
import androidx.compose.ui.graphics.Color as ComposeColor

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setAppContent()
        } else {
            // Handle permission denial gracefully
        }
    }

    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- FINALIZED EDGE-TO-EDGE LOGIC ---

        // 1. Give the app permission to draw behind system bars.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. Make the system bars transparent.
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // 3. Tell the system to use light icons because our app has a dark background.
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        // --- END OF EDGE-TO-EDGE LOGIC ---

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
                // --- THIS IS THE FINAL PIECE OF THE PUZZLE ---
                // The Surface must be transparent to allow the background to be seen.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ComposeColor.Transparent
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController, userRepository = userRepository)
                }
            }
        }
    }
}