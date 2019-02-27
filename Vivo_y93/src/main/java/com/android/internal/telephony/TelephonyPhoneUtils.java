package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.VivoPropertySetter;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.telephony.ITelephony.Stub;
import com.android.internal.telephony.test.SimulatedCommands;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.vivo.services.cust.VivoCustomManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class TelephonyPhoneUtils {
    public static final int CHINA_MCC = 460;
    public static final boolean CRACK_SHOW3G = "1".equals(SystemProperties.get("persist.sys.ril.show3g", ""));
    public static final byte[] ChinaTelecom = new byte[]{(byte) -28, (byte) -72, (byte) -83, (byte) -27, (byte) -101, (byte) -67, (byte) -25, (byte) -108, (byte) -75, (byte) -28, (byte) -65, (byte) -95};
    public static final boolean DUAL_VOLTE;
    public static final String ENTRY_PROP = SystemProperties.get("ro.vivo.op.entry", "").toUpperCase();
    public static boolean IS_CMCC = false;
    private static final int MDM_STATE_BLACKLIST = 1;
    private static final int MDM_STATE_WHITELIST = 2;
    public static final int MODE_PHONE0_ONLY = 1;
    public static final int MODE_PHONE1_ONLY = 2;
    public static final int MODE_PHONE_DOUL = 3;
    public static final int MODE_PHONE_NONE = 0;
    public static final boolean NEED_LOCKDDS;
    public static final int PHONE_COUNT = 2;
    public static final int SCmcc = 0;
    public static final int SCtcc = 2;
    public static final int SCu = 1;
    public static final int SOthers = 5;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static String TAG = "TelephonyPhoneUtils";
    public static final HashSet<String> TYPES_OF_CHINA_MOBILE = new HashSet(Arrays.asList(new String[]{"china  mobile", "china mobile", "cmcc"}));
    public static final HashSet<String> TYPES_OF_CHINA_TELECOM = new HashSet(Arrays.asList(new String[]{"china telecom", "46003", "46011", "chn-ct"}));
    public static final HashSet<String> TYPES_OF_CHINA_UNICOM = new HashSet(Arrays.asList(new String[]{"china  unicom", "china unicom", "chn-unicom", "46001", "46006", "46009"}));
    public static final HashSet<String> TYPES_OF_THAILAND_TRUE_MOVE = new HashSet(Arrays.asList(new String[]{"TRUE", "TRUE-H", "TH 3G+", "TRUE 3G+"}));
    public static final boolean WRONG_OEM = "Lock_YD".equals(SystemProperties.get("ro.anti.switching.oem.type", ""));
    public static final String[][] cmCuPlmn;
    public static final String[][] customEhplmn;
    public static final String[][] customPlmnOperator;
    public static final String[][] customPlmnOperator_Th;
    public static boolean sInNetEntry = "yes".equals(SystemProperties.get("ro.product.net.entry.bbk", "no"));
    public static boolean sIsCMCCEntry;
    public static boolean sIsCTCCEntry = sVivoOpEntry.contains("CTCC_RW");
    public static boolean sIsOversea = "yes".equals(SystemProperties.get("ro.vivo.product.overseas", "no"));
    public static boolean sIsUNICOMEntry;
    public static boolean sVSimSupport = "yes".equals(SystemProperties.get("ro.build.feature.softsim", "no"));
    public static String sVivoOpEntry = SystemProperties.get("ro.vivo.op.entry", "").toUpperCase();

    static {
        boolean z;
        if ("CMCC_RWA".equals(sVivoOpEntry) || "CMCC".equals(sVivoOpEntry) || "CMCC_RWB".equals(sVivoOpEntry)) {
            z = true;
        } else {
            z = "FULL_CMCC_RWA".equals(sVivoOpEntry);
        }
        sIsCMCCEntry = z;
        if (sVivoOpEntry != null) {
            z = sVivoOpEntry.contains("UNICOM_RW");
        } else {
            z = false;
        }
        sIsUNICOMEntry = z;
        if ("CN-YD-B".equals(SystemProperties.get("ro.product.customize.bbk", "")) || "CN-YD-A".equals(SystemProperties.get("ro.product.customize.bbk", ""))) {
            z = true;
        } else {
            z = "1".equals(SystemProperties.get("persist.sys.ril.ddslock", ""));
        }
        NEED_LOCKDDS = z;
        z = ENTRY_PROP != null ? ("CMCC".equals(ENTRY_PROP) || ENTRY_PROP.contains("CMCC_RW")) ? true : ENTRY_PROP.contains("FULL_CMCC_RWA") : false;
        IS_CMCC = z;
        if (SystemProperties.getInt("persist.radio.multi.volte", 1) > 1 || SystemProperties.getInt("ro.mtk_multiple_ims_support", 1) > 1) {
            z = true;
        } else {
            z = false;
        }
        DUAL_VOLTE = z;
        r0 = new String[3][];
        r0[0] = new String[]{"TRUE-H", "TRUE-H", "52099"};
        r0[1] = new String[]{"TRUE-H", "TRUE-H", "52000"};
        r0[2] = new String[]{"TRUE-H", "TRUE-H", "52004"};
        customPlmnOperator_Th = r0;
        r0 = new String[8][];
        r0[0] = new String[]{"MY CELCOM", "CELCOM", "50219"};
        r0[1] = new String[]{"Aircel", "Aircel", "40428"};
        r0[2] = new String[]{"TRUE-H", "TRUE-H", "52099"};
        r0[3] = new String[]{"TRUE-H", "TRUE-H", "52000"};
        r0[4] = new String[]{"TRUE-H", "TRUE-H", "52004"};
        r0[5] = new String[]{"Aircel", "Aircel", "40491"};
        r0[6] = new String[]{"Aircel", "Aircel", "40441"};
        r0[7] = new String[]{"Aircel", "Aircel", "40417"};
        customPlmnOperator = r0;
        r0 = new String[2][];
        r0[0] = new String[]{"46000", "46002", "46007", "46008"};
        r0[1] = new String[]{"46001", "46006", "46009"};
        cmCuPlmn = r0;
        r0 = new String[24][];
        r0[0] = new String[]{"46000", "46002", "46004", "46007", "46008"};
        r0[1] = new String[]{"46003", "46011"};
        r0[2] = new String[]{"46001", "46006", "46009"};
        r0[3] = new String[]{"45502", "45507"};
        r0[4] = new String[]{"45400", "45402", "45418"};
        r0[5] = new String[]{"45403", "45404"};
        r0[6] = new String[]{"45412", "45413"};
        r0[7] = new String[]{"45416", "45419"};
        r0[8] = new String[]{"45501", "45504"};
        r0[9] = new String[]{"45503", "45505"};
        r0[10] = new String[]{"45002", "45008"};
        r0[11] = new String[]{"52501", "52502"};
        r0[12] = new String[]{"52000", "52004", "52099"};
        r0[13] = new String[]{"43602", "43612"};
        r0[14] = new String[]{"46605", "46697"};
        r0[15] = new String[]{"52010", "52099"};
        r0[16] = new String[]{"24001", "24005"};
        r0[17] = new String[]{"26207", "26208"};
        r0[18] = new String[]{"23430", "23431", "23432"};
        r0[19] = new String[]{"72402", "72403", "72404"};
        r0[20] = new String[]{"72406", "72410", "72411", "72423"};
        r0[21] = new String[]{"72432", "72433", "72434"};
        r0[22] = new String[]{"31026", "31031", "310160", "310200", "310210", "310220", "310230", "310240", "310250", SimulatedCommands.FAKE_MCC_MNC, "310270", "310660"};
        r0[23] = new String[]{"310150", "310170", "310380", "310410"};
        customEhplmn = r0;
    }

    public static boolean isSameOperator(String opeartor1, String operator2) {
        boolean isServingPlmnInGroup = false;
        boolean isHomePlmnInGroup = false;
        if (TextUtils.isEmpty(opeartor1) || TextUtils.isEmpty(operator2)) {
            return false;
        }
        if (opeartor1.equals(operator2)) {
            return true;
        }
        for (int i = 0; i < customEhplmn.length; i++) {
            for (int j = 0; j < customEhplmn[i].length; j++) {
                if (opeartor1.equals(customEhplmn[i][j])) {
                    isServingPlmnInGroup = true;
                }
                if (operator2.equals(customEhplmn[i][j])) {
                    isHomePlmnInGroup = true;
                }
            }
            if (isServingPlmnInGroup && isHomePlmnInGroup) {
                return true;
            }
            isServingPlmnInGroup = false;
            isHomePlmnInGroup = false;
        }
        return false;
    }

    public static int checkOperatorByIccid(String iccId) {
        int opType = 5;
        if (sIsOversea) {
            return 5;
        }
        if (!TextUtils.isEmpty(iccId)) {
            if (iccId.startsWith("898603") || iccId.startsWith("898611") || iccId.startsWith("898612") || iccId.startsWith("8985302")) {
                opType = 2;
            } else if (iccId.startsWith("898600") || iccId.startsWith("898602") || iccId.startsWith("898607") || iccId.startsWith("898608") || iccId.startsWith("898675") || iccId.startsWith("8985212")) {
                opType = 0;
            } else if (iccId.startsWith("898601") || iccId.startsWith("898606") || iccId.startsWith("898609") || iccId.startsWith("8985207")) {
                opType = 1;
            }
        }
        return opType;
    }

    public static int getOperatorTypeByImsi(String imsi) {
        if (TextUtils.isEmpty(imsi)) {
            return 5;
        }
        if (imsi.startsWith("46003") || imsi.startsWith("46011") || imsi.startsWith("20404") || imsi.startsWith("45404") || imsi.startsWith("45502") || imsi.startsWith("45507") || imsi.startsWith("46012")) {
            return 2;
        }
        if (imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46004") || imsi.startsWith("46007") || imsi.startsWith("46008") || imsi.startsWith("45412")) {
            return 0;
        }
        if (imsi.startsWith("46001") || imsi.startsWith("46006") || imsi.startsWith("46009") || imsi.startsWith("45407")) {
            return 1;
        }
        return 5;
    }

    public static int getOperatorTypeByIccid(String iccid) {
        if (TextUtils.isEmpty(iccid)) {
            return 5;
        }
        if (iccid.startsWith("898603") || iccid.startsWith("898611") || iccid.startsWith("898612") || iccid.startsWith("8985302")) {
            return 2;
        }
        if (iccid.startsWith("898600") || iccid.startsWith("898602") || iccid.startsWith("898604") || iccid.startsWith("898607") || iccid.startsWith("898608") || iccid.startsWith("898675") || iccid.startsWith("8985212")) {
            return 0;
        }
        if (iccid.startsWith("898601") || iccid.startsWith("898606") || iccid.startsWith("898609") || iccid.startsWith("8985207")) {
            return 1;
        }
        return 5;
    }

    public static String checkOperatorForSimNameByImsi(String imsi, Context context) {
        String defaultSimName = "UNKNOWN";
        if (sIsOversea) {
            return defaultSimName;
        }
        switch (getOperatorTypeByImsi(imsi)) {
            case 0:
                defaultSimName = context.getText(51249521).toString();
                break;
            case 1:
                defaultSimName = context.getText(51249522).toString();
                break;
            case 2:
                defaultSimName = context.getText(51249523).toString();
                break;
        }
        return defaultSimName;
    }

    public static String checkOperatorForSimNameByIccid(String iccid, Context context) {
        String defaultSimName = "UNKNOWN";
        if (sIsOversea) {
            return defaultSimName;
        }
        switch (getOperatorTypeByIccid(iccid)) {
            case 0:
                defaultSimName = context.getText(51249521).toString();
                break;
            case 1:
                defaultSimName = context.getText(51249522).toString();
                break;
            case 2:
                defaultSimName = context.getText(51249523).toString();
                break;
        }
        return defaultSimName;
    }

    public static boolean isInChina() {
        String Mcc = SystemProperties.get("persist.radio.vivo.mcc", "");
        return ("460".equals(Mcc) || "454".equals(Mcc) || "455".equals(Mcc)) ? true : "".equals(Mcc);
    }

    public static boolean isCNYDCracked() {
        int cracked = SystemProperties.getInt("persist.radio.vivo.cracked", 0);
        if (SystemProperties.getInt("persist.sys.ril.crackdds", 0) == 1 || cracked == 1) {
            return true;
        }
        return false;
    }

    public static boolean isSimCtcc(int phoneId, Context context) {
        return 2 == getOperatorTypeByIccid(getSimIccid(phoneId, context));
    }

    public static String getSimIccid(int phoneId, Context context) {
        String iccid = "";
        UiccCard uiccCard = PhoneFactory.getPhone(phoneId).getUiccCard();
        if (uiccCard == null) {
            return null;
        }
        int numApps = uiccCard.getNumApplications();
        for (int i = 0; i < numApps; i++) {
            UiccCardApplication app = uiccCard.getApplicationIndex(i);
            if (!(app == null || app.getType() == AppType.APPTYPE_UNKNOWN)) {
                IccRecords iccRecord = app.getIccRecords();
                if (iccRecord != null) {
                    iccid = iccRecord.getIccId();
                }
            }
            if (!TextUtils.isEmpty(iccid)) {
                break;
            }
        }
        if (TextUtils.isEmpty(iccid)) {
            ArrayList<SubscriptionInfo> activeSubInfoList = (ArrayList) SubscriptionManager.from(context).getActiveSubscriptionInfoList();
            String[] iccids = new String[]{"", ""};
            if (activeSubInfoList != null) {
                for (SubscriptionInfo subInfo : activeSubInfoList) {
                    if (subInfo != null && subInfo.getSimSlotIndex() == phoneId) {
                        iccid = subInfo.getIccId();
                        break;
                    }
                }
            }
        }
        return iccid;
    }

    public static boolean isVSimEnable(int slot) {
        boolean z = true;
        if (slot != 0 && slot != 1) {
            return false;
        }
        if (SystemProperties.getInt("gsm.radio.vivo.vsimenable" + slot, 0) != 1) {
            z = false;
        }
        return z;
    }

    public static boolean isNumeric(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        int i = str.length();
        do {
            i--;
            if (i < 0) {
                return true;
            }
        } while (Character.isDigit(str.charAt(i)));
        return false;
    }

    public static int getInsertSimMode() {
        int nInsertSimNum = 0;
        for (int i = 0; i < 2; i++) {
            Phone phone = PhoneFactory.getPhone(i);
            if (phone != null && phone.getIccCard().hasIccCard()) {
                nInsertSimNum += 1 << i;
            }
        }
        if (nInsertSimNum == 1) {
            return 1;
        }
        if (nInsertSimNum == 2) {
            return 2;
        }
        if (nInsertSimNum == 3) {
            return 3;
        }
        return 0;
    }

    public static int getCardMcc(int phoneId) {
        String[] imsis = getImsiByPhone(PhoneFactory.getPhone(phoneId));
        if (TextUtils.isEmpty(imsis[0])) {
            return 0;
        }
        return Integer.parseInt(imsis[0].substring(0, 3));
    }

    public static boolean isOnlyChinaCardInsert() {
        boolean z = false;
        boolean[] simInsertState = new boolean[]{false, false};
        boolean[] simInsertChinaCard = new boolean[]{false, false};
        for (int i = 0; i < 2; i++) {
            Phone phone = PhoneFactory.getPhone(i);
            if (phone != null && phone.getIccCard().hasIccCard()) {
                boolean z2;
                simInsertState[i] = true;
                if (getCardMcc(i) == CHINA_MCC) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                simInsertChinaCard[i] = z2;
            }
        }
        if (!simInsertState[0] && (simInsertState[1] ^ 1) != 0) {
            return false;
        }
        if (simInsertState[0] && (simInsertState[1] ^ 1) != 0) {
            return simInsertChinaCard[0];
        }
        if (!simInsertState[0] && simInsertState[1]) {
            return simInsertChinaCard[1];
        }
        if (simInsertChinaCard[0]) {
            z = simInsertChinaCard[1];
        }
        return z;
    }

    public static boolean hasNoChinaCardInsert() {
        boolean z = false;
        boolean[] simInsertState = new boolean[]{false, false};
        boolean[] simInsertChinaCard = new boolean[]{false, false};
        for (int i = 0; i < 2; i++) {
            Phone phone = PhoneFactory.getPhone(i);
            if (phone != null && phone.getIccCard().hasIccCard()) {
                boolean z2;
                simInsertState[i] = true;
                if (getCardMcc(i) == CHINA_MCC) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                simInsertChinaCard[i] = z2;
            }
        }
        if (!simInsertState[0] && (simInsertState[1] ^ 1) != 0) {
            return false;
        }
        if (simInsertState[0] && (simInsertState[1] ^ 1) != 0) {
            return simInsertChinaCard[0] ^ 1;
        }
        if (!simInsertState[0] && simInsertState[1]) {
            return simInsertChinaCard[1] ^ 1;
        }
        if (!simInsertChinaCard[0]) {
            z = simInsertChinaCard[1] ^ 1;
        }
        return z;
    }

    public static String[] getImsiByPhone(Phone phone) {
        String[] imsis = new String[]{"", ""};
        if (phone == null) {
            return imsis;
        }
        UiccCard uiccCard = phone.getUiccCard();
        if (uiccCard == null) {
            return imsis;
        }
        UiccCardApplication app3GPP = uiccCard.getApplication(1);
        UiccCardApplication app3GPP2 = uiccCard.getApplication(2);
        if (!(app3GPP == null || app3GPP.getIccRecords() == null)) {
            imsis[0] = app3GPP.getIccRecords().getIMSI();
        }
        if (!(app3GPP2 == null || app3GPP2.getIccRecords() == null)) {
            imsis[1] = app3GPP2.getIccRecords().getIMSI();
        }
        return imsis;
    }

    public static boolean isAllowIroaming() {
        int allowiroaming = SystemProperties.getInt("persist.sys.ril.allowiroaming", 1);
        if (sInNetEntry || !sVSimSupport || (IS_CMCC ^ 1) == 0 || 1 != allowiroaming) {
            return false;
        }
        return true;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public static boolean isMDMForbidden(Context context, String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        int mdmStatus = Secure.getInt(context.getContentResolver(), "ct_sms_restrict_pattern", 0);
        Log.d(TAG, "isMDMForbidden mdmStatus = " + mdmStatus + ",number = " + number);
        VivoCustomManager mVivoCustomManager = new VivoCustomManager();
        if (mdmStatus == 1) {
            return isInMDMList(number, mVivoCustomManager.getSmsBlackList());
        }
        if (mdmStatus == 2) {
            return isInMDMList(number, mVivoCustomManager.getSmsWhiteList()) ^ 1;
        }
        return false;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private static boolean isInMDMList(String number, List<String> list) {
        int i = 0;
        while (i < list.size()) {
            if (list.get(i) != null && number.contains((CharSequence) list.get(i))) {
                return true;
            }
            i++;
        }
        return false;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public static boolean isMmsSendEmmMode(final Context context, int subId) {
        try {
            int ctSmsSend = Secure.getInt(context.getContentResolver(), "ct_sms_send", 1);
            int nctSmsBlock = Secure.getInt(context.getContentResolver(), "nct_sms_block", 1);
            int opType = getOperatorTypeByImsi(invokeTelephonyApi("API_TAG_getSimCdmaIMSI", "imsi", SubscriptionManager.getSlotIndex(subId)));
            Log.d(TAG, "isMmsSendEmmMode:ctSmsSend = " + ctSmsSend + ",nctSmsBlock = " + nctSmsBlock + ",opType = " + opType);
            if ((opType != 2 || ctSmsSend != 0) && (opType == 2 || nctSmsBlock != 0)) {
                return false;
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    Toast.makeText(context, context.getResources().getString(51249709), 0).show();
                }
            });
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Special version error " + Log.getStackTraceString(e));
            return false;
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private static String invokeTelephonyApi(String apiName, String resultName, int slotId) {
        String retValue = "";
        try {
            ITelephony telephony = Stub.asInterface(ServiceManager.getService("phone"));
            if (telephony == null) {
                return retValue;
            }
            Class<?> clsParams = Class.forName("com.android.internal.telephony.VivoTelephonyApiParams");
            Object paramInstance = clsParams.getConstructor(new Class[]{String.class}).newInstance(new Object[]{apiName});
            if (slotId >= 0) {
                clsParams.getMethod("put", new Class[]{String.class, String.class}).invoke(paramInstance, new Object[]{"slot", String.valueOf(slotId)});
            }
            Object ret = telephony.getClass().getMethod("vivoTelephonyApi", new Class[]{clsParams}).invoke(telephony, new Object[]{paramInstance});
            if (ret == null) {
                return retValue;
            }
            return (String) ret.getClass().getMethod("getAsString", new Class[]{String.class}).invoke(ret, new Object[]{resultName});
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return retValue;
        }
    }

    public static String getLocalString(Context context, String originalString) {
        String[] origNames = context.getResources().getStringArray(50923281);
        String[] localNames = context.getResources().getStringArray(50923282);
        if (origNames == null || localNames == null) {
            return null;
        }
        String localString = "";
        int i = 0;
        while (i < origNames.length && i < localNames.length) {
            if (origNames[i] != null && origNames[i].equalsIgnoreCase(originalString)) {
                localString = localNames[i];
                break;
            }
            i++;
        }
        return localString;
    }

    public static void setSystemProperty(String key, String value) {
        if (TextUtils.equals(SystemProperties.get(key, ""), value) ^ 1) {
            VivoPropertySetter.setPropertyAsync(key, value);
        }
    }
}
