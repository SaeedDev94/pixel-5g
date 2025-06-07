package io.github.saeeddev94.pixelnr

enum class NrMode(
    override val value: String,
    val label: String,
) : NvEnum<NrMode> {

    DISABLED("00", "Disabled"),
    NSA("01", "NSA"),
    SA("10", "SA"),
    SA_NAS("11", "SA+NSA");

    override fun fromValue(value: String): NrMode? = NrMode.entries.find { it.value == value }
}
