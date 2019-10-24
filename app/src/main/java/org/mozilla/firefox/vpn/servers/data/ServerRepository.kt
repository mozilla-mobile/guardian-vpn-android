package org.mozilla.firefox.vpn.servers.data

import org.mozilla.firefox.vpn.service.*
import org.mozilla.firefox.vpn.util.Result
import java.net.UnknownHostException

class ServerRepository {

    private val guardianService = GuardianService.newInstance()

    /**
     * @return Result.Success(serverList) or Result.Fail(UnauthorizedException|NetworkException|Otherwise)
     */
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
        } catch (e: UnknownHostException) {
            Result.Fail(NetworkException)
        } catch (e: Exception) {
            Result.Fail(UnknownException("Unknown exception $e"))
        }
    }
}
