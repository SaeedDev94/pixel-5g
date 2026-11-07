package io.github.saeeddev94.pixelnr.ui.home

import io.github.saeeddev94.pixelnr.NrMode

sealed interface SimState {
    data object Loading : SimState
    data class Loaded(val mode: NrMode, val rebootRequired: Boolean) : SimState
    data class Failed(val reason: String?) : SimState
}
