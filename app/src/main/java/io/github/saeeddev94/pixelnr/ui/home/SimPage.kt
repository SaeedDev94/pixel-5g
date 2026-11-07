package io.github.saeeddev94.pixelnr.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled._5g
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.saeeddev94.pixelnr.NrMode
import io.github.saeeddev94.pixelnr.R
import io.github.saeeddev94.pixelnr.ui.theme.Pixel5GTheme

@Composable
fun SimPage(
    sim: Sim,
    onChangeMode: (value: NrMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    // keyed by SIM, so switching tabs doesn't carry an open dialog over
    var showNrModeDialog by rememberSaveable(sim.subscriptionId) { mutableStateOf(false) }
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

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        ListItem(
            leadingContent = {
                Icon(Icons.Default.SimCard, contentDescription = null)
            },
            headlineContent = {
                Text(
                    text = sim.name,
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            supportingContent = {
                Text(stringResource(R.string.simSlot, sim.index + 1))
            },
        )
        HorizontalDivider()
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
                else -> null
            },
        )
    }
}

@Composable
private fun NrModeSummary(state: SimState) {
    when (state) {
        SimState.Loading -> Text(stringResource(R.string.nrModeLoading))
        is SimState.Loaded -> Column {
            Text(state.mode.label)
            if (state.rebootRequired) {
                Text(
                    text = stringResource(R.string.rebootDevice),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
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
            onChangeMode = {},
        )
    }
}
