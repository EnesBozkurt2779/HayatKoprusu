package com.hayatkoprusu.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.hayatkoprusu.R
import com.hayatkoprusu.network.MeshService
import com.hayatkoprusu.sensors.SeismographMonitor

class MainActivity : AppCompatActivity() {
    private var logoClickCount = 0
    private lateinit var seismograph: SeismographMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupUI()
        if (checkPermissions()) {
            startServices()
        }
    }

    private fun setupUI() {
        findViewById<View>(R.id.btn_sos).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra("MODE", "SOS")
            })
        }

        findViewById<View>(R.id.btn_safe).setOnClickListener {
            Toast.makeText(this, "Güvende olduğunuz mesh ağına iletiliyor", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.logo).setOnClickListener {
            logoClickCount++
            if (logoClickCount >= 5) {
                logoClickCount = 0
                Toast.makeText(this, "Mühendislik Modu Aktif", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, EngineeringActivity::class.java))
            }
        }
    }

    private fun startServices() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, MeshService::class.java))
        } else {
            startService(Intent(this, MeshService::class.java))
        }
        seismograph = SeismographMonitor(this)
        seismograph.start()
    }

    private fun checkPermissions(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        val missing = permissions.filter { 
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED 
        }

        return if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startServices()
        } else {
            Toast.makeText(this, "Uygulamanın çalışması için izinler gerekli", Toast.LENGTH_LONG).show()
        }
    }
}
