package io.github.saeeddev94.pixelnr

import android.content.Context
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

data class ImsRegistration(
    val state: State,
    val voice: Boolean = false,
    val video: Boolean = false,
    val sms: Boolean = false,
) {
    enum class State { NOT_REGISTERED, REGISTERING, REGISTERED, UNKNOWN }
}

class ImsStatus(
    private val context: Context,
    private val root: Root,
) {

    @Throws(Exception::class)
    fun getRegistrations(): Map<Int, ImsRegistration> {
        val result = root.exec(
            "dumpsys activity service $DEBUG_SERVICE | " +
                    "grep -E 'mPhoneId=|mImsMmTelRegistrationState|mMmTelCapabilities='"
        )
        if (!result.isSuccess) throw Exception(context.getString(R.string.cmdFailed))

        val states = mutableMapOf<Int, ImsRegistration.State>()
        val capabilities = mutableMapOf<Int, String>()
        var phoneId: Int? = null
        result.out.forEach { line ->
            val text = line.trim()
            when {
                text.startsWith("mPhoneId=") ->
                    phoneId = text.removePrefix("mPhoneId=").toIntOrNull()
                text.startsWith("mImsMmTelRegistrationState") -> {
                    val id = phoneId ?: return@forEach
                    states[id] = when (text.substringAfter("=").trim()) {
                        "0" -> ImsRegistration.State.NOT_REGISTERED
                        "1" -> ImsRegistration.State.REGISTERING
                        "2" -> ImsRegistration.State.REGISTERED
                        else -> ImsRegistration.State.UNKNOWN
                    }
                }
                text.startsWith("mMmTelCapabilities=") -> {
                    val id = phoneId ?: return@forEach
                    capabilities[id] = text
                }
            }
        }
        return states.mapValues { (id, state) ->
            val caps = capabilities[id].orEmpty()
            ImsRegistration(
                state = state,
                voice = caps.contains("Voice: true"),
                video = caps.contains("Video: true"),
                sms = caps.contains("SMS: true"),
            )
        }
    }

    @Throws(Exception::class)
    suspend fun restartPhoneService() {
        val oldPid = phonePid() ?: throw Exception(context.getString(R.string.cmdFailed))
        if (!root.exec("kill $oldPid").isSuccess) throw Exception(context.getString(R.string.cmdFailed))
        repeat(RESTART_POLLS) {
            delay(POLL_INTERVAL)
            val pid = phonePid()
            if (pid != null && pid != oldPid) return
        }
        throw Exception(context.getString(R.string.phoneRestartTimeout))
    }

    private fun phonePid(): String? =
        root.exec("pidof $PHONE_PROCESS").out.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }

    companion object {
        private const val PHONE_PROCESS = "com.android.phone"
        private val POLL_INTERVAL = 500.milliseconds
        private const val RESTART_POLLS = 40
        private const val DEBUG_SERVICE = "com.android.phone/.TelephonyDebugService"
    }
}
