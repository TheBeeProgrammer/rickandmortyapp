package com.renato.logger

import android.util.Log
import timber.log.Timber

/**
 * A [Timber.Tree] that logs warnings and errors to an external service.
 *
 * This class is used in release builds to ensure that important log messages
 * are captured and sent to a remote logging service like Crashlytics.
 *
 * It only handles log messages with priority [Log.WARN] and [Log.ERROR].
 * Other log levels are ignored in release builds by this tree.
 */
class TimberLogging : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        when (priority) {
            Log.WARN -> logWarning(priority, tag, message)
            Log.ERROR -> logError(t, priority, tag, message)
        }
    }

    private fun logWarning(priority: Int, tag: String?, message: String) {
        // Log to external service like Crashlytics
    }

    private fun logError(t: Throwable?, priority: Int, tag: String?, message: String) {
        // Log to external service like Crashlytics

        t?.let {
            // Log to external service like Crashlytics
        }
    }
}