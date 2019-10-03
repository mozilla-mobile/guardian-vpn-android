package org.mozilla.guardian.user.domain

import org.mozilla.guardian.user.data.Result
import org.mozilla.guardian.user.data.User
import org.mozilla.guardian.user.data.UserRepository

class GetAccountUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(): Result<User> {
        return userRepository.getUserInfo()
    }
}
