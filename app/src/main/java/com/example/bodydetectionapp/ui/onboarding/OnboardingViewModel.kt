package com.example.bodydetectionapp.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bodydetectionapp.data.repository.UserRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(private val userRepository: UserRepository) : ViewModel() {

    /**
     * Saves the user's name. This should be called when the user proceeds from the name page.
     */
    fun onNameEntered(name: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                userRepository.saveUserName(name)
            }
        }
    }

    /**
     * Saves all the collected user details at the end of the onboarding process.
     */
    fun saveUserDetails(
        name: String,
        gender: String,
        age: Int,
        weightInKg: Int,
        heightInCm: Int
    ) {
        viewModelScope.launch {
            // First, save the name and mark onboarding as complete
            userRepository.saveUserName(name)
            // Then, save the other details
            userRepository.saveUserVitals(gender, age, weightInKg, heightInCm)
        }
    }
}
