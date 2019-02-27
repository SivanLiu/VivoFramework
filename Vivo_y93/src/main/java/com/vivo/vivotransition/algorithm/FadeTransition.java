package com.vivo.vivotransition.algorithm;

import android.animation.TimeInterpolator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

public class FadeTransition extends BaseTransition {
    private static final float ALPHA_INTERPOLATOR_FACTOR = 0.5f;
    private static final float EFFECT_FADE_MIN_ALPHA = 0.2f;
    private TimeInterpolator mAlphaInterpolator;

    public FadeTransition() {
        this.mAnimationType = "2D";
        this.mAlphaInterpolator = new AccelerateInterpolator(ALPHA_INTERPOLATOR_FACTOR);
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View childView) {
        this.mTransformationInfo.mAlpha = (0.8f * this.mAlphaInterpolator.getInterpolation(1.0f - Math.abs(scrollProgress))) + EFFECT_FADE_MIN_ALPHA;
        this.mTransformationInfo.mAlphaDirty = true;
        return true;
    }
}
