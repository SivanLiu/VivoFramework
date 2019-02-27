package com.vivo.content;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.util.Log;
import java.io.File;

public abstract class DynamicIcon {
    protected static final ComponentName CALENDAR_COMP = new ComponentName("com.bbk.calendar", "com.bbk.calendar.MainActivity");
    protected static final ComponentName CLOCK_COMP = new ComponentName("com.android.BBKClock", "com.android.BBKClock.Timer");
    protected static final ComponentName WEATHER_COMP = new ComponentName("com.vivo.weather", "com.vivo.weather.WeatherMain");
    protected final String DYNAMIC_ICON_DIR = (VivoTheme.getThemePath() + VivoTheme.getIcontonePath(VivoTheme.getThemePath()) + "dynamic_icon/");
    protected final String DYNAMIC_MANIFEST_NAME = "manifest.xml";
    private final String TAG = "DynamicIcon";
    protected final String TAG_ICON = "ICON";
    protected ComponentName mComponentName;
    protected Context mContext;

    protected abstract Bitmap creatDynamicIcon(Context context);

    protected abstract void parserConfig(File file);

    public Bitmap getIcon(Context context) {
        if (this.mComponentName == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(this.DYNAMIC_ICON_DIR);
        builder.append(this.mComponentName.getPackageName());
        builder.append("/");
        File config = new File(builder.toString() + getDensityResPath(context.getResources()) + "manifest.xml");
        if (config.exists()) {
            parserConfig(config);
            return creatDynamicIcon(context);
        }
        config = new File(builder.toString() + "manifest.xml");
        if (!config.exists()) {
            return null;
        }
        parserConfig(config);
        return creatDynamicIcon(context);
    }

    String getDensityResPath(Resources res) {
        switch (res.getDisplayMetrics().densityDpi) {
            case 240:
                return "res/drawable-sw360dp-hdpi/";
            case 320:
                return "res/drawable-sw360dp-xhdpi/";
            case 480:
                return "res/drawable-sw360dp-xxhdpi/";
            case 640:
                return "res/drawable-sw360dp-xxxhdpi/";
            default:
                return null;
        }
    }

    public static DynamicIcon creatDynamicIcon(ComponentName componetName, Context context) {
        if (componetName.equals(CALENDAR_COMP)) {
            return new DynamicCalendarIcon(componetName);
        }
        if (componetName.equals(WEATHER_COMP)) {
            return new DynamicWeatherIcon(componetName, context);
        }
        return null;
    }

    public Typeface getTypeface(Context context, int fontWeight) {
        String path = "";
        switch (fontWeight) {
            case 0:
                path = "/system/fonts/HYQiHei-35.ttf";
                break;
            case 50:
                path = "/system/fonts/DroidSansFallbackBBK.ttf";
                break;
            case 55:
                path = "/system/fonts/DroidSansMediumBBK.ttf";
                break;
            case 60:
                path = "/system/fonts/DroidSansBoldBBK.ttf";
                break;
            case 100:
                path = "/system/fonts/Gyh-B60N.ttf";
                break;
            case 200:
                path = "/system/fonts/Gyh-Normal.ttf";
                break;
            case 210:
                path = "/system/fonts/vivoHYM02narrowB-Normal.otf";
                break;
            default:
                try {
                    path = "/system/fonts/HYQiHei-35.ttf";
                    break;
                } catch (Exception e) {
                    Log.e("DynamicIcon", "can not get " + fontWeight + " font from system, so use launcher internal 35s font");
                    return Typeface.createFromFile("/system/fonts/HYQiHei-35.ttf");
                }
        }
        Log.d("DynamicIcon", "getTypeface fontWeight:" + fontWeight + ", path:" + path);
        return Typeface.createFromFile(path);
    }
}
