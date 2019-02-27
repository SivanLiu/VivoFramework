package com.vivo.common;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.FtBuild;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.vivo.internal.R;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class TabSelector extends LinearLayout {
    public static final int TAB_CENTER = 1;
    public static final int TAB_END = 3;
    public static final int TAB_LEFT = 0;
    public static final int TAB_RIGHT = 2;
    public static final int TAB_START = 0;
    private int mCurrentTab;
    private OnClickListener mOnClickListener;
    private int[] mStateList;
    private OnClickListener[] mTabClickListener;
    private ColorStateList[] mTabColorList;
    private boolean[] mTabEnableState;
    private int mTabHeight;
    private int[] mTabItemHeight;
    private int mTabPaddingHorizontal;
    private Drawable[] mTabSelector;
    private int mTabWidth;
    private MarqueeTextView[] mTabs;

    public TabSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTabs = new MarqueeTextView[3];
        this.mTabWidth = -2;
        this.mTabHeight = -2;
        this.mTabSelector = new Drawable[3];
        this.mTabClickListener = new OnClickListener[3];
        this.mTabColorList = new ColorStateList[3];
        this.mTabEnableState = new boolean[3];
        this.mStateList = new int[1];
        this.mCurrentTab = -1;
        this.mTabItemHeight = new int[3];
        this.mTabPaddingHorizontal = 0;
        this.mOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                int i = 0;
                while (i < 3) {
                    if (v.equals(TabSelector.this.mTabs[i]) && TabSelector.this.mTabEnableState[i]) {
                        TabSelector.this.setSelectorTab(i);
                        if (TabSelector.this.mTabClickListener[i] != null) {
                            TabSelector.this.mTabClickListener[i].onClick(v);
                            return;
                        }
                        return;
                    }
                    i++;
                }
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabSelector, 50397190, 51314991);
        this.mTabWidth = (int) a.getDimension(0, -2.0f);
        this.mTabSelector[2] = a.getDrawable(2);
        this.mTabSelector[0] = a.getDrawable(1);
        this.mTabSelector[1] = a.getDrawable(3);
        setTabStateColorList(a.getColorStateList(4));
        a.recycle();
        this.mTabPaddingHorizontal = context.getResources().getDimensionPixelOffset(51118270);
        setBaselineAligned(false);
        initLayout(context);
        setSelectorTab(0);
    }

    private void initLayout(Context context) {
        for (int i = 0; i < 3; i++) {
            this.mTabs[i] = new MarqueeTextView(context, null, 50397190);
            this.mTabs[i].setGravity(17);
            this.mTabs[i].setOnClickListener(this.mOnClickListener);
            setTabBackground(i, this.mTabSelector[i]);
            this.mTabEnableState[i] = true;
            addView(this.mTabs[i], new LayoutParams(this.mTabWidth, this.mTabHeight));
            this.mTabItemHeight[i] = this.mTabHeight;
        }
        showCenterTabVisible(false);
    }

    public TabSelector(Context context) {
        super(context, null);
        this.mTabs = new MarqueeTextView[3];
        this.mTabWidth = -2;
        this.mTabHeight = -2;
        this.mTabSelector = new Drawable[3];
        this.mTabClickListener = new OnClickListener[3];
        this.mTabColorList = new ColorStateList[3];
        this.mTabEnableState = new boolean[3];
        this.mStateList = new int[1];
        this.mCurrentTab = -1;
        this.mTabItemHeight = new int[3];
        this.mTabPaddingHorizontal = 0;
        this.mOnClickListener = /* anonymous class already generated */;
    }

    public void showCenterTabVisible(boolean value) {
        this.mTabs[1].setVisibility(value ? 0 : 8);
    }

    public void setTabWidth(int w) {
        for (int i = 0; i < 3; i++) {
            this.mTabs[i].getLayoutParams().width = w;
        }
        requestLayout();
    }

    public void setTabHeight(int h) {
        for (int i = 0; i < 3; i++) {
            this.mTabs[i].getLayoutParams().height = h;
        }
        requestLayout();
    }

    public void setTabString(int tabIndex, String str) {
        this.mTabs[tabIndex].setText(str);
        int max = getMeasuredHeight(this.mTabWidth, this.mTabs[tabIndex]);
        if (this.mTabItemHeight[tabIndex] != max) {
            this.mTabItemHeight[tabIndex] = max;
            int i = 0;
            while (i < 3) {
                if (tabIndex != i && this.mTabItemHeight[i] <= 0) {
                    this.mTabItemHeight[i] = getMeasuredHeight(this.mTabWidth, this.mTabs[i]);
                }
                if (max < this.mTabItemHeight[i]) {
                    max = this.mTabItemHeight[i];
                }
                i++;
            }
            for (i = 0; i < 3; i++) {
                this.mTabs[i].getLayoutParams().height = max;
            }
            this.mTabHeight = max;
            requestLayout();
        }
    }

    public void setTabStringColor(int tabIndex, int color) {
        this.mTabs[tabIndex].setTextColor(color);
    }

    public void setTabStateColorList(int tabIndex, ColorStateList color) {
        this.mTabColorList[tabIndex] = color;
    }

    public void setTabStateColorList(ColorStateList color) {
        this.mTabColorList[0] = color;
        for (int i = 1; i < 3; i++) {
            this.mTabColorList[i] = cloneStateColorList(color);
        }
    }

    public void setTabBackground(int tabIndex, Drawable drawable) {
        if (tabIndex < 3 && tabIndex >= 0) {
            TextView view = this.mTabs[tabIndex];
            view.setBackgroundDrawable(drawable);
            view.setPadding(this.mTabPaddingHorizontal, view.getPaddingTop(), this.mTabPaddingHorizontal, view.getPaddingBottom());
        }
    }

    public void setTabStateDrawable(int tabIndex, StateListDrawable drawablelist) {
        this.mTabSelector[tabIndex] = drawablelist;
    }

    public void setTabOnClickListener(int tabIndex, OnClickListener l) {
        this.mTabClickListener[tabIndex] = l;
    }

    public void setTabTextSize(float size) {
        setTabTextSize(1, size);
    }

    public void setTabTextSize(int unit, float size) {
        for (int a = 0; a < 3; a++) {
            this.mTabs[a].setTextSize(unit, size);
        }
    }

    public void setTabItemTextSize(int tabIndex, float size) {
        setTabItemTextSize(tabIndex, 1, size);
    }

    public void setTabItemTextSize(int tabIndex, int unit, float size) {
        this.mTabs[tabIndex].setTextSize(unit, size);
    }

    public int getCurrentTab() {
        return this.mCurrentTab;
    }

    private ColorStateList cloneStateColorList(ColorStateList org) {
        int[][] state = org.getStates();
        int[][] nstate = new int[state.length][];
        if (state.length > 0) {
            System.arraycopy(state, 0, nstate, 0, state.length);
        }
        for (int i = 0; i < state.length; i++) {
            nstate[i] = new int[state[i].length];
            if (nstate[i].length > 0) {
                System.arraycopy(state[i], 0, nstate[i], 0, nstate[i].length);
            }
        }
        int[] color = new int[org.getColors().length];
        if (color.length > 0) {
            System.arraycopy(org.getColors(), 0, color, 0, color.length);
        }
        return new ColorStateList(nstate, color);
    }

    public void setEnableTab(int tabIndex, boolean enable) {
        if (tabIndex < 3) {
            this.mTabEnableState[tabIndex] = enable;
        }
    }

    public void setSelectorTab(int tabIndex) {
        if (tabIndex != this.mCurrentTab) {
            int a = 0;
            while (a < 3) {
                if (a == tabIndex) {
                    this.mStateList[0] = 16842913;
                    if (FtBuild.getRomVersion() >= 4.5f) {
                        this.mTabs[a].setEllipsize(TruncateAt.MARQUEE);
                    } else {
                        this.mTabs[a].setEllipsize(null);
                    }
                } else {
                    this.mStateList[0] = -16842913;
                    this.mTabs[a].setEllipsize(null);
                }
                if (this.mTabColorList[a] != null) {
                    this.mTabs[a].setTextColor(this.mTabColorList[a].getColorForState(this.mStateList, 0));
                }
                if (this.mTabSelector[a] != null && (this.mTabSelector[a] instanceof StateListDrawable)) {
                    StateListDrawable list = this.mTabSelector[a];
                    list.setState(new int[]{this.mStateList[0]});
                    setTabBackground(a, list.getCurrent());
                }
                a++;
            }
            this.mCurrentTab = tabIndex;
        }
    }

    private int getMeasuredHeight(int height, View view) {
        view.measure(MeasureSpec.makeMeasureSpec(this.mTabWidth, 1073741824), MeasureSpec.makeMeasureSpec(-2, 0));
        return view.getMeasuredHeight();
    }
}
