package com.renato.domain.usecases.base

/**
 * Represents the result of a use case execution.
 *
 * This sealed class can either be a [Success] containing the data or a [Failure]
 * containing the reason for the failure.
 *
 * @param T The type of the data returned in case of success.
 */
sealed class UseCaseResult<out T> {
    data class Success<out T>(val data: T): UseCaseResult<T>()
    data class Failure(val reason: Reason): UseCaseResult<Nothing>()

    /**
     * Represents the reason for a use case failure.
     *
     * This sealed class defines the possible reasons why a use case might fail.
     */
    sealed class Reason {
        object NoInternet: Reason()
        data class Unknown(val message: String): Reason()
    }
}