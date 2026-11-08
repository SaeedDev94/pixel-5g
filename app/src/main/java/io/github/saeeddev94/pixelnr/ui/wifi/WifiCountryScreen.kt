package io.github.saeeddev94.pixelnr.ui.wifi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.saeeddev94.pixelnr.R
import io.github.saeeddev94.pixelnr.WifiCountryCode
import io.github.saeeddev94.pixelnr.ui.home.SettingsHeader
import io.github.saeeddev94.pixelnr.ui.theme.Pixel5GTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiCountryScreen(
    state: WifiCountryState,
    applying: Boolean,
    savedCode: WifiCountryCode?,
    applyOnBoot: Boolean,
    onCodeChange: (code: WifiCountryCode) -> Unit,
    onApplyOnBootChange: (value: Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.hotspotTitle)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val current = (state as? WifiCountryState.Loaded)?.code
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            ListItem(
                leadingContent = {
                    Icon(Icons.Default.Public, contentDescription = null)
                },
                headlineContent = {
                    Text(stringResource(R.string.wifiCountryCurrent))
                },
                supportingContent = {
                    when (state) {
                        WifiCountryState.Loading -> Text(stringResource(R.string.nrModeLoading))
                        is WifiCountryState.Loaded -> Text(
                            state.code ?: stringResource(R.string.wifiCountryUnknown)
                        )
                        is WifiCountryState.Failed -> Text(
                            text = state.reason ?: stringResource(R.string.nrModeError),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                trailingContent = if (state == WifiCountryState.Loading || applying) {
                    {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                } else {
                    null
                },
            )

            SettingsHeader(text = stringResource(R.string.wifiCountryCode))
            Column(modifier = Modifier.selectableGroup()) {
                WifiCountryCode.entries.forEach { code ->
                    val selected = code.name == current
                    ListItem(
                        modifier = Modifier.selectable(
                            selected = selected,
                            enabled = !applying && state != WifiCountryState.Loading,
                            role = Role.RadioButton,
                            onClick = { onCodeChange(code) },
                        ),
                        leadingContent = {
                            RadioButton(selected = selected, onClick = null)
                        },
                        headlineContent = { Text(code.name) },
                        supportingContent = { Text(stringResource(code.title)) },
                    )
                }
            }

            SettingsHeader(text = stringResource(R.string.wifiCountryBoot))
            ListItem(
                modifier = Modifier.toggleable(
                    value = applyOnBoot,
                    enabled = savedCode != null,
                    role = Role.Switch,
                    onValueChange = onApplyOnBootChange,
                ),
                leadingContent = {
                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                },
                headlineContent = {
                    Text(stringResource(R.string.wifiCountryApplyOnBoot))
                },
                supportingContent = {
                    Text(
                        when (savedCode) {
                            null -> stringResource(R.string.wifiCountryApplyOnBootEmpty)
                            else -> stringResource(R.string.wifiCountryApplyOnBootSummary, savedCode.name)
                        }
                    )
                },
                trailingContent = {
                    Switch(
                        checked = applyOnBoot,
                        enabled = savedCode != null,
                        onCheckedChange = null,
                    )
                },
            )
        }
    }
}

@Preview
@Composable
private fun WifiCountryScreenPreview() {
    Pixel5GTheme {
        WifiCountryScreen(
            state = WifiCountryState.Loaded("US"),
            applying = false,
            savedCode = WifiCountryCode.US,
            applyOnBoot = true,
            onCodeChange = {},
            onApplyOnBootChange = {},
            onBack = {},
        )
    }
}
