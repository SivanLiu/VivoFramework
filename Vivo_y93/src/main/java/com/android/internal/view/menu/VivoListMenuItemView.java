package com.android.internal.view.menu;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.ViewDebug.CapturedViewProperty;
import android.widget.TextView;
import com.android.internal.view.menu.MenuBuilder.ItemInvoker;
import com.android.internal.view.menu.MenuView.ItemView;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public final class VivoListMenuItemView extends TextView implements ItemView {
    private static final int NO_ALPHA = 255;
    private AudioManager mAudioManager;
    private float mDisabledAlpha;
    private Drawable mIcon;
    private MenuItemImpl mItemData;
    private ItemInvoker mItemInvoker;
    private Rect mPositionIconAvailable;
    private Rect mPositionIconOutput;
    private String mShortcutCaption;
    private boolean mShortcutCaptionMode;
    private int mTextAppearance;
    private Context mTextAppearanceContext;

    public VivoListMenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VivoListMenuItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        this.mPositionIconAvailable = new Rect();
        this.mPositionIconOutput = new Rect();
        this.mAudioManager = null;
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
        if (this.mShortcutCaptionMode) {
            setCaptionMode(true);
        } else if (title != null) {
            setText(title);
        }
    }

    void setCaptionMode(boolean shortcut) {
        if (this.mItemData != null) {
            this.mShortcutCaptionMode = shortcut ? this.mItemData.shouldShowShortcut() : false;
            CharSequence text = this.mItemData.getTitleForItemView(this);
            if (this.mShortcutCaptionMode) {
                if (this.mShortcutCaption == null) {
                    this.mShortcutCaption = this.mItemData.getShortcutLabel();
                }
                text = this.mShortcutCaption;
            }
            setText(text);
        }
    }

    public void setIcon(Drawable icon) {
        if (icon == null || !showsIcon()) {
            setCompoundDrawables(null, null, null, null);
            setGravity(17);
            return;
        }
        this.mIcon = icon;
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        setCompoundDrawables(null, icon, null, null);
        setGravity(5);
        requestLayout();
    }

    public void setItemInvoker(ItemInvoker itemInvoker) {
        this.mItemInvoker = itemInvoker;
    }

    @CapturedViewProperty(retrieveReturn = true)
    public MenuItemImpl getItemData() {
        return this.mItemData;
    }

    public void setVisibility(int v) {
        super.setVisibility(v);
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mItemData != null && this.mIcon != null) {
            int i;
            int isInAlphaState = !this.mItemData.isEnabled() ? !isPressed() ? isFocused() ^ 1 : 1 : 0;
            Drawable drawable = this.mIcon;
            if (isInAlphaState != 0) {
                i = (int) (this.mDisabledAlpha * 255.0f);
            } else {
                i = 255;
            }
            drawable.setAlpha(i);
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        positionIcon();
    }

    private void positionIcon() {
        if (this.mIcon != null) {
            Rect tmpRect = this.mPositionIconOutput;
            getLineBounds(0, tmpRect);
            this.mPositionIconAvailable.set(0, 0, getWidth(), tmpRect.top);
            this.mIcon.setBounds(this.mPositionIconOutput);
        }
    }

    public void setCheckable(boolean checkable) {
    }

    public void setChecked(boolean checked) {
    }

    public void setShortcut(boolean showShortcut, char shortcutKey) {
        if (this.mShortcutCaptionMode) {
            this.mShortcutCaption = null;
            setCaptionMode(true);
        }
    }

    public boolean prefersCondensedTitle() {
        return true;
    }

    public boolean showsIcon() {
        return false;
    }
}
