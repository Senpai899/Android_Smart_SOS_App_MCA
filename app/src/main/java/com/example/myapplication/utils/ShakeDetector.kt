package com.example.myapplication.utils

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(private val onShakeListener: () -> Unit) : SensorEventListener {

    private var firstShakeTime: Long = 0L
    private var lastTriggerTime: Long = 0L
    private var shakeCount: Int = 0

    companion object {
        private const val SHAKE_THRESHOLD_GRAVITY = 2.6f

        // Minimum pause between Shake 1 and Shake 2 (prevents a single long shake from triggering both)
        private const val MIN_INTERVAL_BETWEEN_SHAKES_MS = 350L

        // Maximum time window to perform the second shake (must be within 2.5 seconds)
        private const val MAX_WINDOW_FOR_DOUBLE_SHAKE_MS = 2500L

        // Cooldown after an alert is dispatched before a new 2-shake sequence can be initiated
        private const val POST_TRIGGER_COOLDOWN_MS = 3000L
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        // Total gForce (~1.0 when device is stationary)
        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()
        val now = System.currentTimeMillis()

        // Expire first shake if user waited too long (prevents combining accidental bumps minutes apart)
        if (shakeCount == 1 && now - firstShakeTime > MAX_WINDOW_FOR_DOUBLE_SHAKE_MS) {
            shakeCount = 0
            firstShakeTime = 0L
        }

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            // Ignore if we recently triggered an alert cooldown
            if (now - lastTriggerTime < POST_TRIGGER_COOLDOWN_MS) {
                return
            }

            if (shakeCount == 0) {
                // First distinct shake registered!
                shakeCount = 1
                firstShakeTime = now
            } else if (shakeCount == 1) {
                val timeSinceFirst = now - firstShakeTime

                // If within the minimum interval (< 350ms), it's part of the same continuous single shake -> ignore
                if (timeSinceFirst < MIN_INTERVAL_BETWEEN_SHAKES_MS) {
                    return
                }

                // If within the valid double-shake window (350ms to 2500ms): 2nd shake confirmed!
                if (timeSinceFirst <= MAX_WINDOW_FOR_DOUBLE_SHAKE_MS) {
                    shakeCount = 0
                    firstShakeTime = 0L
                    lastTriggerTime = now
                    onShakeListener.invoke()
                } else {
                    // Beyond the window, treat this as Shake 1 of a new attempt
                    shakeCount = 1
                    firstShakeTime = now
                }
            }
        }
    }
}
