//package com.example.bodydetectionapp.data.repository
//
//import android.content.Context
//import android.content.SharedPreferences
//
///**
// * Manages saving and retrieving user data from the phone's local storage.
// * This acts as the single source of truth for all user-related information.
// */
//class UserRepository(context: Context) {
//
//    private val prefs: SharedPreferences = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
//
//    companion object {
//        private const val KEY_USER_NAME = "user_name"
//        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
//        // We can add keys for weight, height, etc. here later
//    }
//
//    /**
//     * Saves the user's name and marks onboarding as complete.
//     */
//    fun saveUserName(name: String) {
//        prefs.edit()
//            .putString(KEY_USER_NAME, name)
//            .putBoolean(KEY_ONBOARDING_COMPLETE, true)
//            .apply()
//    }
//
//    /**
//     * Retrieves the user's saved name.
//     * Returns null if no name has been saved.
//     */
//    fun getUserName(): String? {
//        return prefs.getString(KEY_USER_NAME, null)
//    }
//
//    /**
//     * Checks if the user has completed the initial onboarding screens.
//     */
//    fun hasCompletedOnboarding(): Boolean {
//        return prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
//    }
//}
package com.example.bodydetectionapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        private const val KEY_GENDER = "user_gender"
        private const val KEY_AGE = "user_age"
        private const val KEY_WEIGHT_KG = "user_weight_kg"
        private const val KEY_HEIGHT_CM = "user_height_cm"
    }

    suspend fun saveUserName(name: String) = withContext(Dispatchers.IO) {
        prefs.edit()
            .putString(KEY_USER_NAME, name)
            .putBoolean(KEY_ONBOARDING_COMPLETE, true)
            .apply()
    }

    suspend fun getUserName(): String? = withContext(Dispatchers.IO) {
        prefs.getString(KEY_USER_NAME, null)
    }

    suspend fun hasCompletedOnboarding(): Boolean = withContext(Dispatchers.IO) {
        prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
    }

    // --- NEW FUNCTION to save all the new details ---
    suspend fun saveUserVitals(gender: String, age: Int, weightInKg: Int, heightInCm: Int) = withContext(Dispatchers.IO) {
        prefs.edit()
            .putString(KEY_GENDER, gender)
            .putInt(KEY_AGE, age)
            .putInt(KEY_WEIGHT_KG, weightInKg)
            .putInt(KEY_HEIGHT_CM, heightInCm)
            .apply()
    }
}

