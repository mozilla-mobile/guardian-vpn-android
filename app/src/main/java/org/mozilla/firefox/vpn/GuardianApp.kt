package org.mozilla.firefox.vpn

import android.app.Application
import android.content.Context
import org.mozilla.firefox.vpn.util.EmojiUtil

class GuardianApp : Application() {

    val coreComponent: CoreComponent by lazy {
        CoreComponentImpl(this)
    }

    lateinit var guardianComponent: GuardianComponent

    override fun onCreate() {
        super.onCreate()

        guardianComponent = GuardianComponentImpl(coreComponent)
        EmojiUtil.initEmoji(this)
    }
}

val Context.coreComponent: CoreComponent
    get() = (applicationContext as GuardianApp).coreComponent

val Context.guardianComponent: GuardianComponent
    get() = (applicationContext as GuardianApp).guardianComponent
