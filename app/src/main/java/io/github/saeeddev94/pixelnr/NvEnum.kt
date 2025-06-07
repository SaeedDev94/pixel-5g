package io.github.saeeddev94.pixelnr

interface NvEnum<T> {
    val value: String
    fun fromValue(value: String): T?
}
