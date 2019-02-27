package com.vivo.content;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoConstants {
    public static String MTK_PLATFORM = "MTK";
    public static String PLATFORM_TAG = "ro.vivo.product.solution";
    public static String PRODUCT_TAG = "ro.product.model.bbk";
    public static String QCOM_PLATFORM = "QCOM";
    public static int SIM_SLOT_1 = 0;
    public static int SIM_SLOT_2 = 1;

    public static class VivoKeyMgmt {
        public static final int WAPI_CERT = 5;
        public static final int WAPI_PSK = 4;
    }

    public static final class VivoWifiManager {
        public static final String EXTRA_DATA_FLAOVER_GPRS = "android.net.wifi.data_flavor_gprs";
        public static final String EXTRA_NOTIFICATION_NETWORKID = "network_id";
        public static final String EXTRA_NOTIFICATION_SSID = "ssid";
        public static final String WIFI_NOTIFICATION_ACTION = "android.net.wifi.WIFI_NOTIFICATION";
    }
}
