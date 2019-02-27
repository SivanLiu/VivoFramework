package com.vivo.common.autobrightness;

import android.os.SystemProperties;
import android.util.Slog;

public class BootBrightnessConfig {
    private static final String TAG = "BootBrightnessConfig";
    private static final BootBrightnessList[] mConfigs = new BootBrightnessList[]{new BootBrightnessList("pd1524", 123), new BootBrightnessList("pd1515", 108), new BootBrightnessList("pd1502", StateInfo.STATE_BIT_BATTERY), new BootBrightnessList("pd1415", 117), new BootBrightnessList("pd1522", 117), new BootBrightnessList("pd1516", 117), new BootBrightnessList("pd1523", StateInfo.STATE_BIT_BATTERY), new BootBrightnessList("pd1602", 80), new BootBrightnessList("pd1603", 117), new BootBrightnessList("pd1505", 123), new BootBrightnessList("td1602", 120), new BootBrightnessList("td1603", 117), new BootBrightnessList("td1608", 117), new BootBrightnessList("std1616", 117), new BootBrightnessList("pd1510", 115), new BootBrightnessList("pd1613bf_ex", 129), new BootBrightnessList("pd1613", 129), new BootBrightnessList("pd1610", 117), new BootBrightnessList("pd1612", StateInfo.STATE_BIT_BATTERY), new BootBrightnessList("pd1616", 117), new BootBrightnessList("pd1621", 129), new BootBrightnessList("vtd1704f_ex", 129), new BootBrightnessList("td1605", 129), new BootBrightnessList("pd1619", 1681), new BootBrightnessList("pd1635", 269), new BootBrightnessList("pd1617", 67), new BootBrightnessList("pd1624", 117), new BootBrightnessList("pd1628", 120), new BootBrightnessList("pd1630f_ex", StateInfo.STATE_BIT_BATTERY), new BootBrightnessList("pd1708", 1681), new BootBrightnessList("pd1705", 1681), new BootBrightnessList("pd1709", 269), new BootBrightnessList("pd1710", 269), new BootBrightnessList("td1702", 1681), new BootBrightnessList("pd1718", 1681), new BootBrightnessList("pd1721", 269), new BootBrightnessList("pd1724", 269), new BootBrightnessList("vtd1702", 269), new BootBrightnessList("td1703", 117), new BootBrightnessList("vtd1703f_ex", 117), new BootBrightnessList("pd1728", 269), new BootBrightnessList("pd1729", 269), new BootBrightnessList("pd1730", 1681), new BootBrightnessList("pd1731", 1681), new BootBrightnessList("td1705", 269), new BootBrightnessList("td1704", 269), new BootBrightnessList("pd1803", 1681), new BootBrightnessList("pd1801", 269), new BootBrightnessList("pd1805", 269), new BootBrightnessList("pd1806", 269), new BootBrightnessList("pd1809", 269), new BootBrightnessList("pd1732", 1681), new BootBrightnessList("pd1813", 1681), new BootBrightnessList("pd1814", 269), new BootBrightnessList("pd1816", 269), new BootBrightnessList("pd1818", 1681), new BootBrightnessList("td1803", 1681)};
    public static final int mInitialBrightness = InitialBrightness();
    public static final int mInitialSettingBrightness = AblConfig.getMapping2048GrayScaleTo256GrayScaleRestore(InitialSettingBrightness());
    private static final String model = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL).toLowerCase();

    public static class BootBrightnessList {
        public int Brightness;
        public String model;

        public BootBrightnessList(String model, int Brightness) {
            this.model = model;
            this.Brightness = Brightness;
        }
    }

    public static int InitialBrightness() {
        if (mConfigs == null) {
            Slog.d(TAG, "[InitialBrightness]:mConfigs is null!");
            return 255;
        }
        if (mConfigs.length > 0) {
            for (int i = 0; i < mConfigs.length; i++) {
                if (model.startsWith(mConfigs[i].model)) {
                    int mBootBrightness = mConfigs[i].Brightness;
                    Slog.d(TAG, "[InitialBrightness]:model matching, set backlight to BootBrightness : " + mBootBrightness);
                    return mBootBrightness;
                }
            }
            Slog.d(TAG, "[InitialBrightness]:model doesn't match, set backlight to default BootBrightness : " + 255);
        } else {
            Slog.d(TAG, "[InitialBrightness]:set backlight to default Brightness : " + 255);
        }
        return 255;
    }

    public static int InitialSettingBrightness() {
        if (mConfigs == null) {
            Slog.d(TAG, "[InitialSettingBrightness]:mConfigs is null!");
            return -1;
        }
        if (mConfigs.length > 0) {
            for (int i = 0; i < mConfigs.length; i++) {
                if (model.startsWith(mConfigs[i].model)) {
                    int mBootBrightness = mConfigs[i].Brightness;
                    Slog.d(TAG, "[InitialSettingBrightness]:model matching, set backlight to BootBrightness : " + mBootBrightness);
                    return mBootBrightness;
                }
            }
            Slog.d(TAG, "[InitialSettingBrightness]:model doesn't match, set backlight to default BootBrightness : " + -1);
        } else {
            Slog.d(TAG, "[InitialSettingBrightness]:set backlight to default Brightness : " + -1);
        }
        return -1;
    }
}
