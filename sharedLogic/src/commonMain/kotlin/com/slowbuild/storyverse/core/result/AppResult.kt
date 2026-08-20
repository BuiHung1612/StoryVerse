package com.slowbuild.storyverse.core.result

import kotlinx.serialization.Serializable

sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun errorOrNull(): AppError? = when (this) {
        is Success -> null
        is Error -> error
    }

    fun <R> map(transform: (T) -> R): AppResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    fun <R> flatMap(transform: (T) -> AppResult<R>): AppResult<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
    }

    fun onSuccess(action: (T) -> Unit): AppResult<T> {
        if (this is Success) action(data)
        return this
    }

    fun onError(action: (AppError) -> Unit): AppResult<T> {
        if (this is Error) action(error)
        return this
    }
}

@Serializable
sealed class AppError {
    abstract val message: String

    @Serializable
    data class Network(
        override val message: String,
        val statusCode: Int? = null
    ) : AppError()

    @Serializable
    data class Database(
        override val message: String
    ) : AppError()

    @Serializable
    data class Source(
        override val message: String,
        val sourceId: String? = null
    ) : AppError()

    @Serializable
    data class Content(
        override val message: String
    ) : AppError()

    @Serializable
    data class Ai(
        override val message: String
    ) : AppError()

    @Serializable
    data class Storage(
        override val message: String
    ) : AppError()

    @Serializable
    data class Unknown(
        override val message: String
    ) : AppError()
}
