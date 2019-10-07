package org.mozilla.firefox.vpn.util

import android.util.Log
import org.mozilla.firefox.vpn.BuildConfig

private const val TAG_GENERAL_LOG = "Guardian"

@Suppress("ClassName")
class GLog {

    object i : BaseLog(Log.INFO, { tag, msg ->
        Log.i(tag, msg)
    })

    object d : BaseLog(Log.DEBUG, { tag, msg ->
        Log.d(tag, msg)
    })

    object e : BaseLog(Log.ERROR, { tag, msg ->
        Log.e(tag, msg)
    })

    object v : BaseLog(Log.VERBOSE, { tag, msg ->
        Log.v(tag, msg)
    })

    object w : BaseLog(Log.WARN, { tag, msg ->
        Log.w(tag, msg)
    })

    abstract class BaseLog(
        private val level: Int,
        private val doLog: (tag: String, msg: String) -> Unit
    ) {

        operator fun invoke(msg: String) {
            checkLoggable(TAG_GENERAL_LOG, level) {
                doLog(TAG_GENERAL_LOG, msg)
            }
        }

        operator fun invoke(tag: String, msg: String) {
            checkLoggable(tag, level) {
                doLog(tag, msg)
            }
        }

        private fun checkLoggable(tag: String, level: Int, action: () -> Unit) {
            if (BuildConfig.DEBUG && Log.isLoggable(tag, level)) {
                action()
            }
        }
    }
}
