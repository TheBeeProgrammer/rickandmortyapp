package com.renato.data.api.interceptor

import android.util.Log
import com.renato.logger.Logger
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject

/**
 * A custom [HttpLoggingInterceptor.Logger] implementation that logs HTTP request
 * and response details using Android's [Log] utility.
 *
 * This logger is useful during development and debugging to inspect network traffic.
 *
 * @constructor Creates an instance of [LoggingInterceptor], typically injected using a DI framework.
 */
class LoggingInterceptor @Inject constructor() : HttpLoggingInterceptor.Logger {
    /**
     * Logs a single HTTP request/response message using the project's info logger.
     *
     * @param message The log line produced by the HTTP logging interceptor (request/response details).
     */
    override fun log(message: String) {
        Logger.i(message = message)
    }
}