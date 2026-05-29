package com.hayatkoprusu.network

import com.hayatkoprusu.data.AppDatabase
import com.hayatkoprusu.data.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.timerTask

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hayatkoprusu.R
import android.app.Service
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log

class MeshService : Service() {
    private val CHANNEL_ID = "MeshServiceChannel"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hayat Köprüsü Aktif")
            .setContentText("Mesh ağı taranıyor ve veri yayını yapılıyor...")
            .setSmallIcon(R.drawable.ic_logo)
            .build()
        startForeground(1, notification)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Mesh Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
    private lateinit var wifiManager: WifiManager
    private var ssidCycleTimer: Timer? = null
    private var scanTimer: Timer? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        db = AppDatabase.getDatabase(this)
        startMeshLoops()
    }

    private fun startMeshLoops() {
        ssidCycleTimer = Timer()
        ssidCycleTimer?.scheduleAtFixedRate(timerTask {
            cycleSsid()
        }, 0, 3000)

        scanTimer = Timer()
        scanTimer?.scheduleAtFixedRate(timerTask {
            startPassiveScan()
        }, 0, 10000)
    }

    private fun cycleSsid() {
        try {
            Log.d("MeshService", "Cycling SSID")
        } catch (e: Exception) {
            Log.e("MeshService", "SSID Cycle failed", e)
        }
    }

    private fun startPassiveScan() {
        wifiManager.startScan()
        val results = wifiManager.scanResults
        for (result in results) {
            if (result.SSID.startsWith("HAYAT_")) {
                parseMeshSsid(result.SSID, result.level)
            }
        }
    }

    private fun parseMeshSsid(ssid: String, rssi: Int) {
        val parts = ssid.split("_")
        if (parts.size == 5) {
            val msgId = parts[1]
            val lat = parts[2].toDouble() / 1000.0
            val lng = parts[3].toDouble() / 1000.0
            val status = parts[4].toInt()
            
            Log.d("MeshService", "Discovered Node: $msgId at ($lat, $lng) RSSI: $rssi Status: $status")
            
            serviceScope.launch {
                db.messageDao().insertMessage(
                    MessageEntity(
                        msgId = msgId.toLong(),
                        senderId = "PEER_$msgId",
                        timestamp = System.currentTimeMillis(),
                        content = "Status: ${if(status == 1) "SOS" else "SAFE"} at ($lat, $lng)",
                        originalLength = 0,
                        statusMask = status.toByte(),
                        isComplete = true,
                        expiration = System.currentTimeMillis() + 3600000
                    )
                )
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        ssidCycleTimer?.cancel()
        scanTimer?.cancel()
        super.onDestroy()
    }
}
