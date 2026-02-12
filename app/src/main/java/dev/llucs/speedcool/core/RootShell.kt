    package dev.llucs.speedcool.core

    import com.topjohnwu.superuser.Shell
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.withContext

    object RootShell {
        init {
            Shell.enableVerboseLogging = false
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_REDIRECT_STDERR)
                    .setTimeout(10)
            )
        }

        suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
            Shell.getShell().isRoot
        }

        suspend fun exec(cmd: String): ExecResult = withContext(Dispatchers.IO) {
            val out = mutableListOf<String>()
            val err = mutableListOf<String>()
            val result = Shell.cmd(cmd).to(out, err).exec()
            ExecResult(
                code = result.code,
                out = out,
                err = err
            )
        }

        suspend fun exec(vararg cmd: String): ExecResult = exec(cmd.joinToString(" && "))

        data class ExecResult(
            val code: Int,
            val out: List<String>,
            val err: List<String>
        ) {
            val ok: Boolean get() = code == 0
            val outputText: String get() = (out + err).joinToString("
")
        }
    }
