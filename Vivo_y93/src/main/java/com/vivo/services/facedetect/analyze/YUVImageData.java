package com.vivo.services.facedetect.analyze;

import android.graphics.Rect;

public class YUVImageData {
    private static final String TAG = "YUVImageData";
    private Rect biggestRect;
    private byte[] mData;
    private int mHeight;
    private int mWidth;

    public YUVImageData(byte[] data, int height, int width) {
        if (((float) data.length) < (((float) (height * width)) * 3.0f) / 2.0f) {
            throw new IllegalArgumentException("data size " + data.length + " is not suitable for the specified height " + height + " and width " + width);
        }
        this.mData = data;
        this.mHeight = height;
        this.mWidth = width;
        this.biggestRect = new Rect(0, 0, this.mWidth, this.mHeight);
    }

    public int getBrightnessAt(int h, int w) {
        return this.mData[(this.mWidth * h) + w] & 255;
    }

    public int getCenterBrightnessForRect(Rect rect, int pixelsH, int pixelsW) {
        LogUtils.debugLog(TAG, "getCenterBrightnessForRect " + rect + " pixelsH " + pixelsH + " pixelsW" + pixelsW);
        int numberOfSubH = rect.height() / pixelsH;
        int numberOfSubW = rect.width() / pixelsW;
        return getAverageBrightnessForRect(RectDivider.getSubRect(rect, numberOfSubH, numberOfSubW, numberOfSubH / 2, numberOfSubW / 2));
    }

    private int getAverageBrightnessForRect(Rect rect) {
        LogUtils.debugLog(TAG, "getAverageBrightnessForRect " + rect);
        if (checkIfRectInData(rect)) {
            int left = rect.left;
            int top = rect.top;
            int sum = 0;
            int h = rect.height();
            int w = rect.width();
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    sum += getBrightnessAt(top + i, left + j);
                }
            }
            LogUtils.debugLog(TAG, "getCenterBrightnessForRect sum = " + sum + " for rect: " + rect + "average :" + (sum / (h * w)));
            return sum / (h * w);
        }
        throw new IllegalArgumentException("rect is not in the image");
    }

    public void release() {
        this.mData = null;
    }

    private boolean checkIfRectInData(Rect rect) {
        LogUtils.debugLog(TAG, "checkIfRectInData big rect" + this.biggestRect + ", this rect " + rect);
        return this.biggestRect.contains(rect);
    }
}
