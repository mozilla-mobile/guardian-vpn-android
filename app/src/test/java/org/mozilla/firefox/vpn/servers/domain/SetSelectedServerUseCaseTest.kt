package org.mozilla.firefox.vpn.servers.domain

import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.firefox.vpn.servers.data.SelectedServer
import org.mozilla.firefox.vpn.servers.data.ServerRepository
import org.mozilla.firefox.vpn.service.MockGuardianService
import org.mozilla.firefox.vpn.testUSAServerInfo

class SetSelectedServerUseCaseTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val selectedServer = SelectedServer(FilterStrategy.ByCity, testUSAServerInfo)
    private val sharedPref = mockk<SharedPreferences> {
        every { getString(any(), any()) } returns Gson().toJson(selectedServer)
        every { edit().putString(any(), any()).apply() } just Runs
    }

    private lateinit var serverRepository: ServerRepository
    private lateinit var selectedServerProvider: SelectedServerProvider
    private lateinit var setSelectedServerUseCase: SetSelectedServerUseCase

    @Before
    fun setUp() {
        serverRepository = ServerRepository(MockGuardianService(), sharedPref)
        selectedServerProvider = SelectedServerProvider(serverRepository)
        setSelectedServerUseCase = SetSelectedServerUseCase(serverRepository, selectedServerProvider)
    }

    @Test
    fun `After selected server, it should be notified from observer of provider`() {
        setSelectedServerUseCase(FilterStrategy.ByCity, testUSAServerInfo)
        assertThat(selectedServerProvider.observable.value).isEqualTo(testUSAServerInfo)
    }
}
