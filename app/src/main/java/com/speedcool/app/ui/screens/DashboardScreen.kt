package com.speedcool.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.speedcool.app.data.Profile
import com.speedcool.app.data.SystemStatus
import com.speedcool.app.ui.components.ProfileSelector
import com.speedcool.app.ui.components.RamBar
import com.speedcool.app.ui.components.StatCard
import com.speedcool.app.ui.components.TempGauge
import com.speedcool.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    status: SystemStatus,
    activeProfile: Profile,
    executorMode: String,
    onProfileSelected: (Profile) -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    isServiceRunning: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var autoRefresh by remember { mutableStateOf(true) }

    LaunchedEffect(autoRefresh) {
        while (autoRefresh) {
            delay(3000)
            onRefresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SpeedCool", fontWeight = FontWeight.Bold) },
                actions = {
                    Text(
                        text = executorMode.uppercase(),
                        color = when (executorMode) {
                            "root" -> PerformanceColor
                            "shizuku" -> BalancedColor
                            else -> OnSurfaceVariant
                        },
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = autoRefresh,
                        onCheckedChange = { autoRefresh = it },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            ProfileSelector(
                currentProfile = activeProfile,
                onProfileSelected = onProfileSelected
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "CPU",
                    value = "${status.cpuUsage}",
                    unit = "%",
                    icon = { Icon(Icons.Default.Speed, "CPU", tint = Primary) },
                    modifier = Modifier.weight(1f),
                    color = Primary
                )
                StatCard(
                    title = "RAM",
                    value = "${status.ramUsagePercent}",
                    unit = "%",
                    icon = { Icon(Icons.Default.Memory, "RAM", tint = Accent) },
                    modifier = Modifier.weight(1f),
                    color = Accent
                )
                StatCard(
                    title = "Bateria",
                    value = "${status.batteryLevel}",
                    unit = "%",
                    icon = { Icon(Icons.Default.BatteryFull, "Battery", tint = EcoColor) },
                    modifier = Modifier.weight(1f),
                    color = EcoColor
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TempGauge(temp = status.cpuTemp)
                Column {
                    Text("Chipset: ${status.chipset}", color = OnSurfaceVariant, fontSize = 12.sp)
                    Text("Governor: ${status.governor}", color = OnSurfaceVariant, fontSize = 12.sp)
                    Text("Núcleos: ${status.coreCount}", color = OnSurfaceVariant, fontSize = 12.sp)
                    if (status.conflictsDetected) {
                        Text("⚠️ Conflitos detectados", color = TempHot, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            RamBar(usagePercent = status.ramUsagePercent)

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (isServiceRunning) onStopService()
                        else onStartService()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isServiceRunning) TempHot else Primary
                    )
                ) {
                    Text(if (isServiceRunning) "Parar" else "Iniciar")
                }
                OutlinedButton(onClick = onRefresh, modifier = Modifier.weight(1f)) {
                    Text("Atualizar")
                }
            }
        }
    }
}
