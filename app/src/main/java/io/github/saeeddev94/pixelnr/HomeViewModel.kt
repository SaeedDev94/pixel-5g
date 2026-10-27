package io.github.saeeddev94.pixelnr

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val modem by lazy { Modem(application) }

    var nrMode by mutableStateOf<NrMode?>(null)
        private set

    fun resolveNrMode() = viewModelScope.launch(Dispatchers.IO) {
        run loop@ {
            repeat (MAX_RETRIES) {
                if (!isActive) return@loop
                runCatching {
                    modem.getNv<NrMode>(DS_NR_MODE)
                }.onSuccess {
                    withContext(Dispatchers.Main) {
                        nrMode = it
                    }
                    return@loop
                }
                delay(TIMEOUT.milliseconds)
            }
        }
    }

    fun changeNrMode(
        value: NrMode,
        cb: (err: Throwable?) -> Unit,
    ) = viewModelScope.launch(Dispatchers.IO) {
        run loop@ {
            repeat(MAX_RETRIES) { index ->
                val isLast = (index + 1) == MAX_RETRIES
                if (!isActive) {
                    withContext(Dispatchers.Main) {
                        cb(Exception("Dead Job"))
                    }
                    return@loop
                }
                runCatching {
                    modem.setNv(NR_MANUAL, NrManual.ENABLED)
                    modem.setNv(NR_MODE, value)
                    modem.setNv(DS_NR_MODE, value)
                }.onSuccess {
                    withContext(Dispatchers.Main) {
                        nrMode = value
                        cb(null)
                    }
                    return@loop
                }.onFailure {
                    if (!isLast) return@onFailure
                    withContext(Dispatchers.Main) {
                        cb(it)
                    }
                    return@loop
                }
                delay(TIMEOUT.milliseconds)
            }
        }
    }

    companion object {
        private const val MAX_RETRIES = 30
        private const val TIMEOUT = 1000
        private const val NR_MANUAL = "NR.MANUAL.MODE.ENABLE"
        private const val NR_MODE = "NR.CONFIG.MODE"
        private const val DS_NR_MODE = "DS.NR.CONFIG.MODE"
    }
}
