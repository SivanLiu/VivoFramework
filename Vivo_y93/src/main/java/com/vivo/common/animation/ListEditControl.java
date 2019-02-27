package com.vivo.common.animation;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class ListEditControl implements Checkable {
    private static final int MAX_CHILD = 20;
    private static final String TAG = "ListEditControl";
    private int mAlpha = 0;
    private float[] mAnimateViewOrginPos = new float[20];
    private View[] mAnimateViewSet = new View[20];
    private boolean mCheck = false;
    private boolean mCheckAlignLeft = true;
    private Context mContext;
    private int mControlViewCount = 0;
    private float mCurrentProgess;
    private Drawable mDrawable;
    private boolean mLayoutRtl = false;
    private int mLeftMargin = 15;
    private int mMovingDis;
    private ViewGroup mParent;
    private int mRightMargin = 15;
    private int mSelfHeight;
    private int mSelfWidth;
    private int mTopMargin = -1;
    private View mTopView;
    private int mVisible = 0;

    private void Log(String str) {
        Log.d(TAG, str);
    }

    public ListEditControl(Context context, ViewGroup parent) {
        this.mParent = parent;
        this.mContext = context;
    }

    public boolean isChecked() {
        return this.mCheck;
    }

    public void setChecked(boolean checked) {
        int i = -16842910;
        this.mCheck = checked;
        if (this.mDrawable == null) {
            return;
        }
        Drawable drawable;
        int[] iArr;
        if (checked) {
            drawable = this.mDrawable;
            iArr = new int[3];
            iArr[0] = 16842912;
            iArr[1] = 16842908;
            if (this.mParent.isEnabled()) {
                i = 16842910;
            }
            iArr[2] = i;
            drawable.setState(iArr);
            return;
        }
        drawable = this.mDrawable;
        iArr = new int[3];
        iArr[0] = -16842912;
        iArr[1] = 16842908;
        if (this.mParent.isEnabled()) {
            i = 16842910;
        }
        iArr[2] = i;
        drawable.setState(iArr);
    }

    public void toggle() {
        setChecked(this.mCheck ^ 1);
    }

    public void draw(Canvas canvas) {
        if (this.mAlpha != 0 && this.mVisible == 0) {
            float startY;
            float startX;
            if (this.mTopMargin == -1) {
                int height = this.mParent.getHeight();
                int height2 = (this.mTopView == null || this.mTopView.getVisibility() != 0) ? 0 : this.mTopView.getHeight();
                startY = (float) (((height2 + height) - this.mSelfHeight) / 2);
            } else {
                startY = (float) ((this.mTopView == null ? 0 : this.mTopView.getHeight()) + this.mTopMargin);
            }
            if ((!this.mCheckAlignLeft || (isLayoutRtl() ^ 1) == 0) && (this.mCheckAlignLeft || !isLayoutRtl())) {
                startX = ((this.mParent.getX() + ((float) this.mParent.getWidth())) - ((float) this.mSelfWidth)) - ((float) this.mRightMargin);
            } else {
                startX = this.mParent.getX() + ((float) this.mLeftMargin);
            }
            this.mDrawable.setBounds(0, 0, this.mDrawable.getIntrinsicWidth(), this.mDrawable.getIntrinsicHeight());
            canvas.save();
            canvas.translate(startX, startY);
            this.mDrawable.setAlpha(this.mAlpha);
            this.mDrawable.draw(canvas);
            canvas.restore();
        }
    }

    public void setCheckAlignLeftOrRight(boolean left) {
        this.mCheckAlignLeft = left;
        computeMovingDistance();
    }

    public void setCheckMarginLeft(int value) {
        this.mLeftMargin = value;
        computeMovingDistance();
    }

    private void computeMovingDistance() {
        this.mMovingDis = (this.mLeftMargin + this.mSelfWidth) + this.mRightMargin;
        if (this.mCheckAlignLeft) {
            this.mMovingDis = (this.mSelfWidth + this.mRightMargin) + this.mLeftMargin;
        } else {
            this.mMovingDis = -((this.mLeftMargin + this.mSelfWidth) + this.mRightMargin);
        }
    }

    public void setCheckMarginTop(int value) {
        this.mTopMargin = value;
    }

    public void setCheckMarginRight(int value) {
        this.mRightMargin = value;
        computeMovingDistance();
    }

    public void addAnimateChildView(View view) {
        if (this.mControlViewCount < 20) {
            this.mAnimateViewSet[this.mControlViewCount] = view;
            this.mAnimateViewOrginPos[this.mControlViewCount] = view.getX();
            this.mControlViewCount++;
        }
    }

    void jumpDrawablesToCurrentState() {
        if (this.mDrawable != null) {
            this.mDrawable.jumpToCurrentState();
        }
    }

    void clearAnimateChildView() {
        this.mControlViewCount = 0;
    }

    public void setVisible(int visible) {
        this.mVisible = visible;
    }

    public int getVisible() {
        return this.mVisible;
    }

    public void setTopView(View view) {
        this.mTopView = view;
    }

    public void onAnimateUpdate(float p) {
        this.mCurrentProgess = p;
        this.mAlpha = (int) (255.0f * p);
        boolean isLayoutRtl = isLayoutRtl();
        for (int i = 0; i < this.mControlViewCount; i++) {
            if (isLayoutRtl) {
                this.mAnimateViewSet[i].setTranslationX((-p) * ((float) this.mMovingDis));
            } else {
                this.mAnimateViewSet[i].setTranslationX(((float) this.mMovingDis) * p);
            }
        }
        this.mParent.invalidate();
    }

    protected boolean isInit() {
        if (this.mDrawable == null) {
            return false;
        }
        this.mDrawable.setCallback(null);
        this.mParent.unscheduleDrawable(this.mDrawable);
        this.mDrawable.setCallback(this.mParent);
        return true;
    }

    protected void init(Drawable drawable, int left, int top, int right, boolean ltr) {
        this.mDrawable = drawable;
        this.mLeftMargin = left;
        this.mTopMargin = top;
        this.mRightMargin = right;
        this.mCheckAlignLeft = ltr;
        this.mDrawable.setCallback(null);
        this.mParent.unscheduleDrawable(this.mDrawable);
        this.mDrawable.setCallback(this.mParent);
        setChecked(false);
        this.mSelfWidth = this.mDrawable.getIntrinsicWidth();
        this.mSelfHeight = this.mDrawable.getIntrinsicHeight();
        computeMovingDistance();
        Log("mSelfWidth:" + this.mSelfWidth + "   mSelfHeight:" + this.mSelfHeight);
    }

    protected boolean verifyDrawable(Drawable who) {
        if (this.mDrawable == null || !this.mDrawable.equals(who)) {
            return false;
        }
        return true;
    }

    protected void setLayoutRtl(boolean value) {
        this.mLayoutRtl = value;
    }

    private boolean isLayoutRtl() {
        return this.mLayoutRtl;
    }
}
