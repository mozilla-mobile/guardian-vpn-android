package org.mozilla.firefox.vpn.onboarding.domain

import android.content.SharedPreferences
import com.google.gson.Gson
import org.mozilla.firefox.vpn.service.LoginInfo

class GetPendingLoginInfoUseCase(
    private val prefs: SharedPreferences
) {

    operator fun invoke(): LoginInfo? {
        return prefs.getString(SetPendingLoginInfoUseCase.PREF_PENDING_LOGIN_INFO, null)
            ?.let {
                try {
                    Gson().fromJson(it, LoginInfo::class.java)
                } catch (e: Exception) {
                    null
                }
            }
    }
}
