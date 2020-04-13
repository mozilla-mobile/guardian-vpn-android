package org.mozilla.firefox.vpn.main.vpn.domain

import kotlin.random.Random
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.servers.domain.FilterStrategy
import org.mozilla.firefox.vpn.servers.domain.GetServersUseCase
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.getOrNull
import org.mozilla.firefox.vpn.util.then

class ResolveDispatchableServerUseCase(
    private val getServersUseCase: GetServersUseCase
) {

    suspend operator fun invoke(server: ServerInfo): ServerInfo? {
        return getServersUseCase(FilterStrategy.All)
            .then { servers ->
                val matched = servers.filter { it.country == server.country && it.city == server.city }
                selectServerByWeight(matched)?.let { Result.Success(it) } ?: Result.Fail(NoSuchElementException())
            }
            .getOrNull()
    }

    private fun selectServerByWeight(servers: List<ServerInfo>): ServerInfo? {
        val weightSum = servers.sumBy { it.server.weight }
        if (weightSum < 0) { return null }

        var randomNumber = Random.nextInt(0, weightSum + 1)
        servers.forEach {
            randomNumber -= it.server.weight
            if (randomNumber <= 0) {
                return it
            }
        }
        return null
    }
}
