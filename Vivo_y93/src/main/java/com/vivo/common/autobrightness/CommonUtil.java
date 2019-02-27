package com.vivo.common.autobrightness;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.icu.util.Calendar;
import android.util.Log;
import com.vivo.common.provider.Calendar.Events;
import java.util.Locale;

public class CommonUtil {
    private static final String MTK_PLATFORM = "ro.mediatek.platform";
    private static final String[] PLATFORMS = new String[]{"6592", "6735", "6750", "6752", "8916", "8929", "8939", "8974"};
    private static final String QCOM_PLATFORM = "ro.board.platform";
    private static final String TAG = "CommonUtil";

    public static boolean isExport() {
        if ("yes".equalsIgnoreCase(getProperty("ro.vivo.product.overseas", "no"))) {
            return true;
        }
        return false;
    }

    public static boolean isSecure() {
        if ("yes".equalsIgnoreCase(getProperty("persist.vivo.unifiedconfig.sec", "no"))) {
            return true;
        }
        return false;
    }

    public static boolean isLogCtrlOpen() {
        if ("yes".equalsIgnoreCase(getProperty("persist.sys.log.ctrl", "no"))) {
            return true;
        }
        return false;
    }

    public static boolean isRemindPlatform() {
        String platform = getProperty("ro.board.platform", Events.DEFAULT_SORT_ORDER).toLowerCase(Locale.getDefault());
        if (TextTool.isNull(platform)) {
            platform = getProperty(MTK_PLATFORM, Events.DEFAULT_SORT_ORDER).toLowerCase(Locale.getDefault());
        }
        if (TextTool.isNull(platform)) {
            return false;
        }
        for (CharSequence contains : PLATFORMS) {
            if (platform.contains(contains)) {
                return true;
            }
        }
        return false;
    }

    public static String getMachineModel() {
        return getProperty("ro.product.model", Events.DEFAULT_SORT_ORDER);
    }

    private static String getProperty(String key, String def) {
        String value = null;
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            value = (String) clazz.getDeclaredMethod("get", new Class[]{String.class, String.class}).invoke(clazz, new Object[]{key, def});
        } catch (Exception e) {
            Log.e(TAG, "read system property failed!", e);
        }
        if (TextTool.isEmpty(value)) {
            return def;
        }
        return value;
    }

    public static boolean isAppInstalled(Context context, String packagename) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packagename, 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, packagename + " app is not install.", e);
        }
        if (packageInfo != null) {
            return true;
        }
        return false;
    }

    public static String getTitle(Context context, String pkgName) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(pkgName, StateInfo.STATE_BIT_BATTERY)).toString();
        } catch (Exception e) {
            Log.e(TAG, "get title fail.");
            return Events.DEFAULT_SORT_ORDER;
        }
    }

    public static String getVersionName(Context context, String pkgName) {
        try {
            return context.getPackageManager().getPackageInfo(pkgName, 0).versionName;
        } catch (Exception e) {
            Log.e(TAG, "get versionName fail.");
            return Events.DEFAULT_SORT_ORDER;
        }
    }

    public static int getVersionCode(Context context, String pkgName) {
        try {
            return context.getPackageManager().getPackageInfo(pkgName, 0).versionCode;
        } catch (Exception e) {
            Log.e(TAG, "get versionName fail.");
            return -1;
        }
    }

    public static long getMidNightTime(long limitTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(limitTime);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        return calendar.getTimeInMillis();
    }

    public static long getTime(int hour, int minute, int second, int milliSecond) {
        return getCalendar(hour, minute, second, milliSecond).getTimeInMillis();
    }

    public static Calendar getCalendar(int hour, int minute, int second, int milliSecond) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(11, hour);
        calendar.set(12, minute);
        calendar.set(13, second);
        calendar.set(14, milliSecond);
        return calendar;
    }
}
