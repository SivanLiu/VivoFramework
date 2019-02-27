package com.vivo.common.indicator;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public interface PageIndicator {
    int getMeasuredWidth();

    float getTranslationY();

    boolean isIndicatorScrolling();

    void setLevel(int i);

    boolean setLevel(int i, int i2);

    void setTotalLevel(int i);

    void setTranslationY(float f);

    void setVisibility(int i);

    void setX(float f);

    void updateIndicator(int i, int i2);
}
