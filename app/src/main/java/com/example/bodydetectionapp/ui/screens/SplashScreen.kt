//
//package com.example.bodydetectionapp.ui.screens
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.*
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.slideInVertically
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.example.bodydetectionapp.R
//import com.example.bodydetectionapp.data.repository.UserRepository
//import com.example.bodydetectionapp.navigation.Screen
//import com.example.bodydetectionapp.ui.components.AppBackground
//import kotlinx.coroutines.delay
//
//@Composable
//fun SplashScreen(navController: NavController, userRepository: UserRepository) {
//    var topTextVisible by remember { mutableStateOf(false) }
//    var bottomContentVisible by remember { mutableStateOf(false) }
//
//    LaunchedEffect(Unit) {
//        delay(500)
//        topTextVisible = true
//        delay(500)
//        bottomContentVisible = true
//        delay(3000)
//
//        // Using your original, stable navigation logic
//        val destination = if (userRepository.hasCompletedOnboarding()) {
//            Screen.Home.route
//        } else {
//            Screen.Onboarding.route
//        }
//        navController.navigate(destination) {
//            popUpTo(Screen.Splash.route) { inclusive = true }
//        }
//    }
//
//    // Use the single, consistent AppBackground for the entire screen
//    AppBackground {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 24.dp, vertical = 60.dp),
//            verticalArrangement = Arrangement.SpaceBetween
//        ) {
//            AnimatedVisibility(
//                visible = topTextVisible,
//                enter = fadeIn(animationSpec = tween(1000)) +
//                        slideInVertically(
//                            initialOffsetY = { -it / 2 },
//                            animationSpec = tween(1000, easing = EaseOutCubic)
//                        )
//            ) {
//                Text(
//                    text = "Your AI Exercise App",
//                    color = Color.White,
//                    fontSize = 28.sp,
//                    fontWeight = FontWeight.Bold,
//                    textAlign = TextAlign.Center,
//                )
//            }
//
//            AnimatedVisibility(
//                visible = bottomContentVisible,
//                enter = fadeIn(animationSpec = tween(1000)) +
//                        slideInVertically(
//                            initialOffsetY = { it / 2 },
//                            animationSpec = tween(1000, easing = EaseOutCubic)
//                        )
//            ) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text(
//                        text = "Powered by",
//                        color = Color.White.copy(alpha = 0.8f),
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Medium,
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier.padding(bottom = 8.dp)
//                    )
//                    Image(
//                        painter = painterResource(id = R.drawable.logo),
//                        contentDescription = "Company Logo",
//                        modifier = Modifier.fillMaxWidth(0.7f)
//                    )
//                }
//            }
//        }
//    }
//}
package com.example.bodydetectionapp.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.bodydetectionapp.R
import com.example.bodydetectionapp.data.repository.UserRepository
import com.example.bodydetectionapp.navigation.Screen
import com.example.bodydetectionapp.ui.components.AppBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

// --- NEW: A dedicated composable to play the GIF ---
@Composable
fun GifPlayer(gifId: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    Image(
        painter = rememberAsyncImagePainter(gifId, imageLoader),
        contentDescription = "Exercise Animation",
        modifier = modifier
    )
}


@Composable
fun SplashScreen(navController: NavController, userRepository: UserRepository) {
    var topTextVisible by remember { mutableStateOf(false) }
    var bottomContentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        topTextVisible = true
        delay(500)
        bottomContentVisible = true
        delay(4000) // Increased delay to let the animation play

        val destination = if (userRepository.hasCompletedOnboarding()) {
            Screen.Home.route
        } else {
            Screen.Onboarding.route
        }
        if (coroutineContext.isActive) {
            navController.navigate(destination) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    AppBackground {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            // Top Text animates in
            AnimatedVisibility(
                visible = topTextVisible,
                enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { -it })
            ) {
                Text(
                    text = "Your AI Exercise App",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 60.dp)
                )
            }


            // The GIF player takes up the middle space
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                GifPlayer(
                    gifId = R.raw.splashnew_gif,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .clip(CircleShape) // Optional: makes the GIF circular
                )
            }

            // Bottom Logo animates in
            AnimatedVisibility(
                visible = bottomContentVisible,
                enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { it })
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 60.dp)
                ) {
                    Text(
                        text = "Powered by",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Company Logo",
                        modifier = Modifier.fillMaxWidth(0.6f)
                    )
                }
            }
        }
    }
}