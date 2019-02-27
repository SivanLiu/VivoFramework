package com.vivo.vivotransition.algorithm;

import android.animation.TimeInterpolator;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import com.vivo.vivotransition.AlgorithmUtil;
import com.vivo.vivotransition.interpolator.ZInterpolator;

public class StackTransition extends BaseTransition {
    private static final float ALPHA_INTERPOLATOR_FACTOR = 0.9f;
    private static final float CAMERA_DISTANCE_FACTOR = 2.67f;
    private static final float SCALE_INTERPOLATOR_FACTOR = 0.5f;
    private static final float TRANSITION_SCALE_FACTOR = 0.74f;
    private TimeInterpolator mAlphaInterpolator;
    private TimeInterpolator mZInterpolator;

    public StackTransition() {
        this.mZInterpolator = new ZInterpolator(SCALE_INTERPOLATOR_FACTOR);
        this.mAlphaInterpolator = new AccelerateInterpolator(0.9f);
        this.mAnimationType = "3D";
        if (this.mTransformationInfo.mCamera == null) {
            this.mTransformationInfo.mCamera = new Camera();
            this.mTransformationInfo.matrix3D = new Matrix();
        }
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View childView) {
        float cameraZ = -2.67f * ((float) childView.getMeasuredHeight());
        if (this.mSetCameraZ) {
            cameraZ = this.mCameraZ;
        }
        if (cameraZ != -8.0f) {
            this.mTransformationInfo.mCamera.setLocation(0.0f, 0.0f, (childView.getResources().getDisplayMetrics().density * cameraZ) / ((float) childView.getResources().getDisplayMetrics().densityDpi));
        }
        float interpolatedProgress = this.mZInterpolator.getInterpolation(Math.abs(Math.max(scrollProgress, 0.0f)));
        int width = childView.getMeasuredWidth();
        float pivotX = ((float) width) / 2.0f;
        float pivotY = ((float) childView.getMeasuredHeight()) / 2.0f;
        float scale = (1.0f - interpolatedProgress) + (TRANSITION_SCALE_FACTOR * interpolatedProgress);
        float alpha = scrollProgress > 0.0f ? this.mAlphaInterpolator.getInterpolation(1.0f - Math.abs(scrollProgress)) : 1.0f;
        this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(childView, pivotX);
        this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(childView, pivotY);
        this.mTransformationInfo.mTranslationX = Math.max(0.0f, scrollProgress) * ((float) width);
        this.mTransformationInfo.mScaleX = scale;
        this.mTransformationInfo.mScaleY = scale;
        this.mTransformationInfo.mMatrixDirty = true;
        this.mTransformationInfo.mAlpha = alpha;
        this.mTransformationInfo.mAlphaDirty = true;
        return true;
    }
}
