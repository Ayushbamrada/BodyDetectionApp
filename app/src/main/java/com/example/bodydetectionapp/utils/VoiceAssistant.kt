package com.example.bodydetectionapp.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

/**
 * A helper class to manage Text-to-Speech functionality for the app.
 * It handles initialization, speaking, and shutting down the TTS engine.
 */
class VoiceAssistant(context: Context) : TextToSpeech.OnInitListener {

    private val tts: TextToSpeech = TextToSpeech(context, this)

    /**
     * Called when the TTS engine is ready.
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set the language to US English.
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("VoiceAssistant", "The Language specified is not supported!")
            }
        } else {
            Log.e("VoiceAssistant", "Initialization Failed!")
        }
    }

    /**
     * Speaks the given text out loud.
     * @param text The text to be spoken.
     */
    fun speak(text: String) {
        // Use QUEUE_ADD to ensure messages are spoken one after another, not interrupting each other.
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, "")
    }

    /**
     * Shuts down the TTS engine to release resources.
     * This should be called when the ViewModel is cleared.
     */
    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
