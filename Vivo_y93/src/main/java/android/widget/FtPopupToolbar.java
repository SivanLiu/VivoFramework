package android.widget;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView.ScaleType;
import com.vivo.internal.R;
import java.lang.reflect.Method;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class FtPopupToolbar implements OnClickListener {
    private static final String TAG = "PopupToolbar";
    private static final int viewItemPaddingEnd = 2;
    private static final int viewItemPaddingStart = 2;
    private int ID_ARROW = R.drawable.vigour_text_toolbar_position_arrow_below_light;
    private int ID_LEFT = R.drawable.vigour_text_toolbar_left_light;
    private int ID_MIDDLE = R.drawable.vigour_text_toolbar_center_light;
    private int ID_RIGHT = R.drawable.vigour_text_toolbar_right_light;
    private int ID_WHOLE = R.drawable.vigour_text_toolbar_single_light;
    private int ITEM_STYLE = R.style.Widget_Vigour_ToolbarItem;
    private boolean isEditable;
    private boolean isShouldShowBelow = false;
    protected int mArrowHeight = 0;
    protected ImageView mArrowImg;
    private int mArrowLocX = -1;
    protected int mArrowWidth = 0;
    private float mArrowXPos = 0.0f;
    protected Context mContext;
    protected LinearLayout mHorizontalLayout;
    protected View mHostView;
    private int mHostViewLocX;
    private int mHostViewLocY;
    private boolean mIsTextView = false;
    private int mItemBackgroundPadding = 0;
    private LayoutInflater mLayoutInflater;
    protected final int mMinPaddinTop;
    private final int mMinpaddingStart = 30;
    private OnItemClickListener mOnItemClickListener;
    protected View mPopupToolbarView;
    private PopupWindow mPopupWindow;
    protected int mPopupWindowLocX = 0;
    protected int mPopupWindowLocY = 0;
    private TextViewToolbarRegulator mRegulator = null;
    private Resources mRes;
    private int mScreenWidth = 1080;
    protected PopupToolLayout mToolBarViewGroup;

    public interface OnItemClickListener {
        void onItemClick(View view);
    }

    class PopupToolLayout extends ViewGroup {
        private static final int MAX_ITEM_COUNT = 6;
        private Drawable mDivider;
        private int mDividerWidth;
        private int mWindowWidth;

        public PopupToolLayout(FtPopupToolbar this$0, Context context) {
            this(this$0, context, null);
        }

        public PopupToolLayout(FtPopupToolbar this$0, Context context, AttributeSet attrs) {
            this(this$0, context, attrs, 0);
        }

        public PopupToolLayout(FtPopupToolbar this$0, Context context, AttributeSet attrs, int defStyleAttr) {
            this(context, attrs, defStyleAttr, 0);
        }

        public PopupToolLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            setWillNotDraw(false);
            WindowManager wm = (WindowManager) context.getSystemService("window");
            Point size = new Point();
            wm.getDefaultDisplay().getSize(size);
            this.mWindowWidth = Math.min(size.x, size.y);
            this.mDivider = context.getResources().getDrawable(R.drawable.vigour_text_toolbar_divider, null);
            this.mDividerWidth = this.mDivider.getIntrinsicWidth();
        }

        protected void onDraw(Canvas canvas) {
            if (this.mDivider != null && (isShowDivider() ^ 1) == 0) {
                int count = getChildCount();
                boolean isLayoutRtl = FtPopupToolbar.this.isRtl();
                int i = 0;
                while (i < count) {
                    View child = getChildAt(i);
                    if (!(child == null || child.getVisibility() == 8 || !hasDividerBeforeChildAt(i))) {
                        int position;
                        if (isLayoutRtl) {
                            position = child.getRight();
                        } else {
                            position = child.getLeft() - this.mDividerWidth;
                        }
                        drawDivider(canvas, position);
                    }
                    i++;
                }
            }
        }

        private boolean hasDividerBeforeChildAt(int childIndex) {
            if (childIndex == 0) {
                return false;
            }
            if (childIndex == getChildCount()) {
                return true;
            }
            boolean hasVisibleViewBefore = false;
            for (int i = childIndex - 1; i >= 0; i--) {
                if (getChildAt(i).getVisibility() != 8) {
                    hasVisibleViewBefore = true;
                    break;
                }
            }
            return hasVisibleViewBefore;
        }

        private void drawDivider(Canvas canvas, int left) {
            this.mDivider.setBounds(left, 0, this.mDividerWidth + left, getHeight());
            this.mDivider.draw(canvas);
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int i = 0;
            int count = getChildCount();
            int visibleCount = 0;
            int measuredWidth = 0;
            int measuredHeight = 0;
            for (int i2 = 0; i2 < count; i2++) {
                View child = getChildAt(i2);
                if (child.getVisibility() == 0) {
                    visibleCount++;
                    child.measure(widthMeasureSpec, heightMeasureSpec);
                    measuredWidth += child.getMeasuredWidth();
                    measuredHeight = Math.max(measuredHeight, child.getMeasuredHeight());
                }
            }
            if (isShowDivider()) {
                i = (visibleCount - 1) * this.mDividerWidth;
            }
            setMeasuredDimension(measuredWidth + i, measuredHeight);
        }

        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int count = getChildCount();
            int left = 0;
            int start = 0;
            int dir = 1;
            if (FtPopupToolbar.this.isRtl()) {
                start = count - 1;
                dir = -1;
            }
            for (int i = 0; i < count; i++) {
                View child = getChildAt(start + (dir * i));
                if (child.getVisibility() == 0) {
                    child.layout(left, 0, child.getMeasuredWidth() + left, child.getMeasuredHeight());
                    left += (isShowDivider() ? this.mDividerWidth : 0) + child.getMeasuredWidth();
                }
            }
        }

        private boolean isShowDivider() {
            return false;
        }
    }

    class TextViewToolbarRegulator {
        private boolean Debug = false;
        private int LEFT_SPACE_WIDTH = 20;
        private int MAX_HEIGHT_TOLERANCE = 20;
        private int MAX_WIDTH_TOLERANCE = 20;
        private int RIGHT_SPACE_WIDHT = 20;
        private String TAG = "toolbar";
        private int availableAreaHeight = 0;
        private int availableAreaWidth = 0;
        private boolean isViewChanged = false;
        private Context mContext;
        private ViewGroup mToolbarGroup;
        private int originViewHeight = 0;
        private int originViewWidth = 0;

        private class AdjustViewInfo {
            TextView[] adjustView;
            int extraWidth;
            int[] marginWidth;
            int maxWidth;
            int[] measuredWidth;
            int viewCount;

            /* synthetic */ AdjustViewInfo(TextViewToolbarRegulator this$1, AdjustViewInfo -this1) {
                this();
            }

            private AdjustViewInfo() {
                this.adjustView = null;
                this.measuredWidth = null;
                this.marginWidth = null;
                this.viewCount = 0;
                this.extraWidth = 0;
                this.maxWidth = 0;
            }
        }

        public TextViewToolbarRegulator(Context con) {
            this.mContext = con;
            this.MAX_WIDTH_TOLERANCE = (int) (((float) this.MAX_WIDTH_TOLERANCE) * con.getResources().getDisplayMetrics().density);
            this.MAX_HEIGHT_TOLERANCE = (int) (((float) this.MAX_HEIGHT_TOLERANCE) * con.getResources().getDisplayMetrics().density);
        }

        protected boolean adjust(ViewGroup view, int[] dimen) {
            this.mToolbarGroup = view;
            if (this.mToolbarGroup == null || !needChange(dimen[0], dimen[1])) {
                return false;
            }
            return layout();
        }

        private boolean layout() {
            TextView adjustView = null;
            AdjustViewInfo info = getAdjustInformation();
            boolean isDuplicateLine = false;
            if (info == null) {
                return false;
            }
            log(this.TAG, "viewCount = " + info.viewCount + "\nextraWidth = " + info.extraWidth + "\nmaxWidth = " + info.maxWidth);
            for (int i = info.viewCount - 1; i >= 0; i--) {
                adjustView = info.adjustView[i];
                Paint paint = adjustView.getPaint();
                if (info.extraWidth + info.maxWidth < info.measuredWidth[i] + info.marginWidth[i]) {
                    int requiredWidth = getProperWidth(adjustView, info.extraWidth + info.maxWidth);
                    log(this.TAG, " requiredWidth = " + requiredWidth);
                    adjustView.setMaxLines(2);
                    adjustView.setMaxWidth(requiredWidth - info.marginWidth[i]);
                    info.extraWidth += info.maxWidth - requiredWidth;
                    isDuplicateLine = true;
                } else {
                    adjustView.setMaxWidth(info.measuredWidth[i]);
                    info.extraWidth += info.maxWidth - (info.measuredWidth[i] + info.marginWidth[i]);
                }
            }
            changeAllItemHeight(adjustView, isDuplicateLine);
            return true;
        }

        private AdjustViewInfo getAdjustInformation() {
            int i;
            View itemView;
            int viewCount = this.mToolbarGroup.getChildCount();
            int adjustWidth = this.availableAreaWidth;
            int textViewCount = 0;
            AdjustViewInfo info = new AdjustViewInfo(this, null);
            AnonymousClass1ViewDimension[] viewDimen = new AnonymousClass1ViewDimension[viewCount];
            info.adjustView = new TextView[viewCount];
            info.measuredWidth = new int[viewCount];
            info.marginWidth = new int[viewCount];
            for (i = 0; i < viewCount; i++) {
                itemView = this.mToolbarGroup.getChildAt(i);
                viewDimen[i] = new Object() {
                    int margin;
                    int width;

                    int getAreaWidth() {
                        return this.width + this.margin;
                    }
                };
                if (itemView.getVisibility() == 0) {
                    if (itemView instanceof TextView) {
                        textViewCount++;
                        Paint paint = ((TextView) itemView).getPaint();
                        LayoutParams lParam = itemView.getLayoutParams();
                        viewDimen[i].width = (((int) paint.measureText(((TextView) itemView).getText().toString())) + itemView.getPaddingLeft()) + itemView.getPaddingRight();
                        viewDimen[i].margin = 0;
                    } else {
                        itemView.measure(0, 0);
                        adjustWidth -= itemView.getMeasuredWidth();
                    }
                }
            }
            if (textViewCount == 0) {
                return null;
            }
            info.maxWidth = adjustWidth / textViewCount;
            i = 0;
            while (i < viewCount) {
                itemView = this.mToolbarGroup.getChildAt(i);
                if (itemView.getVisibility() == 0 && (itemView instanceof TextView)) {
                    if (viewDimen[i].getAreaWidth() > info.maxWidth) {
                        int j = 0;
                        while (j < info.viewCount && info.measuredWidth[j] <= viewDimen[i].getAreaWidth()) {
                            j++;
                        }
                        for (int k = info.viewCount; k > j; k--) {
                            info.adjustView[k] = info.adjustView[k - 1];
                            info.measuredWidth[k] = info.measuredWidth[k - 1];
                        }
                        info.adjustView[j] = (TextView) itemView;
                        info.measuredWidth[j] = viewDimen[i].width;
                        info.marginWidth[j] = viewDimen[i].margin;
                        info.viewCount++;
                    } else {
                        ((TextView) itemView).setMaxWidth(viewDimen[i].width);
                        info.extraWidth += info.maxWidth - viewDimen[i].getAreaWidth();
                    }
                }
                i++;
            }
            return info;
        }

        private void changeAllItemHeight(TextView textView, boolean isDuplicateLine) {
            int adjustLineHeight = this.originViewHeight;
            int itemCnt = this.mToolbarGroup.getChildCount();
            if (textView != null && isDuplicateLine) {
                adjustLineHeight += textView.getLineHeight();
                log(this.TAG, " origin = " + this.originViewHeight + " lineHeight = " + textView.getLineHeight());
            }
            for (int i = 0; i < itemCnt; i++) {
                this.mToolbarGroup.getChildAt(i).setMinimumHeight(adjustLineHeight);
            }
        }

        protected boolean needChange(int usableWidth, int usableHeight) {
            this.mToolbarGroup.measure(0, 0);
            int requiredWidth = this.mToolbarGroup.getMeasuredWidth();
            int requiredHeight = this.mToolbarGroup.getMeasuredHeight();
            this.availableAreaWidth = usableWidth;
            this.availableAreaHeight = usableHeight;
            if (!this.isViewChanged) {
                this.originViewWidth = requiredWidth;
                this.originViewHeight = requiredHeight;
                this.isViewChanged = true;
            }
            return true;
        }

        protected int getProperWidth(TextView view, int maxWidth) {
            String str = view.getText().toString();
            Paint paint = view.getPaint();
            int properPos = 0;
            if (str.length() <= 1 || ((int) paint.measureText(str)) >= maxWidth * 2) {
                return maxWidth;
            }
            int i = 0;
            while (i < str.length()) {
                if (((int) paint.measureText(str.substring(0, i + 1))) >= ((int) paint.measureText(str.substring(i + 1, str.length())))) {
                    properPos = i + 1;
                    break;
                }
                i++;
            }
            if (i >= str.length() - 1) {
                properPos = str.length() - 1;
            }
            int firstLineLen = (int) paint.measureText(str.substring(0, properPos));
            int secondLineLen = (int) paint.measureText(str.substring(properPos, str.length()));
            int max = firstLineLen > secondLineLen ? firstLineLen : secondLineLen;
            int padding = view.getPaddingLeft() + view.getPaddingRight();
            LayoutParams vParam = view.getLayoutParams();
            if (vParam instanceof LayoutParams) {
                LayoutParams mParam = vParam;
            }
            return (max + padding) + 0;
        }

        private void log(String tag, String str) {
            if (this.Debug) {
                Log.d(tag, str);
            }
        }
    }

    public FtPopupToolbar(View hostView) {
        this.mHostView = hostView;
        if (this.mHostView instanceof TextView) {
            this.mIsTextView = true;
        }
        this.mContext = this.mHostView.getContext();
        this.mRes = this.mContext.getResources();
        this.mMinPaddinTop = this.mRes.getDimensionPixelSize(R.dimen.min_padding_top);
        WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        this.mScreenWidth = size.x;
        this.mRegulator = new TextViewToolbarRegulator(this.mContext);
        init();
    }

    private void setLayoutDirectionFromHostView() {
        if (this.mHostView != null && this.mPopupToolbarView != null) {
            this.mPopupToolbarView.setLayoutDirection(this.mHostView.getLayoutDirection());
        }
    }

    protected boolean isSupportHandlers() {
        LayoutParams params = this.mHostView.getRootView().getLayoutParams();
        if (!(params instanceof WindowManager.LayoutParams)) {
            return false;
        }
        WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) params;
        if (windowParams.type >= 1000) {
            return windowParams.type > WindowManager.LayoutParams.LAST_SUB_WINDOW;
        } else {
            return true;
        }
    }

    private void init() {
        this.mLayoutInflater = LayoutInflater.from(this.mContext);
        this.mPopupToolbarView = this.mLayoutInflater.inflate((int) R.layout.vigour_popup_toolbar_layout, null);
        this.mToolBarViewGroup = new PopupToolLayout(this, this.mContext);
        this.mArrowImg = (ImageView) this.mPopupToolbarView.findViewById(R.id.toolbar_position_arrow);
        this.mPopupWindow = new PopupWindow(this.mPopupToolbarView, -2, -2);
        this.mPopupWindow.setBackgroundDrawable(new ColorDrawable(0));
        this.mPopupWindow.setOutsideTouchable(true);
        this.mPopupWindow.setClippingEnabled(false);
        this.mPopupWindow.setInputMethodMode(2);
        this.mPopupWindow.setWindowLayoutType(1002);
        this.mHorizontalLayout = (LinearLayout) this.mPopupToolbarView.findViewById(R.id.horizontal_scroll_view);
        this.mHorizontalLayout.addView(this.mToolBarViewGroup);
        TypedArray a = this.mContext.obtainStyledAttributes(null, R.styleable.TextViewToolbar, R.attr.textViewToolbarStyle, R.style.Vigour_TextViewToolbar);
        this.ID_LEFT = a.getResourceId(0, this.ID_LEFT);
        this.ID_MIDDLE = a.getResourceId(2, this.ID_MIDDLE);
        this.ID_RIGHT = a.getResourceId(1, this.ID_RIGHT);
        this.ID_WHOLE = a.getResourceId(3, this.ID_WHOLE);
        this.ID_ARROW = a.getResourceId(5, this.ID_ARROW);
        this.ITEM_STYLE = a.getResourceId(6, this.ITEM_STYLE);
        a.recycle();
        this.mArrowImg.setImageResource(this.ID_ARROW);
        this.mArrowImg.measure(0, 0);
        this.mArrowHeight = this.mArrowImg.getMeasuredHeight();
        this.mArrowWidth = this.mArrowImg.getMeasuredWidth();
    }

    public boolean isShowing() {
        return this.mPopupWindow.isShowing();
    }

    public void hide() {
        this.mPopupWindow.dismiss();
    }

    private boolean updateToolbarItemsBg() {
        int i;
        int childNum = this.mToolBarViewGroup.getChildCount();
        if (childNum < 1) {
            Throwable th = new Throwable("no item find in popupToolbar");
        }
        int[] visibilityItems = new int[10];
        int visibilityItemsNum = 0;
        for (i = 0; i < childNum; i++) {
            if (this.mToolBarViewGroup.getChildAt(i).getVisibility() == 0) {
                visibilityItems[visibilityItemsNum] = i;
                visibilityItemsNum++;
            }
        }
        if (visibilityItemsNum == 0) {
            Log.w(TAG, "There is no visible item need to show");
            return false;
        }
        if (1 == visibilityItemsNum) {
            this.mToolBarViewGroup.getChildAt(visibilityItems[0]).setBackgroundResource(this.ID_WHOLE);
        } else {
            Boolean isRtl = Boolean.valueOf(isRtl());
            this.mToolBarViewGroup.getChildAt(visibilityItems[0]).setBackgroundResource(isRtl.booleanValue() ? this.ID_RIGHT : this.ID_LEFT);
            this.mToolBarViewGroup.getChildAt(visibilityItems[visibilityItemsNum - 1]).setBackgroundResource(isRtl.booleanValue() ? this.ID_LEFT : this.ID_RIGHT);
            if (visibilityItemsNum > 2) {
                for (i = 1; i < visibilityItemsNum - 1; i++) {
                    this.mToolBarViewGroup.getChildAt(visibilityItems[i]).setBackgroundResource(this.ID_MIDDLE);
                }
            }
        }
        if (this.mItemBackgroundPadding == 0) {
            Rect padding = new Rect();
            this.mToolBarViewGroup.getChildAt(visibilityItems[0]).getBackground().getPadding(padding);
            this.mItemBackgroundPadding = padding.left;
        }
        return true;
    }

    private void updateViewSize() {
        int[] dimen = new int[2];
        getAvailableDimen(dimen);
        this.mRegulator.adjust(this.mToolBarViewGroup, dimen);
    }

    public void show() {
        if (isSupportHandlers()) {
            Log.e(TAG, "the hostView window type is not support PopupToolbar");
        }
        int[] location = new int[2];
        this.mHostView.getLocationInWindow(location);
        this.mHostViewLocX = location[0];
        this.mHostViewLocY = location[1];
        if (!isShowing()) {
            setLayoutDirectionFromHostView();
            if (updateToolbarItemsBg()) {
                updateViewSize();
                calculatePosition();
                updateArrowPosition();
                Log.d(TAG, "x:" + this.mPopupWindowLocX + "y:" + this.mPopupWindowLocY);
                this.mPopupWindow.showAtLocation(this.mHostView, 0, this.mPopupWindowLocX, this.mPopupWindowLocY);
            }
        }
    }

    protected void updateArrowPosition() {
        if (this.mArrowLocX < 0) {
            if (isRtl()) {
                if (this.mIsTextView) {
                    this.mArrowLocX = (this.mPopupWindowLocX + this.mToolBarViewGroup.getMeasuredWidth()) - ((int) (this.mArrowXPos + ((float) (this.mArrowWidth / 2))));
                } else {
                    this.mArrowLocX = (this.mPopupWindowLocX + this.mToolBarViewGroup.getMeasuredWidth()) - ((this.mHostViewLocX + (this.mHostView.getMeasuredWidth() / 2)) + (this.mArrowWidth / 2));
                }
            } else if (this.mIsTextView) {
                this.mArrowLocX = (int) ((this.mArrowXPos - ((float) this.mPopupWindowLocX)) - ((float) (this.mArrowWidth / 2)));
            } else {
                this.mArrowLocX = (this.mHostViewLocX - this.mPopupWindowLocX) + ((this.mHostView.getMeasuredWidth() / 2) - (this.mArrowWidth / 2));
            }
        }
        if (this.mArrowLocX < this.mItemBackgroundPadding) {
            this.mArrowLocX = this.mItemBackgroundPadding + 2;
        } else if (this.mArrowLocX > ((this.mToolBarViewGroup.getMeasuredWidth() - this.mItemBackgroundPadding) - this.mArrowWidth) - 2) {
            this.mArrowLocX = ((this.mToolBarViewGroup.getMeasuredWidth() - this.mItemBackgroundPadding) - this.mArrowWidth) - 2;
        }
        if (this.isShouldShowBelow) {
            this.mArrowImg.setPaddingRelative(0, 0, this.mArrowLocX, 0);
            this.mHorizontalLayout.setPadding(0, this.mArrowImg.getMeasuredHeight(), 0, 0);
        } else {
            this.mArrowImg.setPaddingRelative(this.mArrowLocX, this.mToolBarViewGroup.getMeasuredHeight(), 0, 0);
        }
        this.mArrowLocX = -1;
        if (this.mArrowImg.getVisibility() != 0) {
            this.mArrowImg.setVisibility(0);
        }
    }

    public void setFocusable(boolean focusable) {
        this.mPopupWindow.setFocusable(focusable);
    }

    public boolean isOutsideTouchable() {
        return this.mPopupWindow.isOutsideTouchable();
    }

    public void setOutsideTouchable(boolean touchable) {
        this.mPopupWindow.setOutsideTouchable(touchable);
    }

    public void setArrowPosition(int poisitionX) {
        this.mArrowLocX = poisitionX;
    }

    public void setPopupToolbarPosition(int x, int y) {
    }

    protected void calculatePosition() {
        isShouldShowBelowHost(this.mHostViewLocY);
        this.mArrowXPos = -1.0f;
        if (!this.mIsTextView || getTextViewLayout((TextView) this.mHostView) == null) {
            this.mToolBarViewGroup.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
            this.mPopupWindowLocX = (this.mHostViewLocX + (this.mHostView.getWidth() / 2)) - (this.mToolBarViewGroup.getMeasuredWidth() / 2);
        } else {
            TextView textView = this.mHostView;
            Layout layout = getTextViewLayout((TextView) this.mHostView);
            float start = Float.MAX_VALUE;
            float end = Float.MIN_VALUE;
            int lineCount = layout.getLineCount();
            for (int i = 0; i < lineCount; i++) {
                start = Math.min(start, layout.getLineLeft(i));
                end = Math.max(end, layout.getLineRight(i));
            }
            this.mArrowXPos = (((float) this.mHostViewLocX) + (((Math.max(start, 0.0f) + Math.max(end, 0.0f)) / 2.0f) - ((float) textView.getScrollX()))) + ((float) textView.getTotalPaddingLeft());
            this.mPopupWindowLocX = (int) (this.mArrowXPos - ((float) (this.mToolBarViewGroup.getMeasuredWidth() / 2)));
        }
        if ((this.mPopupWindowLocX + this.mToolBarViewGroup.getMeasuredWidth()) + 30 > this.mScreenWidth) {
            this.mPopupWindowLocX = (this.mScreenWidth - 30) - this.mToolBarViewGroup.getMeasuredWidth();
        } else if (this.mPopupWindowLocX < 30) {
            this.mPopupWindowLocX = 30;
        }
        if (this.isShouldShowBelow) {
            this.mPopupWindowLocY = this.mHostViewLocY + this.mHostView.getHeight();
            Log.d(TAG, "2");
            return;
        }
        this.mPopupWindowLocY = (this.mHostViewLocY - this.mToolBarViewGroup.getMeasuredHeight()) - this.mArrowHeight;
        Log.d(TAG, "1:" + this.mHostViewLocY + "2:" + this.mToolBarViewGroup.getMeasuredHeight());
    }

    private Layout getTextViewLayout(TextView textView) {
        if (textView == null) {
            return null;
        }
        Layout layout = textView.getLayout();
        if (layout == null) {
            try {
                Method m = textView.getClass().getDeclaredMethod("assumeLayout", new Class[0]);
                m.setAccessible(true);
                m.invoke(textView, null);
            } catch (Exception e) {
                Log.d(TAG, "getTextViewLayout", e);
            }
            layout = textView.getLayout();
        }
        return layout;
    }

    private void isShouldShowBelowHost(int ScreenY) {
        Log.d(TAG, "mMinPaddinTop:" + this.mMinPaddinTop + "screenY:" + ScreenY);
        if (this.mMinPaddinTop > ScreenY) {
            this.mArrowImg.setRotation(180.0f);
            this.isShouldShowBelow = true;
            return;
        }
        this.isShouldShowBelow = false;
    }

    public void initItemTextView(int id, int textResId) {
        TextView textView = new TextView(this.mContext, null, this.ITEM_STYLE, R.style.Widget_Vigour_ToolbarItem);
        textView.setGravity(17);
        textView.setMaxLines(2);
        textView.setText(textResId);
        textView.setEllipsize(TruncateAt.END);
        initView(id, textView);
    }

    public void initItemImageView(int id, int imgResId) {
        ImageView imageView = new ImageView(this.mContext, null, this.ITEM_STYLE, R.style.Widget_Vigour_ToolbarItem);
        imageView.setImageResource(imgResId);
        imageView.setScaleType(ScaleType.CENTER);
        initView(id, imageView);
    }

    private void initView(int id, View view) {
        view.setId(id);
        view.setOnClickListener(this);
        this.mToolBarViewGroup.addView(view);
    }

    public void setItemVisibilityById(int id, int visibility) {
        this.mToolBarViewGroup.findViewById(id).setVisibility(visibility);
    }

    public void hideAllItems() {
        for (int i = 0; i < this.mToolBarViewGroup.getChildCount(); i++) {
            this.mToolBarViewGroup.getChildAt(i).setVisibility(8);
        }
    }

    public void onClick(View v) {
        if (this.mOnItemClickListener != null) {
            this.mOnItemClickListener.onItemClick(v);
        }
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mOnItemClickListener = l;
    }

    private boolean isRtl() {
        boolean z = true;
        if (this.mHostView == null) {
            return false;
        }
        if (this.mHostView.getLayoutDirection() != 1) {
            z = false;
        }
        return z;
    }

    private void getAvailableDimen(int[] dimen) {
        int leftSpaceLen = this.mContext.getResources().getDimensionPixelOffset(R.dimen.texttoolbar_leftSpace_width);
        int rightSpaceLen = this.mContext.getResources().getDimensionPixelOffset(R.dimen.texttoolbar_rightSpace_width);
        Rect rect = new Rect();
        this.mToolBarViewGroup.getRootView().getWindowVisibleDisplayFrame(rect);
        dimen[0] = ((rect.right - rect.left) - leftSpaceLen) - rightSpaceLen;
        dimen[1] = rect.bottom - rect.top;
    }
}
