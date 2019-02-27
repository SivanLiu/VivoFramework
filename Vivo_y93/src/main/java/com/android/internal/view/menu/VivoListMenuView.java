package com.android.internal.view.menu;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.internal.view.menu.MenuBuilder.ItemInvoker;
import com.vivo.internal.R;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoListMenuView extends LinearLayout implements ItemInvoker, MenuView {
    private int mMaxItems;
    private MenuBuilder mMenu;
    private int mNumActualItemsShown;
    private OnClickListener mOnClickListener;

    public VivoListMenuView(Context context) {
        this(context, null);
    }

    public VivoListMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                VivoListMenuView.this.mMenu.changeMenuMode();
            }
        };
        this.mMaxItems = 6;
    }

    public int getWindowAnimations() {
        return R.style.Animation_Vigour_Menu;
    }

    public void initialize(MenuBuilder menu) {
        this.mMenu = menu;
    }

    public boolean invokeItem(MenuItemImpl item) {
        return this.mMenu.performItemAction(item, 0);
    }

    void initMoreItemView(VivoListMenuItemView view) {
        view.initialize(getContext().getResources().getText(com.android.internal.R.string.more_item_label), null);
        view.setOnClickListener(this.mOnClickListener);
    }

    VivoListMenuItemView createMoreItemView() {
        VivoListMenuItemView itemView = (VivoListMenuItemView) LayoutInflater.from(getContext()).inflate((int) R.layout.vigour_list_menu_item_layout, (ViewGroup) this, false);
        initMoreItemView(itemView);
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
