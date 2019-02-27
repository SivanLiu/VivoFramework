package com.vivo.common.autobrightness;

import android.os.Build.VERSION;

public final class ScreenState {
    private static final int ANDROID_KITKAT_SDK_INT = 19;
    private static final int POLICY_BRIGHT = 3;
    private static final int POLICY_DIM = 2;
    private static final int POLICY_DOZE = 1;
    private static final int POLICY_OFF = 0;
    private static final int SCREEN_STATE_BRIGHT = 2;
    private static final int SCREEN_STATE_DIM = 1;
    private static final int SCREEN_STATE_OFF = 0;
    public static final int STATE_SCREEN_BRIGHT;
    public static final int STATE_SCREEN_DIM;
    public static final int STATE_SCREEN_DOZE;
    public static final int STATE_SCREEN_OFF;

    static {
        if (VERSION.SDK_INT > 19) {
            STATE_SCREEN_OFF = 0;
            STATE_SCREEN_DOZE = 1;
            STATE_SCREEN_DIM = 2;
            STATE_SCREEN_BRIGHT = 3;
            return;
        }
        STATE_SCREEN_OFF = 0;
        STATE_SCREEN_DOZE = 1;
        STATE_SCREEN_DIM = 1;
        STATE_SCREEN_BRIGHT = 2;
    }
}
