package org.mozilla.firefox.vpn.device.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.service.UnauthorizedException
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result

class GetDevicesUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        userRepository.getUserInfo()?.user?.devices?.let {
            Result.Success(it)
        } ?: Result.Fail(UnauthorizedException())
    }
}
