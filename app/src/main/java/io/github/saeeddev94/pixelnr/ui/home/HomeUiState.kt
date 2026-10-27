package io.github.saeeddev94.pixelnr.ui.home

import io.github.saeeddev94.pixelnr.NrMode

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Loaded(val mode: NrMode, val rebootRequired: Boolean) : HomeUiState
    data class Failed(val reason: String?) : HomeUiState
}
