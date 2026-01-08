package com.renato.logger

import timber.log.Timber

/**
 * A simple logger that uses Timber for debug builds and a no-op logger for release builds.
 *
 * To use, call `Logger.init()` in your Application class. Then, use the static methods to log
 * messages:
 *
 * ```
 * Logger.d("Debug message")
 * Logger.i("Info message")
 * Logger.e(Exception("Error!"), "Error message")
 * ```
 */
object Logger {
    private val logger by lazy {
        TimberLogging()
    }

    fun init() {
        Timber.plant(logger)
    }

    fun d(message: String, t: Throwable? = null) = logger.d(t, message)
    fun i(message: String, t: Throwable? = null) = logger.i(t, message)
    fun e(t: Throwable? = null, message: String) = logger.e(t, message)
    fun wtf(t: Throwable? = null, message: String) = logger.wtf(t, message)
}
