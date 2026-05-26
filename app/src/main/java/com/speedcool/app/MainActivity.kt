package com.speedcool.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.speedcool.app.ui.SpeedCoolNavHost
import com.speedcool.app.ui.theme.SpeedCoolTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpeedCoolTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SpeedCoolNavHost()
                }
            }
        }
    }
}
