package com.example.bodydetectionapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.bodydetectionapp.ui.screens.ExerciseReportScreen
import com.example.bodydetectionapp.ui.screens.ExerciseSelectionScreen
import com.example.bodydetectionapp.ui.screens.ExerciseTrackingScreen
import com.example.bodydetectionapp.ui.screens.SplashScreen
import java.net.URLEncoder // Make sure this import is present

// Assuming your Screen sealed class is defined directly within this file
sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object ExerciseSelection : Screen("exercise_selection_screen")
    object ExerciseTracking : Screen("exercise_tracking_screen/{exerciseName}") {
        fun createRoute(exerciseName: String) = "exercise_tracking_screen/$exerciseName"
    }

    // **** MODIFIED: The route for ExerciseReport now includes exerciseSessionStartTime ****
    object ExerciseReport : Screen("exercise_report_screen/{exerciseName}/{finalRepCount}/{repTimestampsJson}/{exerciseSessionStartTime}") {
        fun createRoute(
            exerciseName: String,
            finalRepCount: Int,
            repTimestampsJson: String,
            exerciseSessionStartTime: Long // NEW ARGUMENT
        ): String {
            // We encode the JSON string so it can be safely passed as a URL parameter
            val encodedTimestamps = URLEncoder.encode(repTimestampsJson, "UTF-8")
            return "exercise_report_screen/$exerciseName/$finalRepCount/$encodedTimestamps/$exerciseSessionStartTime"
        }
    }
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.ExerciseSelection.route) {
            ExerciseSelectionScreen(navController = navController)
        }
        composable(
            route = Screen.ExerciseTracking.route,
            arguments = listOf(navArgument("exerciseName") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: "free_movement"
            ExerciseTrackingScreen(navController = navController, exerciseName = exerciseName)
        }

        // **** MODIFIED: The composable for the report screen is updated to retrieve exerciseSessionStartTime ****
        composable(
            route = Screen.ExerciseReport.route,
            arguments = listOf(
                navArgument("exerciseName") { type = NavType.StringType },
                navArgument("finalRepCount") { type = NavType.IntType },
                navArgument("repTimestampsJson") { type = NavType.StringType }, // Argument name changed from 'timestamps' to 'repTimestampsJson'
                navArgument("exerciseSessionStartTime") { type = NavType.LongType } // NEW: Argument for start time
            )
        ) { backStackEntry ->
            val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: "Unknown Exercise"
            val finalRepCount = backStackEntry.arguments?.getInt("finalRepCount") ?: 0
            val repTimestampsJson = backStackEntry.arguments?.getString("repTimestampsJson") // Retrieve by new name
            val exerciseSessionStartTime = backStackEntry.arguments?.getLong("exerciseSessionStartTime") ?: 0L // Retrieve new argument

            ExerciseReportScreen(
                navController = navController,
                exerciseName = exerciseName,
                finalRepCount = finalRepCount,
                repTimestampsJson = repTimestampsJson,
                exerciseSessionStartTime = exerciseSessionStartTime // Pass the new argument to the screen
            )
        }
    }
}