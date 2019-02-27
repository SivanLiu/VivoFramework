package com.vivo.vivotransition.algorithm;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.View;
import com.vivo.vivotransition.AlgorithmUtil;

public class PageTransition extends BaseTransition {
    private static final float CAMARA_DISTANCE = -15.0f;
    private static final int PAGE_LEFT = 0;
    private static final int PAGE_RIGHT = 1;

    public PageTransition() {
        this.mAnimationType = "3D";
        this.mUseBg = true;
        this.mAlphaMode = false;
        this.mBreakTimes = 2;
        this.mTransformationInfo.mBackgroundDirty = true;
        if (this.mTransformationInfo.mCamera == null) {
            this.mTransformationInfo.mCamera = new Camera();
            this.mTransformationInfo.matrix3D = new Matrix();
        }
    }

    public int getDrawingOrder(int childCount, int i, int part, float scrollProgress) {
        if (part != 0) {
            return (childCount - i) - 1;
        }
        return i;
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View childView) {
        if (!(this.mLayout_type != 0 || (isOverScrollFirst ^ 1) == 0 || (isOverScrollLast ^ 1) == 0)) {
            this.mTransformationInfo.mTranslationX = ((((float) childView.getMeasuredWidth()) * childView.getScaleX()) + ((float) this.mPageSpacing)) * scrollProgress;
            this.mTransformationInfo.mMatrixDirty = true;
        }
        Rect rect = new Rect();
        AlgorithmUtil.getTransformRect(childView, rect);
        float scale = childView.getResources().getDisplayMetrics().density;
        if (this.mOrientation == 0) {
            this.mTransformationInfo.mBounds.set((rect.width() / 2) * part, 0, (rect.width() / 2) * (part + 1), rect.height());
        } else {
            this.mTransformationInfo.mBounds.set(0, (rect.height() / 2) * part, rect.width(), (rect.height() / 2) * (part + 1));
        }
        this.mTransformationInfo.mBoundsDirty = true;
        float pivotX;
        float pivotY;
        if (part == 1) {
            if (scrollProgress < 0.0f) {
                if (((double) scrollProgress) < -0.5d) {
                    this.mTransformationInfo.mAlpha = (1.0f + scrollProgress) * 2.0f;
                    this.mTransformationInfo.mAlphaDirty = true;
                }
                return true;
            } else if (((double) scrollProgress) >= 0.5d) {
                return false;
            } else {
                this.mTransformationInfo.mCamera.setLocation(0.0f, 0.0f, CAMARA_DISTANCE * scale);
                if (this.mOrientation == 0) {
                    this.mTransformationInfo.mRotationY = (-scrollProgress) * 180.0f;
                } else {
                    this.mTransformationInfo.mRotationX = 180.0f * scrollProgress;
                }
                pivotX = ((float) childView.getMeasuredWidth()) / 2.0f;
                this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(childView, pivotX);
                pivotY = ((float) childView.getMeasuredHeight()) / 2.0f;
                this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotX(childView, pivotY);
                this.mTransformationInfo.mMatrixDirty = true;
                return true;
            }
        } else if (scrollProgress >= 0.0f) {
            if (((double) scrollProgress) > 0.5d) {
                this.mTransformationInfo.mAlpha = (1.0f - scrollProgress) * 2.0f;
                this.mTransformationInfo.mAlphaDirty = true;
            }
            return true;
        } else if (((double) scrollProgress) <= -0.5d) {
            return false;
        } else {
            this.mTransformationInfo.mCamera.setLocation(0.0f, 0.0f, CAMARA_DISTANCE * scale);
            if (this.mOrientation == 0) {
                this.mTransformationInfo.mRotationY = (-scrollProgress) * 180.0f;
            } else {
                this.mTransformationInfo.mRotationX = 180.0f * scrollProgress;
            }
            pivotX = ((float) childView.getMeasuredWidth()) / 2.0f;
            this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(childView, pivotX);
            pivotY = ((float) childView.getMeasuredHeight()) / 2.0f;
            this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotX(childView, pivotY);
            this.mTransformationInfo.mMatrixDirty = true;
            return true;
        }
    }
}
