package com.vivo.alphaindex;

import android.os.SystemProperties;
import android.util.Log;

public class VivoLog {
    public static final boolean DEBUG = SystemProperties.getBoolean("debug.AlphabetIndexer.enablelog", false);
    public static String TAG = "AlphabetIndexer";

    public static void debug(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void debug(String tag, String format, Object... args) {
        if (DEBUG) {
            Log.d(tag, String.format(format, args));
        }
    }

    public static void error(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void error(String tag, String format, Object... args) {
        if (DEBUG) {
            Log.e(tag, String.format(format, args));
        }
    }
}
