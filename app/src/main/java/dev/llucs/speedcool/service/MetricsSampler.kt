package dev.llucs.speedcool.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class Metrics(
    val cpuUsagePct: Int,
    val ramUsagePct: Int
)

class MetricsSampler {
    private var lastTotal: Long? = null
    private var lastIdle: Long? = null

    suspend fun sample(): Metrics = withContext(Dispatchers.IO) {
        val (cpuPct) = readCpuUsage()
        val ramPct = readRamUsagePct()
        Metrics(cpuPct, ramPct)
    }

    private fun readCpuUsage(): Pair<Int, Boolean> {
        val line = runCatching { File("/proc/stat").readLines().firstOrNull { it.startsWith("cpu ") } }.getOrNull()
            ?: return 0 to false
        val parts = line.trim().split(Regex("\s+")).drop(1).mapNotNull { it.toLongOrNull() }
        if (parts.size < 4) return 0 to false
        val user = parts.getOrNull(0) ?: 0
        val nice = parts.getOrNull(1) ?: 0
        val system = parts.getOrNull(2) ?: 0
        val idle = parts.getOrNull(3) ?: 0
        val iowait = parts.getOrNull(4) ?: 0
        val irq = parts.getOrNull(5) ?: 0
        val softirq = parts.getOrNull(6) ?: 0
        val steal = parts.getOrNull(7) ?: 0

        val idleAll = idle + iowait
        val nonIdle = user + nice + system + irq + softirq + steal
        val total = idleAll + nonIdle

        val lt = lastTotal
        val li = lastIdle
        lastTotal = total
        lastIdle = idleAll

        if (lt == null || li == null) return 0 to false

        val totald = total - lt
        val idled = idleAll - li
        if (totald <= 0) return 0 to false
        val usage = ((totald - idled).toDouble() / totald.toDouble() * 100.0).toInt().coerceIn(0, 100)
        return usage to true
    }

    private fun readRamUsagePct(): Int {
        val meminfo = runCatching { File("/proc/meminfo").readLines() }.getOrElse { emptyList() }
        fun getKb(key: String): Long? {
            val line = meminfo.firstOrNull { it.startsWith(key) } ?: return null
            return line.split(Regex("\s+")).getOrNull(1)?.toLongOrNull()
        }
        val total = getKb("MemTotal:") ?: return 0
        val avail = getKb("MemAvailable:") ?: return 0
        if (total <= 0) return 0
        val used = (total - avail).coerceAtLeast(0)
        return ((used.toDouble() / total.toDouble()) * 100.0).toInt().coerceIn(0, 100)
    }
}
