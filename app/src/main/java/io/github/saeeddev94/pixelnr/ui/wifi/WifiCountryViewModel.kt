package io.github.saeeddev94.pixelnr.ui.wifi

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.saeeddev94.pixelnr.Root
import io.github.saeeddev94.pixelnr.WifiCountry
import io.github.saeeddev94.pixelnr.WifiCountryCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class WifiCountryViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val root = Root(context).also { addCloseable(it) }
    private val wifiCountry = WifiCountry(context, root)
    private val mutex = Mutex()

    var state by mutableStateOf<WifiCountryState>(WifiCountryState.Loading)
        private set

    var applying by mutableStateOf(false)
        private set

    var savedCode by mutableStateOf(wifiCountry.savedCode)
        private set

    var applyOnBoot by mutableStateOf(wifiCountry.applyOnBoot)
        private set

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            mutex.withLock { load() }
        }
    }

    fun setCode(code: WifiCountryCode) {
        if (applying) return
        applying = true
        viewModelScope.launch(Dispatchers.IO) {
            mutex.withLock {
                val result = runCatching { wifiCountry.set(code) }
                withContext(Dispatchers.Main) {
                    applying = false
                    result
                        .onSuccess { savedCode = code }
                        .onFailure { state = WifiCountryState.Failed(it.message) }
                }
                if (result.isSuccess) load()
            }
        }
    }

    fun changeApplyOnBoot(value: Boolean) {
        wifiCountry.applyOnBoot = value
        applyOnBoot = value
    }

    private suspend fun load() {
        val result = runCatching { wifiCountry.getCurrent() }
        withContext(Dispatchers.Main) {
            state = result.fold(
                onSuccess = { WifiCountryState.Loaded(it) },
                onFailure = { WifiCountryState.Failed(it.message) },
            )
        }
    }
}
