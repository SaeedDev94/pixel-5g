package io.github.saeeddev94.pixelnr.ui.home

data class Sim(
    val index: Int,
    val id: Int,
    val name: String,
    val state: HomeUiState = HomeUiState.Loading,
)
