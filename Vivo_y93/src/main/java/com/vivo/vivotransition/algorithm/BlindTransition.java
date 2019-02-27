package com.vivo.vivotransition.algorithm;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.View;
import com.vivo.vivotransition.AlgorithmUtil;

public class BlindTransition extends BaseTransition {
    public BlindTransition() {
        this.mAnimationType = "3D";
        this.mBreakTimes = 4;
        if (this.mTransformationInfo.mCamera == null) {
            this.mTransformationInfo.mCamera = new Camera();
            this.mTransformationInfo.matrix3D = new Matrix();
        }
    }

    public int getDrawingOrder(int size, int index, int times, float fraction) {
        return (size - 1) - index;
    }

    public int getBreakOrder(int times, float scrollProgress) {
        if (scrollProgress <= 0.0f) {
            return (this.mBreakTimes - 1) - times;
        }
        return times;
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View childView) {
        if (((double) Math.abs(scrollProgress)) > 0.5d) {
            return false;
        }
        Rect rect = new Rect();
        AlgorithmUtil.getTransformRect(childView, rect);
        if (!(this.mLayout_type != 0 || (isOverScrollFirst ^ 1) == 0 || (isOverScrollLast ^ 1) == 0)) {
            this.mTransformationInfo.mTranslationX = ((float) rect.width()) * scrollProgress;
        }
        float clipCenter = ((float) rect.left) + (((float) (rect.width() / this.mBreakTimes)) * (((float) part) + 0.5f));
        float angle = -180.0f * scrollProgress;
        this.mTransformationInfo.mBounds.set(rect.left + ((rect.width() / this.mBreakTimes) * part), rect.top, rect.left + ((rect.width() / this.mBreakTimes) * (part + 1)), rect.bottom);
        this.mTransformationInfo.mBoundsDirty = true;
        this.mTransformationInfo.mPivotX = clipCenter;
        this.mTransformationInfo.mPivotY = (float) (rect.top + (rect.height() / 2));
        this.mTransformationInfo.mRotationY = angle;
        this.mTransformationInfo.mMatrixDirty = true;
        return true;
    }
}
