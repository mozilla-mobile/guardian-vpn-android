package org.mozilla.firefox.vpn

import android.util.Log
import org.mozilla.firefox.vpn.util.GLog

object CrashHandler : Thread.UncaughtExceptionHandler {

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    fun init() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        logException(e)
        defaultHandler?.uncaughtException(t, e)
    }

    private fun logException(e: Throwable) {
        GLog.report(Log.getStackTraceString(e))
    }
}
