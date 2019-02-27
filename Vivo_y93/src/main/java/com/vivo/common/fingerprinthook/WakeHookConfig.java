package com.vivo.common.fingerprinthook;

import android.os.SystemProperties;
import com.vivo.common.autobrightness.AblConfig;

public class WakeHookConfig {
    public static final int FINGERPRINT_PRESS_UNLOCK_TIMEOUT = 4800;
    public static final int FINGERPRINT_TOUCH_UNLOCK_TIMEOUT = 800;
    private static final String[] FRONT_FINGER_PROJECT = new String[]{"PD1602", "PD1603"};
    private static final String MODEL = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, "unkown").toLowerCase();
    private static final String PLATFORM = SystemProperties.get(AblConfig.PROP_BOARD_PLATFORM, "un-known");
    private static final String[] PLATFORM_USE_WINDOW_HIDE_FUNCTION = new String[]{"msm8952", "msm8916", "mt6752", "mt6750", "msm8952", "msm8996"};
    private static final String PROP_FINGERPRINT_FAIL_DELAY = "persist.debug.fp_fail.delay";
    private static final String PROP_FINGERPRINT_SUCCESS_DELAY = "persist.debug.fp_succ.delay";
    private static final long mAuthFailedDelay = getAuthFailedDelayInner();
    private static final long mAuthSuccessDelay = getAuthSuccessDelayIner();
    private static final boolean mIsFrontFingerprint = isFrontFringerprintInner();
    private static final boolean mIsUseWindowHideFunction = true;

    public static long getAuthFailedDelay() {
        return mAuthFailedDelay;
    }

    private static long getAuthFailedDelayInner() {
        if (isFrontFringerprintInner()) {
            return (long) SystemProperties.getInt(PROP_FINGERPRINT_FAIL_DELAY, 0);
        }
        return 150;
    }

    public static long getAuthSuccessDelay() {
        return mAuthSuccessDelay;
    }

    private static long getAuthSuccessDelayIner() {
        if (isFrontFringerprintInner()) {
            return (long) SystemProperties.getInt(PROP_FINGERPRINT_SUCCESS_DELAY, 0);
        }
        return 0;
    }

    public boolean isFrontFringerprint() {
        return mIsFrontFingerprint;
    }

    private static boolean isFrontFringerprintInner() {
        for (String proj : FRONT_FINGER_PROJECT) {
            if (MODEL.startsWith(proj)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUseWindowHideFunction() {
        return mIsUseWindowHideFunction;
    }

    public static boolean isUseWindowHideFunctionInner() {
        for (String platform : PLATFORM_USE_WINDOW_HIDE_FUNCTION) {
            if (PLATFORM.startsWith(platform)) {
                return true;
            }
        }
        return false;
    }
}
