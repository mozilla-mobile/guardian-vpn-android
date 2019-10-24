package org.mozilla.firefox.vpn.servers.data

import org.mozilla.firefox.vpn.service.*
import org.mozilla.firefox.vpn.util.Result

class ServerRepository {

    private val guardianService = GuardianService.newInstance()

    suspend fun getServers(token: String): Result<ServerList> {
        val bearerToken = "Bearer $token"

        return try {
            val response = guardianService.getServers(bearerToken)
            response.resolveBody()
                .handleError(401) {
                    it?.toErrorBody()
                        ?.toUnauthorizedError()
                        ?: UnknownErrorBody(it)
                }
        } catch (e: Exception) {
            Result.Fail(RuntimeException("Unknown exception $e"))
        }
    }
}
