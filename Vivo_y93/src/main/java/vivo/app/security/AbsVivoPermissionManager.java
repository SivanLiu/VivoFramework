package vivo.app.security;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.SystemProperties;
import com.vivo.services.security.client.IVivoPermissionCallback;
import com.vivo.services.security.client.VivoPermissionInfo;
import com.vivo.services.security.client.VivoPermissionType;
import java.util.List;

public abstract class AbsVivoPermissionManager {
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
    public static final boolean ENG = (!"eng".equals(PROP_BUILD_TYPE) ? "branddebug".equals(PROP_BUILD_TYPE) : true);
    public static final String INTENT_KEY_ACTIVITY = "ResolvedActivityName";
    public static final String INTENT_KEY_FLAG = "ResolvedIntentFlag";
    public static final String INTENT_KEY_PACKAGE = "ResolvedPackageName";
    public static final boolean IS_LOG_CTRL_OPEN = SystemProperties.get(KEY_VIVO_LOG_CTRL, "no").equals("yes");
    public static final String KEY_VIVO_LOG_CTRL = "persist.sys.log.ctrl";
    public static final String PROP_BUILD_TYPE = SystemProperties.get("ro.build.type", "user");
    public static final String PROP_MONITOR_BUILD_IN_APPS = "persist.sys.monitor.apps";
    public static final String PROP_MONITOR_SYSTEM_APP = "persist.sys.monitor.system_app";
    public static final String PROP_MONITOR_SYSTEM_UID = "persist.sys.monitor.system_uid";
    public static final String PROP_OS_VERSION = SystemProperties.get("ro.vivo.os.version", "null");
    public static final String VIVO_PERMISSION_SERVICE = "vivo_permission_service";

    public abstract boolean checkCallingVivoPermission(String str);

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public abstract int checkOnePermission(String str, String str2, int i);

    @Deprecated
    public abstract boolean checkPermission(Context context, String str);

    @Deprecated
    public abstract boolean checkPermissionExt(String str);

    public abstract boolean checkVivoPermission(String str, int i, int i2);

    public abstract int checkVivoPermissionWithCB(String str, int i, int i2, IVivoPermissionCallback iVivoPermissionCallback);

    public abstract boolean configInSA();

    public abstract void debugPackages(String[] strArr);

    public abstract void debugPerformance(String str);

    public abstract void debugStack(String str);

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public abstract VivoPermissionInfo getAppPermission(String str);

    public abstract ApplicationInfo getCallingAppInfo(Context context);

    public abstract String getCallingAppName(Context context);

    public abstract int getCallingAppPermisson(Context context, VivoPermissionType vivoPermissionType);

    public abstract PackageInfo getCallingPackageInfo(Context context, int i);

    public abstract IVivoPermissionService getDefaultVPS();

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public abstract List<VivoPermissionInfo> getMonitorAppList();

    public abstract float getOSVersion();

    public abstract String getProjectName();

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public abstract List<VivoPermissionInfo> getSpecifiedPermAppList(int i);

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public abstract List<VivoPermissionInfo> getTrustedAppList();

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public abstract AbsVivoPermissionManager getVPM();

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public abstract int getVPMDataBaseState();

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public abstract int getVPMVersion();

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public abstract boolean isBuildInThirdPartApp(String str);

    public abstract boolean isCMCCTest();

    public abstract boolean isCheckingPermission(int i);

    public abstract boolean isNetEntry();

    public abstract boolean isOverSeas();

    public abstract boolean isSecurityLevelOne();

    public abstract boolean isSystemApp(ApplicationInfo applicationInfo);

    public abstract boolean isSystemAppCalling(Context context);

    public abstract boolean isVivoApp(Context context, String str);

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public abstract boolean isVivoImeiPkg(String str);

    public abstract boolean needMonitorBuildInApps();

    public abstract boolean needMonitorSystemApp();

    public abstract boolean needMonitorSystemUid();

    public abstract boolean needVPM();

    public abstract void noteStartActivityProcess(String str);

    public abstract void printfDebug(String str);

    public abstract void printfError(String str);

    public abstract void printfInfo(String str);

    public abstract ActivityInfo resolveActivityForVivoPermission(Context context, Intent intent, ActivityInfo activityInfo);

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public abstract void setAppPermission(String str, int i, int i2);

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public abstract void setAppPermissionExt(VivoPermissionInfo vivoPermissionInfo);

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public abstract void setBlackListApp(String str, boolean z);

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public abstract boolean setOnePermission(String str, String str2, int i, boolean z);

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public abstract void setWhiteListApp(String str, boolean z);
}
