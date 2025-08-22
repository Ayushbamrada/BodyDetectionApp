package com.example.bodydetectionapp.ui.onboarding

import androidx.lifecycle.ViewModel
import com.example.bodydetectionapp.data.repository.UserRepository

/**
 * The "brain" for the onboarding process.
 */
class OnboardingViewModel(private val userRepository: UserRepository) : ViewModel() {

    /**
     * Saves the user's name via the repository.
     */
    fun onNameEntered(name: String) {
        if (name.isNotBlank()) {
            userRepository.saveUserName(name)
        }
    }
}
