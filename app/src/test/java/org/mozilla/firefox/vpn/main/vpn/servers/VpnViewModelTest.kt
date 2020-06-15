package org.mozilla.firefox.vpn.main.vpn.servers

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.firefox.vpn.main.vpn.VpnViewModel
import org.mozilla.firefox.vpn.servers.data.SelectedServer
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.servers.data.ServerRepository
import org.mozilla.firefox.vpn.servers.domain.FilterStrategy
import org.mozilla.firefox.vpn.servers.domain.GetSelectedServerUseCase
import org.mozilla.firefox.vpn.servers.domain.GetServersUseCase
import org.mozilla.firefox.vpn.servers.domain.SelectedServerProvider
import org.mozilla.firefox.vpn.servers.domain.SetSelectedServerUseCase
import org.mozilla.firefox.vpn.testCanadaServerInfo
import org.mozilla.firefox.vpn.testUSAServerInfo
import org.mozilla.firefox.vpn.util.Result

class VpnViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mainThread = Executors.newSingleThreadExecutor()
        .asCoroutineDispatcher()

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(mainThread)
        MockKAnnotations.init(this, relaxUnitFun = true)

        mockkStatic(Log::class)
        every { Log.isLoggable(any(), any()) } returns false
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * When VpnViewModel#seledtedServer becomes active, it should first notify the selected one returned
     * by ServerRepository#getSelectedServer(), then start to redirect those from the
     * selectedServerProvider#observable
     */
    @Test
    fun testSelectedServerLiveData() {
        // Prepare
        val selectedServerSlot = slot<ServerInfo>()
        var selectedServer = SelectedServer(FilterStrategy.ByCity, testUSAServerInfo)
        val serverRepo = mockk<ServerRepository> {
            coEvery { getServers() } returns
                    Result.Success(listOf(testUSAServerInfo, testCanadaServerInfo))

            every { getSelectedServer() } answers { selectedServer }

            every {
                setSelectedServer(any(), capture(selectedServerSlot))
            } answers {
                selectedServer = SelectedServer(FilterStrategy.ByCity, selectedServerSlot.captured)
            }
        }

        val getServerUseCase = GetServersUseCase(serverRepo)
        val selectedServerProvider = SelectedServerProvider(serverRepo)
        val setSelectedServerUseCase = SetSelectedServerUseCase(serverRepo, selectedServerProvider)
        val getSelectedServerUseCase = GetSelectedServerUseCase(serverRepo)

        val vm = VpnViewModel(
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            selectedServerProvider,
            getServerUseCase,
            getSelectedServerUseCase,
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true)
        )

        val selectedServerLiveData = vm.selectedServer

        val selectedServerObserver = mockk<Observer<ServerInfo?>>(relaxed = true)

        // Test
        selectedServerLiveData.observeForever(selectedServerObserver)
        verify(timeout = 1000) { selectedServerObserver.onChanged(testUSAServerInfo) }

        setSelectedServerUseCase(FilterStrategy.ByCity, testCanadaServerInfo)
        verify(timeout = 1000) { selectedServerObserver.onChanged(testCanadaServerInfo) }

        setSelectedServerUseCase(FilterStrategy.ByCity, testUSAServerInfo)
        verify(timeout = 1000) { selectedServerObserver.onChanged(testUSAServerInfo) }

        confirmVerified(selectedServerObserver)
    }
}
