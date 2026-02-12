package dev.llucs.speedcool.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.llucs.speedcool.core.AppSettings
import kotlinx.coroutines.launch

private data class Day(val label: String, val bit: Int)

@OptIn(ExperimentalMaterial3Api::class)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val settings = remember { AppSettings(context) }
    val scope = rememberCoroutineScope()

    val enableEngine by settings.enableEngine.collectAsState(initial = true)
    val enableCooling by settings.enableCooling.collectAsState(initial = true)
    val enableLearning by settings.enableLearning.collectAsState(initial = true)
    val coolingOnC by settings.coolingOnC.collectAsState(initial = 45)
    val coolingOffC by settings.coolingOffC.collectAsState(initial = 40)
    val weeklyMask by settings.weeklyMask.collectAsState(initial = 0)

    val days = remember {
        listOf(
            Day("Mon", 0),
            Day("Tue", 1),
            Day("Wed", 2),
            Day("Thu", 3),
            Day("Fri", 4),
            Day("Sat", 5),
            Day("Sun", 6)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Engine", style = MaterialTheme.typography.titleMedium)

                    SettingToggle(
                        label = "Enable engine service",
                        value = enableEngine,
                        onChange = { v -> scope.launch { settings.setEnableEngine(v) } }
                    )
                    SettingToggle(
                        label = "Enable cooling override",
                        value = enableCooling,
                        onChange = { v -> scope.launch { settings.setEnableCooling(v) } }
                    )
                    SettingToggle(
                        label = "Enable learning engine",
                        value = enableLearning,
                        onChange = { v -> scope.launch { settings.setEnableLearning(v) } }
                    )
                }
            }

            ElevatedCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Cooling thresholds", style = MaterialTheme.typography.titleMedium)
                    Text("Cooling ON: $coolingOnC°C, OFF: $coolingOffC°C", style = MaterialTheme.typography.bodyMedium)

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = {
                                scope.launch { settings.setCoolingThresholds((coolingOnC - 1).coerceAtLeast(30), coolingOffC) }
                            }
                        ) { Text("- ON") }
                        OutlinedButton(
                            onClick = {
                                scope.launch { settings.setCoolingThresholds((coolingOnC + 1).coerceAtMost(70), coolingOffC) }
                            }
                        ) { Text("+ ON") }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = {
                                scope.launch { settings.setCoolingThresholds(coolingOnC, (coolingOffC - 1).coerceAtLeast(25)) }
                            }
                        ) { Text("- OFF") }
                        OutlinedButton(
                            onClick = {
                                scope.launch { settings.setCoolingThresholds(coolingOnC, (coolingOffC + 1).coerceAtMost(65)) }
                            }
                        ) { Text("+ OFF") }
                    }
                }
            }

            ElevatedCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Weekly scheduling", style = MaterialTheme.typography.titleMedium)
                    Text("If a day is enabled, SpeedCool will prefer Performance profile on that day.")

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        days.forEach { d ->
                            val selected = ((weeklyMask shr d.bit) and 1) == 1
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    val newMask = if (selected) weeklyMask and (1 shl d.bit).inv()
                                    else weeklyMask or (1 shl d.bit)
                                    scope.launch { settings.setWeeklyMask(newMask) }
                                },
                                label = { Text(d.label) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SettingToggle(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = value, onCheckedChange = onChange)
    }
}
