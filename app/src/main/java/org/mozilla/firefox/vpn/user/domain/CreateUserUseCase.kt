package org.mozilla.firefox.vpn.user.domain

import org.mozilla.firefox.vpn.UserStateResolver
import org.mozilla.firefox.vpn.user.data.UserRepository

class CreateUserUseCase(
    private val userRepository: UserRepository,
    private val userStateResolver: UserStateResolver
) {

    suspend operator fun invoke() {
        // Note: `refreshUserInfo` and `refresh` are temporally coupled
        userRepository.refreshUserInfo()
        userStateResolver.refresh()
    }
}
