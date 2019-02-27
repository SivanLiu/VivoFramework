package android.view;

import android.content.Context;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Slog;
import android.view.InputEventReceiver.Factory;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IShortcutService;
import java.io.PrintWriter;

public interface WindowManagerPolicy {
    public static final String ACTION_HDMI_PLUGGED = "android.intent.action.HDMI_PLUGGED";
    public static final int ACTION_PASS_TO_USER = 1;
    public static final int APPLICATION_ABOVE_SUB_PANEL_SUBLAYER = 3;
    public static final int APPLICATION_LAYER = 2;
    public static final int APPLICATION_MEDIA_OVERLAY_SUBLAYER = -1;
    public static final int APPLICATION_MEDIA_SUBLAYER = -2;
    public static final int APPLICATION_PANEL_SUBLAYER = 1;
    public static final int APPLICATION_SUB_PANEL_SUBLAYER = 2;
    public static final String EXTRA_FROM_HOME_KEY = "android.intent.extra.FROM_HOME_KEY";
    public static final String EXTRA_HDMI_PLUGGED_STATE = "state";
    public static final int FINISH_LAYOUT_REDO_ANIM = 8;
    public static final int FINISH_LAYOUT_REDO_CONFIG = 2;
    public static final int FINISH_LAYOUT_REDO_LAYOUT = 1;
    public static final int FINISH_LAYOUT_REDO_WALLPAPER = 4;
    public static final int FLAG_DISABLE_KEY_REPEAT = 134217728;
    public static final int FLAG_FILTERED = 67108864;
    public static final int FLAG_INJECTED = 16777216;
    public static final int FLAG_INTERACTIVE = 536870912;
    public static final int FLAG_PASS_TO_USER = 1073741824;
    public static final int FLAG_TRUSTED = 33554432;
    public static final int FLAG_VIRTUAL = 2;
    public static final int FLAG_WAKE = 1;
    public static final int KEYGUARD_GOING_AWAY_FLAG_NO_WINDOW_ANIMATIONS = 2;
    public static final int KEYGUARD_GOING_AWAY_FLAG_TO_SHADE = 1;
    public static final int KEYGUARD_GOING_AWAY_FLAG_WITH_WALLPAPER = 4;
    public static final int NAV_BAR_BOTTOM = 4;
    public static final int NAV_BAR_LEFT = 1;
    public static final int NAV_BAR_RIGHT = 2;
    public static final int OFF_BECAUSE_OF_ADMIN = 1;
    public static final int OFF_BECAUSE_OF_PROX_SENSOR = 4;
    public static final int OFF_BECAUSE_OF_TIMEOUT = 3;
    public static final int OFF_BECAUSE_OF_USER = 2;
    public static final int PRESENCE_EXTERNAL = 2;
    public static final int PRESENCE_INTERNAL = 1;
    public static final int TRANSIT_ENTER = 1;
    public static final int TRANSIT_EXIT = 2;
    public static final int TRANSIT_HIDE = 4;
    public static final int TRANSIT_PREVIEW_DONE = 5;
    public static final int TRANSIT_SHOW = 3;
    public static final int USER_ROTATION_FREE = 0;
    public static final int USER_ROTATION_LOCKED = 1;
    public static final boolean WATCH_POINTER = false;

    public interface InputConsumer {
        void dismiss();
    }

    public interface OnKeyguardExitResult {
        void onKeyguardExitResult(boolean z);
    }

    public interface PointerEventListener {
        void onPointerEvent(MotionEvent motionEvent);

        void onPointerEvent(MotionEvent motionEvent, int displayId) {
            if (displayId == 0) {
                onPointerEvent(motionEvent);
            }
        }
    }

    public interface ScreenOffListener {
        void onScreenOff();
    }

    public interface ScreenOnListener {
        void onScreenOn();
    }

    public interface StartingSurface {
        void remove();
    }

    public interface WindowManagerFuncs {
        public static final int CAMERA_LENS_COVERED = 1;
        public static final int CAMERA_LENS_COVER_ABSENT = -1;
        public static final int CAMERA_LENS_UNCOVERED = 0;
        public static final int LID_ABSENT = -1;
        public static final int LID_CLOSED = 0;
        public static final int LID_OPEN = 1;

        InputConsumer createInputConsumer(Looper looper, String str, Factory factory);

        int getCameraLensCoverState();

        int getDockedDividerInsetsLw();

        WindowState getInputMethodWindowLw();

        int getLidState();

        void getStackBounds(int i, Rect rect);

        Object getWindowManagerLock();

        void hideKeyguardByFingerprint(int i);

        boolean isWallpaperTarget(WindowState windowState);

        void lockDeviceNow();

        void notifyKeyguardTrustedChanged();

        void notifyShowingDreamChanged();

        void onKeyguardShowingAndNotOccludedChanged();

        void reboot(boolean z);

        void rebootSafeMode(boolean z);

        void reevaluateStatusBarVisibility();

        void registerPointerEventListener(PointerEventListener pointerEventListener);

        void screenTurningOff(ScreenOffListener screenOffListener);

        void shutdown(boolean z);

        void switchInputMethod(boolean z);

        void unregisterPointerEventListener(PointerEventListener pointerEventListener);
    }

    public interface WindowState {
        boolean canAcquireSleepToken();

        boolean canAffectSystemUiFlags();

        void computeFrameLw(Rect rect, Rect rect2, Rect rect3, Rect rect4, Rect rect5, Rect rect6, Rect rect7, Rect rect8);

        void getAppBounds(Rect rect);

        IApplicationToken getAppToken();

        int getAspectRatioResizeType();

        LayoutParams getAttrs();

        int getBaseType();

        Rect getContentFrameLw();

        Rect getDisplayFrameLw();

        int getDisplayId();

        Rect getFrameLw();

        Rect getGivenContentInsetsLw();

        boolean getGivenInsetsPendingLw();

        Rect getGivenVisibleInsetsLw();

        boolean getNeedsMenuLw(WindowState windowState);

        Rect getOverscanFrameLw();

        String getOwningPackage();

        int getOwningUid();

        int getRotationAnimationHint();

        Point getShownPositionLw();

        int getStackId();

        int getSurfaceLayer();

        int getSystemUiVisibility();

        Rect getVisibleFrameLw();

        int getWindowNavColor();

        boolean hasAppShownWindows();

        boolean hasDrawnLw();

        void hideByAOD(boolean z);

        boolean hideLw(boolean z);

        boolean inFreeformWorkspace();

        boolean isAlive();

        boolean isAnimatingLw();

        boolean isDefaultDisplay();

        boolean isDimming();

        boolean isDisplayedLw();

        boolean isDrawnLw();

        boolean isGoneForLayoutLw();

        boolean isInMultiWindowMode();

        boolean isInputMethodWindow();

        boolean isVisibleLw();

        boolean isVoiceInteraction();

        void setAspectRatioResizeType(int i);

        int shouldApplyNavColor();

        boolean showLw(boolean z);

        boolean canAddInternalSystemWindow() {
            return false;
        }
    }

    StartingSurface addSplashScreen(IBinder iBinder, String str, int i, CompatibilityInfo compatibilityInfo, CharSequence charSequence, int i2, int i3, int i4, int i5, Configuration configuration, int i6);

    void adjustConfigurationLw(Configuration configuration, int i, int i2);

    int adjustSystemUiVisibilityLw(int i);

    void adjustWindowParamsLw(LayoutParams layoutParams);

    boolean allowAppAnimationsLw();

    boolean allowOverlayKeyguard(String str);

    boolean allowOverlayKeyguard(String str, String str2, int i);

    void applyPostLayoutPolicyLw(WindowState windowState, LayoutParams layoutParams, WindowState windowState2, WindowState windowState3);

    void beginLayoutLw(boolean z, int i, int i2, int i3, int i4);

    void beginPostLayoutPolicyLw(int i, int i2);

    boolean canBeHiddenByKeyguardLw(WindowState windowState);

    boolean canDismissBootAnimation();

    boolean canMagnifyWindow(int i);

    int checkAddPermission(LayoutParams layoutParams, int[] iArr);

    boolean checkShowToOwnerOnly(LayoutParams layoutParams);

    Animation createHiddenByKeyguardExit(boolean z, boolean z2);

    Animation createKeyguardWallpaperExit(boolean z);

    void dismissKeyguardLw(IKeyguardDismissCallback iKeyguardDismissCallback);

    KeyEvent dispatchUnhandledKey(WindowState windowState, KeyEvent keyEvent, int i);

    void displayReady();

    void dump(String str, PrintWriter printWriter, String[] strArr);

    void enableKeyguard(boolean z);

    void enableScreenAfterBoot();

    void exitKeyguardSecurely(OnKeyguardExitResult onKeyguardExitResult);

    void finishLayoutLw();

    int finishPostLayoutPolicyLw();

    void finishedGoingToSleep(int i);

    void finishedWakingUp();

    int focusChangedLw(WindowState windowState, WindowState windowState2);

    void forceHideNavBar(IBinder iBinder, boolean z);

    int getConfigDisplayHeight(int i, int i2, int i3, int i4, int i5);

    int getConfigDisplayWidth(int i, int i2, int i3, int i4, int i5);

    void getContentRectLw(Rect rect);

    int getInputMethodWindowVisibleHeightLw();

    boolean getInsetHintLw(LayoutParams layoutParams, Rect rect, int i, int i2, int i3, Rect rect2, Rect rect3, Rect rect4);

    int getMaxWallpaperLayer();

    int getNavBarPosition();

    int getNonDecorDisplayHeight(int i, int i2, int i3, int i4, int i5);

    int getNonDecorDisplayWidth(int i, int i2, int i3, int i4, int i5);

    void getNonDecorInsetsLw(int i, int i2, int i3, Rect rect);

    int getSensorOrientation();

    void getStableInsetsLw(int i, int i2, int i3, Rect rect);

    int getSystemDecorLayerLw();

    int getUserRotationMode();

    boolean hasNavigationBar();

    void hideBootMessages();

    void hideKeyguardByFingerprint(int i);

    boolean inKeyguardRestrictedKeyInputMode();

    void init(Context context, IWindowManager iWindowManager, WindowManagerFuncs windowManagerFuncs);

    long interceptKeyBeforeDispatching(WindowState windowState, KeyEvent keyEvent, int i);

    int interceptKeyBeforeQueueing(KeyEvent keyEvent, int i);

    int interceptMotionBeforeQueueingNonInteractive(long j, int i);

    boolean isButtonNeedReflect();

    boolean isDefaultOrientationForced();

    boolean isDockSideAllowed(int i);

    boolean isKeyguardDrawnLw();

    boolean isKeyguardHostWindow(LayoutParams layoutParams);

    boolean isKeyguardLocked();

    boolean isKeyguardOccluded();

    boolean isKeyguardSecure(int i);

    boolean isKeyguardShowingAndNotOccluded();

    boolean isKeyguardTrustedLw();

    boolean isMultiNotPaintEarWhenPortrait();

    boolean isNavBarForcedShownLw(WindowState windowState);

    boolean isScreenOn();

    boolean isShowingDreamLw();

    boolean isTopLevelWindow(int i);

    void keepScreenOnStartedLw();

    void keepScreenOnStoppedLw();

    void layoutWindowLw(WindowState windowState, WindowState windowState2);

    void lockNow(Bundle bundle);

    void notifyCameraLensCoverSwitchChanged(long j, boolean z);

    void notifyLidSwitchChanged(long j, boolean z);

    void notifySoftKeyboardShown(boolean z);

    boolean okToAnimate();

    void onConfigurationChanged();

    void onKeyguardOccludedChangedLw(boolean z);

    void onKeyguardShown(boolean z);

    void onSystemUiStarted();

    boolean performHapticFeedbackLw(WindowState windowState, int i, boolean z);

    int prepareAddWindowLw(WindowState windowState, LayoutParams layoutParams);

    void registerShortcutKey(long j, IShortcutService iShortcutService) throws RemoteException;

    void removeWindowLw(WindowState windowState);

    int rotationForOrientationLw(int i, int i2);

    boolean rotationHasCompatibleMetricsLw(int i, int i2);

    void screenTurnedOff();

    void screenTurnedOn();

    void screenTurningOff(ScreenOffListener screenOffListener);

    void screenTurningOn(ScreenOnListener screenOnListener);

    int selectAnimationLw(WindowState windowState, int i);

    void selectRotationAnimationLw(int[] iArr);

    void sendFingerprintResult(int i);

    void setCurrentOrientationLw(int i);

    void setCurrentUserLw(int i);

    void setDisplayOverscan(Display display, int i, int i2, int i3, int i4);

    void setHideByFingerPrint(boolean z);

    void setInitialDisplaySize(Display display, int i, int i2, int i3);

    boolean setKeyguardHide(int i);

    void setLastInputMethodWindowLw(WindowState windowState, WindowState windowState2);

    void setOrientationListener(boolean z);

    void setPipVisibilityLw(boolean z);

    void setRecentsVisibilityLw(boolean z);

    void setRotationLw(int i);

    void setSafeMode(boolean z);

    void setSwitchingUser(boolean z);

    void setUserRotationMode(int i, int i2);

    boolean shouldRotateSeamlessly(int i, int i2);

    void showBootMessage(CharSequence charSequence, boolean z);

    void showGlobalActions();

    void showRecentApps(boolean z);

    void startKeyguardExitAnimation(long j, long j2);

    void startedGoingToSleep(int i);

    void startedWakingUp();

    void startedWakingUpForReason(int i);

    void systemBooted();

    void systemReady();

    void userActivity();

    boolean validateRotationAnimationLw(int i, int i2, boolean z);

    int getWindowLayerLw(WindowState win) {
        return getWindowLayerFromTypeLw(win.getBaseType(), win.canAddInternalSystemWindow());
    }

    int getWindowLayerFromTypeLw(int type) {
        if (!LayoutParams.isSystemAlertWindowType(type)) {
            return getWindowLayerFromTypeLw(type, false);
        }
        throw new IllegalArgumentException("Use getWindowLayerFromTypeLw() or getWindowLayerLw() for alert window types");
    }

    int getWindowLayerFromTypeLw(int type, boolean canAddInternalSystemWindow) {
        int i = 11;
        int i2 = 10;
        if (type >= 1 && type <= 99) {
            return 2;
        }
        switch (type) {
            case 2000:
                return 18;
            case 2001:
            case LayoutParams.TYPE_VOICE_INTERACTION_STARTING /*2033*/:
                return 4;
            case 2002:
                return 3;
            case LayoutParams.TYPE_SYSTEM_ALERT /*2003*/:
                if (!canAddInternalSystemWindow) {
                    i = 10;
                }
                return i;
            case LayoutParams.TYPE_TOAST /*2005*/:
                return 8;
            case LayoutParams.TYPE_SYSTEM_OVERLAY /*2006*/:
                if (canAddInternalSystemWindow) {
                    i = 22;
                }
                return i;
            case LayoutParams.TYPE_PRIORITY_PHONE /*2007*/:
                return 9;
            case LayoutParams.TYPE_SYSTEM_DIALOG /*2008*/:
                return 7;
            case LayoutParams.TYPE_KEYGUARD_DIALOG /*2009*/:
                return 20;
            case LayoutParams.TYPE_SYSTEM_ERROR /*2010*/:
                if (canAddInternalSystemWindow) {
                    i2 = 26;
                }
                return i2;
            case LayoutParams.TYPE_INPUT_METHOD /*2011*/:
                return 14;
            case LayoutParams.TYPE_INPUT_METHOD_DIALOG /*2012*/:
                return 15;
            case LayoutParams.TYPE_WALLPAPER /*2013*/:
                return 1;
            case LayoutParams.TYPE_STATUS_BAR_PANEL /*2014*/:
                return 19;
            case LayoutParams.TYPE_SECURE_SYSTEM_OVERLAY /*2015*/:
                return 31;
            case LayoutParams.TYPE_DRAG /*2016*/:
                return 29;
            case LayoutParams.TYPE_STATUS_BAR_SUB_PANEL /*2017*/:
                return 17;
            case LayoutParams.TYPE_POINTER /*2018*/:
                return 33;
            case LayoutParams.TYPE_NAVIGATION_BAR /*2019*/:
                return 23;
            case LayoutParams.TYPE_VOLUME_OVERLAY /*2020*/:
                return 21;
            case LayoutParams.TYPE_BOOT_PROGRESS /*2021*/:
                return 32;
            case LayoutParams.TYPE_INPUT_CONSUMER /*2022*/:
                return 6;
            case LayoutParams.TYPE_DREAM /*2023*/:
                return 13;
            case LayoutParams.TYPE_NAVIGATION_BAR_PANEL /*2024*/:
                return 24;
            case LayoutParams.TYPE_DISPLAY_OVERLAY /*2026*/:
                return 28;
            case LayoutParams.TYPE_MAGNIFICATION_OVERLAY /*2027*/:
                return 27;
            case LayoutParams.TYPE_PRIVATE_PRESENTATION /*2030*/:
            case LayoutParams.TYPE_PRESENTATION /*2037*/:
                return 2;
            case LayoutParams.TYPE_VOICE_INTERACTION /*2031*/:
                return 5;
            case LayoutParams.TYPE_ACCESSIBILITY_OVERLAY /*2032*/:
                return 30;
            case LayoutParams.TYPE_DOCK_DIVIDER /*2034*/:
                return 2;
            case LayoutParams.TYPE_QS_DIALOG /*2035*/:
                return 2;
            case LayoutParams.TYPE_SCREENSHOT /*2036*/:
                return 25;
            case LayoutParams.TYPE_APPLICATION_OVERLAY /*2038*/:
                return 12;
            case LayoutParams.TYPE_HOME_INDICATOR /*2998*/:
                return 23;
            case 2999:
                return 2;
            default:
                Slog.e("WindowManager", "Unknown window type: " + type);
                return 2;
        }
    }

    int getSubWindowLayerFromTypeLw(int type) {
        switch (type) {
            case 1000:
            case 1003:
                return 1;
            case 1001:
                return -2;
            case 1002:
                return 2;
            case 1004:
                return -1;
            case 1005:
                return 3;
            default:
                Slog.e("WindowManager", "Unknown sub-window type: " + type);
                return 0;
        }
    }

    void setDismissImeOnBackKeyPressed(boolean newValue) {
    }
}
