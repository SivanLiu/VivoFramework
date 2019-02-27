package com.vivo.vivotransition;

import android.graphics.Rect;
import android.view.View;

public class AlgorithmUtil {
    public static void getTransformRect(View child, Rect rect) {
        rect.set((int) transformPivotX(child, 0.0f), (int) transformPivotY(child, 0.0f), (int) transformPivotX(child, (float) child.getWidth()), (int) transformPivotY(child, (float) child.getHeight()));
    }

    public static float transformPivotX(View view, float pivotX) {
        float basePivotX = view.getPivotX();
        return basePivotX + (view.getScaleX() * (pivotX - basePivotX));
    }

    public static float transformPivotY(View view, float pivotY) {
        float basePivotY = view.getPivotY();
        return basePivotY + (view.getScaleY() * (pivotY - basePivotY));
    }

    public static float sin(float angle) {
        if (angle < -3.1415927f) {
            angle += 6.2831855f;
        } else if (angle > 3.1415927f) {
            angle -= 6.2831855f;
        }
        float sin = (1.2732395f * angle) - ((0.4052847f * angle) * Math.abs(angle));
        return sin + ((0.225f * sin) * (Math.abs(sin) - 1.0f));
    }

    public static float cos(float angle) {
        return sin(1.5707964f + angle);
    }
}
