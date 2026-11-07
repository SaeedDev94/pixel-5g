package io.github.saeeddev94.pixelnr.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.saeeddev94.pixelnr.R

@Composable
fun SimPage(
    sim: Sim,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = stringResource(R.string.nrModeLabel),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = when (val state = sim.state) {
                SimState.Loading -> stringResource(R.string.nrModeLoading)
                is SimState.Loaded -> state.mode.label
                is SimState.Failed -> stringResource(R.string.nrModeError)
            },
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = when (val state = sim.state) {
                is SimState.Loaded -> when (state.rebootRequired) {
                    true -> stringResource(R.string.rebootDevice)
                    false -> ""
                }
                is SimState.Failed -> "${state.reason}"
                else -> ""
            },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
