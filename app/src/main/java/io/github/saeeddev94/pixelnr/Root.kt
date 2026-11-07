package io.github.saeeddev94.pixelnr

import android.content.Context
import com.topjohnwu.superuser.Shell

class Root(
    private val context: Context,
) : AutoCloseable {

    private lateinit var shell: Shell

    fun isRoot(): Boolean = getShell().also {
        shell = it
    }.isRoot

    @Throws(Exception::class)
    fun exec(command: String): Shell.Result {
        if (!isRoot()) throw Exception(context.getString(R.string.rootIsRequired))
        val stdout = ArrayList<String>()
        val stderr = ArrayList<String>()
        return shell.newJob()
            .add(command)
            .to(stdout, stderr)
            .exec()
    }

    private fun getShell(): Shell {
        if (::shell.isInitialized && shell.isRoot) return shell
        close()
        return Shell.Builder.create()
            .setFlags(Shell.FLAG_MOUNT_MASTER)
            .build()
    }

    override fun close() {
        if (::shell.isInitialized) shell.close()
    }
}
