package com.example.bodydetectionapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bodydetectionapp.ui.screens.freepose.FreePoseScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "freepose") {
        composable("freepose") { FreePoseScreen() }
    }
}
