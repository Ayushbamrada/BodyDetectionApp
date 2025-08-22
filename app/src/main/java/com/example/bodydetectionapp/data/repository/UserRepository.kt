package com.example.bodydetectionapp.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages saving and retrieving user data from the phone's local storage.
 * This acts as the single source of truth for all user-related information.
 */
class UserRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        // We can add keys for weight, height, etc. here later
    }

    /**
     * Saves the user's name and marks onboarding as complete.
     */
    fun saveUserName(name: String) {
        prefs.edit()
            .putString(KEY_USER_NAME, name)
            .putBoolean(KEY_ONBOARDING_COMPLETE, true)
            .apply()
    }

    /**
     * Retrieves the user's saved name.
     * Returns null if no name has been saved.
     */
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    /**
     * Checks if the user has completed the initial onboarding screens.
     */
    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
    }
}
