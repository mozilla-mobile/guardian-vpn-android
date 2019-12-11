package org.mozilla.firefox.vpn.user.domain

import org.mozilla.firefox.vpn.service.PlatformVersion
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.then

class GetVersionsUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(): Result<PlatformVersion> {
        return userRepository.getVersions()
            .then { versions ->
                versions.map["android"]
                    ?.let { Result.Success(it) }
                    ?: Result.Fail(RuntimeException("no android version data"))
            }
    }
}
