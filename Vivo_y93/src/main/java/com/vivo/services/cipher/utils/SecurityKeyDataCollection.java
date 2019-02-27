package com.vivo.services.cipher.utils;

import android.content.Context;
import android.os.PowerManager;
import com.android.internal.telephony.SmsConstants;
import com.vivo.common.VivoCollectData;
import com.vivo.content.Weather.HourData;
import java.util.HashMap;

public class SecurityKeyDataCollection {
    public static final int ACTIONTYPE_AES_DECRYPT = 21313;
    public static final int ACTIONTYPE_AES_ENCRYPT = 21312;
    public static final int ACTIONTYPE_DATA_READ = 21319;
    public static final int ACTIONTYPE_DATA_SAVE = 21318;
    public static final int ACTIONTYPE_DEVICE_INIT = 21310;
    public static final int ACTIONTYPE_KEY_UPDATE = 21311;
    public static final int ACTIONTYPE_RSA_DECRYPT = 21315;
    public static final int ACTIONTYPE_RSA_ENCRYPT = 21314;
    public static final int ACTIONTYPE_SIGN_SIGNED = 21316;
    public static final int ACTIONTYPE_SIGN_VERIFY = 21317;
    private static final int DATA_COLLECTION_MAX_ERR_MSG_SIZE = 64;
    public static final String PERMISSION_READ_DATA = "com.bbk.iqoo.logsystem.permission.READ_DATA";
    public static final String PERMISSION_WRITE_DATA = "com.bbk.iqoo.logsystem.permission.WRITE_DATA";
    private static final String VIVO_SECURITYKEY_EVENT_ID = "213";

    private static boolean vivoKeyStorecheckPermission(SecurityKeyConfigure configure) {
        Context context = configure.getContext();
        if (context != null && context.checkCallingOrSelfPermission(PERMISSION_READ_DATA) == 0 && context.checkCallingOrSelfPermission(PERMISSION_WRITE_DATA) == 0) {
            return true;
        }
        VLog.e(Contants.TAG, configure, "check collect data permisson: PERMISSION_DENIED");
        return false;
    }

    public static void collectData(SecurityKeyConfigure configure, int tryTimes, int actionType, int resultCode, String msg) {
        try {
            if (vivoKeyStorecheckPermission(configure)) {
                VivoCollectData vivoCollectData = new VivoCollectData(configure.getContext());
                HashMap<String, String> collectInfoMap = new HashMap();
                if (vivoCollectData != null && collectInfoMap != null && vivoCollectData.getControlInfo(VIVO_SECURITYKEY_EVENT_ID)) {
                    collectInfoMap.put("p_n", configure.getShortPackageName());
                    if (configure.getRealCipherMode() == 2) {
                        collectInfoMap.put("type", "0");
                    } else {
                        collectInfoMap.put("type", "1");
                    }
                    collectInfoMap.put("is_on", getScreenStatus(configure));
                    collectInfoMap.put("err", Integer.toString(resultCode));
                    collectInfoMap.put(HourData.COUNT, Integer.toString(tryTimes));
                    if (isNetworkExp(resultCode) && msg != null) {
                        collectInfoMap.put("e_msg", getValidMsg(msg));
                    }
                    VLog.d(Contants.TAG, "Data collection:" + collectInfoMap.toString());
                    vivoCollectData.writeData(VIVO_SECURITYKEY_EVENT_ID, Integer.toString(actionType), System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, collectInfoMap);
                }
            }
        } catch (Exception e) {
            VLog.e(Contants.TAG, configure, "Data collection Exception: " + e.getMessage());
        }
    }

    public static void collectData(SecurityKeyConfigure configure, int tryTimes, int actionType, int resultCode) {
        collectData(configure, tryTimes, actionType, resultCode, null);
    }

    private static String getScreenStatus(SecurityKeyConfigure configure) {
        PowerManager pm = (PowerManager) configure.getContext().getSystemService("power");
        if (pm == null || !pm.isScreenOn()) {
            return "0";
        }
        return "1";
    }

    private static boolean isNetworkExp(int errCode) {
        if (errCode == 164 || errCode == 168 || errCode == 169 || errCode == 170 || errCode == 400 || errCode == 401 || errCode == 404 || errCode == 500) {
            return true;
        }
        return false;
    }

    private static String getValidMsg(String errMsg) {
        String validMsg = errMsg;
        if (errMsg.length() > 64) {
            validMsg = errMsg.substring(0, 63);
        }
        int nextLineSymPos = validMsg.indexOf(10);
        if (nextLineSymPos != -1) {
            validMsg = validMsg.substring(0, nextLineSymPos);
        }
        nextLineSymPos = validMsg.indexOf(13);
        if (nextLineSymPos != -1) {
            return validMsg.substring(0, nextLineSymPos);
        }
        return validMsg;
    }

    public static String getActionKey(SecurityKeyConfigure configure, int actionType) {
        switch (actionType) {
            case ACTIONTYPE_DEVICE_INIT /*21310*/:
                return "init_d";
            case ACTIONTYPE_KEY_UPDATE /*21311*/:
                return "key_u";
            case ACTIONTYPE_AES_ENCRYPT /*21312*/:
                return "aes_e";
            case ACTIONTYPE_AES_DECRYPT /*21313*/:
                return "aes_d";
            case ACTIONTYPE_RSA_ENCRYPT /*21314*/:
                return "rsa_e";
            case ACTIONTYPE_RSA_DECRYPT /*21315*/:
                return "rsa_d";
            case ACTIONTYPE_SIGN_SIGNED /*21316*/:
                return "sign_s";
            case ACTIONTYPE_SIGN_VERIFY /*21317*/:
                return "sign_v";
            case ACTIONTYPE_DATA_SAVE /*21318*/:
                return "data_s";
            case ACTIONTYPE_DATA_READ /*21319*/:
                return "data_r";
            default:
                VLog.w(Contants.TAG, configure, "unknown actiontype: " + actionType);
                return SmsConstants.FORMAT_UNKNOWN;
        }
    }
}
