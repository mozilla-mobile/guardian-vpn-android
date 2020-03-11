package org.mozilla.firefox.vpn.servers.domain

import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.mozilla.firefox.vpn.servers.data.SelectedServer
import org.mozilla.firefox.vpn.servers.data.ServerRepository
import org.mozilla.firefox.vpn.testCanadaServerInfo
import org.mozilla.firefox.vpn.testServerInfos
import org.mozilla.firefox.vpn.testServerInfos2
import org.mozilla.firefox.vpn.testUSAServerInfo
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.onError
import org.mozilla.firefox.vpn.util.onSuccess

class GetSelectedServerUseCaseTest {

    @MockK
    lateinit var serverRepository: ServerRepository
    lateinit var getSelectedServerUseCase: GetSelectedServerUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        getSelectedServerUseCase = GetSelectedServerUseCase(serverRepository)
    }

    @Test
    fun `When the server list contains the selected server, it should return the selected server`() {
        every { serverRepository.getSelectedServer() } returns SelectedServer(FilterStrategy.ByCity, testUSAServerInfo)

        val result = getSelectedServerUseCase(testServerInfos)
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result.onSuccess { server ->
            assertThat(server).isEqualTo(testUSAServerInfo)
        }
    }

    @Test
    fun `When the server list doesn't contain the selected server, but has at least one US server, it should return random one US server`() {
        every { serverRepository.getSelectedServer() } returns SelectedServer(FilterStrategy.ByCity, testCanadaServerInfo)

        val result = getSelectedServerUseCase(testServerInfos)
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result.onSuccess { server ->
            assertThat(server.country.code).isEqualTo("us")
        }
    }

    @Test
    fun `When the server list doesn't contain the selected server, and there is no US server, it should return the first order of server list`() {
        every { serverRepository.getSelectedServer() } returns SelectedServer(FilterStrategy.ByCity, testUSAServerInfo)

        val result = getSelectedServerUseCase(testServerInfos2)
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result.onSuccess { server ->
            assertThat(server).isEqualTo(testServerInfos2[0])
        }
    }

    @Test
    fun `When the selected server is null, it should return one server from server list`() {
        every { serverRepository.getSelectedServer() } returns null

        val result = getSelectedServerUseCase(testServerInfos2)
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result.onSuccess { server ->
            assertThat(server).isEqualTo(testCanadaServerInfo)
        }
    }

    @Test
    fun `When the server list is empty, it should return fail exception`() {
        every { serverRepository.getSelectedServer() } returns SelectedServer(FilterStrategy.ByCity, testUSAServerInfo)

        val result = getSelectedServerUseCase(emptyList())
        assertThat(result).isInstanceOf(Result.Fail::class.java)
        result.onError { e ->
            assertThat(e.message).isEqualTo("No server available")
        }
    }
}
