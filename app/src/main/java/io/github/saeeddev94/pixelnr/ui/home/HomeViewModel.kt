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
import io.github.saeeddev94.pixelnr.CarrierConfig
import io.github.saeeddev94.pixelnr.ImsOption
import io.github.saeeddev94.pixelnr.ImsRegistration
import io.github.saeeddev94.pixelnr.ImsStatus
import io.github.saeeddev94.pixelnr.Modem
import io.github.saeeddev94.pixelnr.NrManual
import io.github.saeeddev94.pixelnr.NrMode
import io.github.saeeddev94.pixelnr.R
import io.github.saeeddev94.pixelnr.Root
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

    private val root = Root(context).also {
        addCloseable(it)
    }
    private val mutex = Mutex()
    private val modem = Modem(context, root)
    private val configMutex = Mutex()
    private val carrierConfig = CarrierConfig(context, root)
    private val imsStatus = ImsStatus(context, root)

    var sims by mutableStateOf<List<Sim>?>(null)
        private set

    var imsModuleActive by mutableStateOf(false)
        private set

    var registrationRefreshing by mutableStateOf(false)
        private set

    init {
        resolveActiveSims {
            this@HomeViewModel.sims?.forEach { sim ->
                resolveNrMode(sim)
                resolveIms(sim)
            }
            refreshImsRegistration()
        }
        viewModelScope.launch(Dispatchers.IO) {
            val active = carrierConfig.isPixelImsModuleActive()
            withContext(Dispatchers.Main) {
                imsModuleActive = active
            }
        }
    }

    fun refreshImsRegistration() {
        if (sims.isNullOrEmpty() || registrationRefreshing) return
        registrationRefreshing = true
        viewModelScope.launch(Dispatchers.IO) {
            loadRegistrations()
            withContext(Dispatchers.Main) {
                registrationRefreshing = false
            }
        }
    }

    fun restartPhoneService() {
        if (sims.isNullOrEmpty() || registrationRefreshing) return
        registrationRefreshing = true
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                imsStatus.restartPhoneService()
            }.onFailure {
                Log.e(TAG, "restartPhoneService: ${it.message}")
            }
            for (attempt in 1..REGISTRATION_POLLS) {
                delay(REGISTRATION_POLL_INTERVAL)
                val registrations = loadRegistrations()
                val allRegistered = registrations.isNotEmpty() && registrations.values.all {
                    it.state == ImsRegistration.State.REGISTERED
                }
                if (allRegistered) break
            }
            withContext(Dispatchers.Main) {
                registrationRefreshing = false
            }
        }
    }

    private suspend fun loadRegistrations(): Map<Int, ImsRegistration> {
        val registrations = runCatching {
            imsStatus.getRegistrations()
        }.onFailure {
            Log.e(TAG, "loadRegistrations: ${it.message}")
        }.getOrDefault(emptyMap())
        withContext(Dispatchers.Main) {
            sims?.forEach { sim ->
                val registration = registrations[sim.index]
                    ?: ImsRegistration(ImsRegistration.State.UNKNOWN)
                updateSim(sim) { it.copy(registration = registration) }
            }
        }
        return registrations
    }

    fun setImsOption(sim: Sim, option: ImsOption, value: Boolean) {
        val current = (findSim(sim)?.ims as? ImsState.Loaded)?.values ?: return
        writeIms(sim, current + (option to value))
    }

    private fun resolveIms(sim: Sim) {
        viewModelScope.launch(Dispatchers.IO) {
            configMutex.withLock {
                val state = runCatching {
                    val iccId = sim.iccId ?: throw Exception(context.getString(R.string.imsNoIccId))
                    carrierConfig.read(iccId, sim.carrierId)
                }.fold(
                    onSuccess = { ImsState.Loaded(values = it) },
                    onFailure = {
                        Log.e(TAG, "resolveIms: ${it.message}")
                        ImsState.Failed(reason = it.message)
                    },
                )
                withContext(Dispatchers.Main) {
                    updateIms(sim, state)
                }
            }
        }
    }

    private fun writeIms(sim: Sim, values: Map<ImsOption, Boolean>) {
        updateIms(sim, ImsState.Loaded(values = values))
        viewModelScope.launch(Dispatchers.IO) {
            configMutex.withLock {
                runCatching {
                    val iccId = sim.iccId ?: throw Exception(context.getString(R.string.imsNoIccId))
                    carrierConfig.write(iccId, sim.carrierId, values)
                }.onFailure {
                    Log.e(TAG, "writeIms: ${it.message}")
                    withContext(Dispatchers.Main) {
                        updateIms(sim, ImsState.Failed(reason = it.message))
                    }
                }
            }
        }
    }

    fun reboot() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                if (!root.exec("svc power reboot").isSuccess) {
                    throw Exception(context.getString(R.string.cmdFailed))
                }
            }.onFailure {
                Log.e(TAG, "reboot: ${it.message}")
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
            val iccIds = when (sims.isEmpty()) {
                true -> emptyMap()
                false -> runCatching {
                    modem.getIccIds()
                }.onFailure {
                    Log.e(TAG, "iccIds: ${it.message}")
                }.getOrDefault(emptyMap())
            }
            val list = sims
                .sortedBy { it.simSlotIndex }
                .map {
                    Sim(
                        index = it.simSlotIndex,
                        subscriptionId = it.subscriptionId,
                        iccId = iccIds[it.subscriptionId],
                        carrierId = it.carrierId,
                        name = "${it.displayName}",
                    )
                }
            withContext(Dispatchers.Main) {
                this@HomeViewModel.sims = list
                done()
            }
        }
    }

    private fun findSim(sim: Sim): Sim? =
        sims?.firstOrNull { it.subscriptionId == sim.subscriptionId }

    private fun updateSim(sim: Sim, state: SimState) = updateSim(sim) { it.copy(state = state) }

    private fun updateIms(sim: Sim, ims: ImsState) = updateSim(sim) { it.copy(ims = ims) }

    private fun updateSim(sim: Sim, transform: (Sim) -> Sim) {
        val list = sims?.toMutableList() ?: return
        val index = list.indexOfFirst { sim.subscriptionId == it.subscriptionId }
        if (index == -1) return
        list[index] = transform(list[index])
        sims = list.toList()
    }

    companion object {
        private const val MAX_RETRIES = 30
        private const val TIMEOUT = 1000
        private const val NR_MANUAL = "NR.MANUAL.MODE.ENABLE"
        private const val NR_MODE = "NR.CONFIG.MODE"
        private const val DS_NR_MODE = "DS.NR.CONFIG.MODE"
        private const val TAG = "HomeViewModel"
        private const val REGISTRATION_POLLS = 10
        private val REGISTRATION_POLL_INTERVAL = 2000.milliseconds
    }
}
