package vivo.app.backup.utils;

import android.app.ActivityThread;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.util.Slog;
import java.lang.reflect.Method;

public class PackageManagerUtil {
    private static final String TAG = "PackageManagerUtil";
    private static IPackageManager mPM;
    private static Method mtdGetApplicationEnabledSetting;
    private static Method mtdGetApplicationInfo;
    private static Method mtdInstallExistingPackageAsUser;
    private static Method mtdIsPackageAvailable;
    private static Method mtdSetApplicationEnabledSetting;

    static {
        mPM = null;
        mPM = ActivityThread.getPackageManager();
        if (mPM != null) {
            for (Method method : mPM.getClass().getDeclaredMethods()) {
                if ("isPackageAvailable".equals(method.getName())) {
                    mtdIsPackageAvailable = method;
                }
                if ("getApplicationInfo".equals(method.getName())) {
                    mtdGetApplicationInfo = method;
                }
                if ("installExistingPackageAsUser".equals(method.getName())) {
                    mtdInstallExistingPackageAsUser = method;
                }
                if ("getApplicationEnabledSetting".equals(method.getName())) {
                    mtdGetApplicationEnabledSetting = method;
                }
                if ("setApplicationEnabledSetting".equals(method.getName())) {
                    mtdSetApplicationEnabledSetting = method;
                }
            }
        }
    }

    public static boolean isPackageAvailable(String packageName, int userId) {
        try {
            if (!(mPM == null || mtdIsPackageAvailable == null)) {
                return Boolean.valueOf(String.valueOf(mtdIsPackageAvailable.invoke(mPM, new Object[]{packageName, Integer.valueOf(userId)}))).booleanValue();
            }
        } catch (Exception e) {
            Slog.e(TAG, "isPackageAvailable failed", e);
        }
        return false;
    }

    public static ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) {
        try {
            if (mPM == null || mtdGetApplicationInfo == null) {
                return null;
            }
            return (ApplicationInfo) mtdGetApplicationInfo.invoke(mPM, new Object[]{packageName, Integer.valueOf(flags), Integer.valueOf(userId)});
        } catch (Exception e) {
            Slog.e(TAG, "getApplicationInfo failed", e);
            return null;
        }
    }

    public static int installExistingPackageAsUser(String packageName, int userId) {
        try {
            if (!(mPM == null || mtdInstallExistingPackageAsUser == null)) {
                int result = Integer.valueOf(String.valueOf(mtdInstallExistingPackageAsUser.invoke(mPM, new Object[]{packageName, Integer.valueOf(userId), Integer.valueOf(34), Integer.valueOf(0)}))).intValue();
                Slog.i(TAG, "installExistingPackageAsUser " + result);
                return result;
            }
        } catch (Exception e) {
            Slog.e(TAG, "installExistingPackageAsUser failed", e);
        }
        return 0;
    }

    public static int getApplicationEnabledSetting(String packageName, int userId) {
        try {
            if (!(mPM == null || mtdGetApplicationEnabledSetting == null)) {
                int result = Integer.valueOf(String.valueOf(mtdGetApplicationEnabledSetting.invoke(mPM, new Object[]{packageName, Integer.valueOf(userId)}))).intValue();
                Slog.i(TAG, "getApplicationEnabledSetting " + result);
                return result;
            }
        } catch (Exception e) {
            Slog.e(TAG, "getApplicationEnabledSetting failed", e);
        }
        return 0;
    }

    public static void setApplicationEnabledSetting(String packageName, int newState, int flags, int userId, String callingPackage) {
        try {
            if (mPM != null && mtdSetApplicationEnabledSetting != null) {
                mtdSetApplicationEnabledSetting.invoke(mPM, new Object[]{packageName, Integer.valueOf(newState), Integer.valueOf(flags), Integer.valueOf(userId), callingPackage});
            }
        } catch (Exception e) {
            Slog.e(TAG, "setApplicationEnabledSetting failed", e);
        }
    }
}
