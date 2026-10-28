package io.github.saeeddev94.pixelnr.ui.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.saeeddev94.pixelnr.Modem
import io.github.saeeddev94.pixelnr.NrManual
import io.github.saeeddev94.pixelnr.NrMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val mutex = Mutex()
    private val modem = Modem(application).also {
        addCloseable(it)
    }

    var state by mutableStateOf<HomeUiState>(HomeUiState.Loading)
        private set

    init {
        resolveNrMode()
    }

    fun resolveNrMode() {
        viewModelScope.launch(Dispatchers.IO) {
            mutex.withLock {
                run loop@ {
                    repeat (MAX_RETRIES) { index ->
                        val isLast = (index + 1) == MAX_RETRIES
                        if (!isActive) return@loop
                        runCatching {
                            modem.getNv<NrMode>(DS_NR_MODE)
                        }.onSuccess {
                            withContext(Dispatchers.Main) {
                                state = HomeUiState.Loaded(
                                    mode = it,
                                    rebootRequired = false,
                                )
                            }
                            return@loop
                        }.onFailure {
                            if (!isLast) return@onFailure
                            withContext(Dispatchers.Main) {
                                state = HomeUiState.Failed(reason = it.message)
                            }
                            return@loop
                        }
                        delay(TIMEOUT.milliseconds)
                    }
                }
            }
        }
    }

    fun changeNrMode(value: NrMode) {
        viewModelScope.launch(Dispatchers.IO) {
            mutex.withLock {
                run loop@ {
                    withContext(Dispatchers.Main) {
                        state = HomeUiState.Loading
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
                                state = HomeUiState.Loaded(
                                    mode = value,
                                    rebootRequired = true,
                                )
                            }
                            return@loop
                        }.onFailure {
                            if (!isLast) return@onFailure
                            withContext(Dispatchers.Main) {
                                state = HomeUiState.Failed(reason = it.message)
                            }
                            return@loop
                        }
                        delay(TIMEOUT.milliseconds)
                    }
                }
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
