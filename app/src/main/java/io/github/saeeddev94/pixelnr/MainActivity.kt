package io.github.saeeddev94.pixelnr

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.saeeddev94.pixelnr.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var modem = Modem(this)
    private var nrMode: NrMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        resolveNrMode()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val value = when (item.itemId) {
            R.id.nrDisabled -> NrMode.DISABLED
            R.id.nrNsa -> NrMode.NSA
            R.id.nrSa -> NrMode.SA
            R.id.nrSaNsa -> NrMode.SA_NAS
            else -> NrMode.DISABLED
        }
        nrModeConfirmDialog(value)
        return true
    }

    private fun nrModeConfirmDialog(value: NrMode) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.changeNrModeTitle))
            .setMessage(getString(R.string.changeNrModeMessage, value.label))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                if (nrMode == value) {
                    showToast(getString(R.string.nrSameMode))
                    return@setPositiveButton
                }
                runCatching {
                    modem.setNv(NR_MANUAL, NrManual.ENABLED)
                    modem.setNv(NR_MODE, value)
                }.onSuccess {
                    nrMode = it
                    binding.nrMode.text = it.label
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
            modem.getNv<NrMode>(NR_MODE)
        }.onSuccess {
            nrMode = it
            binding.nrMode.text = it.label
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
    }
}
