package org.mozilla.firefox.vpn.servers.domain

import org.mozilla.firefox.vpn.report.doReport
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.servers.data.ServerRepository
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.mapValue

class GetServersUseCase(
    private val serverRepository: ServerRepository
) {

    suspend operator fun invoke(filterStrategy: FilterStrategy): Result<List<ServerInfo>> {
        return serverRepository.getServers().mapValue { filterServers(it, filterStrategy) }
            .doReport(TAG)
    }

    private fun filterServers(servers: List<ServerInfo>, filterStrategy: FilterStrategy): List<ServerInfo> {
        return when (filterStrategy) {
            FilterStrategy.ByCountry -> filterCountry(servers)
            FilterStrategy.ByCity -> filterCity(servers)
            else -> servers
        }
    }

    private fun filterCountry(serverList: List<ServerInfo>): List<ServerInfo> {
        return serverList
            .groupBy { it.country }
            .flatMap { listOf(it.value.first()) }
    }

    private fun filterCity(serverList: List<ServerInfo>): List<ServerInfo> {
        return serverList
            .groupBy { it.country }
            .flatMap { countryList ->
                countryList.value
                    .groupBy { it.city }
                    .flatMap { listOf(it.value.first()) }
            }
    }

    companion object {
        private const val TAG = "GetServersUseCase"
    }
}

enum class FilterStrategy {
    ByCity,
    ByCountry,
    All,
}
