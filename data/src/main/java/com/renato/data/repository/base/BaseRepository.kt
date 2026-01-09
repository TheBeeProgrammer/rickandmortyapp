package com.renato.data.repository.base

import com.renato.domain.model.NetworkUnavailableException
import com.renato.domain.usecases.UseCaseResult
import retrofit2.HttpException
import kotlin.coroutines.cancellation.CancellationException

abstract class BaseRepository {

    /**
     * Executes a suspending block of code safely, catching any exceptions and returning a [UseCaseResult].
     *
     * @param block The suspend function to execute.
     * @return A [UseCaseResult] object, which will be [UseCaseResult.Success] if the block executes without exceptions,
     * or [UseCaseResult.Failure] if an exception is caught.
     */
    protected suspend fun <T> safeCall(
        block: suspend () -> T
    ): UseCaseResult<T> {
        return try {
            UseCaseResult.Success(block())
        } catch (e: HttpException) {
            UseCaseResult.Failure(UseCaseResult.Reason.Unknown(e.message()))
        } catch (_: NetworkUnavailableException) {
            UseCaseResult.Failure(UseCaseResult.Reason.NoInternet)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            UseCaseResult.Failure(UseCaseResult.Reason.Unknown(e.message ?: "Unknown error"))
        }
    }
}