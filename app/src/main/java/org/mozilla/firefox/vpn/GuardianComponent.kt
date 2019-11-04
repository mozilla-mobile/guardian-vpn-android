package org.mozilla.firefox.vpn

import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.servers.data.ServerRepository
import org.mozilla.firefox.vpn.user.data.UserRepository

interface GuardianComponent {
    val userRepo: UserRepository
    val deviceRepo: DeviceRepository
    val serverRepo: ServerRepository
}

class GuardianComponentImpl(
    private val coreComponent: CoreComponent
) : GuardianComponent {

    override val userRepo: UserRepository by lazy {
        UserRepository(coreComponent.app)
    }

    override val deviceRepo: DeviceRepository by lazy {
        DeviceRepository(coreComponent.app)
    }

    override val serverRepo: ServerRepository by lazy {
        ServerRepository()
    }
}
