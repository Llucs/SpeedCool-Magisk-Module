package com.speedcool.app.engine

import com.speedcool.app.data.ConflictInfo
import com.speedcool.app.data.OptimizationResult
import com.speedcool.app.data.Profile
import com.speedcool.app.data.SystemStatus
import com.speedcool.app.root.ExecutorFactory
import com.speedcool.app.root.ShellExecutor

class OptimizationEngine {

    private var executor: ShellExecutor? = null
    private val perfMan get() = executor?.let { PerformanceManager(it) }
    private val thermMan get() = executor?.let { ThermalManager(it) }
    private val ramMan get() = executor?.let { RAMManager(it) }
    private val conflictDetector get() = executor?.let { ConflictDetector(it) }

    suspend fun initialize(preferShizuku: Boolean = true): Result<Unit> = runCatching {
        executor = ExecutorFactory.create(preferShizuku)
    }

    suspend fun isReady() = executor?.isAvailable() == true

    suspend fun applyProfile(profile: Profile): OptimizationResult {
        if (executor == null) return OptimizationResult(false, "Executor não inicializado", profile)
        return try {
            val result = perfMan?.applyProfile(profile)
                ?: return OptimizationResult(false, "PerformanceManager indisponível", profile)
            if (result.isSuccess) OptimizationResult(true, "Perfil ${profile.displayName} aplicado", profile)
            else OptimizationResult(false, result.exceptionOrNull()?.message ?: "Erro desconhecido", profile)
        } catch (e: Exception) {
            OptimizationResult(false, e.message ?: "Erro desconhecido", profile)
        }
    }

    suspend fun collectStatus(): SystemStatus {
        if (executor == null) return SystemStatus()
        return try {
            val e = executor!!
            val cpuInfo = e.execute("grep -c ^processor /proc/cpuinfo 2>/dev/null || echo 0")
                .getOrDefault("0").trim().toIntOrNull() ?: 0
            val cpuUsage = e.execute(
                "top -b -n 1 2>/dev/null | grep -m1 \"CPU\" | awk '{print \$2}' | tr -d '%' || echo 0"
            ).getOrDefault("0").trim().toIntOrNull() ?: 0
            val cpuTemp = thermMan?.getCpuTemp()?.getOrDefault(0) ?: 0
            val batteryLevel = thermMan?.getBatteryLevel()?.getOrDefault(0) ?: 0
            val batteryStatus = thermMan?.getBatteryStatus()?.getOrDefault("Unknown") ?: "Unknown"
            val ramInfo = ramMan?.getRamInfo()?.getOrNull()
            val ramUsage = ramInfo?.third ?: 0
            val ramTotal = ramInfo?.first ?: 0L
            val ramFree = ramInfo?.second ?: 0L
            val conflicts = conflictDetector?.checkConflicts()?.getOrNull()
            val chipset = conflictDetector?.getChipset()?.getOrDefault("Generic") ?: "Generic"
            val governor = e.execute(
                "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor 2>/dev/null || echo N/D"
            ).getOrDefault("N/D").trim()
            val gpuInfo = perfMan?.getGpuInfo()?.getOrDefault("0 MHz | N/D") ?: "0 MHz | N/D"
            val gpuFreq = gpuInfo.substringBefore(" MHz").trim().toIntOrNull() ?: 0
            val gpuGov = gpuInfo.substringAfter("| ").trim()

            SystemStatus(
                cpuUsage = cpuUsage,
                cpuTemp = cpuTemp,
                ramTotal = ramTotal,
                ramFree = ramFree,
                ramUsagePercent = ramUsage,
                batteryLevel = batteryLevel,
                batteryStatus = batteryStatus,
                governor = governor,
                gpuFreq = gpuFreq,
                gpuGovernor = gpuGov,
                conflictsDetected = conflicts != null && (conflicts.conflictingModules.isNotEmpty() || conflicts.suspiciousGovernors.isNotEmpty()),
                chipset = chipset,
                coreCount = cpuInfo
            )
        } catch (e: Exception) {
            SystemStatus()
        }
    }

    suspend fun checkConflicts(): ConflictInfo =
        conflictDetector?.checkConflicts()?.getOrElse { ConflictInfo() } ?: ConflictInfo()

    suspend fun resolveConflicts(): Result<String> =
        conflictDetector?.resolveConflicts() ?: Result.failure(RuntimeException("ConflictDetector indisponível"))

    suspend fun cleanRam(): Result<String> =
        ramMan?.cleanRam() ?: Result.failure(RuntimeException("RAMManager indisponível"))

    suspend fun getExecutor(): ShellExecutor? = executor

    fun cleanup() {
        executor = null
    }
}
