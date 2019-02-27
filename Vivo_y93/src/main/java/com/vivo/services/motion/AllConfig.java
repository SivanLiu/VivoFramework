package com.vivo.services.motion;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import com.sensoroperate.SensorTestResult;
import com.sensoroperate.VivoSensorTest;
import com.vivo.common.autobrightness.AblConfig;
import com.vivo.common.provider.Calendar.Events;

public class AllConfig {
    public static float POSTURE_X_ANGLE = 0.0f;
    public static float POSTURE_Y_ANGLE = 0.0f;
    public static float REMIND_ANGLE = 15.0f;
    public static final float ROMVERION10 = 1.0f;
    public static final float ROMVERION20 = 2.0f;
    private static final int SENSOR_COMMAND_SET_PS_PARA_INDEX = 9;
    public static final int STATE_DIRECT_CALILING = 2;
    public static final int STATE_PROXIMITY_ACCROSS = 4;
    public static final int STATE_SCREEN_OFF_WAKEUP = 8;
    public static float SWITCH_ANGLE = 30.0f;
    private static final String TAG = "AllConfig";
    public static boolean mIsADSPAKMVirtGryo = false;
    public static boolean mIsAKMVirtGryo = false;
    public static boolean mIsALPSVirtGryo = false;
    public static boolean mIsArchADSP = false;
    public static boolean mIsDoubleloudspeaker = false;
    public static boolean mIsNewMTKArch = false;
    public static boolean mIsPhyGryo = false;
    public static boolean mIsShakeTwo = false;
    public static boolean mIsYASVirtGryo = false;
    public static boolean mLimitSwitch = false;
    public static boolean mNeedMag = false;
    public static boolean mOpenDirectory = false;
    private static String mPlatformName = Events.DEFAULT_SORT_ORDER;
    private static String mProductName = Events.DEFAULT_SORT_ORDER;
    public static float mRomVersion = 1.0f;
    private static String mRomVersionSt = Events.DEFAULT_SORT_ORDER;
    public static boolean mScreenOffWakeupDeviceNode = false;
    public static boolean mVibrate_badly = false;

    AllConfig() {
        String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
        Log.d(TAG, "project is" + prop);
        mPlatformName = SystemProperties.get("ro.vivo.product.platform", null);
        Log.d(TAG, "mPlatformName is" + mPlatformName);
        if (prop != null && (prop.equals("PD1303") || prop.equals("PD1405A") || mPlatformName.equals("QCOM8917") || mPlatformName.equals("QCOM8937") || mPlatformName.equals("QCOM8953") || mPlatformName.equals("QCOM8976") || mPlatformName.equals("QCOM8996") || mPlatformName.equals("SDM660") || mPlatformName.equals("SDM845") || mPlatformName.equals("SDM710") || mPlatformName.equals("SDM670") || mPlatformName.equals("SDM439"))) {
            mIsArchADSP = true;
        }
        if (prop != null && prop.equals("PD1503")) {
            mIsDoubleloudspeaker = true;
        }
        if (prop != null && prop.equals("PD1612F_EX")) {
            mVibrate_badly = true;
        }
        if (prop != null && (mPlatformName.equals("MTK6765") || mPlatformName.equals("MTK6771") || mPlatformName.equals("MTK6761"))) {
            mIsNewMTKArch = true;
        }
        if (prop != null && prop.equals("PD1401BL")) {
            mIsShakeTwo = true;
        }
        if (prop != null && (prop.equals("PD1410L") || prop.equals("PD1410F") || prop.equals("PD1410V") || prop.equals("PD1410LG4") || prop.equals("PD1408L") || prop.equals("PD1408F") || prop.equals("PD1408V") || prop.equals("PD1408LG4") || prop.equals("PD1304CL") || prop.equals("PD1304CF") || prop.equals("PD1304CV") || prop.equals("PD1304CLG4") || prop.equals("PD1403L") || prop.equals("PD1403F") || prop.equals("PD1403V") || prop.equals("PD1403LG4") || prop.equals("PD1419L") || prop.equals("PD1419V") || prop.equals("PD1419LG4") || prop.equals("PD1401V") || prop.equals("PD1401F") || prop.equals("PD1420L") || prop.equals("PD1420LG4") || prop.equals("PD1420F") || prop.equals("PD1420V") || prop.equals("PD1420F_EX") || prop.equals("TD1401") || prop.equals("TD1404") || prop.equals("PD1408F_EX") || prop.equals("PD1401F_EX") || prop.equals("PD1410F_EX") || prop.equals("PD1403W_EX"))) {
            mScreenOffWakeupDeviceNode = true;
        }
        if (prop != null && prop.equals("PD1401F")) {
            mOpenDirectory = true;
        }
        if (prop != null && prop.equals("PD1408F_EX")) {
            mLimitSwitch = true;
        }
        mRomVersionSt = SystemProperties.get("ro.vivo.rom.version", null);
        Log.d(TAG, "mRomVersion:" + mRomVersionSt);
        if (mRomVersionSt != null && mRomVersionSt.equals("rom_2.0")) {
            mRomVersion = 2.0f;
        } else if (mRomVersionSt != null && mRomVersionSt.equals("rom_1.0")) {
            mRomVersion = 1.0f;
        }
        if (prop != null && prop.equals("TD1405")) {
            mIsAKMVirtGryo = true;
        }
    }

    public static void changeProximityParam(boolean change, int state) {
        SensorTestResult mTempRes = new SensorTestResult();
        VivoSensorTest mVivoSensorTest = VivoSensorTest.getInstance();
        int[] mTempTestArg = new int[3];
        mTempTestArg[0] = 9;
        if (change) {
            mTempTestArg[1] = 1;
        } else {
            mTempTestArg[1] = 0;
        }
        mTempTestArg[2] = state;
        if (mVivoSensorTest != null) {
            Log.d(TAG, "ps_para: data[0]" + mTempTestArg[0] + " data[1]=" + mTempTestArg[1] + "data[2]=" + mTempTestArg[2]);
            mVivoSensorTest.vivoSensorTest(45, mTempRes, mTempTestArg, mTempTestArg.length);
        }
    }

    public static void collectAirOperationData(Context context, int type, boolean success, int reason) {
        MotionExceptionCollect mMotionExceptionCollect = MotionExceptionCollect.getInstance(context);
        if (mMotionExceptionCollect != null) {
            mMotionExceptionCollect.onAirOperationTriggered(type, success, reason);
        }
    }

    public static void collectRaiseUpException(Context context) {
        MotionExceptionCollect mMotionExceptionCollect = MotionExceptionCollect.getInstance(context);
        if (mMotionExceptionCollect != null) {
            mMotionExceptionCollect.onRaiseUpExceptionTriggered();
        }
    }
}
