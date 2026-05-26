package com.speedcool.app.engine

import com.speedcool.app.root.ShellExecutor

class ThermalManager(private val executor: ShellExecutor) {

    suspend fun getCpuTemp(): Result<Int> = executor.execute(
        "cat /sys/class/thermal/thermal_zone*/temp 2>/dev/null | awk '{sum+=\$1} END{print int(sum/NR/1000)}'"
    ).map { it.trim().toIntOrNull() ?: 0 }

    suspend fun getThermalZones(): Result<List<Triple<String, Int, String>>> = executor.execute(
        "for z in /sys/class/thermal/thermal_zone*/temp; do [ ! -f \"\$z\" ] && continue; raw=\$(cat \"\$z\" 2>/dev/null || echo \"\"); [ -z \"\$raw\" ] && continue; type=\$(cat \"\${z%/*}/type\" 2>/dev/null || echo \"Desconhecido\"); [ \"\$raw\" -lt 1000 ] && raw=\$((raw*1000)); echo \"\$type:\$((raw/1000)):\$(cat \"\${z%/*}/mode\" 2>/dev/null || echo enabled)\"; done"
    ).map { output ->
        output.lines().filter { it.isNotBlank() }.map { line ->
            val parts = line.split(":")
            Triple(parts[0], parts.getOrElse(1) { "0" }.toIntOrNull() ?: 0, parts.getOrElse(2) { "enabled" })
        }
    }

    suspend fun enableThermalZones(): Result<String> =
        executor.execute("for t in /sys/class/thermal/thermal_zone*/mode; do [ -w \"\$t\" ] && echo enabled > \"\$t\"; done")
            .map { "Zonas térmicas ativadas" }

    suspend fun getBatteryTemp(): Result<Int> =
        executor.execute("dumpsys battery 2>/dev/null | grep temperature | awk '{print \$2}'")
            .map { (it.trim().toIntOrNull() ?: 0) / 10 }

    suspend fun getBatteryLevel(): Result<Int> =
        executor.execute("cat /sys/class/power_supply/battery/capacity 2>/dev/null || echo 0")
            .map { it.trim().toIntOrNull() ?: 0 }

    suspend fun getBatteryStatus(): Result<String> =
        executor.execute("cat /sys/class/power_supply/battery/status 2>/dev/null || echo Unknown")
            .map { it.trim() }

    fun getTempColor(tempC: Int): Int = when {
        tempC < 45 -> 0xFF4CAF50.toInt()
        tempC < 65 -> 0xFFFF9800.toInt()
        else -> 0xFFF44336.toInt()
    }
}
