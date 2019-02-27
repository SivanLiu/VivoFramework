package com.vivo.services.sensorlog;

import android.content.Context;
import android.util.Log;
import com.vivo.services.sensorlog.ISensorLogService.Stub;

public class SensorLogService extends Stub {
    public static final int ASSERT = 7;
    public static final int DEBUG = 3;
    public static final int ERROR = 6;
    public static final int INFO = 4;
    private static final int LOG_ID_INIT = 1;
    private static final int LOG_ID_MAIN = 0;
    private static final String TAG = "SensorLogService";
    public static final int VERBOSE = 2;
    public static final int WARN = 5;
    private static int isNativeInt = 0;

    private native int nativeClassInit();

    private native int vivo_println_native(int i, int i2, String str, String str2);

    public SensorLogService(Context context) {
        try {
            System.loadLibrary("jnisensorlog");
        } catch (UnsatisfiedLinkError e) {
            System.out.println("catch  UnsatisfiedLinkError:" + e);
        }
        InitNativeClass();
    }

    private void InitNativeClass() {
        if (isNativeInt == 0) {
            nativeClassInit();
            isNativeInt = 1;
            Log.d(TAG, "nativeClassInit " + isNativeInt);
        }
    }

    public int vivoprintmain(int priority, String tag, String msg) {
        return vivo_println_native(0, 0, tag, msg);
    }

    public int vivoprintinit(int priority, String tag, String msg) {
        return vivo_println_native(1, 0, tag, msg);
    }
}
