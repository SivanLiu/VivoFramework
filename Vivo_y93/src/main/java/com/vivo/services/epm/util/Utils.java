package com.vivo.services.epm.util;

import android.app.StatusBarManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.SystemProperties;

public class Utils {
    private static final String PRODUCT_VERSION_PROP_NAME = "ro.vivo.product.version";
    private static String sSystemVersion = null;

    public static boolean isWifi(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (mConnectivityManager != null) {
            NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
            return info != null && info.getType() == 1;
        }
    }

    public static synchronized String getSystemVersion() {
        String str;
        synchronized (Utils.class) {
            if (sSystemVersion == null) {
                sSystemVersion = SystemProperties.get(PRODUCT_VERSION_PROP_NAME, "unknown");
            }
            str = sSystemVersion;
        }
        return str;
    }

    public static void collapsePanels(Context context) {
        try {
            ((StatusBarManager) context.getSystemService("statusbar")).collapsePanels();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isScreenOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
        if (powerManager != null) {
            return powerManager.isScreenOn();
        }
        return false;
    }
}
