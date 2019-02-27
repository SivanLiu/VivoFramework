package com.sensoroperate;

import android.util.Slog;

public class VivoSensorOperationUtils {
    public static final int SENSOR_OPERATION_GET_ALS_RAWDATA = 201;
    public static final int SENSOR_OPERATION_PROXIMITY_CAM_IRLED_SWITCH_EVENT = 202;
    public static final int SENSOR_OPERATION_PROXIMITY_THRES_NOTIFY_EVENT = 200;
    private static final String TAG = "VivoSensorOperationUtils";
    private static final int VIVO_SENSOR_OP_FAIL = 0;
    private static final int VIVO_SENSOR_OP_SUCCESS = 1;
    private static int isNativeInt = 0;
    private static boolean isSensorLibExist;
    public static VivoSensorOperationUtils mVivoSensorOperationUtils = new VivoSensorOperationUtils();

    private native int jniVivoSensorOperationUtilsInt(int i, VivoSensorOperationResult vivoSensorOperationResult, int[] iArr, int i2);

    private native int nativeClassInit();

    static {
        isSensorLibExist = true;
        try {
            System.loadLibrary("jni_vivosensor_operation_utils");
        } catch (UnsatisfiedLinkError e) {
            isSensorLibExist = false;
            Slog.d(TAG, "catch UnsatisfiedLinkError" + e);
        }
    }

    public static VivoSensorOperationUtils getInstance() {
        return mVivoSensorOperationUtils;
    }

    public synchronized int executeCommand(int type, VivoSensorOperationResult result, int[] commands, int comandLength) {
        int ret = -1;
        if (!isSensorLibExist) {
            return 0;
        }
        try {
            makeInitNativeClass();
        } catch (Exception e) {
            Slog.d(TAG, "Invoke makeInitNativeClass fail");
        }
        try {
            ret = VivoSensorOperationUtilsInt(type, result, commands, comandLength);
        } catch (Exception e2) {
            Slog.d(TAG, "Invoke VivoSensorOperationUtilsInt fail");
        }
        Slog.d(TAG, "executeCommand " + ret);
        return ret;
    }

    private void makeInitNativeClass() {
        if (isSensorLibExist && isNativeInt == 0) {
            try {
                nativeClassInit();
                isNativeInt = 1;
            } catch (Exception e) {
                isNativeInt = 0;
                Slog.d(TAG, "Invoke nativeClassInit fail");
            }
            Slog.d(TAG, "nativeClassInit " + isNativeInt);
        }
    }

    private int VivoSensorOperationUtilsInt(int type, VivoSensorOperationResult result, int[] commands, int cmd_len) {
        int ret = -1;
        try {
            return jniVivoSensorOperationUtilsInt(type, result, commands, cmd_len);
        } catch (Exception e) {
            Slog.d(TAG, "Invoke jniVivoSensorOperationUtilsInt fail");
            return ret;
        }
    }
}
