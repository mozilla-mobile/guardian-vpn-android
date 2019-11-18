package org.mozilla.firefox.vpn.util

import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.Job

fun Job.addCompletionHandler(handle: CompletionHandler): Job {
    this.invokeOnCompletion(handle)
    return this
}
