package com.example.bodydetectionapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.bodydetectionapp.ui.screens.SplashScreen // Import new Splash Screen
import com.example.bodydetectionapp.ui.screens.ExerciseSelectionScreen
import com.example.bodydetectionapp.ui.screens.ExerciseTrackingScreen
import com.example.bodydetectionapp.ui.screens.ExerciseReportScreen // Import new Report Screen

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object ExerciseSelection : Screen("exercise_selection_screen")
    object ExerciseTracking : Screen("exercise_tracking_screen/{exerciseName}") {
        fun createRoute(exerciseName: String) = "exercise_tracking_screen/$exerciseName"
    }
    object ExerciseReport : Screen("exercise_report_screen/{exerciseName}/{finalRepCount}") {
        fun createRoute(exerciseName: String, finalRepCount: Int) = "exercise_report_screen/$exerciseName/$finalRepCount"
    }
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route // Start with the Splash screen
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
        composable(
            route = Screen.ExerciseReport.route,
            arguments = listOf(
                navArgument("exerciseName") { type = NavType.StringType },
                navArgument("finalRepCount") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: "Unknown Exercise"
            val finalRepCount = backStackEntry.arguments?.getInt("finalRepCount") ?: 0
            ExerciseReportScreen(navController = navController, exerciseName = exerciseName, finalRepCount = finalRepCount)
        }
    }
}