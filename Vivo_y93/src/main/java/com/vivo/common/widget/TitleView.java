package com.vivo.common.widget;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.internal.R;
import java.util.HashMap;
import java.util.Map;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public abstract class TitleView extends RelativeLayout {
    protected static final int ICON_VIEW_LEFT = 0;
    protected static final int ICON_VIEW_MASK = 1;
    protected static final int ICON_VIEW_RIGHT = 1;
    protected static final int ICON_VIEW_SHIFT = 1;
    protected static final int ICON_VIEW_START_USE_ID = 0;
    private final boolean DEBUG;
    private final String TAG;
    protected int imagePaddingOuter;
    protected LinearLayout mCenterView;
    protected Context mContext;
    private Drawable mDivider;
    private int mDividerHeight;
    protected int mIconViewStartSysId;
    private Map<Integer, View> mIconViews;
    protected AverageLinearLayout mLeftIconLayout;
    protected AverageLinearLayout mRightIconLayout;
    private boolean mShowDivider;
    private ViewProxy mTitleProxy;
    protected int textPaddingInner;
    protected int textPaddingOuter;

    protected abstract class ViewProxy {
        public abstract int getContentMinWidth();

        public abstract int getContentWidth();

        protected ViewProxy() {
        }

        public void setOffset(int sOff, int eOff) {
            TitleView.this.mCenterView.setPaddingRelative(sOff, TitleView.this.mCenterView.getPaddingTop(), eOff, TitleView.this.mCenterView.getPaddingBottom());
        }

        public boolean supportAutoMove() {
            return true;
        }
    }

    protected class AverageLinearLayout extends LinearLayout {
        static final int POSITION_LEFT = 0;
        static final int POSITION_RIGHT = 1;
        private Map<View, IconViewInformation> mIconViewMap = new HashMap();
        private Map<View, Integer> mLastMaxWidth = new HashMap();
        private int mMaxWidth = Integer.MAX_VALUE;
        private int mPosition = -1;

        AverageLinearLayout(Context context, int position) {
            super(context);
            this.mPosition = position;
        }

        void setMaxWidth(int maxWidth) {
            this.mMaxWidth = maxWidth;
        }

        public void addView(View child, int index, LayoutParams params) {
            super.addView(child, index, params);
            insertViewInfo(child);
        }

        public void removeView(View view) {
            deleteViewInfo(view);
            super.removeView(view);
        }

        private void insertViewInfo(View view) {
            IconViewInformation viewInfo = new IconViewInformation(view);
            Log.d("TitleView", "insertViewInfo Count : " + getChildCount());
            addToList(viewInfo);
            this.mIconViewMap.put(view, viewInfo);
            updateViewList(view, false);
        }

        private void deleteViewInfo(View view) {
            removeFromList((IconViewInformation) this.mIconViewMap.get(view));
            updateViewList(view, true);
            this.mIconViewMap.remove(view);
        }

        private void addToList(IconViewInformation viewInfo) {
            if (viewInfo != null && getChildCount() > 1) {
                IconViewInformation endView = (IconViewInformation) this.mIconViewMap.get(getChildAt(getChildCount() - 2));
                endView.right = viewInfo;
                viewInfo.left = endView;
            }
        }

        private void removeFromList(IconViewInformation viewInfo) {
            if (viewInfo != null) {
                IconViewInformation next = viewInfo.right;
                IconViewInformation prev = viewInfo.left;
                if (prev != null) {
                    prev.right = next;
                }
                if (next != null) {
                    next.left = prev;
                }
            }
        }

        private void updateViewList(View view, boolean hide) {
            IconViewInformation viewInfo = (IconViewInformation) this.mIconViewMap.get(view);
            if (viewInfo != null) {
                IconViewInformation prev;
                IconViewInformation next;
                if (hide) {
                    prev = viewInfo.leftVisible;
                    next = viewInfo.rightVisible;
                    if (prev != null) {
                        prev.rightVisible = next;
                    }
                    if (next != null) {
                        next.leftVisible = prev;
                    }
                } else {
                    IconViewInformation tmp;
                    prev = viewInfo.left;
                    next = viewInfo.right;
                    for (tmp = prev; tmp != null; tmp = tmp.left) {
                        if (tmp.self.getVisibility() == 0) {
                            viewInfo.leftVisible = tmp;
                            viewInfo.rightVisible = tmp.rightVisible;
                            tmp.rightVisible = viewInfo;
                            break;
                        }
                    }
                    tmp = next;
                    while (next != null) {
                        if (tmp.self.getVisibility() == 0) {
                            viewInfo.rightVisible = tmp;
                            viewInfo.leftVisible = tmp.leftVisible;
                            tmp.leftVisible = viewInfo;
                            break;
                        }
                        tmp = tmp.right;
                    }
                }
            }
        }

        private void updateIconViewGap(IconViewInformation info, boolean visible) {
            int i = 0;
            IconViewInformation prev = this.mPosition == 0 ? info.leftVisible : info.rightVisible;
            IconViewInformation next = this.mPosition == 0 ? info.rightVisible : info.leftVisible;
            int i2;
            if (visible) {
                setMarginEnd(prev, 0);
                setMarginEnd(info, next == null ? TitleView.this.textPaddingInner : 0);
                if (!isDisplayImage(info) && !isDisplayImage(prev)) {
                    setMarginStart(info, TitleView.this.textPaddingOuter);
                } else if (prev == null) {
                    setMarginStart(info, TitleView.this.imagePaddingOuter);
                } else {
                    i2 = (isDisplayImage(info) && isDisplayImage(prev)) ? 0 : TitleView.this.textPaddingInner;
                    setMarginStart(info, i2);
                }
                if (isDisplayImage(info) || isDisplayImage(next)) {
                    if (!(isDisplayImage(info) && isDisplayImage(next))) {
                        i = TitleView.this.textPaddingInner;
                    }
                    setMarginStart(next, i);
                } else {
                    setMarginStart(next, TitleView.this.textPaddingOuter);
                }
            } else {
                if (next == null) {
                    i2 = TitleView.this.textPaddingInner;
                } else {
                    i2 = 0;
                }
                setMarginEnd(prev, i2);
                if (!isDisplayImage(prev) && !isDisplayImage(next)) {
                    setMarginStart(next, TitleView.this.textPaddingOuter);
                } else if (prev == null) {
                    setMarginStart(next, TitleView.this.imagePaddingOuter);
                } else {
                    if (!(isDisplayImage(prev) && isDisplayImage(next))) {
                        i = TitleView.this.textPaddingInner;
                    }
                    setMarginStart(next, i);
                }
            }
            TitleView.this.updateIconViewGapByUser(info, visible, this.mPosition);
        }

        private boolean isDisplayText(IconViewInformation info) {
            if (info == null || ((info.self instanceof TextView) ^ 1) != 0) {
                return false;
            }
            CharSequence str = ((TextView) info.self).getText();
            return (str != null ? str.toString().equals(Events.DEFAULT_SORT_ORDER) : 0) ^ 1;
        }

        private boolean isDisplayImage(IconViewInformation info) {
            if (info == null || ((info.self instanceof TextView) ^ 1) != 0) {
                return false;
            }
            CharSequence str = ((TextView) info.self).getText();
            return str != null ? str.toString().equals(Events.DEFAULT_SORT_ORDER) : false;
        }

        private void setMarginStart(IconViewInformation info, int margin) {
            if (info != null) {
                MarginLayoutParams lp = (MarginLayoutParams) info.self.getLayoutParams();
                if (this.mPosition == 0) {
                    lp.setMarginStart(margin);
                } else {
                    info.self.setPadding(0, 0, margin, 0);
                }
                info.self.setLayoutParams(lp);
            }
        }

        private void setMarginEnd(IconViewInformation info, int margin) {
            if (info != null) {
                MarginLayoutParams lp = (MarginLayoutParams) info.self.getLayoutParams();
                if (this.mPosition == 0) {
                    lp.setMarginEnd(margin);
                } else {
                    info.self.setPadding(0, 0, margin, 0);
                }
                info.self.setLayoutParams(lp);
            }
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int i;
            View v;
            int childCount = getChildCount();
            int visibleCount = 0;
            int imageWidth = 0;
            int marginWidth = 0;
            for (i = 0; i < childCount; i++) {
                v = getChildAt(i);
                updateIconViewGap((IconViewInformation) this.mIconViewMap.get(v), v.getVisibility() == 0);
            }
            resetChildMaxWidth();
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            for (i = 0; i < childCount; i++) {
                v = getChildAt(i);
                if (v.getVisibility() != 8) {
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
                    marginWidth += lp.leftMargin + lp.rightMargin;
                    if (isDisplayImage((IconViewInformation) this.mIconViewMap.get(v))) {
                        v.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
                        imageWidth += v.getMeasuredWidth();
                    } else if (v.getVisibility() == 0) {
                        visibleCount++;
                    }
                }
            }
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            widthSize = Math.min(widthSize, this.mMaxWidth);
            if (widthMode == 0) {
                this.mLastMaxWidth.clear();
            } else if (visibleCount <= 0 || widthSize >= getMeasuredWidth()) {
                restoreChildMaxWidth();
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            } else {
                int aveLen = Math.max(((widthSize - imageWidth) - marginWidth) / visibleCount, 0);
                int extraLen = 0;
                for (i = 0; i < childCount; i++) {
                    View childView = getChildAt(i);
                    if (childView.getVisibility() != 8) {
                        if (!isDisplayImage((IconViewInformation) this.mIconViewMap.get(childView))) {
                            TextView v2 = (TextView) childView;
                            if (v2.getMeasuredWidth() < aveLen) {
                                extraLen += aveLen - v2.getMeasuredWidth();
                            } else {
                                this.mLastMaxWidth.put(v2, Integer.valueOf(aveLen + extraLen));
                                v2.setMaxWidth(aveLen + extraLen);
                                extraLen = 0;
                            }
                        }
                    }
                }
                super.onMeasure(MeasureSpec.makeMeasureSpec(widthSize, Integer.MIN_VALUE), heightMeasureSpec);
            }
        }

        private void restoreChildMaxWidth() {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View v = getChildAt(i);
                if (v.getVisibility() != 8 && isDisplayText((IconViewInformation) this.mIconViewMap.get(v))) {
                    ((TextView) v).setMaxWidth(this.mLastMaxWidth.get(v) == null ? Integer.MAX_VALUE : ((Integer) this.mLastMaxWidth.get(v)).intValue());
                }
            }
        }

        private void resetChildMaxWidth() {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View v = getChildAt(i);
                if (v.getVisibility() != 8 && isDisplayText((IconViewInformation) this.mIconViewMap.get(v))) {
                    ((TextView) v).setMaxWidth(Integer.MAX_VALUE);
                }
            }
        }
    }

    protected class IconViewInformation {
        public IconViewInformation left = null;
        public IconViewInformation leftVisible = null;
        public IconViewInformation right = null;
        public IconViewInformation rightVisible = null;
        public View self;

        public IconViewInformation(View view) {
            this.self = view;
        }
    }

    protected abstract ViewProxy initCenterView();

    protected abstract Button initIconView(int i);

    public TitleView(Context context) {
        this(context, null);
    }

    public TitleView(Context context, AttributeSet attrs) {
        super(context, attrs, 50397228);
        this.TAG = "TitleView";
        this.DEBUG = false;
        this.mIconViewStartSysId = 256;
        this.mIconViews = new HashMap();
        this.textPaddingInner = 0;
        this.textPaddingOuter = 0;
        this.imagePaddingOuter = 0;
        this.mDivider = null;
        this.mDividerHeight = 0;
        this.mShowDivider = true;
        this.mContext = context;
        setGravity(16);
        TypedArray a = context.obtainStyledAttributes(attrs, new int[]{16842964}, 16842844, 0);
        setBackground(a.getDrawable(0));
        a.recycle();
        initViewLayout();
    }

    private void initViewLayout() {
        TypedArray typedArray = this.mContext.obtainStyledAttributes(null, R.styleable.TitleView, 50397228, 0);
        int defaultHeight = typedArray.getDimensionPixelOffset(1, 0);
        this.textPaddingInner = typedArray.getDimensionPixelOffset(3, 0);
        this.textPaddingOuter = typedArray.getDimensionPixelOffset(4, 0);
        this.imagePaddingOuter = typedArray.getDimensionPixelOffset(5, 0);
        this.mDivider = typedArray.getDrawable(2);
        this.mDividerHeight = typedArray.getDimensionPixelOffset(0, 0);
        this.mCenterView = new LinearLayout(this.mContext);
        RelativeLayout.LayoutParams pCenterTitle = new RelativeLayout.LayoutParams(-1, defaultHeight);
        pCenterTitle.addRule(20);
        pCenterTitle.addRule(15);
        addView(this.mCenterView, pCenterTitle);
        this.mTitleProxy = initCenterView();
        RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(-2, -2);
        rParams.addRule(20);
        rParams.addRule(15);
        this.mLeftIconLayout = new AverageLinearLayout(this.mContext, 0);
        addView(this.mLeftIconLayout, rParams);
        rParams = new RelativeLayout.LayoutParams(-2, -2);
        rParams.addRule(21);
        rParams.addRule(15);
        this.mRightIconLayout = new AverageLinearLayout(this.mContext, 1);
        addView(this.mRightIconLayout, rParams);
        typedArray.recycle();
    }

    protected void updateIconViewGapByUser(IconViewInformation info, boolean visible, int position) {
    }

    protected void adjustLayoutByUser() {
        requestLayout();
    }

    private ViewGroup getParentLayout(int id) {
        int pos = id & 1;
        if (pos == 0) {
            return this.mLeftIconLayout;
        }
        if (pos == 1) {
            return this.mRightIconLayout;
        }
        throw new IllegalArgumentException("illegal Postion [" + Integer.toBinaryString(pos) + "]");
    }

    protected View getIconViewById(int id) {
        return (View) this.mIconViews.get(Integer.valueOf(id));
    }

    protected int addIconView(int position) {
        switch (position) {
            case 0:
            case 1:
                break;
            default:
                position = 0;
                Log.e("TitleView", "addIconView illegalArgument position[" + 0 + "], use " + 0);
                break;
        }
        int i = this.mIconViewStartSysId + 1;
        this.mIconViewStartSysId = i;
        int id = (i << 1) | position;
        addIconViewWidthId(id);
        return id;
    }

    protected void removeIconView(int id) {
        View view = getIconViewById(id);
        ViewGroup parent = getParentLayout(id);
        if (view != null) {
            parent.removeView(view);
            this.mIconViews.remove(Integer.valueOf(id));
            return;
        }
        Log.e("TitleView", "removeIconView id[" + id + "] failed, not exists", new Throwable());
    }

    protected void addIconViewWidthId(int id) {
        if (this.mIconViews.get(Integer.valueOf(id)) != null) {
            Log.e("TitleView", "IconView [" + id + "] has exists");
            return;
        }
        Button view = initIconView(id & 1);
        if (view == null) {
            throw new RuntimeException("initIconView return null");
        }
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(-2, -2);
        lParams.gravity = 17;
        getParentLayout(id).addView(view, lParams);
        this.mIconViews.put(Integer.valueOf(id), view);
    }

    public void setIconViewDrawableRes(int id, int resId) {
        Button view = (Button) getIconViewById(id);
        if (view != null) {
            view.setBackgroundResource(resId);
            view.setText(Events.DEFAULT_SORT_ORDER);
            return;
        }
        Log.d("TitleView", "setIconViewDrawableRes failed view[" + view + "]");
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int[] padding;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        this.mLeftIconLayout.setMaxWidth(Integer.MAX_VALUE);
        this.mRightIconLayout.setMaxWidth(Integer.MAX_VALUE);
        if (widthMode != 0 && widthSize > 0) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(0, 0), heightMeasureSpec);
            padding = calculateViewPadding(widthSize);
            this.mLeftIconLayout.setMaxWidth(padding[0]);
            this.mLeftIconLayout.forceLayout();
            this.mRightIconLayout.setMaxWidth(padding[1]);
            this.mRightIconLayout.forceLayout();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (widthMode == 0) {
            widthSize = getMeasuredWidth();
        }
        padding = calculateViewPadding(widthSize);
        this.mTitleProxy.setOffset(padding[0], padding[1]);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int[] calculateViewPadding(int totalLen) {
        if (this.mTitleProxy.supportAutoMove()) {
            return autoAdjustViewPosition(totalLen);
        }
        return adjustViewPosition(totalLen);
    }

    private int[] autoAdjustViewPosition(int totalLen) {
        int rightOffset;
        int leftOffset;
        int leftLen = Math.max(this.mLeftIconLayout.getMeasuredWidth(), this.textPaddingOuter);
        int rightLen = Math.max(this.mRightIconLayout.getMeasuredWidth(), this.textPaddingOuter);
        int titleLen = this.mTitleProxy.getContentWidth();
        int maxLen = Math.max(leftLen, rightLen);
        int rTitleLen = Math.min(Math.max(this.mTitleProxy.getContentMinWidth(), (totalLen - rightLen) - leftLen), titleLen);
        int rBtnLen = totalLen - rTitleLen;
        if ((leftLen + rightLen) + rTitleLen <= totalLen) {
            if (maxLen <= rBtnLen / 2) {
                rightOffset = maxLen;
                leftOffset = maxLen;
            } else if (leftLen > rBtnLen / 2) {
                leftOffset = leftLen;
                rightOffset = rBtnLen - leftLen;
            } else {
                rightOffset = rightLen;
                leftOffset = rBtnLen - rightLen;
            }
        } else if (leftLen <= rBtnLen / 2) {
            leftOffset = leftLen;
            rightOffset = rBtnLen - leftLen;
        } else if (rightLen <= rBtnLen / 2) {
            rightOffset = rightLen;
            leftOffset = rBtnLen - rightLen;
        } else {
            rightOffset = rBtnLen / 2;
            leftOffset = rightOffset;
        }
        return new int[]{Math.max(leftOffset, 0), Math.max(rightOffset, 0)};
    }

    private int[] adjustViewPosition(int totalLen) {
        int resPadding = Math.max(Math.min((totalLen - this.mTitleProxy.getContentMinWidth()) / 2, Math.max(this.mLeftIconLayout.getMeasuredWidth(), this.mRightIconLayout.getMeasuredWidth())), 0);
        return new int[]{Math.max(resPadding, 0), Math.max(resPadding, 0)};
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!this.mShowDivider) {
            return;
        }
        if (this.mDivider == null || this.mDividerHeight <= 0) {
            Log.e("TitleView", "ignore Horizontal Divider divider=" + this.mDivider + " dividerHeight=" + this.mDividerHeight);
            return;
        }
        this.mDivider.setBounds(0, getHeight() - this.mDividerHeight, getWidth(), getHeight());
        this.mDivider.draw(canvas);
    }

    public void showDivider(boolean show) {
        if (show != this.mShowDivider) {
            this.mShowDivider = show;
            invalidate();
        }
    }
}
