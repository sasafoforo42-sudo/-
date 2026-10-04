package com.example.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class MotionDetector(
    context: Context,
    private val onMotionTriggered: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var isListening = false
    private var lastAcceleration = 0f
    private var currentAcceleration = SensorManager.GRAVITY_EARTH
    private var isArmed = false
    private var armingTimestamp = 0L

    fun startListening(sensitivityThreshold: Float = 4.5f) {
        if (isListening || accelerometer == null) return
        isArmed = false
        armingTimestamp = System.currentTimeMillis() + 1500L // 1.5s grace period to set phone down
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        isListening = true
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
        isArmed = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !isListening) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        lastAcceleration = currentAcceleration
        currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta = kotlin.math.abs(currentAcceleration - lastAcceleration)

        val now = System.currentTimeMillis()
        if (!isArmed && now >= armingTimestamp) {
            isArmed = true
        }

        if (isArmed && delta > 4.5f) {
            onMotionTriggered()
            // Reset arming to avoid repeated firing within 2 seconds
            isArmed = false
            armingTimestamp = now + 2000L
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
