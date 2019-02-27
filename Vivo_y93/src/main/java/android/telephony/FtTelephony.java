package android.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.PendingIntent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import java.util.ArrayList;
import java.util.List;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public abstract class FtTelephony {
    public static final String ACTION_SIM_MODE_CHANGED = "vivo.intent.action.ACTION_SIM_MODE_CHANGED";
    public static final int GSM_LTE_INDEX = 10;
    public static final int NETWORK_MODE_TOTAL = 11;
    public static int SIM_SLOT_1 = 0;
    public static int SIM_SLOT_2 = 1;
    public static final int TD_SCDMA_CDMA_EVDO_GSM_WCDMA_INDEX = 8;
    public static final int TD_SCDMA_GSM_INDEX = 3;
    public static final int TD_SCDMA_GSM_LTE_INDEX = 4;
    public static final int TD_SCDMA_GSM_WCDMA_INDEX = 5;
    public static final int TD_SCDMA_GSM_WCDMA_LTE_INDEX = 7;
    public static final int TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA_INDEX = 9;
    public static final int TD_SCDMA_LTE_INDEX = 2;
    public static final int TD_SCDMA_ONLY_INDEX = 0;
    public static final int TD_SCDMA_WCDMA_INDEX = 1;
    public static final int TD_SCDMA_WCDMA_LTE_INDEX = 6;

    public class SmsStorageStatus {
        public int mTotal;
        public int mUsed;
    }

    public abstract FtTelephonyApiParams commonApi(FtTelephonyApiParams ftTelephonyApiParams);

    public abstract int getActiveSubCount();

    public abstract int getActiveSubId();

    public abstract List<FtSubInfo> getActiveSubInfoList();

    public abstract List<FtSubInfo> getAllSubInfoList();

    public abstract int getAnrCount(int i);

    public abstract String getApnOperator(int i);

    public abstract int getCallState(int i);

    public abstract int getDefaultDataPhoneId();

    public abstract int getDefaultSmsPhoneId();

    public abstract int getEmailCount(int i);

    public abstract String getIMSIBySlotId(int i);

    public abstract String getImei(int i);

    public abstract String getImsStateChangeBroadcast();

    public abstract int getInsertedSimCount();

    public abstract List<FtSubInfo> getInsertedSubInfoList();

    public abstract int getInvalidSubId();

    public abstract String getMeid();

    public abstract int getNetworkMode(int i);

    public abstract int getPhoneId(int i);

    public abstract int[] getSimCapacityBySlotId(int i);

    public abstract SmsStorageStatus getSimMemoryStatus(int i);

    public abstract int getSimPbMaxNameLength(int i);

    public abstract int getSimState(int i);

    public abstract String getSimTypeStringBySlotId(int i);

    public abstract Uri getSimUriBySlotId(int i);

    public abstract int getSlotBySubId(int i);

    public abstract String getSmsc(int i);

    public abstract int getSubIdBySlot(int i);

    public abstract FtSubInfo getSubInfoBySlot(int i);

    public abstract FtSubInfo getSubInfoBySubId(int i);

    public abstract boolean isFdnEnabed(int i);

    public abstract boolean isIdle(int i);

    public abstract boolean isMultiSimCard();

    public abstract boolean isOffhook(int i);

    public abstract boolean isPhoneBookReady(int i);

    public abstract boolean isRadioOn(int i);

    public abstract boolean isRinging(int i);

    public abstract boolean isSimInserted(int i);

    public abstract boolean isSimPbAnrSupported(int i);

    public abstract boolean isSimPbEmailSupported(int i);

    public abstract boolean isValidSubId(int i);

    public abstract void sendMultipartTextMessage(String str, String str2, ArrayList<String> arrayList, ArrayList<PendingIntent> arrayList2, ArrayList<PendingIntent> arrayList3, int i);

    public abstract void sendMultipartTextMessage(String str, String str2, ArrayList<String> arrayList, ArrayList<PendingIntent> arrayList2, ArrayList<PendingIntent> arrayList3, Bundle bundle, int i);

    public abstract boolean setDataSub(int i);

    public abstract void setSmsc(String str, int i);

    public void setBandMode(int bandMode, Message response, int slotId) {
    }

    public void setDiversityMode(int rat, int operatorCode, Message response) {
    }

    public boolean setPreferredNetworkType(int subId, int networkType) {
        return false;
    }
}
