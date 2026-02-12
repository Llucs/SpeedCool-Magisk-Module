package dev.llucs.speedcool.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val svc = Intent(context, SpeedCoolService::class.java)
        ContextCompat.startForegroundService(context, svc)
    }
}
