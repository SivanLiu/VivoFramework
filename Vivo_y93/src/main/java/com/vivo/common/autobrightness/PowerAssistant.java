package com.vivo.common.autobrightness;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.Slog;
import com.vivo.common.autobrightness.CameraLumaCallback.AppBrightnessCallback;
import com.vivo.common.autobrightness.CameraLumaCallback.PowerAssistantCallback;

public class PowerAssistant {
    private static final String KEY_CAM_BRIGHT_MODE = "cam_bright_mode";
    private static final String KEY_POWER_BRIGHTNESS_MODE = "power_screen_brightness_mode_in_user";
    private static final String KEY_POWER_SAVE_TYPE = "power_save_type";
    public static final int POWER_ASSISTANT_APP_BRIGHT_MODE = 3;
    public static final int POWER_ASSISTANT_CAM_OPTIMIZE_MODE = 2;
    public static final int POWER_ASSISTANT_NORMAL = 0;
    public static final int POWER_ASSISTANT_SMART_SAVING_MODE = 1;
    public static final int POWER_BRIGHTNESS_MODE_AUTO = 1;
    public static final int POWER_BRIGHTNESS_MODE_USER = 0;
    private static final String TAG = "PowerAssistant";
    public static final int TYPE_APP_BRIGHT = 6;
    public static final int TYPE_CAM = 5;
    public static final int TYPE_CPU_MODE = 0;
    public static final int TYPE_DEFAULT = 1;
    public static final int TYPE_SMART = 2;
    public static final int TYPE_SUPER = 3;
    public static final int TYPE_USER = 4;
    private static BrightnessModeSettingsObserver mBrightnessModeSettingObserver;
    private static CamSettingsObserver mCamSettingObserver;
    private static int mPowerAssistantMode = 0;
    private static SettingsObserver mSettingObserver;
    private AppBrightnessCallback mAppBrightnessCallback = new AppBrightnessCallback() {
        public void onAppBrightModeChanged(boolean enable) {
            Slog.d(PowerAssistant.TAG, "0ops onAppBrightModeChanged - " + enable);
            PowerAssistant.this.handleTypeModeChangeForAppBright(enable ? 6 : 1);
        }
    };
    private PowerAssistantCallback mCallback;
    private ContentResolver mContentResolver;
    private Context mContext;
    private Handler mHandler;
    private Object mLock = new Object();
    private int mPowerBrightnessMode = 1;
    private int mPowerSaveType = 1;
    private int mPowerSaveTypeForAppBright = 1;
    private int mPowerSaveTypeForCamOptimize = 1;
    private Runnable mRegisterRunnable = new Runnable() {
        public void run() {
            PowerAssistant.this.updateSettings();
            PowerAssistant.this.registerObserver();
        }
    };
    private int mScreenBrightnessMode = 0;

    private final class BrightnessModeSettingsObserver extends ContentObserver {
        public BrightnessModeSettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            synchronized (PowerAssistant.this.mLock) {
                PowerAssistant.this.updateSettings();
            }
        }
    }

    private final class CamSettingsObserver extends ContentObserver {
        public CamSettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            synchronized (PowerAssistant.this.mLock) {
                PowerAssistant.this.handleSettingsChangedLockedForCamOptimize();
            }
        }
    }

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            synchronized (PowerAssistant.this.mLock) {
                PowerAssistant.this.handleSettingsChangedLocked();
            }
        }
    }

    public PowerAssistant(Context context, Handler handler, PowerAssistantCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
        this.mHandler = handler;
        if (this.mCallback == null) {
            Slog.d(TAG, "AutoBrightnessERROR: mCallback is null.");
        }
        mSettingObserver = new SettingsObserver(this.mHandler);
        mCamSettingObserver = new CamSettingsObserver(this.mHandler);
        mBrightnessModeSettingObserver = new BrightnessModeSettingsObserver(this.mHandler);
        this.mContentResolver = context.getContentResolver();
        int type = getPowerSaveType();
        int mode = getPowerBrightnessMode();
        if (1 != type) {
            handleTypeModeChange(type, mode);
        }
        this.mHandler.post(this.mRegisterRunnable);
    }

    private void log(String msg) {
        if (AblConfig.isDebug()) {
            Slog.d(TAG, msg);
        }
    }

    private void handleTypeModeChange(int type, int brightnessMode) {
        if (type == this.mPowerSaveType && brightnessMode == this.mPowerBrightnessMode) {
            log("handleTypeModeChange same mode.");
            return;
        }
        this.mPowerSaveType = type;
        this.mPowerBrightnessMode = brightnessMode;
        Slog.d(TAG, "handleTypeChange type changed to " + this.mPowerSaveType + " while camo type is " + this.mPowerSaveTypeForCamOptimize);
        if (this.mCallback != null) {
            this.mCallback.onPowerSaveTypeChanged(getPowerAssistantMode(type, brightnessMode));
            return;
        }
        Slog.d(TAG, "handleTypeChange mCallback is null");
    }

    private void handleTypeModeChangeForCamOptimize(int type, int brightnessMode) {
        if (type == this.mPowerSaveTypeForCamOptimize && brightnessMode == this.mPowerBrightnessMode) {
            log("handleTypeModeChange camo same mode.");
            return;
        }
        this.mPowerSaveTypeForCamOptimize = type;
        this.mPowerBrightnessMode = brightnessMode;
        Slog.d(TAG, "handleTypeChange camo type changed to " + this.mPowerSaveTypeForCamOptimize + " while com type is " + this.mPowerSaveType);
        if (this.mCallback != null) {
            int mode = getCamOptimizeMode(type, brightnessMode);
            if (this.mScreenBrightnessMode == 0 && 2 == mode) {
                Slog.d(TAG, "man mode");
                return;
            } else {
                this.mCallback.onPowerSaveTypeChanged(mode);
                return;
            }
        }
        Slog.d(TAG, "handleTypeChange camo mCallback is null");
    }

    private void handleTypeModeChangeForAppBright(int type) {
        if (type != this.mPowerSaveTypeForAppBright || type == 6) {
            this.mPowerSaveTypeForAppBright = type;
            Slog.d(TAG, "handleTypeChange ab type changed to " + this.mPowerSaveTypeForAppBright + " while com type is " + this.mPowerSaveType);
            if (this.mCallback != null) {
                int mode = getAppBrightMode(type);
                if (this.mScreenBrightnessMode == 0 && 3 == mode) {
                    Slog.d(TAG, "man mode");
                    return;
                } else {
                    this.mCallback.onPowerSaveTypeChanged(mode);
                    return;
                }
            }
            Slog.d(TAG, "handleTypeChange ab mCallback is null");
            return;
        }
        log("handleTypeModeChange ab same mode.");
    }

    private int getPowerSaveType() {
        int type = 1;
        try {
            type = System.getInt(this.mContentResolver, KEY_POWER_SAVE_TYPE, 1);
            Slog.d(TAG, "getPowerSaveType type=" + type);
            return type;
        } catch (Exception e) {
            Slog.d(TAG, "handleSettingsChangedLocked failed.");
            return type;
        }
    }

    private int getCamOptimizeType() {
        int type = 1;
        try {
            if (Global.getInt(this.mContentResolver, KEY_CAM_BRIGHT_MODE, 0) == 1) {
                type = 5;
            }
            Slog.d(TAG, "getCamOptimizeType type=" + type);
        } catch (Exception e) {
            Slog.d(TAG, "handleSettingsChangedLocked failed.");
        }
        return type;
    }

    private int getPowerBrightnessMode() {
        int brightnessMode = 1;
        try {
            brightnessMode = System.getInt(this.mContentResolver, KEY_POWER_BRIGHTNESS_MODE, 1);
            Slog.d(TAG, "getPowerBrightnessMode brightnessMode=" + brightnessMode);
            return brightnessMode;
        } catch (Exception e) {
            Slog.d(TAG, "getPowerBrightnessMode failed.");
            return brightnessMode;
        }
    }

    private void handleSettingsChangedLocked() {
        handleTypeModeChange(getPowerSaveType(), getPowerBrightnessMode());
    }

    private void handleSettingsChangedLockedForCamOptimize() {
        handleTypeModeChangeForCamOptimize(getCamOptimizeType(), getPowerBrightnessMode());
    }

    private void registerObserver() {
        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.registerContentObserver(System.getUriFor(KEY_POWER_SAVE_TYPE), false, mSettingObserver, -1);
        resolver.registerContentObserver(System.getUriFor(KEY_POWER_BRIGHTNESS_MODE), false, mSettingObserver, -1);
        resolver.registerContentObserver(Global.getUriFor(KEY_CAM_BRIGHT_MODE), false, mCamSettingObserver, -1);
        resolver.registerContentObserver(System.getUriFor("screen_brightness_mode"), false, mBrightnessModeSettingObserver, -1);
    }

    private void updateSettings() {
        try {
            this.mScreenBrightnessMode = System.getIntForUser(this.mContentResolver, "screen_brightness_mode", 0, -2);
        } catch (Exception e) {
            Slog.d(TAG, "updateSettings failed.");
        }
    }

    private static int getPowerAssistantMode(int type, int brightnessMode) {
        int mode = 0;
        if (2 == type) {
            mode = 1;
        } else if (4 == type && brightnessMode == 1) {
            mode = 1;
        }
        mPowerAssistantMode = mode;
        return mode;
    }

    private static int getCamOptimizeMode(int type, int brightnessMode) {
        int mode;
        if (5 == type) {
            mode = 2;
        } else {
            mode = mPowerAssistantMode;
        }
        Slog.d(TAG, "getCamOptimizeMode mode = " + mode);
        return mode;
    }

    private static int getAppBrightMode(int type) {
        int mode;
        if (6 == type) {
            mode = 3;
        } else {
            mode = mPowerAssistantMode;
        }
        Slog.d(TAG, "getAppBrightMode mode = " + mode);
        return mode;
    }

    public static boolean getPowerAssistantMode() {
        return mPowerAssistantMode == 1;
    }

    public AppBrightnessCallback getAppBrightnessCallback() {
        return this.mAppBrightnessCallback;
    }
}
