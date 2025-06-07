package io.github.saeeddev94.pixelnr

enum class NrManual(
    override val value: String,
) : NvEnum<NrManual> {

    DISABLED("00"),
    ENABLED("01");

    override fun fromValue(value: String): NrManual? = NrManual.entries.find { it.value == value }
}
