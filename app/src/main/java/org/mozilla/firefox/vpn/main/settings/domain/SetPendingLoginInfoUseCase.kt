package org.mozilla.firefox.vpn.main.settings.domain

import android.content.SharedPreferences
import androidx.core.content.edit

class SetGleanUseCase(
    private val prefs: SharedPreferences
) {

    operator fun invoke(enable: Boolean) {
        prefs.edit { putBoolean(PREF_BOOL_GLEAN_ENABLED, enable) }
    }

    companion object {
        const val PREF_BOOL_GLEAN_ENABLED = "pref_bool_glean_enabled"
    }
}
