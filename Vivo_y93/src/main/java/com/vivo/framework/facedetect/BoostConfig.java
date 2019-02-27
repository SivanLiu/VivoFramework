package com.vivo.framework.facedetect;

import android.os.SystemProperties;
import android.util.Slog;
import java.util.HashMap;

public class BoostConfig {
    public static final boolean DEBUG_TIME = ("0".equals(SystemProperties.get("persist.facedetect.debug.level", "0")) ^ 1);
    private static final HashMap<String, Integer> MODEL_FRAME_MAP = new HashMap();
    private static final HashMap<String, int[]> MODEL_MAP = new HashMap();
    private static final HashMap<String, Boolean> MODEL_NOTIFY_MAP = new HashMap();
    private static final String TAG = "BoostConfig";
    private static String sPhoneModel = SystemProperties.get("ro.vivo.product.model", "unknown");

    static {
        MODEL_MAP.put("PD1709", new int[]{1086324736, 1, 1082138624, 3, 1082138880, 2});
        MODEL_MAP.put("PD1709F_EX", new int[]{1086324736, 1, 1082138624, 3, 1082138880, 2});
        MODEL_MAP.put("PD1708", new int[]{1086324736, 1, 1082130688, 2100});
        MODEL_MAP.put("PD1724", new int[]{1086324736, 1, 1082130688, 2100});
        MODEL_MAP.put("PD1708F_EX", new int[]{1086324736, 1, 1082130688, 2100});
        MODEL_MAP.put("PD1710", new int[]{1086324736, 1, 1082138624, 3, 1082138880, 2});
        MODEL_MAP.put("PD1710F_EX", new int[]{1086324736, 1, 1082138624, 3, 1082138880, 2});
        MODEL_MAP.put("PD1621BA", new int[]{1082130688, 1400});
        MODEL_MAP.put("PD1621BF_EX", new int[]{1082130688, 1400});
        MODEL_MAP.put("PD1718F_EX", new int[]{1086324736, 1, 1082130688, 2100});
        MODEL_MAP.put("PD1708BF_EX", new int[]{1086324736, 1, 1082130688, 2100});
        MODEL_MAP.put("unknown", new int[]{1086324736, 1, 1082138624, 3, 1082138880, 2});
        MODEL_FRAME_MAP.put("PD1708", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1708F_EX", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1718", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1724", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("unknown", Integer.valueOf(3));
        MODEL_NOTIFY_MAP.put("PD1709", Boolean.valueOf(true));
        MODEL_NOTIFY_MAP.put("PD1709F_EX", Boolean.valueOf(true));
        MODEL_NOTIFY_MAP.put("PD1710", Boolean.valueOf(true));
        MODEL_NOTIFY_MAP.put("PD1710F_EX", Boolean.valueOf(true));
        MODEL_NOTIFY_MAP.put("unknown", Boolean.valueOf(false));
    }

    public static int[] getCurrentModelBoostConfig() {
        int[] config = (int[]) MODEL_MAP.get(sPhoneModel);
        if (DEBUG_TIME) {
            Slog.d(TAG, "current model: " + sPhoneModel + " with config: " + String.valueOf(config));
        }
        return config;
    }

    public static int getCurrentModelSkipFrameConfig() {
        Integer config = (Integer) MODEL_FRAME_MAP.get(sPhoneModel);
        if (DEBUG_TIME) {
            Slog.d(TAG, "current model: " + sPhoneModel + " with frame: " + config);
        }
        return config != null ? config.intValue() : 3;
    }

    public static boolean getCurrentModelNotifyConfig() {
        Boolean config = (Boolean) MODEL_NOTIFY_MAP.get(sPhoneModel);
        if (DEBUG_TIME) {
            Slog.d(TAG, "current model: " + sPhoneModel + " with frame: " + config);
        }
        return config != null ? config.booleanValue() : false;
    }

    public static boolean isMtkProductModel() {
        String[] mtkModel = new String[]{"PD1718"};
        int i = 0;
        while (i < mtkModel.length) {
            Slog.d(TAG, "mtkModel: " + mtkModel[i] + " sPhoneModel: " + sPhoneModel);
            if (sPhoneModel != null && mtkModel[i] != null && sPhoneModel.equals(mtkModel[i])) {
                return true;
            }
            i++;
        }
        return false;
    }
}
