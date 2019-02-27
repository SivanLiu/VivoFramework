package android.util;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.os.SystemProperties;
import com.android.internal.telephony.PhoneConstants;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public final class DoubleAppSwitcher {
    private static final String CTS_DEFAULT = "default";
    private static final String CTS_ENABLED = "1";
    private static final String CTS_PROP = "persist.debug.ctstest";
    private static final String DEBUG_DEFAULT = "default";
    private static final String DEBUG_ENABLED = "enable";
    private static final String DEBUG_PROP = "persist.vivo.double_debug";
    private static final String DEFAULT_SOLUTION = "default";
    private static final String G_CTS_DEFAULT = "default";
    private static final String G_CTS_ENABLED = "1";
    private static final String G_CTS_PROP = "ro.build.g_test";
    private static final String PROP = "persist.vivo.doubleinstance";
    private static final String SOLUTION = "multi_user";
    public static boolean sDebug = false;
    public static boolean sEnabled = false;

    public static void loadConfig() {
        if (SOLUTION.equals(SystemProperties.get(PROP, PhoneConstants.APN_TYPE_DEFAULT))) {
            sEnabled = true;
        } else {
            sEnabled = false;
        }
        if (DEBUG_ENABLED.equals(SystemProperties.get(DEBUG_PROP, PhoneConstants.APN_TYPE_DEFAULT))) {
            sDebug = true;
        } else {
            sDebug = false;
        }
    }

    public static boolean isInCts() {
        return !"1".equals(SystemProperties.get(CTS_PROP, PhoneConstants.APN_TYPE_DEFAULT)) ? "1".equals(SystemProperties.get(G_CTS_PROP, PhoneConstants.APN_TYPE_DEFAULT)) : true;
    }
}
