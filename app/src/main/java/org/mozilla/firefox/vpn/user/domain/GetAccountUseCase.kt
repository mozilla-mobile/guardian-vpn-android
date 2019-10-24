package org.mozilla.firefox.vpn.user.domain

import org.mozilla.firefox.vpn.service.UnauthorizedException
import org.mozilla.firefox.vpn.service.User
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result

class GetAccountUseCase(
    private val userRepository: UserRepository
) {

    operator fun invoke(): Result<User> {
        return userRepository.getUserInfo()?.let {
            Result.Success(it.user)
        } ?: Result.Fail(UnauthorizedException())
    }
}
