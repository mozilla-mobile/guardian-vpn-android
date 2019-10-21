package org.mozilla.firefox.vpn.user.domain

import org.mozilla.firefox.vpn.user.data.*

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
