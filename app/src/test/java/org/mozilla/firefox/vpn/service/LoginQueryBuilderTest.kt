package org.mozilla.firefox.vpn.service

import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LoginQueryBuilderTest {

    @Test
    fun `only send valid referral info to backend`() {
        var loginQueryBuilder = LoginQueryBuilder("")
        assertTrue(loginQueryBuilder.build().isEmpty())

        loginQueryBuilder = LoginQueryBuilder("aaa")
        assertTrue(loginQueryBuilder.build().size == 1)

        loginQueryBuilder = LoginQueryBuilder("aaa=")
        assertTrue(loginQueryBuilder.build().size == 1)

        loginQueryBuilder = LoginQueryBuilder("=aaa")
        assertTrue(loginQueryBuilder.build().isEmpty())

        loginQueryBuilder = LoginQueryBuilder("aaa=xxx")
        assertTrue(loginQueryBuilder.build().size == 1)

        loginQueryBuilder = LoginQueryBuilder("?aaa")
        assertTrue(loginQueryBuilder.build().size == 1)

        loginQueryBuilder = LoginQueryBuilder("aaa=xxx&bbb=zzz")
        assertTrue(loginQueryBuilder.build().size == 2)

        loginQueryBuilder = LoginQueryBuilder("aaa=xxx&=zzz")
        assertTrue(loginQueryBuilder.build().size == 1)

    }
}