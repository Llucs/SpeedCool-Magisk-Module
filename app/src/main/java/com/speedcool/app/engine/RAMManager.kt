package com.speedcool.app.engine

import com.speedcool.app.root.ShellExecutor

class RAMManager(private val executor: ShellExecutor) {

    suspend fun getRamInfo(): Result<Triple<Long, Long, Int>> = executor.execute(
        "awk '/MemTotal/ {total=\$2/1024} /MemAvailable/ {avail=\$2/1024} END {printf \"%.0f %.0f\", total, total-avail}' /proc/meminfo"
    ).map { output ->
        val parts = output.trim().split(" ")
        val total = parts.getOrElse(0) { "0" }.toLongOrNull() ?: 0
        val used = parts.getOrElse(1) { "0" }.toLongOrNull() ?: 0
        val pct = if (total > 0) ((used * 100) / total).toInt() else 0
        Triple(total, total - used, pct)
    }

    suspend fun cleanRam(): Result<String> = executor.execute("sync; echo 3 > /proc/sys/vm/drop_caches")
        .map { "RAM limpa com sucesso" }

    suspend fun getZramInfo(): Result<String> = executor.execute(
        "cat /proc/swaps 2>/dev/null; echo '---'; cat /sys/block/zram0/mm_stat 2>/dev/null || cat /sys/block/zram0/stat 2>/dev/null"
    )

    suspend fun setSwappiness(value: Int): Result<String> =
        executor.execute("test -w /proc/sys/vm/swappiness && echo $value > /proc/sys/vm/swappiness")
            .map { "Swappiness set to $value" }
}
