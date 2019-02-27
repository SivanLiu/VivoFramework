package com.vivo.vivogamesdk;

import android.os.SystemProperties;
import android.util.Log;
import com.vivo.common.autobrightness.AblConfig;
import com.vivo.common.provider.Weather;
import java.io.BufferedReader;
import java.io.FileReader;

public class PlatformMT6750 implements VivoPlatform {
    private static final String CPU_TEMPERATURE_NODE = "/sys/devices/virtual/thermal/thermal_zone1/temp";
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.log.ctrl", false);
    private static final int FLL_BAD = 3;
    private static final int FLL_ERROR = -1;
    private static final int FLL_MEDIUM = 2;
    private static final int FLL_NONE = 0;
    private static final int FLL_UPCOMING = 1;
    private static final int LIMIT_CPU_TEMPERATURE_BAD_PD1612 = 68000;
    private static final int LIMIT_CPU_TEMPERATURE_MID_PD1612 = 65000;
    private static final int LIMIT_CPU_TEMPERATURE_UP_PD1612 = 62000;
    private static final int LIMIT_NTC_TEMPERATURE_PD1612 = 46000;
    private static final String NTC_TEMPERATURE_NODE = "/sys/devices/virtual/thermal/thermal_zone5/temp";
    private static final String PRODUCT_MODEL_BBK_PD1612 = "PD1612";
    private static final String TAG = "VivoGameSDK";
    private boolean isProductSupport;
    private int limitBadLevelTemperature = -1;
    private String limitCPUTemperatureNode;
    private int limitMediumLevelTemperature = -1;
    private int limitNTCTemperature = -1;
    private String limitNTCTemperatureNode;
    private int limitUpcomingLevelTemperature = -1;

    public PlatformMT6750() {
        if (SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, "unknown").equals(PRODUCT_MODEL_BBK_PD1612)) {
            this.isProductSupport = true;
            this.limitNTCTemperatureNode = NTC_TEMPERATURE_NODE;
            this.limitCPUTemperatureNode = CPU_TEMPERATURE_NODE;
            this.limitNTCTemperature = LIMIT_NTC_TEMPERATURE_PD1612;
            this.limitUpcomingLevelTemperature = LIMIT_CPU_TEMPERATURE_UP_PD1612;
            this.limitMediumLevelTemperature = LIMIT_CPU_TEMPERATURE_MID_PD1612;
            this.limitBadLevelTemperature = LIMIT_CPU_TEMPERATURE_BAD_PD1612;
            return;
        }
        this.isProductSupport = false;
    }

    public boolean getProductSupport() {
        return this.isProductSupport;
    }

    public int getFreqLimitLevel() {
        if (!this.isProductSupport) {
            return -1;
        }
        int limitLevel;
        int NTCTemperature = getNTCTemperature();
        int CPUTemperature = getCPUTemperature();
        if (DEBUG) {
            Log.d(TAG, "NTCTemperature = " + NTCTemperature + ", " + " CPUTemperature = " + CPUTemperature + ".");
        }
        if (NTCTemperature < 0 || CPUTemperature < 0) {
            limitLevel = -1;
        } else if (NTCTemperature < this.limitNTCTemperature) {
            limitLevel = 0;
        } else if (CPUTemperature < this.limitUpcomingLevelTemperature) {
            limitLevel = 0;
        } else if (CPUTemperature < this.limitMediumLevelTemperature) {
            limitLevel = 1;
        } else if (CPUTemperature < this.limitBadLevelTemperature) {
            limitLevel = 2;
        } else {
            limitLevel = 3;
        }
        return limitLevel;
    }

    public int getPhoneTemperature() {
        if (!this.isProductSupport) {
            return -1;
        }
        int temperature = getCPUTemperature();
        if (temperature > 0) {
            return temperature / Weather.WEATHERVERSION_ROM_2_0;
        }
        return -1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x003d A:{SYNTHETIC, Splitter: B:27:0x003d} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0050 A:{Catch:{ Exception -> 0x0043 }} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0042 A:{SYNTHETIC, Splitter: B:30:0x0042} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getCPUTemperature() {
        Exception e;
        Throwable th;
        Throwable th2 = null;
        if (!this.isProductSupport || this.limitCPUTemperatureNode == null) {
            return -1;
        }
        BufferedReader br = null;
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(this.limitCPUTemperatureNode));
            try {
                int temperature = Integer.valueOf(br2.readLine()).intValue();
                if (br2 != null) {
                    try {
                        br2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 == null) {
                    return temperature;
                }
                try {
                    throw th2;
                } catch (Exception e2) {
                    e = e2;
                    br = br2;
                }
            } catch (Throwable th4) {
                th = th4;
                br = br2;
                if (br != null) {
                }
                if (th2 == null) {
                }
            }
        } catch (Throwable th5) {
            th = th5;
            if (br != null) {
                try {
                    br.close();
                } catch (Throwable th6) {
                    if (th2 == null) {
                        th2 = th6;
                    } else if (th2 != th6) {
                        th2.addSuppressed(th6);
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
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x003d A:{SYNTHETIC, Splitter: B:27:0x003d} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0050 A:{Catch:{ Exception -> 0x0043 }} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0042 A:{SYNTHETIC, Splitter: B:30:0x0042} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getNTCTemperature() {
        Exception e;
        Throwable th;
        Throwable th2 = null;
        if (!this.isProductSupport || this.limitNTCTemperatureNode == null) {
            return -1;
        }
        BufferedReader br = null;
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(this.limitNTCTemperatureNode));
            try {
                int temperature = Integer.valueOf(br2.readLine()).intValue();
                if (br2 != null) {
                    try {
                        br2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 == null) {
                    return temperature;
                }
                try {
                    throw th2;
                } catch (Exception e2) {
                    e = e2;
                    br = br2;
                }
            } catch (Throwable th4) {
                th = th4;
                br = br2;
                if (br != null) {
                }
                if (th2 == null) {
                }
            }
        } catch (Throwable th5) {
            th = th5;
            if (br != null) {
                try {
                    br.close();
                } catch (Throwable th6) {
                    if (th2 == null) {
                        th2 = th6;
                    } else if (th2 != th6) {
                        th2.addSuppressed(th6);
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
    }
}
