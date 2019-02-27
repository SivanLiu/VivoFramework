package com.qti.location.sdk.collection;

import android.util.Log;

public class GpsLog {
    private static final String ERROR = "GPS_ERROR ";
    private static final String WARNNING = "GPS_WARNNING ";

    static void w(String tag, String info) {
        Log.w(tag, WARNNING + info);
    }

    static void e(String tag, String info) {
        Log.e(tag, ERROR + info);
    }
}
