package com.vivo.alphaindex;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class ToastThumb extends ThumbSelector {
    private int mAlphabetPos;
    private Context mContext;
    private Handler mDelayHandler;
    private DelayedToast mDelayedToast;
    private int mHeight;
    private boolean mIsShowToast;
    private boolean mIsToastDelayed;
    private OnToastShow mShowListener;
    private long mToastDelayTime;
    private int mToastShowX;
    private int mToastShowY;
    private ToastText mToastTextView;
    private FrameLayout mToastView;
    private PopupWindow mToastWindow;
    private int mWidth;

    private class DelayedToast implements Runnable {
        /* synthetic */ DelayedToast(ToastThumb this$0, DelayedToast -this1) {
            this();
        }

        private DelayedToast() {
        }

        public void run() {
            if (ToastThumb.this.mToastWindow.isShowing() && ToastThumb.this.mIsToastDelayed) {
                ToastThumb.this.mToastWindow.dismiss();
            }
        }
    }

    public interface OnToastShow {
        View getView(ToastThumb toastThumb, int i);
    }

    private class ToastText extends TextView implements OnToastShow {
        ToastText(Context context) {
            super(context, null, 50397224);
        }

        public View getView(ToastThumb view, int pos) {
            if (pos < view.getHeader().size() || pos >= view.getAlphabet().size() + view.getHeader().size()) {
                return null;
            }
            setText((CharSequence) view.getAlphabet().get(pos - view.getHeader().size()));
            return this;
        }
    }

    public ToastThumb(Context context) {
        this(context, null);
    }

    public ToastThumb(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mToastWindow = null;
        this.mToastView = null;
        this.mShowListener = null;
        this.mAlphabetPos = 0;
        this.mToastShowX = 30;
        this.mToastShowY = 40;
        this.mWidth = 0;
        this.mHeight = 0;
        this.mIsShowToast = false;
        this.mContext = null;
        this.mIsToastDelayed = false;
        this.mToastDelayTime = 0;
        this.mDelayHandler = new Handler();
        this.mDelayedToast = new DelayedToast(this, null);
        this.mToastTextView = null;
        this.mContext = context;
        this.mToastShowX = (int) (context.getResources().getDisplayMetrics().density * 30.0f);
        this.mToastShowY = (int) (context.getResources().getDisplayMetrics().density * 40.0f);
        this.mToastView = new FrameLayout(context);
        this.mToastWindow = new PopupWindow(this.mToastView, -2, -2);
        this.mToastWindow.setAnimationStyle(0);
        this.mToastTextView = new ToastText(this.mContext);
        setShowListener(this.mToastTextView);
    }

    protected void onDetachedFromWindow() {
        if (this.mIsToastDelayed) {
            this.mDelayHandler.removeCallbacks(this.mDelayedToast);
        }
        super.onDetachedFromWindow();
    }

    protected void setOnTouchEvent(MotionEvent event, int alphabetPos) {
        super.setOnTouchEvent(event, alphabetPos);
        switch (event.getAction()) {
            case 0:
            case 2:
                if (alphabetPos >= 0) {
                    this.mAlphabetPos = alphabetPos;
                    if (isShow()) {
                        update();
                        return;
                    } else {
                        show();
                        return;
                    }
                }
                return;
            case 1:
            case 3:
                dismiss();
                return;
            default:
                return;
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.mHeight = bottom - top;
        this.mWidth = right - left;
        super.onLayout(changed, left, top, right, bottom);
        update();
    }

    private boolean isShow() {
        return this.mToastWindow.isShowing();
    }

    private void show() {
        int[] location = new int[2];
        setToastContent();
        transToastLocation(location);
        if (this.mIsShowToast) {
            if (!isShow()) {
                this.mToastWindow.showAsDropDown(this, location[0], location[1]);
            }
            if (this.mIsToastDelayed) {
                this.mDelayHandler.removeCallbacks(this.mDelayedToast);
                this.mDelayHandler.postDelayed(this.mDelayedToast, this.mToastDelayTime);
                return;
            }
            return;
        }
        dismiss();
    }

    private void update() {
        int[] location = new int[2];
        setToastContent();
        transToastLocation(location);
        if (!this.mIsShowToast) {
            dismiss();
        } else if (isShow()) {
            this.mToastWindow.update(this, location[0], location[1], -1, -1);
            if (this.mIsToastDelayed) {
                this.mDelayHandler.removeCallbacks(this.mDelayedToast);
                this.mDelayHandler.postDelayed(this.mDelayedToast, this.mToastDelayTime);
            }
        }
    }

    private void dismiss() {
        if (this.mToastWindow.isShowing() && !this.mIsToastDelayed) {
            this.mToastWindow.dismiss();
        }
    }

    private void setToastContent() {
        this.mIsShowToast = false;
        if (this.mShowListener != null) {
            View view = this.mShowListener.getView(this, this.mAlphabetPos);
            if (view != null) {
                this.mToastView.removeAllViews();
                this.mToastView.addView(view, -2, -2);
                this.mIsShowToast = true;
            }
        }
    }

    private void transToastLocation(int[] location) {
        if (location == null) {
            throw new NullPointerException();
        }
        if (getLayoutDirection() == 1) {
            location[0] = Math.abs(this.mToastShowX);
        } else {
            this.mToastView.measure(0, 0);
            location[0] = (-Math.abs(this.mToastShowX)) - this.mToastView.getMeasuredWidth();
        }
        if (this.mHeight >= this.mToastShowY) {
            location[1] = -(this.mHeight - this.mToastShowY);
        } else {
            location[1] = this.mToastShowY - this.mHeight;
        }
    }

    public void dismissToast() {
        if (this.mToastWindow.isShowing()) {
            if (!this.mIsToastDelayed) {
                this.mDelayHandler.removeCallbacks(this.mDelayedToast);
            }
            this.mToastWindow.dismiss();
        }
    }

    public void setShowListener(OnToastShow show) {
        this.mShowListener = show;
    }

    public void setToastLocation(int x, int y) {
        this.mToastShowX = x;
        this.mToastShowY = y;
        update();
    }

    public void getToastLocation(int[] pos) {
        if (pos == null) {
            throw new NullPointerException();
        }
        pos[0] = this.mToastShowX;
        pos[1] = this.mToastShowY;
    }

    public void setToastDelayedTime(long milisec) {
        if (milisec > 0) {
            this.mIsToastDelayed = true;
            this.mToastDelayTime = milisec;
            return;
        }
        this.mIsToastDelayed = false;
    }

    public TextView getToastTextView() {
        return this.mToastTextView;
    }
}
