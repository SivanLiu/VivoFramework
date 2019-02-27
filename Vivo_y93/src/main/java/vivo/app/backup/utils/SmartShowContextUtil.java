package vivo.app.backup.utils;

import android.util.Slog;
import java.lang.reflect.InvocationTargetException;

public class SmartShowContextUtil {
    private static final String TAG = "SmartShowContextUtil";
    public static final int TYPE_CLONE_SECOND = 1;
    public static final int TYPE_SECOND_ACCOUNT = 8;
    public static final int TYPE_SECOND_NOTIFICATION = 4;
    public static final int TYPE_SECOND_SHARED = 2;
    private static Object mShowContext = null;

    public static Object getSmartShowContext() {
        if (mShowContext == null) {
            try {
                mShowContext = Class.forName("android.content.SmartShowContext").getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
            } catch (ClassNotFoundException e) {
                Slog.d(TAG, "android.content.SmartShowContext not found!");
                mShowContext = null;
            } catch (NoSuchMethodException e2) {
                Slog.d(TAG, "Cant get method getInstance in SmartShowContext");
                mShowContext = null;
            } catch (IllegalAccessException e3) {
                Slog.d(TAG, "inoke getInstance IllegalAccessException");
            } catch (InvocationTargetException e4) {
                Slog.d(TAG, "invoke getInstance InvocationTargetException");
            }
        }
        return mShowContext;
    }

    public static boolean isDualInstanceEnabled(String pkg, int uid, int type) {
        boolean enabled = false;
        if (getSmartShowContext() == null) {
            return enabled;
        }
        try {
            return ((Boolean) Class.forName("android.content.ISmartShowContext").getMethod("isDualInstanceEnabled", new Class[]{String.class, Integer.TYPE, Integer.TYPE}).invoke(mShowContext, new Object[]{pkg, Integer.valueOf(uid), Integer.valueOf(type)})).booleanValue();
        } catch (ClassNotFoundException e) {
            Slog.d(TAG, "android.content.ISmartShowContext not found!");
            return enabled;
        } catch (NoSuchMethodException e2) {
            Slog.d(TAG, "Cant get method isDualInstanceEnabled in ISmartShowContext");
            return enabled;
        } catch (IllegalAccessException e3) {
            Slog.d(TAG, "inoke isDualInstanceEnabled IllegalAccessException", e3);
            return enabled;
        } catch (InvocationTargetException e4) {
            Slog.d(TAG, "invoke isDualInstanceEnabled InvocationTargetException", e4);
            return enabled;
        }
    }

    public static boolean saveConfig(String pkg, int uid, int type, boolean enabled) {
        boolean ret = false;
        if (getSmartShowContext() == null) {
            return ret;
        }
        try {
            return ((Boolean) Class.forName("android.content.ISmartShowContext").getMethod("saveConfig", new Class[]{String.class, Integer.TYPE, Integer.TYPE, Boolean.TYPE}).invoke(mShowContext, new Object[]{pkg, Integer.valueOf(uid), Integer.valueOf(type), Boolean.valueOf(enabled)})).booleanValue();
        } catch (ClassNotFoundException e) {
            Slog.d(TAG, "android.content.ISmartShowContext not found!");
            return ret;
        } catch (NoSuchMethodException e2) {
            Slog.d(TAG, "Cant get method saveConfig in ISmartShowContext");
            return ret;
        } catch (IllegalAccessException e3) {
            Slog.d(TAG, "inoke saveConfig IllegalAccessException", e3);
            return ret;
        } catch (InvocationTargetException e4) {
            Slog.d(TAG, "invoke saveConfig InvocationTargetException", e4);
            return ret;
        }
    }
}
