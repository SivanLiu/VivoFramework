package org.ifaa.android.manager;

import android.os.SystemProperties;
import java.util.HashMap;

class VivoModelConfig {
    private static final String TAG = "VivoModelConfig";
    private static final String sPlatform = SystemProperties.get("ro.vivo.product.platform", "unknown");
    private static final HashMap<String, String> vivoModelMap = new HashMap();

    VivoModelConfig() {
    }

    static {
        vivoModelMap.put("QCOM8996", "vivo-msm8996_7_1");
        vivoModelMap.put("SDM660", "vivo-sdm660_7_1");
        vivoModelMap.put("QCOM8953", "vivo-msm8953_7_1");
        vivoModelMap.put("SDM670", "vivo-sdm670_8_1");
        vivoModelMap.put("SDM710", "vivo-sdm670_8_1");
        vivoModelMap.put("MTK6771", "vivo-mt6771_8_1");
        vivoModelMap.put("MTK6765", "vivo-mt6765_8_1");
        vivoModelMap.put("SDM845", "vivo-sdm845_8_0");
        vivoModelMap.put("QCOM8976", "vivo-msm8976_7_1");
        vivoModelMap.put("MTK6761", "vivo-mt6761_8_1");
        vivoModelMap.put("SDM439", "vivo-sdm439_8_1");
    }

    public static String getDeviceModel() {
        return (String) vivoModelMap.get(sPlatform);
    }
}
