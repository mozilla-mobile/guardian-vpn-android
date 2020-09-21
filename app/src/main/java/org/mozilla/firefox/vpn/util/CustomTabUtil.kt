package org.mozilla.firefox.vpn.util

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.ArrayDeque
import java.util.Queue
import kotlin.coroutines.resume
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class LoginCustomTab(private val activity: AppCompatActivity) : DefaultLifecycleObserver {

    private var client: CustomTabsClient? = null
    private var session: CustomTabsSession? = null
    private var connection: CustomTabsServiceConnection? = null

    init {
        GLog.d(TAG, "observe lifecycle")
        activity.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        GLog.d(TAG, "onCreate: warmUp")
        warmUp()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        GLog.d(TAG, "onDestroy: unbind")
        connection?.let { activity.unbindService(it) }
        activity.lifecycle.removeObserver(this)
        super.onDestroy(owner)
    }

    fun mayLaunchUrl(url: String) {
        GLog.d(TAG, "prepare url: $url")
        session?.mayLaunchUrl(Uri.parse(url), null, null)
    }

    fun launchUrl(url: String) {
        GLog.d(TAG, "launchUrl: $url")
        CustomTabsIntent.Builder(session).build().launchUrl(activity, Uri.parse(url))
    }

    private fun warmUp() {
        val pkg = getTargetPackage() ?: return
        connection = connection ?: createConnection()
        GLog.d(TAG, "bindService: pkg=$pkg")
        CustomTabsClient.bindCustomTabsService(activity, pkg, connection!!)
    }

    private fun createConnection(): CustomTabsServiceConnection {
        return object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                GLog.d(TAG, "service connected")
                client.warmup(0)
                this@LoginCustomTab.session = client.newSession(customTabsCallback)
                this@LoginCustomTab.client = client
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                GLog.d(TAG, "service disconnected")
                this@LoginCustomTab.client = null
                this@LoginCustomTab.session = null
            }
        }
    }

    private fun getTargetPackage(): String? {
        return CustomTabsClient.getPackageName(activity, customTabCandidates, true)
    }

    companion object {
        private const val TAG = "LoginCustomTab"
        val customTabCandidates = listOf(
            "org.mozilla.fenix.nightly",
            "org.mozilla.fenix",
            "org.mozilla.fennec_aurora",
            "org.mozilla.firefox_beta",
            "org.mozilla.firefox",
            "org.mozilla.focus",
            "com.android.chrome"
        )

        /**
         * Set a [CompletableDeferred] that will complete the next time custom tabs are closed.
         *
         * IMPORTANT: this should be set _before_ custom tabs are opened.
         *
         * Note that custom tab callbacks are asynchronous and _extremely_ slow.  This
         * class attempts to handle cases where the user quickly opens and closes their
         * tabs, but if you encounter problems around that use case this is a good
         * place to look.
         *
         * This is static because the alternative was a similarly bad practice: weaving
         * the reference through many layers of Android framework code.
         */
        fun setCustomTabsClosedEvent(authCodeReceived: CompletableDeferred<Unit>) {
            queuedCloses.add(authCodeReceived)
        }
        private val queuedCloses: Queue<CompletableDeferred<Unit>> = ArrayDeque()
    }

    private val customTabsCallback = object : CustomTabsCallback() {
        private var openCount = 0

        override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
            when (navigationEvent) {
                // From testing, this seems to always be sent before TAB_HIDDEN
                TAB_SHOWN -> { openCount++ }
                // If the user clicks the button multiple times, we may have more completables
                // than we do TAB_SHOWN events.  Also note that these callbacks are
                // asynchronous and extremely slow, and from testing do not always occur in
                // order, or even at all.  E.g., a very fast `open -> close -> open` might
                // send `SHOWN -> SHOWN`.
                //
                // As a result, we are very conservative here and only close as many
                // completables as we have seen SHOWN events.  Others will be automatically
                // canceled when their parent job ends anyway.
                //
                // Be very careful changing this.  Mistakes here can cause auth to hang
                // indefinitely, as requests are immediately canceled due to extra calls to
                // `complete`.
                TAB_HIDDEN -> {
                    openCount--
                    queuedCloses.poll()?.complete(Unit)

                    var head = queuedCloses.peek()
                    while (head != null && openCount > 0) {
                        head = queuedCloses.poll()
                        head?.complete(Unit)
                        openCount--
                    }
                }
                else -> { /* Do nothing */
                }
            }
        }
    }
}

suspend fun Fragment.launchUrl(url: String): Boolean {
    return withContext(Dispatchers.Main) {
        val activity = activity ?: return@withContext false
        val session = initCustomTabService(activity) ?: return@withContext false
        launchUrl(activity, session, url)
        return@withContext true
    }
}

suspend fun Activity.launchUrl(url: String): Boolean {
    return withContext(Dispatchers.Main) {
        val session = initCustomTabService(this@launchUrl) ?: return@withContext false
        launchUrl(this@launchUrl, session, url)
        return@withContext true
    }
}

private suspend fun initCustomTabService(
    context: Context
) = suspendCancellableCoroutine<CustomTabsSession?> {
    val pkg = getTargetPackage(context) ?: return@suspendCancellableCoroutine it.resume(null)

    val isSuccess = CustomTabsClient.bindCustomTabsService(context, pkg, object : CustomTabsServiceConnection() {

        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            if (it.isActive) {
                client.warmup(0)
                it.resume(client.newSession(null))
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    })

    if (!isSuccess) {
        return@suspendCancellableCoroutine it.resume(null)
    }
}

private fun launchUrl(context: Context, session: CustomTabsSession, url: String) {
    CustomTabsIntent.Builder(session).build().launchUrl(context, Uri.parse(url))
}

private fun getTargetPackage(context: Context): String? {
    return CustomTabsClient.getPackageName(context, LoginCustomTab.customTabCandidates, true)
}
