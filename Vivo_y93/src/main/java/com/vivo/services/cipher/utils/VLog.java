package com.vivo.services.cipher.utils;

import android.util.Log;

public class VLog {
    public static void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    public static void v(String tag, SecurityKeyConfigure configure, String msg) {
        Log.v(tag, configure.getShortPackageName() + ": " + msg);
    }

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void d(String tag, SecurityKeyConfigure configure, String msg) {
        Log.d(tag, configure.getShortPackageName() + ": " + msg);
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void i(String tag, SecurityKeyConfigure configure, String msg) {
        Log.i(tag, configure.getShortPackageName() + ": " + msg);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void w(String tag, SecurityKeyConfigure configure, String msg) {
        Log.w(tag, configure.getShortPackageName() + ": " + msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void e(String tag, SecurityKeyConfigure configure, String msg) {
        Log.e(tag, configure.getShortPackageName() + ": " + msg);
    }
}
