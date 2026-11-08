package io.github.saeeddev94.pixelnr.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import io.github.saeeddev94.pixelnr.ImsOption
import io.github.saeeddev94.pixelnr.NrMode
import io.github.saeeddev94.pixelnr.R
import io.github.saeeddev94.pixelnr.ui.theme.Pixel5GTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sims: List<Sim>?,
    imsModuleActive: Boolean,
    registrationRefreshing: Boolean,
    onChangeMode: (sim: Sim, value: NrMode) -> Unit,
    onImsChange: (sim: Sim, option: ImsOption, value: Boolean) -> Unit,
    onRestartPhoneService: () -> Unit,
    onReboot: () -> Unit,
    onOpenWifiCountry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val selectedSim = sims?.getOrNull(selectedTab)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    when {
                        sims == null -> Unit
                        selectedSim == null -> Text(stringResource(R.string.appName))
                        else -> Column {
                            Text(selectedSim.name)
                            Text(
                                text = stringResource(R.string.simSlot, selectedSim.index + 1),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    if (sims != null) IconButton(onClick = onOpenWifiCountry) {
                        Icon(
                            imageVector = Icons.Default.WifiTethering,
                            contentDescription = stringResource(R.string.hotspotTitle),
                        )
                    }
                },
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
        val sim = selectedSim
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
                imsModuleActive = imsModuleActive,
                registrationRefreshing = registrationRefreshing,
                onChangeMode = { onChangeMode(sim, it) },
                onImsChange = { option, value -> onImsChange(sim, option, value) },
                onRestartPhoneService = onRestartPhoneService,
                onReboot = onReboot,
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
            imsModuleActive = false,
            registrationRefreshing = false,
            onChangeMode = { _, _ -> },
            onImsChange = { _, _, _ -> },
            onRestartPhoneService = {},
            onReboot = {},
            onOpenWifiCountry = {},
        )
    }
}

@Preview
@Composable
private fun HomeScreenNoSimPreview() {
    Pixel5GTheme {
        HomeScreen(
            sims = emptyList(),
            imsModuleActive = false,
            registrationRefreshing = false,
            onChangeMode = { _, _ -> },
            onImsChange = { _, _, _ -> },
            onRestartPhoneService = {},
            onReboot = {},
            onOpenWifiCountry = {},
        )
    }
}

@Preview
@Composable
private fun HomeScreenLoadingPreview() {
    Pixel5GTheme {
        HomeScreen(
            sims = null,
            imsModuleActive = false,
            registrationRefreshing = false,
            onChangeMode = { _, _ -> },
            onImsChange = { _, _, _ -> },
            onRestartPhoneService = {},
            onReboot = {},
            onOpenWifiCountry = {},
        )
    }
}
