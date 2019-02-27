package com.android.internal.view.menu;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.LinearLayout;
import com.android.internal.view.menu.VivoIconMenuItemView.OnCheckedChangeListener;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoIconMenuViewGroup extends LinearLayout {
    private final boolean DEBUG;
    private final String TAG;
    private VivoIconMenuItemView mCheckedView;
    private Context mContext;

    private class CheckedChangeListener implements OnCheckedChangeListener {
        /* synthetic */ CheckedChangeListener(VivoIconMenuViewGroup this$0, CheckedChangeListener -this1) {
            this();
        }

        private CheckedChangeListener() {
        }

        public void onCheckedChange(VivoIconMenuItemView view, boolean isChecked) {
            if (view.singleChoice() && isChecked) {
                if (VivoIconMenuViewGroup.this.mCheckedView != null) {
                    VivoIconMenuViewGroup.this.mCheckedView.setChecked(false);
                }
                VivoIconMenuViewGroup.this.mCheckedView = view;
            }
        }
    }

    private class MenuViewOnHierarchyChangeListener implements OnHierarchyChangeListener {
        /* synthetic */ MenuViewOnHierarchyChangeListener(VivoIconMenuViewGroup this$0, MenuViewOnHierarchyChangeListener -this1) {
            this();
        }

        private MenuViewOnHierarchyChangeListener() {
        }

        public void onChildViewAdded(View parent, View child) {
            Log.d("VivoIconMenuViewGroup", "add view in the ViewTree");
            if (child instanceof VivoIconMenuItemView) {
                VivoIconMenuItemView itemView = (VivoIconMenuItemView) child;
                if (itemView.singleChoice() && itemView.isChecked()) {
                    if (VivoIconMenuViewGroup.this.mCheckedView != null) {
                        VivoIconMenuViewGroup.this.mCheckedView.setChecked(false);
                    }
                    VivoIconMenuViewGroup.this.mCheckedView = itemView;
                }
                itemView.setOnCheckedChangeListener(new CheckedChangeListener(VivoIconMenuViewGroup.this, null));
            }
            if (child instanceof ViewGroup) {
                ((ViewGroup) child).setOnHierarchyChangeListener(new MenuViewOnHierarchyChangeListener());
            }
        }

        public void onChildViewRemoved(View parent, View child) {
            Log.d("VivoIconMenuViewGroup", "remove view from the ViewTree");
        }
    }

    public VivoIconMenuViewGroup(Context context) {
        this(context, null);
    }

    public VivoIconMenuViewGroup(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public VivoIconMenuViewGroup(Context context, AttributeSet attr, int defAttrStyle) {
        super(context, attr, defAttrStyle);
        this.TAG = "VivoIconMenuViewGroup";
        this.DEBUG = true;
        this.mContext = null;
        this.mCheckedView = null;
        this.mContext = context;
        setOnHierarchyChangeListener(new MenuViewOnHierarchyChangeListener(this, null));
    }

    public void addView(View child, int index, LayoutParams params) {
        View checkableView = findCheckableItemView(child);
        if (checkableView instanceof VivoIconMenuItemView) {
            VivoIconMenuItemView button = (VivoIconMenuItemView) checkableView;
            if (button.isChecked() && button.singleChoice()) {
                if (this.mCheckedView != null) {
                    button.setChecked(false);
                }
                this.mCheckedView = button;
            }
            ((VivoIconMenuItemView) child).setOnCheckedChangeListener(new CheckedChangeListener(this, null));
        }
        super.addView(child, index, params);
    }

    private View findCheckableItemView(View rootView) {
        if (rootView instanceof VivoIconMenuItemView) {
            return rootView;
        }
        if (!(rootView instanceof ViewGroup)) {
            return null;
        }
        ViewGroup parent = (ViewGroup) rootView;
        int childCount = parent.getChildCount();
        View checkView = null;
        for (int i = 0; i < childCount; i++) {
            checkView = findCheckableItemView(parent.getChildAt(i));
            if (checkView instanceof VivoIconMenuItemView) {
                break;
            }
        }
        return checkView;
    }
}
