package android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import com.vivo.internal.R;

public class SlideLayout extends ViewGroup {
    private static final boolean DEBUG = true;
    private static final int DEF_COLUMNS = 4;
    private static final int DEF_ROWS = 2;
    private static final int DEF_VELOCITY_UNIT = 100;
    public static final int INDICATOR_COLOR_ACTIVE = -10724260;
    public static final int INDICATOR_COLOR_INACTIVE = -5723992;
    public static final int SCROLL_MODE_NONE = 1;
    public static final int SCROLL_MODE_NORMAL = 2;
    public static final int SCROLL_MODE_USER = 3;
    private static final int SLIDE_FLING_DURATION = 400;
    private static final String TAG = "SlideLayout";
    private static int mIndicatorMaxLevel = 0;
    private float MAX_VERTICAL_OFFSET;
    private int MIN_SLIDE_VELOCITY;
    private boolean mAdaptiveWidth;
    private int mColumnGap;
    private int mColumnsPerPage;
    private int mContentHeight;
    private int mContentWidth;
    private int mCurPages;
    private int mCurRowsPerPage;
    private FrameLayout mCustomView;
    private Rect mCustomViewGap;
    private boolean mDrawWithDrawable;
    private boolean mEnableCoalesce;
    private int mGravity;
    private int mHorizontalOffset;
    private float mHorizontalOffsetAdjustPercent;
    private Drawable mIndicatorActive;
    private Paint mIndicatorActivePaint;
    private int mIndicatorBottomGap;
    private int mIndicatorCircleGap;
    private int mIndicatorCircleRadius;
    private LevelListDrawable mIndicatorDrawable;
    private int mIndicatorHeight;
    private Drawable mIndicatorNormal;
    private Paint mIndicatorPaint;
    private boolean mIndicatorShow;
    private int mIndicatorTopGap;
    private int mIndicatorWidth;
    private int mIndicatorX;
    private int mIndicatorY;
    private int mInitialOffset;
    private int mItemHeight;
    private int mItemWidth;
    private int mLastChildCount;
    private int mLastIndicatorPos;
    private float mLastTouchX;
    private float mLastTouchY;
    private int mLeftScrollOffset;
    private int mMaxRowsPerPage;
    private int mPreHorizontalOffset;
    private int mRightScrollOffset;
    private int mRowGap;
    private int mScrollMode;
    private Scroller mScroller;
    private int mTouchDownPosition;
    private float mTouchDownX;
    private float mTouchDownY;
    private float mTouchHorizontal;
    private TouchMode mTouchMode;
    private float mTouchSlop;
    private VelocityTracker mVelocityTracker;

    private class FlingInterpolator implements Interpolator {
        /* synthetic */ FlingInterpolator(SlideLayout this$0, FlingInterpolator -this1) {
            this();
        }

        private FlingInterpolator() {
        }

        public float getInterpolation(float input) {
            return ((float) Math.pow((double) (input - 1.0f), 3.0d)) + 1.0f;
        }
    }

    private enum TouchMode {
        REST,
        SCROLL,
        DOWN
    }

    public SlideLayout(Context context) {
        this(context, null);
    }

    public SlideLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.slideLayoutStyle);
    }

    public SlideLayout(Context context, AttributeSet attrs, int defAttr) {
        super(context, attrs, defAttr);
        this.MAX_VERTICAL_OFFSET = 100.0f;
        this.MIN_SLIDE_VELOCITY = 60;
        this.mColumnsPerPage = 4;
        this.mMaxRowsPerPage = 2;
        this.mCurRowsPerPage = 0;
        this.mCurPages = 0;
        this.mAdaptiveWidth = true;
        this.mRowGap = 0;
        this.mColumnGap = 0;
        this.mItemWidth = 0;
        this.mItemHeight = 0;
        this.mContentWidth = 0;
        this.mContentHeight = 0;
        this.mIndicatorWidth = 0;
        this.mIndicatorHeight = 20;
        this.mIndicatorX = 0;
        this.mIndicatorY = 0;
        this.mIndicatorCircleGap = 15;
        this.mIndicatorCircleRadius = 3;
        this.mIndicatorTopGap = 0;
        this.mIndicatorBottomGap = 0;
        this.mLastIndicatorPos = 0;
        this.mIndicatorShow = true;
        this.mEnableCoalesce = false;
        this.mDrawWithDrawable = false;
        this.mIndicatorPaint = null;
        this.mIndicatorActivePaint = null;
        this.mCustomView = null;
        this.mCustomViewGap = new Rect();
        this.mScrollMode = 2;
        this.mHorizontalOffset = 0;
        this.mPreHorizontalOffset = 0;
        this.mHorizontalOffsetAdjustPercent = 0.4f;
        this.mInitialOffset = 0;
        this.mTouchDownPosition = 0;
        this.mTouchHorizontal = 0.0f;
        this.mVelocityTracker = null;
        this.mScroller = null;
        this.mTouchMode = TouchMode.REST;
        this.mIndicatorDrawable = null;
        this.mIndicatorNormal = null;
        this.mIndicatorActive = null;
        this.mLeftScrollOffset = 0;
        this.mRightScrollOffset = 0;
        this.mGravity = 3;
        this.mLastChildCount = 0;
        setWillNotDraw(false);
        initParameters(context, attrs, defAttr);
        this.mCustomView = new FrameLayout(context);
        this.mScroller = new Scroller(context, new FlingInterpolator(this, null));
        this.mIndicatorPaint = new Paint();
        this.mIndicatorPaint.setColor(INDICATOR_COLOR_INACTIVE);
        this.mIndicatorPaint.setAntiAlias(true);
        this.mIndicatorActivePaint = new Paint(this.mIndicatorPaint);
        this.mIndicatorActivePaint.setColor(INDICATOR_COLOR_ACTIVE);
        this.mIndicatorActivePaint.setAntiAlias(true);
    }

    private void initParameters(Context context, AttributeSet attrs, int defAttr) {
        float density = getResources().getDisplayMetrics().density;
        this.mIndicatorCircleRadius = (int) (((float) this.mIndicatorCircleRadius) * density);
        this.mIndicatorCircleGap = (int) (((float) this.mIndicatorCircleGap) * density);
        this.mIndicatorTopGap = (int) (((float) this.mIndicatorTopGap) * density);
        this.mIndicatorBottomGap = (int) (((float) this.mIndicatorBottomGap) * density);
        this.mTouchSlop = (float) ViewConfiguration.get(context).getScaledTouchSlop();
        boolean useIndicatorDrawable = false;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlideLayout, defAttr, 0);
        for (int i = a.getIndexCount() - 1; i >= 0; i--) {
            int attr = a.getIndex(i);
            switch (attr) {
                case 0:
                    this.mColumnsPerPage = a.getInt(attr, this.mColumnsPerPage);
                    if (this.mColumnsPerPage > 0) {
                        break;
                    }
                    this.mColumnsPerPage = 4;
                    break;
                case 1:
                    this.mMaxRowsPerPage = a.getInt(attr, this.mMaxRowsPerPage);
                    if (this.mMaxRowsPerPage > 0) {
                        break;
                    }
                    this.mMaxRowsPerPage = 2;
                    break;
                case 2:
                    this.mColumnGap = a.getDimensionPixelOffset(attr, this.mColumnGap);
                    break;
                case 3:
                    this.mRowGap = a.getDimensionPixelOffset(attr, this.mRowGap);
                    break;
                case 4:
                    useIndicatorDrawable = a.getBoolean(attr, false);
                    break;
                case 5:
                    this.mIndicatorCircleGap = a.getDimensionPixelOffset(attr, this.mIndicatorCircleGap);
                    break;
                case 6:
                    this.mIndicatorCircleRadius = a.getDimensionPixelOffset(attr, this.mIndicatorCircleRadius);
                    break;
                case 7:
                    initIndicatorDrawable(a.getResourceId(attr, 0));
                    break;
                case 8:
                    this.mIndicatorTopGap = a.getDimensionPixelOffset(attr, this.mIndicatorTopGap);
                    break;
                case 9:
                    this.mIndicatorBottomGap = a.getDimensionPixelOffset(attr, this.mIndicatorBottomGap);
                    break;
                case 10:
                    this.mLeftScrollOffset = a.getDimensionPixelOffset(attr, this.mLeftScrollOffset);
                    break;
                case 11:
                    this.mRightScrollOffset = a.getDimensionPixelOffset(attr, this.mRightScrollOffset);
                    break;
                default:
                    break;
            }
        }
        a.recycle();
        setIndicatorDrawable(useIndicatorDrawable);
    }

    private void initIndicatorDrawable(int animId) {
        Resources res = getContext().getResources();
        if (this.mIndicatorActive == null || this.mIndicatorNormal == null || this.mIndicatorDrawable == null) {
            int i;
            TypedArray a = getContext().obtainStyledAttributes(null, R.styleable.IndicatorAnim, 0, animId);
            TypedArray animArray = null;
            for (i = a.getIndexCount() - 1; i >= 0; i--) {
                int attr = a.getIndex(i);
                switch (attr) {
                    case 0:
                        if (this.mIndicatorActive != null) {
                            break;
                        }
                        this.mIndicatorActive = a.getDrawable(attr);
                        break;
                    case 1:
                        if (this.mIndicatorNormal != null) {
                            break;
                        }
                        this.mIndicatorNormal = a.getDrawable(attr);
                        break;
                    case 2:
                        animArray = res.obtainTypedArray(a.getResourceId(attr, 0));
                        break;
                    default:
                        break;
                }
            }
            if (animArray != null && this.mIndicatorDrawable == null) {
                this.mIndicatorDrawable = new LevelListDrawable();
                mIndicatorMaxLevel = animArray.length() - 1;
                for (i = 0; i < animArray.length(); i++) {
                    this.mIndicatorDrawable.addLevel(i, i, res.getDrawable(animArray.getResourceId(i, 0)));
                }
                animArray.recycle();
            }
            a.recycle();
            return;
        }
        Log.d(TAG, "indicator drawable already exists, don't retrive again");
    }

    public int getPageColumns() {
        return this.mColumnsPerPage;
    }

    public void setPageColumns(int columns) {
        if (this.mColumnsPerPage != columns && columns > 0) {
            this.mColumnsPerPage = columns;
            caculatePageParams();
            reLayout();
        }
    }

    public int getPageRows() {
        return this.mMaxRowsPerPage;
    }

    public void setPageRows(int rows) {
        if (this.mMaxRowsPerPage != rows && rows > 0) {
            this.mMaxRowsPerPage = rows;
            caculatePageParams();
            reLayout();
        }
    }

    public int getRowGap() {
        return this.mRowGap;
    }

    public void setRowGap(int gap) {
        if (this.mRowGap != gap && gap >= 0) {
            this.mRowGap = gap;
            reLayout();
        }
    }

    public int getColumnGap() {
        return this.mColumnGap;
    }

    public void setColumnGap(int gap) {
        if (this.mColumnGap != gap && gap >= 0) {
            this.mColumnGap = gap;
            reLayout();
        }
    }

    public boolean isAdaptiveWidth() {
        return this.mAdaptiveWidth;
    }

    public void setAdaptiveWidth(boolean adaptive) {
        if (this.mAdaptiveWidth != adaptive) {
            this.mAdaptiveWidth = adaptive;
            reLayout();
        }
    }

    public boolean isIndicatorDrawable() {
        return this.mDrawWithDrawable;
    }

    public void setIndicatorDrawable(boolean use) {
        if (this.mDrawWithDrawable != use) {
            if (use) {
                if (this.mIndicatorDrawable == null || this.mIndicatorNormal == null || this.mIndicatorActive == null) {
                    throw new IllegalArgumentException("must provide indicator drawable use drawable indicator");
                }
            } else if (this.mIndicatorCircleGap <= getSlideCircleRadius() * 2) {
                throw new IllegalArgumentException("invalide CircleRaidus : " + this.mIndicatorCircleRadius + " CircleDiameter must greater than CircelGap : " + this.mIndicatorCircleGap);
            }
            this.mDrawWithDrawable = use;
            invalidate();
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        boolean verify = false;
        if (this.mDrawWithDrawable) {
            verify = who == this.mIndicatorActive || who == this.mIndicatorNormal || who == this.mIndicatorDrawable.getCurrent();
        }
        return !verify ? super.verifyDrawable(who) : true;
    }

    public void setIndicatorCircleRadius(int radius) {
        if (this.mIndicatorCircleRadius != radius && radius >= 0) {
            this.mIndicatorCircleRadius = radius;
            reLayout();
        }
    }

    public void setIndicatorCircleGap(int gap) {
        if (this.mIndicatorCircleGap != gap && gap >= 0) {
            this.mIndicatorCircleGap = gap;
            reLayout();
        }
    }

    public void setIndicatorGap(int top, int bottom) {
        boolean needLayout = false;
        if (this.mIndicatorTopGap != top && top >= 0) {
            this.mIndicatorTopGap = top;
            needLayout = true;
        }
        if (this.mIndicatorBottomGap != bottom && bottom >= 0) {
            this.mIndicatorBottomGap = bottom;
            needLayout = true;
        }
        if (needLayout) {
            reLayout();
        }
    }

    public void setCustomView(View v) {
        if (v != null) {
            this.mCustomView.removeAllViews();
            this.mCustomView.addView(v, new LayoutParams(-1, -2));
            reLayout();
        }
    }

    public void setCustomView(int layoutId) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        this.mCustomView.removeAllViews();
        layoutInflater.inflate(layoutId, this.mCustomView, true);
        reLayout();
    }

    public void setCustomViewGap(int left, int top, int right, int bottom) {
        setCustomViewGap(new Rect(left, top, right, bottom));
    }

    public void setCustomViewGap(Rect rect) {
        if (rect != null) {
            this.mCustomViewGap = rect;
            reLayout();
        }
    }

    public void setGravity(int gravity) {
        if (gravity != this.mGravity) {
            this.mGravity = gravity;
            reLayout();
        }
    }

    public int getCurrentPagePosition() {
        int pages = getCurrentPages() - 1;
        int offset = Math.abs(this.mHorizontalOffset / getWidth());
        if (getWidth() <= 0) {
            return this.mInitialOffset;
        }
        if (isRtl()) {
            offset = pages - offset;
        }
        return offset;
    }

    public int getCurrentPages() {
        caculatePageParams();
        return this.mCurPages;
    }

    public void setToPage(int pos) {
        if (this.mScrollMode == 1) {
            Log.i(TAG, "don't support setToPage in mode : " + this.mScrollMode);
            return;
        }
        caculatePageParams();
        if (pos < 0 || pos >= this.mCurPages) {
            Log.e(TAG, "setTo overflow " + pos + ". max validate is " + this.mCurPages);
            return;
        }
        if (getWidth() > 0) {
            if (isRtl()) {
                pos = (getCurrentPages() - 1) - pos;
            }
            this.mHorizontalOffset = (-pos) * getWidth();
            setScrollX(this.mHorizontalOffset * -1);
        } else {
            this.mInitialOffset = pos;
        }
    }

    public void scrollToPage(int pos) {
        if (this.mScrollMode == 1) {
            Log.i(TAG, "don't support scrollToPage in mode : " + this.mScrollMode);
            return;
        }
        caculatePageParams();
        if (pos < 0 || pos >= this.mCurPages) {
            Log.e(TAG, "scrollTo overflow " + pos + ". max validate is " + this.mCurPages);
            return;
        }
        if (getWidth() > 0) {
            if (isRtl()) {
                pos = (getCurrentPages() - 1) - pos;
            }
            this.mScroller.startScroll(this.mHorizontalOffset, 0, ((-pos) * getWidth()) - this.mHorizontalOffset, 0, 400);
        } else {
            this.mInitialOffset = pos;
        }
    }

    public boolean getIndicatorVisible() {
        return this.mIndicatorShow;
    }

    public void setIndicatorVisible(boolean visible) {
        if (this.mIndicatorShow != visible) {
            this.mIndicatorShow = visible;
            reLayout();
        }
    }

    public void setScrollMode(int mode) {
        if (this.mScrollMode != mode) {
            this.mScrollMode = mode;
            if (this.mScrollMode != 2) {
                this.mIndicatorShow = false;
            }
            reLayout();
        }
    }

    private void reLayout() {
        requestLayout();
    }

    private int getVisibleChildCount() {
        int count = 0;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            if (getChildAt(i).getVisibility() != 8) {
                count++;
            }
        }
        Log.d(TAG, "getVisibleChildCount : " + getChildCount() + "   " + count);
        return count;
    }

    private void caculatePageParams() {
        if (this.mLastChildCount != getChildCount()) {
            this.mCurRowsPerPage = Math.min((int) Math.ceil((((double) getVisibleChildCount()) * 1.0d) / ((double) this.mColumnsPerPage)), this.mMaxRowsPerPage);
            if (this.mCurRowsPerPage <= 0) {
                this.mCurPages = 0;
            } else {
                this.mCurPages = (int) Math.ceil((((double) getVisibleChildCount()) * 1.0d) / ((double) (this.mColumnsPerPage * this.mMaxRowsPerPage)));
            }
        }
        this.mLastChildCount = getChildCount();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        caculatePageParams();
        if (this.mCurRowsPerPage <= 0 || this.mCurPages <= 0) {
            setMeasuredDimension(MeasureSpec.makeMeasureSpec(0, 1073741824), MeasureSpec.makeMeasureSpec(0, 1073741824));
            return;
        }
        this.mContentWidth = 0;
        this.mContentHeight = 0;
        measureSlideItem(widthSize, widthMode, heightSize, heightMode);
        measureCustomView(widthSize, widthMode, Math.max(0, heightSize - this.mContentHeight), heightMode);
        measureIndicator(widthSize, widthMode, Math.max(0, heightSize - this.mContentHeight), heightMode);
        int width = widthSize;
        int height = heightSize;
        if (heightMode != 1073741824) {
            height = (this.mContentHeight + getPaddingTop()) + getPaddingBottom();
            if (heightMode == Integer.MIN_VALUE) {
                height = Math.min(heightSize, height);
            }
        }
        if (widthMode != 1073741824) {
            width = (this.mContentWidth + getPaddingLeft()) + getPaddingRight();
            if (widthMode == Integer.MIN_VALUE) {
                width = Math.min(widthSize, width);
            }
        }
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View v = getChildAt(i);
            if (v.getVisibility() != 8) {
                v.measure(MeasureSpec.makeMeasureSpec(this.mItemWidth, 1073741824), MeasureSpec.makeMeasureSpec(this.mItemHeight, 1073741824));
            }
        }
        Drawable bg = getBackground();
        if (bg != null) {
            width = Math.max(width, bg.getIntrinsicWidth());
            height = Math.max(height, bg.getIntrinsicHeight());
        }
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(width, 1073741824), MeasureSpec.makeMeasureSpec(height, 1073741824));
        Log.d(TAG, "onMeasure  mItemWidth : " + this.mItemWidth + "  mItemHeight : " + this.mItemHeight + "\n" + "mIndicatorHeight : " + this.mIndicatorHeight + " mIndicatorWidth : " + this.mIndicatorWidth + "\n" + "mContentHeight : " + this.mContentHeight + " mContentWidth : " + this.mContentWidth + "\n" + "customViewWidth : " + this.mCustomView.getMeasuredWidth() + " " + "customViewHeight : " + this.mCustomView.getMeasuredHeight() + "\n" + "width : " + getMeasuredWidth() + "  height : " + getMeasuredHeight() + "\n" + "mCurRowsPerPage : " + this.mCurRowsPerPage + "  mColumnsPerPage : " + this.mColumnsPerPage);
    }

    private int generateMeasureSpec(int layoutSize) {
        if (layoutSize < 0) {
            return MeasureSpec.makeMeasureSpec(0, 0);
        }
        return MeasureSpec.makeMeasureSpec(layoutSize, 1073741824);
    }

    private int[] getItemMaxHW(boolean widthUser, boolean heightUser, int widthMeasureSpec, int heightMeasureSpec) {
        int maxItemWidth = 0;
        int maxItemHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v.getVisibility() != 8) {
                LayoutParams lp = v.getLayoutParams();
                v.measure(widthUser ? widthMeasureSpec : generateMeasureSpec(lp.width), heightUser ? heightMeasureSpec : generateMeasureSpec(lp.height));
                maxItemWidth = Math.max(v.getMeasuredWidth(), maxItemWidth);
                maxItemHeight = Math.max(v.getMeasuredHeight(), maxItemHeight);
            }
        }
        Log.d(TAG, "getItemMaxHW maxItemWidth : " + maxItemWidth + "   maxItemHeight : " + maxItemHeight);
        return new int[]{maxItemWidth, maxItemHeight};
    }

    private void measureSlideItem(int pWidthSize, int pWidthMode, int pHeightSize, int pHeightMode) {
        int width;
        int height;
        int[] maxSize = getItemMaxHW(false, false, 0, 0);
        int horizontalOffset = this.mLeftScrollOffset + this.mRightScrollOffset;
        int horizontalPadding = getPaddingLeft() + getPaddingRight();
        int verticalPadding = getPaddingTop() + getPaddingBottom();
        int columnGap = Math.max(0, this.mColumnsPerPage - 1) * this.mColumnGap;
        int needWidth = (maxSize[0] * this.mColumnsPerPage) + columnGap;
        if (pWidthMode == 0) {
            width = needWidth;
        } else {
            width = Math.max(pWidthMode == Integer.MIN_VALUE ? Math.min(pWidthSize - horizontalPadding, needWidth) : pWidthSize - horizontalPadding, 0);
        }
        this.mItemWidth = ((width - columnGap) - horizontalOffset) / this.mColumnsPerPage;
        if (!this.mAdaptiveWidth) {
            this.mItemWidth = Math.min(this.mItemWidth, maxSize[0]);
        }
        maxSize = getItemMaxHW(true, false, MeasureSpec.makeMeasureSpec(this.mItemWidth, 1073741824), 0);
        int rowNum = Math.max(0, this.mCurRowsPerPage - 1);
        int needHeight = (maxSize[1] * this.mCurRowsPerPage) + (this.mRowGap * rowNum);
        if (pHeightMode == 0) {
            height = needHeight;
        } else {
            height = Math.max(Math.min(needHeight, pHeightSize - verticalPadding), 0);
        }
        this.mItemHeight = (height - (this.mRowGap * rowNum)) / this.mCurRowsPerPage;
        this.mContentWidth = Math.max(this.mContentWidth, width);
        this.mContentHeight = Math.max(this.mContentHeight, height);
    }

    private void measureCustomView(int pWidthSize, int pWidthMode, int pHeightSize, int pHeightMode) {
        int heightMeasureSpec;
        LayoutParams lp = this.mCustomView.getLayoutParams();
        int heightGap = ((this.mCustomViewGap.top + this.mCustomViewGap.bottom) + getPaddingTop()) + getPaddingBottom();
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(Math.max(this.mContentWidth - (((this.mCustomViewGap.left + this.mCustomViewGap.right) + getPaddingLeft()) + getPaddingRight()), 0), 1073741824);
        int height = Math.max(pHeightSize - heightGap, 0);
        if (pHeightMode == 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, pHeightMode);
        } else {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, Integer.MIN_VALUE);
        }
        this.mCustomView.measure(widthMeasureSpec, heightMeasureSpec);
        this.mContentHeight += (this.mCustomView.getMeasuredHeight() + this.mCustomViewGap.top) + this.mCustomViewGap.bottom;
        this.mContentWidth = Math.max(this.mContentWidth, (this.mCustomView.getMeasuredWidth() + this.mCustomViewGap.left) + this.mCustomViewGap.right);
    }

    private void measureIndicator(int pWidthSize, int pWidthMode, int pHeightSize, int pHeightMode) {
        if (this.mIndicatorShow) {
            int height = this.mDrawWithDrawable ? this.mIndicatorHeight > 0 ? this.mIndicatorHeight : Math.max(this.mIndicatorActive.getIntrinsicWidth(), this.mIndicatorNormal.getIntrinsicWidth()) : this.mIndicatorHeight > 0 ? this.mIndicatorHeight : getSlideCircleRadius() * 2;
            this.mIndicatorWidth = this.mContentWidth;
            this.mContentHeight += (this.mIndicatorTopGap + height) + this.mIndicatorBottomGap;
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int horizontalPadding = getPaddingLeft() + getPaddingRight();
        int verticalPadding = getPaddingTop() + getPaddingBottom();
        int drawLeft = getPaddingLeft();
        int drawTop = getPaddingTop();
        if (this.mGravity == 5 || this.mGravity == Gravity.END) {
            drawLeft = Math.max((right - getPaddingRight()) - this.mContentWidth, drawLeft);
        } else if (this.mGravity == 1 || this.mGravity == 17) {
            drawLeft = Math.max((((getWidth() - horizontalPadding) - this.mContentWidth) / 2) + drawLeft, drawLeft);
        }
        if (this.mGravity == 80) {
            drawTop = Math.max((bottom - getPaddingBottom()) - this.mContentWidth, drawTop);
        } else if (this.mGravity == 16 || this.mGravity == 17) {
            drawTop = Math.max((((getHeight() - verticalPadding) - this.mContentHeight) / 2) + drawTop, drawTop);
        }
        layoutChild(drawLeft, drawTop, right, bottom);
        int customViewDrawLeft = drawLeft + this.mCustomViewGap.left;
        int customViewDrawTop = ((this.mCustomViewGap.top + drawTop) + (this.mCurRowsPerPage * this.mItemHeight)) + (Math.max(0, this.mCurRowsPerPage - 1) * this.mRowGap);
        this.mCustomView.layout(customViewDrawLeft, customViewDrawTop, this.mCustomView.getMeasuredWidth() + customViewDrawLeft, this.mCustomView.getMeasuredHeight() + customViewDrawTop);
        this.mIndicatorX = drawLeft;
        this.mIndicatorY = (this.mCustomView.getMeasuredHeight() + customViewDrawTop) + this.mIndicatorTopGap;
        if (getCurrentPages() > 0 && getWidth() > 0) {
            int pageOffset = -1;
            if (this.mInitialOffset >= 0) {
                int maxPageIndex = getCurrentPages() - 1;
                pageOffset = Math.min(this.mInitialOffset, maxPageIndex);
                if (getLayoutDirection() == 1) {
                    pageOffset = Math.max(0, maxPageIndex - pageOffset);
                }
                this.mInitialOffset = -1;
            } else if (getCurrentPagePosition() >= getCurrentPages()) {
                pageOffset = Math.max(0, getCurrentPages() - 1);
            }
            if (pageOffset >= 0) {
                this.mHorizontalOffset = (-pageOffset) * getWidth();
                setScrollX(this.mHorizontalOffset * -1);
            }
        }
    }

    private boolean isRtl() {
        return getLayoutDirection() == 1;
    }

    private void layoutChild(int left, int top, int right, int bottom) {
        boolean isRtl = getLayoutDirection() == 1;
        int childCount = getChildCount();
        left += this.mLeftScrollOffset;
        childIndex = isRtl ? childCount - 1 : 0;
        while (childIndex >= 0 && childIndex < childCount) {
            int i;
            View v = getChildAt(childIndex);
            if (v.getVisibility() != 8) {
                int x = childIndex % this.mColumnsPerPage;
                int y = (childIndex / this.mColumnsPerPage) % this.mMaxRowsPerPage;
                int p = childIndex / (this.mColumnsPerPage * this.mMaxRowsPerPage);
                if (isRtl) {
                    x = (this.mColumnsPerPage - 1) - x;
                    p = (this.mCurPages - 1) - p;
                }
                int l = ((getWidth() * p) + left) + ((this.mItemWidth + this.mColumnGap) * x);
                int t = top + ((this.mItemHeight + this.mRowGap) * y);
                v.layout(l, t, l + this.mItemWidth, t + this.mItemHeight);
            }
            if (isRtl) {
                i = -1;
            } else {
                i = 1;
            }
            childIndex += i;
        }
    }

    private void initOrResetVelocityTracker() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            this.mVelocityTracker.clear();
        }
    }

    private void recycleVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private int getViewPosition(float x, float y) {
        x += (float) getScrollX();
        y += (float) getScrollY();
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View v = getChildAt(i);
            if (x > ((float) v.getLeft()) && x < ((float) v.getRight()) && y > ((float) v.getTop()) && y < ((float) v.getBottom())) {
                return i;
            }
        }
        return -1;
    }

    private void handleTouchDown(MotionEvent event) {
        this.mPreHorizontalOffset = this.mHorizontalOffset;
        this.mTouchMode = TouchMode.DOWN;
        initOrResetVelocityTracker();
        this.mVelocityTracker.addMovement(event);
        if (!this.mScroller.isFinished()) {
            this.mScroller.abortAnimation();
        }
    }

    private void startScroll(MotionEvent event) {
        float offsetX = event.getX() - this.mLastTouchX;
        float offsetY = event.getY() - this.mLastTouchY;
        this.mLastTouchX = event.getX();
        this.mLastTouchY = event.getY();
        this.mTouchMode = TouchMode.SCROLL;
        this.mVelocityTracker.addMovement(event);
        if (Math.abs(offsetY) < this.MAX_VERTICAL_OFFSET) {
            this.mHorizontalOffset += (int) getSlideOffsetFromTouchOffset(offsetX);
            scrollTo(this.mHorizontalOffset * -1, 0);
            invalidate();
        }
    }

    private boolean startScrollIfNeed(MotionEvent event) {
        if (Math.abs(event.getX() - this.mTouchDownX) <= this.mTouchSlop) {
            return false;
        }
        setPressed(false);
        View v = getChildAt(this.mTouchDownPosition);
        if (v != null) {
            v.setPressed(false);
        }
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        this.mLastTouchX = event.getX();
        this.mLastTouchY = event.getY();
        startScroll(event);
        return true;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.mScrollMode == 2) {
            int position = getViewPosition(event.getX(), event.getY());
            switch (event.getActionMasked()) {
                case 0:
                    if (position >= 0) {
                        this.mTouchDownPosition = position;
                        this.mTouchDownX = event.getX();
                        this.mTouchDownY = event.getY();
                        handleTouchDown(event);
                        break;
                    }
                    break;
                case 1:
                case 3:
                    this.mTouchMode = TouchMode.REST;
                    recycleVelocityTracker();
                    break;
                case 2:
                    if (this.mTouchMode == TouchMode.DOWN) {
                        this.mVelocityTracker.addMovement(event);
                        if (startScrollIfNeed(event)) {
                            return true;
                        }
                    }
                    break;
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    private void startFling(float velocityX) {
        int pageOffset;
        if (Math.abs(velocityX) >= ((float) this.MIN_SLIDE_VELOCITY)) {
            pageOffset = this.mHorizontalOffset / getWidth();
            if (velocityX <= 0.0f) {
                pageOffset--;
            }
        } else {
            pageOffset = (int) Math.round((((double) this.mHorizontalOffset) * 1.0d) / ((double) getWidth()));
        }
        pageOffset = Math.min(Math.max(pageOffset, 1 - this.mCurPages), 0);
        int scrollStartPos = this.mHorizontalOffset;
        this.mScroller.startScroll(scrollStartPos, 0, (pageOffset * getWidth()) - scrollStartPos, 0, 400);
        invalidate();
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean handle = true;
        if (this.mScrollMode == 2) {
            switch (event.getActionMasked()) {
                case 0:
                    this.mLastTouchX = event.getX();
                    this.mLastTouchY = event.getY();
                    handleTouchDown(event);
                    break;
                case 1:
                case 3:
                    this.mVelocityTracker.computeCurrentVelocity(100);
                    startFling(this.mVelocityTracker.getXVelocity());
                    this.mTouchMode = TouchMode.REST;
                    recycleVelocityTracker();
                    break;
                case 2:
                    startScroll(event);
                    break;
                default:
                    handle = false;
                    break;
            }
        }
        handle = false;
        if (handle) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void computeScroll() {
        if (this.mScroller.computeScrollOffset()) {
            this.mHorizontalOffset = this.mScroller.getCurrX();
            scrollTo(this.mScroller.getCurrX() * -1, 0);
            invalidate();
        }
        super.computeScroll();
    }

    private boolean isSlideOut() {
        int maxHorizontalOffset = Math.max(0, this.mCurPages - 1) * getWidth();
        if (this.mHorizontalOffset > 0 || this.mHorizontalOffset < (-maxHorizontalOffset)) {
            return true;
        }
        return false;
    }

    private float getSlideOffsetFromTouchOffset(float offsetX) {
        float adjustOffsetX = offsetX * this.mHorizontalOffsetAdjustPercent;
        if (isSlideOut()) {
            return offsetX - adjustOffsetX;
        }
        return offsetX + adjustOffsetX;
    }

    private int getSlideCircleRadius() {
        return this.mIndicatorCircleRadius;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mIndicatorShow && this.mCurPages > 1) {
            if (this.mDrawWithDrawable) {
                drawWithDrawable(canvas);
            } else if (this.mEnableCoalesce) {
                drawWithCoalesce(canvas);
            } else {
                drawWithDefault(canvas);
            }
        }
    }

    private int getIndicatorLenWithGap() {
        return (this.mIndicatorCircleRadius * 2) + this.mIndicatorCircleGap;
    }

    private int getMaxCoalesceWidth() {
        return this.mIndicatorCircleRadius + getSlideCircleRadius();
    }

    private int getCoalesceCircleCenterOffset(float coordOffset) {
        int indicatorGapNum = this.mCurPages - 1;
        int slideCircleRadius = getSlideCircleRadius();
        int indicatorCentorPageLen = this.mIndicatorCircleGap + (this.mIndicatorCircleRadius * 2);
        int indicatorIndex = (int) ((coordOffset - ((float) slideCircleRadius)) / ((float) getIndicatorLenWithGap()));
        int centerL = slideCircleRadius + (indicatorIndex * indicatorCentorPageLen);
        int centerR = slideCircleRadius + (Math.min(indicatorIndex + 1, indicatorGapNum) * indicatorCentorPageLen);
        if (coordOffset - ((float) centerL) <= ((float) getMaxCoalesceWidth())) {
            return centerL;
        }
        if (((float) centerR) - coordOffset <= ((float) getMaxCoalesceWidth())) {
            return centerR;
        }
        return -1;
    }

    private void drawWithDefault(Canvas canvas) {
        int circleNum = this.mCurPages;
        int gapNum = Math.max(0, circleNum - 1);
        int slideCircleRadius = getSlideCircleRadius();
        int left = (this.mIndicatorX + (Math.max(0, this.mIndicatorWidth - ((slideCircleRadius + (((this.mIndicatorCircleRadius * 2) * gapNum) + (this.mIndicatorCircleGap * gapNum))) + slideCircleRadius)) / 2)) + (-this.mHorizontalOffset);
        int top = this.mIndicatorY;
        int indicatorWidth = getIndicatorLenWithGap();
        for (int i = 0; i < circleNum; i++) {
            canvas.drawCircle((float) ((((slideCircleRadius - this.mIndicatorCircleRadius) + left) + (i * indicatorWidth)) + this.mIndicatorCircleRadius), (float) (top + slideCircleRadius), (float) this.mIndicatorCircleRadius, this.mIndicatorPaint);
        }
        float x = (float) ((((slideCircleRadius - this.mIndicatorCircleRadius) + left) + ((-Math.min(Math.max((int) Math.round((((double) this.mHorizontalOffset) * 1.0d) / ((double) getWidth())), 1 - this.mCurPages), 0)) * indicatorWidth)) + this.mIndicatorCircleRadius);
        canvas.drawCircle(x, (float) (top + slideCircleRadius), (float) this.mIndicatorCircleRadius, this.mIndicatorActivePaint);
    }

    private void drawWithCoalesce(Canvas canvas) {
        int circleNum = this.mCurPages;
        int gapNum = Math.max(0, circleNum - 1);
        int slideCircleRadius = getSlideCircleRadius();
        int indicatorCenterWidth = ((this.mIndicatorCircleRadius * 2) * gapNum) + (this.mIndicatorCircleGap * gapNum);
        int indicatorWidthR = (slideCircleRadius + indicatorCenterWidth) + slideCircleRadius;
        int left = (this.mIndicatorX + (Math.max(0, this.mIndicatorWidth - indicatorWidthR) / 2)) + (-this.mHorizontalOffset);
        int top = this.mIndicatorY;
        int indicatorWidth = getIndicatorLenWithGap();
        for (int i = 0; i < circleNum; i++) {
            canvas.drawCircle((float) ((((slideCircleRadius - this.mIndicatorCircleRadius) + left) + (i * indicatorWidth)) + this.mIndicatorCircleRadius), (float) (top + slideCircleRadius), (float) this.mIndicatorCircleRadius, this.mIndicatorPaint);
        }
        if (isSlideOut()) {
            canvas.drawCircle(((float) left) + ((float) (this.mHorizontalOffset > 0 ? slideCircleRadius : indicatorWidthR - slideCircleRadius)), (float) (top + slideCircleRadius), (float) slideCircleRadius, this.mIndicatorActivePaint);
            return;
        }
        float slideCenterOffset = (((float) indicatorCenterWidth) * Math.abs((((float) this.mHorizontalOffset) * 1.0f) / ((float) (getWidth() * Math.max(0, this.mCurPages - 1))))) + ((float) slideCircleRadius);
        int coalesceCenterOffset = getCoalesceCircleCenterOffset(slideCenterOffset);
        if (coalesceCenterOffset >= 0) {
            float distancePercent = Math.abs(slideCenterOffset - ((float) coalesceCenterOffset)) / ((float) getMaxCoalesceWidth());
            int radiusC1 = (int) Math.max(((float) slideCircleRadius) * (1.0f - distancePercent), (float) this.mIndicatorCircleRadius);
            int radiusC0 = (int) Math.min(((float) slideCircleRadius) * distancePercent, (float) this.mIndicatorCircleRadius);
            new Paint().setColor(Menu.CATEGORY_MASK);
            float y = (float) (top + slideCircleRadius);
            canvas.drawCircle(((float) left) + slideCenterOffset, y, (float) radiusC0, this.mIndicatorPaint);
            canvas.drawCircle((float) (left + coalesceCenterOffset), y, (float) radiusC1, this.mIndicatorPaint);
            float x1 = ((float) left) + slideCenterOffset;
            float y1 = y - ((float) radiusC0);
            float x2 = (float) (left + coalesceCenterOffset);
            float y2 = y - ((float) radiusC1);
            float x3 = (float) (left + coalesceCenterOffset);
            float y3 = y + ((float) radiusC1);
            float x4 = ((float) left) + slideCenterOffset;
            float y4 = y + ((float) radiusC0);
            Path P = new Path();
            P.moveTo(x1, y1);
            P.quadTo((x1 + x2) / 2.0f, y, x2, y2);
            P.lineTo(x3, y3);
            P.quadTo((x3 + x4) / 2.0f, y, x4, y4);
            P.lineTo(x1, y1);
            canvas.drawPath(P, this.mIndicatorPaint);
        } else {
            canvas.drawCircle(((float) left) + slideCenterOffset, (float) (top + slideCircleRadius), (float) this.mIndicatorCircleRadius, this.mIndicatorPaint);
        }
    }

    private void setDrawableBounds(Drawable d) {
        if (d != null) {
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        }
    }

    private void drawWithDrawable(Canvas canvas) {
        float left = (float) ((this.mIndicatorX + (Math.max(0, this.mIndicatorWidth - ((this.mIndicatorNormal.getIntrinsicWidth() * Math.max(0, this.mCurPages - 1)) + this.mIndicatorActive.getIntrinsicWidth())) / 2)) + (-this.mHorizontalOffset));
        float top = (float) this.mIndicatorY;
        int currentPage = Math.abs(this.mHorizontalOffset / getWidth());
        int level = (int) (((float) mIndicatorMaxLevel) * ((((float) (Math.abs(Math.min(0, Math.max(this.mHorizontalOffset, -(getWidth() * this.mCurPages)))) % getWidth())) * 1.0f) / ((float) getWidth())));
        int offsetX = 0;
        setDrawableBounds(this.mIndicatorActive);
        setDrawableBounds(this.mIndicatorNormal);
        setDrawableBounds(this.mIndicatorDrawable);
        int i = 0;
        while (i < this.mCurPages) {
            Drawable d;
            canvas.save();
            canvas.translate(((float) offsetX) + left, top);
            if (currentPage != i || i + 1 >= this.mCurPages) {
                d = currentPage == i ? this.mIndicatorActive : this.mIndicatorNormal;
            } else {
                this.mIndicatorDrawable.setLevel(level);
                d = this.mIndicatorDrawable;
                i++;
            }
            d.draw(canvas);
            offsetX += d.getIntrinsicWidth();
            canvas.restore();
            i++;
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (oldw <= 0) {
            Log.e(TAG, "onSizeChanged : [" + w + " " + h + "] old [" + oldw + " " + oldh + "]");
            return;
        }
        this.mHorizontalOffset = (int) (((float) this.mHorizontalOffset) * ((((float) w) * 1.0f) / ((float) oldw)));
        scrollTo(this.mHorizontalOffset * -1, 0);
    }
}
