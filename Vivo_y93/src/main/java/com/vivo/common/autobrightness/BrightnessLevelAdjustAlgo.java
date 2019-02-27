package com.vivo.common.autobrightness;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Slog;
import com.vivo.common.brightmapping.MappingConfig;
import com.vivo.common.provider.Calendar.CalendarsColumns;
import com.vivo.common.provider.Calendar.Events;
import java.util.ArrayList;
import java.util.List;

public class BrightnessLevelAdjustAlgo {
    private static final String AUTOBRIGHTNESS_PARAM = "autobrighness_param";
    private static final String AUTObRIGHTNESS_BRIGHTNESS_VALUE = "autobrightness_brightness_value";
    private static final int DEFAULT_LIGHT_LEVEL = 1;
    private static int DEFCONFIG_BRIGHTNESS = 112;
    private static int MAX_LIGHT_LEVEL = 0;
    private static final int MIN_BRIGHTNESS_VALUE = 20;
    private static final int MSG_INERTIA_SENSOR_DISABLE = 5;
    private static final int MSG_INERTIA_SENSOR_ENABLE = 4;
    private static final int MSG_RESTORE_MODIFY_PARAMTER = 3;
    private static final int MSG_USER_MODIFY_AUTOBRIGHT_MODE = 2;
    private static final int MSG_USER_MODIFY_BRIGHTNESS = 1;
    private static final String TAG = "BrightnessLevelAdjustAlgo";
    private static final String mProductModel = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, "unkown").toLowerCase();
    private int MIN_LIGHT_LEVEL;
    private boolean bIsAutoBakclightAdjust = true;
    private boolean bUserSettingBrightness = false;
    private Handler mAdjustBarHandler;
    private int mAdjustBrightnessFlag = 0;
    private long mAdjustBrightnessTimeStamp = -1;
    private ModifyArgument mArg = new ModifyArgument();
    private int[] mAutoBrightnessDownLevels;
    private int[] mAutoBrightnessUpLevels;
    private int mBacklightMode = 0;
    private long mChangUpStartTimeStamp = -1;
    private boolean mChangeDownFlag = false;
    private int mChangeDownLux;
    private long mChangeDownTime = 0;
    private int mChangeUpLux;
    private long mChangeUpTime = 0;
    private Context mContext = null;
    private Handler mInertiaHandler;
    private int mLastScreenLevel = 2;
    private int mLastScreenValueLevelIndex = 15;
    private int[] mLcdBacklightValues;
    private int mLightLevel = 1;
    private int mMontionStatus = 0;
    private ModifyArgumentParser mParser = new ModifyArgumentParser();
    private int mPhoneStatusChangeDownLux = -1;
    private int mPrivBrightness = -1;
    private int mPriveMontionStatus = 0;
    private RgbCurveAlgoDataStruct mRgbCureAlgoData = null;
    private int mSecondSettingBrightness = -1;
    private SensorManager mSensorManager;
    private int mStepCount = -1;
    private SensorEventListener mStepCountListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            int step_value = (int) event.values[0];
            BrightnessLevelAdjustAlgo.this.mStepCount = step_value;
            BrightnessLevelAdjustAlgo.this.mRgbCureAlgoData.pushStepCountData(BrightnessLevelAdjustAlgo.this.mStepCount);
            Slog.e(BrightnessLevelAdjustAlgo.TAG, " mStepCountListener step_count = " + step_value);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private Sensor mStepCountSensor;
    private int mTempLightLevel = 1;
    private TimePeriod mTimePeriod = null;
    private int nCurrentBrightness = 0;
    private List<int[]> screenAutoBrightnessLevelList = new ArrayList();

    private class AdjustBarHandler extends Handler {
        public AdjustBarHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                Slog.e(BrightnessLevelAdjustAlgo.TAG, "handleMessage msg is NULL");
                return;
            }
            switch (msg.what) {
                case 1:
                    BrightnessLevelAdjustAlgo.this.modifyBrightness();
                    break;
                case 2:
                    BrightnessLevelAdjustAlgo.this.modifyBrightnessMode();
                    break;
                case 4:
                    BrightnessLevelAdjustAlgo.this.setSensorEnabledInner(true);
                    break;
                case 5:
                    BrightnessLevelAdjustAlgo.this.setSensorEnabledInner(false);
                    break;
            }
        }
    }

    private void log(String msg) {
        if (AblConfig.isDebug()) {
            Slog.d(TAG, msg);
        }
    }

    private void bbklog(String msg) {
        if (AblConfig.isDebug() || AblConfig.isBbkLog()) {
            Slog.d(TAG, msg);
        }
    }

    public BrightnessLevelAdjustAlgo(Context context, SensorManager sensorManager, Looper looper) {
        this.mContext = context;
        this.mSensorManager = sensorManager;
        this.mInertiaHandler = new Handler(looper);
        String jsonStr = System.getStringForUser(context.getContentResolver(), AUTOBRIGHTNESS_PARAM, -2);
        String brightnessString = System.getStringForUser(context.getContentResolver(), AUTObRIGHTNESS_BRIGHTNESS_VALUE, -2);
        this.mStepCountSensor = this.mSensorManager.getDefaultSensor(19);
        this.mAdjustBarHandler = new AdjustBarHandler(looper);
        this.mRgbCureAlgoData = new RgbCurveAlgoDataStruct(looper);
        initLightParametersNormal();
        initBrightnessValues(this.mContext);
        MAX_LIGHT_LEVEL = this.mAutoBrightnessUpLevels.length;
        this.MIN_LIGHT_LEVEL = 0;
        if (!(jsonStr == null || (Events.DEFAULT_SORT_ORDER.equals(jsonStr) ^ 1) == 0)) {
            this.mParser.stringToArgument(jsonStr, this.mArg);
            this.bUserSettingBrightness = this.mArg.bUserSettingBrightness;
        }
        if (this.bUserSettingBrightness && brightnessString != null) {
            setStringToBrightnessValueInt(brightnessString);
        }
    }

    private void initLightParametersNormal() {
        Resources resources = this.mContext.getResources();
        String configLightUp = Events.DEFAULT_SORT_ORDER;
        String configLightDown = Events.DEFAULT_SORT_ORDER;
        this.mAutoBrightnessUpLevels = resources.getIntArray(50921517);
        this.mAutoBrightnessDownLevels = resources.getIntArray(50921520);
        configLightDown = configLightDown + "config_autoBrightnessLevels_5_levels";
        configLightUp = configLightUp + "config_autoBrightnessUpLevels_5_levels";
        CollectConfiguration.getInstance().addConfiguration(AblConfig.KEY_CONFIG_LIGHT_NORMAL_LEVEL_DOWN, configLightDown);
        CollectConfiguration.getInstance().addConfiguration(AblConfig.KEY_CONFIG_LIGHT_NORMAL_LEVEL_UP, configLightUp);
    }

    private void initBrightnessValues(Context context) {
        Resources resources = context.getResources();
        String[] autoBrightnessLevelNames;
        if (mProductModel.startsWith("pd1621b")) {
            autoBrightnessLevelNames = resources.getStringArray(50923235);
            Slog.d(TAG, "loading pd1621b parameters");
        } else {
            autoBrightnessLevelNames = resources.getStringArray(50923235);
            Slog.d(TAG, "Fatal error, please provide correct parameters");
        }
        for (String identifier : autoBrightnessLevelNames) {
            Slog.d(TAG, "VIVO identifier=" + identifier);
            Slog.d(TAG, "VOVO identifier=" + identifier + " ID=" + resources.getIdentifier(identifier, null, null));
            this.screenAutoBrightnessLevelList.add(resources.getIntArray(resources.getIdentifier(identifier, null, null)));
        }
        this.mLastScreenValueLevelIndex = this.screenAutoBrightnessLevelList.size() / 2;
        adjustScreenBrightnessLevel(DEFCONFIG_BRIGHTNESS);
        this.mLcdBacklightValues = (int[]) this.screenAutoBrightnessLevelList.get(this.mLastScreenValueLevelIndex);
    }

    private void setSensorEnabled(boolean enable) {
        this.mAdjustBarHandler.removeMessages(4);
        this.mAdjustBarHandler.removeMessages(5);
        if (enable) {
            this.mAdjustBarHandler.sendEmptyMessage(4);
        } else {
            this.mAdjustBarHandler.sendEmptyMessage(5);
        }
    }

    private void setSensorEnabledInner(boolean enable) {
        if (enable) {
            if (this.mBacklightMode != 0) {
                this.mRgbCureAlgoData.clearParam();
                if (this.mStepCountSensor != null) {
                    this.mSensorManager.registerListener(this.mStepCountListener, this.mStepCountSensor, 2, this.mInertiaHandler);
                }
            }
        } else if (this.mStepCountSensor != null) {
            this.mSensorManager.unregisterListener(this.mStepCountListener);
        }
    }

    public void setLigtSensorEnable(boolean enable) {
        if (enable) {
            this.bIsAutoBakclightAdjust = true;
            this.mBacklightMode = 1;
            if (this.mBacklightMode != 0) {
                setSensorEnabled(true);
            }
        }
    }

    public void notifyStateChanged(int state) {
        switch (state) {
            case 3:
                if (AblConfig.isDebug()) {
                    Slog.d(TAG, "notifyStateChanged SCREEN_ON: bIsAutoBakclightAdjust=" + this.bIsAutoBakclightAdjust);
                }
                if (this.mBacklightMode != 0) {
                    setSensorEnabled(true);
                }
                this.mPrivBrightness = 0;
                return;
            case 5:
                if (AblConfig.isDebug()) {
                    Slog.d(TAG, "notifyStateChanged SCREEN_OFF: bIsAutoBakclightAdjust=" + this.bIsAutoBakclightAdjust);
                }
                if (!this.bIsAutoBakclightAdjust) {
                    this.mAdjustBarHandler.removeMessages(1);
                    this.mAdjustBarHandler.sendEmptyMessageDelayed(1, 1);
                }
                setSensorEnabled(false);
                return;
            case 18:
                if (this.mBacklightMode != 0) {
                    setSensorEnabled(true);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onGetSettings(int brightness, int mode, String offBy, String changeBy) {
        if (offBy.equals("com.vivo.upslide") || offBy.equals("com.android.settings") || offBy.equals("com.android.settings") || offBy.equals("com.android.systemui")) {
            setBacklightMode(mode);
        }
    }

    public void setSecondUserBrightness(int backlight, int screenState) {
        if (this.mBacklightMode == 1 && screenState == ScreenState.STATE_SCREEN_BRIGHT) {
            this.mSecondSettingBrightness = backlight;
        }
        if (AblConfig.isDebug()) {
            Slog.d(TAG, "setSecondUserBrightness mSecondSettingBrightness=" + this.mSecondSettingBrightness + " bUserSettingBrightness=" + this.bUserSettingBrightness + " mScreenState" + screenState);
        }
        this.nCurrentBrightness = backlight;
        this.bIsAutoBakclightAdjust = false;
        this.mAdjustBarHandler.removeMessages(1);
        this.mAdjustBarHandler.sendEmptyMessageDelayed(1, 2500);
    }

    public boolean isAutoBakclightAdjust() {
        return this.bIsAutoBakclightAdjust;
    }

    public void setBacklightMode(int mode) {
        if (mode == 0 && this.mBacklightMode != mode) {
            this.mSecondSettingBrightness = 0;
            this.bIsAutoBakclightAdjust = true;
            this.bUserSettingBrightness = false;
            this.mAdjustBarHandler.removeMessages(1);
            this.mAdjustBarHandler.removeMessages(2);
            this.mAdjustBarHandler.sendEmptyMessage(2);
            setSensorEnabled(false);
        }
        this.mBacklightMode = mode;
        if (AblConfig.isDebug()) {
            Slog.d(TAG, "setBacklightMode bUserSettingBrightness = " + this.bUserSettingBrightness);
        }
    }

    private void adjustScreenBrightnessLevel(int brightness) {
        int arrayLength = this.screenAutoBrightnessLevelList.size();
        int minValue = MappingConfig.isNeedMapping() ? 2 : 20;
        int value = brightness;
        int level = (int) (((float) (brightness - minValue)) / (((float) (255 - minValue)) / ((float) arrayLength)));
        if (level < 0) {
            level = 0;
        }
        int maxIndex = arrayLength - 1;
        if (level > maxIndex) {
            level = maxIndex;
        }
        if (level != this.mLastScreenValueLevelIndex) {
            this.mLastScreenValueLevelIndex = level;
            this.mLcdBacklightValues = (int[]) this.screenAutoBrightnessLevelList.get(this.mLastScreenValueLevelIndex);
        }
    }

    private String setBrightnessValueIntToString() {
        String ret = Events.DEFAULT_SORT_ORDER;
        for (int i : this.mLcdBacklightValues) {
            ret = ret + i + ",";
        }
        return ret;
    }

    private void setStringToBrightnessValueInt(String str) {
        int k = 0;
        int m = this.mLcdBacklightValues.length;
        String[] temp = str.split(",");
        for (int i = 0; i < m; i++) {
            this.mLcdBacklightValues[i] = Integer.parseInt(temp[k], 10);
            k++;
        }
    }

    private int vivoCalaBrightnessLevel(int lux) {
        int level = this.mLightLevel;
        int i;
        if (level == this.MIN_LIGHT_LEVEL) {
            i = this.MIN_LIGHT_LEVEL;
            while (lux > this.mAutoBrightnessUpLevels[i]) {
                i++;
                level++;
                if (level == MAX_LIGHT_LEVEL) {
                    break;
                }
            }
        } else if (level == MAX_LIGHT_LEVEL) {
            i = MAX_LIGHT_LEVEL - 1;
            while (lux < this.mAutoBrightnessDownLevels[i]) {
                i--;
                level--;
                if (level == this.MIN_LIGHT_LEVEL) {
                    break;
                }
            }
        } else if (lux > this.mAutoBrightnessUpLevels[level]) {
            i = level;
            while (lux > this.mAutoBrightnessUpLevels[i]) {
                i++;
                level++;
                if (level == MAX_LIGHT_LEVEL) {
                    break;
                }
            }
        } else if (lux < this.mAutoBrightnessDownLevels[level]) {
            i = level - 1;
            while (lux < this.mAutoBrightnessDownLevels[i]) {
                i--;
                level--;
                if (level == this.MIN_LIGHT_LEVEL) {
                    break;
                }
            }
        }
        Slog.e(TAG, "calcLightLevel lux=" + lux + " level=" + level);
        return level;
    }

    private int vivoCalacOrigBrighntenss(int x_gm) {
        int level = vivoCalaBrightnessLevel(x_gm);
        adjustScreenBrightnessLevel(DEFCONFIG_BRIGHTNESS);
        int brightness = this.mLcdBacklightValues[level];
        this.mTempLightLevel = level;
        Slog.e(TAG, "vivoCalacOrigBrighntenss x_gm=" + x_gm + " level=" + level + " brightness:" + brightness);
        return brightness;
    }

    private int vivoCalacBrighntenssFinnal(int x_gm) {
        int level = vivoCalaBrightnessLevel(x_gm);
        this.mTempLightLevel = level;
        return this.mLcdBacklightValues[level];
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0038  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void vivoCalaUpDownGmForBrightnessLevel(int current_brightness) {
        this.mChangeDownLux = this.mAutoBrightnessDownLevels[0];
        this.mChangeUpLux = this.mAutoBrightnessUpLevels[MAX_LIGHT_LEVEL - 1];
        int i = MAX_LIGHT_LEVEL;
        while (i >= 0) {
            if (current_brightness <= (i > 0 ? this.mLcdBacklightValues[i - 1] : 0) || current_brightness > this.mLcdBacklightValues[i]) {
                i--;
            } else {
                if (i <= 0) {
                    this.mChangeUpLux = this.mAutoBrightnessUpLevels[0];
                } else if (i >= MAX_LIGHT_LEVEL) {
                    this.mChangeUpLux = this.mAutoBrightnessUpLevels[i - 1];
                } else {
                    this.mChangeUpLux = this.mAutoBrightnessUpLevels[i];
                }
                for (i = 1; i <= MAX_LIGHT_LEVEL; i++) {
                    if (current_brightness == this.mLcdBacklightValues[i]) {
                        this.mChangeDownLux = this.mAutoBrightnessDownLevels[i - 1];
                        break;
                    }
                }
                Slog.e(TAG, "vivoCalaUpDownGmForBrightnessLevel mChangeUpLux " + this.mChangeUpLux + " mChangeDownLux " + this.mChangeDownLux + "current_brightness" + current_brightness);
            }
        }
        while (i <= MAX_LIGHT_LEVEL) {
        }
        Slog.e(TAG, "vivoCalaUpDownGmForBrightnessLevel mChangeUpLux " + this.mChangeUpLux + " mChangeDownLux " + this.mChangeDownLux + "current_brightness" + current_brightness);
    }

    private void vivoCalacUpDownGm(int x_gm, int current_brightness) {
        vivoCalaUpDownGmForBrightnessLevel(current_brightness);
        this.mPhoneStatusChangeDownLux = this.mChangeDownLux;
    }

    public void brightnessBeenApplied(AutobrightInfo info) {
        log("brightnessBeenApplied vivo_calac_up_down_gm lux=" + info.mLightLux + " mBrightness=" + info.mBrightness);
        this.mLightLevel = this.mTempLightLevel;
        vivoCalacUpDownGm(info.mLightLux, info.mBrightness);
        if (!AblConfig.isUseUnderDisplayLight()) {
            return;
        }
        if (info.mChangeDownLux != this.mChangeDownLux || info.mChangeUpLux != this.mChangeUpLux) {
            info.mRecitfiedLuxLock = info.mUnderDispalyRecitfiedLux;
            info.mDriverLuxLock = info.mUnderDispalyDriverLux;
            info.mChangeDownLux = this.mChangeDownLux;
            info.mChangeUpLux = this.mChangeUpLux;
            info.mUnderDisplayThreshChanged = true;
        }
    }

    public void setProximityPositiveToNegative(boolean to) {
        if (to) {
            this.mAdjustBrightnessFlag = 0;
        }
    }

    private void vivoAdjustBrightnessLevel(int cneed_brightness) {
        int i;
        int level = 0;
        int brightness = this.mLcdBacklightValues[this.mLightLevel];
        for (i = 0; i <= MAX_LIGHT_LEVEL; i++) {
            if (this.mLcdBacklightValues[i] >= cneed_brightness) {
                level = i;
                break;
            }
        }
        if (level > this.mLightLevel) {
            for (i = this.mLightLevel; i < level; i++) {
                this.mLcdBacklightValues[i] = cneed_brightness;
            }
        } else {
            for (i = level; i <= this.mLightLevel; i++) {
                this.mLcdBacklightValues[i] = cneed_brightness;
            }
        }
        for (i = 0; i <= MAX_LIGHT_LEVEL; i++) {
            Slog.e(TAG, "vivoAdjustBrightnessLevel mLcdBacklightValues[" + i + "] =" + this.mLcdBacklightValues[i]);
        }
    }

    private void modifyBrightness() {
        Slog.d(TAG, "modifyBrightness");
        if (AblConfig.isUseBrightnessLevel()) {
            adjustScreenBrightnessLevel(this.mSecondSettingBrightness);
            vivoAdjustBrightnessLevel(this.mSecondSettingBrightness);
            this.bUserSettingBrightness = true;
        }
        this.mArg.bUserSettingBrightness = this.bUserSettingBrightness;
        this.bIsAutoBakclightAdjust = true;
        String jsonStr = this.mParser.argumentToJsonString(this.mArg);
        String brigntnessValueString = setBrightnessValueIntToString();
        if (jsonStr != null && (Events.DEFAULT_SORT_ORDER.equals(jsonStr) ^ 1) != 0) {
            System.putString(this.mContext.getContentResolver(), AUTOBRIGHTNESS_PARAM, jsonStr);
            System.putString(this.mContext.getContentResolver(), AUTObRIGHTNESS_BRIGHTNESS_VALUE, brigntnessValueString);
            if (AblConfig.isDebug()) {
                Slog.d(TAG, "modifyBrightness jsonStr = " + jsonStr);
            }
        }
    }

    private void modifyBrightnessMode() {
        this.bUserSettingBrightness = false;
        if (AblConfig.isUseBrightnessLevel()) {
            adjustScreenBrightnessLevel(DEFCONFIG_BRIGHTNESS);
        }
        this.mArg.bUserSettingBrightness = this.bUserSettingBrightness;
        String jsonString = this.mParser.argumentToJsonString(this.mArg);
        if (!(jsonString == null || (Events.DEFAULT_SORT_ORDER.equals(jsonString) ^ 1) == 0)) {
            System.putString(this.mContext.getContentResolver(), AUTOBRIGHTNESS_PARAM, jsonString);
            if (AblConfig.isDebug()) {
                Slog.d(TAG, "modifyBrightnessMode jsonString = " + jsonString);
            }
        }
        System.putString(this.mContext.getContentResolver(), UserModifyRecorder.KEY_APP_BRIGHTNESS_RATIO, Events.DEFAULT_SORT_ORDER);
    }

    private int calcBrightness(int lux, boolean waitFirst) {
        int brightness;
        if (this.bUserSettingBrightness) {
            brightness = vivoCalacBrighntenssFinnal(lux);
        } else {
            brightness = vivoCalacOrigBrighntenss(lux);
        }
        if (brightness <= 1) {
            brightness = 2;
        } else if (brightness > 255) {
            brightness = 255;
        }
        log("calcBrightness brightness=" + brightness);
        return brightness;
    }

    private int vivoCalacMotionChangeDownTheashAls(int lux) {
        int templux = lux;
        int i = 0;
        while (i < MAX_LIGHT_LEVEL && lux < this.mAutoBrightnessDownLevels[i]) {
            i++;
        }
        if (i >= 2) {
            templux = this.mAutoBrightnessDownLevels[i - 1];
        } else {
            templux = -1;
        }
        Slog.d(TAG, "Als  templux " + templux);
        return templux;
    }

    public AutobrightInfo getAutoBrightness(boolean waitFirst, boolean mProximityStatus, int lux, AutobrightInfo info) {
        boolean adjust_flag = false;
        AutobrightInfo abInfo = new AutobrightInfo();
        abInfo.copyFrom(info);
        boolean mLightLuxApplied = false;
        int tempLux = lux;
        int changeTime = 0;
        long t = SystemClock.uptimeMillis();
        this.mChangeDownFlag = false;
        this.mPriveMontionStatus = this.mMontionStatus;
        this.mMontionStatus = this.mRgbCureAlgoData.getPhoneMontionStatus();
        if (this.mMontionStatus == 1 && this.mPriveMontionStatus != this.mMontionStatus) {
            this.mChangeDownLux = vivoCalacMotionChangeDownTheashAls(info.mLightLux);
            Slog.d(TAG, "vivo getAutoBrightness dlux " + this.mChangeDownLux + " motion=" + this.mMontionStatus);
        } else if (this.mPriveMontionStatus != this.mMontionStatus && this.mMontionStatus == 0) {
            this.mChangeDownLux = this.mPhoneStatusChangeDownLux;
            this.mChangeUpTime = t;
            this.mChangeDownTime = t;
            Slog.d(TAG, "vivo getAutoBrightness dlux " + this.mChangeDownLux + " motion=" + this.mMontionStatus);
        }
        if (waitFirst) {
            mLightLuxApplied = true;
            this.nCurrentBrightness = calcBrightness(lux, waitFirst);
            vivoCalacUpDownGm(lux, this.nCurrentBrightness);
            this.mAdjustBrightnessFlag = 0;
            this.mChangeUpTime = t;
            this.mChangeDownTime = t;
        } else if (lux >= this.mChangeUpLux) {
            if (this.mAdjustBrightnessFlag != 2) {
                this.mAdjustBrightnessFlag = 2;
                this.mAdjustBrightnessTimeStamp = SystemClock.uptimeMillis();
            }
            if (AblConfig.isUse2048GrayScaleBacklight() && (AblConfig.isUseOLEDLcm() ^ 1) != 0 && lux <= 2) {
                adjust_flag = true;
            }
            if (t - this.mAdjustBrightnessTimeStamp > 400 || adjust_flag) {
                Slog.d(TAG, "vivo lux " + lux);
                this.mChangeUpTime = t;
                mLightLuxApplied = true;
                this.nCurrentBrightness = calcBrightness(lux, waitFirst);
                this.mChangUpStartTimeStamp = t;
                this.mAdjustBrightnessFlag = 0;
                abInfo.mPrivBrightness = this.mPrivBrightness;
            }
            log("AutoBakclight  nUpBrightness mAdjustBrightnessTimeStamp =" + this.mAdjustBrightnessTimeStamp + " t =" + t);
        } else if (lux > this.mChangeDownLux || (mProximityStatus ^ 1) == 0) {
            this.mAdjustBrightnessFlag = 0;
        } else {
            int brightness = calcBrightness(lux, waitFirst);
            if (this.nCurrentBrightness < 9) {
                changeTime = 240000;
            } else if (this.nCurrentBrightness < 12) {
                changeTime = 200000;
            } else if (this.nCurrentBrightness <= 15) {
                changeTime = 180000;
            } else if (this.nCurrentBrightness < 20) {
                changeTime = StateInfo.STATE_FINGERPRINT_GOTO_SLEEP;
            }
            if (t - this.mChangeUpTime > ((long) changeTime)) {
                if (this.mAdjustBrightnessFlag != 1) {
                    this.mAdjustBrightnessFlag = 1;
                    this.mAdjustBrightnessTimeStamp = SystemClock.uptimeMillis();
                }
                int delayTime = 5500;
                if (AblConfig.isUse2048GrayScaleBacklight()) {
                    if (this.nCurrentBrightness == 4) {
                        delayTime = 100000;
                    } else if (this.nCurrentBrightness <= 6) {
                        delayTime = StateInfo.STATE_FINGERPRINT_GOTO_SLEEP;
                    }
                    if (t - this.mChangUpStartTimeStamp < 4000) {
                        delayTime = CalendarsColumns.RESPOND_ACCESS;
                    }
                }
                if (t - this.mAdjustBrightnessTimeStamp > ((long) delayTime)) {
                    Slog.d(TAG, "vivo lux " + lux);
                    if (this.mMontionStatus == 1) {
                        tempLux = vivoCalacMotionChangeDownTheashAls(info.mLightLux);
                        this.mChangeDownLux = tempLux;
                    }
                    mLightLuxApplied = true;
                    this.mChangeDownFlag = true;
                    this.nCurrentBrightness = brightness;
                    this.mAdjustBrightnessFlag = 0;
                    abInfo.mPrivBrightness = this.mPrivBrightness;
                    Slog.d(TAG, "vivo   nCurrentBrightness " + this.nCurrentBrightness + " templux= " + tempLux + " lux=" + lux);
                }
                abInfo.mScreenLevel = 1;
                log("AutoBakclight  nDownBrightness mAdjustBrightnessTimeStamp =" + this.mAdjustBrightnessTimeStamp + " t =" + t);
            }
        }
        if (this.mChangeDownFlag && ((this.nCurrentBrightness >= 7 && Math.abs(this.nCurrentBrightness - this.mPrivBrightness) < 5) || (this.nCurrentBrightness < 7 && Math.abs(this.nCurrentBrightness - this.mPrivBrightness) < 1))) {
            mLightLuxApplied = false;
            this.nCurrentBrightness = this.mPrivBrightness;
            this.mChangeDownFlag = false;
            this.mChangeUpTime = t;
        }
        if (this.mChangeDownFlag) {
            this.nCurrentBrightness = Math.min(this.nCurrentBrightness, this.mPrivBrightness);
            this.mChangeDownTime = t;
        }
        if (!mLightLuxApplied) {
            log("LightLux not applied lux " + lux + " to return " + abInfo.mLightLux);
            lux = abInfo.mLightLux;
        }
        bbklog("AutoBakclight light=" + lux + " Current=" + this.nCurrentBrightness + " " + this.mChangeUpLux + " " + this.mChangeDownLux + "mMontionStatus =" + this.mMontionStatus + " bUserSettingBrightness=" + this.bUserSettingBrightness + " waitFirst=" + waitFirst + " mLightLuxApplied =" + mLightLuxApplied);
        if (AblConfig.isUseUnderDisplayLight() && !(abInfo.mChangeDownLux == this.mChangeDownLux && abInfo.mChangeUpLux == this.mChangeUpLux)) {
            abInfo.mRecitfiedLuxLock = abInfo.mUnderDispalyRecitfiedLux;
            abInfo.mDriverLuxLock = abInfo.mUnderDispalyDriverLux;
            abInfo.mChangeDownLux = this.mChangeDownLux;
            abInfo.mChangeUpLux = this.mChangeUpLux;
            abInfo.mUnderDisplayThreshChanged = true;
        }
        abInfo.mBrightness = this.nCurrentBrightness;
        abInfo.mLightLux = lux;
        this.mPrivBrightness = this.nCurrentBrightness;
        abInfo.mStepCount = this.mStepCount;
        return abInfo;
    }
}
