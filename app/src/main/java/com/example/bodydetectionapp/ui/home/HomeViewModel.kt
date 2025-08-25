//package com.example.bodydetectionapp.ui.home
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.bodydetectionapp.data.models.BodyPart
//import com.example.bodydetectionapp.data.models.Exercise
//import com.example.bodydetectionapp.data.models.ExerciseDefinitions
//import com.example.bodydetectionapp.data.repository.UserRepository
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//
///**
// * The "brain" for the Home Screen. It prepares the data that the UI needs to display.
// */
//class HomeViewModel(private val userRepository: UserRepository) : ViewModel() {
//
//    private val _userName = MutableStateFlow<String?>(null)
//    val userName: StateFlow<String?> = _userName.asStateFlow()
//
//    private val _exerciseModules = MutableStateFlow<Map<BodyPart, List<Exercise>>>(emptyMap())
//    val exerciseModules: StateFlow<Map<BodyPart, List<Exercise>>> = _exerciseModules.asStateFlow()
//
//    init {
//        loadData()
//    }
//
//    private fun loadData() {
//        viewModelScope.launch {
//            // 1. Get the user's name from our "librarian"
//            _userName.value = userRepository.getUserName()
//
//            // 2. Get all exercises from our "cookbook" and group them by body part
//            _exerciseModules.value = ExerciseDefinitions.ALL_EXERCISES.groupBy { it.bodyPart }
//        }
//    }
//}
package com.example.bodydetectionapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bodydetectionapp.data.models.BodyPart
import com.example.bodydetectionapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    // --- RENAMED for clarity ---
    private val _bodyPartCategories = MutableStateFlow<List<BodyPart>>(emptyList())
    val bodyPartCategories: StateFlow<List<BodyPart>> = _bodyPartCategories.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _userName.value = userRepository.getUserName()
            // --- MODIFIED to just get the distinct list of body parts ---
            _bodyPartCategories.value = BodyPart.values().toList()
        }
    }
}