package io.github.saeeddev94.pixelnr.ui.wifi

sealed interface WifiCountryState {
    data object Loading : WifiCountryState
    data class Loaded(val code: String?) : WifiCountryState
    data class Failed(val reason: String?) : WifiCountryState
}
