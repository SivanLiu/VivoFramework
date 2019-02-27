package com.vivo.framework.facedetect;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import java.util.ArrayList;

public class PhoneWindowNotifyFace {
    private static final String BATTERY_REBOOT = "battery";
    public static final int FACEUNLOCK_STAGE_NONE = 0;
    public static final int FACEUNLOCK_STAGE_POWER_STARTAUTH_BEGIN = 2;
    public static final int FACEUNLOCK_STAGE_POWER_STARTAUTH_FINISH = 0;
    public static final int FACEUNLOCK_STAGE_POWER_WAKEUP = 1;
    public static final int KEYGUARD_STATUS_EXIT = 3;
    public static final int KEYGUARD_STATUS_HIDE = 2;
    public static final int KEYGUARD_STATUS_SHOW = 1;
    private static final String LONG_POWER_REBOOT = "long_power";
    private static final String LONG_VOLUME_REBOOT = "long_volume_down";
    private static final int MOTION_STATE_MOVE = 2;
    private static final int MOTION_STATE_STILL = 1;
    private static final String NONE_REBOOT = "none";
    private static final String PROP_USER_REBOOT = "persist.vivo.finger.reboot";
    private static final String RECOVERY_REBOOT = "recovery";
    private static final String SILENT_REBOOT = "silent";
    private static final String SILENT_UPDATE = "silent_update";
    private static final String TAG = "PhoneWindowNotifyFace";
    private static final String TYPE_MOVE_WAKE = "move_wake";
    private static final String USER_REBOOT = "user";
    public static final int WINDOW_EVENT_DISMISS_GLOBALACTIONS = 24;
    public static final int WINDOW_EVENT_FINGERPRINT_LOCKED = 7;
    public static final int WINDOW_EVENT_FINGER_FAILED = 15;
    public static final int WINDOW_EVENT_FINGER_SUCCESSED = 14;
    public static final int WINDOW_EVENT_KEYGUARD_EXIT = 6;
    public static final int WINDOW_EVENT_KEYGUARD_FOCUSED = 16;
    public static final int WINDOW_EVENT_KEYGUARD_HIDE = 3;
    public static final int WINDOW_EVENT_KEYGUARD_LOSEFOCUS = 17;
    public static final int WINDOW_EVENT_KEYGUARD_PW_CHANGED = 10;
    public static final int WINDOW_EVENT_KEYGUARD_SHOW = 4;
    public static final int WINDOW_EVENT_KEY_FACE_DOWN = 11;
    public static final int WINDOW_EVENT_KEY_FACE_UP = 12;
    public static final int WINDOW_EVENT_MOVE_WAKE_MOVE = 22;
    public static final int WINDOW_EVENT_MOVE_WAKE_STILL = 21;
    public static final int WINDOW_EVENT_SCREEN_DOZE = 13;
    public static final int WINDOW_EVENT_SCREEN_OFF = 2;
    public static final int WINDOW_EVENT_SCREEN_ON = 1;
    public static final int WINDOW_EVENT_SCREEN_ON_FINISHED = 9;
    public static final int WINDOW_EVENT_SHOW_GLOBALACTIONS = 23;
    public static final int WINDOW_EVENT_SMART_KEY_OPENED = 8;
    public static final int WINDOW_EVENT_SMART_WAKE = 20;
    public static final int WINDOW_EVENT_SOFT_KEYBOARD_HIDE = 18;
    public static final int WINDOW_EVENT_SOFT_KEYBOARD_SHOW = 19;
    public static final int WINDOW_EVENT_SYSTEM_REBOOT = 5;
    private static boolean isSupportFinger = ("0".equals(SystemProperties.get("persist.sys.fptype", "0")) ^ 1);
    private static Object mLock = new Object();
    private static PhoneWindowNotifyFace mPhoneWindowNotifyFace = null;
    private FaceDetectNotify mFaceDetectNotify;
    private ArrayList<FaceUnlockDisplayStatus> mFaceUnlocDisplayStatuskListener = new ArrayList();
    private ArrayList<FaceUnlockStatus> mFaceUnlocStatuskListener = new ArrayList();
    private ArrayList<FaceUnlockKeyguard> mFaceUnlockOpListener = new ArrayList();
    private FaceUnlockStage mFaceUnlockStageListener = null;
    private ArrayList<FaceUnlockKeyguardStatus> mFaceUnlockStatusListener = new ArrayList();
    private KeyguardManager mKeyguardManager;
    private boolean mKeyguardOccluded = false;
    private boolean mKeyguardShowing = false;
    private String mRebootReason = "none";
    private String[] rebootReasons = new String[]{"user", LONG_POWER_REBOOT, RECOVERY_REBOOT, BATTERY_REBOOT, LONG_VOLUME_REBOOT};

    public interface FaceDetectNotify {
        boolean isFaceOrFingerLocked();

        void notifyFaceDetectKeyHandler(int i);

        void notifyFaceUnlockStart();

        void notifyFingerResult(int i, int i2);

        void onBacklightStateChanged(int i, int i2);
    }

    public interface FaceUnlockDisplayStatus {
        void turnOffDisplay();

        void turnOnDisplay();

        void updateDisplay();

        void wakeUp();
    }

    public interface FaceUnlockKeyguard {
        void keyguardDone();

        void keyguardHide();
    }

    public interface FaceUnlockKeyguardStatus {
        void onFaceUnlockKeyguardState(int i);
    }

    public interface FaceUnlockStage {
        void notifyFillLightState(boolean z);
    }

    public interface FaceUnlockStatus {
        void updateStateChanged(int i);
    }

    private PhoneWindowNotifyFace() {
    }

    public static PhoneWindowNotifyFace getInstance() {
        if (mPhoneWindowNotifyFace == null) {
            synchronized (mLock) {
                if (mPhoneWindowNotifyFace == null) {
                    mPhoneWindowNotifyFace = new PhoneWindowNotifyFace();
                }
            }
        }
        return mPhoneWindowNotifyFace;
    }

    public void setFaceNotifyListener(FaceDetectNotify mFaceDetectNotify) {
        this.mFaceDetectNotify = mFaceDetectNotify;
    }

    public void facePolicy(int result) {
        notifyObserver(result);
    }

    public boolean isFaceOrFingerLocked() {
        if (this.mFaceDetectNotify != null) {
            return this.mFaceDetectNotify.isFaceOrFingerLocked();
        }
        return false;
    }

    public void onBacklightStateChanged(int state, int backlight) {
        if (this.mFaceDetectNotify != null) {
            this.mFaceDetectNotify.onBacklightStateChanged(state, backlight);
        }
    }

    public void notifyFaceFingerResult(int errorcode, int retrytimes) {
        if (this.mFaceDetectNotify != null) {
            this.mFaceDetectNotify.notifyFingerResult(errorcode, retrytimes);
        }
    }

    private void notifyObserver(int result) {
        Slog.i(TAG, "notifyObserver result:" + result);
        if (this.mFaceDetectNotify != null) {
            this.mFaceDetectNotify.notifyFaceDetectKeyHandler(result);
        } else {
            Slog.i(TAG, "mFaceDetectNotify==null");
        }
    }

    private void clearRebootProp() {
        Slog.i(TAG, "clearRebootProp");
        SystemProperties.set(PROP_USER_REBOOT, "none");
    }

    private boolean isSilentRebootAndUpdate() {
        this.mRebootReason = SystemProperties.get(PROP_USER_REBOOT, "none");
        Slog.i(TAG, "detectRebootReason : " + this.mRebootReason);
        if (SILENT_REBOOT.equals(this.mRebootReason) || SILENT_UPDATE.equals(this.mRebootReason)) {
            return true;
        }
        return false;
    }

    public boolean detectRebootReason() {
        boolean isUserReboot = true;
        if (isSilentRebootAndUpdate()) {
            isUserReboot = false;
        }
        if (!isSupportFinger) {
            clearRebootProp();
        }
        return isUserReboot;
    }

    private boolean checkRebootReason(String rebootReason) {
        boolean isUserReboot = false;
        for (String reason : this.rebootReasons) {
            if (rebootReason.equals(reason)) {
                isUserReboot = true;
                break;
            }
        }
        Slog.i(TAG, "get reboot reason isUserReboot is " + isUserReboot);
        return isUserReboot;
    }

    public void registerFaceUnlockCallBack(FaceUnlockStatus listener) {
        if (listener != null && !this.mFaceUnlocStatuskListener.contains(listener)) {
            this.mFaceUnlocStatuskListener.add(listener);
        }
    }

    public void unRegisterFaceUnlockCallBack(FaceUnlockStatus listener) {
        if (listener != null && (this.mFaceUnlocStatuskListener.contains(listener) ^ 1) == 0) {
            this.mFaceUnlocStatuskListener.remove(listener);
        }
    }

    public void notifyFaceUnlockStatus(int stage) {
        for (FaceUnlockStatus item : this.mFaceUnlocStatuskListener) {
            item.updateStateChanged(stage);
        }
    }

    public void registerFaceUnlockDisplayStatus(FaceUnlockDisplayStatus listener) {
        if (listener != null && !this.mFaceUnlocDisplayStatuskListener.contains(listener)) {
            this.mFaceUnlocDisplayStatuskListener.add(listener);
        }
    }

    public void unRegisterFaceUnlockDisplayStatus(FaceUnlockDisplayStatus listener) {
        if (listener != null && (this.mFaceUnlocDisplayStatuskListener.contains(listener) ^ 1) == 0) {
            this.mFaceUnlocDisplayStatuskListener.remove(listener);
        }
    }

    public void notifyFaceUnlockWakeUp() {
        for (FaceUnlockDisplayStatus item : this.mFaceUnlocDisplayStatuskListener) {
            item.wakeUp();
        }
    }

    public void notifyFaceUnlockTurnOffDisplay() {
        for (FaceUnlockDisplayStatus item : this.mFaceUnlocDisplayStatuskListener) {
            item.turnOffDisplay();
        }
    }

    public void notifyFaceUnlockTurnOnDisplay() {
        for (FaceUnlockDisplayStatus item : this.mFaceUnlocDisplayStatuskListener) {
            item.turnOnDisplay();
        }
    }

    public void notifyFaceUnlockUpdateDisplay() {
        for (FaceUnlockDisplayStatus item : this.mFaceUnlocDisplayStatuskListener) {
            item.updateDisplay();
        }
    }

    public void registerFaceUnlockKeyguardCallback(FaceUnlockKeyguard listener) {
        if (listener != null && !this.mFaceUnlockOpListener.contains(listener)) {
            this.mFaceUnlockOpListener.add(listener);
        }
    }

    public void unRegisterFaceUnlockKeyguardCallback(FaceUnlockKeyguard listener) {
        if (listener != null && (this.mFaceUnlockOpListener.contains(listener) ^ 1) == 0) {
            this.mFaceUnlockOpListener.remove(listener);
        }
    }

    public void notifyFaceUnlockKeyguardHide() {
        for (FaceUnlockKeyguard item : this.mFaceUnlockOpListener) {
            item.keyguardHide();
        }
    }

    public void notifyFaceUnlockKeyguardDone() {
        for (FaceUnlockKeyguard item : this.mFaceUnlockOpListener) {
            item.keyguardDone();
        }
    }

    public void notifyFaceUnlockStart() {
        if (this.mFaceDetectNotify != null) {
            this.mFaceDetectNotify.notifyFaceUnlockStart();
        }
    }

    public void registerFaceUnlockStageCallBack(FaceUnlockStage listener) {
        this.mFaceUnlockStageListener = listener;
    }

    public void unRegisterFaceUnlockStageCallback(FaceUnlockStage listener) {
        this.mFaceUnlockStageListener = null;
    }

    public void notifyBrightnessChange(boolean changed) {
        if (this.mFaceUnlockStageListener != null) {
            this.mFaceUnlockStageListener.notifyFillLightState(changed);
        }
    }

    public void registerFaceUnlockKeyguardStatusCallBack(FaceUnlockKeyguardStatus listener) {
        if (listener != null && !this.mFaceUnlockStatusListener.contains(listener)) {
            this.mFaceUnlockStatusListener.add(listener);
        }
    }

    public void unRegisterFaceUnlockKeyguardStatusCallBack(FaceUnlockKeyguardStatus listener) {
        if (listener != null && (this.mFaceUnlockStatusListener.contains(listener) ^ 1) == 0) {
            this.mFaceUnlockStatusListener.remove(listener);
        }
    }

    public void notifyKeyguardStatus(int stage) {
        for (FaceUnlockKeyguardStatus item : this.mFaceUnlockStatusListener) {
            item.onFaceUnlockKeyguardState(stage);
        }
    }

    public void onKeyguardShown(boolean showing, Context context) {
        Slog.i(TAG, "onKeyguardShown: " + showing + " mKeyguardOccluded: " + this.mKeyguardOccluded);
        if (showing) {
            if (!this.mKeyguardShowing) {
                this.mKeyguardShowing = true;
            }
            if (!this.mKeyguardOccluded && this.mKeyguardShowing) {
                notifyObserver(4);
            }
        } else {
            if (this.mKeyguardManager == null && context != null) {
                this.mKeyguardManager = (KeyguardManager) context.getSystemService(FaceDetectManager.CMD_FACE_DETECT_KEYGUARD);
            }
            if (this.mKeyguardManager == null) {
                Slog.e(TAG, "onKeyguardShown: KeyguardManager invalid");
            } else if (!this.mKeyguardManager.isKeyguardLocked()) {
                this.mKeyguardShowing = false;
                notifyObserver(6);
            }
        }
    }

    public void onKeyguardOccluded(boolean occluded) {
        Slog.i(TAG, "onKeyguardOccluded: " + occluded + " mKeyguardShowing: " + this.mKeyguardShowing);
        this.mKeyguardOccluded = occluded;
        if (occluded || (this.mKeyguardShowing ^ 1) != 0) {
            notifyObserver(3);
        } else if (this.mKeyguardShowing) {
            notifyObserver(4);
        }
    }

    public void notifySoftKeyboardShown(boolean show) {
        Slog.i(TAG, "notifySoftKeyboardShown: " + show);
        if (show) {
            notifyObserver(19);
        } else {
            notifyObserver(18);
        }
    }

    public void notifySensorMessage(String type, int state) {
        Slog.i(TAG, "notifySensorMessage: type: " + type + " state: " + state);
        if (!TYPE_MOVE_WAKE.equals(type)) {
            return;
        }
        if (1 == state) {
            notifyObserver(21);
        } else if (2 == state) {
            notifyObserver(22);
        }
    }
}
