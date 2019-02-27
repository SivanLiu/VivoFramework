package com.vivo.services.security.server;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IProcessObserver;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManagerInternal;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IPermissionController;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.LocalServices;
import com.vivo.framework.security.VivoPermissionManager;
import com.vivo.services.epm.util.MessageCenterHelper;
import com.vivo.services.rms.sdk.RMNative;
import com.vivo.services.security.client.IVivoPermissionCallback;
import com.vivo.services.security.client.VivoPermissionInfo;
import com.vivo.services.security.client.VivoPermissionType;
import com.vivo.services.security.client.VivoPermissionType.VivoPermissionCategory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import vivo.app.security.IVivoPermissionService.Stub;

public class VivoPermissionService extends Stub {
    private static final long CHECK_DELETE_TIME = 300000;
    private static final int DEBUG_CTS_TEST = SystemProperties.getInt("persist.debug.ctstest", 0);
    private static final boolean DEBUG_CTS_TEST_23;
    private static final boolean DEBUG_VPS = SystemProperties.get("persist.sys.debug.vps", "yes").equals("yes");
    public static final int MSG_CONFIG_LOADER = 3;
    public static final int MSG_INSTALL_SOURCE_SETS = 4;
    public static final int PERM_FG_ACTIVITY_CHANGED = 2;
    public static final int PERM_PROCESS_DIED = 1;
    private static final String PROP_SUPER_SAVER = "sys.super_power_save";
    private static final String TAG = "VPS";
    private static final int VERSION_1 = 1;
    private static final int VERSION_2 = 2;
    private static ArraySet<String> installtrustSets = new ArraySet();
    private static Context mContext = null;
    private static byte[] mVPSLock = new byte[0];
    private static VivoPermHandler mVivoPermHandler;
    private static ArrayList<String> mWhitePkgs = new ArrayList();
    private final int MAX_LOCATION_BINDER_CHECK_TIME = 3;
    private ArrayMap<String, ArrayMap<String, CheckDeleteState>> mCheckDeleteState = new ArrayMap();
    private HashMap<String, Integer> mCheckLocationBinderTimes = null;
    PackageManagerInternal mPackageManagerInt;
    private final SparseArray<String> mPackageUid = new SparseArray();
    private IProcessObserver mProcessObserver = new IProcessObserver.Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (foregroundActivities) {
                VivoPermissionService.mVivoPermHandler.sendMessageDelayed(VivoPermissionService.mVivoPermHandler.obtainMessage(2, pid, uid), 3000);
            }
        }

        public void onProcessDied(int pid, int uid) {
            VivoPermissionService.mVivoPermHandler.obtainMessage(1, pid, uid).sendToTarget();
        }
    };
    private Handler mUiHandler = null;
    private HashMap<String, VivoDeleteDialog> mVDDMap = null;
    private VivoPermissionConfig mVPC = null;
    private HashMap<String, VivoPermissionDialog> mVPDMap = null;
    private HashMap<String, VivoPermissionDeniedDialogModeOne> mVPDMap1 = null;
    private HashMap<String, VivoPermissionDeniedDialogModeTwo> mVPDMap2 = null;
    private HashMap<String, VivoPermissionDeniedDialogModeThree> mVPDMap3 = null;
    private VivoPermissionReceiver mVPR = null;
    public HandlerThread permThread;
    private int retryCount = 0;
    private int showIMIEOneTipsLimit = 3;
    private int showIMIETwoTipsLimit = 1;
    private ArraySet<String> vivoImeiSets = new ArraySet();

    private class CheckDeleteState {
        private long checkTime;
        private boolean grant;

        public CheckDeleteState(boolean grant, long time) {
            this.grant = grant;
            this.checkTime = time;
        }

        public boolean isDeleteGrant() {
            return this.grant;
        }

        public boolean isNeedCheck() {
            if (SystemClock.elapsedRealtime() - this.checkTime > VivoPermissionService.CHECK_DELETE_TIME) {
                return true;
            }
            return false;
        }

        public void setDeleteGrant(boolean grant, long time) {
            this.grant = grant;
            this.checkTime = time;
        }
    }

    static class PermissionController extends IPermissionController.Stub {
        private VivoPermissionService mVPS = null;

        PermissionController(VivoPermissionService vps) {
            this.mVPS = vps;
        }

        public boolean checkPermission(String permission, int pid, int uid) {
            boolean z = true;
            VivoPermissionService.printfDebug("PermissionController-->checkPermission (" + permission + ")pid:" + pid + " uid:" + uid);
            if (permission == null) {
                return false;
            }
            if (this.mVPS.checkPermission(permission, pid, uid, null) != 1) {
                z = false;
            }
            return z;
        }

        public String[] getPackagesForUid(int uid) {
            if (VivoPermissionService.mContext != null) {
                return VivoPermissionService.mContext.getPackageManager().getPackagesForUid(uid);
            }
            VivoPermissionService.printfDebug("PermissionController-->getPackagesForUid mContext is null");
            return null;
        }

        public boolean isRuntimePermission(String permission) {
            boolean z = true;
            if (!(permission == null || VivoPermissionService.mContext == null)) {
                try {
                    if (VivoPermissionService.mContext.getPackageManager().getPermissionInfo(permission, 0).protectionLevel != 1) {
                        z = false;
                    }
                    return z;
                } catch (NameNotFoundException nnfe) {
                    Slog.e(VivoPermissionService.TAG, "PermissionController-->isRuntimePermission No such permission: " + permission, nnfe);
                }
            }
            return false;
        }
    }

    class VivoPermHandler extends Handler {
        public VivoPermHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    synchronized (VivoPermissionService.this.mCheckDeleteState) {
                        VivoPermissionService.this.mCheckDeleteState.remove(msg.arg1 + "");
                    }
                case 2:
                    int uid = msg.arg2;
                    if (uid > MessageCenterHelper.REBIND_SERVICE_TIME_INTERVAL) {
                        PackageInfo appInfo = null;
                        long identity = Binder.clearCallingIdentity();
                        try {
                            appInfo = VivoPermissionManager.getInstance().getCallingPackageInfo(VivoPermissionService.mContext, uid);
                            if (appInfo != null && appInfo.packageName != null) {
                                if (appInfo.applicationInfo.targetSdkVersion >= 23) {
                                    String packageName = appInfo.packageName;
                                    if (!VivoPermissionService.this.isTestApp(packageName)) {
                                        if (VivoPermissionService.this.needCheckPkg(appInfo)) {
                                            String perm = "android.permission.READ_PHONE_STATE";
                                            if (VivoPermissionService.this.checkOnePermission(packageName, perm, uid) == 2) {
                                                if (VivoPermissionService.this.isVivoImeiPkg(packageName)) {
                                                    VivoPermissionInfo vpi = VivoPermissionService.this.getAppPermission(packageName);
                                                    int vpid = VivoPermissionType.getVPType(perm).getVPTypeId();
                                                    if ((VivoPermissionService.this.needShowImeiTipsDialogOne(vpi, vpid) || VivoPermissionService.this.needShowImeiTipsDialogTwo(vpi, vpid)) && VivoPermissionService.this.isRunningForeground(packageName)) {
                                                        VivoPermissionService.this.showWarningDialogToChoose(packageName, perm, msg.arg1, uid, VivoPermissionService.this.getVPDMapKey(packageName, perm), null);
                                                        break;
                                                    }
                                                }
                                                VivoPermissionService.printfDebug("no need: " + packageName);
                                                return;
                                            }
                                        }
                                        VivoPermissionService.printfDebug("vivo App: " + packageName);
                                        return;
                                    }
                                    VivoPermissionService.printfDebug("test: " + packageName);
                                    return;
                                }
                            }
                            VivoPermissionService.printfDebug("getCallingPackageInfo == null");
                            return;
                        } finally {
                            Binder.restoreCallingIdentity(identity);
                        }
                    }
                    break;
                case 3:
                    VivoPermissionService.this.getconfig();
                    VivoPermissionService vivoPermissionService = VivoPermissionService.this;
                    vivoPermissionService.retryCount = vivoPermissionService.retryCount + 1;
                    break;
            }
        }
    }

    static {
        boolean z;
        if ("1".equals(SystemProperties.get("ro.build.g_test", "0"))) {
            z = true;
        } else {
            z = "1".equals(SystemProperties.get("ro.build.aia", "0"));
        }
        DEBUG_CTS_TEST_23 = z;
        installtrustSets.add("com.bbk.appstore");
        installtrustSets.add("com.vivo.browser");
        installtrustSets.add("com.vivo.game");
        installtrustSets.add("com.vivo.easyshare");
        installtrustSets.add("com.vivo.sharezone");
    }

    public VivoPermissionService(Context context, Handler uiHandler) {
        printfInfo("Start:VivoPermissionService");
        mContext = context;
        this.mUiHandler = uiHandler;
        this.mVPC = new VivoPermissionConfig(this, context);
        this.mVPDMap = new HashMap();
        this.mVDDMap = new HashMap();
        this.mVPDMap1 = new HashMap();
        this.mVPDMap2 = new HashMap();
        this.mVPDMap3 = new HashMap();
        this.mCheckLocationBinderTimes = new HashMap();
        registerBroadcastReceiver();
        IActivityManager mIActivityManager = ActivityManagerNative.getDefault();
        if (mIActivityManager != null) {
            try {
                mIActivityManager.registerProcessObserver(this.mProcessObserver);
            } catch (RemoteException e) {
                Slog.e(TAG, "registerProcessObserver failed.");
            }
        }
        this.permThread = new HandlerThread("VivoPermission");
        this.permThread.start();
        mVivoPermHandler = new VivoPermHandler(this.permThread.getLooper());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.vivo.daemonService.unifiedconfig.update_finish_broadcast_vivoimei");
        intentFilter.addAction("com.vivo.daemonService.unifiedconfig.update_finish_broadcast_VivoImeiApps");
        mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                VivoPermissionService.printfInfo("unifiedconfig onReceive");
                VivoPermissionService.this.retryCount = 0;
                VivoPermissionService.mVivoPermHandler.sendEmptyMessage(3);
            }
        }, intentFilter);
        ServiceManager.addService("vivo_permission", new PermissionController(this));
        printfInfo("Finish:VivoPermissionService");
    }

    public int checkPermission(String permission, int pid, int uid, IVivoPermissionCallback cb) {
        boolean isMonitorSystemApp = VivoPermissionManager.getInstance().needMonitorSystemApp();
        boolean isMonitorSystemUid = VivoPermissionManager.getInstance().needMonitorSystemUid();
        if (1 == DEBUG_CTS_TEST) {
            printfDebug("debug mode,then PERMISSION_GRANTED");
            return 1;
        } else if (uid == 0) {
            printfDebug("root then PERMISSION_GRANTED!");
            return 1;
        } else if (uid != 1000 || isMonitorSystemUid) {
            VivoPermissionType vpType = VivoPermissionType.getVPType(permission);
            if (vpType == VivoPermissionType.LAST) {
                printfDebug("permission=" + permission + "; It's VivoPermissionType.LAST, then PERMISSION_GRANTED!");
                return 1;
            } else if (vpType.getVPCategory() == VivoPermissionCategory.OTHERS) {
                printfDebug("permission=" + permission + "; It's VivoPermissionCategory.OTHERS, then PERMISSION_GRANTED!");
                return 1;
            } else {
                PackageInfo appInfo = null;
                long identity = Binder.clearCallingIdentity();
                try {
                    appInfo = VivoPermissionManager.getInstance().getCallingPackageInfo(mContext, uid);
                    if (appInfo == null || appInfo.applicationInfo == null) {
                        printfInfo("getCallingPackageInfo == null, then PERMISSION_GRANTED! uid:" + uid + " pid:" + pid);
                        return 1;
                    } else if (appInfo.applicationInfo.targetSdkVersion < 23 || !(this.mVPC.isRuntimePermission(permission) || "android.permission.ACCESS_WIFI_STATE".equals(permission))) {
                        if (isTestApp(appInfo.packageName)) {
                            printfDebug("test: " + appInfo.packageName + "; (" + permission + ") then PERMISSION_GRANTED!");
                            return 1;
                        } else if (needCheckPkg(appInfo) || isMonitorSystemApp) {
                            if ("android.permission.BLUETOOTH".equals(permission) || "android.permission.CHANGE_WIFI_STATE".equals(permission)) {
                                PackageManager pm = mContext.getPackageManager();
                                if (!isMonitorSystemApp && (pm.checkSignatures("android", appInfo.packageName) == 0 || pm.checkSignatures("com.android.providers.contacts", appInfo.packageName) == 0 || pm.checkSignatures("com.android.providers.media", appInfo.packageName) == 0)) {
                                    printfDebug("vivo app: " + appInfo.packageName + "; (" + permission + ") then PERMISSION_GRANTED!");
                                    return 1;
                                }
                            }
                            if (UserHandle.getUserId(uid) != 0) {
                                printfDebug("uid=" + uid + " is not owner user,adjust to owner user :" + UserHandle.getAppId(uid));
                                uid = UserHandle.getAppId(uid);
                            }
                            String packageName = appInfo.packageName;
                            printfDebug("start checkPermission packageName=" + packageName + " (" + permission + ") ;pid=" + pid + ";uid=" + uid);
                            int configResult = waitConfirmPermission(pid, packageName, permission, cb, uid);
                            if (configResult == 1) {
                                return 1;
                            }
                            if (configResult == 3) {
                                return 3;
                            }
                            return 2;
                        } else {
                            printfDebug("no need Check: " + appInfo.packageName + "; (" + permission + ") then PERMISSION_GRANTED!");
                            return 1;
                        }
                    } else {
                        printfDebug("Runtime permission, just pass for vps! uid:" + uid + " pid:" + pid);
                        return 1;
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        } else {
            printfDebug("SYSTEM_UID then PERMISSION_GRANTED!");
            return 1;
        }
    }

    /* JADX WARNING: Missing block: B:6:0x0050, code:
            if (parserXml(new java.io.File("data/bbkcore/delete_white_pkgs.xml")) != false) goto L_0x0052;
     */
    /* JADX WARNING: Missing block: B:60:0x0223, code:
            return r2;
     */
    /* JADX WARNING: Missing block: B:86:0x02ac, code:
            return r2;
     */
    /* JADX WARNING: Missing block: B:116:0x0362, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkDelete(String path, String pkgName, String type, String key, int uid, int pid) {
        String packageName = "";
        if (this.mPackageUid.get(uid) != null) {
            packageName = (String) this.mPackageUid.get(uid);
        } else {
            PackageInfo appInfo = null;
            long identity = Binder.clearCallingIdentity();
            try {
                appInfo = VivoPermissionManager.getInstance().getCallingPackageInfo(mContext, uid);
                if (appInfo == null || appInfo.packageName == null) {
                    printfDebug("getCallingPackageInfo == null, then pass! uid:" + uid + " pid:" + pid);
                    return true;
                }
                packageName = appInfo.packageName;
                this.mPackageUid.put(uid, packageName);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
        printfInfo(packageName + " check delete path = " + path);
        ArrayList<String> tempPkgs = VivoDeleteUtils.mWhitePkgs;
        if (mWhitePkgs.size() <= 0) {
        }
        tempPkgs = mWhitePkgs;
        if (tempPkgs.size() > 0) {
            for (String white : tempPkgs) {
                if (TextUtils.equals(white, packageName)) {
                    printfDebug("white list: " + packageName + "; (" + path + ") then pass!");
                    return true;
                }
            }
        }
        if (TextUtils.equals(pkgName, packageName)) {
            printfDebug("Owner: " + packageName + "; (" + path + ") then pass!");
            return true;
        } else if (isTestApp(packageName)) {
            printfDebug("test: " + packageName + "; (" + path + ") then pass!");
            return true;
        } else if (VivoPermissionManager.getInstance().isVivoApp(mContext, packageName)) {
            printfDebug("vivo App: " + packageName + "; (" + path + ") then pass!");
            return true;
        } else {
            String checkKey = pid + "";
            synchronized (this.mCheckDeleteState) {
                int result;
                boolean z;
                ArrayMap<String, CheckDeleteState> pathMaps;
                if (this.mCheckDeleteState.containsKey(checkKey)) {
                    if (((ArrayMap) this.mCheckDeleteState.get(checkKey)).containsKey(key)) {
                        if (!((CheckDeleteState) ((ArrayMap) this.mCheckDeleteState.get(checkKey)).get(key)).isNeedCheck()) {
                            boolean isDeleteGrant = ((CheckDeleteState) ((ArrayMap) this.mCheckDeleteState.get(checkKey)).get(key)).isDeleteGrant();
                            printfDebug("5 mini in, return last result = " + isDeleteGrant);
                            return isDeleteGrant;
                        } else if (isKeyguardLocked(mContext) || !isRunningForeground(packageName)) {
                            printfDebug("keyguard lock or not running Foreground,just return false");
                            return false;
                        } else {
                            result = showDeleteDialogToChoose(packageName, key, pkgName, type, pid, uid, checkKey);
                            if (result != 0) {
                                ((CheckDeleteState) ((ArrayMap) this.mCheckDeleteState.get(checkKey)).get(key)).setDeleteGrant(result == 1, SystemClock.elapsedRealtime());
                            }
                            printfDebug("5 mini out, show dialog again result = " + result);
                            z = result == 1;
                        }
                    } else if (isKeyguardLocked(mContext) || !isRunningForeground(packageName)) {
                        printfDebug("keyguard lock or not running Foreground,just return false");
                        return false;
                    } else {
                        result = showDeleteDialogToChoose(packageName, key, pkgName, type, pid, uid, checkKey);
                        if (result != 0) {
                            pathMaps = (ArrayMap) this.mCheckDeleteState.get(checkKey);
                            pathMaps.put(key, new CheckDeleteState(result == 1, SystemClock.elapsedRealtime()));
                            this.mCheckDeleteState.put(checkKey, pathMaps);
                        }
                        printfDebug("first show dialog result = " + result);
                        if (result == 1) {
                            z = true;
                        } else {
                            z = false;
                        }
                    }
                } else if (isKeyguardLocked(mContext) || !isRunningForeground(packageName)) {
                    printfDebug("keyguard lock or not running Foreground,just return false");
                    return false;
                } else {
                    result = showDeleteDialogToChoose(packageName, key, pkgName, type, pid, uid, checkKey);
                    if (result != 0) {
                        CheckDeleteState checkDelteState = new CheckDeleteState(result == 1, SystemClock.elapsedRealtime());
                        pathMaps = new ArrayMap();
                        pathMaps.put(key, checkDelteState);
                        this.mCheckDeleteState.put(checkKey, pathMaps);
                    }
                    printfDebug("first show dialog result = " + result);
                    if (result == 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                }
            }
        }
    }

    public List<VivoPermissionInfo> getSpecifiedPermAppList(int vpTypeId) {
        return this.mVPC.getSpecifiedPermAppList(vpTypeId);
    }

    public List<VivoPermissionInfo> getTrustedAppList() {
        return this.mVPC.getTrustedAppList();
    }

    public List<VivoPermissionInfo> getMonitorAppList() {
        return this.mVPC.getMonitorAppList();
    }

    public VivoPermissionInfo getAppPermission(String packageName) {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mVPC.handleRuntimePermission(packageName, false);
            return this.mVPC.getAppPermission(packageName);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void setAppPermissionExt(VivoPermissionInfo vpi) {
        enforcePermission();
        this.mVPC.saveAppPermission(vpi);
    }

    public void setAppPermission(String packageName, int vpTypeId, int result) {
        printfDebug("setAppPermission    packageName=" + packageName + " vpTypeId=" + vpTypeId + " result=" + result);
        enforcePermission();
        if (!this.mVPC.doForRuntimePermission(packageName, vpTypeId, result)) {
            this.mVPC.saveAppPermission(packageName, vpTypeId, result);
        }
    }

    public void setAppPermissions(String packageName, List<VivoPermissionType> vtypes, int result) {
        enforcePermission();
        for (VivoPermissionType vtype : vtypes) {
            this.mVPC.saveAppPermission(packageName, vtype.getVPTypeId(), result);
        }
        this.mVPC.handleGroupPermission(packageName, ((VivoPermissionType) vtypes.get(0)).getVPGroup(), result);
    }

    public void setWhiteListApp(String packageName, boolean enable) {
        enforcePermission();
        this.mVPC.grantAllRuntimePermissions(packageName, enable);
        this.mVPC.setWhiteListApp(packageName, enable);
    }

    public void setBlackListApp(String packageName, boolean enable) {
        enforcePermission();
        this.mVPC.setBlackListApp(packageName, enable);
    }

    public void noteStartActivityProcess(String packageName) {
    }

    public boolean isBuildInThirdPartApp(String packageName) {
        return this.mVPC.isBuildInThirdPartApp(packageName);
    }

    public int getVPMVersion() {
        return 2;
    }

    public int getVPMDataBaseState() {
        return this.mVPC.getDataBaseState();
    }

    private void enforcePermission() {
        mContext.enforcePermission("android.permission.WRITE_SECURE_SETTINGS", Binder.getCallingPid(), Binder.getCallingUid(), null);
    }

    /* JADX WARNING: Missing block: B:15:0x0109, code:
            r18 = r20.mVPDMap3;
     */
    /* JADX WARNING: Missing block: B:16:0x010f, code:
            monitor-enter(r18);
     */
    /* JADX WARNING: Missing block: B:19:0x011a, code:
            if (r20.mVPDMap3.size() <= 0) goto L_0x01a2;
     */
    /* JADX WARNING: Missing block: B:20:0x011c, code:
            r9 = r20.mVPDMap3.entrySet().iterator();
     */
    /* JADX WARNING: Missing block: B:22:0x012e, code:
            if (r9.hasNext() == false) goto L_0x01a2;
     */
    /* JADX WARNING: Missing block: B:23:0x0130, code:
            r16 = (com.vivo.services.security.server.VivoPermissionDeniedDialogModeThree) ((java.util.Map.Entry) r9.next()).getValue();
            printfInfo("3isCheckingPermission pid=" + r21 + " vpdThree.getCallingPid()=" + r16.getCallingPid());
     */
    /* JADX WARNING: Missing block: B:24:0x017a, code:
            if (r16.getCallingPid() != r21) goto L_0x012a;
     */
    /* JADX WARNING: Missing block: B:25:0x017c, code:
            printfInfo("3isCheckingPermission=true; pid=" + r21);
     */
    /* JADX WARNING: Missing block: B:27:0x019d, code:
            monitor-exit(r18);
     */
    /* JADX WARNING: Missing block: B:28:0x019e, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:32:0x01a2, code:
            monitor-exit(r18);
     */
    /* JADX WARNING: Missing block: B:33:0x01a3, code:
            r18 = r20.mVDDMap;
     */
    /* JADX WARNING: Missing block: B:34:0x01a9, code:
            monitor-enter(r18);
     */
    /* JADX WARNING: Missing block: B:37:0x01b4, code:
            if (r20.mVDDMap.size() <= 0) goto L_0x0236;
     */
    /* JADX WARNING: Missing block: B:38:0x01b6, code:
            r8 = r20.mVDDMap.entrySet().iterator();
     */
    /* JADX WARNING: Missing block: B:40:0x01c8, code:
            if (r8.hasNext() == false) goto L_0x0236;
     */
    /* JADX WARNING: Missing block: B:41:0x01ca, code:
            r14 = (com.vivo.services.security.server.VivoDeleteDialog) ((java.util.Map.Entry) r8.next()).getValue();
            printfInfo("delete isCheckingPermission uid=" + r13 + " vdd.getCallingUid()=" + r14.getCallingUid());
     */
    /* JADX WARNING: Missing block: B:42:0x0210, code:
            if (r14.getCallingUid() != r13) goto L_0x01c4;
     */
    /* JADX WARNING: Missing block: B:43:0x0212, code:
            printfInfo("delete isCheckingPermission=true; uid=" + r13);
     */
    /* JADX WARNING: Missing block: B:45:0x0231, code:
            monitor-exit(r18);
     */
    /* JADX WARNING: Missing block: B:46:0x0232, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:50:0x0236, code:
            monitor-exit(r18);
     */
    /* JADX WARNING: Missing block: B:52:0x0239, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isCheckingPermission(int pid) {
        int uid = Process.getUidForPid(pid);
        synchronized (this.mVPDMap) {
            int size = this.mVPDMap.size();
            printfInfo("isCheckingPermission pid=" + pid + " uid=" + uid + " size=" + size);
            if (size > 0) {
                for (Entry<String, VivoPermissionDialog> entry : this.mVPDMap.entrySet()) {
                    int callingPid = ((VivoPermissionDialog) entry.getValue()).getCallingPid();
                    int callingUid = Process.getUidForPid(callingPid);
                    printfInfo("isCheckingPermission pid=" + pid + " uid=" + uid + " callingPid=" + callingPid + " callingUid=" + callingUid);
                    if (callingUid == uid) {
                        printfInfo("isCheckingPermission=true; pid=" + pid + " uid=" + uid);
                        return true;
                    }
                }
            }
        }
    }

    public static boolean isScreenOn(Context context) {
        return ((PowerManager) context.getSystemService("power")).isScreenOn();
    }

    public static boolean isKeyguardLocked(Context context) {
        return ((KeyguardManager) context.getSystemService("keyguard")).isKeyguardLocked();
    }

    public static boolean isSuperPowerSaveOn() {
        return SystemProperties.getBoolean("sys.super_power_save", false);
    }

    private boolean isRunningForeground(String packageName) {
        long origId = Binder.clearCallingIdentity();
        ComponentName cn = null;
        try {
            cn = ((RunningTaskInfo) ((ActivityManager) mContext.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity;
            String currentPackageName = cn.getPackageName();
            return !TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(packageName);
        } catch (SecurityException e) {
            printfInfo(packageName + " requires android.permission.GET_TASKS fail,so consider isRunningForeground");
            e.printStackTrace();
            return true;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    private String getVPDMapKey(String packageName, VivoPermissionType vpType) {
        return packageName + vpType;
    }

    private String getVPDMapKey(String packageName, String permName) {
        return packageName + VivoPermissionType.getVPType(permName);
    }

    /* JADX WARNING: Missing block: B:33:0x00dc, code:
            if (r19 == false) goto L_0x0199;
     */
    /* JADX WARNING: Missing block: B:35:0x00e6, code:
            if (isRunningForeground(r22) != false) goto L_0x0113;
     */
    /* JADX WARNING: Missing block: B:36:0x00e8, code:
            printfInfo(r22 + " is requesting " + r23 + ", but is not RunningForeground, just return DENIED");
     */
    /* JADX WARNING: Missing block: B:37:0x010f, code:
            return 2;
     */
    /* JADX WARNING: Missing block: B:41:0x0113, code:
            printfInfo(r23 + " of " + r22 + " was set DENIED");
            r18 = r20.mVPC.checkConfigDeniedMode(r22, r23);
            r17 = r20.mVPC.checkConfigDeniedDialogMode(r22, r23);
     */
    /* JADX WARNING: Missing block: B:42:0x0155, code:
            if (r18 == 48) goto L_0x015d;
     */
    /* JADX WARNING: Missing block: B:44:0x015b, code:
            if (r18 != 64) goto L_0x0197;
     */
    /* JADX WARNING: Missing block: B:46:0x0161, code:
            if (r17 != 256) goto L_0x0170;
     */
    /* JADX WARNING: Missing block: B:47:0x0163, code:
            showDeniedDialogToSetting(r22, r23, r21, r8);
     */
    /* JADX WARNING: Missing block: B:49:0x016f, code:
            return 2;
     */
    /* JADX WARNING: Missing block: B:51:0x0174, code:
            if (r17 != 512) goto L_0x0182;
     */
    /* JADX WARNING: Missing block: B:52:0x0176, code:
            showDeniedDialogToChoose(r22, r23, r21, r8);
     */
    /* JADX WARNING: Missing block: B:54:0x0186, code:
            if (r17 != 768) goto L_0x016e;
     */
    /* JADX WARNING: Missing block: B:56:0x0196, code:
            return showDeniedDialogToChooseAndCountDown(r22, r23, r21, r8, r24);
     */
    /* JADX WARNING: Missing block: B:58:0x0198, code:
            return 2;
     */
    /* JADX WARNING: Missing block: B:60:0x01aa, code:
            return showWarningDialogToChoose(r22, r23, r21, r25, r8, r24);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int waitConfirmPermission(int pid, String packageName, String permName, IVivoPermissionCallback cb, int uid) {
        String currVPDMapKey = getVPDMapKey(packageName, permName);
        boolean isConfigResultDenied = false;
        synchronized (mVPSLock) {
            int configResult = this.mVPC.checkConfigPermission(packageName, permName);
            if (configResult == 2) {
                isConfigResultDenied = true;
            } else if (configResult == 1) {
                printfDebug(permName + " of " + packageName + " was set GRANTED");
                return 1;
            } else if (configResult == 0) {
                printfInfo(packageName + " has UNKNOWN PERMISSION:" + permName + ", but GRANTED");
                return 1;
            }
            if (isKeyguardLocked(mContext)) {
                printfInfo(packageName + " is requesting " + permName + ", but KeyguardLocked11, so DENIED");
                return 2;
            } else if (isSuperPowerSaveOn()) {
                printfInfo(packageName + " is requesting " + permName + ", but SuperPowerSaveOn, so DENIED");
                return 2;
            }
        }
    }

    public void removeVPD(String key) {
        synchronized (this.mVPDMap) {
            if (this.mVPDMap.containsKey(key)) {
                this.mVPDMap.remove(key);
            }
        }
    }

    public void removeVDD(String key) {
        synchronized (this.mVDDMap) {
            if (this.mVDDMap.containsKey(key)) {
                this.mVDDMap.remove(key);
            }
        }
    }

    public void removeVPD1(String key) {
        synchronized (this.mVPDMap1) {
            if (this.mVPDMap1.containsKey(key)) {
                this.mVPDMap1.remove(key);
            }
        }
    }

    public void removeVPD2(String key) {
        synchronized (this.mVPDMap2) {
            if (this.mVPDMap2.containsKey(key)) {
                this.mVPDMap2.remove(key);
            }
        }
    }

    public void removeVPD3(String key) {
        synchronized (this.mVPDMap3) {
            if (this.mVPDMap3.containsKey(key)) {
                this.mVPDMap3.remove(key);
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0076, code:
            r14 = java.lang.Thread.currentThread().toString();
     */
    /* JADX WARNING: Missing block: B:17:0x0080, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:20:0x0085, code:
            if (r4.isPermissionConfirmed() != false) goto L_0x015d;
     */
    /* JADX WARNING: Missing block: B:21:0x0087, code:
            if (r27 == null) goto L_0x0118;
     */
    /* JADX WARNING: Missing block: B:22:0x0089, code:
            r4.registerCallback(r27);
            printfInfo("0 AsyncModeConfirm: return WARNING to Client!");
     */
    /* JADX WARNING: Missing block: B:24:0x0095, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:25:0x0096, code:
            return 3;
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            printfInfo("0 Waiting ThreadInfo: " + r14 + "; " + r22 + "; " + r23);
            r4.wait(25000);
     */
    /* JADX WARNING: Missing block: B:54:0x01a3, code:
            r15 = move-exception;
     */
    /* JADX WARNING: Missing block: B:56:?, code:
            r15.printStackTrace();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int showWarningDialogToChoose(String packageName, String permName, int pid, int uid, String currVPDMapKey, IVivoPermissionCallback cb) {
        final VivoPermissionDialog vpd;
        Throwable th;
        VivoPermissionType vpType = VivoPermissionType.getVPType(permName);
        synchronized (this.mVPDMap) {
            try {
                VivoPermissionDialog vpd2 = (VivoPermissionDialog) this.mVPDMap.get(currVPDMapKey);
                if (vpd2 == null) {
                    try {
                        if (vpType == VivoPermissionType.ACCESS_LOCATION) {
                            if (this.mCheckLocationBinderTimes.containsKey(currVPDMapKey)) {
                                this.mCheckLocationBinderTimes.remove(currVPDMapKey);
                            }
                            this.mCheckLocationBinderTimes.put(currVPDMapKey, Integer.valueOf(0));
                        }
                        vpd = new VivoPermissionDialog(this, mContext, this.mUiHandler, packageName, permName, pid, currVPDMapKey);
                        this.mVPDMap.put(currVPDMapKey, vpd);
                        VivoPermissionDialog finalVPD = vpd;
                        final String str = packageName;
                        final String str2 = permName;
                        this.mUiHandler.post(new Runnable() {
                            public void run() {
                                VivoPermissionService.printfInfo("0 Showing VivoPermissionDialog: " + str + "; " + str2);
                                vpd.show();
                            }
                        });
                    } catch (Throwable th2) {
                        th = th2;
                        vpd = vpd2;
                        throw th;
                    }
                } else if (vpType != VivoPermissionType.ACCESS_LOCATION) {
                    vpd = vpd2;
                } else if (this.mCheckLocationBinderTimes.containsKey(currVPDMapKey)) {
                    int binderCheckTimes = ((Integer) this.mCheckLocationBinderTimes.get(currVPDMapKey)).intValue();
                    printfInfo("0 binderCheckTimes:" + binderCheckTimes);
                    if (binderCheckTimes >= 3) {
                        return 2;
                    }
                    this.mCheckLocationBinderTimes.remove(currVPDMapKey);
                    String str3 = currVPDMapKey;
                    this.mCheckLocationBinderTimes.put(str3, Integer.valueOf(binderCheckTimes + 1));
                    vpd = vpd2;
                } else {
                    printfError("0 check package:" + packageName + " location perm,but not have record");
                    vpd = vpd2;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (!vpd.isPermissionConfirmed()) {
            printfInfo("0 wait 25s timeout");
            vpd.handleWaitTimeOut();
        }
        int result = vpd.getPermissionResult(permName);
        printfInfo("0 Finishing ThreadInfo: " + curThreadInfo + "; " + packageName + "; " + permName + "=" + result);
        return result;
    }

    /* JADX WARNING: Missing block: B:10:0x004d, code:
            r14 = java.lang.Thread.currentThread().toString();
     */
    /* JADX WARNING: Missing block: B:11:0x0057, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:14:0x005c, code:
            if (r4.isPermissionConfirmed() != false) goto L_0x00a3;
     */
    /* JADX WARNING: Missing block: B:16:?, code:
            printfInfo("0 Waiting ThreadInfo: " + r14 + "; " + r21 + "; " + r22);
            r4.wait(25000);
     */
    /* JADX WARNING: Missing block: B:30:0x00f2, code:
            r15 = move-exception;
     */
    /* JADX WARNING: Missing block: B:32:?, code:
            r15.printStackTrace();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int showDeleteDialogToChoose(String packageName, String path, String pathPkg, String type, int pid, int uid, String currKey) {
        final VivoDeleteDialog vdd;
        Throwable th;
        synchronized (this.mVDDMap) {
            try {
                VivoDeleteDialog vdd2 = (VivoDeleteDialog) this.mVDDMap.get(currKey);
                if (vdd2 == null) {
                    try {
                        vdd = new VivoDeleteDialog(this, mContext, this.mUiHandler, packageName, path, pathPkg, type, uid, currKey);
                        this.mVDDMap.put(currKey, vdd);
                        VivoDeleteDialog finalVDD = vdd;
                        final String str = packageName;
                        final String str2 = path;
                        this.mUiHandler.post(new Runnable() {
                            public void run() {
                                VivoPermissionService.printfInfo("0 Showing VivoDeleteDialog: " + str + "; " + str2);
                                vdd.show();
                            }
                        });
                    } catch (Throwable th2) {
                        th = th2;
                        vdd = vdd2;
                        throw th;
                    }
                }
                vdd = vdd2;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        int result;
        if (!vdd.isPermissionConfirmed()) {
            printfInfo("0 wait 25s timeout");
            vdd.handleWaitTimeOut();
        }
        if (vdd.isUserClicked()) {
            result = vdd.getPermissionResult(path);
        } else {
            result = 0;
        }
        printfInfo("0 Finishing ThreadInfo: " + curThreadInfo + "; " + packageName + "; " + path + "=" + result);
        return result;
    }

    /* JADX WARNING: Missing block: B:10:0x0037, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void showDeniedDialogToSetting(final String packageName, final String permName, int pid, String currVPDMapKey) {
        Throwable th;
        VivoPermissionType vpType = VivoPermissionType.getVPType(permName);
        synchronized (this.mVPDMap1) {
            try {
                VivoPermissionDeniedDialogModeOne vpd = (VivoPermissionDeniedDialogModeOne) this.mVPDMap1.get(currVPDMapKey);
                if (vpd == null) {
                    final VivoPermissionDeniedDialogModeOne vpd2;
                    try {
                        vpd2 = new VivoPermissionDeniedDialogModeOne(this, mContext, this.mUiHandler, packageName, permName, pid, currVPDMapKey);
                        this.mVPDMap1.put(currVPDMapKey, vpd2);
                        VivoPermissionDeniedDialogModeOne finalVPD = vpd2;
                        this.mUiHandler.post(new Runnable() {
                            public void run() {
                                VivoPermissionService.printfInfo("1 Showing VivoPermissionDeniedDialogModeOne: " + packageName + "; " + permName);
                                vpd2.show();
                            }
                        });
                    } catch (Throwable th2) {
                        th = th2;
                        vpd2 = vpd;
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0037, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void showDeniedDialogToChoose(final String packageName, final String permName, int pid, String currVPDMapKey) {
        Throwable th;
        VivoPermissionType vpType = VivoPermissionType.getVPType(permName);
        synchronized (this.mVPDMap2) {
            try {
                VivoPermissionDeniedDialogModeTwo vpd = (VivoPermissionDeniedDialogModeTwo) this.mVPDMap2.get(currVPDMapKey);
                if (vpd == null) {
                    final VivoPermissionDeniedDialogModeTwo vpd2;
                    try {
                        vpd2 = new VivoPermissionDeniedDialogModeTwo(this, mContext, this.mUiHandler, packageName, permName, pid, currVPDMapKey);
                        this.mVPDMap2.put(currVPDMapKey, vpd2);
                        VivoPermissionDeniedDialogModeTwo finalVPD = vpd2;
                        this.mUiHandler.post(new Runnable() {
                            public void run() {
                                VivoPermissionService.printfInfo("2 Showing VivoPermissionDeniedDialogModeTwo: " + packageName + "; " + permName);
                                vpd2.show();
                            }
                        });
                    } catch (Throwable th2) {
                        th = th2;
                        vpd2 = vpd;
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0076, code:
            r14 = java.lang.Thread.currentThread().toString();
     */
    /* JADX WARNING: Missing block: B:17:0x0080, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:20:0x0085, code:
            if (r4.isPermissionConfirmed() != false) goto L_0x0146;
     */
    /* JADX WARNING: Missing block: B:21:0x0087, code:
            if (r26 == null) goto L_0x0101;
     */
    /* JADX WARNING: Missing block: B:22:0x0089, code:
            r4.registerCallback(r26);
            printfInfo("3 AsyncModeConfirm: return WARNING to Client!");
     */
    /* JADX WARNING: Missing block: B:24:0x0095, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:25:0x0096, code:
            return 3;
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            printfInfo("3 Waiting ThreadInfo: " + r14 + "; " + r22 + "; " + r23);
            r4.wait(25000);
     */
    /* JADX WARNING: Missing block: B:54:0x018c, code:
            r15 = move-exception;
     */
    /* JADX WARNING: Missing block: B:56:?, code:
            r15.printStackTrace();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int showDeniedDialogToChooseAndCountDown(String packageName, String permName, int pid, String currVPDMapKey, IVivoPermissionCallback cb) {
        final VivoPermissionDeniedDialogModeThree vpd;
        Throwable th;
        VivoPermissionType vpType = VivoPermissionType.getVPType(permName);
        synchronized (this.mVPDMap3) {
            try {
                VivoPermissionDeniedDialogModeThree vpd2 = (VivoPermissionDeniedDialogModeThree) this.mVPDMap3.get(currVPDMapKey);
                if (vpd2 == null) {
                    try {
                        if (vpType == VivoPermissionType.ACCESS_LOCATION) {
                            if (this.mCheckLocationBinderTimes.containsKey(currVPDMapKey)) {
                                this.mCheckLocationBinderTimes.remove(currVPDMapKey);
                            }
                            this.mCheckLocationBinderTimes.put(currVPDMapKey, Integer.valueOf(0));
                        }
                        vpd = new VivoPermissionDeniedDialogModeThree(this, mContext, this.mUiHandler, packageName, permName, pid, currVPDMapKey);
                        this.mVPDMap3.put(currVPDMapKey, vpd);
                        VivoPermissionDeniedDialogModeThree finalVPD = vpd;
                        final String str = packageName;
                        final String str2 = permName;
                        this.mUiHandler.post(new Runnable() {
                            public void run() {
                                VivoPermissionService.printfInfo("3 Showing VivoPermissionDeniedDialogModeThree: " + str + "; " + str2);
                                vpd.show();
                            }
                        });
                    } catch (Throwable th2) {
                        th = th2;
                        vpd = vpd2;
                        throw th;
                    }
                } else if (vpType != VivoPermissionType.ACCESS_LOCATION) {
                    vpd = vpd2;
                } else if (this.mCheckLocationBinderTimes.containsKey(currVPDMapKey)) {
                    int binderCheckTimes = ((Integer) this.mCheckLocationBinderTimes.get(currVPDMapKey)).intValue();
                    if (binderCheckTimes >= 3) {
                        return 2;
                    }
                    this.mCheckLocationBinderTimes.remove(currVPDMapKey);
                    String str3 = currVPDMapKey;
                    this.mCheckLocationBinderTimes.put(str3, Integer.valueOf(binderCheckTimes + 1));
                    vpd = vpd2;
                } else {
                    printfError("3 check package:" + packageName + " location perm,but not have record");
                    vpd = vpd2;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (!vpd.isPermissionConfirmed()) {
            printfInfo("3 wait 25s timeout");
            vpd.handleWaitTimeOut();
        }
        int result = vpd.getPermissionResult(permName);
        printfInfo("3 Finishing ThreadInfo: " + curThreadInfo + "; " + packageName + "; " + permName + "=" + result);
        return result;
    }

    private void registerBroadcastReceiver() {
        if (this.mVPR == null) {
            this.mVPR = new VivoPermissionReceiver(this);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter.addDataScheme("package");
        mContext.registerReceiver(this.mVPR, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        if (this.mVPR != null) {
            mContext.unregisterReceiver(this.mVPR);
            this.mVPR = null;
        }
    }

    public void dismissAllDialog() {
        if (this.mVPDMap.size() > 0) {
            HashMap<String, VivoPermissionDialog> vpdMapBackUp = (HashMap) this.mVPDMap.clone();
            for (Entry<String, VivoPermissionDialog> entry : vpdMapBackUp.entrySet()) {
                ((VivoPermissionDialog) entry.getValue()).dismiss();
            }
            vpdMapBackUp.clear();
        }
    }

    public int checkConfigDeniedMode(String packageName, String permName) {
        return this.mVPC.checkConfigDeniedMode(packageName, permName);
    }

    public void setConfigDeniedMode(String packageName, String permName, int deniedMode) {
        this.mVPC.setConfigDeniedMode(packageName, permName, deniedMode);
    }

    public void updateForPackageReplaced(String packageName) {
        this.mVPC.updateForPackageReplaced(packageName);
    }

    public void updateForPackageRemoved(String packageName) {
        this.mVPC.updateForPackageRemoved(packageName);
    }

    public void updateForPackageAdded(String packageName, boolean grantPermissions) {
        this.mVPC.updateForPackageAdded(packageName, grantPermissions);
    }

    public static void printfDebug(String msg) {
        if (!DEBUG_VPS) {
            return;
        }
        if (VivoPermissionManager.ENG || VivoPermissionManager.IS_LOG_CTRL_OPEN) {
            Slog.d(TAG, msg);
        }
    }

    public static void printfInfo(String msg) {
        if (DEBUG_VPS) {
            Slog.i(TAG, msg);
        }
    }

    public static void printfError(String msg) {
        if (DEBUG_VPS) {
            Slog.e(TAG, msg);
        }
    }

    public int checkOnePermission(String packageName, String perm, int uid) {
        return this.mVPC.checkOnePermission(packageName, perm, uid);
    }

    public boolean setOnePermission(String packageName, String perm, int uid, boolean granted) {
        enforcePermission();
        return this.mVPC.setOnePermission(packageName, perm, uid, granted);
    }

    public static boolean needHandleGroup(String perm) {
        if (perm.equals("android.permission.GET_ACCOUNTS") || perm.equals("android.permission.READ_CELL_BROADCASTS") || perm.equals("com.android.voicemail.permission.ADD_VOICEMAIL") || perm.equals("android.permission.USE_SIP") || perm.equals("android.permission.PROCESS_OUTGOING_CALLS") || perm.equals("android.permission.ANSWER_PHONE_CALLS") || perm.equals("android.permission.READ_PHONE_NUMBERS")) {
            return true;
        }
        return false;
    }

    PackageManagerInternal getPackageManagerInternalLocked() {
        if (this.mPackageManagerInt == null) {
            this.mPackageManagerInt = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        }
        return this.mPackageManagerInt;
    }

    public boolean isTestApp(String pkg) {
        try {
            PackageManagerInternal pmil = getPackageManagerInternalLocked();
            if (pmil.getClass() == null) {
                return false;
            }
            return ((Boolean) pmil.getClass().getMethod("isTestApp", new Class[]{String.class}).invoke(pmil, new Object[]{pkg})).booleanValue();
        } catch (Exception e) {
            printfError("errors " + e);
            return false;
        }
    }

    public void systemReady() {
        enforcePermission();
        this.mVPC.doForUpdate();
        mVivoPermHandler.sendEmptyMessageDelayed(3, 60000);
    }

    public boolean isDeletedSpecialSysPkg(String pkg) {
        try {
            PackageManagerInternal pmil = getPackageManagerInternalLocked();
            if (pmil.getClass() == null) {
                return false;
            }
            return ((Boolean) pmil.getClass().getMethod("isDeletedSpecialSysPkg", new Class[]{String.class}).invoke(pmil, new Object[]{pkg})).booleanValue();
        } catch (Exception e) {
            printfError("errors " + e);
            return false;
        }
    }

    public boolean needCheckPkg(PackageInfo pi) {
        if (pi == null || pi.applicationInfo == null || (!pi.applicationInfo.isSystemApp() && !pi.applicationInfo.isUpdatedSystemApp() && !pi.applicationInfo.isPrivilegedApp())) {
            return true;
        }
        return false;
    }

    private void getconfig() {
        String uri = "content://com.vivo.daemonservice.unifiedconfigprovider/configs";
        ContentResolver resolver = mContext.getContentResolver();
        AutoCloseable cursor = null;
        try {
            cursor = resolver.query(Uri.parse("content://com.vivo.daemonservice.unifiedconfigprovider/configs"), null, null, new String[]{"VivoImeiApps", "2", "2.5"}, null);
            if (cursor != null) {
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    while (!cursor.isAfterLast()) {
                        loadPkgs(cursor.getBlob(3));
                        cursor.moveToNext();
                    }
                }
            } else if (this.retryCount < 4) {
                if (mVivoPermHandler.hasMessages(3)) {
                    mVivoPermHandler.removeMessages(3);
                }
                mVivoPermHandler.sendEmptyMessageDelayed(3, CHECK_DELETE_TIME);
            }
            IoUtils.closeQuietly(cursor);
        } catch (Exception e) {
            printfError("getconfig = " + e);
            IoUtils.closeQuietly(cursor);
        } catch (Throwable th) {
            IoUtils.closeQuietly(cursor);
            throw th;
        }
        try {
            cursor = resolver.query(Uri.parse("content://com.vivo.daemonservice.unifiedconfigprovider/configs"), null, null, new String[]{"vivoimei", "1", RMNative.VERSION}, null);
            if (cursor != null) {
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    while (!cursor.isAfterLast()) {
                        loadConfig(cursor.getBlob(3));
                        cursor.moveToNext();
                    }
                }
            } else if (this.retryCount < 4) {
                if (mVivoPermHandler.hasMessages(3)) {
                    mVivoPermHandler.removeMessages(3);
                }
                mVivoPermHandler.sendEmptyMessageDelayed(3, CHECK_DELETE_TIME);
            }
            IoUtils.closeQuietly(cursor);
        } catch (Exception e2) {
            printfError("getconfig = " + e2);
            IoUtils.closeQuietly(cursor);
        } catch (Throwable th2) {
            IoUtils.closeQuietly(cursor);
            throw th2;
        }
    }

    private void loadPkgs(byte[] bytes) {
        XmlPullParserException xmle;
        Throwable th;
        Exception e;
        AutoCloseable inputStream = null;
        ArraySet<String> tempSets = new ArraySet();
        if (bytes != null) {
            try {
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                InputStream inputStream2 = new ByteArrayInputStream(bytes);
                try {
                    parser.setInput(inputStream2, "utf-8");
                    tempSets.clear();
                    for (int eventCode = parser.getEventType(); eventCode != 1; eventCode = parser.next()) {
                        switch (eventCode) {
                            case 2:
                                if (!TextUtils.equals(parser.getName(), "package")) {
                                    break;
                                }
                                String pkgName = parser.nextText();
                                if (!TextUtils.isEmpty(pkgName)) {
                                    tempSets.add(pkgName);
                                    break;
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    synchronized (this.vivoImeiSets) {
                        this.vivoImeiSets.clear();
                        this.vivoImeiSets.addAll(tempSets);
                    }
                    IoUtils.closeQuietly(inputStream2);
                } catch (XmlPullParserException e2) {
                    xmle = e2;
                    inputStream = inputStream2;
                    try {
                        printfError("XmlPullParserException = " + xmle);
                        IoUtils.closeQuietly(inputStream);
                    } catch (Throwable th2) {
                        th = th2;
                        IoUtils.closeQuietly(inputStream);
                        throw th;
                    }
                } catch (Exception e3) {
                    e = e3;
                    inputStream = inputStream2;
                    printfError("loadPkgs = " + e);
                    IoUtils.closeQuietly(inputStream);
                } catch (Throwable th3) {
                    th = th3;
                    Object inputStream3 = inputStream2;
                    IoUtils.closeQuietly(inputStream);
                    throw th;
                }
            } catch (XmlPullParserException e4) {
                xmle = e4;
                printfError("XmlPullParserException = " + xmle);
                IoUtils.closeQuietly(inputStream);
            } catch (Exception e5) {
                e = e5;
                printfError("loadPkgs = " + e);
                IoUtils.closeQuietly(inputStream);
            }
        }
    }

    private void loadConfig(byte[] bytes) {
        XmlPullParserException xmle;
        Throwable th;
        Exception e;
        AutoCloseable inputStream = null;
        if (bytes != null) {
            try {
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                InputStream inputStream2 = new ByteArrayInputStream(bytes);
                try {
                    parser.setInput(inputStream2, "utf-8");
                    for (int eventCode = parser.getEventType(); eventCode != 1; eventCode = parser.next()) {
                        switch (eventCode) {
                            case 2:
                                String name = parser.getName();
                                String count;
                                if (!TextUtils.equals(name, "shwo-dialog-one")) {
                                    if (!TextUtils.equals(name, "shwo-dialog-two")) {
                                        break;
                                    }
                                    count = parser.getAttributeValue(null, "counts");
                                    if (!TextUtils.isEmpty(count)) {
                                        this.showIMIETwoTipsLimit = Integer.parseInt(count);
                                        break;
                                    }
                                    break;
                                }
                                count = parser.getAttributeValue(null, "counts");
                                if (!TextUtils.isEmpty(count)) {
                                    this.showIMIEOneTipsLimit = Integer.parseInt(count);
                                    break;
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    IoUtils.closeQuietly(inputStream2);
                } catch (XmlPullParserException e2) {
                    xmle = e2;
                    inputStream = inputStream2;
                    try {
                        printfError("XmlPullParserException = " + xmle);
                        IoUtils.closeQuietly(inputStream);
                    } catch (Throwable th2) {
                        th = th2;
                        IoUtils.closeQuietly(inputStream);
                        throw th;
                    }
                } catch (Exception e3) {
                    e = e3;
                    inputStream = inputStream2;
                    printfError("loadPkgs = " + e);
                    IoUtils.closeQuietly(inputStream);
                } catch (Throwable th3) {
                    th = th3;
                    Object inputStream3 = inputStream2;
                    IoUtils.closeQuietly(inputStream);
                    throw th;
                }
            } catch (XmlPullParserException e4) {
                xmle = e4;
                printfError("XmlPullParserException = " + xmle);
                IoUtils.closeQuietly(inputStream);
            } catch (Exception e5) {
                e = e5;
                printfError("loadPkgs = " + e);
                IoUtils.closeQuietly(inputStream);
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0026, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isVivoImeiPkg(String pkg) {
        if (TextUtils.isEmpty(pkg)) {
            return false;
        }
        if (this.mVPC.isBuildInThirdPartApp(pkg)) {
            return true;
        }
        synchronized (this.vivoImeiSets) {
            if (isInstallTrustPkgs(pkg) && this.vivoImeiSets.contains(pkg)) {
                return true;
            }
        }
    }

    public boolean isInstallTrustPkgs(String pkg) {
        String installResource = "";
        try {
            installResource = mContext.getPackageManager().getInstallerPackageName(pkg);
        } catch (IllegalArgumentException e) {
            printfError("error-->" + e);
        }
        if (!TextUtils.isEmpty(installResource) && installtrustSets.contains(installResource)) {
            return true;
        }
        return false;
    }

    public boolean needShowImeiTipsDialogOne(VivoPermissionInfo vpi, int typeId) {
        return vpi.getTipsDialogOneMode(typeId) < this.showIMIEOneTipsLimit;
    }

    public boolean needShowImeiTipsDialogTwo(VivoPermissionInfo vpi, int typeId) {
        return vpi.getTipsDialogTwoMode(typeId) < this.showIMIETwoTipsLimit;
    }

    private boolean parserXml(File file) {
        IOException e;
        Throwable th;
        XmlPullParserException e2;
        Object fis;
        Exception e3;
        AutoCloseable fis2 = null;
        mWhitePkgs.clear();
        try {
            FileInputStream fis3 = new FileInputStream(file);
            try {
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(fis3, StandardCharsets.UTF_8.name());
                for (int eventCode = parser.getEventType(); eventCode != 1; eventCode = parser.next()) {
                    switch (eventCode) {
                        case 2:
                            if (!TextUtils.equals("package", parser.getName())) {
                                break;
                            }
                            String whitePkg = parser.getAttributeValue(null, "name");
                            mWhitePkgs.add(whitePkg);
                            printfDebug(" whitePkg=" + whitePkg);
                            break;
                        default:
                            break;
                    }
                }
                IoUtils.closeQuietly(fis3);
                return true;
            } catch (IOException e4) {
                e = e4;
                fis2 = fis3;
                try {
                    printfError("Failed parserXml +" + e);
                    mWhitePkgs.clear();
                    IoUtils.closeQuietly(fis2);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(fis2);
                    throw th;
                }
            } catch (XmlPullParserException e5) {
                e2 = e5;
                fis2 = fis3;
                printfError("Failed parserXml +" + e2);
                mWhitePkgs.clear();
                IoUtils.closeQuietly(fis2);
                return false;
            } catch (Exception e6) {
                e3 = e6;
                fis2 = fis3;
                printfError("Failed parserXml +" + e3);
                mWhitePkgs.clear();
                IoUtils.closeQuietly(fis2);
                return false;
            } catch (Throwable th3) {
                th = th3;
                fis2 = fis3;
                IoUtils.closeQuietly(fis2);
                throw th;
            }
        } catch (IOException e7) {
            e = e7;
            printfError("Failed parserXml +" + e);
            mWhitePkgs.clear();
            IoUtils.closeQuietly(fis2);
            return false;
        } catch (XmlPullParserException e8) {
            e2 = e8;
            printfError("Failed parserXml +" + e2);
            mWhitePkgs.clear();
            IoUtils.closeQuietly(fis2);
            return false;
        } catch (Exception e9) {
            e3 = e9;
            printfError("Failed parserXml +" + e3);
            mWhitePkgs.clear();
            IoUtils.closeQuietly(fis2);
            return false;
        }
    }
}
