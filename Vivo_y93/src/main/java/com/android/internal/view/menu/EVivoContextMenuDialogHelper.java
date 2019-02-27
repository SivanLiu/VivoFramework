package com.android.internal.view.menu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.FtBuild;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.animation.PathInterpolator;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.view.NavigationBackground;
import com.android.internal.view.menu.MenuBuilder.ItemInvoker;
import com.android.internal.view.menu.MenuPresenter.Callback;
import com.vivo.internal.R;
import java.util.List;
import vivo.util.VivoThemeUtil;
import vivo.util.VivoThemeUtil.ThemeType;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class EVivoContextMenuDialogHelper extends MenuDialogHelper implements OnKeyListener, OnClickListener, OnDismissListener, Callback, OnItemClickListener, ItemInvoker {
    private static final String TAG = "EVivoContextMenuDialogHelper";
    private LinearLayout mContextMenuContent;
    private Dialog mDialog;
    private Drawable mExpandedIcon = null;
    private boolean mExpandedMode = false;
    private LinearLayout mIconMenus;
    private int mItemSpace = 8;
    private int mLastListItemHeight = 0;
    private MenuAdapter mListAdapter;
    private ListView mListView;
    private int mMaxIconMenus = 4;
    private MenuBuilder mMenu;
    private MenuItemAdjust mMenuAdjust = null;
    private ColorStateList mMenuItemMoreTextColor = null;
    private ColorStateList mMenuItemTextColor = null;
    private MenuPresenter mMenuPresenter = null;
    private Drawable mMoreIcon = null;
    private VivoContextMenuIconItem mMoreItem;
    private NavigationBackground mNavBarBg = null;
    private Callback mPresenterCallback;

    private class MenuAdapter extends BaseAdapter {
        /* synthetic */ MenuAdapter(EVivoContextMenuDialogHelper this$0, MenuAdapter -this1) {
            this();
        }

        private MenuAdapter() {
        }

        public int getCount() {
            return (EVivoContextMenuDialogHelper.this.mMenu.getNonActionItems().size() - EVivoContextMenuDialogHelper.this.mMaxIconMenus) + 1;
        }

        public MenuItemImpl getItem(int position) {
            return (MenuItemImpl) EVivoContextMenuDialogHelper.this.mMenu.getNonActionItems().get(position + (EVivoContextMenuDialogHelper.this.mMaxIconMenus - 1));
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new VivoContextMenuIconItem(EVivoContextMenuDialogHelper.this.mMenu.getContext(), null, R.attr.contextMenuListItemStyle);
                convertView.setLayoutParams(new LayoutParams(-1, -2));
            }
            ((VivoContextMenuIconItem) convertView).setText(getItem(position).getTitle());
            return convertView;
        }

        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }
    }

    private class MenuItemAdjust {
        private final int FONT_ADJUST_SIZE = 2;
        private Context mContext = null;
        private LinearLayout mGroup = null;
        private float originalTextSize = 0.0f;

        MenuItemAdjust(Context context) {
            this.mContext = context;
            TypedArray a = this.mContext.obtainStyledAttributes(null, new int[]{com.android.internal.R.attr.textSize}, R.attr.contextMenuIconItemStyle, 0);
            this.originalTextSize = a.getDimension(0, 0.0f);
            a.recycle();
        }

        void adjust(ViewGroup view) {
            if (view instanceof LinearLayout) {
                this.mGroup = (LinearLayout) view;
                int usableWidth = getAvailableWidth();
                int childCount = this.mGroup.getChildCount();
                if (childCount > 0) {
                    int itemMaxWidth = usableWidth / childCount;
                    for (int i = 0; i < childCount; i++) {
                        TextView textView = (TextView) this.mGroup.getChildAt(i);
                        textView.setTextSize(0, this.originalTextSize);
                        MarginLayoutParams mParam = (MarginLayoutParams) textView.getLayoutParams();
                        if ((((int) textView.getPaint().measureText(textView.getText().toString())) + textView.getPaddingLeft()) + textView.getPaddingRight() > (itemMaxWidth - mParam.leftMargin) - mParam.rightMargin) {
                            tryAdjustTextSize(textView, (itemMaxWidth - mParam.leftMargin) - mParam.rightMargin);
                        }
                    }
                }
            }
        }

        private int getAvailableWidth() {
            int width = this.mContext.getResources().getDisplayMetrics().widthPixels - (this.mGroup.getPaddingLeft() + this.mGroup.getPaddingRight());
            if (!(this.mGroup.getLayoutParams() instanceof MarginLayoutParams)) {
                return width;
            }
            MarginLayoutParams mParam = (MarginLayoutParams) this.mGroup.getLayoutParams();
            return width - (mParam.leftMargin - mParam.rightMargin);
        }

        private boolean tryAdjustTextSize(TextView textView, int availableLength) {
            boolean retval = false;
            if (textView.getText().toString() == null) {
                return false;
            }
            Paint paint = textView.getPaint();
            String textStr = textView.getText().toString();
            textView.setTextSize(0, this.originalTextSize - TypedValue.applyDimension(1, 2.0f, this.mContext.getResources().getDisplayMetrics()));
            if (((((int) paint.measureText(textStr)) + textView.getPaddingLeft()) + textView.getPaddingRight()) + (textStr.length() > 0 ? (int) paint.measureText(textStr.substring(textStr.length() - 1)) : 0) >= availableLength) {
                textView.setTextSize(0, this.originalTextSize);
            } else {
                retval = true;
            }
            return retval;
        }
    }

    public EVivoContextMenuDialogHelper(MenuBuilder menu) {
        super(menu);
        this.mMenu = menu;
        TypedArray typeArray = menu.getContext().obtainStyledAttributes(R.styleable.ContextMenu);
        this.mMenuItemTextColor = typeArray.getColorStateList(3);
        this.mMenuItemMoreTextColor = typeArray.getColorStateList(4);
        this.mMoreIcon = typeArray.getDrawable(8);
        this.mExpandedIcon = typeArray.getDrawable(9);
        typeArray.recycle();
        this.mItemSpace = (int) (((float) this.mItemSpace) * this.mMenu.getContext().getResources().getDisplayMetrics().density);
        this.mMenuAdjust = new MenuItemAdjust(this.mMenu.getContext());
    }

    public void show() {
        MenuBuilder menu = this.mMenu;
        LayoutInflater inflater = (LayoutInflater) menu.getContext().getSystemService("layout_inflater");
        this.mDialog = new Dialog(menu.getContext(), VivoThemeUtil.getSystemThemeStyle(ThemeType.DIALOG_SLIDE));
        this.mDialog.getWindow().requestFeature(1);
        this.mDialog.getWindow().getAttributes().gravity = 80;
        this.mDialog.setCancelable(true);
        this.mDialog.setCanceledOnTouchOutside(true);
        this.mDialog.setContentView(R.layout.vigour_context_menu_layout);
        this.mDialog.setOnDismissListener(this);
        this.mDialog.setOnKeyListener(this);
        Window window = this.mDialog.getWindow();
        if (((double) FtBuild.getRomVersion()) < 4.0d) {
            this.mNavBarBg = new NavigationBackground(this.mMenu.getContext(), this.mDialog.getWindow(), new Handler());
            this.mDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    EVivoContextMenuDialogHelper.this.mNavBarBg.dismiss();
                }
            });
        }
        this.mContextMenuContent = (LinearLayout) window.findViewById(R.id.contextmenu_content);
        window.findViewById(R.id.contextmenu_root).setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                EVivoContextMenuDialogHelper.this.mDialog.dismiss();
            }
        });
        this.mMenuPresenter = new VivoIconMenuPresenter(menu.getContext());
        this.mMenu.addMenuPresenter(this.mMenuPresenter);
        TextView mTitleView = (TextView) window.findViewById(R.id.menuTitle);
        mTitleView.setText(menu.getHeaderTitle());
        mTitleView.setSelected(false);
        this.mIconMenus = (LinearLayout) window.findViewById(R.id.iconMenus);
        this.mListView = (ListView) window.findViewById(R.id.listPanel);
        if (addIconMenuItems()) {
            this.mListAdapter = new MenuAdapter(this, null);
            this.mListView.setAdapter(this.mListAdapter);
            this.mListView.setOnItemClickListener(this);
            this.mListView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (!(EVivoContextMenuDialogHelper.this.mExpandedMode || EVivoContextMenuDialogHelper.this.mLastListItemHeight == EVivoContextMenuDialogHelper.this.mListView.getHeight())) {
                        EVivoContextMenuDialogHelper.this.mContextMenuContent.setTranslationY((float) EVivoContextMenuDialogHelper.this.mListView.getHeight());
                    }
                    EVivoContextMenuDialogHelper.this.mLastListItemHeight = EVivoContextMenuDialogHelper.this.mListView.getHeight();
                }
            });
        } else {
            window.findViewById(R.id.listLayout).setVisibility(8);
            this.mListView.setVisibility(8);
        }
        WindowManager.LayoutParams lp = this.mDialog.getWindow().getAttributes();
        lp.width = -1;
        lp.height = -2;
        if (isInSplitMode()) {
            lp.flags &= Integer.MAX_VALUE;
        }
        this.mDialog.getWindow().setAttributes(lp);
        this.mDialog.getWindow().setSoftInputMode(34);
        this.mDialog.show();
    }

    private boolean isInSplitMode() {
        int docked = -1;
        try {
            docked = WindowManagerGlobal.getWindowManagerService().getDockedStackSide();
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to get dock side: " + e);
        }
        return docked != -1;
    }

    private boolean addIconMenuItems() {
        List<MenuItemImpl> itemsToShow = this.mMenu.getNonActionItems();
        boolean needsMore = itemsToShow.size() > this.mMaxIconMenus;
        int itemCount = itemsToShow.size();
        if (needsMore) {
            itemCount = this.mMaxIconMenus - 1;
        }
        for (int i = 0; i < itemCount; i++) {
            MenuItemImpl item = (MenuItemImpl) itemsToShow.get(i);
            VivoContextMenuIconItem iconItem = new VivoContextMenuIconItem(this.mMenu.getContext(), null, R.attr.contextMenuIconItemStyle);
            iconItem.initialize(item, 0);
            iconItem.setItemInvoker(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2);
            params.weight = 1.0f;
            params.width = 0;
            if (i != 0) {
                params.setMarginStart(this.mItemSpace);
            }
            this.mIconMenus.addView((View) iconItem, (ViewGroup.LayoutParams) params);
        }
        if (needsMore) {
            addIconMenuMoreIcon();
        }
        return needsMore;
    }

    private void addIconMenuMoreIcon() {
        this.mMoreItem = new VivoContextMenuIconItem(this.mMenu.getContext(), null, R.attr.contextMenuIconItemStyle);
        this.mMoreItem.initialize(this.mMenu.getContext().getResources().getString(com.android.internal.R.string.more_item_label), this.mExpandedIcon);
        this.mMoreItem.setTextColor(this.mMenuItemTextColor);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2);
        params.weight = 1.0f;
        params.width = 0;
        params.setMarginStart(this.mItemSpace);
        this.mIconMenus.addView(this.mMoreItem, (ViewGroup.LayoutParams) params);
        this.mMoreItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                EVivoContextMenuDialogHelper.this.switchMode();
            }
        });
    }

    private void switchMode() {
        ValueAnimator anim;
        final int height;
        if (this.mExpandedMode) {
            anim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            anim.setInterpolator(new PathInterpolator(0.25f, 0.1f, 0.25f, 1.0f));
            anim.setDuration(230);
            height = this.mListView.getHeight();
            anim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                }

                public void onAnimationEnd(Animator animation) {
                }
            });
            anim.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    EVivoContextMenuDialogHelper.this.mContextMenuContent.setTranslationY((float) ((int) (((float) height) * ((Float) animation.getAnimatedValue()).floatValue())));
                }
            });
            anim.start();
            this.mMoreItem.setIcon(this.mExpandedIcon);
            this.mMoreItem.setTextColor(this.mMenuItemTextColor);
        } else {
            anim = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
            height = this.mListView.getHeight();
            anim.setInterpolator(new PathInterpolator(0.25f, 0.1f, 0.25f, 1.0f));
            anim.setDuration(230);
            anim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                }

                public void onAnimationEnd(Animator animation) {
                }
            });
            anim.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    EVivoContextMenuDialogHelper.this.mContextMenuContent.setTranslationY((float) ((int) (((float) height) * ((Float) animation.getAnimatedValue()).floatValue())));
                }
            });
            anim.start();
            this.mMoreItem.setIcon(this.mMoreIcon);
            this.mMoreItem.setTextColor(this.mMenuItemMoreTextColor);
        }
        this.mExpandedMode ^= 1;
    }

    public void setPresenterCallback(Callback cb) {
        this.mPresenterCallback = cb;
    }

    public void dismiss() {
        if (this.mDialog != null) {
            this.mDialog.dismiss();
        }
    }

    public void onDismiss(DialogInterface dialog) {
    }

    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        if (allMenusAreClosing || menu == this.mMenu) {
            dismiss();
        }
        if (this.mPresenterCallback != null) {
            this.mPresenterCallback.onCloseMenu(menu, allMenusAreClosing);
        }
    }

    public boolean onOpenSubMenu(MenuBuilder subMenu) {
        if (this.mPresenterCallback != null) {
            return this.mPresenterCallback.onOpenSubMenu(subMenu);
        }
        return false;
    }

    public void onClick(DialogInterface dialog, int which) {
        this.mMenu.performItemAction(this.mListAdapter.getItem(which), 0);
    }

    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        return false;
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        this.mMenu.performItemAction(this.mListAdapter.getItem(position), 0);
        onCloseMenu(this.mMenu, true);
    }

    public boolean invokeItem(MenuItemImpl item) {
        boolean result = this.mMenu.performItemAction(item, 0);
        onCloseMenu(this.mMenu, true);
        return result;
    }
}
