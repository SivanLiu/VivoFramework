package com.mediatek.pq;

import android.os.SystemProperties;
import android.util.Log;

public class PictureQuality {
    private static final String BLUELIGHT_DEFAULT_PROPERTY_NAME = "persist.sys.bluelight.default";
    public static final int CAPABILITY_MASK_COLOR = 1;
    public static final int CAPABILITY_MASK_DC = 8;
    public static final int CAPABILITY_MASK_GAMMA = 4;
    public static final int CAPABILITY_MASK_OD = 16;
    public static final int CAPABILITY_MASK_SHARPNESS = 2;
    private static final String CHAMELEON_DEFAULT_PROPERTY_NAME = "persist.sys.chameleon.default";
    public static final int DCHIST_INFO_NUM = 20;
    private static final String GAMMA_INDEX_PROPERTY_NAME = "persist.sys.gamma.index";
    public static final int GAMMA_LUT_SIZE = 512;
    public static final int MODE_CAMERA = 1;
    public static final int MODE_MASK = 1;
    public static final int MODE_NORMAL = 0;
    public static final int MODE_VIDEO = 2;
    public static final int PIC_MODE_STANDARD = 0;
    public static final int PIC_MODE_USER_DEF = 2;
    public static final int PIC_MODE_VIVID = 1;
    static boolean sLibStatus;

    public static class GammaLut {
        public int hwid;
        public int[] lut = new int[512];

        public GammaLut() {
            for (int i = 0; i < 512; i++) {
                set(i, 0);
            }
        }

        public void set(int index, int value) {
            if (index >= 0 && index < 512) {
                this.lut[index] = value;
            }
        }
    }

    public static class Hist {
        public int[] info = new int[20];

        public Hist() {
            for (int i = 0; i < 20; i++) {
                set(i, 0);
            }
        }

        public void set(int index, int value) {
            if (index >= 0 && index < 20) {
                this.info[index] = value;
            }
        }
    }

    public static class Range {
        public int defaultValue;
        public int max;
        public int min;

        public Range() {
            set(0, 0, 0);
        }

        public void set(int min, int max, int defaultValue) {
            this.min = min;
            this.max = max;
            this.defaultValue = defaultValue;
        }
    }

    private static native boolean nativeEnableBlueLight(boolean z, int i);

    private static native boolean nativeEnableBlueLightvivo(boolean z);

    private static native boolean nativeEnableChameleon(boolean z, int i);

    private static native boolean nativeEnableColor(int i);

    private static native boolean nativeEnableColorEffect(int i);

    private static native boolean nativeEnableContentColor(int i);

    private static native boolean nativeEnableDynamicContrast(int i);

    private static native boolean nativeEnableDynamicSharpness(int i);

    private static native boolean nativeEnableGamma(int i);

    private static native boolean nativeEnableISOAdaptiveSharpness(int i);

    private static native boolean nativeEnableOD(int i);

    private static native boolean nativeEnablePQ(int i);

    private static native boolean nativeEnableSharpness(int i);

    private static native boolean nativeEnableUltraResolution(int i);

    private static native boolean nativeEnableVideoHDR(boolean z);

    private static native int nativeGetBlueLightMappingvivocentre();

    private static native int nativeGetBlueLightMappingvivomin();

    private static native int nativeGetBlueLightStrength();

    private static native int nativeGetBlueLightStrengthvivo();

    private static native int nativeGetCapability();

    private static native int nativeGetChameleonStrength();

    private static native int nativeGetColorEffectIndex();

    private static native void nativeGetColorEffectIndexRange(Range range);

    private static native int nativeGetContrastIndex();

    private static native void nativeGetContrastIndexRange(Range range);

    private static native int nativeGetDefaultOffTransitionStep();

    private static native int nativeGetDefaultOnTransitionStep();

    private static native void nativeGetDynamicContrastHistogram(byte[] bArr, int i, int i2, Hist hist);

    private static native int nativeGetDynamicContrastIndex();

    private static native void nativeGetDynamicContrastIndexRange(Range range);

    private static native int nativeGetExternalPanelNits();

    private static native void nativeGetGammaIndexRange(Range range);

    private static native int nativeGetGlobalPQStrength();

    private static native int nativeGetGlobalPQStrengthRange();

    private static native int nativeGetGlobalPQSwitch();

    private static native int nativeGetPicBrightnessIndex();

    private static native void nativeGetPicBrightnessIndexRange(Range range);

    private static native int nativeGetPictureMode();

    private static native int nativeGetSaturationIndex();

    private static native void nativeGetSaturationIndexRange(Range range);

    private static native int nativeGetSharpnessIndex();

    private static native void nativeGetSharpnessIndexRange(Range range);

    private static native boolean nativeIsBlueLightEnabled();

    private static native boolean nativeIsBlueLightEnabledvivo();

    private static native boolean nativeIsChameleonEnabled();

    private static native boolean nativeIsVideoHDREnabled();

    private static native void nativeSetAALFunction(int i);

    private static native boolean nativeSetBlueLightMappingvivo(int i);

    private static native boolean nativeSetBlueLightStrength(int i, int i2);

    private static native boolean nativeSetBlueLightStrengthvivo(int i);

    private static native void nativeSetCameraPreviewMode(int i);

    private static native boolean nativeSetChameleonStrength(int i, int i2);

    private static native void nativeSetColorEffectIndex(int i);

    private static native boolean nativeSetColorRegion(int i, int i2, int i3, int i4, int i5);

    private static native void nativeSetContrastIndex(int i, int i2);

    private static native void nativeSetDynamicContrastIndex(int i);

    private static native boolean nativeSetExternalPanelNits(int i);

    private static native void nativeSetGalleryNormalMode(int i);

    private static native void nativeSetGammaIndex(int i, int i2);

    private static native boolean nativeSetGlobalPQStrength(int i);

    private static native boolean nativeSetGlobalPQSwitch(int i);

    private static native void nativeSetPicBrightnessIndex(int i, int i2);

    private static native boolean nativeSetPictureMode(int i, int i2);

    private static native boolean nativeSetRGBGain(int i, int i2, int i3, int i4);

    private static native void nativeSetSaturationIndex(int i, int i2);

    private static native void nativeSetSharpnessIndex(int i);

    private static native void nativeSetVideoPlaybackMode(int i);

    static {
        sLibStatus = true;
        try {
            Log.v("JNI_PQ", "loadLibrary");
            System.loadLibrary("jni_pq");
        } catch (UnsatisfiedLinkError e) {
            Log.e("JNI_PQ", "UnsatisfiedLinkError");
            sLibStatus = false;
        }
    }

    public static boolean getLibStatus() {
        return sLibStatus;
    }

    public static int getCapability() {
        return nativeGetCapability();
    }

    public static String setMode(int mode, int step) {
        if (mode == 1) {
            nativeSetCameraPreviewMode(step);
        } else if (mode == 2) {
            nativeSetVideoPlaybackMode(step);
        } else {
            nativeSetGalleryNormalMode(step);
        }
        return null;
    }

    public static String setMode(int mode) {
        if (mode == 1) {
            nativeSetCameraPreviewMode(getDefaultOnTransitionStep());
        } else if (mode == 2) {
            nativeSetVideoPlaybackMode(getDefaultOnTransitionStep());
        } else {
            nativeSetGalleryNormalMode(getDefaultOnTransitionStep());
        }
        return null;
    }

    public static Hist getDynamicContrastHistogram(byte[] srcBuffer, int srcWidth, int srcHeight) {
        Hist outHist = new Hist();
        nativeGetDynamicContrastHistogram(srcBuffer, srcWidth, srcHeight, outHist);
        return outHist;
    }

    public static boolean enablePQ(int isEnable) {
        return nativeEnablePQ(isEnable);
    }

    public static boolean enableColor(int isEnable) {
        return nativeEnableColor(isEnable);
    }

    public static boolean enableContentColor(int isEnable) {
        return nativeEnableContentColor(isEnable);
    }

    public static boolean enableSharpness(int isEnable) {
        return nativeEnableSharpness(isEnable);
    }

    public static boolean enableDynamicContrast(int isEnable) {
        return nativeEnableDynamicContrast(isEnable);
    }

    public static boolean enableDynamicSharpness(int isEnable) {
        return nativeEnableDynamicSharpness(isEnable);
    }

    public static boolean enableColorEffect(int isEnable) {
        return nativeEnableColorEffect(isEnable);
    }

    public static boolean enableGamma(int isEnable) {
        return nativeEnableGamma(isEnable);
    }

    public static boolean enableOD(int isEnable) {
        return nativeEnableOD(isEnable);
    }

    public static boolean enableISOAdaptiveSharpness(int isEnable) {
        return nativeEnableISOAdaptiveSharpness(isEnable);
    }

    public static boolean enableUltraResolution(int isEnable) {
        return nativeEnableUltraResolution(isEnable);
    }

    public static int getPictureMode() {
        return nativeGetPictureMode();
    }

    public static boolean setPictureMode(int mode, int step) {
        return nativeSetPictureMode(mode, step);
    }

    public static boolean setPictureMode(int mode) {
        return nativeSetPictureMode(mode, getDefaultOffTransitionStep());
    }

    public static boolean setColorRegion(int isEnable, int startX, int startY, int endX, int endY) {
        return nativeSetColorRegion(isEnable, startX, startY, endX, endY);
    }

    public static Range getContrastIndexRange() {
        Range r = new Range();
        nativeGetContrastIndexRange(r);
        return r;
    }

    public static int getContrastIndex() {
        return nativeGetContrastIndex();
    }

    public static void setContrastIndex(int index, int step) {
        nativeSetContrastIndex(index, step);
    }

    public static void setContrastIndex(int index) {
        nativeSetContrastIndex(index, getDefaultOffTransitionStep());
    }

    public static Range getSaturationIndexRange() {
        Range r = new Range();
        nativeGetSaturationIndexRange(r);
        return r;
    }

    public static int getSaturationIndex() {
        return nativeGetSaturationIndex();
    }

    public static void setSaturationIndex(int index, int step) {
        nativeSetSaturationIndex(index, step);
    }

    public static void setSaturationIndex(int index) {
        nativeSetSaturationIndex(index, getDefaultOffTransitionStep());
    }

    public static Range getPicBrightnessIndexRange() {
        Range r = new Range();
        nativeGetPicBrightnessIndexRange(r);
        return r;
    }

    public static int getPicBrightnessIndex() {
        return nativeGetPicBrightnessIndex();
    }

    public static void setPicBrightnessIndex(int index, int step) {
        nativeSetPicBrightnessIndex(index, step);
    }

    public static void setPicBrightnessIndex(int index) {
        nativeSetPicBrightnessIndex(index, getDefaultOffTransitionStep());
    }

    public static Range getSharpnessIndexRange() {
        Range r = new Range();
        nativeGetSharpnessIndexRange(r);
        return r;
    }

    public static int getSharpnessIndex() {
        return nativeGetSharpnessIndex();
    }

    public static void setSharpnessIndex(int index) {
        nativeSetSharpnessIndex(index);
    }

    public static Range getDynamicContrastIndexRange() {
        Range r = new Range();
        nativeGetDynamicContrastIndexRange(r);
        return r;
    }

    public static int getDynamicContrastIndex() {
        return nativeGetDynamicContrastIndex();
    }

    public static void setDynamicContrastIndex(int index) {
        nativeSetDynamicContrastIndex(index);
    }

    public static Range getColorEffectIndexRange() {
        Range r = new Range();
        nativeGetColorEffectIndexRange(r);
        return r;
    }

    public static int getColorEffectIndex() {
        return nativeGetColorEffectIndex();
    }

    public static void setColorEffectIndex(int index) {
        nativeSetColorEffectIndex(index);
    }

    public static Range getGammaIndexRange() {
        Range r = new Range();
        nativeGetGammaIndexRange(r);
        return r;
    }

    public static void setGammaIndex(int index, int step) {
        SystemProperties.set(GAMMA_INDEX_PROPERTY_NAME, Integer.toString(index));
        nativeSetGammaIndex(index, step);
    }

    public static void setGammaIndex(int index) {
        SystemProperties.set(GAMMA_INDEX_PROPERTY_NAME, Integer.toString(index));
        nativeSetGammaIndex(index, getDefaultOnTransitionStep());
    }

    public static int getGammaIndex() {
        return SystemProperties.getInt(GAMMA_INDEX_PROPERTY_NAME, getGammaIndexRange().defaultValue);
    }

    public static Range getBlueLightStrengthRange() {
        Range r = new Range();
        r.set(0, 255, SystemProperties.getInt(BLUELIGHT_DEFAULT_PROPERTY_NAME, 128));
        return r;
    }

    public static boolean setBlueLightStrength(int strength, int step) {
        return nativeSetBlueLightStrength(strength, step);
    }

    public static boolean setBlueLightStrength(int strength) {
        return nativeSetBlueLightStrength(strength, getDefaultOffTransitionStep());
    }

    public static int getBlueLightStrength() {
        return nativeGetBlueLightStrength();
    }

    public static boolean enableBlueLight(boolean enable, int step) {
        return nativeEnableBlueLight(enable, step);
    }

    public static boolean enableBlueLight(boolean enable) {
        return nativeEnableBlueLight(enable, getDefaultOnTransitionStep());
    }

    public static boolean isBlueLightEnabled() {
        return nativeIsBlueLightEnabled();
    }

    public static boolean isBlueLightEnabledvivo() {
        return nativeIsBlueLightEnabledvivo();
    }

    public static boolean enableBlueLightvivo(boolean enable) {
        return nativeEnableBlueLightvivo(enable);
    }

    public static int getBlueLightStrengthvivo() {
        return nativeGetBlueLightStrengthvivo();
    }

    public static boolean setBlueLightStrengthvivo(int strength) {
        return nativeSetBlueLightStrengthvivo(strength);
    }

    public static int getBlueLightMappingvivomin() {
        return 0;
    }

    public static int getBlueLightMappingvivocentre() {
        return 128;
    }

    public static boolean setBlueLightMappingvivo(int index) {
        return true;
    }

    public static Range getChameleonStrengthRange() {
        Range r = new Range();
        r.set(0, 255, SystemProperties.getInt(CHAMELEON_DEFAULT_PROPERTY_NAME, 128));
        return r;
    }

    public static boolean setChameleonStrength(int strength, int step) {
        return nativeSetChameleonStrength(strength, step);
    }

    public static boolean setChameleonStrength(int strength) {
        return nativeSetChameleonStrength(strength, getDefaultOffTransitionStep());
    }

    public static int getChameleonStrength() {
        return nativeGetChameleonStrength();
    }

    public static boolean enableChameleon(boolean enable, int step) {
        return nativeEnableChameleon(enable, step);
    }

    public static boolean enableChameleon(boolean enable) {
        return nativeEnableChameleon(enable, getDefaultOnTransitionStep());
    }

    public static boolean isChameleonEnabled() {
        return nativeIsChameleonEnabled();
    }

    public static int getDefaultOffTransitionStep() {
        return nativeGetDefaultOffTransitionStep();
    }

    public static int getDefaultOnTransitionStep() {
        return nativeGetDefaultOnTransitionStep();
    }

    public static boolean setGlobalPQSwitch(int globalPQSwitch) {
        return nativeSetGlobalPQSwitch(globalPQSwitch);
    }

    public static int getGlobalPQSwitch() {
        return nativeGetGlobalPQSwitch();
    }

    public static boolean setGlobalPQStrength(int globalPQStrength) {
        return nativeSetGlobalPQStrength(globalPQStrength);
    }

    public static int getGlobalPQStrength() {
        return nativeGetGlobalPQStrength();
    }

    public static int getGlobalPQStrengthRange() {
        return nativeGetGlobalPQStrengthRange();
    }

    public static boolean enableVideoHDR(boolean enable) {
        return nativeEnableVideoHDR(enable);
    }

    public static boolean isVideoHDREnabled() {
        return nativeIsVideoHDREnabled();
    }

    public static void setAALFunction(int func) {
        nativeSetAALFunction(func);
    }

    public static boolean setExternalPanelNits(int externalPanelNits) {
        return nativeSetExternalPanelNits(externalPanelNits);
    }

    public static int getExternalPanelNits() {
        return nativeGetExternalPanelNits();
    }

    public static boolean setRGBGain(double r_gain, double g_gain, double b_gain, int step) {
        return nativeSetRGBGain((int) (r_gain * 1024.0d), (int) (g_gain * 1024.0d), (int) (b_gain * 1024.0d), step);
    }
}
