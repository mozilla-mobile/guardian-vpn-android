package org.mozilla.firefox.vpn.servers.domain

import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.servers.data.ServerRepository
import org.mozilla.firefox.vpn.util.Result

class GetSelectedServerUseCase(
    private val serverRepository: ServerRepository
) {

    operator fun invoke(servers: List<ServerInfo> = emptyList()): Result<ServerInfo> {
        val selected = serverRepository.getSelectedServer()

        return if (selected == null) {
            defaultServerInfo(servers)
        } else {
            findMatchedServerInfo(servers, selected.server)
        }
    }

    private fun findMatchedServerInfo(servers: List<ServerInfo>, selected: ServerInfo): Result<ServerInfo> {
        return if (servers.contains(selected)) {
            Result.Success(selected)
        } else {
            defaultServerInfo(servers)
        }
    }

    private fun defaultServerInfo(servers: List<ServerInfo>): Result<ServerInfo> {
        return if (servers.any { it.country.code == "us" }) {
            Result.Success(servers.filter { it.country.code == "us" }.random())
        } else {
            servers.firstOrNull()?.let {
                Result.Success(it)
            } ?: Result.Fail(RuntimeException("No server available"))
        }
    }
}
