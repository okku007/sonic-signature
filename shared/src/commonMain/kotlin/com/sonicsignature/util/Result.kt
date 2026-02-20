package com.sonicsignature.util

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/** Convenience: run a suspending block and wrap exceptions as Result.Error */
suspend fun <T> runCatchingResult(
    errorMessage: String = "An unexpected error occurred.",
    block: suspend () -> T
): Result<T> = try {
    Result.Success(block())
} catch (e: Exception) {
    Result.Error(errorMessage, e)
}
