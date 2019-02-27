package com.vivo.vivotransition.algorithm;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.View;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public abstract class BaseTransition {
    public boolean mAlphaMode = true;
    public String mAnimationType = null;
    public int mBreakTimes = 1;
    public float mCameraZ = -8.0f;
    public int mHorizBreakTimes = 1;
    public boolean mIsScrolling = false;
    public int mLayout_type = 0;
    public int mOrientation = 0;
    public int mPageSpacing = 0;
    public boolean mSetCameraZ = false;
    public TransformationInfo mTransformationInfo = new TransformationInfo();
    public boolean mUseBg = false;

    public static class TransformationInfo {
        public float mAlpha = 1.0f;
        public boolean mAlphaDirty = false;
        public boolean mBackgroundDirty = false;
        public final Rect mBounds = new Rect();
        public boolean mBoundsDirty = false;
        public Camera mCamera = null;
        public final Matrix mMatrix = new Matrix();
        public boolean mMatrixDirty = false;
        public float mPivotX = 0.0f;
        public float mPivotY = 0.0f;
        public float mRotation = 0.0f;
        public float mRotationX = 0.0f;
        public float mRotationY = 0.0f;
        public float mScaleX = 1.0f;
        public float mScaleY = 1.0f;
        public float mTranslationX = 0.0f;
        public float mTranslationY = 0.0f;
        public float mTranslationZ = 0.0f;
        public Matrix matrix3D = null;

        private void clearDirty() {
            this.mAlphaDirty = false;
            this.mAlpha = 1.0f;
            this.mMatrixDirty = false;
            this.mBoundsDirty = false;
            clearMatrix();
        }

        private void clearMatrix() {
            this.mRotation = 0.0f;
            this.mRotationX = 0.0f;
            this.mRotationY = 0.0f;
            this.mScaleX = 1.0f;
            this.mScaleY = 1.0f;
            this.mTranslationX = 0.0f;
            this.mTranslationY = 0.0f;
        }
    }

    public static final class TransitionType {
        public static final int Blind = 10;
        public static final int Box = 6;
        public static final int BoxIn = 7;
        public static final int Cylinder = 4;
        public static final int Default = 0;
        public static final int Depth = 3;
        public static final int Fade = 11;
        public static final int Flipover = 8;
        public static final int Page = 9;
        public static final int Push = 1;
        public static final int Rotation = 2;
        public static final int Stack = 12;
        public static final int Windmill = 5;
    }

    public abstract boolean transform(int i, boolean z, boolean z2, float f, View view);

    private static boolean nonzero(float f) {
        if (f < -0.001f || f > 0.001f) {
            return true;
        }
        return false;
    }

    private void updateMatrix() {
        TransformationInfo transformationinfo = this.mTransformationInfo;
        if (transformationinfo.mMatrixDirty) {
            transformationinfo.mMatrix.reset();
            if (nonzero(transformationinfo.mRotationX) || (nonzero(transformationinfo.mRotationY) ^ 1) == 0) {
                transformationinfo.mCamera.save();
                transformationinfo.mMatrix.preScale(transformationinfo.mScaleX, transformationinfo.mScaleY, transformationinfo.mPivotX, transformationinfo.mPivotY);
                transformationinfo.mCamera.translate(0.0f, 0.0f, transformationinfo.mTranslationZ);
                transformationinfo.mCamera.rotate(transformationinfo.mRotationX, transformationinfo.mRotationY, -transformationinfo.mRotation);
                transformationinfo.mCamera.getMatrix(transformationinfo.matrix3D);
                transformationinfo.matrix3D.preTranslate(-transformationinfo.mPivotX, -transformationinfo.mPivotY);
                transformationinfo.matrix3D.postTranslate(transformationinfo.mPivotX + transformationinfo.mTranslationX, transformationinfo.mPivotY + transformationinfo.mTranslationY);
                transformationinfo.mMatrix.postConcat(transformationinfo.matrix3D);
                transformationinfo.mCamera.restore();
                return;
            }
            transformationinfo.mMatrix.setTranslate(transformationinfo.mTranslationX, transformationinfo.mTranslationY);
            transformationinfo.mMatrix.preRotate(transformationinfo.mRotation, transformationinfo.mPivotX, transformationinfo.mPivotY);
            transformationinfo.mMatrix.preScale(transformationinfo.mScaleX, transformationinfo.mScaleY, transformationinfo.mPivotX, transformationinfo.mPivotY);
        }
    }

    public String getAnimationType() {
        return this.mAnimationType;
    }

    public int getBreakOrder(int times, float scrollProgress) {
        return times;
    }

    public int getBreakTimes() {
        return this.mBreakTimes;
    }

    public int getHorizBreakTimes() {
        return this.mHorizBreakTimes;
    }

    public int getDrawingOrder(int size, int index, int times, float fraction) {
        return index;
    }

    public TransformationInfo getTransformation(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View parent, View child, int pageSpacing) {
        this.mTransformationInfo.clearDirty();
        this.mPageSpacing = pageSpacing;
        if (!transform(part, isOverScrollFirst, isOverScrollLast, scrollProgress, child)) {
            return null;
        }
        updateMatrix();
        if (!this.mAlphaMode) {
            this.mTransformationInfo.mAlphaDirty = false;
        }
        return this.mTransformationInfo;
    }

    public boolean isHorizental() {
        return this.mOrientation == 0;
    }

    public void reset() {
        this.mSetCameraZ = false;
    }

    public void setAlphaMode(boolean alphaMode) {
        this.mAlphaMode = alphaMode;
    }

    public void setCameraDistance(float distance) {
        this.mCameraZ = distance;
        this.mSetCameraZ = true;
    }

    public void setLayoutType(int type) {
        this.mLayout_type = type;
    }

    public void setOrientation(int orientation) {
        this.mOrientation = orientation;
    }

    public void setState(boolean isScrolling) {
        this.mIsScrolling = isScrolling;
    }
}
