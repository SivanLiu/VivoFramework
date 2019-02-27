package com.vivo.common.autobrightness;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.os.Looper;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import java.util.Arrays;

public class SuperPowerSaveMode {
    private static final int DEFAULT_LIGHT_LEVEL = 1;
    private static int MAX_LIGHT_LEVEL = 0;
    private static final String TAG = "SuperPowerSaveMode";
    private static final String mProductModel = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, "unkown").toLowerCase();
    private int MIN_LIGHT_LEVEL;
    private int mAdjustBrightnessFlag = 0;
    private long mAdjustBrightnessTimeStamp = -1;
    private int[] mAutoBrightnessDownLevels;
    private int[] mAutoBrightnessUpLevels;
    private int[] mCamBrightModeTarget;
    private int[] mCamBrightModeThres;
    private int mChangeDownLux;
    private int mChangeUpLux;
    private Context mContext = null;
    private int mLightLevel = 1;
    private int[] mSuperPowerSavingBacklightValues;
    private int nCurrentBrightness = 0;

    private void log(String msg) {
        if (AblConfig.isDebug()) {
            Slog.d(TAG, msg);
        }
    }

    public SuperPowerSaveMode(Context context, SensorManager sensorManager, Looper looper) {
        this.mContext = context;
        initLightDownUpParam();
        initSuperPowerSaveModeBrightnesValue();
        initCamBrightModeParam();
        MAX_LIGHT_LEVEL = this.mAutoBrightnessUpLevels.length;
        this.MIN_LIGHT_LEVEL = 0;
    }

    private void initLightDownUpParam() {
        Resources resources = this.mContext.getResources();
        this.mAutoBrightnessUpLevels = resources.getIntArray(50921517);
        this.mAutoBrightnessDownLevels = resources.getIntArray(50921520);
    }

    private void initSuperPowerSaveModeBrightnesValue() {
        Resources resources = this.mContext.getResources();
        if (mProductModel.startsWith("pd1621b")) {
            this.mSuperPowerSavingBacklightValues = resources.getIntArray(50923268);
            Slog.d(TAG, "loading pd1621b parameters");
        } else if (mProductModel.startsWith("vtd1702") || mProductModel.startsWith("pd1728") || mProductModel.startsWith("pd1729") || mProductModel.startsWith("pd1709") || mProductModel.startsWith("pd1710") || mProductModel.startsWith("pd1721")) {
            this.mSuperPowerSavingBacklightValues = resources.getIntArray(50923269);
            Slog.d(TAG, "loading vtd1702 parameters");
        } else if (mProductModel.startsWith("pd1730")) {
            this.mSuperPowerSavingBacklightValues = resources.getIntArray(50923270);
            Slog.d(TAG, "loading pd1730 parameters");
        } else if (mProductModel.startsWith("pd1731")) {
            this.mSuperPowerSavingBacklightValues = resources.getIntArray(50923271);
            Slog.d(TAG, "loading pd1731 parameters");
        } else {
            this.mSuperPowerSavingBacklightValues = resources.getIntArray(50923268);
            Slog.d(TAG, "please provide correct parameters");
        }
    }

    private void initCamBrightModeParam() {
        int thresId = getIdByName("array", "cam_bright_mode_thres_" + mProductModel);
        int targetId = getIdByName("array", "cam_bright_mode_target_" + mProductModel);
        if (thresId == -1 || targetId == -1) {
            Slog.e(TAG, "init param failed " + thresId + " " + targetId);
            return;
        }
        Resources resources = this.mContext.getResources();
        this.mCamBrightModeThres = resources.getIntArray(thresId);
        this.mCamBrightModeTarget = resources.getIntArray(targetId);
        Slog.d(TAG, "thres : " + Arrays.toString(this.mCamBrightModeThres));
        Slog.d(TAG, "target : " + Arrays.toString(this.mCamBrightModeTarget));
        AblConfig.setCamBrightModeParam(this.mCamBrightModeThres, this.mCamBrightModeTarget);
    }

    private int vivoCalaSBrightnessLevel(int lux) {
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

    private int vivoCalaBrighntenss(int x_gm) {
        int level = vivoCalaSBrightnessLevel(x_gm);
        int brightness = this.mSuperPowerSavingBacklightValues[level];
        Slog.e(TAG, "vivoCalaBrighntenss x_gm=" + x_gm + " level=" + level + " brightness:" + brightness);
        return brightness;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0038  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void vivoCalacUpDownGm(int x_gm, int current_brightness) {
        this.mChangeDownLux = this.mAutoBrightnessDownLevels[0];
        this.mChangeUpLux = this.mAutoBrightnessUpLevels[MAX_LIGHT_LEVEL - 1];
        int i = MAX_LIGHT_LEVEL;
        while (i >= 0) {
            if (current_brightness <= (i > 0 ? this.mSuperPowerSavingBacklightValues[i - 1] : 0) || current_brightness > this.mSuperPowerSavingBacklightValues[i]) {
                i--;
            } else {
                if (i <= 0) {
                    this.mChangeUpLux = this.mAutoBrightnessUpLevels[0];
                } else if (i == MAX_LIGHT_LEVEL) {
                    this.mChangeUpLux = this.mAutoBrightnessUpLevels[i - 1];
                } else {
                    this.mChangeUpLux = this.mAutoBrightnessUpLevels[i];
                }
                for (i = 1; i <= MAX_LIGHT_LEVEL; i++) {
                    if (current_brightness == this.mSuperPowerSavingBacklightValues[i]) {
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

    public void brightnessBeenApplied(AutobrightInfo info) {
        log("brightnessBeenApplied vivo_calac_up_down_gm lux=" + info.mLightLux + " mBrightness=" + info.mBrightness);
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

    public AutobrightInfo getAutoBrightness(boolean waitFirst, boolean mProximityStatus, int lux, AutobrightInfo info) {
        AutobrightInfo abInfo = new AutobrightInfo();
        abInfo.copyFrom(info);
        int tempLux = lux;
        long t = SystemClock.uptimeMillis();
        Slog.e(TAG, "111111111111111111111111111111111111 ");
        if (waitFirst) {
            this.nCurrentBrightness = vivoCalaBrighntenss(lux);
            vivoCalacUpDownGm(lux, this.nCurrentBrightness);
            this.mAdjustBrightnessFlag = 0;
            Slog.e(TAG, "3333333333333 ");
        } else {
            Slog.e(TAG, "444444444444444444");
            if (lux >= this.mChangeUpLux) {
                if (this.mAdjustBrightnessFlag != 2) {
                    this.mAdjustBrightnessFlag = 2;
                    this.mAdjustBrightnessTimeStamp = SystemClock.uptimeMillis();
                }
                if (t - this.mAdjustBrightnessTimeStamp > 400) {
                    Slog.d(TAG, "vivo lux " + lux);
                    this.nCurrentBrightness = vivoCalaBrighntenss(lux);
                    this.mAdjustBrightnessFlag = 0;
                }
                log("AutoBakclight  nUpBrightness mAdjustBrightnessTimeStamp =" + this.mAdjustBrightnessTimeStamp + " t =" + t);
            } else if (lux > this.mChangeDownLux || (mProximityStatus ^ 1) == 0) {
                this.mAdjustBrightnessFlag = 0;
            } else {
                if (this.mAdjustBrightnessFlag != 1) {
                    this.mAdjustBrightnessFlag = 1;
                    this.mAdjustBrightnessTimeStamp = SystemClock.uptimeMillis();
                }
                if (t - this.mAdjustBrightnessTimeStamp > 5500) {
                    Slog.d(TAG, "vivo lux " + lux);
                    this.nCurrentBrightness = vivoCalaBrighntenss(lux);
                    this.mAdjustBrightnessFlag = 0;
                    Slog.d(TAG, "vivo   nCurrentBrightness " + this.nCurrentBrightness + " templux= " + lux + " lux=" + lux);
                }
                log("AutoBakclight  nDownBrightness mAdjustBrightnessTimeStamp =" + this.mAdjustBrightnessTimeStamp + " t =" + t);
            }
        }
        Slog.e(TAG, "2222222222222222222222 ");
        log("AutoBakclight lux=" + lux + " nCurrentBrightness=" + this.nCurrentBrightness + " mChangeUpLux=" + this.mChangeUpLux + " mChangeDownLux=" + this.mChangeDownLux + " info.mScreenLevel=" + info.mScreenLevel + " waitFirst=" + waitFirst + " nCurrentBrightness=" + this.nCurrentBrightness);
        if (AblConfig.isUseUnderDisplayLight() && !(abInfo.mChangeDownLux == this.mChangeDownLux && abInfo.mChangeUpLux == this.mChangeUpLux)) {
            abInfo.mRecitfiedLuxLock = abInfo.mUnderDispalyRecitfiedLux;
            abInfo.mDriverLuxLock = abInfo.mUnderDispalyDriverLux;
            abInfo.mChangeDownLux = this.mChangeDownLux;
            abInfo.mChangeUpLux = this.mChangeUpLux;
            abInfo.mUnderDisplayThreshChanged = true;
        }
        abInfo.mBrightness = this.nCurrentBrightness;
        abInfo.mLightLux = lux;
        return abInfo;
    }

    private int getIdByName(String className, String name) {
        int id = -1;
        try {
            Class[] classes = Class.forName("com.vivo.internal" + ".R").getClasses();
            Class desireClass = null;
            for (int i = 0; i < classes.length; i++) {
                if (classes[i].getName().split("\\$")[1].equals(className)) {
                    desireClass = classes[i];
                    break;
                }
            }
            if (desireClass != null) {
                id = desireClass.getField(name).getInt(desireClass);
            }
            Slog.d(TAG, "id = " + id);
            return id;
        } catch (Exception e) {
            Slog.e(TAG, "getIdByNameFailed");
            e.printStackTrace();
            return -1;
        }
    }
}
