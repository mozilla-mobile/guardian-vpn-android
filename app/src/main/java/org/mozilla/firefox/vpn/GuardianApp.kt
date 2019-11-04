package org.mozilla.firefox.vpn

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import androidx.preference.PreferenceManager
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.model.TunnelManager
import com.wireguard.android.util.AsyncWorker
import org.mozilla.firefox.vpn.backend.FileConfigStore
import org.mozilla.firefox.vpn.main.MainActivity

class GuardianApp : Application() {

    lateinit var backend: GoBackend
    internal lateinit var tunnelManager: TunnelManager
    private lateinit var asyncWorker: AsyncWorker

    val coreComponent: CoreComponent by lazy {
        CoreComponentImpl(this)
    }

    lateinit var guardianComponent: GuardianComponent

    override fun onCreate() {
        super.onCreate()

        guardianComponent = GuardianComponentImpl(coreComponent)

        backend = GoBackend(this, ComponentName(this, MainActivity::class.java))

        asyncWorker = AsyncWorker(AsyncTask.SERIAL_EXECUTOR, Handler(Looper.getMainLooper()))

        tunnelManager = TunnelManager(
            this,
            PreferenceManager.getDefaultSharedPreferences(this),
            backend,
            FileConfigStore(),
            asyncWorker
        )
        tunnelManager.onCreate()

    }
}

val Context.coreComponent: CoreComponent
    get() = (applicationContext as GuardianApp).coreComponent

val Context.guardianComponent: GuardianComponent
    get() = (applicationContext as GuardianApp).guardianComponent
