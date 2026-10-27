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
    private val context by lazy { getApplication<Application>() }

    var mode by mutableStateOf<String?>(null)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    fun resolveNrMode() = viewModelScope.launch(Dispatchers.IO) {
        run loop@ {
            repeat (MAX_RETRIES) { index ->
                val isLast = (index + 1) == MAX_RETRIES
                if (!isActive) return@loop
                runCatching {
                    modem.getNv<NrMode>(DS_NR_MODE)
                }.onSuccess {
                    withContext(Dispatchers.Main) {
                        mode = it.label
                        message = null
                    }
                    return@loop
                }.onFailure {
                    if (!isLast) return@onFailure
                    withContext(Dispatchers.Main) {
                        mode = context.getString(R.string.nrModeError)
                        message = "${it.message}"
                    }
                    return@loop
                }
                delay(TIMEOUT.milliseconds)
            }
        }
    }

    fun changeNrMode(value: NrMode) = viewModelScope.launch(Dispatchers.IO) {
        run loop@ {
            withContext(Dispatchers.Main) {
                mode = null
                message = null
            }
            repeat(MAX_RETRIES) { index ->
                val isLast = (index + 1) == MAX_RETRIES
                if (!isActive) return@loop
                runCatching {
                    modem.setNv(NR_MANUAL, NrManual.ENABLED)
                    modem.setNv(NR_MODE, value)
                    modem.setNv(DS_NR_MODE, value)
                }.onSuccess {
                    withContext(Dispatchers.Main) {
                        mode = value.label
                        message = context.getString(R.string.rebootDevice)
                    }
                    return@loop
                }.onFailure {
                    if (!isLast) return@onFailure
                    withContext(Dispatchers.Main) {
                        mode = context.getString(R.string.nrModeError)
                        message = "${it.message}"
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
