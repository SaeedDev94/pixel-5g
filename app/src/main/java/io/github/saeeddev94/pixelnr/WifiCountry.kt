package io.github.saeeddev94.pixelnr

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.edit

enum class WifiCountryCode(@param:StringRes val title: Int) {
    US(R.string.countryUs),
    GB(R.string.countryGb),
    AU(R.string.countryAu),
}

class WifiCountry(
    private val context: Context,
    private val root: Root,
) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var savedCode: WifiCountryCode?
        get() = prefs.getString(KEY_CODE, null)?.let { code ->
            WifiCountryCode.entries.find { it.name == code }
        }
        private set(value) = prefs.edit { putString(KEY_CODE, value?.name) }

    var applyOnBoot: Boolean
        get() = prefs.getBoolean(KEY_APPLY_ON_BOOT, false)
        set(value) = prefs.edit { putBoolean(KEY_APPLY_ON_BOOT, value) }

    @Throws(Exception::class)
    fun getCurrent(): String? {
        val result = root.exec("cmd wifi get-country-code")
        if (!result.isSuccess) throw Exception(context.getString(R.string.cmdFailed))
        return Regex("""=\s*([A-Za-z]{2})\b""")
            .find(result.out.joinToString("\n"))
            ?.groupValues?.get(1)
            ?.uppercase()
    }

    @Throws(Exception::class)
    fun set(code: WifiCountryCode) {
        val result = root.exec("cmd wifi force-country-code enabled ${code.name}")
        if (!result.isSuccess) throw Exception(context.getString(R.string.cmdFailed))
        savedCode = code
    }

    private companion object {
        const val PREFS = "wifiCountry"
        const val KEY_CODE = "code"
        const val KEY_APPLY_ON_BOOT = "applyOnBoot"
    }
}
