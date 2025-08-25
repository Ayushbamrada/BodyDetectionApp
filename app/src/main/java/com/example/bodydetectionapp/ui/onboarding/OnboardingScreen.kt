package com.example.bodydetectionapp.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bodydetectionapp.ui.components.AppBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onContinueClicked: () -> Unit,
    onboardingViewModel: OnboardingViewModel
) {
    var name by remember { mutableStateOf("") }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                // Add padding to respect system bars and keyboard
                .safeDrawingPadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Let's get to know you.",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                color = Color.White.copy(alpha = 0.8f)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("What should we call you?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.8f)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onboardingViewModel.onNameEntered(name)
                    onContinueClicked()
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Continue", fontSize = 18.sp)
            }
        }
    }
}