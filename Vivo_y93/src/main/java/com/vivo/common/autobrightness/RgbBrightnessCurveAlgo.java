package com.vivo.common.autobrightness;

import android.content.Context;
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
import com.vivo.common.autobrightness.TimePeriod.PeriodType;
import com.vivo.common.fingerprinthook.WakeHookConfig;
import com.vivo.common.provider.Calendar.CalendarsColumns;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.common.provider.Weather;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;

public class RgbBrightnessCurveAlgo {
    private static final String AUTOBRIGHTNESS_COUNT = "autobrighness_count";
    private static final String AUTOBRIGHTNESS_KEY_PARAM = "autobrighness_key_param";
    private static final String AUTOBRIGHTNESS_PARAM = "autobrighness_param";
    private static final String AUTOBRIGHTNESS_RECORD = "autobrighness_record";
    public static final int KEY_POINT_SIZE = 10;
    private static final int MSG_INERTIA_SENSOR_DISABLE = 5;
    private static final int MSG_INERTIA_SENSOR_ENABLE = 4;
    private static final int MSG_RESTORE_MODIFY_PARAMTER = 3;
    private static final int MSG_USER_MODIFY_AUTOBRIGHT_MODE = 2;
    private static final int MSG_USER_MODIFY_BRIGHTNESS = 1;
    public static final int ORIGKEY_LINE = 2;
    public static final int ORIGKEY_ROW = 5;
    public static final int ORIGPARAM_LINE = 2;
    public static final int ORIGPARAM_ROW = 4;
    public static final int PARAMETER_BAR_LINE = 9;
    public static final int PARAMETER_LINE = 2;
    public static final int PARAMETER_ROW = 9;
    public static final int PARAMETER_UPDOWN_LINE = 2;
    public static final int PARAMETER_UPDOWN_ROW = 9;
    private static final int RECORD_LINE = 4;
    private static final int RECORD_ROW = 15;
    private static final int SIGNIFICANT_BIT = 4;
    private static final String TAG = "RgbBrightnessCurveAlgo";
    private static int TYPE_ANGLE_DIRECTION = 169;
    public static final double VAR = Math.pow(10.0d, 4.0d);
    private static int[][] mOrigKeyValue = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{5, 2}));
    private static double[][] mOrigParam = ((double[][]) Array.newInstance(Double.TYPE, new int[]{4, 2}));
    private static double[][] mUpDownParam = ((double[][]) Array.newInstance(Double.TYPE, new int[]{9, 2}));
    private static final String model = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, "unkown").toLowerCase();
    private boolean bIsAutoBakclightAdjust = true;
    private boolean bUserSettingBrightness = false;
    private boolean bUserSettingBrightnessBak;
    private boolean isIgnoreMotionStatus = false;
    private Handler mAdjustBarHandler;
    private int mAdjustBrightnessFlag = 0;
    private long mAdjustBrightnessTimeStamp = -1;
    private int mAmbientLux = 0;
    private boolean mAppBrightRatio = false;
    private ModifyArgument mArg = new ModifyArgument();
    private int mBacklightBrightness = -1;
    private int mBacklightMode = 0;
    private double mBrightnessDeta = 0.0d;
    private int[] mCameraBoundaries;
    private long mChangUpStartTimeStamp = -1;
    private int[] mChangeBar = new int[9];
    private int[] mChangeBarBak = new int[9];
    private int[] mChangeCneed = new int[9];
    private int[] mChangeCneedBak = new int[9];
    private int mChangeCount = 0;
    private boolean mChangeDownAlsFlag = false;
    private int mChangeDownBrightness;
    private boolean mChangeDownFlag = false;
    private int mChangeDownLux;
    private int mChangeDownPhoneStatus;
    private long mChangeDownTime = 0;
    private int[] mChangeLux = new int[9];
    private int[] mChangeLuxBak = new int[9];
    private long mChangeTime = 0;
    private int mChangeUpBrightness;
    private int mChangeUpLux;
    private long mChangeUpTime = 0;
    private Context mContext = null;
    private Handler mInertiaHandler;
    public double[][] mKeyParameter = ((double[][]) Array.newInstance(Double.TYPE, new int[]{9, 2}));
    public double[][] mKeyParameterBak = ((double[][]) Array.newInstance(Double.TYPE, new int[]{9, 2}));
    private int[] mKeyPoint = new int[10];
    private int[] mKeyPointValue = new int[10];
    private int mLastChangeDownGm = 0;
    private int mLastChangeUpGm = 0;
    private int[] mLimitBrightness = new int[5];
    private ScreenBrightnessModeRestore mModeRestore = null;
    private int mMontionStatus = 0;
    private boolean mOffVideoGameFlag = false;
    private ModifyArgumentParser mParser = new ModifyArgumentParser();
    private int mPhoneStatus;
    private int[] mPhoneStatusChangeDownLux = new int[8];
    private SensorEventListener mPhoneStatusListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            int temp = (int) event.values[0];
            if (RgbBrightnessCurveAlgo.this.mPrivPhoneStatus != temp) {
                RgbBrightnessCurveAlgo.this.mArg.mPhoneStatusCount++;
                if (RgbBrightnessCurveAlgo.this.mArg.mPhoneStatusCount > 10000000) {
                    RgbBrightnessCurveAlgo.this.mArg.mPhoneStatusCount = 0;
                }
                RgbBrightnessCurveAlgo.this.mRgbCureAlgoData.pushPhoneStatus(temp);
                RgbBrightnessCurveAlgo.this.mPrivPhoneStatus = temp;
                if (RgbBrightnessCurveAlgo.this.mMontionStatus == 0 && (RgbBrightnessCurveAlgo.this.mPriveVideoGameFlag ^ 1) != 0 && RgbBrightnessCurveAlgo.this.mPrivPhoneStatus > 0 && RgbBrightnessCurveAlgo.this.mPrivPhoneStatus < 8) {
                    RgbBrightnessCurveAlgo.this.mChangeDownLux = RgbBrightnessCurveAlgo.this.mPhoneStatusChangeDownLux[RgbBrightnessCurveAlgo.this.mPrivPhoneStatus - 1];
                }
                Slog.d(RgbBrightnessCurveAlgo.TAG, "mPS =" + RgbBrightnessCurveAlgo.this.mPrivPhoneStatus + "dlux = " + RgbBrightnessCurveAlgo.this.mChangeDownLux);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private Sensor mPhoneStatusSensor;
    private boolean mPowerAssistantMode = false;
    private int mPrivBrightness = -1;
    private int mPrivPhoneStatus = -1;
    private int mPriveChangeCount = 0;
    private int mPriveChangeDownLux;
    private boolean mPriveChangeFlag = false;
    private int mPriveMontionStatus = 0;
    private boolean mPriveVideoGameFlag = false;
    private int mRecordCount = 0;
    private int mRecordCountBak = 0;
    private int[][] mRecordNeed = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{15, 4}));
    private int[][] mRecordNeedBak = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{15, 4}));
    private RgbCurveAlgoDataStruct mRgbCureAlgoData = null;
    private int mSecondSettingBrightness = -1;
    private SensorManager mSensorManager;
    private boolean mStartCameraFlag;
    private int mStepCount = -1;
    private SensorEventListener mStepCountListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            int step_value = (int) event.values[0];
            RgbBrightnessCurveAlgo.this.mStepCount = step_value;
            RgbBrightnessCurveAlgo.this.mRgbCureAlgoData.pushStepCountData(RgbBrightnessCurveAlgo.this.mStepCount);
            Slog.e(RgbBrightnessCurveAlgo.TAG, "RGB mStepCountListener step_count = " + step_value);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private Sensor mStepCountSensor;
    private TimePeriod mTimePeriod = null;
    private long mUserAdjustTime = 0;
    private int[] mkeyPointBak = new int[10];
    private int nCurrentBrightness = 0;

    private class AdjustBarHandler extends Handler {
        public AdjustBarHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                Slog.e(RgbBrightnessCurveAlgo.TAG, "handleMessage msg is NULL");
                return;
            }
            switch (msg.what) {
                case 1:
                    RgbBrightnessCurveAlgo.this.modifyBrightness();
                    break;
                case 2:
                    RgbBrightnessCurveAlgo.this.modifyBrightnessMode();
                    break;
                case 3:
                    RgbBrightnessCurveAlgo.this.restore_param();
                    break;
                case 4:
                    RgbBrightnessCurveAlgo.this.setInertiaSensorEnabledInner(true);
                    break;
                case 5:
                    RgbBrightnessCurveAlgo.this.setInertiaSensorEnabledInner(false);
                    break;
            }
        }
    }

    public RgbBrightnessCurveAlgo(Context context, SensorManager sensorManager, Looper looper) {
        this.mContext = context;
        this.mSensorManager = sensorManager;
        this.mInertiaHandler = new Handler(looper);
        TYPE_ANGLE_DIRECTION = AblConfig.getAngleDirectonType();
        this.mPhoneStatusSensor = this.mSensorManager.getDefaultSensor(TYPE_ANGLE_DIRECTION);
        this.mStepCountSensor = this.mSensorManager.getDefaultSensor(19);
        String jsonStr = System.getStringForUser(context.getContentResolver(), AUTOBRIGHTNESS_PARAM, -2);
        String recordString = System.getStringForUser(context.getContentResolver(), AUTOBRIGHTNESS_RECORD, -2);
        String countString = System.getStringForUser(context.getContentResolver(), AUTOBRIGHTNESS_COUNT, -2);
        String keyString = System.getStringForUser(context.getContentResolver(), AUTOBRIGHTNESS_KEY_PARAM, -2);
        AutobrightOrigParam.getAutoOrigParam(mOrigParam, mOrigKeyValue, mUpDownParam);
        if (!(jsonStr == null || (Events.DEFAULT_SORT_ORDER.equals(jsonStr) ^ 1) == 0)) {
            this.mParser.stringToArgument(jsonStr, this.mArg);
            if (recordString != null) {
                setStringToRecordInt(recordString);
            } else {
                setParamToNegative(this.mRecordNeed);
                this.mRecordCount = 0;
            }
            if (keyString != null) {
                setStringToKeyParamInt(keyString);
                int n = this.mKeyPoint.length;
                if (this.mKeyParameter[0][0] >= 0.0d) {
                    for (int i = 0; i < n; i++) {
                        this.mKeyPointValue[i] = vivoCalacBrighntenssFinnal(this.mKeyPoint[i]);
                    }
                }
            }
            this.bUserSettingBrightness = this.mArg.bUserSettingBrightness;
        }
        if (countString != null) {
            setStringToCountInt(countString);
        } else {
            this.mArg.mCameraOpenCount = 0;
            this.mArg.mPhoneStatusCount = 0;
        }
        this.mAdjustBarHandler = new AdjustBarHandler(looper);
        this.mTimePeriod = TimePeriod.getInstance(context, looper);
        this.mRgbCureAlgoData = new RgbCurveAlgoDataStruct(looper);
        this.isIgnoreMotionStatus = isUsingMotionStatus();
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

    private int calcBrightness(int lux, boolean waitFirst) {
        int brightness;
        if (this.bUserSettingBrightness) {
            brightness = vivoCalacBrighntenssFinnal(lux);
        } else {
            brightness = vivoCalacOrigBrighntenss(lux);
            if (this.mTimePeriod.getTimePeriod() != PeriodType.DAY_TIME || lux >= 20 || (AblConfig.isUse2048GrayScaleBacklight() ^ 1) == 0) {
                log("calcBrightness lux=" + lux + " bright=" + brightness + " period=" + this.mTimePeriod.timePeriodToString());
            } else {
                int temp = brightness;
                brightness++;
                if (brightness < 4) {
                    brightness = 4;
                }
                log("calcBrightness brightness=" + temp + " force as " + brightness + " cause daytime and lux=" + lux);
            }
        }
        if (this.mTimePeriod.getTimePeriod() == PeriodType.DAY_TIME && brightness == 2) {
            brightness++;
        }
        if (brightness <= 1) {
            brightness = 2;
        } else if (brightness > 255) {
            brightness = 255;
        }
        log("calcBrightness brightness=" + brightness);
        return brightness;
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

    public JSONObject saveModifyRecord(JSONObject obj) {
        JSONObject arg = ModifyArgumentParser.argumentToJsonObject(this.mArg);
        if (arg == null) {
            return obj;
        }
        try {
            obj.put(UserModifyRecorder.KEY_RECORD_ARGUMENT, arg);
            return obj;
        } catch (JSONException e) {
            Slog.e(TAG, "saveModifyRecord put ARGUMENT:", e);
            return null;
        }
    }

    private boolean isUsingMotionStatus() {
        if ("qcom".equals(SystemProperties.get("ro.vivo.product.solution", "unkown").toLowerCase())) {
            return false;
        }
        return true;
    }

    public AutobrightInfo getAutoBrightness(boolean waitFirst, boolean mProximityStatus, int lux, AutobrightInfo info, boolean videogameflag) {
        int brightness;
        int[] tempAngle = new int[3];
        boolean adjust_flag = false;
        AutobrightInfo abInfo = new AutobrightInfo();
        abInfo.copyFrom(info);
        int[] alsChange = new int[3];
        boolean mLightLuxApplied = false;
        int[] alsCondition = new int[6];
        int tempLux = lux;
        int tempVideoLux = lux;
        int tempOftenLux = lux;
        int changeTime = 0;
        int temp = lux;
        long t = SystemClock.uptimeMillis();
        this.mChangeDownFlag = false;
        if (lux > 5000) {
            lux = 5000;
        }
        this.mRgbCureAlgoData.pushLuxData(lux);
        this.mAmbientLux = lux;
        this.mPriveMontionStatus = this.mMontionStatus;
        if (this.isIgnoreMotionStatus) {
            this.mMontionStatus = 0;
        } else {
            this.mMontionStatus = this.mRgbCureAlgoData.getPhoneMontionStatus();
        }
        int alsCount = this.mRgbCureAlgoData.popAlsValueChangeCount();
        if (this.mMontionStatus == 1 || alsCount >= 2 || this.mChangeCount > 0 || videogameflag) {
            this.mStartCameraFlag = false;
        } else {
            this.mStartCameraFlag = true;
        }
        if (t - this.mChangeTime > 360000) {
            this.mChangeCount = 0;
        }
        if (!this.mPriveVideoGameFlag && videogameflag) {
            this.mChangeDownLux = vivoCalacSidewardsChangeDownTheashAls(info.mLightLux, videogameflag);
            Slog.d(TAG, "vivo getAutoBrightness dlux " + this.mChangeDownLux + " vg=" + videogameflag);
            this.mOffVideoGameFlag = false;
        } else if (this.mMontionStatus == 1 && this.mPriveMontionStatus != this.mMontionStatus) {
            this.mChangeDownLux = vivoCalacMotionChangeDownTheashAls(info.mLightLux);
            Slog.d(TAG, "vivo getAutoBrightness dlux " + this.mChangeDownLux + " motion=" + this.mMontionStatus);
        } else if ((this.mPriveMontionStatus != this.mMontionStatus && this.mMontionStatus == 0) || (this.mPriveVideoGameFlag && (videogameflag ^ 1) != 0)) {
            int minlux;
            int maxlux;
            if (this.mPhoneStatusChangeDownLux[0] >= this.mPhoneStatusChangeDownLux[3]) {
                minlux = this.mPhoneStatusChangeDownLux[3];
                maxlux = this.mPhoneStatusChangeDownLux[0];
            } else {
                minlux = this.mPhoneStatusChangeDownLux[0];
                maxlux = this.mPhoneStatusChangeDownLux[3];
            }
            if (this.mTimePeriod.getTimePeriod() == PeriodType.DAY_TIME) {
                this.mChangeDownLux = minlux;
            } else {
                this.mChangeDownLux = maxlux;
            }
            this.mChangeCount = 0;
            this.mChangeUpTime = t;
            this.mChangeDownTime = t;
            this.mChangeTime = t;
            this.mPriveChangeFlag = false;
            this.mOffVideoGameFlag = true;
            Slog.d(TAG, "vivo getAutoBrightness dlux " + this.mChangeDownLux + " motion=" + this.mMontionStatus);
        }
        int delayTime;
        if (waitFirst) {
            brightness = calcBrightness(lux, waitFirst);
            this.mStartCameraFlag = true;
            mLightLuxApplied = true;
            this.nCurrentBrightness = brightness;
            vivoCalacUpDownGm(lux, this.nCurrentBrightness);
            this.mAdjustBrightnessFlag = 0;
            this.mChangeCount = 0;
            this.mChangeUpTime = t;
            this.mChangeDownTime = t;
            this.mChangeTime = t;
            this.mPriveChangeFlag = true;
            this.mOffVideoGameFlag = false;
        } else if (lux >= this.mChangeUpLux) {
            if (this.mAdjustBrightnessFlag != 2) {
                this.mAdjustBrightnessFlag = 2;
                this.mAdjustBrightnessTimeStamp = SystemClock.uptimeMillis();
            }
            delayTime = vivoCalacMotionChangTime(this.nCurrentBrightness, calcBrightness(lux, waitFirst), videogameflag, this.mMontionStatus, true);
            if (AblConfig.isUse2048GrayScaleBacklight() && (AblConfig.isUseOLEDLcm() ^ 1) != 0 && lux <= 2) {
                adjust_flag = true;
            }
            if (t - this.mAdjustBrightnessTimeStamp > ((long) delayTime) || adjust_flag) {
                bbklog("vivo lux " + lux + "ch ut  " + delayTime);
                if (this.mPriveChangeFlag) {
                    if (t - this.mChangeDownTime > 300000 || Math.abs(this.mPrivBrightness - this.nCurrentBrightness) >= 30) {
                        this.mChangeCount = 0;
                    } else {
                        this.mChangeCount++;
                    }
                }
                if (this.mChangeCount >= 1 && lux <= 500 && lux >= 100) {
                    lux = vivoCalacChangeUpAls(this.mRgbCureAlgoData.evaluateChangeDownAls(), lux);
                }
                this.mChangeUpTime = t;
                this.mChangeTime = t;
                this.mPriveChangeFlag = false;
                brightness = calcBrightness(lux, waitFirst);
                mLightLuxApplied = true;
                this.nCurrentBrightness = brightness;
                this.mChangUpStartTimeStamp = t;
                this.mAdjustBrightnessFlag = 0;
                abInfo.mPrivBrightness = this.mPrivBrightness;
                log("vivo getAutoBrightness  Count " + this.mChangeCount + " brightness:" + brightness);
            }
            log("AutoBakclight  nUpBrightness mAdjustBrightnessTimeStamp =" + this.mAdjustBrightnessTimeStamp + " t =" + t);
        } else if (lux > this.mChangeDownLux || (mProximityStatus ^ 1) == 0) {
            this.mAdjustBrightnessFlag = 0;
        } else {
            brightness = calcBrightness(lux, waitFirst);
            if (this.nCurrentBrightness < 6) {
                changeTime = 240000;
            } else if (this.nCurrentBrightness < 10) {
                changeTime = StateInfo.STATE_FINGERPRINT_GOTO_SLEEP;
            }
            if (this.mOffVideoGameFlag && this.nCurrentBrightness <= 4) {
                changeTime = Weather.WEATHERVERSION_ROM_4_5;
            } else if (this.mOffVideoGameFlag && this.nCurrentBrightness <= 8) {
                changeTime = Weather.WEATHERVERSION_ROM_3_0;
            }
            if (t - this.mChangeUpTime > ((long) changeTime) || (this.mChangeCount == 0 && (this.mOffVideoGameFlag ^ 1) != 0)) {
                if (this.mAdjustBrightnessFlag != 1) {
                    this.mAdjustBrightnessFlag = 1;
                    this.mAdjustBrightnessTimeStamp = SystemClock.uptimeMillis();
                }
                delayTime = vivoCalacMotionChangTime(this.nCurrentBrightness, calcBrightness(lux, waitFirst), videogameflag, this.mMontionStatus, false);
                if (model.equals("pd1616") && this.nCurrentBrightness <= 60) {
                    log("pd1616 prelong delayTime from " + delayTime + " to 5500 when nCurrentBrightness less than 60");
                    delayTime = 5500;
                }
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
                    Slog.d(TAG, "ch dt " + delayTime + " vivo lux " + lux + "vivo changeTime " + changeTime + "count " + this.mChangeCount);
                    this.mChangeDownPhoneStatus = this.mPrivPhoneStatus;
                    alsCondition = this.mRgbCureAlgoData.evaluateChangeDownAls();
                    this.mOffVideoGameFlag = false;
                    if (videogameflag || this.mMontionStatus == 1) {
                        if (this.mMontionStatus == 1) {
                            tempLux = vivoCalacMotionChangeDownTheashAls(info.mLightLux);
                        } else {
                            tempLux = 5001;
                        }
                        if (videogameflag) {
                            tempVideoLux = vivoCalacSidewardsChangeDownTheashAls(info.mLightLux, videogameflag);
                        } else {
                            tempVideoLux = 5001;
                        }
                        if (tempLux > tempVideoLux) {
                            tempLux = tempVideoLux;
                        }
                        if (tempLux >= lux) {
                            if (this.mMontionStatus == 1) {
                                tempLux = vivoCalacMotionChangeDownAls(lux);
                            } else {
                                tempLux = 0;
                            }
                            if (videogameflag) {
                                tempVideoLux = vivoCalacVideoGameChangeDownAls(lux, alsCondition);
                            } else {
                                tempVideoLux = 0;
                            }
                            if (tempLux < tempVideoLux) {
                                tempLux = tempVideoLux;
                            }
                            if (this.mChangeCount > 0) {
                                tempOftenLux = vivoCalacOftenChangeDownAls(lux, alsCondition, info.mLightLux, true);
                            } else {
                                tempOftenLux = 0;
                            }
                            if (tempLux < tempOftenLux) {
                                tempLux = tempOftenLux;
                            }
                            mLightLuxApplied = true;
                            this.nCurrentBrightness = calcBrightness(tempLux, waitFirst);
                            lux = tempLux;
                            this.mChangeDownFlag = true;
                            abInfo.mPrivBrightness = this.mPrivBrightness;
                        } else {
                            this.mChangeDownLux = tempLux;
                        }
                    } else if (alsCondition[2] >= 1) {
                        if (this.mPriveChangeDownLux > 10) {
                            temp = (int) (((double) this.mPriveChangeDownLux) * 0.5d);
                        } else if (this.mPriveChangeDownLux >= 6) {
                            temp = 4;
                        } else {
                            temp = this.mPriveChangeDownLux - 2;
                        }
                        if (temp < 0) {
                            temp = 0;
                        }
                        if (lux <= temp) {
                            tempLux = vivoChangeDownAls(lux, this.mChangeDownPhoneStatus, alsCondition);
                            if (this.mChangeCount > 0) {
                                tempOftenLux = vivoCalacOftenChangeDownAls(lux, alsCondition, info.mLightLux, false);
                            } else {
                                tempOftenLux = 0;
                            }
                            if (tempOftenLux > tempLux) {
                                tempLux = tempOftenLux;
                            }
                            mLightLuxApplied = true;
                            this.nCurrentBrightness = calcBrightness(tempLux, waitFirst);
                            lux = tempLux;
                            this.mChangeDownFlag = true;
                            abInfo.mPrivBrightness = this.mPrivBrightness;
                        } else {
                            this.mChangeDownLux = temp;
                            if (this.mChangeDownPhoneStatus == 1 || this.mChangeDownPhoneStatus == 2) {
                                this.mPhoneStatusChangeDownLux[0] = this.mChangeDownLux;
                                this.mPhoneStatusChangeDownLux[1] = this.mChangeDownLux;
                                this.mPhoneStatusChangeDownLux[2] = this.mPriveChangeDownLux;
                                this.mPhoneStatusChangeDownLux[3] = this.mPriveChangeDownLux;
                            } else if (this.mChangeDownPhoneStatus == 3 || this.mChangeDownPhoneStatus == 4) {
                                this.mPhoneStatusChangeDownLux[0] = this.mPriveChangeDownLux;
                                this.mPhoneStatusChangeDownLux[1] = this.mPriveChangeDownLux;
                                this.mPhoneStatusChangeDownLux[2] = this.mChangeDownLux;
                                this.mPhoneStatusChangeDownLux[3] = this.mChangeDownLux;
                            }
                        }
                    } else if (this.mPriveChangeDownLux != this.mChangeDownLux || alsCondition[1] >= 2) {
                        tempLux = vivoChangeDownAls(lux, this.mChangeDownPhoneStatus, alsCondition);
                        if (this.mChangeCount > 0) {
                            tempOftenLux = vivoCalacOftenChangeDownAls(lux, alsCondition, info.mLightLux, false);
                        } else {
                            tempOftenLux = 0;
                        }
                        if (tempOftenLux > tempLux) {
                            tempLux = tempOftenLux;
                        }
                        mLightLuxApplied = true;
                        this.nCurrentBrightness = calcBrightness(tempLux, waitFirst);
                        lux = tempLux;
                        this.mChangeDownFlag = true;
                        abInfo.mPrivBrightness = this.mPrivBrightness;
                    } else if (this.mChangeCount > 0) {
                        tempLux = vivoCalacOftenChangeDownAls(lux, alsCondition, info.mLightLux, false);
                        mLightLuxApplied = true;
                        this.nCurrentBrightness = calcBrightness(tempLux, waitFirst);
                        lux = tempLux;
                        this.mChangeDownFlag = true;
                        abInfo.mPrivBrightness = this.mPrivBrightness;
                    } else {
                        mLightLuxApplied = true;
                        this.nCurrentBrightness = calcBrightness(lux, waitFirst);
                        this.mChangeDownFlag = true;
                        abInfo.mPrivBrightness = this.mPrivBrightness;
                    }
                    this.mAdjustBrightnessFlag = 0;
                    log("vivo getAutoBrightness  Count " + this.mChangeCount);
                    log("vivo status  vg " + videogameflag + " cd " + this.mChangeDownFlag);
                    log("vivo nCurrentBrightness " + this.nCurrentBrightness + " templux= " + tempLux + " lux=" + lux);
                    log("vivo pstatus " + this.mChangeDownPhoneStatus + " dlux " + this.mChangeDownLux + " dlux0 = " + this.mPhoneStatusChangeDownLux[0] + " dlux1=" + this.mPhoneStatusChangeDownLux[2]);
                }
                abInfo.mScreenLevel = 1;
                log("AutoBakclight  nDownBrightness mAdjustBrightnessTimeStamp =" + this.mAdjustBrightnessTimeStamp + " t =" + t + " delayTime = " + delayTime);
            }
        }
        if (this.mMontionStatus == 1 && this.nCurrentBrightness <= 6 && Math.abs(t - this.mUserAdjustTime) > 3600000) {
            tempLux = vivoCalacMotionChangeDownAls(lux);
            brightness = calcBrightness(tempLux, waitFirst);
            if (this.nCurrentBrightness < brightness) {
                mLightLuxApplied = true;
                this.nCurrentBrightness = brightness;
                lux = tempLux;
                abInfo.mPrivBrightness = this.mPrivBrightness;
            }
            Slog.d(TAG, "vivo vgbrightness  nCurrentBrightness " + this.nCurrentBrightness);
        }
        if (this.mChangeDownFlag && ((this.nCurrentBrightness >= 7 && Math.abs(this.nCurrentBrightness - this.mPrivBrightness) < 5) || (this.nCurrentBrightness < 7 && Math.abs(this.nCurrentBrightness - this.mPrivBrightness) < 1))) {
            mLightLuxApplied = false;
            this.nCurrentBrightness = this.mPrivBrightness;
            this.mChangeDownFlag = false;
            if (t - this.mChangeUpTime > 300000) {
                this.mChangeCount = 0;
            } else {
                this.mChangeCount++;
            }
            this.mChangeUpTime = t;
            this.mChangeTime = t;
        }
        if (this.mChangeDownFlag) {
            if (!this.mPriveChangeFlag) {
                if (t - this.mChangeUpTime > 300000 || Math.abs(this.mPrivBrightness - this.nCurrentBrightness) >= 30) {
                    this.mChangeCount = 0;
                } else {
                    this.mChangeCount++;
                }
            }
            this.nCurrentBrightness = Math.min(this.nCurrentBrightness, this.mPrivBrightness);
            this.mChangeDownTime = t;
            this.mChangeTime = t;
            this.mPriveChangeFlag = true;
        }
        if (!mLightLuxApplied) {
            log("LightLux not applied lux " + lux + " to return " + abInfo.mLightLux);
            lux = abInfo.mLightLux;
        }
        bbklog("AutoBakclight light =" + lux + " Current=" + this.nCurrentBrightness + " " + this.mChangeUpLux + " " + this.mChangeDownLux + " bUserSettingBrightness=" + this.bUserSettingBrightness + " waitFirst=" + waitFirst + " mChangeCount =" + this.mChangeCount + " mOffVideoGameFlag =" + this.mOffVideoGameFlag + " mLightLuxApplied=" + mLightLuxApplied + " mMontionStatus=" + this.mMontionStatus);
        if (AblConfig.isUseUnderDisplayLight() && !(abInfo.mChangeDownLux == this.mChangeDownLux && abInfo.mChangeUpLux == this.mChangeUpLux)) {
            abInfo.mRecitfiedLuxLock = abInfo.mUnderDispalyRecitfiedLux;
            abInfo.mDriverLuxLock = abInfo.mUnderDispalyDriverLux;
            abInfo.mChangeDownLux = this.mChangeDownLux;
            abInfo.mChangeUpLux = this.mChangeUpLux;
            abInfo.mUnderDisplayThreshChanged = true;
        }
        this.mPriveVideoGameFlag = videogameflag;
        abInfo.mBrightness = this.nCurrentBrightness;
        abInfo.mLightLux = lux;
        this.mPrivBrightness = this.nCurrentBrightness;
        abInfo.mStepCount = this.mStepCount;
        abInfo.mPhoneStatus = this.mPrivPhoneStatus;
        return abInfo;
    }

    private int vivoCalacChangeUpAls(int[] alsCondition, int lux) {
        int tempLux = lux;
        if (alsCondition[1] <= 5) {
            tempLux = alsCondition[0] + ((lux - alsCondition[0]) / 2);
        } else {
            tempLux = alsCondition[0] + (((lux - alsCondition[0]) * 3) / 4);
        }
        if (tempLux > lux) {
            tempLux = lux;
        }
        Slog.d(TAG, "1Als  templux " + tempLux + " lux = " + lux);
        return tempLux;
    }

    private int vivoCalacMotionChangTime(int currentbrightness, int targetbrightness, boolean videogameflag, int motionstatus, boolean updowm) {
        if (Math.abs(targetbrightness - currentbrightness) >= 30) {
            if (updowm) {
                return 400;
            }
            if (videogameflag) {
                return Weather.WEATHERVERSION_ROM_3_0;
            }
            return 1200;
        } else if (Math.abs(targetbrightness - currentbrightness) >= 15) {
            if (updowm) {
                return 400;
            }
            if (AblConfig.isUse2048GrayScaleBacklight()) {
                return 1200;
            }
            return Weather.WEATHERVERSION_ROM_2_5_1;
        } else if (Math.abs(targetbrightness - currentbrightness) > 10) {
            if (updowm) {
                return 400;
            }
            if (!AblConfig.isUse2048GrayScaleBacklight() || (AblConfig.isUseOLEDLcm() ^ 1) == 0) {
                return Weather.WEATHERVERSION_ROM_3_0;
            }
            return 1200;
        } else if (Math.abs(targetbrightness - currentbrightness) > 5) {
            if (updowm) {
                return 400;
            }
            if (!AblConfig.isUse2048GrayScaleBacklight() || (AblConfig.isUseOLEDLcm() ^ 1) == 0) {
                return 5500;
            }
            return 1200;
        } else if (updowm) {
            if (motionstatus == 1) {
                return 1200;
            }
            return 400;
        } else if (!AblConfig.isUse2048GrayScaleBacklight() || (AblConfig.isUseOLEDLcm() ^ 1) == 0) {
            return 5500;
        } else {
            return 1200;
        }
    }

    private int vivoChangeDownAls(int lux, int phoneStatus, int[] alsCondition) {
        int templux = lux;
        if (this.mTimePeriod.getTimePeriod() == PeriodType.DAY_TIME) {
            if (lux <= 8) {
                templux = lux + 2;
            } else if (lux <= 12) {
                templux = 12;
            }
        } else if (lux == 0 && alsCondition[1] <= 1) {
            templux = lux;
        } else if (lux <= 8) {
            templux = lux + 2;
        } else if (lux <= 12) {
            templux = 12;
        }
        if (phoneStatus == 5 || phoneStatus == 6) {
            if (this.mTimePeriod.getTimePeriod() == PeriodType.DAY_TIME) {
                if (lux <= 5) {
                    templux = lux + 5;
                } else if (lux <= 10) {
                    templux = 11;
                }
            } else if (lux == 0 && alsCondition[1] <= 1) {
                templux = lux;
            } else if (lux == 0) {
                templux = lux + 2;
            } else if (lux <= 5) {
                templux = lux + 3;
            } else if (lux <= 8) {
                templux = 9;
            }
        }
        Slog.d(TAG, "2Als " + templux);
        return templux;
    }

    private int vivoCalacMotionChangeDownAls(int lux) {
        int templux = lux;
        if (this.mTimePeriod.getTimePeriod() == PeriodType.DAY_TIME) {
            if (lux == 0) {
                templux = 3;
            } else if (lux <= 30) {
                templux = lux + 3;
            }
        } else if (lux == 0) {
            templux = 7;
        } else if (lux <= 5) {
            templux = 8;
        } else if (lux <= 8) {
            templux = 10;
        } else if (lux <= 30) {
            templux = lux + 3;
        }
        if (templux < lux) {
            templux = lux;
        }
        Slog.d(TAG, "4Als  templux " + templux);
        return templux;
    }

    private int vivoCalacMotionChangeDownTheashAls(int lux) {
        int templux = lux;
        if (lux > Weather.WEATHERVERSION_ROM_2_5_1) {
            templux = Weather.WEATHERVERSION_ROM_2_0;
        } else if (lux > 1500) {
            templux = CalendarsColumns.RESPOND_ACCESS;
        } else if (lux > Weather.WEATHERVERSION_ROM_2_0) {
            templux = 160;
        } else if (lux > 800) {
            templux = 30;
        } else if (lux >= CalendarsColumns.RESPOND_ACCESS) {
            templux = 14;
        } else if (lux >= 30) {
            templux = 5;
        } else if (lux >= 12) {
            templux = 0;
        } else {
            templux = -1;
        }
        Slog.d(TAG, "5Als  templux " + templux);
        return templux;
    }

    private int vivoCalacSidewardsChangeDownTheashAls(int lux, boolean videogameflag) {
        int tempLux;
        if (lux >= Weather.WEATHERVERSION_ROM_2_5_1) {
            tempLux = Weather.WEATHERVERSION_ROM_2_0;
        } else if (lux >= Weather.WEATHERVERSION_ROM_2_0) {
            tempLux = 100;
        } else if (lux >= CalendarsColumns.RESPOND_ACCESS) {
            tempLux = 15;
        } else if (lux >= 20) {
            tempLux = 5;
        } else if (lux >= 13) {
            tempLux = 0;
        } else {
            tempLux = -1;
        }
        if (this.mTimePeriod.getTimePeriod() == PeriodType.NIGHT_TIME && (videogameflag ^ 1) != 0 && lux >= 5 && lux < 13) {
            tempLux = 0;
        }
        Slog.d(TAG, "6Als  templux " + tempLux);
        return tempLux;
    }

    private int vivoCalacVideoGameChangeDownAls(int lux, int[] alsCondition) {
        int templux = lux;
        if (alsCondition[1] >= 2) {
            if (lux <= 30) {
                templux = lux + 5;
            } else if (lux <= 100) {
                templux = lux + AblConfig.BRIGHTNESS_MAP_HIGH;
            } else {
                templux = lux + CalendarsColumns.RESPOND_ACCESS;
            }
        }
        if (this.mTimePeriod.getTimePeriod() == PeriodType.DAY_TIME) {
            if (lux <= 10) {
                templux = lux + 5;
            } else if (lux <= 15) {
                templux = 15;
            }
        } else if (this.mTimePeriod.getTimePeriod() == PeriodType.NIGHT_TIME) {
            if (lux <= 5) {
                templux = lux + 3;
            } else if (lux <= 8) {
                templux = 9;
            }
        } else if (this.mTimePeriod.getTimePeriod() == PeriodType.DARK_NIGHT_TIME) {
            if (lux <= 5) {
                templux = lux + 2;
            } else if (lux <= 8) {
                templux = 8;
            }
        } else if (lux <= 5) {
            templux = lux + 3;
        } else if (lux <= 10) {
            templux = 10;
        }
        if (lux > templux) {
            templux = lux;
        }
        Slog.d(TAG, "7Als " + templux);
        return templux;
    }

    private int vivoCalacOftenChangeDownAls(int lux, int[] alsCondition, int currentAls, boolean vgmFlag) {
        int templux = lux;
        int tempchangecount = this.mChangeCount;
        if (alsCondition[0] > 15 || currentAls > 16) {
            if (lux < CalendarsColumns.EDITOR_ACCESS) {
                if (this.mChangeCount >= 10) {
                    tempchangecount = 10;
                }
            } else if (this.mChangeCount >= 6) {
                tempchangecount = 6;
            }
            templux = ((((tempchangecount * 10) / 2) + 100) * lux) / 100;
        } else if (this.mTimePeriod.getTimePeriod() == PeriodType.DAY_TIME) {
            if (lux == 0) {
                templux = (this.mChangeCount / 2) + 2;
                if (templux >= 5) {
                    templux = 5;
                }
            } else if (lux <= 5) {
                templux = lux + 3;
            } else if (lux <= 10) {
                templux = 13;
            } else if (lux <= 15) {
                templux = 15;
            } else {
                templux = lux;
            }
        } else if (this.mTimePeriod.getTimePeriod() == PeriodType.NIGHT_TIME) {
            if (lux == 0) {
                templux = (this.mChangeCount / 2) + 2;
                if (templux >= 2) {
                    templux = 2;
                }
            } else if (lux == 0) {
                templux = lux;
            } else if (lux <= 6) {
                templux = lux + 2;
            } else {
                templux = lux;
            }
        } else if (this.mTimePeriod.getTimePeriod() == PeriodType.DARK_NIGHT_TIME) {
            templux = lux;
        } else if (lux == 0) {
            templux = (this.mChangeCount / 2) + 2;
            if (templux >= 5) {
                templux = 5;
            }
        } else if (lux <= 5) {
            templux = lux + 3;
        } else if (lux <= 10) {
            templux = 13;
        } else if (lux <= 15) {
            templux = 15;
        } else {
            templux = lux;
        }
        if (templux >= alsCondition[0] && lux >= 13) {
            templux = alsCondition[0];
        }
        if (templux < lux) {
            templux = lux;
        }
        Slog.d(TAG, "8Als  templux " + templux);
        return templux;
    }

    public void setProximityPositiveToNegative(boolean to) {
        if (to) {
            this.mAdjustBrightnessFlag = 0;
        }
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

    private void vivoPrintAdjustBarLog(int current_brightness) {
        if (AblConfig.isDebug() || AblConfig.isBbkLog()) {
            int i;
            int n = this.mKeyPoint.length;
            for (i = 0; i < n; i++) {
                Slog.e(TAG, "vivo_adjust_bar mKeyPoint= " + this.mKeyPoint[i] + " mKeyPointValue= " + this.mKeyPointValue[i] + " i= " + i);
            }
            Slog.e(TAG, "vivo_adjust_bar mrecord_need_count= " + this.mRecordCount);
            n = this.mRecordNeed.length;
            int m = this.mRecordNeed[0].length;
            for (i = 0; i < n; i++) {
                if (this.mRecordNeed[i][0] != -1) {
                    Slog.e(TAG, "vivo_adjust_bar mRecordNeed= " + this.mRecordNeed[i][0] + " " + this.mRecordNeed[i][1] + " " + this.mRecordNeed[i][2] + " " + this.mRecordNeed[i][3] + " i= " + i);
                }
            }
            n = this.mKeyPoint.length;
            for (i = 0; i < n - 1; i++) {
                Slog.e(TAG, "vivo_adjust_bar mKeyParameter[i][0]= " + this.mKeyParameter[i][0] + " mKeyParameter[i][1]= " + this.mKeyParameter[i][1] + " i=" + i);
            }
            n = this.mChangeBar.length;
            for (i = 0; i < n; i++) {
                Slog.e(TAG, "vivo_adjust_bar mChangeLux[i]= " + this.mChangeLux[i] + " mChangeBar[i]" + this.mChangeBar[i] + " mChangeCneed[i]" + this.mChangeCneed[i] + " i=" + i);
            }
            Slog.e(TAG, "vivo_adjust_bar mChangeDownLux " + this.mChangeDownLux + " mChangeUpLux" + this.mChangeUpLux);
            Slog.e(TAG, "vivo_adjust_bar mChangeDownBrightness " + this.mChangeDownBrightness + " current_brightness" + current_brightness + " mChangeUpBrightness " + this.mChangeUpBrightness);
        }
    }

    private int adjustFunc(int x, int deta_x, int x_gm, int change_cneed) {
        int up_adjust_eight;
        int up_adjust_night;
        int down_adjust_night;
        double up_c;
        double output_args;
        int[][] adjust_als_threshold = new int[][]{new int[]{0, 0, 30, 65}, new int[]{1, 5, 35, 70}, new int[]{1, 20, 40, 100}, new int[]{30, 115, 400, 500}, new int[]{65, CalendarsColumns.RESPOND_ACCESS, 560, 660}, new int[]{AblConfig.BRIGHTNESS_MAP_HIGH, 500, 900, 1100}, new int[]{CalendarsColumns.RESPOND_ACCESS, 800, 1400, 1600}, new int[]{CalendarsColumns.EDITOR_ACCESS, 1200, 2100, 2300}, new int[]{Weather.WEATHERVERSION_ROM_2_0, 1700, 2700, Weather.WEATHERVERSION_ROM_3_0}, new int[]{1300, 2400, 3600, Weather.WEATHERVERSION_ROM_3_5}, new int[]{1500, 3200, WakeHookConfig.FINGERPRINT_PRESS_UNLOCK_TIMEOUT, 5200}};
        int i = 11;
        double[] dArr = new double[2];
        dArr = new double[]{0.0d, 0.0d};
        int location_flag = Arrays.binarySearch(new int[]{0, 5, 20, 115, CalendarsColumns.RESPOND_ACCESS, 500, 800, 1200, 1700, 2400, 3200}, vivoCalacAdjustPoint(x_gm));
        if (location_flag < 0 || location_flag > 10) {
            up_adjust_eight = x_gm + Weather.WEATHERVERSION_ROM_3_0;
            up_adjust_night = x_gm + Weather.WEATHERVERSION_ROM_3_5;
            down_adjust_night = x_gm - 2100;
        } else {
            up_adjust_eight = adjust_als_threshold[location_flag][2];
            up_adjust_night = adjust_als_threshold[location_flag][3];
            down_adjust_night = adjust_als_threshold[location_flag][0];
        }
        double down_c = 0.4659832246039143d * ((double) (x_gm - down_adjust_night));
        dArr[0] = ((double) (up_adjust_eight - x_gm)) * 0.5574136008918618d;
        dArr[1] = ((double) (up_adjust_night - x_gm)) * 0.4659832246039143d;
        if (dArr[0] < dArr[1]) {
            up_c = dArr[0];
        } else {
            up_c = dArr[1];
        }
        if (x >= x_gm) {
            output_args = ((double) deta_x) * Math.pow(2.718281828459045d, ((double) ((-(x - x_gm)) * (x - x_gm))) / ((2.0d * up_c) * up_c));
        } else if (down_c == 0.0d) {
            output_args = (double) change_cneed;
        } else {
            output_args = ((double) deta_x) * Math.pow(2.718281828459045d, ((double) ((-(x - x_gm)) * (x - x_gm))) / ((2.0d * down_c) * down_c));
        }
        return (int) output_args;
    }

    private void vivoAdjustSlope(int x_gm) {
        int i;
        int m = this.mKeyParameter.length;
        double[] temp_slope = new double[]{2.5d, 3.0d, 4.0d, 0.2d, 0.2d, 0.2d, 0.3d, 0.35d, 0.35d, 0.35d};
        int location = Arrays.binarySearch(this.mKeyPoint, x_gm);
        for (i = location; i > 0; i--) {
            if (this.mKeyParameter[i - 1][0] > temp_slope[i - 1]) {
                this.mKeyPointValue[i - 1] = (int) (((double) this.mKeyPointValue[i]) - (temp_slope[i - 1] * ((double) (this.mKeyPoint[i] - this.mKeyPoint[i - 1]))));
                this.mKeyParameter[i - 1][0] = temp_slope[i - 1];
                this.mKeyParameter[i - 1][1] = (double) ((int) (((double) this.mKeyPointValue[i]) - (temp_slope[i - 1] * ((double) this.mKeyPoint[i]))));
                if (i > 1) {
                    if (this.mKeyPoint[i - 2] != this.mKeyPoint[i - 1]) {
                        this.mKeyParameter[i - 2][0] = (((double) this.mKeyPointValue[i - 1]) - ((double) this.mKeyPointValue[i - 2])) / (((double) this.mKeyPoint[i - 1]) - ((double) this.mKeyPoint[i - 2]));
                        this.mKeyParameter[i - 2][1] = ((double) this.mKeyPointValue[i - 1]) - (this.mKeyParameter[i - 2][0] * ((double) this.mKeyPoint[i - 1]));
                    } else {
                        this.mKeyParameter[i - 2][0] = 0.0d;
                        this.mKeyParameter[i - 2][1] = (double) this.mKeyPointValue[i - 1];
                    }
                }
            }
        }
        i = location;
        while (i < m) {
            if (i != 0 && this.mKeyParameter[i][0] > temp_slope[i - 1]) {
                this.mKeyPointValue[i + 1] = (int) (((double) this.mKeyPointValue[i]) + (temp_slope[i - 1] * ((double) (this.mKeyPoint[i + 1] - this.mKeyPoint[i]))));
                this.mKeyParameter[i][0] = temp_slope[i - 1];
                this.mKeyParameter[i][1] = ((double) this.mKeyPointValue[i]) - (this.mKeyParameter[i][0] * ((double) this.mKeyPoint[i]));
                if (i < m - 1) {
                    if (this.mKeyPoint[i + 1] != this.mKeyPoint[i + 2]) {
                        this.mKeyParameter[i + 1][0] = ((double) (this.mKeyPointValue[i + 2] - this.mKeyPointValue[i + 1])) / ((double) (this.mKeyPoint[i + 2] - this.mKeyPoint[i + 1]));
                        this.mKeyParameter[i + 1][1] = ((double) this.mKeyPointValue[i + 1]) - (this.mKeyParameter[i + 1][0] * ((double) this.mKeyPoint[i + 1]));
                    } else {
                        this.mKeyParameter[i + 1][0] = 0.0d;
                        this.mKeyParameter[i + 1][1] = (double) this.mKeyPointValue[i + 1];
                    }
                }
            }
            i++;
        }
    }

    private void vivoAdjustFunc(int x_gm, int cneed_brightness) {
        int i;
        int n = this.mChangeLux.length;
        for (i = 0; i < n; i++) {
            if (this.mChangeLux[i] != -1) {
                int temp_location = Arrays.binarySearch(this.mKeyPoint, this.mChangeLux[i]);
                int j = 0;
                while (j <= temp_location) {
                    if (this.mKeyPoint[j] <= this.mChangeLux[i] && this.mKeyPointValue[j] > this.mKeyPointValue[temp_location]) {
                        this.mKeyPointValue[j] = this.mKeyPointValue[temp_location];
                    }
                    j++;
                }
            }
        }
        n = this.mKeyPoint.length;
        i = 0;
        while (i < n) {
            if (this.mKeyPoint[i] <= x_gm && this.mKeyPointValue[i] > cneed_brightness) {
                this.mKeyPointValue[i] = cneed_brightness;
            }
            if (this.mKeyPoint[i] == x_gm) {
                this.mKeyPointValue[i] = cneed_brightness;
            }
            i++;
        }
        for (i = 0; i < n - 1; i++) {
            if (this.mKeyPointValue[i] >= this.mKeyPointValue[i + 1]) {
                this.mKeyPointValue[i + 1] = this.mKeyPointValue[i];
            }
        }
        int positive_mark = -1;
        i = 0;
        while (i < n && this.mKeyPointValue[i] < 0) {
            positive_mark = i;
            i++;
        }
        positive_mark++;
        if (positive_mark >= 1 && positive_mark < n) {
            double temp;
            if (x_gm >= 4000 && cneed_brightness < 60) {
                temp = (double) cneed_brightness;
            } else if (x_gm >= 1500 && cneed_brightness < 40) {
                temp = (double) cneed_brightness;
            } else if (cneed_brightness < 20) {
                temp = (double) cneed_brightness;
            } else {
                temp = (double) this.mKeyPointValue[positive_mark];
            }
            if (temp != ((double) this.mKeyPointValue[positive_mark])) {
                for (i = 0; i < n - 1; i++) {
                    if (temp < ((double) this.mKeyPointValue[i])) {
                        positive_mark = i;
                        break;
                    }
                }
            } else if (temp < ((double) ((cneed_brightness * 2) / 3)) && positive_mark < n - 1) {
                temp = Math.floor((((double) this.mKeyPointValue[positive_mark]) + (((double) this.mKeyPointValue[positive_mark + 1]) * 3.0d)) / 4.0d);
                positive_mark++;
            }
            i = 0;
            while (i <= positive_mark - 1) {
                if (i == positive_mark - 1) {
                    this.mKeyPointValue[i] = (int) (0.98d * temp);
                } else {
                    this.mKeyPointValue[i] = (int) ((((double) i) / (((double) i) + 2.0d)) * temp);
                }
                if (i == 1 && this.mKeyPointValue[i] >= 7) {
                    this.mKeyPointValue[i] = 6;
                }
                i++;
            }
        }
        for (i = 0; i < n - 1; i++) {
            if (this.mKeyPoint[i + 1] != this.mKeyPoint[i]) {
                this.mKeyParameter[i][0] = ((double) (this.mKeyPointValue[i + 1] - this.mKeyPointValue[i])) / ((double) (this.mKeyPoint[i + 1] - this.mKeyPoint[i]));
                this.mKeyParameter[i][1] = ((double) this.mKeyPointValue[i]) - (this.mKeyParameter[i][0] * ((double) this.mKeyPoint[i]));
            } else {
                this.mKeyParameter[i][0] = 0.0d;
                this.mKeyParameter[i][1] = (double) this.mKeyPointValue[i];
            }
        }
        vivoAdjustSlope(x_gm);
    }

    private void vivoAdjustCneed(int location) {
        int n = this.mRecordNeed.length;
        int temp_orig = this.mLimitBrightness[this.mLimitBrightness.length / 2];
        int i = 0;
        while (i < n) {
            if (this.mRecordNeed[i][0] == this.mRecordNeed[location][0] && this.mRecordNeed[i][2] == 0) {
                if (this.mRecordNeed[i][1] < this.mLimitBrightness[0]) {
                    this.mRecordNeed[i][1] = this.mRecordNeed[i][1] + ((int) Math.ceil(((double) (temp_orig - this.mRecordNeed[i][1])) * 0.25d));
                } else if (this.mRecordNeed[i][1] < this.mLimitBrightness[1]) {
                    this.mRecordNeed[i][1] = this.mRecordNeed[i][1] + ((int) Math.ceil(((double) (temp_orig - this.mRecordNeed[i][1])) * 0.15d));
                } else if (this.mRecordNeed[i][1] < this.mLimitBrightness[2]) {
                    this.mRecordNeed[i][1] = this.mRecordNeed[i][1] + ((int) Math.ceil(((double) (temp_orig - this.mRecordNeed[i][1])) * 0.05d));
                } else if (this.mRecordNeed[i][1] < this.mLimitBrightness[3]) {
                    this.mRecordNeed[i][1] = this.mRecordNeed[i][1] - ((int) Math.ceil(((double) (this.mRecordNeed[i][1] - temp_orig)) * 0.05d));
                } else if (this.mRecordNeed[i][1] < this.mLimitBrightness[4]) {
                    this.mRecordNeed[i][1] = this.mRecordNeed[i][1] - ((int) Math.ceil(((double) (this.mRecordNeed[i][1] - temp_orig)) * 0.2d));
                } else {
                    this.mRecordNeed[i][1] = this.mRecordNeed[i][1] - ((int) Math.ceil(((double) (this.mRecordNeed[i][1] - temp_orig)) * 0.3d));
                }
                this.mRecordNeed[i][2] = 1;
                if (this.mRecordNeed[i][1] < this.mLimitBrightness[0] || this.mRecordNeed[i][1] >= this.mLimitBrightness[4]) {
                    for (int j = 0; j < n; j++) {
                        if (this.mRecordNeed[j][3] > this.mRecordNeed[i][3]) {
                            this.mRecordNeed[j][3] = this.mRecordNeed[j][3] - 1;
                        }
                    }
                    this.mRecordCount--;
                    this.mRecordNeed[i][0] = -1;
                    this.mRecordNeed[i][1] = -1;
                    this.mRecordNeed[i][2] = -1;
                    this.mRecordNeed[i][3] = -1;
                }
            }
            i++;
        }
    }

    private void vivoCalacLimitBrightness(int x_gm) {
        this.mLimitBrightness[2] = vivoCalacOrigBrighntenss(x_gm);
        if (x_gm == 0) {
            this.mLimitBrightness[0] = 0;
            this.mLimitBrightness[1] = 0;
            this.mLimitBrightness[3] = 4;
            this.mLimitBrightness[4] = 6;
        } else if (x_gm <= 5) {
            this.mLimitBrightness[0] = 2;
            this.mLimitBrightness[1] = 2;
            this.mLimitBrightness[3] = 16;
            this.mLimitBrightness[4] = 20;
        } else if (x_gm <= 10) {
            this.mLimitBrightness[0] = 2;
            this.mLimitBrightness[1] = 2;
            this.mLimitBrightness[3] = 24;
            this.mLimitBrightness[4] = 26;
        } else if (x_gm <= 20) {
            this.mLimitBrightness[0] = 4;
            this.mLimitBrightness[1] = (int) Math.floor(((double) this.mLimitBrightness[2]) * 0.4d);
            this.mLimitBrightness[3] = (int) Math.floor(((double) this.mLimitBrightness[2]) * 1.3d);
            this.mLimitBrightness[4] = (int) Math.ceil(((double) this.mLimitBrightness[2]) * 2.6d);
        } else if (x_gm <= 500) {
            this.mLimitBrightness[0] = (int) Math.floor(((double) this.mLimitBrightness[2]) * 0.3d);
            this.mLimitBrightness[1] = (int) Math.floor(((double) this.mLimitBrightness[2]) * 0.6d);
            this.mLimitBrightness[3] = (int) Math.floor(((double) this.mLimitBrightness[2]) * 1.3d);
            this.mLimitBrightness[4] = (int) Math.ceil(((double) this.mLimitBrightness[2]) * 2.1d);
        } else {
            this.mLimitBrightness[0] = (int) Math.floor(((double) this.mLimitBrightness[2]) * 0.5d);
            this.mLimitBrightness[1] = (int) Math.floor(((double) this.mLimitBrightness[2]) * 0.7d);
            this.mLimitBrightness[3] = (int) Math.floor(((double) this.mLimitBrightness[2]) * 1.3d);
            this.mLimitBrightness[4] = (int) Math.ceil(((double) this.mLimitBrightness[2]) * 2.0d);
        }
    }

    private void vivoClusterDistance(int[][] tempVale, int count, int x_gm, int cneed_brightness) {
        int i;
        int temp;
        int[] clusterResult = new int[count];
        int[][] tempResult = (int[][]) Array.newInstance(Integer.TYPE, new int[]{9, 5});
        int[] tempPoint = new int[]{0, 8, 19, 215, CalendarsColumns.EDITOR_ACCESS, 1500, 2750, 4250, 5000};
        for (i = 0; i < count; i++) {
            if (tempVale[i][0] == 0) {
                clusterResult[i] = 1;
            } else if (tempVale[i][0] < 15) {
                clusterResult[i] = 2;
            } else if (tempVale[i][0] <= 30) {
                clusterResult[i] = 3;
            } else if (tempVale[i][0] <= 400) {
                clusterResult[i] = 4;
            } else if (tempVale[i][0] <= 1000) {
                clusterResult[i] = 5;
            } else if (tempVale[i][0] <= 2000) {
                clusterResult[i] = 6;
            } else if (tempVale[i][0] <= 3500) {
                clusterResult[i] = 7;
            } else if (tempVale[i][0] < 5000) {
                clusterResult[i] = 8;
            } else {
                clusterResult[i] = 9;
            }
        }
        i = 0;
        while (i < count - 1) {
            temp = Math.abs(tempVale[i][0] - tempVale[i + 1][0]);
            if (clusterResult[i] != clusterResult[i + 1]) {
                if (tempVale[i][0] == 0) {
                    i++;
                } else if (tempVale[i][0] < 15) {
                    if (temp <= 5) {
                        clusterResult[i + 1] = 2;
                        i++;
                    }
                } else if (tempVale[i][0] <= 30) {
                    if (temp < 10) {
                        clusterResult[i + 1] = 3;
                        i++;
                    }
                } else if (tempVale[i][0] <= 400) {
                    if (temp < 100) {
                        clusterResult[i + 1] = 4;
                        i++;
                    }
                } else if (tempVale[i][0] <= 1000) {
                    if (temp < 200) {
                        clusterResult[i + 1] = 5;
                        i++;
                    }
                } else if (tempVale[i][0] <= 2000) {
                    if (temp < 240) {
                        clusterResult[i + 1] = 6;
                        i++;
                    }
                } else if (tempVale[i][0] <= 3500 && temp < CalendarsColumns.RESPOND_ACCESS) {
                    clusterResult[i + 1] = 7;
                    i++;
                }
            }
            i++;
        }
        for (i = 0; i < count; i++) {
            tempResult[clusterResult[i] - 1][0] = tempResult[clusterResult[i] - 1][0] + this.mRecordNeed[tempVale[i][1]][0];
            tempResult[clusterResult[i] - 1][1] = tempResult[clusterResult[i] - 1][1] + this.mRecordNeed[tempVale[i][1]][1];
            tempResult[clusterResult[i] - 1][2] = tempResult[clusterResult[i] - 1][2] + 1;
        }
        for (i = 0; i < 9; i++) {
            if (tempResult[i][2] != 0) {
                tempResult[i][3] = (int) (((double) tempResult[i][0]) / ((double) tempResult[i][2]));
                tempResult[i][4] = (int) (((double) tempResult[i][1]) / ((double) tempResult[i][2]));
                tempPoint[i] = tempResult[i][3];
            } else {
                tempResult[i][3] = -1;
                tempResult[i][4] = -1;
            }
        }
        temp = 0;
        for (i = 0; i < count; i++) {
            if (tempVale[i][0] == x_gm) {
                temp = i;
                break;
            }
        }
        tempResult[clusterResult[temp] - 1][3] = x_gm;
        tempPoint[clusterResult[temp] - 1] = x_gm;
        tempResult[clusterResult[temp] - 1][4] = cneed_brightness;
        for (i = 0; i < 9; i++) {
            this.mChangeLux[i] = tempResult[i][3];
            this.mChangeCneed[i] = tempResult[i][4];
            if (tempResult[i][3] != -1) {
                this.mChangeBar[i] = tempResult[i][4] - vivoCalacOrigBrighntenss(this.mChangeLux[i]);
            }
        }
        vivoAdjustKeyPoint(tempPoint, 30);
    }

    private void vivoOptimizeAdjustParameter(int x_gm, int cneed_brightness) {
        int[][] tempAls = (int[][]) Array.newInstance(Integer.TYPE, new int[]{30, 2});
        int tempCount = 0;
        int n = this.mRecordNeed.length;
        int i = 0;
        while (i < n) {
            if (!(this.mRecordNeed[i][0] == x_gm || this.mRecordNeed[i][0] == -1 || this.mRecordNeed[i][2] == 1)) {
                vivoCalacLimitBrightness(this.mRecordNeed[i][0]);
                vivoAdjustCneed(i);
            }
            i++;
        }
        for (i = 0; i < n; i++) {
            if (this.mRecordNeed[i][0] != -1) {
                tempAls[tempCount][0] = this.mRecordNeed[i][0];
                tempAls[tempCount][1] = i;
                tempCount++;
            }
        }
        for (i = 0; i < tempCount; i++) {
            for (int j = i + 1; j < tempCount; j++) {
                if (tempAls[i][0] > tempAls[j][0]) {
                    int tempVale = tempAls[i][0];
                    tempAls[i][0] = tempAls[j][0];
                    tempAls[j][0] = tempVale;
                    tempVale = tempAls[i][1];
                    tempAls[i][1] = tempAls[j][1];
                    tempAls[j][1] = tempVale;
                }
            }
        }
        vivoClusterDistance(tempAls, tempCount, x_gm, cneed_brightness);
    }

    private void vivoAdjustKeyPoint(int[] tempPoint, int x_gm) {
        boolean flag = true;
        int n = tempPoint.length;
        int i = 0;
        while (i < n - 1) {
            if (tempPoint[i] <= x_gm && tempPoint[i + 1] > x_gm) {
                this.mKeyPoint[i + 1] = x_gm;
                this.mKeyPoint[i] = tempPoint[i];
                flag = false;
            } else if (flag) {
                this.mKeyPoint[i] = tempPoint[i];
            } else {
                this.mKeyPoint[i + 1] = tempPoint[i];
            }
            i++;
        }
        if (flag) {
            this.mKeyPoint[n - 1] = x_gm;
        }
        this.mKeyPoint[this.mKeyPoint.length - 1] = 5000;
    }

    private int vivoCalacOrigBrighntenss(int x_gm) {
        double temp;
        if (x_gm <= mOrigKeyValue[1][0]) {
            temp = (mOrigParam[0][0] * ((double) x_gm)) + mOrigParam[0][1];
        } else if (x_gm <= mOrigKeyValue[2][0]) {
            temp = (mOrigParam[1][0] * ((double) x_gm)) + mOrigParam[1][1];
        } else if (x_gm <= mOrigKeyValue[3][0]) {
            temp = (mOrigParam[2][0] * ((double) x_gm)) + mOrigParam[2][1];
        } else if (x_gm <= mOrigKeyValue[4][0]) {
            temp = (mOrigParam[3][0] * ((double) x_gm)) + mOrigParam[3][1];
        } else {
            temp = (double) mOrigKeyValue[4][1];
        }
        return (int) temp;
    }

    private double vivoCalacBrighntenss(int x_gm) {
        double temp_brightness = (double) vivoCalacOrigBrighntenss(x_gm);
        int n = this.mChangeBar.length;
        for (int i = 0; i < n; i++) {
            if (this.mChangeLux[i] >= 0) {
                temp_brightness += (double) adjustFunc(x_gm, this.mChangeBar[i], this.mChangeLux[i], this.mChangeCneed[i]);
            }
        }
        double brightness = temp_brightness;
        return temp_brightness;
    }

    private int vivoCalacBrighntenssFinnal(int x_gm) {
        int n = this.mKeyParameter.length;
        double temp_brightness = 30.0d;
        int i = 0;
        while (i < n) {
            if (x_gm >= this.mKeyPoint[i] && x_gm <= this.mKeyPoint[i + 1]) {
                temp_brightness = Math.floor((this.mKeyParameter[i][0] * ((double) x_gm)) + this.mKeyParameter[i][1]);
                break;
            }
            i++;
        }
        return (int) temp_brightness;
    }

    private int vivoCalacAdjustPoint(int x_gm) {
        if (x_gm <= 1 && x_gm >= 0) {
            return 0;
        }
        if (x_gm > 1 && x_gm <= 10) {
            return 5;
        }
        if (x_gm > 10 && x_gm <= 30) {
            return 20;
        }
        if (x_gm > 30 && x_gm <= 200) {
            return 115;
        }
        if (x_gm > 200 && x_gm <= 400) {
            return CalendarsColumns.RESPOND_ACCESS;
        }
        if (x_gm > 400 && x_gm <= CalendarsColumns.EDITOR_ACCESS) {
            return 500;
        }
        if (x_gm > CalendarsColumns.EDITOR_ACCESS && x_gm <= Weather.WEATHERVERSION_ROM_2_0) {
            return 800;
        }
        if (x_gm <= 1400) {
            return 1200;
        }
        if (x_gm <= Weather.WEATHERVERSION_ROM_2_5_1) {
            return 1700;
        }
        if (x_gm <= 2800) {
            return 2400;
        }
        if (x_gm <= 3600) {
            return 3200;
        }
        if (x_gm <= 4400) {
            return Weather.WEATHERVERSION_ROM_3_5;
        }
        return 5000;
    }

    private void vivoAdjustBar(int x_gm, int cneed_brightness) {
        int temp_brightness;
        if (this.bUserSettingBrightness) {
            temp_brightness = vivoCalacBrighntenssFinnal(x_gm);
        } else {
            temp_brightness = vivoCalacOrigBrighntenss(x_gm);
        }
        if (temp_brightness <= 255 || cneed_brightness <= 240) {
            int tempValue;
            int i;
            int temp_current_brightness = cneed_brightness;
            int n = this.mRecordNeed.length;
            this.mRecordCount++;
            if (this.mRecordCount > n) {
                this.mRecordCount = n;
            }
            if (this.mRecordCount == n) {
                tempValue = 1;
            } else {
                tempValue = -1;
            }
            int recordLocation = n - 1;
            boolean flag = true;
            for (i = 0; i < n; i++) {
                if (this.mRecordNeed[i][3] == tempValue && flag) {
                    recordLocation = i;
                    flag = false;
                }
                if (tempValue == 1) {
                    this.mRecordNeed[i][3] = this.mRecordNeed[i][3] - 1;
                }
            }
            this.mRecordNeed[recordLocation][0] = x_gm;
            this.mRecordNeed[recordLocation][1] = cneed_brightness;
            this.mRecordNeed[recordLocation][2] = 0;
            this.mRecordNeed[recordLocation][3] = this.mRecordCount;
            vivoOptimizeAdjustParameter(x_gm, cneed_brightness);
            n = this.mKeyPoint.length;
            for (i = 0; i < n; i++) {
                this.mKeyPointValue[i] = (int) vivoCalacBrighntenss(this.mKeyPoint[i]);
            }
            vivoAdjustFunc(x_gm, cneed_brightness);
            vivoPrintAdjustBarLog(cneed_brightness);
            this.bUserSettingBrightness = true;
            this.mArg.mPhoneStatus[2] = this.mRgbCureAlgoData.popPhoneMotionStatus();
            vivoCalacUpDownGm(x_gm, cneed_brightness);
            if (x_gm <= 6) {
                this.mUserAdjustTime = SystemClock.uptimeMillis();
            }
        }
    }

    private void vivoCalacUpDownBrightness(int current_brightness) {
        if (current_brightness < 15) {
            this.mChangeUpBrightness = (int) ((mUpDownParam[0][0] * ((double) current_brightness)) + mUpDownParam[0][1]);
        } else if (current_brightness < 35) {
            this.mChangeUpBrightness = (int) ((mUpDownParam[1][0] * ((double) current_brightness)) + mUpDownParam[1][1]);
        } else if (current_brightness < 80) {
            this.mChangeUpBrightness = (int) ((mUpDownParam[2][0] * ((double) current_brightness)) + mUpDownParam[2][1]);
        } else {
            this.mChangeUpBrightness = (int) ((mUpDownParam[3][0] * ((double) current_brightness)) + mUpDownParam[3][1]);
        }
        if (current_brightness < 25) {
            this.mChangeDownBrightness = (int) ((mUpDownParam[4][0] * ((double) current_brightness)) + mUpDownParam[4][1]);
        } else if (current_brightness < 35) {
            this.mChangeDownBrightness = (int) ((mUpDownParam[5][0] * ((double) current_brightness)) + mUpDownParam[5][1]);
        } else if (current_brightness < 80) {
            this.mChangeDownBrightness = (int) ((mUpDownParam[6][0] * ((double) current_brightness)) + mUpDownParam[6][1]);
        } else if (current_brightness < 120) {
            this.mChangeDownBrightness = (int) ((mUpDownParam[7][0] * ((double) current_brightness)) + mUpDownParam[7][1]);
        } else {
            this.mChangeDownBrightness = (int) ((mUpDownParam[8][0] * ((double) current_brightness)) + mUpDownParam[8][1]);
        }
        if (this.mChangeUpBrightness > 255) {
            this.mChangeUpBrightness = 255;
        }
        if (this.mChangeDownBrightness > 255) {
            this.mChangeDownBrightness = 255;
        }
    }

    private void vivoCalacOrigUpDowmGm(int current_brightness, int up_brightness, int down_brightness) {
        boolean temp_flag_up = true;
        boolean temp_flag_down = true;
        int i = 0;
        while (i < 4) {
            if (current_brightness == 255) {
                temp_flag_up = false;
                this.mChangeUpLux = 5001;
            }
            if (down_brightness < 2) {
                temp_flag_down = false;
                this.mChangeDownLux = -1;
            }
            if (AblConfig.isUse2048GrayScaleBacklight()) {
                if (down_brightness < 2 && current_brightness <= 4 && this.mTimePeriod.getTimePeriod() == PeriodType.DAY_TIME) {
                    temp_flag_down = false;
                    this.mChangeDownLux = -1;
                } else if (down_brightness < 2 && current_brightness == 4) {
                    temp_flag_down = false;
                    this.mChangeDownLux = 0;
                } else if (down_brightness < 2 && current_brightness > 4) {
                    temp_flag_down = false;
                    this.mChangeDownLux = 0;
                }
            }
            if (up_brightness >= mOrigKeyValue[i][1] && up_brightness <= mOrigKeyValue[i + 1][1] && temp_flag_up) {
                this.mChangeUpLux = (int) ((((double) up_brightness) - mOrigParam[i][1]) / mOrigParam[i][0]);
                temp_flag_up = false;
                if (this.mChangeUpLux == 0) {
                    this.mChangeUpLux = 1;
                }
            }
            if (down_brightness >= mOrigKeyValue[i][1] && down_brightness <= mOrigKeyValue[i + 1][1] && temp_flag_down) {
                temp_flag_down = false;
                if (down_brightness >= 0) {
                    this.mChangeDownLux = (int) ((((double) down_brightness) - mOrigParam[i][1]) / mOrigParam[i][0]);
                } else {
                    this.mChangeDownLux = -1;
                }
            }
            if (!temp_flag_up && (temp_flag_down ^ 1) != 0) {
                break;
            }
            i++;
        }
        if (i == 4 && temp_flag_up) {
            this.mChangeUpLux = 5001;
        }
    }

    private void vivoCalacAdjustUpDowmGm(int current_brightness, int up_brightness, int down_brightness) {
        boolean temp_flag_up = true;
        boolean temp_flag_down = true;
        int n = this.mKeyPointValue.length;
        int i = 0;
        while (i < n - 1) {
            if (current_brightness == 255) {
                temp_flag_up = false;
                this.mChangeUpLux = 5001;
            }
            if (down_brightness < 2) {
                temp_flag_down = false;
                this.mChangeDownLux = -1;
            }
            if (up_brightness >= this.mKeyPointValue[i] && up_brightness <= this.mKeyPointValue[i + 1] && temp_flag_up) {
                if (this.mKeyParameter[i][0] != 0.0d) {
                    this.mChangeUpLux = (int) ((((double) up_brightness) - this.mKeyParameter[i][1]) / this.mKeyParameter[i][0]);
                } else {
                    this.mChangeUpLux = this.mKeyPoint[i];
                }
                temp_flag_up = false;
            }
            if (down_brightness >= this.mKeyPointValue[i] && down_brightness <= this.mKeyPointValue[i + 1] && temp_flag_down) {
                temp_flag_down = false;
                if (this.mKeyParameter[i][0] != 0.0d && down_brightness >= 1) {
                    this.mChangeDownLux = (int) ((((double) down_brightness) - this.mKeyParameter[i][1]) / this.mKeyParameter[i][0]);
                } else if (down_brightness > 0) {
                    this.mChangeDownLux = this.mKeyPoint[i + 1];
                } else {
                    this.mChangeDownLux = -1;
                }
            }
            if (!temp_flag_up && (temp_flag_down ^ 1) != 0) {
                break;
            }
            i++;
        }
        if (i == n - 1 && temp_flag_up) {
            this.mChangeUpLux = 5001;
        }
        if (i == n - 1 && temp_flag_down) {
            this.mChangeDownLux = -1;
        }
    }

    private void vivoOptimizeUpDowmGm(int x_gm, int current_brightness, int up_gm, int down_gm) {
        int temp_threshold;
        int temp_up_brightness = vivoCalacBrighntenssFinnal(up_gm);
        int temp_down_brightness = vivoCalacBrighntenssFinnal(down_gm);
        if (this.mChangeDownLux < down_gm) {
            if (current_brightness <= 10) {
                temp_threshold = 5;
            } else if (current_brightness <= 20) {
                temp_threshold = 10;
            } else {
                temp_threshold = 15;
            }
            while (current_brightness - temp_down_brightness < temp_threshold) {
                if (down_gm > Weather.WEATHERVERSION_ROM_2_5_1) {
                    down_gm -= 400;
                } else if (down_gm > Weather.WEATHERVERSION_ROM_2_0) {
                    down_gm -= 200;
                } else if (down_gm > 400) {
                    down_gm -= 100;
                } else if (down_gm > 100) {
                    down_gm -= 30;
                } else if (down_gm > 20) {
                    down_gm -= 20;
                } else {
                    down_gm -= 5;
                }
                if (down_gm <= 5) {
                    this.mChangeDownLux = 1;
                }
                temp_down_brightness = vivoCalacBrighntenssFinnal(down_gm);
                if (down_gm < 0) {
                    break;
                }
            }
            this.mChangeDownLux = down_gm;
        } else {
            this.mChangeDownLux = down_gm;
        }
        if (this.mChangeUpLux > up_gm) {
            if (current_brightness <= 10) {
                temp_threshold = 7;
            } else if (current_brightness <= 20) {
                temp_threshold = 10;
            } else {
                temp_threshold = 15;
            }
            while (up_gm < this.mChangeUpLux) {
                if (temp_up_brightness - current_brightness >= temp_threshold) {
                    this.mChangeUpLux = up_gm;
                    return;
                }
                if (up_gm > Weather.WEATHERVERSION_ROM_2_5_1) {
                    up_gm += 400;
                } else if (up_gm > Weather.WEATHERVERSION_ROM_2_0) {
                    up_gm += 200;
                } else if (up_gm > 100) {
                    up_gm += 100;
                } else {
                    up_gm += 20;
                }
                if (up_gm >= 4600) {
                    this.mChangeUpLux = 5000;
                }
                temp_up_brightness = vivoCalacBrighntenssFinnal(up_gm);
                if (up_gm > 5000) {
                    return;
                }
            }
            return;
        }
        this.mChangeUpLux = up_gm;
    }

    private int vivoAdjustAls(int x_gm, int current_brightness) {
        int temp_als = x_gm;
        boolean temp_flag = true;
        int temp_brightness = vivoCalacBrighntenssFinnal(x_gm);
        vivoCalacUpDownBrightness(current_brightness);
        if ((temp_brightness < this.mChangeUpBrightness && temp_brightness > this.mChangeDownBrightness) || temp_brightness == 255) {
            return x_gm;
        }
        int n = this.mKeyPointValue.length;
        if (current_brightness < 2) {
            temp_brightness = 2;
        } else if (current_brightness > 255) {
            temp_brightness = 255;
        } else {
            temp_brightness = current_brightness;
        }
        int i = 0;
        while (i < n - 1) {
            if (temp_brightness >= this.mKeyPointValue[i] && temp_brightness <= this.mKeyPointValue[i + 1] && temp_flag) {
                if (this.mKeyParameter[i][0] != 0.0d) {
                    temp_als = (int) ((((double) temp_brightness) - this.mKeyParameter[i][1]) / this.mKeyParameter[i][0]);
                } else {
                    temp_als = (this.mKeyPoint[i] + this.mKeyPoint[i + 1]) / 2;
                }
                temp_flag = false;
            }
            i++;
        }
        return temp_als;
    }

    public void onAppBrightRatioChanged(int bright) {
        if (bright != -1) {
            if (!this.mAppBrightRatio) {
                this.mLastChangeDownGm = this.mChangeDownLux;
                this.mLastChangeUpGm = this.mChangeUpLux;
                this.mChangeDownLux = -1;
                this.mChangeUpLux = 11;
                this.mAppBrightRatio = true;
            }
        } else if (this.mAppBrightRatio) {
            this.mChangeDownLux = this.mLastChangeDownGm;
            this.mChangeUpLux = this.mLastChangeUpGm;
            this.mAppBrightRatio = false;
        }
    }

    private void vivoAlsCalacUpDownGm(int x_gm, int current_brightness) {
        if (x_gm > Weather.WEATHERVERSION_ROM_3_5) {
            this.mChangeUpLux = 5000;
            this.mChangeDownLux = Weather.WEATHERVERSION_ROM_2_5_1;
        } else if (x_gm > Weather.WEATHERVERSION_ROM_2_5_1) {
            this.mChangeUpLux = 3500;
            this.mChangeDownLux = CalendarsColumns.EDITOR_ACCESS;
        } else if (x_gm > 900) {
            this.mChangeUpLux = 200;
            this.mChangeDownLux = CalendarsColumns.EDITOR_ACCESS;
        }
        if (current_brightness == 255) {
            this.mChangeUpLux = 5001;
        }
        if (current_brightness <= 3) {
            this.mChangeDownLux = -1;
        }
    }

    private void vivoCalacUpDownGm(int x_gm, int current_brightness) {
        int temp_als = x_gm;
        if (this.mAppBrightRatio) {
            this.mChangeDownLux = this.mLastChangeDownGm;
            this.mChangeUpLux = this.mLastChangeUpGm;
            this.mAppBrightRatio = false;
        }
        if (this.bUserSettingBrightness) {
            temp_als = vivoAdjustAls(x_gm, current_brightness);
            int temp_brightness = vivoCalacOrigBrighntenss(temp_als);
            vivoCalacUpDownBrightness(temp_brightness);
            vivoCalacOrigUpDowmGm(temp_brightness, this.mChangeUpBrightness, this.mChangeDownBrightness);
            int orig_up_gm = this.mChangeUpLux;
            int orig_down_gm = this.mChangeDownLux;
            vivoCalacUpDownBrightness(current_brightness);
            vivoCalacAdjustUpDowmGm(current_brightness, this.mChangeUpBrightness, this.mChangeDownBrightness);
            vivoOptimizeUpDowmGm(temp_als, current_brightness, orig_up_gm, orig_down_gm);
        } else {
            vivoCalacUpDownBrightness(current_brightness);
            vivoCalacOrigUpDowmGm(current_brightness, this.mChangeUpBrightness, this.mChangeDownBrightness);
        }
        vivoPrintAdjustBarLog(current_brightness);
        vivoCalacPhoneStatusChangDownAls(x_gm);
        this.mPriveChangeDownLux = this.mChangeDownLux;
        if (this.mPrivPhoneStatus > 0 && this.mPrivPhoneStatus < 8) {
            this.mChangeDownLux = this.mPhoneStatusChangeDownLux[this.mPrivPhoneStatus - 1];
        }
        if (this.mMontionStatus == 1) {
            this.mChangeDownLux = vivoCalacMotionChangeDownTheashAls(x_gm);
        }
        if (this.mPriveVideoGameFlag) {
            this.mChangeDownLux = vivoCalacSidewardsChangeDownTheashAls(x_gm, true);
        }
        Slog.e(TAG, "vivo_adjust_bar mChangeUpLux " + this.mChangeUpLux + " mChangeDownLux " + this.mChangeDownLux + " x_gm " + x_gm + " current_brightness " + current_brightness);
    }

    private void vivoCalacPhoneStatusChangDownAls(int x_gm) {
        for (int j = 0; j < 8; j++) {
            this.mPhoneStatusChangeDownLux[j] = this.mChangeDownLux;
            if (x_gm >= 1 && x_gm <= 5 && this.mTimePeriod.getTimePeriod() == PeriodType.DARK_NIGHT_TIME) {
                this.mPhoneStatusChangeDownLux[j] = 0;
            }
        }
    }

    private void saveRecordNeed() {
        int n = this.mRecordNeed.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 2; j++) {
                if (j == 1) {
                    this.mArg.mRecordNeed[i][j] = (this.mRecordNeed[i][j] * 100) + this.mRecordNeed[i][3];
                } else {
                    this.mArg.mRecordNeed[i][j] = this.mRecordNeed[i][j];
                }
            }
        }
    }

    private String setCountIntToString() {
        return (Events.DEFAULT_SORT_ORDER + this.mArg.mPhoneStatusCount + ",") + this.mArg.mCameraOpenCount + ",";
    }

    private String setRecordIntToString() {
        String ret = Events.DEFAULT_SORT_ORDER;
        int m = this.mRecordNeed[0].length;
        for (int[] iArr : this.mRecordNeed) {
            for (int j = 0; j < m; j++) {
                ret = ret + iArr[j] + ",";
            }
        }
        return ret + this.mRecordCount + ",";
    }

    private void setStringToRecordInt(String str) {
        int k = 0;
        int m = this.mRecordNeed[0].length;
        String[] temp = str.split(",");
        for (int[] iArr : this.mRecordNeed) {
            for (int j = 0; j < m; j++) {
                iArr[j] = Integer.parseInt(temp[k], 10);
                k++;
            }
        }
        this.mRecordCount = Integer.parseInt(temp[k], 10);
    }

    private void setStringToKeyParamInt(String str) {
        int i;
        int k = 0;
        String[] temp = str.split(",");
        int m = this.mKeyPoint.length;
        for (i = 0; i < m; i++) {
            this.mKeyPoint[i] = Integer.parseInt(temp[k], 10);
            k++;
        }
        m = this.mKeyParameter[0].length;
        for (double[] dArr : this.mKeyParameter) {
            for (int j = 0; j < m; j++) {
                dArr[j] = ((double) Integer.parseInt(temp[k], 10)) / VAR;
                k++;
            }
        }
    }

    private String setKeyParamIntToString() {
        String ret = Events.DEFAULT_SORT_ORDER;
        for (int i : this.mKeyPoint) {
            ret = ret + i + ",";
        }
        int m = this.mKeyParameter[0].length;
        for (double[] dArr : this.mKeyParameter) {
            for (int j = 0; j < m; j++) {
                ret = ret + ((int) Math.round(dArr[j] * VAR)) + ",";
            }
        }
        return ret;
    }

    private void setStringToCountInt(String str) {
        String[] temp = str.split(",");
        this.mArg.mPhoneStatusCount = Integer.parseInt(temp[0], 10);
        this.mArg.mCameraOpenCount = Integer.parseInt(temp[1], 10);
    }

    private void setParamToNegative(int[] param) {
        int m = param.length;
        for (int i = 0; i < m; i++) {
            param[i] = -1;
        }
    }

    private void setParamToNegative(double[][] param) {
        int m = param[0].length;
        for (double[] dArr : param) {
            for (int j = 0; j < m; j++) {
                dArr[j] = -1.0d;
            }
        }
    }

    private void setParamToNegative(int[][] param) {
        int m = param[0].length;
        for (int[] iArr : param) {
            for (int j = 0; j < m; j++) {
                iArr[j] = -1;
            }
        }
    }

    private void paramCopy(double[][] purpose_param, double[][] orig_param) {
        int m = orig_param[0].length;
        int n = orig_param.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                purpose_param[i][j] = orig_param[i][j];
            }
        }
    }

    private void paramCopy(int[][] purpose_param, int[][] orig_param) {
        int m = orig_param[0].length;
        int n = orig_param.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                purpose_param[i][j] = orig_param[i][j];
            }
        }
    }

    private void paramCopy(int[] purpose_param, int[] orig_param) {
        int m = orig_param.length;
        for (int i = 0; i < m; i++) {
            purpose_param[i] = orig_param[i];
        }
    }

    private void back_param() {
        paramCopy(this.mRecordNeedBak, this.mRecordNeed);
        this.mRecordCountBak = this.mRecordCount;
        paramCopy(this.mChangeBarBak, this.mChangeBar);
        paramCopy(this.mChangeLuxBak, this.mChangeLux);
        paramCopy(this.mChangeCneedBak, this.mChangeCneed);
        paramCopy(this.mKeyParameterBak, this.mKeyParameter);
        paramCopy(this.mkeyPointBak, this.mKeyPoint);
        this.bUserSettingBrightnessBak = this.bUserSettingBrightness;
    }

    private void restore_param() {
        paramCopy(this.mRecordNeed, this.mRecordNeedBak);
        this.mRecordCount = this.mRecordCountBak;
        paramCopy(this.mChangeBar, this.mChangeBarBak);
        paramCopy(this.mChangeLux, this.mChangeLuxBak);
        paramCopy(this.mChangeCneed, this.mChangeCneedBak);
        paramCopy(this.mKeyParameter, this.mKeyParameterBak);
        paramCopy(this.mKeyPoint, this.mkeyPointBak);
        this.bUserSettingBrightness = this.bUserSettingBrightnessBak;
    }

    private void modifyBrightness() {
        int[] temp = new int[]{-1, -1};
        Slog.d(TAG, "modifyBrightness");
        vivoAdjustBar(this.mAmbientLux, this.mSecondSettingBrightness);
        this.mArg.bUserSettingBrightness = this.bUserSettingBrightness;
        this.bIsAutoBakclightAdjust = true;
        saveRecordNeed();
        temp = this.mRgbCureAlgoData.popPhoneStatus();
        this.mArg.mPhoneStatus[0] = temp[0];
        this.mArg.mPhoneStatus[1] = temp[1];
        String jsonStr = this.mParser.argumentToJsonString(this.mArg);
        String recordString = setRecordIntToString();
        String keyParamString = setKeyParamIntToString();
        if (jsonStr != null && (Events.DEFAULT_SORT_ORDER.equals(jsonStr) ^ 1) != 0) {
            System.putString(this.mContext.getContentResolver(), AUTOBRIGHTNESS_PARAM, jsonStr);
            System.putString(this.mContext.getContentResolver(), AUTOBRIGHTNESS_RECORD, recordString);
            System.putString(this.mContext.getContentResolver(), AUTOBRIGHTNESS_KEY_PARAM, keyParamString);
            if (AblConfig.isDebug()) {
                Slog.d(TAG, "modifyBrightness jsonStr = " + jsonStr);
                Slog.d(TAG, "modifyBrightnessMode recordString = " + recordString);
                Slog.d(TAG, "modifyBrightnessMode keyParamString = " + keyParamString);
            }
        }
    }

    private void modifyBrightnessMode() {
        int[] temp = new int[]{-1, -1};
        setParamToNegative(this.mChangeLux);
        setParamToNegative(this.mChangeBar);
        setParamToNegative(this.mChangeCneed);
        setParamToNegative(this.mKeyPointValue);
        setParamToNegative(this.mKeyPoint);
        setParamToNegative(this.mKeyParameter);
        setParamToNegative(this.mRecordNeed);
        this.mRecordCount = 0;
        this.bUserSettingBrightness = false;
        this.mArg.bUserSettingBrightness = this.bUserSettingBrightness;
        saveRecordNeed();
        temp = this.mRgbCureAlgoData.popPhoneStatus();
        this.mArg.mPhoneStatus[0] = temp[0];
        this.mArg.mPhoneStatus[1] = temp[1];
        String jsonString = this.mParser.argumentToJsonString(this.mArg);
        String recordString = setRecordIntToString();
        String keyParamString = setKeyParamIntToString();
        if (!(jsonString == null || (Events.DEFAULT_SORT_ORDER.equals(jsonString) ^ 1) == 0)) {
            System.putString(this.mContext.getContentResolver(), AUTOBRIGHTNESS_PARAM, jsonString);
            System.putString(this.mContext.getContentResolver(), AUTOBRIGHTNESS_RECORD, recordString);
            System.putString(this.mContext.getContentResolver(), AUTOBRIGHTNESS_KEY_PARAM, keyParamString);
            if (AblConfig.isDebug()) {
                Slog.d(TAG, "modifyBrightnessMode jsonString = " + jsonString);
                Slog.d(TAG, "modifyBrightnessMode recordString = " + recordString);
                Slog.d(TAG, "modifyBrightnessMode keyParamString = " + keyParamString);
            }
        }
        System.putString(this.mContext.getContentResolver(), UserModifyRecorder.KEY_APP_BRIGHTNESS_RATIO, Events.DEFAULT_SORT_ORDER);
    }

    public void setLigtSensorEnable(boolean enable) {
        if (enable) {
            this.bIsAutoBakclightAdjust = true;
            this.mBacklightMode = 1;
            if (this.mBacklightMode != 0) {
                setInertiaSensorEnabled(true);
            }
        }
    }

    public void notifyStateChanged(int state) {
        String countString;
        switch (state) {
            case 3:
                if (AblConfig.isDebug()) {
                    Slog.d(TAG, "notifyStateChanged SCREEN_ON: bIsAutoBakclightAdjust=" + this.bIsAutoBakclightAdjust);
                }
                if (this.mBacklightMode != 0) {
                    setInertiaSensorEnabled(true);
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
                setInertiaSensorEnabled(false);
                return;
            case 18:
                if (this.mBacklightMode != 0) {
                    setInertiaSensorEnabled(true);
                    return;
                }
                return;
            case StateInfo.STATE_ACTION_SHUTDOWN /*27*/:
                Slog.e(TAG, "Rgb auto brightness STATE_ACTION_SHUTDOWN");
                countString = setCountIntToString();
                if (countString != null && (Events.DEFAULT_SORT_ORDER.equals(countString) ^ 1) != 0) {
                    System.putString(this.mContext.getContentResolver(), AUTOBRIGHTNESS_COUNT, countString);
                    return;
                }
                return;
            case StateInfo.STATE_ACTION_REBOOT /*28*/:
                Slog.e(TAG, "Rgb auto brightness STATE_ACTION_REBOOT");
                countString = setCountIntToString();
                if (countString != null && (Events.DEFAULT_SORT_ORDER.equals(countString) ^ 1) != 0) {
                    System.putString(this.mContext.getContentResolver(), AUTOBRIGHTNESS_COUNT, countString);
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

    public void setBacklightMode(int mode) {
        if (mode == 0 && this.mBacklightMode != mode) {
            this.mSecondSettingBrightness = 0;
            this.bIsAutoBakclightAdjust = true;
            this.mBrightnessDeta = 0.0d;
            this.bUserSettingBrightness = false;
            this.mAdjustBarHandler.removeMessages(1);
            this.mAdjustBarHandler.removeMessages(2);
            this.mAdjustBarHandler.sendEmptyMessage(2);
            setInertiaSensorEnabled(false);
        }
        this.mBacklightMode = mode;
        if (AblConfig.isDebug()) {
            Slog.d(TAG, "setBacklightMode bUserSettingBrightness = " + this.bUserSettingBrightness);
        }
    }

    public boolean isAutoBakclightAdjust() {
        return this.bIsAutoBakclightAdjust;
    }

    public void notifyPowerAssistantMode(boolean newPowerAssistantMode) {
        if (this.mPowerAssistantMode != newPowerAssistantMode) {
            this.mPowerAssistantMode = newPowerAssistantMode;
            if (this.mPowerAssistantMode) {
                Slog.e(TAG, "vivo_adjust_bar notifyPowerAssistantMode back_param");
                back_param();
                return;
            }
            Slog.e(TAG, "vivo_adjust_bar notifyPowerAssistantMode restore_param");
            this.mAdjustBarHandler.sendEmptyMessageDelayed(3, 2800);
        }
    }

    public void dump(PrintWriter pw) {
        if (AblConfig.isDebug()) {
            pw.println("  ======== RgbBrightnessCurveAlgo");
            pw.println("  mArgs = " + this.mParser.argumentToJsonString(this.mArg));
            pw.println("  autobrighness_param = " + System.getStringForUser(this.mContext.getContentResolver(), AUTOBRIGHTNESS_PARAM, -2));
            pw.println("  ========");
        }
    }

    private void setInertiaSensorEnabled(boolean enable) {
        if (!AblConfig.isUseUnderDisplayLight()) {
            this.mAdjustBarHandler.removeMessages(4);
            this.mAdjustBarHandler.removeMessages(5);
            if (enable) {
                this.mAdjustBarHandler.sendEmptyMessage(4);
            } else {
                this.mAdjustBarHandler.sendEmptyMessage(5);
            }
        }
    }

    private void setInertiaSensorEnabledInner(boolean enable) {
        if (enable) {
            if (this.mBacklightMode != 0) {
                this.mRgbCureAlgoData.clearParam();
                if (this.mPhoneStatusSensor != null) {
                    this.mSensorManager.registerListener(this.mPhoneStatusListener, this.mPhoneStatusSensor, 1, this.mInertiaHandler);
                    this.mSensorManager.registerListener(this.mStepCountListener, this.mStepCountSensor, 2, this.mInertiaHandler);
                }
            }
        } else if (this.mPhoneStatusSensor != null) {
            this.mSensorManager.unregisterListener(this.mPhoneStatusListener);
            this.mSensorManager.unregisterListener(this.mStepCountListener);
        }
    }

    public int calcBrightnessForUDFinger(int lux) {
        return calcBrightness(lux, true);
    }
}
