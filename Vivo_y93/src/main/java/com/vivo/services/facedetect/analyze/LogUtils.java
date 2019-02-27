package com.vivo.services.facedetect.analyze;

import android.util.Log;

public class LogUtils {
    private static final boolean DEBUG = true;
    private static final String TAG = "YUVImageAnalyze";

    public static void debugLog(String msg) {
        Log.d(TAG, msg);
    }

    public static void debugLog(String tag, String msg) {
        log(tag, msg);
    }

    public static void log(String msg) {
        Log.d(TAG, msg);
    }

    public static void log(String tag, String msg) {
        log(tag + "/" + msg);
    }
}
