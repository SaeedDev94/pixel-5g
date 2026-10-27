package io.github.saeeddev94.pixelnr

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private var modem = Modem(this)
    private var nrMode: NrMode? = null
    private var nrModeText by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nrModeText = getString(R.string.nrModeLoading)
        setContent {
            Pixel5GTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(nrMode = nrModeText)
                }
            }
        }
        resolveNrMode()
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
                if (nrMode == value) {
                    showToast(getString(R.string.nrSameMode))
                    modem.setNv(DS_NR_MODE, value)
                    return@setPositiveButton
                }
                runCatching {
                    modem.setNv(NR_MANUAL, NrManual.ENABLED)
                    modem.setNv(NR_MODE, value)
                    modem.setNv(DS_NR_MODE, value)
                }.onSuccess {
                    nrMode = it
                    nrModeText = it.label
                    showToast(getString(R.string.rebootDevice))
                }.onFailure {
                    showToast(it.message ?: "")
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .setCancelable(true)
            .show()
    }

    private fun resolveNrMode() {
        runCatching {
            modem.getNv<NrMode>(DS_NR_MODE)
            modem.getNv<NrMode>(NR_MODE)
        }.onSuccess {
            nrMode = it
            nrModeText = it.label
        }.onFailure {
            nrModeErrorDialog(it.message ?: "")
        }
    }

    private fun nrModeErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.errorDialogTitle))
            .setMessage(message)
            .setPositiveButton(getString(R.string.errorDialogReload)) { _, _ ->
                resolveNrMode()
            }
            .setNegativeButton(getString(R.string.errorDialogExit)) { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val NR_MANUAL = "NR.MANUAL.MODE.ENABLE"
        private const val NR_MODE = "NR.CONFIG.MODE"
        private const val DS_NR_MODE = "DS.NR.CONFIG.MODE"
    }
}
