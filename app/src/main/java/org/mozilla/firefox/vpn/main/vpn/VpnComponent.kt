package org.mozilla.firefox.vpn.main.vpn

import org.mozilla.firefox.vpn.CoreComponent
import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.device.domain.CurrentDeviceUseCase
import org.mozilla.firefox.vpn.main.vpn.domain.GetLatestUpdateMessageUseCase
import org.mozilla.firefox.vpn.main.vpn.domain.ResolveDispatchableServerUseCase
import org.mozilla.firefox.vpn.main.vpn.domain.SetLatestUpdateMessageUseCase
import org.mozilla.firefox.vpn.main.vpn.domain.VpnManagerStateProvider
import org.mozilla.firefox.vpn.servers.domain.GetSelectedServerUseCase
import org.mozilla.firefox.vpn.servers.domain.GetServersUseCase
import org.mozilla.firefox.vpn.user.domain.LogoutUseCase
import org.mozilla.firefox.vpn.user.domain.NotifyUserStateUseCase
import org.mozilla.firefox.vpn.user.domain.RefreshUserInfoUseCase

interface VpnComponent {
    val viewModel: VpnViewModel
}

class VpnComponentImpl(
    private val coreComponent: CoreComponent,
    private val guardianComponent: GuardianComponent
) : VpnComponent, GuardianComponent by guardianComponent, CoreComponent by coreComponent {

    override val viewModel: VpnViewModel
        get() = VpnViewModel(
            application = app,
            vpnManager = vpnManager,
            vpnStateProvider = VpnManagerStateProvider(vpnManager),
            selectedServerProvider = selectedServerProvider,
            getServersUseCase = GetServersUseCase(serverRepo),
            getSelectedServerUseCase = GetSelectedServerUseCase(serverRepo),
            resolveDispatchableServerUseCase = ResolveDispatchableServerUseCase(GetServersUseCase(serverRepo)),
            currentDeviceUseCase = CurrentDeviceUseCase(deviceRepo, userRepo, userStateResolver),
            setLatestUpdateMessageUseCase = SetLatestUpdateMessageUseCase(prefs),
            getLatestUpdateMessageUseCase = GetLatestUpdateMessageUseCase(prefs),
            refreshUserInfoUseCase = RefreshUserInfoUseCase(userRepo),
            logoutUseCase = LogoutUseCase(userRepo, deviceRepo),
            notifyUserStateUseCase = NotifyUserStateUseCase(userStateResolver),
            updateManager = updateManager
        )
}
