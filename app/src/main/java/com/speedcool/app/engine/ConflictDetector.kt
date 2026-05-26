package com.speedcool.app.engine

import com.speedcool.app.data.ConflictInfo
import com.speedcool.app.root.ShellExecutor

class ConflictDetector(private val executor: ShellExecutor) {

    companion object {
        private val KNOWN_CONFLICTING = listOf(
            "lspeed", "magnetar", "rickthermal", "frigus_thermal", "godspeed",
            "fde.ai", "aionix", "lkt", "nfs", "ktweak", "xtreme"
        )
        private val SAFE_GOVERNORS = setOf("schedutil", "performance", "powersave", "interactive", "ondemand")
    }

    suspend fun checkConflicts(): Result<ConflictInfo> {
        val modulesDir = "/data/adb/modules"
        val ksuDir = "/data/adb/ksu/modules"

        val conflictingModules = mutableListOf<String>()
        val suspiciousGovernors = mutableListOf<String>()
        val thermalZonesOff = mutableListOf<String>()

        val moduleCheck = executor.execute(
            "ls $modulesDir 2>/dev/null; ls $ksuDir 2>/dev/null"
        ).getOrElse("")

        for (conflict in KNOWN_CONFLICTING) {
            if (moduleCheck.contains(conflict, ignoreCase = true)) {
                conflictingModules.add(conflict)
            }
        }

        val govCheck = executor.execute(
            "for cpu in /sys/devices/system/cpu/cpu[0-9]*/cpufreq/scaling_governor; do [ -f \"\$cpu\" ] && cat \"\$cpu\"; done"
        ).getOrElse("")

        for (gov in govCheck.lines().filter { it.isNotBlank() }) {
            if (gov !in SAFE_GOVERNORS) suspiciousGovernors.add(gov)
        }

        val thermalCheck = executor.execute(
            "for t in /sys/class/thermal/thermal_zone*/mode; do [ -f \"\$t\" ] && echo \"\$(cat \"\$t\"):\$t\"; done"
        ).getOrElse("")

        for (line in thermalCheck.lines().filter { it.isNotBlank() }) {
            if (line.startsWith("disabled")) thermalZonesOff.add(line.substringAfter(":"))
        }

        return Result.success(ConflictInfo(conflictingModules, suspiciousGovernors, thermalZonesOff))
    }

    suspend fun resolveConflicts(): Result<String> {
        val cmds = mutableListOf<String>()

        val defaultGov = "schedutil"
        for (cpu in 0..7) {
            cmds.add("test -w /sys/devices/system/cpu/cpu$cpu/cpufreq/scaling_governor && echo $defaultGov > /sys/devices/system/cpu/cpu$cpu/cpufreq/scaling_governor")
        }

        cmds.add("for t in /sys/class/thermal/thermal_zone*/mode; do [ -w \"\$t\" ] && echo enabled > \"\$t\"; done")

        return executor.executeBatch(cmds).map { "Conflitos resolvidos" }
    }

    suspend fun getChipset(): Result<String> = executor.execute(
        "platform=\$(getprop ro.board.platform); hardware=\$(getprop ro.hardware); " +
        "case \"\$platform\" in msm*|sdm*|sm*) echo Qualcomm;; mt*) echo MediaTek;; exynos*) echo Exynos;; kirin*) echo Kirin;; tensor*) echo Tensor;; " +
        "*) case \"\$hardware\" in qcom*) echo Qualcomm;; mtk*) echo MediaTek;; samsung*) echo Exynos;; *) echo Generic;; esac;; esac"
    )
}
