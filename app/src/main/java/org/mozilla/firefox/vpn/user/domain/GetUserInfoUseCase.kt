package org.mozilla.firefox.vpn.user.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.service.UnauthorizedException
import org.mozilla.firefox.vpn.service.User
import org.mozilla.firefox.vpn.user.data.UserInfo
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result

class GetUserInfoUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(): UserInfo? = withContext(Dispatchers.IO) {
        userRepository.getUserInfo()
    }
}
