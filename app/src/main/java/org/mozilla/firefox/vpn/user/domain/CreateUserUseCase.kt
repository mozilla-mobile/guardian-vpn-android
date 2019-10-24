package org.mozilla.firefox.vpn.user.domain

import org.mozilla.firefox.vpn.service.LoginResult
import org.mozilla.firefox.vpn.user.data.*
import org.mozilla.firefox.vpn.util.Result

class CreateUserUseCase(private val userRepository: UserRepository) {

    operator fun invoke(loginResult: LoginResult): Result<UserInfo> {
        return UserInfo(
            user = loginResult.user,
            token = loginResult.token,
            latestUpdateTime = System.currentTimeMillis()
        ).let {
            userRepository.createUserInfo(it)
            Result.Success(it)
        }
    }
}
