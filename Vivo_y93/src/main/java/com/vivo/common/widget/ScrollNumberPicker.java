package com.vivo.common.widget;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.os.FtBuild;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Scroller;
import com.vivo.common.MarqueeTextView;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.common.provider.Weather;
import com.vivo.internal.R;
import java.util.ArrayList;
import java.util.List;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class ScrollNumberPicker extends FrameLayout {
    private static final float targetRomVersion = 3.6f;
    private int DEFAULT_VISIBLE_ITEM_COUNT;
    private final boolean LOG_DEBUG;
    private final String TAG;
    private float TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    private int centerScrollTextSize;
    private boolean isFling;
    private Context mContext;
    private int mCurrentScrollOffset;
    private float mDensity;
    private int mDisplayX;
    private int mDisplayY;
    private boolean mDrawCalled;
    private int mGravity;
    private int mHalfScrollItemCount;
    private int mItemCount;
    private int mItemGravity;
    private int mItemHeight;
    private boolean mItemHeightDefined;
    private float mLastDownY;
    private float mLastY;
    @Deprecated
    private int mLeft;
    private OnChangedListener mListener;
    private int mMaximumFlingVelocity;
    private int mMinimumFlingVelocity;
    private String mNumberText;
    private Paint mPickerPaint;
    private String mPickerTextStr;
    private int mScrollItemGap;
    private boolean mScrollItemNeedMeasure;
    private Paint mScrollItemPaint;
    private int mScrollItemWidth;
    private boolean mScrollItemWidthDefined;
    private int mScrollOffsetOnFling;
    private int mScrollState;
    private Scroller mScroller;
    private Paint mSelectItemPaint;
    private String mSelectItemText;
    private List<String> mSelectList;
    private int mSelectPosition;
    private boolean mSelectPositionByUser;
    private int mSelfHeight;
    private int mSelfWidth;
    private Shader mShader;
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;
    private boolean mWrapWheel;
    private MarqueeTextView marqueeTextView;
    private LayoutParams params;
    private int pickerLeftMagin;
    private int pickerTextColor;
    private float pickerTextSize;
    private int pickerTopMagin;
    private float romVersion;

    public interface OnChangedListener {
        void onChanged(String str, String str2);
    }

    public interface OnScrollListener {
        public static final int SCROLL_STATE_FLING = 2;
        public static final int SCROLL_STATE_IDLE = 0;
        public static final int SCROLL_STATE_TOUCH_ON_SCROLL = 3;
        public static final int SCROLL_STATE_TOUCH_SCROLL = 1;

        void onScrollStateChange(ScrollNumberPicker scrollNumberPicker, int i);
    }

    public ScrollNumberPicker(Context context) {
        this(context, null);
    }

    public ScrollNumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 50397232);
    }

    public ScrollNumberPicker(Context context, AttributeSet attrs, int defAttr) {
        boolean z;
        super(context, attrs, defAttr);
        this.TAG = "ScrollNumberPicker";
        this.LOG_DEBUG = false;
        this.TOP_AND_BOTTOM_FADING_EDGE_STRENGTH = 0.9f;
        this.DEFAULT_VISIBLE_ITEM_COUNT = 5;
        this.mSelfWidth = 0;
        this.mSelfHeight = 0;
        this.mScrollItemNeedMeasure = true;
        this.mScrollItemWidthDefined = false;
        this.mScrollItemWidth = 0;
        this.mSelectList = new ArrayList();
        this.mNumberText = Events.DEFAULT_SORT_ORDER;
        this.mItemHeightDefined = false;
        this.mItemHeight = 0;
        this.mScrollItemGap = 0;
        this.mItemCount = this.DEFAULT_VISIBLE_ITEM_COUNT;
        this.mHalfScrollItemCount = this.mItemCount / 2;
        this.mSelectPosition = 0;
        this.mCurrentScrollOffset = 0;
        this.mSelectPositionByUser = false;
        this.mScrollState = 0;
        this.mWrapWheel = true;
        this.isFling = false;
        this.mDisplayX = 0;
        this.mDisplayY = 0;
        this.mItemGravity = 17;
        this.mDrawCalled = true;
        this.mScrollOffsetOnFling = 0;
        this.pickerLeftMagin = -1;
        this.pickerTopMagin = -1;
        this.mContext = context;
        this.mDensity = context.getResources().getDisplayMetrics().density;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollNumberPicker, defAttr, 0);
        this.mItemHeight = a.getDimensionPixelSize(8, 0);
        this.mScrollItemGap = a.getDimensionPixelSize(9, 0);
        this.mGravity = a.getInt(0, 0);
        if (this.mItemHeight <= 0) {
            z = false;
        } else {
            z = true;
        }
        this.mItemHeightDefined = z;
        this.mPickerPaint = new Paint(1);
        this.mPickerPaint.setTextSize((float) a.getDimensionPixelSize(5, -1));
        this.mPickerPaint.setColor(a.getColor(1, -16777216));
        this.mScrollItemPaint = new Paint(1);
        if (FtBuild.getRomVersion() < 4.0f) {
            this.mScrollItemPaint.setTextSize((float) a.getDimensionPixelSize(6, -1));
        }
        this.mScrollItemPaint.setColor(a.getColor(2, -16777216));
        this.mSelectItemPaint = new Paint(1);
        this.mSelectItemPaint.setColor(a.getColor(3, -16777216));
        if (FtBuild.getRomVersion() >= 4.0f) {
            if (this instanceof LunarScrollNumberPicker) {
                this.centerScrollTextSize = getResources().getDimensionPixelSize(51118208);
            } else {
                this.centerScrollTextSize = a.getDimensionPixelSize(7, -1);
            }
            this.mSelectItemPaint.setTextSize((float) this.centerScrollTextSize);
        } else {
            this.mSelectItemPaint.setTextSize((float) a.getDimensionPixelSize(7, -1));
        }
        this.pickerTextSize = ((float) a.getDimensionPixelSize(5, -1)) / this.mDensity;
        this.pickerTextColor = a.getColor(1, -16777216);
        this.marqueeTextView = new MarqueeTextView(this.mContext);
        this.marqueeTextView.setGravity(19);
        this.marqueeTextView.setTextSize(this.pickerTextSize);
        this.marqueeTextView.setTextColor(this.pickerTextColor);
        this.params = new LayoutParams(-1, -1);
        this.marqueeTextView.setVisibility(8);
        addView(this.marqueeTextView, this.params);
        setWillNotDraw(false);
        a.recycle();
        this.mScroller = new Scroller(context, new DecelerateInterpolator(3.0f));
        ViewConfiguration configuration = ViewConfiguration.get(context);
        this.mTouchSlop = configuration.getScaledTouchSlop();
        this.mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        this.mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        this.romVersion = FtBuild.getRomVersion();
    }

    public void setScrollPickerParams(int itemHeight, float scrollItemSize, float pickerItemSize, int scrollItemColor, int pickerItemColor) {
        boolean z = false;
        if (this.mScrollState == 0) {
            this.mItemHeight = Math.max(0, itemHeight);
            if (itemHeight > 0) {
                z = true;
            }
            this.mItemHeightDefined = z;
            this.mScrollItemPaint.setColor(scrollItemColor);
            this.mScrollItemPaint.setTextSize(this.mDensity * scrollItemSize);
            this.mScrollItemNeedMeasure = this.mScrollItemWidthDefined ^ 1;
            this.mPickerPaint.setTextSize(this.mDensity * pickerItemSize);
            this.mPickerPaint.setColor(pickerItemColor);
            this.marqueeTextView.setTextSize(this.mDensity * pickerItemSize);
            this.marqueeTextView.setTextColor(pickerItemColor);
            refreshDrawWithSelectPosition();
        }
    }

    public int getSelectPosition() {
        return this.mSelectPosition;
    }

    public String getSelectItemText() {
        return this.mSelectItemText;
    }

    public void setItemHeight(int itemHeight) {
        boolean z = false;
        if (this.mScrollState == 0) {
            this.mItemHeight = Math.max(0, itemHeight);
            if (itemHeight > 0) {
                z = true;
            }
            this.mItemHeightDefined = z;
            refreshDrawWithSelectPosition();
        }
    }

    public void setItemWidth(int itemWidth) {
        if (itemWidth > 0) {
            this.mScrollItemWidth = itemWidth;
            this.mScrollItemNeedMeasure = false;
            this.mScrollItemWidthDefined = true;
        } else {
            this.mScrollItemWidthDefined = false;
            this.mScrollItemNeedMeasure = true;
        }
        refreshDraw();
    }

    public void setScrollItemTextSize(float scrollItemSize) {
        if (this.mScrollState == 0 || (this.mScrollState == 2 && this.mItemHeightDefined)) {
            this.mScrollItemPaint.setTextSize(this.mDensity * scrollItemSize);
            this.mScrollItemNeedMeasure = this.mScrollItemWidthDefined ^ 1;
            refreshDrawWithSelectPosition();
        }
    }

    public void setScrollItemTextColor(int scrollItemColor) {
        this.mScrollItemPaint.setColor(scrollItemColor);
        invalidate();
    }

    public void setSelectedItemTextSize(float selectedItemSize) {
        if (this.mScrollState == 0 || (this.mScrollState == 2 && this.mItemHeightDefined)) {
            this.mSelectItemPaint.setTextSize(this.mDensity * selectedItemSize);
            this.mScrollItemNeedMeasure = this.mScrollItemWidthDefined ^ 1;
            refreshDrawWithSelectPosition();
        }
    }

    public void setSelectedItemTextColor(int selectedItemColor) {
        this.mSelectItemPaint.setColor(selectedItemColor);
        invalidate();
    }

    public void setPickerTextSize(float pickerItemSize) {
        if (this.mScrollState == 0 || (this.mScrollState == 2 && this.mItemHeightDefined)) {
            this.mPickerPaint.setTextSize(this.mDensity * pickerItemSize);
            this.marqueeTextView.setTextSize(this.mDensity * pickerItemSize);
            refreshDrawWithSelectPosition();
        }
    }

    public void setPickerTextColor(int pickerItemColor) {
        this.mPickerPaint.setColor(pickerItemColor);
        this.marqueeTextView.setTextColor(pickerItemColor);
        invalidate();
    }

    public void setPickText(String text) {
        if (TextUtils.isEmpty(this.mPickerTextStr) || (this.mPickerTextStr.equals(text) ^ 1) != 0) {
            this.mPickerTextStr = text;
            if (((double) FtBuild.getRomVersion()) >= 4.0d) {
                this.mItemGravity = 17;
            } else if (TextUtils.isEmpty(text)) {
                this.mItemGravity = 17;
            } else {
                this.mItemGravity = 5;
            }
            refreshDraw();
        }
    }

    @Deprecated
    public void setTextPadding(int pickerLeftPadding, int pickerTopPadding, int selectedItemLeftPadding) {
    }

    @Deprecated
    public void setLeftPadding(int left) {
        setPadding(left, getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    @Deprecated
    public void setInitialOffset(int initialOffset) {
    }

    public void setPickerTextLeftPadding(int padding) {
        this.mScrollItemGap = padding;
        refreshDraw();
    }

    @Deprecated
    public void setListItemTextRightPadding(int padding) {
    }

    public void setRange(int start, int end, int maxLines) {
        int size = Math.abs(end - start) + 1;
        int step = start < end ? 1 : -1;
        String[] list = new String[size];
        for (int i = 0; i < size; i++) {
            list[i] = String.valueOf((i * step) + start);
        }
        setRange(list, maxLines);
    }

    /* JADX WARNING: Missing block: B:4:0x000b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setRange(String[] list, int maxLines) {
        if (this.mScrollState != 3 && this.mScrollState != 1 && list != null && list.length >= 1) {
            this.mSelectList.clear();
            for (Object add : list) {
                this.mSelectList.add(add);
            }
            if (maxLines % 2 == 0) {
                maxLines++;
            }
            this.mItemCount = maxLines;
            this.mHalfScrollItemCount = this.mItemCount / 2;
            this.mScrollItemNeedMeasure = this.mScrollItemWidthDefined ^ 1;
            if (this.mSelectList.size() < this.mItemCount) {
                this.mWrapWheel = false;
            }
            refreshDraw();
        }
    }

    @Deprecated
    public void setDisableRang(int start, int end) {
    }

    public void setWrapWheel(boolean wrapWheel) {
        this.mWrapWheel = wrapWheel;
        invalidate();
    }

    public void setNumberText(String text) {
        if (TextUtils.isEmpty(this.mNumberText) || (this.mNumberText.equals(text) ^ 1) != 0) {
            this.mNumberText = text;
            this.mScrollItemNeedMeasure = this.mScrollItemWidthDefined ^ 1;
            refreshDraw();
        }
    }

    public void setScrollItemBackground(int res) {
    }

    public void setScrollItemPositionByRange(int rangeNumber) {
        int i = 0;
        while (i < this.mSelectList.size() && rangeNumber != Integer.valueOf((String) this.mSelectList.get(i)).intValue()) {
            i++;
        }
        setScrollItemPositionByIndex(i);
    }

    public void setScrollItemPositionByRange(String name) {
        int i = 0;
        while (i < this.mSelectList.size() && !((String) this.mSelectList.get(i)).equals(name)) {
            i++;
        }
        setScrollItemPositionByIndex(i);
    }

    public void setScrollItemPositionByIndex(int index) {
        if (index >= 0 && index < this.mSelectList.size()) {
            switch (this.mScrollState) {
                case 0:
                    this.mSelectPosition = index;
                    this.mSelectItemText = (String) this.mSelectList.get(index);
                    this.mSelectPositionByUser = true;
                    invalidate();
                    break;
                case 2:
                    int scrollFinalPos = this.mScroller.getFinalY();
                    int[] pos = computeSelectPosition(scrollFinalPos);
                    if (this.mWrapWheel) {
                        int size;
                        if (this.mCurrentScrollOffset > scrollFinalPos) {
                            size = ((pos[0] - index) + this.mSelectList.size()) % this.mSelectList.size();
                        } else {
                            size = -(((index - pos[0]) + this.mSelectList.size()) % this.mSelectList.size());
                        }
                        this.mScrollOffsetOnFling = size;
                    } else {
                        this.mScrollOffsetOnFling = this.mCurrentScrollOffset > scrollFinalPos ? pos[0] - index : index - pos[0];
                    }
                    this.mScrollOffsetOnFling *= this.mItemHeight;
                    break;
                default:
                    log("setScrollItemPosition at Index[" + index + "] failed, invalied state : " + this.mScrollState);
                    break;
            }
        }
    }

    public void stopFling() {
        if (this.mScrollState == 2) {
            this.mScrollState = 0;
            this.mScroller.abortAnimation();
            this.mScrollOffsetOnFling = 0;
            this.isFling = false;
        }
    }

    public void setOnSelectChangedListener(OnChangedListener listener) {
        this.mListener = listener;
    }

    private int currentSelectItemPosition() {
        return computeSelectPosition(this.mScroller.getFinalY())[0];
    }

    private void refreshDrawWithSelectPosition() {
        if (this.mDrawCalled && getVisibility() != 8) {
            requestLayout();
            setScrollItemPositionByIndex(this.mScroller.isFinished() ? this.mSelectPosition : currentSelectItemPosition());
        }
    }

    private void refreshDraw() {
        if (this.mDrawCalled && getVisibility() != 8) {
            requestLayout();
            invalidate();
        }
    }

    private void fling(int velocityY) {
        this.isFling = true;
        velocityY -= (this.mCurrentScrollOffset + velocityY) % this.mItemHeight;
        int desPos = computeSelectPosition(this.mCurrentScrollOffset + velocityY)[0];
        log("fling   destination Postion is :" + desPos + "     wrapWheel : " + this.mWrapWheel);
        if (!this.mWrapWheel) {
            if (desPos <= 0) {
                desPos = 0;
                velocityY = -this.mCurrentScrollOffset;
            } else if (desPos >= this.mSelectList.size() - 1) {
                desPos = this.mSelectList.size() - 1;
                velocityY = ((-(this.mSelectList.size() - 1)) * this.mItemHeight) - this.mCurrentScrollOffset;
            }
        }
        this.mScroller.startScroll(0, this.mCurrentScrollOffset, 0, velocityY, Math.max(Weather.WEATHERVERSION_ROM_2_0, (Math.abs(velocityY) * 100) / this.mItemHeight));
        invalidate();
        String curStr = (String) this.mSelectList.get(desPos);
        if (!curStr.equals(this.mSelectItemText)) {
            onSelectChanged(desPos, curStr, this.mSelectItemText);
        }
        this.mSelectItemText = curStr;
    }

    private int getScrollOffsetAdjustValue() {
        return this.mCurrentScrollOffset >= 0 ? this.mItemHeight / 2 : (-this.mItemHeight) / 2;
    }

    private void ensureScrollWheelAdjusted() {
        fling(getScrollOffsetAdjustValue());
    }

    private void onScrollStateChange(int scrollState) {
        if (this.mScrollState != scrollState) {
            this.mScrollOffsetOnFling = 0;
            this.mScrollState = scrollState;
            if (scrollState == 0 || scrollState == 3) {
                this.isFling = false;
                int selectPos = computeSelectPosition(this.mCurrentScrollOffset + getScrollOffsetAdjustValue())[0];
                if (selectPos >= 0 && selectPos < this.mSelectList.size()) {
                    String curStr = (String) this.mSelectList.get(selectPos);
                    if (!curStr.equals(this.mSelectItemText)) {
                        onSelectChanged(selectPos, curStr, this.mSelectItemText);
                    }
                    this.mSelectItemText = curStr;
                }
            }
        }
    }

    public void computeScroll() {
        if (this.isFling) {
            if (this.mScroller.computeScrollOffset()) {
                this.mCurrentScrollOffset = this.mScroller.getCurrY() + this.mScrollOffsetOnFling;
                invalidate();
            } else {
                onScrollStateChange(0);
            }
        }
    }

    private boolean hasContent() {
        return this.mItemHeight > 0 && this.mScrollItemWidth > 0 && this.mSelectList.size() > 0;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || (hasContent() ^ 1) != 0) {
            return false;
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(event);
        switch (event.getActionMasked()) {
            case 0:
                float y = event.getY();
                this.mLastDownY = y;
                this.mLastY = y;
                if (!this.mScroller.isFinished()) {
                    this.mScroller.abortAnimation();
                    onScrollStateChange(3);
                    break;
                }
                break;
            case 1:
                if (this.romVersion >= targetRomVersion) {
                    float actionUpY = event.getY();
                    if (((int) Math.abs(actionUpY - this.mLastDownY)) < this.mTouchSlop) {
                        singleClickChoose((int) actionUpY);
                        break;
                    }
                }
                VelocityTracker velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(Weather.WEATHERVERSION_ROM_2_0, (float) this.mMaximumFlingVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity();
                if (Math.abs(initialVelocity) > this.mMinimumFlingVelocity) {
                    fling(initialVelocity / 5);
                } else {
                    ensureScrollWheelAdjusted();
                }
                onScrollStateChange(2);
                this.mVelocityTracker.recycle();
                this.mVelocityTracker = null;
                break;
            case 2:
                float currentY = event.getY();
                if (this.mScrollState == 1) {
                    this.mCurrentScrollOffset += (int) (currentY - this.mLastY);
                    invalidate();
                } else if (((int) Math.abs(currentY - this.mLastDownY)) > this.mTouchSlop) {
                    onScrollStateChange(1);
                }
                this.mLastY = currentY;
                break;
        }
        return true;
    }

    protected float getBottomFadingEdgeStrength() {
        return this.TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    protected float getTopFadingEdgeStrength() {
        return this.TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mDrawCalled = true;
        if (hasContent()) {
            if (this.mSelectPositionByUser) {
                this.mSelectPositionByUser = false;
                this.mCurrentScrollOffset = -(this.mSelectPosition * this.mItemHeight);
            }
            initDisplayParam();
            int scrollNumberWidth = this.mScrollItemWidth;
            int displayPosX = this.mDisplayX;
            int displayPosY = this.mDisplayY;
            int[] scrollInfo = computeSelectPosition(this.mCurrentScrollOffset);
            int xPos = displayPosX;
            int yPos = scrollInfo[1] + displayPosY;
            this.mSelectPosition = scrollInfo[0];
            log("onDraw displayPosX  [" + displayPosX + "] displayPosY [" + displayPosY + "]");
            log("onDraw xPos [" + displayPosX + "] yPos [" + yPos + "]");
            log("onDraw mSelectPosition [" + this.mSelectPosition + "]");
            if (this.mShader == null) {
                int color = this.mScrollItemPaint.getColor();
                if (FtBuild.getRomVersion() < 4.0f) {
                    this.mShader = new LinearGradient(0.0f, 0.0f, 0.0f, (float) getHeight(), new int[]{0, 1358954495 & color, -1056964609 & color, 1358954495 & color, 0}, new float[]{0.0f, 0.05f, 0.5f, 0.95f, 1.0f}, TileMode.CLAMP);
                } else {
                    this.mShader = new LinearGradient(0.0f, 0.0f, 0.0f, (float) getHeight(), new int[]{0, 1291845631 & color, color & -1, 1291845631 & color, 0}, new float[]{0.0f, 0.05f, 0.5f, 0.95f, 1.0f}, TileMode.CLAMP);
                }
                this.mScrollItemPaint.setShader(this.mShader);
            }
            Rect rect;
            if (FtBuild.getRomVersion() < 4.0f) {
                rect = new Rect(displayPosX, displayPosY, displayPosX + scrollNumberWidth, displayPosY + (this.mItemHeight * this.mHalfScrollItemCount));
                drawContent(canvas, displayPosX, yPos, rect, this.mScrollItemPaint);
                rect.top = (this.mItemHeight * this.mHalfScrollItemCount) + displayPosY;
                rect.bottom = rect.top + this.mItemHeight;
                drawContent(canvas, displayPosX, yPos, rect, this.mSelectItemPaint);
                rect.top = ((this.mItemHeight * this.mHalfScrollItemCount) + this.mItemHeight) + displayPosY;
                rect.bottom = rect.top + (this.mItemHeight * this.mHalfScrollItemCount);
                drawContent(canvas, displayPosX, yPos, rect, this.mScrollItemPaint);
                drawPickerText(canvas, displayPosX, displayPosY);
            } else if (FtBuild.getRomVersion() < 4.5f || this.marqueeTextView.getVisibility() == 0 || TextUtils.isEmpty(this.mPickerTextStr)) {
                int temp = this.mHalfScrollItemCount;
                rect = new Rect(displayPosX, displayPosY, displayPosX + scrollNumberWidth, displayPosY);
                int i = 0;
                while (i < this.mHalfScrollItemCount) {
                    rect.top = (this.mItemHeight * i) + displayPosY;
                    rect.bottom = rect.top + this.mItemHeight;
                    int temp2 = temp - 1;
                    this.mScrollItemPaint.setTextSize((float) new Double(((double) this.centerScrollTextSize) * Math.pow(0.85d, (double) temp)).intValue());
                    drawContent(canvas, displayPosX, yPos, rect, this.mScrollItemPaint);
                    i++;
                    temp = temp2;
                }
                rect.top = (this.mItemHeight * this.mHalfScrollItemCount) + displayPosY;
                rect.bottom = rect.top + this.mItemHeight;
                drawContent(canvas, displayPosX, yPos, rect, this.mSelectItemPaint);
                for (i = 0; i < this.mHalfScrollItemCount; i++) {
                    rect.top = ((this.mItemHeight * this.mHalfScrollItemCount) + displayPosY) + (this.mItemHeight * (i + 1));
                    rect.bottom = rect.top + this.mItemHeight;
                    this.mScrollItemPaint.setTextSize((float) new Double(((double) this.centerScrollTextSize) * Math.pow(0.85d, (double) (i + 1))).intValue());
                    drawContent(canvas, displayPosX, yPos, rect, this.mScrollItemPaint);
                }
                if (FtBuild.getRomVersion() < 4.5f) {
                    drawPickerText(canvas, displayPosX, displayPosY);
                }
            } else {
                drawMarqueePickerText(canvas, displayPosX, displayPosY);
            }
        }
    }

    private void drawPickerText(Canvas canvas, int displayPosX, int displayPosY) {
        if (!TextUtils.isEmpty(this.mPickerTextStr)) {
            int offsetY = measureTextHeightOffset(this.mPickerPaint);
            int xPos = (this.mScrollItemWidth + displayPosX) + this.mScrollItemGap;
            int yPos = displayPosY + (this.mItemHeight * this.mHalfScrollItemCount);
            CharSequence tmpStr = TextUtils.ellipsize(this.mPickerTextStr, new TextPaint(this.mPickerPaint), (float) ((this.mSelfWidth - getPaddingRight()) - xPos), TruncateAt.END);
            if (tmpStr != null) {
                canvas.drawText(tmpStr.toString(), (float) xPos, (float) (yPos + offsetY), this.mPickerPaint);
            }
        }
    }

    private void drawMarqueePickerText(Canvas canvas, int displayPosX, int displayPosY) {
        if (!TextUtils.isEmpty(this.mPickerTextStr)) {
            int offsetY = measureTextHeightOffset(this.mPickerPaint);
            float avalWidth = (float) ((this.mSelfWidth - getPaddingRight()) - ((this.mScrollItemWidth + displayPosX) + this.mScrollItemGap));
            CharSequence tmpStr = TextUtils.ellipsize(this.mPickerTextStr, new TextPaint(this.mPickerPaint), avalWidth, TruncateAt.END);
            if (this.pickerTopMagin == -1 || this.pickerLeftMagin == -1) {
                this.pickerTopMagin = (this.mItemHeight * this.mHalfScrollItemCount) + displayPosY;
                this.pickerLeftMagin = ((this.mScrollItemWidth + displayPosX) + this.mScrollItemGap) - getPaddingLeft();
                this.params.width = (int) avalWidth;
                this.params.height = this.mItemHeight;
                this.params.setMargins(this.pickerLeftMagin, this.pickerTopMagin, 0, 0);
                this.marqueeTextView.setText(tmpStr);
                this.marqueeTextView.setLayoutParams(this.params);
            }
            this.marqueeTextView.setVisibility(0);
        }
    }

    private void initDisplayParam() {
        int paddingHorizontal = getPaddingLeft() + getPaddingRight();
        int paddingVertical = getPaddingTop() + getPaddingBottom();
        int scrollNumberWidth = this.mScrollItemWidth;
        int contentHeight = this.mItemHeight * this.mItemCount;
        int contentWidth = scrollNumberWidth;
        if (!TextUtils.isEmpty(this.mPickerTextStr)) {
            contentWidth = scrollNumberWidth + (((int) this.mPickerPaint.measureText(this.mPickerTextStr)) + this.mScrollItemGap);
        }
        log("onDraw contentWidth [" + contentWidth + "] contentHeight [" + contentHeight + "]");
        log("onDraw mGravity = " + Integer.toHexString(this.mGravity));
        int verticalGravity = this.mGravity & 112;
        int horizontalGravity = this.mGravity & 8388615;
        if (verticalGravity == 16) {
            this.mDisplayY = (((this.mSelfHeight - paddingVertical) - contentHeight) / 2) + getPaddingTop();
        } else if (verticalGravity == 80) {
            this.mDisplayY = (this.mSelfHeight - getPaddingBottom()) - contentHeight;
        } else {
            this.mDisplayY = getPaddingTop();
        }
        if (getLayoutDirection() == 1) {
            if (horizontalGravity == 8388611) {
                horizontalGravity = 5;
            } else if (horizontalGravity == 8388613) {
                horizontalGravity = 3;
            }
        } else if (horizontalGravity == 8388611) {
            horizontalGravity = 3;
        } else if (horizontalGravity == 8388613) {
            horizontalGravity = 5;
        }
        if (horizontalGravity == 1) {
            this.mDisplayX = (((this.mSelfWidth - paddingHorizontal) - contentWidth) / 2) + getPaddingLeft();
        } else if (horizontalGravity == 5) {
            this.mDisplayX = (this.mSelfWidth - getPaddingEnd()) - contentWidth;
        } else {
            this.mDisplayX = getPaddingStart();
        }
        this.mDisplayX = Math.max(0, this.mDisplayX);
        this.mDisplayY = Math.max(0, this.mDisplayY);
    }

    private void drawContent(Canvas canvas, int drawPosX, int drawPosY, Rect rect, Paint paint) {
        int size = this.mSelectList.size();
        int offsetY = measureTextHeightOffset(paint);
        canvas.save();
        canvas.clipRect(rect);
        log("drawContent mSelectPosition [" + this.mSelectPosition + "]");
        for (int i = 0; i < (this.mItemCount + 1) + 1; i++) {
            int drawPos = (this.mSelectPosition - (this.mHalfScrollItemCount + 1)) + i;
            if (this.mWrapWheel) {
                drawPos = (drawPos + size) % size;
            }
            if (drawPos >= 0 && drawPos < size) {
                String str = ((String) this.mSelectList.get(drawPos)) + this.mNumberText;
                canvas.drawText(str, (float) (drawPosX + measureTextWidthOffset(paint, str)), (float) (drawPosY + offsetY), paint);
            }
            drawPosY += this.mItemHeight;
        }
        canvas.restore();
    }

    private int[] computeSelectPosition(int scrollOffset) {
        int selectPosition = (-scrollOffset) / this.mItemHeight;
        int scrollPosition = (scrollOffset % this.mItemHeight) - this.mItemHeight;
        if (this.mWrapWheel) {
            while (selectPosition < 0) {
                selectPosition += this.mSelectList.size();
            }
            while (selectPosition >= this.mSelectList.size()) {
                selectPosition -= this.mSelectList.size();
            }
        }
        return new int[]{selectPosition, scrollPosition};
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mSelfHeight = getMeasuredHeight();
        this.mSelfWidth = getMeasuredWidth();
        measureScrollItemWidth();
        if (widthSpecMode != 1073741824) {
            int needWidthSize = this.mScrollItemWidth;
            if (!TextUtils.isEmpty(this.mPickerTextStr)) {
                needWidthSize += ((int) this.mPickerPaint.measureText(this.mPickerTextStr)) + this.mScrollItemGap;
            }
            if (widthSpecMode == Integer.MIN_VALUE) {
                this.mSelfWidth = Math.min(needWidthSize, MeasureSpec.getSize(widthMeasureSpec));
            } else {
                this.mSelfWidth = needWidthSize;
            }
            this.mSelfWidth += getPaddingLeft() + getPaddingRight();
            this.mSelfWidth = Math.max(getMinimumWidth(), this.mSelfWidth);
        }
        if (heightSpecMode != 1073741824) {
            if (this.mItemHeightDefined) {
                this.mSelfHeight = this.mItemCount * this.mItemHeight;
            } else {
                this.mSelfHeight = this.mItemCount * measureScrollItemHeight();
            }
            this.mSelfHeight += getPaddingBottom() + getPaddingTop();
            if (heightSpecMode == Integer.MIN_VALUE) {
                this.mSelfHeight = Math.min(this.mSelfHeight, MeasureSpec.getSize(heightMeasureSpec));
            }
            this.mSelfHeight = Math.max(getMinimumHeight(), this.mSelfHeight);
            if (!this.mItemHeightDefined) {
                int i;
                if (this.mItemCount == 0) {
                    i = 0;
                } else {
                    i = Math.max(0, (this.mSelfHeight - getPaddingBottom()) - getPaddingTop()) / this.mItemCount;
                }
                this.mItemHeight = i;
            }
        } else if (this.mItemCount > 0) {
            this.mItemHeight = this.mSelfHeight / this.mItemCount;
        }
        this.mItemHeight = Math.max(0, this.mItemHeight);
        setFadingEdgeLength(((this.mItemHeight * (this.mHalfScrollItemCount * 2)) + this.mItemHeight) / 2);
        log("onMeasure selfWidth[" + this.mSelfWidth + "] selfHeight[" + this.mSelfHeight + "]");
        log("onMeasure mItemHeight[" + this.mItemHeight + "]");
        setMeasuredDimension(this.mSelfWidth, this.mSelfHeight);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mShader = null;
    }

    private void measureScrollItemWidth() {
        if (this.mScrollItemNeedMeasure) {
            int maxWidth = 0;
            String maxStr = Events.DEFAULT_SORT_ORDER;
            for (String str : this.mSelectList) {
                int strLen = (int) this.mScrollItemPaint.measureText(str);
                if (maxWidth < strLen) {
                    maxStr = str;
                }
                maxWidth = Math.max(maxWidth, strLen);
            }
            this.mScrollItemWidth = Math.max(maxWidth, (int) this.mSelectItemPaint.measureText(maxStr));
            this.mScrollItemNeedMeasure = false;
        }
        log("measureScrollItemWidth : " + this.mScrollItemWidth);
    }

    private int measureScrollItemHeight() {
        int height = 0;
        if (this.mSelectList.size() > 0) {
            height = (int) Math.max(Math.abs(this.mScrollItemPaint.descent() - this.mScrollItemPaint.ascent()), Math.abs(this.mSelectItemPaint.descent() - this.mSelectItemPaint.ascent()));
        }
        return Math.max(0, height);
    }

    private int measureTextHeightOffset(Paint paint) {
        if (paint == null) {
            return 0;
        }
        return Math.max(((this.mItemHeight - ((int) Math.abs(paint.descent() - paint.ascent()))) / 2) + ((int) Math.abs(paint.ascent())), 0);
    }

    private int measureTextWidthOffset(Paint paint, String str) {
        if (paint == null || TextUtils.isEmpty(str)) {
            return 0;
        }
        int offsetX;
        int itemDisplayWidth = this.mScrollItemWidth;
        int textWidth = (int) paint.measureText(str);
        if (this.mItemGravity == 3) {
            offsetX = 0;
        } else if (this.mItemGravity == 5) {
            offsetX = itemDisplayWidth - textWidth;
        } else {
            offsetX = (itemDisplayWidth - textWidth) / 2;
        }
        return Math.max(0, offsetX);
    }

    protected void onSelectChanged(int desPos, String curStr, String selectItem) {
        if (this.mListener != null) {
            log("onSelectChanged, desPos:" + desPos + " old:" + this.mSelectItemText + " new:" + curStr);
            this.mListener.onChanged(selectItem, curStr);
        }
    }

    private void log(String msg) {
    }

    private void singleClickChoose(int tabY) {
        int currentY = tabY;
        int dispplayTop = getPaddingTop() + (this.mItemHeight * this.mHalfScrollItemCount);
        int dispplayBottom = (getPaddingTop() + (this.mItemHeight * this.mHalfScrollItemCount)) + this.mItemHeight;
        int flingDistance = 0;
        if (tabY < dispplayTop) {
            flingDistance = (((dispplayTop - tabY) / this.mItemHeight) + 1) * this.mItemHeight;
        } else if (tabY > dispplayBottom) {
            flingDistance = (((dispplayBottom - tabY) / this.mItemHeight) - 1) * this.mItemHeight;
        }
        fling(flingDistance);
        onScrollStateChange(2);
    }
}
