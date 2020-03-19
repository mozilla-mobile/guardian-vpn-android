package org.mozilla.firefox.vpn

import android.app.Application
import android.content.Context
import org.mozilla.firefox.vpn.main.vpn.MockVpnManager
import org.mozilla.firefox.vpn.main.vpn.VpnNotificationSender
import org.mozilla.firefox.vpn.report.ReportUtil
import org.mozilla.firefox.vpn.service.MockGuardianService
import org.mozilla.firefox.vpn.util.NotificationUtil

class GuardianApp : Application() {

    val coreComponent: CoreComponent by lazy {
        CoreComponentImpl(this)
    }

    lateinit var guardianComponent: GuardianComponent
    lateinit var vpnNotificationSender: VpnNotificationSender

    override fun onCreate() {
        super.onCreate()

        ReportUtil.initReport(this)

        guardianComponent = GuardianComponentImpl(coreComponent)

        NotificationUtil.init(this)
        vpnNotificationSender = VpnNotificationSender(this, guardianComponent.vpnManager)
    }

    /**
     * Mock api response from GuardianService and connection state from VpnManager
     */
    private fun GuardianComponentImpl.mockRemote(): GuardianComponentImpl {
        service = MockGuardianService()
        vpnManager = MockVpnManager()
        return this
    }
}

val Context.coreComponent: CoreComponent
    get() = (applicationContext as GuardianApp).coreComponent

val Context.guardianComponent: GuardianComponent
    get() = (applicationContext as GuardianApp).guardianComponent
