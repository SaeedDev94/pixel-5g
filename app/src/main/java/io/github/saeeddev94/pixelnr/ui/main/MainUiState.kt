package io.github.saeeddev94.pixelnr.ui.main

sealed interface MainUiState {
    data object Checking : MainUiState
    data class Setup(
        val rootGranted: Boolean,
        val phoneStateGranted: Boolean,
    ) : MainUiState
    data object Ready : MainUiState
}
