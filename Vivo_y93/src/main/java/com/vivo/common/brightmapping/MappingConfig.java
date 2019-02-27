package com.vivo.common.brightmapping;

import android.os.SystemProperties;
import com.vivo.common.autobrightness.AblConfig;
import com.vivo.vivogamesdk.PlatformMSM8996;

public class MappingConfig {
    private static final String MODEL = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, "unkown");
    private static final String[] NEED_MAPPING_LIST = new String[]{PlatformMSM8996.PRODUCT_MODEL_BBK_PD1610, "PD1616", "PD1624", "PD1619", "STD1616", "PD1635", "TD1608", "PD1708", "SPD1706", "PD1709", "PD1710", "PD1705", "TD1702", "VTD1702", "TD1703", "VTD1703F_EX", "PD1728", "PD1729", "PD1730", "PD1704F_EX", "TD1705", "VTD1704F_EX", "PD1731", "TD1704", "PD1803", "PD1801", "PD1805", "PD1806", "PD1732", "PD1721", "PD1718", "PD1809", "PD1814", "PD1816", "PD1813", "TD1803", "PD1818"};
    private static final boolean mNeedMapping = isNeedMappingInner();
    private static final int mProgressMax = getProgressMaxInner();
    private static final int mProgressMin = getProgressMinInner();

    private static boolean isNeedMappingInner() {
        for (String model : NEED_MAPPING_LIST) {
            if (MODEL.startsWith(model)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNeedMapping() {
        return mNeedMapping;
    }

    private static int getProgressMinInner() {
        return 1;
    }

    public static int getProgressMin() {
        return mProgressMin;
    }

    private static int getProgressMaxInner() {
        return 466;
    }

    public static int getProgressMax() {
        return mProgressMax;
    }
}
