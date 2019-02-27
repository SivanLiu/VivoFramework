package com.android.internal.policy;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Canvas;
import android.os.SystemProperties;
import android.provider.SettingsStringUtil;
import android.util.Log;
import android.view.View;
import android.view.View.ViewDelegate;
import android.view.WindowInsets;
import android.view.WindowManager.LayoutParams;

public class NavigationBarImmersive {
    private static final int COLOR_ANIM_DURATION = 100;
    private final boolean ENABLE_ANIMATE = SystemProperties.getBoolean("persist.navcolor.animate", false);
    private AnimatorUpdateListener mAnimatorListener = new AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
            NavigationBarImmersive.this.setImmersiveColor(((Integer) animation.getAnimatedValue()).intValue(), NavigationBarImmersive.this.mNavBgViewColor);
        }
    };
    private ValueAnimator mColorAnimator = null;
    private boolean mColorValid = false;
    private NavigationBarColorCallback mControllerCallback = new NavigationBarColorCallback() {
        public void updateColor(int color, int bgColor, boolean animate) {
            NavigationBarImmersive.this.animateImmersiveColor(color, bgColor, animate ? NavigationBarImmersive.this.mFirstDrawn : false);
        }
    };
    private boolean mEnable = true;
    private boolean mFirstDrawn = false;
    private ImmersiveCallback mImmersiveCallback;
    private int mImmersiveColor = 0;
    private String mLogTag = "NavigationBarImmersive";
    private int mNavBgViewColor;
    private final NavigationBarController mNavigationBarController;
    private int mStartColor = 0;
    private int mTargetColor = 0;
    private final View mView;
    private ViewDelegate mViewDelegate = new ViewDelegate() {
        public void dispatchDraw(Canvas canvas) {
            if (NavigationBarImmersive.this.mEnable) {
                NavigationBarImmersive.this.mNavigationBarController.drawDebug(canvas);
            }
            if (!NavigationBarImmersive.this.mFirstDrawn) {
                NavigationBarImmersive.this.mFirstDrawn = true;
            }
        }

        public void onApplyWindowInsets(WindowInsets insets) {
            if (insets != null) {
                int topInset = insets.getSystemWindowInsetTop();
                int bottomInset = insets.getSystemWindowInsetBottom();
                int rightInset = insets.getSystemWindowInsetRight();
                NavigationBarImmersive.this.mNavigationBarController.insetChanged(insets.getSystemWindowInsetLeft(), topInset, rightInset, bottomInset);
            }
        }

        public void onAttachedToWindow() {
            if (NavigationBarImmersive.this.mEnable) {
                NavigationBarImmersive.this.mNavigationBarController.addPostDrawListener();
            }
        }

        public void onDetachedFromWindow() {
            if (NavigationBarImmersive.this.mEnable) {
                NavigationBarImmersive.this.mNavigationBarController.removeCallbacks();
            }
        }
    };

    public interface ImmersiveCallback {
        void updateNavigationColor(int i, int i2);
    }

    public NavigationBarImmersive(View view) {
        this.mView = view;
        this.mNavigationBarController = new NavigationBarController(this.mView, this.mControllerCallback);
        this.mView.setViewDelegate(this.mViewDelegate);
    }

    public void setEnable(boolean enable) {
        this.mEnable = enable;
        if (this.mEnable) {
            this.mNavigationBarController.addPostDrawListener();
        } else {
            this.mNavigationBarController.removeCallbacks();
        }
    }

    public void setImmersiveCallback(ImmersiveCallback callback) {
        this.mImmersiveCallback = callback;
        if (this.mImmersiveCallback != null && this.mColorValid) {
            this.mImmersiveCallback.updateNavigationColor(this.mImmersiveColor, this.mNavBgViewColor);
        }
    }

    private static int getColorViewTopInset(int stableTop, int systemTop) {
        return Math.min(stableTop, systemTop);
    }

    private static int getColorViewBottomInset(int stableBottom, int systemBottom) {
        return Math.min(stableBottom, systemBottom);
    }

    private static int getColorViewRightInset(int stableRight, int systemRight) {
        return Math.min(stableRight, systemRight);
    }

    private static int getColorViewLeftInset(int stableLeft, int systemLeft) {
        return Math.min(stableLeft, systemLeft);
    }

    private void setImmersiveColor(int color, int bgColor) {
        if (this.mImmersiveColor != color || !this.mColorValid) {
            this.mImmersiveColor = color;
            this.mNavBgViewColor = bgColor;
            if (!this.mColorValid) {
                this.mColorValid = true;
            }
            if (this.mImmersiveCallback != null) {
                this.mImmersiveCallback.updateNavigationColor(this.mImmersiveColor, bgColor);
            }
        }
    }

    private void animateImmersiveColor(int color, int bgColor, boolean animate) {
        if (NavigationBarController.DEBUG) {
            Log.i(this.mLogTag, "animateImmersiveColor, color = " + Integer.toHexString(color) + ", mTargetColor = " + Integer.toHexString(this.mTargetColor) + "  mNavBgColor = " + Integer.toHexString(bgColor));
        }
        if (!this.ENABLE_ANIMATE) {
            animate = false;
        }
        if (!animate) {
            if (this.mColorAnimator != null) {
                this.mColorAnimator.cancel();
                this.mColorAnimator = null;
            }
            setImmersiveColor(color, bgColor);
        } else if (color != this.mTargetColor || this.mColorAnimator == null || !this.mColorAnimator.isRunning()) {
            if (this.mColorAnimator != null) {
                this.mColorAnimator.cancel();
            }
            this.mStartColor = this.mImmersiveColor;
            this.mTargetColor = color;
            this.mColorAnimator = ValueAnimator.ofArgb(new int[]{this.mStartColor, this.mTargetColor});
            this.mColorAnimator.addUpdateListener(this.mAnimatorListener);
            this.mColorAnimator.setDuration(100);
            this.mColorAnimator.start();
        }
    }

    public void updateLogTag(LayoutParams params) {
        this.mLogTag = "NavigationBarImmersive[" + getTitleSuffix(params) + (params != null ? SettingsStringUtil.DELIMITER + params.type : "") + "]";
        this.mNavigationBarController.updateLogTag(params);
    }

    private static String getTitleSuffix(LayoutParams params) {
        if (params == null) {
            return "";
        }
        String[] split = params.getTitle().toString().split("\\.");
        if (split.length > 0) {
            return split[split.length - 1];
        }
        return "";
    }
}
