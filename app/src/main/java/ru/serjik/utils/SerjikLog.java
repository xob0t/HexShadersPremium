package ru.serjik.utils;

import android.text.TextUtils;
import android.util.Log;

public final class SerjikLog {
    private static final String TAG = "SerjikOEL";

    private static String getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            String className = stackTrace[i].getClassName();
            if (className.equals(SerjikLog.class.getName())) {
                if (i + 1 < stackTrace.length) {
                    StackTraceElement caller = stackTrace[i + 1];
                    String simpleName = caller.getClassName();
                    int lastDot = simpleName.lastIndexOf('.');
                    if (lastDot >= 0) simpleName = simpleName.substring(lastDot + 1);
                    return "[" + simpleName + ":" + caller.getMethodName() + ":" + caller.getLineNumber() + "]: ";
                }
            }
        }
        return "";
    }

    public static void logWithCaller(String message) {
        Log.i(TAG, getCallerInfo() + message);
    }

    public static void log(String message) {
        Log.i(TAG, message);
    }
}
