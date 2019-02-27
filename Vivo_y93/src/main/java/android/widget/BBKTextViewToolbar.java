package android.widget;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView.ScaleType;
import com.vivo.internal.R;
import java.lang.reflect.Method;
import java.util.ArrayList;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public abstract class BBKTextViewToolbar {
    private static final int TOLERANCE_TOUCH = 15;
    private static int TOOLBAR_ITEM_PADDING_BOTTOM = 3;
    private static final int TOOLBAR_ITEM_PADDING_LEFT_AND_RIGHT = 5;
    private boolean isEditEnable = true;
    private int mArrowAboveDrawableResId;
    private int mArrowBelowDrawableResId;
    private int mCenterDrawableResId;
    private PopupWindow mContainer;
    protected Context mContext;
    private float mDensityScale = 0.0f;
    private int mDraghander = -1;
    protected View mHostView;
    protected LayoutInflater mLayoutInflater;
    protected LayoutParams mLayoutParams = null;
    private int mLeftDrawableResId;
    private int mLineHeight;
    private Paint mPaint;
    private int mPositionX;
    private int mPositionY;
    private int mRightDrawableResId;
    private int mScreenX;
    private int mScreenY;
    private int mSingleDrawableResId;
    private int mStatusBarHeight;
    protected TextView mTextView;
    private int mTextViewLocationX = 0;
    protected int mToleranceTouch;
    private boolean mToolbarAbove = true;
    private String mToolbarAppTag;
    private Object mToolbarAppTagObject;
    protected LinearLayout mToolbarGroup;
    protected int mToolbarItemPaddingBottom;
    protected int mToolbarItemPaddingLeftAndRight;
    private int mToolbarPositionArrowHeight;
    protected ImageView mToolbarPositionArrowView;
    private int mToolbarPositionArrowWidth;
    protected View mToolbarView;
    private ArrayList<View> mVisibleItems = new ArrayList();
    protected WindowManager mWindowManager;

    protected abstract OnClickListener getOnClickListener();

    protected abstract void updateToolbarItems();

    public BBKTextViewToolbar(TextView hostView) {
        this.mHostView = hostView;
        this.mContext = this.mHostView.getContext();
        this.mDensityScale = this.mContext.getResources().getDisplayMetrics().density;
        Resources res = this.mContext.getResources();
        this.mTextView = hostView;
        this.mPaint = hostView.getPaint();
        TypedArray toolbarType = this.mContext.obtainStyledAttributes(null, R.styleable.TextViewToolbar, R.attr.textViewToolbarStyle, R.style.Vigour_TextViewToolbar);
        this.mLeftDrawableResId = toolbarType.getResourceId(0, 0);
        this.mCenterDrawableResId = toolbarType.getResourceId(2, 0);
        this.mRightDrawableResId = toolbarType.getResourceId(1, 0);
        this.mSingleDrawableResId = toolbarType.getResourceId(3, 0);
        this.mArrowAboveDrawableResId = toolbarType.getResourceId(4, 0);
        this.mArrowBelowDrawableResId = toolbarType.getResourceId(5, 0);
        TOOLBAR_ITEM_PADDING_BOTTOM = res.getInteger(R.integer.toolbar_item_padding_bottom);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        calculateTolerance();
        this.mStatusBarHeight = res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
        this.mLayoutInflater = LayoutInflater.from(this.mContext);
        this.mToolbarView = this.mLayoutInflater.inflate((int) R.layout.vigour_text_toolbar, null);
        this.mToolbarGroup = (LinearLayout) this.mToolbarView.findViewById(R.id.toolbar_group);
        this.mToolbarGroup.setPadding(2, 2, 2, 2);
        this.mToolbarPositionArrowView = (ImageView) this.mToolbarView.findViewById(R.id.toolbar_position_arrow);
        this.mToolbarPositionArrowView.setImageResource(this.mArrowBelowDrawableResId);
        this.mToolbarView.measure(0, 0);
        this.mToolbarPositionArrowWidth = this.mToolbarPositionArrowView.getMeasuredWidth();
        this.mToolbarPositionArrowHeight = this.mToolbarPositionArrowView.getMeasuredHeight();
        this.mContainer = new PopupWindow(this.mToolbarView, 400, 72);
        this.mContainer.setBackgroundDrawable(new ColorDrawable(0));
        this.mContainer.setOutsideTouchable(true);
        this.mContainer.setSplitTouchEnabled(true);
        this.mToolbarAppTagObject = this.mTextView.getTag();
        if (this.mToolbarAppTagObject instanceof String) {
            this.mToolbarAppTag = (String) this.mTextView.getTag();
        } else {
            this.mToolbarAppTag = "";
        }
        this.mContainer.setWindowLayoutType(1002);
    }

    public boolean isShowing() {
        return this.mContainer.isShowing();
    }

    public void hide() {
        this.mContainer.dismiss();
    }

    protected void rePrepare(int screenX, int screenY, int cursorLineHeight, boolean selected) {
        this.mToolbarView.measure(0, 0);
        boolean aboveCursor = calculatePosition(screenX, screenY, cursorLineHeight, selected);
        int paddingLeft = Math.min((this.mToolbarGroup.getMeasuredWidth() - this.mToolbarPositionArrowWidth) - 10, Math.max(10, (screenX - this.mPositionX) - (this.mToolbarPositionArrowWidth / 2)));
        if (aboveCursor) {
            this.mToolbarPositionArrowView.setImageResource(this.mArrowBelowDrawableResId);
            this.mToolbarGroup.setPadding(2, 2, 2, 2);
            this.mToolbarPositionArrowView.setPadding(paddingLeft, this.mToolbarGroup.getMeasuredHeight() - 3, 0, 0);
            return;
        }
        this.mToolbarPositionArrowView.setImageResource(this.mArrowAboveDrawableResId);
        this.mToolbarGroup.setPadding(2, this.mToolbarPositionArrowHeight - 1, 2, 2);
        this.mToolbarPositionArrowView.setPadding(paddingLeft, 0, 0, 0);
    }

    protected void prepare(int screenX, int screenY, int cursorLineHeight, boolean selected) {
        this.mToolbarView.measure(0, 0);
        boolean aboveCursor = calculatePosition(screenX, screenY, cursorLineHeight, selected);
        if (this.mToolbarGroup.getMeasuredHeight() <= 0) {
            Log.w("perseus", "ensure measure");
        }
        int paddingLeft = Math.min((this.mToolbarGroup.getMeasuredWidth() - this.mToolbarPositionArrowWidth) - 10, Math.max(10, ((screenX - this.mPositionX) - (this.mToolbarPositionArrowWidth / 2)) - 1));
        if (this.mTextView.getLayoutDirection() == 1) {
            paddingLeft = (this.mToolbarGroup.getMeasuredWidth() - paddingLeft) - ((int) (this.mDensityScale * 10.0f));
        }
        int paddingBottom = (this.mToolbarGroup.getMeasuredHeight() - this.mToolbarGroup.getPaddingTop()) - this.mToolbarGroup.getPaddingBottom();
        if (aboveCursor) {
            this.mToolbarPositionArrowView.setImageResource(this.mArrowBelowDrawableResId);
            this.mToolbarGroup.setPadding(2, 2, 2, 2);
            int density = this.mHostView.getResources().getDisplayMetrics().densityDpi;
            if (density == 320) {
                this.mToolbarPositionArrowView.setPaddingRelative(paddingLeft, 73, 0, 0);
                return;
            } else if (density == 480) {
                this.mToolbarPositionArrowView.setPaddingRelative(paddingLeft, paddingBottom, 0, 0);
                return;
            } else if (density == 640) {
                this.mToolbarPositionArrowView.setPaddingRelative(paddingLeft, paddingBottom, 0, 0);
                return;
            } else if (density == 240) {
                this.mToolbarPositionArrowView.setPaddingRelative(paddingLeft, 52, 0, 0);
                return;
            } else {
                this.mToolbarPositionArrowView.setPaddingRelative(paddingLeft, 51, 0, 0);
                return;
            }
        }
        this.mToolbarPositionArrowView.setImageResource(this.mArrowAboveDrawableResId);
        this.mToolbarGroup.setPadding(2, this.mToolbarPositionArrowHeight - 1, 2, 2);
        this.mToolbarPositionArrowView.setPaddingRelative(paddingLeft, 0, 0, 0);
    }

    protected void update() {
        int i;
        updateToolbarItems();
        this.mVisibleItems.clear();
        int childCount = this.mToolbarGroup.getChildCount();
        for (i = 0; i < childCount; i++) {
            View child = this.mToolbarGroup.getChildAt(i);
            if (child != null && child.getVisibility() == 0) {
                this.mVisibleItems.add(child);
            }
        }
        int visibleItemsCount = this.mVisibleItems.size();
        View view;
        if (visibleItemsCount >= 2) {
            for (i = 0; i < visibleItemsCount; i++) {
                view = (View) this.mVisibleItems.get(i);
                if (i == 0) {
                    view.setBackgroundResource(this.mLeftDrawableResId);
                    view.setPadding(this.mToolbarItemPaddingLeftAndRight * 2, 0, this.mToolbarItemPaddingLeftAndRight, this.mToolbarItemPaddingBottom);
                } else if (i == visibleItemsCount - 1) {
                    view.setBackgroundResource(this.mRightDrawableResId);
                    view.setPadding(this.mToolbarItemPaddingLeftAndRight, 0, this.mToolbarItemPaddingLeftAndRight * 2, this.mToolbarItemPaddingBottom);
                } else {
                    view.setBackgroundResource(this.mCenterDrawableResId);
                    view.setPadding(this.mToolbarItemPaddingLeftAndRight, 0, this.mToolbarItemPaddingLeftAndRight, this.mToolbarItemPaddingBottom);
                }
            }
        } else if (visibleItemsCount == 1) {
            view = (View) this.mVisibleItems.get(0);
            view.setBackgroundResource(this.mSingleDrawableResId);
            view.setPadding(this.mToolbarItemPaddingLeftAndRight * 2, 0, this.mToolbarItemPaddingLeftAndRight * 2, this.mToolbarItemPaddingBottom);
        }
    }

    private boolean calculatePosition(int screenX, int screenY, int cursorLineHeight, boolean selected) {
        int x;
        int y;
        boolean aboveCursor;
        int px = screenX - this.mHostView.getRootView().getScrollX();
        int half = this.mToolbarGroup.getMeasuredWidth() / 2;
        int displayWidth = this.mWindowManager.getDefaultDisplay().getWidth();
        if (px + half < displayWidth) {
            x = px - half;
        } else {
            x = displayWidth - this.mToolbarGroup.getMeasuredWidth();
        }
        int density = this.mHostView.getResources().getDisplayMetrics().densityDpi;
        if (density == 320) {
            this.mPositionX = Math.max(this.mTextViewLocationX - 30, x);
        } else if (density == 480) {
            this.mPositionX = Math.max(this.mTextViewLocationX - 50, x);
        } else if (density == 640) {
            this.mPositionX = Math.max(this.mTextViewLocationX - 50, x);
        } else if (density == 240) {
            this.mPositionX = Math.max(this.mTextViewLocationX - 30, x);
        } else {
            this.mPositionX = Math.max(this.mTextViewLocationX, x);
        }
        if ("bbk.com.android.mms".equals(this.mToolbarAppTag) && this.mScreenY < this.mStatusBarHeight + 20) {
            screenY = this.mStatusBarHeight + 20;
        }
        int py = screenY - this.mHostView.getRootView().getScrollY();
        int th = this.mToolbarGroup.getMeasuredHeight() + this.mToolbarPositionArrowHeight;
        int lh = cursorLineHeight / 2;
        if ((py - th) - lh < this.mStatusBarHeight) {
            if (this.isEditEnable) {
                y = (py + lh) + this.mToleranceTouch;
            } else {
                y = py + lh;
            }
            if ("bbk.com.android.mms".equals(this.mToolbarAppTag) && this.mScreenY <= this.mStatusBarHeight + 20) {
            }
            if (this.mScreenY <= 0) {
                aboveCursor = true;
            } else {
                aboveCursor = false;
            }
            if (selected) {
                y += 50;
            }
        } else {
            if (this.isEditEnable) {
                y = (((py - th) - lh) - this.mToleranceTouch) + 6;
            } else {
                y = (py - th) - lh;
            }
            aboveCursor = true;
        }
        this.mPositionY = Math.max(this.mStatusBarHeight, y);
        return aboveCursor;
    }

    private void calculateTolerance() {
        DisplayMetrics dm = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getMetrics(dm);
        float ratio = (((float) dm.densityDpi) * 1.0f) / 160.0f;
        this.mToleranceTouch = Math.round(15.0f * ratio);
        this.mToolbarItemPaddingLeftAndRight = Math.round(5.0f * ratio);
        if (dm.densityDpi == 320) {
            this.mToolbarItemPaddingBottom = 11;
        } else {
            this.mToolbarItemPaddingBottom = Math.round(((float) TOOLBAR_ITEM_PADDING_BOTTOM) * ratio);
        }
    }

    public void setEditEable(boolean enable) {
        this.isEditEnable = enable;
    }

    public boolean getEditEable() {
        return this.isEditEnable;
    }

    private void calculateScreenPosition() {
        int[] location = new int[2];
        this.mTextView.getLocationOnScreen(new int[2]);
        int start = this.mTextView.getSelectionStart();
        int end = this.mTextView.getSelectionEnd();
        Layout layout = this.mTextView.getLayout();
        if (layout == null) {
            try {
                Method m = this.mTextView.getClass().getDeclaredMethod("assumeLayout", new Class[0]);
                m.setAccessible(true);
                m.invoke(this.mTextView, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            layout = this.mTextView.getLayout();
        }
        this.mTextView.getLocationInWindow(location);
        int line = layout.getLineForOffset(start);
        int top = layout.getLineTop(line);
        int bottom = layout.getLineBottom(line);
        this.mTextViewLocationX = location[0];
        this.mLineHeight = bottom - top;
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }
        this.mScreenY = ((((this.mLineHeight / 2) + top) + location[1]) + this.mTextView.getTotalPaddingTop()) - this.mTextView.getScrollY();
        if (start == end) {
            this.mScreenX = ((Math.round(layout.getPrimaryHorizontal(start)) + location[0]) + this.mTextView.getTotalPaddingLeft()) - this.mTextView.getScrollX();
        } else {
            int right;
            int left = Math.round(layout.getPrimaryHorizontal(start));
            if (line == layout.getLineForOffset(end)) {
                right = Math.round(layout.getPrimaryHorizontal(end));
            } else {
                left = 0;
                right = Math.round(layout.getLineRight(line));
            }
            this.mScreenX = ((((left + right) / 2) + location[0]) + this.mTextView.getTotalPaddingLeft()) - this.mTextView.getScrollX();
        }
        if (!getEditEable()) {
            float textWidth = 0.0f;
            if (this.mTextView.length() > 0) {
                textWidth = this.mPaint.measureText(this.mTextView.getText().toString());
            }
            if (textWidth > ((float) this.mTextView.getWidth())) {
                this.mScreenX = (location[0] + (this.mTextView.getWidth() / 2)) + this.mTextView.getPaddingLeft();
            } else {
                this.mScreenX = (location[0] + ((int) (textWidth / 2.0f))) + this.mTextView.getPaddingLeft();
            }
        }
        this.mScreenY = Math.max(location[1], this.mScreenY);
        Rect r = new Rect();
        this.mHostView.getRootView().getWindowVisibleDisplayFrame(r);
        this.mScreenY = Math.min(this.mScreenY, r.bottom - r.top);
    }

    private boolean isSupportHandle() {
        boolean windowSupportsHandles = false;
        ViewGroup.LayoutParams params = this.mTextView.getRootView().getLayoutParams();
        if (params instanceof LayoutParams) {
            LayoutParams windowParams = (LayoutParams) params;
            windowSupportsHandles = windowParams.type >= 1000 ? windowParams.type > LayoutParams.LAST_SUB_WINDOW : true;
        }
        if (!windowSupportsHandles || this.mTextView.getLayout() == null) {
            return false;
        }
        return true;
    }

    public void show() {
        if (!("com.chaozh.iReaderFree15".equals(this.mContext.getPackageName()) || this.mContainer.isShowing() || !isSupportHandle())) {
            boolean z;
            this.mContainer.dismiss();
            calculateScreenPosition();
            update();
            int start = this.mTextView.getSelectionStart();
            int end = this.mTextView.getSelectionEnd();
            int i = this.mScreenX;
            int i2 = this.mScreenY;
            int i3 = this.mLineHeight;
            if (start != end) {
                z = true;
            } else {
                z = false;
            }
            prepare(i, i2, i3, z);
            this.mContainer.setWidth(this.mToolbarView.getMeasuredWidth());
            this.mContainer.setHeight(this.mToolbarView.getMeasuredHeight() + 20);
            this.mContainer.showAtLocation(this.mTextView, 0, this.mPositionX, this.mPositionY);
        }
    }

    public void move() {
        if (this.mContainer.isShowing() && isSupportHandle()) {
            calculateScreenPosition();
            update();
            prepare(this.mScreenX, this.mScreenY, this.mLineHeight, this.mTextView.getSelectionStart() != this.mTextView.getSelectionEnd());
            this.mContainer.update(this.mPositionX, this.mPositionY, this.mToolbarView.getMeasuredWidth(), this.mToolbarView.getMeasuredHeight());
        }
    }

    protected TextView initToolbarItemTextView(int id, int textResId) {
        TextView textView = new TextView(this.mContext);
        textView.setGravity(17);
        textView.setTextColor(this.mContext.getResources().getColor(R.color.vigour_primary_text_toolbar));
        int mDensity = this.mHostView.getResources().getDisplayMetrics().densityDpi;
        if (mDensity == 160) {
            textView.setMinimumWidth(53);
            textView.setHeight(34);
        } else if (mDensity == 240) {
            textView.setMinimumWidth(80);
            textView.setHeight(51);
        } else if (mDensity == 320) {
            textView.setMinimumWidth(120);
            textView.setHeight(81);
        } else if (mDensity == 480) {
            textView.setMinimumWidth(180);
            textView.setMinimumHeight(120);
        } else if (mDensity == 640) {
            textView.setMinimumWidth(240);
            textView.setMinimumHeight(160);
        } else {
            textView.setMinimumWidth(80);
            textView.setHeight(51);
        }
        textView.setId(id);
        textView.setPadding(this.mToolbarItemPaddingLeftAndRight, 0, this.mToolbarItemPaddingLeftAndRight, 0);
        textView.setText(textResId);
        textView.setOnClickListener(getOnClickListener());
        return textView;
    }

    protected ImageView initToolbarItemImage(int id, int imgResId) {
        ImageView imageView = new ImageView(this.mContext);
        float mDensity = (float) this.mHostView.getResources().getDisplayMetrics().densityDpi;
        if (mDensity == 160.0f) {
            imageView.setMinimumWidth(53);
            imageView.setMinimumHeight(34);
        } else if (mDensity == 240.0f) {
            imageView.setMinimumWidth(80);
            imageView.setMinimumHeight(51);
        } else if (mDensity == 320.0f) {
            imageView.setMinimumWidth(120);
            imageView.setMinimumHeight(81);
        } else if (mDensity == 480.0f) {
            imageView.setMinimumWidth(180);
            imageView.setMinimumHeight(120);
        } else if (mDensity == 640.0f) {
            imageView.setMinimumWidth(240);
            imageView.setMinimumHeight(160);
        } else {
            imageView.setMinimumWidth(80);
            imageView.setMinimumHeight(51);
        }
        imageView.setId(id);
        imageView.setPadding(this.mToolbarItemPaddingLeftAndRight, 0, this.mToolbarItemPaddingLeftAndRight, 0);
        imageView.setImageResource(imgResId);
        imageView.setScaleType(ScaleType.CENTER);
        imageView.setOnClickListener(getOnClickListener());
        return imageView;
    }
}
