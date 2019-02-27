package com.android.internal.policy;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Trace;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.Arrays;

public class DrawableUtils {
    private static final int BITMAP_HEIGHT = 10;
    private static final int BITMAP_WIDTH = 100;
    private static final int COPONENTS_COUNT = 4;
    private static int DISCARD_COLOR_COUNT = 50;
    private static final float MAX_SATURATION_THRESHOLD = 0.095f;
    private static int MEASURE_COLOR_COUNT = 100;
    private static final int MIN_COLOR_COMPONENT = 60;
    public static final int NAVIGATION_BAR_DEFAULT_COLOR = NavigationBarPolicy.DEFAULT_COLOR;
    private static final int OFFSET_HEIGHT = 15;
    private static final String TAG = "DrawableUtils";
    private static final int TRANSPARENT_COLOR = 0;
    private static long mHandle = 0;
    private static Thread mInitializeNativeThread = new Thread() {
        public void run() {
            long start = 0;
            if (NavigationBarController.DEBUG_TRACE) {
                start = System.currentTimeMillis();
            }
            if (DrawableUtils.sViewBitmap == null) {
                DrawableUtils.sViewBitmap = Bitmap.createBitmap(100, 10, Config.ARGB_8888);
                DrawableUtils.sViewCanvas = new Canvas(DrawableUtils.sViewBitmap);
                DrawableUtils.sViewCanvas.setHwBitmapsInSwModeEnabled(true);
            }
            if (DrawableUtils.sViewColorBitmap == null) {
                DrawableUtils.sViewColorBitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
                DrawableUtils.sViewColorCanvas = new Canvas(DrawableUtils.sViewColorBitmap);
                DrawableUtils.sViewColorCanvas.setHwBitmapsInSwModeEnabled(true);
                DrawableUtils.initNativeInstance();
            }
            if (NavigationBarController.DEBUG_TRACE) {
                Log.d(DrawableUtils.TAG, "DrawableUtils mInitializeNativeThread run time: " + (System.currentTimeMillis() - start));
            }
        }
    };
    private static boolean mNeedChangeRenderSource = false;
    private static Bitmap sTempBitmap = null;
    private static Bitmap sTempBlendColorBitmap = null;
    private static Canvas sTempBlendColorCanvas = null;
    private static int[] sTempBlendColorPixels = new int[1];
    private static Canvas sTempCanvas = null;
    private static Bitmap sTempColorBitmap = null;
    private static Canvas sTempColorCanvas = null;
    private static int[] sTempColorPixels = new int[1];
    private static Matrix sTempMatrix = null;
    private static Bitmap sViewBitmap = null;
    private static Canvas sViewCanvas = null;
    private static Bitmap sViewColorBitmap = null;
    private static Canvas sViewColorCanvas = null;

    private static class ColorInfo {
        long dis;
        int index;
        boolean isDiscard;

        /* synthetic */ ColorInfo(ColorInfo -this0) {
            this();
        }

        private ColorInfo() {
        }
    }

    static class MaskLayerInfo {
        float blendRatio;
        Rect bound;
        boolean isValidColor;
        int maskColor;
        View view;

        MaskLayerInfo() {
        }
    }

    static native long nativeCreate();

    static native void nativeDestroy(long j);

    static native int nativeGetBlendImagePixel(long j, int i, int i2);

    static native int nativeGetViewImagePixel(long j, int i, int i2);

    static native int nativeMeasureViewColor(long j, int i, int i2, int i3, int i4);

    static native void nativeSetBlendImageData(long j, Bitmap bitmap, int i, int i2);

    static native void nativeSetDebugState(long j, boolean z);

    static native void nativeSetViewImageData(long j, Bitmap bitmap, int i, int i2);

    /* JADX WARNING: Missing block: B:66:0x0207, code:
            return r11;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized int getColorFromView(View view, Rect bound, int decorWidth, boolean isWebView, boolean isBottomBar, boolean isNonFillUp, boolean isNeedRecordColor) {
        synchronized (DrawableUtils.class) {
            if (NavigationBarController.DEBUG_TRACE) {
                Trace.traceBegin(8, "getColorFromView:" + view.getClass().getSimpleName());
            }
            int width = bound.width();
            int height = bound.height();
            int i;
            if (width <= 0 || height <= 0 || decorWidth <= 0) {
                return 0;
            } else if (isWebView) {
                i = NAVIGATION_BAR_DEFAULT_COLOR;
                return i;
            } else {
                boolean isImageView = view instanceof ImageView;
                Integer recordedColor;
                if (isImageView) {
                    Drawable drawable = ((ImageView) view).getDrawable();
                    if (drawable == null || !(drawable instanceof TransitionDrawable)) {
                        recordedColor = view.getRecordedColor();
                        if (recordedColor != null) {
                            i = recordedColor.intValue();
                            return i;
                        }
                    }
                    view.setRecordedColor(null);
                } else {
                    recordedColor = view.getRecordedColor();
                    if (recordedColor != null) {
                        i = recordedColor.intValue();
                        return i;
                    }
                }
                createRenderSource();
                if (NavigationBarController.DEBUG_TRACE) {
                    Trace.traceBegin(8, "drawNoChildren");
                }
                sTempCanvas.drawColor(0, Mode.CLEAR);
                sTempMatrix.reset();
                int saveCount = sTempCanvas.save();
                sTempMatrix.postTranslate(0.0f, -((float) ((height - 10) - 15)));
                sTempCanvas.setMatrix(sTempMatrix);
                boolean isCenterImageView = false;
                if (!isImageView) {
                    view.drawNoChildren(sTempCanvas, false);
                } else if (isBottomBar) {
                    isCenterImageView = bound.left > 5 && bound.right < decorWidth - 5;
                    if (!isCenterImageView) {
                        if (view.getBackground() != null) {
                            view.drawNoChildren(sTempCanvas, true);
                        } else {
                            view.drawNoChildren(sTempCanvas, false);
                        }
                    }
                } else if (isNonFillUp) {
                    view.drawNoChildren(sTempCanvas, true);
                } else {
                    view.drawNoChildren(sTempCanvas, false);
                }
                if (saveCount >= 1) {
                    sTempCanvas.restoreToCount(saveCount);
                }
                if (NavigationBarController.DEBUG_TRACE) {
                    Trace.traceEnd(8);
                }
                if (NavigationBarController.DEBUG_TRACE) {
                    Trace.traceBegin(8, "measureColor");
                }
                int color = 0;
                if (!isCenterImageView) {
                    color = getMeasureColor(sTempBitmap, width, decorWidth);
                }
                if (NavigationBarController.DEBUG_TRACE) {
                    Trace.traceEnd(8);
                }
                int resultColor = color;
                if (isNeedRecordColor) {
                    view.setRecordedColor(new Integer(color));
                }
                if (NavigationBarController.DEBUG) {
                    Log.d(TAG, "test measure color Controller DrawableUtils getColorFromView isWebView: " + isWebView + ", isImageView: " + isImageView + ", isCenterImageView: " + isCenterImageView + ", isNonFillUp: " + isNonFillUp + ", color: " + color + ", alpha: " + Color.alpha(color) + ", isNeedRecordColor: " + isNeedRecordColor + ", resultColor: " + resultColor + ", width: " + width + ", height: " + height + ", vWidth: " + view.getWidth() + ", vHeight: " + view.getHeight() + ", view = " + view.getClass().getSimpleName() + ", colorCount: " + MEASURE_COLOR_COUNT + ", mHandle: " + mHandle);
                }
                if (NavigationBarController.DEBUG_TRACE) {
                    Trace.traceEnd(8);
                }
            }
        }
    }

    private static void initNativeInstance() {
        long handle = nativeCreate();
        if (NavigationBarController.DEBUG) {
            nativeSetDebugState(handle, true);
        } else {
            nativeSetDebugState(handle, false);
        }
        nativeSetViewImageData(handle, sViewBitmap, 100, 10);
        nativeSetBlendImageData(handle, sViewColorBitmap, 1, 1);
        mNeedChangeRenderSource = true;
        mHandle = handle;
    }

    private static void createRenderSource() {
        if (sTempBitmap == null) {
            sTempBitmap = Bitmap.createBitmap(100, 10, Config.ARGB_8888);
            sTempCanvas = new Canvas(sTempBitmap);
            sTempCanvas.setHwBitmapsInSwModeEnabled(true);
            sTempMatrix = new Matrix();
            MEASURE_COLOR_COUNT = NavigationBarController.SAMPLE_COLOR_COUNT;
            DISCARD_COLOR_COUNT = MEASURE_COLOR_COUNT >> 1;
        }
        if (sTempColorBitmap == null) {
            sTempColorBitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
            sTempColorCanvas = new Canvas(sTempColorBitmap);
            sTempColorCanvas.setHwBitmapsInSwModeEnabled(true);
            if (NavigationBarController.IMMERSIVE_PERFORMANCE_OPTIMIZE) {
                mInitializeNativeThread.start();
            }
        }
        if (NavigationBarController.IMMERSIVE_PERFORMANCE_OPTIMIZE) {
            changeRenderSource();
        }
    }

    private static void changeRenderSource() {
        if (mNeedChangeRenderSource) {
            if (sTempBitmap != null) {
                sTempBitmap.recycle();
                sTempBitmap = null;
            }
            sTempBitmap = sViewBitmap;
            sTempCanvas = sViewCanvas;
            if (sTempColorBitmap != null) {
                sTempColorBitmap.recycle();
                sTempColorBitmap = null;
            }
            sTempColorBitmap = sViewColorBitmap;
            sTempColorCanvas = sViewColorCanvas;
            mNeedChangeRenderSource = false;
        }
    }

    private static int getMeasureColor(Bitmap bitmap, int viewWidth, int decorWidth) {
        if (bitmap == null || bitmap.isRecycled() || viewWidth <= 0) {
            return 0;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= 0 || height <= 0) {
            return 0;
        }
        int neededWidth = 100;
        int measureColorCount = MEASURE_COLOR_COUNT;
        int discardColorCount = DISCARD_COLOR_COUNT;
        int isOperateByNative = mHandle != 0 ? mNeedChangeRenderSource ^ 1 : 0;
        if (viewWidth > 3) {
            if (viewWidth < 100) {
                neededWidth = viewWidth;
                int visibleDataLen = viewWidth * 10;
                int halfSampleColorCount = MEASURE_COLOR_COUNT >> 1;
                if (visibleDataLen < halfSampleColorCount) {
                    measureColorCount = visibleDataLen;
                    discardColorCount = visibleDataLen >> 1;
                } else {
                    measureColorCount = halfSampleColorCount;
                    discardColorCount = halfSampleColorCount >> 1;
                }
            }
            if (NavigationBarController.IMMERSIVE_PERFORMANCE_OPTIMIZE && isOperateByNative != 0) {
                return nativeMeasureViewColor(mHandle, neededWidth, height, measureColorCount, discardColorCount);
            }
            int i;
            long colorA;
            int index;
            long dis;
            int stride = (neededWidth * 10) / measureColorCount;
            int[] pixels = new int[(neededWidth * height)];
            bitmap.getPixels(pixels, 0, neededWidth, 0, 0, neededWidth, height);
            long sumA = 0;
            long sumR = 0;
            long sumG = 0;
            long sumB = 0;
            int end = neededWidth * height;
            int length = measureColorCount;
            if (NavigationBarController.DEBUG) {
                Log.d(TAG, "test measure color DrawableUtils getMeasureColor width: " + width + ", height: " + height + ", length: " + length + ", neededWidth: " + neededWidth + ", measureColorCount: " + measureColorCount + ", discardColorCount: " + discardColorCount + ", stride: " + stride);
            }
            long[] components = new long[(length * 4)];
            ColorInfo[] colorList = new ColorInfo[length];
            for (i = 0; i < end; i += stride) {
                int color = pixels[i];
                if (NavigationBarController.DEBUG) {
                    Log.d(TAG, "test measure color DrawableUtils getMeasureColor index: " + i + ", color: " + color);
                }
                colorA = (long) Color.alpha(color);
                long colorR = (long) Color.red(color);
                long colorG = (long) Color.green(color);
                long colorB = (long) Color.blue(color);
                sumA += colorA;
                sumR += colorR;
                sumG += colorG;
                sumB += colorB;
                index = ((i + 0) / stride) * 4;
                if (index + 4 > components.length) {
                    break;
                }
                components[index] = colorA;
                components[index + 1] = colorR;
                components[index + 2] = colorG;
                components[index + 3] = colorB;
            }
            float inverse = 1.0f / ((float) length);
            int aveA = (int) (((float) sumA) * inverse);
            int aveR = (int) (((float) sumR) * inverse);
            int aveG = (int) (((float) sumG) * inverse);
            int aveB = (int) (((float) sumB) * inverse);
            long[] distance = new long[length];
            for (i = 0; i < length; i++) {
                index = i * 4;
                dis = ((Math.abs(components[index] - ((long) aveA)) + Math.abs(components[index + 1] - ((long) aveR))) + Math.abs(components[index + 2] - ((long) aveG))) + Math.abs(components[index + 3] - ((long) aveB));
                distance[i] = dis;
                ColorInfo colorInfo = new ColorInfo();
                colorInfo.isDiscard = false;
                colorInfo.index = i;
                colorInfo.dis = dis;
                colorList[i] = colorInfo;
            }
            Arrays.sort(distance);
            int[] discardIndex = new int[discardColorCount];
            index = 0;
            for (int k = length - discardColorCount; k < length; k++) {
                dis = distance[k];
                int t = 0;
                while (t < length) {
                    ColorInfo info = colorList[t];
                    if (!info.isDiscard && info.dis == dis) {
                        discardIndex[index] = info.index;
                        if (NavigationBarController.DEBUG) {
                            Log.d(TAG, "test measure color DrawableUtils getMeasureColor index: " + index + ", discardIndex: " + discardIndex[index]);
                        }
                        index++;
                        info.isDiscard = true;
                    } else {
                        t++;
                    }
                }
            }
            sumA = 0;
            sumR = 0;
            sumG = 0;
            sumB = 0;
            for (i = 0; i < length; i++) {
                boolean isDiscard = false;
                for (int j = 0; j < discardColorCount; j++) {
                    if (i == discardIndex[j]) {
                        isDiscard = true;
                        break;
                    }
                }
                if (!isDiscard) {
                    index = i * 4;
                    colorA = components[index];
                    sumA += colorA;
                    sumR += components[index + 1];
                    sumG += components[index + 2];
                    sumB += components[index + 3];
                }
            }
            inverse = 1.0f / ((float) (length - discardColorCount));
            int resultColor = Color.argb((int) (((float) sumA) * inverse), (int) (((float) sumR) * inverse), (int) (((float) sumG) * inverse), (int) (((float) sumB) * inverse));
            if (NavigationBarController.DEBUG) {
                Log.d(TAG, "test measure color DrawableUtils getMeasureColor resultColor: " + resultColor);
            }
            return resultColor;
        } else if (!NavigationBarController.IMMERSIVE_PERFORMANCE_OPTIMIZE || isOperateByNative == 0) {
            return bitmap.getPixel(1, 1);
        } else {
            return nativeGetViewImagePixel(mHandle, 1, 1);
        }
    }

    public static boolean isFullColor(int color) {
        if (Color.alpha(color) <= 60 || getSaturation(color) <= MAX_SATURATION_THRESHOLD) {
            return false;
        }
        return true;
    }

    public static int adjustFullColor(int color) {
        int colorA = Color.alpha(color);
        int colorR = Color.red(color);
        int colorG = Color.green(color);
        int minComponent = Math.min(Math.min(colorR, colorG), Color.blue(color));
        return Color.argb(colorA, minComponent, minComponent, minComponent);
    }

    public static boolean isDarkColor(int color) {
        int colorA = Color.alpha(color);
        int colorR = Color.red(color);
        int colorG = Color.green(color);
        int colorB = Color.blue(color);
        if (colorA <= 60 || Math.max(Math.max(colorR, colorG), colorB) <= 60) {
            return true;
        }
        return false;
    }

    public static int getBright(int color) {
        int colorR = Color.red(color);
        int colorG = Color.green(color);
        return Math.max(Math.max(colorR, colorG), Color.blue(color));
    }

    public static float getSaturation(int color) {
        int colorA = Color.alpha(color);
        int colorR = Color.red(color);
        int colorG = Color.green(color);
        int colorB = Color.blue(color);
        if (colorA <= 0) {
            return 0.0f;
        }
        int minComponent = Math.min(Math.min(colorR, colorG), colorB);
        int maxComponent = Math.max(Math.max(colorR, colorG), colorB);
        if (maxComponent > 0) {
            return (((float) (maxComponent - minComponent)) / ((float) maxComponent)) * (((float) colorA) / 255.0f);
        }
        return 0.0f;
    }

    public static int distance(int dstColor, int srcColor) {
        int dstA = Color.alpha(dstColor);
        int dstR = Color.red(dstColor);
        int dstG = Color.green(dstColor);
        int dstB = Color.blue(dstColor);
        return ((Math.abs(dstA - Color.alpha(srcColor)) + Math.abs(dstR - Color.red(srcColor))) + Math.abs(dstG - Color.green(srcColor))) + Math.abs(dstB - Color.blue(srcColor));
    }

    private static int blendColor(int srcColor, int maskColor, float ratio) {
        int resultColor = srcColor;
        if (ratio == 0.0f) {
            return srcColor;
        }
        if (sTempBlendColorBitmap == null) {
            sTempBlendColorBitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
            sTempBlendColorCanvas = new Canvas(sTempBlendColorBitmap);
            sTempBlendColorCanvas.setHwBitmapsInSwModeEnabled(true);
        }
        sTempBlendColorCanvas.drawColor(0, Mode.CLEAR);
        sTempBlendColorCanvas.drawColor(srcColor);
        sTempBlendColorCanvas.drawColor(maskColor);
        sTempBlendColorBitmap.getPixels(sTempBlendColorPixels, 0, 1, 0, 0, 1, 1);
        resultColor = sTempBlendColorPixels[0];
        if (NavigationBarController.DEBUG) {
            Log.i(TAG, "test measure color Controller overlayMask maskColor: " + Integer.toHexString(maskColor) + ", ratio: " + ratio + ", srcColor: " + Integer.toHexString(srcColor) + ", resultColor: " + Integer.toHexString(resultColor) + ", colorA: " + Color.alpha(resultColor));
        }
        return resultColor;
    }

    /* JADX WARNING: Missing block: B:7:0x000c, code:
            return;
     */
    /* JADX WARNING: Missing block: B:109:0x033b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void blendColors(ArrayList<MaskLayerInfo> maskList, ArrayList<MaskLayerInfo> resultList) {
        synchronized (DrawableUtils.class) {
            if (maskList != null) {
                if (maskList.size() > 0 && resultList != null) {
                    int i;
                    MaskLayerInfo info;
                    int size = maskList.size();
                    if (NavigationBarController.DEBUG) {
                        Log.i(TAG, "test measure color Controller NavigationBar blendMask before blend list size: " + size);
                    }
                    createRenderSource();
                    int isOperateByNative = mHandle != 0 ? mNeedChangeRenderSource ^ 1 : 0;
                    for (i = 0; i < size; i++) {
                        info = (MaskLayerInfo) maskList.get(i);
                        if (info.isValidColor) {
                            if (i == 0) {
                                resultList.add(info);
                            } else {
                                int validIndex = i;
                                Rect bounds = info.bound;
                                int resultSize = resultList.size();
                                MaskLayerInfo[] relatedMaskInfos = new MaskLayerInfo[i];
                                int index = 0;
                                for (int j = validIndex - 1; j >= 0; j--) {
                                    MaskLayerInfo mask = (MaskLayerInfo) maskList.get(j);
                                    if (!(mask == null || (mask.isValidColor ^ 1) == 0 || !isCoverRegion(bounds, mask.bound))) {
                                        relatedMaskInfos[index] = mask;
                                        index++;
                                    }
                                }
                                if (index == 0) {
                                    resultList.add(info);
                                } else {
                                    MaskLayerInfo[] blendedMaskInfos = new MaskLayerInfo[index];
                                    if (index > 0) {
                                        MaskLayerInfo currMask = relatedMaskInfos[0];
                                        sTempColorCanvas.drawColor(0, Mode.CLEAR);
                                        boolean isNeedDivideRegion = false;
                                        int colorBeforeLastBlend = 0;
                                        Rect lastMaskBound = null;
                                        if (index == 1) {
                                            lastMaskBound = currMask.bound;
                                            if (lastMaskBound.left > info.bound.left || lastMaskBound.right < info.bound.right) {
                                                isNeedDivideRegion = true;
                                                colorBeforeLastBlend = info.maskColor;
                                            }
                                        }
                                        if (info.maskColor != 0) {
                                            sTempColorCanvas.drawColor(info.maskColor);
                                        }
                                        sTempColorCanvas.drawColor(currMask.maskColor);
                                        blendedMaskInfos[0] = currMask;
                                        boolean isNeedBlend = true;
                                        for (int k = 1; k < index; k++) {
                                            MaskLayerInfo nextMask = relatedMaskInfos[k];
                                            for (int t = 0; t < index; t++) {
                                                MaskLayerInfo blendedInfo = blendedMaskInfos[t];
                                                if (blendedInfo != null && (isCoverRegion(blendedInfo.bound, nextMask.bound) ^ 1) != 0) {
                                                    isNeedBlend = false;
                                                    break;
                                                }
                                            }
                                            if (isNeedBlend) {
                                                if (k == index - 1) {
                                                    lastMaskBound = nextMask.bound;
                                                    if (lastMaskBound.left > info.bound.left || lastMaskBound.right < info.bound.right) {
                                                        if (!NavigationBarController.IMMERSIVE_PERFORMANCE_OPTIMIZE || isOperateByNative == 0) {
                                                            sTempColorBitmap.getPixels(sTempColorPixels, 0, 1, 0, 0, 1, 1);
                                                        } else {
                                                            sTempColorPixels[0] = nativeGetBlendImagePixel(mHandle, 1, 1);
                                                        }
                                                        isNeedDivideRegion = true;
                                                        colorBeforeLastBlend = sTempColorPixels[0];
                                                    }
                                                }
                                                sTempColorCanvas.drawColor(nextMask.maskColor);
                                                blendedMaskInfos[k] = nextMask;
                                            } else {
                                                isNeedBlend = true;
                                            }
                                        }
                                        int leftAfterLastBlend = info.bound.left;
                                        int rightAfterLastBlend = info.bound.right;
                                        if (isNeedDivideRegion) {
                                            MaskLayerInfo result;
                                            if (lastMaskBound.left > info.bound.left) {
                                                result = new MaskLayerInfo();
                                                result.view = info.view;
                                                result.bound = new Rect(info.bound.left, info.bound.top, lastMaskBound.left, info.bound.bottom);
                                                result.maskColor = colorBeforeLastBlend;
                                                leftAfterLastBlend = lastMaskBound.left;
                                                resultList.add(result);
                                            }
                                            if (lastMaskBound.right < info.bound.right) {
                                                result = new MaskLayerInfo();
                                                result.view = info.view;
                                                result.bound = new Rect(lastMaskBound.right, info.bound.top, info.bound.right, info.bound.bottom);
                                                result.maskColor = colorBeforeLastBlend;
                                                rightAfterLastBlend = lastMaskBound.right;
                                                resultList.add(result);
                                            }
                                        }
                                        if (!NavigationBarController.IMMERSIVE_PERFORMANCE_OPTIMIZE || isOperateByNative == 0) {
                                            sTempColorBitmap.getPixels(sTempColorPixels, 0, 1, 0, 0, 1, 1);
                                        } else {
                                            sTempColorPixels[0] = nativeGetBlendImagePixel(mHandle, 1, 1);
                                        }
                                        MaskLayerInfo resultInfo = new MaskLayerInfo();
                                        resultInfo.view = info.view;
                                        resultInfo.bound = new Rect(leftAfterLastBlend, info.bound.top, rightAfterLastBlend, info.bound.bottom);
                                        resultInfo.maskColor = sTempColorPixels[0];
                                        resultList.add(resultInfo);
                                    }
                                    if (resultSize >= resultList.size()) {
                                        resultList.add(info);
                                    }
                                }
                            }
                        }
                        if (NavigationBarController.DEBUG) {
                            Log.i(TAG, "test measure color Controller NavigationBar blendMask blending list index: " + i + ", color = " + Integer.toHexString(info.maskColor) + ", bound = " + info.bound + ", result list size: " + resultList.size());
                        }
                    }
                    if (NavigationBarController.DEBUG) {
                        for (i = 0; i < resultList.size(); i++) {
                            info = (MaskLayerInfo) resultList.get(i);
                            Log.i(TAG, "test measure color Controller NavigationBar blendMask after blend result index: " + i + ", bound: " + info.bound + ", color: " + Integer.toHexString(info.maskColor));
                        }
                    }
                }
            }
        }
    }

    private static boolean isCoverRegion(Rect bound, Rect mask) {
        if (bound == null || mask == null) {
            return false;
        }
        int bCenterX = bound.centerX();
        if ((bound.left < mask.left || bound.right > mask.right) && ((bCenterX <= mask.left || bound.right > mask.right) && ((bound.left < mask.left || bCenterX >= mask.right) && (bound.left > mask.left || bound.right < mask.right || mask.width() < (bound.width() >> 1))))) {
            return false;
        }
        return true;
    }

    public static int blendAlpha(int color, float alpha) {
        return Color.argb((int) (((((float) Color.alpha(color)) / 255.0f) * alpha) * 255.0f), Color.red(color), Color.green(color), Color.blue(color));
    }

    public static int blendColor(int srcColor, int targetColor) {
        return blendColor(srcColor, targetColor, ((float) Color.alpha(targetColor)) / 255.0f);
    }
}
