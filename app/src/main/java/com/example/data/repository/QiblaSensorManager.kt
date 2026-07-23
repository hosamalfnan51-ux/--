package com.example.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QiblaSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private var rotationSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private var accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var magnetometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _azimuthDegree = MutableStateFlow(0f)
    val azimuthDegree: StateFlow<Float> = _azimuthDegree.asStateFlow()

    private val _hasSensorSupport = MutableStateFlow(true)
    val hasSensorSupport: StateFlow<Boolean> = _hasSensorSupport.asStateFlow()

    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var isLastAccelerometerSet = false
    private var isLastMagnetometerSet = false

    private var rMatrix = FloatArray(9)
    private var orientationValues = FloatArray(3)

    fun registerListeners() {
        if (sensorManager == null) {
            _hasSensorSupport.value = false
            return
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        } else {
            _hasSensorSupport.value = false
        }
    }

    fun unregisterListeners() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMatrix, event.values)
            SensorManager.getOrientation(rMatrix, orientationValues)
            var azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
            if (azimuth < 0) azimuth += 360f
            _azimuthDegree.value = azimuth
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            isLastAccelerometerSet = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            isLastMagnetometerSet = true
        }

        if (isLastAccelerometerSet && isLastMagnetometerSet && event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) {
            if (SensorManager.getRotationMatrix(rMatrix, null, lastAccelerometer, lastMagnetometer)) {
                SensorManager.getOrientation(rMatrix, orientationValues)
                var azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                if (azimuth < 0) azimuth += 360f
                _azimuthDegree.value = azimuth
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
