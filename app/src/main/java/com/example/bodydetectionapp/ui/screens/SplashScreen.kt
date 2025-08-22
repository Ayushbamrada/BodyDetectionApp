package com.example.bodydetectionapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bodydetectionapp.R
import com.example.bodydetectionapp.data.repository.UserRepository
import com.example.bodydetectionapp.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, userRepository: UserRepository) {
    val logoScale = remember { Animatable(0f) }
    val appTitle = "Your AI Exercise App"
    var visibleAppTitle by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        appTitle.forEach { char ->
            visibleAppTitle += char
            delay(50)
        }
        delay(300)
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
        delay(1000)

        // --- FIX: Logic now correctly checks onboarding status ---
        val destination = if (userRepository.hasCompletedOnboarding()) {
            Screen.Home.route
        } else {
            Screen.Onboarding.route
        }
        navController.navigate(destination) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = visibleAppTitle,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 40.dp)
                    .height(70.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(logoScale.value)
            ) {
                Text(
                    text = "Powered by",
                    color = Color.Gray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.ripple_healthcare_logo),
                    contentDescription = "Company Logo",
                    modifier = Modifier.size(260.dp)
                )
            }
        }
    }
}
