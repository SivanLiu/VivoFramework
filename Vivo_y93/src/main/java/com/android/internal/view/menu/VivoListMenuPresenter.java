package com.android.internal.view.menu;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import com.android.internal.view.menu.MenuView.ItemView;
import com.vivo.internal.R;
import java.lang.reflect.Field;
import java.util.ArrayList;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoListMenuPresenter extends IconMenuPresenter {
    private int mMaxItems = -1;
    private VivoListMenuItemView mMoreView;

    public VivoListMenuPresenter(Context context) {
        super(context);
        try {
            Field field = BaseMenuPresenter.class.getDeclaredField("mMenuLayoutRes");
            field.setAccessible(true);
            field.set(this, Integer.valueOf(R.layout.vigour_list_menu_layout));
            field = BaseMenuPresenter.class.getDeclaredField("mItemLayoutRes");
            field.setAccessible(true);
            field.set(this, Integer.valueOf(R.layout.vigour_list_menu_item_layout));
            this.mSystemContext = context;
            this.mSystemInflater = LayoutInflater.from(context);
        } catch (Exception e) {
            Log.i("VivoListMenuPresenter", e.getMessage());
        }
    }

    public void initForMenu(Context context, MenuBuilder menu) {
        super.initForMenu(context, menu);
    }

    public void bindItemView(MenuItemImpl item, ItemView itemView) {
        VivoListMenuItemView view = (VivoListMenuItemView) itemView;
        view.setItemData(item);
        view.initialize(item.getTitleForItemView(view), item.getIcon());
        view.setVisibility(item.isVisible() ? 0 : 8);
        view.setEnabled(item.isEnabled());
    }

    public boolean shouldIncludeItem(int childIndex, MenuItemImpl item) {
        boolean fits = (this.mMenu.getNonActionItems().size() != this.mMaxItems || childIndex >= this.mMaxItems) ? childIndex < this.mMaxItems + -1 : true;
        return fits ? item.isActionButton() ^ 1 : false;
    }

    protected void addItemView(View itemView, int childIndex) {
        ((VivoListMenuItemView) itemView).setItemInvoker(this.mMenuView);
        ViewGroup currentParent = (ViewGroup) itemView.getParent();
        if (currentParent != null) {
            currentParent.removeView(itemView);
        }
        ((ViewGroup) this.mMenuView).addView(itemView, childIndex);
    }

    public void updateMenuView(boolean cleared) {
        ViewParent menuView = this.mMenuView;
        if (this.mMaxItems < 0) {
            this.mMaxItems = menuView.getMaxItems();
        }
        ArrayList<MenuItemImpl> itemsToShow = this.mMenu.getNonActionItems();
        boolean needsMore = itemsToShow.size() > this.mMaxItems;
        internalUpdateMenuView(cleared);
        if (needsMore && (this.mMoreView == null || this.mMoreView.getParent() != menuView)) {
            if (this.mMoreView == null) {
                this.mMoreView = menuView.createMoreItemView();
            } else {
                menuView.initMoreItemView(this.mMoreView);
            }
            menuView.addView(this.mMoreView);
        }
        if (!(needsMore || this.mMoreView == null)) {
            this.mMoreView.setOnClickListener(null);
            this.mMoreView.setItemInvoker((VivoListMenuView) this.mMenuView);
        }
        menuView.setNumActualItemsShown(needsMore ? this.mMaxItems - 1 : itemsToShow.size());
    }

    private void internalUpdateMenuView(boolean cleared) {
        ViewGroup parent = this.mMenuView;
        if (parent != null) {
            int childIndex = 0;
            if (this.mMenu != null) {
                this.mMenu.flagActionItems();
                ArrayList<MenuItemImpl> visibleItems = this.mMenu.getVisibleItems();
                int itemCount = visibleItems.size();
                for (int i = 0; i < itemCount; i++) {
                    MenuItemImpl item = (MenuItemImpl) visibleItems.get(i);
                    if (shouldIncludeItem(childIndex, item)) {
                        MenuItemImpl oldItem;
                        View convertView = parent.getChildAt(childIndex);
                        if (convertView instanceof ItemView) {
                            oldItem = ((ItemView) convertView).getItemData();
                        } else {
                            oldItem = null;
                        }
                        View itemView = getItemView(item, convertView, parent);
                        if (item != oldItem) {
                            itemView.setPressed(false);
                        }
                        if (itemView != convertView) {
                            addItemView(itemView, childIndex);
                        }
                        childIndex++;
                    }
                }
            }
            while (childIndex < parent.getChildCount()) {
                if (!filterLeftoverView(parent, childIndex)) {
                    childIndex++;
                }
            }
        }
    }

    public int getNumActualItemsShown() {
        return ((VivoListMenuView) this.mMenuView).getNumActualItemsShown();
    }
}
