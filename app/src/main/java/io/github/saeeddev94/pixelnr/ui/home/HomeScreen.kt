package io.github.saeeddev94.pixelnr.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.saeeddev94.pixelnr.R
import io.github.saeeddev94.pixelnr.ui.theme.Pixel5GTheme

@Composable
fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = stringResource(R.string.nrModeLabel),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = when (state) {
                HomeUiState.Loading -> stringResource(R.string.nrModeLoading)
                is HomeUiState.Loaded -> state.mode.label
                is HomeUiState.Failed -> stringResource(R.string.nrModeError)
            },
            fontSize = 24.sp,
        )
        Text(
            text = when (state) {
                is HomeUiState.Loaded -> when (state.rebootRequired) {
                    true -> stringResource(R.string.rebootDevice)
                    false -> ""
                }
                is HomeUiState.Failed -> "${state.reason}"
                else -> ""
            },
            fontSize = 24.sp,
        )
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    Pixel5GTheme {
        Surface { HomeScreen(state = HomeUiState.Loading) }
    }
}
