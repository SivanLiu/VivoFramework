package com.vivo.vivotransition.algorithm;

import android.view.View;
import com.vivo.vivotransition.AlgorithmUtil;

public class WindMillTransition extends BaseTransition {
    private static final float COORDINATE_Y_FACTOR = 2.0f;
    private static final float PAGE_ANGLE = 45.0f;

    public WindMillTransition() {
        this.mAnimationType = "2D";
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View childView) {
        float pivotY;
        int cw = childView.getWidth();
        int ch = childView.getHeight();
        this.mTransformationInfo.mRotation = (-scrollProgress) * PAGE_ANGLE;
        float pivotX = ((float) cw) / 2.0f;
        this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(childView, pivotX);
        if (ch > cw) {
            pivotY = (float) ch;
        } else {
            pivotY = (float) cw;
        }
        pivotY *= 2.0f;
        this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(childView, pivotY);
        if (!(this.mLayout_type != 0 || (isOverScrollFirst ^ 1) == 0 || (isOverScrollLast ^ 1) == 0)) {
            this.mTransformationInfo.mTranslationX = (((float) cw) * scrollProgress) * childView.getScaleX();
        }
        this.mTransformationInfo.mMatrixDirty = true;
        return true;
    }
}
