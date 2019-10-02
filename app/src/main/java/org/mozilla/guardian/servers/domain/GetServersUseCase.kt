package org.mozilla.guardian.servers.domain

import org.mozilla.guardian.servers.data.ServerRepository
import org.mozilla.guardian.user.data.*

class GetServersUseCase(
    private val userRepository: UserRepository,
    private val serverRepository: ServerRepository
) {

    suspend operator fun invoke(filterStrategy: FilterStrategy): Result<ServerList> {
        val token = userRepository.getToken() ?: return Result.Fail(UnauthorizedException)

        return when (val result = serverRepository.getServers(token)) {
            is Result.Success -> Result.Success(filterServers(result.value, filterStrategy))
            is Result.Fail -> result
        }
    }

    private fun filterServers(
        serverList: ServerList,
        filterStrategy: FilterStrategy
    ): ServerList {

        val filterCountries = mutableListOf<Country>()
        serverList.countries.forEach { country ->
            val filteredCities = mutableListOf<City>()
            filterCities(country, filterStrategy).forEach {
                val filteredCity = it.copy(servers = filterServers(it, filterStrategy))
                filteredCities.add(filteredCity)
            }

            val filteredCountry = country.copy(cities = filteredCities)
            filterCountries.add(filteredCountry)
        }
        return ServerList(filterCountries)
    }

    private fun filterCities(country: Country, filterStrategy: FilterStrategy): List<City> {
        return when (filterStrategy) {
            FilterStrategy.ByCountry -> country.cities.subList(0, 1)
            else -> country.cities
        }
    }

    private fun filterServers(city: City, filterStrategy: FilterStrategy): List<Server> {
        return when (filterStrategy) {
            FilterStrategy.All -> city.servers
            else -> city.servers.subList(0, 1)
        }
    }

    enum class FilterStrategy {
        ByCity,
        ByCountry,
        All,
    }
}
