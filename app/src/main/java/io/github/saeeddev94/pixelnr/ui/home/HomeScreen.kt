package io.github.saeeddev94.pixelnr.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.github.saeeddev94.pixelnr.NrMode
import io.github.saeeddev94.pixelnr.R
import io.github.saeeddev94.pixelnr.ui.theme.Pixel5GTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sims: List<Sim>?,
    onChangeMode: (sim: Sim, value: NrMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var selectedMode by rememberSaveable { mutableStateOf<NrMode?>(null) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    selectedMode?.let { mode ->
        val sim = sims?.getOrNull(selectedTab) ?: return@let
        AlertDialog(
            title = {
                Text(stringResource(R.string.changeNrModeTitle))
            },
            text = {
                Text(stringResource(R.string.changeNrModeMessage, sim.name, mode.label))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedMode = null
                        onChangeMode(sim, mode)
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
                    if (sims.isNullOrEmpty()) return@TopAppBar
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
        bottomBar = {
            NavigationBar {
                sims?.forEachIndexed { index, sim ->
                    NavigationBarItem(
                        selected = index == selectedTab,
                        onClick = { selectedTab = index },
                        icon = { Icon(Icons.Default.SimCard, contentDescription = null) },
                        label = { Text(sim.name) },
                    )
                }
            }
        }
    ) { innerPadding ->
        when {
            sims == null -> Unit
            else -> SimPage(
                sim = sims.getOrNull(selectedTab),
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    Pixel5GTheme {
        HomeScreen(
            sims = listOf(
                Sim(0, 1, "Vodafone"),
                Sim(1, 2, "T-Mobile"),
            ),
            onChangeMode = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun HomeScreenNoSimPreview() {
    Pixel5GTheme {
        HomeScreen(
            sims = emptyList(),
            onChangeMode = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun HomeScreenLoadingPreview() {
    Pixel5GTheme {
        HomeScreen(
            sims = null,
            onChangeMode = { _, _ -> },
        )
    }
}
