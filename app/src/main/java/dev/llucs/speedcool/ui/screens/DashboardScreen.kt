package dev.llucs.speedcool.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.llucs.speedcool.core.AppSettings
import dev.llucs.speedcool.core.ApplyReport
import dev.llucs.speedcool.core.DeviceProbe
import dev.llucs.speedcool.core.ProfileId
import dev.llucs.speedcool.core.ProfilesEngine
import dev.llucs.speedcool.core.RootShell
import dev.llucs.speedcool.core.SpeedCoolLogger
import dev.llucs.speedcool.core.SpeedCoolStatus
import dev.llucs.speedcool.core.StatusRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    onOpenLogs: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val settings = remember { AppSettings(context) }
    val logger = remember { SpeedCoolLogger(context) }
    val probe = remember { DeviceProbe() }
    val profilesEngine = remember { ProfilesEngine(probe) }
    val statusRepo = remember { StatusRepository(probe) }
    val scope = rememberCoroutineScope()

    val activeProfile by settings.activeProfile.collectAsState(initial = ProfileId.BALANCED)

    var status by remember { mutableStateOf<SpeedCoolStatus?>(null) }
    var lastReport by remember { mutableStateOf<ApplyReport?>(null) }
    var rootOk by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    suspend fun refresh() {
        rootOk = RootShell.isRootAvailable()
        status = statusRepo.readStatus(activeProfile)
    }

    LaunchedEffect(activeProfile) { refresh() }
    LaunchedEffect(Unit) { refresh() }

    fun applyProfile(p: ProfileId) {
        scope.launch {
            if (busy) return@launch
            busy = true
            settings.setActiveProfile(p)
            if (RootShell.isRootAvailable()) {
                lastReport = profilesEngine.apply(p, logger)
            }
            status = statusRepo.readStatus(p)
            busy = false
        }
    }

    fun doRefresh() {
        scope.launch {
            if (busy) return@launch
            busy = true
            refresh()
            busy = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SpeedCool") },
                actions = {
                    IconButton(onClick = onOpenLogs) { Icon(Icons.Default.Terminal, contentDescription = "Logs") }
                    IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Settings, contentDescription = "Settings") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Status", style = MaterialTheme.typography.titleMedium)
                        val s = status
                        if (s == null) {
                            Text("Loading…")
                        } else {
                            Text(if (s.root) "Root: Available" else "Root: Not available")
                            Text("Active Profile: ${s.activeProfile.displayName}")
                            Text("CPU: ${s.cpuGovernorSummary}")
                            Text("RAM Used: ${s.ramUsedPct}%")
                            Text("Temperature: ${s.temperatureC?.let { "%.1f°C".format(it) } ?: "N/A"}")
                            Text("Battery: ${s.batteryPct?.let { "$it%" } ?: "N/A"}")
                        }
                    }
                }
            }

            item {
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Profiles", style = MaterialTheme.typography.titleMedium)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileId.values().forEach { p ->
                                FilterChip(
                                    selected = p == activeProfile,
                                    onClick = { applyProfile(p) },
                                    enabled = !busy,
                                    label = { Text(p.displayName) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(enabled = rootOk && !busy, onClick = { applyProfile(activeProfile) }) {
                                Text("Apply")
                            }
                            OutlinedButton(enabled = !busy, onClick = { doRefresh() }) { Text("Refresh") }
                        }
                    }
                }
            }

            item {
                val report = lastReport
                if (report != null) {
                    ElevatedCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Last Apply", style = MaterialTheme.typography.titleMedium)
                            Text("Profile: ${report.profile.displayName}")
                            if (report.actions.isNotEmpty()) {
                                Text("Actions", style = MaterialTheme.typography.labelLarge)
                                report.actions.take(8).forEach { Text("• $it") }
                            }
                            if (report.warnings.isNotEmpty()) {
                                Text("Warnings", style = MaterialTheme.typography.labelLarge)
                                report.warnings.take(6).forEach { Text("• $it") }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}
