package io.github.saeeddev94.pixelnr.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled._5g
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.saeeddev94.pixelnr.ImsOption
import io.github.saeeddev94.pixelnr.NrMode
import io.github.saeeddev94.pixelnr.R
import io.github.saeeddev94.pixelnr.ui.theme.Pixel5GTheme

@Composable
fun SimPage(
    sim: Sim,
    imsModuleActive: Boolean,
    registrationRefreshing: Boolean,
    onChangeMode: (value: NrMode) -> Unit,
    onImsChange: (option: ImsOption, value: Boolean) -> Unit,
    onRestartPhoneService: () -> Unit,
    onReboot: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // keyed by SIM, so switching tabs doesn't carry an open dialog over
    var showNrModeDialog by rememberSaveable(sim.subscriptionId) { mutableStateOf(false) }
    var showRebootDialog by rememberSaveable { mutableStateOf(false) }
    val state = sim.state

    if (showNrModeDialog) {
        NrModeDialog(
            current = (state as? SimState.Loaded)?.mode,
            onConfirm = {
                showNrModeDialog = false
                onChangeMode(it)
            },
            onDismiss = { showNrModeDialog = false },
        )
    }

    if (showRebootDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.rebootTitle)) },
            text = { Text(stringResource(R.string.rebootMessage)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRebootDialog = false
                        onReboot()
                    },
                ) {
                    Text(stringResource(R.string.reboot))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRebootDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onDismissRequest = { showRebootDialog = false },
        )
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        SettingsHeader(text = stringResource(R.string.settingsNetwork))
        ListItem(
            modifier = Modifier.clickable(
                enabled = state !is SimState.Loading,
                onClick = { showNrModeDialog = true },
            ),
            leadingContent = {
                Icon(Icons.Default._5g, contentDescription = null)
            },
            headlineContent = {
                Text(stringResource(R.string.nrModeLabel))
            },
            supportingContent = {
                NrModeSummary(state = state)
            },
            trailingContent = when (state) {
                SimState.Loading -> {
                    {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }
                is SimState.Loaded if state.rebootRequired -> {
                    {
                        IconButton(onClick = { showRebootDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = stringResource(R.string.reboot),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                else -> null
            },
        )
        ImsRegistrationItem(
            registration = sim.registration,
            refreshing = registrationRefreshing,
            onRefresh = onRestartPhoneService,
        )
        ImsSection(
            state = sim.ims,
            moduleActive = imsModuleActive,
            onChange = onImsChange,
        )
    }
}

@Composable
private fun NrModeSummary(state: SimState) {
    when (state) {
        SimState.Loading -> Text(stringResource(R.string.nrModeLoading))
        // one line on purpose: a second line makes it a three-line item, which top-aligns the icons
        is SimState.Loaded -> {
            val rebootRequired = stringResource(R.string.rebootRequired)
            val accent = MaterialTheme.colorScheme.primary
            Text(
                text = buildAnnotatedString {
                    append(state.mode.label)
                    if (state.rebootRequired) {
                        append(" · ")
                        withStyle(SpanStyle(color = accent)) {
                            append(rebootRequired)
                        }
                    }
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        is SimState.Failed -> Text(
            text = state.reason ?: stringResource(R.string.nrModeError),
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SimPagePreview() {
    Pixel5GTheme {
        SimPage(
            sim = Sim(
                index = 0,
                subscriptionId = 1,
                iccId = null,
                carrierId = 0,
                name = "Vodafone",
                state = SimState.Loaded(mode = NrMode.SA, rebootRequired = true),
            ),
            imsModuleActive = false,
            registrationRefreshing = false,
            onChangeMode = {},
            onImsChange = { _, _ -> },
            onRestartPhoneService = {},
            onReboot = {},
        )
    }
}
