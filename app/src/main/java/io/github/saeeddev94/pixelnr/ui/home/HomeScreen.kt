package io.github.saeeddev94.pixelnr.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.appName)) },
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
        val sim = sims?.getOrNull(selectedTab)
        when {
            sims == null -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            sim == null -> NoSimPage(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
            else -> SimPage(
                sim = sim,
                onChangeMode = { onChangeMode(sim, it) },
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
                Sim(0, 1, null, 0, "Vodafone"),
                Sim(1, 2, null, 0, "T-Mobile"),
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
