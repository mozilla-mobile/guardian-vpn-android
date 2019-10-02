package org.mozilla.guardian.user.domain

import org.mozilla.guardian.user.data.LoginInfo
import org.mozilla.guardian.user.data.UserRepository

class GetLoginInfoUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(): LoginInfo {
        return userRepository.getLoginInfo()
    }
}
