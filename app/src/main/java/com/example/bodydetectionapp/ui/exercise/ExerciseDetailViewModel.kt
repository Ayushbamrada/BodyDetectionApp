package com.example.bodydetectionapp.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bodydetectionapp.data.models.Exercise
import com.example.bodydetectionapp.data.models.ExerciseDefinitions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The "brain" for the Exercise Detail Screen. Its job is to load the
 * correct exercise information based on the user's selection.
 */
class ExerciseDetailViewModel : ViewModel() {

    private val _exercise = MutableStateFlow<Exercise?>(null)
    val exercise: StateFlow<Exercise?> = _exercise.asStateFlow()

    fun loadExercise(exerciseName: String) {
        viewModelScope.launch {
            // Find the exercise "recipe" in our "cookbook" that matches the given name.
            _exercise.value = ExerciseDefinitions.ALL_EXERCISES.find { it.name == exerciseName }
        }
    }
}
