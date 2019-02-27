package com.vivo.vivotransition.algorithm;

import android.animation.TimeInterpolator;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import com.vivo.vivotransition.AlgorithmUtil;

public class CylinderTransition extends BaseTransition {
    public static final float CAMERA_DISTANCE_FACTOR = 2.5f;
    public static final float DEGREE_TO_RADIAN = 0.01745329f;
    public static final float RADIUS_SCALE = 0.9f;
    private float mAngle;
    private TimeInterpolator mInterpolator;
    boolean mIsCylinderform;
    private float mRadius;

    public CylinderTransition() {
        this.mAngle = -1.0f;
        this.mInterpolator = new DecelerateInterpolator();
        this.mRadius = -1.0f;
        this.mIsCylinderform = false;
        this.mAnimationType = "3D";
        this.mBreakTimes = 4;
        this.mAngle = 360.0f / ((float) (this.mBreakTimes * 2));
        if (this.mTransformationInfo.mCamera == null) {
            this.mTransformationInfo.mCamera = new Camera();
            this.mTransformationInfo.matrix3D = new Matrix();
        }
    }

    public int getBreakOrder(int times, float scrollProgress) {
        if (scrollProgress <= 0.0f) {
            return (this.mBreakTimes - times) - 1;
        }
        return times;
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
        Rect rect = new Rect();
        AlgorithmUtil.getTransformRect(childView, rect);
        float cameraZ = 2.5f * ((float) (-childView.getMeasuredHeight()));
        if (this.mSetCameraZ) {
            cameraZ = this.mCameraZ;
        }
        if (cameraZ != -8.0f) {
            this.mTransformationInfo.mCamera.setLocation(0.0f, 0.0f, (childView.getResources().getDisplayMetrics().density * cameraZ) / ((float) childView.getResources().getDisplayMetrics().densityDpi));
        }
        if (!(this.mLayout_type != 0 || (isOverScrollFirst ^ 1) == 0 || (isOverScrollLast ^ 1) == 0)) {
            this.mTransformationInfo.mTranslationX = ((float) rect.width()) * scrollProgress;
        }
        this.mRadius = (((float) rect.width()) * 0.9f) / 2.0f;
        float clipCenter = ((float) rect.left) + (((float) (rect.width() / this.mBreakTimes)) * (((float) part) + 0.5f));
        float angle = ((this.mAngle * (((float) part) + 0.5f)) - 0.049804688f) - (180.0f * scrollProgress);
        float tz = this.mRadius * (1.0f - AlgorithmUtil.cos(0.01745329f * angle));
        float tx = (((this.mRadius * AlgorithmUtil.sin(0.01745329f * angle)) + ((float) rect.left)) + (((float) rect.width()) / 2.0f)) - clipCenter;
        float alpha = 2.0f - (tz / this.mRadius);
        if (alpha > 1.0f) {
            alpha = 1.0f;
        } else if (alpha < 0.4f) {
            alpha = 0.4f;
        }
        if (Math.abs(scrollProgress) > 0.9f) {
            alpha = 10.0f * ((1.0f - Math.abs(scrollProgress)) * alpha);
        }
        float percent = 1.0f;
        float aProgress = Math.abs(scrollProgress);
        if (aProgress >= 0.05f) {
            if (alpha == 1.0f) {
                this.mIsCylinderform = true;
            }
            this.mTransformationInfo.mAlpha = alpha;
            this.mTransformationInfo.mAlphaDirty = true;
        } else if (!this.mIsCylinderform) {
            percent = this.mInterpolator.getInterpolation(aProgress / 0.05f);
        } else if (!this.mIsScrolling) {
            this.mIsCylinderform = false;
        }
        this.mTransformationInfo.mBounds.set(rect.left + ((rect.width() / this.mBreakTimes) * part), rect.top, rect.left + ((rect.width() / this.mBreakTimes) * (part + 1)), rect.bottom);
        this.mTransformationInfo.mBoundsDirty = true;
        this.mTransformationInfo.mPivotX = clipCenter;
        this.mTransformationInfo.mPivotY = (float) (rect.top + (rect.height() / 2));
        this.mTransformationInfo.mRotationY = angle * percent;
        this.mTransformationInfo.mTranslationX += tx * percent;
        this.mTransformationInfo.mTranslationZ = tz * percent;
        this.mTransformationInfo.mMatrixDirty = true;
        return true;
    }
}
