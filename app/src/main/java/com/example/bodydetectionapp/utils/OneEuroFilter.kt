package com.example.bodydetectionapp.utils

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max

/**
 * A Kotlin implementation of the 1€ filter.
 * This filter is highly effective at smoothing noisy, real-time signals like pose landmarks.
 *
 * @param freq The frequency of the input signal (e.g., frames per second).
 * @param minCutoff The minimum cutoff frequency. Lower values mean more smoothing.
 * @param beta The beta parameter for adjusting the cutoff frequency based on speed.
 * @param dCutoff The cutoff frequency for the derivative.
 */
class OneEuroFilter(
    private var freq: Double,
    private var minCutoff: Double = 1.0,
    private var beta: Double = 0.0,
    private var dCutoff: Double = 1.0
) {
    private var x: LowPassFilter
    private var dx: LowPassFilter
    private var lastTime: Double = -1.0
    private var firstTime: Boolean = true

    init {
        x = LowPassFilter(alpha(minCutoff))
        dx = LowPassFilter(alpha(dCutoff))
    }

    private fun alpha(cutoff: Double): Double {
        val te = 1.0 / freq
        val tau = 1.0 / (2 * Math.PI * cutoff)
        return 1.0 / (1.0 + tau / te)
    }

    fun apply(value: Double, timestamp: Long): Double {
        val currentTime = timestamp.toDouble() / 1000.0 // Convert ms to seconds

        if (firstTime) {
            firstTime = false
            lastTime = currentTime
        }

        val dt = if (lastTime != -1.0) currentTime - lastTime else 0.0
        if (dt > 0.0) {
            freq = 1.0 / dt
        }
        lastTime = currentTime

        val dValue = if (x.hasLastRawValue()) (value - x.lastRawValue()) / max(dt, 1e-6) else 0.0
        val edValue = dx.apply(dValue)

        val cutoff = minCutoff + beta * abs(edValue)
        // We can now set the alpha property directly
        x.alpha = alpha(cutoff)

        return x.apply(value)
    }

    private class LowPassFilter(var alpha: Double) {
        private var y: Double = 0.0
        private var s: Double = 0.0
        private var firstTime = true

        fun apply(value: Double): Double {
            y = if (firstTime) {
                s = value
                firstTime = false
                s
            } else {
                s = alpha * value + (1.0 - alpha) * s
                s
            }
            return y
        }

        fun lastRawValue(): Double = y

        fun hasLastRawValue(): Boolean = !firstTime

        // --- FIX: The redundant setAlpha function is removed ---
        // fun setAlpha(alpha: Double) {
        //     this.alpha = alpha
        // }
    }
}