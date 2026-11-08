package io.github.saeeddev94.pixelnr

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                applyWifiCountry(context.applicationContext)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun applyWifiCountry(context: Context) {
        Root(context).use { root ->
            val wifiCountry = WifiCountry(context, root)
            if (!wifiCountry.applyOnBoot) return
            val code = wifiCountry.savedCode ?: return
            repeat(ATTEMPTS) { attempt ->
                try {
                    wifiCountry.set(code)
                    return
                } catch (e: Exception) {
                    Log.w(TAG, "set wifi country code failed, attempt ${attempt + 1}", e)
                    delay(3.seconds)
                }
            }
        }
    }

    private companion object {
        const val TAG = "BootReceiver"
        const val ATTEMPTS = 5
    }
}
