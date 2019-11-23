package org.mozilla.firefox.vpn.user.domain

import org.mozilla.firefox.vpn.UserStateResolver

class NotifyUserStateUseCase(
    private val userStateResolver: UserStateResolver
) {

    operator fun invoke() {
        userStateResolver.refresh()
    }
}
