package org.mozilla.firefox.vpn.servers.data

import android.content.SharedPreferences
import com.google.gson.Gson
import org.mozilla.firefox.vpn.servers.domain.FilterStrategy
import org.mozilla.firefox.vpn.servers.domain.GetServersUseCase
import org.mozilla.firefox.vpn.service.*
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.mapValue
import java.net.UnknownHostException

class ServerRepository(
    private val guardianService: GuardianService,
    private val prefs: SharedPreferences
) {

    /**
     * @return Result.Success(serverList) or Result.Fail(UnauthorizedException|NetworkException|Otherwise)
     */
    suspend fun getServers(token: String): Result<List<ServerInfo>> {
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

    companion object {
        private const val PREF_SERVER_SELECTED = "pref_selected_server"
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
