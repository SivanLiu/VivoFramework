package com.vivo.common.proximity;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import com.sensoroperate.SensorTestResult;
import com.sensoroperate.VivoSensorTest;
import com.vivo.common.autobrightness.AblConfig;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class ProximityStatus {
    private static final int PROXIMITY_DUAL = 2;
    private static final int PROXIMITY_SINGLE = 1;
    private static final int PS_GET_TEMP_PARA = 47;
    private static final int PS_RAWDATA_TEST = 33;
    private static final String TAG = "ProximityStatus";
    private float[] DefBase;
    private float[] MaxBase;
    private float[] MinBase;
    private float[] TestVal;
    private int[] args;
    private float[] data;
    private boolean isUseDualProximity;
    private VivoSensorTest mVivoSensorTest;
    private float[] range;
    private SensorTestResult result;

    public static class ProximityData {
        public float mDataX;
        public float mDataY;
        public float mDataZ;
    }

    public static class ProximityRange {
        public float mDefault;
        public float mMax;
        public float mMin;
    }

    public ProximityStatus() {
        this.mVivoSensorTest = null;
        this.result = new SensorTestResult();
        this.isUseDualProximity = AblConfig.isUseDualProximity();
        this.TestVal = new float[3];
        this.DefBase = new float[3];
        this.MinBase = new float[3];
        this.MaxBase = new float[3];
        this.data = new float[6];
        this.range = new float[6];
        this.args = new int[1];
        this.mVivoSensorTest = VivoSensorTest.getInstance();
        this.args[0] = 1;
    }

    private boolean isUseDualProximitySensor() {
        return this.isUseDualProximity;
    }

    public int getProximityCounts() {
        return this.isUseDualProximity ? 2 : 1;
    }

    public ProximityData getProximityData(int index) {
        ProximityData mProximityData = new ProximityData();
        this.mVivoSensorTest.vivoSensorTest(PS_RAWDATA_TEST, this.result, this.args, 0);
        int ret = this.result.getAllTestResult(this.TestVal, this.DefBase, this.MinBase, this.MaxBase);
        switch (index) {
            case 2:
                mProximityData.mDataX = this.DefBase[0];
                mProximityData.mDataY = this.DefBase[1];
                mProximityData.mDataZ = this.DefBase[2];
                break;
        }
        mProximityData.mDataX = this.TestVal[0];
        mProximityData.mDataY = this.TestVal[1];
        mProximityData.mDataZ = this.DefBase[0];
        return mProximityData;
    }

    public ProximityRange getProximityRange(int index) {
        ProximityRange mProximityRange = new ProximityRange();
        this.mVivoSensorTest.vivoSensorTest(PS_GET_TEMP_PARA, this.result, this.args, 0);
        int ret = this.result.getAllTestResult(this.TestVal, this.DefBase, this.MinBase, this.MaxBase);
        switch (index) {
            case 2:
                mProximityRange.mMin = this.MinBase[1];
                mProximityRange.mMax = this.MaxBase[1];
                mProximityRange.mDefault = this.DefBase[1];
                break;
        }
        mProximityRange.mMin = this.MinBase[0];
        mProximityRange.mMax = this.MaxBase[0];
        mProximityRange.mDefault = this.MaxBase[1];
        return mProximityRange;
    }
}
