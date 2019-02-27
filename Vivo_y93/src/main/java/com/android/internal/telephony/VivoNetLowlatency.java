package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import com.android.internal.telephony.ITelephony.Stub;
import java.util.List;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoNetLowlatency {
    public static final String ACTION_BASE_STATION_REFER_INFO_REQUEST = "com.vivo.gw.baseinfo";
    public static final String ACTION_QTI_LOW_LATENCY_LEVEL_CHANGE = "com.qualcomm.qti.telephonyservice.intent.action.CHANGE_LEVEL";
    public static final String ACTION_QTI_LOW_LATENCY_PROIOR_CHANGE = "com.qualcomm.qti.telephonyservice.intent.action.CHANGE_PRIO";
    private static boolean DBUG = true;
    public static final long LEVEL_SET_MINIMUM_TIME_INTERVAL = 10000;
    private static final String LOWLATENCY_SERVER_NAME = "com.qualcomm.qti.telephonyservice.LowLatencyService";
    public static final int MISC_INFO_CELL_INFO_REQUEST = 2;
    public static final String MISC_INFO_DEFAULT_SEND_BUFFER = "empty";
    private static final String SETTINGS_SHEET_CURRENT_GAME_PKG = "current_game_package";
    private static final String SETTINGS_SHEET_IS_GAME_MODE = "is_game_mode";
    public static final String SYSTEMPROPERTIES_AUTOMODE_FLAG = "sys.vivo.lowlatency.automode.flag";
    public static final String SYSTEMPROPERTIES_LATENCY_DL_LEVEL = "persist.sys.vivo.qtilowlatencyleveldl";
    public static final String SYSTEMPROPERTIES_LATENCY_PRIO = "persist.sys.vivo.qtilowlatencyprio";
    public static final String SYSTEMPROPERTIES_LATENCY_UL_LEVEL = "persist.sys.vivo.qtilowlatencylevelul";
    public static final String SYSTEMPROPERTIES_LEVEL_CONFIG = "persist.vivo.lowlatency.level.config";
    public static final String SYSTEMPROPERTIES_PRINT_LOG_FLAG = "persist.vivo.test.network.flag";
    private static final String TAG = "NetLowlatency";
    private static int dataPhoneId = 0;
    public static long lastBaseStationReferRequeTime = 0;
    public static long lastSetLevelTime = 0;
    private ContentResolver mContentResolver = null;
    public Context mContext = null;

    public enum Level {
        ERROR(0),
        L1(1),
        L2(2),
        L3(3),
        L4(4),
        LOW_LATENCY_SERVICE(5);
        
        private long level;

        private Level(long level) {
            this.level = level;
        }

        public long getLevel() {
            return this.level;
        }
    }

    public enum Radio {
        WWAN,
        WLAN
    }

    public enum StatusCade {
        OK,
        INVALID_ARGUMENTS,
        UNKNOWN_ERROR
    }

    VivoNetLowlatency(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mContentResolver = context.getContentResolver();
            log("start lowlatency monitor");
        }
    }

    public void setDataPhoneId(int dataPhoneId) {
        dataPhoneId = dataPhoneId;
    }

    public static boolean isAutoMode() {
        boolean isAutoMode = SystemProperties.getBoolean(SYSTEMPROPERTIES_AUTOMODE_FLAG, true);
        log("is automode : " + isAutoMode);
        return isAutoMode;
    }

    public boolean isGaming() {
        boolean z = true;
        if (this.mContentResolver == null) {
            return false;
        }
        int isGaming = System.getInt(this.mContentResolver, SETTINGS_SHEET_IS_GAME_MODE, 0);
        log("isGaming " + isGaming);
        if (1 != isGaming) {
            z = false;
        }
        return z;
    }

    public boolean isGamingOnFrontDesk() {
        String packageName = getCurrentGamePkgName();
        if (packageName == null || (packageName.equals("") ^ 1) == 0) {
            log("isGamingOnFrontDesk false");
            return false;
        }
        log("isGamingOnFrontDesk true packageName is " + packageName);
        return true;
    }

    private String getCurrentGamePkgName() {
        if (this.mContentResolver == null) {
            return "";
        }
        return System.getString(this.mContentResolver, SETTINGS_SHEET_CURRENT_GAME_PKG);
    }

    public static boolean isMobileNetworkType(Context mContext) {
        if (mContext == null) {
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService("connectivity");
        if (connectivityManager == null) {
            log("get network type error, the connectivityManager is null");
            return false;
        }
        NetworkInfo mobileNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (mobileNetworkInfo == null) {
            log("get network type error, the mobileNetworkInfo is null");
            return false;
        } else if (mobileNetworkInfo.getType() == 0) {
            log("the network type is WWAN");
            return true;
        } else {
            log("the network type is not WWAN");
            return false;
        }
    }

    public boolean isScreenOn() {
        PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
        if (pm == null) {
            return false;
        }
        boolean isScreenOn = pm.isScreenOn();
        log("is screen on: " + isScreenOn);
        return isScreenOn;
    }

    public static boolean setLatencyLevel(Context mContext, Radio networkType, Level ulLevel, Level dlLevel) {
        if (mContext == null) {
            return false;
        }
        if (isLowLatencyServiceAlive(mContext)) {
            Intent intent = new Intent();
            intent.setAction(ACTION_QTI_LOW_LATENCY_LEVEL_CHANGE);
            intent.setPackage("com.qualcomm.qti.telephonyservice");
            intent.putExtra("Rat_type", Radio.WWAN);
            intent.putExtra("Level_UL", ulLevel.getLevel());
            intent.putExtra("Level_DL", dlLevel.getLevel());
            mContext.sendBroadcast(intent);
            lastSetLevelTime = System.currentTimeMillis();
            log("sent set latency level broad cast: upLevel: " + ulLevel.getLevel() + " dlLevel: " + dlLevel.getLevel() + "the set time is: " + lastSetLevelTime);
            return true;
        }
        startLowlatencyService(mContext);
        return false;
    }

    public static void startLowLatency(Context mContext, boolean isEnable) {
        if (mContext != null) {
            log("startLowLatency etered, isEnable: " + isEnable);
            if (isEnable) {
                Level configLevel = getLowLatencyLevelConf();
                if (isMobileNetworkType(mContext) && isOutOfMinimumTimeOfSetlevel()) {
                    setLatencyLevel(mContext, Radio.WWAN, configLevel, configLevel);
                    log("startLowLatency enabled");
                }
            } else {
                setLatencyLevel(mContext, Radio.WWAN, Level.L1, Level.L1);
                log("startLowLatency disabled");
            }
        }
    }

    private static Level getLowLatencyLevelConf() {
        long configLevel = SystemProperties.getLong(SYSTEMPROPERTIES_LEVEL_CONFIG, 4);
        if (2 == configLevel) {
            return Level.L2;
        }
        if (3 == configLevel) {
            return Level.L3;
        }
        if (4 == configLevel) {
            return Level.L4;
        }
        return Level.L1;
    }

    public static boolean isOutOfMinimumTimeOfSetlevel() {
        long currentTime = System.currentTimeMillis();
        long currentTimeSpace = currentTime - lastSetLevelTime;
        if (currentTimeSpace > LEVEL_SET_MINIMUM_TIME_INTERVAL) {
            log("set level time space check: current time: " + currentTime + " time space: " + currentTimeSpace + "is out of : 1");
            return true;
        }
        log("set level time space check: current time: " + currentTime + " time space: " + currentTimeSpace + "is out of : 0");
        return false;
    }

    public static Level getUplinkLatencyLevel() {
        long uplinkLevel = SystemProperties.getLong(SYSTEMPROPERTIES_LATENCY_UL_LEVEL, Level.L1.getLevel());
        log("get the current uplink latency level is " + uplinkLevel);
        if (2 == uplinkLevel) {
            return Level.L2;
        }
        if (3 == uplinkLevel) {
            return Level.L3;
        }
        if (4 == uplinkLevel) {
            return Level.L4;
        }
        return Level.L1;
    }

    public static Level getDownlinkLatencyLevel() {
        long dllinkLevel = SystemProperties.getLong(SYSTEMPROPERTIES_LATENCY_DL_LEVEL, Level.L1.getLevel());
        log("get the current downlink latency level is " + dllinkLevel);
        if (2 == dllinkLevel) {
            return Level.L2;
        }
        if (3 == dllinkLevel) {
            return Level.L3;
        }
        if (4 == dllinkLevel) {
            return Level.L4;
        }
        return Level.L1;
    }

    private static void reportBigdata(Level level, StatusCade result) {
    }

    public static boolean isLowLatencyServiceAlive(Context mContext) {
        if (mContext == null) {
            return false;
        }
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService("activity");
        if (activityManager != null) {
            List<RunningServiceInfo> serviceList = activityManager.getRunningServices(40);
            if (serviceList == null) {
                return false;
            }
            int size = serviceList.size();
            for (int i = 0; i < size; i++) {
                if (((RunningServiceInfo) serviceList.get(i)).service.getClassName().equals(LOWLATENCY_SERVER_NAME)) {
                    isRunning = true;
                    break;
                }
            }
        }
        log("is the lowlatency server running: " + isRunning);
        return isRunning;
    }

    public static void startLowlatencyService(Context context) {
        if (context != null) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.qualcomm.qti.telephonyservice", LOWLATENCY_SERVER_NAME));
            context.startService(intent);
            log("starts the lowlatency server.");
        }
    }

    private static void log(String logInfo) {
        if (DBUG) {
            Log.i(TAG, logInfo);
        }
    }

    public static boolean isMoblieDataReady(Context context) {
        return true;
    }

    public static boolean isAirplaneMode(Context context) {
        boolean z = true;
        if (context == null) {
            return true;
        }
        int isAirplaneMode = System.getInt(context.getContentResolver(), "airplane_mode_on", 0);
        log("is air plane mode : " + isAirplaneMode);
        if (isAirplaneMode != 1) {
            z = false;
        }
        return z;
    }

    public static boolean reportNetTriple(Context context, String triple) {
        return false;
    }

    public static String netDiagnose(Context context) {
        String cellInfo = sendMiscInfo(dataPhoneId, 2, MISC_INFO_DEFAULT_SEND_BUFFER);
        return null;
    }

    public static void baseStationReferenceInfo(Context context) {
        if (context != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastBaseStationReferRequeTime >= LEVEL_SET_MINIMUM_TIME_INTERVAL) {
                Intent intent = new Intent();
                intent.setAction(ACTION_BASE_STATION_REFER_INFO_REQUEST);
                intent.setPackage("com.vivo.gw");
                context.sendBroadcast(intent);
                log("sent base station reference info request broadcast, the last sent time is: " + lastBaseStationReferRequeTime);
                lastBaseStationReferRequeTime = currentTime;
            }
        }
    }

    public static void reportBaseStationReferInfo(String baseStationInfo) {
    }

    private static String sendMiscInfo(int phoneId, int commandId, String buffer) {
        try {
            VivoTelephonyApiParams param = new VivoTelephonyApiParams("API_TAG_sendMiscInfo");
            param.put("phoneId", Integer.valueOf(phoneId));
            param.put("commandId", Integer.valueOf(commandId));
            param.put("buffer", buffer);
            VivoTelephonyApiParams ret = getITelephony().vivoTelephonyApi(param);
            if (ret != null) {
                return (String) ret.getAsObject("response");
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ITelephony getITelephony() {
        return Stub.asInterface(ServiceManager.getService("phone"));
    }
}
