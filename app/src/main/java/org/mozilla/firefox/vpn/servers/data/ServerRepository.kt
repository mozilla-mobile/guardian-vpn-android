package org.mozilla.firefox.vpn.servers.data

import android.content.SharedPreferences
import com.google.gson.Gson
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import org.mozilla.firefox.vpn.servers.domain.FilterStrategy
import org.mozilla.firefox.vpn.service.City
import org.mozilla.firefox.vpn.service.Country
import org.mozilla.firefox.vpn.service.GuardianService
import org.mozilla.firefox.vpn.service.NetworkException
import org.mozilla.firefox.vpn.service.Server
import org.mozilla.firefox.vpn.service.UnknownErrorBody
import org.mozilla.firefox.vpn.service.UnknownException
import org.mozilla.firefox.vpn.service.handleError
import org.mozilla.firefox.vpn.service.resolveBody
import org.mozilla.firefox.vpn.service.toErrorBody
import org.mozilla.firefox.vpn.service.toUnauthorizedError
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.mapValue
import org.mozilla.firefox.vpn.util.onSuccess

class ServerRepository(
    private val guardianService: GuardianService,
    private val prefs: SharedPreferences
) {

    /**
     * @return Result.Success(serverList) or Result.Fail(UnauthorizedException|NetworkException|Otherwise)
     */
    suspend fun getServers(token: String): Result<List<ServerInfo>> {
        getCachedServers()
            ?.takeIf { System.currentTimeMillis() - it.lastUpdate < TimeUnit.DAYS.toMillis(1) }
            ?.takeIf { it.servers.isNotEmpty() }
            ?.let { return Result.Success(it.servers) }

        val bearerToken = "Bearer $token"

        return try {
            val response = guardianService.getServers(bearerToken)
            response.resolveBody()
                .mapValue { serverList ->
                    serverList.countries
                        .flatMap { country ->
                            country.cities.map { Triple(country, it, null) }
                        }
                        .flatMap { (country, city, _) ->
                            city.servers.map { Triple(country, city, it) }
                        }
                        .map { (country, city, server) ->
                            ServerInfo(country.toInfo(), city.toInfo(), server)
                        }
                }.onSuccess {
                    cacheServers(it)
                }
                .handleError(401) {
                    it?.toErrorBody()
                        ?.toUnauthorizedError()
                        ?: UnknownErrorBody(it)
                }
        } catch (e: UnknownHostException) {
            Result.Fail(NetworkException)
        } catch (e: Exception) {
            Result.Fail(UnknownException("Unknown exception $e"))
        }
    }

    fun setSelectedServer(strategy: FilterStrategy, server: ServerInfo) {
        val selected = SelectedServer(strategy, server)
        val json = Gson().toJson(selected)
        prefs.edit().putString(PREF_SERVER_SELECTED, json).apply()
    }

    fun getSelectedServer(): SelectedServer? {
        return prefs.getString(PREF_SERVER_SELECTED, null)?.let {
            Gson().fromJson(it, SelectedServer::class.java)
        }
    }

    private fun cacheServers(servers: List<ServerInfo>) {
        val json = Gson().toJson(ServersCache(servers, System.currentTimeMillis()))
        prefs.edit().putString(PREF_SERVERS, json).apply()
    }

    private fun getCachedServers(): ServersCache? {
        return prefs.getString(PREF_SERVERS, null)?.let {
            Gson().fromJson(it, ServersCache::class.java)
        }
    }

    companion object {
        private const val PREF_SERVER_SELECTED = "pref_selected_server"
        private const val PREF_SERVERS = "pref_servers"
    }
}

data class CountryInfo(
    val name: String,
    val code: String
) {
    override fun toString(): String {
        return name
    }
}

private fun Country.toInfo(): CountryInfo {
    return CountryInfo(name, code)
}

data class CityInfo(
    val name: String,
    val code: String,
    val latitude: Double,
    val longitude: Double
) {
    override fun toString(): String {
        return name
    }
}

private fun City.toInfo(): CityInfo {
    return CityInfo(name, code, latitude, longitude)
}

data class ServerInfo(
    val country: CountryInfo,
    val city: CityInfo,
    val server: Server
) {
    override fun toString(): String {
        return "$country - $city - ${server.hostName}"
    }
}

data class SelectedServer(
    val strategy: FilterStrategy,
    val server: ServerInfo
)

data class ServersCache(
    val servers: List<ServerInfo>,
    val lastUpdate: Long
)
