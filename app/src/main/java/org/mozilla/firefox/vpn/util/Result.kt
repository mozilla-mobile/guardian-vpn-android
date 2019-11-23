package org.mozilla.firefox.vpn.util

sealed class Result<out T : Any> {
    data class Success<out T : Any>(val value: T) : Result<T>()
    data class Fail(val exception: Exception) : Result<Nothing>()
}

inline fun <T : Any, R : Any> Result<T>.mapValue(function: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(function(value))
        is Result.Fail -> this
    }
}

inline fun <T : Any> Result<T>.mapError(function: (Exception) -> Exception): Result<T> {
    return when (this) {
        is Result.Success -> this
        is Result.Fail -> Result.Fail(function(exception))
    }
}

inline fun <T : Any> Result<T>.onSuccess(function: (T) -> Unit): Result<T> {
    when (this) {
        is Result.Success -> function(value)
    }
    return this
}

inline fun <T : Any> Result<T>.onError(function: (Exception) -> Unit): Result<T> {
    when (this) {
        is Result.Fail -> function(exception)
    }
    return this
}

fun <T : Any> Result<T>.getOrNull(): T? {
    return when (this) {
        is Result.Success -> value
        is Result.Fail -> null
    }
}

inline fun <T : Any, K: Any> Result<T>.then(action: (T) -> Result<K>): Result<K> {
    return when (this) {
        is Result.Success -> action(value)
        is Result.Fail -> Result.Fail(exception)
    }
}
