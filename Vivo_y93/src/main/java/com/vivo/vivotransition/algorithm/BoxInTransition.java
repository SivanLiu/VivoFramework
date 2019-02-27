package com.vivo.vivotransition.algorithm;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import com.vivo.vivotransition.AlgorithmUtil;

public class BoxInTransition extends BaseTransition {
    private static final float PAGE_ANGLE = 80.0f;

    public BoxInTransition() {
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
        if (scrollProgress < 0.0f) {
            if (this.mOrientation == 0) {
                this.mTransformationInfo.mCamera.setLocation(0.0f, 0.0f, -8.0f - ((1.0f + scrollProgress) * 40.0f));
                this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(childView, 0.0f);
            } else {
                this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(childView, 0.0f);
            }
        } else if (this.mOrientation == 0) {
            this.mTransformationInfo.mCamera.setLocation(0.0f, 0.0f, -8.0f - (40.0f * scrollProgress));
            this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(childView, childWidth);
        } else {
            this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(childView, childHeight);
        }
        if (this.mOrientation == 0) {
            this.mTransformationInfo.mRotationY = PAGE_ANGLE * scrollProgress;
            this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(childView, childHeight / 2.0f);
        } else {
            this.mTransformationInfo.mRotationX = (PAGE_ANGLE * scrollProgress) * -1.0f;
            this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(childView, childWidth / 2.0f);
        }
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
