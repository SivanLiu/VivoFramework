package com.vivo.framework.security;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Singleton;
import com.vivo.common.autobrightness.AblConfig;
import com.vivo.common.autobrightness.StateInfo;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.common.provider.Weather;
import com.vivo.services.security.client.IVivoPermissionCallback;
import com.vivo.services.security.client.VivoPermissionInfo;
import com.vivo.services.security.client.VivoPermissionType;
import java.util.List;
import vivo.app.security.AbsVivoPermissionManager;
import vivo.app.security.IVivoPermissionService;
import vivo.app.security.IVivoPermissionService.Stub;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS_PART)
public class VivoPermissionManager extends AbsVivoPermissionManager {
    public static final String ACTION_KEY_PACKAGE = "package";
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final String ACTION_PACKAGE_PERMISSION_ADDED = "com.vivo.services.security.client.PACKAGE_PERMISSION_ADDED";
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final String ACTION_PACKAGE_PERMISSION_REMOVED = "com.vivo.services.security.client.PACKAGE_PERMISSION_REMOVED";
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final String ACTION_PACKAGE_PERMISSION_REPLACED = "com.vivo.services.security.client.PACKAGE_PERMISSION_REPLACED";
    public static final String CLASS_PERMISSION_MANAGER = "com.vivo.services.security.client.VivoPermissionManager";
    public static final String CLASS_PERMISSION_SERVICE = "com.vivo.services.security.server.VivoPermissionService";
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final int DATABASE_STATE_CREAT = 2;
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final int DATABASE_STATE_NORMAL = 1;
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final int DATABASE_STATE_UNKNOWN = 0;
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final int DATABASE_STATE_UPGRADE = 3;
    private static final boolean DEBUG_NEED_VPM = SystemProperties.get("persist.sys.debug.vpm_need", "yes").equals("yes");
    private static final boolean DEBUG_PACKAGES = SystemProperties.get("persist.sys.debug.vpm_pa", "no").equals("yes");
    private static final boolean DEBUG_PERFORMANCE = SystemProperties.get("persist.sys.debug.vpm_pe", "no").equals("yes");
    private static final boolean DEBUG_STACK = SystemProperties.get("persist.sys.debug.vpm_s", "no").equals("yes");
    private static final boolean DEBUG_VPM = SystemProperties.get("persist.sys.debug.vpm", "no").equals("yes");
    public static final boolean ENG = AbsVivoPermissionManager.ENG;
    public static final String INTENT_KEY_ACTIVITY = "ResolvedActivityName";
    public static final String INTENT_KEY_FLAG = "ResolvedIntentFlag";
    public static final String INTENT_KEY_PACKAGE = "ResolvedPackageName";
    public static final boolean IS_LOG_CTRL_OPEN = AbsVivoPermissionManager.IS_LOG_CTRL_OPEN;
    public static final String KEY_VIVO_LOG_CTRL = "persist.sys.log.ctrl";
    public static final String PROP_BUILD_TYPE = AbsVivoPermissionManager.PROP_BUILD_TYPE;
    public static final String PROP_MONITOR_BUILD_IN_APPS = "persist.sys.monitor.apps";
    public static final String PROP_MONITOR_SYSTEM_APP = "persist.sys.monitor.system_app";
    public static final String PROP_MONITOR_SYSTEM_UID = "persist.sys.monitor.system_uid";
    public static final String PROP_OS_VERSION = AbsVivoPermissionManager.PROP_OS_VERSION;
    private static final boolean PROP_OS_VERSION_DEBUG = "1".equals(SystemProperties.get("persist.sys.debug.vpmos", "0"));
    private static final String TAG = "VPM";
    public static final String VIVO_PERMISSION_SERVICE = "vivo_permission_service";
    private static String mCMCCTest = null;
    private static VivoPermissionManager mInstance;
    private static String mMonitorBuildInApps = null;
    private static String mMonitorSystemApp = null;
    private static String mMonitorSystemUid = null;
    private static String mNeedVPM = null;
    private static String mNetEntry = null;
    private static Float mOSVer = null;
    private static String mOverSeas = null;
    private static String mProjectName = null;
    private static VivoPermissionManager mVPM = null;
    private static final Singleton<IVivoPermissionService> sDefaultVPS = new Singleton<IVivoPermissionService>() {
        protected IVivoPermissionService create() {
            return Stub.asInterface(ServiceManager.getService(VivoPermissionManager.VIVO_PERMISSION_SERVICE));
        }
    };
    private Context mContext = null;
    private PackageManager mPackageManager = null;
    private Handler mUiHandler = null;

    public void printfDebug(String msg) {
        if (DEBUG_VPM) {
            Log.d(TAG, msg);
        }
    }

    public void printfInfo(String msg) {
        Log.i(TAG, msg);
    }

    public void printfError(String msg) {
        if (needVPM()) {
            Log.e(TAG, msg);
        }
    }

    public void debugPerformance(String msg) {
        if (DEBUG_PERFORMANCE) {
            printfDebug(msg);
        }
    }

    public void debugPackages(String[] packages) {
        if (DEBUG_PACKAGES && packages != null && packages.length > 0) {
            for (String str : packages) {
                printfDebug("package=" + str);
            }
        }
    }

    public void debugStack(String divider) {
        if (DEBUG_STACK) {
            printfDebug("===============Begin:" + divider + "===============");
            for (StackTraceElement s : (StackTraceElement[]) Thread.getAllStackTraces().get(Thread.currentThread())) {
                printfDebug(s.toString());
            }
            printfDebug("===============End:" + divider + "===============");
        }
    }

    public IVivoPermissionService getDefaultVPS() {
        if (needVPM()) {
            return (IVivoPermissionService) sDefaultVPS.get();
        }
        return null;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public VivoPermissionManager getVPM() {
        if (mVPM == null) {
            mVPM = getInstance();
        }
        return mVPM;
    }

    public float getOSVersion() {
        if (mOSVer != null) {
            return mOSVer.floatValue();
        }
        String osVer = PROP_OS_VERSION;
        if (osVer != null) {
            String[] osVerSplit = osVer.split("_");
            if (osVerSplit != null) {
                try {
                    if (osVerSplit.length >= 2) {
                        mOSVer = Float.valueOf(osVerSplit[1]);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            mOSVer = Float.valueOf(osVer);
        }
        if (mOSVer == null) {
            mOSVer = Float.valueOf(1.0f);
        }
        if (PROP_OS_VERSION_DEBUG) {
            mOSVer = Float.valueOf(3.0f);
        }
        printfInfo("getOSVersion mOSVer=" + mOSVer);
        return mOSVer.floatValue();
    }

    public String getProjectName() {
        if (mProjectName != null && mProjectName.length() > 0) {
            return mProjectName;
        }
        mProjectName = SystemProperties.get("ro.vivo.product.model", Events.DEFAULT_SORT_ORDER);
        if (mProjectName == null || mProjectName.length() <= 0) {
            mProjectName = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, Events.DEFAULT_SORT_ORDER);
            printfInfo("getProjectName ro.product.model.bbk=" + mProjectName);
            return mProjectName;
        }
        printfInfo("getProjectName ro.vivo.product.model=" + mProjectName);
        return mProjectName;
    }

    public boolean isSecurityLevelOne() {
        return false;
    }

    public boolean isNetEntry() {
        if (mNetEntry != null && mNetEntry.length() > 0) {
            return "yes".equals(mNetEntry);
        } else {
            mNetEntry = SystemProperties.get("ro.vivo.net.entry", "no");
            if ("yes".equals(mNetEntry)) {
                printfInfo("isNetEntry yes ro.vivo.net.entry=" + mNetEntry);
                return true;
            }
            mNetEntry = SystemProperties.get("ro.product.net.entry.bbk", "no");
            if ("yes".equals(mNetEntry)) {
                printfInfo("isNetEntry yes ro.product.net.entry.bbk=" + mNetEntry);
                return true;
            }
            printfInfo("isNetEntry no");
            return false;
        }
    }

    public boolean isCMCCTest() {
        if (mCMCCTest != null && mCMCCTest.length() > 0) {
            return "cmcc".equals(mCMCCTest) || "yes".equals(mCMCCTest);
        } else {
            mCMCCTest = SystemProperties.get("ro.vivo.op.entry", "null");
            if ("cmcc".equals(mCMCCTest)) {
                printfInfo("isCMCCTest yes ro.vivo.op.entry=" + mCMCCTest);
                return true;
            }
            mCMCCTest = SystemProperties.get("ro.cmcc.test", "no");
            if ("yes".equals(mCMCCTest)) {
                printfInfo("isCMCCTest yes ro.cmcc.test=" + mCMCCTest);
                return true;
            }
            printfInfo("isCMCCTest no");
            return false;
        }
    }

    public boolean isOverSeas() {
        if (mOverSeas != null && mOverSeas.length() > 0) {
            return "yes".equals(mOverSeas);
        } else {
            mOverSeas = SystemProperties.get("ro.vivo.product.overseas", "no");
            if ("yes".equals(mOverSeas)) {
                printfInfo("isOverSeas yes ro.vivo.product.overseas=" + mOverSeas);
                return true;
            }
            printfInfo("isOverSeas no");
            return false;
        }
    }

    public boolean isRuntimePermission(Context context, String permission) {
        boolean z = true;
        try {
            if ((context.getPackageManager().getPermissionInfo(permission, 0).protectionLevel & 15) != 1) {
                z = false;
            }
            return z;
        } catch (NameNotFoundException nnfe) {
            Log.e(TAG, "No such permission: " + permission, nnfe);
            return false;
        }
    }

    public boolean needMonitorBuildInApps() {
        if (mMonitorBuildInApps != null && mMonitorBuildInApps.length() > 0) {
            return "yes".equals(mMonitorBuildInApps);
        } else {
            mMonitorBuildInApps = SystemProperties.get(PROP_MONITOR_BUILD_IN_APPS, "no");
            printfInfo("needMonitorBuildInAppspersist.sys.monitor.apps=" + mMonitorBuildInApps);
            return "yes".equals(mMonitorBuildInApps);
        }
    }

    public boolean needMonitorSystemApp() {
        if (mMonitorSystemApp != null && mMonitorSystemApp.length() > 0) {
            return "yes".equals(mMonitorSystemApp);
        } else {
            mMonitorSystemApp = SystemProperties.get(PROP_MONITOR_SYSTEM_APP, "no");
            printfInfo("needMonitorSystemApppersist.sys.monitor.system_app=" + mMonitorSystemApp);
            return "yes".equals(mMonitorSystemApp);
        }
    }

    public boolean needMonitorSystemUid() {
        if (mMonitorSystemUid != null && mMonitorSystemUid.length() > 0) {
            return "yes".equals(mMonitorSystemUid);
        } else {
            mMonitorSystemUid = SystemProperties.get(PROP_MONITOR_SYSTEM_UID, "no");
            printfInfo("needMonitorSystemUidpersist.sys.monitor.system_uid=" + mMonitorSystemUid);
            return "yes".equals(mMonitorSystemUid);
        }
    }

    public boolean configInSA() {
        if (getProjectName().equals("PD1304B")) {
            return true;
        }
        return false;
    }

    public boolean needVPM() {
        if (mNeedVPM != null && mNeedVPM.length() > 0) {
            return "yes".equals(mNeedVPM);
        } else {
            if (!ENG || (DEBUG_NEED_VPM ^ 1) == 0) {
                String project = getProjectName();
                if (project.equals("PD1224CT") || project.equals("PD1224CW") || project.equals("PD1224CW_EX") || project.equals("PD1304T") || project.equals("PD1304W") || project.equals("PD1311TG3")) {
                    printfInfo("needVPM no project=" + project);
                    mNeedVPM = "no";
                    return false;
                }
                float osVer = getOSVersion();
                if (!isOverSeas() || osVer < 3.0f) {
                    printfInfo("needVPM yes project=" + project + " osVer=" + osVer);
                    mNeedVPM = "yes";
                    return true;
                }
                printfInfo("needVPM no isOverSeas and under 3.0");
                mNeedVPM = "no";
                return false;
            }
            printfInfo("needVPM debug mode---------------------------------");
            mNeedVPM = "no";
            return false;
        }
    }

    @Deprecated
    public boolean checkPermission(Context context, String permission) {
        return checkCallingVivoPermission(permission);
    }

    @Deprecated
    public boolean checkPermissionExt(String permission) {
        return checkCallingVivoPermission(permission);
    }

    public static boolean checkCallingDelete(String path, String pkg, String type, String key) {
        if (path != null && path.length() > 0) {
            int uid = Binder.getCallingUid();
            int pid = Binder.getCallingPid();
            try {
                IVivoPermissionService vps = getInstance().getDefaultVPS();
                if (vps != null) {
                    return vps.checkDelete(path, pkg, type, key, uid, pid);
                }
                getInstance().printfError("VivoPermissionService is null!");
                return true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean checkCallingVivoPermission(String permission) {
        return checkVivoPermission(permission, Binder.getCallingPid(), Binder.getCallingUid());
    }

    public boolean checkVivoPermission(String permission, int pid, int uid) {
        return checkVivoPermissionWithCB(permission, pid, uid, null) == 1;
    }

    public int checkVivoPermissionWithCB(String permission, int pid, int uid, IVivoPermissionCallback cb) {
        debugStack("checkVivoPermissionWithCB:permission=" + permission);
        debugPerformance("debugPerformance Begin:checkVivoPermissionWithCB");
        enforcePermissionName(permission);
        int result = -1;
        try {
            IVivoPermissionService vps = getDefaultVPS();
            if (vps == null) {
                printfError("VivoPermissionService is null!");
                return 1;
            }
            result = vps.checkPermission(permission, pid, uid, cb);
            debugPerformance("debugPerformance End:checkVivoPermissionWithCB");
            return result;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isCheckingPermission(int pid) {
        debugStack("isCheckingPermission:pid=" + pid);
        try {
            IVivoPermissionService vps = getDefaultVPS();
            if (vps != null) {
                return vps.isCheckingPermission(pid);
            }
            printfError("VivoPermissionService is null!");
            return false;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public PackageInfo getCallingPackageInfo(Context context, int callingUid) {
        PackageManager pm = context.getPackageManager();
        String[] packageNames = pm.getPackagesForUid(callingUid);
        if (packageNames == null || packageNames.length == 0) {
            printfError("Can't get calling app package name!");
            return null;
        }
        debugPackages(packageNames);
        PackageInfo appInfo = null;
        int size = packageNames.length;
        int i = 0;
        while (i < size) {
            try {
                appInfo = pm.getPackageInfo(packageNames[i], 64);
                if (appInfo != null) {
                    printfDebug("Success:getPackageInfo()-->packageName=" + packageNames[i]);
                    break;
                }
                i++;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (appInfo == null) {
            printfError("Can't get calling app package info!");
        }
        return appInfo;
    }

    public ApplicationInfo getCallingAppInfo(Context context) {
        PackageInfo pi = getCallingPackageInfo(context, Binder.getCallingUid());
        if (pi == null) {
            return null;
        }
        return pi.applicationInfo;
    }

    public String getCallingAppName(Context context) {
        ApplicationInfo ai = getCallingAppInfo(context);
        if (ai == null) {
            return null;
        }
        return ai.loadLabel(context.getPackageManager()).toString();
    }

    public boolean isSystemAppCalling(Context context) {
        int callingUid = Binder.getCallingUid();
        if (callingUid == 0 || callingUid == Weather.WEATHERVERSION_ROM_2_0) {
            return true;
        }
        ApplicationInfo ai = getCallingAppInfo(context);
        if (ai == null) {
            return true;
        }
        return isSystemApp(ai);
    }

    public boolean isSystemApp(ApplicationInfo ai) {
        if (ai == null) {
            throw new IllegalArgumentException("Invalid ApplicationInfo");
        } else if ((ai.flags & 1) == 0 && (ai.flags & StateInfo.STATE_BIT_BATTERY) == 0) {
            return false;
        } else {
            return true;
        }
    }

    /* JADX WARNING: Missing block: B:15:0x0035, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isVivoApp(Context context, String packageName) {
        boolean z = true;
        if (TextUtils.equals(packageName, "com.mobile.iroaming")) {
            return false;
        }
        PackageManager pm = context.getPackageManager();
        if (pm != null) {
            PackageInfo pi = null;
            long identity = Binder.clearCallingIdentity();
            try {
                pi = pm.getPackageInfo(packageName, 0);
                if ((pi != null && pi.applicationInfo != null && (pi.applicationInfo.isSystemApp() || pi.applicationInfo.isUpdatedSystemApp())) || pm.checkSignatures("android", packageName) == 0 || pm.checkSignatures("com.android.providers.contacts", packageName) == 0 || pm.checkSignatures("com.android.providers.media", packageName) == 0) {
                    return z;
                }
                return false;
            } catch (NameNotFoundException e) {
                z = "  error = ";
                printfError("No package: " + packageName + z + e);
                return false;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
        return false;
    }

    public int getCallingAppPermisson(Context context, VivoPermissionType vpType) {
        ApplicationInfo ai = getCallingAppInfo(context);
        if (ai == null || ai.packageName == null || ai.packageName.length() <= 0) {
            return 1;
        }
        VivoPermissionManager vpm = getVPM();
        if (vpm == null) {
            return 1;
        }
        VivoPermissionInfo vpi = vpm.getAppPermission(ai.packageName);
        if (vpi == null) {
            return 1;
        }
        int result = vpi.getPermissionResult(vpType.getVPTypeId());
        if (!(result == 2 || result == 1 || result == 3)) {
            printfError("getCallingAppPermisson-->error result=" + result);
            result = 1;
        }
        return result;
    }

    public ActivityInfo resolveActivityForVivoPermission(Context context, Intent intent, ActivityInfo aInfo) {
        return aInfo;
    }

    public VivoPermissionManager(Context context, Handler uiHandler) {
        this.mContext = context;
        this.mUiHandler = uiHandler;
        this.mPackageManager = context.getPackageManager();
    }

    private static void enforcePackageName(String packageName) {
        if (packageName == null || packageName.length() == 0) {
            throw new IllegalArgumentException("Invalid packageName");
        }
    }

    private static void enforcePermissionName(String permName) {
        if (permName == null || permName.length() == 0) {
            throw new IllegalArgumentException("Invalid PermissionName");
        }
    }

    private static void enforceValidTypeId(int vpTypeId) {
        if (!VivoPermissionType.isValidTypeId(vpTypeId)) {
            throw new IllegalArgumentException("Invalid vpTypeId");
        }
    }

    private static void enforcePermissionResult(int premResult) {
        if (!VivoPermissionInfo.isValidPermissionResult(premResult)) {
            throw new IllegalArgumentException("Invalid vpTypeId");
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public List<VivoPermissionInfo> getSpecifiedPermAppList(int vpTypeId) {
        enforceValidTypeId(vpTypeId);
        List<VivoPermissionInfo> result = null;
        try {
            IVivoPermissionService vps = getDefaultVPS();
            if (vps == null) {
                printfError("VivoPermissionService is null!");
                return null;
            }
            result = vps.getSpecifiedPermAppList(vpTypeId);
            return result;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public List<VivoPermissionInfo> getTrustedAppList() {
        List<VivoPermissionInfo> result = null;
        try {
            IVivoPermissionService vps = getDefaultVPS();
            if (vps == null) {
                printfError("VivoPermissionService is null!");
                return null;
            }
            result = vps.getTrustedAppList();
            return result;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public List<VivoPermissionInfo> getMonitorAppList() {
        List<VivoPermissionInfo> result = null;
        try {
            IVivoPermissionService vps = getDefaultVPS();
            if (vps == null) {
                printfError("VivoPermissionService is null!");
                return null;
            }
            result = vps.getMonitorAppList();
            return result;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public VivoPermissionInfo getAppPermission(String packageName) {
        enforcePackageName(packageName);
        VivoPermissionInfo result = null;
        try {
            IVivoPermissionService vps = getDefaultVPS();
            if (vps == null) {
                printfError("VivoPermissionService is null!");
                return null;
            }
            result = vps.getAppPermission(packageName);
            return result;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public void setAppPermissionExt(VivoPermissionInfo vpi) {
        if (vpi != null) {
            try {
                IVivoPermissionService vps = getDefaultVPS();
                if (vps == null) {
                    printfError("VivoPermissionService is null!");
                } else {
                    vps.setAppPermissionExt(vpi);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public void setAppPermission(String packageName, int vpTypeId, int result) {
        enforcePackageName(packageName);
        enforceValidTypeId(vpTypeId);
        enforcePermissionResult(result);
        try {
            IVivoPermissionService vps = getDefaultVPS();
            if (vps == null) {
                printfError("VivoPermissionService is null!");
            } else {
                vps.setAppPermission(packageName, vpTypeId, result);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public void setWhiteListApp(String packageName, boolean enable) {
        enforcePackageName(packageName);
        try {
            IVivoPermissionService vps = getDefaultVPS();
            if (vps == null) {
                printfError("VivoPermissionService is null!");
            } else {
                vps.setWhiteListApp(packageName, enable);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public void setBlackListApp(String packageName, boolean enable) {
        enforcePackageName(packageName);
        try {
            IVivoPermissionService vps = getDefaultVPS();
            if (vps == null) {
                printfError("VivoPermissionService is null!");
            } else {
                vps.setBlackListApp(packageName, enable);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void noteStartActivityProcess(String packageName) {
        try {
            IVivoPermissionService vps = getDefaultVPS();
            if (vps == null) {
                printfError("VivoPermissionService is null!");
            } else {
                vps.noteStartActivityProcess(packageName);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public boolean isBuildInThirdPartApp(String packageName) {
        enforcePackageName(packageName);
        boolean result = false;
        try {
            IVivoPermissionService vps = getDefaultVPS();
            if (vps == null) {
                printfError("VivoPermissionService is null!");
                return false;
            }
            result = vps.isBuildInThirdPartApp(packageName);
            return result;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isVivoImeiPkg(String packageName) {
        enforcePackageName(packageName);
        boolean result = false;
        try {
            IVivoPermissionService vps = getDefaultVPS();
            if (vps == null) {
                printfError("VivoPermissionService is null!");
                return false;
            }
            result = vps.isVivoImeiPkg(packageName);
            return result;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public int getVPMVersion() {
        int version = 1;
        try {
            IVivoPermissionService vps = getDefaultVPS();
            if (vps == null) {
                printfError("VivoPermissionService is null!");
                return 1;
            }
            version = vps.getVPMVersion();
            return version;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public int getVPMDataBaseState() {
        int state = 0;
        try {
            IVivoPermissionService vps = getDefaultVPS();
            if (vps == null) {
                printfError("VivoPermissionService is null!");
                return 0;
            }
            state = vps.getVPMDataBaseState();
            return state;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public int checkOnePermission(String packageName, String perm, int uid) {
        if (packageName == null || packageName.length() == 0 || perm == null || perm.length() == 0) {
            printfError("packageName or perm is null!");
            return 1;
        }
        try {
            IVivoPermissionService vps = getDefaultVPS();
            if (vps != null) {
                return vps.checkOnePermission(packageName, perm, uid);
            }
            printfError("VivoPermissionService is null!");
            return 1;
        } catch (RemoteException e) {
            e.printStackTrace();
            return 1;
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public boolean setOnePermission(String packageName, String perm, int uid, boolean granted) {
        if (packageName == null || packageName.length() == 0 || perm == null || perm.length() == 0) {
            printfError("packageName or perm is null!");
            return false;
        }
        try {
            IVivoPermissionService vps = getDefaultVPS();
            if (vps != null) {
                return vps.setOnePermission(packageName, perm, uid, granted);
            }
            printfError("VivoPermissionService is null!");
            return false;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static VivoPermissionManager getInstance() {
        if (mInstance != null) {
            return mInstance;
        }
        mInstance = new VivoPermissionManager();
        return mInstance;
    }
}
