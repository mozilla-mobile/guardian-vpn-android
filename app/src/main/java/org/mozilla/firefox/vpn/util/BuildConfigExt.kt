package org.mozilla.firefox.vpn.util

import org.mozilla.firefox.vpn.BuildConfig

object BuildConfigExt {
    private const val BUILD_FLAVOR_PREVIEW = "preview"
    private const val BUILD_TYPE_DEBUG = "debug"

    fun isFlavorPreview() = BuildConfig.FLAVOR == BUILD_FLAVOR_PREVIEW

    fun isBuildTypeDebug() = BuildConfig.BUILD_TYPE == BUILD_TYPE_DEBUG
}
