package com.android.internal.view.menu;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.view.menu.MenuBuilder.ItemInvoker;
import com.vivo.internal.R;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoIconMenuView extends LinearLayout implements ItemInvoker, MenuView {
    private final boolean DEBUG;
    private final String TAG;
    private int mMaxItems;
    private MenuBuilder mMenu;
    private int mNumActualItemsShown;

    public VivoIconMenuView(Context context) {
        this(context, null);
    }

    public VivoIconMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.TAG = "VivoIconMenuView";
        this.DEBUG = true;
        this.mMaxItems = 9;
    }

    public void initialize(MenuBuilder menu) {
        this.mMenu = menu;
        setTitle(menu);
        Log.d("VivoIconMenuView", "initialize new VivoIconMenuView");
    }

    private void setTitle(MenuBuilder menu) {
        View headerView = findViewById(R.id.head);
        if (menu.getHeaderView() == null) {
            boolean showHeader = false;
            TextView titleView = (TextView) headerView.findViewById(R.id.title);
            ImageView iconView = (ImageView) headerView.findViewById(R.id.icon);
            Drawable icon = menu.getHeaderIcon();
            if (!(icon == null || iconView == null)) {
                iconView.setVisibility(0);
                iconView.setImageDrawable(icon);
                showHeader = true;
                Log.d("VivoIconMenuView", "setTitle icon");
            }
            CharSequence titleStr = menu.getHeaderTitle();
            if (!(titleStr == null || titleView == null)) {
                titleView.setVisibility(0);
                titleView.setText(titleStr);
                if (showHeader) {
                    int padding = menu.getHeaderIcon().getIntrinsicWidth();
                    titleView.setPaddingRelative(padding, 0, padding, 0);
                }
                showHeader = true;
                Log.d("VivoIconMenuView", "setTitle [ " + titleStr + " ]");
            }
            if (showHeader) {
                headerView.setVisibility(0);
                return;
            }
            return;
        }
        headerView.setVisibility(8);
        addView(menu.getHeaderView(), 0, (LayoutParams) new LinearLayout.LayoutParams(-1, -2));
    }

    public int getWindowAnimations() {
        return R.style.Animation_Vigour_Menu;
    }

    public boolean invokeItem(MenuItemImpl item) {
        Log.d("VivoIconMenuView", "invokeItem in VivoIconMenuView");
        return this.mMenu.performItemAction(item, 0);
    }

    public VivoIconMenuItemView createMoreItemView() {
        VivoIconMenuItemView itemView = (VivoIconMenuItemView) LayoutInflater.from(getContext()).inflate((int) R.layout.vigour_icon_menu_item_layout, (ViewGroup) this, false);
        itemView.initialize(getContext().getResources().getText(com.android.internal.R.string.more_item_label), null);
        itemView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                VivoIconMenuView.this.mMenu.changeMenuMode();
            }
        });
        return itemView;
    }

    int getMaxItems() {
        return this.mMaxItems;
    }

    int getNumActualItemsShown() {
        return this.mNumActualItemsShown;
    }

    void setNumActualItemsShown(int count) {
        this.mNumActualItemsShown = count;
    }
}
