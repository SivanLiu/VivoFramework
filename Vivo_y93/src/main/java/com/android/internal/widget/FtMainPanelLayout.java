package com.android.internal.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.util.Preconditions;
import com.vivo.internal.R;
import java.util.LinkedList;
import vivo.util.VivoThemeUtil;
import vivo.util.VivoThemeUtil.ThemeType;

public class FtMainPanelLayout extends LinearLayout {
    private int ID_ARROW_ABOVE;
    private int ID_ARROW_BELOW;
    private int ID_BUTTON_APPEARANCE;
    private int ID_LEFT;
    private int ID_MIDDLE;
    private int ID_RIGHT;
    private int ID_WHOLE;
    private int INIT_TOOLBAR_CONTENT_HEIGHT;
    private final int MAX_ITEM_NUME;
    private final String TAG;
    private int TOOLBAR_CONTENT_HEIGHT;
    private Drawable mArrowAboveDrawable;
    private Drawable mArrowBelowDrawable;
    private int mArrowHeight;
    private int mArrowWidth;
    private Context mContext;
    private boolean mIsAbove;
    private OnClickListener mMenuItemButtonOnClickListener;
    private int mPointTo;
    private int mScreenWidth;

    public FtMainPanelLayout(Context context) {
        this(context, null);
    }

    public FtMainPanelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FtMainPanelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FtMainPanelLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.TAG = "FtMainPanelLayout";
        this.ID_LEFT = R.drawable.vigour_text_toolbar_left_light;
        this.ID_RIGHT = R.drawable.vigour_text_toolbar_right_light;
        this.ID_WHOLE = R.drawable.vigour_text_toolbar_single_light;
        this.ID_MIDDLE = R.drawable.vigour_text_toolbar_center_light;
        this.ID_ARROW_ABOVE = R.drawable.vigour_text_toolbar_position_arrow_above_light;
        this.ID_ARROW_BELOW = R.drawable.vigour_text_toolbar_position_arrow_below_light;
        this.ID_BUTTON_APPEARANCE = R.style.Widget_Vigour_ToolbarItem;
        this.mMenuItemButtonOnClickListener = null;
        this.mContext = null;
        this.TOOLBAR_CONTENT_HEIGHT = 0;
        this.INIT_TOOLBAR_CONTENT_HEIGHT = 132;
        this.mArrowAboveDrawable = null;
        this.mArrowBelowDrawable = null;
        this.mArrowWidth = 0;
        this.mArrowHeight = 0;
        this.mIsAbove = false;
        this.mPointTo = 0;
        this.mScreenWidth = 0;
        this.MAX_ITEM_NUME = 5;
        this.mScreenWidth = ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getWidth();
        this.mContext = new ContextThemeWrapper(context, VivoThemeUtil.getSystemThemeStyle(ThemeType.SYSTEM_DEFAULT));
        setGravity(16);
        setWillNotDraw(false);
        TypedArray a = this.mContext.obtainStyledAttributes(null, R.styleable.TextViewToolbar, R.attr.textViewToolbarStyle, R.style.Vigour_TextViewToolbar);
        this.ID_LEFT = a.getResourceId(0, this.ID_LEFT);
        this.ID_MIDDLE = a.getResourceId(2, this.ID_MIDDLE);
        this.ID_RIGHT = a.getResourceId(1, this.ID_RIGHT);
        this.ID_WHOLE = a.getResourceId(3, this.ID_WHOLE);
        this.ID_ARROW_ABOVE = a.getResourceId(4, this.ID_ARROW_ABOVE);
        this.ID_ARROW_BELOW = a.getResourceId(5, this.ID_ARROW_BELOW);
        this.ID_BUTTON_APPEARANCE = a.getResourceId(6, this.ID_BUTTON_APPEARANCE);
        a.recycle();
        this.mArrowAboveDrawable = getResources().getDrawable(this.ID_ARROW_ABOVE, null);
        this.mArrowBelowDrawable = getResources().getDrawable(this.ID_ARROW_BELOW, null);
        this.mArrowWidth = this.mArrowAboveDrawable.getIntrinsicWidth();
        this.mArrowHeight = this.mArrowAboveDrawable.getIntrinsicHeight();
        this.INIT_TOOLBAR_CONTENT_HEIGHT = this.mContext.getResources().getDimensionPixelSize(R.dimen.popup_toolbar_item_height);
    }

    public void setMenuItemOnClickListener(OnClickListener l) {
        this.mMenuItemButtonOnClickListener = l;
    }

    public void updateFloatingArrow(boolean isAbove, int pointTo) {
        this.mIsAbove = isAbove;
        this.mPointTo = pointTo;
        invalidate();
    }

    public LinkedList<MenuItem> layoutMenuItems(LinkedList<MenuItem> MenuItems, int suggestedWidth, int overflowButtonWidth) {
        Preconditions.checkNotNull(MenuItems);
        LinkedList<MenuItem> remainingMenuItems = MenuItems;
        int availableWidth = suggestedWidth;
        int itemSize = MenuItems.size();
        if (itemSize > 5) {
            availableWidth = suggestedWidth - overflowButtonWidth;
        }
        if (itemSize == 0) {
            MenuItems.clear();
            return MenuItems;
        }
        LayoutParams params;
        itemSize = Math.min(itemSize, 5);
        int maxAvgWidth = availableWidth / itemSize;
        Log.d("FtMainPanelLayout", "maxAvgWidth = " + maxAvgWidth);
        int remainWidth = 0;
        int overAvgWidthNum = 0;
        removeAllViews();
        boolean isFirstItem = true;
        this.TOOLBAR_CONTENT_HEIGHT = this.INIT_TOOLBAR_CONTENT_HEIGHT;
        int menuItemNum = 0;
        while (!MenuItems.isEmpty() && menuItemNum < 5) {
            MenuItem menuItem = (MenuItem) MenuItems.peek();
            View menuItemButton = createMenuItemButton(this.mContext, menuItem);
            if (itemSize == 1) {
                menuItemButton.setBackgroundResource(this.ID_WHOLE);
            } else if (isFirstItem) {
                menuItemButton.setBackgroundResource(isRtl() ? this.ID_RIGHT : this.ID_LEFT);
                menuItemButton.setPaddingRelative((int) (((double) menuItemButton.getPaddingStart()) * 1.5d), menuItemButton.getPaddingTop(), menuItemButton.getPaddingEnd(), menuItemButton.getPaddingBottom());
                isFirstItem = false;
            } else if (MenuItems.size() == 1) {
                menuItemButton.setBackgroundResource(isRtl() ? this.ID_LEFT : this.ID_RIGHT);
                menuItemButton.setPaddingRelative(menuItemButton.getPaddingStart(), menuItemButton.getPaddingTop(), (int) (((double) menuItemButton.getPaddingEnd()) * 1.5d), menuItemButton.getPaddingBottom());
            } else {
                menuItemButton.setBackgroundResource(this.ID_MIDDLE);
                menuItemButton.setPaddingRelative(menuItemButton.getPaddingStart() * 1, menuItemButton.getPaddingTop(), menuItemButton.getPaddingEnd() * 1, menuItemButton.getPaddingBottom());
            }
            menuItemButton.measure(0, 0);
            int itemMeasuredWidth = menuItemButton.getMeasuredWidth();
            int menuItemButtonWidth = Math.min(itemMeasuredWidth, maxAvgWidth);
            if (itemMeasuredWidth > maxAvgWidth) {
                overAvgWidthNum++;
            }
            setButtonTagAndClickListener(menuItemButton, menuItem);
            addView(menuItemButton);
            params = menuItemButton.getLayoutParams();
            params.height = this.TOOLBAR_CONTENT_HEIGHT;
            params.width = menuItemButtonWidth;
            remainWidth += maxAvgWidth - menuItemButtonWidth;
            menuItemButton.setLayoutParams(params);
            menuItemNum++;
            MenuItems.pop();
        }
        Log.d("FtMainPanelLayout", "remainWidth = " + remainWidth);
        Log.d("FtMainPanelLayout", "overAvgWidthNum = " + overAvgWidthNum);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (!(child == null || overAvgWidthNum == 0)) {
                int deltaWidth = child.getMeasuredWidth() - maxAvgWidth;
                if (deltaWidth > 0 && deltaWidth <= remainWidth / overAvgWidthNum) {
                    params = child.getLayoutParams();
                    params.width += deltaWidth;
                    child.setLayoutParams(params);
                }
            }
        }
        return MenuItems;
    }

    private static View createMenuItemButton(Context context, MenuItem menuItem) {
        View menuItemButton = LayoutInflater.from(context).inflate((int) com.android.internal.R.layout.floating_popup_menu_button, null);
        if (menuItem != null) {
            updateMenuItemButton(menuItemButton, menuItem);
        }
        return menuItemButton;
    }

    private static void updateMenuItemButton(View menuItemButton, MenuItem menuItem) {
        TextView buttonText = (TextView) menuItemButton.findViewById(com.android.internal.R.id.floating_toolbar_menu_item_text);
        if (TextUtils.isEmpty(menuItem.getTitle())) {
            buttonText.setVisibility(8);
        } else {
            buttonText.setVisibility(0);
            buttonText.setText(menuItem.getTitle());
        }
        ImageView buttonIcon = (ImageView) menuItemButton.findViewById(com.android.internal.R.id.floating_toolbar_menu_item_image);
        if (menuItem.getIcon() == null) {
            buttonIcon.setVisibility(8);
            if (buttonText != null) {
                buttonText.setPaddingRelative(0, 0, 0, 0);
            }
        } else {
            buttonIcon.setVisibility(0);
            buttonIcon.setImageDrawable(menuItem.getIcon());
        }
        CharSequence contentDescription = menuItem.getContentDescription();
        if (TextUtils.isEmpty(contentDescription)) {
            menuItemButton.setContentDescription(menuItem.getTitle());
        } else {
            menuItemButton.setContentDescription(contentDescription);
        }
    }

    private void setButtonTagAndClickListener(View menuItemButton, MenuItem menuItem) {
        View button = menuItemButton;
        menuItemButton.setTag(menuItem);
        Preconditions.checkNotNull(this.mMenuItemButtonOnClickListener);
        menuItemButton.setOnClickListener(this.mMenuItemButtonOnClickListener);
    }

    private boolean isIconOnlyMenuItem(MenuItem menuItem) {
        if (!TextUtils.isEmpty(menuItem.getTitle()) || menuItem.getIcon() == null) {
            return false;
        }
        return true;
    }

    private boolean isRtl() {
        if (this.mContext.getApplicationInfo().hasRtlSupport()) {
            return this.mContext.getResources().getConfiguration().getLayoutDirection() == 1;
        } else {
            return false;
        }
    }

    protected void onDraw(Canvas canvas) {
        int left;
        super.onDraw(canvas);
        int[] locationIn = new int[2];
        getLocationOnScreen(locationIn);
        int startEdge = locationIn[0];
        int endEdge = locationIn[0] + getWidth();
        if (this.mPointTo <= (this.mArrowWidth / 2) + startEdge) {
            left = this.mArrowWidth / 2;
        } else if (this.mPointTo >= endEdge - (this.mArrowWidth / 2)) {
            left = (getWidth() - this.mArrowWidth) - (this.mArrowWidth / 2);
        } else {
            left = (this.mPointTo - startEdge) - (this.mArrowWidth / 2);
        }
        int top;
        if (this.mIsAbove) {
            top = ((getHeight() / 2) - (this.TOOLBAR_CONTENT_HEIGHT / 2)) - this.mArrowHeight;
            this.mArrowAboveDrawable.setBounds(left, top, this.mArrowWidth + left, this.mArrowHeight + top);
            this.mArrowAboveDrawable.draw(canvas);
            return;
        }
        top = (getHeight() / 2) + (this.TOOLBAR_CONTENT_HEIGHT / 2);
        this.mArrowBelowDrawable.setBounds(left, top, this.mArrowWidth + left, this.mArrowHeight + top);
        this.mArrowBelowDrawable.draw(canvas);
    }
}
