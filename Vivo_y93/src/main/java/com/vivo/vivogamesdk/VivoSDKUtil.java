package com.vivo.vivogamesdk;

import android.os.SystemProperties;
import android.util.Log;

public class VivoSDKUtil {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.log.ctrl", false);
    private static final String PRODUCT_PLATFORM_MSM8939 = "QCOM8939";
    private static final String PRODUCT_PLATFORM_MSM8976 = "QCOM8976";
    private static final String PRODUCT_PLATFORM_MSM8996 = "QCOM8996";
    private static final String PRODUCT_PLATFORM_MT6750 = "MTK6750";
    private static final String SDKVersion = "V1.0.0";
    private static final String TAG = "VivoGameSDK";
    private static volatile VivoSDKUtil instance;
    private VivoPlatform mPlatform = null;

    public static VivoSDKUtil getInstance() {
        if (instance == null) {
            synchronized (VivoSDKUtil.class) {
                if (instance == null) {
                    instance = new VivoSDKUtil();
                }
            }
        }
        return instance;
    }

    private VivoSDKUtil() {
        String platform = SystemProperties.get("ro.vivo.product.platform", "unknown");
        if (DEBUG) {
            Log.d(TAG, "platform is " + platform + ".");
        }
        if (platform.equals(PRODUCT_PLATFORM_MSM8939)) {
            this.mPlatform = new PlatformMSM8939();
        } else if (platform.equals(PRODUCT_PLATFORM_MSM8976)) {
            this.mPlatform = new PlatformMSM8976();
        } else if (platform.equals(PRODUCT_PLATFORM_MSM8996)) {
            this.mPlatform = new PlatformMSM8996();
        } else if (platform.equals(PRODUCT_PLATFORM_MT6750)) {
            this.mPlatform = new PlatformMT6750();
        } else {
            this.mPlatform = null;
        }
    }

    public int getFreqLimitLevel() {
        if (this.mPlatform == null) {
            return -1;
        }
        return this.mPlatform.getFreqLimitLevel();
    }

    public int getPhoneTemperature() {
        if (this.mPlatform == null) {
            return -1;
        }
        return this.mPlatform.getPhoneTemperature();
    }

    public String getSDKVersion() {
        if (this.mPlatform != null && this.mPlatform.getProductSupport()) {
            return SDKVersion;
        }
        return null;
    }
}
