package org.mozilla.firefox.vpn.servers.data

import org.mozilla.firefox.vpn.user.data.*

class ServerRepository {

    private val guardianService = GuardianService.newInstance()

    suspend fun getServers(token: String): Result<ServerList> {
        val bearerToken = "Bearer $token"

        return try {
            val response = guardianService.getServers(bearerToken)
            if (response.isSuccessful) {
                val serverList = response.body()!!
                Result.Success(serverList)
            } else {
                when (val code = response.code()) {
                    401 -> Result.Fail(UnauthorizedException)
                    else -> Result.Fail(RuntimeException("Unknown response code $code"))
                }
            }
        } catch (e: Exception) {
            Result.Fail(e)
        }
    }
}
