package com.vivo.common.widget;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public interface BBKAnimWidgetBase {
    public static final int DIRECTION_FROM_LEFT = 0;
    public static final int DIRECTION_FROM_RIGHT = 1;
    public static final int DIRECTION_FROM_SELF = 2;
    public static final int MOTION_INIT = 0;
    public static final int MOTION_SCREEN_LOCK = 9;
    public static final int MOTION_SCREEN_UNLOCK = 1;
    public static final int MOTION_SCROLL_START = 6;
    public static final int MOTION_SCROLL_STOP = 2;
    public static final int MOTION_SORT_START = 7;
    public static final int MOTION_SORT_STOP = 3;
    public static final int MOTION_WINDOW_PAUSE = 8;
    public static final int MOTION_WINDOW_RESUME = 4;

    void onActive(int i, int i2);

    void onInactive(int i);
}
