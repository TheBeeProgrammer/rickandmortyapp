package com.renato.data.api.interceptor

import com.renato.logger.Logger
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject

/**
 * A custom [HttpLoggingInterceptor.Logger] implementation that redirects OkHttp
 * network traffic logs to the application's internal [Logger].
 *
 * this class allows for centralized logging of HTTP requests and responses,
 * facilitating debugging and network monitoring during development.
 *
 * @constructor Creates an instance of [LoggingInterceptor], intended for dependency injection.
 */
class LoggingInterceptor @Inject constructor() : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        Logger.i(message = message)
    }
}