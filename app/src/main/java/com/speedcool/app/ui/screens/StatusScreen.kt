package com.speedcool.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.speedcool.app.data.Profile
import com.speedcool.app.data.SystemStatus
import com.speedcool.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
    status: SystemStatus,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status do Sistema", fontWeight = FontWeight.Bold) },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Informações do Sistema", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, "Atualizar")
                }
            }

            Spacer(Modifier.height(16.dp))

            StatusSection("CPU") {
                StatusItem("Uso", "${status.cpuUsage}%")
                StatusItem("Governor", status.governor)
                StatusItem("Núcleos", "${status.coreCount}")
                StatusItem("Frequência", "${status.gpuFreq} MHz")
            }

            Spacer(Modifier.height(12.dp))

            StatusSection("GPU") {
                StatusItem("Governor", status.gpuGovernor)
                StatusItem("Frequência", "${status.gpuFreq} MHz")
            }

            Spacer(Modifier.height(12.dp))

            StatusSection("RAM") {
                StatusItem("Uso", "${status.ramUsagePercent}%")
                StatusItem("Total", "${status.ramTotal} MB")
                StatusItem("Livre", "${status.ramFree} MB")
            }

            Spacer(Modifier.height(12.dp))

            StatusSection("Temperatura") {
                StatusItem("CPU", "${status.cpuTemp}°C",
                    color = when {
                        status.cpuTemp < 45 -> TempNormal
                        status.cpuTemp < 65 -> TempWarm
                        else -> TempHot
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            StatusSection("Bateria") {
                StatusItem("Nível", "${status.batteryLevel}%")
                StatusItem("Status", status.batteryStatus)
            }

            Spacer(Modifier.height(12.dp))

            StatusSection("Dispositivo") {
                StatusItem("Chipset", status.chipset)
                StatusItem("Modo", executorModeDisplay(status.activeProfile))
            }
        }
    }
}

@Composable
private fun StatusSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Primary)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun StatusItem(label: String, value: String, color: androidx.compose.ui.graphics.Color = OnSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = OnSurfaceVariant, fontSize = 14.sp)
        Text(value, color = color, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
    Divider(color = DividerColor, thickness = 0.5.dp)
}

private fun executorModeDisplay(profile: Profile): String = when (profile) {
    Profile.ECO -> "Eco"
    Profile.BALANCED -> "Equilibrado"
    Profile.PERFORMANCE -> "Performance"
    Profile.LEARNING -> "Aprendizado"
}
