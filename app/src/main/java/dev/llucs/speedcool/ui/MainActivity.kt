package dev.llucs.speedcool.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import dev.llucs.speedcool.service.SpeedCoolService
import dev.llucs.speedcool.ui.theme.SpeedCoolTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ContextCompat.startForegroundService(this, Intent(this, SpeedCoolService::class.java))

        setContent {
            SpeedCoolTheme {
                SpeedCoolApp()
            }
        }
    }
}
