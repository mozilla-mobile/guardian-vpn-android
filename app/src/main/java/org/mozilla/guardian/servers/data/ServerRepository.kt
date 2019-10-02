package org.mozilla.guardian.servers.data

import org.mozilla.guardian.user.data.*

class ServerRepository {

    private val guardianService = GuardianService.newInstance()

    suspend fun getServers(token: String): Result<ServerList> {
        val bearerToken = "Bearer $token"

        val response = guardianService.getServers(bearerToken)
        return if (response.isSuccessful) {
            val serverList = response.body()!!
            Result.Success(serverList)
        } else {
            when (val code = response.code()) {
                401 -> Result.Fail(UnauthorizedException)
                else -> Result.Fail(RuntimeException("Unknown response code $code"))
            }
        }
    }
}
