package dev.llucs.speedcool.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object KernelFs {

    suspend fun exists(path: String): Boolean = withContext(Dispatchers.IO) { File(path).exists() }

    suspend fun canRead(path: String): Boolean = withContext(Dispatchers.IO) { File(path).canRead() }

    suspend fun canWrite(path: String): Boolean = withContext(Dispatchers.IO) { File(path).canWrite() }

    suspend fun readText(path: String): String? = withContext(Dispatchers.IO) {
        runCatching { File(path).readText().trim() }.getOrNull()
    }

    suspend fun readLong(path: String): Long? = readText(path)?.toLongOrNull()

    suspend fun writeText(path: String, value: String): Boolean {
        val escaped = value.replace("\", "\\").replace(""", "\"")
        val cmd = "echo "$escaped" > "$path""
        val res = RootShell.exec(cmd)
        return res.ok
    }

    suspend fun chmod(path: String, mode: String): Boolean {
        val res = RootShell.exec("chmod $mode "$path"")
        return res.ok
    }
}
