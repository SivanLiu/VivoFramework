package com.vivo.services.facedetect;

import android.os.SystemProperties;
import android.util.Slog;
import java.util.HashMap;

public class BoostConfig {
    private static final HashMap<String, Integer> MODEL_CAMERA_DATA_INTERNAL = new HashMap();
    private static final HashMap<String, Integer> MODEL_CAMERA_FPS = new HashMap();
    private static final HashMap<String, Integer> MODEL_FRAME_MAP = new HashMap();
    private static final HashMap<String, int[]> MODEL_MAP = new HashMap();
    private static final HashMap<String, Integer> MODEL_NOTIFY_MAP = new HashMap();
    private static final String TAG = "BoostConfig";
    private static String mDaemonVersion = SystemProperties.get("persist.vivo.vivo_daemon", "");
    private static String mPlatform = SystemProperties.get("ro.board.platform", "");
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
        MODEL_MAP.put("PD1728UD", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1728", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1728F_EX", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1730F_EX", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1730BF_EX", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1730", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1731", new int[]{1086324736, 1, 1082130432, 1500});
        MODEL_MAP.put("PD1731D", new int[]{1086324736, 1, 1082130432, 1500});
        MODEL_MAP.put("PD1731F_EX", new int[]{1086324736, 1, 1082130432, 1500});
        MODEL_MAP.put("PD1730C", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1730CF_EX", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1730D", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1730DF_EX", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1730E", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1610", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1619", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1635", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1616BA", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1616B", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1616", new int[]{1086324736, 1, 1082130688, 2100});
        MODEL_MAP.put("PD1624", new int[]{1086324736, 1, 1082130688, 2100});
        MODEL_MAP.put("PD1731EF_EX", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1731CF_EX", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1818EF_EX", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1818BF_EX", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1731C", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1818F_EX", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("PD1818", new int[]{1086324736, 1, 1082130432, 4095, 1082130688, 4095});
        MODEL_MAP.put("sdm710", new int[]{1082130432, 4095, 1082130688, 4095, 1098907648, 4095, 1086324736, 1, 1086439424, 40, 1086455808, 30, 1077936128, 1});
        MODEL_MAP.put("PD1814F_EX", new int[]{1082130432, 4095, 1082130688, 4095, 1098907648, 4095, 1086324736, 1, 1077936128, 1, 1098907648, 49, 1090519040, 4});
        MODEL_MAP.put("PD1814", new int[]{1082130432, 4095, 1082130688, 4095, 1098907648, 4095, 1086324736, 1, 1077936128, 1, 1098907648, 49, 1090519040, 4});
        MODEL_MAP.put("PD1816", new int[]{1082130432, 4095, 1082130688, 4095, 1098907648, 4095, 1086324736, 1, 1077936128, 1, 1098907648, 49, 1090519040, 4});
        MODEL_MAP.put("unknown", new int[]{1086324736, 2, 1082138624, 3, 1082138880, 2});
        MODEL_FRAME_MAP.put("PD1708", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1708F_EX", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1718", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1724", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1730F_EX", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1730BF_EX", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1730DF_EX", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1730", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1730E", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1731", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1731D", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1731F_EX", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1801", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1813", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1813F_EX", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1813BF_EX", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1813C", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1616BA", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1616B", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1616", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1624", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1718F_EX", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1708BF_EX", Integer.valueOf(1));
        MODEL_FRAME_MAP.put("PD1730D", Integer.valueOf(2));
        MODEL_FRAME_MAP.put("unknown", Integer.valueOf(3));
        MODEL_NOTIFY_MAP.put("PD1709", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1709F_EX", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1710", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1710F_EX", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1728", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1728UD", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1728F_EX", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1801", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1730C", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1730CF_EX", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1730DF_EX", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1730D", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1730E", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1610", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1619", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1635", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1616BA", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1616B", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1616", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1624", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1708F_EX", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1708BF_EX", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1718F_EX", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1809", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1813", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1813B", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1813D", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1813F_EX", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1813BF_EX", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1814F_EX", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1814", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1816", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("PD1813C", Integer.valueOf(0));
        MODEL_NOTIFY_MAP.put("unknown", Integer.valueOf(-1));
        MODEL_CAMERA_FPS.put("PD1709", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1709F_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1710", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1710F_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1728", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1728F_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1730", Integer.valueOf(20));
        MODEL_CAMERA_FPS.put("PD1730F_EX", Integer.valueOf(20));
        MODEL_CAMERA_FPS.put("PD1730BF_EX", Integer.valueOf(20));
        MODEL_CAMERA_FPS.put("PD1728UD", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1731", Integer.valueOf(15));
        MODEL_CAMERA_FPS.put("PD1731D", Integer.valueOf(15));
        MODEL_CAMERA_FPS.put("PD1731F_EX", Integer.valueOf(15));
        MODEL_CAMERA_FPS.put("PD1803", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1803F_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1732", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1730C", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1730CF_EX", Integer.valueOf(20));
        MODEL_CAMERA_FPS.put("PD1730DF_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1730D", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1730E", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1610", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1619", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1635", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1616BA", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1616B", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1616", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1624", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1708F_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1708BF_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1718F_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1732F_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1732CF_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1732F_EX_2", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1814F_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1814", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1813F_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1813", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1813C", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1813B", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1813D", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1816", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1813BF_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1818B", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1818C", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1818CF_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1731EF_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1731CF_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1818EF_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1818BF_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1731C", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("PD1818F_EX", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("1818", Integer.valueOf(24));
        MODEL_CAMERA_FPS.put("unknown", Integer.valueOf(30));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1709", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1709F_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1710", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1710F_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1728", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1728F_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1728UD", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1730D", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1730", Integer.valueOf(100));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1730E", Integer.valueOf(100));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1730F_EX", Integer.valueOf(100));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1730BF_EX", Integer.valueOf(100));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1731", Integer.valueOf(100));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1731D", Integer.valueOf(100));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1731F_EX", Integer.valueOf(100));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1801", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1803", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1803F_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1732", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1730C", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1730CF_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1730DF_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1610", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1619", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1635", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1616BA", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1616B", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1616", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1624", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1708F_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1708BF_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1718F_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1732F_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1732F_EX_2", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1732CF_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1809", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1814F_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1814", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1816", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1813F_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1813BF_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1813", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1813C", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1813B", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1813D", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1818B", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1818C", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1818CF_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1731EF_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1731CF_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1818EF_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1818BF_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1731C", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1818F_EX", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("PD1818", Integer.valueOf(70));
        MODEL_CAMERA_DATA_INTERNAL.put("unknown", Integer.valueOf(0));
    }

    public static String getCurrentModelDaemonVersion() {
        return mDaemonVersion;
    }

    public static int[] getCurrentModelBoostConfig() {
        int[] config = (int[]) MODEL_MAP.get(sPhoneModel);
        if (config == null) {
            config = (int[]) MODEL_MAP.get(mPlatform);
        }
        Slog.d(TAG, "getCurrentModelBoostConfig config: " + String.valueOf(config));
        if (FaceDebugConfig.DEBUG_TIME) {
            Slog.d(TAG, "current model: " + sPhoneModel + " with config: " + String.valueOf(config));
        }
        return config;
    }

    public static int[] filterOutSchedBoost(int[] configs) {
        int count = 0;
        int[] tempList = new int[configs.length];
        int i = 0;
        while (i < configs.length) {
            int arg = configs[i];
            if (arg == 1086324736) {
                i++;
            } else {
                tempList[count] = arg;
                count++;
            }
            i++;
        }
        int[] list = new int[count];
        System.arraycopy(tempList, 0, list, 0, count);
        return list;
    }

    public static int getCurrentModelSkipFrameConfig() {
        Integer config = (Integer) MODEL_FRAME_MAP.get(sPhoneModel);
        if (FaceDebugConfig.DEBUG_TIME) {
            Slog.d(TAG, "current model: " + sPhoneModel + " with frame: " + config);
        }
        return config != null ? config.intValue() : 3;
    }

    public static int getCurrentModelNotifyConfig() {
        Integer config = (Integer) MODEL_NOTIFY_MAP.get(sPhoneModel);
        if (FaceDebugConfig.DEBUG_TIME) {
            Slog.d(TAG, "current model: " + sPhoneModel + " with notify: " + config);
        }
        return config != null ? config.intValue() : -1;
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

    public static boolean isMtkNewBoostFramework() {
        String[] mtkModel = new String[]{"PD1803", "PD1803F_EX", "PD1801", "PD1732", "PD1732F_EX", "PD1732F_EX_2", "PD1813", "PD1813F_EX", "PD1813BF_EX", "PD1813C", "PD1818B", "PD1818C", "PD1818CF_EX", "PD1732CF_EX"};
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

    public static int getCurrentCameraFps() {
        Integer config = (Integer) MODEL_CAMERA_FPS.get(sPhoneModel);
        if (FaceDebugConfig.DEBUG_TIME) {
            Slog.d(TAG, "current model: " + sPhoneModel + " with fp: " + config);
        }
        return config != null ? config.intValue() : 30;
    }

    public static int getCurrentCameraDataInternal() {
        Integer config = (Integer) MODEL_CAMERA_DATA_INTERNAL.get(sPhoneModel);
        if (FaceDebugConfig.DEBUG_TIME) {
            Slog.d(TAG, "current model: " + sPhoneModel + " with time: " + config);
        }
        return config != null ? config.intValue() : 0;
    }
}
