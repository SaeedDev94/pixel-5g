package io.github.saeeddev94.pixelnr

import android.content.Context

class SimCard(context: Context) : AutoCloseable {

    private val root by lazy { Root(context) }

    @Throws(Exception::class)
    fun iccIds(): Map<Int, String> {
        val result = root.exec(
            "content query --uri content://telephony/siminfo " +
                    "--projection _id:icc_id --where \"sim_id>=0\""
        )
        if (!result.isSuccess) {
            throw Exception("iccIds: command failed: ${result.err.joinToString("\n")}")
        }
        val regex = "_id=(\\d+), icc_id=(\\w+)".toRegex()
        return result.out.mapNotNull { line ->
            val match = regex.find(line) ?: return@mapNotNull null
            match.groupValues[1].toInt() to match.groupValues[2]
        }.toMap()
    }

    override fun close() = root.close()
}
