package org.mozilla.firefox.vpn.user.domain

import org.mozilla.firefox.vpn.user.data.Result
import org.mozilla.firefox.vpn.user.data.UnauthorizedException
import org.mozilla.firefox.vpn.user.data.User
import org.mozilla.firefox.vpn.user.data.UserRepository

class GetAccountUseCase(
    private val userRepository: UserRepository
) {

    operator fun invoke(): Result<User> {
        return userRepository.getUserInfo()?.let {
            Result.Success(it.user)
        } ?: Result.Fail(UnauthorizedException)
    }
}
