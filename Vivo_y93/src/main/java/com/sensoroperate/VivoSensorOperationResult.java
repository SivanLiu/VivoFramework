package com.sensoroperate;

import android.util.Slog;

public class VivoSensorOperationResult {
    private static final String TAG = "VivoSensorOperationResult";
    public static final int VIVO_SENSOR_OP_FAIL = 0;
    public static final int VIVO_SENSOR_OP_SUCCESS = 1;
    public static final int VIVO_SENSOR_OP_UNKNOW = -1;
    public String mInfo = "No operation result";
    public int mSuccess = -1;
    public float[] mTestVal = new float[12];

    public void showTestResult() {
        Slog.d(TAG, "mSuccess=" + this.mSuccess + " mTestVal=[" + this.mTestVal[0] + "," + this.mTestVal[1] + "," + this.mTestVal[2] + " ," + this.mTestVal[3] + "," + this.mTestVal[4] + "," + this.mTestVal[5] + " , " + this.mTestVal[6] + "," + this.mTestVal[7] + "," + this.mTestVal[8] + " , " + this.mTestVal[9] + "," + this.mTestVal[10] + "," + this.mTestVal[11] + " , " + " info=" + this.mInfo);
    }

    public String toString() {
        return "mSuccess=" + this.mSuccess + " mTestVal=[" + this.mTestVal[0] + "," + this.mTestVal[1] + "," + this.mTestVal[2] + " ," + this.mTestVal[3] + "," + this.mTestVal[4] + "," + this.mTestVal[5] + " , " + this.mTestVal[6] + "," + this.mTestVal[7] + "," + this.mTestVal[8] + " , " + this.mTestVal[9] + "," + this.mTestVal[10] + "," + this.mTestVal[11] + " , " + " info=" + this.mInfo;
    }
}
