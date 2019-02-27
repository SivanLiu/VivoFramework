package android.widget;

import android.content.res.Resources;
import android.graphics.Rect;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import com.android.internal.R;
import java.lang.reflect.Method;

public class FtTextViewPopupToolbar extends FtPopupToolbar {
    static final int ID_COPY = 16908321;
    private static final int ID_COPY_STR = 17039361;
    static final int ID_CUT = 16908320;
    private static final int ID_CUT_STR = 17039363;
    private static final int ID_INPUTMETHOD_IMG = 50463698;
    static final int ID_PASTE = 16908322;
    private static final int ID_PASTE_STR = 17039371;
    static final int ID_SELECT_ALL = 16908319;
    private static final int ID_SELECT_ALL_STR = 17039373;
    static final int ID_START_SELECTING_TEXT = 16908328;
    private static final int ID_START_SELECTING_TEXT_STR = 51249307;
    static final int ID_SWITCH_INPUT_METHOD = 16908324;
    private static final String TAG = "FtTextViewPopupToolbar";
    private static final int TOLERANCE_TOUCH = 15;
    private boolean isEditEnable = true;
    private boolean mAboveCursor = true;
    private float mDensityScale = 0.0f;
    private int mHandleHeight = 0;
    private int mScreenX;
    private int mScreenY;
    private int mSpecifiedLineHeight;
    private int mStatusBarHeight;
    private TextView mTextView;
    private int mTextViewLocationX = 0;
    private int mToleranceTouch;
    private String mToolbarAppTag;
    private WindowManager mWindowManager;

    public FtTextViewPopupToolbar(TextView hostView) {
        super(hostView);
        this.mTextView = hostView;
        Resources res = this.mContext.getResources();
        this.mDensityScale = res.getDisplayMetrics().density;
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        if (this.mTextView.getTag() instanceof String) {
            this.mToolbarAppTag = (String) this.mTextView.getTag();
        } else {
            this.mToolbarAppTag = "";
        }
        this.mStatusBarHeight = res.getDimensionPixelSize(R.dimen.status_bar_height);
        calculateTolerance();
        initToolbar();
        this.mHandleHeight = this.mContext.getDrawable(this.mTextView.mTextSelectHandleLeftRes).getIntrinsicHeight();
        this.mHorizontalLayout.setPadding(2, 2, 2, 2);
    }

    private void initToolbar() {
        initItemTextView(16908328, 51249307);
        initItemTextView(16908319, 17039373);
        initItemTextView(16908320, 17039363);
        initItemTextView(16908321, 17039361);
        initItemTextView(16908322, 17039371);
        initItemImageView(16908324, 50463698);
    }

    public void setEditEable(boolean enable) {
        this.isEditEnable = enable;
    }

    public boolean getEditEable() {
        return this.isEditEnable;
    }

    public void show() {
        if (!"com.chaozh.iReaderFree15".equals(this.mContext.getPackageName()) && !isShowing() && (isSupportHandlers() ^ 1) == 0) {
            super.show();
        }
    }

    protected void updateArrowPosition() {
        int paddingLeft = Math.min((this.mToolBarViewGroup.getMeasuredWidth() - this.mArrowWidth) - 10, Math.max(10, ((this.mScreenX - this.mPopupWindowLocX) - (this.mArrowWidth / 2)) - 1));
        if (this.mTextView.getLayoutDirection() == 1) {
            paddingLeft = (this.mToolBarViewGroup.getMeasuredWidth() - paddingLeft) - ((int) (this.mDensityScale * 10.0f));
        }
        int paddingBottom = (this.mToolBarViewGroup.getMeasuredHeight() - this.mToolBarViewGroup.getPaddingTop()) - this.mToolBarViewGroup.getPaddingBottom();
        if (this.mAboveCursor) {
            this.mArrowImg.setRotation(0.0f);
            this.mHorizontalLayout.setPadding(2, 2, 2, 2);
            this.mArrowImg.setPaddingRelative(this.mHorizontalLayout.getPaddingStart() + paddingLeft, this.mHorizontalLayout.getPaddingTop() + paddingBottom, 0, 0);
            return;
        }
        this.mArrowImg.setRotation(180.0f);
        this.mHorizontalLayout.setPadding(2, this.mArrowHeight, 2, 2);
        this.mArrowImg.setPaddingRelative(0, 0, this.mHorizontalLayout.getPaddingStart() + paddingLeft, 0);
    }

    protected void calculatePosition() {
        int[] location = new int[2];
        this.mTextView.getLocationInWindow(location);
        int start = this.mTextView.getSelectionStart();
        int end = this.mTextView.getSelectionEnd();
        boolean selected = isInSelectionMode();
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
        int line = layout.getLineForOffset(start);
        int top = layout.getLineTop(line);
        int bottom = layout.getLineBottom(line);
        Log.i(TAG, " line = " + line + " top = " + top + " bottom = " + bottom);
        this.mTextViewLocationX = location[0];
        this.mSpecifiedLineHeight = bottom - top;
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }
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
                textWidth = this.mTextView.getPaint().measureText(this.mTextView.getText().toString());
            }
            if (textWidth > ((float) this.mTextView.getWidth())) {
                this.mScreenX = (location[0] + (this.mTextView.getWidth() / 2)) + this.mTextView.getPaddingLeft();
            } else {
                this.mScreenX = (location[0] + ((int) (textWidth / 2.0f))) + this.mTextView.getPaddingLeft();
            }
        }
        this.mScreenY = ((((this.mSpecifiedLineHeight / 2) + top) + location[1]) + this.mTextView.getTotalPaddingTop()) - this.mTextView.getScrollY();
        this.mScreenY = Math.max(location[1], this.mScreenY);
        Rect visibleRect = new Rect();
        this.mTextView.getGlobalVisibleRect(visibleRect);
        this.mScreenY = Math.max((visibleRect.top + (this.mSpecifiedLineHeight / 2)) + this.mTextView.getTotalPaddingTop(), this.mScreenY);
        Rect r = new Rect();
        this.mHostView.getRootView().getWindowVisibleDisplayFrame(r);
        this.mScreenY = Math.min(this.mScreenY, r.bottom - r.top);
        this.mPopupToolbarView.measure(0, 0);
        if (this.mToolBarViewGroup.getMeasuredHeight() <= 0) {
            Log.e(TAG, "Tool bar View group measure height <= 0");
        }
        this.mAboveCursor = calculateWindowPosition(selected);
    }

    private boolean calculateWindowPosition(boolean selected) {
        int x;
        int y;
        boolean aboveCursor;
        int[] rootLocationOnScreen = new int[2];
        this.mHostView.getRootView().getLocationOnScreen(rootLocationOnScreen);
        int[] rootLocationInWindow = new int[2];
        this.mHostView.getRootView().getLocationInWindow(rootLocationInWindow);
        int windowLeft = rootLocationOnScreen[0] - rootLocationInWindow[0];
        int px = this.mScreenX - this.mHostView.getRootView().getScrollX();
        int toolbarWidth = this.mHorizontalLayout.getMeasuredWidth();
        int half = toolbarWidth / 2;
        int displayWidth = this.mWindowManager.getDefaultDisplay().getWidth();
        if ((windowLeft + px) + half < displayWidth) {
            x = px - half;
        } else {
            x = (displayWidth - toolbarWidth) - windowLeft;
        }
        int density = this.mHostView.getResources().getDisplayMetrics().densityDpi;
        if (density == 320) {
            this.mPopupWindowLocX = Math.max(this.mTextViewLocationX - 30, x);
        } else if (density == 480) {
            this.mPopupWindowLocX = Math.max(this.mTextViewLocationX - 50, x);
        } else if (density == 640) {
            this.mPopupWindowLocX = Math.max(this.mTextViewLocationX - 50, x);
        } else if (density == 240) {
            this.mPopupWindowLocX = Math.max(this.mTextViewLocationX - 30, x);
        } else {
            this.mPopupWindowLocX = Math.max(this.mTextViewLocationX, x);
        }
        this.mPopupWindowLocX = Math.max(0, this.mPopupWindowLocX);
        this.mPopupWindowLocX = Math.min(this.mPopupWindowLocX, (displayWidth - toolbarWidth) - windowLeft);
        if ("bbk.com.android.mms".equals(this.mToolbarAppTag) && this.mScreenY < this.mStatusBarHeight + 20) {
            this.mScreenY = this.mStatusBarHeight + 20;
        }
        int py = this.mScreenY - this.mHostView.getRootView().getScrollY();
        int th = this.mToolBarViewGroup.getMeasuredHeight() + this.mArrowHeight;
        int lh = this.mSpecifiedLineHeight / 2;
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
                y = ((py + lh) + this.mHandleHeight) - this.mArrowHeight;
            }
        } else {
            if (this.isEditEnable) {
                y = (((py - th) - lh) - this.mToleranceTouch) + 6;
            } else {
                y = (py - th) - lh;
            }
            aboveCursor = true;
        }
        this.mPopupWindowLocY = Math.max(this.mStatusBarHeight, y);
        return aboveCursor;
    }

    private void calculateTolerance() {
        DisplayMetrics dm = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getMetrics(dm);
        this.mToleranceTouch = Math.round(15.0f * ((((float) dm.densityDpi) * 1.0f) / 160.0f));
    }

    protected boolean isInSelectionMode() {
        return this.mTextView.getSelectionStart() != this.mTextView.getSelectionEnd();
    }
}
