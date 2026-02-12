package dev.llucs.speedcool.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class SpeedCoolStatus(
    val root: Boolean,
    val activeProfile: ProfileId,
    val cpuGovernorSummary: String,
    val ramUsedPct: Int,
    val temperatureC: Double?,
    val batteryPct: Int?,
    val charging: Boolean?
)

class StatusRepository(
    private val probe: DeviceProbe
) {

    suspend fun readStatus(activeProfile: ProfileId): SpeedCoolStatus {
        val root = RootShell.isRootAvailable()
        val cpuGov = readCpuGovernorSummary()
        val (ramPct) = readRamUsage()
        val temp = readTemperatureC()
        val (batPct, charging) = readBattery()
        return SpeedCoolStatus(
            root = root,
            activeProfile = activeProfile,
            cpuGovernorSummary = cpuGov,
            ramUsedPct = ramPct,
            temperatureC = temp,
            batteryPct = batPct,
            charging = charging
        )
    }

    private suspend fun readCpuGovernorSummary(): String {
        val policies = probe.cpuPolicies()
        if (policies.isEmpty()) return "Unknown"
        val govs = mutableMapOf<String, Int>()
        for (p in policies) {
            val gov = KernelFs.readText(p.governorPath)?.ifBlank { null } ?: "unknown"
            govs[gov] = (govs[gov] ?: 0) + 1
        }
        return govs.entries.sortedByDescending { it.value }.joinToString(", ") { "${it.key} x${it.value}" }
    }

    private suspend fun readRamUsage(): Pair<Int, Long> = withContext(Dispatchers.IO) {
        val meminfo = runCatching { File("/proc/meminfo").readLines() }.getOrElse { emptyList() }
        fun getKb(key: String): Long? {
            val line = meminfo.firstOrNull { it.startsWith(key) } ?: return null
            return line.split(Regex("\s+")).getOrNull(1)?.toLongOrNull()
        }
        val total = getKb("MemTotal:") ?: 0L
        val avail = getKb("MemAvailable:") ?: 0L
        if (total <= 0) return@withContext 0 to 0L
        val used = (total - avail).coerceAtLeast(0)
        val pct = ((used.toDouble() / total.toDouble()) * 100.0).toInt().coerceIn(0, 100)
        pct to used
    }

    private suspend fun readTemperatureC(): Double? {
        val sensors = probe.thermalSensors()
        val milli = probe.readBestTemperatureMilliC(sensors) ?: return null
        return milli / 1000.0
    }

    private suspend fun readBattery(): Pair<Int?, Boolean?> {
        val res = RootShell.exec("dumpsys battery")
        if (!res.ok) return null to null
        val text = res.out.joinToString("\n")
        val level = Regex("level:\s*(\d+)").find(text)?.groupValues?.getOrNull(1)?.toIntOrNull()
        val status = Regex("status:\s*(\d+)").find(text)?.groupValues?.getOrNull(1)?.toIntOrNull()
        val charging = status?.let { it == 2 || it == 5 }
        return level to charging
    }
}
