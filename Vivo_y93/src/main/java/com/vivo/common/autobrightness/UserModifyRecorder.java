package com.vivo.common.autobrightness;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.util.Log;
import android.util.Slog;
import com.vivo.common.autobrightness.AppClassify.AppType;
import com.vivo.common.autobrightness.CameraLumaCallback.ModeRestoreCallback;
import com.vivo.common.provider.Calendar.Events;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class UserModifyRecorder {
    private static long DAY_INTERVAL = (MILLIS_OF_DAY * 1);
    private static final int DELAY_RECORD_TO_DB = 3000;
    private static long GET_LOC_COOL_DOWN = 2000;
    private static long GET_NAME_COOL_DOWN = 3000;
    public static final String KEY_APP_BRIGHTNESS_RATIO = "app_bri_ratio";
    public static final String KEY_RECORD_ARGUMENT = "arg";
    private static final String KEY_RECORD_RUNNING_INFO = "inf";
    private static final String KEY_RECORD_TYPE = "typ";
    private static long MILLIS_OF_DAY = 86400000;
    private static final int MODE_AUTO = 1;
    private static final int MODE_MANUAL = 0;
    private static final int MODE_UNKOWN = -1;
    private static final int MSG_BACKLIGHT_ON = 4;
    private static final int MSG_GET_FOREGROUND_PKG = 1;
    private static final int MSG_ON_GET_SETTINGS = 2;
    private static final int MSG_PENDING_COLLECT_DATA = 3;
    private static final String PKG_SYSTEM = "android";
    private static final String PKG_UNKOWN = "unknown";
    private static final String TAG = "UserModifyRecorder";
    private static final String VAL_MODE_AUTO = "auto";
    private static final String VAL_MODE_MANUAL = "man";
    private static final String VAL_MODE_UNKOWN = "unk";
    private AppBriRatioJson mAppBriRatioJson = new AppBriRatioJson();
    private int mBacklightMode = -1;
    private ModeRestoreCallback mCallback = null;
    private String mChangeBy = PKG_UNKOWN;
    private Context mContext = null;
    private SensorEventListener mEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            float lux = event.values[0];
            if (UserModifyRecorder.this.mLightLux == -1.0f) {
                UserModifyRecorder.this.mLightLux = AblConfig.getRectifiedLux(lux, UserModifyRecorder.this.mLightSensorName);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private String mForegroundAn = PKG_UNKOWN;
    private String mForegroundPkg = PKG_UNKOWN;
    private long mGetLocTimestamp = -1;
    private long mGetNameTimestamp = -1;
    private MyHandler mHandler = null;
    private long mLastRecordTimeMillis = -1;
    private int mLcmBacklight = -1;
    private boolean mLightEnabled = false;
    private float mLightLux = -1.0f;
    private Sensor mLightSensor = null;
    private String mLightSensorName = PKG_UNKOWN;
    private String mLocation = PKG_UNKOWN;
    private String mOffBy = PKG_UNKOWN;
    private boolean mPowerSaving = false;
    private String mPreForegroundAn = PKG_UNKOWN;
    private String mPreForegroundPkg = PKG_UNKOWN;
    private RunningInfo mPreRunningInfo = null;
    private ContentResolver mResolver = null;
    private SensorManager mSensorManager = null;
    private int mSettingBrightness = -1;
    private WakeLock mWakeLock = null;
    private WifiInfoManager mWifiInfoManager = null;

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg != null) {
                switch (msg.what) {
                    case 1:
                        String name = UserModifyRecorder.this.getAppNameFromUid(msg.arg1);
                        String aname = UserModifyRecorder.this.getTopActivityFullName();
                        String loc = UserModifyRecorder.this.getCurrentLocation();
                        if (name == null) {
                            name = UserModifyRecorder.PKG_UNKOWN;
                        }
                        if (!"unknown1".equals(aname)) {
                            UserModifyRecorder.this.mPreForegroundAn = UserModifyRecorder.this.mForegroundAn;
                            UserModifyRecorder.this.mForegroundAn = aname;
                        }
                        if (!"unknown1".equals(loc)) {
                            UserModifyRecorder.this.mLocation = loc;
                        }
                        UserModifyRecorder.this.mPreForegroundPkg = UserModifyRecorder.this.mForegroundPkg;
                        UserModifyRecorder.this.mForegroundPkg = name;
                        UserModifyRecorder.this.log("MSG_GET_FOREGROUND_PKG current:" + UserModifyRecorder.this.mForegroundPkg + " pre:" + UserModifyRecorder.this.mPreForegroundPkg + " curAn:" + UserModifyRecorder.this.mForegroundAn + " preAn:" + UserModifyRecorder.this.mPreForegroundAn);
                        break;
                    case 2:
                        if (UserModifyRecorder.this.mPreRunningInfo == null) {
                            UserModifyRecorder.this.mPreRunningInfo = (RunningInfo) msg.obj;
                        }
                        UserModifyRecorder.this.acquireWakelock();
                        UserModifyRecorder.this.enableLightSensor(true);
                        UserModifyRecorder.this.mHandler.removeMessages(3);
                        UserModifyRecorder.this.mHandler.sendMessageDelayed(UserModifyRecorder.this.mHandler.obtainMessage(3), 3000);
                        break;
                    case 3:
                        UserModifyRecorder.this.mPreRunningInfo.lux = (int) UserModifyRecorder.this.mLightLux;
                        UserModifyRecorder.this.mPreRunningInfo.setting = UserModifyRecorder.this.mSettingBrightness;
                        UserModifyRecorder.this.mPreRunningInfo.backlight = UserModifyRecorder.this.mLcmBacklight;
                        UserModifyRecorder.this.mPreRunningInfo.mode = UserModifyRecorder.this.mBacklightMode;
                        UserModifyRecorder.this.mPreRunningInfo.pwrAssistant = PowerAssistant.getPowerAssistantMode();
                        UserModifyRecorder.this.enableLightSensor(false);
                        int mode = UserModifyRecorder.this.mBacklightMode;
                        String strMode = UserModifyRecorder.VAL_MODE_UNKOWN;
                        if (mode == 1) {
                            strMode = "auto";
                        } else if (mode == 0) {
                            strMode = UserModifyRecorder.VAL_MODE_MANUAL;
                        }
                        JSONObject obj = new JSONObject();
                        JSONObject info = UserModifyRecorder.this.mPreRunningInfo.toJsonObject();
                        if (info != null) {
                            try {
                                obj.put(UserModifyRecorder.KEY_RECORD_TYPE, strMode);
                                obj.put(UserModifyRecorder.KEY_RECORD_RUNNING_INFO, info);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                obj = null;
                            }
                        }
                        if (UserModifyRecorder.this.mLightLux <= 5.0f && UserModifyRecorder.this.mBacklightMode == 1 && UserModifyRecorder.this.mPreRunningInfo.preMode == 1 && UserModifyRecorder.this.mPreRunningInfo.preSetting != UserModifyRecorder.this.mPreRunningInfo.setting) {
                            Slog.d(UserModifyRecorder.TAG, "MSG_PENDING_COLLECT_DATA pMod=" + UserModifyRecorder.this.mPreRunningInfo.preMode + " pSett=" + UserModifyRecorder.this.mPreRunningInfo.preSetting + " sett=" + UserModifyRecorder.this.mPreRunningInfo.setting);
                            String typ = AppClassify.getAppType(UserModifyRecorder.this.mPreRunningInfo.pkg);
                            if (!typ.equals(AppType.TYPE_UNKOWN)) {
                                UserModifyRecorder.this.mAppBriRatioJson.parseJsonString(System.getString(UserModifyRecorder.this.mResolver, UserModifyRecorder.KEY_APP_BRIGHTNESS_RATIO));
                                UserModifyRecorder.this.mAppBriRatioJson.addType(typ, UserModifyRecorder.this.mPreRunningInfo.pkg);
                                String json = UserModifyRecorder.this.mAppBriRatioJson.toJsonString();
                                if (json != null) {
                                    try {
                                        System.putString(UserModifyRecorder.this.mResolver, UserModifyRecorder.KEY_APP_BRIGHTNESS_RATIO, json);
                                    } catch (Exception e2) {
                                        Slog.d(UserModifyRecorder.TAG, "try to putString KEY_APP_BRIGHTNESS_RATIO got exception.");
                                    }
                                } else {
                                    Log.e(UserModifyRecorder.TAG, "MSG_PENDING_COLLECT_DATA mAppBriRatioJson json null");
                                }
                            }
                        }
                        if (!(obj == null || UserModifyRecorder.this.mCallback == null)) {
                            UserModifyRecorder.this.mCallback.saveModifyRecord(obj);
                        }
                        UserModifyRecorder.this.mPreRunningInfo = null;
                        UserModifyRecorder.this.mLastRecordTimeMillis = System.currentTimeMillis();
                        UserModifyRecorder.this.releaseWakelock();
                        break;
                    case 4:
                        long now = System.currentTimeMillis();
                        int backlight = msg.arg1;
                        if (UserModifyRecorder.this.mLastRecordTimeMillis > 0) {
                            if (((!UserModifyRecorder.this.mHandler.hasMessages(2) ? UserModifyRecorder.this.mHandler.hasMessages(3) : 1) ^ 1) != 0 && now > UserModifyRecorder.this.mLastRecordTimeMillis && now - UserModifyRecorder.this.mLastRecordTimeMillis > UserModifyRecorder.DAY_INTERVAL) {
                                RunningInfo selfInfo = new RunningInfo(backlight, UserModifyRecorder.this.mSettingBrightness, UserModifyRecorder.this.mBacklightMode, backlight, UserModifyRecorder.this.mSettingBrightness, UserModifyRecorder.this.mBacklightMode, UserModifyRecorder.this.mOffBy, UserModifyRecorder.this.mChangeBy, UserModifyRecorder.this.mForegroundPkg, UserModifyRecorder.this.mPreForegroundPkg, UserModifyRecorder.this.mForegroundAn, UserModifyRecorder.this.mPreForegroundAn, UserModifyRecorder.this.mLocation, UserModifyRecorder.this.mPowerSaving);
                                selfInfo.reason = RunningInfo.REASON_SELF;
                                UserModifyRecorder.this.acquireWakelock();
                                UserModifyRecorder.this.enableLightSensor(true);
                                Message selfMsg = UserModifyRecorder.this.mHandler.obtainMessage(2);
                                selfMsg.obj = selfInfo;
                                UserModifyRecorder.this.mHandler.sendMessageDelayed(selfMsg, 3000);
                                break;
                            }
                        }
                        break;
                }
            }
        }
    }

    public UserModifyRecorder(Context context, Looper looper) {
        this.mContext = context;
        this.mHandler = new MyHandler(looper);
        this.mResolver = this.mContext.getContentResolver();
        PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
        if (pm != null) {
            this.mWakeLock = pm.newWakeLock(1, TAG);
        }
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        if (this.mSensorManager != null) {
            this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
            getSensorName(this.mLightSensor);
        }
        this.mWifiInfoManager = WifiInfoManager.getInstance(this.mContext);
    }

    public void setCallback(ModeRestoreCallback callback) {
        if (AblConfig.isCollectDataVer2nd()) {
            this.mCallback = callback;
        }
    }

    private void log(String msg) {
        if (AblConfig.isDebug()) {
            Slog.d(TAG, msg);
        }
    }

    private void acquireWakelock() {
        if (!this.mWakeLock.isHeld()) {
            this.mWakeLock.acquire();
        }
    }

    private void releaseWakelock() {
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    private void enableLightSensor(boolean enable) {
        if (enable == this.mLightEnabled) {
            return;
        }
        if (enable) {
            this.mLightLux = -1.0f;
            this.mLightEnabled = this.mSensorManager.registerListener(this.mEventListener, this.mLightSensor, 3, this.mHandler);
            return;
        }
        this.mSensorManager.unregisterListener(this.mEventListener);
        this.mLightEnabled = false;
    }

    public void onForegroundActivitiesChanged(int uid) {
        if (AblConfig.isCollectDataVer2nd()) {
            Message msg = this.mHandler.obtainMessage(1);
            msg.arg1 = uid;
            this.mHandler.sendMessage(msg);
        }
    }

    public boolean onGetSettings(int setting, int mode, String offBy, String changeBy) {
        boolean ret = false;
        if (!AblConfig.isCollectDataVer2nd()) {
            return false;
        }
        if (AblConfig.isDebug()) {
            Slog.d(TAG, "onGetSettings setting:" + setting + " mode:" + mode + " offBy:" + offBy + " changeBy:" + changeBy);
        }
        if (offBy == null || Events.DEFAULT_SORT_ORDER.equals(offBy)) {
            offBy = PKG_UNKOWN;
        }
        if (changeBy == null || Events.DEFAULT_SORT_ORDER.equals(changeBy)) {
            offBy = PKG_UNKOWN;
        }
        if (this.mSettingBrightness == -1 || !((this.mSettingBrightness == setting || (PKG_SYSTEM.equals(changeBy) ^ 1) == 0) && mode == this.mBacklightMode)) {
            String aname = getTopActivityFullName();
            String loc = getCurrentLocation();
            if (!"unknown1".equals(aname)) {
                this.mPreForegroundAn = this.mForegroundAn;
                this.mForegroundAn = aname;
            }
            if (!"unknown1".equals(loc)) {
                this.mLocation = loc;
            }
            RunningInfo info = new RunningInfo(this.mLcmBacklight, this.mSettingBrightness, this.mBacklightMode, this.mLcmBacklight, setting, mode, offBy, changeBy, this.mForegroundPkg, this.mPreForegroundPkg, this.mForegroundAn, this.mPreForegroundAn, this.mLocation, this.mPowerSaving);
            if (this.mSettingBrightness == -1) {
                info.reason = RunningInfo.REASON_BOOT;
            } else {
                info.reason = RunningInfo.REASON_USER;
            }
            Message msg = this.mHandler.obtainMessage(2);
            msg.obj = info;
            this.mHandler.sendMessage(msg);
            ret = true;
        }
        this.mSettingBrightness = setting;
        this.mBacklightMode = mode;
        this.mOffBy = offBy;
        this.mChangeBy = changeBy;
        return ret;
    }

    public void onLcmBacklighChanged(int backlight) {
        if (this.mLcmBacklight == 0 && backlight != 0) {
            Message msg = this.mHandler.obtainMessage(4);
            msg.arg1 = backlight;
            this.mHandler.sendMessage(msg);
        }
        this.mLcmBacklight = backlight;
    }

    private String getAppNameFromUid(int uid) {
        int uidInt = uid;
        Slog.d(TAG, "pakage name is " + this.mContext.getPackageManager().getNameForUid(uid) + " with :" + uid);
        return this.mContext.getPackageManager().getNameForUid(uid);
    }

    public void setPowerSaving(boolean saving) {
        if (AblConfig.isCollectDataVer2nd()) {
            this.mPowerSaving = saving;
        }
    }

    private void getSensorName(Sensor sensor) {
        if (sensor != null) {
            String name = sensor.getName();
            if (name != null) {
                this.mLightSensorName = name;
            }
        }
    }

    private String getTopActivityFullName() {
        long time = SystemClock.uptimeMillis();
        if (time - this.mGetNameTimestamp < GET_NAME_COOL_DOWN) {
            return "unknown1";
        }
        this.mGetNameTimestamp = time;
        List<RunningTaskInfo> taskInfo = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(3);
        if (taskInfo.size() == 0) {
            Slog.e(TAG, "not init well, task info size is " + taskInfo.size());
            return PKG_UNKOWN;
        }
        String className = ((RunningTaskInfo) taskInfo.get(0)).topActivity.getClassName();
        log("Misaka cln:" + className);
        return className;
    }

    private String getCurrentLocation() {
        String tempLoc = PKG_UNKOWN;
        long time = SystemClock.uptimeMillis();
        if (time - this.mGetLocTimestamp < GET_LOC_COOL_DOWN) {
            return "unknown1";
        }
        this.mGetLocTimestamp = time;
        if (this.mWifiInfoManager != null) {
            tempLoc = this.mWifiInfoManager.getInfo();
        }
        log("Misaka lc:" + tempLoc);
        return tempLoc;
    }
}
