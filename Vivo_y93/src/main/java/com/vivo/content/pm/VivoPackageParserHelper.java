package com.vivo.content.pm;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.ActivityIntentInfo;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.Service;
import android.content.pm.PackageParser.ServiceIntentInfo;
import android.provider.MediaStore.Video.VideoColumns;
import android.util.Log;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoPackageParserHelper {
    private static final String HOME_CATEGORY = "android.intent.category.HOME";
    private static final String INPUT_ACTION = "android.view.InputMethod";
    private static final String LIVEWALLPAPER_ACTION = "android.service.wallpaper.WallpaperService";
    private static final String TAG = "VivoPackageParserHelper";
    private static final String WIDGET_ACTION = "android.appwidget.action.APPWIDGET_UPDATE";
    private static boolean limitApp = false;

    private static boolean isLimitApp() {
        Log.d(TAG, "now get limitApp value is :  " + limitApp);
        boolean isLimit = limitApp;
        limitApp = false;
        return isLimit;
    }

    private static boolean toJudgeAppCategory(String nodeName, String value) {
        if (nodeName.equals("action") && ((value.equals("android.view.InputMethod") || value.equals("android.service.wallpaper.WallpaperService") || value.equals(WIDGET_ACTION)) && (limitApp ^ 1) != 0)) {
            Log.d(TAG, "action is : " + nodeName + " ; value is : " + value);
            limitApp = true;
        }
        if (nodeName.equals(VideoColumns.CATEGORY) && value.equals(HOME_CATEGORY) && (limitApp ^ 1) != 0) {
            Log.d(TAG, "   action is :  " + nodeName + "   ;  value is : " + value);
            limitApp = true;
        }
        return limitApp;
    }

    public static void initLimitVal() {
        limitApp = false;
    }

    public static boolean isLimitApp(Package pkg) {
        int count;
        int i;
        for (Activity a : pkg.activities) {
            if (!(a.intents == null || a.intents.isEmpty())) {
                for (ActivityIntentInfo ai : a.intents) {
                    count = ai.countActions();
                    for (i = 0; i < count; i++) {
                        if (toJudgeAppCategory("action", ai.getAction(i))) {
                            return true;
                        }
                    }
                    count = ai.countCategories();
                    for (i = 0; i < count; i++) {
                        if (toJudgeAppCategory(VideoColumns.CATEGORY, ai.getCategory(i))) {
                            return true;
                        }
                    }
                }
                continue;
            }
        }
        for (Service s : pkg.services) {
            if (!(s.intents == null || s.intents.isEmpty())) {
                for (ServiceIntentInfo si : s.intents) {
                    count = si.countActions();
                    for (i = 0; i < count; i++) {
                        if (toJudgeAppCategory("action", si.getAction(i))) {
                            return true;
                        }
                    }
                    count = si.countCategories();
                    for (i = 0; i < count; i++) {
                        if (toJudgeAppCategory(VideoColumns.CATEGORY, si.getCategory(i))) {
                            return true;
                        }
                    }
                }
                continue;
            }
        }
        return limitApp;
    }
}
