package org.mozilla.firefox.vpn.service

import com.google.gson.internal.bind.util.ISO8601Utils
import java.util.Calendar
import java.util.Date
import retrofit2.Response

class MockGuardianService : GuardianService {

    private val loginUrl = "https://www.google.com"
    private val verifyUrl = "https://www.google.com"
    private val ip4 = "127.0.0.1"
    private val ip6 = "0:0:0:0:0:0:0:1"

    private val devices = mutableListOf(
        DeviceInfo(
            "Old fake device",
            "old_fake_device_key",
            ip4,
            ip6,
            lastYear()
        )
    )

    private val subscription = Subscription(
        VpnInfo(
            true,
            lastYear(),
            nextYear()
        )
    )

    private val user = User(
        "Mock email",
        "Mock name",
        "https://miro.medium.com/max/1200/0*c_bcCIQ4G2gvzXX0.png",
        subscription,
        devices,
        5
    )

    private val servers = listOf(
        Server(
            "host1",
            "ip4",
            1,
            true,
            "public_key",
            listOf(listOf(25)),
            "gateway4",
            "gateway6"
        )
    )

    private val cities = listOf(
        City("Mock city", "LA", 0.0, 0.0, servers),
        City("Mock city 2", "LA", 0.0, 0.0, servers)
    )

    private val countries = listOf(Country("Mock country", "US", cities))

    override suspend fun verifyLogin(data: GuardianService.PostData): Response<LoginResult> {
        return Response.success(LoginResult(("mock_token")))
    }

    override suspend fun getUserInfo(connectTimeout: String, readTimeout: String): Response<User> {
        delay()
        return Response.success(user)
    }

    override suspend fun getServers(): Response<ServerList> {
        delay()
        return Response.success(ServerList(countries))
    }

    override suspend fun getVersions(): Response<Versions> {
        delay()
        return Response.success(Versions(emptyMap()))
    }

    override suspend fun addDevice(body: DeviceRequestBody): Response<DeviceInfo> {
        delay()
        val info = DeviceInfo(
            body.name,
            body.pubkey,
            ip4,
            ip6,
            now()
        )
        devices.add(info)
        return Response.success(info)
    }

    override suspend fun removeDevice(pubkey: String): Response<Unit> {
        delay()
        devices.removeAll { it.pubKey == pubkey }
        return Response.success(Unit)
    }

    private suspend fun delay(millis: Long = 500L) {
        kotlinx.coroutines.delay(millis)
    }

    private fun now(): String {
        return ISO8601Utils.format(Date())
    }

    private fun lastYear(): String {
        return ISO8601Utils.format(Calendar.getInstance().apply {
            add(Calendar.YEAR, -1)
        }.time)
    }

    private fun nextYear(): String {
        return ISO8601Utils.format(Calendar.getInstance().apply {
            add(Calendar.YEAR, 1)
        }.time)
    }
}
