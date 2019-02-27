package com.vivo.services.cust.server;

import android.content.pm.PackageInfo;
import android.os.SystemProperties;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Utils {
    public static final String ACTION_ADD = "add";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_PERSIST_APPS_UPDATED = "vivo.custom.action.PERSIST_APPS_UPDATED";
    public static final String ACTION_UPDATE = "update";
    public static final int ALL = 0;
    public static final int BROWSER = 2;
    public static final int CAMERA = 7;
    public static final boolean CHECK_UP = "yes".equals(SystemProperties.get("persist.gn.p_check", "yes"));
    public static final String CT_APP_DATA_NETWORK_PATTERN = "ct_app_data_network_pattern";
    public static final String CT_APP_WIFI_NETWORK_PATTERN = "ct_app_wifi_network_pattern";
    public static final String CT_BLUETOOTH_RESTRICT_PATTERN = "ct_bluetooth_restrict_pattern";
    public static final String CT_NOTIFICATION_RESTRICT_PATTERN = "ct_notification_restrict_pattern";
    public static final String DATABASE_NAMESPACE_GLOBAL = "global";
    public static final String DATABASE_NAMESPACE_SECURE = "secure";
    public static final String DATABASE_NAMESPACE_SYSTEM = "system";
    public static final int DATABL = 6;
    public static final int DATAWL = 7;
    public static final boolean DEBUG = true;
    public static final boolean DEBUG_EXTRA = SystemProperties.get("persist.sys.log.ctrl", "no").equals("yes");
    public static final int DOMAIN_RESTRICTION = 0;
    public static final int EMAIL = 10;
    public static final String EMM_SECURITY_PERMISSION = "com.chinatelecom.permission.security.EMM";
    public static final String FLAG_BJHJL = "49";
    public static final String FLAG_BJWHBT = "133";
    public static final String FLAG_GS62 = "29";
    public static final String FLAG_HNTCBD = "236";
    public static final String FLAG_LSJY = "1";
    public static final String FLAG_SZPAPH = "199";
    public static final String FLAG_YDHYY = "11";
    public static final String FLAG_ZHDL = "48";
    public static final String FLAG_ZXWY = "35";
    public static final int FORBITRUN = 1;
    public static final int FORBIT_BOTH = 3;
    public static final int FORBIT_SIM1 = 1;
    public static final int FORBIT_SIM2 = 2;
    public static final int GALLERY = 3;
    public static final String GN_CONFIG_FILE = "/oem/sct_config.xml";
    public static final boolean GN_TESTFLAG = FLAG_LSJY.equals(SystemProperties.get("ro.build.gn.testflag", "0"));
    public static final String GN_TRUST_APP_STORE_LIST = "/data/bbkcore/gn_trust_app_store_list.xml";
    public static final int HIDE = 1;
    public static final int INPUTMETHOD = 0;
    public static final int INSTALLBL = 4;
    public static final int INSTALLWL = 5;
    public static final int INT_BOTH = 0;
    public static final int INT_CALLIN = 1;
    public static final int INT_CALLOUT = 2;
    public static final int INT_SIM1 = 1;
    public static final int INT_SIM2 = 2;
    public static final int IP_RESTRICTION = 1;
    public static final int LAUNCHER = 1;
    public static final int MESSAGE = 6;
    public static final int MUSIC = 4;
    public static final int NORMAL = 0;
    public static final String OP_MODE_ALLOWED = "allowed";
    public static final String OP_MODE_ERRORED = "errored";
    public static final String OP_MODE_IGNORED = "ignored";
    public static final int PATTERN_BLACKLIS = 1;
    public static final int PATTERN_NORMAL = 0;
    public static final int PATTERN_WHITELIST = 2;
    public static final int PATTERN_WHITELIST_FORCE_ON = 3;
    public static final int PERSIST = 0;
    public static final String SECOND_VIVO_CUSTOM_SECURE_PERMISSION = "com.vivo.custom.permission.SECOND_PLUG_IN";
    public static final int SIM1 = 1;
    public static final int SIM2 = 2;
    public static final int SIM_ALL = 0;
    public static final int STATE_ALLOWED = 1;
    public static final int STATE_FORBIDDEN = 0;
    public static final int STATE_FORCE_PTP = 5;
    public static final int STATE_FORCE_TURN_ON = 4;
    public static final int STATE_TURN_OFF = 2;
    public static final int STATE_TURN_ON = 3;
    public static final String STR_BOTH = "both";
    public static final String STR_CALLIN = "callIn";
    public static final String STR_CALLOUT = "callOut";
    public static final String STR_SIM1 = "sim1";
    public static final String STR_SIM2 = "sim2";
    public static final int SYSTEM = 1;
    private static final String TAG = "VCS";
    public static final int THIRDPARTY = 2;
    public static final int TRAFF_ALL = 0;
    public static final int TRAFF_DATA_NETWORK = 1;
    public static final int TRAFF_WIFI = 2;
    public static final String TYPE_PERSIST_PROPERTY = "persist";
    public static final String TYPE_SETTING_DATABASE = "setting";
    public static final int UNHIDE = 0;
    public static final int UNINSTALLBL = 2;
    public static final int UNINSTALLWL = 3;
    public static final String VALUE_MODE_INT = "int";
    public static final String VALUE_MODE_STRING = "string";
    public static final int VIDEO = 5;
    public static final String VIVO_CUSTOM_SECURE_PERMISSION = "com.vivo.custom.permission.PLUG_IN";
    public static final String VIVO_CUSTOM_SUPPORT = SystemProperties.get("ro.build.gn.support", "0");
    public static final String VIVO_CUSTOM_VERSION = SystemProperties.get("ro.build.version.bbk", "0");
    public static final String VIVO_EMAIL_EXTRAL_PROVIDER = "content://com.vivo.email.provider/extral_provider";
    public static final String VIVO_YDHYY_PERMISSION = "komect.aqb.permission.MDM_PLUGIN";
    public static final String WESTONE_EMM_SECURITY_PERMISSION = "com.westone.permission.security.EMM";
    public static final int WIFIBL = 8;
    public static final int WIFIWL = 9;
    public static List<String> allowDisableSysApp = new ArrayList(Arrays.asList(new String[]{"com.bbk.appstore", "com.android.bbkmusic", "com.bbk.calendar", "com.android.BBKClock", "com.android.bbksoundrecorder", "com.vivo.Tips", "com.android.bbkcalculator", "com.android.notes", "com.vivo.compass", "com.vivo.FMRadio", "com.vivo.browser", "com.vivo.email", "com.android.VideoPlayer", "com.vivo.weather", "com.bbk.cloud", "com.vivo.game", "com.vivo.space", "com.chaozh.iReader", "com.vivo.childrenmode", "com.bbk.VoiceAssistant"}));
    public static List<String> disallowNetworkRestrictApps = new ArrayList(Arrays.asList(new String[]{"com.bbk.updater"}));
    public static List<String> disallowNotificationRestrictPackages = new ArrayList(Arrays.asList(new String[]{"android"}));
    public static final boolean isSupportTF = FLAG_LSJY.equals(SystemProperties.get("vold.decrypt.sd_card_support", "0"));

    public static class CustomStateLock {
        public int mNotificationPattern;
    }

    public static class DynamicRecord {
        public String mAction;
        public String mMode;
        public String mName;
        public String mNamespace;
        public String mType;
        public int mValue;
        public String mValueStr;

        public DynamicRecord(String name, String type, String namespace, String action, String mode, String valuestr) {
            this.mName = name;
            this.mType = type;
            this.mNamespace = namespace;
            this.mAction = action;
            this.mValueStr = valuestr;
            this.mMode = mode;
            this.mValue = -1;
        }

        public DynamicRecord(String name, String type, String namespace, String action, String mode, int value) {
            this.mName = name;
            this.mType = type;
            this.mNamespace = namespace;
            this.mAction = action;
            this.mMode = mode;
            this.mValue = value;
            this.mValueStr = null;
        }

        public DynamicRecord(String name, String type, String namespace, String action, String valuestr) {
            this.mName = name;
            this.mType = type;
            this.mNamespace = namespace;
            this.mAction = action;
            this.mMode = Utils.VALUE_MODE_STRING;
            this.mValueStr = valuestr;
            this.mValue = -1;
        }

        public DynamicRecord(String name, String type, String namespace, String action, int value) {
            this.mName = name;
            this.mType = type;
            this.mNamespace = namespace;
            this.mAction = action;
            this.mMode = Utils.VALUE_MODE_INT;
            this.mValue = value;
            this.mValueStr = null;
        }
    }

    public static Object invokeVivoCustomUtilsStaticMethods(String methodName) {
        try {
            return Class.forName("android.util.VivoCustomUtils").getMethod(methodName, new Class[0]).invoke(null, new Object[0]);
        } catch (Exception e) {
            Log.d(TAG, "invokeVivoCustomUtilsStaticMethods e = " + e);
            return null;
        }
    }

    public static List<String> getCustomizedApps(int type) {
        try {
            return (List) Class.forName("android.util.VivoCustomUtils").getMethod("getCustomizedApps", new Class[]{Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(type)});
        } catch (Exception e) {
            return null;
        }
    }

    public static String getExternalSdPath(String[] pathList) {
        String sdPath = null;
        if (!isSupportTF || pathList == null) {
            return null;
        }
        int i = 0;
        while (i < pathList.length) {
            if (pathList[i].contains("/external_sd") || pathList[i].contains("/sdcard1")) {
                sdPath = pathList[i];
            }
            Log.d(TAG, "getExternalSdPath i = " + i + ", path i = " + pathList[i]);
            i++;
        }
        return sdPath;
    }

    public static boolean isAllowDisabled(PackageInfo pkgInfo) {
        if ((pkgInfo.applicationInfo.flags & 1) == 0 || (allowDisableSysApp.contains(pkgInfo.packageName) ^ 1) == 0) {
            return true;
        }
        return false;
    }
}
