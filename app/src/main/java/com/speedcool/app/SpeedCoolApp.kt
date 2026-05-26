package com.speedcool.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.speedcool.app.data.SettingsData

class SpeedCoolApp : Application() {

    lateinit var settings: SettingsData
        private set

    override fun onCreate() {
        super.onCreate()
        settings = SettingsData(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "SpeedCool Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificações do serviço de otimização SpeedCool"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "speedcool_service"
        const val NOTIFICATION_ID = 1001
    }
}
