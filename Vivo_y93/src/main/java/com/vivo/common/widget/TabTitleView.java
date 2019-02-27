package com.vivo.common.widget;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import com.vivo.common.BbkTitleView;
import com.vivo.common.TabSelector;
import java.util.ArrayList;
import java.util.List;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class TabTitleView extends BbkTitleView {
    /* renamed from: -com-vivo-common-widget-TabTitleView$ViewModeSwitchesValues */
    private static final /* synthetic */ int[] f1-com-vivo-common-widget-TabTitleView$ViewModeSwitchesValues = null;
    private final boolean DEBUG;
    private final String TAG;
    private List<View> mCenterViews;
    private ViewMode mMode;
    private TabSelector mSelector;
    private boolean mSupportAutoMove;

    private final class SelectorProxy extends ViewProxy {
        private final int SELECTOR_ITEM_COUNT = 2;
        private final int SELECTOR_ITEM_MAX_WIDTH = 90;
        private final int SELECTOR_ITEM_MIN_WIDTH = 60;
        private final int SELECTOR_ITEM_NOR_WIDTH = 90;
        private int mLastWidth = 0;
        private int mMaximumWidth = 0;
        private int mMinimumWidth = 0;
        private int mNormalWidth = 0;

        SelectorProxy() {
            super();
            float density = TabTitleView.this.mContext.getResources().getDisplayMetrics().density;
            this.mMinimumWidth = ((int) (60.0f * density)) * 2;
            this.mMaximumWidth = ((int) (90.0f * density)) * 2;
            this.mNormalWidth = ((int) (90.0f * density)) * 2;
            TabTitleView.this.mSelector.setTabWidth(this.mNormalWidth / 2);
        }

        public int getContentWidth() {
            TabTitleView.this.mSelector.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
            return TabTitleView.this.mSelector.getMeasuredWidth();
        }

        public int getContentMinWidth() {
            return this.mMinimumWidth;
        }

        public void setOffset(int sOff, int eOff) {
            int selfWidth = TabTitleView.this.getMeasuredWidth();
            int selectorWidth = TabTitleView.this.mSelector.getMeasuredWidth();
            int maxWidth = Math.min(selfWidth - (Math.max(0, Math.max(sOff, eOff)) * 2), this.mMaximumWidth);
            if (selectorWidth < this.mNormalWidth || selectorWidth > maxWidth) {
                TabTitleView.this.mSelector.setTabWidth(Math.max(maxWidth, this.mMinimumWidth) / 2);
            }
        }

        public boolean supportAutoMove() {
            return TabTitleView.this.mSupportAutoMove;
        }
    }

    public enum ViewMode {
        MODE_NULL,
        MODE_SELECTOR,
        MODE_NORMAL
    }

    private final class ViewProxyAgent extends ViewProxy {
        private ViewProxy mBaseViewProxy;
        private ViewProxy mSelectorViewProxy;

        ViewProxyAgent(ViewProxy baseProxy, ViewProxy selectorProxy) {
            super();
            this.mSelectorViewProxy = selectorProxy;
            this.mBaseViewProxy = baseProxy;
        }

        public int getContentWidth() {
            if (TabTitleView.this.mMode == ViewMode.MODE_SELECTOR) {
                return this.mSelectorViewProxy.getContentWidth();
            }
            return this.mBaseViewProxy.getContentWidth();
        }

        public int getContentMinWidth() {
            if (TabTitleView.this.mMode == ViewMode.MODE_SELECTOR) {
                return this.mSelectorViewProxy.getContentMinWidth();
            }
            return this.mBaseViewProxy.getContentMinWidth();
        }

        public void setOffset(int sOff, int eOff) {
            if (TabTitleView.this.mMode == ViewMode.MODE_SELECTOR) {
                this.mSelectorViewProxy.setOffset(sOff, eOff);
            } else {
                this.mBaseViewProxy.setOffset(sOff, eOff);
            }
        }

        public boolean supportAutoMove() {
            if (TabTitleView.this.mMode == ViewMode.MODE_SELECTOR) {
                return this.mSelectorViewProxy.supportAutoMove();
            }
            return this.mBaseViewProxy.supportAutoMove();
        }
    }

    /* renamed from: -getcom-vivo-common-widget-TabTitleView$ViewModeSwitchesValues */
    private static /* synthetic */ int[] m1-getcom-vivo-common-widget-TabTitleView$ViewModeSwitchesValues() {
        if (f1-com-vivo-common-widget-TabTitleView$ViewModeSwitchesValues != null) {
            return f1-com-vivo-common-widget-TabTitleView$ViewModeSwitchesValues;
        }
        int[] iArr = new int[ViewMode.values().length];
        try {
            iArr[ViewMode.MODE_NORMAL.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ViewMode.MODE_NULL.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ViewMode.MODE_SELECTOR.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        f1-com-vivo-common-widget-TabTitleView$ViewModeSwitchesValues = iArr;
        return iArr;
    }

    public TabTitleView(Context context) {
        this(context, null);
    }

    public TabTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.TAG = "TabTitleView";
        this.DEBUG = false;
        this.mMode = ViewMode.MODE_NULL;
        this.mSupportAutoMove = false;
        setMode(ViewMode.MODE_SELECTOR);
    }

    protected ViewProxy initCenterView() {
        ViewProxy baseProxy = super.initCenterView();
        this.mCenterViews = new ArrayList();
        int childCount = this.mCenterView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.mCenterViews.add(this.mCenterView.getChildAt(i));
        }
        View parent = LayoutInflater.from(this.mContext).inflate(50528367, this.mCenterView);
        this.mSelector = (TabSelector) findViewById(51183779);
        if (this.mSelector != null) {
            return new ViewProxyAgent(baseProxy, new SelectorProxy());
        }
        throw new RuntimeException("create TabSelector failed");
    }

    public void setMode(ViewMode mode) {
        if (mode != this.mMode) {
            ViewMode tmpMode = this.mMode;
            this.mMode = mode;
            switch (m1-getcom-vivo-common-widget-TabTitleView$ViewModeSwitchesValues()[mode.ordinal()]) {
                case 1:
                    this.mSelector.setVisibility(8);
                    setCenterViewsVisible(0);
                    this.mSupportAutoMove = true;
                    break;
                case 2:
                    this.mSelector.setVisibility(8);
                    setCenterViewsVisible(8);
                    this.mSupportAutoMove = true;
                    break;
                case 3:
                    this.mSelector.setVisibility(0);
                    setCenterViewsVisible(8);
                    this.mSupportAutoMove = false;
                    break;
                default:
                    this.mMode = tmpMode;
                    Log.w("TabTitleView", "setMode unkown mode : " + mode);
                    break;
            }
        }
    }

    public TabSelector getTabSelector() {
        return this.mSelector;
    }

    private void setCenterViewsVisible(int visible) {
        for (View v : this.mCenterViews) {
            v.setVisibility(visible);
        }
    }
}
