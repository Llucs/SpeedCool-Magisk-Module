package com.speedcool.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.speedcool.app.data.Profile
import com.speedcool.app.data.SystemStatus
import com.speedcool.app.engine.OptimizationEngine
import com.speedcool.app.ui.screens.DashboardScreen
import com.speedcool.app.ui.screens.SettingsScreen
import com.speedcool.app.ui.screens.StatusScreen
import com.speedcool.app.ui.theme.Background
import com.speedcool.app.ui.theme.OnSurfaceVariant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Screen(val title: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Default.Home),
    Status("Status", Icons.Default.MonitorHeart),
    Settings("Ajustes", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedCoolNavHost() {
    val engine = remember { OptimizationEngine() }
    val scope = rememberCoroutineScope()
    var selectedScreen by remember { mutableStateOf(Screen.Dashboard) }
    var status by remember { mutableStateOf(SystemStatus()) }
    var activeProfile by remember { mutableStateOf(Profile.BALANCED) }
    var executorMode by remember { mutableStateOf("none") }
    var isInitialized by remember { mutableStateOf(false) }
    var isServiceRunning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        val result = engine.initialize(preferShizuku = true)
        if (result.isSuccess) {
            executorMode = engine.getExecutor()?.mode ?: "none"
            status = engine.collectStatus()
            isInitialized = true
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Background) {
                Screen.entries.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, screen.title) },
                        label = { Text(screen.title) },
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedTextColor = OnSurfaceVariant,
                            unselectedIconColor = OnSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedScreen) {
                Screen.Dashboard -> DashboardScreen(
                    status = status,
                    activeProfile = activeProfile,
                    executorMode = executorMode,
                    onProfileSelected = { profile ->
                        activeProfile = profile
                        scope.launch {
                            engine.applyProfile(profile)
                            status = engine.collectStatus()
                        }
                    },
                    onStartService = {
                        scope.launch {
                            isServiceRunning = true
                            executorMode = engine.getExecutor()?.mode ?: "none"
                        }
                    },
                    onStopService = {
                        isServiceRunning = false
                    },
                    isServiceRunning = isServiceRunning,
                    onRefresh = {
                        scope.launch {
                            status = engine.collectStatus()
                        }
                    }
                )
                Screen.Status -> StatusScreen(
                    status = status,
                    onRefresh = {
                        scope.launch {
                            status = engine.collectStatus()
                        }
                    }
                )
                Screen.Settings -> SettingsScreen(
                    autoStart = false,
                    backgroundService = true,
                    autoRamClean = true,
                    learningEnabled = true,
                    conflictAutoResolve = true,
                    tempUnit = "celsius",
                    ramCleanInterval = 180,
                    onAutoStartChange = {},
                    onBackgroundServiceChange = {},
                    onAutoRamCleanChange = {},
                    onLearningEnabledChange = {},
                    onConflictAutoResolveChange = {},
                    onTempUnitChange = {},
                    onRamCleanIntervalChange = {}
                )
            }
        }
    }
}
