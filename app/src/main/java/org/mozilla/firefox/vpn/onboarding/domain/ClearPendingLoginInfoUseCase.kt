package org.mozilla.firefox.vpn.onboarding.domain

import android.content.SharedPreferences
import androidx.core.content.edit

class ClearPendingLoginInfoUseCase(
    private val prefs: SharedPreferences
) {

    operator fun invoke() {
        prefs.edit { remove(SetPendingLoginInfoUseCase.PREF_PENDING_LOGIN_INFO) }
    }
}
