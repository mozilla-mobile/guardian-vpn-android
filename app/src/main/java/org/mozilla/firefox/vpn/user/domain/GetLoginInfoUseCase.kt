package org.mozilla.firefox.vpn.user.domain

import org.mozilla.firefox.vpn.service.LoginInfo
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result

class GetLoginInfoUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(): Result<LoginInfo> {
        return userRepository.getLoginInfo()
    }
}
