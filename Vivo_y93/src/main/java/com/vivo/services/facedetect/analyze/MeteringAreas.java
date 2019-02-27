package com.vivo.services.facedetect.analyze;

import android.graphics.Rect;
import com.vivo.services.rms.ProcessList;

public class MeteringAreas {
    public static final int NUMBER_OF_H = 5;
    public static final int NUMBER_OF_W = 5;
    private static final Rect fullRect = new Rect(ProcessList.NATIVE_ADJ, ProcessList.NATIVE_ADJ, 1000, 1000);

    private MeteringAreas() {
    }

    public static int getCount() {
        return 25;
    }

    public static Rect get(int h, int w) {
        return RectDivider.getSubRect(fullRect, 5, 5, h, w);
    }

    public static Rect get(int i) {
        return RectDivider.getSubRect(fullRect, 5, 5, i);
    }

    public static Rect toImageArea(Rect rect) {
        return new Rect((int) ((((float) (rect.left + 1000)) / 2000.0f) * 640.0f), (int) ((((float) (rect.top + 1000)) / 2000.0f) * 480.0f), (int) ((((float) (rect.right + 1000)) / 2000.0f) * 640.0f), (int) ((((float) (rect.bottom + 1000)) / 2000.0f) * 480.0f));
    }

    public static Rect toScreenArea(Rect rect, int sreenWidth, int screenHeight) {
        int w = sreenWidth;
        int h = screenHeight;
        int left = rect.left;
        int top = rect.bottom;
        int newTop = left + 1000;
        left = (int) ((((float) (1000 - top)) / 2000.0f) * ((float) sreenWidth));
        int right = (int) ((((float) (1000 - rect.top)) / 2000.0f) * ((float) sreenWidth));
        int newLeft = left;
        int newRight = right;
        return new Rect(left, screenHeight - ((int) ((((float) (rect.right + 1000)) / 2000.0f) * ((float) screenHeight))), right, screenHeight - ((int) ((((float) newTop) / 2000.0f) * ((float) screenHeight))));
    }
}
