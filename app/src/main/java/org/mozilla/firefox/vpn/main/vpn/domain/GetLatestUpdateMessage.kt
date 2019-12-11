package org.mozilla.firefox.vpn.main.vpn.domain

import android.content.SharedPreferences

class GetLatestUpdateMessage(private val pref: SharedPreferences) {

    operator fun invoke(): Int {
        return pref.getInt(SetLatestUpdateMessageUseCase.PREF_LATEST_UPDATE_MESSAGE, 0)
    }
}
