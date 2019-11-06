package org.mozilla.firefox.vpn.util

import android.content.Context

class StringResource {
    var id: Int? = null
    var str: String? = null

    constructor(str: String) {
        this.str = str
    }

    constructor(id: Int) {
        this.id = id
    }

    fun resolve(context: Context): String? {
        return str?.let { it } ?: id?.let { context.getString(it).apply { str = this } }
    }
}
