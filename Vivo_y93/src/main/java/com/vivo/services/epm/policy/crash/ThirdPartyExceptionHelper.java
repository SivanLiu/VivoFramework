package com.vivo.services.epm.policy.crash;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Slog;
import android.widget.Toast;
import com.vivo.core.interfaces.messagecore.IDisplayCallback.Stub;
import com.vivo.services.epm.ExceptionType.PerformanceException;
import com.vivo.services.epm.RuledMap;
import com.vivo.services.epm.config.ConfigurationObserver;
import com.vivo.services.epm.config.ContentValuesList;
import com.vivo.services.epm.config.DefaultConfigurationManager;
import com.vivo.services.epm.config.Switch;
import com.vivo.services.epm.util.AppStoreHelper;
import com.vivo.services.epm.util.LogSystemHelper;
import com.vivo.services.epm.util.MessageCenterHelper;
import com.vivo.services.epm.util.NotificationMessage;
import com.vivo.services.epm.util.NotificationMessage.DisplayID;
import com.vivo.services.epm.util.Utils;
import com.vivo.services.rms.ProcessList;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ThirdPartyExceptionHelper {
    public static final int ANR_TYPE = 1;
    public static final int CRASH_TYPE = 2;
    private static ThirdPartyExceptionHelper sInstance = null;
    private final int ANR_LIST = PerformanceException.EXCEPTION_TYPE_JANK;
    private final int APPSTORE_CHANNEL_NUM = 9;
    private final String APPSTORE_URL = "http://az.appstore.vivo.com.cn/third-service/appinfo";
    private final int CLICK_LEFT = 3;
    private final int CLICK_NOTIFICATION = 1;
    private final int CLICK_RIGHT = 4;
    private final int CRASH_LIST = 2002;
    private final long DIED_INTVERAL_TIME = 20000;
    private final int DISPLAY_ID = DisplayID.DISPLAY_ID_0;
    private final long IGNORE_INTVERAL_TIME = 259200000;
    private final String KEY_ANR_BROADCAST = "Broadcast of Intent";
    private final String KEY_ANR_COUNT = "anr_count";
    private final String KEY_ANR_IGNORE_INTVERAL_TIME = "anr_ignore_intveral_time";
    private final String KEY_ANR_INPUT = "Input dispatching";
    private final String KEY_ANR_LIMIT_TIME = "anr_limit_time";
    private final String KEY_ANR_SERVICE = "executing service";
    private final String KEY_CRASH_COUNT = "crash_count";
    private final String KEY_CRASH_LIMIT_TIME = "crash_limit_time";
    private final String KEY_CV_EPM_THIRD_ANR = "epm_third_anr";
    private final String KEY_CV_EPM_THIRD_CRASH = "epm_third_crash";
    private final String KEY_DIED_COUNT = "died_count";
    private final String KEY_DIED_LIMIT_TIME = "died_limit_time";
    private final String KEY_EPM_ANR_MESSAGE = "epm_anr_message";
    private final String KEY_EPM_THIRD_CRASH = "epm_thirdparty_crash";
    private final String KEY_EPM_THIRD_MESSAGE = "epm_thirdparty_message";
    private final String KEY_IGNORE_INTVERAL_TIME = "ignore_intveral_time";
    private final String KEY_MESSAGE_TIMEOUT = "message_timeout";
    private final String KEY_THIRD_CONFIG = "thirdapp.notifyconfig";
    private final int MAX_DIED_NUM = 5;
    private final int MAX_EXP_COUNT = 2;
    private final long MESSAGE_TIMEOUT = 3000;
    private final int MSG_CHECK_JIAGU = 1002;
    private final int MSG_CONNECT_APPSTORE = ProcessList.UNKNOWN_ADJ;
    private final int MSG_UNINSTALL_APP = 1004;
    private final int MSG_UPDATE_ANR_CV = 1005;
    private final int MSG_UPDATE_CV = 1003;
    private final int NEW_VERION = 1;
    private final String NE_KEY = "Native crash";
    private final int OLD_VERSION = -1;
    private final int SAME_VERSION = 0;
    private final String TAG = "TPE";
    private final long TRIGGER_INTVERAL_TIME = 86400000;
    private boolean isANRMessageEnable = true;
    private boolean isEpmThirdCrashEnable = true;
    private boolean isEpmThirdMessageEnable = true;
    private boolean isShowNotification;
    private ContentValuesList mANRContentValuesList;
    private ConfigurationObserver mANRContentValuesListObserver = new ConfigurationObserver() {
        public void onConfigChange(String file, String name) {
            Slog.d("TPE", "file=" + file + " name=" + name);
            ThirdPartyExceptionHelper.this.mANRContentValuesList = ThirdPartyExceptionHelper.this.mDefaultConfigurationManager.getContentValuesList("epm_third_anr");
            Message.obtain(ThirdPartyExceptionHelper.this.mHandler, 1005).sendToTarget();
        }
    };
    private long mANRIgnoreInterveralTime = 259200000;
    private long mANRTriggerInterveralTime = 86400000;
    private ArrayList<AppInfo> mAnrAppList = new ArrayList();
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getData().getSchemeSpecificPart();
            if (ThirdPartyExceptionHelper.this.containAppInfo(ThirdPartyExceptionHelper.this.mCrashAppList, packageName)) {
                ThirdPartyExceptionHelper.this.mCrashAppList.remove(ThirdPartyExceptionHelper.this.getAppInfo(ThirdPartyExceptionHelper.this.mCrashAppList, packageName));
            } else if (ThirdPartyExceptionHelper.this.containAppInfo(ThirdPartyExceptionHelper.this.mAnrAppList, packageName)) {
                ThirdPartyExceptionHelper.this.mAnrAppList.remove(ThirdPartyExceptionHelper.this.getAppInfo(ThirdPartyExceptionHelper.this.mAnrAppList, packageName));
            }
        }
    };
    private ContentValuesList mContentValuesList;
    private ConfigurationObserver mContentValuesListObserver = new ConfigurationObserver() {
        public void onConfigChange(String file, String name) {
            Slog.d("TPE", "file=" + file + " name=" + name);
            ThirdPartyExceptionHelper.this.mContentValuesList = ThirdPartyExceptionHelper.this.mDefaultConfigurationManager.getContentValuesList("epm_third_crash");
            Message.obtain(ThirdPartyExceptionHelper.this.mHandler, 1003).sendToTarget();
        }
    };
    private Context mContext;
    private ArrayList<AppInfo> mCrashAppList = new ArrayList();
    private DefaultConfigurationManager mDefaultConfigurationManager;
    private long mDiedInterveralTime = 20000;
    private final Stub mDisplayCallback = new Stub() {
        public void onShow(int messageId, Bundle bundle) throws RemoteException {
            Slog.w("TPE", "messageId:" + messageId);
            ThirdPartyExceptionHelper.this.mPreMessgeId = messageId;
        }

        public void onClick(int messageId, Bundle bundle) throws RemoteException {
            switch (bundle.getInt("click")) {
                case 1:
                case 3:
                    if (ThirdPartyExceptionHelper.this.mVersionResult == 1) {
                        AppStoreHelper.gotoAppStoreDetailActivity(ThirdPartyExceptionHelper.this.mContext, ThirdPartyExceptionHelper.this.mPackageName);
                    } else {
                        Message.obtain(ThirdPartyExceptionHelper.this.mHandler, 1004).sendToTarget();
                    }
                    Utils.collapsePanels(ThirdPartyExceptionHelper.this.mContext);
                    return;
                case 4:
                    AppInfo appInfo = null;
                    int index = -1;
                    int listType = ThirdPartyExceptionHelper.this.isWhichList();
                    if (listType == 2002) {
                        appInfo = ThirdPartyExceptionHelper.this.getAppInfo(ThirdPartyExceptionHelper.this.mCrashAppList, ThirdPartyExceptionHelper.this.mPackageName);
                        index = ThirdPartyExceptionHelper.this.mCrashAppList.indexOf(appInfo);
                    }
                    if (listType == PerformanceException.EXCEPTION_TYPE_JANK) {
                        appInfo = ThirdPartyExceptionHelper.this.getAppInfo(ThirdPartyExceptionHelper.this.mAnrAppList, ThirdPartyExceptionHelper.this.mPackageName);
                        index = ThirdPartyExceptionHelper.this.mAnrAppList.indexOf(appInfo);
                    }
                    long currentTime = System.currentTimeMillis();
                    appInfo.beginTime = currentTime;
                    switch (ThirdPartyExceptionHelper.this.mEventType) {
                        case 1:
                            appInfo.endTime = ThirdPartyExceptionHelper.this.mANRIgnoreInterveralTime + currentTime;
                            break;
                        case 2:
                            appInfo.endTime = ThirdPartyExceptionHelper.this.mIgnoreInterveralTime + currentTime;
                            break;
                    }
                    appInfo.bIgnore = true;
                    appInfo.crashCount = 0;
                    if (listType == 2002) {
                        ThirdPartyExceptionHelper.this.mCrashAppList.set(index, appInfo);
                    }
                    if (listType == PerformanceException.EXCEPTION_TYPE_JANK) {
                        ThirdPartyExceptionHelper.this.mAnrAppList.set(index, appInfo);
                    }
                    Utils.collapsePanels(ThirdPartyExceptionHelper.this.mContext);
                    return;
                default:
                    return;
            }
        }

        public void onCancel(int messageId, Bundle bundle) throws RemoteException {
        }

        public void onError(int errCode, String errMsg, Bundle bundle) throws RemoteException {
        }
    };
    private Switch mEpmANRMessageSwitch;
    private ConfigurationObserver mEpmANRMessageSwitchObserver = new ConfigurationObserver() {
        public void onConfigChange(String file, String name) {
            Switch w = ThirdPartyExceptionHelper.this.mDefaultConfigurationManager.getSwitch("epm_anr_message");
            ThirdPartyExceptionHelper thirdPartyExceptionHelper = ThirdPartyExceptionHelper.this;
            boolean isOn = (w == null || (w.isUninitialized() ^ 1) == 0) ? true : w.isOn();
            thirdPartyExceptionHelper.isANRMessageEnable = isOn;
            Slog.d("TPE", "file=" + file + " name=" + name + " isANRMessageEnable=" + ThirdPartyExceptionHelper.this.isANRMessageEnable);
        }
    };
    private Switch mEpmThirdCrashSwitch;
    private ConfigurationObserver mEpmThirdCrashSwitchObserver = new ConfigurationObserver() {
        public void onConfigChange(String file, String name) {
            Switch w = ThirdPartyExceptionHelper.this.mDefaultConfigurationManager.getSwitch("epm_thirdparty_crash");
            ThirdPartyExceptionHelper thirdPartyExceptionHelper = ThirdPartyExceptionHelper.this;
            boolean isOn = (w == null || (w.isUninitialized() ^ 1) == 0) ? true : w.isOn();
            thirdPartyExceptionHelper.isEpmThirdCrashEnable = isOn;
            Slog.d("TPE", "file=" + file + " name=" + name + " isEpmThirdCrashEnable=" + ThirdPartyExceptionHelper.this.isEpmThirdCrashEnable);
        }
    };
    private Switch mEpmThirdMessageSwitch;
    private ConfigurationObserver mEpmThirdMessageSwitchObserver = new ConfigurationObserver() {
        public void onConfigChange(String file, String name) {
            Switch w = ThirdPartyExceptionHelper.this.mDefaultConfigurationManager.getSwitch("epm_thirdparty_message");
            ThirdPartyExceptionHelper thirdPartyExceptionHelper = ThirdPartyExceptionHelper.this;
            boolean isOn = (w == null || (w.isUninitialized() ^ 1) == 0) ? true : w.isOn();
            thirdPartyExceptionHelper.isEpmThirdMessageEnable = isOn;
            Slog.d("TPE", "file=" + file + " name=" + name + " isEpmThirdMessageEnable=" + ThirdPartyExceptionHelper.this.isEpmThirdMessageEnable);
        }
    };
    private int mEventType;
    private ArrayList<File> mFiles = new ArrayList();
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ProcessList.UNKNOWN_ADJ /*1001*/:
                    if (ThirdPartyExceptionHelper.this.isNetworkConnected()) {
                        int versionCode = AppStoreHelper.getAppVersionCode(ThirdPartyExceptionHelper.this.mPackageName);
                        if (versionCode > ThirdPartyExceptionHelper.this.mVersionCode) {
                            ThirdPartyExceptionHelper.this.mVersionResult = 1;
                        } else if (versionCode == ThirdPartyExceptionHelper.this.mVersionCode) {
                            ThirdPartyExceptionHelper.this.mVersionResult = 0;
                        } else {
                            ThirdPartyExceptionHelper.this.mVersionResult = -1;
                        }
                    } else {
                        ThirdPartyExceptionHelper.this.mVersionResult = 0;
                    }
                    ThirdPartyExceptionHelper.this.notifyMessage();
                    return;
                case 1002:
                    if (ThirdPartyExceptionHelper.this.mEventType == 2 && ThirdPartyExceptionHelper.this.hasJiaguMessage()) {
                        Slog.d("TPE", "toast...");
                        Toast.makeText(ThirdPartyExceptionHelper.this.mContext, String.format(ThirdPartyExceptionHelper.this.mContext.getString(51249701), new Object[]{ThirdPartyExceptionHelper.this.getAppLable()}), 0).show();
                        return;
                    }
                    return;
                case 1003:
                    Slog.d("TPE", "MSG_UPDATE_CV...");
                    if (!ThirdPartyExceptionHelper.this.mContentValuesList.isEmpty()) {
                        Slog.d("TPE", "mContentValuesList:" + ThirdPartyExceptionHelper.this.mContentValuesList);
                        for (ContentValues cv : ThirdPartyExceptionHelper.this.mContentValuesList.getValues()) {
                            if (cv.getAsString("name").equals("ignore_intveral_time")) {
                                ThirdPartyExceptionHelper.this.mIgnoreInterveralTime = Long.parseLong(cv.getAsString("value"));
                            } else if (cv.getAsString("name").equals("crash_limit_time")) {
                                ThirdPartyExceptionHelper.this.mTriggerInterveralTime = Long.parseLong(cv.getAsString("value"));
                            } else if (cv.getAsString("name").equals("died_limit_time")) {
                                ThirdPartyExceptionHelper.this.mDiedInterveralTime = Long.parseLong(cv.getAsString("value"));
                            } else if (cv.getAsString("name").equals("crash_count")) {
                                ThirdPartyExceptionHelper.this.mLimitCrashCount = Integer.parseInt(cv.getAsString("value"));
                            } else if (cv.getAsString("name").equals("died_count")) {
                                ThirdPartyExceptionHelper.this.mLimitDiedCount = Integer.parseInt(cv.getAsString("value"));
                            } else if (cv.getAsString("name").equals("message_timeout")) {
                                ThirdPartyExceptionHelper.this.mMessageTimeout = (long) Integer.parseInt(cv.getAsString("value"));
                            }
                        }
                        Slog.d("TPE", "mIgnoreInterveralTime:" + ThirdPartyExceptionHelper.this.mIgnoreInterveralTime + " ,mTriggerInterveralTime:" + ThirdPartyExceptionHelper.this.mTriggerInterveralTime + " ,mDiedInterveralTime:" + ThirdPartyExceptionHelper.this.mDiedInterveralTime + " ,mLimitCrashCount:" + ThirdPartyExceptionHelper.this.mLimitCrashCount + " ,mLimitDiedCount:" + ThirdPartyExceptionHelper.this.mLimitDiedCount + ",mMessageTimeout:" + ThirdPartyExceptionHelper.this.mMessageTimeout);
                        return;
                    }
                    return;
                case 1004:
                    Slog.d("TPE", "uninstall package:" + ThirdPartyExceptionHelper.this.mPackageName);
                    ThirdPartyExceptionHelper.this.mContext.startActivity(new Intent("android.intent.action.UNINSTALL_PACKAGE", Uri.parse("package:" + ThirdPartyExceptionHelper.this.mPackageName)));
                    return;
                case 1005:
                    Slog.d("TPE", "MSG_UPDATE_ANR_CV...");
                    if (!ThirdPartyExceptionHelper.this.mANRContentValuesList.isEmpty()) {
                        Slog.d("TPE", "mANRContentValuesList:" + ThirdPartyExceptionHelper.this.mANRContentValuesList);
                        for (ContentValues cv2 : ThirdPartyExceptionHelper.this.mANRContentValuesList.getValues()) {
                            if (cv2.getAsString("name").equals("anr_ignore_intveral_time")) {
                                ThirdPartyExceptionHelper.this.mANRIgnoreInterveralTime = Long.parseLong(cv2.getAsString("value"));
                            } else if (cv2.getAsString("name").equals("anr_limit_time")) {
                                ThirdPartyExceptionHelper.this.mANRTriggerInterveralTime = Long.parseLong(cv2.getAsString("value"));
                            } else if (cv2.getAsString("name").equals("anr_count")) {
                                ThirdPartyExceptionHelper.this.mLimitANRCount = Integer.parseInt(cv2.getAsString("value"));
                            }
                        }
                        Slog.d("TPE", "mANRIgnoreInterveralTime:" + ThirdPartyExceptionHelper.this.mANRIgnoreInterveralTime + " ,mANRTriggerInterveralTime:" + ThirdPartyExceptionHelper.this.mANRTriggerInterveralTime + " ,mLimitANRCount:" + ThirdPartyExceptionHelper.this.mLimitANRCount);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private long mIgnoreInterveralTime = 259200000;
    private final ArrayList<String> mJiaguList = new ArrayList<String>() {
        {
            add("libjiagu.so");
            add("libDexHelper.so");
            add("libbaiduprotect.so");
            add("libexecmain.so");
        }
    };
    private String mLastPackageName;
    private long mLastShowMessageTime = -1;
    private int mLimitANRCount = 2;
    private int mLimitCrashCount = 2;
    private int mLimitDiedCount = 5;
    private String mLongMsg;
    private long mMessageTimeout = 3000;
    private String mPackageName;
    private int mPreMessgeId;
    private String mTrace;
    private long mTriggerInterveralTime = 86400000;
    private int mVersionCode;
    private int mVersionResult;

    static class AppInfo {
        boolean bIgnore;
        long beginTime;
        int crashCount;
        long endTime;
        long firstCrashTime;
        String packageName;

        AppInfo() {
        }
    }

    private ThirdPartyExceptionHelper(Context context) {
        boolean z;
        boolean z2 = true;
        this.mContext = context;
        System.putInt(this.mContext.getContentResolver(), "thirdapp.notifyconfig", 1);
        registerReceiver();
        this.mDefaultConfigurationManager = DefaultConfigurationManager.getInstance();
        this.mEpmThirdCrashSwitch = this.mDefaultConfigurationManager.getSwitch("epm_thirdparty_crash");
        this.mEpmThirdMessageSwitch = this.mDefaultConfigurationManager.getSwitch("epm_thirdparty_message");
        this.mEpmThirdMessageSwitch = this.mDefaultConfigurationManager.getSwitch("epm_anr_message");
        if (this.mEpmThirdCrashSwitch == null || (this.mEpmThirdCrashSwitch.isUninitialized() ^ 1) == 0) {
            z = true;
        } else {
            z = this.mEpmThirdCrashSwitch.isOn();
        }
        this.isEpmThirdCrashEnable = z;
        if (this.mEpmThirdMessageSwitch == null || (this.mEpmThirdMessageSwitch.isUninitialized() ^ 1) == 0) {
            z = true;
        } else {
            z = this.mEpmThirdMessageSwitch.isOn();
        }
        this.isEpmThirdMessageEnable = z;
        if (!(this.mEpmANRMessageSwitch == null || (this.mEpmANRMessageSwitch.isUninitialized() ^ 1) == 0)) {
            z2 = this.mEpmANRMessageSwitch.isOn();
        }
        this.isANRMessageEnable = z2;
        Slog.d("TPE", "isEpmThirdCrashEnable=" + this.isEpmThirdCrashEnable + " isEpmThirdMessageEnable=" + this.isEpmThirdMessageEnable + " isANRMessageEnable=" + this.isANRMessageEnable);
        this.mDefaultConfigurationManager.registerSwitchObserver(this.mEpmThirdCrashSwitch, this.mEpmThirdCrashSwitchObserver);
        this.mDefaultConfigurationManager.registerSwitchObserver(this.mEpmThirdMessageSwitch, this.mEpmThirdMessageSwitchObserver);
        this.mDefaultConfigurationManager.registerSwitchObserver(this.mEpmANRMessageSwitch, this.mEpmANRMessageSwitchObserver);
        this.mContentValuesList = this.mDefaultConfigurationManager.getContentValuesList("epm_third_crash");
        Message.obtain(this.mHandler, 1003).sendToTarget();
        this.mDefaultConfigurationManager.registerContentValuesListObserver(this.mContentValuesList, this.mContentValuesListObserver);
        this.mANRContentValuesList = this.mDefaultConfigurationManager.getContentValuesList("epm_third_anr");
        Message.obtain(this.mHandler, 1005).sendToTarget();
        this.mDefaultConfigurationManager.registerContentValuesListObserver(this.mANRContentValuesList, this.mANRContentValuesListObserver);
    }

    public static ThirdPartyExceptionHelper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ThirdPartyExceptionHelper.class) {
                if (sInstance == null) {
                    sInstance = new ThirdPartyExceptionHelper(context);
                }
            }
        }
        return sInstance;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0038  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isInLimitTime(long beginTime, long endTime) {
        Exception e;
        Calendar date;
        Calendar begin;
        Calendar end;
        Date currentDate = null;
        Date beginDate = null;
        Date endDate = null;
        try {
            Date currentDate2 = new Date(System.currentTimeMillis());
            try {
                Date beginDate2 = new Date(beginTime);
                try {
                    endDate = new Date(endTime);
                    beginDate = beginDate2;
                    currentDate = currentDate2;
                } catch (Exception e2) {
                    e = e2;
                    beginDate = beginDate2;
                    currentDate = currentDate2;
                    e.printStackTrace();
                    date = Calendar.getInstance();
                    date.setTime(currentDate);
                    begin = Calendar.getInstance();
                    begin.setTime(beginDate);
                    end = Calendar.getInstance();
                    end.setTime(endDate);
                    if (date.after(begin)) {
                    }
                    return false;
                }
            } catch (Exception e3) {
                e = e3;
                currentDate = currentDate2;
                e.printStackTrace();
                date = Calendar.getInstance();
                date.setTime(currentDate);
                begin = Calendar.getInstance();
                begin.setTime(beginDate);
                end = Calendar.getInstance();
                end.setTime(endDate);
                if (date.after(begin)) {
                }
                return false;
            }
        } catch (Exception e4) {
            e = e4;
            e.printStackTrace();
            date = Calendar.getInstance();
            date.setTime(currentDate);
            begin = Calendar.getInstance();
            begin.setTime(beginDate);
            end = Calendar.getInstance();
            end.setTime(endDate);
            if (date.after(begin)) {
            }
            return false;
        }
        date = Calendar.getInstance();
        date.setTime(currentDate);
        begin = Calendar.getInstance();
        begin.setTime(beginDate);
        end = Calendar.getInstance();
        end.setTime(endDate);
        if (date.after(begin) || !date.before(end)) {
            return false;
        }
        return true;
    }

    private boolean containAppInfo(ArrayList<AppInfo> appList, String packageName) {
        if (appList.size() > 0) {
            for (AppInfo appInfo : appList) {
                if (appInfo != null && packageName.equals(appInfo.packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int isWhichList() {
        if (this.mAnrAppList.size() > 0) {
            for (AppInfo appInfo : this.mAnrAppList) {
                if (appInfo != null && this.mPackageName.equals(appInfo.packageName)) {
                    return PerformanceException.EXCEPTION_TYPE_JANK;
                }
            }
        }
        if (this.mCrashAppList.size() > 0) {
            for (AppInfo appInfo2 : this.mCrashAppList) {
                if (appInfo2 != null && this.mPackageName.equals(appInfo2.packageName)) {
                    return 2002;
                }
            }
        }
        return -1;
    }

    public synchronized void updateAppList(String packageName, int eventTpye, int versionCode, String longMsg, String stackTrace, String anrReason) {
        this.mEventType = eventTpye;
        this.mLongMsg = longMsg;
        this.mTrace = stackTrace;
        this.mPackageName = packageName;
        this.mVersionCode = versionCode;
        switch (eventTpye) {
            case 1:
                updateAppListLocked(this.mAnrAppList, this.mLimitANRCount, this.mANRTriggerInterveralTime, anrReason);
                break;
            case 2:
                updateAppListLocked(this.mCrashAppList, this.mLimitCrashCount, this.mTriggerInterveralTime, anrReason);
                break;
        }
    }

    private void updateAppListLocked(ArrayList<AppInfo> appList, int maxEPCout, long triggerInterveralTime, String anrReason) {
        AppInfo appInfo;
        if (appList == null || appList.size() <= 0 || !containAppInfo(appList, this.mPackageName)) {
            appInfo = new AppInfo();
            appInfo.packageName = this.mPackageName;
            appInfo.crashCount++;
            appInfo.firstCrashTime = System.currentTimeMillis();
            appList.add(appInfo);
            this.isShowNotification = false;
            Message.obtain(this.mHandler, 1002).sendToTarget();
        } else {
            for (AppInfo appInfo2 : appList) {
                if (appInfo2 != null && this.mPackageName.equals(appInfo2.packageName)) {
                    int index = appList.indexOf(appInfo2);
                    if (appInfo2.bIgnore) {
                        if (!isInLimitTime(appInfo2.beginTime, appInfo2.endTime)) {
                            appInfo2.bIgnore = false;
                            appInfo2.crashCount++;
                            Message.obtain(this.mHandler, 1002).sendToTarget();
                            if (appInfo2.crashCount == 1) {
                                appInfo2.firstCrashTime = System.currentTimeMillis();
                            }
                        }
                        this.isShowNotification = false;
                    } else if (appInfo2.crashCount < maxEPCout) {
                        appInfo2.crashCount++;
                        if (appInfo2.crashCount == 1) {
                            appInfo2.firstCrashTime = System.currentTimeMillis();
                        }
                        if (appInfo2.crashCount == maxEPCout) {
                            updateAppInfo(appInfo2, triggerInterveralTime, 0);
                        } else {
                            Message.obtain(this.mHandler, 1002).sendToTarget();
                            this.isShowNotification = false;
                        }
                    } else if (appInfo2.crashCount == maxEPCout) {
                        updateAppInfo(appInfo2, triggerInterveralTime, 0);
                    }
                    appList.set(index, appInfo2);
                }
            }
        }
        writeToLogSystem(anrReason, 0);
    }

    private void updateAppInfo(AppInfo appInfo, long triggerInterveralTime, int freq) {
        if (isInLimitTime(appInfo.firstCrashTime, appInfo.firstCrashTime + triggerInterveralTime)) {
            this.isShowNotification = true;
            Message.obtain(this.mHandler, ProcessList.UNKNOWN_ADJ).sendToTarget();
            return;
        }
        this.isShowNotification = false;
        appInfo.firstCrashTime = System.currentTimeMillis();
        appInfo.crashCount = 1;
        Message.obtain(this.mHandler, 1002).sendToTarget();
    }

    private void writeToLogSystem(String anrReason, int freq) {
        int subt = -1;
        int res = -1;
        String pn = this.mPackageName;
        String ver = getVersionName(this.mPackageName);
        String osysversion = SystemProperties.get("ro.build.version.bbk");
        long otime = System.currentTimeMillis();
        switch (this.mEventType) {
            case 1:
                subt = 1;
                if (anrReason != null) {
                    if (!anrReason.contains("Broadcast of Intent")) {
                        if (!anrReason.contains("executing service")) {
                            if (anrReason.contains("Input dispatching")) {
                                res = 3;
                                break;
                            }
                        }
                        res = 2;
                        break;
                    }
                    res = 1;
                    break;
                }
                break;
            case 2:
                if (!isNativeCrash(this.mLongMsg)) {
                    subt = 2;
                    break;
                } else {
                    subt = 3;
                    break;
                }
        }
        Slog.d("TPE", "extype=" + 2 + " subt=" + subt + " res=" + res + " fre=" + freq + " pn=" + pn + " ver=" + ver + " osysversion:" + osysversion + " otime=" + otime);
        try {
            RuledMap log = new RuledMap("extype", "subt", "res", "fre", "pn", "ver", "osysversion", "otime", "times");
            log.addKeyValue("extype", 2 + "");
            log.addKeyValue("subt", subt + "");
            log.addKeyValue("res", res + "");
            log.addKeyValue("fre", freq + "");
            log.addKeyValue("pn", pn + "");
            log.addKeyValue("ver", ver + "");
            log.addKeyValue("osysversion", osysversion + "");
            log.addKeyValue("otime", otime + "");
            log.addKeyValue("times", "0");
            LogSystemHelper.getInstance().addLog(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getVersionName(String packageName) {
        String versionName = "";
        try {
            return this.mContext.getPackageManager().getPackageInfo(this.mContext.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return versionName;
        }
    }

    private boolean isNativeCrash(String longMsg) {
        if (longMsg != null) {
            return longMsg.contains("Native crash");
        }
        return false;
    }

    private void sendBroadcastToAMS() {
        Slog.d("TPE", "sendBroadcastToAMS...");
        Intent intent = new Intent();
        intent.setAction("action.vivo.hideanrdialog");
        this.mContext.sendBroadcast(intent);
    }

    private synchronized void notifyMessage() {
        if (this.isEpmThirdMessageEnable && this.isShowNotification) {
            if (!this.isANRMessageEnable && this.mEventType == 1) {
                return;
            }
            if (!Utils.isScreenOn(this.mContext)) {
                return;
            }
            if (this.mLastShowMessageTime <= 0 || this.mLastPackageName == null || System.currentTimeMillis() - this.mLastShowMessageTime >= this.mMessageTimeout || !this.mLastPackageName.equals(this.mPackageName)) {
                Slog.w("TPE", "notifyMessage");
                if (this.mEventType == 1) {
                    sendBroadcastToAMS();
                }
                try {
                    if (this.mPreMessgeId != 0) {
                        Slog.i("TPE", "mPreMessgeId:" + this.mPreMessgeId);
                        MessageCenterHelper.getInstance().cancel(DisplayID.DISPLAY_ID_0, this.mPreMessgeId, null);
                    }
                    Slog.i("TPE", "mVersionResult:" + this.mVersionResult);
                    this.mLastShowMessageTime = System.currentTimeMillis();
                    this.mLastPackageName = this.mPackageName;
                    MessageCenterHelper.getInstance().send(DisplayID.DISPLAY_ID_0, buildBundle(this.mEventType, this.mVersionResult), this.mDisplayCallback);
                } catch (Exception e) {
                }
            }
        }
    }

    private boolean isNetworkConnected() {
        NetworkInfo networkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            return false;
        }
        return true;
    }

    private AppInfo getAppInfo(ArrayList<AppInfo> infoList, String packageName) {
        if (infoList.size() > 0) {
            for (AppInfo appInfo : infoList) {
                if (appInfo != null && packageName.equals(appInfo.packageName)) {
                    return appInfo;
                }
            }
        }
        return null;
    }

    private NotificationMessage buildBundle(int eventType, int versionResult) {
        NotificationMessage bundle = new NotificationMessage();
        String title = "";
        String leftStr = "";
        String rightStr = "";
        String content = "";
        String appLable = "";
        title = this.mContext.getString(51249693);
        appLable = getAppLable();
        switch (eventType) {
            case 1:
                switch (versionResult) {
                    case -1:
                    case 0:
                        content = String.format(this.mContext.getString(51249697), new Object[]{appLable, appLable});
                        break;
                    case 1:
                        content = String.format(this.mContext.getString(51249696), new Object[]{appLable, appLable});
                        break;
                }
                break;
            case 2:
                switch (versionResult) {
                    case -1:
                    case 0:
                        content = String.format(this.mContext.getString(51249695), new Object[]{appLable, appLable});
                        break;
                    case 1:
                        content = String.format(this.mContext.getString(51249694), new Object[]{appLable, appLable});
                        break;
                }
                break;
        }
        rightStr = this.mContext.getString(51249700);
        switch (versionResult) {
            case -1:
            case 0:
                leftStr = this.mContext.getString(51249699);
                break;
            case 1:
                leftStr = this.mContext.getString(51249698);
                break;
        }
        bundle.setPattern(2);
        bundle.setTitle(title);
        bundle.setOnGoing(0);
        bundle.setSound(1);
        bundle.setFloating(1);
        bundle.setContent(content);
        bundle.setBtn1(leftStr);
        bundle.setBtn2(rightStr);
        return bundle;
    }

    private String getAppLable() {
        String appLable = "";
        try {
            PackageManager pm = this.mContext.getPackageManager();
            return pm.getPackageInfo(this.mPackageName, 0).applicationInfo.loadLabel(pm).toString().trim();
        } catch (Exception e) {
            return appLable;
        }
    }

    private boolean hasJiaguMessage() {
        if (!(this.mLongMsg == null || this.mTrace == null)) {
            for (String key : this.mJiaguList) {
                Slog.d("TPE", "key:" + key);
                if (!this.mLongMsg.contains(key)) {
                    if (this.mTrace.contains(key)) {
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    public ArrayList<AppInfo> getCrashAppList() {
        return this.mCrashAppList;
    }

    public ArrayList<AppInfo> getAnrAppList() {
        return this.mAnrAppList;
    }

    public long getDiedLimitTime() {
        return this.mDiedInterveralTime;
    }

    public int getLimitDiedCount() {
        return this.mLimitDiedCount;
    }

    public boolean isEpmThirdCrashEnable() {
        return this.isEpmThirdCrashEnable;
    }

    public String dump() {
        return "isEpmThirdCrashEnable=" + this.isEpmThirdCrashEnable + " isEpmThirdMessageEnable=" + this.isEpmThirdMessageEnable + " isShowNotification=" + this.isShowNotification + " isANRMessageEnable=" + this.isANRMessageEnable + " mLimitCrashCount=" + this.mLimitCrashCount + " mLimitANRCount=" + this.mLimitANRCount + " mLimitDiedCount=" + this.mLimitDiedCount;
    }
}
