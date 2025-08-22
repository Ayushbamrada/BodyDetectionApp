package com.example.bodydetectionapp.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.bodydetectionapp.data.repository.UserRepository
import com.example.bodydetectionapp.di.ViewModelFactory
import com.example.bodydetectionapp.ui.exercise.ExerciseDetailScreen
import com.example.bodydetectionapp.ui.exercise.ExerciseDetailViewModel
import com.example.bodydetectionapp.ui.exercise.ExerciseTrackingScreen
import com.example.bodydetectionapp.ui.home.HomeScreen
import com.example.bodydetectionapp.ui.home.HomeViewModel
import com.example.bodydetectionapp.ui.onboarding.OnboardingScreen
import com.example.bodydetectionapp.ui.onboarding.OnboardingViewModel
import com.example.bodydetectionapp.ui.report.ExerciseReportScreen
import com.example.bodydetectionapp.ui.screens.SplashScreen
import java.net.URLEncoder

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Onboarding : Screen("onboarding_screen")
    object Home : Screen("home_screen")
    object ExerciseDetail : Screen("exercise_detail_screen/{exerciseName}") {
        fun createRoute(exerciseName: String) = "exercise_detail_screen/$exerciseName"
    }
    object ExerciseTracking : Screen("exercise_tracking_screen/{exerciseName}/{repGoal}/{timeGoal}") {
        fun createRoute(exerciseName: String, repGoal: Int, timeGoal: Int) =
            "exercise_tracking_screen/$exerciseName/$repGoal/$timeGoal"
    }
    object ExerciseReport : Screen("exercise_report_screen/{exerciseName}/{finalRepCount}/{repTimestampsJson}/{exerciseSessionStartTime}") {
        fun createRoute(
            exerciseName: String,
            finalRepCount: Int,
            repTimestampsJson: String,
            exerciseSessionStartTime: Long
        ): String {
            val encodedTimestamps = URLEncoder.encode(repTimestampsJson, "UTF-8")
            return "exercise_report_screen/$exerciseName/$finalRepCount/$encodedTimestamps/$exerciseSessionStartTime"
        }
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    userRepository: UserRepository
) {
    val factory = ViewModelFactory(userRepository)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController, userRepository = userRepository)
        }

        composable(Screen.Onboarding.route) {
            // --- FIX: The NavGraph creates the ViewModel using the factory ---
            val onboardingViewModel: OnboardingViewModel = viewModel(factory = factory)
            // --- FIX: The ViewModel is then passed to the screen ---
            OnboardingScreen(
                onboardingViewModel = onboardingViewModel,
                onContinueClicked = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(factory = factory)
            HomeScreen(
                homeViewModel = homeViewModel,
                onExerciseClicked = { exerciseName ->
                    navController.navigate(Screen.ExerciseDetail.createRoute(exerciseName))
                }
            )
        }

        composable(
            route = Screen.ExerciseDetail.route,
            arguments = listOf(navArgument("exerciseName") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: ""
            val exerciseDetailViewModel: ExerciseDetailViewModel = viewModel()
            ExerciseDetailScreen(
                exerciseName = exerciseName,
                exerciseDetailViewModel = exerciseDetailViewModel,
                onStartClicked = { name, reps, time ->
                    navController.navigate(Screen.ExerciseTracking.createRoute(name, reps, time))
                }
            )
        }

        composable(
            route = Screen.ExerciseTracking.route,
            arguments = listOf(
                navArgument("exerciseName") { type = NavType.StringType },
                navArgument("repGoal") { type = NavType.IntType },
                navArgument("timeGoal") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: "Unknown"
            val repGoal = backStackEntry.arguments?.getInt("repGoal") ?: 0
            val timeGoal = backStackEntry.arguments?.getInt("timeGoal") ?: 0

            ExerciseTrackingScreen(
                navController = navController,
                exerciseName = exerciseName,
                repGoal = repGoal,
                timeGoal = timeGoal
            )
        }

        composable(
            route = Screen.ExerciseReport.route,
            arguments = listOf(
                navArgument("exerciseName") { type = NavType.StringType },
                navArgument("finalRepCount") { type = NavType.IntType },
                navArgument("repTimestampsJson") { type = NavType.StringType },
                navArgument("exerciseSessionStartTime") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: "Unknown Exercise"
            val finalRepCount = backStackEntry.arguments?.getInt("finalRepCount") ?: 0
            val repTimestampsJson = backStackEntry.arguments?.getString("repTimestampsJson")
            val exerciseSessionStartTime = backStackEntry.arguments?.getLong("exerciseSessionStartTime") ?: 0L

            ExerciseReportScreen(
                navController = navController,
                exerciseName = exerciseName,
                finalRepCount = finalRepCount,
                repTimestampsJson = repTimestampsJson,
                exerciseSessionStartTime = exerciseSessionStartTime
            )
        }
    }
}
