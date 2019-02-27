package com.vivo.common.animation;

import android.R;
import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.vivo.common.animation.SearchView.IScrollLock;
import com.vivo.common.widget.VivoListView;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class LKListView extends VivoListView implements IScrollLock {
    private static final float CLICK_BOTTOM_PADDING = 0.1f;
    private static final int CLICK_INTERVAL = 500;
    private static final float CLICK_LEFT_PADDING = 0.1f;
    private static final float CLICK_RIGHT_PADDING = 0.1f;
    private static final float CLICK_TOP_PADDING = 0.1f;
    private float DownY;
    private Rect mClickRect;
    private boolean mClickWillBack;
    private Context mContext;
    private long mDownTime;
    private float mDownX;
    private float mDownY;
    private boolean mLock;
    private int mNotifyPosY;
    private String mNotifyText;
    private Paint mPaint;
    private Rect mRect;
    private SearchControl mSearchControl;
    private boolean mShowNotify;
    private boolean mSoftInputAffectLayout;
    private int mTouchSlop;
    private boolean mTouchValid;

    private class TextDirectionHeuristicImpl implements TextDirectionHeuristic {
        /* synthetic */ TextDirectionHeuristicImpl(LKListView this$0, TextDirectionHeuristicImpl -this1) {
            this();
        }

        private TextDirectionHeuristicImpl() {
        }

        public boolean isRtl(CharSequence cs, int start, int count) {
            return LKListView.this.getLayoutDirection() == 1;
        }

        public boolean isRtl(char[] array, int start, int count) {
            return LKListView.this.getLayoutDirection() == 1;
        }
    }

    public LKListView(Context context) {
        this(context, null);
    }

    public LKListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mLock = false;
        this.mDownY = 0.0f;
        this.mPaint = new Paint();
        this.mRect = new Rect();
        this.mShowNotify = false;
        this.mSoftInputAffectLayout = false;
        this.mClickWillBack = false;
        this.mDownX = 0.0f;
        this.DownY = 0.0f;
        this.mTouchValid = true;
        this.mClickRect = new Rect();
        this.mNotifyPosY = -1;
        this.mContext = context;
        initalData();
    }

    public LKListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mLock = false;
        this.mDownY = 0.0f;
        this.mPaint = new Paint();
        this.mRect = new Rect();
        this.mShowNotify = false;
        this.mSoftInputAffectLayout = false;
        this.mClickWillBack = false;
        this.mDownX = 0.0f;
        this.DownY = 0.0f;
        this.mTouchValid = true;
        this.mClickRect = new Rect();
        this.mNotifyPosY = -1;
        this.mContext = context;
        initalData();
    }

    private void initalData() {
        this.mTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
        TypedArray a = getContext().obtainStyledAttributes(null, R.styleable.TextView, 50397198, 0);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setTextSize(a.getDimension(2, 0.0f));
        this.mPaint.setColor(a.getColor(5, 0));
        a.recycle();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (checkLock(ev)) {
            return true;
        }
        switch (ev.getAction() & 255) {
            case 0:
                this.mDownX = ev.getX();
                this.mDownY = ev.getY();
                this.mTouchValid = true;
                this.mDownTime = System.currentTimeMillis();
                break;
            case 1:
            case 3:
                if (this.mTouchValid && Math.abs(System.currentTimeMillis() - this.mDownTime) < 500) {
                    onClick();
                    break;
                }
            case 2:
                if (this.mTouchValid && (Math.abs(this.mDownX - ev.getX()) > ((float) this.mTouchSlop) || Math.abs(this.mDownY - ev.getY()) > ((float) this.mTouchSlop))) {
                    this.mTouchValid = false;
                    break;
                }
        }
        return super.onTouchEvent(ev);
    }

    private boolean checkLock(MotionEvent event) {
        switch (event.getAction() & 255) {
            case 0:
                this.mDownY = event.getY();
                break;
            case 2:
                break;
        }
        if (this.mLock && Math.abs(event.getY() - this.mDownY) >= ((float) this.mTouchSlop)) {
            return true;
        }
        return false;
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mNotifyText != null && this.mShowNotify) {
            StaticLayout layout = new StaticLayout(this.mNotifyText, 0, this.mNotifyText.length(), new TextPaint(this.mPaint), getWidth(), Alignment.ALIGN_CENTER, new TextDirectionHeuristicImpl(this, null), 1.0f, 0.0f, false, TruncateAt.END, getWidth(), 2);
            int offsetY = this.mNotifyPosY;
            if (offsetY < 0) {
                offsetY = (getHeight() - layout.getHeight()) / 2;
                if (this.mSoftInputAffectLayout) {
                    offsetY /= 2;
                }
            }
            canvas.save();
            canvas.translate(0.0f, (float) offsetY);
            layout.draw(canvas);
            canvas.restore();
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (checkLock(ev)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void lockScroll() {
        this.mLock = true;
    }

    public void unLockScroll() {
        this.mLock = false;
    }

    public void showNotifyText(boolean value) {
        if (!(this.mShowNotify == value || this.mNotifyText == null)) {
            invalidate();
        }
        this.mShowNotify = value;
    }

    public void setNotifyText(String text) {
        this.mNotifyText = text;
        if (this.mShowNotify) {
            invalidate();
        }
    }

    public void setNotifyTextSize(int textSize) {
        this.mPaint.setTextSize(TypedValue.applyDimension(2, (float) textSize, this.mContext.getResources().getDisplayMetrics()));
        if (this.mShowNotify && this.mNotifyText != null) {
            invalidate();
        }
    }

    public void setSoftInputAffectLayout(boolean value) {
        this.mSoftInputAffectLayout = value;
        if (this.mShowNotify && this.mNotifyText != null) {
            invalidate();
        }
    }

    void setSearchControl(SearchControl searchControl) {
        this.mSearchControl = searchControl;
    }

    void setClickWillBack(boolean value) {
        this.mClickWillBack = value;
    }

    /* JADX WARNING: Missing block: B:4:0x001f, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean rectCheck() {
        int width = getWidth();
        int height = getHeight();
        if (this.mDownX < ((float) width) * 0.1f || this.mDownX > ((float) width) * 0.9f || this.mDownY < ((float) height) * 0.1f || this.mDownY > ((float) height) * 0.9f) {
            return false;
        }
        return true;
    }

    private void onClick() {
        if (this.mClickWillBack && getAdapter() == null && (this.mShowNotify ^ 1) != 0 && this.mSearchControl != null && this.mSearchControl.getSearchState() == 4097 && rectCheck()) {
            this.mSearchControl.switchToNormal();
        }
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        if (this.mLock) {
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    public void setNotifyVerticalPos(int pos) {
        if (this.mNotifyPosY != pos) {
            this.mNotifyPosY = pos;
            invalidate();
        }
    }

    public void setNotifyVerticalPos(int unit, int pos) {
        setNotifyVerticalPos((int) TypedValue.applyDimension(unit, (float) pos, getContext().getResources().getDisplayMetrics()));
    }
}
