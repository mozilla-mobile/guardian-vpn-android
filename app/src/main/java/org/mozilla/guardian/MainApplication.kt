package org.mozilla.guardian

import android.app.Application
import android.content.ComponentName
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import androidx.preference.PreferenceManager
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.model.TunnelManager
import com.wireguard.android.util.AsyncWorker
import org.mozilla.guardian.backend.FileConfigStore
import org.mozilla.guardian.main.MainActivity

class MainApplication : Application() {

    lateinit var backend: GoBackend
    internal lateinit var tunnelManager: TunnelManager
    private lateinit var asyncWorker: AsyncWorker

    override fun onCreate() {
        super.onCreate()


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