package org.mozilla.firefox.vpn.util

import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

fun Job.addCompletionHandler(handle: CompletionHandler): Job {
    this.invokeOnCompletion(handle)
    return this
}

fun <T, R> Flow<T>.flatMap(transform: suspend (T) -> Flow<R>): Flow<R> {
    return flow {
        map(transform).collect { subFlow ->
            subFlow.collect { emit(it) }
        }
    }
}

fun <T> Flow<T>.distinctBy(selector: (v1: T, v2: T) -> Boolean): Flow<T> {
    return flow {
        var current: T? = null
        collect { newValue ->
            if (current == null || selector(current!!, newValue)) {
                current = newValue
                emit(newValue)
            }
        }
    }
}
