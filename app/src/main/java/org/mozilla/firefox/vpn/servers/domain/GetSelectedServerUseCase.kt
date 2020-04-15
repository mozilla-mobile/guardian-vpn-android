package org.mozilla.firefox.vpn.servers.domain

import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.servers.data.ServerRepository
import org.mozilla.firefox.vpn.util.Result

class GetSelectedServerUseCase(
    private val serverRepository: ServerRepository
) {

    operator fun invoke(filterStrategy: FilterStrategy, servers: List<ServerInfo> = emptyList()): Result<ServerInfo> {
        val selected = serverRepository.getSelectedServer()

        return if (selected == null) {
            defaultServerInfo(filterStrategy, servers)
        } else {
            findMatchedServerInfo(filterStrategy, servers, selected.server)
        }
    }

    private fun findMatchedServerInfo(filterStrategy: FilterStrategy, servers: List<ServerInfo>, selected: ServerInfo): Result<ServerInfo> {
        return if (servers.contains(selected)) {
            Result.Success(selected)
        } else {
            defaultServerInfo(filterStrategy, servers)
        }
    }

    private fun defaultServerInfo(filterStrategy: FilterStrategy, servers: List<ServerInfo>): Result<ServerInfo> {
        return if (servers.any { it.country.code == "us" }) {
            val selected = servers.filter { it.country.code == "us" }.random()
            serverRepository.setSelectedServer(filterStrategy, selected)
            Result.Success(selected)
        } else {
            servers.firstOrNull()?.let {
                serverRepository.setSelectedServer(filterStrategy, it)
                Result.Success(it)
            } ?: Result.Fail(RuntimeException("No server available"))
        }
    }
}
