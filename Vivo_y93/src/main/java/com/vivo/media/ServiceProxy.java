package com.vivo.media;

import android.util.Log;

public class ServiceProxy {
    static final String TAG = "ServiceProxy";

    public static class AudioParameters {
        public static native int ReadNvData(int i, byte[] bArr, int i2);

        public static native int ReadNvSize(int i);

        public static native int WriteNvData(int i, byte[] bArr, int i2);

        static {
            ServiceProxy.dummy_init();
        }
    }

    public static native int native_init();

    static {
        Log.v(TAG, "static initilize of ServiceProxy");
        System.loadLibrary("mars-service_jni");
        native_init();
    }

    private static void dummy_init() {
    }
}
