package com.vivo.common.autobrightness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.util.Slog;
import com.sensoroperate.VivoSensorOperationResult;
import com.sensoroperate.VivoSensorOperationUtils;
import com.vivo.common.autobrightness.CameraLumaCallback.AppBrightnessCallback;
import com.vivo.common.autobrightness.CameraLumaCallback.AppRatioUpdateLuxThreshold;
import com.vivo.common.autobrightness.CameraLumaCallback.AutoBrightnessCallback;
import com.vivo.common.autobrightness.CameraLumaCallback.ModeRestoreCallback;
import com.vivo.common.autobrightness.CameraLumaCallback.PreLightCallback;
import com.vivo.common.autobrightness.CameraLumaCallback.UnderDisplayLightCallback;
import com.vivo.common.provider.Calendar.CalendarsColumns;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.common.provider.Weather;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

public class AutoBrightnessManager {
    private static final int ALS_AFTER_ON_TIME_LIMIT = AblConfig.getFirstFewSeconds();
    private static final String AUTOBRIGHTNESS_LUX_LEVEL_USED_TIME = "autobrighness_litlvl";
    public static final int DEFAULT_ENV_SCREEN_LEVEL = 7;
    private static final String EXTRA_NETWORK_INFO = "networkInfo";
    private static final String KEY_END_TIME = "etime";
    private static final String KEY_LEVEL = "lv";
    private static final String KEY_START_TIME = "stime";
    private static final int LIGHT_SENSOR_NORMAL_DELAY = 10;
    private static final int LIGHT_SENSOR_NO_DELAY = 0;
    private static final String LUX_LEVEL_1 = "lv1";
    private static final String LUX_LEVEL_2 = "lv2";
    private static final String LUX_LEVEL_3 = "lv3";
    private static final String LUX_LEVEL_4 = "lv4";
    private static final String LUX_LEVEL_5 = "lv5";
    private static final String LUX_LEVEL_6 = "lv6";
    private static final String LUX_LEVEL_UNKNOW = "unknow";
    private static final int MSG_BRIGHTNESS_BEEN_APPLY = 6;
    private static final int MSG_CHECK_LIGHT_CALIBRATION_CORRETION = 7;
    private static final int MSG_CONTEXT_REGISTER_RECEIVER = 5;
    private static final int MSG_GET_LUX_IN_FAST_MODE = 4;
    private static final int MSG_LIGHT_SENSOR_DISABLE = 4;
    private static final int MSG_LIGHT_SENSOR_ENABLE = 3;
    private static final int MSG_LUX_LEVEL_CHANGED = 1;
    private static final int MSG_NOTIFY_BRIGHTNESS_TO_UDFINGER = 3;
    private static final int MSG_OBJECT_UNCOVER_ACTION_TIMEOUT = 1;
    private static final int MSG_USED_DAYS_CHANGED = 2;
    private static final int MSG_USER_MODIFY_SETTING_BRIGHTNESS = 2;
    private static final String NETWORK_STATE_CHANGED_ACTION = "android.net.wifi.STATE_CHANGE";
    private static final String NOTIFY_UDFINGER_BRIGHTNESS = "autobrightness_udf";
    private static final String PROP_LIGHT_CALI_CORRETION = "persist.sys.light_cali_check";
    private static final String PROP_VIVO_ROM_VERSION = "ro.vivo.rom.version";
    private static final String TAG = "AutoBrightnessManager";
    private static final int TIME_UNKNOWN = -1;
    private static final String TOTAL_DAYS = "ds";
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final String mRomVersion = SystemProperties.get(PROP_VIVO_ROM_VERSION, "rom_1.0").toLowerCase();
    private static int mScreenState = ScreenState.STATE_SCREEN_BRIGHT;
    private static final String model = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, "unkown").toLowerCase();
    private boolean bDimStatus = false;
    private boolean ignoreProxStatus = false;
    private boolean isBootCompleted = false;
    private boolean isScreenTurnOnHappen = false;
    private AutobrightInfoApplyHistory mApplyHistroy = null;
    private boolean mAutoBacklightEnabled = false;
    private long mAutoBacklightEnabledTimeStamp = -1;
    private AutoBrightnessCallback mAutoBrightnessCallback;
    private AutoBrightnessHandler mAutoBrightnessHandler;
    private HandlerThread mAutobacklightThread;
    private AutobrightInfo mAutobrightInfo = new AutobrightInfo();
    private BrightnessLevelAdjustAlgo mBriLevelAlgo = null;
    private int mBrightnessToUDFinger = -2;
    private boolean mCallProximityStatus = false;
    private CollectConfiguration mCollectConfiguration;
    private CollectDataHandler mCollectDataHandler;
    private HandlerThread mCollectDataThread;
    private CollectUseData mCollectUseData;
    private Context mContext;
    private boolean mFirstLightPersist = false;
    private boolean mHasNotifiedUDFinger = false;
    private boolean mHasObjectUncoverAction = false;
    private int mLastLightLux = -1;
    private String mLastLuxLevel = LUX_LEVEL_UNKNOW;
    private long mLastUsedDays = 0;
    private boolean mLightEventArrived = false;
    private long mLightFirstLuxTimetamp = -1;
    private Handler mLightHandler;
    private SensorEventListener mLightListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            AutoBrightnessManager.this.mLightEventArrived = true;
            float light = event.values[0];
            AutoBrightnessManager.this.mUnderDisplayDriverLux = event.values[1];
            if (event.values.length >= 9) {
                AutoBrightnessManager.this.mAutobrightInfo.mAngleX = event.values[5];
                AutoBrightnessManager.this.mAutobrightInfo.mAngleY = event.values[6];
                AutoBrightnessManager.this.mAutobrightInfo.mAngleZ = event.values[7];
                AutoBrightnessManager.this.mAutobrightInfo.mMotionState = (int) event.values[8];
            } else {
                AutoBrightnessManager.this.mAutobrightInfo.mAngleX = 0.0f;
                AutoBrightnessManager.this.mAutobrightInfo.mAngleY = 0.0f;
                AutoBrightnessManager.this.mAutobrightInfo.mAngleZ = 0.0f;
                AutoBrightnessManager.this.mAutobrightInfo.mMotionState = 0;
            }
            AutoBrightnessManager.this.log("onSensorChanged x,y,z,mo = [" + AutoBrightnessManager.this.mAutobrightInfo.mAngleX + "," + AutoBrightnessManager.this.mAutobrightInfo.mAngleY + "," + AutoBrightnessManager.this.mAutobrightInfo.mAngleZ + "," + AutoBrightnessManager.this.mAutobrightInfo.mMotionState + "]");
            AutoBrightnessManager.this.log("onSensorChanged priority = " + Thread.currentThread().getPriority());
            AutoBrightnessManager.this.handleNewLux(light);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private long mLightLuxTimestamp = -1;
    private Sensor mLightSensor;
    private String mLightSensorName = "unkown";
    private LocManager mLocManager = null;
    private Object mLock = new Object();
    private long mLuxLevelEnterTimeStamp = -1;
    private HashMap<String, String> mLuxLevelUsedTimeMap;
    private LuxMedian mLuxMedian;
    private ScreenBrightnessModeRestore mModeRestore = null;
    private int mOldScreenBrightnessModeSetting;
    private int mOrientation = -1;
    public PreLightCallback mPreLightCallback = new PreLightCallback() {
        public void onSensorChanged(SensorEvent event) {
            if (event == null) {
                AutoBrightnessManager.this.mPreLightSensorEvent = null;
                return;
            }
            AutoBrightnessManager.this.mPreLightSensorEvent = event;
            if (AutoBrightnessManager.this.mAutoBacklightEnabled && AutoBrightnessManager.this.mWaitFirstLightLux) {
                Slog.d(AutoBrightnessManager.TAG, "mPreLightCallback onSensorChanged:" + event.values[0]);
                if (AblConfig.isUseUDFingerprint()) {
                    AutoBrightnessManager.this.mLightLuxTimestamp = AutoBrightnessManager.this.mLightLuxTimestamp - 60;
                    AutoBrightnessManager.this.mLightListener.onSensorChanged(event);
                }
            }
        }

        public void notifyBrightnessToUDFinger(SensorEvent event) {
            if (event != null && (AblConfig.isUseUDFingerprint() ^ 1) == 0) {
                if (AutoBrightnessManager.this.mHasNotifiedUDFinger) {
                    Slog.d(AutoBrightnessManager.TAG, "mPreLightCallback notifyBrightnessToUDFinger has been notified");
                } else {
                    AutoBrightnessManager.this.mHasNotifiedUDFinger = true;
                    int udfbrightness = 255;
                    if (AutoBrightnessManager.this.mRgbAlgo != null) {
                        udfbrightness = AblConfig.getMapping2048GrayScaleFrom256GrayScale(AutoBrightnessManager.this.mRgbAlgo.calcBrightnessForUDFinger((int) AblConfig.getRectifiedLux(event.values[0], AutoBrightnessManager.this.mLightSensorName)));
                    } else if (AutoBrightnessManager.this.mUDLightAlgo != null) {
                        udfbrightness = AblConfig.getMapping2048GrayScaleFrom256GrayScale(AutoBrightnessManager.this.mUDLightAlgo.calcBrightnessForUDFinger((int) AblConfig.getRectifiedLux(event.values[0], AutoBrightnessManager.this.mLightSensorName)));
                    }
                    Message udfmsg = Message.obtain();
                    udfmsg.what = 3;
                    udfmsg.arg1 = udfbrightness;
                    AutoBrightnessManager.this.mCollectDataHandler.removeMessages(3);
                    AutoBrightnessManager.this.mCollectDataHandler.sendMessageAtFrontOfQueue(udfmsg);
                }
            }
        }
    };
    private SensorEvent mPreLightSensorEvent = null;
    private PreloadingLightSensor mPreLoadingLightSensor = null;
    private int mProximityChangedCount = 0;
    private Handler mProximityHandler;
    private SensorEventListener mProximityListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            float distance = event.values[0];
            boolean positive = distance >= 0.0f && distance < AutoBrightnessManager.this.mProximityThreshold;
            if (positive || !AutoBrightnessManager.this.mProximityStatus) {
                Slog.d(AutoBrightnessManager.TAG, "onSensorChanged prox: positive=" + positive);
                AutoBrightnessManager.this.mProximityHandler.removeCallbacks(AutoBrightnessManager.this.mProximityRunnable);
                AutoBrightnessManager.this.mProximityTimeStamp = SystemClock.uptimeMillis();
                AutoBrightnessManager.this.mProximityStatus = positive;
                return;
            }
            if (AutoBrightnessManager.this.mRgbAlgo != null) {
                AutoBrightnessManager.this.mRgbAlgo.setProximityPositiveToNegative(true);
            } else if (AutoBrightnessManager.this.mBriLevelAlgo != null) {
                AutoBrightnessManager.this.mBriLevelAlgo.setProximityPositiveToNegative(true);
            } else if (AutoBrightnessManager.this.mUDLightAlgo != null) {
                AutoBrightnessManager.this.mUDLightAlgo.setProximityPositiveToNegative(true);
            }
            AutoBrightnessManager.this.mProximityStatusDebounce = positive;
            AutoBrightnessManager.this.mProximityHandler.postDelayed(AutoBrightnessManager.this.mProximityRunnable, AblConfig.proximityToNegtiveDebounce());
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private Runnable mProximityRunnable = new Runnable() {
        public void run() {
            AutoBrightnessManager.this.mProximityStatus = AutoBrightnessManager.this.mProximityStatusDebounce;
        }
    };
    private Sensor mProximitySensor;
    private boolean mProximityStatus = false;
    private boolean mProximityStatusDebounce = false;
    private float mProximityThreshold;
    private long mProximityTimeStamp = -1;
    private AppRatioUpdateLuxThreshold mRatioLuxCallback = new AppRatioUpdateLuxThreshold() {
        public void onBrightnessRatioChanged(int bright) {
            if (AutoBrightnessManager.this.mRgbAlgo != null) {
                AutoBrightnessManager.this.mRgbAlgo.onAppBrightRatioChanged(bright);
            } else if (AutoBrightnessManager.this.mUDLightAlgo != null) {
                AutoBrightnessManager.this.mUDLightAlgo.onAppBrightRatioChanged(bright);
            }
        }
    };
    private float mRectifiedCoefficient = 1.0f;
    private RgbBrightnessCurveAlgo mRgbAlgo = null;
    private boolean mScreenBrightnessModeChange = false;
    private int mScreenBrightnessModeSetting;
    private long mScreenOnTime = -1;
    private SensorManager mSensorManager;
    private ShutdownRebootReceiver mShutdownRebootReceiver = new ShutdownRebootReceiver(this, null);
    private SuperPowerSaveMode mSuperPowerSaveMode = null;
    private boolean mSuperPowerSavingMode = false;
    private boolean mSuperPowerSavingModeOpen = false;
    private SuperPowerSavingReceiver mSuperPowerSavingreceiver = new SuperPowerSavingReceiver(this, null);
    private TimePeriod mTimePeriod = null;
    private UnderDisplayLightAlgo mUDLightAlgo = null;
    private float mUnderDisplayDriverLux = 0.0f;
    private ModeRestoreCallback mUserBrightnessCallback = new ModeRestoreCallback() {
        public void setSecondUserBrightness(int backlight) {
            if (AutoBrightnessManager.this.mRgbAlgo != null) {
                AutoBrightnessManager.this.mRgbAlgo.setSecondUserBrightness(backlight, AutoBrightnessManager.mScreenState);
            } else if (AutoBrightnessManager.this.mBriLevelAlgo != null) {
                AutoBrightnessManager.this.mBriLevelAlgo.setSecondUserBrightness(backlight, AutoBrightnessManager.mScreenState);
            } else if (AutoBrightnessManager.this.mUDLightAlgo != null) {
                AutoBrightnessManager.this.mUDLightAlgo.setSecondUserBrightness(backlight, AutoBrightnessManager.mScreenState);
            }
        }

        public void saveModifyRecord(JSONObject obj) {
            if (obj == null) {
                Slog.w(AutoBrightnessManager.TAG, "saveModifyRecord obj is null.");
                return;
            }
            try {
                obj.put(ModifyArgumentParser.KEY_LCM_COLOR, AutoBrightnessManager.this.mCollectUseData.mLcmColor);
                String label = CollectUseData.LABLE_USER_MODIFY_INFO;
                if (AutoBrightnessManager.this.mRgbAlgo != null) {
                    AutoBrightnessManager.this.mApplyHistroy.saveApplyToDb();
                    label = CollectUseData.LABLE_USER_MODIFY_INFO_ARG;
                    obj = AutoBrightnessManager.this.mRgbAlgo.saveModifyRecord(obj);
                } else if (AutoBrightnessManager.this.mUDLightAlgo != null) {
                    AutoBrightnessManager.this.mApplyHistroy.saveApplyToDb();
                    label = CollectUseData.LABLE_USER_MODIFY_INFO_ARG;
                    obj = AutoBrightnessManager.this.mUDLightAlgo.saveModifyRecord(obj);
                }
                if (obj != null) {
                    HashMap<String, String> map = new HashMap(3);
                    map.put("record", obj.toString());
                    AutoBrightnessManager.this.mCollectUseData.sendDataParameter(new DataParameter(CollectUseData.EVENTID_AUTOBRIGHTNESS, label, System.currentTimeMillis(), -1, 0, 1, map));
                }
            } catch (JSONException e) {
                Slog.e(AutoBrightnessManager.TAG, "put lcm color got excetpion.", e);
            }
        }
    };
    private VivoSensorOperationUtils mVivoSensorOperationUtils;
    private boolean mWaitFirstLightLux = false;
    private boolean mWasHasObjectUncoverAction = false;
    private WifiInfoManager mWifiInfoManager = null;
    private WifiStatusReceiver mWifiStatusReceiver = new WifiStatusReceiver(this, null);
    private boolean shouldIgnoreProxmityWhenFirst = false;

    private class AutoBrightnessHandler extends Handler {
        public AutoBrightnessHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            AutoBrightnessManager.this.log("handleMessage msg.what=" + AutoBrightnessManager.msgToString(msg.what));
            Object -get14;
            switch (msg.what) {
                case 1:
                    AutoBrightnessManager.this.mHasObjectUncoverAction = false;
                    AutoBrightnessManager.this.log("mHasObjectUncoverAction set to false because TIMEOUT");
                    long t = SystemClock.uptimeMillis() - AutoBrightnessManager.this.mAutoBacklightEnabledTimeStamp;
                    if (AutoBrightnessManager.this.mAutoBacklightEnabledTimeStamp > 0 && t > 0 && ((double) t) < ((double) AblConfig.getObjectUncoverActionTimeout()) * 1.5d) {
                        AutoBrightnessManager.this.mWasHasObjectUncoverAction = true;
                        return;
                    }
                    return;
                case 2:
                    if (msg.obj != null) {
                        DataParameter data = msg.obj;
                        AutoBrightnessManager.this.log("notifyScreenBrightness collectScreenBrightness:" + data.toString());
                        AutoBrightnessManager.this.mCollectUseData.sendDataParameter(data);
                        return;
                    }
                    return;
                case 3:
                    -get14 = AutoBrightnessManager.this.mLock;
                    synchronized (-get14) {
                        Slog.d(AutoBrightnessManager.TAG, "MSG_LIGHT_SENSOR_ENABLE mLock acquire" + Thread.currentThread().getPriority());
                        Thread.currentThread().setPriority(10);
                        AutoBrightnessManager.this.setLightSensorEnabledInner(true);
                        break;
                    }
                case 4:
                    -get14 = AutoBrightnessManager.this.mLock;
                    synchronized (-get14) {
                        AutoBrightnessManager.this.setLightSensorEnabledInner(false);
                        break;
                    }
                case 5:
                    -get14 = AutoBrightnessManager.this.mLock;
                    synchronized (-get14) {
                        Slog.d(AutoBrightnessManager.TAG, "start registerReceiver...");
                        AutoBrightnessManager.this.mContext.registerReceiver(AutoBrightnessManager.this.mShutdownRebootReceiver, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"));
                        AutoBrightnessManager.this.mContext.registerReceiver(AutoBrightnessManager.this.mShutdownRebootReceiver, new IntentFilter("android.intent.action.REBOOT"));
                        AutoBrightnessManager.this.mContext.registerReceiver(AutoBrightnessManager.this.mShutdownRebootReceiver, new IntentFilter("android.intent.action.BOOT_COMPLETED"));
                        AutoBrightnessManager.this.mContext.registerReceiver(AutoBrightnessManager.this.mShutdownRebootReceiver, new IntentFilter("android.intent.action.CONFIGURATION_CHANGED"));
                        AutoBrightnessManager.this.mContext.registerReceiver(AutoBrightnessManager.this.mWifiStatusReceiver, new IntentFilter(AutoBrightnessManager.NETWORK_STATE_CHANGED_ACTION));
                        AutoBrightnessManager.this.registerLogBroadcast(AutoBrightnessManager.this.mContext);
                        float romVer = 2.0f;
                        String version = AutoBrightnessManager.this.getSystemPropertiesValue(AutoBrightnessManager.PROP_VIVO_ROM_VERSION, "rom_2.0");
                        try {
                            String[] tmpVer = version.split("_");
                            if (tmpVer.length >= 2) {
                                romVer = Float.parseFloat(tmpVer[1]);
                            }
                        } catch (NumberFormatException e) {
                            Slog.d(AutoBrightnessManager.TAG, "AutoBrightnessHandler Failed to get rom version: " + version);
                        }
                        if (romVer < 2.5f) {
                            AutoBrightnessManager.this.mContext.registerReceiver(AutoBrightnessManager.this.mSuperPowerSavingreceiver, new IntentFilter("intent.action.super_power_save"));
                            break;
                        }
                        AutoBrightnessManager.this.mContext.registerReceiver(AutoBrightnessManager.this.mSuperPowerSavingreceiver, new IntentFilter("intent.action.super_power_save_send"));
                        break;
                    }
                case 6:
                    -get14 = AutoBrightnessManager.this.mLock;
                    synchronized (-get14) {
                        AutobrightInfo newAutobrightInfo = new AutobrightInfo();
                        newAutobrightInfo = msg.obj;
                        if (AblConfig.isDebug()) {
                            Slog.d(AutoBrightnessManager.TAG, "brightness:" + newAutobrightInfo.mBrightness + " screenLevel=" + newAutobrightInfo.mScreenLevel + " delay=" + newAutobrightInfo.mDelayTime);
                        }
                        AutoBrightnessManager.this.mAutobrightInfo.copyFrom(newAutobrightInfo);
                        if (!AutoBrightnessManager.this.mSuperPowerSavingMode) {
                            if (AutoBrightnessManager.this.mRgbAlgo == null) {
                                if (AutoBrightnessManager.this.mBriLevelAlgo == null) {
                                    if (AutoBrightnessManager.this.mUDLightAlgo != null) {
                                        AutoBrightnessManager.this.mUDLightAlgo.brightnessBeenApplied(newAutobrightInfo);
                                        if (AblConfig.isCollectAutobrightApplyHistory() && AutoBrightnessManager.this.mUDLightAlgo.isAutoBakclightAdjust()) {
                                            if (AutoBrightnessManager.this.mModeRestore != null) {
                                                newAutobrightInfo.mForegroundPkg = AutoBrightnessManager.this.mModeRestore.getForegroundAppName();
                                            }
                                            AutoBrightnessManager.this.mApplyHistroy.onNewInfoApplied(newAutobrightInfo);
                                            AutoBrightnessManager.this.mAutobrightInfo.copyFrom(newAutobrightInfo);
                                            break;
                                        }
                                    }
                                }
                                AutoBrightnessManager.this.mBriLevelAlgo.brightnessBeenApplied(newAutobrightInfo);
                                break;
                            }
                            AutoBrightnessManager.this.mRgbAlgo.brightnessBeenApplied(newAutobrightInfo);
                            if (AblConfig.isCollectAutobrightApplyHistory() && AutoBrightnessManager.this.mRgbAlgo.isAutoBakclightAdjust()) {
                                if (AutoBrightnessManager.this.mModeRestore != null) {
                                    newAutobrightInfo.mForegroundPkg = AutoBrightnessManager.this.mModeRestore.getForegroundAppName();
                                }
                                AutoBrightnessManager.this.mApplyHistroy.onNewInfoApplied(newAutobrightInfo);
                                AutoBrightnessManager.this.mAutobrightInfo.copyFrom(newAutobrightInfo);
                                break;
                            }
                        }
                        AutoBrightnessManager.this.mSuperPowerSaveMode.brightnessBeenApplied(newAutobrightInfo);
                        break;
                    }
                    break;
                case 7:
                    try {
                        LightSensorCalibrationCorrection mLightSensorCalibrationCorrection = new LightSensorCalibrationCorrection();
                        String mIMEI = ((TelephonyManager) AutoBrightnessManager.this.mContext.getSystemService("phone")).getImei();
                        Slog.d(AutoBrightnessManager.TAG, "ALS_UPDATA_NV_CALIBRATION mIMEI:" + mIMEI);
                        if (mLightSensorCalibrationCorrection.isNeedCalibrationCorrection(mIMEI)) {
                            int lightThreshold = mLightSensorCalibrationCorrection.getCalibrationCorrection(mIMEI);
                            int currentLightThreshold = Integer.parseInt(SystemProperties.get("persist.sys.light_threshold", "500"));
                            Slog.d(AutoBrightnessManager.TAG, "ALS_UPDATA_NV_CALIBRATION:" + lightThreshold + " <- " + currentLightThreshold);
                            if (lightThreshold != currentLightThreshold) {
                                String rsp = new NVItemSocketClient().sendMessage("light_sensor_data " + lightThreshold);
                                Slog.d(AutoBrightnessManager.TAG, "ALS_UPDATA_NV_CALIBRATION: rsp " + rsp + " val: " + lightThreshold);
                                if ("ok".equals(rsp)) {
                                    AutoBrightnessManager.this.setSystemPropertiesValue("persist.sys.light_threshold", lightThreshold + Events.DEFAULT_SORT_ORDER);
                                    AutoBrightnessManager.this.setSystemPropertiesValue(AutoBrightnessManager.PROP_LIGHT_CALI_CORRETION, "yes");
                                }
                            } else {
                                AutoBrightnessManager.this.setSystemPropertiesValue(AutoBrightnessManager.PROP_LIGHT_CALI_CORRETION, "yes");
                            }
                        } else {
                            AutoBrightnessManager.this.setSystemPropertiesValue(AutoBrightnessManager.PROP_LIGHT_CALI_CORRETION, "yes");
                        }
                        mLightSensorCalibrationCorrection.clearCalibrationCorrection();
                        return;
                    } catch (Exception e2) {
                        Slog.e(AutoBrightnessManager.TAG, "Fail to do calibration correction check");
                        return;
                    }
                default:
                    return;
            }
        }
    }

    private class CollectDataHandler extends Handler {
        public CollectDataHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            AutoBrightnessManager.this.log("handleMessage collect data msg.what=" + msg.what);
            switch (msg.what) {
                case 1:
                    long usedtime;
                    String jSONObject;
                    Bundle bundle = msg.getData();
                    long start = bundle.getLong(AutoBrightnessManager.KEY_START_TIME);
                    long end = bundle.getLong(AutoBrightnessManager.KEY_END_TIME);
                    String level = bundle.getString(AutoBrightnessManager.KEY_LEVEL);
                    try {
                        usedtime = Long.parseLong((String) AutoBrightnessManager.this.mLuxLevelUsedTimeMap.get(level));
                    } catch (Exception e) {
                        Slog.e(AutoBrightnessManager.TAG, "error to parseLong of KEY_LEVEL");
                        usedtime = -1;
                    }
                    AutoBrightnessManager.this.mLuxLevelUsedTimeMap.put(level, String.valueOf((usedtime + end) - start));
                    long days = AutoBrightnessManager.this.mTimePeriod.getDays();
                    if (days > AutoBrightnessManager.this.mLastUsedDays) {
                        long useddays;
                        try {
                            useddays = Long.parseLong((String) AutoBrightnessManager.this.mLuxLevelUsedTimeMap.get(AutoBrightnessManager.TOTAL_DAYS));
                        } catch (Exception e2) {
                            Slog.e(AutoBrightnessManager.TAG, "error to parseLong of TOTAL_DAYS");
                            useddays = 0;
                        }
                        AutoBrightnessManager.this.mLuxLevelUsedTimeMap.put(AutoBrightnessManager.TOTAL_DAYS, String.valueOf((useddays + days) - AutoBrightnessManager.this.mLastUsedDays));
                        AutoBrightnessManager.this.mLastUsedDays = days;
                        Message cmsg = Message.obtain();
                        cmsg.what = 2;
                        AutoBrightnessManager.this.mCollectDataHandler.removeMessages(2);
                        AutoBrightnessManager.this.mCollectDataHandler.sendMessageDelayed(cmsg, 500);
                    }
                    AutoBrightnessManager.this.log("hashmap = " + (AutoBrightnessManager.this.mLuxLevelUsedTimeMap != null ? AutoBrightnessManager.this.mLuxLevelUsedTimeMap.toString() : "null"));
                    JSONObject jSONObject2 = new JSONObject(AutoBrightnessManager.this.mLuxLevelUsedTimeMap);
                    AutoBrightnessManager autoBrightnessManager = AutoBrightnessManager.this;
                    StringBuilder append = new StringBuilder().append("jsonobject = ");
                    if (jSONObject2 != null) {
                        jSONObject = jSONObject2.toString();
                    } else {
                        jSONObject = "null";
                    }
                    autoBrightnessManager.log(append.append(jSONObject).toString());
                    return;
                case 2:
                    long current = SystemClock.uptimeMillis();
                    AutoBrightnessManager.this.collectUseDuration(current, current, AutoBrightnessManager.this.mLuxLevelUsedTimeMap);
                    return;
                case 3:
                    int udfbrightness = msg.arg1;
                    if (udfbrightness == AutoBrightnessManager.this.mBrightnessToUDFinger) {
                        Slog.d(AutoBrightnessManager.TAG, "notifyBrightnessToUDFinger the same, no need to set brightness to " + udfbrightness);
                        return;
                    } else if (System.putInt(AutoBrightnessManager.this.mContext.getContentResolver(), AutoBrightnessManager.NOTIFY_UDFINGER_BRIGHTNESS, udfbrightness)) {
                        Slog.d(AutoBrightnessManager.TAG, "notifyBrightnessToUDFinger, set brightness to " + udfbrightness);
                        AutoBrightnessManager.this.mBrightnessToUDFinger = udfbrightness;
                        return;
                    } else {
                        Slog.d(AutoBrightnessManager.TAG, "notifyBrightnessToUDFinger mBrightnessToUDFinger = " + AutoBrightnessManager.this.mBrightnessToUDFinger + ", failed to set brightness to " + udfbrightness);
                        return;
                    }
                case 4:
                    AutoBrightnessManager.this.deliverLuxFromFastMode();
                    return;
                default:
                    return;
            }
        }
    }

    private final class ShutdownRebootReceiver extends BroadcastReceiver {
        /* synthetic */ ShutdownRebootReceiver(AutoBrightnessManager this$0, ShutdownRebootReceiver -this1) {
            this();
        }

        private ShutdownRebootReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            boolean z = true;
            if (intent != null) {
                String action = intent.getAction();
                if ("android.intent.action.REBOOT".equals(action)) {
                    AutoBrightnessManager.this.notifyStateChanged(28);
                } else if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                    AutoBrightnessManager.this.notifyStateChanged(27);
                } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    AutoBrightnessManager.this.notifyStateChanged(18);
                } else if ("android.intent.action.CONFIGURATION_CHANGED".equals(action)) {
                    int orientation;
                    if (AutoBrightnessManager.this.mContext.getResources().getConfiguration().orientation == 2) {
                        AutoBrightnessManager.this.log("ori LANDSCAPE");
                        orientation = 0;
                    } else {
                        AutoBrightnessManager.this.log("ori PORTRAIT");
                        orientation = 1;
                    }
                    if (AutoBrightnessManager.this.mOrientation != orientation) {
                        if (AutoBrightnessManager.this.mUDLightAlgo != null) {
                            boolean z2;
                            UnderDisplayLightAlgo -get30 = AutoBrightnessManager.this.mUDLightAlgo;
                            if (1 == orientation) {
                                z2 = true;
                            } else {
                                z2 = false;
                            }
                            -get30.setOrientation(z2);
                        }
                        ScreenBrightnessModeRestore -get16 = AutoBrightnessManager.this.mModeRestore;
                        if (1 != orientation) {
                            z = false;
                        }
                        -get16.setOrientation(z);
                        AutoBrightnessManager.this.mOrientation = orientation;
                    }
                } else {
                    AutoBrightnessManager.this.log("unkown action:" + action);
                }
            }
        }
    }

    private final class SuperPowerSavingReceiver extends BroadcastReceiver {
        /* synthetic */ SuperPowerSavingReceiver(AutoBrightnessManager this$0, SuperPowerSavingReceiver -this1) {
            this();
        }

        private SuperPowerSavingReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action.equals("intent.action.super_power_save")) {
                    if (AblConfig.isDebug()) {
                        Slog.d(AutoBrightnessManager.TAG, "getExtra(sps_action)=" + intent.getExtra("sps_action"));
                    }
                    if (intent.getExtra("sps_action").equals("start")) {
                        if (AblConfig.isDebug()) {
                            Slog.d(AutoBrightnessManager.TAG, "switchOnSuperPowerSaving(true)");
                        }
                        AutoBrightnessManager.this.notifyStateChanged(7);
                    } else {
                        if (AblConfig.isDebug()) {
                            Slog.d(AutoBrightnessManager.TAG, "switchOnSuperPowerSaving(false)");
                        }
                        AutoBrightnessManager.this.notifyStateChanged(6);
                    }
                } else if (action.equals("intent.action.super_power_save_send")) {
                    if (intent.getExtra("sps_action").equals("entered")) {
                        AutoBrightnessManager.this.log("switchOnSuperPowerSaving(true)");
                        AutoBrightnessManager.this.notifyStateChanged(7);
                    } else {
                        AutoBrightnessManager.this.log("switchOnSuperPowerSaving(false)");
                        AutoBrightnessManager.this.notifyStateChanged(6);
                    }
                }
            }
        }
    }

    private final class WifiStatusReceiver extends BroadcastReceiver {
        /* synthetic */ WifiStatusReceiver(AutoBrightnessManager this$0, WifiStatusReceiver -this1) {
            this();
        }

        private WifiStatusReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action.equals(AutoBrightnessManager.NETWORK_STATE_CHANGED_ACTION)) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(AutoBrightnessManager.EXTRA_NETWORK_INFO);
                    if (networkInfo == null || networkInfo.getDetailedState() != DetailedState.CONNECTED) {
                        AutoBrightnessManager.this.mAutobrightInfo.mWifiStatus = false;
                    } else {
                        AutoBrightnessManager.this.mAutobrightInfo.mWifiStatus = true;
                    }
                } else {
                    Slog.e(AutoBrightnessManager.TAG, "WifiStatusReceiver unkown action:" + action);
                }
                Slog.e(AutoBrightnessManager.TAG, "WifiStatusReceiver status = " + AutoBrightnessManager.this.mAutobrightInfo.mWifiStatus);
            }
        }
    }

    public void dump(PrintWriter pw) {
        boolean z;
        if (AblConfig.isDebug()) {
            z = true;
        } else {
            z = getSystemPropertiesValue("ro.build.type", "unkown").equals("eng");
        }
        if (z) {
            pw.println();
            pw.println("===========================");
            pw.println("AutoBrightnessManager State:");
            pw.println("  mScreenAutoBrightness:" + this.mAutoBrightnessCallback.getCurrentAutoBrightness() + ";");
            pw.println("  mAutobrightInfo:" + this.mAutobrightInfo.toString());
            pw.println();
            pw.println();
            pw.println("  mLuxMedian:" + this.mLuxMedian.toString());
            pw.println("  AutoBrigtnessConfiguration:");
            this.mCollectConfiguration.dump(pw);
            pw.println("===========================");
            if (this.mRgbAlgo != null) {
                this.mRgbAlgo.dump(pw);
            }
            if (this.mUDLightAlgo != null) {
                this.mUDLightAlgo.dump(pw);
            }
            pw.println();
            this.mModeRestore.dump(pw);
        }
    }

    private static String msgToString(int what) {
        switch (what) {
            case 1:
                return "MSG_OBJECT_UNCOVER_ACTION_TIMEOUT";
            case 2:
                return "MSG_USER_MODIFY_SETTING_BRIGHTNESS";
            case 3:
                return "MSG_LIGHT_SENSOR_ENABLE";
            case 4:
                return "MSG_LIGHT_SENSOR_ENABLE";
            case 5:
                return "MSG_CONTEXT_REGISTER_RECEIVER";
            case 6:
                return "MSG_BRIGHTNESS_BEEN_APPLY";
            case 7:
                return "MSG_CHECK_LIGHT_CALIBRATION_CORRETION";
            default:
                return "unknown";
        }
    }

    private float getAlsRawdata() {
        VivoSensorOperationResult operationRes = new VivoSensorOperationResult();
        int[] mOperationArgs = new int[]{-1.0f, ExceptionCode.FORCE_UPDATE, 111};
        if (this.mVivoSensorOperationUtils != null) {
            try {
                this.mVivoSensorOperationUtils.executeCommand(mOperationArgs[0], operationRes, mOperationArgs, mOperationArgs.length);
            } catch (Exception e) {
                Slog.e(TAG, "Fail to get als data");
                return -1.0f;
            }
        }
        if (operationRes.mSuccess == 0) {
            operationRes.mTestVal[0] = -1.0f;
        }
        Slog.d(TAG, "Get als data : " + operationRes.mTestVal[0]);
        return operationRes.mTestVal[0];
    }

    private void deliverLuxFromFastMode() {
        log("deliverLuxFromFastMode");
        float fastLux = getAlsRawdata();
        if (((double) fastLux) != -1.0d && this.mAutoBacklightEnabled && this.mWaitFirstLightLux && this.isScreenTurnOnHappen) {
            try {
                this.mLightLuxTimestamp -= 60;
                handleNewLux(fastLux);
            } catch (Exception e) {
                Slog.e(TAG, "setScreenOnBacklihgt invoke LightSensor onSensorChanged Fail");
            }
            return;
        }
        Slog.e(TAG, "deliverLuxFromFastMode fail, fastLux = " + fastLux + ", mAutoBacklightEnabled = " + this.mAutoBacklightEnabled + ", mWaitFirstLightLux = " + this.mWaitFirstLightLux + ", mLightEventArrived = " + this.mLightEventArrived);
    }

    private void handleNewLux(float light) {
        AutobrightInfo tempAbInfo = new AutobrightInfo();
        if (this.shouldIgnoreProxmityWhenFirst && this.isScreenTurnOnHappen && this.mWaitFirstLightLux) {
            Slog.d(TAG, "no need to care the proximity status when first light after rom 3.2");
            this.ignoreProxStatus = true;
        }
        if (AblConfig.isUseUnderDisplayLight()) {
            this.mAutobrightInfo.mUnderDispalyRecitfiedLux = light;
            this.mAutobrightInfo.mUnderDispalyDriverLux = this.mUnderDisplayDriverLux;
        }
        log("handleNewLux priority = " + Thread.currentThread().getPriority());
        float rectifiedLux = AblConfig.getRectifiedLux(light, this.mLightSensorName);
        if (this.mRgbAlgo != null) {
            if (!this.mRgbAlgo.isAutoBakclightAdjust()) {
                log("mRgbAlgo bIsAutoBakclightAdjust is false");
                return;
            }
        } else if (this.mBriLevelAlgo != null) {
            if (!this.mBriLevelAlgo.isAutoBakclightAdjust()) {
                log("mBrieLevelAlgo bIsAutoBakclightAdjust is false");
                return;
            }
        } else if (!(this.mUDLightAlgo == null || this.mUDLightAlgo.isAutoBakclightAdjust())) {
            log("mUDlightAlgo bIsAutoBakclightAdjust is false");
            return;
        }
        if (isLuxValid(SystemClock.uptimeMillis())) {
            int lux = this.mLuxMedian.putAndGetLightMedian((int) rectifiedLux);
            this.mLastLightLux = lux;
            String luxLevel = LUX_LEVEL_UNKNOW;
            if (lux >= 0 && lux <= 5) {
                luxLevel = LUX_LEVEL_1;
            } else if (5 < lux && lux <= 30) {
                luxLevel = LUX_LEVEL_2;
            } else if (30 < lux && lux <= CalendarsColumns.EDITOR_ACCESS) {
                luxLevel = LUX_LEVEL_3;
            } else if (CalendarsColumns.EDITOR_ACCESS < lux && lux <= Weather.WEATHERVERSION_ROM_2_5_1) {
                luxLevel = LUX_LEVEL_4;
            } else if (Weather.WEATHERVERSION_ROM_2_5_1 < lux && lux <= 20000) {
                luxLevel = LUX_LEVEL_5;
            } else if (20000 < lux) {
                luxLevel = LUX_LEVEL_6;
            }
            log("0ops lux = " + lux + ", mLastLuxLevel = " + this.mLastLuxLevel + ", luxLevel = " + luxLevel + ", mWaitFirstLightLux = " + this.mWaitFirstLightLux);
            if (!this.mLastLuxLevel.equals(luxLevel)) {
                if (!this.mWaitFirstLightLux && this.isBootCompleted) {
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_LEVEL, this.mLastLuxLevel);
                    bundle.putLong(KEY_START_TIME, this.mLuxLevelEnterTimeStamp);
                    bundle.putLong(KEY_END_TIME, SystemClock.uptimeMillis());
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.setData(bundle);
                    this.mCollectDataHandler.removeMessages(1);
                    this.mCollectDataHandler.sendMessageDelayed(msg, 50);
                }
                this.mLastLuxLevel = luxLevel;
                this.mLuxLevelEnterTimeStamp = SystemClock.uptimeMillis();
            }
            if (this.mHasObjectUncoverAction) {
                long t = SystemClock.uptimeMillis() - this.mAutoBacklightEnabledTimeStamp;
                if (this.mAutoBacklightEnabledTimeStamp > 0 && t >= 0 && ((double) t) < ((double) AblConfig.getObjectUncoverActionTimeout()) * 1.5d) {
                    if (this.mLuxMedian.getUsedLen() < 2) {
                        log("light onSensorChagned mHasObjectUncoverAction && usedLen=" + this.mLuxMedian.getUsedLen() + " return");
                        return;
                    }
                    log("light onSensorChagned mHasObjectUncoverAction && usedLen=" + this.mLuxMedian.getUsedLen() + " go get max");
                    this.mAutoBrightnessHandler.removeMessages(1);
                    this.mHasObjectUncoverAction = false;
                    this.mWasHasObjectUncoverAction = true;
                }
            }
            if (this.mWasHasObjectUncoverAction) {
                this.mWasHasObjectUncoverAction = false;
                lux = (int) this.mLuxMedian.getMax();
                log("light onSensorChagned got max lux=" + lux);
                this.mLastLightLux = lux;
            }
            synchronized (this.mLock) {
                tempAbInfo.copyFrom(this.mAutobrightInfo);
            }
            if (this.mSuperPowerSavingMode && this.mSuperPowerSaveMode != null) {
                tempAbInfo = this.mSuperPowerSaveMode.getAutoBrightness(this.mWaitFirstLightLux, this.mProximityStatus, lux, this.mAutobrightInfo);
            } else if (this.mRgbAlgo != null) {
                tempAbInfo = this.mRgbAlgo.getAutoBrightness(this.mWaitFirstLightLux, this.mProximityStatus, lux, this.mAutobrightInfo, this.mModeRestore.getVideoGameFlag());
            } else if (this.mBriLevelAlgo != null) {
                tempAbInfo = this.mBriLevelAlgo.getAutoBrightness(this.mWaitFirstLightLux, this.mProximityStatus, lux, this.mAutobrightInfo);
            } else if (this.mUDLightAlgo != null) {
                tempAbInfo = this.mUDLightAlgo.getAutoBrightness(this.mWaitFirstLightLux, this.mProximityStatus, lux, this.mAutobrightInfo, this.mModeRestore.getVideoGameFlag(), this.mFirstLightPersist);
            }
            if (this.mWaitFirstLightLux) {
                String str;
                Slog.d(TAG, "onSensorChanged light: first lux tempAbInfo:" + (tempAbInfo == null ? "null" : tempAbInfo.toString()));
                String str2 = TAG;
                StringBuilder append = new StringBuilder().append("onSensorChanged light: first lux mAutobrightInfo:");
                if (this.mAutobrightInfo == null) {
                    str = "null";
                } else {
                    str = this.mAutobrightInfo.toString();
                }
                Slog.d(str2, append.append(str).toString());
            }
            if (!this.mWaitFirstLightLux && 10 == Thread.currentThread().getPriority()) {
                Thread.currentThread().setPriority(5);
            }
            if (SystemClock.uptimeMillis() - this.mLightFirstLuxTimetamp < 30) {
                Slog.d(TAG, "onSensorChanged light: mLightFirstLuxTimetamp drop");
                return;
            }
            if (this.mWaitFirstLightLux && mScreenState == ScreenState.STATE_SCREEN_BRIGHT) {
                if (AblConfig.isUseUnderDisplayLight() && (this.isScreenTurnOnHappen ^ 1) != 0 && this.mScreenBrightnessModeChange) {
                    this.mFirstLightPersist = true;
                }
                this.mWaitFirstLightLux = false;
                this.isScreenTurnOnHappen = false;
                this.mScreenBrightnessModeChange = false;
                tempAbInfo.mDelayTime = 0;
                this.mLightFirstLuxTimetamp = SystemClock.uptimeMillis();
            } else {
                tempAbInfo.mDelayTime = 10;
            }
            if (AblConfig.isUseUnderDisplayLight() && this.mFirstLightPersist) {
                tempAbInfo.mDelayTime = 0;
            }
            if (AblConfig.isUseUnderDisplayLight() && this.mFirstLightPersist && SystemClock.uptimeMillis() - this.mLightFirstLuxTimetamp > 5000) {
                this.mFirstLightPersist = false;
            }
            tempAbInfo.mProximity = this.mProximityStatus;
            int proximityChanged = this.mAutobrightInfo.mProximity ? tempAbInfo.mProximity ^ 1 : 0;
            synchronized (this.mLock) {
                this.mAutobrightInfo.copyFrom(tempAbInfo);
            }
            if (proximityChanged != 0 && this.mAutobrightInfo.mDelayTime > 0) {
                if (this.mAutobrightInfo.mBrightness < this.mAutoBrightnessCallback.getCurrentAutoBrightness() && this.mProximityChangedCount < 5) {
                    this.mProximityChangedCount++;
                }
                AutobrightInfo autobrightInfo = this.mAutobrightInfo;
                autobrightInfo.mDelayTime -= this.mProximityChangedCount;
            }
            if (mScreenState == ScreenState.STATE_SCREEN_BRIGHT && (!this.mProximityStatus || this.ignoreProxStatus || ((this.mProximityStatus && this.mAutobrightInfo.mBrightness > this.mAutoBrightnessCallback.getCurrentAutoBrightness()) || (this.mAutobrightInfo.mUnderDisplayThreshChanged && AblConfig.isUseUnderDisplayLight())))) {
                this.mAutoBrightnessCallback.onNewScreenValue(this.mAutobrightInfo);
                this.mAutobrightInfo.mUnderDisplayThreshChanged = false;
            }
            return;
        }
        log("isLuxValid false");
    }

    private void registerLogBroadcast(Context mContext) {
        IntentFilter bbklogFilter = new IntentFilter();
        bbklogFilter.addAction("android.vivo.bbklog.action.CHANGED");
        mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean status = "on".equals(intent.getStringExtra("adblog_status"));
                AblConfig.setBbkLog(status);
                Slog.w(AutoBrightnessManager.TAG, "***** registerLogBroadcast SWITCH LOG TO " + status);
            }
        }, bbklogFilter, null, this.mAutoBrightnessHandler);
    }

    public AutoBrightnessManager(Context context, SensorManager sensorManager, AutoBrightnessCallback callback) {
        boolean z = false;
        this.mContext = context;
        this.mCollectConfiguration = CollectConfiguration.getInstance();
        this.mAutobacklightThread = new HandlerThread("AutobacklightThread");
        this.mCollectDataThread = new HandlerThread("AutobacklightCollectThread");
        this.mAutobacklightThread.start();
        this.mCollectDataThread.start();
        this.mProximityHandler = new Handler(this.mAutobacklightThread.getLooper());
        this.mLightHandler = new Handler(this.mAutobacklightThread.getLooper());
        this.mAutoBrightnessHandler = new AutoBrightnessHandler(this.mAutobacklightThread.getLooper());
        this.mCollectDataHandler = new CollectDataHandler(this.mCollectDataThread.getLooper());
        this.mLuxMedian = new LuxMedian();
        this.mVivoSensorOperationUtils = VivoSensorOperationUtils.getInstance();
        this.mLocManager = LocManager.getInstance(this.mContext);
        if (this.mLocManager != null) {
            this.mLocManager.createJobber();
        }
        this.mWifiInfoManager = WifiInfoManager.getInstance(this.mContext);
        this.mCollectUseData = CollectUseData.getInstance(context, this.mAutobacklightThread.getLooper());
        this.mAutoBrightnessCallback = callback;
        this.mSensorManager = sensorManager;
        this.mProximitySensor = this.mSensorManager.getDefaultSensor(8);
        this.mPreLoadingLightSensor = new PreloadingLightSensor(this.mSensorManager, this.mPreLightCallback, this.mAutobacklightThread.getLooper());
        this.mModeRestore = ScreenBrightnessModeRestore.getInstance(this.mContext, this.mAutobacklightThread.getLooper());
        this.mModeRestore.setUserBrightnessCallback(this.mUserBrightnessCallback);
        this.mModeRestore.setAppRatioUpdateLuxThreshold(this.mRatioLuxCallback);
        if (AblConfig.isUseBrightnessLevel()) {
            this.mBriLevelAlgo = new BrightnessLevelAdjustAlgo(this.mContext, this.mSensorManager, this.mAutobacklightThread.getLooper());
        } else if (AblConfig.isUseUnderDisplayLight()) {
            this.mUDLightAlgo = new UnderDisplayLightAlgo(this.mContext, this.mSensorManager, this.mAutobacklightThread.getLooper());
        } else {
            this.mRgbAlgo = new RgbBrightnessCurveAlgo(this.mContext, this.mSensorManager, this.mAutobacklightThread.getLooper());
        }
        this.mSuperPowerSaveMode = new SuperPowerSaveMode(this.mContext, this.mSensorManager, this.mAutobacklightThread.getLooper());
        this.mApplyHistroy = new AutobrightInfoApplyHistory(this.mContext, this.mAutobacklightThread.getLooper());
        this.mTimePeriod = TimePeriod.getInstance(this.mContext, this.mAutobacklightThread.getLooper());
        if (this.mProximitySensor == null) {
            Slog.e(TAG, "AutoBrightnessFATAL:mProximitySensor is null!");
            this.mProximityThreshold = Math.min(1.0f, TYPICAL_PROXIMITY_THRESHOLD);
        } else {
            this.mProximityThreshold = Math.min(this.mProximitySensor.getMaximumRange(), TYPICAL_PROXIMITY_THRESHOLD);
        }
        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
        if (this.mLightSensor == null) {
            Slog.e(TAG, "AutoBrightnessFATAL:mLightSensor is null");
        }
        if (this.mLightSensor != null) {
            String name = this.mLightSensor.getName();
            if (name != null) {
                this.mLightSensorName = name;
            }
        }
        if (mRomVersion.compareTo("rom_3.2") >= 0) {
            z = true;
        }
        this.shouldIgnoreProxmityWhenFirst = z;
        this.mRectifiedCoefficient = AblConfig.getRectifiedCoefficient(this.mLightSensorName);
        this.mAutoBrightnessHandler.removeMessages(5);
        this.mAutoBrightnessHandler.sendEmptyMessage(5);
        log("AutoBrightnessManager constructer finished");
        this.mLuxLevelUsedTimeMap = new HashMap();
        this.mLuxLevelUsedTimeMap.put(LUX_LEVEL_1, "0");
        this.mLuxLevelUsedTimeMap.put(LUX_LEVEL_2, "0");
        this.mLuxLevelUsedTimeMap.put(LUX_LEVEL_3, "0");
        this.mLuxLevelUsedTimeMap.put(LUX_LEVEL_4, "0");
        this.mLuxLevelUsedTimeMap.put(LUX_LEVEL_5, "0");
        this.mLuxLevelUsedTimeMap.put(LUX_LEVEL_6, "0");
        this.mLuxLevelUsedTimeMap.put(TOTAL_DAYS, "0");
    }

    private void log(String msg) {
        if (AblConfig.isDebug()) {
            Slog.d(TAG, msg);
        }
    }

    public boolean isLuxValid(int auto, int waiting) {
        boolean valid;
        if (this.mAutoBacklightEnabled && mScreenState == ScreenState.STATE_SCREEN_BRIGHT) {
            valid = true;
        } else {
            valid = this.ignoreProxStatus;
        }
        if (this.mProximityStatus && valid && (this.ignoreProxStatus ^ 1) != 0 && auto > waiting) {
            valid = false;
        }
        if (this.ignoreProxStatus) {
            this.ignoreProxStatus = false;
        }
        log("isLuxValid()=" + valid + " lightEn=" + this.mAutoBacklightEnabled + " prox=" + this.mProximityStatus + " auto=" + auto + " waiting=" + waiting);
        return valid;
    }

    private boolean isLuxValid(long time) {
        boolean valid = this.mAutoBacklightEnabled ? time - this.mLightLuxTimestamp >= 30 : false;
        log("isLuxValid(time)=" + valid);
        return valid;
    }

    public boolean setLightSensorEnabled(boolean enable) {
        this.mAutoBrightnessHandler.removeMessages(3);
        this.mAutoBrightnessHandler.removeMessages(4);
        Message msg;
        if (enable) {
            msg = Message.obtain();
            msg.what = 3;
            this.mAutoBrightnessHandler.sendMessageAtFrontOfQueue(msg);
        } else {
            msg = Message.obtain();
            msg.what = 4;
            this.mAutoBrightnessHandler.sendMessageAtFrontOfQueue(msg);
        }
        return enable;
    }

    private boolean setLightSensorEnabledInner(boolean enable) {
        if (this.mAutoBacklightEnabled != enable) {
            if (enable) {
                this.mProximityTimeStamp = -1;
                this.mProximityStatus = false;
                if (this.mRgbAlgo != null) {
                    this.mRgbAlgo.setLigtSensorEnable(true);
                } else if (this.mBriLevelAlgo != null) {
                    this.mBriLevelAlgo.setLigtSensorEnable(true);
                } else if (this.mUDLightAlgo != null) {
                    this.mUDLightAlgo.setLigtSensorEnable(true);
                }
                this.mProximityChangedCount = 0;
                this.mLastLightLux = -1;
                this.mLastLuxLevel = LUX_LEVEL_UNKNOW;
                this.mLuxLevelEnterTimeStamp = -1;
                this.mWaitFirstLightLux = true;
                this.mLuxMedian.reset();
                this.mSensorManager.registerListener(this.mProximityListener, this.mProximitySensor, 2, this.mLightHandler);
                notifyStateChanged(16);
                this.mAutoBacklightEnabled = enable;
                if (this.mHasObjectUncoverAction) {
                    this.mAutoBrightnessHandler.removeMessages(1);
                    this.mAutoBrightnessHandler.sendEmptyMessageDelayed(1, (long) (AblConfig.getObjectUncoverActionTimeout() - 50));
                }
                this.mAutoBacklightEnabledTimeStamp = SystemClock.uptimeMillis();
                this.mSensorManager.registerListener(this.mLightListener, this.mLightSensor, 2, this.mLightHandler);
                this.mLightLuxTimestamp = SystemClock.uptimeMillis() + 30;
                if (this.mPreLightSensorEvent != null) {
                    if (AblConfig.isUseUDFingerprint() || AblConfig.getProductSolution() == 2) {
                        this.mLightLuxTimestamp -= 60;
                        Slog.d(TAG, "setLightSensorEnabledInner mPreLightSensorEvent valid,call mLightListener.onSensorChanged");
                        this.mLightListener.onSensorChanged(this.mPreLightSensorEvent);
                    } else {
                        Slog.d(TAG, "setLightSensorEnabledInner mPreLightSensorEvent valid,call mLightListener.onSensorChanged1");
                    }
                }
                Slog.e(TAG, "setLightSensorEnabledInner get lux in fast mode");
                Message lmsg = Message.obtain();
                lmsg.what = 4;
                this.mCollectDataHandler.removeMessages(4);
                if (AblConfig.isNeedExtraDelay()) {
                    this.mCollectDataHandler.sendMessageDelayed(lmsg, 150);
                } else if (this.mHasObjectUncoverAction) {
                    this.mCollectDataHandler.sendMessageDelayed(lmsg, 100);
                } else {
                    this.mCollectDataHandler.sendMessageAtFrontOfQueue(lmsg);
                }
                this.mPreLoadingLightSensor.enablePreLightSensor(false);
                Slog.d(TAG, "setLightSensorEnabled(" + this.mAutoBacklightEnabled + ")");
            } else {
                notifyStateChanged(17);
                this.mAutoBacklightEnabled = enable;
                this.mPreLoadingLightSensor.enablePreLightSensor(false);
                this.mHasNotifiedUDFinger = false;
                if (AblConfig.isUseUDFingerprint()) {
                    Message udfmsg = Message.obtain();
                    udfmsg.what = 3;
                    udfmsg.arg1 = -1;
                    this.mCollectDataHandler.removeMessages(3);
                    this.mCollectDataHandler.sendMessageAtFrontOfQueue(udfmsg);
                }
                this.mAutoBacklightEnabledTimeStamp = -1;
                this.mProximityStatus = false;
                this.mProximityTimeStamp = -1;
                this.mLightLuxTimestamp = -1;
                this.mLastLightLux = -1;
                this.mFirstLightPersist = false;
                this.mScreenBrightnessModeChange = false;
                this.mSensorManager.unregisterListener(this.mLightListener);
                this.mSensorManager.unregisterListener(this.mProximityListener);
                Slog.d(TAG, "setLightSensorEnabled(" + enable + ")");
                if (!this.mWaitFirstLightLux) {
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_LEVEL, this.mLastLuxLevel);
                    bundle.putLong(KEY_START_TIME, this.mLuxLevelEnterTimeStamp);
                    bundle.putLong(KEY_END_TIME, SystemClock.uptimeMillis());
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.setData(bundle);
                    this.mCollectDataHandler.removeMessages(1);
                    this.mCollectDataHandler.sendMessageDelayed(msg, 50);
                }
                this.mWaitFirstLightLux = false;
                this.mLightEventArrived = false;
                this.mLastLuxLevel = LUX_LEVEL_UNKNOW;
                this.mLuxLevelEnterTimeStamp = -1;
            }
        }
        return this.mAutoBacklightEnabled;
    }

    public void setDebug(String[] args) {
        AblConfig.setDebug(args);
    }

    public int notifyStateChanged(int state) {
        String jsonStr;
        JSONObject obj;
        switch (state) {
            case 3:
                if (mScreenState != ScreenState.STATE_SCREEN_DIM) {
                    this.isScreenTurnOnHappen = true;
                }
                mScreenState = ScreenState.STATE_SCREEN_BRIGHT;
                this.mAutobrightInfo.mDelayTime = 0;
                log("notifyStateChanged STATE_SCREEN_BRIGHT");
                if (SystemClock.uptimeMillis() - this.mLightLuxTimestamp > 1500) {
                    this.mAutoBrightnessCallback.onNewScreenValue(this.mAutobrightInfo);
                }
                this.mModeRestore.setScreenState(ScreenState.STATE_SCREEN_BRIGHT);
                break;
            case 4:
                log("notifyStateChanged STATE_SCREEN_DIM");
                this.bDimStatus = true;
                mScreenState = ScreenState.STATE_SCREEN_DIM;
                this.mModeRestore.setScreenState(ScreenState.STATE_SCREEN_DIM);
                break;
            case 5:
                log("notifyStateChanged STATE_SCREEN_OFF");
                this.bDimStatus = false;
                mScreenState = ScreenState.STATE_SCREEN_OFF;
                this.mModeRestore.setScreenState(ScreenState.STATE_SCREEN_OFF);
                this.mPreLoadingLightSensor.enablePreLightSensor(false);
                if (this.mRgbAlgo == null) {
                    if (this.mBriLevelAlgo == null) {
                        if (this.mUDLightAlgo != null) {
                            this.mUDLightAlgo.notifyStateChanged(5);
                            break;
                        }
                    }
                    this.mBriLevelAlgo.notifyStateChanged(5);
                    break;
                }
                this.mRgbAlgo.notifyStateChanged(5);
                break;
                break;
            case 6:
                this.mSuperPowerSavingMode = false;
                this.mWaitFirstLightLux = true;
                this.mModeRestore.setPowerSaving(false);
                break;
            case 7:
                this.mSuperPowerSavingMode = true;
                this.mWaitFirstLightLux = true;
                this.mSuperPowerSavingModeOpen = true;
                this.mModeRestore.setPowerSaving(true);
                break;
            case 14:
                if (this.mCallProximityStatus) {
                    this.mAutoBrightnessHandler.removeMessages(1);
                    this.mHasObjectUncoverAction = true;
                    this.mAutoBrightnessHandler.sendEmptyMessageDelayed(1, (long) AblConfig.getObjectUncoverActionTimeout());
                    log("notifyStateChanged set mHasObjectUncoverAction as true because PROXIMITY_FAR");
                }
                this.mCallProximityStatus = false;
                break;
            case 15:
                this.mCallProximityStatus = true;
                break;
            case 18:
                Slog.d(TAG, "notifyStateChanged STATE_BOOT_COMPLETE");
                int bright = System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", -1, -2);
                if (!AblConfig.isCollectDataVer2nd()) {
                    collectUserBrightness(bright);
                }
                jsonStr = System.getStringForUser(this.mContext.getContentResolver(), AUTOBRIGHTNESS_LUX_LEVEL_USED_TIME, -2);
                if (jsonStr == null || (Events.DEFAULT_SORT_ORDER.equals(jsonStr) ^ 1) == 0) {
                    log("bootcompleted jsonStr is null or empty");
                    log("bootcompleted mLuxLevelUsedTimeMap = " + (this.mLuxLevelUsedTimeMap != null ? this.mLuxLevelUsedTimeMap.toString() : "null"));
                } else {
                    log("bootcompleted jsonStr = " + jsonStr);
                    this.mLuxLevelUsedTimeMap.putAll(jsonStringToMap(jsonStr));
                    log("bootcompleted mLuxLevelUsedTimeMap = " + (this.mLuxLevelUsedTimeMap != null ? this.mLuxLevelUsedTimeMap.toString() : "null"));
                }
                this.isBootCompleted = true;
                String lightCheck = getSystemPropertiesValue(PROP_LIGHT_CALI_CORRETION, "no");
                if (model.equals("pd1801") && lightCheck.equals("no")) {
                    Message msg = Message.obtain();
                    msg.what = 7;
                    this.mAutoBrightnessHandler.sendMessageAtFrontOfQueue(msg);
                    Slog.d(TAG, "MSG_CHECK_LIGHT_CALIBRATION_CORRETION check");
                    break;
                }
                break;
            case StateInfo.STATE_ACTION_SHUTDOWN /*27*/:
                log("notifyStateChanged STATE_ACTION_SHUTDOWN");
                if (this.mRgbAlgo != null) {
                    this.mRgbAlgo.notifyStateChanged(27);
                } else if (this.mBriLevelAlgo != null) {
                    this.mBriLevelAlgo.notifyStateChanged(27);
                } else if (this.mUDLightAlgo != null) {
                    this.mUDLightAlgo.notifyStateChanged(27);
                }
                if (this.isBootCompleted) {
                    obj = new JSONObject(this.mLuxLevelUsedTimeMap);
                    jsonStr = obj != null ? obj.toString() : "null";
                    log("shutdown jsonStr = " + jsonStr);
                    if (!(jsonStr == null || (Events.DEFAULT_SORT_ORDER.equals(jsonStr) ^ 1) == 0)) {
                        System.putString(this.mContext.getContentResolver(), AUTOBRIGHTNESS_LUX_LEVEL_USED_TIME, jsonStr);
                        break;
                    }
                }
                break;
            case StateInfo.STATE_ACTION_REBOOT /*28*/:
                log("notifyStateChanged STATE_ACTION_REBOOT");
                if (this.mRgbAlgo != null) {
                    this.mRgbAlgo.notifyStateChanged(28);
                } else if (this.mBriLevelAlgo != null) {
                    this.mBriLevelAlgo.notifyStateChanged(28);
                } else if (this.mUDLightAlgo != null) {
                    this.mUDLightAlgo.notifyStateChanged(28);
                }
                if (this.isBootCompleted) {
                    obj = new JSONObject(this.mLuxLevelUsedTimeMap);
                    jsonStr = obj != null ? obj.toString() : "null";
                    log("reboot jsonStr = " + jsonStr);
                    if (!(jsonStr == null || (Events.DEFAULT_SORT_ORDER.equals(jsonStr) ^ 1) == 0)) {
                        System.putString(this.mContext.getContentResolver(), AUTOBRIGHTNESS_LUX_LEVEL_USED_TIME, jsonStr);
                        break;
                    }
                }
                break;
            case StateInfo.STATE_SCREEN_DOZE /*29*/:
                log("notifyStateChanged STATE_SCREEN_DOZE");
                mScreenState = ScreenState.STATE_SCREEN_DOZE;
                this.mModeRestore.setScreenState(ScreenState.STATE_SCREEN_DOZE);
                break;
            case StateInfo.STATE_FINGERPRINT_BLOCK_AUTOBRIGHTNESS_ON /*10001*/:
                log("notifyStateChanged STATE_FINGERPRINT_BLOCK_AUTOBRIGHTNESS_ON");
                this.mPreLoadingLightSensor.enablePreLightSensor(true);
                if (AblConfig.getProductSolution() == 2) {
                    getAlsRawdata();
                    break;
                }
                break;
        }
        return 0;
    }

    private void collectUserBrightness(int brightness) {
        if (brightness <= 0) {
            return;
        }
        if (this.mAutoBacklightEnabled) {
            HashMap<String, String> map = new HashMap(3);
            map.put("setbright", String.valueOf(brightness));
            String str = "autobright";
            if (this.mAutobrightInfo != null) {
                brightness = this.mAutobrightInfo.mBrightness;
            }
            map.put(str, String.valueOf(brightness));
            map.put("autoinfo", this.mAutobrightInfo != null ? this.mAutobrightInfo.toSimpleString() : "NULL");
            DataParameter data = new DataParameter(CollectUseData.EVENTID_AUTOBRIGHTNESS, CollectUseData.LABLE_USER_BRIGHTNESS, System.currentTimeMillis(), -1, 0, 1, map);
            this.mAutoBrightnessHandler.removeMessages(2);
            Message msg = this.mAutoBrightnessHandler.obtainMessage(2);
            msg.obj = data;
            this.mAutoBrightnessHandler.sendMessageDelayed(msg, 200);
            return;
        }
        this.mCollectUseData.notifyBrightnessChanged(brightness);
    }

    public int notifyScreenBrightness(int brightness) {
        int ret = 0;
        if (!this.mSuperPowerSavingMode) {
            ret = brightness;
        }
        if (!AblConfig.isCollectDataVer2nd()) {
            collectUserBrightness(ret);
        }
        return ret;
    }

    private boolean isFirstFewSeconds() {
        long diff = SystemClock.uptimeMillis() - this.mScreenOnTime;
        if (this.mWaitFirstLightLux || this.mScreenOnTime == -1 || diff <= 0 || diff > ((long) ALS_AFTER_ON_TIME_LIMIT)) {
            return false;
        }
        return true;
    }

    public void setScreenOn(boolean on) {
        if (on) {
            this.mScreenOnTime = SystemClock.uptimeMillis();
        } else {
            this.mScreenOnTime = -1;
        }
    }

    public void brightnessBeenApplied(AutobrightInfo info) {
        Message msg = this.mAutoBrightnessHandler.obtainMessage(6);
        msg.obj = info;
        msg.setAsynchronous(true);
        this.mAutoBrightnessHandler.sendMessage(msg);
    }

    private void collectUseDuration(long start, long end) {
        DataParameter data = new DataParameter(CollectUseData.EVENTID_AUTOBRIGHTNESS, CollectUseData.LABEL_USE_DURATION, start, end, end - start, 1, null);
        log("collectUseDuration:" + data.toString());
        this.mCollectUseData.sendDataParameter(data);
    }

    private void collectUseDuration(long start, long end, HashMap<String, String> params) {
        DataParameter data = new DataParameter(CollectUseData.EVENTID_AUTOBRIGHTNESS, CollectUseData.LABEL_USE_DURATION, start, end, end - start, 1, params);
        Slog.e(TAG, "0ops collectUseDuration:" + data.toString());
        this.mCollectUseData.sendDataParameter(data);
    }

    public void setUseAutoBrightness(boolean use) {
        if (this.mModeRestore != null) {
            this.mModeRestore.setUseAutoBrightness(use);
        }
    }

    public void onGetSettings(int brightness, int mode, String offBy, String changeBy) {
        this.mOldScreenBrightnessModeSetting = this.mScreenBrightnessModeSetting;
        this.mScreenBrightnessModeSetting = mode;
        if (this.mOldScreenBrightnessModeSetting == 0 && this.mScreenBrightnessModeSetting == 1) {
            this.mScreenBrightnessModeChange = true;
        }
        if (this.mModeRestore != null) {
            this.mModeRestore.onGetSettings(brightness, mode, offBy, changeBy);
        }
        if (this.mRgbAlgo != null) {
            this.mRgbAlgo.onGetSettings(brightness, mode, offBy, changeBy);
        } else if (this.mBriLevelAlgo != null) {
            this.mBriLevelAlgo.onGetSettings(brightness, mode, offBy, changeBy);
        } else if (this.mUDLightAlgo != null) {
            this.mUDLightAlgo.onGetSettings(brightness, mode, offBy, changeBy);
        }
    }

    public void setBrightnessRestoreStatus(boolean bStatus) {
        if (this.mModeRestore != null) {
            this.mModeRestore.setBrightnessRestoreStatus(bStatus);
        }
    }

    public boolean getBrightnessRestoreStatus() {
        if (this.mModeRestore != null) {
            return this.mModeRestore.getBrightnessRestoreStatus();
        }
        return false;
    }

    public void notifyPowerAssistantMode(boolean newPowerAssistantMode) {
        if (this.mRgbAlgo != null) {
            this.mRgbAlgo.notifyPowerAssistantMode(newPowerAssistantMode);
        } else if (this.mUDLightAlgo != null) {
            this.mUDLightAlgo.notifyPowerAssistantMode(newPowerAssistantMode);
        }
    }

    public boolean getAnimateFlagForSuperPowerSaveMode() {
        return this.mSuperPowerSavingModeOpen;
    }

    public void setAnimateFlagForSuperPowerSaveMode() {
        this.mSuperPowerSavingModeOpen = false;
        Slog.d(TAG, "sps flag clear");
    }

    public void setUnderDisplayLightCallback(UnderDisplayLightCallback callback) {
        if (this.mModeRestore != null) {
            this.mModeRestore.setUnderDisplayLightCallback(callback);
        }
    }

    public void setAppBrightnessCallback(AppBrightnessCallback callback) {
        if (this.mModeRestore != null) {
            this.mModeRestore.setAppBrightnessCallback(callback);
        }
    }

    private HashMap<String, String> jsonStringToMap(String jsonString) {
        HashMap<String, String> map = new HashMap();
        try {
            JSONObject jObject = new JSONObject(jsonString);
            Iterator<?> keys = jObject.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                map.put(key, jObject.getString(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    private boolean setSystemPropertiesValue(String key, String val) {
        try {
            SystemProperties.set(key, val);
            return true;
        } catch (Exception e) {
            Slog.e(TAG, "error to set " + key + " as " + val);
            return false;
        }
    }

    private String getSystemPropertiesValue(String key, String def) {
        String str = def;
        try {
            return SystemProperties.get(key, def);
        } catch (Exception e) {
            str = def;
            Slog.e(TAG, "error to get " + key);
            return str;
        }
    }
}
