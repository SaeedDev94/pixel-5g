package io.github.saeeddev94.pixelnr.ui.home

import android.content.Context
import android.telephony.AccessNetworkConstants
import android.telephony.NetworkRegistrationInfo
import android.telephony.ServiceState
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import androidx.annotation.StringRes
import io.github.saeeddev94.pixelnr.R

enum class CellNetwork(@param:StringRes val title: Int) {
    NO_SERVICE(R.string.networkNoService),
    UNKNOWN(R.string.networkUnknown),
    G2(R.string.network2g),
    G3(R.string.network3g),
    G4(R.string.network4g),
    G4_PLUS(R.string.network4gPlus),
    G4_5(R.string.network4_5g),
    G5_NSA(R.string.network5gNsa),
    G5_SA(R.string.network5gSa),
}

class CellNetworkWatcher(
    sim: Sim,
    private val context: Context,
    private val onChange: (CellNetwork) -> Unit,
) : AutoCloseable {

    private val telephonyManager = context.getSystemService(TelephonyManager::class.java)
        .createForSubscriptionId(sim.subscriptionId)

    private var serviceState: ServiceState? = null
    private var displayInfo: TelephonyDisplayInfo? = null
    private var last: CellNetwork? = null

    private val callback = object : TelephonyCallback(),
        TelephonyCallback.ServiceStateListener,
        TelephonyCallback.DisplayInfoListener {

        override fun onServiceStateChanged(serviceState: ServiceState) {
            this@CellNetworkWatcher.serviceState = serviceState
            publish()
        }

        override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
            displayInfo = telephonyDisplayInfo
            publish()
        }
    }

    fun start() {
        @Suppress("MissingPermission")
        telephonyManager.registerTelephonyCallback(context.mainExecutor, callback)
    }

    override fun close() {
        telephonyManager.unregisterTelephonyCallback(callback)
    }

    private fun publish() {
        val state = serviceState ?: return
        val network = resolve(state, displayInfo)
        if (network == last) return
        last = network
        onChange(network)
    }

    private fun resolve(state: ServiceState, displayInfo: TelephonyDisplayInfo?): CellNetwork {
        val registrations = state.networkRegistrationInfoList.filter {
            @Suppress("DEPRECATION")
            val isRegistered = it.isRegistered
            it.transportType == AccessNetworkConstants.TRANSPORT_TYPE_WWAN && isRegistered
        }
        val registration = registrations.firstOrNull {
            (it.domain and NetworkRegistrationInfo.DOMAIN_PS) != 0
        } ?: registrations.firstOrNull()
            ?: return CellNetwork.NO_SERVICE

        return when (registration.accessNetworkTechnology) {
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_EDGE,
            @Suppress("DEPRECATION")
            TelephonyManager.NETWORK_TYPE_CDMA,
            @Suppress("DEPRECATION")
            TelephonyManager.NETWORK_TYPE_1xRTT,
            @Suppress("DEPRECATION")
            TelephonyManager.NETWORK_TYPE_IDEN,
            TelephonyManager.NETWORK_TYPE_GSM -> CellNetwork.G2
            TelephonyManager.NETWORK_TYPE_UMTS,
            @Suppress("DEPRECATION")
            TelephonyManager.NETWORK_TYPE_EVDO_0,
            @Suppress("DEPRECATION")
            TelephonyManager.NETWORK_TYPE_EVDO_A,
            @Suppress("DEPRECATION")
            TelephonyManager.NETWORK_TYPE_EVDO_B,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_HSPAP,
            @Suppress("DEPRECATION")
            TelephonyManager.NETWORK_TYPE_EHRPD,
            TelephonyManager.NETWORK_TYPE_TD_SCDMA -> CellNetwork.G3
            TelephonyManager.NETWORK_TYPE_LTE,
            @Suppress("SwitchIntDef")
            NETWORK_TYPE_LTE_CA -> when (displayInfo?.overrideNetworkType) {
                TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA,
                TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED -> CellNetwork.G5_NSA
                TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO -> CellNetwork.G4_5
                TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA -> CellNetwork.G4_PLUS
                else -> when {
                    registration.accessNetworkTechnology == NETWORK_TYPE_LTE_CA ||
                            state.cellBandwidths.size > 1 -> CellNetwork.G4_PLUS
                    else -> CellNetwork.G4
                }
            }
            TelephonyManager.NETWORK_TYPE_NR -> CellNetwork.G5_SA
            else -> CellNetwork.UNKNOWN
        }
    }

    private companion object {
        const val NETWORK_TYPE_LTE_CA = 19
    }
}
