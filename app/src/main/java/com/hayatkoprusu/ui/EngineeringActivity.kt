package com.hayatkoprusu.ui

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hayatkoprusu.R
import com.hayatkoprusu.sensors.SeismographMonitor
import java.util.*

/**
 * Engineering Interface for Real-time Telemetry.
 */
class EngineeringActivity : AppCompatActivity() {
    private lateinit var tvTelemetry: TextView
    private lateinit var seismograph: SeismographMonitor
    private val timer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.BLACK)
        }

        val btnTestSiren = Button(this).apply {
            text = "SİREN TEST ET"
            setBackgroundColor(android.graphics.Color.RED)
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                seismograph.triggerLocalAlert()
            }
        }
        layout.addView(btnTestSiren)

        val scrollView = android.widget.ScrollView(this)
        tvTelemetry = TextView(this).apply {
            setTextColor(android.graphics.Color.CYAN)
            textSize = 12f
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(20, 20, 20, 20)
        }
        scrollView.addView(tvTelemetry)
        layout.addView(scrollView)
        
        setContentView(layout)

        seismograph = SeismographMonitor(this)
        startTelemetryStream()
    }

    private fun startTelemetryStream() {
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val mockPacket = "PKT_${System.currentTimeMillis()}: [${(0..255).random().toString(16).uppercase()}] RSSI: -${(40..90).random()}dBm\n"
                runOnUiThread {
                    tvTelemetry.append(mockPacket)
                    if (tvTelemetry.lineCount > 50) {
                        tvTelemetry.text = tvTelemetry.text.substring(tvTelemetry.text.indexOf('\n') + 1)
                    }
                }
            }
        }, 0, 500)
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }
}
