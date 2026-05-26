package com.speedcool.app.engine

import com.speedcool.app.data.Profile
import com.speedcool.app.root.ShellExecutor

class PerformanceManager(private val executor: ShellExecutor) {

    suspend fun setCpuGovernor(governor: String): Result<String> =
        executor.executeBatch(
            (0..7).map { "echo $governor > /sys/devices/system/cpu/cpu$it/cpufreq/scaling_governor" }
        ).map { "Governor set to $governor" }

    suspend fun setCpuFreqLimit(percent: Int): Result<String> {
        val cmds = mutableListOf<String>()
        for (cpu in 0..7) {
            cmds.add("test -f /sys/devices/system/cpu/cpu$cpu/cpufreq/cpuinfo_max_freq || exit 0")
            if (percent == 0 || percent == 100) {
                cmds.add("cat /sys/devices/system/cpu/cpu$cpu/cpufreq/cpuinfo_max_freq > /sys/devices/system/cpu/cpu$cpu/cpufreq/scaling_max_freq")
            } else {
                cmds.add("val=\$(cat /sys/devices/system/cpu/cpu$cpu/cpufreq/cpuinfo_max_freq)")
                cmds.add("echo \$((val * percent / 100)) > /sys/devices/system/cpu/cpu$cpu/cpufreq/scaling_max_freq")
            }
        }
        return executor.executeBatch(cmds).map { "CPU freq limited to $percent%" }
    }

    suspend fun setIOScheduler(scheduler: String): Result<String> =
        executor.execute("for blk in /sys/block/*; do test -w \"\$blk/queue/scheduler\" && grep -q \"$scheduler\" \"\$blk/queue/scheduler\" && echo \"$scheduler\" > \"\$blk/queue/scheduler\"; done")
            .map { "I/O scheduler set to $scheduler" }

    suspend fun optimizeGpu(mode: String): Result<String> {
        val gov = when (mode) {
            "eco" -> "powersave"
            "performance" -> "performance"
            else -> "simple_ondemand"
        }
        return executor.execute("echo $gov > /sys/class/kgsl/kgsl-3d0/devfreq/governor 2>/dev/null; echo $gov > /sys/class/devfreq/*gpu*/governor 2>/dev/null")
            .map { "GPU governor set to $gov" }
    }

    suspend fun applyProfile(profile: Profile): Result<String> {
        val results = mutableListOf<String>()
        when (profile) {
            Profile.ECO -> {
                setCpuGovernor("powersave").onSuccess { results.add(it) }
                setCpuFreqLimit(70).onSuccess { results.add(it) }
                setIOScheduler("bfq").onSuccess { results.add(it) }
                optimizeGpu("eco").onSuccess { results.add(it) }
            }
            Profile.PERFORMANCE -> {
                setCpuGovernor("performance").onSuccess { results.add(it) }
                setCpuFreqLimit(100).onSuccess { results.add(it) }
                setIOScheduler("kyber").onSuccess { results.add(it) }
                optimizeGpu("performance").onSuccess { results.add(it) }
            }
            else -> {
                setCpuGovernor("schedutil").onSuccess { results.add(it) }
                setCpuFreqLimit(100).onSuccess { results.add(it) }
                setIOScheduler("bfq").onSuccess { results.add(it) }
                optimizeGpu("balanced").onSuccess { results.add(it) }
            }
        }
        return if (results.isNotEmpty()) Result.success(results.joinToString("\n"))
        else Result.failure(RuntimeException("Falha ao aplicar perfil"))
    }

    suspend fun getCpuInfo(): Result<String> =
        executor.execute("cat /proc/cpuinfo | grep -c processor && for cpu in /sys/devices/system/cpu/cpu[0-9]*; do idx=\$(basename \"\$cpu\" | cut -c4-); freq=\$(cat \"\$cpu/cpufreq/scaling_cur_freq\" 2>/dev/null || echo 0); gov=\$(cat \"\$cpu/cpufreq/scaling_governor\" 2>/dev/null || echo N/D); echo \"CPU\$idx: \$((freq/1000))MHz (\$gov)\"; done")

    suspend fun getGpuInfo(): Result<String> =
        executor.execute("gov=\$(cat /sys/class/kgsl/kgsl-3d0/devfreq/governor 2>/dev/null); [ -z \"\$gov\" ] && gov=\$(cat /sys/class/devfreq/*gpu*/governor 2>/dev/null | head -n1); freq=\$(cat /sys/class/kgsl/kgsl-3d0/gpuclk 2>/dev/null); [ -z \"\$freq\" ] && freq=\$(cat /sys/class/devfreq/*gpu*/cur_freq 2>/dev/null | head -n1); echo \"\$((freq/1000000)) MHz | \$gov\"")

    suspend fun getAvailableGovernors(): Result<List<String>> =
        executor.execute("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors")
            .map { it.trim().split(" ") }
}
