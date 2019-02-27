package com.vivo.common.autobrightness;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IProcessObserver;
import android.app.IProcessObserver.Stub;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Slog;
import com.vivo.common.autobrightness.AppClassify.AppType;
import com.vivo.common.autobrightness.CameraLumaCallback.AppBrightnessCallback;
import com.vivo.common.autobrightness.CameraLumaCallback.AppRatioUpdateLuxThreshold;
import com.vivo.common.autobrightness.CameraLumaCallback.BrightnessRatioCallback;
import com.vivo.common.autobrightness.CameraLumaCallback.ModeRestoreCallback;
import com.vivo.common.autobrightness.CameraLumaCallback.UnderDisplayLightCallback;
import com.vivo.common.provider.Calendar.Events;
import java.io.PrintWriter;
import java.util.List;

public class ScreenBrightnessModeRestore {
    private static final String[] BLACK_MODE_PKG_LIST = new String[]{"com.theotino.chinadaily", "com.qiyi.video.pad", "tv.pps.mobile", "com.qiyi.video", "com.tencent.qqlive", "com.qiyi.video.pad.intel", "com.tudou.android", "com.sina.weibo", "cn.com.sina.sports", "com.sohu.sohuvideo", "com.ifeng.newvideo"};
    private static final String JSON_KEY_CHANGEBY = "changeBy";
    private static final String JSON_KEY_OFFBY = "offBy";
    private static final String KEY_CAM_BRIGHT_MODE = "cam_bright_mode";
    private static final int MSG_APP_BRIGHT_MODE_RESET = 13;
    private static final int MSG_BOOT_GET_LAST_LOW_LUX_APP = 6;
    private static final int MSG_CAM_MODE_RESET = 11;
    private static final int MSG_FOREGROUND_ACTIVITY_CHANGED = 1;
    private static final int MSG_FOREGROUND_APP_CHANGE = 0;
    private static final int MSG_GET_FOREGROUND_APP_NAME = 8;
    private static final int MSG_ON_GET_SETTINGS_ARG3 = 4;
    private static final int MSG_ON_GET_SETTINGS_ARG4 = 3;
    private static final int MSG_PROCESS_DIED = 2;
    private static final int MSG_REGISTER_CONTENT_OBSERVER = 10;
    private static final int MSG_REGISTER_PROCESS_OBSERVER = 9;
    private static final int MSG_SET_BACKLIGHT = 5;
    private static final int MSG_SET_BACKLIGHT_ARG2 = 7;
    private static final int MSG_UDL_ON_APP_CHANGED = 12;
    private static final String PKG_UNKOWN = "unknown";
    private static final String TAG = "ScreenBrightnessModeRestore";
    private static final String[] USE_PRELAST_PKG_LIST = new String[]{"com.qiyi.video.pad", "tv.pps.mobile", "com.qiyi.video", "com.qiyi.video.pad.intel", "com.ifeng.newvideo"};
    private static final boolean USE_RESTORE = SystemProperties.getBoolean("persist.screenmode.restore", true);
    private static final String VIVO_GAME_SDK_PKG = "com.vivo.sdkplugin";
    private static final String[] WHITE_MODE_PKG_LIST = new String[]{"com.android.systemui", "com.android.settings", "android", "com.vivo.upslide", "com.bbk.SuperPowerSave", "com.iqoo.powersaving", "com.iqoo.engineermode", "com.vivo.easyshare", "com.bbk.scene.indoor", "com.vivo.childrenmode", "com.qihoo360.ilauncher", "com.gau.go.launcherex", "com.apusapps.launcher", "com.dianxinos.dxhome", "com.nd.android.pandahome2", "com.moxiu.launcher", "com.Dean.launcher", "com.zeroteam.zerolauncher", "com.fhhr.launcherEx", "com.kk.launcher", "com.lo.launcher", "com.vivo.agent", "com.kugou.launcher", "com.nearme.launcher", "com.ouchn.desktop", "com.android.shell", "root", "com.vivo.udfingerprint"};
    private static ActivityManager mActivityManager;
    private static String mAutoBacklightChangeBy = Events.DEFAULT_SORT_ORDER;
    private static String mBacklightChangeBy = Events.DEFAULT_SORT_ORDER;
    private static IActivityManager mIActivityManager;
    private static ScreenBrightnessModeRestore mInstance = null;
    private static Object mLock = new Object();
    private static Object mSettingsUpdateLock = new Object();
    private static int mUpdateSettingsCounts = 0;
    private boolean bBrighnessInitStatus = true;
    private boolean bBrightnessRestoreStatus = false;
    private AppBriRatioJson mAppBriRatioJson = new AppBriRatioJson();
    private AppBrightnessCallback mAppBrightnessCallback = null;
    private String mAppName = null;
    private String mAppType = AppType.TYPE_UNKOWN;
    private int mBeforeChengedScreenBrightness = -1;
    private BrightnessSceneRatio mBrightnessSceneRatio = null;
    private ModeRestoreCallback mCallback = null;
    private int mCamMode = 0;
    private CamSettingsObserver mCamSettingObserver;
    private ContentResolver mContentResolver;
    private Context mContext;
    private int mCurrentForceGroudUid = -1;
    private int mCurrentForceGroundPid = -1;
    private String mForegroundPkg = PKG_UNKOWN;
    private boolean mIsPortrait = true;
    private String mLastChangeBy = "unknwon";
    private int mLastForceGroundPid = -1;
    private String mLastLowLuxModifyApp = Events.DEFAULT_SORT_ORDER;
    private String mLastOffBy = "unknwon";
    private int mLastScreenBrightness = -1;
    private int mLastScreenMode = -1;
    private int mLastSettingBrightness = -1;
    private int mLastSettingMode = -1;
    private int mLcmBacklight = 255;
    private int mLightLux = -1;
    private Looper mLooper;
    private UserModifyRecorder mModifyRecorder = null;
    private Handler mPackageHandler;
    private int mPreLastScreenBrightness = -1;
    private IProcessObserver mProcessObserver = new Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            Message msg = ScreenBrightnessModeRestore.this.mRestoreHandler.obtainMessage(1);
            msg.arg1 = pid;
            msg.arg2 = uid;
            msg.obj = Boolean.valueOf(foregroundActivities);
            ScreenBrightnessModeRestore.this.mRestoreHandler.sendMessage(msg);
        }

        public void onProcessDied(int pid, int uid) {
            Message msg = ScreenBrightnessModeRestore.this.mRestoreHandler.obtainMessage(2);
            msg.arg1 = pid;
            msg.arg2 = uid;
            ScreenBrightnessModeRestore.this.mRestoreHandler.sendMessage(msg);
        }
    };
    private BrightnessRatioCallback mRatioCallback = null;
    private AppRatioUpdateLuxThreshold mRatioLuxCallback = null;
    private int mRealtimeSettingBrightness = -1;
    private boolean mRegisteredProcessObserver = false;
    private RestoreHandler mRestoreHandler = null;
    private Runnable mRunnable = new Runnable() {
        public void run() {
            Slog.d(ScreenBrightnessModeRestore.TAG, "mRunnable try to restore, mLastScreenBrightness: " + ScreenBrightnessModeRestore.this.mLastScreenBrightness + " mWaitToRestoreBrightness: " + ScreenBrightnessModeRestore.this.mWaitToRestoreBrightness + " mWaitToRestoreMode: " + ScreenBrightnessModeRestore.this.mWaitToRestoreMode + " mLastScreenMode: " + ScreenBrightnessModeRestore.this.mLastScreenMode);
            if (ScreenBrightnessModeRestore.this.mWaitToRestoreBrightness && ScreenBrightnessModeRestore.this.mLastScreenBrightness > 0) {
                ScreenBrightnessModeRestore.this.log("mRunnable restore prelast SCREEN_BRIGHTNESS as mLastScreenBrightness=" + ScreenBrightnessModeRestore.this.mLastScreenBrightness);
                System.putInt(ScreenBrightnessModeRestore.this.mContentResolver, "screen_brightness", ScreenBrightnessModeRestore.this.mLastScreenBrightness);
                ScreenBrightnessModeRestore.this.mWaitToRestoreBrightness = false;
                ScreenBrightnessModeRestore.this.bBrightnessRestoreStatus = true;
            }
            if (ScreenBrightnessModeRestore.this.mWaitToRestoreMode && ScreenBrightnessModeRestore.this.mLastScreenMode >= 0) {
                ScreenBrightnessModeRestore.this.log("mRunnable restore prelast SCREEN_BRIGHTNESS as mLastScreenMode=" + ScreenBrightnessModeRestore.this.mLastScreenMode);
                if (ScreenBrightnessModeRestore.this.mLastScreenMode <= 1) {
                    System.putInt(ScreenBrightnessModeRestore.this.mContentResolver, "screen_brightness_mode", ScreenBrightnessModeRestore.this.mLastScreenMode);
                }
                ScreenBrightnessModeRestore.this.mWaitToRestoreMode = false;
            }
        }
    };
    private Runnable mRunnableRollback = new Runnable() {
        public void run() {
            Slog.d(ScreenBrightnessModeRestore.TAG, "mRunnableRollback try to restore, mLastScreenBrightness:" + ScreenBrightnessModeRestore.this.mBeforeChengedScreenBrightness);
            if (ScreenBrightnessModeRestore.this.mBeforeChengedScreenBrightness > 0) {
                System.putInt(ScreenBrightnessModeRestore.this.mContentResolver, "screen_brightness", ScreenBrightnessModeRestore.this.mBeforeChengedScreenBrightness);
            }
        }
    };
    private String mScreenBrightnessChangedBy = Events.DEFAULT_SORT_ORDER;
    private String mScreenBrightnessModeOffBy = null;
    private int mScreenState = ScreenState.STATE_SCREEN_BRIGHT;
    private SettingObserver mSettingObserve;
    private boolean mShouldRestore = false;
    private boolean mSystemReady = false;
    private UnderDisplayLightCallback mUDLightCallback = null;
    private UpslideReceiver mUpslideReceiver = new UpslideReceiver(this, null);
    private boolean mUseAutoBrightness = false;
    private boolean mVideoGameFlag = false;
    private boolean mWaitToRestore = false;
    private boolean mWaitToRestoreBrightness = false;
    private boolean mWaitToRestoreMode = false;
    private boolean mWaitToRollbackChangedBrightness = false;
    private boolean shouldWriteBackToSettings = true;

    private class CamSettingsObserver extends ContentObserver {
        public CamSettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            try {
                ScreenBrightnessModeRestore.this.mCamMode = Global.getInt(ScreenBrightnessModeRestore.this.mContentResolver, ScreenBrightnessModeRestore.KEY_CAM_BRIGHT_MODE, 0);
            } catch (Exception e) {
                Slog.d(ScreenBrightnessModeRestore.TAG, "getInt KEY_CAM_BRIGHT_MODE failed.");
            }
        }
    }

    private class RestoreHandler extends Handler {
        public RestoreHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int pid;
            String pkg;
            String typ;
            switch (msg.what) {
                case 0:
                    pid = msg.arg1;
                    pkg = ScreenBrightnessModeRestore.this.mContext.getPackageManager().getNameForUid(msg.arg2);
                    if (pkg != null) {
                        if (pkg != null && pkg.contains(":")) {
                            pkg = pkg.split(":")[0];
                        }
                        typ = AppClassify.getAppType(pkg);
                        if (!pkg.startsWith(ScreenBrightnessModeRestore.VIVO_GAME_SDK_PKG)) {
                            ScreenBrightnessModeRestore.this.mForegroundPkg = pkg;
                            if (ScreenBrightnessModeRestore.this.mUDLightCallback != null) {
                                ScreenBrightnessModeRestore.this.mRestoreHandler.removeMessages(12);
                                ScreenBrightnessModeRestore.this.mRestoreHandler.sendMessage(ScreenBrightnessModeRestore.this.mRestoreHandler.obtainMessage(12));
                            }
                            ScreenBrightnessModeRestore.this.mVideoGameFlag = AppClassify.getVideoGameFlag(typ);
                            if (!typ.equals(AppType.TYPE_UNKOWN)) {
                                ScreenBrightnessModeRestore.this.log("FOREGROUND_APP_CHANGE pkg=" + pkg + " typ=" + typ + " ptyp=" + ScreenBrightnessModeRestore.this.mAppType + " callback=" + (ScreenBrightnessModeRestore.this.mRatioCallback == null ? "Null" : "NotNull"));
                                if (typ != ScreenBrightnessModeRestore.this.mAppType) {
                                    ScreenBrightnessModeRestore.this.mAppType = typ;
                                    if (ScreenBrightnessModeRestore.this.mAppBrightnessCallback != null) {
                                        ScreenBrightnessModeRestore.this.mAppBrightnessCallback.onAppBrightModeChanged(true);
                                        break;
                                    }
                                }
                            }
                            if (AblConfig.isDebug()) {
                                Slog.d(ScreenBrightnessModeRestore.TAG, "FOREGROUND_APP_CHANGE reset use:" + ScreenBrightnessModeRestore.this.mUseAutoBrightness + " lux:" + ScreenBrightnessModeRestore.this.mLightLux + " pkg:" + pkg + " typ:" + typ + " ptyp:" + ScreenBrightnessModeRestore.this.mAppType);
                            }
                            ScreenBrightnessModeRestore.this.mAppType = typ;
                            if (ScreenBrightnessModeRestore.this.mAppBrightnessCallback != null) {
                                ScreenBrightnessModeRestore.this.mAppBrightnessCallback.onAppBrightModeChanged(false);
                            }
                            return;
                        }
                        Slog.d(ScreenBrightnessModeRestore.TAG, "FOREGROUND_APP_CHANGE ignore use:" + ScreenBrightnessModeRestore.this.mUseAutoBrightness + " lux:" + ScreenBrightnessModeRestore.this.mLightLux + " pkg:" + pkg + " typ:" + typ + " ptyp:" + ScreenBrightnessModeRestore.this.mAppType);
                        return;
                    }
                    Slog.e(ScreenBrightnessModeRestore.TAG, "FOREGROUND_APP_CHANGE pkg is null!");
                    return;
                    break;
                case 1:
                    if (msg.obj != null) {
                        ScreenBrightnessModeRestore.this.onForegroundActivitiesChangedInner(msg.arg1, msg.arg2, ((Boolean) msg.obj).booleanValue());
                        break;
                    } else {
                        Slog.e(ScreenBrightnessModeRestore.TAG, "MSG_FOREGROUND_ACTIVITY_CHANGED msg.obj == null");
                        return;
                    }
                case 2:
                    ScreenBrightnessModeRestore.this.onProcessDiedInner(msg.arg1, msg.arg2);
                    break;
                case 3:
                    if (msg.obj != null) {
                        SettingBrightnessInfo info = msg.obj;
                        String off = info.infoOffBy;
                        String change = info.infoChangeBy;
                        if (off != null && change != null) {
                            ScreenBrightnessModeRestore.this.onGetSettingsInner(msg.arg1, msg.arg2, off, change);
                            break;
                        } else {
                            Slog.e(ScreenBrightnessModeRestore.TAG, "MSG_ON_GET_SETTINGS_ARG4 get json null, off=" + off + " change=" + change);
                            return;
                        }
                    }
                    Slog.e(ScreenBrightnessModeRestore.TAG, "MSG_ON_GET_SETTINGS_ARG4 msg.obj == null; return");
                    return;
                case 4:
                    String str = null;
                    if (msg.obj != null) {
                        str = msg.obj;
                    }
                    ScreenBrightnessModeRestore.this.onGetSettingsInner(msg.arg1, msg.arg2, str);
                    break;
                case 5:
                    ScreenBrightnessModeRestore.this.setbacklightInner(msg.arg1);
                    break;
                case 6:
                    ScreenBrightnessModeRestore.this.handleSettingsChanged();
                    break;
                case 7:
                    ScreenBrightnessModeRestore.this.setbacklightInner(msg.arg1, ((Boolean) msg.obj).booleanValue());
                    break;
                case 8:
                    pid = msg.arg1;
                    pkg = ScreenBrightnessModeRestore.this.mContext.getPackageManager().getNameForUid(msg.arg2);
                    if (pkg != null && pkg.contains(":")) {
                        pkg = pkg.split(":")[0];
                    }
                    ScreenBrightnessModeRestore.this.mForegroundPkg = pkg;
                    typ = AppClassify.getAppType(pkg);
                    if (AblConfig.isDebug()) {
                        Slog.d(ScreenBrightnessModeRestore.TAG, "MSG_GET_FOREGROUND_APP_NAME  pkg:" + pkg + " typ:" + typ);
                    }
                    if (ScreenBrightnessModeRestore.this.mUDLightCallback != null) {
                        ScreenBrightnessModeRestore.this.mRestoreHandler.removeMessages(12);
                        ScreenBrightnessModeRestore.this.mRestoreHandler.sendMessage(ScreenBrightnessModeRestore.this.mRestoreHandler.obtainMessage(12));
                    }
                    if (!typ.equals(AppType.TYPE_VIDEO) && !typ.equals(AppType.TYPE_GAME) && !typ.equals(AppType.TYPE_MOBA_GAME) && !typ.equals(AppType.TYPE_PUBG_GAME)) {
                        ScreenBrightnessModeRestore.this.mVideoGameFlag = false;
                        break;
                    } else {
                        ScreenBrightnessModeRestore.this.mVideoGameFlag = true;
                        break;
                    }
                    break;
                case 9:
                    ScreenBrightnessModeRestore.this.registerProcessObserver();
                    break;
                case 10:
                    if (msg.obj != null) {
                        String type = msg.obj;
                    }
                    ScreenBrightnessModeRestore.this.registerContentObserver();
                    if (ScreenBrightnessModeRestore.this.mUpslideReceiver != null && AblConfig.isUseUnderDisplayLight()) {
                        Slog.d(ScreenBrightnessModeRestore.TAG, "register Upslide Receiver");
                        ScreenBrightnessModeRestore.this.mContext.registerReceiver(ScreenBrightnessModeRestore.this.mUpslideReceiver, new IntentFilter("vivo.intent.action.UPSLIDE_PANEL_STATE_CHANGED"));
                        break;
                    }
                case 11:
                    if (1 == ScreenBrightnessModeRestore.this.mCamMode && (ScreenBrightnessModeRestore.this.mScreenState == ScreenState.STATE_SCREEN_OFF || ScreenBrightnessModeRestore.this.mScreenState == ScreenState.STATE_SCREEN_DOZE)) {
                        try {
                            Slog.d(ScreenBrightnessModeRestore.TAG, "reset camo mode");
                            Global.putInt(ScreenBrightnessModeRestore.this.mContentResolver, ScreenBrightnessModeRestore.KEY_CAM_BRIGHT_MODE, 0);
                            break;
                        } catch (Exception e) {
                            Slog.d(ScreenBrightnessModeRestore.TAG, "setInt KEY_CAM_BRIGHT_MODE failed.");
                            break;
                        }
                    }
                case 12:
                    if (ScreenBrightnessModeRestore.this.mUDLightCallback != null) {
                        ScreenBrightnessModeRestore.this.mUDLightCallback.onAppChanged(ScreenBrightnessModeRestore.this.mForegroundPkg, ScreenBrightnessModeRestore.this.mIsPortrait);
                        break;
                    }
                    break;
                case 13:
                    try {
                        String ttyp = AppClassify.getAppType(ScreenBrightnessModeRestore.this.getTopAppPackage());
                        if (ttyp.equals(AppType.TYPE_UNKOWN)) {
                            if (AblConfig.isDebug()) {
                                Slog.d(ScreenBrightnessModeRestore.TAG, "APP_BRIGHT_MODE_RESET reset use:" + ScreenBrightnessModeRestore.this.mUseAutoBrightness + " lux:" + ScreenBrightnessModeRestore.this.mLightLux + " ttyp:" + ttyp + " ptyp:" + ScreenBrightnessModeRestore.this.mAppType);
                            }
                            if (ScreenBrightnessModeRestore.this.mAppType != ttyp) {
                                ScreenBrightnessModeRestore.this.mAppType = ttyp;
                                if (ScreenBrightnessModeRestore.this.mAppBrightnessCallback != null) {
                                    ScreenBrightnessModeRestore.this.mAppBrightnessCallback.onAppBrightModeChanged(false);
                                    break;
                                }
                            }
                        }
                    } catch (Exception e2) {
                        Slog.e(ScreenBrightnessModeRestore.TAG, "APP_BRIGHT_MODE_RESET failed.");
                        break;
                    }
                    break;
            }
        }
    }

    private class SettingBrightnessInfo {
        String infoChangeBy = ScreenBrightnessModeRestore.JSON_KEY_CHANGEBY;
        String infoOffBy = ScreenBrightnessModeRestore.JSON_KEY_OFFBY;

        public SettingBrightnessInfo(String offBy, String changeBy) {
            this.infoOffBy = offBy;
            this.infoChangeBy = changeBy;
        }
    }

    private class SettingObserver extends ContentObserver {
        public SettingObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            ScreenBrightnessModeRestore.this.handleSettingsChanged();
        }
    }

    private final class UpslideReceiver extends BroadcastReceiver {
        /* synthetic */ UpslideReceiver(ScreenBrightnessModeRestore this$0, UpslideReceiver -this1) {
            this();
        }

        private UpslideReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action.equals("vivo.intent.action.UPSLIDE_PANEL_STATE_CHANGED")) {
                    ScreenBrightnessModeRestore.this.mUDLightCallback.onAppChanged("UpSlide changed", false);
                    Slog.e(ScreenBrightnessModeRestore.TAG, "UpslideReceiver action:" + action);
                } else {
                    Slog.e(ScreenBrightnessModeRestore.TAG, "UpslideReceiver unkown action:" + action);
                }
            }
        }
    }

    public ScreenBrightnessModeRestore(Context context, Looper looper) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        mIActivityManager = ActivityManagerNative.getDefault();
        this.mLooper = looper;
        this.mPackageHandler = new Handler(this.mLooper);
        this.mRestoreHandler = new RestoreHandler(looper);
        this.mModifyRecorder = new UserModifyRecorder(this.mContext, this.mLooper);
        if (AblConfig.isUse2048GrayScaleBacklight() && AblConfig.isDebug()) {
            for (int index = 0; index < 256; index++) {
                Slog.d(TAG, "Brightness " + index + "[256] --> " + AblConfig.mBrightnessMap[index] + "[2048]");
            }
        }
        onSystemReady();
        if (this.mRestoreHandler != null) {
            this.mSettingObserve = new SettingObserver(this.mRestoreHandler);
            this.mCamSettingObserver = new CamSettingsObserver(this.mRestoreHandler);
            this.mRestoreHandler.sendEmptyMessage(6);
            Message msg = this.mRestoreHandler.obtainMessage(10);
            msg.obj = UserModifyRecorder.KEY_APP_BRIGHTNESS_RATIO;
            this.mRestoreHandler.sendMessage(msg);
        }
        this.mBrightnessSceneRatio = new BrightnessSceneRatio(this.mContext, looper);
    }

    public static ScreenBrightnessModeRestore getInstance() {
        ScreenBrightnessModeRestore screenBrightnessModeRestore;
        synchronized (mLock) {
            screenBrightnessModeRestore = mInstance;
        }
        return screenBrightnessModeRestore;
    }

    public static ScreenBrightnessModeRestore getInstance(Context context, Looper looper) {
        ScreenBrightnessModeRestore screenBrightnessModeRestore;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new ScreenBrightnessModeRestore(context, looper);
            }
            screenBrightnessModeRestore = mInstance;
        }
        return screenBrightnessModeRestore;
    }

    public void setBrightnessRatioCallback(BrightnessRatioCallback callback) {
        this.mRatioCallback = callback;
    }

    public void setScreenState(int screenState) {
        log("set screen state to " + screenState);
        this.mScreenState = screenState;
        if (screenState == ScreenState.STATE_SCREEN_OFF || screenState == ScreenState.STATE_SCREEN_DOZE) {
            this.mRestoreHandler.removeMessages(11);
            this.mRestoreHandler.sendMessageDelayed(this.mRestoreHandler.obtainMessage(11), 2000);
        } else if (screenState == ScreenState.STATE_SCREEN_BRIGHT) {
            this.mRestoreHandler.removeMessages(11);
        }
    }

    public void setUserBrightnessCallback(ModeRestoreCallback callback) {
        this.mCallback = callback;
        this.mModifyRecorder.setCallback(this.mCallback);
    }

    public void setUnderDisplayLightCallback(UnderDisplayLightCallback callback) {
        this.mUDLightCallback = callback;
    }

    public void setOrientation(boolean isPortrait) {
        this.mIsPortrait = isPortrait;
        if (this.mUDLightCallback != null) {
            Slog.d(TAG, "ori-" + isPortrait);
            this.mRestoreHandler.removeMessages(12);
            this.mRestoreHandler.sendMessage(this.mRestoreHandler.obtainMessage(12));
        }
        if (this.mIsPortrait) {
            this.mRestoreHandler.removeMessages(13);
            this.mRestoreHandler.sendMessageDelayed(this.mRestoreHandler.obtainMessage(13), 200);
        }
    }

    public void setAppBrightnessCallback(AppBrightnessCallback callback) {
        this.mAppBrightnessCallback = callback;
    }

    private void log(String msg) {
        if (AblConfig.isDebug()) {
            Slog.d(TAG, msg);
        }
    }

    private void handleSettingsChanged() {
        try {
            this.mAppBriRatioJson.parseJsonString(System.getString(this.mContentResolver, UserModifyRecorder.KEY_APP_BRIGHTNESS_RATIO));
        } catch (Exception e) {
            Slog.d(TAG, "getString KEY_APP_BRIGHTNESS_RATIO failed.");
        }
    }

    private void onForegroundActivitiesChangedInner(int pid, int uid, boolean foregroundActivities) {
        log("onForegroundActivitiesChangedInner pid=" + pid + " mCurrentForceGroundPid=" + this.mCurrentForceGroundPid + " mLastForceGroundPid=" + this.mLastForceGroundPid);
        log("onForegroundActivitiesChangedInner mWaitToRestore=" + this.mWaitToRestore + " mWaitToRollbackChangedBrightness=" + this.mWaitToRollbackChangedBrightness);
        Message msg;
        if (AblConfig.isUseBrightnessSceneRatio() && foregroundActivities) {
            msg = this.mRestoreHandler.obtainMessage(0);
            msg.arg1 = pid;
            msg.arg2 = uid;
            this.mRestoreHandler.sendMessage(msg);
        } else if (foregroundActivities) {
            msg = this.mRestoreHandler.obtainMessage(8);
            msg.arg1 = pid;
            msg.arg2 = uid;
            this.mRestoreHandler.sendMessage(msg);
        }
        if (foregroundActivities) {
            this.mModifyRecorder.onForegroundActivitiesChanged(uid);
            this.mCurrentForceGroudUid = uid;
        }
        if (!(this.mLastForceGroundPid == -1 || this.mCurrentForceGroundPid != this.mLastForceGroundPid || pid == this.mLastForceGroundPid)) {
            this.mPackageHandler.postDelayed(this.mRunnable, 2);
        }
        this.mCurrentForceGroundPid = pid;
    }

    public String getForegroundAppName() {
        return this.mForegroundPkg;
    }

    public boolean getVideoGameFlag() {
        return this.mVideoGameFlag;
    }

    private void onProcessDiedInner(int pid, int uid) {
        this.mAppName = getAppNameFromUid(uid);
        Slog.d(TAG, " onProcessDiedInner, pid = " + pid + " , mCurrentForceGroundPid = " + this.mCurrentForceGroundPid + " mAppName: " + this.mAppName + " mWaitToRestore=" + this.mWaitToRestore);
        if (!isInList(this.mAppName, WHITE_MODE_PKG_LIST) && this.mCurrentForceGroudUid == uid) {
            this.mPackageHandler.postDelayed(this.mRunnable, 2);
        }
    }

    private void registerProcessObserver() {
        try {
            if (mIActivityManager != null) {
                mIActivityManager.registerProcessObserver(this.mProcessObserver);
                this.mRegisteredProcessObserver = true;
            }
        } catch (RemoteException e) {
            log("registerProcessObserver failed.");
        }
    }

    private void unregisterProcessObserver() {
        try {
            if (mIActivityManager != null) {
                mIActivityManager.unregisterProcessObserver(this.mProcessObserver);
                this.mRegisteredProcessObserver = false;
            }
        } catch (RemoteException e) {
            log("registerProcessObserver failed.");
        }
    }

    private void registerContentObserver() {
        try {
            if (this.mContentResolver != null) {
                this.mContentResolver.registerContentObserver(System.getUriFor(UserModifyRecorder.KEY_APP_BRIGHTNESS_RATIO), false, this.mSettingObserve, -2);
                this.mContentResolver.registerContentObserver(Global.getUriFor(KEY_CAM_BRIGHT_MODE), false, this.mCamSettingObserver, -1);
            }
        } catch (Exception e) {
            Slog.e(TAG, "registerContentObserver failed.");
        }
    }

    private boolean isInList(String pkg, String[] list) {
        if (pkg == null || list == null) {
            return false;
        }
        for (String name : list) {
            if (pkg.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void onSystemReady() {
        this.mSystemReady = true;
        if (USE_RESTORE) {
            System.putString(this.mContentResolver, "screen_brightness_mode_off_by", Events.DEFAULT_SORT_ORDER);
            System.putString(this.mContentResolver, "screen_brightness_change_by", Events.DEFAULT_SORT_ORDER);
            this.mLastScreenMode = System.getIntForUser(this.mContentResolver, "screen_brightness_mode", 0, -2);
            this.mLastScreenBrightness = System.getIntForUser(this.mContentResolver, "screen_brightness", 112, -2);
            if (this.mRestoreHandler != null) {
                this.mRestoreHandler.sendEmptyMessage(9);
            }
        }
    }

    private boolean isAuto(int mode) {
        return mode == 1;
    }

    private void onGetSettingsInner(int brightness, int mode, String offBy, String changeBy) {
        boolean isUnderAutoBrightnessMode = mode == 1 && this.mLastScreenMode == 1;
        if (this.mLastSettingBrightness == brightness && this.mLastSettingMode == mode && (this.mLastOffBy.equals(offBy) ^ 1) == 0 && (this.mLastChangeBy.equals(changeBy) ^ 1) == 0) {
            log("onGetSettingsInner same arg, no change.");
            return;
        }
        this.mLastSettingBrightness = brightness;
        this.mLastSettingMode = mode;
        this.mLastOffBy = offBy;
        this.mLastChangeBy = changeBy;
        boolean userChanged = this.mModifyRecorder.onGetSettings(brightness, mode, offBy, changeBy);
        if (((changeBy != null && changeBy.equals("android")) || this.mLastScreenMode == 0 || this.bBrighnessInitStatus) && mode == 1) {
            setWriteFlag(true);
            this.bBrighnessInitStatus = false;
        } else {
            setWriteFlag(false);
        }
        if (USE_RESTORE) {
            if (changeBy != null) {
                mBacklightChangeBy = changeBy;
                if (this.mUseAutoBrightness) {
                    mAutoBacklightChangeBy = changeBy;
                }
            }
            if (isUnderAutoBrightnessMode && this.mUseAutoBrightness && changeBy != null && (changeBy.equals("android") ^ 1) != 0) {
                if (this.mCallback != null && (mAutoBacklightChangeBy.equals("com.android.systemui") || mAutoBacklightChangeBy.equals("com.android.settings") || mAutoBacklightChangeBy.equals("com.vivo.upslide"))) {
                    Slog.d(TAG, "setSecondUserBrightness = " + brightness);
                    this.mCallback.setSecondUserBrightness(brightness);
                }
                increaseUpdateSettingsCounts();
            }
            Slog.d(TAG, "onGetSettingsInner_111 bright:" + this.mLastScreenBrightness + ", animating brightness:" + brightness + " mode:" + mode + " offBy:" + offBy + " changeBy:" + changeBy);
            if (offBy != null) {
                if (isInList(offBy, WHITE_MODE_PKG_LIST) || TextUtils.isEmpty(offBy)) {
                    this.mLastScreenMode = mode;
                } else {
                    this.mLastForceGroundPid = this.mCurrentForceGroundPid;
                    this.mWaitToRestoreMode = true;
                }
            }
            if (changeBy != null) {
                if (isInList(changeBy, WHITE_MODE_PKG_LIST) || TextUtils.isEmpty(changeBy)) {
                    Slog.d(TAG, "onGetSettingsInner_222 mLastScreenBrightness:" + this.mLastScreenBrightness);
                    this.mLastScreenBrightness = brightness;
                } else {
                    Slog.d(TAG, "onGetSettingsInner_333 mLastForceGroundPid:" + this.mLastForceGroundPid);
                    this.mLastForceGroundPid = this.mCurrentForceGroundPid;
                    this.mWaitToRestoreBrightness = true;
                }
            }
        }
    }

    public void onGetSettings(int brightness, int mode, String offBy, String changeBy) {
        if (AblConfig.isDebug()) {
            Slog.d(TAG, "onGetSettings brightness=" + brightness + " mode=" + mode + " offBy=" + offBy + " changeBy=" + changeBy + " mLastScreenMode=" + this.mLastScreenMode);
        }
        if (offBy == null || changeBy == null) {
            Slog.e(TAG, "onGetSettings invalid arg: offBy=" + (offBy == null ? "NULL" : "NotNULL") + " changeBy=" + (changeBy == null ? "NULL" : "NotNULL"));
            return;
        }
        this.mRestoreHandler.removeMessages(3);
        Message msg = this.mRestoreHandler.obtainMessage(3);
        SettingBrightnessInfo mSettingBrightnessInfo = new SettingBrightnessInfo(offBy, changeBy);
        msg.arg1 = brightness;
        msg.arg2 = mode;
        msg.obj = mSettingBrightnessInfo;
        this.mRestoreHandler.sendMessage(msg);
    }

    private void onGetSettingsInner(int brightness, int mode, String offBy) {
        boolean modeChanged = false;
        Slog.d(TAG, "onGetSettingsInner bright:" + this.mLastScreenBrightness + ", animating brightness:" + brightness + " mode:" + mode + " offBy:" + offBy);
        if (isAuto(mode)) {
            this.mPreLastScreenBrightness = this.mLastScreenBrightness;
            this.mLastScreenBrightness = brightness;
        }
        if (isAuto(this.mLastScreenMode) && (isAuto(mode) ^ 1) != 0) {
            modeChanged = true;
            Slog.d(TAG, "modeChanged");
        }
        this.mLastScreenMode = mode;
        if (offBy != null) {
            this.mScreenBrightnessModeOffBy = offBy;
        }
        if (modeChanged) {
            Slog.d(TAG, "Need this mCurrentForceGroundPid = " + this.mCurrentForceGroundPid + "mLastForceGroundPid" + this.mLastForceGroundPid);
            this.mLastForceGroundPid = this.mCurrentForceGroundPid;
            this.mWaitToRestore = isInList(offBy, BLACK_MODE_PKG_LIST);
        }
        if (isAuto(mode)) {
            this.mWaitToRestore = false;
        }
    }

    public void onGetSettings(int brightness, int mode, String offBy) {
        if (USE_RESTORE) {
            this.mRestoreHandler.removeMessages(4);
            Message msg = this.mRestoreHandler.obtainMessage(4);
            msg.arg1 = brightness;
            msg.arg2 = mode;
            msg.obj = offBy;
            this.mRestoreHandler.sendMessage(msg);
        }
    }

    public void setAppRatioUpdateLuxThreshold(AppRatioUpdateLuxThreshold callback) {
        this.mRatioLuxCallback = callback;
    }

    private void setbacklightInner(int backlight) {
        if (AblConfig.isDebug()) {
            Slog.d(TAG, "setbacklight shouldWriteBackToSettings=" + this.shouldWriteBackToSettings + " mBacklightMode=" + this.mUseAutoBrightness);
        }
        if (!this.shouldWriteBackToSettings || !this.mUseAutoBrightness || this.mScreenState == ScreenState.STATE_SCREEN_DIM) {
            Slog.d(TAG, "mAutoBacklightChangeBy:" + mAutoBacklightChangeBy);
            if (mAutoBacklightChangeBy != null && mAutoBacklightChangeBy.equals("com.bbk.SuperPowerSave")) {
                Slog.d(TAG, "****backlight = " + backlight);
            } else if (this.mUseAutoBrightness && backlight > 0) {
                Slog.d(TAG, "++++++backlight = " + backlight);
                if (this.mCallback == null || !(mAutoBacklightChangeBy.equals("com.android.systemui") || mAutoBacklightChangeBy.equals("com.android.settings") || mAutoBacklightChangeBy.equals("com.vivo.upslide"))) {
                    Slog.e(TAG, "setbacklightInner mCallback is NULL.");
                }
                mAutoBacklightChangeBy = Events.DEFAULT_SORT_ORDER;
            }
        } else if (backlight > 0) {
            Slog.d(TAG, " backlight = " + backlight);
            System.putInt(this.mContentResolver, "screen_brightness", backlight);
        } else {
            Slog.d(TAG, " backlight = " + backlight + ", this value should not be written back to settings db");
        }
        this.shouldWriteBackToSettings = true;
    }

    public void setbacklight(int backlight) {
        backlight = AblConfig.getMapping2048GrayScaleTo256GrayScaleRestore(backlight);
        this.mLcmBacklight = backlight;
        this.mModifyRecorder.onLcmBacklighChanged(backlight);
        Message msg = this.mRestoreHandler.obtainMessage(5);
        msg.arg1 = backlight;
        this.mRestoreHandler.sendMessage(msg);
    }

    private void setbacklightInner(int backlight, boolean isUseUpdateSettingsCounts) {
        if (AblConfig.isDebug()) {
            Slog.d(TAG, "setbacklight shouldWriteBackToSettings=" + this.shouldWriteBackToSettings + " mBacklightMode=" + this.mUseAutoBrightness + " mUpdateSettingsCounts=" + mUpdateSettingsCounts);
        }
        if (!this.shouldWriteBackToSettings || !this.mUseAutoBrightness || this.mScreenState == ScreenState.STATE_SCREEN_DIM || mUpdateSettingsCounts != 0) {
            Slog.d(TAG, "mAutoBacklightChangeBy:" + mAutoBacklightChangeBy);
            if (mAutoBacklightChangeBy != null && mAutoBacklightChangeBy.equals("com.bbk.SuperPowerSave")) {
                Slog.d(TAG, "****backlight = " + backlight);
            } else if (this.mUseAutoBrightness && backlight > 0) {
                Slog.d(TAG, "++++++backlight = " + backlight);
                if (this.mCallback == null || !(mAutoBacklightChangeBy.equals("com.android.systemui") || mAutoBacklightChangeBy.equals("com.android.settings") || mAutoBacklightChangeBy.equals("com.vivo.upslide"))) {
                    Slog.e(TAG, "setbacklight mCallback is NULL.");
                }
            }
            if (mUpdateSettingsCounts > 0) {
                synchronized (mSettingsUpdateLock) {
                    mUpdateSettingsCounts--;
                }
                Slog.d(TAG, "setbacklight mUpdateSettingsCounts:" + mUpdateSettingsCounts);
            }
        } else if (backlight > 0) {
            Slog.d(TAG, " backlight = " + backlight);
            System.putInt(this.mContentResolver, "screen_brightness", backlight);
        } else {
            Slog.d(TAG, " backlight = " + backlight + ", this value should not be written back to settings db");
        }
        this.shouldWriteBackToSettings = true;
    }

    public void setbacklight(int backlight, boolean isUseUpdateSettingsCounts) {
        if (isUseUpdateSettingsCounts) {
            backlight = AblConfig.getMapping2048GrayScaleTo256GrayScaleRestore(backlight);
            this.mLcmBacklight = backlight;
            this.mModifyRecorder.onLcmBacklighChanged(backlight);
            Message msg = this.mRestoreHandler.obtainMessage(7);
            msg.arg1 = backlight;
            msg.obj = Boolean.valueOf(isUseUpdateSettingsCounts);
            this.mRestoreHandler.sendMessage(msg);
        }
    }

    public void setUseAutoBrightness(boolean use) {
        if (AblConfig.isDebug()) {
            Slog.d(TAG, "setUseAutoBrightness use=" + use);
        }
        if (this.mUseAutoBrightness != use && use) {
            setWriteFlag(true);
            if (AblConfig.isDebug()) {
                Slog.d(TAG, "Change to Auto Mode.update ths system brightness");
            }
            mUpdateSettingsCounts = 0;
        }
        this.mUseAutoBrightness = use;
        if (!use) {
            this.mLightLux = -1;
        }
    }

    private void setWriteFlag(boolean flag) {
        Slog.d(TAG, "setWriteFlag flag = " + flag);
        this.shouldWriteBackToSettings = flag;
    }

    private String getTopAppPackage() {
        String mTopPackageName = PKG_UNKOWN;
        if (mActivityManager != null) {
            List<RunningTaskInfo> taskInfo = mActivityManager.getRunningTasks(1);
            if (taskInfo != null && taskInfo.size() >= 1) {
                mTopPackageName = ((RunningTaskInfo) taskInfo.get(0)).topActivity.getPackageName();
                if (mTopPackageName == null) {
                    mTopPackageName = PKG_UNKOWN;
                }
            }
        }
        log("tpkg = " + mTopPackageName);
        return mTopPackageName;
    }

    public void increaseUpdateSettingsCounts() {
        if (this.mUseAutoBrightness) {
            synchronized (mSettingsUpdateLock) {
                mUpdateSettingsCounts++;
            }
            Slog.d(TAG, "increaseUpdateSettingsCounts mUpdateSettingsCounts: " + mUpdateSettingsCounts);
        }
    }

    public void releaseUpdateSettingsCounts() {
        if (this.mUseAutoBrightness) {
            synchronized (mSettingsUpdateLock) {
                if (mUpdateSettingsCounts > 0) {
                    mUpdateSettingsCounts--;
                }
            }
            Slog.d(TAG, "releaseUpdateSettingsCounts mUpdateSettingsCounts: " + mUpdateSettingsCounts);
        }
    }

    private String getAppNameFromUid(int uid) {
        int uidInt = uid;
        Slog.d(TAG, "pakage name is " + this.mContext.getPackageManager().getNameForUid(uid) + "with :" + uid);
        return this.mContext.getPackageManager().getNameForUid(uid);
    }

    public void setPowerSaving(boolean saving) {
        this.mModifyRecorder.setPowerSaving(saving);
    }

    public void setBrightnessRestoreStatus(boolean bStatus) {
        this.bBrightnessRestoreStatus = bStatus;
        Slog.d(TAG, "bBrightnessRestoreStatus =" + this.bBrightnessRestoreStatus);
    }

    public boolean getBrightnessRestoreStatus() {
        return this.bBrightnessRestoreStatus;
    }

    public void setLightLux(int lux) {
        this.mLightLux = lux;
    }

    public boolean getChangeByUserWhenCamOptimizeStatus() {
        if (mBacklightChangeBy == null || (!mBacklightChangeBy.equals("com.android.systemui") && !mBacklightChangeBy.equals("com.android.settings") && !mBacklightChangeBy.equals("com.vivo.upslide"))) {
            return false;
        }
        return true;
    }

    public int getAppBrightPromotion(int brightnessOnFullScale) {
        return BrightnessSceneRatio.getAppBrightPromotion(brightnessOnFullScale, this.mLightLux, this.mForegroundPkg);
    }

    public void dump(PrintWriter pw) {
        this.mBrightnessSceneRatio.dump(pw);
    }
}
