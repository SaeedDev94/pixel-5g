package io.github.saeeddev94.pixelnr

enum class NrManual(
    override val value: String,
) : NvEnum {
    DISABLED("00"),
    ENABLED("01");
}
