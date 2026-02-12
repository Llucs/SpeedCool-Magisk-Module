package dev.llucs.speedcool.core

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SpeedCoolLogger(private val context: Context) {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val logFile: File by lazy { File(context.filesDir, "speedcool.log") }

    suspend fun log(tag: String, msg: String) = withContext(Dispatchers.IO) {
        val line = "[${dateFmt.format(Date())}][$tag] $msg\n"
        runCatching { logFile.appendText(line) }
    }

    suspend fun readLastLines(maxLines: Int = 400): String = withContext(Dispatchers.IO) {
        if (!logFile.exists()) return@withContext ""
        val lines = runCatching { logFile.readLines() }.getOrElse { emptyList() }
        lines.takeLast(maxLines).joinToString("\n")
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        runCatching { if (logFile.exists()) logFile.writeText("") }
    }

    fun logPath(): String = logFile.absolutePath
}
