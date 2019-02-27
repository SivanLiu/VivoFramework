package com.vivo.services.facedetect.analyze;

import com.vivo.framework.facedetect.AdjusterParams;
import com.vivo.services.rms.sdk.Consts.ProcessStates;

public class YUVImageAnalysor {
    private int mDarkCount;
    private int mLightCount;
    private AdjusterParams mParams;

    public YUVImageAnalysor(AdjusterParams params) {
        this.mParams = params;
    }

    public AnalyzeResult analyze(YUVImageData data) {
        int countOfMeteringAreas = MeteringAreas.getCount();
        int[] brightnessList = new int[25];
        for (int i = 0; i < countOfMeteringAreas; i++) {
            int brightness = data.getCenterBrightnessForRect(MeteringAreas.toImageArea(MeteringAreas.get(i)), 20, 20);
            LogUtils.debugLog("average brightness for" + i + ":" + brightness);
            brightnessList[i] = brightness;
        }
        return analyzeImpl(brightnessList);
    }

    private AnalyzeResult analyzeImpl(int[] brightnessList) {
        int[] darkAreas = getDarkAreas(brightnessList);
        int[] lightAreas = getLightAreas(brightnessList);
        int brightestArea = getLightestArea(brightnessList);
        int darkestArea = getDarkestArea(brightnessList);
        return new AnalyzeResult(decideHDROrDark(brightnessList, darkestArea, brightestArea, darkAreas, lightAreas), MeteringAreas.get(darkestArea));
    }

    private int decideHDROrDark(int[] brightnessList, int darkestArea, int brightestArea, int[] darkAreas, int[] lightAreas) {
        boolean someOfImageIsDark = false;
        boolean someOfImageIsLight = false;
        boolean mostOfImageIsLight = false;
        boolean highContrast = false;
        boolean wholePictureIsDark = false;
        if (((float) this.mDarkCount) / ((float) brightnessList.length) > this.mParams.thresholdHDRDarkPercentage) {
            someOfImageIsDark = true;
        }
        if (((float) this.mLightCount) / ((float) brightnessList.length) > this.mParams.thresholdHDRLightPercentage) {
            someOfImageIsLight = true;
        }
        if (((float) this.mLightCount) / ((float) brightnessList.length) > this.mParams.thresholdOverLightPercentage) {
            mostOfImageIsLight = true;
        }
        if (((float) (brightnessList[brightestArea] - brightnessList[darkestArea])) >= this.mParams.thresholdHDRContrast) {
            highContrast = true;
        }
        if (((float) getAverageLightness(brightnessList)) <= this.mParams.thresholdDarkRoom) {
            wholePictureIsDark = true;
        }
        LogUtils.debugLog("wholePictureIsDark " + wholePictureIsDark + " someOfImageIsDark :" + someOfImageIsDark + " mostOfImageIsLight :" + mostOfImageIsLight + " someOfImageIsLight: " + someOfImageIsLight + " highContrast " + highContrast);
        if (wholePictureIsDark && (highContrast ^ 1) != 0 && (someOfImageIsLight ^ 1) != 0) {
            return 1;
        }
        if (mostOfImageIsLight) {
            return 3;
        }
        if (someOfImageIsDark && highContrast && someOfImageIsLight) {
            return 2;
        }
        return 0;
    }

    private int[] getDarkAreas(int[] brightList) {
        int[] darkAreas = new int[25];
        int count = 0;
        for (int i = 0; i < brightList.length; i++) {
            if (((float) brightList[i]) < this.mParams.thresholdDark) {
                darkAreas[count] = i;
                count++;
            }
        }
        this.mDarkCount = count;
        return darkAreas;
    }

    private int[] getLightAreas(int[] brightList) {
        int[] lightAreas = new int[25];
        int count = 0;
        for (int i = 0; i < brightList.length; i++) {
            if (((float) brightList[i]) > this.mParams.thresholdLight) {
                lightAreas[count] = i;
                count++;
            }
        }
        this.mLightCount = count;
        return lightAreas;
    }

    private int getDarkestArea(int[] lightness) {
        int darkValue = 255;
        int darkest = 0;
        for (int i = 0; i < lightness.length; i++) {
            if (darkValue >= lightness[i]) {
                darkest = i;
                darkValue = lightness[i];
            }
        }
        return darkest;
    }

    private int getMiddleLightArea(int[] lightness) {
        int index = -1;
        int diff = ProcessStates.HASNOTIFICATION;
        for (int i = 0; i < lightness.length; i++) {
            int temp = 128 - lightness[i];
            if (temp >= 0 && diff >= temp) {
                index = i;
                diff = temp;
            }
        }
        return index;
    }

    private int getLightestArea(int[] lightness) {
        int lightValue = 0;
        int lightest = 0;
        for (int i = 0; i < lightness.length; i++) {
            if (lightValue <= lightness[i]) {
                lightest = i;
                lightValue = lightness[i];
            }
        }
        return lightest;
    }

    private int getAverageLightness(int[] brightlist) {
        int sum = 0;
        for (int i : brightlist) {
            sum += i;
        }
        int average = sum / brightlist.length;
        LogUtils.debugLog("getAverageLightness :" + average);
        return average;
    }

    public void setParameters(AdjusterParams params) {
        this.mParams = params;
    }
}
