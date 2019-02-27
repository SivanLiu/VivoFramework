package vivo.app.backup.utils;

import android.content.pm.ApplicationInfo;
import android.os.IUserManager;
import android.os.IUserManager.Stub;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Slog;

public class DoubleInstanceUtil {
    public static final int CONFIG_TYPE_ACCOUNT = 8;
    public static final int CONFIG_TYPE_ENABLED = 1;
    public static final int CONFIG_TYPE_NOTIFICATION = 4;
    public static final int CONFIG_TYPE_SHARED = 2;
    public static final String DEFAULT = "default";
    private static final String DOUBLE_INSTANCE_KEY = "persist.vivo.doubleinstance";
    public static final String MULTI_USER = "multi_user";
    private static final String TAG = "DoubleInstanceUtil";
    private static final String UNKNOWN_VALUE = "unkown";

    private static class DoubleInstanceDefault {
        private DoubleInstanceDefault() {
        }

        static boolean enableClone(String pkg) {
            if (SmartShowContextUtil.getSmartShowContext() == null) {
                return false;
            }
            int id = myUserId();
            if (id == -1) {
                return false;
            }
            return ((SmartShowContextUtil.saveConfig(pkg, id, 1, true) & SmartShowContextUtil.saveConfig(pkg, id, 2, true)) & SmartShowContextUtil.saveConfig(pkg, id, 4, true)) & SmartShowContextUtil.saveConfig(pkg, id, 8, true);
        }

        static boolean isCloneEnabled(String pkg) {
            int id = myUserId();
            if (id == -1) {
                return false;
            }
            return SmartShowContextUtil.isDualInstanceEnabled(pkg, id, 1);
        }

        static String getCloneDataPath(String pkg) {
            String cloneDataPath = String.format("/data/data/%s/.1", new Object[]{pkg});
            int userId = myUserId();
            if (userId == -1) {
                return cloneDataPath;
            }
            ApplicationInfo ai = PackageManagerUtil.getApplicationInfo(pkg, 8192, userId);
            if (ai == null || (TextUtils.isEmpty(ai.dataDir) ^ 1) == 0) {
                return cloneDataPath;
            }
            return String.format("%s/.1", new Object[]{ai.dataDir});
        }

        static int myUserId() {
            int userId = -1;
            try {
                userId = ((Integer) UserHandle.class.getDeclaredMethod("myUserId", new Class[0]).invoke(null, new Object[0])).intValue();
            } catch (Exception e) {
                Slog.d(DoubleInstanceUtil.TAG, "invoke myUSerId Exception", e);
            }
            Slog.d(DoubleInstanceUtil.TAG, "invoke myUSerId " + userId);
            return userId;
        }
    }

    private static class DoubleInstanceMultiUser {
        static final int USER_NULL = -10000;

        private DoubleInstanceMultiUser() {
        }

        static boolean enableClone(String pkg) {
            int userId = getDoubleAppUserId();
            if (userId != USER_NULL) {
                return PackageManagerUtil.installExistingPackageAsUser(pkg, userId) == 1;
            } else {
                return false;
            }
        }

        static boolean isCloneEnabled(String pkg) {
            int userId = getDoubleAppUserId();
            if (userId != USER_NULL) {
                return PackageManagerUtil.isPackageAvailable(pkg, userId);
            }
            return false;
        }

        static boolean isDoubleAppUserExist() {
            boolean isDoubleAppUserExist = false;
            IUserManager service = Stub.asInterface(ServiceManager.getService("user"));
            if (service != null) {
                try {
                    isDoubleAppUserExist = Boolean.valueOf(String.valueOf(service.getClass().getMethod("isDoubleAppUserExist", new Class[0]).invoke(service, new Object[0]))).booleanValue();
                } catch (Exception e) {
                    Slog.e(DoubleInstanceUtil.TAG, "isDoubleAppUserExist", e);
                }
            }
            Slog.i(DoubleInstanceUtil.TAG, "isDoubleAppUserExist " + isDoubleAppUserExist);
            return isDoubleAppUserExist;
        }

        static int getDoubleAppUserId() {
            int userId = USER_NULL;
            IUserManager service = Stub.asInterface(ServiceManager.getService("user"));
            if (service != null) {
                try {
                    userId = Integer.valueOf(String.valueOf(service.getClass().getMethod("getDoubleAppUserId", new Class[0]).invoke(service, new Object[0]))).intValue();
                } catch (Exception e) {
                    Slog.e(DoubleInstanceUtil.TAG, "getDoubleAppUserId", e);
                    userId = -1;
                }
            }
            Slog.i(DoubleInstanceUtil.TAG, "userId " + userId);
            return userId;
        }
    }

    private static boolean isSupportMultiUser() {
        boolean isSupportMultiUser;
        if (MULTI_USER.equals(SystemProperties.get(DOUBLE_INSTANCE_KEY, UNKNOWN_VALUE))) {
            isSupportMultiUser = DoubleInstanceMultiUser.isDoubleAppUserExist();
        } else {
            isSupportMultiUser = false;
        }
        Slog.d(TAG, "isSupportMultiUser:" + isSupportMultiUser);
        return isSupportMultiUser;
    }

    private static boolean isSupportDefault() {
        String doubleInstance = SystemProperties.get(DOUBLE_INSTANCE_KEY, UNKNOWN_VALUE);
        boolean isSupportDefault = (DEFAULT.equals(doubleInstance) || UNKNOWN_VALUE.equals(doubleInstance)) ? SmartShowContextUtil.getSmartShowContext() != null : false;
        Slog.d(TAG, "isSupportDefault:" + isSupportDefault);
        return isSupportDefault;
    }

    public static boolean isSupportDoubleInstance() {
        return !isSupportMultiUser() ? isSupportDefault() : true;
    }

    public static boolean enableClone(String pkg) {
        boolean enabled = false;
        if (isSupportDefault()) {
            enabled = DoubleInstanceDefault.enableClone(pkg);
        } else if (isSupportMultiUser()) {
            enabled = DoubleInstanceMultiUser.enableClone(pkg);
        }
        Slog.d(TAG, "enable clone for " + pkg + " reslut:" + enabled);
        return enabled;
    }

    public static boolean isCloneEnabled(String pkg) {
        boolean enabled = false;
        if (isSupportDefault()) {
            enabled = DoubleInstanceDefault.isCloneEnabled(pkg);
        } else if (isSupportMultiUser()) {
            enabled = DoubleInstanceMultiUser.isCloneEnabled(pkg);
        }
        Slog.d(TAG, "is the clone enabled for " + pkg + ":" + enabled);
        return enabled;
    }

    public static int getDualUserId() {
        if (isSupportDefault()) {
            return DoubleInstanceDefault.myUserId();
        }
        if (isSupportMultiUser()) {
            return DoubleInstanceMultiUser.getDoubleAppUserId();
        }
        return -1;
    }
}
