package ru.serjik.utils

import android.util.Log

object SerjikLog {
    private const val TAG = "SerjikOEL"

    private fun getCallerInfo(): String {
        val stackTrace = Thread.currentThread().stackTrace
        for (i in stackTrace.indices) {
            if (stackTrace[i].className == SerjikLog::class.java.name) {
                if (i + 1 < stackTrace.size) {
                    val caller = stackTrace[i + 1]
                    val simpleName = caller.className.substringAfterLast('.')
                    return "[${simpleName}:${caller.methodName}:${caller.lineNumber}]: "
                }
            }
        }
        return ""
    }

    fun logWithCaller(message: String) {
        Log.i(TAG, getCallerInfo() + message)
    }

    fun log(message: String) {
        Log.i(TAG, message)
    }
}
