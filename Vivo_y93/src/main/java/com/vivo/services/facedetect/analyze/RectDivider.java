package com.vivo.services.facedetect.analyze;

import android.graphics.Rect;

public class RectDivider {
    public static Rect getSubRect(Rect rect, int h, int w, int subH, int subW) {
        int height = rect.height();
        int width = rect.width();
        if (height < h || width < w || subH > h || subW > w) {
            throw new IllegalArgumentException("height or width is too small,or index is greater ");
        }
        int stepH = (int) (((float) height) / ((float) h));
        int stepW = (int) (((float) width) / ((float) w));
        int left = rect.left + (subW * stepW);
        int top = rect.top + (subH * stepH);
        return new Rect(left, top, left + stepW, top + stepH);
    }

    public static Rect getSubRect(Rect fullRect, int numberOfH, int numberOfW, int i) {
        return getSubRect(fullRect, numberOfH, numberOfW, i / numberOfW, i % numberOfW);
    }
}
