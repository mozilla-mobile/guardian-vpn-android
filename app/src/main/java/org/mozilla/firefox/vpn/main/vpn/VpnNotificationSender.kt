package org.mozilla.firefox.vpn.main.vpn

import android.content.Context
import androidx.core.app.NotificationCompat
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.main.vpn.domain.VpnManagerStateProvider
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider
import org.mozilla.firefox.vpn.util.NotificationUtil

class VpnNotificationSender(private val appContext: Context, private val vpnManager: VpnManager) {

    private val vpnStateProvider: VpnStateProvider by lazy {
        VpnManagerStateProvider(vpnManager)
    }

    private var builder: NotificationCompat.Builder? = null
    private var switchPair: Pair<String, String>? = null

    init {
        vpnStateProvider.stateObservable.observeForever {
            handleVpnState(it)
        }
    }

    private fun handleVpnState(vpnState: VpnState) {
        when (vpnState) {
            VpnState.Connected -> {
                builder = if (switchPair != null) {
                    NotificationUtil.createBaseBuilder(appContext).apply {
                        setContentTitle(appContext.getString(R.string.windows_notification_vpn_switch_title, switchPair!!.first, switchPair!!.second))
                        setContentText(appContext.getString(R.string.windows_notification_vpn_switch_content))
                    }
                } else {
                    NotificationUtil.createBaseBuilder(appContext).apply {
                        setContentTitle(appContext.getString(R.string.windows_notification_vpn_on_title))
                        setContentText(appContext.getString(R.string.windows_notification_vpn_on_content))
                    }
                }
                switchPair = null
            }
            VpnState.Disconnected -> {
                builder = NotificationUtil.createBaseBuilder(appContext).apply {
                    setContentTitle(appContext.getString(R.string.windows_notification_vpn_off_title))
                    setContentText(appContext.getString(R.string.windows_notification_vpn_off_content))
                }
            }
            is VpnState.Switching -> {
                switchPair = vpnState.oldServer.city.name to vpnState.newServer.city.name
            }
            VpnState.Unstable -> {
                builder = NotificationUtil.createBaseBuilder(appContext).apply {
                    setContentTitle(appContext.getString(R.string.windows_notification_vpn_unstable_title))
                    setContentText(appContext.getString(R.string.windows_notification_vpn_unstable_content))
                }
            }
            VpnState.NoSignal -> {
                builder = NotificationUtil.createImportantBuilder(appContext).apply {
                    setContentTitle(appContext.getString(R.string.windows_notification_vpn_no_signal_title))
                    setContentText(appContext.getString(R.string.windows_notification_vpn_no_signal_content))
                }
            }
        }
        builder?.let { NotificationUtil.sendNotification(appContext, it) }
    }
}
