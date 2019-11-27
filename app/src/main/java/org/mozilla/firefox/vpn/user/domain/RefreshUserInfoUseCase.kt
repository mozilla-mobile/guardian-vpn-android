package org.mozilla.firefox.vpn.user.domain

import org.mozilla.firefox.vpn.user.data.UserInfo
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result

class RefreshUserInfoUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(): Result<UserInfo> {
        return userRepository.refreshUserInfo()
    }
}
