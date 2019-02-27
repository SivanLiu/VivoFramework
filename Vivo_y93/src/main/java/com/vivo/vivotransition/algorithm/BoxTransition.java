package com.vivo.vivotransition.algorithm;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import com.vivo.vivotransition.AlgorithmUtil;

public class BoxTransition extends BaseTransition {
    private static final float CAMERA_DISTANCE_FACTOR = 2.67f;
    private static final float PAGE_ANGLE = 105.0f;

    public BoxTransition() {
        this.mAnimationType = "3D";
        if (this.mTransformationInfo.mCamera == null) {
            this.mTransformationInfo.mCamera = new Camera();
            this.mTransformationInfo.matrix3D = new Matrix();
        }
    }

    public int getDrawingOrder(int childCount, int i, int part, float scrollProgress) {
        if (scrollProgress == 0.0f || Math.abs(scrollProgress) == 1.0f) {
            return i;
        }
        int index;
        if (scrollProgress > 0.0f) {
            if (((double) scrollProgress) > 0.5d) {
                return i;
            }
            index = i + 1;
            if (index >= childCount) {
                return i;
            }
            return index;
        } else if (((double) scrollProgress) > -0.5d) {
            return i;
        } else {
            index = i - 1;
            if (index < 0) {
                return i;
            }
            return index;
        }
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View childView) {
        float childWidth = (float) childView.getMeasuredWidth();
        float childHeight = (float) childView.getMeasuredHeight();
        float scaleX = childView.getScaleX();
        float cz = -2.67f * childHeight;
        if (this.mSetCameraZ) {
            cz = this.mCameraZ;
        }
        if (cz != -8.0f) {
            this.mTransformationInfo.mCamera.setLocation(0.0f, 0.0f, (childView.getResources().getDisplayMetrics().density * cz) / ((float) childView.getResources().getDisplayMetrics().densityDpi));
        }
        if (scrollProgress < 0.0f) {
            if (this.mOrientation == 0) {
                this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(childView, 0.0f);
            } else {
                this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(childView, 0.0f);
            }
        } else if (this.mOrientation == 0) {
            this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(childView, childWidth);
        } else {
            this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(childView, childHeight);
        }
        if (this.mOrientation == 0) {
            this.mTransformationInfo.mRotationY = (PAGE_ANGLE * scrollProgress) * -1.0f;
            this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(childView, childHeight / 2.0f);
        } else {
            this.mTransformationInfo.mRotationX = PAGE_ANGLE * scrollProgress;
            this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(childView, childWidth / 2.0f);
        }
        this.mTransformationInfo.mAlpha = 1.0f - Math.abs(scrollProgress);
        this.mTransformationInfo.mAlphaDirty = true;
        if (this.mLayout_type == 0) {
            if (isOverScrollFirst || isOverScrollLast) {
                this.mTransformationInfo.mTranslationX = ((scrollProgress * childWidth) * scaleX) * -1.0f;
            } else {
                this.mTransformationInfo.mTranslationX = 0.0f;
            }
        } else if (this.mOrientation == 0) {
            this.mTransformationInfo.mTranslationX = ((scrollProgress * childWidth) * scaleX) * -1.0f;
        } else {
            this.mTransformationInfo.mTranslationY = ((scrollProgress * childHeight) * childView.getScaleY()) * -1.0f;
        }
        this.mTransformationInfo.mMatrixDirty = true;
        return true;
    }
}
