package com.speedcool.app.ui.screens

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
import com.speedcool.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    autoStart: Boolean,
    backgroundService: Boolean,
    autoRamClean: Boolean,
    learningEnabled: Boolean,
    conflictAutoResolve: Boolean,
    tempUnit: String,
    ramCleanInterval: Int,
    onAutoStartChange: (Boolean) -> Unit,
    onBackgroundServiceChange: (Boolean) -> Unit,
    onAutoRamCleanChange: (Boolean) -> Unit,
    onLearningEnabledChange: (Boolean) -> Unit,
    onConflictAutoResolveChange: (Boolean) -> Unit,
    onTempUnitChange: (String) -> Unit,
    onRamCleanIntervalChange: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações", fontWeight = FontWeight.Bold) },
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
            SettingsSection("Serviço") {
                SettingsSwitch(
                    title = "Iniciar com o sistema",
                    description = "Iniciar otimização automaticamente após a inicialização",
                    checked = autoStart,
                    onCheckedChange = onAutoStartChange,
                    icon = { Icon(Icons.Default.PlayArrow, null) }
                )
                SettingsSwitch(
                    title = "Serviço em segundo plano",
                    description = "Manter otimização ativa em segundo plano",
                    checked = backgroundService,
                    onCheckedChange = onBackgroundServiceChange,
                    icon = { Icon(Icons.Default.Settings, null) }
                )
            }

            Spacer(Modifier.height(16.dp))

            SettingsSection("Otimização") {
                SettingsSwitch(
                    title = "Limpeza automática de RAM",
                    description = "Limpar RAM automaticamente quando necessário",
                    checked = autoRamClean,
                    onCheckedChange = onAutoRamCleanChange,
                    icon = { Icon(Icons.Default.Memory, null) }
                )
                SettingsSwitch(
                    title = "Motor de aprendizado",
                    description = "Ajustar perfis baseado no uso do dispositivo",
                    checked = learningEnabled,
                    onCheckedChange = onLearningEnabledChange,
                    icon = { Icon(Icons.Default.SmartToy, null) }
                )
                SettingsSwitch(
                    title = "Auto-resolver conflitos",
                    description = "Detectar e resolver conflitos automaticamente",
                    checked = conflictAutoResolve,
                    onCheckedChange = onConflictAutoResolveChange,
                    icon = { Icon(Icons.Default.Shield, null) }
                )
            }

            Spacer(Modifier.height(16.dp))

            SettingsSection("Monitoramento") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Unidade de temperatura", fontWeight = FontWeight.Medium)
                        Text("°C / °F", color = OnSurfaceVariant, fontSize = 12.sp)
                    }
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(tempUnit.uppercase())
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Celsius") },
                                onClick = { onTempUnitChange("celsius"); expanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Fahrenheit") },
                                onClick = { onTempUnitChange("fahrenheit"); expanded = false }
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Intervalo limpeza RAM", fontWeight = FontWeight.Medium)
                        Text("$ramCleanInterval segundos", color = OnSurfaceVariant, fontSize = 12.sp)
                    }
                    Slider(
                        value = ramCleanInterval.toFloat(),
                        onValueChange = { onRamCleanIntervalChange(it.toInt()) },
                        valueRange = 60f..600f,
                        steps = 8,
                        modifier = Modifier.width(200.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
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
private fun SettingsSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(description, color = OnSurfaceVariant, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    Divider(color = DividerColor, thickness = 0.5.dp)
}
