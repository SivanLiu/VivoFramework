package com.vivo.vivogamesdk;

import android.os.SystemProperties;
import android.util.Log;
import com.vivo.common.autobrightness.AblConfig;
import java.io.BufferedReader;
import java.io.FileReader;

public class PlatformMSM8976 implements VivoPlatform {
    private static final int BIG_CORE_LIMIT_TEMPERATURE_PD1602 = 44;
    private static final int BIG_CORE_LIMIT_TEMPERATURE_PD1616B = 44;
    private static final int BIG_CORE_LIMIT_TEMPERATURE_PD1619 = 44;
    private static final int BIG_CORE_LIMIT_TEMPERATURE_PD1635 = 44;
    private static final int BIG_CORE_OFFLINE_TEMPERATURE_PD1602 = 45;
    private static final int BIG_CORE_OFFLINE_TEMPERATURE_PD1616B = 45;
    private static final int BIG_CORE_OFFLINE_TEMPERATURE_PD1619 = 45;
    private static final int BIG_CORE_OFFLINE_TEMPERATURE_PD1635 = 45;
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.log.ctrl", false);
    private static final int FLL_BAD = 3;
    private static final int FLL_ERROR = -1;
    private static final int FLL_MEDIUM = 2;
    private static final int FLL_NONE = 0;
    private static final int FLL_UPCOMING = 1;
    private static final int LITTLE_CORE_LIMIT_TEMPERATURE_PD1602 = 46;
    private static final int LITTLE_CORE_LIMIT_TEMPERATURE_PD1616B = 46;
    private static final int LITTLE_CORE_LIMIT_TEMPERATURE_PD1619 = 46;
    private static final int LITTLE_CORE_LIMIT_TEMPERATURE_PD1635 = 46;
    private static final String NTC_TEMPERATURE_NODE_MSM8976 = "/sys/devices/virtual/thermal/thermal_zone2/temp";
    private static final String NTC_TEMPERATURE_NODE_MSM8976PRO = "/sys/devices/virtual/thermal/thermal_zone3/temp";
    private static final String PRODUCT_MODEL_BBK_PD1602 = "PD1602";
    private static final String PRODUCT_MODEL_BBK_PD1616B = "PD1616B";
    private static final String PRODUCT_MODEL_BBK_PD1619 = "PD1619";
    private static final String PRODUCT_MODEL_BBK_PD1635 = "PD1635";
    private static final String TAG = "VivoGameSDK";
    private boolean isBigCoreOffline = false;
    private boolean isProductSupport;
    private int limitBadLevelTemperature = -1;
    private int limitMediumLevelTemperature = -1;
    private String limitTemperatureNode;
    private int limitUpcomingLevelTemperature = -1;

    public PlatformMSM8976() {
        String productModelBbk = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, "unknown");
        if (productModelBbk.equals(PRODUCT_MODEL_BBK_PD1619)) {
            this.isProductSupport = true;
            this.limitTemperatureNode = NTC_TEMPERATURE_NODE_MSM8976PRO;
            this.limitUpcomingLevelTemperature = 42;
            this.limitMediumLevelTemperature = 44;
            this.limitBadLevelTemperature = 45;
        } else if (productModelBbk.equals(PRODUCT_MODEL_BBK_PD1602)) {
            this.isProductSupport = true;
            this.limitTemperatureNode = NTC_TEMPERATURE_NODE_MSM8976;
            this.limitUpcomingLevelTemperature = 42;
            this.limitMediumLevelTemperature = 44;
            this.limitBadLevelTemperature = 45;
        } else if (productModelBbk.equals(PRODUCT_MODEL_BBK_PD1635)) {
            this.isProductSupport = true;
            this.limitTemperatureNode = NTC_TEMPERATURE_NODE_MSM8976;
            this.limitUpcomingLevelTemperature = 42;
            this.limitMediumLevelTemperature = 44;
            this.limitBadLevelTemperature = 45;
        } else if (productModelBbk.equals(PRODUCT_MODEL_BBK_PD1616B)) {
            this.isProductSupport = true;
            this.limitTemperatureNode = NTC_TEMPERATURE_NODE_MSM8976;
            this.limitUpcomingLevelTemperature = 42;
            this.limitMediumLevelTemperature = 44;
            this.limitBadLevelTemperature = 45;
        } else {
            this.isProductSupport = false;
        }
    }

    public boolean getProductSupport() {
        return this.isProductSupport;
    }

    public int getFreqLimitLevel() {
        if (!this.isProductSupport) {
            return -1;
        }
        int limitLevel;
        int temperature = getNTCTemperature();
        if (DEBUG) {
            Log.d(TAG, "NTCTemperature is " + temperature + ".");
        }
        if (temperature < 0) {
            limitLevel = -1;
        } else if (temperature <= this.limitUpcomingLevelTemperature) {
            limitLevel = 0;
        } else if (temperature <= this.limitMediumLevelTemperature) {
            limitLevel = 1;
        } else if (temperature <= this.limitBadLevelTemperature) {
            limitLevel = 2;
        } else {
            this.isBigCoreOffline = true;
            limitLevel = 3;
        }
        if (this.isBigCoreOffline) {
            if (temperature >= this.limitBadLevelTemperature - 2) {
                limitLevel = 3;
            } else {
                this.isBigCoreOffline = false;
            }
        }
        return limitLevel;
    }

    public int getPhoneTemperature() {
        if (this.isProductSupport) {
            return getNTCTemperature();
        }
        return -1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x003d A:{SYNTHETIC, Splitter: B:27:0x003d} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0050 A:{Catch:{ Exception -> 0x0043 }} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0042 A:{SYNTHETIC, Splitter: B:30:0x0042} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getNTCTemperature() {
        Exception e;
        Throwable th;
        Throwable th2 = null;
        if (!this.isProductSupport || this.limitTemperatureNode == null) {
            return -1;
        }
        BufferedReader br = null;
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(this.limitTemperatureNode));
            try {
                int temperature = Integer.valueOf(br2.readLine()).intValue();
                if (br2 != null) {
                    try {
                        br2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (Exception e2) {
                        e = e2;
                        br = br2;
                    }
                } else {
                    if (temperature < 0) {
                        temperature = -1;
                    }
                    return temperature;
                }
            } catch (Throwable th4) {
                th = th4;
                br = br2;
                if (br != null) {
                    try {
                        br.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    try {
                        throw th2;
                    } catch (Exception e3) {
                        e = e3;
                        e.printStackTrace();
                        return -1;
                    }
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (br != null) {
            }
            if (th2 == null) {
            }
        }
    }
}
