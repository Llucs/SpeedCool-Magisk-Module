package dev.llucs.speedcool.core

enum class ProfileId(val displayName: String) {
    ECO("Eco"),
    BALANCED("Balanced"),
    PERFORMANCE("Performance"),
    COOLING("Cooling"),
    STEALTH("Stealth"),
    HARDCORE("Hardcore")
}

data class ApplyReport(
    val profile: ProfileId,
    val actions: List<String>,
    val warnings: List<String>
)

class ProfilesEngine(
    private val probe: DeviceProbe
) {

    suspend fun apply(profile: ProfileId, logger: SpeedCoolLogger): ApplyReport {
        val actions = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        val policies = probe.cpuPolicies()
        if (policies.isEmpty()) {
            warnings += "No CPU policies detected."
        }

        suspend fun setGovernorPreferred(preferred: List<String>) {
            for (p in policies) {
                val available = p.availableGovernorsPath?.let { KernelFs.readText(it) }?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()
                val chosen = preferred.firstOrNull { it in available } ?: preferred.firstOrNull()
                if (chosen == null) continue
                val ok = KernelFs.writeText(p.governorPath, chosen)
                if (ok) actions += "CPU governor -> $chosen (${p.policyDir})"
                else warnings += "Failed to set governor $chosen (${p.policyDir})"
            }
        }

        suspend fun setMaxFreqPercent(percent: Double) {
            for (p in policies) {
                val maxPath = p.maxFreqPath ?: continue
                val cpuinfoMax = KernelFs.readLong(p.policyDir + "/cpuinfo_max_freq") ?: KernelFs.readLong(p.policyDir + "/scaling_max_freq")
                if (cpuinfoMax == null) continue
                val target = (cpuinfoMax * percent).toLong().coerceAtLeast(1)
                val ok = KernelFs.writeText(maxPath, target.toString())
                if (ok) actions += "CPU max_freq -> $target (${p.policyDir})"
                else warnings += "Failed to set max_freq (${p.policyDir})"
            }
        }

        suspend fun setMinFreqPercent(percent: Double) {
            for (p in policies) {
                val minPath = p.minFreqPath ?: continue
                val cpuinfoMin = KernelFs.readLong(p.policyDir + "/cpuinfo_min_freq") ?: KernelFs.readLong(p.policyDir + "/scaling_min_freq")
                if (cpuinfoMin == null) continue
                val target = (cpuinfoMin * percent).toLong().coerceAtLeast(1)
                val ok = KernelFs.writeText(minPath, target.toString())
                if (ok) actions += "CPU min_freq -> $target (${p.policyDir})"
                else warnings += "Failed to set min_freq (${p.policyDir})"
            }
        }

        suspend fun dropCaches() {
            val ok = RootShell.exec("sync && echo 3 > /proc/sys/vm/drop_caches").ok
            if (ok) actions += "Drop caches executed"
            else warnings += "Drop caches failed"
        }

        suspend fun tuneVmSwappiness(value: Int) {
            val ok = KernelFs.writeText("/proc/sys/vm/swappiness", value.toString())
            if (ok) actions += "vm.swappiness -> $value"
            else warnings += "vm.swappiness not writable"
        }

        when (profile) {
            ProfileId.ECO -> {
                setGovernorPreferred(listOf("powersave", "schedutil", "conservative"))
                setMaxFreqPercent(0.70)
                setMinFreqPercent(1.00)
                tuneVmSwappiness(80)
            }
            ProfileId.BALANCED -> {
                setGovernorPreferred(listOf("schedutil", "ondemand", "interactive"))
                setMaxFreqPercent(0.90)
                setMinFreqPercent(1.00)
                tuneVmSwappiness(60)
            }
            ProfileId.PERFORMANCE -> {
                setGovernorPreferred(listOf("performance", "schedutil", "ondemand"))
                setMaxFreqPercent(1.00)
                setMinFreqPercent(1.20)
                tuneVmSwappiness(40)
            }
            ProfileId.COOLING -> {
                setGovernorPreferred(listOf("powersave", "schedutil"))
                setMaxFreqPercent(0.60)
                tuneVmSwappiness(70)
                dropCaches()
            }
            ProfileId.STEALTH -> {
                setGovernorPreferred(listOf("schedutil", "ondemand"))
                setMaxFreqPercent(0.80)
                tuneVmSwappiness(65)
            }
            ProfileId.HARDCORE -> {
                setGovernorPreferred(listOf("performance", "schedutil"))
                setMaxFreqPercent(1.00)
                tuneVmSwappiness(30)
                dropCaches()
            }
        }

        logger.log("Profiles", "Applied ${profile.displayName}. Actions=${actions.size} Warnings=${warnings.size}")
        return ApplyReport(profile, actions, warnings)
    }
}
