package org.mozilla.firefox.vpn.main.vpn.domain

import android.content.SharedPreferences
import org.mozilla.firefox.vpn.service.Version

class SetLatestUpdateMessageUseCase(private val pref: SharedPreferences) {

    operator fun invoke(version: Version) {
        pref.edit().putInt(PREF_LATEST_UPDATE_MESSAGE, version.version.toInt()).apply()
    }

    companion object {
        const val PREF_LATEST_UPDATE_MESSAGE = "latest_update_msg"
    }
}
