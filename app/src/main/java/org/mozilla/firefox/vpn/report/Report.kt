package org.mozilla.firefox.vpn.report

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.FileProvider
import com.bosphere.filelogger.FL
import com.bosphere.filelogger.FLConfig
import com.bosphere.filelogger.FLConst
import java.io.File
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.service.User
import org.mozilla.firefox.vpn.user.data.UserInfo
import org.mozilla.firefox.vpn.user.data.isDeviceLimitReached
import org.mozilla.firefox.vpn.user.data.isSubscribed
import org.mozilla.firefox.vpn.util.GLog
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.onError
import org.mozilla.firefox.vpn.util.onSuccess

object ReportUtil {
    private const val DEFAULT_TAG = "GuardianReport"
    private const val LOG_DIR = "report"
    private const val LOG_FILE_NAME = "debug_logs.txt"

    fun initReport(context: Context) {
        val logDir = File(context.filesDir, LOG_DIR)
        if (!logDir.exists()) {
            logDir.mkdirs()
        }

        FL.init(
            FLConfig.Builder(context)
                .logger(null)
                .defaultTag(DEFAULT_TAG)
                .minLevel(FLConst.Level.V)
                .formatter(object : FLConfig.DefaultFormatter() {
                    override fun formatFileName(timeInMillis: Long): String {
                        return LOG_FILE_NAME
                    }
                })
                .logToFile(true)
                .dir(logDir)
                .retentionPolicy(FLConst.RetentionPolicy.TOTAL_SIZE)
                .maxTotalSize(FLConst.DEFAULT_MAX_TOTAL_SIZE)
                .build()
        )

        FL.setEnabled(true)
    }

    fun sendLog(activity: Activity, email: String, subject: String, body: String): Boolean {
        val logFile = getLogFile(activity) ?: return false
        val contentUri: Uri = FileProvider.getUriForFile(activity, getAuthority(activity), logFile)

        val intent = createSendIntent(contentUri, email, subject, body)
        val pkgMgr = activity.packageManager ?: return false

        if (intent.resolveActivity(pkgMgr) != null) {
            activity.startActivityForResult(intent, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            return true
        }

        intent.type = "*/*"
        if (intent.resolveActivity(pkgMgr) != null) {
            activity.startActivityForResult(intent, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            return true
        }

        return false
    }

    private fun getAuthority(context: Context): String {
        return "${context.packageName}.report"
    }

    private fun getLogFile(context: Context): File? {
        val logPath = File(context.filesDir, LOG_DIR)
        if (!logPath.exists()) {
            return null
        }
        val logFile = File(logPath, LOG_FILE_NAME)
        if (logFile.exists()) {
            return logFile
        }
        return null
    }

    private fun createSendIntent(
        fileUri: Uri,
        email: String,
        subject: String,
        body: String
    ) = Intent(Intent.ACTION_SEND).apply {
        val to = arrayOf(email)
        type = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
        putExtra(Intent.EXTRA_EMAIL, to)
        putExtra(Intent.EXTRA_STREAM, fileUri)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
}

inline fun <reified T : Any> Result<T>.doReport(
    tag: String,
    errorMsg: String? = null,
    successMsg: String? = null
): Result<T> {
    return this
        .onSuccess { data ->
            val dataString = "data=(${reportDataType(data)})"
            val msg = successMsg?.let { "msg=$it, $dataString" } ?: dataString
            reportSuccess(tag, msg)
        }
        .onError { e ->
            val errorString = "e=$e"
            val msg = errorMsg?.let { "msg=$it, $errorString" } ?: errorString
            reportError(tag, msg)
        }
}

inline fun <reified T : Any> reportDataType(value: T): String {
    return when (value) {
        is User -> value.toReport()
        is UserInfo -> value.toReport()
        is DeviceInfo -> value.toReport()
        is List<*> -> {
            val type = value.random()?.let { it::class.java.simpleName } ?: "Unknown"
            "List(type=$type, size=${value.size})"
        }
        else -> "${T::class.java.simpleName}, please impl toReport() explicitly"
    }
}

fun reportSuccess(tag: String, msg: String) {
    GLog.report(tag, "success($msg)")
}

fun reportError(tag: String, msg: String) {
    GLog.report(tag, "error($msg)")
}

fun User.toReport(): String {
    val subscribed = subscription.isSubscribed
    val numDevices = devices
    return "subscribed=$subscribed, numDevices=${numDevices.size}"
}

fun UserInfo.toReport(): String {
    val subscribed = isSubscribed
    val limitReached = isDeviceLimitReached
    return "subscribed=$subscribed, deviceLimitReached=$limitReached"
}

fun DeviceInfo.toReport(): String {
    return this.pubKey
}
