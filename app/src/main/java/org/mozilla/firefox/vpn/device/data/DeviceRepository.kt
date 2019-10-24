package org.mozilla.firefox.vpn.device.data

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.wireguard.crypto.KeyPair
import org.mozilla.firefox.vpn.service.*
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.onSuccess
import java.net.UnknownHostException

class DeviceRepository(
    private val appContext: Context
) {

    private val guardianService = GuardianService.newInstance()

    /**
     * @return Result.Success(deviceInfo) or Result.Fail(UnauthorizedException|DeviceApiError|NetworkException|Otherwise)
     */
    suspend fun addDevice(name: String, token: String): Result<DeviceInfo> {
        val keyPair = KeyPair()
        val response = guardianService.addDevice(DeviceRequestBody(name, keyPair.publicKey.toBase64()), token)

        return try {
            response.resolveBody()
                .onSuccess {
                    saveDevice(CurrentDevice(it, keyPair.privateKey.toBase64()))
                }
                .handleError(400) {
                    it?.toErrorBody()
                        ?.toDeviceApiError()
                        ?: UnknownErrorBody(it)
                }
                .handleError(401) {
                    it?.toErrorBody()
                        ?.toUnauthorizedError()
                        ?: UnknownErrorBody(it)
                }
        } catch (e: UnknownHostException) {
            Result.Fail(NetworkException)
        } catch (e: Exception) {
            Result.Fail(UnknownException("Unknown exception=$e"))
        }
    }

    /**
     * @return Result.Success(Unit) or Result.Fail(UnauthorizedException|DeviceApiError|NetworkException|Otherwise)
     */
    suspend fun removeDevice(pubKey: String, token: String): Result<Unit> {
        val response = guardianService.removeDevice(pubKey, token)

        return try {
            response.resolveBody()
                .handleError(400) {
                    it?.toErrorBody()
                        ?.toDeviceApiError()
                        ?: UnknownErrorBody(it)
                }
                .handleError(401) {
                    it?.toErrorBody()
                        ?.toUnauthorizedError()
                        ?: UnknownErrorBody(it)
                }
        } catch (e: UnknownHostException) {
            Result.Fail(NetworkException)
        } catch (e: Exception) {
            Result.Fail(UnknownException("Unknown exception=$e"))
        }
    }

    private fun saveDevice(device: CurrentDevice) {
        PreferenceManager.getDefaultSharedPreferences(appContext).edit()
            .putString(PREF_DEVICE, Gson().toJson(device))
            .apply()
    }

    fun getDevice(): CurrentDevice? {
        val json = PreferenceManager.getDefaultSharedPreferences(appContext)
            .getString(PREF_DEVICE, null) ?: return null
        return Gson().fromJson(json, CurrentDevice::class.java)
    }

    companion object {
        private const val PREF_DEVICE = "pref_current_device"
    }
}

data class CurrentDevice(
    val device: DeviceInfo,
    val privateKeyBase64: String
)
