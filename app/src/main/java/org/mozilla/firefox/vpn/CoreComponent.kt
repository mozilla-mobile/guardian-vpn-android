package org.mozilla.firefox.vpn

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

interface CoreComponent {
    val app: Application
    val prefs: SharedPreferences
}

class CoreComponentImpl(
    override val app: Application
) : CoreComponent {

    override val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(app)
    }
}
