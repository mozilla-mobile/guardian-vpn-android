package org.mozilla.firefox.vpn.user.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.user.data.UserInfo
import org.mozilla.firefox.vpn.user.data.UserRepository

class GetUserInfoUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(forceUpdate: Boolean = false): UserInfo? = withContext(Dispatchers.IO) {
        if (forceUpdate) {
            userRepository.refreshUserInfo()
        }
        userRepository.getUserInfo()
    }
}
