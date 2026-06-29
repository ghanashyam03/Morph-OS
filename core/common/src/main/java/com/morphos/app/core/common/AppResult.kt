package com.morphos.app.core.common

sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Error(val exception: Throwable) : AppResult<Nothing>
    object Loading : AppResult<Nothing>
}
