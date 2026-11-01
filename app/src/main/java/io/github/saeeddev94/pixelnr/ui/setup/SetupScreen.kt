package io.github.saeeddev94.pixelnr.ui.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.saeeddev94.pixelnr.R
import io.github.saeeddev94.pixelnr.ui.main.MainUiState
import io.github.saeeddev94.pixelnr.ui.theme.Pixel5GTheme

@Composable
fun SetupScreen(
    state: MainUiState.Setup,
    showRootDialog: Boolean,
    onDismissRootDialog: () -> Unit,
    onRequestRoot: () -> Unit,
    onRequestPhoneState: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (showRootDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.setupRootDialogTitle)) },
            text = { Text(stringResource(R.string.setupRootDialogMessage)) },
            confirmButton = {
                TextButton(onClick = onDismissRootDialog) {
                    Text(stringResource(R.string.ok))
                }
            },
            onDismissRequest = onDismissRootDialog,
        )
    }

    val slides = listOf(
        SetupSlide(
            icon = Icons.Default.Tag,
            title = R.string.setupRootTitle,
            description = R.string.setupRootDescription,
            granted = state.rootGranted,
            onRequest = onRequestRoot,
        ),
        SetupSlide(
            icon = Icons.Default.SimCard,
            title = R.string.setupPhoneStateTitle,
            description = R.string.setupPhoneStateDescription,
            granted = state.phoneStateGranted,
            onRequest = onRequestPhoneState,
        ),
    )
    val pagerState = rememberPagerState(
        pageCount = { slides.size },
    )

    val moveTo = slides.indexOfFirst { !it.granted }
    LaunchedEffect(moveTo) {
        if (moveTo < 0) return@LaunchedEffect
        pagerState.animateScrollToPage(moveTo)
    }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            HorizontalPager(
                modifier = Modifier
                    .weight(1f),
                state = pagerState,
            ) { index ->
                SetupSlidePage(
                    slide = slides[index],
                )
            }
            SetupSlideIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(24.dp),
                current = pagerState.currentPage,
                total = slides.size,
            )
        }
    }
}

@Preview
@Composable
private fun SetupScreenNothingGrantedPreview() {
    Pixel5GTheme {
        SetupScreen(
            state = MainUiState.Setup(rootGranted = false, phoneStateGranted = false),
            showRootDialog = false,
            onDismissRootDialog = {},
            onRequestRoot = {},
            onRequestPhoneState = {},
        )
    }
}
