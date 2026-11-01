package io.github.saeeddev94.pixelnr.ui.setup

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class SetupSlide(
    val icon: ImageVector,
    @StringRes
    val title: Int,
    @StringRes
    val description: Int,
    val granted: Boolean,
    val onRequest: () -> Unit,
)
