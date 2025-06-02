package io.github.saeeddev94.pixelnr

enum class NrMode(val value: String, val label: String) {
    DISABLED("00", "Disabled"),
    NSA("01", "NSA"),
    SA("10", "SA"),
    SA_NAS("11", "SA+NSA");

    companion object {
        fun fromValue(value: String): NrMode? = NrMode.entries.find { it.value == value }
    }
}
