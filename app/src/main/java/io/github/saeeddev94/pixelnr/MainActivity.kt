package io.github.saeeddev94.pixelnr

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topjohnwu.superuser.Shell
import io.github.saeeddev94.pixelnr.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var nrMode: NrMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        runCatching { getNrMode() }.onSuccess {
            nrMode = it
            binding.nrMode.text = it.label
        }.onFailure {
            binding.nrMode.text = it.message
        }
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

        if (nrMode == null) {
            Toast.makeText(
                applicationContext, getString(R.string.checkRootAccess), Toast.LENGTH_SHORT
            ).show()
        } else {
            nrModeDialog(value)
        }

        return true
    }

    private fun nrModeDialog(value: NrMode) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.changeNrModeTitle))
            .setMessage(getString(R.string.changeNrModeMessage, value.label))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                if (nrMode == value) {
                    Toast.makeText(
                        applicationContext, getString(R.string.nrSameMode), Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }
                runCatching {
                    setNrManual()
                    setNrMode(value)
                }.onSuccess {
                    nrMode = it
                    binding.nrMode.text = it.label
                    Toast.makeText(
                        applicationContext, getString(R.string.rebootDevice), Toast.LENGTH_SHORT
                    ).show()
                }.onFailure {
                    Toast.makeText(
                        applicationContext, it.message, Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .setCancelable(true)
            .show()
    }

    private fun makeNrManual(value: String): NrManual {
        val nrManual = NrManual.fromValue(value)
        if (nrManual == null || nrManual == NrManual.DISABLED) {
            throw Exception(getString(R.string.resInvalidCase))
        }
        return nrManual
    }

    private fun makeNrMode(value: String): NrMode {
        return NrMode.fromValue(value) ?:
        throw Exception(getString(R.string.resInvalidCase))
    }

    private fun setNrManual(): NrManual {
        val result = runATCommand("SETNV=\"$NR_MANUAL\",0,\"${NrManual.ENABLED.value}\"")
        return makeNrManual(getOutput(result, NR_MANUAL))
    }

    private fun getNrMode(): NrMode {
        val result = runATCommand("GETNV=\"$NR_MODE\"")
        return makeNrMode(getOutput(result, NR_MODE))
    }

    private fun setNrMode(nrMode: NrMode): NrMode {
        val result = runATCommand("SETNV=\"$NR_MODE\",0,\"${nrMode.value}\"")
        return makeNrMode(getOutput(result, NR_MODE))
    }

    private fun getOutput(result: Shell.Result, key: String): String {
        val output = result.out.joinToString("\n").trim()
        if (!result.isSuccess) throw Exception(getString(R.string.cmdFailed))
        if (!output.contains("OK")) throw Exception(getString(R.string.resNoOk))
        val regex = "\"$key\",0,\"(.*)\"".toRegex()
        val matches = regex.find(output) ?: throw Exception(getString(R.string.resRegexpIssue))
        val group = matches.groups[1] ?: throw Exception(getString(R.string.resMatchIssue))
        return group.value
    }

    private fun runATCommand(nv: String): Shell.Result {
        val command = "echo 'AT+GOOG$nv\\r' > $MODEM & cat $MODEM"
        return Shell.cmd(command).exec()
    }

    companion object {
        private const val MODEM = "/dev/umts_router"
        private const val NR_MODE = "NR.CONFIG.MODE"
        private const val NR_MANUAL = "NR.MANUAL.MODE.ENABLE"
    }
}
