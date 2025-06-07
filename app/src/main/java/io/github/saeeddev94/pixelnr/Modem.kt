package io.github.saeeddev94.pixelnr

import android.content.Context
import com.topjohnwu.superuser.Shell

class Modem(private val context: Context) {

    @Throws(Exception::class)
    fun <T> getNv(key: String, nv: NvEnum<T>): T {
        val result = runAtCommand("GETNV=\"$key\"")
        return getOutput(result, key, nv)
    }

    @Throws(Exception::class)
    fun <T> setNv(key: String, nv: NvEnum<T>): T {
        val result = runAtCommand("SETNV=\"$key\",0,\"${nv.value}\"")
        return getOutput(result, key, nv)
    }

    @Throws(Exception::class)
    private fun <T> getOutput(result: Shell.Result, key: String, nv: NvEnum<T>): T {
        val output = result.out.joinToString("\n").trim()
        if (!result.isSuccess) throw Exception(context.getString(R.string.cmdFailed))
        if (!output.contains("OK")) throw Exception(context.getString(R.string.resNoOk))
        val regex = "\"$key\",0,\"(.*)\"".toRegex()
        val matches = regex.find(output) ?:
        throw Exception(context.getString(R.string.resRegexpIssue))
        val group = matches.groups[1] ?:
        throw Exception(context.getString(R.string.resMatchIssue))
        return nv.fromValue(group.value) ?:
        throw Exception(context.getString(R.string.resInvalidCase))
    }

    companion object {
        private fun runAtCommand(cmd: String): Shell.Result {
            val device = "/dev/umts_router"
            val command = "echo 'AT+GOOG$cmd\\r' > $device & cat $device"
            return Shell.cmd(command).exec()
        }
    }
}
