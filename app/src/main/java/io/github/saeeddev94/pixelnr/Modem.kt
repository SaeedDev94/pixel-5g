package io.github.saeeddev94.pixelnr

import android.content.Context
import com.topjohnwu.superuser.Shell

class Modem(val context: Context) : AutoCloseable {

    private val root by lazy { Root(context) }

    @Throws(Exception::class)
    inline fun <reified T> getNv(key: String): T where T : Enum<T>, T : NvEnum {
        val result = runCommand("GETNV=\"$key\"")
        return getOutput(result, key)
    }

    @Throws(Exception::class)
    inline fun <reified T> setNv(key: String, nv: T): T where T : Enum<T>, T : NvEnum {
        val result = runCommand("SETNV=\"$key\",0,\"${nv.value}\"")
        return getOutput(result, key)
    }

    @Throws(Exception::class)
    inline fun <reified T> getOutput(
        result: Shell.Result,
        key: String
    ): T where T : Enum<T>, T : NvEnum {
        val output = result.out.joinToString("\n").trim()
        if (!result.isSuccess) throw Exception(context.getString(R.string.cmdFailed))
        if (!output.contains("OK")) throw Exception(context.getString(R.string.resNoOk))
        val regex = "\"$key\",0,\"(.*)\"".toRegex()
        val matches = regex.find(output) ?:
        throw Exception(context.getString(R.string.resRegexpIssue))
        val group = matches.groups[1] ?:
        throw Exception(context.getString(R.string.resMatchIssue))
        return enumValues<T>().find { it.value == group.value } ?:
        throw Exception(context.getString(R.string.resInvalidCase))
    }

    @Throws(Exception::class)
    fun runCommand(cmd: String): Shell.Result {
        val device = "/dev/umts_router"
        val command = "echo 'AT+GOOG$cmd\\r' > $device & cat $device"
        return root.exec(command)
    }

    @Throws(Exception::class)
    fun getIccIds(): Map<Int, String> {
        val result = root.exec(
            "content query --uri content://telephony/siminfo " +
                    "--projection _id:icc_id --where \"sim_id>=0\""
        )
        if (!result.isSuccess) {
            throw Exception("getIccIds: command failed: ${result.err.joinToString("\n")}")
        }
        val regex = "_id=(\\d+), icc_id=(\\w+)".toRegex()
        return result.out.mapNotNull { line ->
            val match = regex.find(line) ?: return@mapNotNull null
            match.groupValues[1].toInt() to match.groupValues[2]
        }.toMap()
    }

    override fun close() = root.close()
}
