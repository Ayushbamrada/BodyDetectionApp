//package com.example.bodydetectionapp.ui.onboarding
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.bodydetectionapp.ui.components.AppBackground
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun OnboardingScreen(
//    onContinueClicked: () -> Unit,
//    onboardingViewModel: OnboardingViewModel
//) {
//    var name by remember { mutableStateOf("") }
//
//    AppBackground {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(32.dp)
//                // Add padding to respect system bars and keyboard
//                .safeDrawingPadding(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = "Welcome!",
//                fontSize = 32.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.White
//            )
//            Text(
//                text = "Let's get to know you.",
//                fontSize = 18.sp,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
//                color = Color.White.copy(alpha = 0.8f)
//            )
//
//            OutlinedTextField(
//                value = name,
//                onValueChange = { name = it },
//                label = { Text("What should we call you?") },
//                singleLine = true,
//                modifier = Modifier.fillMaxWidth(),
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    focusedTextColor = Color.White,
//                    cursorColor = Color.White,
//                    focusedBorderColor = Color.White,
//                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
//                    focusedLabelColor = Color.White,
//                    unfocusedLabelColor = Color.White.copy(alpha = 0.8f)
//                )
//            )
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            Button(
//                onClick = {
//                    onboardingViewModel.onNameEntered(name)
//                    onContinueClicked()
//                },
//                enabled = name.isNotBlank(),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(50.dp)
//            ) {
//                Text("Continue", fontSize = 18.sp)
//            }
//        }
//    }
//}
package com.example.bodydetectionapp.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bodydetectionapp.ui.components.AppBackground
import com.example.bodydetectionapp.ui.theme.RippleTeal
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onContinueClicked: () -> Unit,
    onboardingViewModel: OnboardingViewModel
) {
    var currentPage by remember { mutableStateOf(0) }
    val totalPages = 4

    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var weightInKg by remember { mutableStateOf(70) }
    var heightInCm by remember { mutableStateOf(170) }

    val isNextEnabled = when (currentPage) {
        0 -> name.isNotBlank()
        1 -> gender.isNotBlank()
        2 -> age.isNotBlank() && age.toIntOrNull() != null
        else -> true
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProgressIndicator(currentPage = currentPage + 1, totalPages = totalPages)

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                                slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                    }, label = "OnboardingPageAnimation"
                ) { page ->
                    when (page) {
                        0 -> NamePage(name = name, onNameChange = { name = it })
                        1 -> GenderPage(selectedGender = gender, onGenderSelect = { gender = it })
                        2 -> AgePage(age = age, onAgeChange = { age = it })
                        3 -> VitalsPage(
                            weightInKg = weightInKg,
                            onWeightChange = { weightInKg = it },
                            heightInCm = heightInCm,
                            onHeightChange = { heightInCm = it }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (currentPage < totalPages - 1) {
                        currentPage++
                    } else {
                        onboardingViewModel.saveUserDetails(name, gender, age.toInt(), weightInKg, heightInCm)
                        onContinueClicked()
                    }
                },
                enabled = isNextEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(if (currentPage < totalPages - 1) "Continue" else "Finish", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun ProgressIndicator(currentPage: Int, totalPages: Int) {
    Text(
        text = "Step $currentPage of $totalPages",
        color = Color.White.copy(alpha = 0.7f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun PageTitle(title: String) {
    Text(
        text = title,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 32.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamePage(name: String, onNameChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PageTitle(title = "What should we call you?")
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.8f)
            )
        )
    }
}

@Composable
fun GenderPage(selectedGender: String, onGenderSelect: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PageTitle(title = "Select Your Gender")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GenderChip("Male", selectedGender == "Male") { onGenderSelect("Male") }
            GenderChip("Female", selectedGender == "Female") { onGenderSelect("Female") }
            GenderChip("Other", selectedGender == "Other") { onGenderSelect("Other") }
        }
    }
}

@Composable
fun RowScope.GenderChip(gender: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(60.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) RippleTeal else Color.White.copy(alpha = 0.1f),
        contentColor = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = gender, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgePage(age: String, onAgeChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PageTitle(title = "How old are you?")
        OutlinedTextField(
            value = age,
            onValueChange = { if (it.length <= 3) onAgeChange(it.filter { char -> char.isDigit() }) },
            label = { Text("Age") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.8f)
            )
        )
    }
}

// --- NEW, CORRECTED VITALS PAGE LAYOUT ---
@Composable
fun VitalsPage(
    weightInKg: Int, onWeightChange: (Int) -> Unit,
    heightInCm: Int, onHeightChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        PageTitle(title = "Your Vitals")

        // Height ruler takes up the flexible space in the middle
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            HeightRuler(heightInCm = heightInCm, onHeightChange = onHeightChange)
        }

        // Weight ruler is fixed at the bottom of the available space
        WeightRuler(weightInKg = weightInKg, onWeightChange = onWeightChange)
    }
}

// --- NEW, CORRECTED HEIGHT RULER ---
@Composable
fun HeightRuler(heightInCm: Int, onHeightChange: (Int) -> Unit) {
    val feet = (heightInCm * 0.0328084).toInt()
    val inches = ((heightInCm * 0.0328084 - feet) * 12).roundToInt()

    var currentHeight by remember(heightInCm) { mutableStateOf(heightInCm.toFloat()) }

    Row(
        modifier = Modifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Height", color = Color.White.copy(alpha = 0.8f))
            Text(
                text = "$feet ft $inches in",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight(0.9f)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        currentHeight -= dragAmount.y * 0.1f // Adjust sensitivity
                        onHeightChange(currentHeight.roundToInt().coerceIn(50, 220)) // Min 4ft (122cm), Max ~7ft2 (220cm)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val lineSpacing = 20.dp.toPx()
                val totalLines = (size.height / lineSpacing).toInt()

                for (i in -totalLines / 2..totalLines / 2) {
                    val cmOffset = (currentHeight - heightInCm).toInt()
                    val currentCm = heightInCm + i - cmOffset
                    val yPos = centerY + i * lineSpacing

                    val isFoot = (currentCm * 0.0328084 * 12).roundToInt() % 12 == 0
                    val isSixInch = (currentCm * 0.0328084 * 12).roundToInt() % 6 == 0

                    val lineWidth = when {
                        isFoot -> 60.dp.toPx()
                        isSixInch -> 40.dp.toPx()
                        else -> 20.dp.toPx()
                    }
                    val color = if (isFoot) Color.White else Color.White.copy(alpha = 0.5f)

                    drawLine(
                        color = color,
                        start = Offset(centerX - lineWidth / 2, yPos),
                        end = Offset(centerX + lineWidth / 2, yPos),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(4.dp)
                    .background(RippleTeal, CircleShape)
            )
        }
    }
}

// --- NEW, CORRECTED WEIGHT RULER ---
@Composable
fun WeightRuler(weightInKg: Int, onWeightChange: (Int) -> Unit) {
    var currentWeight by remember(weightInKg) { mutableStateOf(weightInKg.toFloat()) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Weight", color = Color.White.copy(alpha = 0.8f))
        Text(
            text = "$weightInKg kg",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        currentWeight -= dragAmount.x * 0.1f // Adjust sensitivity
                        onWeightChange(currentWeight.roundToInt().coerceIn(20, 200)) // 30kg to 200kg range
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val lineSpacing = 12.dp.toPx()
                val totalLines = (size.width / lineSpacing).toInt()

                for (i in -totalLines / 2..totalLines / 2) {
                    val kgOffset = (currentWeight - weightInKg).toInt()
                    val currentKg = weightInKg + i - kgOffset
                    val xPos = centerX + i * lineSpacing

                    val isTenKg = currentKg % 10 == 0
                    val isFiveKg = currentKg % 5 == 0

                    val lineHeight = when {
                        isTenKg -> 60.dp.toPx()
                        isFiveKg -> 40.dp.toPx()
                        else -> 20.dp.toPx()
                    }
                    val color = if (isTenKg) Color.White else Color.White.copy(alpha = 0.5f)

                    drawLine(
                        color = color,
                        start = Offset(xPos, centerY - lineHeight / 2),
                        end = Offset(xPos, centerY + lineHeight / 2),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)
                    .background(RippleTeal, CircleShape)
            )
        }
    }
}

