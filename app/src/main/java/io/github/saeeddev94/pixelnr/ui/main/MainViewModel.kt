package io.github.saeeddev94.pixelnr.ui.main

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.saeeddev94.pixelnr.Root
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context by lazy { application.applicationContext }
    private val root by lazy {
        Root(context).also { addCloseable(it) }
    }

    var state by mutableStateOf<MainUiState>(MainUiState.Checking)
        private set

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val rootGranted = root.isRoot()
            val phoneStateGranted = context.checkSelfPermission(
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
            val permissionsGranted = rootGranted && phoneStateGranted
            withContext(Dispatchers.Main) {
                state = when (permissionsGranted) {
                    true -> MainUiState.Ready
                    false -> MainUiState.Setup(rootGranted, phoneStateGranted)
                }
            }
        }
    }
}
