package com.speedcool.app.root

import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

interface ShellExecutor {
    suspend fun execute(command: String): Result<String>
    suspend fun executeBatch(commands: List<String>): Result<List<String>>
    suspend fun isAvailable(): Boolean
    val mode: String
}

class RootExecutor : ShellExecutor {
    override val mode = "root"

    override suspend fun execute(command: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val output = process.inputStream.bufferedReader().readText().trim()
            val error = process.errorStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            if (exitCode == 0) Result.success(output)
            else Result.failure(RuntimeException("Exit $exitCode: $error"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun executeBatch(commands: List<String>): Result<List<String>> =
        withContext(Dispatchers.IO) {
            val results = mutableListOf<String>()
            try {
                val process = Runtime.getRuntime().exec("su")
                val writer = process.outputStream.bufferedWriter()
                val reader = process.inputStream.bufferedReader()
                for (cmd in commands) {
                    writer.write("$cmd\n")
                    writer.flush()
                    results.add(reader.readLine() ?: "")
                }
                writer.write("exit\n")
                writer.flush()
                process.waitFor()
                Result.success(results)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val output = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            output.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }
}

class ShizukuExecutor : ShellExecutor {
    override val mode = "shizuku"

    override suspend fun execute(command: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!Shizuku.pingBinder()) {
                return@withContext Result.failure(RuntimeException("Shizuku not running"))
            }
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val output = process.inputStream.bufferedReader().readText().trim()
            val error = process.errorStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            if (exitCode == 0) Result.success(output)
            else Result.failure(RuntimeException("Exit $exitCode: $error"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun executeBatch(commands: List<String>): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                if (!Shizuku.pingBinder()) {
                    return@withContext Result.failure(RuntimeException("Shizuku not running"))
                }
                val process = Shizuku.newProcess(arrayOf("sh"), null, null)
                val writer = process.outputStream.bufferedWriter()
                val reader = process.inputStream.bufferedReader()
                val results = mutableListOf<String>()
                for (cmd in commands) {
                    writer.write("$cmd\n")
                    writer.flush()
                    results.add(reader.readLine() ?: "")
                }
                writer.write("exit\n")
                writer.flush()
                process.waitFor()
                Result.success(results)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Shizuku.pingBinder() && Shizuku.getVersion() >= 13
            } else {
                Shizuku.pingBinder()
            }
        } catch (e: Exception) {
            false
        }
    }
}

class ExecutorFactory {
    companion object {
        suspend fun create(preferShizuku: Boolean = false): ShellExecutor {
            val shizuku = ShizukuExecutor()
            if (preferShizuku && shizuku.isAvailable()) return shizuku
            val root = RootExecutor()
            if (root.isAvailable()) return root
            if (shizuku.isAvailable()) return shizuku
            throw RuntimeException("Nenhum método de execução disponível")
        }

        suspend fun checkAvailability(): Triple<Boolean, Boolean, Boolean> {
            val root = RootExecutor().isAvailable()
            val shizuku = ShizukuExecutor().isAvailable()
            return Triple(root, shizuku, root || shizuku)
        }
    }
}
