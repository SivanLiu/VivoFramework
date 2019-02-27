package com.vivo.services.rms;

import android.os.Bundle;
import com.android.server.am.RMAmsHelper;
import com.android.server.gameopt.GameOptManager;
import com.vivo.services.rms.appmng.namelist.OomPreviousList;
import com.vivo.services.rms.appmng.namelist.OomProtectList;
import com.vivo.services.rms.appmng.namelist.OomStaticList;

public class Config {
    private static final String GAME_SCENE = "com.vivo.gamewatch.game_scene";
    private static final String IS_RMS_ENABLED = "com.vivo.rms.policy.PolicyManager.isRmsEnabled";
    private static final String OOM_PREVIOUS_EXCLUDED_LIST = "previous_excluded_list";
    private static final String OOM_PREVIOUS_LIST = "oom_previous_list";
    private static final String OOM_STATIC_LIST = "oom_static_list";
    private static final String PROTECT_LIST = "com.vivo.rms.policy.PolicyManager.protectlist";

    public static boolean setBundle(String name, Bundle bundle) {
        if (bundle == null || name == null) {
            return false;
        }
        if (name.equals(IS_RMS_ENABLED)) {
            if (!bundle.containsKey("started")) {
                return false;
            }
            setRmsEnable(bundle.getBoolean("started"));
            return true;
        } else if (name.equals(PROTECT_LIST)) {
            OomProtectList.apply(bundle.getStringArrayList("PROTECT_ACTIVITY"), bundle.getStringArrayList("PROTECT_SERVICE"), bundle.getStringArrayList("PROTECT_GAME"));
            return true;
        } else if (name.equals(OOM_STATIC_LIST)) {
            OomStaticList.apply(bundle.getString("policy"), bundle.getStringArrayList("proc"), bundle.getIntegerArrayList("adj"), bundle.getIntegerArrayList("state"), bundle.getIntegerArrayList("sched"));
            return true;
        } else if (name.equals(OOM_PREVIOUS_LIST)) {
            OomPreviousList.apply(bundle.getIntegerArrayList("adj"), bundle.getIntegerArrayList("state"));
            return true;
        } else if (name.equals(OOM_PREVIOUS_EXCLUDED_LIST)) {
            OomPreviousList.updateExcludedList(bundle.getStringArrayList("proc"));
            return true;
        } else if (!name.equals(GAME_SCENE)) {
            return false;
        } else {
            String pkg = bundle.getString("pkg");
            if (bundle.getInt("scene") == 1) {
                GameOptManager.enterGame(pkg);
            } else {
                GameOptManager.exitGame(pkg);
            }
            return true;
        }
    }

    public static void setRmsEnable(boolean enable) {
        RMAmsHelper.setRmsEnable(enable);
        if (!enable) {
            SysFsModifier.restore();
            OomProtectList.restore();
            OomStaticList.restore();
            OomPreviousList.restore();
            OomPreviousList.restoreExcludedList();
        }
    }
}
