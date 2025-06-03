package io.github.saeeddev94.pixelnr

enum class NrManual(val value: String) {
    DISABLED("00"),
    ENABLED("01");

    companion object {
        fun fromValue(value: String): NrManual? = NrManual.entries.find { it.value == value }
    }
}
