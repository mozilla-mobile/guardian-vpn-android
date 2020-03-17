package org.mozilla.firefox.vpn

import android.app.Application
import android.content.Context
import com.bosphere.filelogger.FL
import com.bosphere.filelogger.FLConfig
import com.bosphere.filelogger.FLConst
import java.io.File
import org.mozilla.firefox.vpn.main.vpn.MockVpnManager
import org.mozilla.firefox.vpn.main.vpn.VpnNotificationSender
import org.mozilla.firefox.vpn.service.MockGuardianService
import org.mozilla.firefox.vpn.util.NotificationUtil
import org.mozilla.firefox.vpn.util.TAG_GENERAL_LOG

class GuardianApp : Application() {

    val coreComponent: CoreComponent by lazy {
        CoreComponentImpl(this)
    }

    lateinit var guardianComponent: GuardianComponent
    lateinit var vpnNotificationSender: VpnNotificationSender

    override fun onCreate() {
        super.onCreate()

        initReport()

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

    private fun initReport() {
        val logDir = File(filesDir, "report")
        if (!logDir.exists()) {
            logDir.mkdirs()
        }

        FL.init(FLConfig.Builder(this)
            .logger(null)
            .defaultTag(TAG_GENERAL_LOG)
            .minLevel(FLConst.Level.V)
            .formatter(object : FLConfig.DefaultFormatter() {
                override fun formatFileName(timeInMillis: Long): String {
                    return "log-report.txt"
                }
            })
            .logToFile(true)
            .dir(logDir)
            .retentionPolicy(FLConst.RetentionPolicy.TOTAL_SIZE)
            .maxTotalSize(FLConst.DEFAULT_MAX_TOTAL_SIZE)
            .build())

        FL.setEnabled(true)
    }
}

val Context.coreComponent: CoreComponent
    get() = (applicationContext as GuardianApp).coreComponent

val Context.guardianComponent: GuardianComponent
    get() = (applicationContext as GuardianApp).guardianComponent
