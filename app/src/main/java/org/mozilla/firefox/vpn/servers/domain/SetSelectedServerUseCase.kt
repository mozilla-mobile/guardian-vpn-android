package org.mozilla.firefox.vpn.servers.domain

import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.servers.data.ServerRepository

class SetSelectedServerUseCase(
    private val serverRepository: ServerRepository
) {

    operator fun invoke(filterStrategy: FilterStrategy, item: ServerInfo) {
        serverRepository.setSelectedServer(filterStrategy, item)
    }
}