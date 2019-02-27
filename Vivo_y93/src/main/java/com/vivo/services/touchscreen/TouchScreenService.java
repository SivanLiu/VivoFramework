package com.vivo.services.touchscreen;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.IActivityManager;
import android.app.IProcessObserver;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.vivo.common.VivoCollectData;
import com.vivo.services.epm.config.BaseList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Calendar;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import vivo.app.touchscreen.ITouchScreen.Stub;

public class TouchScreenService extends Stub {
    static final String ACTION_FM_RECORDING_STATUS = "codeaurora.intent.action.FM";
    static final int BBK_TP_GESTURE_DCLICK_SWITCH = 256;
    static final int BBK_TP_GESTURE_FINGERPRINT_QUICK_SWITCH = 2048;
    static final int BBK_TP_GESTURE_LETTER_C_SWITCH = 64;
    static final int BBK_TP_GESTURE_LETTER_E_SWITCH = 32;
    static final int BBK_TP_GESTURE_LETTER_M_SWITCH = 16;
    static final int BBK_TP_GESTURE_LETTER_O_SWITCH = 4;
    static final int BBK_TP_GESTURE_LETTER_W_SWITCH = 8;
    static final int BBK_TP_GESTURE_PN_FINGER_SWITCH = 4096;
    static final int BBK_TP_GESTURE_SWIPE_DOWN_SWITCH = 128;
    static final int BBK_TP_GESTURE_SWIPE_LEFT_RIGHT_SWITCH = 1;
    static final int BBK_TP_GESTURE_SWIPE_UP_SWITCH = 2;
    static final int BBK_TP_GESTURE_WAKE_EMAIL = 512;
    static final int BBK_TP_GESTURE_WAKE_FACEBOOK = 1024;
    private static final boolean DBG = true;
    private static final String FINGERPRINT_UNLOCK_SWITCH = "finger_unlock_open";
    static final String HALL_LOCK_BROADCAST_ACTION = "com.android.service.hallobserver.lock";
    static final String HALL_UNLOCK_BROADCAST_ACTION = "com.android.service.hallobserver.unlock";
    static final long MILLIS_PER_DAY = 86400000;
    private static final String PROP_FINGERPRINT_TYPE = "persist.sys.fptype";
    private static final String PROP_VALUE_PREFIX_UDFP = "udfp_";
    private static final float PROXIMITY_THRESHOLD = 5.0f;
    static final int RESET_START_ALARM_CLOCK = 3;
    static final int SET_FM_RADIO_OFF = 6;
    static final int SET_FM_RADIO_ON = 5;
    static final int SET_HALL_LOCK = 2;
    static final int SET_HALL_UNLOCK = 3;
    static final int SET_NATIVE_LCD_STATE_OFF = 0;
    static final int SET_NATIVE_LCD_STATE_ON = 1;
    static final int SET_START_ALARM_CLOCK = 2;
    static final int SET_SUPER_POWER_SAVE_EXIT = 4;
    static final String SUPER_POWER_SAVE_BROADCAST_ACTION = "intent.action.super_power_save_send";
    static final String[] SystemGesturesSettings = new String[]{"bbk_screen_disable_change_music_setting", "bbk_screen_disable_to_unlock_setting", "bbk_screen_disable_wake_qq_setting", "bbk_screen_disable_wake_wechat_setting", "bbk_screen_disable_wake_music_setting", "bbk_screen_disable_wake_browser_setting", "bbk_screen_disable_wake_dial_setting", "bbk_quick_open_camera_setting", "bbk_smart_wakeup", "bbk_screen_disable_wake_email_setting", "bbk_screen_disable_wake_facebook_setting", "quick_launch_app_primary_switch", FINGERPRINT_UNLOCK_SWITCH};
    private static final String TAG = "BBKTouchScreenServiceService";
    static final int VTS_TOUCH_INFO_COLLECT = 116;
    static final boolean isSupportUDFingerprint = SystemProperties.get(PROP_FINGERPRINT_TYPE, "unknown").startsWith(PROP_VALUE_PREFIX_UDFP);
    private static IActivityManager mIActivityManager;
    private boolean isLcdBacklightCalled = false;
    private boolean last_state = false;
    private AlarmManager mAlarmManager;
    private VivoCollectData mCollectData;
    private final Object mCollectDataLock = new Object();
    private Context mContext;
    private int mGesturesSetting = 0;
    private int mGesturesSettingSave = 256;
    private boolean mHallLockEnabled = false;
    private HallLockReceiver mHallLockReceiver = new HallLockReceiver();
    private Handler mHandler;
    private boolean mHasGesturesEnabled = false;
    private boolean mIsScreenOn = DBG;
    private final Object mLock = new Object();
    private boolean mNeedDownSensitivity = false;
    private Handler mNewHandler;
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            Slog.d(TouchScreenService.TAG, "onCallStateChanged state is" + state);
            switch (state) {
                case 0:
                    Slog.d(TouchScreenService.TAG, "rf stop work");
                    TouchScreenService.this.SetAppName("VivoPhoneState:0".getBytes());
                    return;
                case 1:
                case 2:
                    Slog.d(TouchScreenService.TAG, "rf start work");
                    TouchScreenService.this.SetAppName("VivoPhoneState:1".getBytes());
                    return;
                default:
                    Slog.d(TouchScreenService.TAG, "other state");
                    return;
            }
        }
    };
    private IProcessObserver mProcessObserver = new IProcessObserver.Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (foregroundActivities) {
                try {
                    TouchScreenService.this.SetAppName(("t" + TouchScreenService.this.getAppNameFromUid(uid)).getBytes());
                    return;
                } catch (Exception e) {
                    Slog.d(TouchScreenService.TAG, "Failed in TouchscreenSetAppCode");
                    return;
                }
            }
            TouchScreenService.this.SetAppName(("f" + TouchScreenService.this.getAppNameFromUid(uid)).getBytes());
        }

        public void onProcessStateChanged(int pid, int uid, int importance) {
        }

        public void onProcessDied(int pid, int uid) {
        }
    };
    private int mProximiteListenerRegistered = 0;
    private int mProximited = -1;
    SensorEventListener mProximityListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            synchronized (TouchScreenService.this.mLock) {
                int ProximityState;
                float distance = event.values[0];
                if (((double) distance) < 0.0d || distance >= TouchScreenService.PROXIMITY_THRESHOLD || distance >= TouchScreenService.this.mProximitySensor.getMaximumRange()) {
                    ProximityState = 0;
                } else {
                    ProximityState = 1;
                }
                if (TouchScreenService.this.mNeedDownSensitivity) {
                    if (TouchScreenService.this.mIsScreenOn) {
                        if (TouchScreenService.this.mProximiteListenerRegistered == 1 && TouchScreenService.this.mProximited != ProximityState) {
                            TouchScreenService.this.mProximited = ProximityState;
                            if (ProximityState == 1) {
                                Slog.d(TouchScreenService.TAG, "NeedDownSensitivity Proximity Sensor proximited");
                                TouchScreenService.nativeTouchScreenGlovesModeSwitch(1);
                            } else {
                                Slog.d(TouchScreenService.TAG, "NeedDownSensitivity Proximity Sensor check move away");
                                TouchScreenService.nativeTouchScreenGlovesModeSwitch(0);
                            }
                        }
                    } else if (TouchScreenService.this.mProximiteListenerRegistered == 1 && TouchScreenService.this.mProximited != ProximityState) {
                        TouchScreenService.this.mProximited = ProximityState;
                        if (ProximityState == 1) {
                            Slog.d(TouchScreenService.TAG, "Proximity Sensor proximited");
                            TouchScreenService.nativeTouchScreenDclickEnable(0);
                            TouchScreenService.this.mHasGesturesEnabled = false;
                        } else {
                            Slog.d(TouchScreenService.TAG, "Proximity Sensor check move away");
                            TouchScreenService.nativeTouchScreenDclickEnable(1);
                            TouchScreenService.this.mHasGesturesEnabled = TouchScreenService.DBG;
                        }
                    }
                } else if (TouchScreenService.this.mProximiteListenerRegistered == 1 && TouchScreenService.this.mProximited != ProximityState) {
                    TouchScreenService.this.mProximited = ProximityState;
                    if (ProximityState == 1) {
                        Slog.d(TouchScreenService.TAG, "Proximity Sensor proximited");
                        TouchScreenService.nativeTouchScreenDclickEnable(0);
                        TouchScreenService.this.mHasGesturesEnabled = false;
                    } else {
                        Slog.d(TouchScreenService.TAG, "Proximity Sensor check move away");
                        TouchScreenService.nativeTouchScreenDclickEnable(1);
                        TouchScreenService.this.mHasGesturesEnabled = TouchScreenService.DBG;
                    }
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private Sensor mProximitySensor;
    private SensorManager mSensorManager;
    private boolean mSpsSwitch = false;
    private int mTemplateValid = 0;
    private Handler mTsLcdStateHandler;
    private int mUdgesturesSetting = 0;
    private BroadcastReceiver mVivoBroadcastReceiver;
    private IntentFilter mVivoIntentFilter;
    private WakeLock mWakeLock;
    private Handler tsSuperNodeHandler;
    private final String vivoTsCollectData = "com.vivo.touchscreen.AlarmColck";
    private final String vivoTsUpdateAction = "com.vivo.daemonService.unifiedconfig.update_finish_broadcast_VTSG";
    private final String vivoTsUri = "content://com.vivo.daemonservice.unifiedconfigprovider/configs";
    private boolean wzrySwitch = false;

    class HallLockReceiver extends BroadcastReceiver {
        HallLockReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Message msg;
            if (action.equals(TouchScreenService.HALL_LOCK_BROADCAST_ACTION)) {
                Slog.d(TouchScreenService.TAG, "Hall lock recive");
                msg = TouchScreenService.this.mTsLcdStateHandler.obtainMessage();
                msg.what = 2;
                TouchScreenService.this.mTsLcdStateHandler.sendMessage(msg);
            } else if (action.equals(TouchScreenService.HALL_UNLOCK_BROADCAST_ACTION)) {
                Slog.d(TouchScreenService.TAG, "Hall unlock recive");
                msg = TouchScreenService.this.mTsLcdStateHandler.obtainMessage();
                msg.what = 3;
                TouchScreenService.this.mTsLcdStateHandler.sendMessage(msg);
            } else if (action.equals(TouchScreenService.SUPER_POWER_SAVE_BROADCAST_ACTION) && "exited".equals(intent.getStringExtra("sps_action"))) {
                Log.d(TouchScreenService.TAG, "exited: mIsScreenOn = " + TouchScreenService.this.mIsScreenOn + " mHasGesturesEnabled = " + TouchScreenService.this.mHasGesturesEnabled);
                msg = TouchScreenService.this.mTsLcdStateHandler.obtainMessage();
                msg.what = 4;
                TouchScreenService.this.mTsLcdStateHandler.sendMessage(msg);
            } else if (action.equals(TouchScreenService.ACTION_FM_RECORDING_STATUS)) {
                Log.d(TouchScreenService.TAG, "FMRedioReceiver action \n");
                msg = TouchScreenService.this.mTsLcdStateHandler.obtainMessage();
                int state = intent.getIntExtra("state", 0);
                if (state == 1) {
                    Slog.d(TouchScreenService.TAG, "FMRedioReceiver on \n");
                    msg.what = 5;
                } else if (state == 0) {
                    Slog.d(TouchScreenService.TAG, "FMRedioReceiver off \n");
                    msg.what = 6;
                }
                TouchScreenService.this.mTsLcdStateHandler.sendMessage(msg);
            } else if (action.equals("android.intent.action.HEADSET_PLUG") && intent.getIntExtra("state", 0) == 0) {
                Slog.d(TouchScreenService.TAG, "Headset disconnect, FM off");
                msg = TouchScreenService.this.mTsLcdStateHandler.obtainMessage();
                msg.what = 6;
                TouchScreenService.this.mTsLcdStateHandler.sendMessage(msg);
            }
        }
    }

    final class TouchScreenServiceHandler extends Handler {
        public TouchScreenServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
        }
    }

    private static native int nativeCallingSwitch(int i);

    private static native int nativeGetDriverICName();

    private static native void nativeInit();

    private static native int nativeSensorRxTx();

    private static native int nativeSetAppName(byte[] bArr);

    private static native int nativeSetFingerGestureSwitch(int i);

    private static native int nativeTouchDclickSwitch(int i);

    private static native int nativeTouchGestureLetterSwitch(int i);

    private static native int nativeTouchGestureSignSwitch(int i);

    private static native int nativeTouchGestureSwitch(int i);

    private static native int nativeTouchGestureSwitchexport(int i);

    private static native int nativeTouchScreenDclickEnable(int i);

    private static native int nativeTouchScreenDclickSimulateSwitch(int i);

    private static native int nativeTouchScreenEdgeSuppressSwitch(int i);

    private static native int nativeTouchScreenGlovesModeSwitch(int i);

    private static native int nativeTouchScreenLcdStateSet(int i);

    private static native int nativeTouchSwipeWakeupSwitch(int i);

    private static native int nativeUdgClearTemplate();

    private static native int nativeUdgGetAttn();

    private static native int nativeUdgGetCoordinates(byte[] bArr);

    private static native int nativeUdgGetDetectionScore();

    private static native int nativeUdgGetGesturePoints(byte[] bArr);

    private static native int nativeUdgGetGesturePointsLength();

    private static native int nativeUdgGetMatchScore(byte[] bArr, byte[] bArr2);

    private static native int nativeUdgGetMaxNumberSigs();

    private static native int nativeUdgGetMaxSigLength();

    private static native int nativeUdgGetRegistrationStatus();

    private static native int nativeUdgGetScore();

    private static native int nativeUdgGetTemplateData(float[] fArr, float[] fArr2, byte[] bArr);

    private static native int nativeUdgGetTemplateSize();

    private static native int nativeUdgGetThreshold();

    private static native int nativeUdgGetTraceData(int[] iArr, int[] iArr2, byte[] bArr);

    private static native int nativeUdgGetTraceSize();

    private static native int nativeUdgGetgestureEnable();

    private static native byte nativeUdgReadDetection();

    private static native int nativeUdgReadDetectionIndex();

    private static native int nativeUdgReadIndex();

    private static native int nativeUdgReadSignature(byte[] bArr);

    private static native int nativeUdgReadTemplateDetection();

    private static native int nativeUdgReadTemplateMaxIndex();

    private static native int nativeUdgReadTemplateValid();

    private static native int nativeUdgSetDetectionEnable(int i);

    private static native int nativeUdgSetEnable(int i);

    private static native int nativeUdgSetEngineEnable(int i);

    private static native int nativeUdgSetEnroll(int i);

    private static native int nativeUdgSetMode(int i);

    private static native int nativeUdgSetRegistrationBegin(int i);

    private static native int nativeUdgSetRegistrationEnable(int i);

    private static native int nativeUdgSetTemplateValid(int i);

    private static native int nativeUdgSetThreshold(int i);

    private static native int nativeUdgSetgestureEnable(int i);

    private static native int nativeUdgWriteIndex(int i);

    private static native int nativeUdgWriteSignature(byte[] bArr);

    private static native int nativeUdgWriteTemplateData(float[] fArr, float[] fArr2, byte[] bArr);

    private static native int nativeUdgWriteTemplateIndex(char c);

    int GetGesturesSwitchState() {
        int GesturesSettings = 0;
        int i = 0;
        while (i < SystemGesturesSettings.length) {
            if (isSupportUDFingerprint || !SystemGesturesSettings[i].equals(FINGERPRINT_UNLOCK_SWITCH)) {
                int TempSetting = System.getInt(this.mContext.getContentResolver(), SystemGesturesSettings[i], 0);
                Slog.d(TAG, "System Setting " + SystemGesturesSettings[i] + " is " + TempSetting);
                GesturesSettings |= TempSetting << i;
            }
            i++;
        }
        return GesturesSettings;
    }

    void RegisterProximityListener(boolean on) {
        Slog.d(TAG, "The mProximiteListenerRegistered is " + this.mProximiteListenerRegistered + " on is " + on);
        this.mProximited = -1;
        if (on) {
            if (this.mProximiteListenerRegistered != 1) {
                this.mProximiteListenerRegistered = 1;
                this.mSensorManager.registerListener(this.mProximityListener, this.mProximitySensor, 3, this.mHandler);
            }
        } else if (this.mProximiteListenerRegistered != 0) {
            this.mProximiteListenerRegistered = 0;
            this.mSensorManager.unregisterListener(this.mProximityListener);
        }
    }

    void SetNativeGesturesSwitchState(int GesturesSettings) {
        int tempsetting;
        int GestureSignSetting = 0;
        int SettingBitmap = GesturesSettings ^ this.mGesturesSettingSave;
        if ((SettingBitmap & 256) != 0) {
            if ((GesturesSettings & 256) > 0) {
                nativeTouchDclickSwitch(1);
            } else {
                nativeTouchDclickSwitch(0);
            }
        }
        if ((SettingBitmap & 128) != 0) {
            if ((GesturesSettings & 128) > 0) {
                nativeTouchSwipeWakeupSwitch(1);
            } else {
                nativeTouchSwipeWakeupSwitch(0);
            }
        }
        if (!((SettingBitmap & 1536) == 0 && (SettingBitmap & 4096) == 0)) {
            tempsetting = GesturesSettings & 1536;
            Slog.d(TAG, "before add finger switch,tempsetting=" + tempsetting);
            tempsetting |= (GesturesSettings & 4096) >> 4;
            Slog.d(TAG, "after add finger switch,tempsetting=" + tempsetting);
            nativeTouchGestureSwitchexport(tempsetting);
        }
        Slog.d(TAG, "SettingBitmap:" + SettingBitmap + "mGesturesSettingSave:" + this.mGesturesSettingSave);
        if ((SettingBitmap & 2175) != 0) {
            tempsetting = (GesturesSettings & 127) | ((GesturesSettings & 2048) >> 4);
            Slog.d(TAG, "set gesture_switch:" + tempsetting);
            nativeTouchGestureSwitch(tempsetting);
        }
        if ((SettingBitmap & 1) != 0 && (GesturesSettings & 1) > 0) {
            GestureSignSetting = 3;
        }
        if ((SettingBitmap & 2) != 0 && (GesturesSettings & 2) > 0) {
            GestureSignSetting += 4;
        }
        nativeTouchGestureSignSwitch(GestureSignSetting);
        if (((SettingBitmap >> 2) & 31) != 0) {
            nativeTouchGestureLetterSwitch((GesturesSettings >> 2) & 31);
        }
        this.mGesturesSettingSave = GesturesSettings;
    }

    public void TouchscreenAccStateSet(int isLandscape) {
        Slog.d(TAG, "set isLandscape  " + isLandscape);
        nativeTouchScreenEdgeSuppressSwitch(isLandscape);
    }

    public int TouchscreenSetFingerGestureSwitch(int state) {
        return nativeSetFingerGestureSwitch(state);
    }

    public void TouchscreenLcdBacklightStateSet(boolean isScreenOn) {
        String lcdState = isScreenOn ? "On" : "Off";
        if (isScreenOn != this.last_state) {
            Slog.d(TAG, "Get lcd status " + lcdState);
            Message msg = this.mTsLcdStateHandler.obtainMessage();
            msg.what = isScreenOn ? 1 : 0;
            this.mTsLcdStateHandler.sendMessage(msg);
            this.last_state = isScreenOn;
        }
    }

    public int TouchScreenDclickSimulateSwitch(int on) {
        return nativeTouchScreenDclickSimulateSwitch(on);
    }

    public int TouchScreenGlovesModeSwitch(int on) {
        return nativeTouchScreenGlovesModeSwitch(on);
    }

    public int TouchScreenCallingSwitch(int on) {
        return nativeCallingSwitch(on);
    }

    public int TouchScreenUserDefineGestureSetThreshold(int setting) {
        return nativeUdgSetThreshold(setting);
    }

    public int TouchScreenUserDefineGestureWriteIndex(int setting) {
        return nativeUdgWriteIndex(setting);
    }

    public int TouchScreenUserDefineGestureWriteSignature(byte[] signature) {
        return nativeUdgWriteSignature(signature);
    }

    public int TouchScreenUserDefineGestureSetgestureEnable(int setting) {
        return nativeUdgSetgestureEnable(setting);
    }

    public int TouchScreenUserDefineGestureSetEnroll(int setting) {
        return nativeUdgSetEnroll(setting);
    }

    public int TouchScreenUserDefineGestureSetEnable(int setting) {
        return nativeUdgSetEnable(setting);
    }

    public int TouchScreenUserDefineGestureSetMode(int setting) {
        return nativeUdgSetMode(setting);
    }

    public int TouchScreenUserDefineGestureGetAttn() {
        return nativeUdgGetAttn();
    }

    public int TouchScreenUserDefineGestureGetThreshold() {
        return nativeUdgGetThreshold();
    }

    public int TouchScreenUserDefineGestureGetScore() {
        return nativeUdgGetScore();
    }

    public int TouchScreenUserDefineReadIndex() {
        return nativeUdgReadIndex();
    }

    public int TouchScreenUserDefineReadSignature(byte[] signature) {
        return nativeUdgReadSignature(signature);
    }

    public byte TouchScreenUserDefineReadDetection() {
        return nativeUdgReadDetection();
    }

    public int TouchScreenUserDefineGetMaxSigLength() {
        return nativeUdgGetMaxSigLength();
    }

    public int TouchScreenUserDefineGetMaxNumberSigs() {
        return nativeUdgGetMaxNumberSigs();
    }

    public int TouchScreenUserDefineGetgestureEnable() {
        return nativeUdgGetgestureEnable();
    }

    public int TouchScreenUserDefineGetCoordinates(byte[] coordinates) {
        return nativeUdgGetCoordinates(coordinates);
    }

    public int TouchScreenUserDefineGetGesturePoints(byte[] points) {
        return nativeUdgGetGesturePoints(points);
    }

    public int TouchScreenUserDefineGetGesturePointsLength() {
        return nativeUdgGetGesturePointsLength();
    }

    public int TouchScreenUserDefineGetMatchScore(byte[] signature1, byte[] signature2) {
        return nativeUdgGetMatchScore(signature1, signature2);
    }

    public int TouchscreenUserDefineGestureSetEngineEnable(int setting) {
        return nativeUdgSetEngineEnable(setting);
    }

    public int TouchscreenUserDefineGestureSetDetectionEnable(int setting) {
        return nativeUdgSetDetectionEnable(setting);
    }

    public int TouchscreenUserDefineGestureSetRegistrationEnable(int setting) {
        return nativeUdgSetRegistrationEnable(setting);
    }

    public int TouchscreenUserDefineGestureSetRegistrationBegin(int setting) {
        return nativeUdgSetRegistrationBegin(setting);
    }

    public int TouchscreenUserDefineGestureWriteTemplateIndex(char index) {
        return nativeUdgWriteTemplateIndex(index);
    }

    public int TouchscreenUserDefineGestureSetTemplateValid(int setting) {
        return nativeUdgSetTemplateValid(setting);
    }

    public int TouchscreenUserDefineGestureClearTemplate() {
        return nativeUdgClearTemplate();
    }

    public int TouchscreenUserDefineGestureReadDetectionIndex() {
        return nativeUdgReadDetectionIndex();
    }

    public int TouchscreenUserDefineGestureGetDetectionScore() {
        return nativeUdgGetDetectionScore();
    }

    public int TouchscreenUserDefineGestureGetRegistrationStatus() {
        return nativeUdgGetRegistrationStatus();
    }

    public int TouchscreenUserDefineGestureGetTemplateSize() {
        return nativeUdgGetTemplateSize();
    }

    public int TouchscreenUserDefineGestureReadTemplateMaxIndex() {
        return nativeUdgReadTemplateMaxIndex();
    }

    public int TouchscreenUserDefineGestureReadTemplateDetection() {
        return nativeUdgReadTemplateDetection();
    }

    public int TouchscreenUserDefineGestureReadTemplateValid() {
        return nativeUdgReadTemplateValid();
    }

    public int TouchscreenUserDefineGestureGetTraceSize() {
        return nativeUdgGetTraceSize();
    }

    public int TouchscreenUserDefineGestureGetTraceData(int[] x_trace, int[] y_trace, byte[] segments) {
        return nativeUdgGetTraceData(x_trace, y_trace, segments);
    }

    public int TouchscreenUserDefineGestureGetTemplateData(float[] data, float[] scalefac, byte[] segments) {
        return nativeUdgGetTemplateData(data, scalefac, segments);
    }

    public int TouchscreenUserDefineGestureWriteTemplateData(float[] data, float[] scalefac, byte[] segments) {
        return nativeUdgWriteTemplateData(data, scalefac, segments);
    }

    public int TouchSensorRxTx() {
        return nativeSensorRxTx();
    }

    public int TouchScreenGetDriverICName() {
        return nativeGetDriverICName();
    }

    public TouchScreenService(final Context context) {
        Slog.i(TAG, "BBKTouchScreenService Service");
        this.mContext = context;
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mProximitySensor = this.mSensorManager.getDefaultSensor(8);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        Slog.d(TAG, "TelephonyManager init !!!!!!!!!!!!!!!!!!!!!");
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 32);
        nativeInit();
        Slog.d(TAG, "new hander thread !!!!!!!!!!!!!!!!!!!!!");
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mNewHandler = new TouchScreenServiceHandler(thread.getLooper());
        HandlerThread handlerThread = new HandlerThread("TS_Service");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper());
        handlerThread = new HandlerThread("TSLcdState");
        handlerThread.start();
        this.mTsLcdStateHandler = new Handler(handlerThread.getLooper()) {
            /* JADX WARNING: Missing block: B:6:0x000f, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void handleMessage(Message msg) {
                boolean z = false;
                synchronized (TouchScreenService.this.mLock) {
                    switch (msg.what) {
                        case 0:
                        case 1:
                            if (msg.what != 0) {
                                z = TouchScreenService.DBG;
                            }
                            Boolean isScreenOn = Boolean.valueOf(z);
                            TouchScreenService.this.mSpsSwitch = SystemProperties.getBoolean("sys.super_power_save", false);
                            Slog.d(TouchScreenService.TAG, "Super power save property is " + TouchScreenService.this.mSpsSwitch + ". msg:" + msg.what);
                            if (isScreenOn.booleanValue() != TouchScreenService.this.mIsScreenOn || !TouchScreenService.this.isLcdBacklightCalled) {
                                if (!TouchScreenService.this.isLcdBacklightCalled) {
                                    Slog.d(TouchScreenService.TAG, "first called by LcdBacklight after BBKTouchScreenService is create.");
                                    TouchScreenService.this.isLcdBacklightCalled = TouchScreenService.DBG;
                                }
                                if (isScreenOn.booleanValue()) {
                                    Slog.d(TouchScreenService.TAG, "Set LCD backlight state ON");
                                    TouchScreenService.nativeTouchScreenLcdStateSet(1);
                                    if (TouchScreenService.this.mNeedDownSensitivity) {
                                        Slog.d(TouchScreenService.TAG, "Need register listener for sensitivity change");
                                        TouchScreenService.this.RegisterProximityListener(false);
                                        TouchScreenService.this.RegisterProximityListener(TouchScreenService.DBG);
                                    } else {
                                        TouchScreenService.this.RegisterProximityListener(false);
                                    }
                                } else {
                                    Slog.d(TouchScreenService.TAG, "Set LCD backlight state OFF");
                                    TouchScreenService.nativeTouchScreenLcdStateSet(0);
                                    if (TouchScreenService.this.mHallLockEnabled) {
                                        Slog.d(TouchScreenService.TAG, "Hall lock is enabled");
                                        TouchScreenService.this.RegisterProximityListener(false);
                                        TouchScreenService.this.mIsScreenOn = isScreenOn.booleanValue();
                                        return;
                                    }
                                    TouchScreenService.this.mGesturesSetting = TouchScreenService.this.GetGesturesSwitchState();
                                    Slog.d(TouchScreenService.TAG, "mGesturesSetting is " + TouchScreenService.this.mGesturesSetting);
                                    TouchScreenService.this.mUdgesturesSetting = TouchScreenService.nativeUdgGetgestureEnable();
                                    Slog.d(TouchScreenService.TAG, "mUdgesturesSetting is " + TouchScreenService.this.mUdgesturesSetting);
                                    TouchScreenService.this.mTemplateValid = TouchScreenService.nativeUdgReadTemplateValid();
                                    Slog.d(TouchScreenService.TAG, "mTemplateValid is " + TouchScreenService.this.mTemplateValid);
                                    if (TouchScreenService.this.mTemplateValid < 0) {
                                        TouchScreenService.this.mTemplateValid = 0;
                                    }
                                    TouchScreenService.this.SetNativeGesturesSwitchState(TouchScreenService.this.mGesturesSetting);
                                    if (TouchScreenService.this.mNeedDownSensitivity) {
                                        if (TouchScreenService.this.mSpsSwitch || (TouchScreenService.this.mGesturesSetting == 0 && TouchScreenService.this.mUdgesturesSetting != 1 && (TouchScreenService.this.mTemplateValid & 31) == 0)) {
                                            TouchScreenService.this.RegisterProximityListener(false);
                                        } else {
                                            TouchScreenService.this.RegisterProximityListener(false);
                                            TouchScreenService.this.RegisterProximityListener(TouchScreenService.DBG);
                                        }
                                    } else if (!(TouchScreenService.this.mSpsSwitch || (TouchScreenService.this.mGesturesSetting == 0 && TouchScreenService.this.mUdgesturesSetting != 1 && (TouchScreenService.this.mTemplateValid & 31) == 0))) {
                                        TouchScreenService.this.RegisterProximityListener(TouchScreenService.DBG);
                                    }
                                }
                                TouchScreenService.this.mIsScreenOn = isScreenOn.booleanValue();
                                break;
                            }
                            return;
                            break;
                        case 2:
                            TouchScreenService.this.RegisterProximityListener(false);
                            TouchScreenService.this.mHallLockEnabled = TouchScreenService.DBG;
                            if (TouchScreenService.this.mHasGesturesEnabled) {
                                TouchScreenService.nativeTouchScreenDclickEnable(0);
                                TouchScreenService.this.mHasGesturesEnabled = false;
                                break;
                            }
                            break;
                        case 3:
                            TouchScreenService.this.mHallLockEnabled = false;
                            break;
                        case 4:
                            PowerManager pm = (PowerManager) TouchScreenService.this.mContext.getSystemService("power");
                            if (!TouchScreenService.this.mIsScreenOn) {
                                Log.d(TouchScreenService.TAG, "exited: isScreen off");
                                TouchScreenService.this.TouchscreenLcdBacklightStateSet(false);
                                break;
                            }
                            Log.d(TouchScreenService.TAG, "exited: isScreen on");
                            break;
                        case 5:
                            TouchScreenService.this.SetAppName("FM_ON".getBytes());
                            break;
                        case 6:
                            TouchScreenService.this.SetAppName("FM_OFF".getBytes());
                            break;
                    }
                }
            }
        };
        this.mCollectData = VivoCollectData.getInstance(context);
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "vivoTouchScreen");
        HandlerThread superNodeThread = new HandlerThread("TouchSuperNode");
        superNodeThread.start();
        this.tsSuperNodeHandler = new Handler(superNodeThread.getLooper()) {
            final String touchEventId = "121";

            private PendingIntent getPendingIntent() {
                return PendingIntent.getBroadcast(context, 0, new Intent("com.vivo.touchscreen.AlarmColck"), 0);
            }

            private void setAlarmTimer() {
                Slog.d(TouchScreenService.TAG, "set AlarmColck");
                Calendar calendar = Calendar.getInstance();
                calendar.set(11, 23);
                calendar.set(12, 59);
                calendar.set(13, 0);
                long startTime = calendar.getTimeInMillis();
                long currentTime = System.currentTimeMillis();
                Slog.d(TouchScreenService.TAG, "start: " + startTime + "  current:" + currentTime + " duration:" + (startTime - currentTime));
                if (TouchScreenService.this.mAlarmManager != null) {
                    TouchScreenService.this.mAlarmManager.setRepeating(0, startTime, TouchScreenService.MILLIS_PER_DAY, getPendingIntent());
                }
            }

            private void cancelAlarmTimer() {
                Slog.d(TouchScreenService.TAG, "cancel AlarmColck");
                if (TouchScreenService.this.mAlarmManager != null) {
                    TouchScreenService.this.mAlarmManager.cancel(getPendingIntent());
                }
            }

            private void vivoTouchCollectData(String label, String key, String dataContent) {
                if (TouchScreenService.this.mCollectData != null && TouchScreenService.this.mCollectData.getControlInfo("121")) {
                    long time = System.currentTimeMillis();
                    HashMap<String, String> params = new HashMap();
                    params.put(key, dataContent);
                    TouchScreenService.this.mCollectData.writeData("121", label, time, time, 0, 1, params);
                }
            }

            public void handleMessage(Message msg) {
                synchronized (TouchScreenService.this.mCollectDataLock) {
                    switch (msg.what) {
                        case 2:
                            setAlarmTimer();
                            break;
                        case 3:
                            cancelAlarmTimer();
                            setAlarmTimer();
                            break;
                        case TouchScreenService.VTS_TOUCH_INFO_COLLECT /*116*/:
                            TouchScreenService.this.mWakeLock.acquire(500);
                            String s = TouchScreenService.this.TouchReadSuperNode(Integer.toString(msg.what));
                            if (!s.isEmpty()) {
                                String[] data = s.split("\n");
                                Slog.d(TouchScreenService.TAG, "Get data: " + data[0]);
                                vivoTouchCollectData("12110", "fwStatus", data[0]);
                                break;
                            }
                            break;
                        default:
                            Slog.d(TouchScreenService.TAG, "Invalid msg " + msg.what);
                            break;
                    }
                }
            }
        };
        Slog.d(TAG, "end !!!!!!!!!!!!!!!!!!!!!");
        IntentFilter filter = new IntentFilter();
        filter.addAction(HALL_LOCK_BROADCAST_ACTION);
        filter.addAction(HALL_UNLOCK_BROADCAST_ACTION);
        filter.addAction(SUPER_POWER_SAVE_BROADCAST_ACTION);
        filter.addAction(ACTION_FM_RECORDING_STATUS);
        filter.addAction("android.intent.action.HEADSET_PLUG");
        this.mContext.registerReceiver(this.mHallLockReceiver, filter);
        boolean equalsIgnoreCase = (SystemProperties.get("ro.product.model.bbk", "other").equalsIgnoreCase("PD1227T") || SystemProperties.get("ro.product.model.bbk", "other").equalsIgnoreCase("PD1227TG3")) ? DBG : SystemProperties.get("ro.product.model.bbk", "other").equalsIgnoreCase("PD1227B");
        this.mNeedDownSensitivity = equalsIgnoreCase;
        this.mSpsSwitch = SystemProperties.getBoolean("sys.super_power_save", false);
        if (this.mNeedDownSensitivity) {
            RegisterProximityListener(DBG);
        }
        Slog.d(TAG, "construct function called !!!!!!!!!!!!!!!!!!!!!");
        mIActivityManager = ActivityManagerNative.getDefault();
        setupVivoReceiver();
    }

    private void setupVivoReceiver() {
        this.mVivoBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                try {
                    String action = intent.getAction();
                    Slog.d(TouchScreenService.TAG, "setupVivoReceiver action:" + action);
                    if (!action.equals("com.vivo.daemonService.unifiedconfig.update_finish_broadcast_VTSG")) {
                        Message msg;
                        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                            msg = TouchScreenService.this.tsSuperNodeHandler.obtainMessage();
                            msg.what = 2;
                            TouchScreenService.this.tsSuperNodeHandler.sendMessage(msg);
                        } else if (action.equals("com.vivo.battlemode.touchscreen")) {
                            int state = ((Integer) intent.getExtra("state", Integer.valueOf(0))).intValue();
                            if (state == 0) {
                                Slog.d(TouchScreenService.TAG, "Exit game mode");
                                TouchScreenService.this.SetAppName("VivoGameMode:0".getBytes());
                            } else if (state == 1) {
                                Slog.d(TouchScreenService.TAG, "Enter game mode");
                                TouchScreenService.this.SetAppName("VivoGameMode:1".getBytes());
                            }
                        } else if (action.equals("android.intent.action.TIMEZONE_CHANGED") || action.equals("android.intent.action.TIME_SET")) {
                            msg = TouchScreenService.this.tsSuperNodeHandler.obtainMessage();
                            msg.what = 3;
                            TouchScreenService.this.tsSuperNodeHandler.sendMessage(msg);
                        } else if (action.equals("com.vivo.touchscreen.AlarmColck")) {
                            msg = TouchScreenService.this.tsSuperNodeHandler.obtainMessage();
                            msg.what = TouchScreenService.VTS_TOUCH_INFO_COLLECT;
                            TouchScreenService.this.tsSuperNodeHandler.sendMessage(msg);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        this.mVivoIntentFilter = new IntentFilter();
        this.mVivoIntentFilter.addAction("com.vivo.daemonService.unifiedconfig.update_finish_broadcast_VTSG");
        this.mVivoIntentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mVivoIntentFilter.addAction("com.vivo.battlemode.touchscreen");
        this.mVivoIntentFilter.addAction("com.vivo.touchscreen.AlarmColck");
        this.mVivoIntentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        this.mVivoIntentFilter.addAction("android.intent.action.TIME_SET");
        this.mContext.registerReceiver(this.mVivoBroadcastReceiver, this.mVivoIntentFilter);
    }

    private void readXml() {
        new Thread() {
            public void run() {
                try {
                    TouchScreenService.this.getConfig("content://com.vivo.daemonservice.unifiedconfigprovider/configs", "VTSG", "1", "v1.0", "vivotsgwzryswitch");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.run();
    }

    private void getConfig(String uri, String moduleName, String type, String version, String identifier) {
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(Uri.parse(uri), null, null, new String[]{moduleName, type, version, identifier}, null);
            if (cursor != null) {
                String fileId = "";
                String tartgetIdentifier = "";
                String fileVersion = "";
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    while (!cursor.isAfterLast()) {
                        fileId = cursor.getString(cursor.getColumnIndex("id"));
                        tartgetIdentifier = cursor.getString(cursor.getColumnIndex("identifier"));
                        fileVersion = cursor.getString(cursor.getColumnIndex("fileversion"));
                        String applists = new String(cursor.getBlob(cursor.getColumnIndex("filecontent")), "UTF-8");
                        Slog.d(TAG, "getConfig VivoFakeWifiState.xml:\n  " + applists);
                        updateWzrySwitch(new StringReader(applists));
                        cursor.moveToNext();
                    }
                } else {
                    Slog.d(TAG, "getConfig nodata");
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Slog.d(TAG, "getConfig error:" + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private void updateWzrySwitch(StringReader reader) {
        Slog.d(TAG, "updateWzrySwitch start");
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(reader);
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                switch (eventType) {
                    case 0:
                        this.wzrySwitch = false;
                        break;
                    case 2:
                        if (parser.getName().equalsIgnoreCase(BaseList.STANDARD_LIST_ITEM_TAG)) {
                            if (!parser.nextText().contains("1")) {
                                this.wzrySwitch = false;
                                SetAppName("fwzrySwitch".getBytes());
                                Slog.d(TAG, "wzrySwitch is seteing to false");
                                break;
                            }
                            this.wzrySwitch = DBG;
                            SetAppName("twzrySwitch".getBytes());
                            Slog.d(TAG, "wzrySwitch is seteing to true");
                            break;
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Slog.d(TAG, "updateWzrySwitch end");
    }

    private void registerProcessObserver() {
        try {
            if (mIActivityManager != null) {
                mIActivityManager.registerProcessObserver(this.mProcessObserver);
            }
        } catch (RemoteException e) {
            Slog.d(TAG, "registerProcessObserver failed.");
        }
    }

    private String getAppNameFromUid(int uid) {
        int uidInt = uid;
        Slog.d(TAG, "pakage name is " + this.mContext.getPackageManager().getNameForUid(uid) + "with :" + uid);
        return this.mContext.getPackageManager().getNameForUid(uid);
    }

    public int SetAppName(final byte[] appName) {
        Slog.d(TAG, "called and appName is " + appName);
        new Thread(new Runnable() {
            public void run() {
                Slog.d(TouchScreenService.TAG, "app name Thread run");
                TouchScreenService.nativeSetAppName(appName);
            }
        }).start();
        return 0;
    }

    public String TouchReadSuperNode(String cmd) {
        String path = "/sys/touchscreen/ts_super_node";
        String result = "";
        File file = new File("/sys/touchscreen/ts_super_node");
        if (file.isFile() && file.exists()) {
            try {
                Integer.parseInt(cmd);
                try {
                    FileOutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(cmd.getBytes());
                    outputStream.flush();
                    outputStream.close();
                    try {
                        FileInputStream inputStream = new FileInputStream(file);
                        byte[] buffer = new byte[inputStream.available()];
                        int len = inputStream.read(buffer);
                        inputStream.close();
                        result = new String(buffer);
                    } catch (Exception e) {
                        Slog.e(TAG, "Fail to read from /sys/touchscreen/ts_super_node");
                        return result;
                    }
                } catch (Exception e2) {
                    Slog.e(TAG, "Fail to write " + cmd + " to " + "/sys/touchscreen/ts_super_node");
                    return result;
                }
            } catch (NumberFormatException e3) {
                Slog.e(TAG, "Invalid cmd" + cmd);
                return result;
            }
        }
        return result;
    }
}
