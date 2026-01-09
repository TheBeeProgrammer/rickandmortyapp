package com.renato.domain.usecases.base

/**
 * Represents an executable use case that takes input parameters of type [Params]
 * and produces a result of type [Result].
 *
 * This interface defines a single suspend function `invoke` which allows the use case
 * to be executed asynchronously.
 *
 * @param Params The type of the input parameters for the use case.
 * @param Result The type of the result produced by the use case.
 */
interface ExecutableUseCase<in Params, out Result> {
    /**
 * Executes the use case with the provided parameters.
 *
 * @param params Input parameters for the use case execution.
 * @return The result produced by this use case.
 */
suspend operator fun invoke(params: Params): Result
}