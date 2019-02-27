package com.android.internal.view.menu;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayoutDrawable;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.ViewDebug.CapturedViewProperty;
import android.widget.TextView;
import com.android.internal.view.menu.MenuBuilder.ItemInvoker;
import com.android.internal.view.menu.MenuView.ItemView;
import com.vivo.internal.R;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public final class VivoContextMenuIconItem extends TextView implements ItemView {
    private static final int MAX_ICON_SIZE = 35;
    private static final int MIN_ICON_SIZE = 34;
    private static final int NO_ALPHA = 255;
    private AudioManager mAudioManager;
    private float mDisabledAlpha;
    private Drawable mIcon;
    private MenuItemImpl mItemData;
    private ItemInvoker mItemInvoker;
    private int mMaxIconSize;
    private int mMinIconSize;

    public VivoContextMenuIconItem(Context context) {
        this(context, null);
    }

    public VivoContextMenuIconItem(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.contextMenuIconItemStyle);
    }

    public VivoContextMenuIconItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mAudioManager = null;
        this.mMaxIconSize = (int) (context.getResources().getDisplayMetrics().density * 35.0f);
        this.mMinIconSize = (int) (context.getResources().getDisplayMetrics().density * 34.0f);
    }

    void initialize(CharSequence title, Drawable icon) {
        setClickable(true);
        setFocusable(true);
        setTitle(title);
        setIcon(icon);
    }

    public void initialize(MenuItemImpl itemData, int menuType) {
        this.mItemData = itemData;
        initialize(itemData.getTitle(), itemData.getIcon());
        setVisibility(itemData.isVisible() ? 0 : 8);
        setEnabled(itemData.isEnabled());
    }

    public void setItemData(MenuItemImpl data) {
        this.mItemData = data;
    }

    public boolean performClick() {
        if (super.performClick()) {
            return true;
        }
        if (this.mItemInvoker == null || !this.mItemInvoker.invokeItem(this.mItemData) || !isSoundEffectsEnabled()) {
            return false;
        }
        getAudioManager().playSoundEffect(0);
        return true;
    }

    private AudioManager getAudioManager() {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
        }
        return this.mAudioManager;
    }

    public void setTitle(CharSequence title) {
        if (title != null) {
            setText(title);
        }
    }

    void setCaptionMode(boolean shortcut) {
        if (this.mItemData != null) {
            setText(this.mItemData.getTitle());
        }
    }

    private Drawable wrapperLayoutDrawable(Drawable dr) {
        if (dr == null) {
            return dr;
        }
        return new LayoutDrawable(dr, this.mMinIconSize, this.mMinIconSize);
    }

    public void setIcon(Drawable icon) {
        icon = wrapperLayoutDrawable(icon);
        this.mIcon = icon;
        if (icon != null) {
            float scale;
            int width = icon.getIntrinsicWidth();
            int height = icon.getIntrinsicHeight();
            if (width > this.mMaxIconSize) {
                scale = ((float) this.mMaxIconSize) / ((float) width);
                width = this.mMaxIconSize;
                height = (int) (((float) height) * scale);
            }
            if (height > this.mMaxIconSize) {
                scale = ((float) this.mMaxIconSize) / ((float) height);
                height = this.mMaxIconSize;
                width = (int) (((float) width) * scale);
            }
            icon.setBounds(0, 0, width, height);
            setCompoundDrawables(null, icon, null, null);
            setGravity(17);
            requestLayout();
            return;
        }
        setCompoundDrawables(null, null, null, null);
        setGravity(17);
    }

    public void setItemInvoker(ItemInvoker itemInvoker) {
        this.mItemInvoker = itemInvoker;
    }

    @CapturedViewProperty(retrieveReturn = true)
    public MenuItemImpl getItemData() {
        return this.mItemData;
    }

    public void setCheckable(boolean checkable) {
    }

    public void setChecked(boolean checked) {
    }

    public boolean prefersCondensedTitle() {
        return true;
    }

    public boolean showsIcon() {
        return true;
    }

    public void setShortcut(boolean showShortcut, char shortcutKey) {
    }
}
