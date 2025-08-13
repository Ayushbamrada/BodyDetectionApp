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

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object ExerciseSelection : Screen("exercise_selection_screen")
    object ExerciseTracking : Screen("exercise_tracking_screen/{exerciseName}") {
        fun createRoute(exerciseName: String) = "exercise_tracking_screen/$exerciseName"
    }

    // --- MODIFIED: The route for the report screen is updated to accept timestamps ---
    object ExerciseReport : Screen("exercise_report_screen/{exerciseName}/{finalRepCount}?timestamps={timestamps}") {
        // --- MODIFIED: The createRoute function now accepts the timestamp data ---
        fun createRoute(exerciseName: String, finalRepCount: Int, repTimestampsJson: String): String {
            // We encode the JSON string so it can be safely passed as a URL parameter
            val encodedTimestamps = java.net.URLEncoder.encode(repTimestampsJson, "UTF-8")
            return "exercise_report_screen/$exerciseName/$finalRepCount?timestamps=$encodedTimestamps"
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

        // --- MODIFIED: The composable for the report screen is updated ---
        composable(
            route = Screen.ExerciseReport.route,
            arguments = listOf(
                navArgument("exerciseName") { type = NavType.StringType },
                navArgument("finalRepCount") { type = NavType.IntType },
                // We tell the navigator to expect the new 'timestamps' argument
                navArgument("timestamps") {
                    type = NavType.StringType
                    nullable = true // Make it optional in case something goes wrong
                }
            )
        ) { backStackEntry ->
            val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: "Unknown Exercise"
            val finalRepCount = backStackEntry.arguments?.getInt("finalRepCount") ?: 0
            // We get the new 'timestamps' argument from the backStackEntry
            val repTimestampsJson = backStackEntry.arguments?.getString("timestamps")

            ExerciseReportScreen(
                navController = navController,
                exerciseName = exerciseName,
                finalRepCount = finalRepCount,
                repTimestampsJson = repTimestampsJson // Pass it to the screen
            )
        }
    }
}
