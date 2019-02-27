package com.vivo.services.security.client;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Handler;
import java.util.List;
import vivo.app.VivoFrameworkFactory;
import vivo.app.security.AbsVivoPermissionManager;
import vivo.app.security.IVivoPermissionService;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS_PART)
public class VivoPermissionManager {
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
    public static final String VIVO_PERMISSION_SERVICE = "vivo_permission_service";
    private static AbsVivoPermissionManager mInstance;
    private static VivoPermissionManager mVPM;

    public static void printfDebug(String msg) {
        if (getInstance() != null) {
            getInstance().printfDebug(msg);
        }
    }

    public static void printfInfo(String msg) {
        if (getInstance() != null) {
            getInstance().printfInfo(msg);
        }
    }

    public static void printfError(String msg) {
        if (getInstance() != null) {
            getInstance().printfError(msg);
        }
    }

    public static void debugPerformance(String msg) {
        if (getInstance() != null) {
            getInstance().debugPerformance(msg);
        }
    }

    public static void debugPackages(String[] packages) {
        if (getInstance() != null) {
            getInstance().debugPackages(packages);
        }
    }

    public static void debugStack(String divider) {
        if (getInstance() != null) {
            getInstance().debugStack(divider);
        }
    }

    public static IVivoPermissionService getDefaultVPS() {
        if (getInstance() != null) {
            return getInstance().getDefaultVPS();
        }
        return null;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public static VivoPermissionManager getVPM(Context context) {
        if (mVPM != null) {
            return mVPM;
        }
        mVPM = new VivoPermissionManager();
        return mVPM;
    }

    public static float getOSVersion() {
        if (getInstance() != null) {
            return getInstance().getOSVersion();
        }
        return Float.valueOf(1.0f).floatValue();
    }

    public static String getProjectName() {
        if (getInstance() != null) {
            return getInstance().getProjectName();
        }
        return "";
    }

    public static boolean isSecurityLevelOne() {
        if (getInstance() != null) {
            return getInstance().isSecurityLevelOne();
        }
        return false;
    }

    public static boolean isNetEntry() {
        if (getInstance() != null) {
            return getInstance().isNetEntry();
        }
        return false;
    }

    public static boolean isCMCCTest() {
        if (getInstance() != null) {
            return getInstance().isCMCCTest();
        }
        return false;
    }

    public static boolean isOverSeas() {
        if (getInstance() != null) {
            return getInstance().isOverSeas();
        }
        return false;
    }

    public static boolean needMonitorBuildInApps() {
        if (getInstance() != null) {
            return getInstance().needMonitorBuildInApps();
        }
        return false;
    }

    public static boolean needMonitorSystemApp() {
        if (getInstance() != null) {
            return getInstance().needMonitorSystemApp();
        }
        return false;
    }

    public static boolean needMonitorSystemUid() {
        if (getInstance() != null) {
            return getInstance().needMonitorSystemUid();
        }
        return false;
    }

    public static boolean configInSA() {
        if (getInstance() != null) {
            return getInstance().configInSA();
        }
        return false;
    }

    public static boolean needVPM() {
        if (getInstance() != null) {
            return getInstance().needVPM();
        }
        return true;
    }

    @Deprecated
    public static boolean checkPermission(Context context, String permission) {
        if (getInstance() != null) {
            return getInstance().checkPermission(context, permission);
        }
        return true;
    }

    @Deprecated
    public static boolean checkPermissionExt(String permission) {
        if (getInstance() != null) {
            return getInstance().checkPermissionExt(permission);
        }
        return true;
    }

    public static boolean checkCallingVivoPermission(String permission) {
        if (getInstance() != null) {
            return getInstance().checkCallingVivoPermission(permission);
        }
        return true;
    }

    public static boolean checkVivoPermission(String permission, int pid, int uid) {
        if (getInstance() != null) {
            return getInstance().checkVivoPermission(permission, pid, uid);
        }
        return true;
    }

    public static int checkVivoPermissionWithCB(String permission, int pid, int uid, IVivoPermissionCallback cb) {
        if (getInstance() != null) {
            return getInstance().checkVivoPermissionWithCB(permission, pid, uid, cb);
        }
        return 1;
    }

    public static boolean isCheckingPermission(int pid) {
        if (getInstance() != null) {
            return getInstance().isCheckingPermission(pid);
        }
        return false;
    }

    public static PackageInfo getCallingPackageInfo(Context context, int callingUid) {
        if (getInstance() != null) {
            return getInstance().getCallingPackageInfo(context, callingUid);
        }
        return null;
    }

    public static ApplicationInfo getCallingAppInfo(Context context) {
        if (getInstance() != null) {
            return getInstance().getCallingAppInfo(context);
        }
        return null;
    }

    public static String getCallingAppName(Context context) {
        if (getInstance() != null) {
            return getInstance().getCallingAppName(context);
        }
        return null;
    }

    public static boolean isSystemAppCalling(Context context) {
        if (getInstance() != null) {
            return getInstance().isSystemAppCalling(context);
        }
        return true;
    }

    public static boolean isSystemApp(ApplicationInfo ai) {
        if (getInstance() != null) {
            return getInstance().isSystemApp(ai);
        }
        return true;
    }

    public static boolean isVivoApp(Context context, String packageName) {
        if (context == null) {
            throw new IllegalArgumentException("Invalid Argument context");
        } else if (packageName == null || getInstance() == null) {
            return false;
        } else {
            return getInstance().isVivoApp(context, packageName);
        }
    }

    public static int getCallingAppPermisson(Context context, VivoPermissionType vpType) {
        if (getInstance() != null) {
            return getInstance().getCallingAppPermisson(context, vpType);
        }
        return 1;
    }

    public static ActivityInfo resolveActivityForVivoPermission(Context context, Intent intent, ActivityInfo aInfo) {
        if (getInstance() != null) {
            return getInstance().resolveActivityForVivoPermission(context, intent, aInfo);
        }
        return aInfo;
    }

    public VivoPermissionManager(Context context, Handler uiHandler) {
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public List<VivoPermissionInfo> getSpecifiedPermAppList(int vpTypeId) {
        if (getInstance() != null) {
            return getInstance().getSpecifiedPermAppList(vpTypeId);
        }
        return null;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public List<VivoPermissionInfo> getTrustedAppList() {
        if (getInstance() != null) {
            return getInstance().getTrustedAppList();
        }
        return null;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public List<VivoPermissionInfo> getMonitorAppList() {
        if (getInstance() != null) {
            return getInstance().getMonitorAppList();
        }
        return null;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public VivoPermissionInfo getAppPermission(String packageName) {
        if (getInstance() != null) {
            return getInstance().getAppPermission(packageName);
        }
        return null;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public void setAppPermissionExt(VivoPermissionInfo vpi) {
        if (getInstance() != null) {
            getInstance().setAppPermissionExt(vpi);
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public void setAppPermission(String packageName, int vpTypeId, int result) {
        if (getInstance() != null) {
            getInstance().setAppPermission(packageName, vpTypeId, result);
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public void setWhiteListApp(String packageName, boolean enable) {
        if (getInstance() != null) {
            getInstance().setWhiteListApp(packageName, enable);
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public void setBlackListApp(String packageName, boolean enable) {
        if (getInstance() != null) {
            getInstance().setBlackListApp(packageName, enable);
        }
    }

    public static void noteStartActivityProcess(String packageName) {
        if (getInstance() != null) {
            getInstance().noteStartActivityProcess(packageName);
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public boolean isBuildInThirdPartApp(String packageName) {
        if (getInstance() != null) {
            return getInstance().isBuildInThirdPartApp(packageName);
        }
        return false;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public boolean isVivoImeiPkg(String packageName) {
        if (getInstance() != null) {
            return getInstance().isVivoImeiPkg(packageName);
        }
        return false;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public int getVPMVersion() {
        if (getInstance() != null) {
            return getInstance().getVPMVersion();
        }
        return 1;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public int getVPMDataBaseState() {
        if (getInstance() != null) {
            return getInstance().getVPMDataBaseState();
        }
        return 0;
    }

    public static AbsVivoPermissionManager getInstance() {
        if (mInstance != null) {
            return mInstance;
        }
        if (VivoFrameworkFactory.getFrameworkFactoryImpl() == null) {
            return null;
        }
        mInstance = VivoFrameworkFactory.getFrameworkFactoryImpl().getVivoPermissionManager();
        return mInstance;
    }
}
