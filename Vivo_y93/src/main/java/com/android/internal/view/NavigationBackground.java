package com.android.internal.view;

import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.View.OnLayoutChangeListener;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import com.vivo.internal.R;

public class NavigationBackground {
    private static final boolean DEBUG = true;
    private static final String TAG = "NavigationBackground";
    private Context mContext = null;
    private int mDismissDuration = 0;
    private Handler mHandler = null;
    private boolean mInitForActivity = false;
    private boolean mLastDocked = false;
    private View mNavBarBg = null;
    private LayoutParams mNavLayoutParams = null;
    private Window mWindow = null;
    private WindowManager mWindowManager = null;

    private class WindowInsetsListener implements OnApplyWindowInsetsListener {
        /* synthetic */ WindowInsetsListener(NavigationBackground this$0, WindowInsetsListener -this1) {
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
                if (navBarLeft) {
                    NavigationBackground.this.mNavLayoutParams.width = leftInset;
                    NavigationBackground.this.mNavLayoutParams.height = -1;
                    NavigationBackground.this.mNavLayoutParams.gravity = 3;
                } else if (navBarRight) {
                    NavigationBackground.this.mNavLayoutParams.width = rightInset;
                    NavigationBackground.this.mNavLayoutParams.height = -1;
                    NavigationBackground.this.mNavLayoutParams.gravity = 5;
                } else {
                    NavigationBackground.this.mNavLayoutParams.width = -1;
                    NavigationBackground.this.mNavLayoutParams.height = bottomInset;
                    NavigationBackground.this.mNavLayoutParams.gravity = 80;
                }
                if (NavigationBackground.this.mNavBarBg == null || NavigationBackground.this.mNavBarBg.getParent() == null) {
                    NavigationBackground.this.mNavBarBg = new View(NavigationBackground.this.mContext);
                } else {
                    NavigationBackground.this.mWindowManager.removeView(NavigationBackground.this.mNavBarBg);
                }
                NavigationBackground.this.mNavBarBg.setBackgroundColor(NavigationBackground.this.mWindow.getNavigationBarColor());
                NavigationBackground.this.mWindowManager.addView(NavigationBackground.this.mNavBarBg, NavigationBackground.this.mNavLayoutParams);
                return v.onApplyWindowInsets(insets);
            }
            NavigationBackground.this.removeNavBarBg();
            return v.onApplyWindowInsets(insets);
        }
    }

    private class WindowLayoutChangedListener implements OnLayoutChangeListener {
        /* synthetic */ WindowLayoutChangedListener(NavigationBackground this$0, WindowLayoutChangedListener -this1) {
            this();
        }

        private WindowLayoutChangedListener() {
        }

        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            Log.d(NavigationBackground.TAG, "onLayoutChange : " + left + " " + top + " " + right + "  " + bottom);
            boolean docked = NavigationBackground.this.isInSplitMode();
            if (NavigationBackground.this.mLastDocked != docked) {
                if (docked) {
                    NavigationBackground.this.mWindow.clearFlags(Integer.MIN_VALUE);
                } else {
                    NavigationBackground.this.mWindow.addFlags(Integer.MIN_VALUE);
                }
            }
            NavigationBackground.this.mLastDocked = docked;
        }
    }

    public NavigationBackground(Context context, Window w, Handler handler) {
        this.mContext = context;
        this.mWindow = w;
        this.mHandler = handler;
        this.mDismissDuration = this.mContext.getResources().getInteger(R.integer.navigationbar_exit_duration);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        initNavBarBg();
        initDecorView();
    }

    private void initNavBarBg() {
        this.mNavLayoutParams = new LayoutParams();
        LayoutParams layoutParams = this.mNavLayoutParams;
        layoutParams.flags |= -2147483384;
    }

    private void initDecorView() {
        int flags = 65792;
        this.mLastDocked = isInSplitMode();
        if (!this.mLastDocked) {
            flags = -2147417856;
        }
        this.mWindow.addFlags(flags);
        this.mWindow.getDecorView().setOnApplyWindowInsetsListener(new WindowInsetsListener(this, null));
        this.mWindow.getDecorView().addOnLayoutChangeListener(new WindowLayoutChangedListener(this, null));
    }

    public void initForActivity() {
        this.mInitForActivity = true;
        this.mNavLayoutParams.windowAnimations = R.style.Animation_Vigour_NavigationBarBg;
    }

    public void setDuration(int duration) {
        if (duration > 0) {
            duration = this.mDismissDuration;
        }
        this.mDismissDuration = duration;
    }

    public void dismiss() {
        if (this.mNavBarBg == null) {
            Log.w(TAG, "Not show NavigationBar, ingore dismiss...");
            return;
        }
        if (this.mInitForActivity) {
            removeNavBarBg();
        } else if (this.mNavBarBg.getParent() != null) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    NavigationBackground.this.removeNavBarBg();
                }
            }, (long) this.mDismissDuration);
        }
    }

    private void removeNavBarBg() {
        if (!(this.mNavBarBg == null || this.mNavBarBg.getParent() == null)) {
            this.mWindowManager.removeView(this.mNavBarBg);
        }
        this.mNavBarBg = null;
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
}
