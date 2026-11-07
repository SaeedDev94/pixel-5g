package io.github.saeeddev94.pixelnr.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LteMobiledata
import androidx.compose.material.icons.filled.LtePlusMobiledata
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SettingsPhone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiCalling3
import androidx.compose.material.icons.filled._4gMobiledata
import androidx.compose.material.icons.filled._4gPlusMobiledata
import androidx.compose.material.icons.filled._5g
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.saeeddev94.pixelnr.ImsOption

val ImsOption.inverted: Boolean
    get() = this == ImsOption.HIDE_LTE_PLUS

val ImsOption.icon: ImageVector
    get() = when (this) {
        ImsOption.VOLTE -> Icons.Default.PhoneInTalk
        ImsOption.VT -> Icons.Default.Videocam
        ImsOption.WFC -> Icons.Default.WifiCalling3
        ImsOption.WFC_WIFI_ONLY -> Icons.Default.Wifi
        ImsOption.EDITABLE_ENHANCED_4G -> Icons.Default._4gMobiledata
        ImsOption.ENHANCED_4G_DEFAULT -> Icons.Default._4gPlusMobiledata
        ImsOption.EDITABLE_WFC_MODE -> Icons.Default.SettingsPhone
        ImsOption.EDITABLE_WFC_ROAMING -> Icons.Default.Public
        ImsOption.HIDE_LTE_PLUS -> Icons.Default.LtePlusMobiledata
        ImsOption.SHOW_4G -> Icons.Default.LteMobiledata
        ImsOption.VONR -> Icons.Default._5g
        ImsOption.VONR_VISIBLE -> Icons.Default.Visibility
    }
