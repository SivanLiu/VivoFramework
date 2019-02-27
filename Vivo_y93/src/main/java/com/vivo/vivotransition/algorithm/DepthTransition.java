package com.vivo.vivotransition.algorithm;

import android.animation.TimeInterpolator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import com.vivo.vivotransition.AlgorithmUtil;
import com.vivo.vivotransition.interpolator.ZInterpolator;

public class DepthTransition extends BaseTransition {
    private static final float ALPHA_INTERPOLATOR_FACTOR = 0.9f;
    private static final float SCALE_INTERPOLATOR_FACTOR = 0.5f;
    private static final float TRANSITION_SCALE_FACTOR = 0.74f;
    private TimeInterpolator mAlphaInterpolator;
    private TimeInterpolator mScaleInterpolator;

    public DepthTransition() {
        this.mAnimationType = "2D";
        this.mScaleInterpolator = new ZInterpolator(SCALE_INTERPOLATOR_FACTOR);
        this.mAlphaInterpolator = new AccelerateInterpolator(0.9f);
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View childView) {
        float interpolatedProgress = this.mScaleInterpolator.getInterpolation(Math.abs(scrollProgress));
        float screenPivotX = ((float) childView.getWidth()) / 2.0f;
        this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(childView, screenPivotX);
        float screenPivotY = ((float) childView.getHeight()) / 2.0f;
        this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(childView, screenPivotY);
        this.mTransformationInfo.mScaleX = (1.0f - interpolatedProgress) + (TRANSITION_SCALE_FACTOR * interpolatedProgress);
        this.mTransformationInfo.mScaleY = this.mTransformationInfo.mScaleX;
        this.mTransformationInfo.mMatrixDirty = true;
        this.mTransformationInfo.mAlpha = this.mAlphaInterpolator.getInterpolation(1.0f - Math.abs(scrollProgress));
        this.mTransformationInfo.mAlphaDirty = true;
        if (this.mLayout_type != 0) {
            this.mTransformationInfo.mTranslationX = ((((float) childView.getMeasuredWidth()) * scrollProgress) * childView.getScaleX()) * -1.0f;
        } else if (isOverScrollFirst || isOverScrollLast) {
            this.mTransformationInfo.mTranslationX = ((-scrollProgress) * ((float) childView.getMeasuredWidth())) * childView.getScaleX();
        }
        return true;
    }
}
