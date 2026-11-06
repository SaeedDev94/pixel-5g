package io.github.saeeddev94.pixelnr.extensions

import android.content.Context
import android.content.pm.PackageManager

fun Context.hasPermission(name: String): Boolean =
    checkSelfPermission(name) == PackageManager.PERMISSION_GRANTED
