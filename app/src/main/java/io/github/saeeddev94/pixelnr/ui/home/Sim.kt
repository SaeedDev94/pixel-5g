package io.github.saeeddev94.pixelnr.ui.home

data class Sim(
    val index: Int,
    val subscriptionId: Int,
    val iccId: String?,
    val carrierId: Int,
    val name: String,
    val state: SimState = SimState.Loading,
)
