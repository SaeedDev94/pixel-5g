package io.github.saeeddev94.pixelnr

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import io.github.saeeddev94.pixelnr.ui.home.HomeScreen
import io.github.saeeddev94.pixelnr.ui.home.ImsState
import io.github.saeeddev94.pixelnr.ui.home.NrModeDialog
import io.github.saeeddev94.pixelnr.ui.home.Sim
import io.github.saeeddev94.pixelnr.ui.home.SimState
import io.github.saeeddev94.pixelnr.ui.theme.Pixel5GTheme

// Store screenshots, rendered from previews with fake SIMs:
// ./gradlew updateDebugScreenshotTest
private const val DEVICE = "spec:width=1008px,height=2244px,dpi=420"

private val imsValues = ImsOption.entries.associateWith {
    when (it) {
        ImsOption.VONR, ImsOption.VONR_VISIBLE -> false
        else -> it.recommended
    }
}

private fun sims(nr: SimState, registration: ImsRegistration) = listOf(
    Sim(
        index = 0,
        subscriptionId = 1,
        iccId = null,
        carrierId = 0,
        name = "Vodafone",
        state = nr,
        ims = ImsState.Loaded(values = imsValues),
        registration = registration,
    ),
    Sim(
        index = 1,
        subscriptionId = 2,
        iccId = null,
        carrierId = 0,
        name = "T-Mobile",
    ),
)

@Composable
private fun Home(sims: List<Sim>) {
    HomeScreen(
        sims = sims,
        imsModuleActive = false,
        registrationRefreshing = false,
        onChangeMode = { _, _ -> },
        onImsChange = { _, _, _ -> },
        onRestartPhoneService = {},
        onReboot = {},
    )
}

@PreviewTest
@Preview(device = DEVICE, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SimSettingsScreenshot() {
    Pixel5GTheme {
        Home(
            sims(
                nr = SimState.Loaded(mode = NrMode.SA_NAS, rebootRequired = false),
                registration = ImsRegistration(
                    state = ImsRegistration.State.REGISTERED,
                    voice = true,
                    sms = true,
                ),
            )
        )
    }
}

@PreviewTest
@Preview(device = DEVICE, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NrModeScreenshot() {
    Pixel5GTheme {
        Box {
            Home(
                sims(
                    nr = SimState.Loaded(mode = NrMode.SA, rebootRequired = false),
                    registration = ImsRegistration(ImsRegistration.State.NOT_REGISTERED),
                )
            )
            NrModeDialog(
                current = NrMode.SA,
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}

@PreviewTest
@Preview(device = DEVICE, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RebootScreenshot() {
    Pixel5GTheme {
        Home(
            sims(
                nr = SimState.Loaded(mode = NrMode.SA_NAS, rebootRequired = true),
                registration = ImsRegistration(
                    state = ImsRegistration.State.REGISTERED,
                    voice = true,
                    sms = true,
                ),
            )
        )
    }
}
