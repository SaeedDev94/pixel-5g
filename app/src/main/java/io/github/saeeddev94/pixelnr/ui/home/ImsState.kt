package io.github.saeeddev94.pixelnr.ui.home

import io.github.saeeddev94.pixelnr.ImsOption

sealed interface ImsState {
    data object Loading : ImsState
    data class Loaded(
        val values: Map<ImsOption, Boolean>,
    ) : ImsState
    data class Failed(val reason: String?) : ImsState
}
