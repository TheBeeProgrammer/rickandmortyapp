package com.renato.logger

import android.os.Build
import timber.log.Timber
import kotlin.jvm.java

/**
 * A [Timber.Tree] implementation that provides enhanced logging information.
 *
 * This class extends [Timber.DebugTree] to customize the log tag format.
 * It includes the filename, line number, and method name of the calling code
 * in the log tag, making it easier to trace log origins.
 *
 * On Android S (API level 31) and above, it filters out stack trace elements
 * originating from the logging framework itself (Logger, TimberLogging, Timber)
 * to provide a cleaner and more relevant caller information.
 */
class TimberLogging : Timber.DebugTree() {

    private val ignoredPackages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Logger::class.java.packageName,
            TimberLogging::class.java.packageName,
            Timber::class.java.packageName
        )
    } else {
        emptyList()
    }

    override fun createStackElementTag(element: StackTraceElement): String {
        val stackTrace = Throwable().stackTrace
        val caller = stackTrace.firstOrNull { frame ->
            ignoredPackages.none { frame.className.startsWith(it) }
        } ?: element

        return "(${caller.fileName}:${caller.lineNumber}) on ${caller.methodName}"
    }
}
