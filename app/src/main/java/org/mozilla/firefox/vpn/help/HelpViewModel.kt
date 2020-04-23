package org.mozilla.firefox.vpn.help

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hadilq.liveevent.LiveEvent
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import org.mozilla.firefox.vpn.BuildConfig
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.service.User
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.TimeFormat
import org.mozilla.firefox.vpn.util.TimeUtil

class HelpViewModel(
    private val app: Application,
    userRepository: UserRepository,
    deviceRepository: DeviceRepository
) : AndroidViewModel(app) {

    private val _uiState = MutableLiveData<UIState>()
    val uiState: LiveData<UIState> = _uiState

    val reportLog = LiveEvent<ReportInfo>()

    private val currentUser: User? = userRepository.getUserInfo()?.user
    private val currentDevice: DeviceInfo? = deviceRepository.getDevice()?.device

    init {
        val showDebugEntry = currentUser != null && currentDevice != null
        _uiState.value = UIState(isDebugEntryVisible = showDebugEntry)
    }

    fun reportLogClicked() {
        val user = currentUser ?: return
        val device = currentDevice ?: return

        val userEmail = user.email.takeIf { it.isNotBlank() } ?: return
        val userName = user.displayName.takeIf { it.isNotBlank() }
            ?: app.getString(R.string.settings_default_user_name)

        val createdAt = device.createdAt
        val createdDate = TimeUtil.parseOrNull(createdAt, TimeFormat.Iso8601)?.let { date ->
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZ", Locale.US)
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
                .format(date)
        } ?: createdAt

        reportLog.value = ReportInfo(
            email = EMAIL_SUPPORT,
            subject = app.getString(R.string.log_report_subject, userName, userEmail),
            body = app.getString(
                R.string.log_report_body,
                device.name,
                createdDate,
                device.pubKey
            )
        )
    }

    data class UIState(
        val isDebugEntryVisible: Boolean
    )

    data class ReportInfo(
        val email: String,
        val subject: String,
        val body: String
    )

    companion object {
        private val EMAIL_SUPPORT = if (BuildConfig.DEBUG) {
            "debug_mail"
        } else {
            "firefox-team@mozilla.com"
        }
    }
}
