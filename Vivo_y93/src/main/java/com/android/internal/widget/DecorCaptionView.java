package com.android.internal.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewOutlineProvider;
import android.view.Window.WindowControllerCallback;
import com.android.internal.policy.DecorView;
import com.android.internal.policy.PhoneWindow;
import com.vivo.internal.R;
import java.util.ArrayList;

public class DecorCaptionView extends ViewGroup implements OnGestureListener {
    private static final boolean DEBUG = false;
    private static final String TAG = "DecorCaptionView";
    private float mAlphaValue = 0.8f;
    private Drawable mBackGroundDarwable = null;
    private View mCaption;
    private boolean mCheckForDragging;
    private View mClickTarget;
    private View mClose;
    private final Rect mCloseRect = new Rect();
    private View mContent;
    private int mDragSlop;
    private boolean mDragging = false;
    private final boolean mFreeformEnableAlpha = false;
    private GestureDetector mGestureDetector;
    private View mMaximize;
    private final Rect mMaximizeRect = new Rect();
    private View mMinimize;
    private final Rect mMinimizeRect = new Rect();
    private boolean mOverlayWithAppContent = false;
    private PhoneWindow mOwner = null;
    private View mResize;
    private final Rect mResizeRect = new Rect();
    private boolean mShow = false;
    private ArrayList<View> mTouchDispatchList = new ArrayList(2);
    private int mTouchDownX;
    private int mTouchDownY;
    OnTouchListener mTouchListner = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent e) {
            boolean z = true;
            int x = (int) e.getX();
            int y = (int) e.getY();
            boolean fromMouse = e.getToolType(e.getActionIndex()) == 3;
            boolean primaryButton = (e.getButtonState() & 1) != 0;
            switch (e.getActionMasked()) {
                case 0:
                    if (DecorCaptionView.this.mShow) {
                        if (!fromMouse || primaryButton) {
                            DecorCaptionView.this.mCheckForDragging = true;
                            DecorCaptionView.this.mTouchDownX = x;
                            DecorCaptionView.this.mTouchDownY = y;
                            break;
                        }
                    }
                    return false;
                case 1:
                    if (!DecorCaptionView.this.mDragging) {
                        if (DecorCaptionView.this.mMaximizeRect.contains(x, y)) {
                            DecorCaptionView.this.maximizeWindow();
                        }
                        if (DecorCaptionView.this.mCloseRect.contains(x, y)) {
                            DecorCaptionView.this.closeFreeformMode();
                        }
                        if (DecorCaptionView.this.mMinimizeRect.contains(x, y)) {
                            DecorCaptionView.this.miniMizeWindow();
                        }
                        if (DecorCaptionView.this.mResizeRect.contains(x, y)) {
                            DecorCaptionView.this.enterResizeMode();
                            break;
                        }
                    }
                    DecorCaptionView.this.mDragging = false;
                    return DecorCaptionView.this.mCheckForDragging ^ 1;
                    break;
                case 2:
                    if (!DecorCaptionView.this.mDragging && DecorCaptionView.this.mCheckForDragging && (fromMouse || DecorCaptionView.this.passedSlop(x, y))) {
                        DecorCaptionView.this.mCheckForDragging = false;
                        DecorCaptionView.this.mDragging = true;
                        DecorCaptionView.this.startMovingTask(e.getRawX(), e.getRawY());
                        break;
                    }
                case 3:
                    if (DecorCaptionView.this.mDragging) {
                        DecorCaptionView.this.mDragging = false;
                        return DecorCaptionView.this.mCheckForDragging ^ 1;
                    }
                    break;
            }
            if (!DecorCaptionView.this.mDragging) {
                z = DecorCaptionView.this.mCheckForDragging;
            }
            return z;
        }
    };
    private View mTransparentViewForDragging = null;

    public DecorCaptionView(Context context) {
        super(context);
        init(context);
    }

    public DecorCaptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DecorCaptionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.mDragSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mGestureDetector = new GestureDetector(context, (OnGestureListener) this);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mCaption = getChildAt(0);
    }

    public void setPhoneWindow(PhoneWindow owner, boolean show) {
        this.mOwner = owner;
        this.mShow = show;
        updateCaptionVisibility();
        this.mOwner.getDecorView().setOutlineProvider(ViewOutlineProvider.BOUNDS);
        this.mMinimize = findViewById(R.id.vigour_freeform_minimize);
        this.mMaximize = findViewById(R.id.vigour_freeform_maximize);
        this.mClose = findViewById(R.id.vigour_freeform_close);
        this.mResize = findViewById(R.id.vigour_freeform_resize);
        if (((DecorView) this.mOwner.getDecorView()).isInDirectFreeformState() && this.mMinimize != null) {
            this.mMinimize.setVisibility(8);
        }
        this.mTransparentViewForDragging = findViewById(R.id.vigour_caption_top);
        if (this.mTransparentViewForDragging != null) {
            this.mTransparentViewForDragging.setOnTouchListener(this.mTouchListner);
        }
    }

    public ArrayList<View> buildTouchDispatchChildList() {
        this.mTouchDispatchList.ensureCapacity(3);
        if (this.mCaption != null) {
            this.mTouchDispatchList.add(this.mCaption);
        }
        if (this.mContent != null) {
            this.mTouchDispatchList.add(this.mContent);
        }
        return this.mTouchDispatchList;
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    private boolean passedSlop(int x, int y) {
        return Math.abs(x - this.mTouchDownX) > this.mDragSlop || Math.abs(y - this.mTouchDownY) > this.mDragSlop;
    }

    public void onConfigurationChanged(boolean show) {
        this.mShow = show;
        updateCaptionVisibility();
    }

    public void addView(View child, int index, LayoutParams params) {
        if (!(params instanceof MarginLayoutParams)) {
            throw new IllegalArgumentException("params " + params + " must subclass MarginLayoutParams");
        } else if (index >= 2 || getChildCount() >= 2) {
            throw new IllegalStateException("DecorCaptionView can only handle 1 client view");
        } else {
            super.addView(child, 0, params);
            this.mContent = child;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int captionHeight;
        if (this.mCaption.getVisibility() != 8 && 2 == ((DecorView) this.mOwner.getDecorView()).getmStackId() && getParent() == this.mOwner.getDecorView()) {
            measureChildWithMargins(this.mCaption, widthMeasureSpec, 0, heightMeasureSpec, 0);
            captionHeight = this.mCaption.getMeasuredHeight();
        } else {
            captionHeight = 0;
        }
        if (this.mContent != null) {
            if (this.mOverlayWithAppContent) {
                measureChildWithMargins(this.mContent, widthMeasureSpec, 0, heightMeasureSpec, 0);
            } else {
                measureChildWithMargins(this.mContent, widthMeasureSpec, 0, heightMeasureSpec, captionHeight);
            }
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int captionHeight;
        if (this.mCaption.getVisibility() != 8 && 2 == ((DecorView) this.mOwner.getDecorView()).getmStackId() && getParent() == this.mOwner.getDecorView()) {
            this.mCaption.layout(0, 0, this.mCaption.getMeasuredWidth(), this.mCaption.getMeasuredHeight());
            captionHeight = this.mCaption.getBottom() - this.mCaption.getTop();
            this.mMaximize.getHitRect(this.mMaximizeRect);
            this.mClose.getHitRect(this.mCloseRect);
            this.mMinimize.getHitRect(this.mMinimizeRect);
            this.mResize.getHitRect(this.mResizeRect);
            if (((DecorView) this.mOwner.getDecorView()).isInDirectFreeformState()) {
                this.mMinimizeRect.setEmpty();
            }
        } else {
            captionHeight = 0;
            this.mMaximizeRect.setEmpty();
            this.mCloseRect.setEmpty();
            this.mMinimizeRect.setEmpty();
            this.mResizeRect.setEmpty();
        }
        if (this.mContent != null) {
            if (this.mOverlayWithAppContent) {
                this.mContent.layout(0, 0, this.mContent.getMeasuredWidth(), this.mContent.getMeasuredHeight());
            } else {
                this.mContent.layout(0, captionHeight, this.mContent.getMeasuredWidth(), this.mContent.getMeasuredHeight() + captionHeight);
            }
        }
        this.mOwner.notifyRestrictedCaptionAreaCallback(this.mResize.getLeft(), this.mResize.getTop(), this.mClose.getRight(), this.mClose.getBottom());
    }

    private boolean isFillingScreen() {
        return ((getWindowSystemUiVisibility() | getSystemUiVisibility()) & 2565) != 0;
    }

    private void updateCaptionVisibility() {
        this.mCaption.setVisibility(this.mShow ^ 1 ? 8 : 0);
        if (((DecorView) this.mOwner.getDecorView()).isInDirectFreeformState() && this.mMinimize != null) {
            this.mMinimize.setVisibility(8);
        }
    }

    private void maximizeWindow() {
        WindowControllerCallback callback = this.mOwner.getWindowControllerCallback();
        if (callback != null) {
            try {
                callback.exitFreeformMode();
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot change task workspace.");
            }
        }
    }

    public boolean isCaptionShowing() {
        return this.mShow;
    }

    public int getCaptionHeight() {
        return this.mCaption != null ? this.mCaption.getHeight() : 0;
    }

    public void removeContentView() {
        if (this.mContent != null) {
            removeView(this.mContent);
            this.mContent = null;
        }
    }

    public View getCaption() {
        return this.mCaption;
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(-1, -1);
    }

    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    public boolean onDown(MotionEvent e) {
        return false;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        if (this.mClickTarget == this.mMaximize) {
            maximizeWindow();
        } else if (this.mClickTarget == this.mClose) {
            closeFreeformMode();
        } else if (this.mClickTarget == this.mMinimize) {
            miniMizeWindow();
        }
        return true;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public void onLongPress(MotionEvent e) {
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private void updateWindowAlpha(float alpha) {
    }

    private void miniMizeWindow() {
        WindowControllerCallback callback = this.mOwner.getWindowControllerCallback();
        if (callback != null) {
            try {
                callback.miniMizeWindowFreeformMode(true);
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot change task workspace.");
            }
        }
    }

    private void closeFreeformMode() {
        WindowControllerCallback callback = this.mOwner.getWindowControllerCallback();
        if (callback != null) {
            try {
                callback.miniMizeWindowFreeformMode(false);
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot change task workspace.");
            }
        }
        this.mOwner.dispatchOnWindowDismissed(true, true);
    }

    public void enterResizeMode() {
        WindowControllerCallback callback = this.mOwner.getWindowControllerCallback();
        if (callback != null) {
            try {
                callback.enterResizeMode(true);
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot change task workspace.");
            }
        }
    }

    protected void dispatchDraw(Canvas canvas) {
        if (!(this.mBackGroundDarwable == null || this.mOwner == null || ((((ViewGroup) this.mOwner.getDecorView()).getChildCount() != 1 && !((DecorView) this.mOwner.getDecorView()).isVivoThemeApp(getContext().getPackageName())) || this.mCaption == null || this.mContent == null || 2 != getStackId()))) {
            this.mBackGroundDarwable.setBounds(new Rect(0, this.mCaption.getMeasuredHeight(), this.mContent.getMeasuredWidth(), this.mContent.getMeasuredHeight() + this.mCaption.getMeasuredHeight()));
            this.mBackGroundDarwable.draw(canvas);
        }
        super.dispatchDraw(canvas);
    }

    public void saveBackGroundDrawable(Drawable background) {
        this.mBackGroundDarwable = background;
    }

    private int getStackId() {
        int workspaceId = -1;
        WindowControllerCallback callback = this.mOwner.getWindowControllerCallback();
        if (callback != null) {
            try {
                workspaceId = callback.getWindowStackId();
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to get the workspace ID of a PhoneWindow.");
            }
        }
        if (workspaceId == -1) {
            return 1;
        }
        return workspaceId;
    }

    public void setContentNull() {
        this.mContent = null;
    }
}
