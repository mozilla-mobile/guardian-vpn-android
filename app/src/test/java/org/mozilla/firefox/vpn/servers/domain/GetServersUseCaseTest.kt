package org.mozilla.firefox.vpn.servers.domain

import android.util.Log
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import java.lang.Exception
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mozilla.firefox.vpn.servers.data.ServerRepository
import org.mozilla.firefox.vpn.testServerInfos
import org.mozilla.firefox.vpn.testUSAServerInfo
import org.mozilla.firefox.vpn.testUSAServerInfo2
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.onError
import org.mozilla.firefox.vpn.util.onSuccess

class GetServersUseCaseTest {

    @MockK
    lateinit var serverRepository: ServerRepository
    lateinit var getServersUseCase: GetServersUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        getServersUseCase = GetServersUseCase(serverRepository)

        mockkStatic(Log::class)
        every { Log.isLoggable(any(), any()) } returns false
    }

    @Test
    fun `When the filter strategy is by country, it should return server list group by country`() {
        runBlocking {
            coEvery { serverRepository.getServers() } returns Result.Success(testServerInfos)

            val result = getServersUseCase(FilterStrategy.ByCountry)
            assertThat(result).isInstanceOf(Result.Success::class.java)
            result.onSuccess { list ->
                assertThat(list.size).isEqualTo(1)
                assertThat(list[0]).isEqualTo(testUSAServerInfo)
            }
        }
    }

    @Test
    fun `When the filter strategy is by city, it should return server list group by city`() {
        runBlocking {
            coEvery { serverRepository.getServers() } returns Result.Success(testServerInfos)

            val result = getServersUseCase(FilterStrategy.ByCity)
            assertThat(result).isInstanceOf(Result.Success::class.java)
            result.onSuccess { list ->
                assertThat(list[0]).isEqualTo(testUSAServerInfo)
                assertThat(list[1]).isEqualTo(testUSAServerInfo2)
            }
        }
    }

    @Test
    fun `When getting server from repository is fail, it should also return fail from use case`() {
        runBlocking {
            coEvery { serverRepository.getServers() } returns Result.Fail(Exception())

            val result = getServersUseCase(FilterStrategy.ByCountry)
            assertThat(result).isInstanceOf(Result.Fail::class.java)
            result.onError { e ->
                assertThat(e).isInstanceOf(Exception::class.java)
            }
        }
    }
}
