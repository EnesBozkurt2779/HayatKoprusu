package com.hayatkoprusu.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.sqrt

class SeismographMonitor(private val context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    private val P_WAVE_THRESHOLD = 2.5f 
    private var lastAlertTime = 0L

    fun start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            val magnitude = sqrt(x*x + y*y + z*z)
            val delta = Math.abs(magnitude - SensorManager.GRAVITY_EARTH)

            if (delta > P_WAVE_THRESHOLD) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastAlertTime > 5000) { 
                    lastAlertTime = currentTime
                    triggerLocalAlert()
                }
            }
        }
    }

    fun triggerLocalAlert() {
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 3000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(3000)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        toneGenerator.release()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
