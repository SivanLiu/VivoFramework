package com.vivo.common.fingerprinthook;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Slog;
import com.vivo.common.autobrightness.AblConfig;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.common.provider.Weather;
import java.util.HashMap;
import vivo.app.VivoFrameworkFactory;
import vivo.app.nightpearl.AbsNightPearlManager;

public class FingerprintWakeHook {
    private static final boolean DEBUG = SystemProperties.getBoolean("debug.finger.wakehook", true);
    private static long DELAY_AFTER_FAILED = WakeHookConfig.getAuthFailedDelay();
    private static long DELAY_AFTER_SUCCESS = WakeHookConfig.getAuthSuccessDelay();
    private static int DELAY_FOR_BRIGHT = 100;
    private static final int DELAY_FOR_GOTOSLEEP = 50;
    public static final String[] FINGERKEY_LIST = new String[]{FINGERPRINT_WAKEUP};
    public static final String FINGERPRINT_INIT = "Init";
    public static final String FINGERPRINT_LOGOUT = "Logout";
    public static final String FINGERPRINT_LOUGOUT_PROXIMITY = "proximity";
    public static final String FINGERPRINT_LOUGOUT_WAKELOCK = "wakeLock";
    public static final String FINGERPRINT_LOUGOUT_WAKEUP = "wakeUp";
    private static int FINGERPRINT_TYPE = SystemProperties.getInt(PROP_FINGERPRINT_TYPE, -1);
    private static final int FINGERPRINT_TYPE_FPC = 1;
    private static final int FINGERPRINT_TYPE_GOODIX = 2;
    private static final int FINGERPRINT_TYPE_NONE = -1;
    private static String FINGERPRINT_TYPE_STRING = SystemProperties.get(PROP_FINGERPRINT_TYPE, "default");
    private static final String FINGERPRINT_WAKEUP = "FingerPrint";
    private static final int FINGER_TOUCH_UNLOCK_DISABLED = 0;
    private static final int FINGER_TOUCH_UNLOCK_ENABLED = 1;
    public static final int FP_AUTH_ERROR = 0;
    public static final int FP_AUTH_FAILED = 1;
    public static final int FP_AUTH_FAILED_AND_LIGHT_ON = 4;
    public static final int FP_AUTH_HELP = 2;
    public static final int FP_AUTH_IDLE = -2;
    public static final int FP_AUTH_ING = -1;
    public static final int FP_AUTH_ING_WITH_PHYSICAL_WAKE = 10;
    public static final int FP_AUTH_SUCCESS = 3;
    public static final int KEYGUARD_HIDE = 1;
    public static final int KEYGUARD_SHOW = 0;
    private static final String MODEL = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, "unkown");
    private static final int MSG_NOTIFIER_BLOCK = 1;
    private static final int MSG_NOTIFIER_TIMEOUT = 3;
    private static final int MSG_REGISTER_OBSERVER = 4;
    private static final int MSG_WAKE_HOOK_AUTH_RESULT = 7;
    private static final int MSG_WAKE_HOOK_FAILED_AND_LIGHT_ON = 8;
    private static final int MSG_WAKE_HOOK_HAPPENED = 5;
    private static final int MSG_WAKE_HOOK_ING_WITH_PHYSICAL_WAKE = 9;
    private static final int MSG_WAKE_HOOK_SUCCESS = 2;
    private static final int MSG_WAKE_HOOK_SUCCESS_NO_WAKE = 3;
    private static final int MSG_WAKE_HOOK_TIMEOUT = 1;
    private static final int MSG_WAKE_HOOK_WAKEUP_FINISHED = 6;
    private static final long PHYSICAL_WAKE_AFTER_SUCCESS_INTERVAL = 200;
    private static final String[] PHYSICAL_WAKE_SOURCES = new String[]{WAKE_SOURCE_WAKE_KEY, WAKE_SOURCE_HOME_KEY, FINGERPRINT_LOGOUT};
    private static final String PROP_BRIGHT_DELAY = "persist.sys.fpbrdelay";
    private static final String PROP_FAIL_THREE_STATUS = "persist.vivo.fp.bright";
    private static final String PROP_FAIL_THREE_STATUS_SYS = "sys.vivo.fp.bright";
    private static final String PROP_FINGERPRINT_TYPE = "persist.sys.fptype";
    private static final String PROP_KEY_KEYGUARD_FINGER = "sys.fingerprint.keguard";
    private static final int PROP_VAL_FAILED_REPEATLY = 3;
    private static final int SETTING_DISABLE_FINGER_UNLOCK = 0;
    private static final int SETTING_ENABLE_FINGER_UNLOCK = 1;
    private static final String SETTING_FINGER_PRESS_KEY_UNLOCK = "finger_press_key_unlock";
    private static final String SETTING_FINGER_UNLOCK_OPEN = "finger_unlock_open";
    private static final String TAG = "FingerprintWakeHook";
    private static final String WAKE_SOURCE_HOME_KEY = "HomeKey";
    private static final String WAKE_SOURCE_WAKE_KEY = "WakeKey";
    private static int mAnimatedBacklight = 255;
    private static boolean mBlockNotifier = false;
    private static FingerprintWakeHookCollectData mCollectData;
    private static ColorFadeOffAnimator mColorFadeOffAnimator = null;
    private static Context mContext = null;
    private static WakeHookHandler mHandler;
    private static FingerprintWakeHook mInstance = null;
    private static long mLastSuccessResultTime = -1;
    private static Object mLock = new Object();
    private static NotifierHandler mNotifierHandler;
    private int DELAY_FOR_TIMEOUT;
    private long end;
    private boolean identifying;
    private boolean mEnableUnlock;
    private boolean mFingerTouchUnlockEnabled;
    private boolean mFingerWakeUpKey;
    private int mFpAuthStatus;
    private boolean mFpWakingUp;
    private boolean mHasFingerprinitWake;
    private AbsNightPearlManager mNightPearlManager;
    private SettingOberserver mObserver;
    private boolean mRegisteredObeserver;
    private boolean mSleep;
    private HandlerThread mThread;
    private long mTimeGoToSleep;
    private FingerprintWakeHookCallback mWakeHookCallback;
    private boolean mWakingUp;
    private boolean mWakingUpByFinger;
    private long start;

    public interface ColorFadeOffAnimator {
        boolean isStarted();
    }

    private static class NotifierHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    FingerprintWakeHook.mBlockNotifier = true;
                    FingerprintWakeHook.log("handleMessage MSG_NOTIFIER_BLOCK");
                    return;
                case 3:
                    FingerprintWakeHook.mBlockNotifier = false;
                    FingerprintWakeHook.log("handleMessage MSG_NOTIFIER_TIMEOUT");
                    return;
                default:
                    return;
            }
        }
    }

    private class SettingOberserver extends ContentObserver {
        public SettingOberserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            synchronized (FingerprintWakeHook.mLock) {
                FingerprintWakeHook.this.handleSettingsChangedLocked();
            }
        }
    }

    private class WakeHookHandler extends Handler {
        public WakeHookHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            Object -get2;
            switch (msg.what) {
                case 1:
                    Slog.d(FingerprintWakeHook.TAG, "handleMessage MSG_WAKE_HOOK_TIMEOUT");
                    synchronized (FingerprintWakeHook.mLock) {
                        if (FingerprintWakeHook.this.mWakeHookCallback != null) {
                            if (!WakeHookConfig.isUseWindowHideFunction()) {
                                FingerprintWakeHook.this.showKeyguard();
                            }
                            Slog.d(FingerprintWakeHook.TAG, "handleMessage goToSleep");
                            FingerprintWakeHook.this.mWakeHookCallback.goToSleep();
                            FingerprintWakeHook.this.mTimeGoToSleep = SystemClock.uptimeMillis();
                        }
                    }
                    if (FingerprintWakeHook.this.identifying) {
                        FingerprintWakeHook.this.identifying = false;
                        return;
                    }
                    return;
                case 2:
                    Slog.d(FingerprintWakeHook.TAG, "handleMessage MSG_WAKE_HOOK_SUCCESS");
                    synchronized (FingerprintWakeHook.mLock) {
                        FingerprintWakeHook.this.mWakingUp = true;
                        FingerprintWakeHook.this.mWakingUpByFinger = true;
                        if (FingerprintWakeHook.this.mWakeHookCallback != null) {
                            FingerprintWakeHook.this.hideKeyguard();
                        }
                        FingerprintWakeHook.this.logoutWakeHookLocked();
                        if (FingerprintWakeHook.this.mWakeHookCallback != null) {
                            Slog.d(FingerprintWakeHook.TAG, "handleMessage wakeUp");
                            FingerprintWakeHook.this.mWakeHookCallback.wakeUp();
                        }
                        FingerprintWakeHook.mHandler.removeMessages(5);
                    }
                    if (FingerprintWakeHook.this.identifying) {
                        FingerprintWakeHook.this.identifying = false;
                        FingerprintWakeHook.this.end = System.currentTimeMillis();
                        FingerprintWakeHook.this.setCollectData();
                        return;
                    }
                    return;
                case 3:
                    Slog.d(FingerprintWakeHook.TAG, "handleMessage MSG_WAKE_HOOK_SUCCESS_NO_WAKE");
                    synchronized (FingerprintWakeHook.mLock) {
                        FingerprintWakeHook.this.logoutWakeHookLocked();
                        FingerprintWakeHook.mHandler.removeMessages(5);
                        if (FingerprintWakeHook.this.isPolicyOff() || FingerprintWakeHook.this.isLcmOff()) {
                            Slog.d(FingerprintWakeHook.TAG, "handleMessage wakeUp");
                            FingerprintWakeHook.this.hideKeyguard();
                            FingerprintWakeHook.this.mWakingUp = true;
                            FingerprintWakeHook.this.mWakingUpByFinger = true;
                            FingerprintWakeHook.this.mWakeHookCallback.wakeUp();
                        } else {
                            Slog.d(FingerprintWakeHook.TAG, "handleMessage MSG_WAKE_HOOK_SUCCESS_NO_WAKE wakeUp");
                            FingerprintWakeHook.this.mWakeHookCallback.wakeUp();
                        }
                    }
                    if (FingerprintWakeHook.this.identifying) {
                        FingerprintWakeHook.this.identifying = false;
                        return;
                    }
                    return;
                case 4:
                    Slog.d(FingerprintWakeHook.TAG, "handleMessage MSG_REGISTER_OBSERVER");
                    -get2 = FingerprintWakeHook.mLock;
                    synchronized (-get2) {
                        FingerprintWakeHook.this.handleSettingsChangedLocked();
                        FingerprintWakeHook.this.registerOberserver();
                        break;
                    }
                case 5:
                    -get2 = FingerprintWakeHook.mLock;
                    synchronized (-get2) {
                        Slog.d(FingerprintWakeHook.TAG, "handleMessage MSG_WAKE_HOOK_HAPPENED");
                        FingerprintWakeHook.this.setHasFingerprintWake(true);
                        FingerprintWakeHook.this.setFpAuthStatus(-1);
                        break;
                    }
                case 6:
                    -get2 = FingerprintWakeHook.mLock;
                    synchronized (-get2) {
                        FingerprintWakeHook.this.mWakingUp = false;
                        FingerprintWakeHook.this.mWakingUpByFinger = false;
                        Slog.d(FingerprintWakeHook.TAG, "handleMessage MSG_WAKE_HOOK_WAKEUP_FINISHED");
                        break;
                    }
                case 7:
                    -get2 = FingerprintWakeHook.mLock;
                    synchronized (-get2) {
                        int result = msg.arg1;
                        Slog.d(FingerprintWakeHook.TAG, "handleMessage MSG_WAKE_HOOK_AUTH_RESULT(" + result + ")");
                        FingerprintWakeHook.this.handleFingerPrintResult(result);
                        break;
                    }
                case 8:
                    Slog.d(FingerprintWakeHook.TAG, "handleMessage MSG_WAKE_HOOK_FAILED_AND_LIGHT_ON");
                    synchronized (FingerprintWakeHook.mLock) {
                        FingerprintWakeHook.this.logoutWakeHookLocked();
                        FingerprintWakeHook.mHandler.removeMessages(5);
                        if (FingerprintWakeHook.this.isPolicyOff() || FingerprintWakeHook.this.isLcmOff()) {
                            Slog.d(FingerprintWakeHook.TAG, "handleMessage wakeUp");
                            FingerprintWakeHook.this.mWakingUp = true;
                            FingerprintWakeHook.this.mWakeHookCallback.wakeUp();
                        } else {
                            Slog.d(FingerprintWakeHook.TAG, "handleMessage MSG_WAKE_HOOK_FAILED_AND_LIGHT_ON userActivity");
                            FingerprintWakeHook.this.mWakeHookCallback.userActivity();
                        }
                    }
                    if (FingerprintWakeHook.this.identifying) {
                        FingerprintWakeHook.this.identifying = false;
                        return;
                    }
                    return;
                case 9:
                    Slog.d(FingerprintWakeHook.TAG, "handleMessage MSG_WAKE_HOOK_ING_WITH_PHYSICAL_WAKE");
                    synchronized (FingerprintWakeHook.mLock) {
                        FingerprintWakeHook.this.logoutWakeHookLocked();
                        FingerprintWakeHook.mHandler.removeMessages(5);
                        if (FingerprintWakeHook.this.isPolicyOff() || FingerprintWakeHook.this.isLcmOff()) {
                            Slog.d(FingerprintWakeHook.TAG, "handleMessage wakeUp");
                            FingerprintWakeHook.this.mWakingUp = true;
                            FingerprintWakeHook.this.mWakeHookCallback.wakeUp();
                        } else {
                            Slog.d(FingerprintWakeHook.TAG, "handleMessage MSG_WAKE_HOOK_FAILED_AND_LIGHT_ON userActivity");
                            FingerprintWakeHook.this.mWakeHookCallback.userActivity();
                        }
                    }
                    if (FingerprintWakeHook.this.identifying) {
                        FingerprintWakeHook.this.identifying = false;
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private FingerprintWakeHook(Looper looper) {
        this.DELAY_FOR_TIMEOUT = 800;
        this.mFpAuthStatus = -2;
        this.mFpWakingUp = false;
        this.mSleep = false;
        this.mHasFingerprinitWake = false;
        this.mWakeHookCallback = null;
        this.mRegisteredObeserver = false;
        this.mObserver = null;
        this.mEnableUnlock = false;
        this.mFingerTouchUnlockEnabled = true;
        this.mWakingUp = false;
        this.mWakingUpByFinger = false;
        this.mFingerWakeUpKey = false;
        this.identifying = false;
        this.start = 0;
        this.end = 0;
        this.mTimeGoToSleep = 0;
        mHandler = new WakeHookHandler(looper);
        getDisplayDelay();
    }

    private FingerprintWakeHook() {
        this.DELAY_FOR_TIMEOUT = 800;
        this.mFpAuthStatus = -2;
        this.mFpWakingUp = false;
        this.mSleep = false;
        this.mHasFingerprinitWake = false;
        this.mWakeHookCallback = null;
        this.mRegisteredObeserver = false;
        this.mObserver = null;
        this.mEnableUnlock = false;
        this.mFingerTouchUnlockEnabled = true;
        this.mWakingUp = false;
        this.mWakingUpByFinger = false;
        this.mFingerWakeUpKey = false;
        this.identifying = false;
        this.start = 0;
        this.end = 0;
        this.mTimeGoToSleep = 0;
        this.mThread = new HandlerThread(TAG);
        this.mThread.start();
        mHandler = new WakeHookHandler(this.mThread.getLooper());
        getDisplayDelay();
    }

    private void getDisplayDelay() {
        if ("PD1515A".equals(MODEL)) {
            DELAY_FOR_BRIGHT = 0;
        } else {
            DELAY_FOR_BRIGHT = 0;
        }
    }

    private static Context getContext() {
        return mContext;
    }

    private static void setContext(Context context) {
        mContext = context;
        mHandler.sendEmptyMessage(4);
        mCollectData = FingerprintWakeHookCollectData.getInstance(mContext);
    }

    public static FingerprintWakeHook getInstance() {
        if (mInstance == null) {
            synchronized (mLock) {
                if (mInstance == null) {
                    mInstance = new FingerprintWakeHook();
                }
            }
        }
        return mInstance;
    }

    public static FingerprintWakeHook getInstance(Context context, Handler handler) {
        log("FingerprintWakeHook getInstance");
        if (mInstance == null || getContext() == null) {
            synchronized (mLock) {
                if (mInstance == null) {
                    mInstance = new FingerprintWakeHook(handler.getLooper());
                    setContext(context);
                } else if (getContext() == null) {
                    setContext(context);
                }
            }
        }
        return mInstance;
    }

    public static FingerprintWakeHook getInstance(Context context) {
        if (mInstance == null || getContext() == null) {
            synchronized (mLock) {
                if (mInstance == null) {
                    mInstance = new FingerprintWakeHook();
                    setContext(context);
                } else if (getContext() == null) {
                    setContext(context);
                }
            }
        }
        return mInstance;
    }

    private boolean isNeedWakeHook() {
        return hasFingerprint() ? this.mEnableUnlock : false;
    }

    private void registerOberserver() {
        if (!this.mRegisteredObeserver) {
            ContentResolver resolver = mContext.getContentResolver();
            this.mObserver = new SettingOberserver(mHandler);
            resolver.registerContentObserver(System.getUriFor(SETTING_FINGER_UNLOCK_OPEN), false, this.mObserver, -1);
            resolver.registerContentObserver(System.getUriFor(SETTING_FINGER_PRESS_KEY_UNLOCK), false, this.mObserver, -1);
            this.mRegisteredObeserver = true;
        }
    }

    private void handleSettingsChangedLocked() {
        boolean z;
        boolean z2 = true;
        if (1 == System.getInt(mContext.getContentResolver(), SETTING_FINGER_UNLOCK_OPEN, 0)) {
            z = true;
        } else {
            z = false;
        }
        this.mEnableUnlock = z;
        FINGERPRINT_TYPE = SystemProperties.getInt(PROP_FINGERPRINT_TYPE, -1);
        FINGERPRINT_TYPE_STRING = SystemProperties.get(PROP_FINGERPRINT_TYPE, "default");
        Slog.d(TAG, "handleSettingsChangedLocked mEnableUnlock=" + this.mEnableUnlock + " type=" + FINGERPRINT_TYPE + " :" + FINGERPRINT_TYPE_STRING);
        if (System.getInt(mContext.getContentResolver(), SETTING_FINGER_PRESS_KEY_UNLOCK, 1) != 1) {
            z2 = false;
        }
        this.mFingerTouchUnlockEnabled = z2;
        Slog.d(TAG, "handleSettingsChangedLocked mEnableUnlock=" + this.mEnableUnlock + " type=" + FINGERPRINT_TYPE + " :" + FINGERPRINT_TYPE_STRING + " press=" + this.mFingerTouchUnlockEnabled);
        if (this.mFingerTouchUnlockEnabled) {
            this.DELAY_FOR_TIMEOUT = 800;
        } else {
            this.DELAY_FOR_TIMEOUT = WakeHookConfig.FINGERPRINT_PRESS_UNLOCK_TIMEOUT;
        }
    }

    private static void log(String msg) {
        if (DEBUG) {
            Slog.d(TAG, msg);
        }
    }

    public void setupCallback(FingerprintWakeHookCallback callback) {
        this.mWakeHookCallback = callback;
    }

    private static boolean hasFingerprint() {
        boolean conf = SystemProperties.getBoolean("persist.vivo.fp_wakehook", true);
        if ((FINGERPRINT_TYPE != -1 || (FINGERPRINT_TYPE_STRING.startsWith("default") ^ 1) != 0) && conf) {
            return true;
        }
        return false;
    }

    private void setHasFingerprintWake(boolean has) {
        this.mFpWakingUp = has;
        this.mHasFingerprinitWake = has;
        removeTimeoutMessage();
        if (has) {
            mHandler.sendEmptyMessageDelayed(1, (long) this.DELAY_FOR_TIMEOUT);
        }
        Slog.d(TAG, "setHasFingerprintWake " + has);
    }

    public boolean blockBrightnessOn() {
        boolean z = false;
        if (!isNeedWakeHook()) {
            return false;
        }
        if (!(!this.mHasFingerprinitWake || this.mFpAuthStatus == -2 || this.mFpAuthStatus == 3)) {
            z = true;
        }
        return z;
    }

    public boolean blockNotifier() {
        if (isNeedWakeHook()) {
            return mBlockNotifier;
        }
        return false;
    }

    private boolean isPolicyOff() {
        return this.mWakeHookCallback != null && this.mWakeHookCallback.getCurrentDisplayPolicy() == 0;
    }

    private void handleFingerPrintResult(int result) {
        if (this.mHasFingerprinitWake) {
            Slog.d(TAG, "sendFingerprintResult result:" + authStatusToString(result));
            if (3 == result) {
                removeAllMessages();
                Slog.d(TAG, "sendFingerprintResultDelayed result:" + authStatusToString(result));
                if (DELAY_AFTER_SUCCESS > 0) {
                    mHandler.sendEmptyMessageDelayed(2, DELAY_AFTER_SUCCESS);
                } else {
                    mHandler.sendEmptyMessage(2);
                }
            } else if (4 == result) {
                removeAllMessages();
                if (!WakeHookConfig.isUseWindowHideFunction()) {
                    showKeyguard();
                }
                Slog.d(TAG, "sendFingerprintResultDelayed result:" + authStatusToString(result));
                if (DELAY_AFTER_FAILED > 0) {
                    mHandler.sendEmptyMessageDelayed(8, DELAY_AFTER_FAILED);
                } else {
                    mHandler.sendEmptyMessage(8);
                }
            } else if (10 == result) {
                removeAllMessages();
                Slog.d(TAG, "sendFingerprintResultDelayed result:" + authStatusToString(result));
                if (DELAY_AFTER_FAILED > 0) {
                    mHandler.sendEmptyMessageDelayed(9, DELAY_AFTER_FAILED);
                } else {
                    mHandler.sendEmptyMessage(9);
                }
            } else {
                setFpAuthStatus(result);
            }
        } else if (3 == result) {
            removeAllMessages();
            Slog.d(TAG, "sendFingerprintResult result:" + authStatusToString(result) + " no WAKEUP but success!");
            if (DELAY_AFTER_SUCCESS > 0) {
                mHandler.sendEmptyMessageDelayed(3, DELAY_AFTER_SUCCESS);
            } else {
                mHandler.sendEmptyMessage(3);
            }
        } else if (4 == result) {
            removeAllMessages();
            if (!WakeHookConfig.isUseWindowHideFunction()) {
                showKeyguard();
            }
            Slog.d(TAG, "sendFingerprintResult result:" + authStatusToString(result) + " no WAKEUP but light on!");
            if (DELAY_AFTER_FAILED > 0) {
                mHandler.sendEmptyMessageDelayed(8, DELAY_AFTER_FAILED);
            } else {
                mHandler.sendEmptyMessage(8);
            }
        } else if (10 == result) {
            removeAllMessages();
            Slog.d(TAG, "sendFingerprintResultDelayed result:" + authStatusToString(result));
            if (DELAY_AFTER_FAILED > 0) {
                mHandler.sendEmptyMessageDelayed(9, DELAY_AFTER_FAILED);
            } else {
                mHandler.sendEmptyMessage(9);
            }
        } else {
            Slog.d(TAG, "sendFingerprintResult result:" + authStatusToString(result) + " no WAKEUP, ignore");
        }
    }

    private boolean isPhysicalWakeJustAfterSuccess() {
        if (mLastSuccessResultTime < 0) {
            return false;
        }
        long now = SystemClock.uptimeMillis();
        if (now <= mLastSuccessResultTime) {
            return false;
        }
        long interval = now - mLastSuccessResultTime;
        boolean ret = interval < PHYSICAL_WAKE_AFTER_SUCCESS_INTERVAL;
        log("isPhysicalWakeJustAfterSuccess: now=" + now + " interval=" + interval + " ret=" + ret);
        return ret;
    }

    public void sendFingerprintResult(int result) {
        if (!isNeedWakeHook()) {
            log("Not Need Wake Hook, sendFingerprintResult result:" + authStatusToString(result) + " press=" + this.mFingerTouchUnlockEnabled);
        } else if (this.mFingerTouchUnlockEnabled) {
            if (3 == result) {
                mLastSuccessResultTime = SystemClock.uptimeMillis();
            }
            Message msg = mHandler.obtainMessage(7);
            msg.arg1 = result;
            mHandler.sendMessage(msg);
        } else {
            log("sendFingerprintResult touch: " + this.mFingerTouchUnlockEnabled);
        }
    }

    private static String authStatusToString(int status) {
        switch (status) {
            case -2:
                return "FP_AUTH_IDLE";
            case -1:
                return "FP_AUTH_ING";
            case 0:
                return "FP_AUTH_ERROR";
            case 1:
                return "FP_AUTH_FAILED";
            case 2:
                return "FP_AUTH_HELP";
            case 3:
                return "FP_AUTH_SUCCESS";
            case 4:
                return "FP_AUTH_FAILED_AND_LIGHT_ON";
            case 10:
                return "FP_AUTH_ING_WITH_PHYSICAL_WAKE";
            default:
                return "unknown";
        }
    }

    private void setFpAuthStatus(int status) {
        switch (status) {
            case -2:
            case -1:
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                this.mFpAuthStatus = status;
                Slog.d(TAG, "setFpAuthStatus mFpAuthStatus=" + authStatusToString(this.mFpAuthStatus));
                break;
        }
        if (this.mFpAuthStatus == 1 || this.mFpAuthStatus == 0) {
            removeAllMessages();
            if (this.mWakingUp) {
                log("setFpAuthStatus mWakingUp, ignore " + authStatusToString(this.mFpAuthStatus));
            } else {
                mHandler.sendEmptyMessage(1);
            }
        } else if (this.mFpAuthStatus == 3) {
            removeAllMessages();
            mHandler.sendEmptyMessageDelayed(2, (long) DELAY_FOR_BRIGHT);
        } else if (4 == status) {
            removeAllMessages();
            showKeyguard();
            mHandler.sendEmptyMessageDelayed(8, 150);
        }
    }

    private void removeTimeoutMessage() {
        mHandler.removeMessages(1);
    }

    private void removeAllMessages() {
        mHandler.removeMessages(1);
    }

    private void sendNotifierBlock() {
        if (mNotifierHandler != null) {
            mBlockNotifier = true;
            mNotifierHandler.removeMessages(3);
            log("sendNotifierBlock send MSG_NOTIFIER_BLOCK");
            mNotifierHandler.sendEmptyMessageDelayed(3, (long) this.DELAY_FOR_TIMEOUT);
            return;
        }
        Slog.d(TAG, "sendNotifierBlock mNotifierHandler is null!");
    }

    public void logoutWakeHookLocked() {
        Slog.d(TAG, "clear wake hook");
        sendNotifierPass();
        setHasFingerprintWake(false);
        setFpAuthStatus(-2);
    }

    private void sendNotifierPass() {
        mBlockNotifier = false;
        if (mNotifierHandler != null) {
            mNotifierHandler.removeMessages(3);
        } else {
            Slog.d(TAG, "sendNotifierPass mNotifierHandler is null!");
        }
    }

    private boolean isLcmOff() {
        if (this.mWakeHookCallback == null) {
            return false;
        }
        if (this.mWakeHookCallback.getCurrentDisplayPolicy() == 0 || this.mWakeHookCallback.getAnimatedBrightness() == 0 || mAnimatedBacklight == 0) {
            return true;
        }
        return isFadeOffAnimatorStarted();
    }

    private void hideKeyguard() {
        if (!(this.mWakeHookCallback == null || (isFpAuthFailedRepeatedly() ^ 1) == 0 || (mAnimatedBacklight != 0 && !isFadeOffAnimatorStarted()))) {
            Slog.d(TAG, "hideKeyguard");
            this.mWakeHookCallback.hideKeyguardByFingerprint(1);
        }
        if (mContext != null) {
            if (this.mNightPearlManager == null && VivoFrameworkFactory.getFrameworkFactoryImpl() != null) {
                this.mNightPearlManager = VivoFrameworkFactory.getFrameworkFactoryImpl().getNightPearlManager();
            }
            if (this.mNightPearlManager != null) {
                this.mNightPearlManager.onShowOff(3);
                return;
            } else {
                Slog.d(TAG, "hideKeyguard mNightPearlManager:null");
                return;
            }
        }
        Slog.d(TAG, "hideKeyguard mContext:null");
    }

    private void showKeyguard() {
        if (this.mWakeHookCallback != null) {
            Slog.d(TAG, "showKeyguard");
            this.mWakeHookCallback.hideKeyguardByFingerprint(0);
        }
    }

    public static void createNotifierHandler() {
        if (mNotifierHandler == null) {
            mNotifierHandler = new NotifierHandler();
        }
    }

    public static void setAnimatedBacklight(int backlight) {
        boolean currentOn = backlight != 0;
        boolean prevOn = mAnimatedBacklight != 0;
        mAnimatedBacklight = backlight;
        if (currentOn != prevOn) {
            Slog.d(TAG, "setAnimatedBacklight backlight=" + mAnimatedBacklight);
        }
    }

    public Object getLock() {
        return mLock;
    }

    public static boolean isFingerKey(String key) {
        if (key == null || Events.DEFAULT_SORT_ORDER.equals(key)) {
            return false;
        }
        for (String k : FINGERKEY_LIST) {
            if (k.equals(key)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPhysiscalWakeSource(String who) {
        for (String source : PHYSICAL_WAKE_SOURCES) {
            if (source.equals(who)) {
                return true;
            }
        }
        return false;
    }

    public void onWakeUpByWho(long eventTime, String who, int uid, long ident) {
        Slog.d(TAG, "onWakeUpByWho: eventTime:" + eventTime + " who:" + who + " uid:" + uid + " ident:" + ident);
        createNotifierHandler();
        if (!FINGERPRINT_INIT.equals(who)) {
            if (!isNeedWakeHook()) {
                Slog.d(TAG, "Need to do nothing.");
            } else if (FINGERPRINT_WAKEUP.equals(who)) {
                this.mFingerWakeUpKey = true;
                if (this.mWakingUp) {
                    log("onWakeUpByWho mWakingUp return");
                    return;
                }
                if (isLcmOff()) {
                    if (!WakeHookConfig.isUseWindowHideFunction()) {
                        hideKeyguard();
                    }
                    sendNotifierBlock();
                    mHandler.sendEmptyMessage(5);
                    if (!this.identifying) {
                        this.start = System.currentTimeMillis();
                        this.identifying = true;
                    }
                } else {
                    this.start = System.currentTimeMillis();
                    log("onWakeUpByWho screen is light on, return.");
                }
            } else {
                mHandler.removeMessages(5);
                mBlockNotifier = false;
                mNotifierHandler.removeMessages(3);
                boolean justSuccess = isPhysicalWakeJustAfterSuccess();
                boolean shouldWaitShowKeyguard = false;
                if (this.mWakingUp) {
                    justSuccess = true;
                }
                if (!justSuccess) {
                    if (!WakeHookConfig.isUseWindowHideFunction()) {
                        showKeyguard();
                    }
                    if (-1 != this.mFpAuthStatus || (WakeHookConfig.isUseWindowHideFunction() ^ 1) == 0) {
                        log("onWakeUpByWho: showKeyguard mFpAuthStatus:" + authStatusToString(this.mFpAuthStatus));
                    } else {
                        shouldWaitShowKeyguard = true;
                    }
                }
                if (shouldWaitShowKeyguard) {
                    sendFingerprintResult(10);
                } else {
                    removeAllMessages();
                    this.mHasFingerprinitWake = false;
                    setHasFingerprintWake(false);
                    this.mFpAuthStatus = -2;
                }
                if (this.mWakeHookCallback != null) {
                    this.mWakeHookCallback.setFingerFlagDirty();
                }
                setSleep(false);
                this.identifying = false;
                if (isPhysiscalWakeSource(who)) {
                    this.mWakingUp = true;
                }
            }
        }
    }

    public void onWakeUpFinish() {
        if (isNeedWakeHook()) {
            mHandler.removeMessages(6);
            mHandler.sendEmptyMessage(6);
        }
    }

    public void setSleep(boolean sleep) {
        if (isNeedWakeHook()) {
            this.mSleep = sleep;
        }
    }

    private void setCollectData() {
        HashMap<String, String> params = new HashMap();
        if (mCollectData != null) {
        }
    }

    public static boolean isFpAuthFailedRepeatedly() {
        boolean ret = 3 == SystemProperties.getInt(PROP_KEY_KEYGUARD_FINGER, 0);
        boolean failedWhenScreenOff3Times = SystemProperties.getBoolean(PROP_FAIL_THREE_STATUS, false);
        boolean failedWhenScreenOff3TimesSys = SystemProperties.getBoolean(PROP_FAIL_THREE_STATUS_SYS, false);
        log("isFpAuthFailedRepeatedly:" + ret + " off3:" + failedWhenScreenOff3Times);
        return (ret || failedWhenScreenOff3Times) ? true : failedWhenScreenOff3TimesSys;
    }

    public boolean isWakeUpByFinger() {
        return this.mWakingUpByFinger;
    }

    public boolean isFingerWakeUpKey() {
        boolean fingerSleep = false;
        int elapsed = (int) (SystemClock.uptimeMillis() - this.mTimeGoToSleep);
        if (elapsed > 0 && elapsed < Weather.WEATHERVERSION_ROM_2_0) {
            log("Screen on quickly after finger sleep");
            fingerSleep = true;
        }
        return (this.mFingerWakeUpKey || this.mWakingUpByFinger) ? true : fingerSleep;
    }

    public void setFingerWakeUpKeyDefault() {
        this.mFingerWakeUpKey = false;
    }

    public void setColorFadeOffAnimator(ColorFadeOffAnimator animator) {
        if (animator != null) {
            mColorFadeOffAnimator = animator;
        }
    }

    private boolean isFadeOffAnimatorStarted() {
        if (mColorFadeOffAnimator == null) {
            return false;
        }
        Slog.d(TAG, "isFadeOffAnimatorStarted:" + mColorFadeOffAnimator.isStarted());
        return mColorFadeOffAnimator.isStarted();
    }
}
