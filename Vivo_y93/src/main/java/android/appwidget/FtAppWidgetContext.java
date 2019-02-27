package android.appwidget;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import java.util.ArrayList;

public final class FtAppWidgetContext {
    private static final String TAG = "FtAppWidgetContext";
    private static ArrayList<String> mUpgradeWidgetsList = new ArrayList();
    private static ArrayList<String> mWidgetsList = new ArrayList();

    static {
        mWidgetsList.add("com.vivo.magic.clockweathermusic");
        mWidgetsList.add("com.vivo.magic.note");
        mWidgetsList.add("com.vivo.magic.manager");
        mWidgetsList.add("com.vivo.dream.clock");
        mWidgetsList.add("com.vivo.dream.music");
        mWidgetsList.add("com.vivo.dream.weather");
        mWidgetsList.add("com.vivo.dream.note");
        mWidgetsList.add("com.vivo.dream.manager");
        mWidgetsList.add("com.vivo.simpleclock");
        mWidgetsList.add("com.vivo.simplemusic");
        mWidgetsList.add("com.vivo.simpleweather");
        mWidgetsList.add("com.android.contacts.contactsstaredwidget");
        mWidgetsList.add("com.android.providers.calendar");
        mWidgetsList.add("com.bbk.photoframewidget");
        mWidgetsList.add("com.vivo.browser");
        mWidgetsList.add("com.iqoo.secure");
        mWidgetsList.add("com.vivo.simpleweather.mix");
        mWidgetsList.add("com.vivo.magic.clock");
        mWidgetsList.add("com.vivo.magic.music");
        mWidgetsList.add("com.vivo.widget.calendar");
        mWidgetsList.add("com.vivo.doubletimezoneclock");
        mUpgradeWidgetsList.add("com.iqoo.secure");
        mUpgradeWidgetsList.add("com.vivo.browser");
    }

    private FtAppWidgetContext() {
    }

    public static Context newWidgetContext(Context context, ApplicationInfo application) {
        try {
            if (!mWidgetsList.contains(application.packageName)) {
                return context;
            }
            Log.v(TAG, "Appwidget has custom view, package name: " + application.packageName);
            return context.createApplicationContext(application, 3);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Appwidget Package name " + application.packageName + " not found");
            return context;
        }
    }

    public static boolean isUpgradeApp(String packageName) {
        if (mUpgradeWidgetsList.contains(packageName)) {
            return true;
        }
        return false;
    }

    public static void addUpgradeApp(String packageName) {
        if (!mUpgradeWidgetsList.contains(packageName)) {
            mUpgradeWidgetsList.add(packageName);
        }
    }

    public static void removeUpgradeApp(String packageName) {
        if (mUpgradeWidgetsList.contains(packageName)) {
            mUpgradeWidgetsList.remove(packageName);
        }
    }

    public static void addCustomViewApp(String packageName) {
        if (!mWidgetsList.contains(packageName)) {
            mWidgetsList.add(packageName);
        }
    }

    public static void removeCustomViewApp(String packageName) {
        if (mWidgetsList.contains(packageName)) {
            mWidgetsList.remove(packageName);
        }
    }
}
