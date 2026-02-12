package dev.llucs.speedcool.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class CpuPolicy(
    val policyDir: String,
    val governorPath: String,
    val minFreqPath: String?,
    val maxFreqPath: String?,
    val availableGovernorsPath: String?
)

data class ThermalSensor(val path: String)

class DeviceProbe {

    suspend fun cpuPolicies(): List<CpuPolicy> = withContext(Dispatchers.IO) {
        val policies = mutableListOf<CpuPolicy>()
        val base = File("/sys/devices/system/cpu/cpufreq")
        val policyDirs = base.listFiles { f -> f.isDirectory && f.name.startsWith("policy") }.orEmpty()
        for (dir in policyDirs) {
            val gov = File(dir, "scaling_governor")
            if (!gov.exists()) continue
            val min = File(dir, "scaling_min_freq").takeIf { it.exists() }?.absolutePath
            val max = File(dir, "scaling_max_freq").takeIf { it.exists() }?.absolutePath
            val avGov = File(dir, "scaling_available_governors").takeIf { it.exists() }?.absolutePath
            policies += CpuPolicy(
                policyDir = dir.absolutePath,
                governorPath = gov.absolutePath,
                minFreqPath = min,
                maxFreqPath = max,
                availableGovernorsPath = avGov
            )
        }

        if (policies.isNotEmpty()) return@withContext policies

        val cpuBase = File("/sys/devices/system/cpu")
        val cpuDirs = cpuBase.listFiles { f -> f.isDirectory && f.name.matches(Regex("cpu\d+")) }.orEmpty()
        for (cpuDir in cpuDirs) {
            val cpufreq = File(cpuDir, "cpufreq")
            val gov = File(cpufreq, "scaling_governor")
            if (!gov.exists()) continue
            val min = File(cpufreq, "scaling_min_freq").takeIf { it.exists() }?.absolutePath
            val max = File(cpufreq, "scaling_max_freq").takeIf { it.exists() }?.absolutePath
            val avGov = File(cpufreq, "scaling_available_governors").takeIf { it.exists() }?.absolutePath
            policies += CpuPolicy(
                policyDir = cpufreq.absolutePath,
                governorPath = gov.absolutePath,
                minFreqPath = min,
                maxFreqPath = max,
                availableGovernorsPath = avGov
            )
        }
        policies
    }

    suspend fun thermalSensors(): List<ThermalSensor> = withContext(Dispatchers.IO) {
        val sensors = mutableListOf<ThermalSensor>()
        fun addGlob(dir: File, regex: Regex) {
            dir.listFiles()?.forEach { f ->
                if (f.isFile && regex.matches(f.name)) sensors += ThermalSensor(f.absolutePath)
            }
        }

        val thermal = File("/sys/class/thermal")
        thermal.listFiles { f -> f.isDirectory && f.name.startsWith("thermal_zone") }?.forEach { zone ->
            val temp = File(zone, "temp")
            if (temp.exists() && temp.canRead()) sensors += ThermalSensor(temp.absolutePath)
        }

        val ps = File("/sys/class/power_supply")
        ps.listFiles()?.forEach { d ->
            if (d.isDirectory) {
                val temp = File(d, "temp")
                if (temp.exists() && temp.canRead()) sensors += ThermalSensor(temp.absolutePath)
            }
        }

        val hwmon = File("/sys/class/hwmon")
        hwmon.listFiles()?.forEach { d ->
            if (d.isDirectory) addGlob(d, Regex("temp\d+_input"))
        }

        sensors.distinctBy { it.path }
    }

    suspend fun readBestTemperatureMilliC(sensors: List<ThermalSensor>): Long? {
        var best: Long? = null
        for (s in sensors) {
            val v = KernelFs.readLong(s.path) ?: continue
            if (v <= 0) continue
            val milli = if (v < 1000) v * 1000 else v
            if (best == null || milli > best!!) best = milli
        }
        return best
    }
}
