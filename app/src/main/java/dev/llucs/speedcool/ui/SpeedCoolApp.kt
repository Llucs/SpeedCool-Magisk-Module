package dev.llucs.speedcool.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.llucs.speedcool.ui.screens.DashboardScreen
import dev.llucs.speedcool.ui.screens.LogsScreen
import dev.llucs.speedcool.ui.screens.SettingsScreen

@Composable
fun SpeedCoolApp() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "dashboard") {
        composable("dashboard") { DashboardScreen(onOpenLogs = { nav.navigate("logs") }, onOpenSettings = { nav.navigate("settings") }) }
        composable("logs") { LogsScreen(onBack = { nav.popBackStack() }) }
        composable("settings") { SettingsScreen(onBack = { nav.popBackStack() }) }
    }
}
