//package com.example.bodydetectionapp.ui.home
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.bodydetectionapp.data.models.BodyPart
//import com.example.bodydetectionapp.data.models.Exercise
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HomeScreen(
//    homeViewModel: HomeViewModel,
//    onExerciseClicked: (String) -> Unit // Callback to navigate to the detail screen
//) {
//    val userName by homeViewModel.userName.collectAsState()
//    val exerciseModules by homeViewModel.exerciseModules.collectAsState()
//
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(24.dp)
//    ) {
//        // --- Dynamic Welcome Message ---
//        item {
//            Text(
//                text = "Welcome back, ${userName ?: "User"}!",
//                fontSize = 28.sp,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(bottom = 16.dp)
//            )
//        }
//
//        // --- Exercise Modules ---
//        items(exerciseModules.entries.toList()) { (bodyPart, exercises) ->
//            ExerciseModuleCard(
//                bodyPart = bodyPart,
//                exercises = exercises,
//                onExerciseClicked = onExerciseClicked
//            )
//        }
//    }
//}
//
//@Composable
//fun ExerciseModuleCard(
//    bodyPart: BodyPart,
//    exercises: List<Exercise>,
//    onExerciseClicked: (String) -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(16.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(
//                text = formatBodyPartName(bodyPart),
//                fontSize = 22.sp,
//                fontWeight = FontWeight.SemiBold,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//
//            exercises.forEach { exercise ->
//                Text(
//                    text = exercise.name,
//                    fontSize = 18.sp,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable { onExerciseClicked(exercise.name) }
//                        .padding(vertical = 8.dp)
//                )
//                Divider()
//            }
//        }
//    }
//}
//
//// Helper function to make the enum names look nice on the screen
//private fun formatBodyPartName(bodyPart: BodyPart): String {
//    return bodyPart.name.replace('_', ' ').split(' ')
//        .joinToString(" ") { it.capitalize() }
//}
package com.example.bodydetectionapp.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bodydetectionapp.R
import com.example.bodydetectionapp.data.models.BodyPart
import com.example.bodydetectionapp.ui.components.AppBackground
import com.example.bodydetectionapp.ui.theme.RippleTeal
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onBodyPartClicked: (String) -> Unit
) {
    val userName by homeViewModel.userName.collectAsState()
    val bodyPartCategories by homeViewModel.bodyPartCategories.collectAsState()

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Ripple Healthcare Logo",
                        modifier = Modifier.height(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Welcome,\n${userName ?: "User"}!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 28.sp
                    )
                }
            }
        ) { innerPadding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
                )
            ) {
                itemsIndexed(bodyPartCategories) { index, bodyPart ->
                    BodyPartCard(
                        bodyPart = bodyPart,
                        index = index,
                        onCardClicked = { onBodyPartClicked(bodyPart.name) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyPartCard(
    bodyPart: BodyPart,
    index: Int,
    onCardClicked: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 100L)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)) +
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 400)
                )
    ) {
        Card(
            modifier = Modifier
                .aspectRatio(1f)
                .clickable(onClick = onCardClicked),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = RippleTeal.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = getBodyPartIcon(bodyPart),
                    contentDescription = formatBodyPartName(bodyPart),
                    modifier = Modifier.size(56.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = formatBodyPartName(bodyPart),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

private fun getBodyPartIcon(bodyPart: BodyPart): ImageVector {
    return when (bodyPart) {
        BodyPart.UPPER_BODY -> Icons.Rounded.FitnessCenter
        BodyPart.LOWER_BODY -> Icons.Rounded.DirectionsWalk
        BodyPart.CORE -> Icons.Rounded.SelfImprovement
        BodyPart.FULL_BODY -> Icons.Rounded.AccessibilityNew
        BodyPart.YOGA -> Icons.Rounded.Spa
    }
}

private fun formatBodyPartName(bodyPart: BodyPart): String {
    return bodyPart.name.replace('_', ' ').split(' ')
        .joinToString(" ") { it.capitalize() }
}