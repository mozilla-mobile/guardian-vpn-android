package org.mozilla.firefox.vpn.user.domain

import org.mozilla.firefox.vpn.UserStateResolver
import org.mozilla.firefox.vpn.service.LoginResult
import org.mozilla.firefox.vpn.user.data.UserInfo
import org.mozilla.firefox.vpn.user.data.UserRepository

class CreateUserUseCase(
    private val userRepository: UserRepository,
    private val userStateResolver: UserStateResolver
) {

    operator fun invoke(loginResult: LoginResult): UserInfo {
        return UserInfo(
            user = loginResult.user,
            token = loginResult.token,
            latestUpdateTime = System.currentTimeMillis()
        ).apply {
            userRepository.createUserInfo(this)
            userStateResolver.refresh()
        }
    }
}
