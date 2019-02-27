package com.vivo.vivotransition;

import android.view.View;
import android.view.ViewGroup;

public class TransitionUtil {
    private static int getChildOffset(ViewGroup targetView, int index, int pageSpacing) {
        float offset = 0.0f;
        for (int i = 0; i < index; i++) {
            View child = targetView.getChildAt(i);
            offset += (((float) child.getMeasuredWidth()) * child.getScaleX()) + ((float) pageSpacing);
        }
        return (int) offset;
    }

    public static float getScrollProgress(ViewGroup targetView, int transitionX, View view, int page, int pageSpacing) {
        return Math.max(Math.min(((float) (transitionX - getChildOffset(targetView, page, pageSpacing))) / ((view.getScaleX() * ((float) view.getWidth())) + ((float) pageSpacing)), 1.0f), -1.0f);
    }
}
