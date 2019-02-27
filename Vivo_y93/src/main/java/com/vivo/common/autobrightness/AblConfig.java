package com.vivo.common.autobrightness;

import android.os.SystemProperties;
import android.util.Slog;
import com.vivo.common.provider.Weather;
import com.vivo.services.DeviceParaProvideService;
import java.io.FileInputStream;

public class AblConfig {
    public static final String ARG_DISABLE_DEBUG = "disabled";
    public static final String ARG_ENABLE_DEBUG = "enabled";
    private static final String BOARD_VERSION = "/sys/devs_list/board_version";
    public static final int BRIGHTNESS_MAP_HIGH = 150;
    public static final int BRIGHTNESS_MAP_LOW = 25;
    public static final int BRIGHTNESS_MAP_MAX = 256;
    public static final int BRIGHTNESS_MAP_MIDDLE = 100;
    public static final String CMD_DEBUG = "--debug";
    private static final int COLOR_BLACK = 1;
    private static final int COLOR_WITHE = 0;
    private static boolean DEBUG = SystemProperties.getBoolean(PROP_DEBUG_PMS_LIGHT, false);
    private static boolean IS_BBKLOG = false;
    public static final String KEY_CONFIG_BRIGHTNESS_LUMA = "BrightnessLuma";
    public static final String KEY_CONFIG_BRIGHTNESS_MEMORY = "BrightnessMemory";
    public static final String KEY_CONFIG_BRIGHTNESS_NORMAL = "BrightnessNormal";
    public static final String KEY_CONFIG_BRIGHTNESS_SUPER_POWER = "BrightnessSuperPower";
    public static final String KEY_CONFIG_LIGHT_MEMORY_LEVEL_DOWN = "LightMemoryLevelDown";
    public static final String KEY_CONFIG_LIGHT_MEMORY_LEVEL_UP = "LightMemoryLevelUp";
    public static final String KEY_CONFIG_LIGHT_NORMAL_LEVEL_DOWN = "LightNormalLevelDown";
    public static final String KEY_CONFIG_LIGHT_NORMAL_LEVEL_UP = "LightNormalLevelUp";
    public static final String KEY_CONFIG_LUMA_BOUNDARY = "LumaBoundary";
    public static final String KEY_CONFIG_WARNING_DEFAULT = "{WARNING-DEFAULT}";
    private static final String LCM_COLOR_PATH = "/sys/lcm/lcm_color";
    private static final String LCM_ID_PATH = "/sys/lcm/lcm_id";
    private static final String LCM_ID_PD1813 = "/sys/lcm/bl_hw_version";
    private static final String[] NeedRegMotiontList = new String[]{"pd1814", "pd1816"};
    public static final int PRODUCT_SOLUTION_MTK = 2;
    public static final int PRODUCT_SOLUTION_QCOM = 1;
    public static final String PROP_BOARD_PLATFORM = "ro.board.platform";
    public static final String PROP_BOARD_VERSION = "ro.vivo.project.board_version";
    public static final String PROP_DEBUG_PMS_LIGHT = "debug.pms.lightlog";
    private static final String PROP_FINGERPRINT_TYPE = "persist.sys.fptype";
    public static final String PROP_PRODUCT_MODEL = "ro.product.model.bbk";
    public static final String PROP_PRODUCT_VERSION = "ro.vivo.product.version";
    private static final String PROP_VALUE_PREFIX_UDFP = "udfp_";
    private static final String[] RawDataNeedExtraDelayList = new String[]{"pd1813b"};
    public static final String TAG_ERROR = "AutoBrightnessERROR:";
    public static final String TAG_FATAL = "AutoBrightnessFATAL:";
    public static final String TAG_WARNING = "AutoBrightnessWARNING:";
    private static final String[] UseUnderDisplayLightList = new String[]{"pd1805", "pd1806", "pd1809", "pd1814", "pd1816"};
    private static boolean hasConfigCamBrightModeParam = false;
    private static final String mBoardPlatform = SystemProperties.get(PROP_BOARD_PLATFORM, "unkown");
    private static final String mBoardVersion = SystemProperties.get(PROP_BOARD_VERSION, "unkown");
    public static int[] mBrightnessMap = new int[256];
    private static final String[] mBrightnessSceneRatioList = new String[]{"pd1728", "pd1801", "pd1805", "pd1806", "pd1809", "pd1813", "pd1814", "pd1816", "td1803", "pd1818"};
    private static int[] mCamBrightModeTarget = new int[]{-1};
    private static int[] mCamBrightModeThres = new int[]{-1};
    private static final boolean mDriverProxTempCali = isDriverProxTempCaliInner();
    private static final String[] mDriverProxTempCaliList = new String[]{"pd1805", "pd1806", "pd1809", "pd1813b"};
    private static final String[] mDualProximityList = new String[]{"pd1635", "pd1709", "pd1710", "pd1721", "vtd1702"};
    private static final String mFingerprintModuleName = SystemProperties.get(PROP_FINGERPRINT_TYPE, "unknown");
    private static final int mFirstFewSeconds = getFirstFewSecondsInner();
    private static final boolean mIsCollectAutobrightApplyHistory = isCollectAutobrightApplyHistoryInner();
    private static final boolean mIsUseBrightnessLevel = isUseBrightnessLevelInnger();
    private static boolean mIsUseBrightnessSceneRatio = isUseBrightnessSceneRatioInner();
    private static final boolean mIsUseDualProximity = isUseDualProximityInner();
    private static final boolean mIsUseInstantCali = isUseInstantCaliInner();
    private static final boolean mIsUseOLEDLcm = isUseOLEDLcmInner();
    private static final boolean mIsUseUDFingerprint = isUseUDFingerprintInner();
    private static final boolean mLcmAckChangeFlickering = isLcmAckChangeFlickeringInner();
    private static final String[] mLcmAckChangeFlickeringList = new String[]{"pd1510", "pd1628"};
    private static final int mLcmColor = getLcmColor();
    private static final String[] mLcmDimChangeFickeringList = new String[]{"pd1616", "pd1610", "pd1617", "td1605", "pd1621", "std1616", "td1608", "spd1706", "vtd1704f_ex"};
    private static final boolean mLcmDimChangeFlickering = isLcmDimChangeFickeringInner();
    private static final boolean mLcmFlickering = isLcmFlickeringInner();
    private static final String[] mLcmFlickeringList = new String[]{"pd1421", "pd1408", "pd1405"};
    private static final int mLcmThresholdHigh = getLcmThresholdHighInner();
    private static final int mLuxMedianBufferLen = getLuxMedianBufferLenInner();
    public static int mMaxHardwareBrightness = 255;
    private static final boolean mNeedRegMotion = isNeedRegMotionInner();
    private static final String[] mOLEDLcmList = new String[]{"pd1635"};
    private static final String mOpEntry = SystemProperties.get("ro.vivo.op.entry", "no");
    private static final String mProductBoard = SystemProperties.get("ro.product.board", "unkown");
    private static final int mProductSolution = updateProductSolutionInner();
    private static final boolean mRawDataNeedExtraDelay = isNeedExtraDelayInner();
    private static final boolean mUse2048GrayScaleBacklight = isUse2048GrayScaleBacklightInner();
    private static final String[] mUseBrigtnessLevelList = new String[]{"pd1621b", "vtd1704f_ex"};
    private static final String[] mUseInstantCaliList = new String[]{"pd1728", "pd1730", "pd1801", "pd1731", "pd1803", "pd1732", "pd1805", "pd1806", "pd1809", "pd1813", "pd1814", "pd1816", "td1803", "pd1818"};
    private static final boolean mUseUnderDisplayLight = isUseUnderDisplayLightInner();
    private static final String model = SystemProperties.get(PROP_PRODUCT_MODEL, "unkown").toLowerCase();
    private static final String[] use2048GrayScaleBacklightList = new String[]{"pd1401bl", "pd1619", "pd1635", "pd1708", "pd1709", "pd1710", "pd1705", "td1702", "pd1718", "pd1721", "pd1724", "vtd1702", "pd1728", "pd1729", "pd1730", "td1705", "pd1731", "td1704", "pd1803", "pd1801", "pd1805", "pd1806", "pd1732", "pd1809", "pd1813", "pd1814", "pd1816", "td1803", "pd1818"};

    static {
        if (mUse2048GrayScaleBacklight) {
            initMappingGrayScaleFrom2048To256();
        }
    }

    private static boolean isOpEntry() {
        if (mOpEntry == null || !mOpEntry.contains("CMCC") || (mOpEntry.contains("CMCC_SC") ^ 1) == 0 || (mOpEntry.contains("CMCC_RWB") ^ 1) == 0) {
            return false;
        }
        return true;
    }

    public static long proximityToNegtiveDebounce() {
        return 700;
    }

    public static boolean isUseLightSmooth() {
        return true;
    }

    public static int getLightSmoothThreshold() {
        return 8;
    }

    private static boolean isUse2048GrayScaleBacklightInner() {
        if (use2048GrayScaleBacklightList.length > 0) {
            for (String startsWith : use2048GrayScaleBacklightList) {
                if (model.startsWith(startsWith)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isUse2048GrayScaleBacklight() {
        return mUse2048GrayScaleBacklight;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0056 A:{SYNTHETIC, Splitter: B:21:0x0056} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0064 A:{SYNTHETIC, Splitter: B:28:0x0064} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean isPD1813() {
        Exception e;
        FileInputStream mInputStream = null;
        try {
            FileInputStream mInputStream2 = new FileInputStream(LCM_ID_PD1813);
            try {
                boolean flag;
                byte[] buf = new byte[10];
                String board_version = new String(buf, 0, mInputStream2.read(buf));
                Slog.e("AblConfig", "lcm id " + board_version);
                if (board_version.toCharArray()[1] == '1') {
                    flag = true;
                } else {
                    flag = false;
                }
                if (mInputStream2 != null) {
                    try {
                        mInputStream2.close();
                    } catch (Exception e2) {
                        System.out.println(e2);
                    }
                }
                return flag;
            } catch (Exception e3) {
                e = e3;
                mInputStream = mInputStream2;
                try {
                    System.out.println(e);
                    if (mInputStream != null) {
                    }
                    return false;
                } catch (Throwable th) {
                    if (mInputStream != null) {
                        try {
                            mInputStream.close();
                        } catch (Exception e22) {
                            System.out.println(e22);
                        }
                    }
                    return false;
                }
            } catch (Throwable th2) {
                mInputStream = mInputStream2;
                if (mInputStream != null) {
                }
                return false;
            }
        } catch (Exception e4) {
            e = e4;
            System.out.println(e);
            if (mInputStream != null) {
                try {
                    mInputStream.close();
                } catch (Exception e222) {
                    System.out.println(e222);
                }
            }
            return false;
        }
    }

    private static boolean isPD1732D() {
        try {
            FileInputStream mInputStream = new FileInputStream(BOARD_VERSION);
            byte[] buf = new byte[100];
            int len = mInputStream.read(buf);
            String board_version = new String(buf, 0, len);
            Slog.e("Sensor", "Light sensor board version: " + board_version + " len: " + len);
            char[] temp = board_version.toCharArray();
            mInputStream.close();
            if (temp[2] == '0') {
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public static int getLuxMedianBufferLen() {
        return mLuxMedianBufferLen;
    }

    private static int getLuxMedianBufferLenInner() {
        return 3;
    }

    private static int getFirstFewSecondsInner() {
        return Weather.WEATHERVERSION_ROM_3_0;
    }

    public static int getFirstFewSeconds() {
        return mFirstFewSeconds;
    }

    public static boolean isLcmFlickering() {
        return mLcmFlickering;
    }

    private static boolean isLcmFlickeringInner() {
        for (String x : mLcmFlickeringList) {
            if (model.startsWith(x)) {
                return true;
            }
        }
        return false;
    }

    private static int getLcmThresholdHighInner() {
        if (model.startsWith("pd1616") || model.startsWith("std1616")) {
            return 60;
        }
        if (model.startsWith("pd1624") || model.startsWith("vtd1703f_ex")) {
            return 2;
        }
        if (model.startsWith("pd1628") || model.startsWith("pd1613bf_ex")) {
            return 20;
        }
        return 90;
    }

    public static int getLcmThresholdHigh() {
        return mLcmThresholdHigh;
    }

    public static boolean isLcmDimChangeFickering() {
        return mLcmDimChangeFlickering;
    }

    private static boolean isLcmDimChangeFickeringInner() {
        for (String x : mLcmDimChangeFickeringList) {
            if (model.startsWith(x)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLcmAckChangeFlickering() {
        return mLcmAckChangeFlickering;
    }

    private static boolean isLcmAckChangeFlickeringInner() {
        for (String x : mLcmAckChangeFlickeringList) {
            if (model.startsWith(x)) {
                return true;
            }
        }
        return model.startsWith("pd1613") && (mBoardVersion.startsWith("10100") || mBoardVersion.startsWith("10110"));
    }

    public static int getObjectUncoverActionTimeout() {
        if (model.startsWith("pd1709") || model.startsWith("pd1710") || model.startsWith("pd1721") || model.startsWith("pd1610") || model.startsWith("vtd1702") || model.startsWith("pd1730") || model.startsWith("pd1731") || model.startsWith("pd1728") || model.startsWith("pd1732") || model.startsWith("pd1805") || model.startsWith("pd1806") || model.startsWith("pd1809") || model.startsWith("pd1814") || model.startsWith("pd1816") || model.startsWith("pd1813") || model.startsWith("pd1818") || model.startsWith("td1803")) {
            return 240;
        }
        return 200;
    }

    public static int getAngleDirectonType() {
        try {
            Class<?> sensorClass = Class.forName("android.hardware.Sensor");
            return sensorClass.getDeclaredField("TYPE_ANGLE_DIRECTION").getInt(sensorClass);
        } catch (Exception e) {
            e.printStackTrace();
            return 169;
        }
    }

    public static boolean isUseUDFingerprint() {
        return mIsUseUDFingerprint;
    }

    private static boolean isUseUDFingerprintInner() {
        return mFingerprintModuleName.startsWith(PROP_VALUE_PREFIX_UDFP);
    }

    public static boolean isUseInstantCali() {
        return mIsUseInstantCali;
    }

    private static boolean isUseInstantCaliInner() {
        if (mLcmColor == 1 && model.startsWith("pd1731")) {
            return false;
        }
        for (String x : mUseInstantCaliList) {
            if (model.startsWith(x)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDriverProxTempCali() {
        return mDriverProxTempCali;
    }

    private static boolean isDriverProxTempCaliInner() {
        for (String x : mDriverProxTempCaliList) {
            if (model.startsWith(x)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNeedExtraDelay() {
        return mRawDataNeedExtraDelay;
    }

    private static boolean isNeedExtraDelayInner() {
        for (String x : RawDataNeedExtraDelayList) {
            if (model.startsWith(x)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDebug() {
        return DEBUG;
    }

    public static boolean isBbkLog() {
        return IS_BBKLOG;
    }

    public static void setBbkLog(boolean status) {
        IS_BBKLOG = status;
    }

    public static void setDebug(String[] args) {
        if (args != null && args.length >= 2 && CMD_DEBUG.equals(args[0])) {
            if (ARG_ENABLE_DEBUG.equals(args[1])) {
                DEBUG = true;
            } else if (ARG_DISABLE_DEBUG.equals(args[1])) {
                DEBUG = false;
            }
        }
    }

    public static int getLcmColor() {
        String model = SystemProperties.get(PROP_PRODUCT_MODEL, "unkown").toLowerCase();
        String data = DeviceParaProvideService.readKernelData(LCM_ID_PATH);
        if (model == null || data == null) {
            return 0;
        }
        int ret = 0;
        if (model.startsWith("pd1610")) {
            if (data.trim().equals("22")) {
                ret = 1;
            }
        } else if (model.startsWith("pd1624") || model.equals("pd1616") || model.startsWith("pd1708") || model.startsWith("pd1718") || model.startsWith("vtd1703f_ex")) {
            if (DeviceParaProvideService.readKernelData(LCM_COLOR_PATH).trim().equals("01")) {
                ret = 1;
            }
        } else if (model.startsWith("pd1635") || model.equals("pd1616b") || model.startsWith("pd1619")) {
            if (DeviceParaProvideService.readKernelData(LCM_COLOR_PATH).trim().equals("02")) {
                ret = 1;
            }
        } else if (DeviceParaProvideService.readKernelData(LCM_COLOR_PATH).trim().equals("01")) {
            ret = 1;
        }
        return ret;
    }

    public static float getRectifiedLux(float light, String sensorName) {
        float rectifiedLux = light;
        float lightThreshold = Float.parseFloat(SystemProperties.get("persist.sys.light_threshold", "500"));
        if (lightThreshold <= 0.0f) {
            lightThreshold = 500.0f;
        }
        if (((double) light) == 1.0d) {
            return light;
        }
        if (SystemProperties.get("ro.hardware.bbk", "unknown").startsWith("PD1401CL")) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return ((light * 500.0f) * 0.87f) / lightThreshold;
            }
            return light;
        } else if (SystemProperties.get("ro.hardware.bbk", "unknown").startsWith("PD1501D")) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return ((light * 500.0f) * 1.16f) / lightThreshold;
            }
            return light;
        } else if (SystemProperties.get("ro.hardware.bbk", "unknown").startsWith("PD1421V") || SystemProperties.get("ro.hardware.bbk", "unknown").startsWith("PD1421F_EX") || (SystemProperties.get("ro.hardware.bbk", "unknown").startsWith("PD1421M") && sensorName.startsWith("APDS9920"))) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return ((light * 500.0f) * 1.29f) / lightThreshold;
            }
            return light;
        } else if (SystemProperties.get("ro.hardware.bbk", "unknown").startsWith("PD1523") && sensorName.startsWith("APDS9922")) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return ((light * 500.0f) * 0.77f) / lightThreshold;
            }
            return light;
        } else if (SystemProperties.get("ro.hardware.bbk", "unknown").startsWith("PD1613F_EX") && sensorName.startsWith("APDS-9922")) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return ((light * 500.0f) * 1.3f) / lightThreshold;
            }
            return light;
        } else if (model.equals("pd1616") && mLcmColor == 1) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return ((light * 500.0f) * 0.794f) / lightThreshold;
            }
            return light;
        } else if (model.equals("pd1616b") && mLcmColor == 1) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return ((light * 500.0f) * 0.81f) / lightThreshold;
            }
            return light;
        } else if (model.startsWith("pd1619") && mLcmColor == 1) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return ((light * 500.0f) * 0.74f) / lightThreshold;
            }
            return light;
        } else if (model.startsWith("pd1635") && mLcmColor == 1) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return ((light * 500.0f) * 0.75f) / lightThreshold;
            }
            return light;
        } else if (model.startsWith("pd1708") && mLcmColor == 1) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return ((light * 500.0f) * 0.892f) / lightThreshold;
            }
            return light;
        } else if (model.startsWith("pd1709") && mLcmColor == 1) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return ((light * 500.0f) * 0.93f) / lightThreshold;
            }
            return light;
        } else if (model.startsWith("pd1718a") && mLcmColor == 1) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return ((light * 500.0f) * 0.84f) / lightThreshold;
            }
            return light;
        } else if ((model.startsWith("pd1710") || model.startsWith("pd1721") || model.startsWith("vtd1702")) && mLcmColor == 1) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return ((light * 500.0f) * 0.88f) / lightThreshold;
            }
            return light;
        } else if (model.startsWith("pd1718") && mLcmColor == 1) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return ((light * 500.0f) * 0.84f) / lightThreshold;
            }
            return light;
        } else if (model.equals("pd1731")) {
            if (sensorName.startsWith("TMD4903")) {
                if (mLcmColor == 1) {
                    return ((light * 500.0f) * 0.74f) / lightThreshold;
                }
                return light;
            } else if (!sensorName.startsWith("stk3x1x")) {
                return rectifiedLux;
            } else {
                if (mLcmColor == 1) {
                    return ((light * 500.0f) * 0.72f) / lightThreshold;
                }
                return ((light * 500.0f) * 0.77f) / lightThreshold;
            }
        } else if ((!model.startsWith("pd1731c") && !model.startsWith("pd1731e")) || mLcmColor != 1) {
            return (light * 500.0f) / lightThreshold;
        } else {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return ((light * 500.0f) * 0.74f) / lightThreshold;
            }
            return light;
        }
    }

    public static float getRectifiedCoefficient(String sensorName) {
        if (SystemProperties.get("ro.hardware.bbk", "unknown").startsWith("PD1401CL")) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return 0.87f;
            }
            return 1.0f;
        } else if (SystemProperties.get("ro.hardware.bbk", "unknown").startsWith("PD1501D")) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return 1.16f;
            }
            return 1.0f;
        } else if (SystemProperties.get("ro.hardware.bbk", "unknown").startsWith("PD1421V") || SystemProperties.get("ro.hardware.bbk", "unknown").startsWith("PD1421F_EX") || (SystemProperties.get("ro.hardware.bbk", "unknown").startsWith("PD1421M") && sensorName.startsWith("APDS9920"))) {
            if (SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
                return 1.29f;
            }
            return 1.0f;
        } else if (SystemProperties.get("ro.hardware.bbk", "unknown").startsWith("PD1523") && sensorName.startsWith("APDS9922") && SystemProperties.get("persist.sys.als_cali_flag", "0").equals("1")) {
            return 0.77f;
        } else {
            return 1.0f;
        }
    }

    public static int getTargetGrayScaleForPD1619(int brightness) {
        int newBrightness;
        if (brightness == 0) {
            newBrightness = 0;
        } else if (brightness == 2) {
            newBrightness = 615;
        } else if (brightness == 3) {
            newBrightness = 756;
        } else if (brightness == 4) {
            newBrightness = 840;
        } else if (brightness > 4 && brightness <= 30) {
            newBrightness = (int) ((Math.pow(2.718281828459045d, ((double) brightness) * 0.0051d) * 1279.0d) - (Math.pow(2.718281828459045d, ((double) brightness) * -0.1366d) * 788.1d));
        } else if (brightness > 30 && brightness <= 130) {
            newBrightness = (int) Math.ceil((Math.pow(2.718281828459045d, ((double) brightness) * 1.222E-4d) * 1922.0d) - (Math.pow(2.718281828459045d, ((double) brightness) * -0.01339d) * 659.7d));
        } else if (brightness > 130) {
            newBrightness = (int) Math.ceil((((((double) brightness) * -0.005385d) * ((double) brightness)) + (((double) brightness) * 3.839d)) + 1430.0d);
        } else {
            newBrightness = 615;
        }
        if (newBrightness > 2047) {
            return 2047;
        }
        return newBrightness;
    }

    public static int getTargetGrayScaleForPD1708(int brightness) {
        int newBrightness;
        if (brightness == 0) {
            newBrightness = 0;
        } else if (brightness <= 2) {
            newBrightness = 530;
        } else if (brightness == 3) {
            newBrightness = 690;
        } else if (brightness == 4) {
            newBrightness = 840;
        } else if (brightness > 4 && brightness <= 30) {
            newBrightness = (int) ((Math.pow(2.718281828459045d, ((double) brightness) * 0.0051d) * 1279.0d) - (Math.pow(2.718281828459045d, ((double) brightness) * -0.1366d) * 788.1d));
        } else if (brightness > 30 && brightness <= 130) {
            newBrightness = (int) Math.ceil((Math.pow(2.718281828459045d, ((double) brightness) * 1.222E-4d) * 1922.0d) - (Math.pow(2.718281828459045d, ((double) brightness) * -0.01339d) * 659.7d));
        } else if (brightness > 130) {
            newBrightness = (int) Math.ceil((((((double) brightness) * -0.005385d) * ((double) brightness)) + (((double) brightness) * 3.839d)) + 1430.0d);
        } else {
            newBrightness = 615;
        }
        if (newBrightness > 2047) {
            return 2047;
        }
        return newBrightness;
    }

    public static int getTargetGrayScaleForPD1813(int brightness) {
        int newBrightness;
        if (brightness == 0) {
            newBrightness = 0;
        } else if (brightness <= 2) {
            newBrightness = 530;
        } else if (brightness == 3) {
            newBrightness = 705;
        } else if (brightness == 4) {
            newBrightness = 810;
        } else if (brightness > 4 && brightness <= 32) {
            newBrightness = (int) ((((((((double) brightness) * 0.05364d) * ((double) brightness)) * ((double) brightness)) - ((((double) brightness) * 3.775d) * ((double) brightness))) + (((double) brightness) * 98.13d)) + 481.9d);
        } else if (brightness > 32 && brightness <= 111) {
            newBrightness = (int) ((((((double) brightness) * -0.02778d) * ((double) brightness)) + (((double) brightness) * 7.813d)) + 1297.0d);
        } else if (brightness > 111) {
            newBrightness = (int) ((((double) brightness) * 1.5664d) + 1648.0d);
        } else {
            newBrightness = 530;
        }
        if (newBrightness > 2047) {
            return 2047;
        }
        return newBrightness;
    }

    public static int getTargetGrayScaleForPD1813_2(int brightness) {
        int newBrightness;
        if (brightness == 0) {
            newBrightness = 0;
        } else if (brightness <= 2) {
            newBrightness = 256;
        } else if (brightness == 3) {
            newBrightness = 394;
        } else if (brightness == 4) {
            newBrightness = 620;
        } else if (brightness > 4 && brightness <= 32) {
            newBrightness = (int) ((((((((double) brightness) * 0.05899d) * ((double) brightness)) * ((double) brightness)) - ((((double) brightness) * 4.186d) * ((double) brightness))) + (((double) brightness) * 110.6d)) + 274.7d);
        } else if (brightness > 32 && brightness <= 111) {
            newBrightness = (int) ((((((double) brightness) * -0.03022d) * ((double) brightness)) + (((double) brightness) * 8.795d)) + 1206.0d);
        } else if (brightness > 111) {
            newBrightness = (int) ((((double) brightness) * 1.65d) + 1626.0d);
        } else {
            newBrightness = 256;
        }
        if (newBrightness > 2047) {
            return 2047;
        }
        return newBrightness;
    }

    public static int getTargetGrayScaleForPD1731(int brightness) {
        int newBrightness;
        if (brightness == 0) {
            newBrightness = 0;
        } else if (brightness <= 2) {
            newBrightness = 20;
        } else if (brightness == 3) {
            newBrightness = 30;
        } else if (brightness == 4) {
            newBrightness = 37;
        } else if (brightness <= 32) {
            newBrightness = (int) ((((double) brightness) * 16.56d) - 21.32d);
        } else if (brightness <= 111) {
            newBrightness = (int) ((((double) brightness) * 12.34d) + 108.0d);
        } else {
            newBrightness = (int) ((((double) brightness) * 18.21d) - 548.0d);
        }
        if (newBrightness > Weather.WEATHERVERSION_ROM_3_5) {
            return Weather.WEATHERVERSION_ROM_3_5;
        }
        return newBrightness;
    }

    public static int getTargetGrayScaleForPD1732C(int brightness) {
        int newBrightness;
        if (brightness == 0) {
            newBrightness = 0;
        } else if (brightness <= 2) {
            newBrightness = 19;
        } else if (brightness == 3) {
            newBrightness = 41;
        } else if (brightness == 4) {
            newBrightness = 57;
        } else if (brightness <= 32) {
            newBrightness = (int) ((((double) brightness) * 29.56d) - 42.8d);
        } else if (brightness <= 111) {
            newBrightness = (int) ((((double) brightness) * 23.21d) + 160.69d);
        } else {
            newBrightness = (int) ((((double) brightness) * 9.29d) + 1726.5d);
        }
        if (newBrightness > 4095) {
            return 4095;
        }
        return newBrightness;
    }

    public static int getTargetGrayScaleForPD1635(int brightness) {
        int newBrightness;
        if (brightness == 0) {
            newBrightness = 0;
        } else if (brightness == 1 || brightness == 2) {
            newBrightness = 3;
        } else {
            newBrightness = (((brightness - 2) * 1020) / 253) + 3;
        }
        if (newBrightness > 1023) {
            return 1023;
        }
        return newBrightness;
    }

    public static int getTargetGrayScaleForPD1709(int brightness) {
        int newBrightness;
        if (brightness == 0) {
            newBrightness = 0;
        } else if (brightness == 1 || brightness == 2) {
            newBrightness = 2;
        } else if (brightness == 3) {
            newBrightness = 26;
        } else if (brightness == 4) {
            newBrightness = 65;
        } else if (brightness <= 35) {
            newBrightness = (int) ((((((((double) brightness) * 0.004793d) * ((double) brightness)) * ((double) brightness)) - ((((double) brightness) * 0.3776d) * ((double) brightness))) + (((double) brightness) * 13.14d)) + 20.96d);
        } else if (brightness <= 70) {
            newBrightness = (int) ((((double) brightness) * 2.391d) + 137.8d);
        } else {
            newBrightness = (int) ((((double) brightness) * 3.8967d) + 29.5d);
        }
        if (newBrightness > 1023) {
            return 1023;
        }
        return newBrightness;
    }

    public static int getTargetGrayScaleForPD1728(int brightness) {
        int newBrightness;
        if (brightness == 0) {
            newBrightness = 0;
        } else if (brightness == 1 || brightness == 2) {
            newBrightness = 2;
        } else if (brightness == 3) {
            newBrightness = 24;
        } else if (brightness == 4) {
            newBrightness = 48;
        } else if (brightness == 5) {
            newBrightness = 74;
        } else if (brightness <= 32) {
            newBrightness = (int) ((((((double) brightness) * -0.09416d) * ((double) brightness)) + (((double) brightness) * 8.48d)) + 46.72d);
        } else if (brightness <= 111) {
            newBrightness = (int) ((((((double) brightness) * 0.02064d) * ((double) brightness)) - (((double) brightness) * 0.1402d)) + 215.1d);
        } else {
            newBrightness = (int) ((((((double) brightness) * 0.001131d) * ((double) brightness)) + (((double) brightness) * 3.545d)) + 46.43d);
        }
        if (newBrightness > 1023) {
            return 1023;
        }
        return newBrightness;
    }

    public static int getTargetGrayScaleForPD1801(int brightness) {
        int newBrightness;
        if (brightness == 0) {
            newBrightness = 0;
        } else if (brightness == 1 || brightness == 2) {
            newBrightness = 2;
        } else if (brightness == 3) {
            newBrightness = 36;
        } else if (brightness == 4) {
            newBrightness = 75;
        } else if (brightness == 5) {
            newBrightness = 85;
        } else if (brightness <= 32) {
            newBrightness = (int) ((((((double) brightness) * -0.1099d) * ((double) brightness)) + (((double) brightness) * 9.148d)) + 45.37d);
        } else if (brightness <= 111) {
            newBrightness = (int) ((((((double) brightness) * 0.01943d) * ((double) brightness)) + (((double) brightness) * 0.116d)) + 209.6d);
        } else {
            newBrightness = (int) ((((((double) brightness) * -4.161E-5d) * ((double) brightness)) + (((double) brightness) * 4.127d)) + 0.01183d);
        }
        if (newBrightness > 1023) {
            return 1023;
        }
        return newBrightness;
    }

    public static int getTargetGrayScaleForPD1730(int brightness) {
        int newBrightness;
        if (brightness == 0) {
            newBrightness = 0;
        } else if (brightness == 1 || brightness == 2) {
            newBrightness = 560;
        } else if (brightness == 3) {
            newBrightness = 720;
        } else if (brightness == 4) {
            newBrightness = 850;
        } else if (brightness == 5) {
            newBrightness = 950;
        } else if (brightness <= 31) {
            newBrightness = (int) ((((((((double) brightness) * 0.06504d) * ((double) brightness)) * ((double) brightness)) - ((((double) brightness) * 4.344d) * ((double) brightness))) + (((double) brightness) * 106.4d)) + 515.7d);
        } else if (brightness <= 111) {
            newBrightness = (int) ((((((double) brightness) * -0.02757d) * ((double) brightness)) + (((double) brightness) * 7.794d)) + 1361.0d);
        } else {
            newBrightness = (int) ((((double) brightness) * 1.098d) + 1768.0d);
        }
        if (newBrightness > 2047) {
            return 2047;
        }
        return newBrightness;
    }

    public static int getTargetGrayScaleForPD1805(int brightness) {
        int newBrightness;
        if (brightness <= 0) {
            newBrightness = 0;
        } else if (brightness == 1 || brightness == 2) {
            newBrightness = 2;
        } else if (brightness == 3) {
            newBrightness = 27;
        } else if (brightness == 4) {
            newBrightness = 61;
        } else if (brightness <= 32) {
            newBrightness = (int) ((((((double) brightness) * -0.1203d) * ((double) brightness)) + (((double) brightness) * 8.93d)) + 33.03d);
        } else if (brightness <= 111) {
            newBrightness = (int) ((((double) brightness) * 3.328d) + 89.23d);
        } else {
            newBrightness = (int) ((((double) brightness) * 3.9231d) + 22.63d);
        }
        if (newBrightness > 1023) {
            return 1023;
        }
        return newBrightness;
    }

    public static int getTargetGrayScaleForPD1731C(int brightness) {
        int newBrightness;
        if (brightness == 0) {
            newBrightness = 0;
        } else if (brightness <= 2) {
            newBrightness = 20;
        } else if (brightness == 3) {
            newBrightness = 43;
        } else if (brightness == 4) {
            newBrightness = 69;
        } else if (brightness == 5) {
            newBrightness = 97;
        } else if (brightness <= 31) {
            newBrightness = (int) ((((double) brightness) * 25.79d) - 27.35d);
        } else if (brightness <= 111) {
            newBrightness = (int) ((((double) brightness) * 19.8d) + 154.5d);
        } else {
            newBrightness = (int) ((((double) brightness) * 12.049d) + 1022.6d);
        }
        if (newBrightness > 4095) {
            return 4095;
        }
        return newBrightness;
    }

    public static int getTargetGrayScaleForPD1818(int brightness) {
        int newBrightness;
        if (brightness == 0) {
            newBrightness = 0;
        } else if (brightness <= 2) {
            newBrightness = 27;
        } else if (brightness == 3) {
            newBrightness = 55;
        } else if (brightness == 4) {
            newBrightness = 81;
        } else if (brightness == 5) {
            newBrightness = 97;
        } else if (brightness <= 31) {
            newBrightness = (int) ((((double) brightness) * 22.94d) - 19.33d);
        } else if (brightness <= 111) {
            newBrightness = (int) ((((double) brightness) * 16.85d) + 166.0d);
        } else {
            newBrightness = (int) ((((double) brightness) * 14.11d) + 497.0d);
        }
        if (newBrightness > 4095) {
            return 4095;
        }
        return newBrightness;
    }

    public static int getMapping2048GrayScaleFrom256GrayScale(int brightness) {
        if (!mUse2048GrayScaleBacklight) {
            return brightness;
        }
        if (model.startsWith("pd1619") || model.startsWith("pd1705")) {
            return getTargetGrayScaleForPD1619(brightness);
        }
        if (model.startsWith("pd1732")) {
            if (isPD1732D()) {
                return getTargetGrayScaleForPD1732C(brightness);
            }
            return getTargetGrayScaleForPD1708(brightness);
        } else if (model.startsWith("pd1708") || model.startsWith("td1702") || model.startsWith("pd1718") || model.startsWith("pd1803")) {
            return getTargetGrayScaleForPD1708(brightness);
        } else {
            if (model.startsWith("pd1635")) {
                return getTargetGrayScaleForPD1635(brightness);
            }
            if (model.startsWith("pd1709") || model.startsWith("pd1710") || model.startsWith("pd1721") || model.startsWith("pd1724") || model.startsWith("vtd1702") || model.startsWith("td1705") || model.startsWith("td1704")) {
                return getTargetGrayScaleForPD1709(brightness);
            }
            if (model.startsWith("pd1728") || model.startsWith("pd1729")) {
                return getTargetGrayScaleForPD1728(brightness);
            }
            if (model.startsWith("pd1801")) {
                return getTargetGrayScaleForPD1801(brightness);
            }
            if (model.startsWith("pd1730")) {
                return getTargetGrayScaleForPD1730(brightness);
            }
            if (model.startsWith("pd1731c") || model.startsWith("pd1731e")) {
                return getTargetGrayScaleForPD1731C(brightness);
            }
            if (model.equals("pd1731")) {
                return getTargetGrayScaleForPD1731(brightness);
            }
            if (model.startsWith("pd1805") || model.startsWith("pd1806") || model.startsWith("pd1809") || model.startsWith("pd1814") || model.startsWith("pd1816")) {
                return getTargetGrayScaleForPD1805(brightness);
            }
            if (model.startsWith("pd1813")) {
                if (isPD1813()) {
                    return getTargetGrayScaleForPD1813_2(brightness);
                }
                return getTargetGrayScaleForPD1813(brightness);
            } else if (model.startsWith("td1803") || model.startsWith("pd1818")) {
                return getTargetGrayScaleForPD1818(brightness);
            } else {
                return brightness;
            }
        }
    }

    public static void initMappingGrayScaleFrom2048To256() {
        if (model.startsWith("pd1619") || model.startsWith("pd1708") || model.startsWith("pd1705") || model.startsWith("td1702") || model.startsWith("pd1718") || model.startsWith("pd1730") || model.startsWith("pd1803") || model.startsWith("pd1813")) {
            mMaxHardwareBrightness = 2047;
        } else if (model.startsWith("pd1635") || model.startsWith("pd1709") || model.startsWith("pd1710") || model.startsWith("vtd1702") || model.startsWith("pd1721") || model.startsWith("pd1724") || model.startsWith("pd1728") || model.startsWith("pd1729") || model.startsWith("td1705") || model.startsWith("td1704") || model.startsWith("pd1801") || model.startsWith("pd1805") || model.startsWith("pd1806") || model.startsWith("pd1809") || model.startsWith("pd1814") || model.startsWith("pd1816")) {
            mMaxHardwareBrightness = 1023;
        } else if (model.startsWith("pd1731") || model.startsWith("td1803") || model.startsWith("pd1818")) {
            mMaxHardwareBrightness = 4095;
        } else if (model.startsWith("pd1732")) {
            if (isPD1732D()) {
                mMaxHardwareBrightness = 4095;
            } else {
                mMaxHardwareBrightness = 2047;
            }
        }
        for (int tmp = 0; tmp < 256; tmp++) {
            mBrightnessMap[tmp] = getMapping2048GrayScaleFrom256GrayScale(tmp);
            if (mBrightnessMap[tmp] > mMaxHardwareBrightness) {
                mBrightnessMap[tmp] = mMaxHardwareBrightness;
            }
        }
    }

    public static int getMapping2048GrayScaleTo256GrayScaleRestore(int brightness) {
        if (!mUse2048GrayScaleBacklight) {
            return brightness;
        }
        int mapBegin;
        int mapEnd;
        if (brightness < mBrightnessMap[25]) {
            mapBegin = 0;
            mapEnd = 25;
        } else if (brightness < mBrightnessMap[100]) {
            mapBegin = 25;
            mapEnd = 100;
        } else if (brightness < mBrightnessMap[BRIGHTNESS_MAP_HIGH]) {
            mapBegin = 100;
            mapEnd = BRIGHTNESS_MAP_HIGH;
        } else {
            mapBegin = BRIGHTNESS_MAP_HIGH;
            mapEnd = 255;
        }
        int newBrightness = mapEnd;
        while (newBrightness >= mapBegin) {
            if (brightness != mBrightnessMap[newBrightness]) {
                if (brightness > mBrightnessMap[newBrightness]) {
                    break;
                }
                newBrightness--;
            } else {
                newBrightness--;
                break;
            }
        }
        newBrightness++;
        if (newBrightness < 0) {
            newBrightness = 0;
        }
        if (newBrightness > 255) {
            newBrightness = 255;
        }
        return newBrightness;
    }

    public static int getBrightThresholdForCamOptimize() {
        return getMapping2048GrayScaleFrom256GrayScale(51);
    }

    public static int getBrightThresholdForCamOptimize(int brightnessOnFullScale) {
        if (hasConfigCamBrightModeParam) {
            return getSpecificBrightThresholdForCamOpimize(brightnessOnFullScale);
        }
        return getDefaultBrightThresholdForCamOptimize(brightnessOnFullScale);
    }

    private static int getDefaultBrightThresholdForCamOptimize(int brightnessOnFullScale) {
        int target;
        int brightness = getMapping2048GrayScaleTo256GrayScaleRestore(brightnessOnFullScale);
        if (brightness <= 18) {
            target = 52;
        } else if (brightness <= 33) {
            target = 75;
        } else if (brightness <= 54) {
            target = 137;
        } else if (brightness <= 87) {
            target = 166;
        } else if (brightness <= 134) {
            target = 197;
        } else if (brightness <= 166) {
            target = 215;
        } else {
            target = 255;
        }
        return getMapping2048GrayScaleFrom256GrayScale(target);
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0014  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int getSpecificBrightThresholdForCamOpimize(int brightnessOnFullScale) {
        int brightness = getMapping2048GrayScaleTo256GrayScaleRestore(brightnessOnFullScale);
        int target = brightness;
        try {
            if (!(mCamBrightModeThres == null || mCamBrightModeTarget == null)) {
                if (mCamBrightModeThres.length >= 1 && mCamBrightModeTarget.length >= 1 && !((mCamBrightModeThres.length == 1 && mCamBrightModeThres[0] == -1) || (mCamBrightModeTarget.length == 1 && mCamBrightModeTarget[0] == -1))) {
                    int idx = binarySearchRange(mCamBrightModeThres, brightness);
                    if (idx >= mCamBrightModeTarget.length) {
                        target = 255;
                    } else if (idx == mCamBrightModeTarget.length - 1) {
                        target = mCamBrightModeTarget[idx];
                    } else {
                        target = mCamBrightModeTarget[idx];
                    }
                    if (target > 255) {
                        target = 255;
                    }
                    return getMapping2048GrayScaleFrom256GrayScale(target);
                }
            }
            target = brightness;
            if (target > 255) {
            }
            return getMapping2048GrayScaleFrom256GrayScale(target);
        } catch (Exception e) {
            e.printStackTrace();
            if (brightness > 255) {
                target = 255;
            }
            return getMapping2048GrayScaleFrom256GrayScale(target);
        } catch (Throwable th) {
            if (brightness > 255) {
                target = 255;
            }
            return getMapping2048GrayScaleFrom256GrayScale(target);
        }
    }

    private static int binarySearchRange(int[] A, int target) {
        int low = 0;
        int high = A.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (A[mid] > target) {
                high = mid - 1;
            } else if (A[mid] <= target) {
                low = mid + 1;
            }
        }
        return low;
    }

    public static void setCamBrightModeParam(int[] thresArray, int[] targetArray) {
        mCamBrightModeThres = thresArray;
        mCamBrightModeTarget = targetArray;
        hasConfigCamBrightModeParam = true;
    }

    public static boolean isCollectDataVer2nd() {
        return true;
    }

    public static boolean isUseBrightnessSceneRatio() {
        return mIsUseBrightnessSceneRatio;
    }

    public static void setUseBrightnessSceneRatio(boolean use) {
        mIsUseBrightnessSceneRatio = use;
    }

    private static boolean isUseBrightnessSceneRatioInner() {
        int i = 0;
        boolean ret = SystemProperties.getBoolean("debug.abl.sceneratio", false);
        String[] strArr = mBrightnessSceneRatioList;
        int length = strArr.length;
        while (i < length) {
            if (model.startsWith(strArr[i])) {
                return true;
            }
            i++;
        }
        return ret;
    }

    public static boolean isUseBrightnessLevel() {
        return mIsUseBrightnessLevel;
    }

    private static boolean isUseBrightnessLevelInnger() {
        for (String x : mUseBrigtnessLevelList) {
            if (model.startsWith(x)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUseOLEDLcmInner() {
        for (String x : mOLEDLcmList) {
            if (model.startsWith(x)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUseOLEDLcm() {
        return mIsUseOLEDLcm;
    }

    public static boolean isCollectAutobrightApplyHistory() {
        return mIsCollectAutobrightApplyHistory;
    }

    private static boolean isCollectAutobrightApplyHistoryInner() {
        return true;
    }

    private static boolean isUseDualProximityInner() {
        for (String x : mDualProximityList) {
            if (model.startsWith(x)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUseDualProximity() {
        return mIsUseDualProximity;
    }

    private static boolean isUseUnderDisplayLightInner() {
        if (UseUnderDisplayLightList.length > 0) {
            for (String startsWith : UseUnderDisplayLightList) {
                if (model.startsWith(startsWith)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isUseUnderDisplayLight() {
        return mUseUnderDisplayLight;
    }

    private static boolean isNeedRegMotionInner() {
        if (NeedRegMotiontList.length > 0) {
            for (String startsWith : NeedRegMotiontList) {
                if (model.startsWith(startsWith)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isNeedRegMotion() {
        return mNeedRegMotion;
    }

    private static int updateProductSolutionInner() {
        String platform = SystemProperties.get("ro.vivo.product.solution", "unkown").toLowerCase();
        if ("qcom".equals(platform)) {
            return 1;
        }
        if ("mtk".equals(platform)) {
            return 2;
        }
        return 1;
    }

    public static int getProductSolution() {
        return mProductSolution;
    }
}
