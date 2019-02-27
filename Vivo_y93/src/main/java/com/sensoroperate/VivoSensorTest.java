package com.sensoroperate;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.os.SystemProperties;
import android.util.Log;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoSensorTest {
    public static final int ALSPS_CLOSE_DEVICE = 35;
    public static final int ALS_CALI_TEST = 16;
    public static final int ALS_CLOSE_DEVICE = 18;
    public static final int ALS_ENABLE_DEVICE = 19;
    public static final int ALS_GET_PARA = 21;
    public static final int ALS_RAWDATA_TEST = 17;
    public static final int ALS_SET_PARA_INDEX = 20;
    public static final int BYTE_ARRAY_TEST = 3;
    public static final int FLOAT_ARRAY_TEST = 2;
    public static final int GESTURE_CALI_STATUS = 43;
    public static final int GESTURE_CALI_TEST = 42;
    public static final int GS_CALI_TEST = 48;
    public static final int GS_DATATOMG_TEST = 50;
    public static final int GS_DATA_TEST = 49;
    public static final int GS_EVENT = 57;
    public static final int GS_EVENT_DISABLE_INT = 20;
    public static final int GS_EVENT_DISABLE_POCKET = 17;
    public static final int GS_EVENT_ENABLE_INT = 19;
    public static final int GS_EVENT_ENABLE_POCKET = 16;
    public static final int GS_EVENT_GET_DATA = 21;
    public static final int GS_EVENT_GET_FIFO_RESULT = 18;
    public static final int GS_SELFTEST = 51;
    public static final int GYRO_CALI_TEST = 80;
    public static final int GYRO_DATATODPS_TEST = 82;
    public static final int GYRO_DATA_TEST = 81;
    public static final int GYRO_SELFTEST = 83;
    public static final int INT_ARRAY_TEST = 1;
    public static final int MAG_CALI_TEST = 64;
    public static final int MAG_CLOSE_TEST = 68;
    public static final int MAG_DATA_TEST = 65;
    public static final int MAG_OPEN_TEST = 67;
    public static final int MAG_SELFTEST_TEST = 66;
    public static final int PS_CALI_TEST = 32;
    public static final int PS_CLOSE_DEVICE = 38;
    public static final int PS_ENABLE_DEVICE = 39;
    public static final int PS_GET_NEAR_AWAY = 37;
    public static final int PS_GET_PARA = 46;
    public static final int PS_GET_TEMP_PARA = 47;
    public static final int PS_IS_CONFIGURED_TOLERANCE = 513;
    public static final int PS_RAWDATA_TEST = 33;
    public static final int PS_READ_REG = 41;
    public static final int PS_SET_CALDATA = 34;
    public static final int PS_SET_PARA_INDEX = 45;
    public static final int PS_SET_PARA_MODE = 44;
    public static final int PS_TEM_CALI_TEST = 36;
    public static final int PS_WRITE_REG = 40;
    public static final int RESERVED_TEST = 0;
    public static final int STRING_ARRAY_TEST = 4;
    public static final String TAG = "VivoSensorTest";
    private static int isNativeInt = 0;
    public static boolean isSensorLibExist;
    public static VivoSensorTest mVivoSensorTest = new VivoSensorTest();

    private native int jniVivoSensorTestByte(int i, SensorTestResult sensorTestResult, byte[] bArr, int i2);

    private native int jniVivoSensorTestFloat(int i, SensorTestResult sensorTestResult, float[] fArr, int i2);

    private native int jniVivoSensorTestInt(int i, SensorTestResult sensorTestResult, int[] iArr, int i2);

    private native int jniVivoSensorTestString(int i, SensorTestResult sensorTestResult, String str, int i2);

    private native int nativeClassInit();

    static {
        isSensorLibExist = true;
        String prop = SystemProperties.get("ro.product.model.bbk", null);
        try {
            System.loadLibrary("jni_vivosensortest");
        } catch (UnsatisfiedLinkError e) {
            System.out.println("VivoSensorTest catch  UnsatisfiedLinkError:" + e);
            isSensorLibExist = false;
        }
    }

    private boolean isOperationForbidden(int type) {
        if (type == 38) {
            return true;
        }
        return false;
    }

    public int vivoSensorTest(int type, SensorTestResult result, int[] p_data, int len) {
        if (!isSensorLibExist || isOperationForbidden(type)) {
            return -1;
        }
        InitNativeClass();
        return jniVivoSensorTestInt(type, result, p_data, len);
    }

    public int vivoSensorTest(int type, SensorTestResult result, float[] p_data, int len) {
        if (!isSensorLibExist || isOperationForbidden(type)) {
            return -1;
        }
        InitNativeClass();
        return jniVivoSensorTestFloat(type, result, p_data, len);
    }

    public int vivoSensorTest(int type, SensorTestResult result, byte[] p_data, int len) {
        if (!isSensorLibExist || isOperationForbidden(type)) {
            return -1;
        }
        InitNativeClass();
        return jniVivoSensorTestByte(type, result, p_data, len);
    }

    public int vivoSensorTest(int type, SensorTestResult result, String p_data, int len) {
        if (!isSensorLibExist || isOperationForbidden(type)) {
            Log.d(TAG, "vivoSensorTest string SensorLibExist false");
            return -1;
        }
        InitNativeClass();
        return jniVivoSensorTestString(type, result, p_data, len);
    }

    public static VivoSensorTest getInstance() {
        return mVivoSensorTest;
    }

    private void InitNativeClass() {
        if (isSensorLibExist && isNativeInt == 0) {
            nativeClassInit();
            isNativeInt = 1;
            Log.d(TAG, "nativeClassInit " + isNativeInt);
        }
    }

    public int VivoSensorOprate(int type, float[] operate_reslt, int[] p_data, int len) {
        if (operate_reslt == null || p_data == null) {
            return -1;
        }
        if (!isSensorLibExist) {
            return -1;
        }
        InitNativeClass();
        SensorTestResult result = new SensorTestResult();
        Log.d(TAG, "VivoSensorOprate" + type + " " + p_data[0] + " " + p_data[1] + " " + p_data[2] + " " + len);
        int ret = jniVivoSensorTestInt(type, result, p_data, len);
        result.getTestResult(operate_reslt);
        return ret;
    }
}
