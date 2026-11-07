package io.github.saeeddev94.pixelnr

import android.content.Context
import android.util.Log
import java.io.File

class CarrierConfig(
    private val context: Context,
    private val root: Root,
) {

    @Throws(Exception::class)
    fun read(iccId: String, carrierId: Int): Map<ImsOption, Boolean> {
        val content = readFile(path(iccId, carrierId))
        return ImsOption.entries.associateWith { option ->
            "<boolean name=\"${option.key}\" value=\"(true|false)\" />".toRegex()
                .findAll(content)
                .lastOrNull()
                ?.groupValues?.get(1)
                ?.toBooleanStrict()
                ?: option.androidDefault
        }
    }

    @Throws(Exception::class)
    fun write(iccId: String, carrierId: Int, values: Map<ImsOption, Boolean>) {
        val path = path(iccId, carrierId)
        val content = readFile(path).replace(BLOCK_REGEX, "")
        val end = content.lastIndexOf(BUNDLE_END)
        if (end == -1) throw Exception(context.getString(R.string.imsInvalidConfig))
        val block = buildBlock(values)
        val newContent = content.substring(0, end) + block + content.substring(end)

        val tmp = File(context.cacheDir, "carrierconfig.xml")
        try {
            tmp.writeText(newContent.trimEnd() + "\n")
            val result = root.exec(
                "{ [ -e '$path.bak' ] || cp -p '$path' '$path.bak'; } && " +
                        "cat '${tmp.absolutePath}' > '$path'"
            )
            if (!result.isSuccess) throw Exception(context.getString(R.string.cmdFailed))
        } finally {
            tmp.delete()
        }
    }

    fun isPixelImsModuleActive(): Boolean = runCatching {
        val dir = "/data/adb/modules/pixel_ims_module"
        root.exec("[ -d $dir ] && [ ! -e $dir/disable ] && [ ! -e $dir/remove ]").isSuccess
    }.getOrDefault(false)

    private fun readFile(path: String): String {
        val result = root.exec("cat '$path'")
        if (!result.isSuccess) {
            Log.e(TAG, "readFile: code=${result.code} err=${result.err.joinToString("\n")}")
            throw Exception(context.getString(R.string.imsConfigNotFound))
        }
        return result.out.joinToString("\n")
    }

    private fun buildBlock(values: Map<ImsOption, Boolean>): String = buildString {
        appendLine(BLOCK_START)
        ImsOption.entries.forEach { option ->
            val value = values[option] ?: return@forEach
            appendLine("<boolean name=\"${option.key}\" value=\"$value\" />")
        }
        appendLine(BLOCK_END)
    }

    private fun path(iccId: String, carrierId: Int) =
        "$DIR/carrierconfig-com.google.android.carrier-$iccId-$carrierId.xml"

    companion object {
        private const val TAG = "CarrierConfig"
        private const val DIR = "/data/user_de/0/com.android.phone/files"
        private const val BUNDLE_END = "</bundle>"
        private const val BLOCK_START = "<!--PIXEL_5G_START-->"
        private const val BLOCK_END = "<!--PIXEL_5G_END-->"
        private val BLOCK_REGEX = "$BLOCK_START.*?$BLOCK_END\\n?".toRegex(RegexOption.DOT_MATCHES_ALL)
    }
}
