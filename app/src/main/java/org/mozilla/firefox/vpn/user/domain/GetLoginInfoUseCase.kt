package org.mozilla.firefox.vpn.user.domain

import org.mozilla.firefox.vpn.user.data.LoginInfo
import org.mozilla.firefox.vpn.user.data.UserRepository

class GetLoginInfoUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(): LoginInfo {
        return userRepository.getLoginInfo()
    }
}
