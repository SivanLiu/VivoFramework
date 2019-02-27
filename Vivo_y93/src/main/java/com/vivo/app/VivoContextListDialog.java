package com.vivo.app;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.Dialog;
import android.content.Context;
import android.os.FtBuild;
import android.os.Handler;
import android.util.Log;
import android.view.IWindowManager;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import vivo.util.VivoThemeUtil;
import vivo.util.VivoThemeUtil.ThemeType;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class VivoContextListDialog extends Dialog {
    private static final String TAG = "VivoContextListDialog";
    private float mDensity;
    private Handler mDismissHandler;
    private Runnable mDismissNavBarBg = new Runnable() {
        public void run() {
            VivoContextListDialog.this.removeNavBarBg();
        }
    };
    private Runnable mHideNavBarBg = new Runnable() {
        public void run() {
            if (VivoContextListDialog.this.mNavBarBg != null) {
                VivoContextListDialog.this.mNavBarBg.setVisibility(8);
            } else {
                Log.e(VivoContextListDialog.TAG, "hideNavBarBg invalidate");
            }
        }
    };
    private ArrayList<String> mItems;
    private boolean mLastDocked;
    private OnLayoutChangeListener mLayoutChangeListener;
    private ContextListAdapter mListAdapter;
    private ListView mListView;
    private View mNavBarBg;
    private boolean mNavBarDismiss;
    private boolean mNavBarHide;
    private int mNavDismissDuration;
    private OnItemClickListener mOnItemClickListener;
    private View mRootView;
    private FrameLayout mTitleRoot;
    private TextView mTitleView;
    private WindowManager mWindowManager;

    private class ContextListAdapter extends BaseAdapter {
        /* synthetic */ ContextListAdapter(VivoContextListDialog this$0, ContextListAdapter -this1) {
            this();
        }

        private ContextListAdapter() {
        }

        public int getCount() {
            if (VivoContextListDialog.this.mItems != null) {
                return VivoContextListDialog.this.mItems.size();
            }
            return 0;
        }

        public String getItem(int position) {
            return (String) VivoContextListDialog.this.mItems.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = VivoContextListDialog.this.getLayoutInflater().inflate(50528272, parent, false);
            }
            ((TextView) convertView).setText(getItem(position));
            return convertView;
        }

        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }
    }

    private class WindowInsetsListener implements OnApplyWindowInsetsListener {
        /* synthetic */ WindowInsetsListener(VivoContextListDialog this$0, WindowInsetsListener -this1) {
            this();
        }

        private WindowInsetsListener() {
        }

        public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
            int leftInset = insets.getSystemWindowInsetLeft();
            int rightInset = insets.getSystemWindowInsetRight();
            int bottomInset = insets.getSystemWindowInsetBottom();
            boolean navBarLeft = bottomInset == 0 && leftInset > 0;
            boolean navBarRight = bottomInset == 0 && rightInset > 0;
            boolean showNavBar = navBarLeft || navBarRight || bottomInset > 0;
            if (showNavBar) {
                LayoutParams lp = new LayoutParams();
                lp.flags |= -2147483384;
                if (navBarLeft) {
                    lp.width = leftInset;
                    lp.height = -1;
                    lp.gravity = 3;
                } else if (navBarRight) {
                    lp.width = rightInset;
                    lp.height = -1;
                    lp.gravity = 5;
                } else {
                    lp.width = -1;
                    lp.height = bottomInset;
                    lp.gravity = 80;
                }
                if (VivoContextListDialog.this.mNavBarBg != null) {
                    VivoContextListDialog.this.mWindowManager.removeView(VivoContextListDialog.this.mNavBarBg);
                } else {
                    VivoContextListDialog.this.mNavBarBg = new View(VivoContextListDialog.this.getContext());
                }
                VivoContextListDialog.this.mNavBarHide = false;
                VivoContextListDialog.this.mNavBarDismiss = false;
                VivoContextListDialog.this.mNavBarBg.setBackgroundColor(VivoContextListDialog.this.getWindow().getNavigationBarColor());
                VivoContextListDialog.this.mWindowManager.addView(VivoContextListDialog.this.mNavBarBg, lp);
                return v.onApplyWindowInsets(insets);
            }
            VivoContextListDialog.this.removeNavBarBg();
            return v.onApplyWindowInsets(insets);
        }
    }

    private class WindowLayoutChangedListener implements OnLayoutChangeListener {
        /* synthetic */ WindowLayoutChangedListener(VivoContextListDialog this$0, WindowLayoutChangedListener -this1) {
            this();
        }

        private WindowLayoutChangedListener() {
        }

        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            boolean docked = VivoContextListDialog.this.isInSplitMode();
            if (VivoContextListDialog.this.mLastDocked != docked) {
                if (docked) {
                    VivoContextListDialog.this.getWindow().clearFlags(Integer.MIN_VALUE);
                } else {
                    VivoContextListDialog.this.getWindow().addFlags(Integer.MIN_VALUE);
                }
            }
            VivoContextListDialog.this.mLastDocked = docked;
        }
    }

    public VivoContextListDialog(Context context, ArrayList<String> items) {
        super(context, VivoThemeUtil.getSystemThemeStyle(ThemeType.CONTEXT_MENU_DIALOG));
        this.mItems = items;
        this.mNavDismissDuration = context.getResources().getInteger(51052565);
        this.mDensity = context.getResources().getDisplayMetrics().density;
        this.mWindowManager = (WindowManager) getContext().getSystemService("window");
        this.mDismissHandler = new Handler();
        this.mLayoutChangeListener = new WindowLayoutChangedListener(this, null);
        initVivoContextListDialog(context);
        if (FtBuild.getRomVersion() < 4.0f) {
            initNavigationBarBackground();
        }
    }

    private void initVivoContextListDialog(Context context) {
        Window window = getWindow();
        window.requestFeature(1);
        setContentView(50528271);
        window.setLayout(-1, -2);
        window.setGravity(80);
        this.mRootView = findViewById(51183658);
        this.mTitleView = (TextView) findViewById(51183661);
        this.mListView = (ListView) findViewById(51183663);
        this.mTitleRoot = (FrameLayout) findViewById(51183660);
        this.mTitleRoot.setVisibility(8);
        getWindow().setSoftInputMode(34);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        this.mListAdapter = new ContextListAdapter(this, null);
        this.mListView.setAdapter(this.mListAdapter);
        this.mListView.setOnItemClickListener(this.mOnItemClickListener);
    }

    private void initNavigationBarBackground() {
        int flags = 65792;
        this.mLastDocked = isInSplitMode();
        if (!this.mLastDocked) {
            flags = -2147417856;
        }
        Window window = getWindow();
        window.addFlags(flags);
        window.getDecorView().setOnApplyWindowInsetsListener(new WindowInsetsListener(this, null));
        window.getDecorView().addOnLayoutChangeListener(this.mLayoutChangeListener);
    }

    public void show() {
        if (this.mNavBarBg != null) {
            this.mNavBarHide = false;
            this.mNavBarDismiss = false;
            this.mNavBarBg.setVisibility(0);
        }
        super.show();
    }

    public void dismiss() {
        if (!(!isShowing() || this.mNavBarBg == null || (this.mNavBarDismiss ^ 1) == 0)) {
            this.mNavBarDismiss = true;
            this.mDismissHandler.removeCallbacks(this.mHideNavBarBg);
            this.mDismissHandler.postDelayed(this.mDismissNavBarBg, (long) this.mNavDismissDuration);
        }
        getWindow().getDecorView().removeOnLayoutChangeListener(this.mLayoutChangeListener);
        super.dismiss();
    }

    public void hide() {
        if (!(!isShowing() || this.mNavBarBg == null || (this.mNavBarHide ^ 1) == 0)) {
            this.mNavBarHide = true;
            this.mDismissHandler.postDelayed(this.mHideNavBarBg, (long) this.mNavDismissDuration);
        }
        super.hide();
    }

    private void removeNavBarBg() {
        if (this.mNavBarBg == null || this.mNavBarBg.getParent() == null) {
            Log.e(TAG, "dismissNavBarBg invalidate");
            return;
        }
        this.mWindowManager.removeView(this.mNavBarBg);
        this.mNavBarBg = null;
    }

    public void setTitle(String title) {
        this.mTitleRoot.setVisibility(0);
        this.mTitleView.setText(title);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
        this.mListView.setOnItemClickListener(this.mOnItemClickListener);
    }

    private boolean isInSplitMode() {
        int docked;
        try {
            IWindowManager windowManager = WindowManagerGlobal.getWindowManagerService();
            docked = ((Integer) windowManager.getClass().getDeclaredMethod("getDockedStackSide", new Class[0]).invoke(windowManager, new Object[0])).intValue();
        } catch (Exception e) {
            docked = -1;
            Log.w(TAG, "Failed to get dock side: " + e);
        }
        if (docked >= 0) {
            return true;
        }
        return false;
    }
}
