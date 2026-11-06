package io.github.saeeddev94.pixelnr

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.saeeddev94.pixelnr.ui.home.HomeScreen
import io.github.saeeddev94.pixelnr.ui.home.HomeViewModel
import io.github.saeeddev94.pixelnr.ui.main.MainUiState
import io.github.saeeddev94.pixelnr.ui.main.MainViewModel
import io.github.saeeddev94.pixelnr.ui.setup.SetupScreen
import io.github.saeeddev94.pixelnr.ui.theme.Pixel5GTheme

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private val phoneStateLauncher = registerForActivityResult(RequestPermission()) {
        if (it) return@registerForActivityResult
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null),
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pixel5GTheme {
                var showRootDialog by remember { mutableStateOf(false) }

                LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
                    showRootDialog = false
                }

                when (val state = mainViewModel.state) {
                    MainUiState.Checking -> Unit
                    MainUiState.Ready -> HomeScreen(
                        sims = homeViewModel.sims,
                        onChangeMode = { sim, value ->
                            homeViewModel.changeNrMode(sim, value)
                        },
                    )
                    is MainUiState.Setup -> SetupScreen(
                        state = state,
                        showRootDialog = showRootDialog,
                        onDismissRootDialog = { showRootDialog = false },
                        onRequestRoot = {
                            mainViewModel.refresh { isRoot ->
                                if (isRoot) return@refresh
                                showRootDialog = true
                            }
                        },
                        onRequestPhoneState = {
                            phoneStateLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.refresh()
    }
}
