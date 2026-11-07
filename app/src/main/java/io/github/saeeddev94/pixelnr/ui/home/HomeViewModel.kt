package io.github.saeeddev94.pixelnr.ui.home

import android.Manifest
import android.app.Application
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.saeeddev94.pixelnr.Modem
import io.github.saeeddev94.pixelnr.NrManual
import io.github.saeeddev94.pixelnr.NrMode
import io.github.saeeddev94.pixelnr.extensions.hasPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val context by lazy { application.applicationContext }
    private val subscriptionManager by lazy {
        context.getSystemService(SubscriptionManager::class.java)
    }

    private val mutex = Mutex()
    private val modem = Modem(context).also {
        addCloseable(it)
    }

    var sims by mutableStateOf<List<Sim>?>(null)
        private set

    init {
        resolveActiveSims {
            this@HomeViewModel.sims?.forEach { sim ->
                resolveNrMode(sim)
            }
        }
    }

    fun changeNrMode(sim: Sim, value: NrMode) {
        viewModelScope.launch(Dispatchers.IO) {
            mutex.withLock {
                run loop@ {
                    withContext(Dispatchers.Main) {
                        updateSim(
                            sim,
                            SimState.Loading,
                        )
                    }
                    repeat(MAX_RETRIES) { index ->
                        val isLast = (index + 1) == MAX_RETRIES
                        if (!isActive) return@loop
                        runCatching {
                            modem.setNv(NR_MANUAL, NrManual.ENABLED)
                            when (sim.index) {
                                0 -> modem.setNv(NR_MODE, value)
                                else -> modem.setNv(DS_NR_MODE, value)
                            }
                        }.onSuccess {
                            withContext(Dispatchers.Main) {
                                updateSim(
                                    sim,
                                    SimState.Loaded(
                                        mode = value,
                                        rebootRequired = true,
                                    ),
                                )
                            }
                            return@loop
                        }.onFailure {
                            if (!isLast) return@onFailure
                            withContext(Dispatchers.Main) {
                                updateSim(
                                    sim,
                                    SimState.Failed(
                                        reason = it.message,
                                    ),
                                )
                            }
                            return@loop
                        }
                        delay(TIMEOUT.milliseconds)
                    }
                }
            }
        }
    }

    private fun resolveNrMode(sim: Sim) {
        viewModelScope.launch(Dispatchers.IO) {
            mutex.withLock {
                run loop@ {
                    repeat (MAX_RETRIES) { index ->
                        val isLast = (index + 1) == MAX_RETRIES
                        if (!isActive) return@loop
                        runCatching {
                            when (sim.index) {
                                0 -> NR_MODE
                                else -> DS_NR_MODE
                            }.let { key ->
                                modem.getNv<NrMode>(key)
                            }
                        }.onSuccess {
                            withContext(Dispatchers.Main) {
                                updateSim(
                                    sim,
                                    SimState.Loaded(
                                        mode = it,
                                        rebootRequired = false,
                                    ),
                                )
                            }
                            return@loop
                        }.onFailure {
                            if (!isLast) return@onFailure
                            withContext(Dispatchers.Main) {
                                updateSim(
                                    sim,
                                    SimState.Failed(reason = it.message),
                                )
                            }
                            return@loop
                        }
                        delay(TIMEOUT.milliseconds)
                    }
                }
            }
        }
    }

    private fun resolveActiveSims(done: () -> Unit) {
        if (!context.hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            Log.e(TAG, "resolveActiveSims: Missing READ_PHONE_STATE permission")
            sims = emptyList()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            @Suppress("MissingPermission")
            val sims = runCatching {
                subscriptionManager.activeSubscriptionInfoList
            }.onFailure {
                Log.e(TAG, "activeSubscriptionInfoList: ${it.message}")
            }.getOrNull()
                .orEmpty()
                .sortedBy { it.simSlotIndex }
                .map {
                    Sim(it.simSlotIndex, it.subscriptionId, "${it.displayName}")
                }
            withContext(Dispatchers.Main) {
                this@HomeViewModel.sims = sims
                done()
            }
        }
    }

    private fun updateSim(sim: Sim, state: SimState) {
        val list = sims?.toMutableList() ?: return
        val index = list.indexOfFirst { sim.id == it.id }
        if (index == -1) return
        list[index] = list[index].copy(state = state)
        sims = list.toList()
    }

    companion object {
        private const val MAX_RETRIES = 30
        private const val TIMEOUT = 1000
        private const val NR_MANUAL = "NR.MANUAL.MODE.ENABLE"
        private const val NR_MODE = "NR.CONFIG.MODE"
        private const val DS_NR_MODE = "DS.NR.CONFIG.MODE"
        private const val TAG = "HomeViewModel"
    }
}
