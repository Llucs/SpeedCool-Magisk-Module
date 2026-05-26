package com.speedcool.app.data

enum class Profile(val id: String, val displayName: String) {
    ECO("eco", "Eco"),
    BALANCED("balanced", "Equilibrado"),
    PERFORMANCE("performance", "Performance"),
    LEARNING("learning", "Aprendizado");

    companion object {
        fun fromId(id: String): Profile = entries.find { it.id == id } ?: BALANCED
    }
}

enum class ExecutorMode { ROOT, SHIZUKU, NONE }

data class SystemStatus(
    val cpuUsage: Int = 0,
    val cpuTemp: Int = 0,
    val gpuTemp: Int = 0,
    val ramTotal: Long = 0,
    val ramFree: Long = 0,
    val ramUsagePercent: Int = 0,
    val batteryLevel: Int = 0,
    val batteryStatus: String = "Unknown",
    val activeProfile: Profile = Profile.BALANCED,
    val governor: String = "N/D",
    val gpuFreq: Int = 0,
    val gpuGovernor: String = "N/D",
    val conflictsDetected: Boolean = false,
    val chipset: String = "Generic",
    val ioLatency: Int = 0,
    val coreCount: Int = 0
)

data class ConflictInfo(
    val conflictingModules: List<String> = emptyList(),
    val suspiciousGovernors: List<String> = emptyList(),
    val thermalZonesOff: List<String> = emptyList()
)

data class OptimizationResult(
    val success: Boolean,
    val message: String,
    val profile: Profile
)
