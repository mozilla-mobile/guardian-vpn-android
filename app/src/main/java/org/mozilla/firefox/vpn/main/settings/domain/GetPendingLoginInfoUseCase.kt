package org.mozilla.firefox.vpn.main.settings.domain

import android.content.SharedPreferences
import androidx.core.content.edit

class GetGleanUseCase(
    private val prefs: SharedPreferences
) {

    suspend operator fun invoke(): Boolean {
        return prefs.getBoolean(PREF_BOOL_GLEAN_ENABLED, false)
    }

    companion object {
        private const val PREF_BOOL_GLEAN_ENABLED = "pref_bool_glean_enabled"
    }
}
