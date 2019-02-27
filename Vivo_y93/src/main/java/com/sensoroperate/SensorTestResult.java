package com.sensoroperate;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.util.Log;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class SensorTestResult {
    private static final String TAG = "SensorTestResult";
    public float[] mDefBase = new float[3];
    public String mInfo = "Not finish yet.";
    public float[] mMaxBase = new float[3];
    public float[] mMinBase = new float[3];
    public int mSuccess = -1;
    public float[] mTestVal = new float[3];

    public void showTestResult() {
        Log.d(TAG, "mSuccess=" + this.mSuccess + " mTestVal=[" + this.mTestVal[0] + "," + this.mTestVal[1] + "," + this.mTestVal[2] + "]" + " mDefBase=[" + this.mDefBase[0] + "," + this.mDefBase[1] + "," + this.mDefBase[2] + "]" + " mMinBase=[" + this.mMinBase[0] + "," + this.mMinBase[1] + "," + this.mMinBase[2] + "]" + " mMaxBase=[" + this.mMaxBase[0] + "," + this.mMaxBase[1] + "," + this.mMaxBase[2] + "]" + " info=" + this.mInfo);
    }

    public String string() {
        return "mSuccess=" + this.mSuccess + " mTestVal=[" + this.mTestVal[0] + "," + this.mTestVal[1] + "," + this.mTestVal[2] + "]" + " mDefBase=[" + this.mDefBase[0] + "," + this.mDefBase[1] + "," + this.mDefBase[2] + "]" + " mMinBase=[" + this.mMinBase[0] + "," + this.mMinBase[1] + "," + this.mMinBase[2] + "]" + " mMaxBase=[" + this.mMaxBase[0] + "," + this.mMaxBase[1] + "," + this.mMaxBase[2] + "]" + " info=" + this.mInfo;
    }

    public String dumpString() {
        return "mSuccess=" + this.mSuccess + " mTestVal=[" + this.mTestVal[0] + "," + this.mTestVal[1] + "," + this.mTestVal[2] + "]" + " mDefBase=[" + this.mDefBase[0] + "," + this.mDefBase[1] + "," + this.mDefBase[2] + "]" + " mMinBase=[" + this.mMinBase[0] + "," + this.mMinBase[1] + "," + this.mMinBase[2] + "]" + " mMaxBase=[" + this.mMaxBase[0] + "," + this.mMaxBase[1] + "," + this.mMaxBase[2] + "]" + " info=" + this.mInfo;
    }

    public int getAllTestResult(float[] TestVal, float[] DefBase, float[] MinBase, float[] MaxBase) {
        TestVal[0] = this.mTestVal[0];
        TestVal[1] = this.mTestVal[1];
        TestVal[2] = this.mTestVal[2];
        DefBase[0] = this.mDefBase[0];
        DefBase[1] = this.mDefBase[1];
        DefBase[2] = this.mDefBase[2];
        MinBase[0] = this.mMinBase[0];
        MinBase[1] = this.mMinBase[1];
        MinBase[2] = this.mMinBase[2];
        MaxBase[0] = this.mMaxBase[0];
        MaxBase[1] = this.mMaxBase[1];
        MaxBase[2] = this.mMaxBase[2];
        return this.mSuccess;
    }

    public int getTestResult(float[] operate_reslt) {
        Log.d(TAG, "mSuccess=" + this.mSuccess + " mTestVal=[" + this.mTestVal[0] + "," + this.mTestVal[1] + "," + this.mTestVal[2] + "]" + " mDefBase=[" + this.mDefBase[0] + "," + this.mDefBase[1] + "," + this.mDefBase[2] + "]" + " mMinBase=[" + this.mMinBase[0] + "," + this.mMinBase[1] + "," + this.mMinBase[2] + "]" + " mMaxBase=[" + this.mMaxBase[0] + "," + this.mMaxBase[1] + "," + this.mMaxBase[2] + "]" + " info=" + this.mInfo);
        if (this.mSuccess != 0) {
            return 1;
        }
        operate_reslt[0] = this.mTestVal[0];
        operate_reslt[1] = this.mTestVal[1];
        operate_reslt[2] = this.mTestVal[2];
        return 0;
    }
}
