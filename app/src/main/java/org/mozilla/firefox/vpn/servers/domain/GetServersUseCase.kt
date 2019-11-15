package org.mozilla.firefox.vpn.servers.domain

import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.servers.data.ServerRepository
import org.mozilla.firefox.vpn.service.UnauthorizedException
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.mapValue

class GetServersUseCase(
    private val userRepository: UserRepository,
    private val serverRepository: ServerRepository
) {

    suspend operator fun invoke(filterStrategy: FilterStrategy): Result<List<ServerInfo>> {
        val token = userRepository.getUserInfo()?.token ?: return Result.Fail(UnauthorizedException())
        return serverRepository.getServers(token).mapValue { filterServers(it, filterStrategy) }
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
}

enum class FilterStrategy {
    ByCity,
    ByCountry,
    All,
}
