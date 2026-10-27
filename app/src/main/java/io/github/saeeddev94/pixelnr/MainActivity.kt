package io.github.saeeddev94.pixelnr

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Job

class MainActivity : AppCompatActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    private var resolveJob: Job? = null
    private var changeJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Pixel5GTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        nrMode = homeViewModel.nrMode?.label ?: stringResource(R.string.nrModeLoading),
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        resolveJob = homeViewModel.resolveNrMode()
    }

    override fun onStop() {
        resolveJob?.cancel()
        changeJob?.cancel()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nrDisabled -> NrMode.DISABLED
            R.id.nrNsa -> NrMode.NSA
            R.id.nrSa -> NrMode.SA
            R.id.nrSaNsa -> NrMode.SA_NAS
            else -> NrMode.DISABLED
        }.let { nrModeConfirmDialog(it) }
        return true
    }

    private fun nrModeConfirmDialog(value: NrMode) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.changeNrModeTitle))
            .setMessage(getString(R.string.changeNrModeMessage, value.label))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                changeJob?.cancel()
                changeJob = homeViewModel.changeNrMode(value) { err ->
                    when (err) {
                        null -> getString(R.string.rebootDevice)
                        else -> "${err.message}"
                    }.let { text ->
                        Toast.makeText(
                            applicationContext, text, Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .setCancelable(true)
            .show()
    }
}
