package org.mozilla.firefox.vpn.onboarding.domain

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import org.mozilla.firefox.vpn.service.LoginInfo

class SetPendingLoginInfoUseCase(
    private val prefs: SharedPreferences
) {

    operator fun invoke(info: LoginInfo) {
        prefs.edit { putString(PREF_PENDING_LOGIN_INFO, Gson().toJson(info)) }
    }

    companion object {
        const val PREF_PENDING_LOGIN_INFO = "pending_login_info"
    }
}
