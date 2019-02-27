package com.android.internal.policy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.Settings.Secure;
import android.provider.SettingsStringUtil;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.FtFeature;
import android.util.Log;
import android.view.IWindow;
import android.view.IWindowManager;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import com.android.internal.policy.NavigationBarImmersive.ImmersiveCallback;

public class NavigationBarWindow {
    private static final boolean DEBUG_SYSTEMUI = SystemProperties.getBoolean("persist.systemuilog.debug", false);
    private static final int DEFAULT_NAV_COLOR = NavigationBarPolicy.DEFAULT_COLOR;
    private static final int DEFAULT_NAV_COLOR_GESTURE = NavigationBarPolicy.DEFAULT_COLOR_GESTURE;
    private static final boolean DISABLE_IMMERSIVE = SystemProperties.getBoolean("persist.navcolor.disable", false);
    private static final boolean ENABLE_ALL = SystemProperties.getBoolean("persist.navcolor.enableAll", false);
    private static final boolean FORCE_DRAW = SystemProperties.getBoolean("persist.navcolor.force", false);
    private static final int MSG_APPLY_NAV_COLOR = 1;
    private static final int MSG_DRAW = 0;
    public static final int NAV_COLOR_POLICY_AUTO_IMMERSE = 1;
    public static final int NAV_COLOR_POLICY_FIX = 3;
    public static final int NAV_COLOR_POLICY_NONE = 0;
    public static final int NAV_COLOR_POLICY_SPECIFIC_COLOR = 2;
    public static final int PADDING_COLOR_POLICY_DARK = 1;
    public static final int PADDING_COLOR_POLICY_LIGHT = 2;
    public static final int PADDING_COLOR_POLICY_NONE = 0;
    public static final int PADDING_COLOR_POLICY_SPECIFIC_COLOR = 3;
    private static HandlerThread sHandlerThread = null;
    private boolean mApplyNavColor = false;
    private final LayoutParams mAttrs = new LayoutParams();
    private Callback mCallback;
    private final Context mContext;
    private final Handler mDrawHandler;
    private boolean mDrawNeeded = true;
    private boolean mForceHomeIndicatorColor = false;
    private boolean mForceNavigationBarColor = false;
    private int mForceNavigationBarColorRGB = -16777216;
    private boolean mHasApplyNavColor = false;
    private ImmersiveCallback mImmersiveCallback = new ImmersiveCallback() {
        public void updateNavigationColor(int color, int bgColor) {
            if (!PixelFormat.formatHasAlpha(NavigationBarWindow.this.mAttrs.format)) {
                color |= -16777216;
            }
            NavigationBarWindow.this.mApplyNavColor = true;
            NavigationBarWindow.this.setNavigationBarColorInner(color, bgColor);
        }
    };
    private String mLogTag = "NavigationBarWindow";
    private int mNavColorPolicy = 0;
    private int mNavigationBarColor = 0;
    private NavigationBarColorCallback mNavigationBarColorCallback = new NavigationBarColorCallback() {
        public void setNavigationBarColor(int color) {
            NavigationBarWindow.this.mForceNavigationBarColorRGB = color;
            NavigationBarWindow.this.mForceNavigationBarColor = true;
            NavigationBarWindow.this.applyNavColorPolicy();
        }
    };
    private NavigationBarImmersive mNavigationBarImmersive;
    private int mPaddingColor = -16777216;
    private int mPaddingColorPolicy = 0;
    private int mRotation = 0;
    private int mSystemUiVisibility = 0;
    private final View mView;
    private boolean mViewVisible = true;
    private int mVivoFixNavBarColor;
    private int mVivoSpecificNavBarColor;
    private final IWindow mWindow;

    public interface NavigationBarColorCallback {
        void setNavigationBarColor(int i);
    }

    public interface Callback {
        void setNavigationBarColorCallback(NavigationBarColorCallback navigationBarColorCallback);

        void updateNavigationBarColor(int i);
    }

    private class DrawHandler extends Handler {
        public DrawHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    NavigationBarWindow.this.drawNavigationBarInner((Surface) msg.obj);
                    return;
                case 1:
                    long start = 0;
                    if (NavigationBarController.DEBUG_TRACE) {
                        Trace.traceBegin(8, "applyNavColor:" + Integer.toHexString(msg.arg1));
                        start = SystemClock.uptimeMillis();
                    }
                    try {
                        NavigationBarWindow.getIWindowManager().applyNavColorForWindow(NavigationBarWindow.this.mWindow, msg.arg1);
                    } catch (Exception e) {
                        Log.e(NavigationBarWindow.this.mLogTag, "", e);
                    }
                    if (NavigationBarController.DEBUG_TRACE) {
                        Log.i(NavigationBarWindow.this.mLogTag, "applyNavColor cost: " + (SystemClock.uptimeMillis() - start) + "ms");
                        Trace.traceEnd(8);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private static synchronized HandlerThread getDrawingThread() {
        HandlerThread handlerThread;
        synchronized (NavigationBarWindow.class) {
            if (sHandlerThread == null) {
                sHandlerThread = new HandlerThread("NavDrawThread");
                sHandlerThread.start();
            }
            handlerThread = sHandlerThread;
        }
        return handlerThread;
    }

    public NavigationBarWindow(View view, IWindow window, LayoutParams lp) {
        this.mView = view;
        this.mWindow = window;
        this.mContext = view.getContext();
        initNavColorPolicy(lp);
        if (this.mNavColorPolicy == 1) {
            this.mDrawHandler = new DrawHandler(getDrawingThread().getLooper());
        } else {
            this.mDrawHandler = new DrawHandler(Looper.myLooper());
        }
        applyNavColorPolicy();
    }

    private static IWindowManager getIWindowManager() {
        WindowManagerGlobal.getInstance();
        return WindowManagerGlobal.getWindowManagerService();
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
        this.mCallback.setNavigationBarColorCallback(this.mNavigationBarColorCallback);
        this.mCallback.updateNavigationBarColor(this.mNavigationBarColor);
    }

    private void setNavigationBarColorInner(int color) {
        setNavigationBarColorInner(color, color);
    }

    private void setNavigationBarColorInner(int color, int bgColor) {
        if (this.mNavigationBarColor != color || !this.mHasApplyNavColor) {
            this.mNavigationBarColor = color;
            this.mDrawNeeded = true;
            if (DEBUG_SYSTEMUI) {
                Log.i(this.mLogTag, "setNavigationBarColorInner, color = " + Integer.toHexString(color) + ", applyColor = " + this.mApplyNavColor, new RuntimeException().fillInStackTrace());
            }
            if (this.mCallback != null) {
                this.mCallback.updateNavigationBarColor(color);
            }
            if (this.mApplyNavColor) {
                this.mDrawHandler.removeMessages(1);
                this.mDrawHandler.sendMessage(this.mDrawHandler.obtainMessage(1, bgColor, 0));
                this.mHasApplyNavColor = true;
            }
        }
    }

    private int getRotation() {
        try {
            return this.mContext.getDisplay().getRotation();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private boolean isHomeIndicatorOn() {
        try {
            return "true".equals(getIWindowManager().fetchSystemSetting("homeIndicator_on"));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isGestureBarOn() {
        try {
            return "true".equals(getIWindowManager().fetchSystemSetting("gesturebar_on"));
        } catch (Exception e) {
            return false;
        }
    }

    public void updateAttributes(LayoutParams lp) {
        if (this.mAttrs.copyFrom(lp) != 0) {
            applyNavColorPolicy();
            updateLogTag(this.mAttrs);
            if (this.mNavigationBarImmersive != null) {
                this.mNavigationBarImmersive.updateLogTag(this.mAttrs);
            }
        }
    }

    public void onConfigurationChanged() {
        int rotation = getRotation();
        if (this.mRotation != rotation) {
            this.mRotation = rotation;
            applyNavColorPolicy();
        }
    }

    public void onWindowVisibilityChanged(boolean visible) {
        this.mViewVisible = visible;
        applyNavColorPolicy();
    }

    public void onSystemUIVisibilityChanged(int systemUiVisibility) {
        if (this.mSystemUiVisibility != systemUiVisibility) {
            if (DEBUG_SYSTEMUI) {
                Log.i(this.mLogTag, "onSystemUIVisibilityChanged, systemUiVisibility = " + Integer.toHexString(systemUiVisibility) + ", callers = " + Debug.getCallers(5));
            }
            this.mSystemUiVisibility = systemUiVisibility;
            this.mHasApplyNavColor = false;
            applyNavColorPolicy();
        }
    }

    public void drawNavigationBar(Surface surface, boolean forceDraw) {
        if (DEBUG_SYSTEMUI) {
            Log.i(this.mLogTag, "drawNavigationBar, mNavigationBarColor = " + Integer.toHexString(this.mNavigationBarColor) + ", mDrawNedded = " + this.mDrawNeeded + ", forceDraw = " + forceDraw + ", FORCE_DRAW = " + FORCE_DRAW);
        }
        this.mDrawHandler.removeMessages(0);
        if (this.mDrawNeeded || FORCE_DRAW || forceDraw) {
            this.mDrawHandler.sendMessageAtFrontOfQueue(this.mDrawHandler.obtainMessage(0, surface));
        }
    }

    private void drawNavigationBarInner(Surface surface) {
        Object syncObj = null;
        if (surface != null) {
            syncObj = surface.getSurfaceSyncObj();
        }
        if (syncObj != null) {
            synchronized (syncObj) {
                if (surface != null) {
                    if (surface.isValid()) {
                        long start = 0;
                        if (NavigationBarController.DEBUG_TRACE) {
                            Trace.traceBegin(8, "drawNavigationBar:" + Integer.toHexString(this.mNavigationBarColor));
                            start = SystemClock.uptimeMillis();
                        }
                        try {
                            Canvas canvas;
                            Surface subSurface = surface.getSubSurface();
                            if (subSurface != null && subSurface.isValid()) {
                                if (DEBUG_SYSTEMUI) {
                                    Log.i(this.mLogTag, "drawNavigationBarInner, subSurface color = " + -16777216);
                                }
                                canvas = subSurface.lockCanvas(null);
                                canvas.drawColor(0, Mode.CLEAR);
                                canvas.drawColor(-16777216);
                                subSurface.unlockCanvasAndPost(canvas);
                            }
                            canvas = surface.lockCanvas(null);
                            canvas.drawColor(0, Mode.CLEAR);
                            canvas.drawColor(this.mNavigationBarColor);
                            surface.unlockCanvasAndPost(canvas);
                            this.mDrawNeeded = false;
                        } catch (Exception e) {
                        }
                        if (NavigationBarController.DEBUG_TRACE) {
                            Log.i(this.mLogTag, "drawNavigationBar cost: " + (SystemClock.uptimeMillis() - start) + "ms");
                            Trace.traceEnd(8);
                        }
                    }
                }
            }
        }
    }

    private void initNavColorPolicy(LayoutParams lp) {
        this.mAttrs.copyFrom(lp);
        updateLogTag(this.mAttrs);
        this.mSystemUiVisibility = this.mAttrs.systemUiVisibility | this.mAttrs.subtreeSystemUiVisibility;
        try {
            IWindowManager wm = getIWindowManager();
            this.mRotation = getRotation();
            String[] policyArray = wm.getNavigationBarPolicy(this.mWindow).split(",");
            String navColorPolicy = (policyArray == null || policyArray.length <= 0) ? "" : policyArray[0];
            String navFixColor = (policyArray == null || policyArray.length <= 1) ? "" : policyArray[1];
            String paddingColorPolicy = (policyArray == null || policyArray.length <= 2) ? "" : policyArray[2];
            if (DISABLE_IMMERSIVE) {
                if (!TextUtils.isEmpty(navColorPolicy) && ("IMMERSE".equals(navColorPolicy) ^ 1) != 0 && (KeyProperties.DIGEST_NONE.equals(navColorPolicy) ^ 1) != 0) {
                    this.mVivoSpecificNavBarColor = Color.parseColor(navColorPolicy);
                    this.mNavColorPolicy = 2;
                } else if (TextUtils.isEmpty(navFixColor)) {
                    this.mNavColorPolicy = 0;
                } else {
                    this.mVivoFixNavBarColor = Color.parseColor(navFixColor);
                    this.mNavColorPolicy = 3;
                }
            } else if (TextUtils.isEmpty(navColorPolicy)) {
                if (TextUtils.isEmpty(navFixColor)) {
                    this.mNavColorPolicy = 0;
                } else {
                    this.mVivoFixNavBarColor = Color.parseColor(navFixColor);
                    this.mNavColorPolicy = 3;
                }
            } else if ("IMMERSE".equals(navColorPolicy)) {
                this.mNavColorPolicy = 1;
            } else if (KeyProperties.DIGEST_NONE.equals(navColorPolicy)) {
                this.mNavColorPolicy = 0;
            } else {
                this.mVivoSpecificNavBarColor = Color.parseColor(navColorPolicy);
                this.mNavColorPolicy = 2;
            }
            if (DEBUG_SYSTEMUI) {
                Log.d(this.mLogTag, "initNavColorPolicy, navColorPolicy=" + navColorPolicy + " paddingColorPolicy=" + paddingColorPolicy);
            }
            if (!TextUtils.isEmpty(paddingColorPolicy)) {
                if ("DARK".equals(paddingColorPolicy)) {
                    this.mPaddingColorPolicy = 1;
                } else if ("LIGHT".equals(paddingColorPolicy)) {
                    this.mPaddingColorPolicy = 2;
                }
                if (DEBUG_SYSTEMUI) {
                    Log.d(this.mLogTag, "initNavColorPolicy, navColorPolicy=" + navColorPolicy + " paddingColorPolicy=" + paddingColorPolicy);
                }
            }
            if (ENABLE_ALL && this.mNavColorPolicy == 0) {
                this.mNavColorPolicy = 1;
            }
        } catch (Exception e) {
            Log.e(this.mLogTag, "", e);
        }
    }

    private void applyNavColorPolicy() {
        boolean doDisableImmersive;
        if (DEBUG_SYSTEMUI) {
            Log.i(this.mLogTag, "applyNavColorPolicy, mSystemUiVisibility = " + Integer.toHexString(this.mSystemUiVisibility) + ", mAttrs.flags = " + this.mAttrs.flags);
        }
        int sysUiFlag = this.mSystemUiVisibility;
        int flags = this.mAttrs.flags;
        boolean navHidden = (sysUiFlag & 2) != 0;
        boolean navTranslucent = ((Integer.MIN_VALUE & sysUiFlag) == 0 || (Integer.MIN_VALUE & flags) != 0) ? (134217728 & flags) != 0 : true;
        boolean navTranslucentOrHidden = !navHidden ? navTranslucent : true;
        boolean forceNavigationBarColor = this.mForceNavigationBarColor ? (Integer.MIN_VALUE & flags) != 0 : false;
        boolean forceSettingsColor = getSystemSettingColor() != DEFAULT_NAV_COLOR;
        boolean forceHomeIndicatorColor = this.mForceHomeIndicatorColor;
        boolean homeIndicatorTransparent = this.mAttrs.homeIndicatorState != -1;
        boolean isPortrait = this.mRotation == 0;
        int isAspectRestricted = 0;
        try {
            isAspectRestricted = getIWindowManager().isAspectRestricted(this.mWindow);
        } catch (Exception e) {
        }
        boolean isHomeIndicatorOn = isHomeIndicatorOn();
        boolean isHomeIndicatorBlank = Secure.getInt(this.mContext.getContentResolver(), "navigation_home_indicator_icon_style", 10) == 12;
        if (!isHomeIndicatorOn) {
            homeIndicatorTransparent = false;
            forceHomeIndicatorColor = false;
        } else if (isHomeIndicatorBlank) {
            if (DEBUG_SYSTEMUI) {
                Log.i(this.mLogTag, "applyNavColorPolicy: no ear phone feature.");
            }
            this.mNavColorPolicy = 0;
            forceSettingsColor = false;
            navTranslucentOrHidden = false;
            forceNavigationBarColor = false;
            homeIndicatorTransparent = false;
            forceHomeIndicatorColor = false;
        } else if (isPortrait && this.mNavColorPolicy != 0) {
            forceSettingsColor = false;
            navTranslucentOrHidden = false;
            forceNavigationBarColor = false;
        }
        int isInMultiWindow = 0;
        try {
            isInMultiWindow = getIWindowManager().isInMultiWindow();
        } catch (Exception e2) {
        }
        if (!(isPortrait || isInMultiWindow == 0)) {
            homeIndicatorTransparent = false;
            forceHomeIndicatorColor = false;
        }
        if (forceNavigationBarColor || ((navTranslucentOrHidden && (isAspectRestricted ^ 1) != 0) || forceSettingsColor || forceHomeIndicatorColor || ((homeIndicatorTransparent && (isAspectRestricted ^ 1) != 0) || (navHidden && (homeIndicatorTransparent ^ 1) != 0)))) {
            if (DEBUG_SYSTEMUI) {
                Log.i(this.mLogTag, "applyNavColorPolicy: forceNavigationBarColor = " + forceNavigationBarColor + ", navTranslucentOrHidden = " + navTranslucentOrHidden);
            }
            doDisableImmersive = true;
            this.mApplyNavColor = true;
            if (!forceNavigationBarColor && forceSettingsColor) {
                setNavigationBarColorInner(getSystemSettingColor());
            } else if (!homeIndicatorTransparent) {
                setNavigationBarColorInner(this.mForceNavigationBarColorRGB);
            } else if (this.mAttrs.homeIndicatorState == 0 || this.mAttrs.homeIndicatorState == 2) {
                setNavigationBarColorInner(-1);
            } else {
                setNavigationBarColorInner(-16777216);
            }
        } else if (!isPortrait || this.mNavColorPolicy == 0) {
            doDisableImmersive = true;
            this.mApplyNavColor = true;
            if (isAspectRestricted != 0) {
                if (isPortrait) {
                    if (isHomeIndicatorOn) {
                        setNavigationBarColorInner(DEFAULT_NAV_COLOR_GESTURE);
                    } else if (navHidden) {
                        setNavigationBarColorInner(-16777216);
                    } else {
                        setNavigationBarColorInner(getSystemSettingColor());
                        if (DEBUG_SYSTEMUI) {
                            Log.i(this.mLogTag, "applyNavColorPolicy: setting color");
                        }
                    }
                } else if (isInMultiWindow == 0) {
                    setNavigationBarColorInner(-16777216);
                } else if (((sysUiFlag & 32) == 0 || this.mPaddingColorPolicy == 2) && this.mPaddingColorPolicy != 1) {
                    setNavigationBarColorInner(DEFAULT_NAV_COLOR_GESTURE);
                } else {
                    setNavigationBarColorInner(-16777216);
                }
            } else if (isPortrait) {
                if (isHomeIndicatorOn) {
                    setNavigationBarColorInner(DEFAULT_NAV_COLOR_GESTURE);
                } else if (navHidden) {
                    setNavigationBarColorInner(-16777216);
                } else {
                    setNavigationBarColorInner(getSystemSettingColor());
                    if (DEBUG_SYSTEMUI) {
                        Log.i(this.mLogTag, "applyNavColorPolicy: setting color");
                    }
                }
            } else if ((!FtFeature.isFeatureSupport(32) || this.mAttrs.keepFullScreen == 1) && (isHomeIndicatorOn ^ 1) != 0 && (isInMultiWindow ^ 1) != 0) {
                setNavigationBarColorInner(getSystemSettingColor());
            } else if (isInMultiWindow != 0) {
                if (((sysUiFlag & 32) == 0 || this.mPaddingColorPolicy == 2) && this.mPaddingColorPolicy != 1) {
                    setNavigationBarColorInner(DEFAULT_NAV_COLOR_GESTURE);
                } else {
                    setNavigationBarColorInner(-16777216);
                }
            } else if (this.mPaddingColorPolicy == 1) {
                setNavigationBarColorInner(-16777216);
            } else if (this.mPaddingColorPolicy == 2) {
                setNavigationBarColorInner(DEFAULT_NAV_COLOR_GESTURE);
            } else {
                setNavigationBarColorInner(getSystemSettingColor());
            }
        } else if (this.mNavColorPolicy == 1) {
            doDisableImmersive = this.mViewVisible ^ 1;
            this.mApplyNavColor = false;
            if (DEBUG_SYSTEMUI) {
                Log.i(this.mLogTag, "applyNavColorPolicy: immersive");
            }
        } else if (this.mNavColorPolicy == 2) {
            doDisableImmersive = true;
            this.mApplyNavColor = true;
            setNavigationBarColorInner(this.mVivoSpecificNavBarColor);
            if (DEBUG_SYSTEMUI) {
                Log.i(this.mLogTag, "applyNavColorPolicy: specific color = " + this.mVivoSpecificNavBarColor);
            }
        } else if (this.mNavColorPolicy == 3) {
            doDisableImmersive = true;
            this.mApplyNavColor = true;
            setNavigationBarColorInner(this.mVivoFixNavBarColor);
            if (DEBUG_SYSTEMUI) {
                Log.i(this.mLogTag, "applyNavColorPolicy: fix color = " + this.mVivoFixNavBarColor);
            }
        } else {
            doDisableImmersive = true;
            this.mApplyNavColor = true;
        }
        if (!doDisableImmersive) {
            if (this.mNavigationBarImmersive == null) {
                this.mNavigationBarImmersive = new NavigationBarImmersive(this.mView);
                setNavigationBarColorInner(DEFAULT_NAV_COLOR);
            }
            this.mNavigationBarImmersive.setEnable(true);
            this.mNavigationBarImmersive.setImmersiveCallback(this.mImmersiveCallback);
        } else if (this.mNavigationBarImmersive != null) {
            this.mNavigationBarImmersive.setEnable(false);
            this.mNavigationBarImmersive.setImmersiveCallback(null);
        }
    }

    public static int getSystemSettingColor() {
        if (!DISABLE_IMMERSIVE) {
            return DEFAULT_NAV_COLOR;
        }
        try {
            String navColorString = getIWindowManager().fetchSystemSetting("nav_color");
            if (!TextUtils.isEmpty(navColorString)) {
                return Color.parseColor(navColorString);
            }
        } catch (Exception e) {
        }
        return DEFAULT_NAV_COLOR;
    }

    private void updateLogTag(LayoutParams params) {
        this.mLogTag = "NavigationBarWindow[" + getTitleSuffix(params) + (params != null ? SettingsStringUtil.DELIMITER + params.type : "") + "]";
    }

    private static String getTitleSuffix(LayoutParams params) {
        if (params == null) {
            return "";
        }
        String[] split = params.getTitle().toString().split("\\.");
        if (split.length > 0) {
            return split[split.length - 1];
        }
        return "";
    }
}
