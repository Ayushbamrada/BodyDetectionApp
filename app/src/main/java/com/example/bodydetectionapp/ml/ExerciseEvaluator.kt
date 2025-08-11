package com.example.bodydetectionapp.ml

import com.example.bodydetectionapp.data.models.Exercise
import com.example.bodydetectionapp.data.models.ExerciseDefinitions
import com.example.bodydetectionapp.data.models.ExercisePhase // Import ExercisePhase
import android.util.Log // Added for logging
import com.example.bodydetectionapp.data.models.AngleRange

class ExerciseEvaluator {

    // Current exercise being evaluated
    var currentExercise: Exercise? = null
        private set

    // Current phase index within the active exercise
    var currentPhaseIndex: Int = 0
        private set

    // Repetition counter
    var repCount: Int = 0
        private set

    // State to manage transitions:
    // This could be 'IDLE', 'IN_PHASE', 'TRANSITIONING', 'COMPLETED'
    // For rep counting, we need to know if we've reached the "bottom" of a rep
    private var isRepInProgress: Boolean = false // True if user is between start and end of a rep
    private var isAtRepPeak: Boolean = false     // True if user reached the deepest/highest point of a rep

    // Callback for when a rep is completed (optional, for UI updates)
    var onRepCompleted: ((Int) -> Unit)? = null
    var onFeedbackUpdate: ((List<String>) -> Unit)? = null // New callback for external feedback updates
    var onPhaseChanged: ((ExercisePhase) -> Unit)? = null // New callback for phase changes

    /**
     * Sets the exercise to be evaluated and resets the state.
     */
    fun setExercise(exercise: Exercise?) { // Made nullable to support "free_movement"
        currentExercise = exercise
        currentPhaseIndex = 0
        repCount = 0
        isRepInProgress = false
        isAtRepPeak = false
        // Immediately provide initial feedback for the first phase if exercise is not null
        currentExercise?.phases?.firstOrNull()?.let {
            onPhaseChanged?.invoke(it) // Notify initial phase
            onFeedbackUpdate?.invoke(listOf(it.feedbackMessage ?: "Ready to start."))
        } ?: run {
            onFeedbackUpdate?.invoke(listOf("Free Movement Mode")) // For free movement
        }
    }

    /**
     * Evaluates the current pose against the active exercise's current phase.
     * Manages phase transitions and rep counting.
     *
     * @param currentAngles The map of calculated joint angles for the current frame.
     * @return A list of feedback messages for the current frame.
     */
    fun evaluate(currentAngles: Map<String, Double>): List<String> {
        val feedback = mutableListOf<String>()
        val exercise = currentExercise
        if (exercise == null) {
            // No specific exercise selected (e.g., "free_movement")
            return listOf("Free Movement: No exercise guidance.")
        }

        val currentPhase = exercise.phases.getOrNull(currentPhaseIndex)

        if (currentPhase == null) {
            return listOf("Exercise definition error: No phases found.")
        }

        var allAnglesMetInCurrentPhase = true
        currentPhase.targetAngles.entries.forEach { (angleName, targetRange) -> // Use .entries for clarity
            val actualAngle = currentAngles[angleName]

            if (actualAngle != null && actualAngle.isFinite()) {
                if (actualAngle < targetRange.min) {
                    feedback.add("$angleName: Too low! (Target: ${targetRange.min.toInt()}-${targetRange.max.toInt()}°, Current: %.1f°)".format(actualAngle))
                    allAnglesMetInCurrentPhase = false
                } else if (actualAngle > targetRange.max) {
                    feedback.add("$angleName: Too high! (Target: ${targetRange.min.toInt()}-${targetRange.max.toInt()}°, Current: %.1f°)".format(actualAngle))
                    allAnglesMetInCurrentPhase = false
                }
            } else {
                allAnglesMetInCurrentPhase = false // Missing angle means phase condition not met
            }
        }

        // Add general phase feedback if specific angle issues are not present
        if (allAnglesMetInCurrentPhase && feedback.isEmpty()) {
            currentPhase.feedbackMessage?.let { feedback.add(it) }
        } else if (!allAnglesMetInCurrentPhase && currentPhase.feedbackMessage != null) {
            // If angles are not met, still give general phase feedback if there are specific angle errors too.
            // Or prioritize specific errors by not adding general feedback. Depends on UX.
            // For now, let's keep it simple: if specific errors, show them, otherwise show general phase message.
        }


        // --- Repetition Counting and Phase Transition Logic ---
        when (exercise.name) {
            ExerciseDefinitions.SQUAT.name -> {
                // Phase 0: Starting Position (Top)
                // Phase 1: Lowering Phase
                // Phase 2: Bottom Position
                // Phase 3: Ascending Phase

                // Transition to next phase if current phase's angles are met
                if (allAnglesMetInCurrentPhase) {
                    when (currentPhaseIndex) {
                        0 -> { // From Starting Position
                            if (!isRepInProgress) { // Start a new rep cycle
                                currentPhaseIndex = 1 // Move to Lowering
                                isRepInProgress = true
                                onPhaseChanged?.invoke(exercise.phases[currentPhaseIndex])
                                feedback.add("Start lowering for squat.")
                            }
                        }
                        1 -> { // From Lowering Phase
                            // Transition to Bottom if met
                            if (exercise.phases.getOrNull(2)?.let { phase ->
                                    isAnglesMet(currentAngles, phase.targetAngles)
                                } == true) {
                                currentPhaseIndex = 2
                                onPhaseChanged?.invoke(exercise.phases[currentPhaseIndex])
                                feedback.add("Reached bottom position.")
                                isAtRepPeak = true // Mark that the deepest point was reached
                            }
                        }
                        2 -> { // From Bottom Position
                            // If conditions for Ascending are met, move to Ascending
                            if (isAtRepPeak && exercise.phases.getOrNull(3)?.let { phase ->
                                    isAnglesMet(currentAngles, phase.targetAngles)
                                } == true) {
                                currentPhaseIndex = 3
                                onPhaseChanged?.invoke(exercise.phases[currentPhaseIndex])
                                feedback.add("Begin ascending.")
                            }
                        }
                        3 -> { // From Ascending Phase
                            // If conditions for Starting Position are met, and we were in a rep, count rep
                            if (isRepInProgress && isAtRepPeak && exercise.phases.getOrNull(0)?.let { phase ->
                                    isAnglesMet(currentAngles, phase.targetAngles)
                                } == true) {
                                repCount++
                                isRepInProgress = false
                                isAtRepPeak = false
                                currentPhaseIndex = 0 // Reset to starting position for next rep
                                feedback.add("Rep $repCount completed!")
                                onRepCompleted?.invoke(repCount)
                                onPhaseChanged?.invoke(exercise.phases[currentPhaseIndex])
                                feedback.add("Ready for next squat.")
                            }
                        }
                    }
                }
            }
            ExerciseDefinitions.HAND_RAISING.name -> {
                // Phase 0: Arms Down
                // Phase 1: Arms Up

                if (allAnglesMetInCurrentPhase) {
                    when (currentPhaseIndex) {
                        0 -> { // From Arms Down
                            if (!isRepInProgress) {
                                currentPhaseIndex = 1 // Move to Arms Up
                                isRepInProgress = true
                                onPhaseChanged?.invoke(exercise.phases[currentPhaseIndex])
                                feedback.add("Start raising arms.")
                            }
                        }
                        1 -> { // From Arms Up
                            if (isRepInProgress && exercise.phases.getOrNull(0)?.let { phase ->
                                    isAnglesMet(currentAngles, phase.targetAngles)
                                } == true) {
                                repCount++
                                isRepInProgress = false
                                currentPhaseIndex = 0 // Reset to Arms Down for next rep
                                feedback.add("Rep $repCount completed!")
                                onRepCompleted?.invoke(repCount)
                                onPhaseChanged?.invoke(exercise.phases[currentPhaseIndex])
                                feedback.add("Arms down, ready for next raise.")
                            }
                        }
                    }
                }
            }
            // Add other exercises here
        }

        // Notify listeners of updated feedback (if there are new messages or state changes)
        // Only invoke if feedback is not empty OR if phase has changed to ensure UI updates
        if (feedback.isNotEmpty() || (currentPhase != exercise.phases.getOrNull(currentPhaseIndex))) {
            onFeedbackUpdate?.invoke(feedback)
        }

        return feedback // Also return for immediate use in ViewModel
    }

    // Helper function to check if a set of angles meets a target
    private fun isAnglesMet(currentAngles: Map<String, Double>, targetAngles: Map<String, AngleRange>): Boolean {
        return targetAngles.entries.all { (angleName, targetRange) ->
            val actualAngle = currentAngles[angleName]
            actualAngle != null && actualAngle.isFinite() &&
                    actualAngle >= targetRange.min && actualAngle <= targetRange.max
        }
    }


    /**
     * Returns the current state of the evaluator for display.
     */
    fun getEvaluationSummary(): String {
        val phaseName = currentExercise?.phases?.getOrNull(currentPhaseIndex)?.name ?: "N/A"
        return "Exercise: ${currentExercise?.name ?: "None"}\n" +
                "Phase: $phaseName\n" +
                "Reps: $repCount"
    }

    // Call this if you need to reset the exercise without changing the type
    fun resetCurrentExercise() {
        setExercise(currentExercise) // Re-sets with existing exercise, triggering reset logic
    }
}