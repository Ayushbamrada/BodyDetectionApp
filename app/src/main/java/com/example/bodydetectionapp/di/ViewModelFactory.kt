package com.example.bodydetectionapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bodydetectionapp.data.repository.UserRepository
import com.example.bodydetectionapp.ui.home.HomeViewModel
import com.example.bodydetectionapp.ui.onboarding.OnboardingViewModel

/**
 * A factory that knows how to create our ViewModels that require a UserRepository.
 * This is the "key maker" that gives our ViewModels the "keys" (dependencies) they need.
 */
class ViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> {
                OnboardingViewModel(userRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
