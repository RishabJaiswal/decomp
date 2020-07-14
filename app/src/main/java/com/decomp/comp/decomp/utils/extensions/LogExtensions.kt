package com.decomp.comp.decomp.utils.extensions

import android.util.Log
import com.crashlytics.android.Crashlytics
import com.decomp.comp.decomp.BuildConfig

fun Any.logError(
        tag: String? = null,
        message: String? = null,
        throwable: Throwable? = null,
        isOnlyForDebug: Boolean? = null
) {
    log(Log.ERROR, tag, message, throwable, isOnlyForDebug)
}

fun Any.logInfo(
        tag: String? = null,
        message: String? = null,
        throwable: Throwable? = null,
        isOnlyForDebug: Boolean? = null
) {
    log(Log.INFO, tag, message, throwable, isOnlyForDebug)
}

fun Any.logDebug(
        tag: String? = null,
        message: String? = null,
        throwable: Throwable? = null,
        isOnlyForDebug: Boolean? = null
) {
    log(Log.DEBUG, tag, message, throwable, isOnlyForDebug)
}

fun Any.logVerbose(
        tag: String? = null,
        message: String? = null,
        throwable: Throwable? = null,
        isOnlyForDebug: Boolean? = null
) {
    log(Log.VERBOSE, tag, message, throwable, isOnlyForDebug)
}

fun Any.logWarning(
        tag: String? = null,
        message: String? = null,
        throwable: Throwable? = null,
        isOnlyForDebug: Boolean? = null
) {
    log(Log.WARN, tag, message, throwable, isOnlyForDebug)
}

private fun Any.log(type: Int, tag: String?, message: String?, throwable: Throwable?, isOnlyForDebug: Boolean?) {
    val logTag: String = APPLICATION_TAG + (tag ?: DEFAULT_TAG)
    val shouldLog: Boolean = isOnlyForDebug ?: BuildConfig.DEBUG
    if (shouldLog) {
        when (type) {
            Log.DEBUG -> Log.d(logTag, message, throwable)
            Log.ERROR -> {
                Log.e(logTag, message, throwable)
                logToCrashlytics(logTag, message, throwable)
            }
            Log.INFO -> Log.i(logTag, message, throwable)
            Log.WARN -> Log.w(logTag, message, throwable)
            Log.VERBOSE -> Log.v(logTag, message, throwable)
        }
    } else {
        if (type == Log.ERROR) {
            logToCrashlytics(logTag, message, throwable)
        }
        // ignore as we do not want to print logs in production build until explicitly specified
    }
}

fun logToCrashlytics(logTag: String, message: String?, throwable: Throwable? = null) {
    if (throwable != null) {
        Crashlytics.logException(throwable)
    }

    if (message != null) {
        Crashlytics.log(Log.ERROR, logTag, message)
    }
}

// if tag is not specified it will take the name of the class from which the log has been printed
private val Any.DEFAULT_TAG: String get() = this::class.java.simpleName

private const val APPLICATION_TAG = "pdf/"