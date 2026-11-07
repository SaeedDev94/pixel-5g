package io.github.saeeddev94.pixelnr

import androidx.annotation.StringRes

enum class ImsOption(
    val key: String,
    @param:StringRes val title: Int,
    val androidDefault: Boolean,
    val recommended: Boolean,
) {
    VOLTE("carrier_volte_available_bool", R.string.imsVolte, false, true),
    HIDE_LTE_PLUS("hide_lte_plus_data_icon_bool", R.string.imsShowLtePlus, true, false),
    SHOW_4G("show_4g_for_lte_data_icon_bool", R.string.imsShow4g, false, true),
    EDITABLE_ENHANCED_4G("editable_enhanced_4g_lte_bool", R.string.imsEditableEnhanced4g, true, true),
    ENHANCED_4G_DEFAULT("enhanced_4g_lte_on_by_default_bool", R.string.imsEnhanced4gDefault, true, true),
    EDITABLE_WFC_MODE("editable_wfc_mode_bool", R.string.imsEditableWfcMode, true, true),
    EDITABLE_WFC_ROAMING("editable_wfc_roaming_mode_bool", R.string.imsEditableWfcRoaming, false, true),
    WFC("carrier_wfc_ims_available_bool", R.string.imsWfc, false, true),
    WFC_WIFI_ONLY("carrier_wfc_supports_wifi_only_bool", R.string.imsWfcWifiOnly, false, true),
    VT("carrier_vt_available_bool", R.string.imsVt, false, true),
    VONR("vonr_enabled_bool", R.string.imsVonr, false, true),
    VONR_VISIBLE("vonr_setting_visibility_bool", R.string.imsVonrVisible, true, true),
}
