package io.github.saeeddev94.pixelnr.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.saeeddev94.pixelnr.NrMode
import io.github.saeeddev94.pixelnr.R
import io.github.saeeddev94.pixelnr.ui.theme.Pixel5GTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onChangeMode: (value: NrMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var selectedMode by rememberSaveable { mutableStateOf<NrMode?>(null) }

    selectedMode?.let { mode ->
        AlertDialog(
            title = { Text(stringResource(R.string.changeNrModeTitle)) },
            text = {
                Text(stringResource(R.string.changeNrModeMessage, mode.label))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedMode = null
                        onChangeMode(mode)
                    },
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { selectedMode = null },
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onDismissRequest = { selectedMode = null },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.appName)) },
                actions = {
                    IconButton(
                        onClick = { menuExpanded = true },
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        NrMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.label) },
                                onClick = {
                                    menuExpanded = false
                                    selectedMode = mode
                                }
                            )
                        }
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
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
}

@Preview
@Composable
private fun HomeScreenPreview() {
    Pixel5GTheme {
        HomeScreen(
            state = HomeUiState.Loading,
            onChangeMode = {},
        )
    }
}
