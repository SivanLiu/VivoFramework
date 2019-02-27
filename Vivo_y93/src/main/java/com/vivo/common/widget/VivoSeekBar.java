package com.vivo.common.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.AbsSeekBar;

public class VivoSeekBar extends AbsSeekBar {
    private boolean mIsDragging;
    private boolean mIsMoved;
    boolean mIsUserSeekable;
    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mScaledTouchSlop;
    private int mTempProgress;
    private Drawable mThumb;
    private float mTouchDownX;
    float mTouchProgressOffset;

    public interface OnSeekBarChangeListener {
        void onProgressChanged(VivoSeekBar vivoSeekBar, int i, boolean z);

        void onStartTrackingTouch(VivoSeekBar vivoSeekBar);

        void onStopTrackingTouch(VivoSeekBar vivoSeekBar);
    }

    public VivoSeekBar(Context context) {
        this(context, null);
    }

    public VivoSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 16842875);
    }

    public VivoSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mTempProgress = 0;
        this.mIsMoved = false;
        this.mIsUserSeekable = true;
        this.mPaddingLeft = 0;
        this.mPaddingRight = 0;
        this.mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        this.mOnSeekBarChangeListener = l;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mIsUserSeekable || (isEnabled() ^ 1) != 0) {
            return false;
        }
        this.mThumb = getThumb();
        switch (event.getAction()) {
            case 0:
                if (isScrollContainer()) {
                    this.mTouchDownX = event.getX();
                } else {
                    setPressed(true);
                    if (this.mThumb != null) {
                        invalidate(this.mThumb.getBounds());
                    }
                    this.mTouchDownX = event.getX();
                    attemptClaimDrag();
                }
                this.mTempProgress = getProgress();
                this.mIsMoved = false;
                break;
            case 1:
                if (this.mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouchExtend();
                    setPressed(false);
                } else {
                    onStartTrackingTouchExtend();
                    trackTouchEvent(event);
                    onStopTrackingTouchExtend();
                }
                invalidate();
                break;
            case 2:
                if (!this.mIsDragging) {
                    if (Math.abs(event.getX() - this.mTouchDownX) > ((float) this.mScaledTouchSlop)) {
                        setPressed(true);
                        if (this.mThumb != null) {
                            invalidate(this.mThumb.getBounds());
                        }
                        this.mIsMoved = true;
                        onStartTrackingTouchExtend();
                        trackTouchEvent(event);
                        attemptClaimDrag();
                        break;
                    }
                }
                trackTouchEvent(event);
                break;
                break;
            case 3:
                if (this.mIsDragging) {
                    onStopTrackingTouchExtend();
                    setPressed(false);
                }
                invalidate();
                break;
        }
        return true;
    }

    private void attemptClaimDrag() {
    }

    void onStartTrackingTouchExtend() {
        this.mIsDragging = true;
        if (this.mOnSeekBarChangeListener != null) {
            this.mOnSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }

    void onStopTrackingTouchExtend() {
        this.mIsDragging = false;
        if (this.mOnSeekBarChangeListener != null) {
            this.mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    void onProgressRefresh(boolean fromUser) {
        if (this.mOnSeekBarChangeListener != null) {
            this.mOnSeekBarChangeListener.onProgressChanged(this, getProgress(), fromUser);
        }
    }

    private void trackTouchEvent(MotionEvent event) {
        int width = getWidth();
        int available = (width - this.mPaddingLeft) - this.mPaddingRight;
        int x = (int) event.getX();
        float progress = 0.0f;
        float scale;
        if (this.mIsDragging && this.mIsMoved) {
            scale = ((float) (x - ((int) this.mTouchDownX))) / ((float) available);
            progress = (this.mTouchProgressOffset + ((float) this.mTempProgress)) + (((float) getMax()) * scale);
        } else {
            if (getLayoutDirection() == 1) {
                if (x > width - this.mPaddingRight) {
                    scale = 0.0f;
                } else if (x < this.mPaddingLeft) {
                    scale = 1.0f;
                } else {
                    scale = ((float) ((available - x) + this.mPaddingLeft)) / ((float) available);
                    progress = this.mTouchProgressOffset;
                }
            } else if (x < this.mPaddingLeft) {
                scale = 0.0f;
            } else if (x > width - this.mPaddingRight) {
                scale = 1.0f;
            } else {
                scale = ((float) (x - this.mPaddingLeft)) / ((float) available);
                progress = this.mTouchProgressOffset;
            }
            progress += ((float) getMax()) * scale;
        }
        setProgress((int) progress);
        onProgressRefresh(false);
    }
}
