package com.vivo.gamewatch.service;

import android.util.Log;

public class VGTJni {
    private static boolean sJNILoaded;

    private static native void native_setGraphicFlags(int i, int i2);

    static {
        sJNILoaded = false;
        try {
            System.loadLibrary("VGT_JNI");
            sJNILoaded = true;
        } catch (Throwable e) {
            Log.e("VGT", "Couldn't find libVGT_JNI");
            e.printStackTrace();
        }
    }

    public static void setGraphicFlags(int flags, int mask) {
        if (sJNILoaded) {
            native_setGraphicFlags(flags, mask);
        }
    }

    public static void clearGraphicFlags() {
        setGraphicFlags(0, Integer.MAX_VALUE);
    }
}
