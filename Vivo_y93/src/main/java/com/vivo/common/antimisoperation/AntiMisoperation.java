package com.vivo.common.antimisoperation;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManagerInternal.DisplayPowerRequest;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.util.Slog;
import com.sensoroperate.SensorTestResult;
import com.sensoroperate.VivoSensorOperationResult;
import com.sensoroperate.VivoSensorOperationUtils;
import com.sensoroperate.VivoSensorTest;
import com.vivo.common.autobrightness.AblConfig;
import com.vivo.framework.facedetect.FaceDetectManager;

public class AntiMisoperation {
    private static final int ANTIMIS_DRAW_WAKE_LOCK_FLAG = 128;
    private static final int COMPARE_CONTAIN_CASE = 1;
    private static final int COMPARE_CONTAIN_NOCASE = 0;
    private static final int COMPARE_EQUALS_CASE = 3;
    private static final int COMPARE_EQUALS_NOCASE = 2;
    private static final boolean DEBUG;
    private static final boolean HAVE_HOLSTER_WINDOW = "Have_holster_with_window".equals(SystemProperties.get("persist.vivo.phone.holster"));
    public static final boolean IS_ENG = Build.TYPE.equals("eng");
    public static final boolean IS_LOG_CTRL_OPEN = SystemProperties.get("persist.sys.log.ctrl", "no").equals("yes");
    public static final String KEY_VIVO_LOG_CTRL = "persist.sys.log.ctrl";
    private static final int MSG_ANTIMISOPERATION_LOGOUT = 4;
    private static final int MSG_ANTIMISOPERATION_MMCHECK = 11;
    private static final int MSG_ANTIMISOPERATION_MOVEED = 3;
    private static final int MSG_ANTIMISOPERATION_QQCHECK = 8;
    private static final int MSG_ANTIMISOPERATION_TRIGGERED = 2;
    private static final int MSG_ANTIMISOPERATION_WAKELOCK_ACQUIRE = 5;
    private static final int MSG_ANTIMISOPERATION_WAKELOCK_RELEASE = 6;
    private static final int MSG_ANTIMISOPERATION_WAKELOCK_WATCHDOG = 7;
    private static final int MSG_CHANGE_PROXIMITY_PARAM_FOR_PHONE_STATE = 10;
    private static final int MSG_CHANGE_PROXIMITY_PARAM_FOR_SCREEN_OFF = 9;
    private static final int MSG_NOTIFY_PROXIMITY_SWITCH_THRES = 14;
    private static final int MSG_PHONE_STATUS_SENSOR_DISABLE = 12;
    private static final int MSG_PHONE_STATUS_SENSOR_ENABLE = 13;
    private static final int MSG_PROXIMITY_SENSOR_DISABLE = 0;
    private static final int MSG_PROXIMITY_SENSOR_ENABLE = 1;
    private static final int PROXIMITY_NEGATIVE = 0;
    private static final int PROXIMITY_POSITIVE = 1;
    private static final int PROXIMITY_SENSOR_THRES_CALL_FLAT = 3;
    private static final int PROXIMITY_SENSOR_THRES_CALL_GESTURE = 2;
    private static final int PROXIMITY_SENSOR_THRES_COMMON = 0;
    private static final int PROXIMITY_SENSOR_THRES_HIGH = 1;
    private static final int PROXIMITY_UNKNOWN = -1;
    private static final int SENSOR_COMMAND_SET_PS_PARA_INDEX = 9;
    private static final int STATE_PHONE_CALLING = 32;
    private static final int STATE_PROXIMITY_SCREEN_OFF_WAKE_LOCK = 16;
    private static final String TAG = "AntiMisoperation";
    private static int TYPE_ANGLE_DIRECTION = 169;
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final int WAKELOCK_WATDOG_KICKTIME = 30000;
    private static final String[] WHITELIST_BACKDOORS = new String[]{"SmartWakeLogoutAntiMisoperation"};
    private static final String[] WHITELIST_CONTAIN = new String[]{FaceDetectManager.CMD_FACE_DETECT_KEYGUARD};
    private static final String[] WHITELIST_EQUALS = new String[]{"BBKCrontab", "AlarmClock", "VoiceAssistantActivity-PWM", "PhoneGlobals", "RaiseUpWakeService", "PhoneApp", "SmartWake", "vivo_hall_bright", "MSimPhoneGlobals", "PhoneAppCrystal", "EngineerMode", "CalendarAlertWakeLock", "com.android.BBKClock", "com.vivo.agent"};
    private static final String[] WHITELIST_HALLLOCKED_EQUALS = new String[]{"BBKCrontab", "AlarmClock", "VoiceAssistantActivity-PWM", "PhoneGlobals", "PhoneApp", "SmartWake", "NotesAlertWakeLock", "CalendarAlertWakeLock", "sleepdialog", "vivo_hall_bright", "SmsWakeLock", "MSimPhoneGlobals"};
    private static final String[] WHITELIST_IGNORE_EPROXIMITY = new String[]{"PhoneAppCrystal"};
    private static int callerUid = 0;
    private static Context mAppContextImpl;
    private static final String mOpEntry = SystemProperties.get("ro.vivo.op.entry", "no");
    private static final String[] mProjectList = new String[]{"pd1516", "pd1602", "pd1603", "pd1522", "pd1612", "vtd1703f_ex"};
    private static final String[] mProjectList_equals = new String[0];
    public static boolean mUseAntiMisoperation = (isOpEntry() ^ 1);
    private static final String model = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, "unkown").toLowerCase();
    private boolean isHallLocked = false;
    private boolean isInWeChat = false;
    private boolean isQQ = false;
    private AntiMisoperationHandler mAntiMisoperationHandler;
    private AntiMisoperationCallback mCallback;
    private Context mContext;
    private int mCurrentThresState = -1;
    private SensorEventListener mEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            int i = 0;
            if (AntiMisoperation.this.mProximityEnabled) {
                float distance = event.values[0];
                boolean positive = distance >= 0.0f && distance < AntiMisoperation.this.mProximityThreshold;
                AntiMisoperation antiMisoperation = AntiMisoperation.this;
                if (positive) {
                    i = 1;
                }
                antiMisoperation.mProximity = i;
                Slog.d(AntiMisoperation.TAG, "got proximity event:" + AntiMisoperation.proximityToString(AntiMisoperation.this.mProximity));
                if (AntiMisoperation.this.getTriggered() && (positive ^ 1) != 0) {
                    if (AntiMisoperation.this.getWakelockTimeout()) {
                        Slog.d(AntiMisoperation.TAG, "onSensorChanged proximity negative after triggered but wakelock-timeout, not logout");
                        return;
                    }
                    Slog.d(AntiMisoperation.TAG, "onSensorChanged proximity negative after triggered, logout");
                    AntiMisoperation.this.mAntiMisoperationHandler.removeMessages(3);
                    AntiMisoperation.this.mAntiMisoperationHandler.sendMessage(AntiMisoperation.this.mAntiMisoperationHandler.obtainMessage(3));
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private HandlerThread mHandlerThread;
    private boolean mIsUseInstantCali = AblConfig.isUseInstantCali();
    private boolean mMonitorEnabled = false;
    private PhoneCallHandler mPhoneCallHandler;
    private HandlerThread mPhoneCallHandlerThread;
    private boolean mPhoneStateIdle = true;
    private boolean mPhoneStatusEnabled = false;
    private Handler mPhoneStatusEventHandler;
    private SensorEventListener mPhoneStatusListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            int phoneStatus = (int) event.values[0];
            if (AntiMisoperation.this.mPreviousPhoneStatus != phoneStatus) {
                AntiMisoperation.this.mPreviousPhoneStatus = phoneStatus;
                Slog.e(AntiMisoperation.TAG, "mPhoneStatusListener PhoneStatus = " + phoneStatus);
                AntiMisoperation.this.mPhoneCallHandler.removeMessages(14);
                Message stmsg = AntiMisoperation.this.mPhoneCallHandler.obtainMessage(14);
                if (phoneStatus == 3 || phoneStatus == 5 || phoneStatus == 6) {
                    stmsg.arg1 = 2;
                } else {
                    stmsg.arg1 = 3;
                }
                AntiMisoperation.this.mPhoneCallHandler.sendMessage(stmsg);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private boolean mPhoneStatusListenerRegisted = false;
    private Sensor mPhoneStatusSensor;
    private int mPreviousPhoneStatus = -1;
    private int mProximity = -1;
    private boolean mProximityEnabled = false;
    private Handler mProximityEventHandler;
    private boolean mProximityListenerRegisted = false;
    private Sensor mProximitySensor;
    private float mProximityThreshold;
    private SensorManager mSensorManager;
    private long mTimestamp = -1;
    private boolean mTriggered = false;
    private VivoSensorOperationUtils mVivoSensorOperationUtils;
    private VivoSensorTest mVivoSensorTest = VivoSensorTest.getInstance();
    private WakeLock mWakeLock;
    private int mWakelockKickCount = 0;
    private boolean mWakelockTimeout = false;
    private boolean mchangeParam = false;

    private final class AntiMisoperationHandler extends Handler {
        public AntiMisoperationHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            Slog.d(AntiMisoperation.TAG, "handleMessage " + AntiMisoperation.this.msgToString(msg.what));
            switch (msg.what) {
                case 0:
                    AntiMisoperation.this.setProximitySensorEnabledInner(false);
                    return;
                case 1:
                    AntiMisoperation.this.setProximitySensorEnabledInner(true);
                    return;
                case 2:
                    if (AntiMisoperation.this.mCallback != null) {
                        AntiMisoperation.this.mCallback.onTriggered(true);
                        return;
                    }
                    return;
                case 3:
                    AntiMisoperation.this.onAntiMisoperationMoved();
                    return;
                case 4:
                    AntiMisoperation.this.logoutAntiMisoperationInnger();
                    return;
                case 5:
                    AntiMisoperation.this.acquirePartialWakeLock();
                    return;
                case 6:
                    AntiMisoperation.this.releasePartialWakeLock();
                    return;
                case 7:
                    if (AntiMisoperation.this.mWakeLock.isHeld()) {
                        AntiMisoperation antiMisoperation = AntiMisoperation.this;
                        antiMisoperation.mWakelockKickCount = antiMisoperation.mWakelockKickCount + 1;
                        if (AntiMisoperation.this.mTimestamp != -1 && SystemClock.uptimeMillis() - AntiMisoperation.this.mTimestamp > 1800000) {
                            AntiMisoperation.this.mAntiMisoperationHandler.removeMessages(5);
                            AntiMisoperation.this.mAntiMisoperationHandler.removeMessages(6);
                            AntiMisoperation.this.mAntiMisoperationHandler.sendMessage(AntiMisoperation.this.mAntiMisoperationHandler.obtainMessage(6));
                            AntiMisoperation.this.mTimestamp = -1;
                        }
                        Slog.d(AntiMisoperation.TAG, "AntiMisoperationWakelockWARNING: wake held for too long. last for ( " + AntiMisoperation.this.mWakelockKickCount + "*" + AntiMisoperation.WAKELOCK_WATDOG_KICKTIME + " )ms");
                        AntiMisoperation.this.mAntiMisoperationHandler.sendMessageDelayed(AntiMisoperation.this.mAntiMisoperationHandler.obtainMessage(7), 30000);
                        return;
                    }
                    AntiMisoperation.this.mWakelockKickCount = 0;
                    return;
                case 8:
                    if (AntiMisoperation.isQQWorkSource()) {
                        AntiMisoperation.this.logoutAntiMisoperation(false);
                        return;
                    }
                    return;
                case 9:
                    if (AntiMisoperation.this.mIsUseInstantCali) {
                        if (AntiMisoperation.this.mchangeParam) {
                            AntiMisoperation.this.setPhoneStatusEnabled(true);
                        } else {
                            AntiMisoperation.this.setPhoneStatusEnabled(false);
                        }
                    }
                    AntiMisoperation.this.changeProximityParam(AntiMisoperation.this.mchangeParam, 16);
                    return;
                case 10:
                    AntiMisoperation.this.changeProximityParam(AntiMisoperation.this.mPhoneStateIdle ^ 1, 32);
                    return;
                case 11:
                    AntiMisoperation.this.isInWeChat = false;
                    AntiMisoperation.this.isQQ = false;
                    if (AntiMisoperation.isWeChatWorkSource()) {
                        AntiMisoperation.this.changeProximityParam(true, 16);
                        AntiMisoperation.this.isInWeChat = true;
                        return;
                    } else if (AntiMisoperation.isQQWorkSource()) {
                        AntiMisoperation.this.changeProximityParam(true, 16);
                        AntiMisoperation.this.isQQ = true;
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        }
    }

    private final class PhoneCallHandler extends Handler {
        public PhoneCallHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            Slog.d(AntiMisoperation.TAG, "handleMessage " + AntiMisoperation.this.msgToString(msg.what));
            switch (msg.what) {
                case 12:
                    AntiMisoperation.this.setPhoneStatusEnabledInner(false);
                    AntiMisoperation.this.mPhoneCallHandler.removeMessages(14);
                    Message stmsg = AntiMisoperation.this.mPhoneCallHandler.obtainMessage(14);
                    stmsg.arg1 = 0;
                    AntiMisoperation.this.mPhoneCallHandler.sendMessage(stmsg);
                    return;
                case 13:
                    AntiMisoperation.this.setPhoneStatusEnabledInner(true);
                    return;
                case 14:
                    Slog.e(AntiMisoperation.TAG, "Mikoto msg.arg1 = " + msg.arg1 + ", mCurrentThresState = " + AntiMisoperation.this.mCurrentThresState);
                    if (AntiMisoperation.this.mCurrentThresState != msg.arg1) {
                        AntiMisoperation.this.notifyProxmitySwitchThres(msg.arg1);
                    } else {
                        Slog.e(AntiMisoperation.TAG, "Mikoto mCts = " + AntiMisoperation.this.mCurrentThresState + ", no need to switch");
                    }
                    if (msg.arg1 == 0) {
                        AntiMisoperation.this.mCurrentThresState = -1;
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    static {
        boolean z;
        if (IS_LOG_CTRL_OPEN) {
            z = true;
        } else {
            z = IS_ENG;
        }
        DEBUG = z;
    }

    private static boolean isQQWorkSource() {
        try {
            if (mAppContextImpl.getPackageManager().getApplicationInfo("com.tencent.mobileqq", 1).uid == callerUid) {
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isWeChatWorkSource() {
        try {
            if (mAppContextImpl.getPackageManager().getApplicationInfo("com.tencent.mm", 1).uid != callerUid) {
                return false;
            }
            Slog.d(TAG, "I am in WeChat!");
            return true;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isOpEntry() {
        return !mOpEntry.equals("CMCC") ? mOpEntry.contains("_RW") : true;
    }

    public AntiMisoperation(Context context, AntiMisoperationCallback callback) {
        String ProxSensorName = "Null";
        String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
        this.mContext = context;
        mAppContextImpl = context;
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mCallback = callback;
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mAntiMisoperationHandler = new AntiMisoperationHandler(this.mHandlerThread.getLooper());
        this.mProximityEventHandler = new Handler(this.mHandlerThread.getLooper());
        this.mPhoneCallHandlerThread = new HandlerThread("PhoneCallOptimize");
        this.mPhoneCallHandlerThread.start();
        this.mPhoneCallHandler = new PhoneCallHandler(this.mPhoneCallHandlerThread.getLooper());
        this.mPhoneStatusEventHandler = new Handler(this.mPhoneCallHandlerThread.getLooper());
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "AntiMisoperationWakeLock");
        this.mWakeLock.setReferenceCounted(false);
        this.mProximitySensor = this.mSensorManager.getDefaultSensor(8);
        if (this.mProximitySensor != null) {
            ProxSensorName = this.mProximitySensor.getName();
        }
        if (ProxSensorName.toLowerCase().startsWith("stk3x1x") && prop.toLowerCase().startsWith("pd1731")) {
            this.mIsUseInstantCali = false;
            Slog.d(TAG, "Force mIsUseInstantCali false");
        }
        if (this.mProximitySensor != null) {
            this.mProximityThreshold = Math.min(this.mProximitySensor.getMaximumRange(), TYPICAL_PROXIMITY_THRESHOLD);
        }
        TYPE_ANGLE_DIRECTION = AblConfig.getAngleDirectonType();
        this.mPhoneStatusSensor = this.mSensorManager.getDefaultSensor(TYPE_ANGLE_DIRECTION);
        this.mVivoSensorOperationUtils = VivoSensorOperationUtils.getInstance();
        Slog.d(TAG, "called AntiMisOperation. mUseAntiMisoperation=" + mUseAntiMisoperation);
    }

    public static boolean isUseAntiMisoperation() {
        return mUseAntiMisoperation;
    }

    private void onAntiMisoperationMoved() {
        if (this.mCallback != null) {
            this.mCallback.onTriggered(false);
            this.mAntiMisoperationHandler.removeMessages(4);
            this.mAntiMisoperationHandler.sendMessage(this.mAntiMisoperationHandler.obtainMessage(4));
        }
    }

    private static void log(String msg) {
        if (DEBUG) {
            Slog.d(TAG, msg);
        }
    }

    private static String proximityToString(int state) {
        switch (state) {
            case -1:
                return "Unknown";
            case 0:
                return "Negative";
            case 1:
                return "Positive";
            default:
                return Integer.toString(state);
        }
    }

    String msgToString(int msg) {
        switch (msg) {
            case 0:
                return "MSG_PROXIMITY_SENSOR_DISABLE";
            case 1:
                return "MSG_PROXIMITY_SENSOR_ENABLE";
            case 2:
                return "MSG_ANTIMISOPERATION_TRIGGERED";
            case 3:
                return "MSG_ANTIMISOPERATION_MOVEED";
            case 4:
                return "MSG_ANTIMISOPERATION_LOGOUT";
            case 5:
                return "MSG_ANTIMISOPERATION_WAKELOCK_ACQUIRE";
            case 6:
                return "MSG_ANTIMISOPERATION_WAKELOCK_RELEASE";
            default:
                return "UnkownMsg:" + msg;
        }
    }

    private void setProximityEnabled(boolean enable) {
        if (enable != this.mProximityEnabled) {
            Slog.d(TAG, "setProximityEnabled(" + enable + ")");
            this.mProximityEnabled = enable;
            if (enable) {
                this.mProximity = -1;
                setTriggered(false);
                setWakelockTimeout(false);
                this.mAntiMisoperationHandler.removeMessages(0);
                this.mAntiMisoperationHandler.sendMessage(this.mAntiMisoperationHandler.obtainMessage(1));
                return;
            }
            this.mProximity = -1;
            setTriggered(false);
            setWakelockTimeout(false);
            this.mAntiMisoperationHandler.removeMessages(1);
            this.mAntiMisoperationHandler.sendMessage(this.mAntiMisoperationHandler.obtainMessage(0));
            this.mAntiMisoperationHandler.removeMessages(5);
            this.mAntiMisoperationHandler.removeMessages(6);
            this.mAntiMisoperationHandler.sendMessage(this.mAntiMisoperationHandler.obtainMessage(6));
        }
    }

    private boolean setProximitySensorEnabledInner(boolean enable) {
        if (enable != this.mProximityListenerRegisted) {
            if (enable) {
                boolean ret = this.mSensorManager.registerListener(this.mEventListener, this.mProximitySensor, 3, this.mProximityEventHandler);
                Slog.d(TAG, "ret = " + ret);
                this.mProximityListenerRegisted = ret;
            } else {
                this.mProximityListenerRegisted = false;
                this.mSensorManager.unregisterListener(this.mEventListener, this.mProximitySensor);
            }
        }
        return this.mProximityListenerRegisted;
    }

    private void setPhoneStatusEnabled(boolean enable) {
        if (enable != this.mPhoneStatusEnabled) {
            Slog.d(TAG, "Mikoto setPhoneStatusEnabled(" + enable + ")");
            this.mPhoneStatusEnabled = enable;
            if (enable) {
                this.mPreviousPhoneStatus = -1;
                this.mPhoneCallHandler.removeMessages(12);
                this.mPhoneCallHandler.sendMessage(this.mPhoneCallHandler.obtainMessage(13));
                return;
            }
            this.mPreviousPhoneStatus = -1;
            this.mPhoneCallHandler.removeMessages(13);
            this.mPhoneCallHandler.sendMessage(this.mPhoneCallHandler.obtainMessage(12));
        }
    }

    private boolean setPhoneStatusEnabledInner(boolean enable) {
        if (enable != this.mPhoneStatusListenerRegisted) {
            if (enable) {
                boolean ret = this.mSensorManager.registerListener(this.mPhoneStatusListener, this.mPhoneStatusSensor, 1, this.mPhoneStatusEventHandler);
                Slog.d(TAG, "mPhoneStatusListener register ret = " + ret);
                this.mPhoneStatusListenerRegisted = ret;
            } else {
                this.mPhoneStatusListenerRegisted = false;
                this.mSensorManager.unregisterListener(this.mPhoneStatusListener);
            }
        }
        return this.mPhoneStatusListenerRegisted;
    }

    public void enableMonitor(boolean enable) {
        if (!mUseAntiMisoperation) {
            return;
        }
        if (this.mCallback == null) {
            log("enableMonitor error mCallback is null");
        } else if (!enable || (this.mPhoneStateIdle && !this.mCallback.getUseProximity())) {
            if (enable != this.mMonitorEnabled) {
                if (enable) {
                    setProximityEnabled(true);
                } else {
                    setProximityEnabled(false);
                }
                this.mMonitorEnabled = enable;
            }
        } else {
            log("enableMonitor mPhoneStateIdle=" + this.mPhoneStateIdle + " getUseProximity=" + this.mCallback.getUseProximity() + " can not enable!!!");
        }
    }

    private void logoutAntiMisoperationInnger() {
        enableMonitor(false);
        if (this.mCallback != null) {
            this.mCallback.onLogout();
        }
    }

    private void acquirePartialWakeLock() {
        if (this.mWakeLock == null) {
            Slog.d(TAG, "acquirePartialWakeLock mWakeLock is null");
            return;
        }
        if (!this.mWakeLock.isHeld()) {
            this.mWakeLock.acquire();
            this.mTimestamp = SystemClock.uptimeMillis();
            this.mWakelockKickCount = 0;
            this.mAntiMisoperationHandler.removeMessages(7);
            this.mAntiMisoperationHandler.sendMessageDelayed(this.mAntiMisoperationHandler.obtainMessage(7), 30000);
            Slog.d(TAG, "mWakeLock.acquire");
        }
    }

    private void releasePartialWakeLock() {
        if (this.mWakeLock == null) {
            Slog.d(TAG, "releasePartialWakeLock mWakeLock is null");
            return;
        }
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
            this.mWakelockKickCount = 0;
            this.mAntiMisoperationHandler.removeMessages(7);
            Slog.d(TAG, "mWakeLock.release");
        }
    }

    public void logoutAntiMisoperation(boolean callCallback) {
        if (callCallback) {
            this.mAntiMisoperationHandler.removeMessages(4);
            this.mAntiMisoperationHandler.sendMessage(this.mAntiMisoperationHandler.obtainMessage(4));
            return;
        }
        enableMonitor(false);
    }

    public boolean getMonitorEnabled() {
        return this.mMonitorEnabled;
    }

    public boolean getTriggered() {
        if (this.mMonitorEnabled && this.mProximityEnabled) {
            return this.mTriggered;
        }
        return false;
    }

    private void setWakelockTimeout(boolean out) {
        this.mWakelockTimeout = out;
    }

    private boolean getWakelockTimeout() {
        return this.mWakelockTimeout;
    }

    public void setTriggered(boolean trig) {
        this.mTriggered = trig;
        if (trig) {
            this.mAntiMisoperationHandler.removeMessages(2);
            this.mAntiMisoperationHandler.sendMessage(this.mAntiMisoperationHandler.obtainMessage(2));
        }
        log("setTriggered(" + trig + ")");
    }

    public boolean getProximityStatus() {
        boolean z = true;
        if (!this.mProximityEnabled) {
            return false;
        }
        if (this.mProximity != 1) {
            z = false;
        }
        return z;
    }

    public void setProximityStatus(boolean mProximityStatus) {
        if (mProximityStatus) {
            this.mProximity = 1;
        } else {
            this.mProximity = 0;
        }
        log("mProximity =" + this.mProximity);
    }

    private boolean isInList(String tag, String[] list, int type) {
        if (tag == null || list == null) {
            return false;
        }
        for (String x : list) {
            switch (type) {
                case 0:
                    if (!x.toLowerCase().contains(tag.toLowerCase())) {
                        break;
                    }
                    return true;
                case 1:
                    if (!x.contains(tag)) {
                        break;
                    }
                    return true;
                case 2:
                    if (!x.equalsIgnoreCase(tag)) {
                        break;
                    }
                    return true;
                case 3:
                    if (!x.equals(tag)) {
                        break;
                    }
                    return true;
                default:
                    if (!x.equals(tag)) {
                        break;
                    }
                    return true;
            }
        }
        return false;
    }

    private boolean isNeedAntiMisoperation(String tag) {
        if (tag.equals("CalendarAlertWakeLock") || tag.equals("AlarmClock")) {
            if (mProjectList.length > 0) {
                for (String startsWith : mProjectList) {
                    if (model.startsWith(startsWith)) {
                        return true;
                    }
                }
            }
            if (mProjectList_equals.length > 0) {
                for (Object equals : mProjectList_equals) {
                    if (model.equals(equals)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isBrightFlag(int flags) {
        int bright = flags & 15;
        if (bright == 10 || bright == 6) {
            return true;
        }
        return false;
    }

    private boolean isPartialFlag(int flag) {
        if (((flag & 15) & 1) != 0) {
            return true;
        }
        return false;
    }

    private boolean isWhitelistAppTag(String tag) {
        if (isInList(tag, WHITELIST_CONTAIN, 0)) {
            return true;
        }
        if (isNeedAntiMisoperation(tag)) {
            return false;
        }
        if (this.isHallLocked && HAVE_HOLSTER_WINDOW) {
            if (isInList(tag, WHITELIST_HALLLOCKED_EQUALS, 3)) {
                return true;
            }
        } else if (isInList(tag, WHITELIST_EQUALS, 3)) {
            return true;
        }
        return false;
    }

    private boolean isBackdoorTag(String tag) {
        return isInList(tag, WHITELIST_BACKDOORS, 3);
    }

    private boolean isIgnoreProximityTag(String tag) {
        return isInList(tag, WHITELIST_IGNORE_EPROXIMITY, 3);
    }

    public boolean isNeedIngoreProximity(String tag) {
        return isIgnoreProximityTag(tag);
    }

    public boolean onAcquireWakeLock(int flags, String tag, String packageName, WorkSource ws, int uid, int pid) {
        boolean ret = false;
        log("onAcquireWakeLock flags=" + Integer.toHexString(flags) + " tag=" + tag + " packageName:" + packageName);
        log("onAcquireWakeLock mUseAntiMisoperation=" + mUseAntiMisoperation + " policy=" + DisplayPowerRequest.policyToString(this.mCallback.getCurrentDisplayPolicy()) + " mMonitorEnabled=" + this.mMonitorEnabled + " getTriggered=" + getTriggered());
        if (!mUseAntiMisoperation) {
            return false;
        }
        int po = this.mCallback.getCurrentDisplayPolicy();
        if (po != 0 && po != 1 && getMonitorEnabled()) {
            return false;
        }
        boolean isBrightWakelock = isBrightFlag(flags);
        if (isBrightWakelock) {
            Slog.d(TAG, "add to test bright wake lock flags = " + Integer.toHexString(flags));
            if (tag.equals("WindowManager")) {
                if (!(ws == null || ws.size() == 0)) {
                    callerUid = ws.get(0);
                }
                this.mAntiMisoperationHandler.sendMessage(this.mAntiMisoperationHandler.obtainMessage(11));
            }
        }
        if (!this.mMonitorEnabled) {
            return false;
        }
        boolean isProximityWakelock = (flags & 32) != 0;
        boolean isWhitelist = isWhitelistAppTag(tag);
        boolean isBackdoorList = isBackdoorTag(tag);
        boolean isPartialWakelook = isPartialFlag(flags);
        if (isBrightWakelock) {
            if (tag.equals("WindowManager")) {
                if (!(ws == null || ws.size() == 0)) {
                    callerUid = ws.get(0);
                }
                this.mAntiMisoperationHandler.sendMessage(this.mAntiMisoperationHandler.obtainMessage(8));
            }
            if (getTriggered()) {
                if (isWhitelist) {
                    log("onAcquireWakeLock logoutAntiMisoperation isWhitelist");
                    logoutAntiMisoperation(false);
                } else if (isProximityWakelock) {
                    log("onAcquireWakeLock logoutAntiMisoperation isProximityWakelock");
                    logoutAntiMisoperation(false);
                } else if (this.mProximity == 1) {
                    setWakelockTimeout(false);
                    ret = true;
                    this.mAntiMisoperationHandler.removeMessages(5);
                    this.mAntiMisoperationHandler.removeMessages(6);
                    this.mAntiMisoperationHandler.sendMessage(this.mAntiMisoperationHandler.obtainMessage(5));
                    log("onAcquireWakeLock getTriggered and keep current.");
                } else {
                    Slog.d(TAG, "onAcquireWakeLock logoutAntiMisoperation mProximity=" + this.mProximity);
                    logoutAntiMisoperation(false);
                }
            } else if (isWhitelist) {
                log("onAcquireWakeLock not triggered and enableMonitor(false): isWhitelist=true");
                enableMonitor(false);
            } else if (isProximityWakelock) {
                log("onAcquireWakeLock not triggered and enableMonitor(false): isProximityWakelock=true");
                enableMonitor(false);
            } else if (getProximityStatus()) {
                Slog.d(TAG, "onAcquireWakeLock setTriggered(true).");
                setTriggered(true);
                this.mAntiMisoperationHandler.removeMessages(5);
                this.mAntiMisoperationHandler.removeMessages(6);
                this.mAntiMisoperationHandler.sendMessage(this.mAntiMisoperationHandler.obtainMessage(5));
                ret = true;
            } else {
                log("onAcquireWakeLock not triggered and enableMonitor(false): getProximityStatus=false");
                enableMonitor(false);
            }
        } else if (isPartialWakelook && isBackdoorList) {
            log("logoutAntiMisoperation as backdoors:" + tag);
            logoutAntiMisoperation(false);
        } else {
            log("onAcquireWakeLock not bright,keep current:isBrightWakelock=" + isBrightWakelock + " isWhitelist=" + isWhitelist + " getMonitorEnabled=" + getMonitorEnabled() + " getTriggered=" + getTriggered() + " getWakelockTimeout=" + getWakelockTimeout());
        }
        return ret;
    }

    /* JADX WARNING: Missing block: B:9:0x0018, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onReleaseWakelock() {
        if (mUseAntiMisoperation && getMonitorEnabled() && (getTriggered() ^ 1) == 0 && this.mCallback != null && this.mCallback.countBrightFullLocks() == 0) {
            setWakelockTimeout(true);
            this.mAntiMisoperationHandler.removeMessages(5);
            this.mAntiMisoperationHandler.removeMessages(6);
            this.mAntiMisoperationHandler.sendMessage(this.mAntiMisoperationHandler.obtainMessage(6));
        }
    }

    public void setHallStatus(boolean lock) {
        int policy = this.mCallback.getCurrentDisplayPolicy();
        this.isHallLocked = lock;
        Slog.d(TAG, "setHallStatus policy" + policy + "getMonitorEnabled() = " + getMonitorEnabled());
        if (lock || !mUseAntiMisoperation || !getMonitorEnabled()) {
            return;
        }
        if (policy == 0 || policy == 1) {
            logoutAntiMisoperation(true);
        } else {
            logoutAntiMisoperation(false);
        }
    }

    public void setPhoneCallState(boolean idle) {
        this.mPhoneStateIdle = idle;
        this.mAntiMisoperationHandler.removeMessages(10);
        this.mAntiMisoperationHandler.sendMessage(this.mAntiMisoperationHandler.obtainMessage(10));
    }

    private boolean notifyProxmitySwitchThres(int thresState) {
        VivoSensorOperationResult operationRes = new VivoSensorOperationResult();
        int[] mOperationArgs = new int[3];
        mOperationArgs[0] = 200;
        mOperationArgs[1] = thresState;
        if (this.mVivoSensorOperationUtils != null) {
            int retry = 0;
            while (retry < 3) {
                try {
                    this.mVivoSensorOperationUtils.executeCommand(mOperationArgs[0], operationRes, mOperationArgs, mOperationArgs.length);
                    retry++;
                } catch (Exception e) {
                    Slog.d(TAG, "Fail to notify the sensors");
                }
            }
            this.mCurrentThresState = thresState;
        }
        Slog.d(TAG, "Mikoto notifyProxmitySwitchThres, thresState = " + thresState + ", mCurrentThresState = " + this.mCurrentThresState);
        return true;
    }

    private void changeProximityParam(boolean change, int state) {
        if (!this.mIsUseInstantCali) {
            SensorTestResult mTempRes = new SensorTestResult();
            int[] mTempTestArg = new int[3];
            mTempTestArg[0] = 9;
            if (change || this.isInWeChat || this.isQQ) {
                mTempTestArg[1] = 1;
            } else {
                mTempTestArg[1] = 0;
            }
            mTempTestArg[2] = state;
            this.mVivoSensorTest.vivoSensorTest(45, mTempRes, mTempTestArg, mTempTestArg.length);
        }
    }

    public void notifyChangeProximityParam(boolean change) {
        if (this.mchangeParam != change) {
            this.mchangeParam = change;
            this.mAntiMisoperationHandler.removeMessages(9);
            this.mAntiMisoperationHandler.sendMessage(this.mAntiMisoperationHandler.obtainMessage(9));
        }
    }
}
