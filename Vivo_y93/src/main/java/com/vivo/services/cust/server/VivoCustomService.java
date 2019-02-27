package com.vivo.services.cust.server;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.INotificationManager;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PackageDeleteObserver;
import android.app.PackageInstallObserver;
import android.app.StatusBarManager;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.VerificationParams;
import android.content.pm.VersionedPackage;
import android.graphics.Bitmap;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkStatsHistory.Entry;
import android.net.NetworkTemplate;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.INfcAdapter;
import android.nfc.NfcAdapter;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.IStorageManager;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.telephony.ISub;
import com.android.internal.telephony.ITelephony;
import com.vivo.services.cust.INetworkListDelegate;
import com.vivo.services.cust.IPhoneListListener;
import com.vivo.services.cust.ISensitiveMmsDelegate;
import com.vivo.services.cust.ISmsListListener;
import com.vivo.services.cust.IUninstallListListener;
import com.vivo.services.cust.IVivoCustomService.Stub;
import com.vivo.services.cust.IWlanListListener;
import com.vivo.services.cust.database.VivoCustomDbBridge;
import com.vivo.services.cust.server.Utils.CustomStateLock;
import com.vivo.services.cust.server.Utils.DynamicRecord;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VivoCustomService extends Stub {
    private static final String TAG = "VCS";
    private static VivoCustomService sInstance;
    private INotificationManager iNotificationManager;
    private List<ResolveInfo> listAllSystemApps;
    private List<ResolveInfo> listAllUserApps;
    private ActivityManager mActivityManager;
    private AlarmManager mAlarmManager;
    private AppOpsManager mAppOps;
    private List<String> mAppStoreList = new ArrayList();
    private BluetoothAdapter mBluetoothAdapter;
    private ContentResolver mContentResolver;
    private Context mContext;
    private CustomConfigInfo mCustomConfigInfo;
    private DevicePolicyManager mDPM;
    private ISub mISub;
    private boolean mIsBootCompleted = false;
    private boolean mIsDoingUnmount = false;
    private IStorageManager mMountService;
    private INetworkListDelegate mNetworkListDelegate;
    private NfcAdapter mNfcAdapter;
    private NotificationManager mNotificationManager;
    private PackageManager mPackageManager;
    private IPhoneListListener mPhoneListListener;
    private IPackageManager mPms;
    private IPowerManager mPower;
    private VivoCustomReceiver mReceiver;
    private ISensitiveMmsDelegate mSensitiveDelegate;
    private ISmsListListener mSmsListListener;
    private CustomStateLock mStateLcok = new CustomStateLock();
    private INetworkStatsService mStatsService;
    StorageEventListener mStorageListener = new StorageEventListener() {
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Log.i(VivoCustomService.TAG, "Received storage state changed notification that " + path + " changed state from " + oldState + " to " + newState);
            String sdPath = Utils.getExternalSdPath(VivoCustomService.this.mStorageManager.getVolumePaths());
            if (sdPath != null && sdPath.equals(path) && "unmounted".equals(newState) && VivoCustomService.this.mIsDoingUnmount) {
                VivoCustomService.this.mIsDoingUnmount = false;
                new Thread() {
                    public void run() {
                        String path = Utils.getExternalSdPath(VivoCustomService.this.mStorageManager.getVolumePaths());
                        try {
                            Log.d(VivoCustomService.TAG, "formatInternalStorage path = " + path + ", format begin!");
                            VivoCustomService.this.mMountService.formatVolume(path);
                            Log.d(VivoCustomService.TAG, "formatInternalStorage path = " + path + ", format end!");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            Log.d(VivoCustomService.TAG, "formatInternalStorage path = " + path + ", mount begin!");
                            VivoCustomService.this.mMountService.mountVolume(path);
                            Log.d(VivoCustomService.TAG, "formatInternalStorage path = " + path + ", mount end!");
                            return;
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            return;
                        }
                    }
                }.start();
            }
        }
    };
    private StorageManager mStorageManager;
    private Handler mUiHandler;
    private IUninstallListListener mUninstallListListener;
    private VivoCustomDbBridge mVivoCustomDbBridge;
    private WifiManager mWifiManager;
    private IWlanListListener mWlanListListener;
    private StatusBarManager statusBarManager;
    private TelephonyManager tm;

    class ClearUserDataObserver extends IPackageDataObserver.Stub {
        ClearUserDataObserver() {
        }

        public void onRemoveCompleted(String packageName, boolean succeeded) {
            Log.d(VivoCustomService.TAG, " onRemoveCompleted packageName = " + packageName);
        }
    }

    class LocalPackageDeleteObserver extends PackageDeleteObserver {
        private boolean loop = false;

        public LocalPackageDeleteObserver(boolean value) {
            this.loop = value;
        }

        public void onPackageDeleted(String basePackageName, int returnCode, String msg) {
            if (returnCode == 1) {
                Log.d(VivoCustomService.TAG, basePackageName + " deleted suc. " + msg);
            } else {
                Log.d(VivoCustomService.TAG, basePackageName + " deleted failed. returnCode:" + returnCode + " msg:" + msg);
            }
        }
    }

    class LocalPackageInstallObserver extends PackageInstallObserver {
        LocalPackageInstallObserver() {
        }

        public void onPackageInstalled(String name, int status, String msg, Bundle extras) {
            if (status == 1) {
                Log.d(VivoCustomService.TAG, name + " install success.");
            } else {
                Log.d(VivoCustomService.TAG, name + " install failed. " + msg + " resultCode:" + status);
            }
        }
    }

    private class VivoCustomReceiver extends BroadcastReceiver {
        /* synthetic */ VivoCustomReceiver(VivoCustomService this$0, VivoCustomReceiver -this1) {
            this();
        }

        private VivoCustomReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String pkgName;
            if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                VivoCustomService.this.mIsBootCompleted = true;
                if (Utils.GN_TESTFLAG) {
                    VivoCustomService.this.sendTestVersionWarningNotification();
                    VivoCustomService.this.showDebugModeOverlay();
                } else {
                    String[] str = Utils.VIVO_CUSTOM_VERSION.split("_");
                    if (str.length > 2 && str[2].contains("A")) {
                        VivoCustomService.this.sendTestVersionWarningNotification();
                        VivoCustomService.this.showDebugModeOverlay();
                    }
                }
                VivoCustomService.this.setActivedAdmins();
                VivoCustomService.this.setActivedAccessibilityServcies();
                if (VERSION.SDK_INT > 23) {
                    VivoCustomService.this.setDeviceOwner();
                }
            } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                pkgName = intent.getData().getEncodedSchemeSpecificPart();
                Log.d(VivoCustomService.TAG, "Package added pkgName = " + pkgName);
                VivoCustomService.this.checkAppNotificationState(pkgName, true);
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                pkgName = intent.getData().getEncodedSchemeSpecificPart();
                Log.d(VivoCustomService.TAG, "Package removed pkgName = " + pkgName + ", state = " + VivoCustomService.this.mVivoCustomDbBridge.getPackageNotificationBackup(pkgName, true));
            }
        }
    }

    public static VivoCustomService main(Context context, Handler handler) {
        if (sInstance == null) {
            sInstance = new VivoCustomService(context, handler);
        }
        return sInstance;
    }

    public static void init() {
        if (sInstance != null) {
            sInstance.initState();
        }
    }

    public VivoCustomService(Context context, Handler handler) {
        this.mContext = context;
        this.mUiHandler = handler;
        this.mCustomConfigInfo = CustomConfigInfo.getDefault();
    }

    public void initState() {
        registerBroadcastReceiver();
        this.tm = TelephonyManager.from(this.mContext);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mPms = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        this.mPower = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mStorageManager = (StorageManager) this.mContext.getSystemService("storage");
        this.mStorageManager.registerListener(this.mStorageListener);
        this.mMountService = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
        this.mDPM = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        this.mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
        this.mPackageManager = this.mContext.getPackageManager();
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mContentResolver = this.mContext.getContentResolver();
        this.mVivoCustomDbBridge = new VivoCustomDbBridge(this.mContext);
        this.statusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        this.iNotificationManager = NotificationManager.getService();
        this.mISub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
        preload();
    }

    private void preload() {
        Log.d(TAG, "VivoCustomService begin to preload");
        disableSystemApps();
        if (VERSION.SDK_INT <= 23) {
            setDeviceOwner();
        }
        preSetAppOp();
        disableStatusBar();
        initTrustedAppStore();
        setNotificationListener();
        initSecureSetting();
        initStateLock();
    }

    private void initStateLock() {
        synchronized (this.mStateLcok) {
            this.mStateLcok.mNotificationPattern = Secure.getInt(this.mContentResolver, Utils.CT_NOTIFICATION_RESTRICT_PATTERN, 0);
        }
    }

    public List<String> getCustomizedApps(int type) {
        return this.mCustomConfigInfo.getCustomizedApps(type);
    }

    public String getDefaultDeviceOwner() {
        return this.mCustomConfigInfo.getDefaultDeviceOwner();
    }

    public List<String> getByPassPermissions() {
        return this.mCustomConfigInfo.getByPassPermissions();
    }

    public List<String> getByPassOps() {
        return this.mCustomConfigInfo.getByPassOps();
    }

    public List<String> getDefaultSettingString() {
        List<String> settingListStr = new ArrayList();
        List<DynamicRecord> settingList = this.mCustomConfigInfo.getDefaultSetting();
        if (settingList != null) {
            for (DynamicRecord settingRecord : settingList) {
                if (Utils.TYPE_SETTING_DATABASE.equals(settingRecord.mType) && Utils.DATABASE_NAMESPACE_SECURE.equals(settingRecord.mNamespace) && Utils.VALUE_MODE_INT.equals(settingRecord.mMode)) {
                    settingListStr.add(settingRecord.mName + "," + Integer.toString(settingRecord.mValue));
                }
            }
        }
        Log.d(TAG, "getDefaultSettingString settingListStr = " + settingListStr);
        return settingListStr;
    }

    private void initSecureSetting() {
        String lastInitVersion = Secure.getString(this.mContentResolver, "ct_init_setting_version");
        boolean shouldUpdate = false;
        if (lastInitVersion == null || "".equals(lastInitVersion)) {
            shouldUpdate = true;
            Log.i(TAG, "initSecureSetting lastInitVersion is null");
        } else if (Utils.VIVO_CUSTOM_VERSION == null || "".equals(Utils.VIVO_CUSTOM_VERSION)) {
            shouldUpdate = true;
            Log.i(TAG, "initSecureSetting VIVO_CUSTOM_VERSION is null");
        } else if (!Utils.VIVO_CUSTOM_VERSION.equals(lastInitVersion)) {
            shouldUpdate = true;
            Log.i(TAG, "initSecureSetting shout update : VIVO_CUSTOM_VERSION == " + Utils.VIVO_CUSTOM_VERSION + ", lastInitVersion = " + lastInitVersion);
        }
        if (shouldUpdate) {
            Secure.putString(this.mContentResolver, "ct_init_setting_version", Utils.VIVO_CUSTOM_VERSION == null ? "error" : Utils.VIVO_CUSTOM_VERSION);
            List<DynamicRecord> settingList = this.mCustomConfigInfo.getDefaultSetting();
            if (settingList != null) {
                Log.d(TAG, "initSecureSetting settingList size = " + settingList.size());
                for (DynamicRecord item : settingList) {
                    Log.i(TAG, "initSecureSetting : mName = " + item.mName + ", type = " + item.mType + ", mAction = " + item.mAction + ", mValue = " + item.mValue + ", mNamespace = " + item.mNamespace + ", mMode = " + item.mMode + ", mValueStr = " + item.mValueStr);
                }
                long callingId = Binder.clearCallingIdentity();
                for (DynamicRecord settingRecord : settingList) {
                    String name = settingRecord.mName;
                    String type = settingRecord.mType;
                    String namespace = settingRecord.mNamespace;
                    String action = settingRecord.mAction;
                    String mode = settingRecord.mMode;
                    String valueStr = settingRecord.mValueStr;
                    int value = settingRecord.mValue;
                    String oldValueStr;
                    if (Utils.TYPE_PERSIST_PROPERTY.equals(type)) {
                        oldValueStr = SystemProperties.get(name, "notExist");
                        if (Utils.ACTION_ADD.equals(action) && "notExist".equals(oldValueStr)) {
                            SystemProperties.set(name, valueStr);
                        } else if (Utils.ACTION_UPDATE.equals(action)) {
                            SystemProperties.set(name, valueStr);
                        }
                    } else if (Utils.TYPE_SETTING_DATABASE.equals(type)) {
                        if (Utils.VALUE_MODE_INT.equals(mode)) {
                            int oldValue = -123;
                            boolean exist;
                            if (namespace.equals(Utils.DATABASE_NAMESPACE_SECURE)) {
                                try {
                                    oldValue = Secure.getInt(this.mContentResolver, name);
                                    exist = true;
                                } catch (SettingNotFoundException e) {
                                    Log.w(TAG, "initSecureSetting getInt failed", e);
                                    exist = false;
                                }
                                try {
                                    if (Utils.ACTION_ADD.equals(action) && (exist ^ 1) != 0) {
                                        Secure.putInt(this.mContentResolver, name, value);
                                    } else if (Utils.ACTION_UPDATE.equals(action) && value != oldValue) {
                                        Secure.putInt(this.mContentResolver, name, value);
                                    }
                                } catch (Exception e2) {
                                    Log.d(TAG, "preSetAppOp exception occur! " + e2);
                                } finally {
                                    Binder.restoreCallingIdentity(callingId);
                                }
                            } else if (namespace.equals(Utils.DATABASE_NAMESPACE_SYSTEM)) {
                                try {
                                    oldValue = System.getInt(this.mContentResolver, name);
                                    exist = true;
                                } catch (SettingNotFoundException e3) {
                                    Log.w(TAG, "initSecureSetting getInt failed", e3);
                                    exist = false;
                                }
                                if (Utils.ACTION_ADD.equals(action) && (exist ^ 1) != 0) {
                                    System.putInt(this.mContentResolver, name, value);
                                } else if (Utils.ACTION_UPDATE.equals(action) && value != oldValue) {
                                    System.putInt(this.mContentResolver, name, value);
                                }
                            } else if (namespace.equals(Utils.DATABASE_NAMESPACE_GLOBAL)) {
                                try {
                                    oldValue = Global.getInt(this.mContentResolver, name);
                                    exist = true;
                                } catch (SettingNotFoundException e32) {
                                    Log.w(TAG, "initSecureSetting getInt failed", e32);
                                    exist = false;
                                }
                                if (Utils.ACTION_ADD.equals(action) && (exist ^ 1) != 0) {
                                    Global.putInt(this.mContentResolver, name, value);
                                } else if (Utils.ACTION_UPDATE.equals(action) && value != oldValue) {
                                    Global.putInt(this.mContentResolver, name, value);
                                }
                            }
                        } else if (Utils.VALUE_MODE_STRING.equals(mode)) {
                            if (namespace.equals(Utils.DATABASE_NAMESPACE_SECURE)) {
                                oldValueStr = Secure.getString(this.mContentResolver, name);
                                if (Utils.ACTION_ADD.equals(action) && oldValueStr == null) {
                                    Secure.putString(this.mContentResolver, name, valueStr);
                                } else if (Utils.ACTION_UPDATE.equals(action) && (oldValueStr == null || (oldValueStr.equals(valueStr) ^ 1) != 0)) {
                                    Secure.putString(this.mContentResolver, name, valueStr);
                                }
                            } else if (namespace.equals(Utils.DATABASE_NAMESPACE_SYSTEM)) {
                                oldValueStr = System.getString(this.mContentResolver, name);
                                if (Utils.ACTION_ADD.equals(action) && oldValueStr == null) {
                                    System.putString(this.mContentResolver, name, valueStr);
                                } else if (Utils.ACTION_UPDATE.equals(action) && (oldValueStr == null || (oldValueStr.equals(valueStr) ^ 1) != 0)) {
                                    System.putString(this.mContentResolver, name, valueStr);
                                }
                            } else if (namespace.equals(Utils.DATABASE_NAMESPACE_GLOBAL)) {
                                oldValueStr = Global.getString(this.mContentResolver, name);
                                if (Utils.ACTION_ADD.equals(action) && oldValueStr == null) {
                                    Global.putString(this.mContentResolver, name, valueStr);
                                } else if (Utils.ACTION_UPDATE.equals(action) && (oldValueStr == null || (oldValueStr.equals(valueStr) ^ 1) != 0)) {
                                    Global.putString(this.mContentResolver, name, valueStr);
                                }
                            }
                        }
                    }
                }
            }
            return;
        }
        Log.w(TAG, "initSecureSetting VIVO_CUSTOM_VERSION(" + Utils.VIVO_CUSTOM_VERSION + ") and the lastInitVersion(" + lastInitVersion + ") is match");
    }

    private void disableSystemApps() {
        List<String> pkgs = this.mCustomConfigInfo.getDefaultDisabledApps();
        if (pkgs != null) {
            Log.d(TAG, "getDefaultDisabledApps apps size = " + pkgs.size() + ", pkgs" + pkgs);
            for (String packageName : pkgs) {
                try {
                    this.mPms.setApplicationEnabledSetting(packageName, 2, 0, 0, null);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to talk to package manager", e);
                }
            }
        }
    }

    private void setNotificationListener() {
        List<String> pkgs = this.mCustomConfigInfo.getDefaultNotificationListeners();
        if (pkgs != null) {
            String enabelNofitications = Secure.getString(this.mContentResolver, "enabled_notification_listeners");
            for (String component : pkgs) {
                if (enabelNofitications == null || (enabelNofitications.contains(component) ^ 1) != 0) {
                    enabelNofitications = enabelNofitications == null ? component : enabelNofitications + ":" + component;
                }
            }
            Log.d(TAG, "updateNotificationListenerSetting enabelNofitications = " + enabelNofitications);
            Secure.putString(this.mContentResolver, "enabled_notification_listeners", enabelNofitications);
        }
    }

    private void initTrustedAppStore() {
        parseTrustedAppStoreFile();
        getdefaultTrustedAppStore();
    }

    private void getdefaultTrustedAppStore() {
        List<String> pkgs = this.mCustomConfigInfo.getDefaultTrustedAppStores();
        if (pkgs != null) {
            Log.d(TAG, "getdefaultTrustedAppStore pkgs = " + pkgs);
            addTrustedAppStoreList(pkgs);
        }
    }

    private void parseTrustedAppStoreFile() {
        IOException e;
        Throwable th;
        this.mAppStoreList.clear();
        File pkgFile = new File(Utils.GN_TRUST_APP_STORE_LIST);
        if (pkgFile == null || !pkgFile.exists() || 0 == pkgFile.length()) {
            Log.w(TAG, "initTrustedAppStore failed.  pkgFile:" + pkgFile);
            return;
        }
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(pkgFile));
            if (reader != null) {
                while (true) {
                    try {
                        String pkgName = reader.readLine();
                        if (pkgName == null) {
                            break;
                        } else if (pkgName == null || pkgName.length() == 0) {
                            Log.d(TAG, "parseTrustedAppStoreFile pkgName length is 0.  pkgName:" + pkgName);
                        } else {
                            this.mAppStoreList.add(pkgName);
                        }
                    } catch (IOException e2) {
                        e = e2;
                        bufferedReader = reader;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedReader = reader;
                    }
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                }
            }
        } catch (IOException e4) {
            e = e4;
            try {
                e.printStackTrace();
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e5) {
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e6) {
                    }
                }
                throw th;
            }
        }
    }

    private void setActivedAdmins() {
        List<String> admins = this.mCustomConfigInfo.getDefaultActivedAdmins();
        if (admins != null) {
            Log.d(TAG, "activedAdmins admins size = " + admins.size());
            try {
                for (String admin : admins) {
                    String[] pc = admin.split("/", 2);
                    setDevicePolicyManager(new ComponentName(pc[0], pc[1]), true);
                }
            } catch (Exception e) {
                Log.d(TAG, "activedAdmins exception occur! " + e);
            }
        }
    }

    private void setActivedAccessibilityServcies() {
        List<String> services = this.mCustomConfigInfo.getDefaultActivedAccessibilityServcies();
        if (services != null) {
            Log.d(TAG, "setActivedAccessibilityServcies services size = " + services.size() + ", services" + services);
            try {
                for (String service : services) {
                    String[] pc = service.split("/", 2);
                    setAccessibilityServcie(new ComponentName(pc[0], pc[1]), true);
                }
            } catch (Exception e) {
                Log.d(TAG, "setActivedAccessibilityServcies exception occur! " + e);
            }
        }
    }

    private void setDeviceOwner() {
        String packageName = this.mCustomConfigInfo.getDefaultDeviceOwner();
        Log.d(TAG, "setDeviceOwner packageName = " + packageName);
        if (packageName != null) {
            String[] pc = packageName.split("/", 2);
            long callingId = Binder.clearCallingIdentity();
            try {
                Class<?> dpm = Class.forName("android.app.admin.DevicePolicyManager");
                if (((Boolean) dpm.getMethod("isDeviceOwnerApp", new Class[]{String.class}).invoke(this.mDPM, new Object[]{pc[0]})).booleanValue()) {
                    Log.d(TAG, "device owner has been add.");
                } else if (VERSION.SDK_INT <= 23) {
                    dpm.getMethod("setDeviceOwner", new Class[]{String.class}).invoke(this.mDPM, new Object[]{pc[0]});
                } else {
                    ComponentName cn = new ComponentName(pc[0], pc[1]);
                    dpm.getMethod("setDeviceOwner", new Class[]{ComponentName.class}).invoke(this.mDPM, new Object[]{cn});
                }
                Log.d(TAG, "current deviceOwner" + this.mDPM.getDeviceOwner());
            } catch (Exception e) {
                Log.d(TAG, "setDeviceOwner exception occur! " + e);
                e.printStackTrace();
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    private void preSetAppOp() {
        List<DynamicRecord> opList = this.mCustomConfigInfo.getDefaultAllowOps();
        if (opList != null) {
            Log.d(TAG, "preSetAppOp opList size = " + opList.size());
            for (DynamicRecord item : opList) {
                Log.i(TAG, "preSetAppOp : mName = " + item.mName + ", type = " + item.mType + ", mAction = " + item.mAction + ", mValue = " + item.mValue + ", mNamespace = " + item.mNamespace + ", mMode = " + item.mMode + ", mValueStr = " + item.mValueStr);
            }
            long callingId = Binder.clearCallingIdentity();
            try {
                for (DynamicRecord opRecord : opList) {
                    String packageName = opRecord.mName;
                    String modeStr = opRecord.mMode;
                    int code = opRecord.mValue;
                    int mode = 3;
                    PackageInfo packageInfo = this.mPms.getPackageInfo(packageName, 0, 0);
                    if (!(packageInfo == null || packageInfo.applicationInfo == null || code <= -1)) {
                        String action = opRecord.mAction;
                        int oldMode = this.mAppOps.noteOpNoThrow(code, packageInfo.applicationInfo.uid, packageName);
                        if (Utils.OP_MODE_IGNORED.equals(modeStr)) {
                            mode = 1;
                        } else if (Utils.OP_MODE_ERRORED.equals(modeStr)) {
                            mode = 2;
                        } else if (Utils.OP_MODE_ALLOWED.equals(modeStr)) {
                            mode = 0;
                        }
                        Log.d(TAG, "preSetAppOp packageName = " + packageName + ", action = " + action + ", mode = " + mode + ", code = " + code);
                        if (Utils.ACTION_ADD.equals(action) && oldMode == 3 && oldMode != mode) {
                            this.mAppOps.setMode(code, packageInfo.applicationInfo.uid, packageName, mode);
                        } else if (Utils.ACTION_DELETE.equals(action) && oldMode != 3 && oldMode != mode) {
                            this.mAppOps.setMode(code, packageInfo.applicationInfo.uid, packageName, 3);
                        } else if (Utils.ACTION_UPDATE.equals(action) && oldMode != mode) {
                            this.mAppOps.setMode(code, packageInfo.applicationInfo.uid, packageName, mode);
                        }
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "preSetAppOp exception occur! " + e);
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    private void registerBroadcastReceiver() {
        if (this.mReceiver == null) {
            this.mReceiver = new VivoCustomReceiver(this, null);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter2.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter2.addDataScheme("package");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        this.mContext.registerReceiver(this.mReceiver, intentFilter2);
    }

    private void showDebugModeOverlay() {
        try {
            Class<?> c = Class.forName("android.app.ActivityManager");
            c.getDeclaredMethod("showCustomDebugModeOverlay", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendTestVersionWarningNotification() {
        String title = this.mContext.getResources().getString(51249707);
        String message = this.mContext.getResources().getString(51249708);
        if (this.mNotificationManager == null) {
            this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        }
        this.mNotificationManager.notify(891216, new Builder(this.mContext, SystemNotificationChannels.DEVELOPER).setSmallIcon(50462811).setWhen(0).setOngoing(true).setTicker(title).setDefaults(0).setColor(this.mContext.getColor(17170765)).setContentTitle(title).setContentText(message).setContentIntent(null).setVisibility(1).setFlag(32, true).build());
    }

    public boolean isDeviceRoot() {
        checkUp();
        boolean isRoot = SystemProperties.getInt("persist.sys.is_root", 0) == 1;
        Log.d(TAG, "is device root = " + isRoot);
        return isRoot;
    }

    public boolean shutDown() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        Log.d(TAG, "devcie shutDown from callingId: " + callingId);
        try {
            Class<?> powerManager = Class.forName("android.os.IPowerManager");
            if (VERSION.SDK_INT <= 23) {
                powerManager.getMethod("shutdown", new Class[]{Boolean.TYPE, Boolean.TYPE}).invoke(this.mPower, new Object[]{Boolean.valueOf(false), Boolean.valueOf(false)});
            } else {
                powerManager.getMethod("shutdown", new Class[]{Boolean.TYPE, String.class, Boolean.TYPE}).invoke(this.mPower, new Object[]{Boolean.valueOf(false), null, Boolean.valueOf(false)});
            }
            Binder.restoreCallingIdentity(callingId);
            return true;
        } catch (Exception e) {
            Log.d(TAG, "shutDown e = " + e.getMessage());
            Binder.restoreCallingIdentity(callingId);
            return false;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
    }

    public boolean reBoot() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        Log.d(TAG, "devcie reBoot from callingId: " + callingId);
        try {
            this.mPower.reboot(false, null, false);
            return true;
        } catch (Exception e) {
            Log.d(TAG, "reBoot e = " + e.getMessage());
            return false;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean formatSDCard() {
        checkUp();
        if (Utils.isSupportTF) {
            long callingId = Binder.clearCallingIdentity();
            Log.d(TAG, "formatSDCard from callingId = " + callingId);
            try {
                String path = Utils.getExternalSdPath(this.mStorageManager.getVolumePaths());
                if (path == null) {
                    Log.w(TAG, "formatSDCard failed because the sdcard is removed!");
                    return false;
                }
                boolean z;
                String status = this.mStorageManager.getVolumeState(path);
                if ("nofs".equals(status) || "unmounted".equals(status) || "unmountable".equals(status) || "mounted".equals(status)) {
                    z = true;
                } else {
                    z = "mounted_ro".equals(status);
                }
                if (z) {
                    new Thread() {
                        public void run() {
                            try {
                                String path = Utils.getExternalSdPath(VivoCustomService.this.mStorageManager.getVolumePaths());
                                Log.d(VivoCustomService.TAG, "formatSDCard path = " + path + ", unmountVolume begin!");
                                VivoCustomService.this.mMountService.unmountVolume(path, true, false);
                                VivoCustomService.this.mIsDoingUnmount = true;
                                Log.d(VivoCustomService.TAG, "formatSDCard path = " + path + ", unmountVolume end!");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    Log.d(TAG, "formatSDCard path = " + path);
                    Binder.restoreCallingIdentity(callingId);
                    return true;
                }
                Log.w(TAG, "formatSDCard failed because the sdcard's(path = " + path + ") state is " + status);
                Binder.restoreCallingIdentity(callingId);
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Failed to get state from storage manager", e);
                return false;
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        } else {
            Log.d(TAG, "formatSDCard fail for device is not support TF card");
            return false;
        }
    }

    public boolean setWifiState(int state) {
        if (state < 0 || state > 4) {
            throw new IllegalArgumentException("IllegalArgumentException:setWifiState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            int st = Secure.getInt(this.mContentResolver, "ct_network_wifi", 1);
            switch (state) {
                case 0:
                    if (this.mWifiManager.isWifiEnabled()) {
                        this.mWifiManager.setWifiEnabled(false);
                    }
                    Secure.putInt(this.mContentResolver, "ct_network_wifi", state);
                    break;
                case 1:
                    Secure.putInt(this.mContentResolver, "ct_network_wifi", state);
                    break;
                case 2:
                    if (st != 4) {
                        if (this.mWifiManager.isWifiEnabled()) {
                            this.mWifiManager.setWifiEnabled(false);
                            break;
                        }
                    }
                    Log.d(TAG, "setWifiState state(" + state + ") fail cause wifi is force turn on !");
                    Binder.restoreCallingIdentity(callingId);
                    return false;
                    break;
                case 3:
                    if (st != 0) {
                        if (!this.mWifiManager.isWifiEnabled()) {
                            this.mWifiManager.setWifiEnabled(true);
                            break;
                        }
                    }
                    Log.d(TAG, "setWifiState state(" + state + ") fail cause wifi is forbit !");
                    Binder.restoreCallingIdentity(callingId);
                    return false;
                    break;
                case 4:
                    if (!this.mWifiManager.isWifiEnabled()) {
                        this.mWifiManager.setWifiEnabled(true);
                    }
                    Secure.putInt(this.mContentResolver, "ct_network_wifi", state);
                    break;
                default:
                    throw new IllegalArgumentException("setWifiState fail : the para is not valid state=" + state);
            }
            Binder.restoreCallingIdentity(callingId);
            Log.d(TAG, "setWifiState state = " + state);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getWifiState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_network_wifi", 1);
            Log.d(TAG, "getWifiState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setWifiApState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setWifiApState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
                Secure.putInt(this.mContentResolver, "ct_network_wifi_ap", state);
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setWifiApState state = " + state);
                return true;
            default:
                try {
                    throw new IllegalArgumentException("setWifiApState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public int getWifiApState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_network_wifi_ap", 1);
            Log.d(TAG, "getWifiApState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setNFCState(int state) {
        if (state < 0 || state > 4) {
            throw new IllegalArgumentException("IllegalArgumentException:setNFCState state is illegal!");
        }
        checkUp();
        Log.d(TAG, "NFC feature supported:" + this.mPackageManager.hasSystemFeature("android.hardware.nfc"));
        int nfcOnOffState = -1;
        if (this.mPackageManager.hasSystemFeature("android.hardware.nfc")) {
            INfcAdapter service = getServiceInterface();
            if (service == null) {
                Log.d(TAG, "the nfc service is null!");
                return false;
            }
            try {
                nfcOnOffState = service.getState();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            long callingId = Binder.clearCallingIdentity();
            try {
                int st = Secure.getInt(this.mContentResolver, "ct_peripheral_nfc", 1);
                boolean disable;
                switch (state) {
                    case 0:
                        Secure.putInt(this.mContentResolver, "ct_peripheral_nfc", state);
                        break;
                    case 1:
                        Secure.putInt(this.mContentResolver, "ct_peripheral_nfc", state);
                        break;
                    case 2:
                        if (st == 4) {
                            Log.d(TAG, "setNFCState state(" + state + ") fail cause wifi is force turn on !");
                            Binder.restoreCallingIdentity(callingId);
                            return false;
                        } else if (nfcOnOffState == 3) {
                            disable = service.disable(true);
                            Binder.restoreCallingIdentity(callingId);
                            return disable;
                        }
                        break;
                    case 3:
                        if (st == 0) {
                            Log.d(TAG, "setNFCState state(" + state + ") fail cause wifi is forbit !");
                            Binder.restoreCallingIdentity(callingId);
                            return false;
                        } else if (nfcOnOffState != 3) {
                            try {
                                disable = service.enable();
                                Binder.restoreCallingIdentity(callingId);
                                return disable;
                            } catch (RemoteException e2) {
                                e2.printStackTrace();
                                Binder.restoreCallingIdentity(callingId);
                                return false;
                            }
                        }
                        break;
                    case 4:
                        Secure.putInt(this.mContentResolver, "ct_peripheral_nfc", state);
                        break;
                    default:
                        throw new IllegalArgumentException("setNFCState fail : the para is not valid state=" + state);
                }
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setNFCState state = " + state);
                return true;
            } catch (RemoteException e22) {
                e22.printStackTrace();
                Binder.restoreCallingIdentity(callingId);
                return false;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(callingId);
                throw th;
            }
        }
        Log.w(TAG, "setNFCState failed because the nfc featrue is not supported!");
        return false;
    }

    private INfcAdapter getServiceInterface() {
        IBinder b = ServiceManager.getService("nfc");
        if (b != null) {
            return INfcAdapter.Stub.asInterface(b);
        }
        Log.d(TAG, "ibinder is null .");
        return null;
    }

    public int getNFCState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_peripheral_nfc", 1);
            Log.d(TAG, "getNFCState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setBluetoothState(int state) {
        if (state < 0 || state > 4) {
            throw new IllegalArgumentException("IllegalArgumentException:setBluetoothState state is illegal!");
        }
        checkUp();
        if (this.mBluetoothAdapter == null) {
            this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            int st = Secure.getInt(this.mContentResolver, "ct_network_bluetooth", 1);
            switch (state) {
                case 0:
                    if (this.mBluetoothAdapter.isEnabled()) {
                        this.mBluetoothAdapter.disable();
                    }
                    Secure.putInt(this.mContentResolver, "ct_network_bluetooth", state);
                    break;
                case 1:
                    Secure.putInt(this.mContentResolver, "ct_network_bluetooth", state);
                    break;
                case 2:
                    if (st != 4) {
                        if (this.mBluetoothAdapter.isEnabled()) {
                            this.mBluetoothAdapter.disable();
                            break;
                        }
                    }
                    Log.d(TAG, "setBluetoothState state(" + state + ") fail cause bluetooth is force turn on !");
                    Binder.restoreCallingIdentity(callingId);
                    return false;
                    break;
                case 3:
                    if (st != 0) {
                        if (!this.mBluetoothAdapter.isEnabled()) {
                            this.mBluetoothAdapter.enable();
                            break;
                        }
                    }
                    Log.d(TAG, "setBluetoothState state(" + state + ") fail cause bluetooth is forbit !");
                    Binder.restoreCallingIdentity(callingId);
                    return false;
                    break;
                case 4:
                    if (!this.mBluetoothAdapter.isEnabled()) {
                        this.mBluetoothAdapter.enable();
                    }
                    Secure.putInt(this.mContentResolver, "ct_network_bluetooth", state);
                    break;
                default:
                    throw new IllegalArgumentException("setBluetoothState fail : the para is not valid state=" + state);
            }
            Binder.restoreCallingIdentity(callingId);
            Log.d(TAG, "setBluetoothState state = " + state);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getBluetoothState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_network_bluetooth", 1);
            Log.d(TAG, "getBluetoothState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setBluetoothApState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setBluetoothApState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
                Secure.putInt(this.mContentResolver, "ct_network_bluetooth_ap", state);
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setBluetoothApState state = " + state);
                return true;
            default:
                try {
                    throw new IllegalArgumentException("setBluetoothApState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public int getBluetoothApState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_network_bluetooth_ap", 1);
            Log.d(TAG, "getBluetoothApState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setGpsLocationState(int state) {
        if (state < 0 || state > 4) {
            throw new IllegalArgumentException("IllegalArgumentException:setGpsLocationState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            int mod = Secure.getInt(this.mContentResolver, "location_mode", 3);
            int st = Secure.getInt(this.mContentResolver, "ct_peripheral_location_gps", 1);
            switch (state) {
                case 0:
                    if (mod == 3) {
                        Secure.putInt(this.mContentResolver, "location_mode", 2);
                    } else if (mod == 1) {
                        Secure.putInt(this.mContentResolver, "location_mode", 0);
                    }
                    Secure.putInt(this.mContentResolver, "ct_peripheral_location_gps", state);
                    Secure.setLocationProviderEnabled(this.mContentResolver, "gps", false);
                    break;
                case 1:
                    Secure.putInt(this.mContentResolver, "ct_peripheral_location_gps", state);
                    break;
                case 2:
                    if (st != 2) {
                        if (mod == 3) {
                            Secure.putInt(this.mContentResolver, "location_mode", 2);
                        } else if (mod == 1) {
                            Secure.putInt(this.mContentResolver, "location_mode", 0);
                        }
                        Secure.setLocationProviderEnabled(this.mContentResolver, "gps", false);
                        break;
                    }
                    Log.d(TAG, "setGpsLocationState state(" + state + ") fail cause gps location is force turn on !");
                    Binder.restoreCallingIdentity(callingId);
                    return false;
                case 3:
                    if (st != 0) {
                        if (mod == 0) {
                            Secure.putInt(this.mContentResolver, "location_mode", 1);
                        } else if (mod == 2) {
                            Secure.putInt(this.mContentResolver, "location_mode", 3);
                        }
                        Secure.setLocationProviderEnabled(this.mContentResolver, "gps", true);
                        break;
                    }
                    Log.d(TAG, "setGpsLocationState state(" + state + ") fail cause gps location is forbit !");
                    Binder.restoreCallingIdentity(callingId);
                    return false;
                case 4:
                    if (mod == 0) {
                        Secure.putInt(this.mContentResolver, "location_mode", 1);
                    } else if (mod == 2) {
                        Secure.putInt(this.mContentResolver, "location_mode", 3);
                    }
                    Secure.putInt(this.mContentResolver, "ct_peripheral_location_gps", 2);
                    Secure.setLocationProviderEnabled(this.mContentResolver, "gps", true);
                    break;
                default:
                    throw new IllegalArgumentException("setGpsLocationState fail : the para is not valid state=" + state);
            }
            Binder.restoreCallingIdentity(callingId);
            Log.d(TAG, "setGpsLocationState state = " + state);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getGpsLocationState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            int state = Secure.getInt(this.mContentResolver, "ct_peripheral_location_gps", 1);
            if (state == 2) {
                state = 4;
            }
            Binder.restoreCallingIdentity(callingId);
            Log.d(TAG, "getGpsLocationState state = " + state);
            return state;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setNetworkLocationState(int state) {
        if (state < 0 || state > 4) {
            throw new IllegalArgumentException("IllegalArgumentException:setNetworkLocationState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            int mod = Secure.getInt(this.mContentResolver, "location_mode", 3);
            int st = Secure.getInt(this.mContentResolver, "ct_peripheral_location_network", 1);
            switch (state) {
                case 0:
                    if (mod == 3) {
                        Secure.putInt(this.mContentResolver, "location_mode", 1);
                    } else if (mod == 2) {
                        Secure.putInt(this.mContentResolver, "location_mode", 0);
                    }
                    Secure.putInt(this.mContentResolver, "ct_peripheral_location_network", state);
                    Secure.setLocationProviderEnabled(this.mContentResolver, "network", false);
                    break;
                case 1:
                    Secure.putInt(this.mContentResolver, "ct_peripheral_location_network", state);
                    break;
                case 2:
                    if (st != 2) {
                        if (mod == 3) {
                            Secure.putInt(this.mContentResolver, "location_mode", 1);
                        } else if (mod == 2) {
                            Secure.putInt(this.mContentResolver, "location_mode", 0);
                        }
                        Secure.setLocationProviderEnabled(this.mContentResolver, "network", false);
                        break;
                    }
                    Log.d(TAG, "setNetworkLocationState state(" + state + ") fail cause network location is force turn on !");
                    Binder.restoreCallingIdentity(callingId);
                    return false;
                case 3:
                    if (st != 0) {
                        if (mod == 0) {
                            Secure.putInt(this.mContentResolver, "location_mode", 2);
                        } else if (mod == 1) {
                            Secure.putInt(this.mContentResolver, "location_mode", 3);
                        }
                        Secure.setLocationProviderEnabled(this.mContentResolver, "network", true);
                        break;
                    }
                    Log.d(TAG, "setNetworkLocationState state(" + state + ") fail cause network location is forbit !");
                    Binder.restoreCallingIdentity(callingId);
                    return false;
                case 4:
                    if (mod == 0) {
                        Secure.putInt(this.mContentResolver, "location_mode", 2);
                    } else if (mod == 1) {
                        Secure.putInt(this.mContentResolver, "location_mode", 3);
                    }
                    Secure.putInt(this.mContentResolver, "ct_peripheral_location_network", 2);
                    Secure.setLocationProviderEnabled(this.mContentResolver, "network", true);
                    break;
                default:
                    throw new IllegalArgumentException("setNetworkLocationState fail : the para is not valid state=" + state);
            }
            Binder.restoreCallingIdentity(callingId);
            Log.d(TAG, "setNetworkLocationState state = " + state);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getNetworkLocationState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            int state = Secure.getInt(this.mContentResolver, "ct_peripheral_location_network", 1);
            if (state == 2) {
                state = 4;
            }
            Binder.restoreCallingIdentity(callingId);
            Log.d(TAG, "getNetworkLocationState state = " + state);
            return state;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setUsbTransferState(int state) {
        if (state < 0 || state > 5) {
            throw new IllegalArgumentException("IllegalArgumentException:setUsbTransferState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
            case 4:
            case 5:
                Secure.putInt(this.mContentResolver, "ct_peripheral_usbtransfer", state);
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setUsbTransferState state = " + state);
                return true;
            default:
                try {
                    throw new IllegalArgumentException("setUsbTransferState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public int getUsbTransferState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_peripheral_usbtransfer", 1);
            Log.d(TAG, "getUsbTransferState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setUsbApState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setUsbApState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
                Secure.putInt(this.mContentResolver, "ct_peripheral_usbtransfer_ap", state);
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setUsbApState state = " + state);
                return true;
            default:
                try {
                    throw new IllegalArgumentException("setUsbApState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public int getUsbApState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_peripheral_usbtransfer_ap", 1);
            Log.d(TAG, "getUsbApState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setCameraState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setCameraState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
                Secure.putInt(this.mContentResolver, "ct_peripheral_camera", state);
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setCameraState state = " + state);
                return true;
            default:
                try {
                    throw new IllegalArgumentException("setCameraState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public int getCameraState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_peripheral_camera", 1);
            Log.d(TAG, "getCameraState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.vivo.services.cust.server.VivoCustomService.setMicrophoneState(int):boolean, dom blocks: [B:7:0x0018, B:16:0x0040]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1249)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public boolean setMicrophoneState(int r13) {
        /*
        r12 = this;
        r11 = 1;
        if (r13 < 0) goto L_0x0005;
    L_0x0003:
        if (r13 <= r11) goto L_0x000e;
    L_0x0005:
        r7 = new java.lang.IllegalArgumentException;
        r8 = "IllegalArgumentException:setMicrophoneState state is illegal!";
        r7.<init>(r8);
        throw r7;
    L_0x000e:
        r12.checkUp();
        r2 = android.os.Binder.clearCallingIdentity();
        switch(r13) {
            case 0: goto L_0x0037;
            case 1: goto L_0x0037;
            default: goto L_0x0018;
        };
    L_0x0018:
        r7 = new java.lang.IllegalArgumentException;	 Catch:{ all -> 0x0032 }
        r8 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0032 }
        r8.<init>();	 Catch:{ all -> 0x0032 }
        r9 = "setMicrophoneState fail : the para is not valid state=";	 Catch:{ all -> 0x0032 }
        r8 = r8.append(r9);	 Catch:{ all -> 0x0032 }
        r8 = r8.append(r13);	 Catch:{ all -> 0x0032 }
        r8 = r8.toString();	 Catch:{ all -> 0x0032 }
        r7.<init>(r8);	 Catch:{ all -> 0x0032 }
        throw r7;	 Catch:{ all -> 0x0032 }
    L_0x0032:
        r7 = move-exception;
        android.os.Binder.restoreCallingIdentity(r2);
        throw r7;
    L_0x0037:
        r7 = r12.mContentResolver;	 Catch:{ all -> 0x0032 }
        r8 = "ct_peripheral_microphone";	 Catch:{ all -> 0x0032 }
        android.provider.Settings.Secure.putInt(r7, r8, r13);	 Catch:{ all -> 0x0032 }
        r1 = 0;
        r7 = "android.media.AudioFeatures";	 Catch:{ Exception -> 0x00c8 }
        r0 = java.lang.Class.forName(r7);	 Catch:{ Exception -> 0x00c8 }
        r7 = 3;	 Catch:{ Exception -> 0x00c8 }
        r7 = new java.lang.Class[r7];	 Catch:{ Exception -> 0x00c8 }
        r8 = android.content.Context.class;	 Catch:{ Exception -> 0x00c8 }
        r9 = 0;	 Catch:{ Exception -> 0x00c8 }
        r7[r9] = r8;	 Catch:{ Exception -> 0x00c8 }
        r8 = java.lang.String.class;	 Catch:{ Exception -> 0x00c8 }
        r9 = 1;	 Catch:{ Exception -> 0x00c8 }
        r7[r9] = r8;	 Catch:{ Exception -> 0x00c8 }
        r8 = java.lang.Object.class;	 Catch:{ Exception -> 0x00c8 }
        r9 = 2;	 Catch:{ Exception -> 0x00c8 }
        r7[r9] = r8;	 Catch:{ Exception -> 0x00c8 }
        r4 = r0.getConstructor(r7);	 Catch:{ Exception -> 0x00c8 }
        r7 = 3;	 Catch:{ Exception -> 0x00c8 }
        r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x00c8 }
        r8 = r12.mContext;	 Catch:{ Exception -> 0x00c8 }
        r9 = 0;	 Catch:{ Exception -> 0x00c8 }
        r7[r9] = r8;	 Catch:{ Exception -> 0x00c8 }
        r8 = "MICROPHONE";	 Catch:{ Exception -> 0x00c8 }
        r9 = 1;	 Catch:{ Exception -> 0x00c8 }
        r7[r9] = r8;	 Catch:{ Exception -> 0x00c8 }
        r8 = 0;	 Catch:{ Exception -> 0x00c8 }
        r9 = 2;	 Catch:{ Exception -> 0x00c8 }
        r7[r9] = r8;	 Catch:{ Exception -> 0x00c8 }
        r1 = r4.newInstance(r7);	 Catch:{ Exception -> 0x00c8 }
        r7 = "setMicPhoneEnable";	 Catch:{ Exception -> 0x00c8 }
        r8 = 2;	 Catch:{ Exception -> 0x00c8 }
        r8 = new java.lang.Class[r8];	 Catch:{ Exception -> 0x00c8 }
        r9 = java.lang.String.class;	 Catch:{ Exception -> 0x00c8 }
        r10 = 0;	 Catch:{ Exception -> 0x00c8 }
        r8[r10] = r9;	 Catch:{ Exception -> 0x00c8 }
        r9 = java.lang.Object.class;	 Catch:{ Exception -> 0x00c8 }
        r10 = 1;	 Catch:{ Exception -> 0x00c8 }
        r8[r10] = r9;	 Catch:{ Exception -> 0x00c8 }
        r6 = r0.getDeclaredMethod(r7, r8);	 Catch:{ Exception -> 0x00c8 }
        if (r13 != 0) goto L_0x00b7;	 Catch:{ Exception -> 0x00c8 }
    L_0x0089:
        r7 = 2;	 Catch:{ Exception -> 0x00c8 }
        r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x00c8 }
        r8 = "MICROPHONE:state=false";	 Catch:{ Exception -> 0x00c8 }
        r9 = 0;	 Catch:{ Exception -> 0x00c8 }
        r7[r9] = r8;	 Catch:{ Exception -> 0x00c8 }
        r8 = 0;	 Catch:{ Exception -> 0x00c8 }
        r9 = 1;	 Catch:{ Exception -> 0x00c8 }
        r7[r9] = r8;	 Catch:{ Exception -> 0x00c8 }
        r6.invoke(r1, r7);	 Catch:{ Exception -> 0x00c8 }
    L_0x0099:
        android.os.Binder.restoreCallingIdentity(r2);
        r7 = "VCS";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r9 = "setMicrophoneState state = ";
        r8 = r8.append(r9);
        r8 = r8.append(r13);
        r8 = r8.toString();
        android.util.Log.d(r7, r8);
        return r11;
    L_0x00b7:
        r7 = 2;
        r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x00c8 }
        r8 = "MICROPHONE:state=true";	 Catch:{ Exception -> 0x00c8 }
        r9 = 0;	 Catch:{ Exception -> 0x00c8 }
        r7[r9] = r8;	 Catch:{ Exception -> 0x00c8 }
        r8 = 0;	 Catch:{ Exception -> 0x00c8 }
        r9 = 1;	 Catch:{ Exception -> 0x00c8 }
        r7[r9] = r8;	 Catch:{ Exception -> 0x00c8 }
        r6.invoke(r1, r7);	 Catch:{ Exception -> 0x00c8 }
        goto L_0x0099;
    L_0x00c8:
        r5 = move-exception;
        r7 = "VCS";	 Catch:{ all -> 0x0032 }
        r8 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0032 }
        r8.<init>();	 Catch:{ all -> 0x0032 }
        r9 = "MICROPHONE e = ";	 Catch:{ all -> 0x0032 }
        r8 = r8.append(r9);	 Catch:{ all -> 0x0032 }
        r8 = r8.append(r5);	 Catch:{ all -> 0x0032 }
        r8 = r8.toString();	 Catch:{ all -> 0x0032 }
        android.util.Log.d(r7, r8);	 Catch:{ all -> 0x0032 }
        goto L_0x0099;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.vivo.services.cust.server.VivoCustomService.setMicrophoneState(int):boolean");
    }

    public int getMicrophoneState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_peripheral_microphone", 1);
            Log.d(TAG, "getMicrophoneState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setScreenshotState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setScreenshotState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
                SystemProperties.set("persist.sys.gn.screen", Integer.toString(state));
                Secure.putInt(this.mContentResolver, "ct_peripheral_screen", state);
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setScreenshotState state = " + state);
                return true;
            default:
                try {
                    throw new IllegalArgumentException("setScreenshotState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public int getScreenshotState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_peripheral_screen", 1);
            Log.d(TAG, "getScreenshotState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setSDCardState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setSDCardState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
                Secure.putInt(this.mContentResolver, "ct_peripheral_sdcard", state);
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setSDCardState state = " + state);
                return true;
            default:
                try {
                    throw new IllegalArgumentException("setSDCardState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public int getSDCardState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_peripheral_sdcard", 1);
            Log.d(TAG, "getSDCardState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setOTGState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setOTGState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
                Secure.putInt(this.mContentResolver, "ct_peripheral_otg", state);
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setOTGState state = " + state);
                return true;
            default:
                try {
                    throw new IllegalArgumentException("setOTGState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public int getOTGState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_peripheral_otg", 1);
            Log.d(TAG, "getOTGState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setUsbDebugState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setUsbDebugState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
                Secure.putInt(this.mContentResolver, "ct_security_usbdebug", state);
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setUsbDebugState state = " + state);
                return true;
            default:
                try {
                    throw new IllegalArgumentException("setUsbDebugState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public int getUsbDebugState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_security_usbdebug", 1);
            Log.d(TAG, "getUsbDebugState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setAPNState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setAPNState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
                Secure.putInt(this.mContentResolver, "ct_emm_apn_activemode", state);
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setAPNState state = " + state);
                return true;
            default:
                try {
                    throw new IllegalArgumentException("setAPNState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public int getAPNState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_emm_apn_activemode", 1);
            Log.d(TAG, "getAPNState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setVPNState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setVPNState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
                Secure.putInt(this.mContentResolver, "ct_network_vpn", state);
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setVPNState state = " + state);
                return true;
            default:
                try {
                    throw new IllegalArgumentException("setVPNState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public int getVPNState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_network_vpn", 1);
            Log.d(TAG, "getVPNState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setTimeState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setTimeState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
                Secure.putInt(this.mContentResolver, "ct_security_time", state);
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setTimeState state = " + state);
                return true;
            default:
                try {
                    throw new IllegalArgumentException("setTimeState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public int getTimeState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_security_time", 1);
            Log.d(TAG, "getTimeState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setRestoreState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setRestoreState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
                Secure.putInt(this.mContentResolver, "ct_security_restore", state);
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setRestoreState state = " + state);
                return true;
            default:
                try {
                    throw new IllegalArgumentException("setRestoreState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public int getRestoreState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_security_restore", 1);
            Log.d(TAG, "getRestoreState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setFactoryResetState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setFactoryResetState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
                Secure.putInt(this.mContentResolver, "ct_security_factoryreset", state);
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setFactoryResetState state = " + state);
                return true;
            default:
                try {
                    throw new IllegalArgumentException("setFactoryResetState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public int getFactoryResetState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_security_factoryreset", 1);
            Log.d(TAG, "getFactoryResetState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void installPackage(String path, int flags, String installerPackage) {
        installPackageWithObserver(path, flags, installerPackage, new LocalPackageInstallObserver().getBinder());
    }

    public void installPackageWithObserver(String path, int flags, String installerPackage, IPackageInstallObserver2 observer) {
        if (TextUtils.isEmpty(path)) {
            throw new IllegalArgumentException("IllegalArgumentException:installPackage path is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        Log.d(TAG, "installPackage pid = " + Process.myPid());
        try {
            Log.d(TAG, "installPackage  packagePath:" + path + " installFlags:" + flags);
            Class<?> pm = Class.forName("android.content.pm.IPackageManager");
            if (VERSION.SDK_INT <= 23) {
                VerificationParams verificationParams = (VerificationParams) VerificationParams.class.getConstructor(new Class[]{Uri.class, Uri.class, Uri.class, Integer.TYPE, Class.forName("android.content.pm.ManifestDigest")}).newInstance(new Object[]{null, null, null, Integer.valueOf(-1), null});
                pm.getMethod("installPackageAsUser", new Class[]{String.class, IPackageInstallObserver2.class, Integer.TYPE, String.class, VerificationParams.class, String.class, Integer.TYPE}).invoke(this.mPms, new Object[]{path, observer, Integer.valueOf(flags), installerPackage, verificationParams, null, Integer.valueOf(0)});
            } else {
                pm.getMethod("installPackageAsUser", new Class[]{String.class, IPackageInstallObserver2.class, Integer.TYPE, String.class, Integer.TYPE}).invoke(this.mPms, new Object[]{path, observer, Integer.valueOf(flags), installerPackage, Integer.valueOf(0)});
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to talk to package manager", e);
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
    }

    public void deletePackage(String packageName, int flags) {
        deletePackageWithObserver(packageName, flags, new LocalPackageDeleteObserver(true).getBinder());
    }

    public void deletePackageWithObserver(String packageName, int flags, IPackageDeleteObserver2 observer) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("IllegalArgumentException:deletePackage packageName is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            UserHandle userHandle = Process.myUserHandle();
            Log.d(TAG, "deletePackage  packageName:" + packageName + " flags:" + flags + " userId:" + userHandle.getIdentifier());
            PackageInfo info = this.mPackageManager.getPackageInfo(packageName, 0);
            if (info != null) {
                this.mPms.deletePackageVersioned(new VersionedPackage(packageName, info.versionCode), observer, userHandle.getIdentifier(), flags);
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to talk to package manager", e);
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
    }

    public void disablePackage(String packageName, int flags) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("IllegalArgumentException:disablePackage packageName is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            setApplicationEnabled(packageName, flags, false);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addDisabledApps(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addDisabledApps packageNames is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            List<String> persistApps = this.mVivoCustomDbBridge.getPersistApps();
            List<String> pkgAdded = new ArrayList();
            for (String pkg : packageNames) {
                if (persistApps.contains(pkg)) {
                    Log.d(TAG, "pkg is persist apps, can not be disabled! pkg=" + pkg);
                } else if (setApplicationEnabled(pkg, 0, false)) {
                    pkgAdded.add(pkg);
                }
            }
            if (pkgAdded.size() > 0) {
                this.mVivoCustomDbBridge.setDisallowedRunningApp(pkgAdded, true);
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Exception e) {
            Log.d(TAG, "addDisabledApps  FAILD!!");
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
    }

    public void removeDisableApps(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:removeDisableApps packageNames is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            List<String> pkgRemoved = new ArrayList();
            List<String> disAllowedRunApps = this.mVivoCustomDbBridge.getDisallowedRunningApp();
            if (disAllowedRunApps != null) {
                for (String pkg : packageNames) {
                    if (disAllowedRunApps.contains(pkg) && setApplicationEnabled(pkg, 0, true)) {
                        pkgRemoved.add(pkg);
                    }
                }
                if (pkgRemoved.size() > 0) {
                    this.mVivoCustomDbBridge.setDisallowedRunningApp(pkgRemoved, false);
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Exception e) {
            Log.d(TAG, "removeDisableApps FAILD!!");
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
    }

    private boolean setApplicationEnabled(String pkg, int flags, boolean enable) {
        if (enable) {
            try {
                if (pkg.equals("com.android.dialer") && Utils.VIVO_CUSTOM_SUPPORT.equals(Utils.FLAG_YDHYY)) {
                    setTelephonyPhoneState(0, 1, 1);
                    return true;
                } else if (pkg.equals("com.android.mms") && Utils.VIVO_CUSTOM_SUPPORT.equals(Utils.FLAG_YDHYY)) {
                    setTelephonySmsState(0, 1, 1);
                    setTelephonyMmsState(0, 1, 1);
                    return true;
                } else {
                    Log.d(TAG, "enable app:" + pkg);
                    this.mPms.setApplicationEnabledSetting(pkg, 1, flags, 0, this.mContext.getPackageName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "enable " + pkg + " FAILD!!");
                return false;
            }
        }
        try {
            if (pkg.equals("com.android.dialer") && Utils.VIVO_CUSTOM_SUPPORT.equals(Utils.FLAG_YDHYY)) {
                setTelephonyPhoneState(0, 0, 0);
                return true;
            } else if (pkg.equals("com.android.mms") && Utils.VIVO_CUSTOM_SUPPORT.equals(Utils.FLAG_YDHYY)) {
                setTelephonySmsState(0, 0, 0);
                setTelephonyMmsState(0, 0, 0);
                return true;
            } else {
                PackageInfo packageInfo = this.mPackageManager.getPackageInfo(pkg, 0);
                if (packageInfo == null || (Utils.isAllowDisabled(packageInfo) ^ 1) != 0) {
                    return false;
                }
                Log.d(TAG, "disable app:" + pkg);
                this.mPms.setApplicationEnabledSetting(pkg, 2, flags, 0, this.mContext.getPackageName());
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            Log.d(TAG, "disable " + pkg + " FAILD!!");
            return false;
        }
        return true;
    }

    public List<String> getDisableApps() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getDisallowedRunningApp();
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void setInstallPattern(int pattern) {
        if (pattern < 0 || pattern > 2) {
            throw new IllegalArgumentException("IllegalArgumentException:setInstallPattern pattern is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_app_install_restrict_pattern", pattern);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getInstallPattern() {
        long callingId = Binder.clearCallingIdentity();
        int state = 0;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_app_install_restrict_pattern", 0);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addInstallBlackList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addInstallBlackList packageNames is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setPackageInstallBlackList(packageNames, true);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void deleteInstallBlackList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deleteInstallBlackList packageNames is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setPackageInstallBlackList(packageNames, false);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getInstallBlackList() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getPackageInstallBlackList();
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addInstallWhiteList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addInstallWhiteList packageNames is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setPackageInstallWhiteList(packageNames, true);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void deleteInstallWhiteList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deleteInstallWhiteList packageNames is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setPackageInstallWhiteList(packageNames, false);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getInstallWhiteList() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getPackageInstallWhiteList();
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void setUninstallPattern(int pattern) {
        if (pattern < 0 || pattern > 2) {
            throw new IllegalArgumentException("IllegalArgumentException:setInstallPattern pattern is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_app_uninstall_restrict_pattern", pattern);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getUninstallPattern() {
        long callingId = Binder.clearCallingIdentity();
        int state = 0;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_app_uninstall_restrict_pattern", 0);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addUninstallBlackList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addUninstallBlackList packageNames is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setPackageUnInstallBlackList(packageNames, true);
            if (this.mUninstallListListener != null) {
                Log.d(TAG, "uninstall updateBlackList!");
                try {
                    this.mUninstallListListener.updateBlackList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void deleteUninstallBlackList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deleteUninstallBlackList packageNames is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setPackageUnInstallBlackList(packageNames, false);
            if (this.mUninstallListListener != null) {
                Log.d(TAG, "uninstall updateBlackList!");
                try {
                    this.mUninstallListListener.updateBlackList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getUninstallBlackList() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getPackageUnInstallBlackList();
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addUninstallWhiteList(List<String> list) {
        checkUp();
    }

    public void deleteUninstallWhiteList(List<String> list) {
        checkUp();
    }

    public List<String> getUninstallWhiteList() {
        return null;
    }

    public void registerUninstallListChangeCallback(IUninstallListListener callback) {
        Log.d(TAG, "register UninstallListChangeListener ");
        this.mUninstallListListener = callback;
    }

    public void addPersistApps(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addPersistApps packageNames is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setPersistApps(packageNames, true);
            for (int i = 0; i < packageNames.size(); i++) {
                Log.d(TAG, "add persistent app: " + ((String) packageNames.get(i)) + " " + (this.mActivityManager.addPreloadProcess((String) packageNames.get(i), 0) ? "successed" : "failed"));
            }
            if (packageNames != null && packageNames.size() > 0) {
                Intent i2 = new Intent();
                i2.setAction(Utils.ACTION_PERSIST_APPS_UPDATED);
                i2.addFlags(536870912);
                this.mContext.sendBroadcast(i2);
                Log.d(TAG, "Broadcast: persist apps list updated!");
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void removePersistApps(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:removePersistApps packageNames is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setPersistApps(packageNames, false);
            for (int i = 0; i < packageNames.size(); i++) {
                Log.d(TAG, "remove persistent app: " + ((String) packageNames.get(i)) + "  " + (this.mActivityManager.removePreloadProcess((String) packageNames.get(i), 0) ? "successed" : "failed"));
            }
            if (packageNames != null && packageNames.size() > 0) {
                Intent i2 = new Intent();
                i2.setAction(Utils.ACTION_PERSIST_APPS_UPDATED);
                i2.addFlags(536870912);
                this.mContext.sendBroadcast(i2);
                Log.d(TAG, "Broadcast: persist apps list updated!");
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getPersistApps() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getPersistApps();
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    private void appNetworkStateChange() {
        Intent intent = new Intent("iqoo.secure.action_fire_wall_changed");
        intent.addFlags(536870912);
        this.mContext.sendBroadcast(intent);
        if (Utils.DEBUG_EXTRA) {
            Log.d(TAG, "appNetworkStateChange send broadcase");
        }
    }

    public void setAppDataNetworkPattern(int pattern) {
        if (pattern < 0 || pattern > 3) {
            throw new IllegalArgumentException("IllegalArgumentException:setInstallPattern pattern is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, Utils.CT_APP_DATA_NETWORK_PATTERN, pattern);
            appNetworkStateChange();
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getAppDataNetworkPattern() {
        long callingId = Binder.clearCallingIdentity();
        int state = 0;
        try {
            state = Secure.getInt(this.mContentResolver, Utils.CT_APP_DATA_NETWORK_PATTERN, 0);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addAppDataNetworkBlackList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addAppDataNetworkBlackList packageNames is illegal!");
        }
        checkUp();
        this.mVivoCustomDbBridge.setPackageDataNetworkBlackList(packageNames, true);
        appNetworkStateChange();
    }

    public void deleteAppDataNetworkBlackList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addAppDataNetworkBlackList packageNames is illegal!");
        }
        checkUp();
        this.mVivoCustomDbBridge.setPackageDataNetworkBlackList(packageNames, false);
        appNetworkStateChange();
    }

    public List<String> getAppDataNetworkBlackList() {
        List<String> dataNetworkList = this.mVivoCustomDbBridge.getPackageDataNetworkBlackList();
        if (!(dataNetworkList == null || (dataNetworkList.isEmpty() ^ 1) == 0 || Utils.disallowNetworkRestrictApps == null || (Utils.disallowNetworkRestrictApps.isEmpty() ^ 1) == 0)) {
            for (String packageName : Utils.disallowNetworkRestrictApps) {
                if (dataNetworkList.contains(packageName)) {
                    dataNetworkList.remove(packageName);
                }
            }
        }
        return dataNetworkList;
    }

    public void addAppDataNetworkWhiteList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addAppDataNetworkWhiteList packageNames is illegal!");
        }
        checkUp();
        this.mVivoCustomDbBridge.setPackageDataNetworkWhiteList(packageNames, true);
        appNetworkStateChange();
    }

    public void deleteAppDataNetworkWhiteList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deleteAppDataNetworkWhiteList packageNames is illegal!");
        }
        checkUp();
        this.mVivoCustomDbBridge.setPackageDataNetworkWhiteList(packageNames, false);
        appNetworkStateChange();
    }

    public List<String> getAppDataNetworkWhiteList() {
        List<String> dataNetworkList = this.mVivoCustomDbBridge.getPackageDataNetworkWhiteList();
        if (!(Utils.disallowNetworkRestrictApps == null || (Utils.disallowNetworkRestrictApps.isEmpty() ^ 1) == 0)) {
            for (String packageName : Utils.disallowNetworkRestrictApps) {
                if (!(dataNetworkList == null || (dataNetworkList.contains(packageName) ^ 1) == 0)) {
                    dataNetworkList.add(packageName);
                }
            }
        }
        return dataNetworkList;
    }

    public boolean isAppDataNetworkWhiteListNotNull() {
        if (this.mVivoCustomDbBridge.getPackageDataNetworkWhiteList().size() > 0) {
            return true;
        }
        return false;
    }

    public void setAppWifiNetworkPattern(int pattern) {
        if (pattern < 0 || pattern > 3) {
            throw new IllegalArgumentException("IllegalArgumentException:setInstallPattern pattern is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, Utils.CT_APP_WIFI_NETWORK_PATTERN, pattern);
            appNetworkStateChange();
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getAppWifiNetworkPattern() {
        long callingId = Binder.clearCallingIdentity();
        int state = 0;
        try {
            state = Secure.getInt(this.mContentResolver, Utils.CT_APP_WIFI_NETWORK_PATTERN, 0);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addAppWifiNetworkBlackList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addAppWifiNetworkBlackList packageNames is illegal!");
        }
        checkUp();
        this.mVivoCustomDbBridge.setPackageWifiNetworkBlackList(packageNames, true);
        appNetworkStateChange();
    }

    public void deleteAppWifiNetworkBlackList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deleteAppWifiNetworkBlackList packageNames is illegal!");
        }
        checkUp();
        this.mVivoCustomDbBridge.setPackageWifiNetworkBlackList(packageNames, false);
        appNetworkStateChange();
    }

    public List<String> getAppWifiNetworkBlackList() {
        List<String> wifiNetworkList = this.mVivoCustomDbBridge.getPackageWifiNetworkBlackList();
        if (!(wifiNetworkList == null || (wifiNetworkList.isEmpty() ^ 1) == 0 || Utils.disallowNetworkRestrictApps == null || (Utils.disallowNetworkRestrictApps.isEmpty() ^ 1) == 0)) {
            for (String packageName : Utils.disallowNetworkRestrictApps) {
                if (wifiNetworkList.contains(packageName)) {
                    wifiNetworkList.remove(packageName);
                }
            }
        }
        return wifiNetworkList;
    }

    public void addAppWifiNetworkWhiteList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addAppWifiNetworkWhiteList packageNames is illegal!");
        }
        checkUp();
        this.mVivoCustomDbBridge.setPackageWifiNetworkWhiteList(packageNames, true);
        appNetworkStateChange();
    }

    public void deleteAppWifiNetworkWhiteList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deleteAppWifiNetworkWhiteList packageNames is illegal!");
        }
        checkUp();
        this.mVivoCustomDbBridge.setPackageWifiNetworkWhiteList(packageNames, false);
        appNetworkStateChange();
    }

    public List<String> getAppWifiNetworkWhiteList() {
        List<String> wifiNetworkList = this.mVivoCustomDbBridge.getPackageWifiNetworkWhiteList();
        if (!(Utils.disallowNetworkRestrictApps == null || (Utils.disallowNetworkRestrictApps.isEmpty() ^ 1) == 0)) {
            for (String packageName : Utils.disallowNetworkRestrictApps) {
                if (!(wifiNetworkList == null || (wifiNetworkList.contains(packageName) ^ 1) == 0)) {
                    wifiNetworkList.add(packageName);
                }
            }
        }
        return wifiNetworkList;
    }

    public boolean isAppWifiNetworkWhiteListNotNull() {
        if (this.mVivoCustomDbBridge.getPackageWifiNetworkWhiteList().size() > 0) {
            return true;
        }
        return false;
    }

    public void clearPackageState(int state) {
        if (state < 0 || state > 9) {
            throw new IllegalArgumentException("IllegalArgumentException:setWifiState state is illegal!");
        }
        checkUp();
        if (state == 0) {
            List<String> persistList = getPersistApps();
            if (persistList != null) {
                removePersistApps(persistList);
            }
        } else if (1 == state) {
            List<String> disabledList = getDisableApps();
            if (disabledList != null) {
                removeDisableApps(disabledList);
            }
        }
        this.mVivoCustomDbBridge.clearPackageState(state);
        switch (state) {
            case 6:
            case 7:
            case Utils.WIFIBL /*8*/:
            case Utils.WIFIWL /*9*/:
                appNetworkStateChange();
                return;
            default:
                return;
        }
    }

    public void setDomainNamePattern(int pattern) {
        if (pattern < 0 || pattern > 2) {
            throw new IllegalArgumentException("IllegalArgumentException:setDomainNamePattern pattern is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_network_domainname_restrict_pattern", pattern);
            if (this.mNetworkListDelegate != null) {
                Log.d(TAG, "setDomainNamePattern!");
                try {
                    this.mNetworkListDelegate.updateBlackList(0);
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getDomainNamePattern() {
        long callingId = Binder.clearCallingIdentity();
        int state = 0;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_network_domainname_restrict_pattern", 0);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void setDomainNameBlackList(List<String> urls, boolean isBlackList) {
        if (urls == null) {
            throw new IllegalArgumentException("IllegalArgumentException:setDomainNameBlackList urls is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setDomainNameBlackList(urls, isBlackList);
            if (this.mNetworkListDelegate != null) {
                Log.d(TAG, "domain updateBlackList!");
                try {
                    this.mNetworkListDelegate.updateBlackList(0);
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void clearDomainNameBlackList() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.clearDomainNameBlackList();
            if (this.mNetworkListDelegate != null) {
                Log.d(TAG, "domain clear BlackList!");
                try {
                    this.mNetworkListDelegate.updateBlackList(0);
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getDomainNameBlackList() {
        return this.mVivoCustomDbBridge.getDomainNameBlackList();
    }

    public void setDomainNameWhiteList(List<String> urls, boolean isWhiteList) {
        if (urls == null) {
            throw new IllegalArgumentException("IllegalArgumentException:setDomainNameWhiteList urls is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setDomainNameWhiteList(urls, isWhiteList);
            if (this.mNetworkListDelegate != null) {
                Log.d(TAG, "domain updateWhiteList!");
                try {
                    this.mNetworkListDelegate.updateWhiteList(0);
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void clearDomainNameWhiteList() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.clearDomainNameWhiteList();
            if (this.mNetworkListDelegate != null) {
                Log.d(TAG, "domain clear WhiteList!");
                try {
                    this.mNetworkListDelegate.updateWhiteList(0);
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getDomainNameWhiteList() {
        return this.mVivoCustomDbBridge.getDomainNameWhiteList();
    }

    public void setIpAddrPattern(int pattern) {
        if (pattern < 0 || pattern > 2) {
            throw new IllegalArgumentException("IllegalArgumentException:setIpAddrPattern pattern is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_network_ip_restrict_pattern", pattern);
            if (this.mNetworkListDelegate != null) {
                Log.d(TAG, "setIpAddrPattern!");
                try {
                    this.mNetworkListDelegate.updateBlackList(1);
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getIpAddrPattern() {
        long callingId = Binder.clearCallingIdentity();
        int state = 0;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_network_ip_restrict_pattern", 0);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void setIpAddrBlackList(List<String> ips, boolean isBlackList) {
        if (ips == null) {
            throw new IllegalArgumentException("IllegalArgumentException:setIpAddrBlackList ips is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setIpAddrBlackList(ips, isBlackList);
            if (this.mNetworkListDelegate != null) {
                Log.d(TAG, "ip addr updateBlackList!");
                try {
                    this.mNetworkListDelegate.updateBlackList(1);
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void clearIpAddrBlackList() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.clearIpAddrBlackList();
            if (this.mNetworkListDelegate != null) {
                Log.d(TAG, "ip addr clear BlackList!");
                try {
                    this.mNetworkListDelegate.updateBlackList(1);
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getIpAddrBlackList() {
        return this.mVivoCustomDbBridge.getIpAddrBlackList();
    }

    public void setIpAddrWhiteList(List<String> ips, boolean isWhiteList) {
        if (ips == null) {
            throw new IllegalArgumentException("IllegalArgumentException:setIpAddrWhiteList ips is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setIpAddrWhiteList(ips, isWhiteList);
            if (this.mNetworkListDelegate != null) {
                Log.d(TAG, "ip addr updateWhiteList!");
                try {
                    this.mNetworkListDelegate.updateWhiteList(1);
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void clearIpAddrWhiteList() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.clearIpAddrWhiteList();
            if (this.mNetworkListDelegate != null) {
                Log.d(TAG, "ip addr clear WhiteList!");
                try {
                    this.mNetworkListDelegate.updateWhiteList(1);
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getIpAddrWhiteList() {
        return this.mVivoCustomDbBridge.getIpAddrWhiteList();
    }

    public void registerNetworkListChangeCallback(INetworkListDelegate callback) {
        Log.d(TAG, "register NetworkListChangeListener ");
        this.mNetworkListDelegate = callback;
    }

    public void setTelephonyPhoneState(int simId, int callinState, int calloutState) {
        if (simId < 0 || simId > 2 || callinState < 0 || callinState > 1 || calloutState < 0 || calloutState > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setTelephonyPhoneState input is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_network_phone_blocksim", simId);
            Secure.putInt(this.mContentResolver, "ct_network_phone_blockcallin", callinState);
            Secure.putInt(this.mContentResolver, "ct_network_phone_blockcallout", calloutState);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public String getTelephonyPhoneState() {
        long callingId = Binder.clearCallingIdentity();
        String simId = "";
        String callinState = "";
        String calloutState = "";
        try {
            simId = String.valueOf(Secure.getInt(this.mContentResolver, "ct_network_phone_blocksim", 0));
            callinState = String.valueOf(Secure.getInt(this.mContentResolver, "ct_network_phone_blockcallin", 1));
            calloutState = String.valueOf(Secure.getInt(this.mContentResolver, "ct_network_phone_blockcallout", 1));
            return simId + ":" + callinState + ":" + calloutState;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void setPhoneRestrictPattern(int pattern) {
        if (pattern < 0 || pattern > 2) {
            throw new IllegalArgumentException("IllegalArgumentException:setPhoneRestrictPattern pattern is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_phone_restrict_pattern", pattern);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getPhoneRestrictPattern() {
        long callingId = Binder.clearCallingIdentity();
        int state = 0;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_phone_restrict_pattern", 0);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addPhoneBlackList(List<String> numbers) {
        if (numbers == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addPhoneBlackList numbers is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setPhoneBlackList(numbers, true, 0, 0);
            if (this.mPhoneListListener != null) {
                Log.d(TAG, "addPhoneBlackList updateBlackList!");
                try {
                    this.mPhoneListListener.updateBlackList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addPhoneBlackListInfo(String number, int inOutMode, int simID) {
        if (TextUtils.isEmpty(number) || inOutMode < 0 || inOutMode > 2 || simID < 0 || simID > 2) {
            throw new IllegalArgumentException("IllegalArgumentException:addPhoneBlackListInfo input is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            List<String> numbers = new ArrayList();
            numbers.add(number);
            this.mVivoCustomDbBridge.setPhoneBlackList(numbers, true, inOutMode, simID);
            if (this.mPhoneListListener != null) {
                Log.d(TAG, "addPhoneBlackListInfo updateBlackList!");
                try {
                    this.mPhoneListListener.updateBlackList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addPhoneBlackListInfo(List<String> numbers, int inOutMode, int simID) {
        if (numbers == null || inOutMode < 0 || inOutMode > 2 || simID < 0 || simID > 2) {
            throw new IllegalArgumentException("IllegalArgumentException:addPhoneBlackListInfo input is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setPhoneBlackList(numbers, true, inOutMode, simID);
            if (this.mPhoneListListener != null) {
                Log.d(TAG, "addPhoneBlackListInfo updateBlackList!");
                try {
                    this.mPhoneListListener.updateBlackList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void deletePhoneBlackList(List<String> numbers) {
        if (numbers == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deletePhoneBlackList numbers is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setPhoneBlackList(numbers, false, 0, 0);
            if (this.mPhoneListListener != null) {
                Log.d(TAG, "deletePhoneBlackList updateBlackList!");
                try {
                    this.mPhoneListListener.updateBlackList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getPhoneBlackList() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getPhoneBlackList();
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getPhoneBlackListInfo() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getPhoneBlackListInfo();
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addPhoneWhiteList(List<String> numbers) {
        if (numbers == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addPhoneWhiteList numbers is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setPhoneWhiteList(numbers, true, 0, 0);
            if (this.mPhoneListListener != null) {
                Log.d(TAG, "addPhoneWhiteList updateWhiteList!");
                try {
                    this.mPhoneListListener.updateWhiteList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addPhoneWhiteListInfo(String number, int inOutMode, int simID) {
        if (TextUtils.isEmpty(number) || inOutMode < 0 || inOutMode > 2 || simID < 0 || simID > 2) {
            throw new IllegalArgumentException("IllegalArgumentException:addPhoneWhiteListInfo input is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            List<String> numbers = new ArrayList();
            numbers.add(number);
            this.mVivoCustomDbBridge.setPhoneWhiteList(numbers, true, inOutMode, simID);
            if (this.mPhoneListListener != null) {
                Log.d(TAG, "addPhoneWhiteList updateWhiteList!");
                try {
                    this.mPhoneListListener.updateWhiteList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addPhoneWhiteListInfo(List<String> numbers, int inOutMode, int simID) {
        if (numbers == null || inOutMode < 0 || inOutMode > 2 || simID < 0 || simID > 2) {
            throw new IllegalArgumentException("IllegalArgumentException:addPhoneWhiteListInfo input is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setPhoneWhiteList(numbers, true, inOutMode, simID);
            if (this.mPhoneListListener != null) {
                Log.d(TAG, "addPhoneWhiteList updateWhiteList!");
                try {
                    this.mPhoneListListener.updateWhiteList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void deletePhoneWhiteList(List<String> numbers) {
        if (numbers == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deletePhoneWhiteList numbers is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setPhoneWhiteList(numbers, false, 0, 0);
            if (this.mPhoneListListener != null) {
                Log.d(TAG, "deletePhoneWhiteList updateWhiteList!");
                try {
                    this.mPhoneListListener.updateWhiteList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getPhoneWhiteList() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getPhoneWhiteList();
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getPhoneWhiteListInfo() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getPhoneWhiteListInfo();
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void registerPhoneListChangeCallback(IPhoneListListener callback) {
        Log.d(TAG, "register PhoneListChangeCallback ");
        this.mPhoneListListener = callback;
    }

    public void setTelephonyMmsState(int simId, int receiveState, int sendState) {
        if (simId < 0 || simId > 2 || receiveState < 0 || receiveState > 1 || sendState < 0 || sendState > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setTelephonyMmsState input is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_network_mms_blocksim", simId);
            Secure.putInt(this.mContentResolver, "ct_network_mms_blockreceive", receiveState);
            Secure.putInt(this.mContentResolver, "ct_network_mms_blocksend", sendState);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public String getTelephonyMmsState() {
        long callingId = Binder.clearCallingIdentity();
        String simId = "";
        String receiveState = "";
        String sendState = "";
        try {
            simId = String.valueOf(Secure.getInt(this.mContentResolver, "ct_network_mms_blocksim", 0));
            receiveState = String.valueOf(Secure.getInt(this.mContentResolver, "ct_network_mms_blockreceive", 1));
            sendState = String.valueOf(Secure.getInt(this.mContentResolver, "ct_network_mms_blocksend", 1));
            return simId + ":" + receiveState + ":" + sendState;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void setTelephonySmsState(int simId, int receiveState, int sendState) {
        if (simId < 0 || simId > 2 || receiveState < 0 || receiveState > 1 || sendState < 0 || sendState > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setTelephonySmsState input is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_network_sms_blocksim", simId);
            Secure.putInt(this.mContentResolver, "ct_network_sms_blockreceive", receiveState);
            Secure.putInt(this.mContentResolver, "ct_network_sms_blocksend", sendState);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public String getTelephonySmsState() {
        long callingId = Binder.clearCallingIdentity();
        String simId = "";
        String receiveState = "";
        String sendState = "";
        try {
            simId = String.valueOf(Secure.getInt(this.mContentResolver, "ct_network_sms_blocksim", 0));
            receiveState = String.valueOf(Secure.getInt(this.mContentResolver, "ct_network_sms_blockreceive", 1));
            sendState = String.valueOf(Secure.getInt(this.mContentResolver, "ct_network_sms_blocksend", 1));
            return simId + ":" + receiveState + ":" + sendState;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void setSmsRestrictPattern(int pattern) {
        if (pattern < 0 || pattern > 2) {
            throw new IllegalArgumentException("IllegalArgumentException:setSmsRestrictPattern pattern is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_sms_restrict_pattern", pattern);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getSmsRestrictPattern() {
        long callingId = Binder.clearCallingIdentity();
        int state = 0;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_sms_restrict_pattern", 0);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addSmsBlackList(List<String> numbers) {
        if (numbers == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addSmsBlackList numbers is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setSmsBlackList(numbers, true);
            if (this.mSmsListListener != null) {
                Log.d(TAG, "addSmsBlackList updateBlackList!");
                try {
                    this.mSmsListListener.updateBlackList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void deleteSmsBlackList(List<String> numbers) {
        if (numbers == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deleteSmsBlackList numbers is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setSmsBlackList(numbers, false);
            if (this.mSmsListListener != null) {
                Log.d(TAG, "deleteSmsBlackList updateBlackList!");
                try {
                    this.mSmsListListener.updateBlackList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getSmsBlackList() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getSmsBlackList();
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addSmsWhiteList(List<String> numbers) {
        if (numbers == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addSmsWhiteList numbers is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setSmsWhiteList(numbers, true);
            if (this.mSmsListListener != null) {
                Log.d(TAG, "addSmsWhiteList updateWhiteList!");
                try {
                    this.mSmsListListener.updateWhiteList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void deleteSmsWhiteList(List<String> numbers) {
        if (numbers == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deleteSmsWhiteList numbers is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setSmsWhiteList(numbers, false);
            if (this.mSmsListListener != null) {
                Log.d(TAG, "deleteSmsWhiteList updateWhiteList!");
                try {
                    this.mSmsListListener.updateWhiteList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getSmsWhiteList() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getSmsWhiteList();
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void registerSmsListChangeCallback(ISmsListListener callback) {
        Log.d(TAG, "register SmsListChangeCallback ");
        this.mSmsListListener = callback;
    }

    public void setTelephonyDataState(int simId, int dataState) {
        if (simId < 0 || simId > 2 || dataState < 0 || dataState > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setTelephonyDataState input is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_network_data_blocksim", simId);
            Secure.putInt(this.mContentResolver, "ct_network_data_blockdata", dataState);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public String getTelephonyDataState() {
        long callingId = Binder.clearCallingIdentity();
        String simId = "";
        String dataState = "";
        try {
            simId = String.valueOf(Secure.getInt(this.mContentResolver, "ct_network_data_blocksim", 0));
            dataState = String.valueOf(Secure.getInt(this.mContentResolver, "ct_network_data_blockdata", 1));
            return simId + ":" + dataState;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void setWlanRestrictPattern(int pattern) {
        if (pattern < 0 || pattern > 2) {
            throw new IllegalArgumentException("IllegalArgumentException:setWlanRestrictPattern pattern is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_wlan_restrict_pattern", pattern);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getWlanRestrictPattern() {
        long callingId = Binder.clearCallingIdentity();
        int state = 0;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_wlan_restrict_pattern", 0);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addWlanBlackList(List<String> iccids) {
        if (iccids == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addWlanBlackList ssids is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setWlanBlackList(iccids, true);
            if (this.mWlanListListener != null) {
                Log.d(TAG, "add WlanBlackList updateBlackList!");
                try {
                    this.mWlanListListener.updateBlackList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void deleteWlanBlackList(List<String> iccids) {
        if (iccids == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deleteWlanBlackList ssids is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setWlanBlackList(iccids, false);
            if (this.mWlanListListener != null) {
                Log.d(TAG, "delete WlanBlackList updateBlackList!");
                try {
                    this.mWlanListListener.updateBlackList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getWlanBlackList() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getWlanBlackList();
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addWlanWhiteList(List<String> iccids) {
        if (iccids == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addWlanWhiteList ssids is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setWlanWhiteList(iccids, true);
            if (this.mWlanListListener != null) {
                Log.d(TAG, "add WlanWhiteList updateWhiteList!");
                try {
                    this.mWlanListListener.updateWhiteList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void deleteWlanWhiteList(List<String> iccids) {
        if (iccids == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deleteWlanWhiteList ssids is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setWlanWhiteList(iccids, false);
            if (this.mWlanListListener != null) {
                Log.d(TAG, "delete WlanWhiteList updateWhiteList!");
                try {
                    this.mWlanListListener.updateWhiteList();
                } catch (Exception e) {
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getWlanWhiteList() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getWlanWhiteList();
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void registerWlanListChangeCallback(IWlanListListener callback) {
        Log.d(TAG, "register WlanListChangeCallback ");
        this.mWlanListListener = callback;
    }

    public void setBluetoothRestrictPattern(int pattern) {
        if (pattern < 0 || pattern > 2) {
            throw new IllegalArgumentException("IllegalArgumentException:setBluetoothRestrictPattern pattern is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, Utils.CT_BLUETOOTH_RESTRICT_PATTERN, pattern);
            bluetoothStateChange();
            Log.d(TAG, "setBluetoothRestrictPattern : " + pattern);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getBluetoothRestrictPattern() {
        long callingId = Binder.clearCallingIdentity();
        int state = 0;
        try {
            state = Secure.getInt(this.mContentResolver, Utils.CT_BLUETOOTH_RESTRICT_PATTERN, 0);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addBluetoothBlackList(List<String> macs) {
        if (macs == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addBluetoothBlackList macs is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setBluetoothBlackList(macs, true);
            bluetoothStateChange();
            Log.d(TAG, "addBluetoothBlackList : " + macs);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void deleteBluetoothBlackList(List<String> macs) {
        if (macs == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deleteBluetoothBlackList macs is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setBluetoothBlackList(macs, false);
            bluetoothStateChange();
            Log.d(TAG, "deleteBluetoothBlackList : " + macs);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getBluetoothBlackList() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getBluetoothBlackList();
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addBluetoothWhiteList(List<String> macs) {
        if (macs == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addBluetoothWhiteList macs is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setBluetoothWhiteList(macs, true);
            bluetoothStateChange();
            Log.d(TAG, "addBluetoothWhiteList : " + macs);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void deleteBluetoothWhiteList(List<String> macs) {
        if (macs == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deleteBluetoothWhiteList macs is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setBluetoothWhiteList(macs, false);
            bluetoothStateChange();
            Log.d(TAG, "deleteBluetoothWhiteList : " + macs);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getBluetoothWhiteList() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getBluetoothWhiteList();
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    private void bluetoothStateChange() {
        Intent intent = new Intent("vivo.custom.action.BLUETOOTH_RESTRICT_STATE_CHANGE");
        intent.addFlags(536870912);
        this.mContext.sendBroadcast(intent);
    }

    public boolean setDevicePolicyManager(ComponentName componentName, boolean isActive) {
        if (componentName == null) {
            throw new IllegalArgumentException("IllegalArgumentException:setDeviceManagerEnable componentName is null!");
        }
        checkUp();
        Log.d(TAG, "setDevicePolicyManager : isActive = " + isActive);
        boolean success = true;
        long callingId = Binder.clearCallingIdentity();
        try {
            List<ResolveInfo> allDeviceManagers = this.mPackageManager.queryBroadcastReceivers(new Intent("android.app.action.DEVICE_ADMIN_ENABLED"), 32896, 0);
            if (allDeviceManagers == null) {
                allDeviceManagers = Collections.emptyList();
            }
            int n = allDeviceManagers.size();
            Log.d(TAG, "setDeviceManagerEnable allDeviceManagers size = " + n);
            for (int i = 0; i < n; i++) {
                ResolveInfo resolveInfo = (ResolveInfo) allDeviceManagers.get(i);
                if (resolveInfo.activityInfo != null) {
                    Log.d(TAG, "setDeviceManagerEnable allDeviceManagers packageName = " + resolveInfo.activityInfo.packageName + ", name" + resolveInfo.activityInfo.name);
                }
                if (resolveInfo.activityInfo != null && componentName.getPackageName().equals(resolveInfo.activityInfo.packageName) && resolveInfo.activityInfo.name != null && resolveInfo.activityInfo.name.contains(componentName.getClassName())) {
                    ComponentName riComponentName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                    if (!isActive) {
                        Log.d(TAG, "setDeviceManagerEnable remove active :" + riComponentName.toString());
                        this.mDPM.removeActiveAdmin(riComponentName);
                    } else if (this.mDPM.isAdminActive(riComponentName)) {
                        Log.d(TAG, "Admin has already add : " + riComponentName.toString());
                    } else {
                        Log.d(TAG, "setDeviceManagerEnable active :" + riComponentName.toString());
                        this.mDPM.setActiveAdmin(riComponentName, true);
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "setDeviceManagerEnable failed :", e);
            success = false;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
        return success;
    }

    public boolean isDevicePolicyManagerEnable(ComponentName componentName) {
        if (componentName == null) {
            throw new IllegalArgumentException("IllegalArgumentException:isDevicePolicyManagerEnable componentName is null!");
        }
        checkUp();
        Log.d(TAG, "getDeviceManagerEnable componentName = " + componentName.getPackageName());
        long callingId = Binder.clearCallingIdentity();
        try {
            List<ComponentName> activeAdminsList = this.mDPM.getActiveAdmins();
            if (activeAdminsList == null) {
                activeAdminsList = Collections.emptyList();
            }
            int n = activeAdminsList.size();
            Log.d(TAG, "getDeviceManagerEnable activeAdminsList size = " + n);
            for (int i = 0; i < n; i++) {
                if (componentName.getPackageName().equals(((ComponentName) activeAdminsList.get(i)).getPackageName())) {
                    return true;
                }
            }
            Binder.restoreCallingIdentity(callingId);
            return false;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setAccessibilityServcie(ComponentName componentName, boolean isActive) {
        if (componentName == null) {
            throw new IllegalArgumentException("IllegalArgumentException:setAccessibilityServcie componentName is null!");
        }
        checkUp();
        Log.d(TAG, "setAccessibilityServcie admin = " + componentName.getPackageName() + " isActive = " + isActive);
        long callingId = Binder.clearCallingIdentity();
        try {
            String emmAccessService = "";
            List<AccessibilityServiceInfo> installedServiceInfos = AccessibilityManager.getInstance(this.mContext).getInstalledAccessibilityServiceList();
            Log.d(TAG, "the list's size is:" + installedServiceInfos.size());
            if (installedServiceInfos != null) {
                for (AccessibilityServiceInfo as : installedServiceInfos) {
                    ResolveInfo resolveInfo = as.getResolveInfo();
                    if (!(resolveInfo == null || resolveInfo.serviceInfo == null)) {
                        Log.d(TAG, "query packageName is:" + resolveInfo.serviceInfo.packageName + ", name = " + resolveInfo.serviceInfo.name + ",getClassName = " + componentName.getClassName());
                    }
                    if (resolveInfo != null && resolveInfo.serviceInfo != null && componentName.getPackageName().equals(resolveInfo.serviceInfo.packageName) && resolveInfo.serviceInfo.name != null && resolveInfo.serviceInfo.name.contains(componentName.getClassName())) {
                        emmAccessService = resolveInfo.serviceInfo.packageName + "/" + resolveInfo.serviceInfo.name;
                        break;
                    }
                }
            }
            boolean shouldUpdate = false;
            StringBuffer sb = new StringBuffer();
            String enabledAccServices = Secure.getString(this.mContentResolver, "enabled_accessibility_services");
            Log.d(TAG, "enableAccessibilityService enabledAccServices = " + enabledAccServices + ", available service = " + emmAccessService);
            if (isActive) {
                if (TextUtils.isEmpty(enabledAccServices)) {
                    sb = new StringBuffer(emmAccessService);
                    shouldUpdate = true;
                } else if (!enabledAccServices.contains(emmAccessService)) {
                    sb = new StringBuffer(enabledAccServices);
                    sb.append(":" + emmAccessService);
                    shouldUpdate = true;
                }
            } else if (!TextUtils.isEmpty(enabledAccServices)) {
                String[] services = enabledAccServices.split(":");
                Log.d(TAG, "enableAccessibilityService services length = " + services.length);
                sb = new StringBuffer();
                for (int i = 0; i < services.length; i++) {
                    if (emmAccessService.equals(services[i])) {
                        shouldUpdate = true;
                    } else {
                        if (i > 0) {
                            sb.append(":");
                        }
                        sb.append(services[i]);
                    }
                }
            }
            Log.d(TAG, "enableAccessibilityService shouldUpdate = " + shouldUpdate + ",sb = " + sb.toString());
            if (shouldUpdate) {
                Secure.putString(this.mContentResolver, "enabled_accessibility_services", sb.toString());
            }
            if (isActive) {
                Log.d(TAG, "enableAccessibilityService setEnable");
                Secure.putString(this.mContentResolver, "accessibility_enabled", Utils.FLAG_LSJY);
            }
            Binder.restoreCallingIdentity(callingId);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean isAccessibilityServcieEnable(ComponentName componentName) {
        if (componentName == null) {
            throw new IllegalArgumentException("IllegalArgumentException:isAccessibilityServcieEnable componentName is null!");
        }
        checkUp();
        Log.d(TAG, "IsAccessibilityServcieEnable componentName = " + componentName.getPackageName());
        long callingId = Binder.clearCallingIdentity();
        try {
            boolean res;
            String emmAccessService = "";
            List<AccessibilityServiceInfo> installedServiceInfos = AccessibilityManager.getInstance(this.mContext).getInstalledAccessibilityServiceList();
            Log.d(TAG, "the list's size is:" + installedServiceInfos.size());
            if (installedServiceInfos != null) {
                for (AccessibilityServiceInfo as : installedServiceInfos) {
                    ResolveInfo resolveInfo = as.getResolveInfo();
                    if (resolveInfo != null && resolveInfo.serviceInfo != null && componentName.getPackageName().equals(resolveInfo.serviceInfo.packageName) && resolveInfo.serviceInfo.name != null && resolveInfo.serviceInfo.name.contains(componentName.getClassName())) {
                        emmAccessService = resolveInfo.serviceInfo.packageName + "/" + resolveInfo.serviceInfo.name;
                        break;
                    }
                }
            }
            String enabledAccServices = Secure.getString(this.mContentResolver, "enabled_accessibility_services");
            Log.d(TAG, "IsAccessibilityServcieEnable enabledAccServices = " + enabledAccServices + ", available service = " + emmAccessService);
            if (TextUtils.isEmpty(enabledAccServices) || !enabledAccServices.contains(emmAccessService)) {
                res = false;
            } else {
                res = true;
            }
            Binder.restoreCallingIdentity(callingId);
            return res;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean killProcess(String procName) {
        if (TextUtils.isEmpty(procName)) {
            throw new IllegalArgumentException("IllegalArgumentException:killProcess process name is null!");
        }
        checkUp();
        Log.d(TAG, "killProcess procName = " + procName);
        String[] ps = procName.split(",");
        long callingId = Binder.clearCallingIdentity();
        boolean result = true;
        try {
            for (RunningAppProcessInfo info : this.mActivityManager.getRunningAppProcesses()) {
                for (String equals : ps) {
                    if (equals.equals(info.processName)) {
                        Process.killProcess(info.pid);
                        Log.d(TAG, "kill " + info.processName + " pid = " + info.pid);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
        return result;
    }

    public boolean forceStopPackage(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("IllegalArgumentException:forceStopPackage package name is null!");
        }
        checkUp();
        Log.d(TAG, "forceStopPackage packageName = " + packageName);
        String[] ps = packageName.split(",");
        long callingId = Binder.clearCallingIdentity();
        int i = 0;
        while (i < ps.length) {
            try {
                this.mActivityManager.forceStopPackage(ps[i]);
                i++;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
        return true;
    }

    public boolean clearAppData(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("IllegalArgumentException:clearAppData package name is null!");
        }
        boolean result;
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            result = this.mActivityManager.clearApplicationUserData(packageName, new ClearUserDataObserver());
            Binder.restoreCallingIdentity(callingId);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
        Log.d(TAG, "clearAppData packageName = " + packageName + ", result = " + result);
        return result;
    }

    public void endCall() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            ITelephony iTelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
            int[] subIds = new SubscriptionManager(this.mContext).getActiveSubscriptionIdList();
            if (!(iTelephony == null || subIds == null)) {
                for (int i = 0; i < subIds.length; i++) {
                    Log.d(TAG, "get in end call and the subid is:" + subIds[i]);
                    iTelephony.endCallForSubscriber(subIds[i]);
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Exception e) {
            Log.d(TAG, "remote exception on call endCall.");
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
    }

    public Bundle captureScreen() {
        checkUp();
        Bundle mBundle = new Bundle();
        long callingId = Binder.clearCallingIdentity();
        try {
            ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealMetrics(new DisplayMetrics());
            float[] dims = new float[]{(float) displayMetrics.widthPixels, (float) displayMetrics.heightPixels};
            Bitmap screenBitmap = SurfaceControl.screenshot((int) dims[0], (int) dims[1]);
            screenBitmap.setHasAlpha(false);
            screenBitmap.prepareToDraw();
            Log.d(TAG, "bitmap's size is:" + screenBitmap.getAllocationByteCount());
            mBundle.putParcelable("CAPTURE_SCREEN", screenBitmap);
            return mBundle;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setDefaultLauncher(ComponentName componentName, int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setDefaultLauncher input is illegal!");
        }
        checkUp();
        String packageName = null;
        String className = null;
        if (componentName != null) {
            packageName = componentName.getPackageName();
            className = componentName.getClassName();
        }
        Log.d(TAG, "setDefaultLauncher packageName = " + packageName + ", className = " + className + ", state = " + state);
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putString(this.mContentResolver, "ct_preferred_launcher_activity", className);
            Secure.putString(this.mContentResolver, "ct_preferred_launcher_pkg", packageName);
            Secure.putInt(this.mContentResolver, "ct_oem_launcher_only", state);
        } catch (Exception e) {
            Log.d(TAG, "setDefaultLauncher exception " + e);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
        return setDefaultApp(packageName, className, state, 1);
    }

    public boolean setDefaultBrowser(ComponentName componentName, int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setDefaultBrowser input is illegal!");
        }
        checkUp();
        String packageName = null;
        String className = null;
        if (componentName != null) {
            packageName = componentName.getPackageName();
            className = componentName.getClassName();
        }
        Log.d(TAG, "setDefaultBrowser packageName = " + packageName + ", className = " + className + ", state = " + state);
        return setDefaultApp(packageName, className, state, 2);
    }

    public boolean setDefaultEmail(ComponentName componentName, int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setDefaultEmail input is illegal!");
        }
        checkUp();
        String packageName = null;
        String className = null;
        if (componentName != null) {
            packageName = componentName.getPackageName();
            className = componentName.getClassName();
        }
        Log.d(TAG, "setDefaultEmail packageName = " + packageName + ", className = " + className + ", state = " + state);
        return setDefaultApp(packageName, className, state, 10);
    }

    public boolean setDefaultMessage(ComponentName componentName, int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setDefaultMessage input is illegal!");
        }
        checkUp();
        String packageName = null;
        String className = null;
        if (componentName != null) {
            packageName = componentName.getPackageName();
            className = componentName.getClassName();
        }
        Log.d(TAG, "setDefaultMessage packageName = " + packageName + ", className = " + className + ", state = " + state);
        return setDefaultApp(packageName, className, state, 6);
    }

    private boolean setDefaultApp(String packageName, String className, int state, int type) {
        long callingId = Binder.clearCallingIdentity();
        try {
            int st = Secure.getInt(this.mContentResolver, "ct_dafaultapp_restrict_state", 0);
            int flag = 1 << type;
            switch (state) {
                case 0:
                    st &= ~flag;
                    break;
                case 1:
                    break;
                default:
                    throw new IllegalArgumentException("setDefaultApp fail : the para is not valid state=" + state);
            }
            Secure.putInt(this.mContentResolver, "ct_dafaultapp_restrict_state", st | flag);
            Intent intent = new Intent();
            intent.setAction("vivo.custom.intent.action.DEFAULT_APP_RESTRICTION");
            intent.putExtra("type", type);
            intent.putExtra("value", state);
            intent.putExtra("packageName", packageName);
            intent.putExtra("activityName", className);
            this.mContext.sendBroadcast(intent, "com.vivo.custom.permission.SEND_CUSTOM_BROADCAST");
            Log.d(TAG, "setDefaultApp:packageName = " + packageName + ", className = " + className + " state = " + state);
            return true;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setTelephonySlotState(int state) {
        if (state < 0 || state > 3) {
            throw new IllegalArgumentException("IllegalArgumentException:setTelephonySlotState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
            case 2:
            case 3:
                Secure.putInt(this.mContentResolver, "ct_network_sim_block", state);
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setTelephonySlotState state = " + state);
                return true;
            default:
                try {
                    throw new IllegalArgumentException("setTelephonySlotState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public int getTelephonySlotState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 0;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_network_sim_block", 0);
            Log.d(TAG, "getTelephonySlotState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setDataNetworkState(int state) {
        if (state < 0 || state > 4) {
            throw new IllegalArgumentException("IllegalArgumentException:setDataNetworkState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
            case 4:
                Secure.putInt(this.mContentResolver, "ct_network_data", state);
                break;
            case 2:
                this.tm.setDataEnabled(false);
                break;
            case 3:
                this.tm.setDataEnabled(true);
                break;
            default:
                try {
                    throw new IllegalArgumentException("setDataNetworkState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
        Log.d(TAG, "setDataNetworkState state = " + state);
        return true;
    }

    public int getDataNetworkState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_network_data", 1);
            Log.d(TAG, "getDataNetworkState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setFlightModeState(int state) {
        if (state < 0 || state > 4) {
            throw new IllegalArgumentException("IllegalArgumentException:setFlightModeState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
                Secure.putInt(this.mContentResolver, "ct_network_flightmode", state);
                break;
            case 4:
                Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", 3);
                Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
                intent.putExtra("state", 3);
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                Secure.putInt(this.mContentResolver, "ct_network_flightmode", state);
                break;
            default:
                try {
                    throw new IllegalArgumentException("setFlightModeState fail : the para is not valid state=" + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
        Log.d(TAG, "setFlightModeState state = " + state);
        return true;
    }

    public int getFlightModeState() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_network_flightmode", 1);
            Log.d(TAG, "getFlightModeState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setStatusBarState(boolean enable) {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        if (enable) {
            this.statusBarManager.disable(0);
            this.statusBarManager.disable2(0);
        } else {
            try {
                this.statusBarManager.disable(327680);
                this.statusBarManager.disable2(1);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(callingId);
            }
        }
        Log.d(TAG, "setStatusBarState enable = " + enable);
        SystemProperties.set("persist.sys.gn.sb_enable", enable ? Utils.FLAG_LSJY : "0");
        Binder.restoreCallingIdentity(callingId);
        return true;
    }

    public boolean getStatusBarState() {
        checkUp();
        return Utils.FLAG_LSJY.equals(SystemProperties.get("persist.sys.gn.sb_enable", Utils.FLAG_LSJY));
    }

    private void disableStatusBar() {
        boolean enable = Utils.FLAG_LSJY.equals(SystemProperties.get("persist.sys.gn.sb_enable", Utils.FLAG_LSJY));
        Log.d(TAG, "disableStatusBar enable = " + enable);
        if (enable) {
            this.statusBarManager.disable(0);
            this.statusBarManager.disable2(0);
            return;
        }
        this.statusBarManager.disable(327680);
        this.statusBarManager.disable2(1);
    }

    public List<String> getPhoneNumbers() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        String[] arr = new String[2];
        try {
            Class<?> telephony;
            int[] subIds = SubscriptionManager.getSubId(0);
            if (subIds != null && subIds.length > 0) {
                Log.d(TAG, "getPhoneNumbers subId1 = " + subIds[0]);
                telephony = Class.forName("android.telephony.TelephonyManager");
                if (VERSION.SDK_INT <= 23) {
                    arr[0] = (String) telephony.getMethod("getLine1NumberForSubscriber", new Class[]{Integer.TYPE}).invoke(this.tm, new Object[]{Integer.valueOf(subIds[0])});
                } else {
                    arr[0] = (String) telephony.getMethod("getLine1Number", new Class[]{Integer.TYPE}).invoke(this.tm, new Object[]{Integer.valueOf(subIds[0])});
                }
            }
            subIds = SubscriptionManager.getSubId(1);
            if (subIds != null && subIds.length > 0) {
                Log.d(TAG, "getPhoneNumbers subId2 = " + subIds[0]);
                telephony = Class.forName("android.telephony.TelephonyManager");
                if (VERSION.SDK_INT <= 23) {
                    arr[1] = (String) telephony.getMethod("getLine1NumberForSubscriber", new Class[]{Integer.TYPE}).invoke(this.tm, new Object[]{Integer.valueOf(subIds[0])});
                } else {
                    arr[1] = (String) telephony.getMethod("getLine1Number", new Class[]{Integer.TYPE}).invoke(this.tm, new Object[]{Integer.valueOf(subIds[0])});
                }
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Exception e) {
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
        Log.d(TAG, "getPhoneNumbers arr = " + arr[0] + " " + arr[1]);
        return Arrays.asList(arr);
    }

    public List<String> getPhoneIccids() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        String[] arr = new String[2];
        try {
            int[] subIds = SubscriptionManager.getSubId(0);
            if (subIds != null && subIds.length > 0) {
                Log.d(TAG, "getPhoneIccids subId1 = " + subIds[0]);
                arr[0] = this.tm.getSimSerialNumber(subIds[0]);
            }
            subIds = SubscriptionManager.getSubId(1);
            if (subIds != null && subIds.length > 0) {
                Log.d(TAG, "getPhoneIccids subId2 = " + subIds[0]);
                arr[1] = this.tm.getSimSerialNumber(subIds[0]);
            }
            Binder.restoreCallingIdentity(callingId);
            Log.d(TAG, "getPhoneIccids arr = " + arr[0] + " " + arr[1]);
            return Arrays.asList(arr);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getPhoneImeis() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        String[] arr = new String[2];
        try {
            arr[0] = this.tm.getImei(0);
            arr[1] = this.tm.getImei(1);
            Log.d(TAG, "getPhoneIMEIs arr = " + arr[0] + " " + arr[1]);
            return Arrays.asList(arr);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void setHomeKeyEventState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setHomeKeyEventState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            SystemProperties.set("persist.sys.gn.home_enable", Integer.toString(state));
            Log.d(TAG, "home key event state : " + state);
        } catch (Exception e) {
            Log.d(TAG, "setBackKeyEventState exception occur! " + e);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getHomeKeyEventState() {
        checkUp();
        int state = SystemProperties.getInt("persist.sys.gn.home_enable", 1);
        Log.d(TAG, "home key event state : " + state);
        return state;
    }

    public void setMenuKeyEventState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setMenuKeyEventState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            SystemProperties.set("persist.sys.gn.menu_enable", Integer.toString(state));
            Log.d(TAG, "menu key event state : " + state);
        } catch (Exception e) {
            Log.d(TAG, "setBackKeyEventState exception occur! " + e);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getMenuKeyEventState() {
        checkUp();
        int state = SystemProperties.getInt("persist.sys.gn.menu_enable", 1);
        Log.d(TAG, "menu key event state : " + state);
        return state;
    }

    public void setBackKeyEventState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setBackKeyEventState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            SystemProperties.set("persist.sys.gn.back_enable", Integer.toString(state));
            Log.d(TAG, "back key event state : " + state);
        } catch (Exception e) {
            Log.d(TAG, "setBackKeyEventState exception occur! " + e);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getBackKeyEventState() {
        checkUp();
        int state = SystemProperties.getInt("persist.sys.gn.back_enable", 1);
        Log.d(TAG, "back key event state : " + state);
        return state;
    }

    public void setSafeModeState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setSafeModeState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            SystemProperties.set("persist.sys.gn.sm_enable", Integer.toString(state));
            Log.d(TAG, "safemode state : " + state);
        } catch (Exception e) {
            Log.d(TAG, "setSafeModeState exception occur! " + e);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getSafeModeState() {
        checkUp();
        int state = SystemProperties.getInt("persist.sys.gn.sm_enable", 1);
        Log.d(TAG, "safemode state : " + state);
        return state;
    }

    public long getTrafficBytes(int mode, String packageName) {
        if (TextUtils.isEmpty(packageName) || mode < 0 || mode > 2) {
            throw new IllegalArgumentException("IllegalArgumentException:getTrafficBytes input is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        long wifi_bytes = 0;
        long sim1_data_bytes = 0;
        long sim2_data_bytes = 0;
        long currentTime = System.currentTimeMillis();
        try {
            int uid;
            Class<?> pm = Class.forName("android.content.pm.IPackageManager");
            Class<?> cls;
            if (VERSION.SDK_INT <= 23) {
                cls = pm;
                uid = ((Integer) cls.getMethod("getPackageUid", new Class[]{String.class, Integer.TYPE}).invoke(this.mPms, new Object[]{packageName, Integer.valueOf(0)})).intValue();
            } else {
                cls = pm;
                uid = ((Integer) cls.getMethod("getPackageUid", new Class[]{String.class, Integer.TYPE, Integer.TYPE}).invoke(this.mPms, new Object[]{packageName, Integer.valueOf(0), Integer.valueOf(0)})).intValue();
            }
            Log.d(TAG, "getTrafficBytes packageName:" + packageName + " uid:" + uid);
            if (this.mStatsService != null) {
                INetworkStatsSession mSession = this.mStatsService.openSession();
                NetworkTemplate networkTemplate = NetworkTemplate.buildTemplateWifi();
                this.mStatsService.forceUpdate();
                Entry entry = mSession.getHistoryForUid(networkTemplate, uid, -1, 0, 10).getValues(0, Long.MAX_VALUE, currentTime, null);
                Log.d(TAG, "getTrafficBytes wifi entry.rxBytes:" + entry.rxBytes + " entry.txBytes:" + entry.txBytes);
                wifi_bytes = entry.rxBytes + entry.txBytes;
                networkTemplate = NetworkTemplate.buildTemplateMobileAll(this.tm.getSubscriberId(SubscriptionManager.getSubId(0)[0]));
                this.mStatsService.forceUpdate();
                entry = mSession.getHistoryForUid(networkTemplate, uid, -1, 0, 10).getValues(0, Long.MAX_VALUE, currentTime, null);
                Log.d(TAG, "getTrafficBytes Utils.SIM1 entry.rxBytes:" + entry.rxBytes + " entry.txBytes:" + entry.txBytes);
                sim1_data_bytes = entry.rxBytes + entry.txBytes;
                networkTemplate = NetworkTemplate.buildTemplateMobileAll(this.tm.getSubscriberId(SubscriptionManager.getSubId(1)[0]));
                this.mStatsService.forceUpdate();
                entry = mSession.getHistoryForUid(networkTemplate, uid, -1, 0, 10).getValues(0, Long.MAX_VALUE, currentTime, null);
                Log.d(TAG, "getTrafficBytes Utils.SIM2 entry.rxBytes:" + entry.rxBytes + " entry.txBytes:" + entry.txBytes);
                sim2_data_bytes = entry.rxBytes + entry.txBytes;
            }
            long j;
            switch (mode) {
                case 0:
                    j = (wifi_bytes + sim1_data_bytes) + sim2_data_bytes;
                    Binder.restoreCallingIdentity(callingId);
                    return j;
                case 1:
                    j = sim1_data_bytes + sim2_data_bytes;
                    Binder.restoreCallingIdentity(callingId);
                    return j;
                case 2:
                    Binder.restoreCallingIdentity(callingId);
                    return wifi_bytes;
                default:
                    break;
            }
        } catch (Throwable e) {
            Log.e(TAG, "getTrafficBytes Failed to talk Remote Service", e);
            return 0;
        } finally {
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public boolean addTrustedAppStore(String pkgs) {
        checkUp();
        Log.d(TAG, "addTrustedAppStore pkgs = " + pkgs);
        long callingId = Binder.clearCallingIdentity();
        boolean addTrustedAppStoreLI;
        try {
            addTrustedAppStoreLI = addTrustedAppStoreLI(Arrays.asList(pkgs.split(",")));
            return addTrustedAppStoreLI;
        } catch (Exception e) {
            addTrustedAppStoreLI = TAG;
            Log.d(addTrustedAppStoreLI, "addTrustedAppStore e = " + e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean addTrustedAppStoreList(List<String> pkgList) {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        for (String pkgName : pkgList) {
            Log.d(TAG, "addTrustedAppStore list pkgName = " + pkgName);
        }
        boolean addTrustedAppStoreLI;
        try {
            addTrustedAppStoreLI = addTrustedAppStoreLI(pkgList);
            return addTrustedAppStoreLI;
        } catch (Exception e) {
            addTrustedAppStoreLI = TAG;
            Log.d(addTrustedAppStoreLI, "addTrustedAppStore e = " + e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    private boolean addTrustedAppStoreLI(List<String> pkgList) {
        boolean update = false;
        for (String pkgName : pkgList) {
            if (!this.mAppStoreList.contains(pkgName)) {
                Log.d(TAG, "addTrustedAppStoreLI pkgName = " + pkgName);
                this.mAppStoreList.add(pkgName);
                update = true;
            }
        }
        if (update) {
            syncTrustedAppStore();
        }
        return update;
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0070 A:{SYNTHETIC, Splitter: B:27:0x0070} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00aa A:{SYNTHETIC, Splitter: B:41:0x00aa} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void syncTrustedAppStore() {
        IOException e;
        Throwable th;
        File pkgFile = new File(Utils.GN_TRUST_APP_STORE_LIST);
        if (!(pkgFile == null || (pkgFile.exists() ^ 1) == 0)) {
            try {
                pkgFile.createNewFile();
            } catch (IOException e2) {
                Log.e(TAG, "create new file catche exception. " + e2.toString());
            }
        }
        if (pkgFile == null || !pkgFile.exists()) {
            Log.w(TAG, "addTrustedAppStore failed. May file not exist");
            return;
        }
        BufferedWriter bufferWriter = null;
        try {
            BufferedWriter bufferWriter2 = new BufferedWriter(new FileWriter(pkgFile, false));
            try {
                if (this.mAppStoreList == null || this.mAppStoreList.size() <= 0) {
                    bufferWriter2.write("");
                } else {
                    for (String packageName : this.mAppStoreList) {
                        bufferWriter2.write(packageName);
                        bufferWriter2.newLine();
                    }
                }
                bufferWriter2.flush();
                if (bufferWriter2 != null) {
                    try {
                        bufferWriter2.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (IOException e4) {
                e2 = e4;
                bufferWriter = bufferWriter2;
                try {
                    Log.e(TAG, "write catch exception " + e2.toString());
                    if (bufferWriter == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferWriter != null) {
                        try {
                            bufferWriter.close();
                        } catch (IOException e5) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferWriter = bufferWriter2;
                if (bufferWriter != null) {
                }
                throw th;
            }
        } catch (IOException e6) {
            e2 = e6;
            Log.e(TAG, "write catch exception " + e2.toString());
            if (bufferWriter == null) {
                try {
                    bufferWriter.close();
                } catch (IOException e7) {
                }
            }
        }
    }

    public boolean deleteTrustedAppStore(String pkgs) {
        checkUp();
        Log.d(TAG, "deleteTrustedAppStore pkgs = " + pkgs);
        long callingId = Binder.clearCallingIdentity();
        boolean deleteTrustedAppStoreLI;
        try {
            deleteTrustedAppStoreLI = deleteTrustedAppStoreLI(Arrays.asList(pkgs.split(",")));
            return deleteTrustedAppStoreLI;
        } catch (Exception e) {
            deleteTrustedAppStoreLI = TAG;
            Log.d(deleteTrustedAppStoreLI, "deleteTrustedAppStore e = " + e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean deleteTrustedAppStore(List<String> pkgList) {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        boolean deleteTrustedAppStoreLI;
        try {
            deleteTrustedAppStoreLI = deleteTrustedAppStoreLI(pkgList);
            return deleteTrustedAppStoreLI;
        } catch (Exception e) {
            deleteTrustedAppStoreLI = TAG;
            Log.d(deleteTrustedAppStoreLI, "deleteTrustedAppStore e = " + e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    private boolean deleteTrustedAppStoreLI(List<String> pkgList) {
        boolean update = false;
        for (String pkgName : pkgList) {
            if (this.mAppStoreList.contains(pkgName)) {
                Log.d(TAG, "deleteTrustedAppStoreLI pkgName = " + pkgName);
                this.mAppStoreList.remove(pkgName);
                update = true;
            }
        }
        if (update) {
            syncTrustedAppStore();
        }
        return update;
    }

    public List<String> getTrustedAppStore() {
        long callingId = Binder.clearCallingIdentity();
        Log.d(TAG, "getTrustedAppStore callingId = " + callingId);
        List<String> list;
        try {
            list = this.mAppStoreList;
            return list;
        } catch (Exception e) {
            list = TAG;
            Log.d(list, "getTrustedAppStore e = " + e);
            return null;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean isTrustedAppStore(String packageName) {
        if (this.mAppStoreList == null || this.mAppStoreList.size() <= 0) {
            return false;
        }
        return this.mAppStoreList.contains(packageName);
    }

    public boolean setTrustedAppStoreState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setTrustedAppStoreState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            SystemProperties.set("persist.sys.gn.trust_enabled", Integer.toString(state));
            Log.d(TAG, "setTrustedAppStoreState state = " + state);
            return true;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getTrustedAppStoreState() {
        int state = SystemProperties.getInt("persist.sys.gn.trust_enabled", 0);
        Log.d(TAG, "get Trusted AppStore State : " + state);
        return state;
    }

    private void checkUp() {
        int callingUid = Binder.getCallingUid();
        if (!Utils.CHECK_UP) {
            return;
        }
        if (callingUid != 0 && callingUid == 1000) {
            return;
        }
        long valid_time;
        boolean isvalid;
        if (Utils.VIVO_CUSTOM_SUPPORT.equals(Utils.FLAG_YDHYY)) {
            boolean xiaolvGrant = this.mContext.checkCallingOrSelfPermission(Utils.VIVO_YDHYY_PERMISSION) == 0;
            boolean heGuanjiaGrant = this.mContext.checkCallingOrSelfPermission(Utils.VIVO_CUSTOM_SECURE_PERMISSION) == 0;
            if (!xiaolvGrant && !heGuanjiaGrant) {
                throw new SecurityException("SecurityException: signature is unavailable, APP doesn't have permission.");
            }
        } else if (Utils.VIVO_CUSTOM_SUPPORT.equals(Utils.FLAG_LSJY)) {
            this.mContext.enforceCallingOrSelfPermission(Utils.EMM_SECURITY_PERMISSION, null);
            if (SystemProperties.getBoolean("persist.ctemmsign.enable", true)) {
                valid_time = Long.parseLong(SystemProperties.get("persist.security.cvtm", "0"));
                isvalid = System.currentTimeMillis() <= valid_time;
                Log.d(TAG, "checkCert valid_time " + valid_time + " is valid = " + isvalid);
                if (!isvalid) {
                    throw new SecurityException("SecurityException: signature is unavailable, should be available!");
                }
            }
        } else {
            if (Utils.VIVO_CUSTOM_SUPPORT.equals(Utils.FLAG_ZXWY) || Utils.VIVO_CUSTOM_SUPPORT.equals(Utils.FLAG_ZHDL) || Utils.VIVO_CUSTOM_SUPPORT.equals(Utils.FLAG_BJHJL) || Utils.VIVO_CUSTOM_SUPPORT.equals(Utils.FLAG_BJWHBT) || Utils.VIVO_CUSTOM_SUPPORT.equals(Utils.FLAG_SZPAPH)) {
                if (this.mContext.checkCallingOrSelfPermission(Utils.SECOND_VIVO_CUSTOM_SECURE_PERMISSION) == 0) {
                    return;
                }
            } else if (Utils.VIVO_CUSTOM_SUPPORT.equals(Utils.FLAG_HNTCBD)) {
                this.mContext.enforceCallingOrSelfPermission(Utils.WESTONE_EMM_SECURITY_PERMISSION, null);
                if (SystemProperties.getBoolean("persist.westoneemmsign.enable", true)) {
                    valid_time = Long.parseLong(SystemProperties.get("persist.security.wvtm", "0"));
                    isvalid = System.currentTimeMillis() <= valid_time;
                    Log.d(TAG, "checkCert valid_time " + valid_time + " is valid = " + isvalid);
                    if (!isvalid) {
                        throw new SecurityException("SecurityException: signature is unavailable, should be available!");
                    }
                }
                return;
            }
            this.mContext.enforceCallingOrSelfPermission(Utils.VIVO_CUSTOM_SECURE_PERMISSION, null);
        }
    }

    public boolean setSystemTime(long when) {
        if (when < 0 || when / 1000 >= 2147483647L) {
            Log.w(TAG, "setSystemTime fail : the para is not valid when = " + when);
            throw new IllegalArgumentException("IllegalArgumentException:setSystemTime state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mAlarmManager.setTime(when);
            Log.d(TAG, "setSystemTime when = " + when);
            return true;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setTimeAutoState(int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("IllegalArgumentException:setTimeAutoState state is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
            case 1:
                Global.putInt(this.mContentResolver, "auto_time", state);
                Binder.restoreCallingIdentity(callingId);
                Log.d(TAG, "setTimeAutoState state = " + state);
                return true;
            default:
                try {
                    throw new IllegalArgumentException("setSystemTimeAuto fail : the para is not valid state = " + state);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    public int getTimeAutoState() {
        long callingId = Binder.clearCallingIdentity();
        int state = 1;
        try {
            state = Global.getInt(this.mContentResolver, "auto_time", 1);
            Log.d(TAG, "getTimeAutoState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setNetworkDataSimState(int state) {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        switch (state) {
            case 0:
                break;
            case 1:
            case 2:
                if (this.mISub == null) {
                    this.mISub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                }
                if (this.mISub != null) {
                    int[] subId = this.mISub.getSubId(state - 1);
                    if (subId != null && subId.length > 0 && subId[0] > 0) {
                        Log.d(TAG, "setNetworkDataSimState state = " + state + ", subId[0] = " + subId[0]);
                        this.mISub.setDefaultDataSubId(subId[0]);
                        break;
                    }
                }
                break;
            default:
                try {
                    throw new IllegalArgumentException("setNetworkDataSimState fail : the para is not valid state=" + state);
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to talk to isub service", e);
                    break;
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
        }
        Secure.putInt(this.mContentResolver, "ct_network_data_targetsim", state);
        Binder.restoreCallingIdentity(callingId);
        Log.d(TAG, "setNetworkDataSimState state = " + state);
        return true;
    }

    public int getNetworkDataSimState() {
        long callingId = Binder.clearCallingIdentity();
        int state = 0;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_network_data_targetsim", 0);
            Log.d(TAG, "getNetworkDataSimState state = " + state);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setVivoEmailPara(ContentValues values) {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        if (values == null) {
            Log.w(TAG, "setVivoEmailPara fail: values is null !");
            return false;
        }
        try {
            this.mContentResolver.insert(Uri.parse(Utils.VIVO_EMAIL_EXTRAL_PROVIDER), values);
            Log.d(TAG, "setVivoEmailPara values = " + values.toString());
            return true;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean ClearDnsCache() {
        checkUp();
        Intent intent = new Intent("android.intent.action.CLEAR_DNS_CACHE");
        intent.addFlags(536870912);
        intent.addFlags(67108864);
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            Log.d(TAG, "ClearDnsCache callingId = " + callingId);
            return true;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean startCallRecordPolicy(String filePath, int fileNameMode, int samplingRate, int fileFormat, String fileExtension) {
        return startCallRecordPolicyEx(filePath, fileNameMode, samplingRate, fileFormat, fileExtension, false, false);
    }

    public boolean startCallRecordPolicyEx(String filePath, int fileNameMode, int samplingRate, int fileFormat, String fileExtension, boolean disableToast, boolean hide) {
        int i = 0;
        if (fileNameMode < 0 || fileNameMode > 2) {
            throw new IllegalArgumentException("IllegalArgumentException:startCallRecordPolicy fileNameMode is illegal!");
        } else if (fileFormat < 0 || fileFormat > 4) {
            throw new IllegalArgumentException("IllegalArgumentException:startCallRecordPolicy fileFormat is illegal!");
        } else if (samplingRate <= 0 || (samplingRate >= 8000 && samplingRate <= 48000)) {
            checkUp();
            long callingId = Binder.clearCallingIdentity();
            try {
                Secure.putInt(this.mContentResolver, "ct_call_record_toast_disable", disableToast ? 1 : 0);
                ContentResolver contentResolver = this.mContentResolver;
                String str = "ct_call_record_detail_hide";
                if (hide) {
                    i = 1;
                }
                Secure.putInt(contentResolver, str, i);
                Secure.putString(this.mContentResolver, "ct_call_record_filepath", filePath);
                Secure.putString(this.mContentResolver, "ct_call_record_filename", null);
                Secure.putInt(this.mContentResolver, "ct_call_record_filename_mode", fileNameMode);
                Secure.putInt(this.mContentResolver, "ct_call_record_samplingrate", samplingRate);
                Secure.putInt(this.mContentResolver, "ct_call_record_maxtime", -1);
                Secure.putInt(this.mContentResolver, "ct_call_record_fileformat", fileFormat);
                Secure.putString(this.mContentResolver, "ct_call_record_fileextension", fileExtension);
                Secure.putInt(this.mContentResolver, "ct_call_record_enable", 2);
                Log.d(TAG, "startCallRecordPolicy");
                return true;
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        } else {
            throw new IllegalArgumentException("IllegalArgumentException:startCallRecordPolicy samplingRate is illegal!");
        }
    }

    public boolean stopCallRecordPolicy() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_call_record_toast_disable", 0);
            Secure.putInt(this.mContentResolver, "ct_call_record_detail_hide", 0);
            Secure.putInt(this.mContentResolver, "ct_call_record_enable", 0);
            Secure.putString(this.mContentResolver, "ct_call_record_filepath", null);
            Secure.putString(this.mContentResolver, "ct_call_record_filename", null);
            Secure.putInt(this.mContentResolver, "ct_call_record_filename_mode", 0);
            Secure.putInt(this.mContentResolver, "ct_call_record_samplingrate", -1);
            Secure.putInt(this.mContentResolver, "ct_call_record_maxtime", -1);
            Secure.putInt(this.mContentResolver, "ct_call_record_fileformat", 0);
            Secure.putString(this.mContentResolver, "ct_call_record_fileextension", null);
            Log.d(TAG, "stopCallRecordPolicy");
            return true;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean startCallRecord(String filePath, String fileName, int fileNameMode, int maxTime, int samplingRate, int fileFormat, String fileExtension) {
        return startCallRecordEx(filePath, fileName, fileNameMode, maxTime, samplingRate, fileFormat, fileExtension, false, false);
    }

    public boolean startCallRecordEx(String filePath, String fileName, int fileNameMode, int maxTime, int samplingRate, int fileFormat, String fileExtension, boolean disableToast, boolean hide) {
        if (fileNameMode < 0 || fileNameMode > 2) {
            throw new IllegalArgumentException("IllegalArgumentException:startCallRecord fileNameMode is illegal!");
        } else if (fileFormat < 0 || fileFormat > 4) {
            throw new IllegalArgumentException("IllegalArgumentException:startCallRecord fileFormat is illegal!");
        } else if (samplingRate > 0 && (samplingRate < 8000 || samplingRate > 48000)) {
            throw new IllegalArgumentException("IllegalArgumentException:startCallRecord samplingRate is illegal!");
        } else if (maxTime <= 0) {
            throw new IllegalArgumentException("IllegalArgumentException:startCallRecord maxTime is illegal!");
        } else {
            checkUp();
            long callingId = Binder.clearCallingIdentity();
            try {
                Secure.putInt(this.mContentResolver, "ct_call_record_toast_disable", disableToast ? 1 : 0);
                Secure.putInt(this.mContentResolver, "ct_call_record_detail_hide", hide ? 1 : 0);
                Secure.putString(this.mContentResolver, "ct_call_record_filepath", filePath);
                Secure.putString(this.mContentResolver, "ct_call_record_filename", fileName);
                Secure.putInt(this.mContentResolver, "ct_call_record_filename_mode", fileNameMode);
                Secure.putInt(this.mContentResolver, "ct_call_record_maxtime", maxTime);
                Secure.putInt(this.mContentResolver, "ct_call_record_samplingrate", samplingRate);
                Secure.putInt(this.mContentResolver, "ct_call_record_fileformat", fileFormat);
                Secure.putString(this.mContentResolver, "ct_call_record_fileextension", fileExtension);
                Secure.putInt(this.mContentResolver, "ct_call_record_enable", 1);
                Log.d(TAG, "startCallRecord");
                return true;
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    public boolean stopCallRecord() {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_call_record_toast_disable", 0);
            Secure.putInt(this.mContentResolver, "ct_call_record_detail_hide", 0);
            Secure.putInt(this.mContentResolver, "ct_call_record_enable", 0);
            Secure.putString(this.mContentResolver, "ct_call_record_filepath", null);
            Secure.putString(this.mContentResolver, "ct_call_record_filename", null);
            Secure.putInt(this.mContentResolver, "ct_call_record_filename_mode", 0);
            Secure.putInt(this.mContentResolver, "ct_call_record_samplingrate", -1);
            Secure.putInt(this.mContentResolver, "ct_call_record_maxtime", -1);
            Secure.putInt(this.mContentResolver, "ct_call_record_fileformat", 0);
            Secure.putString(this.mContentResolver, "ct_call_record_fileextension", null);
            Log.d(TAG, "stopCallRecord");
            return true;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    private void notificationStateChange() {
        Intent intent = new Intent("vivo.custom.action.NOTIFICATION_RESTRICT_STATE_CHANGE");
        intent.addFlags(536870912);
        this.mContext.sendBroadcast(intent);
    }

    private void checkAppNotificationState(String packageName, boolean allowUpdate) {
        if (this.mStateLcok.mNotificationPattern != 0) {
            synchronized (this.mStateLcok) {
                int state = this.mVivoCustomDbBridge.getPackageNotificationState(packageName);
                boolean isBlack = (state & 1) != 0;
                boolean isWhite = (state & 2) != 0;
                if (this.mStateLcok.mNotificationPattern == 1) {
                    if (isBlack) {
                        setNotificaionEnabled(packageName, false, true);
                    } else {
                        restoreAppNotificaionState(packageName, allowUpdate);
                    }
                } else if (this.mStateLcok.mNotificationPattern == 2) {
                    if (isWhite) {
                        setNotificaionEnabled(packageName, true, true);
                    } else {
                        setNotificaionEnabled(packageName, false, false);
                    }
                }
            }
        }
    }

    private void setNotificaionEnabled(String packageName, boolean enabled, boolean updateNMS) {
        if (packageName == null) {
            Log.w(TAG, "setNotificaionEnabled but app packageNames is null!");
            return;
        }
        try {
            if (Utils.disallowNotificationRestrictPackages == null || !Utils.disallowNotificationRestrictPackages.contains(packageName)) {
                ApplicationInfo appInfo = this.mPackageManager.getApplicationInfo(packageName, 0);
                Log.d(TAG, "setNotificaionEnabled app name = " + packageName + ", enabled = " + enabled);
                if (appInfo != null) {
                    boolean oldState = this.iNotificationManager.areNotificationsEnabledForPackage(packageName, appInfo.uid);
                    Log.d(TAG, "setNotificaionEnabled1111 for app: " + packageName + ", oldState = " + oldState);
                    if (enabled == oldState) {
                        if (updateNMS) {
                            this.iNotificationManager.setNotificationsEnabledForPackage(packageName, appInfo.uid, enabled);
                        }
                        return;
                    }
                    this.iNotificationManager.setNotificationsEnabledForPackage(packageName, appInfo.uid, enabled);
                    Log.d(TAG, "setNotificaionEnabled2222 for app: " + packageName + ", oldState = " + oldState);
                    this.mVivoCustomDbBridge.backupPackageNotificationState(packageName, oldState, false);
                    Log.d(TAG, "set and backup notifivaiton state for app: " + packageName + ", oldState = " + oldState);
                }
            }
        } catch (NameNotFoundException e) {
            Log.w(TAG, "setNotificaionEnabled but app is not be installed : " + packageName);
        } catch (RemoteException e2) {
            Log.w(TAG, "setNotificaionEnabled failed : " + packageName, e2);
        }
    }

    private void restoreAppNotificaionState(String packageName, boolean allowUpdate) {
        if (packageName == null) {
            Log.w(TAG, "restoreAppNotificaionState but app packageNames is null!");
            return;
        }
        try {
            int backupState = this.mVivoCustomDbBridge.getPackageNotificationBackup(packageName, allowUpdate);
            if (backupState != -1) {
                ApplicationInfo appInfo = this.mPackageManager.getApplicationInfo(packageName, 0);
                if (appInfo != null) {
                    boolean oldState = this.iNotificationManager.areNotificationsEnabledForPackage(packageName, appInfo.uid);
                    Log.d(TAG, "restoreAppNotificaionState app name = " + packageName + ", oldState = " + oldState);
                    boolean newState = backupState == 1;
                    if (newState != oldState) {
                        this.iNotificationManager.setNotificationsEnabledForPackage(packageName, appInfo.uid, newState);
                        if (Utils.DEBUG_EXTRA) {
                            Log.d(TAG, "restoreAppNotificaionState app name = " + packageName + ", newState = " + newState);
                        }
                    }
                }
            }
        } catch (NameNotFoundException e) {
            Log.w(TAG, "restoreAppNotificaionState but app is not be installed : " + packageName);
        } catch (RemoteException e2) {
            Log.w(TAG, "restoreAppNotificaionState failed : " + packageName, e2);
        }
    }

    private void restoreNotificaionState() {
        List<String> backupList = this.mVivoCustomDbBridge.getPackageNotificationBackupList();
        if (backupList != null && !backupList.isEmpty()) {
            for (String packageName : backupList) {
                restoreAppNotificaionState(packageName, false);
            }
        }
    }

    private void initNotificationWhitePattern() {
        List<PackageInfo> packageInfoList = this.mPackageManager.getInstalledPackages(0);
        if (packageInfoList != null && (packageInfoList.isEmpty() ^ 1) != 0) {
            for (PackageInfo packageInfo : packageInfoList) {
                checkAppNotificationState(packageInfo.packageName, false);
            }
        }
    }

    private void initNotificationBlackPattern() {
        List<String> blackList = this.mVivoCustomDbBridge.getPackageNotificationBlackList();
        if (blackList != null && (blackList.isEmpty() ^ 1) != 0) {
            for (String pkgName : blackList) {
                checkAppNotificationState(pkgName, false);
            }
        }
    }

    public void setNotificationRestrictPattern(int pattern) {
        if (pattern < 0 || pattern > 3) {
            throw new IllegalArgumentException("IllegalArgumentException:setNotificationRestrictPattern pattern is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mStateLcok) {
                int oldPattern = this.mStateLcok.mNotificationPattern;
                if (Utils.DEBUG_EXTRA) {
                    Log.d(TAG, "setNotificationRestrictPattern oldPattern = " + oldPattern);
                }
                if (oldPattern != pattern) {
                    this.mStateLcok.mNotificationPattern = pattern;
                    Secure.putInt(this.mContentResolver, Utils.CT_NOTIFICATION_RESTRICT_PATTERN, pattern);
                    Log.d(TAG, "setNotificationRestrictPattern pattern = " + pattern);
                    if (oldPattern == 2 || oldPattern == 1) {
                        restoreNotificaionState();
                    }
                    if (pattern == 2) {
                        initNotificationWhitePattern();
                    } else if (pattern == 1) {
                        initNotificationBlackPattern();
                    }
                    this.mVivoCustomDbBridge.syncPackages();
                    notificationStateChange();
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getNotificationRestrictPattern() {
        long callingId = Binder.clearCallingIdentity();
        int state = 0;
        try {
            synchronized (this.mStateLcok) {
                state = this.mStateLcok.mNotificationPattern;
            }
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addNotificationBlackList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addNotificationBlackList packageNames is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mStateLcok) {
                this.mVivoCustomDbBridge.setPackageNotificationBlackList(packageNames, true, false);
                for (String packageName : packageNames) {
                    checkAppNotificationState(packageName, false);
                }
                this.mVivoCustomDbBridge.syncPackages();
            }
            notificationStateChange();
            Log.d(TAG, "addNotificationBlackList :" + packageNames);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void deleteNotificationBlackList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deleteNotificationBlackList packageNames is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mStateLcok) {
                this.mVivoCustomDbBridge.setPackageNotificationBlackList(packageNames, false, false);
                for (String packageName : packageNames) {
                    checkAppNotificationState(packageName, false);
                }
                this.mVivoCustomDbBridge.syncPackages();
            }
            notificationStateChange();
            Log.d(TAG, "deleteNotificationBlackList :" + packageNames);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getNotificationBlackList() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getPackageNotificationBlackList();
            if (Utils.DEBUG_EXTRA) {
                Log.d(TAG, "getNotificationBlackList :" + list);
            }
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addNotificationWhiteList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addNotificationWhiteList packageNames is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mStateLcok) {
                this.mVivoCustomDbBridge.setPackageNotificationWhiteList(packageNames, true, false);
                for (String packageName : packageNames) {
                    checkAppNotificationState(packageName, false);
                }
                this.mVivoCustomDbBridge.syncPackages();
            }
            notificationStateChange();
            Log.d(TAG, "addNotificationWhiteList :" + packageNames);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void deleteNotificationWhiteList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deleteNotificationWhiteList packageNames is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mStateLcok) {
                this.mVivoCustomDbBridge.setPackageNotificationWhiteList(packageNames, false, false);
                for (String packageName : packageNames) {
                    checkAppNotificationState(packageName, false);
                }
                this.mVivoCustomDbBridge.syncPackages();
            }
            notificationStateChange();
            Log.d(TAG, "deleteNotificationWhiteList :" + packageNames);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getNotificationWhiteList() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getPackageNotificationWhiteList();
            if (Utils.DEBUG_EXTRA) {
                Log.d(TAG, "getNotificationWhiteList :" + list);
            }
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void registerSensitiveMmsListenerCallback(ISensitiveMmsDelegate callback) {
        Log.d(TAG, "register sensitive mms eListener ");
        this.mSensitiveDelegate = callback;
    }

    public void transmitSensitiveMms(String str) {
        List<String> strList = Arrays.asList(str.split(","));
        if (this.mSensitiveDelegate != null) {
            Log.d(TAG, "transmit SensitiveMms");
            try {
                this.mSensitiveDelegate.updateSensitiveMms(strList);
            } catch (Exception e) {
            }
        }
    }

    public void setMmsKeywords(String str) {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putString(this.mContentResolver, "ct_message_keyword", str);
            Log.d(TAG, "setMmsKeywords test = " + str);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean setClipBoardState(int state) {
        Log.d(TAG, "set ClipBoard State = " + state);
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_clipboard_state", state);
            return true;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getClipBoardState() {
        Log.d(TAG, "get ClipBoard State.");
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            int clipboardState = Secure.getInt(this.mContentResolver, "ct_clipboard_state", 1);
            return clipboardState;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean isBootCompleted() {
        return this.mIsBootCompleted;
    }

    public void setForbitRunPattern(int pattern) {
        if (pattern < 0 || pattern > 2) {
            throw new IllegalArgumentException("IllegalArgumentException:setForbitRunPattern pattern is illegal!");
        }
        Log.d(TAG, "setForbitRunPattern pattern = " + pattern);
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_forbit_run_pattern", pattern);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getForbitRunPattern() {
        long callingId = Binder.clearCallingIdentity();
        int state = 0;
        try {
            state = Secure.getInt(this.mContentResolver, "ct_forbit_run_pattern", 0);
            return state;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void addForbitRunBlackList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:addInstallBlackList packageNames is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setForbitRunBlackList(packageNames, true);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void deleteForbitRunBlackList(List<String> packageNames) {
        if (packageNames == null) {
            throw new IllegalArgumentException("IllegalArgumentException:deleteInstallBlackList packageNames is illegal!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mVivoCustomDbBridge.setForbitRunBlackList(packageNames, false);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> getForbitRunBlackList() {
        long callingId = Binder.clearCallingIdentity();
        List<String> list = null;
        try {
            list = this.mVivoCustomDbBridge.getForbitRunBlackList();
            return list;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }
}
