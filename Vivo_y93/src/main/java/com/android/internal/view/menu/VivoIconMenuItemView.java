package com.android.internal.view.menu;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.internal.view.menu.MenuBuilder.ItemInvoker;
import com.android.internal.view.menu.MenuView.ItemView;
import com.vivo.internal.R;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoIconMenuItemView extends LinearLayout implements ItemView, Checkable {
    private final boolean DEBUG;
    private final String TAG;
    private AudioManager mAudioManager;
    private CheckBox mCheckBox;
    private boolean mCheckable;
    private Context mContext;
    private CompoundButton mEffectiveButton;
    private boolean mIconVisible;
    private MenuItemImpl mItemData;
    private ItemInvoker mItemInvoker;
    private int mMaxIconSize;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private RadioButton mRadioButton;
    private TextView mShortcut;
    private boolean mSingleChoice;
    private ImageView mTitleImage;
    private TextView mTitleView;

    public interface OnCheckedChangeListener {
        void onCheckedChange(VivoIconMenuItemView vivoIconMenuItemView, boolean z);
    }

    public VivoIconMenuItemView(Context context) {
        this(context, null);
    }

    public VivoIconMenuItemView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public VivoIconMenuItemView(Context context, AttributeSet attr, int defAttrStyle) {
        super(context, attr, defAttrStyle);
        this.TAG = "VivoListMenuItemView";
        this.DEBUG = true;
        this.mTitleImage = null;
        this.mTitleView = null;
        this.mShortcut = null;
        this.mMaxIconSize = 35;
        this.mRadioButton = null;
        this.mCheckBox = null;
        this.mEffectiveButton = null;
        this.mItemInvoker = null;
        this.mItemData = null;
        this.mContext = null;
        this.mSingleChoice = true;
        this.mCheckable = false;
        this.mIconVisible = false;
        this.mAudioManager = null;
        this.mOnCheckedChangeListener = null;
        this.mContext = context;
        this.mMaxIconSize = (int) (((float) this.mMaxIconSize) * context.getResources().getDisplayMetrics().density);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTitleImage = (ImageView) findViewById(R.id.image);
        this.mTitleView = (TextView) findViewById(R.id.title);
        this.mShortcut = (TextView) findViewById(R.id.shortcut);
        this.mRadioButton = (RadioButton) findViewById(R.id.radio);
        this.mCheckBox = (CheckBox) findViewById(R.id.checkbox);
        if (this.mRadioButton == null) {
            Log.w("VivoListMenuItemView", "RadioButton doesn't exist");
        }
        if (this.mCheckBox == null) {
            Log.w("VivoListMenuItemView", "CheckBox doesn't exist");
        }
        if (this.mShortcut == null) {
            Log.w("VivoListMenuItemView", "Shortcut doesn't exist");
        }
        if (this.mTitleImage == null || this.mTitleView == null) {
            throw new RuntimeException("Could not create VivoListMenuItemView because could not find view with id image or title");
        }
    }

    public void initialize(MenuItemImpl itemData) {
        initialize(itemData, 0);
    }

    public void initialize(MenuItemImpl itemData, int menuType) {
        int i = 0;
        this.mItemData = itemData;
        if (itemData.isExclusiveCheckable()) {
            this.mSingleChoice = false;
        }
        setEnabled(itemData.isEnabled());
        if (!itemData.isVisible()) {
            i = 8;
        }
        setVisibility(i);
        setCheckable(itemData.isCheckable());
        initialize(itemData.getTitle(), itemData.getIcon());
    }

    public void initialize(CharSequence title, Drawable icon) {
        setClickable(true);
        setFocusable(true);
        setTitle(title);
        setIcon(icon);
    }

    public void setItemInvoker(ItemInvoker invoker) {
        this.mItemInvoker = invoker;
    }

    public MenuItemImpl getItemData() {
        return this.mItemData;
    }

    public void setTitle(CharSequence title) {
        Log.d("VivoListMenuItemView", "setTitle [ " + title + " ]");
        if (title == null) {
            this.mTitleView.setVisibility(8);
        } else {
            this.mTitleView.setVisibility(0);
        }
        this.mTitleView.setText(title.toString());
    }

    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        setEnabledRecusive(this, enable);
    }

    private void setEnabledRecusive(View view, boolean enable) {
        if (view instanceof ViewGroup) {
            int childCount = ((ViewGroup) view).getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = ((ViewGroup) view).getChildAt(i);
                childView.setEnabled(enable);
                setEnabledRecusive(childView, enable);
            }
        }
    }

    public void setCheckable(boolean checkable) {
        if (this.mRadioButton == null && this.mCheckBox == null) {
            Log.e("VivoListMenuItemView", "There are no RadioButton and CheckBox");
            return;
        }
        CompoundButton otherButton;
        Log.d("VivoListMenuItemView", "setCheckable + [ " + checkable + " ]");
        this.mCheckable = checkable;
        if (this.mSingleChoice) {
            this.mEffectiveButton = this.mRadioButton;
            otherButton = this.mCheckBox;
            Log.d("VivoListMenuItemView", "set RadioButton");
        } else {
            this.mEffectiveButton = this.mCheckBox;
            otherButton = this.mRadioButton;
            Log.d("VivoListMenuItemView", "set CheckBox");
        }
        if (!checkable) {
            this.mCheckable = false;
            if (this.mEffectiveButton != null) {
                this.mEffectiveButton.setOnCheckedChangeListener(null);
                this.mEffectiveButton.setVisibility(8);
            }
            if (otherButton != null) {
                otherButton.setVisibility(8);
            }
        } else if (this.mEffectiveButton == null) {
            Log.e("VivoListMenuItemView", "There is no CompoundButton");
        } else {
            this.mCheckable = true;
            this.mEffectiveButton.setVisibility(0);
            this.mEffectiveButton.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (VivoIconMenuItemView.this.mOnCheckedChangeListener != null) {
                        VivoIconMenuItemView.this.mOnCheckedChangeListener.onCheckedChange(VivoIconMenuItemView.this, isChecked);
                    }
                }
            });
            if (otherButton != null) {
                otherButton.setVisibility(8);
            }
        }
        setTitlePosition();
    }

    public void setChecked(boolean checked) {
        if (this.mEffectiveButton == null) {
            Log.e("VivoListMenuItemView", "setChecked failed because RadioButton or CheckBox dosn't exist");
            return;
        }
        Log.d("VivoListMenuItemView", "setChecked [ " + checked + " ] ");
        this.mEffectiveButton.setChecked(checked);
    }

    public boolean isChecked() {
        if (this.mEffectiveButton != null) {
            return this.mEffectiveButton.isChecked();
        }
        return false;
    }

    public void toggle() {
        if (this.mEffectiveButton != null) {
            this.mEffectiveButton.toggle();
        }
    }

    public boolean singleChoice() {
        return this.mSingleChoice;
    }

    public void setSingleChoice(boolean single) {
        this.mSingleChoice = single;
        setCheckable(this.mCheckable);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mOnCheckedChangeListener = listener;
    }

    public void setShortcut(boolean showShortcut, char shortcutKey) {
        if (showShortcut && this.mShortcut == null) {
            Log.e("VivoListMenuItemView", "There is no shortcutKey");
        } else if (this.mItemData == null) {
            Log.e("VivoListMenuItemView", "setShortcut MenuItemImpl null");
        } else {
            if (showShortcut && this.mItemData.shouldShowShortcut()) {
                this.mShortcut.setVisibility(0);
                this.mShortcut.setText(this.mItemData.getShortcutLabel());
            } else if (this.mShortcut != null) {
                this.mShortcut.setVisibility(8);
            }
        }
    }

    public void setIcon(Drawable icon) {
        float scale = 1.0f;
        if (icon != null) {
            Log.d("VivoListMenuItemView", "setIcon");
            if (showsIcon()) {
                this.mTitleImage.setVisibility(0);
                this.mIconVisible = true;
                int height = icon.getIntrinsicHeight();
                int width = icon.getIntrinsicHeight();
                if (width > this.mMaxIconSize) {
                    scale = (float) (width / this.mMaxIconSize);
                }
                if (height > this.mMaxIconSize) {
                    scale = Math.max(scale, (float) (height / this.mMaxIconSize));
                }
                icon.setBounds(0, 0, width, height);
                this.mTitleImage.setImageDrawable(icon);
            } else {
                this.mTitleImage.setVisibility(8);
                this.mIconVisible = false;
            }
            setTitlePosition();
        }
    }

    private void setTitlePosition() {
        LayoutParams lp = (LayoutParams) this.mTitleView.getLayoutParams();
        if (this.mIconVisible || this.mCheckable) {
            lp.removeRule(14);
            lp.addRule(20);
        } else {
            lp.removeRule(20);
            lp.addRule(14);
        }
        this.mTitleView.setLayoutParams(lp);
    }

    public boolean prefersCondensedTitle() {
        return false;
    }

    public boolean showsIcon() {
        return true;
    }

    public boolean performClick() {
        if (this.mEffectiveButton == this.mRadioButton) {
            setChecked(true);
        } else if (this.mEffectiveButton == this.mCheckBox) {
            toggle();
        }
        if (super.performClick()) {
            return true;
        }
        if (this.mItemInvoker == null || !this.mItemInvoker.invokeItem(this.mItemData) || !isSoundEffectsEnabled()) {
            return false;
        }
        playSoundEffectiveInternal(0);
        return true;
    }

    private void playSoundEffectiveInternal(int soundEffective) {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
        }
        this.mAudioManager.playSoundEffect(0);
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mItemData != null) {
            boolean z = this.mIconVisible;
        }
    }
}
