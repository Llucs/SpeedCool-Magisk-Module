package com.speedcool.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.speedcool.app.MainActivity
import com.speedcool.app.R
import com.speedcool.app.SpeedCoolApp
import com.speedcool.app.engine.OptimizationEngine
import kotlinx.coroutines.*
import moe.shizuku.api.Shizuku

class SpeedCoolService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var engine: OptimizationEngine? = null
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        engine = OptimizationEngine()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isRunning) return START_STICKY
        isRunning = true

        val notification = createNotification()
        startForeground(SpeedCoolApp.NOTIFICATION_ID, notification)

        scope.launch {
            engine?.initialize(preferShizuku = true)
            while (isActive) {
                engine?.let {
                    val status = it.collectStatus()
                    it.applyProfile(
                        when {
                            status.cpuUsage > 70 -> com.speedcool.app.data.Profile.PERFORMANCE
                            status.cpuUsage > 35 -> com.speedcool.app.data.Profile.BALANCED
                            else -> com.speedcool.app.data.Profile.ECO
                        }
                    )
                    if (status.ramUsagePercent > 85) {
                        it.cleanRam()
                    }
                }
                delay(30000)
            }
        }

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, SpeedCoolApp.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("SpeedCool")
            .setContentText("Otimizando seu dispositivo")
            .setSmallIcon(android.R.drawable.ic_menu_sort_by_size)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isRunning = false
        scope.cancel()
        engine?.cleanup()
        super.onDestroy()
    }
}
