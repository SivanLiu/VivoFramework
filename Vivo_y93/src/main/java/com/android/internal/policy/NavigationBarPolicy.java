package com.android.internal.policy;

import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.WindowState;

public abstract class NavigationBarPolicy {
    public static int APPLY_COLOR_FULL = 2;
    public static int APPLY_COLOR_NONE = 0;
    public static int APPLY_COLOR_NOT_MATCH = 1;
    public static int DEFAULT_COLOR = -592138;
    public static int DEFAULT_COLOR_GESTURE = DEFAULT_COLOR;

    public static boolean hasNavigationBarWindow(WindowState win, LayoutParams attrs) {
        boolean z = true;
        int type = attrs.type;
        if (type == LayoutParams.TYPE_INPUT_METHOD || type == LayoutParams.TYPE_INPUT_METHOD_DIALOG) {
            return true;
        }
        if (type < 1 || type > LayoutParams.LAST_SUB_WINDOW) {
            return type == LayoutParams.TYPE_SYSTEM_ALERT || type == LayoutParams.TYPE_SYSTEM_DIALOG;
        } else {
            if (win != null && win.getAppToken() == null) {
                z = false;
            }
            return z;
        }
    }

    public static boolean forceImmersive(LayoutParams attrs) {
        if (attrs.type == LayoutParams.TYPE_INPUT_METHOD || attrs.type == LayoutParams.TYPE_INPUT_METHOD_DIALOG) {
            return true;
        }
        return false;
    }
}
