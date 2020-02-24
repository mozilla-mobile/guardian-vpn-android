package org.mozilla.firefox.vpn.splash.domain

import org.mozilla.firefox.vpn.user.data.UserInfo
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result

class RefreshUserInfoUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(): Result<UserInfo> {
        return userRepository.refreshUserInfo(CONNECT_TIMEOUT, READ_TIMEOUT)
    }

    companion object {
        private const val CONNECT_TIMEOUT = 3000L
        private const val READ_TIMEOUT = 3000L
    }
}
