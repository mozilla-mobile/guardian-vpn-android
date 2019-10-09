package org.mozilla.firefox.vpn.util

import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParsePosition
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

object TimeUtil {

    private const val TAG = "TimeUtil"

    fun now(timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Date {
        return Calendar.getInstance(timeZone).time
    }

    fun parse(time: String, format: TimeFormat): Date? {
        return try {
            format.parse(time)
        } catch (e: Exception) {
            GLog.d(TAG, "parse exception: $e")
            null
        }
    }
}

interface TimeFormat {
    fun parse(time: String): Date

    object Iso8601 : TimeFormat {
        override fun parse(time: String): Date {
            return ISO8601Utils.parse(time, ParsePosition(0))
        }
    }
}
