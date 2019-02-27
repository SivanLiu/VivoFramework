package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.telephony.Rlog;
import com.vivo.common.VivoCollectData;
import java.util.HashMap;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public final class VivoBigDataManager {
    private static final String LOG_TAG = "VivoBigDataManager";
    private static final boolean[] MODULE_ENABLE = new boolean[]{false};
    private static final String[][] MODULE_INDEX;

    public enum MODULE_TAG {
        NITZ_TAG
    }

    static {
        String[][] strArr = new String[1][];
        strArr[0] = new String[]{"800", "8002"};
        MODULE_INDEX = strArr;
    }

    public static boolean isModuleEnable(MODULE_TAG tag) {
        return MODULE_ENABLE[tag.ordinal()];
    }

    public static void collectData(Context context, MODULE_TAG tag, HashMap<String, String> data) {
        int moduleIndex = tag.ordinal();
        if (isModuleEnable(tag)) {
            new VivoCollectData(context.getApplicationContext()).writeData(MODULE_INDEX[moduleIndex][0], MODULE_INDEX[moduleIndex][1], System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, data);
        } else {
            log("module is not enable:" + tag);
        }
    }

    private static void log(String str) {
        Rlog.v(LOG_TAG, str);
    }
}
