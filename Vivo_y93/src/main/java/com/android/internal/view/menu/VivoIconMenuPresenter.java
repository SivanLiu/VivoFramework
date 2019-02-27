package com.android.internal.view.menu;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.view.menu.MenuPresenter.Callback;
import com.android.internal.view.menu.MenuView.ItemView;
import com.vivo.internal.R;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoIconMenuPresenter extends IconMenuPresenter {
    private final boolean DEBUG = true;
    private final String TAG = "VivoIconMenuPresenter";
    private Callback mCallback = null;
    private int mMaxItems = -1;
    private VivoIconMenuItemView mMoreView = null;
    private VivoIconMenuDialogHelper mOpenSubMenu = null;
    int mOpenSubMenuId;

    private class SubMenuPresenterCallback implements Callback {
        /* synthetic */ SubMenuPresenterCallback(VivoIconMenuPresenter this$0, SubMenuPresenterCallback -this1) {
            this();
        }

        private SubMenuPresenterCallback() {
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            VivoIconMenuPresenter.this.mOpenSubMenuId = 0;
            if (VivoIconMenuPresenter.this.mOpenSubMenu != null) {
                VivoIconMenuPresenter.this.mOpenSubMenu.dismiss();
                VivoIconMenuPresenter.this.mOpenSubMenu = null;
            }
        }

        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            if (subMenu != null) {
                VivoIconMenuPresenter.this.mOpenSubMenuId = ((SubMenuBuilder) subMenu).getItem().getItemId();
            }
            return false;
        }
    }

    public VivoIconMenuPresenter(Context context) {
        super(context);
        setFieldInt("mMenuLayoutRes", R.layout.vigour_icon_menu_layout);
        setFieldInt("mItemLayoutRes", R.layout.vigour_icon_menu_item_layout);
        this.mSystemContext = context;
        this.mSystemInflater = LayoutInflater.from(this.mSystemContext);
    }

    private void setFieldInt(String key, int value) {
        try {
            Field field = BaseMenuPresenter.class.getDeclaredField(key);
            field.setAccessible(true);
            field.setInt(this, value);
        } catch (Exception e) {
            Log.e("VivoIconMenuPresenter", "set private Filed[" + key + " : " + value + "] failed");
            logException(e);
        }
    }

    public void bindItemView(MenuItemImpl item, ItemView itemView) {
        ((VivoIconMenuItemView) itemView).initialize(item);
        Log.d("VivoIconMenuPresenter", "bindItemView " + item.getTitle());
    }

    public boolean shouldIncludeItem(int childIndex, MenuItemImpl item) {
        boolean fits = (this.mMenu.getNonActionItems().size() != this.mMaxItems || childIndex >= this.mMaxItems) ? childIndex < this.mMaxItems + -1 : true;
        return fits ? item.isActionButton() ^ 1 : false;
    }

    public void updateMenuView(boolean cleared) {
        VivoIconMenuView menuView = this.mMenuView;
        if (menuView == null) {
            Log.e("VivoIconMenuPresenter", "updateMenuView exit for mMenuView[null]");
            return;
        }
        List<MenuItemImpl> itemsToShow = this.mMenu.getNonActionItems();
        if (this.mMaxItems < 0) {
            this.mMaxItems = menuView.getMaxItems();
        }
        boolean needsMore = itemsToShow.size() > this.mMaxItems;
        updateMenuViewInternal(cleared);
        menuView.setNumActualItemsShown(needsMore ? this.mMaxItems - 1 : itemsToShow.size());
    }

    private void updateMenuViewInternal(boolean cleared) {
        ViewGroup parent = (ViewGroup) ((View) this.mMenuView).findViewById(R.id.content);
        if (parent == null) {
            Log.e("VivoIconMenuPresenter", "updateMenuViewInternal not found ViewGroup id[content]");
            return;
        }
        int childIndex = 0;
        if (this.mMenu != null) {
            this.mMenu.flagActionItems();
            List<MenuItemImpl> visibleItems = this.mMenu.getVisibleItems();
            int itemCount = visibleItems.size();
            Log.d("VivoIconMenuPresenter", "we will add [ " + itemCount + " ] menu items");
            for (int i = 0; i < itemCount; i++) {
                MenuItemImpl item = (MenuItemImpl) visibleItems.get(i);
                if (shouldIncludeItem(childIndex, item)) {
                    View convertView = parent.getChildAt(childIndex);
                    MenuItemImpl oldItem = convertView instanceof ItemView ? ((ItemView) convertView).getItemData() : null;
                    View itemView = getItemView(item, convertView, parent);
                    if (item != oldItem) {
                        itemView.setPressed(false);
                        itemView.jumpDrawablesToCurrentState();
                    }
                    if (itemView != convertView) {
                        addItemView(itemView, childIndex);
                        Log.d("VivoIconMenuPresenter", "add [ " + childIndex + " ]  menu item");
                    }
                    childIndex++;
                }
            }
        }
        while (childIndex < parent.getChildCount()) {
            if (!filterLeftoverView(parent, childIndex)) {
                Log.d("VivoIconMenuPresenter", "filter menu item index[ " + childIndex + " ]");
                childIndex++;
            }
        }
    }

    private void logException(Exception e) {
        if (e != null) {
            PrintWriter pr = new PrintWriter(new StringWriter());
            e.printStackTrace(pr);
            Log.e("VivoIconMenuPresenter", "\n\r" + pr.toString() + "\n\r");
        }
    }

    protected void addItemView(View itemView, int childIndex) {
        ((VivoIconMenuItemView) itemView).setItemInvoker(this.mMenuView);
        ViewGroup currentParent = (ViewGroup) itemView.getParent();
        if (currentParent != null) {
            currentParent.removeView(itemView);
        }
        ViewGroup content = (ViewGroup) ((ViewGroup) this.mMenuView).findViewById(R.id.content);
        if (content == null) {
            throw new RuntimeException("addItemView not find View with id [content]");
        }
        content.addView(itemView, childIndex);
        Log.d("VivoIconMenuPresenter", "addView [ " + childIndex + " ]");
    }

    public void setCallback(Callback b) {
        this.mCallback = b;
    }

    public Callback getCallback() {
        return this.mCallback;
    }

    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        if (!subMenu.hasVisibleItems()) {
            return false;
        }
        VivoIconMenuDialogHelper helper = new VivoIconMenuDialogHelper(subMenu);
        helper.setPresenterCallback(new SubMenuPresenterCallback(this, null));
        helper.show(null);
        this.mOpenSubMenu = helper;
        this.mOpenSubMenuId = subMenu.getItem().getItemId();
        if (this.mCallback != null) {
            this.mCallback.onOpenSubMenu(subMenu);
        }
        Log.d("VivoIconMenuPresenter", "onSubMenuSelected invoked");
        return true;
    }

    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        if (this.mCallback != null) {
            this.mCallback.onCloseMenu(menu, allMenusAreClosing);
        }
        Log.d("VivoIconMenuPresenter", "onCloseMenu invoked");
    }

    public int getNumActualItemsShown() {
        return ((VivoIconMenuView) this.mMenuView).getNumActualItemsShown();
    }
}
