package com.vivo.vivotransition.algorithm;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import com.vivo.vivotransition.AlgorithmUtil;

public class GoRotateTransition extends BaseTransition {
    private static final float CAMERA_DISTANCE_FACTOR = 2.67f;
    private static final float PAGE_ANGLE = 180.0f;

    public GoRotateTransition() {
        this.mAnimationType = "3D";
        if (this.mTransformationInfo.mCamera == null) {
            this.mTransformationInfo.mCamera = new Camera();
            this.mTransformationInfo.matrix3D = new Matrix();
        }
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View childView) {
        if (Math.abs(scrollProgress) > 0.5f) {
            return false;
        }
        int cw = childView.getWidth();
        int ch = childView.getHeight();
        this.mTransformationInfo.mCamera.setLocation(0.0f, 0.0f, ((((float) (-childView.getMeasuredHeight())) * CAMERA_DISTANCE_FACTOR) * childView.getResources().getDisplayMetrics().density) / ((float) childView.getResources().getDisplayMetrics().densityDpi));
        if (scrollProgress < 0.0f) {
            this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(childView, 0.0f);
        } else {
            float leftScreenPivotX = (float) cw;
            this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(childView, leftScreenPivotX);
        }
        float screenPivotY = ((float) ch) / 2.0f;
        this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(childView, screenPivotY);
        this.mTransformationInfo.mRotationY = (PAGE_ANGLE * scrollProgress) * -1.0f;
        if (this.mLayout_type != 0) {
            this.mTransformationInfo.mTranslationX = ((((float) childView.getMeasuredWidth()) * scrollProgress) * childView.getScaleX()) * -1.0f;
        } else if (isOverScrollFirst || isOverScrollLast) {
            this.mTransformationInfo.mTranslationX = ((-scrollProgress) * ((float) childView.getMeasuredWidth())) * childView.getScaleX();
        }
        this.mTransformationInfo.mMatrixDirty = true;
        return true;
    }
}
