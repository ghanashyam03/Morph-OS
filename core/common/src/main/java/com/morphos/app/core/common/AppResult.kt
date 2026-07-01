package com.morphos.app.core.common

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val exception: Throwable, val message: String = exception.message ?: "Unknown error") : AppResult<Nothing>()
    object Loading : AppResult<Nothing>()
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) {
        action(data)
    }
    return this
}

inline fun <T> AppResult<T>.onError(action: (Throwable) -> Unit): AppResult<T> {
    if (this is AppResult.Error) {
        action(exception)
    }
    return this
}

fun <T> AppResult<T>.getOrNull(): T? {
    return when (this) {
        is AppResult.Success -> data
        else -> null
    }
}

fun <T> AppResult<T>.getOrDefault(default: T): T {
    return when (this) {
        is AppResult.Success -> data
        else -> default
    }
}

suspend fun <T> safeCall(block: suspend () -> T): AppResult<T> {
    return try {
        AppResult.Success(block())
    } catch (e: Throwable) {
        AppResult.Error(e)
    }
}
