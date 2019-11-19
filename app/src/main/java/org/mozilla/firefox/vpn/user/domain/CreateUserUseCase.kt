package org.mozilla.firefox.vpn.user.domain

import android.os.SystemClock
import org.mozilla.firefox.vpn.service.LoginResult
import org.mozilla.firefox.vpn.user.data.*

class CreateUserUseCase(private val userRepository: UserRepository) {

    operator fun invoke(loginResult: LoginResult): UserInfo {
        return UserInfo(
            user = loginResult.user,
            token = loginResult.token,
            latestUpdateTime = SystemClock.elapsedRealtime()
        ).apply {
            userRepository.createUserInfo(this)
        }
    }
}
