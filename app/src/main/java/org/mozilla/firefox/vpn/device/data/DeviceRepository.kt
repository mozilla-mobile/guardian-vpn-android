package org.mozilla.firefox.vpn.device.data

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.wireguard.crypto.KeyPair
import org.mozilla.firefox.vpn.user.data.*
import java.lang.RuntimeException

class DeviceRepository(
    private val appContext: Context
) {

    private val guardianService = GuardianService.newInstance()

    suspend fun addDevice(name: String, token: String): Result<DeviceInfo> {
        val keyPair = KeyPair()
        val response = guardianService.addDevice(DeviceRequestBody(name, keyPair.publicKey.toBase64()), token)
        savePrivateKey(keyPair.privateKey.toBase64())

        return if (response.isSuccessful) {
            response.body()?.let {
                saveDevice(it)
                Result.Success(it)
            } ?: Result.Fail(UnknownException("empty response body"))
        } else {
            when (val code = response.code()) {
                400, 401 -> Result.Fail(RuntimeException("code=$code, msg=${response.errorBody()?.string()}"))
                else -> Result.Fail(UnknownException("Unknown status code: $code"))
            }
        }
    }

    suspend fun removeDevice(pubKey: String, token: String): Result<Unit> {
        val response = guardianService.removeDevice(pubKey, token)

        return if (response.isSuccessful) {
            return Result.Success(Unit)
        } else {
            when (val code = response.code()) {
                400 -> Result.Fail(RuntimeException("code=$code, msg=${response.errorBody()?.string()}"))
                401 -> Result.Fail(UnauthorizedException)
                else -> Result.Fail(UnknownException("Unknown status code: $code"))
            }
        }
    }

    fun getPrivateKey(): String? {
        return PreferenceManager.getDefaultSharedPreferences(appContext).getString(PREF_PRIVATE_KEY, null)
    }

    private fun savePrivateKey(privateKey: String) {
        PreferenceManager.getDefaultSharedPreferences(appContext).edit()
            .putString(PREF_PRIVATE_KEY, privateKey)
            .apply()
    }

    fun getDevice(): DeviceInfo? {
        val json = PreferenceManager.getDefaultSharedPreferences(appContext)
            .getString(PREF_DEVICE, null) ?: return null
        return Gson().fromJson(json, DeviceInfo::class.java)
    }

    private fun saveDevice(device: DeviceInfo) {
        PreferenceManager.getDefaultSharedPreferences(appContext).edit()
            .putString(PREF_DEVICE, Gson().toJson(device))
            .apply()
    }

    companion object {
        private const val PREF_DEVICE = "pref_device_info"
        private const val PREF_PRIVATE_KEY = "pref_private_key"
    }
}
