package com.android.internal.telephony;

import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.FtSubInfo;
import android.telephony.FtTelephony;
import android.telephony.FtTelephony.SmsStorageStatus;
import android.telephony.FtTelephonyApiParams;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.codeaurora.internal.IExtTelephony;
import org.codeaurora.internal.IExtTelephony.Stub;

public class FtTelephonyAdapterImpl extends FtTelephony {
    private static final int CARD_NOT_PRESENT = -2;
    private static final boolean DBG = (!ENG ? SystemProperties.getBoolean("debug.telephony", false) : true);
    private static final boolean ENG = Build.TYPE.equals("eng");
    private static final int INVALID_STATE = -1;
    private static final int NOT_PROVISIONED = 0;
    private static final int PROVISIONED = 1;
    private static final boolean QMI_PBM_ENABLED = SystemProperties.getBoolean("persist.vivo.qmi.pbm.enabled", false);
    private static final int SIM_PB_CAPABILITIES_COUNT = 9;
    private static final int SIM_PB_CAPABILITY = 0;
    private static final int SIM_PB_CAPABILITY_ANR = 2;
    private static final int SIM_PB_CAPABILITY_EMAIL = 1;
    private static final int SIM_PB_MAX_ADNUM_COUNT = 7;
    private static final int SIM_PB_MAX_ADNUM_LEN = 8;
    private static final int SIM_PB_MAX_EMAIL_COUNT = 5;
    private static final int SIM_PB_MAX_EMAIL_LEN = 6;
    private static final int SIM_PB_MAX_NAME_LEN = 3;
    private static final int SIM_PB_MAX_NUM_LEN = 4;
    private static final int SIM_PB_MAX_RECORDS = 2;
    private static final int SIM_PB_USED_RECORDS = 1;
    protected static final String TAG = "FtTelephonyAdapterImpl";
    private static boolean isMultiSimCard = false;
    private static int[] networkModes = new int[11];
    private Context mContext = null;
    private final int[] mSimCardIcon = new int[]{50463606, 50463607};
    private SubscriptionManager mSubManager = null;

    public interface SimType {
        public static final String SIM_TYPE_CSIM_TAG = "CSIM";
        public static final int SIM_TYPE_NONE = -1;
        public static final int SIM_TYPE_SIM = 0;
        public static final String SIM_TYPE_SIM_TAG = "SIM";
        public static final int SIM_TYPE_UIM = 2;
        public static final String SIM_TYPE_UIM_TAG = "RUIM";
        public static final int SIM_TYPE_USIM = 1;
        public static final String SIM_TYPE_USIM_TAG = "USIM";
    }

    private static class SimUri {
        public static final Uri mIccUri = buildPrivilegeUri(Uri.parse("content://icc/adn/"));

        private SimUri() {
        }

        private static Uri buildPrivilegeUri(Uri uri) {
            return FtTelephonyAdapterImpl.getVisitorUri(uri);
        }
    }

    public FtTelephonyAdapterImpl() {
        networkModes[0] = 13;
        networkModes[1] = 14;
        networkModes[2] = 15;
        networkModes[3] = 16;
        networkModes[4] = 17;
        networkModes[5] = 18;
        networkModes[6] = 19;
        networkModes[7] = 20;
        networkModes[8] = 21;
        networkModes[9] = 22;
        networkModes[10] = 40;
    }

    public FtTelephonyAdapterImpl(Context context) {
        networkModes[0] = 13;
        networkModes[1] = 14;
        networkModes[2] = 15;
        networkModes[3] = 16;
        networkModes[4] = 17;
        networkModes[5] = 18;
        networkModes[6] = 19;
        networkModes[7] = 20;
        networkModes[8] = 21;
        networkModes[9] = 22;
        networkModes[10] = 40;
        Context appContext = context.getApplicationContext();
        if (appContext != null) {
            this.mContext = appContext;
        } else {
            this.mContext = context;
        }
        this.mSubManager = SubscriptionManager.from(context);
        isMultiSimCard = getTelephonyManager(this.mContext).isMultiSimEnabled();
    }

    private boolean isValidSlotId(int slot) {
        return slot == 0 || slot == 1;
    }

    private static Uri getVisitorUri(Uri uri) {
        if (uri != null) {
            return uri.buildUpon().appendQueryParameter("secret_privilege", "true").build();
        }
        return uri;
    }

    public int getNetworkMode(int index) {
        if (index < 0 || index >= 11) {
            return 0;
        }
        return networkModes[index];
    }

    public boolean isMultiSimCard() {
        return isMultiSimCard;
    }

    public boolean isSimInserted(int slotId) {
        if (slotId < 0 || slotId > 1) {
            return false;
        }
        boolean isInserted = TelephonyManager.getDefault().hasIccCard(slotId);
        if (DBG) {
            Slog.d(TAG, "isSimInserted(" + slotId + ") is : " + isInserted);
        }
        return isInserted;
    }

    public boolean isRadioOn(int slotId) {
        boolean z = true;
        IExtTelephony extTelephony = Stub.asInterface(ServiceManager.getService("extphone"));
        if (extTelephony == null) {
            return false;
        }
        int uiccProvisonStatus;
        try {
            uiccProvisonStatus = extTelephony.getUiccCardProvisioningUserPreference(slotId);
            if (DBG) {
                Slog.d(TAG, "sim" + (slotId + 1) + ".isRadioOn uiccProvisonStatus = " + uiccProvisonStatus);
            }
        } catch (RemoteException e) {
            uiccProvisonStatus = -1;
            Slog.e(TAG, "sim" + (slotId + 1) + ".isRadioOn catch RemoteException." + e.toString());
        } catch (NullPointerException e2) {
            uiccProvisonStatus = -1;
            Slog.e(TAG, "sim" + (slotId + 1) + ".isRadioOn catch NullPointerException." + e2.toString());
        }
        if (1 != uiccProvisonStatus) {
            z = false;
        }
        return z;
    }

    public int getInsertedSimCount() {
        int mSimCount;
        boolean slot_1_inserted = isSimInserted(SIM_SLOT_1);
        boolean slot_2_inserted = isSimInserted(SIM_SLOT_2);
        if (isMultiSimCard()) {
            if (slot_1_inserted && slot_2_inserted) {
                mSimCount = 2;
            } else if (slot_1_inserted && (slot_2_inserted ^ 1) != 0) {
                mSimCount = 1;
            } else if (slot_1_inserted || !slot_2_inserted) {
                mSimCount = 0;
            } else {
                mSimCount = 1;
            }
        } else if (slot_1_inserted) {
            mSimCount = 1;
        } else {
            mSimCount = 0;
        }
        if (DBG) {
            Slog.d(TAG, "getSimCount is : " + mSimCount);
        }
        return mSimCount;
    }

    public int getSimState(int slotId) {
        if (slotId < 0 || slotId > 1) {
            return 0;
        }
        int simState;
        if (isMultiSimCard()) {
            simState = getTelephonyManager(this.mContext).getSimState(slotId);
        } else {
            simState = getTelephonyManager(this.mContext).getSimState();
        }
        if (DBG) {
            Slog.d(TAG, "sim" + (slotId + 1) + ".getSimState = " + simState);
        }
        return simState;
    }

    public int getCallState(int slotId) {
        if (slotId < 0 || slotId > 1) {
            return 0;
        }
        int callState;
        if (isMultiSimCard()) {
            int[] subId = SubscriptionManager.getSubId(slotId);
            if (subId == null || subId.length <= 0 || subId[0] <= 0) {
                callState = 0;
            } else {
                callState = getTelephonyManager(this.mContext).getCallState(subId[0]);
            }
        } else {
            callState = getTelephonyManager(this.mContext).getCallState();
        }
        if (DBG) {
            Slog.d(TAG, "getCallState is : " + callState);
        }
        return callState;
    }

    public int getSubIdBySlot(int slotId) {
        if (slotId < 0 || slotId > 1) {
            return -1;
        }
        int subId = -1;
        List<SubscriptionInfo> subInfoList = this.mSubManager == null ? null : this.mSubManager.getActiveSubscriptionInfoList();
        if (subInfoList == null) {
            return -1;
        }
        for (SubscriptionInfo subInfo : subInfoList) {
            if (subInfo != null && subInfo.getSimSlotIndex() == slotId) {
                subId = subInfo.getSubscriptionId();
                break;
            }
        }
        if (DBG) {
            Slog.d(TAG, "getSubIdBySlot(" + slotId + ") return subId is :" + subId);
        }
        return subId;
    }

    public int getSlotBySubId(int subId) {
        if (subId <= 0) {
            return -1;
        }
        int slotId;
        if (isMultiSimCard()) {
            slotId = SubscriptionManager.getSlotIndex(subId);
        } else {
            slotId = 0;
        }
        if (DBG) {
            Slog.d(TAG, "getSlotBySubId(" + subId + ") return slotId is :" + slotId);
        }
        return slotId;
    }

    public FtSubInfo getSubInfoBySubId(int subId) {
        if (subId <= 0) {
            return null;
        }
        FtSubInfo subInfo = new FtSubInfo();
        if (isMultiSimCard()) {
            SubscriptionInfo subRecord = null;
            if (this.mSubManager != null) {
                subRecord = this.mSubManager.getActiveSubscriptionInfo(subId);
            }
            if (subRecord == null) {
                return null;
            }
            subInfo.mSubId = subRecord.getSubscriptionId();
            subInfo.mIccId = subRecord.getIccId();
            subInfo.mSlotId = subRecord.getSimSlotIndex();
            if (subRecord.getDisplayName() != null) {
                subInfo.mDisplayName = subRecord.getDisplayName().toString();
            } else {
                subInfo.mDisplayName = "";
            }
            subInfo.mNameSource = subRecord.getNameSource();
            subInfo.mColor = subRecord.getIconTint();
            subInfo.mNumber = subRecord.getNumber();
            subInfo.mDispalyNumberFormat = 0;
            subInfo.mDataRoaming = subRecord.getDataRoaming();
            if (isValidSlotId(subInfo.mSlotId)) {
                subInfo.mSimIconRes = this.mSimCardIcon[subInfo.mSlotId];
            } else {
                subInfo.mSimIconRes = 0;
            }
            subInfo.mMcc = subRecord.getMcc();
            subInfo.mMnc = subRecord.getMnc();
            if (subRecord.getCarrierName() != null) {
                subInfo.mCarrierName = subRecord.getCarrierName().toString();
            } else {
                subInfo.mCarrierName = "";
            }
            if (this.mContext != null) {
                subInfo.mIconBitmap = subRecord.createIconBitmap(this.mContext);
            }
            subInfo.mCountryIso = subRecord.getCountryIso();
            subInfo.mStatus = 0;
            subInfo.mNwMode = -1;
        } else if (isSimInserted(SIM_SLOT_1)) {
            subInfo.mSubId = -1;
            subInfo.mIccId = "";
            subInfo.mSlotId = -1;
            subInfo.mDisplayName = "";
            subInfo.mCarrierName = "";
            subInfo.mNameSource = 0;
            subInfo.mColor = 0;
            subInfo.mNumber = "";
            subInfo.mDispalyNumberFormat = 0;
            subInfo.mDataRoaming = 0;
            subInfo.mSimIconRes = 0;
            subInfo.mIconBitmap = null;
            subInfo.mMcc = 0;
            subInfo.mMnc = 0;
            subInfo.mCountryIso = "";
            subInfo.mStatus = 0;
            subInfo.mNwMode = -1;
        } else {
            subInfo = null;
        }
        if (subInfo != null) {
            if (DBG) {
                Slog.d(TAG, "getSubInfoBySubId subInfo is : " + subInfo);
            }
            return subInfo;
        }
        if (DBG) {
            Slog.d(TAG, "getSubInfoBySubId subInfo is : null");
        }
        return null;
    }

    public FtSubInfo getSubInfoBySlot(int slotId) {
        if (slotId < 0 || slotId > 1) {
            return null;
        }
        FtSubInfo subInfo = new FtSubInfo();
        if (isMultiSimCard()) {
            SubscriptionInfo subRecord = null;
            if (this.mSubManager != null) {
                subRecord = this.mSubManager.getActiveSubscriptionInfoForSimSlotIndex(slotId);
            }
            if (subRecord == null) {
                return null;
            }
            subInfo.mSubId = subRecord.getSubscriptionId();
            subInfo.mIccId = subRecord.getIccId();
            subInfo.mSlotId = subRecord.getSimSlotIndex();
            if (subRecord.getDisplayName() != null) {
                subInfo.mDisplayName = subRecord.getDisplayName().toString();
            } else {
                subInfo.mDisplayName = "";
            }
            subInfo.mNameSource = subRecord.getNameSource();
            subInfo.mColor = subRecord.getIconTint();
            subInfo.mNumber = subRecord.getNumber();
            subInfo.mDispalyNumberFormat = 0;
            subInfo.mDataRoaming = subRecord.getDataRoaming();
            if (isValidSlotId(subInfo.mSlotId)) {
                subInfo.mSimIconRes = this.mSimCardIcon[subInfo.mSlotId];
            } else {
                subInfo.mSimIconRes = 0;
            }
            subInfo.mMcc = subRecord.getMcc();
            subInfo.mMnc = subRecord.getMnc();
            if (subRecord.getCarrierName() != null) {
                subInfo.mCarrierName = subRecord.getCarrierName().toString();
            } else {
                subInfo.mCarrierName = "";
            }
            if (this.mContext != null) {
                subInfo.mIconBitmap = subRecord.createIconBitmap(this.mContext);
            }
            subInfo.mCountryIso = subRecord.getCountryIso();
            subInfo.mStatus = 0;
            subInfo.mNwMode = -1;
        } else if (isSimInserted(SIM_SLOT_1)) {
            subInfo.mSubId = -1;
            subInfo.mIccId = "";
            subInfo.mSlotId = -1;
            subInfo.mDisplayName = "";
            subInfo.mCarrierName = "";
            subInfo.mNameSource = 0;
            subInfo.mColor = 0;
            subInfo.mNumber = "";
            subInfo.mDispalyNumberFormat = 0;
            subInfo.mDataRoaming = 0;
            subInfo.mSimIconRes = 0;
            subInfo.mIconBitmap = null;
            subInfo.mMcc = 0;
            subInfo.mMnc = 0;
            subInfo.mCountryIso = "";
            subInfo.mStatus = 0;
            subInfo.mNwMode = -1;
        } else {
            subInfo = null;
        }
        if (subInfo != null) {
            if (DBG) {
                Slog.d(TAG, "getSubInfoBySlot subInfo is : " + subInfo);
            }
            return subInfo;
        }
        if (DBG) {
            Slog.d(TAG, "getSubInfoBySlot subInfo is : null");
        }
        return null;
    }

    public List<FtSubInfo> getAllSubInfoList() {
        ArrayList<FtSubInfo> infoList = new ArrayList();
        Iterable subInfo = null;
        if (this.mSubManager != null) {
            subInfo = this.mSubManager.getAllSubscriptionInfoList();
        }
        if (subInfo == null || subInfo.size() == 0) {
            Slog.w(TAG, "getAllSubInfoList.subInfo == null");
            return null;
        }
        for (SubscriptionInfo ss : subInfo) {
            infoList.add(covertOtherSubInfoToMe(ss));
        }
        return infoList;
    }

    public boolean isFdnEnabed(int slotId) {
        return false;
    }

    private ITelephony getITelephony() {
        return ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
    }

    private TelephonyManager getTelephonyManager(Context context) {
        return (TelephonyManager) context.getSystemService("phone");
    }

    private boolean isSimStateReady(int slotId) {
        return 5 == getSimState(slotId);
    }

    public boolean isPhoneBookReady(int slotId) {
        return isSimStateReady(slotId);
    }

    public int[] getSimCapacityBySlotId(int slotId) {
        if (QMI_PBM_ENABLED) {
            int[] simPbCapabilities = getSimPbCapabilities(slotId);
            return new int[]{simPbCapabilities[1], simPbCapabilities[2]};
        } else if (slotId < 0 || slotId > 1) {
            return new int[]{0, 0};
        } else {
            int[] subId = SubscriptionManager.getSubId(slotId);
            if (subId == null || subId.length <= 0 || subId[0] <= 0) {
                Slog.w(TAG, "getSimCapacityBySlotId sim" + (slotId + 1) + " get subId error");
                return new int[]{0, 0};
            }
            int adnCount = 0;
            try {
                IIccPhoneBook simPhoneBook = getIIccPhoneBook();
                if (simPhoneBook != null) {
                    adnCount = isMultiSimCard() ? simPhoneBook.getAdnCountUsingSubId(subId[0]) : simPhoneBook.getAdnCount();
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "sim" + (slotId + 1) + ".getSimCapacityBySlotId catch RemoteException!");
            } catch (SecurityException e2) {
                Slog.e(TAG, "sim" + (slotId + 1) + ".getSimCapacityBySlotId catch SecurityException!");
            }
            if (DBG) {
                Slog.d(TAG, "sim" + (slotId + 1) + ".getSimCapacityBySlotId.adnCount = " + adnCount);
            }
            Uri simuri = getSimUriBySlotId(slotId);
            Cursor cursor = null;
            int simCount = 0;
            if (!(simuri == null || this.mContext == null)) {
                try {
                    cursor = this.mContext.getContentResolver().query(simuri, null, null, null, null);
                    if (cursor != null) {
                        simCount = cursor.getCount();
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            Slog.i(TAG, "sim" + (slotId + 1) + ".getSimCapacityBySlotId.simcount = " + simCount + " , simuri = " + simuri);
            return new int[]{simCount, adnCount};
        }
    }

    public Uri getSimUriBySlotId(int slotId) {
        if (slotId < 0 || slotId > 1) {
            return null;
        }
        Uri uri;
        if (isMultiSimCard()) {
            int[] subId = SubscriptionManager.getSubId(slotId);
            if (subId == null || subId.length <= 0 || subId[0] <= 0) {
                Slog.w(TAG, "sim" + (slotId + 1) + " get subId error");
                return null;
            }
            uri = SimUri.buildPrivilegeUri(ContentUris.withAppendedId(Uri.parse("content://icc/adn/subId"), (long) subId[0]));
        } else {
            uri = SimUri.mIccUri;
        }
        return uri;
    }

    public String getIMSIBySlotId(int slotId) {
        String imsi;
        if (isMultiSimCard()) {
            int[] subId = SubscriptionManager.getSubId(slotId);
            if (subId == null || subId.length <= 0 || subId[0] <= 0) {
                Slog.w(TAG, "sim" + (slotId + 1) + " get subId error");
                return null;
            }
            imsi = getTelephonyManager(this.mContext).getSubscriberId(subId[0]);
        } else {
            imsi = getTelephonyManager(this.mContext).getSubscriberId();
        }
        Slog.i(TAG, "sim" + (slotId + 1) + ".getIMSIBySlotId.imsi = " + imsi);
        return imsi;
    }

    private IIccPhoneBook getIIccPhoneBook() {
        return IIccPhoneBook.Stub.asInterface(ServiceManager.getService("simphonebook"));
    }

    public int getAnrCount(int slotId) {
        int anrCount = 0;
        if (slotId < 0 || slotId > 1) {
            return 0;
        }
        try {
            IIccPhoneBook simPhoneBook = getIIccPhoneBook();
            int[] subId = SubscriptionManager.getSubId(slotId);
            if (subId == null || subId.length <= 0 || subId[0] <= 0) {
                Slog.w(TAG, "getAnrCount sim" + (slotId + 1) + " get subId error");
                return 0;
            }
            if (simPhoneBook != null) {
                anrCount = isMultiSimCard() ? simPhoneBook.getAnrCountUsingSubId(subId[0]) : simPhoneBook.getAnrCount();
            }
            return anrCount;
        } catch (SecurityException e) {
            Slog.e(TAG, "sim" + (slotId + 1) + ".getAnrCount catch SecurityException." + e.toString());
        } catch (RemoteException e2) {
            Slog.e(TAG, "sim" + (slotId + 1) + ".getAnrCount catch RemoteException." + e2.toString());
        }
    }

    public int getEmailCount(int slotId) {
        int emailCount = 0;
        if (slotId < 0 || slotId > 1) {
            return 0;
        }
        try {
            IIccPhoneBook simPhoneBook = getIIccPhoneBook();
            int[] subId = SubscriptionManager.getSubId(slotId);
            if (subId == null || subId.length <= 0 || subId[0] <= 0) {
                Slog.w(TAG, "getEmailCount sim" + (slotId + 1) + " get subId error");
                return 0;
            }
            if (simPhoneBook != null) {
                emailCount = isMultiSimCard() ? simPhoneBook.getEmailCountUsingSubId(subId[0]) : simPhoneBook.getEmailCount();
            }
            return emailCount;
        } catch (SecurityException e) {
            Slog.e(TAG, "sim" + (slotId + 1) + ".getEmailCount catch SecurityException." + e.toString());
        } catch (RemoteException e2) {
            Slog.e(TAG, "sim" + (slotId + 1) + ".getEmailCount catch RemoteException." + e2.toString());
        }
    }

    public int getActiveSubCount() {
        int subCount;
        boolean slot_1_active = isRadioOn(SIM_SLOT_1);
        boolean slot_2_active = isRadioOn(SIM_SLOT_2);
        if (isMultiSimCard()) {
            if (slot_1_active && slot_2_active) {
                subCount = 2;
            } else if (slot_1_active && (slot_2_active ^ 1) != 0) {
                subCount = 1;
            } else if (slot_1_active || !slot_2_active) {
                subCount = 0;
            } else {
                subCount = 1;
            }
        } else if (slot_1_active) {
            subCount = 1;
        } else {
            subCount = 0;
        }
        if (DBG) {
            Slog.d(TAG, "getActiveSubCount is : " + subCount);
        }
        return subCount;
    }

    public boolean isIdle(int slotId) {
        ITelephony iTelephony;
        if (!isMultiSimCard()) {
            iTelephony = getITelephony();
            if (!(iTelephony == null || this.mContext == null)) {
                try {
                    return iTelephony.isIdle(this.mContext.getOpPackageName());
                } catch (RemoteException e) {
                    Slog.e(TAG, "RemoteException when calling iTelephony.isIdle()");
                }
            }
        } else if (slotId < 0 || slotId > 1) {
            return false;
        } else {
            int[] subIds = SubscriptionManager.getSubId(slotId);
            if (subIds == null || subIds.length <= 0 || subIds[0] <= 0) {
                return true;
            }
            iTelephony = getITelephony();
            if (!(iTelephony == null || this.mContext == null)) {
                try {
                    return iTelephony.isIdleForSubscriber(subIds[0], this.mContext.getOpPackageName());
                } catch (RemoteException e2) {
                    Slog.e(TAG, "sim" + (slotId + 1) + ".isIdle catch RemoteException!");
                }
            }
        }
        return false;
    }

    public boolean isOffhook(int slotId) {
        ITelephony iTelephony;
        if (!isMultiSimCard()) {
            iTelephony = getITelephony();
            if (!(iTelephony == null || this.mContext == null)) {
                try {
                    return iTelephony.isOffhook(this.mContext.getOpPackageName());
                } catch (RemoteException e) {
                    Slog.e(TAG, "RemoteException when calling iTelephony.isOffhook()");
                }
            }
        } else if (slotId < 0 || slotId > 1) {
            return false;
        } else {
            int[] subIds = SubscriptionManager.getSubId(slotId);
            if (subIds == null || subIds.length <= 0 || subIds[0] <= 0) {
                return false;
            }
            iTelephony = getITelephony();
            if (!(iTelephony == null || this.mContext == null)) {
                try {
                    return iTelephony.isOffhookForSubscriber(subIds[0], this.mContext.getOpPackageName());
                } catch (RemoteException e2) {
                    Slog.e(TAG, "sim" + (slotId + 1) + ".isOffhook catch RemoteException!");
                }
            }
        }
        return false;
    }

    public boolean isRinging(int slotId) {
        ITelephony iTelephony;
        if (!isMultiSimCard()) {
            iTelephony = getITelephony();
            if (!(iTelephony == null || this.mContext == null)) {
                try {
                    return iTelephony.isRinging(this.mContext.getOpPackageName());
                } catch (RemoteException e) {
                    Slog.e(TAG, "RemoteException when calling iTelephony.isRinging()");
                }
            }
        } else if (slotId < 0 || slotId > 1) {
            return false;
        } else {
            int[] subIds = SubscriptionManager.getSubId(slotId);
            if (subIds == null || subIds.length <= 0 || subIds[0] <= 0) {
                return false;
            }
            iTelephony = getITelephony();
            if (!(iTelephony == null || this.mContext == null)) {
                try {
                    return iTelephony.isRingingForSubscriber(subIds[0], this.mContext.getOpPackageName());
                } catch (RemoteException e2) {
                    Slog.e(TAG, "sim" + (slotId + 1) + ".isRinging catch RemoteException!");
                }
            }
        }
        return false;
    }

    public List<FtSubInfo> getActiveSubInfoList() {
        ArrayList<FtSubInfo> infoList = new ArrayList();
        Iterable subInfo = null;
        if (this.mSubManager != null) {
            subInfo = this.mSubManager.getActiveSubscriptionInfoList();
        }
        if (subInfo == null || subInfo.size() == 0) {
            Slog.w(TAG, "getActiveSubInfoList.subInfo is empty");
            return infoList;
        }
        for (SubscriptionInfo ss : subInfo) {
            if (ss != null && isRadioOn(ss.getSimSlotIndex())) {
                infoList.add(covertOtherSubInfoToMe(ss));
            }
        }
        return infoList;
    }

    public List<FtSubInfo> getInsertedSubInfoList() {
        ArrayList<FtSubInfo> infoList = new ArrayList();
        Iterable subInfo = null;
        if (this.mSubManager != null) {
            subInfo = this.mSubManager.getActiveSubscriptionInfoList();
        }
        if (subInfo == null || subInfo.size() == 0) {
            Slog.w(TAG, "getInsertedSubInfoList.subInfo is empty");
            return infoList;
        }
        for (SubscriptionInfo ss : subInfo) {
            infoList.add(covertOtherSubInfoToMe(ss));
        }
        return infoList;
    }

    private FtSubInfo covertOtherSubInfoToMe(SubscriptionInfo subRecord) {
        FtSubInfo subInfo = new FtSubInfo();
        subInfo.mSubId = subRecord.getSubscriptionId();
        subInfo.mIccId = subRecord.getIccId();
        subInfo.mSlotId = subRecord.getSimSlotIndex();
        if (subRecord.getDisplayName() != null) {
            subInfo.mDisplayName = subRecord.getDisplayName().toString();
        } else {
            subInfo.mDisplayName = "";
        }
        subInfo.mNameSource = subRecord.getNameSource();
        subInfo.mColor = subRecord.getIconTint();
        subInfo.mNumber = subRecord.getNumber();
        subInfo.mDispalyNumberFormat = 0;
        subInfo.mDataRoaming = subRecord.getDataRoaming();
        if (isValidSlotId(subInfo.mSlotId)) {
            subInfo.mSimIconRes = this.mSimCardIcon[subInfo.mSlotId];
        } else {
            subInfo.mSimIconRes = 0;
        }
        subInfo.mMcc = subRecord.getMcc();
        subInfo.mMnc = subRecord.getMnc();
        if (subRecord.getCarrierName() != null) {
            subInfo.mCarrierName = subRecord.getCarrierName().toString();
        } else {
            subInfo.mCarrierName = "";
        }
        if (this.mContext != null) {
            subInfo.mIconBitmap = subRecord.createIconBitmap(this.mContext);
        }
        subInfo.mCountryIso = subRecord.getCountryIso();
        subInfo.mStatus = 0;
        subInfo.mNwMode = -1;
        return subInfo;
    }

    private String getIccCardType(int slotId) {
        if (isMultiSimCard()) {
            try {
                VivoTelephonyApiParams param = new VivoTelephonyApiParams("API_TAG_getIccCardType");
                param.put("slot", Integer.valueOf(slotId));
                VivoTelephonyApiParams ret = getITelephony().vivoTelephonyApi(param);
                if (ret != null) {
                    return ret.getAsString("iccCardType");
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "sim" + (slotId + 1) + ".getIccCardType catch RemoteException!");
            } catch (NullPointerException e2) {
                Slog.e(TAG, "sim" + (slotId + 1) + ".getIccCardType catch NullPointerException!");
            }
            return null;
        } else if (slotId == SIM_SLOT_1) {
            return getIccCardType();
        } else {
            return null;
        }
    }

    private String getIccCardType() {
        try {
            VivoTelephonyApiParams ret = getITelephony().vivoTelephonyApi(new VivoTelephonyApiParams("API_TAG_getIccCardType"));
            if (ret != null) {
                return ret.getAsString("iccCardType");
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "getIccCardType RemoteException!");
        } catch (NullPointerException e2) {
            Slog.e(TAG, "getIccCardType NullPointerException!");
        }
        return null;
    }

    private boolean get3gTransferTo2gFlag(int slotId) {
        boolean use2gPhonebook = false;
        try {
            IIccPhoneBook simPhoneBook = getIIccPhoneBook();
            if (simPhoneBook != null) {
                use2gPhonebook = simPhoneBook.get3gTransferTo2gFlag(slotId);
            }
        } catch (RemoteException ex) {
            Slog.d(TAG, "sim" + (slotId + 1) + ".get3gTransferTo2gFlag.ex = " + ex.getMessage());
        } catch (SecurityException ex2) {
            Slog.d(TAG, "sim" + (slotId + 1) + ".get3gTransferTo2gFlag.ex = " + ex2.getMessage());
        }
        Slog.d(TAG, "sim" + (slotId + 1) + ".get3gTransferTo2gFlag = " + use2gPhonebook);
        return use2gPhonebook;
    }

    public String getSimTypeStringBySlotId(int slotId) {
        String str = null;
        String simType = getIccCardType(slotId);
        if (SimType.SIM_TYPE_USIM_TAG.equals(simType)) {
            str = SimType.SIM_TYPE_USIM_TAG;
        } else if (SimType.SIM_TYPE_UIM_TAG.equals(simType)) {
            str = SimType.SIM_TYPE_SIM_TAG;
        } else if (SimType.SIM_TYPE_SIM_TAG.equals(simType)) {
            str = SimType.SIM_TYPE_SIM_TAG;
        } else if (SimType.SIM_TYPE_CSIM_TAG.equals(simType)) {
            if (get3gTransferTo2gFlag(slotId)) {
                str = SimType.SIM_TYPE_SIM_TAG;
            } else {
                str = SimType.SIM_TYPE_USIM_TAG;
            }
        }
        if (DBG) {
            Slog.d(TAG, "sim" + (slotId + 1) + ".getSimTypeStringBySlotId.simType =  " + simType);
        }
        if (DBG) {
            Slog.d(TAG, "sim" + (slotId + 1) + ".getSimTypeStringBySlotId.str =  " + str);
        }
        return str;
    }

    public int getActiveSubId() {
        int subId = -1;
        List<FtSubInfo> infoList = getActiveSubInfoList();
        if (infoList == null || infoList.size() == 0) {
            return -1;
        }
        for (FtSubInfo subInfo : infoList) {
            if (subInfo != null && isValidSubId(subInfo.mSubId)) {
                subId = subInfo.mSubId;
                break;
            }
        }
        return subId;
    }

    public String getMeid() {
        try {
            VivoTelephonyApiParams ret = getITelephony().vivoTelephonyApi(new VivoTelephonyApiParams("API_TAG_getMeid"));
            if (ret != null) {
                return ret.getAsString("meid");
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "getMeidOfCdmaPhone catch RemoteException!");
        } catch (NullPointerException e2) {
            Slog.e(TAG, "getMeidOfCdmaPhone catch NullPointerException!");
        }
        return "";
    }

    public String getImei(int slotId) {
        if (isMultiSimCard()) {
            return getTelephonyManager(this.mContext).getImei(slotId);
        }
        return getTelephonyManager(this.mContext).getImei();
    }

    public String getSmsc(int slotId) {
        try {
            VivoTelephonyApiParams param = new VivoTelephonyApiParams("API_TAG_getSmsc");
            param.put("slot", Integer.valueOf(slotId));
            VivoTelephonyApiParams ret = getITelephony().vivoTelephonyApi(param);
            if (ret != null) {
                return ret.getAsString("sc_address");
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "sim" + (slotId + 1) + ".getSmsc catch RemoteException!");
        } catch (NullPointerException e2) {
            Slog.e(TAG, "sim" + (slotId + 1) + ".getSmsc catch NullPointerException!");
        }
        return "";
    }

    public void setSmsc(String scAddress, int slotId) {
        try {
            VivoTelephonyApiParams param = new VivoTelephonyApiParams("API_TAG_setSmsc");
            param.put("slot", Integer.valueOf(slotId));
            param.put("sc_address", scAddress);
            getITelephony().vivoTelephonyApi(param);
        } catch (RemoteException e) {
            Slog.e(TAG, "sim" + (slotId + 1) + ".setSmsc catch RemoteException!");
        } catch (NullPointerException e2) {
            Slog.e(TAG, "sim" + (slotId + 1) + ".setSmsc catch NullPointerException!");
        }
    }

    public SmsStorageStatus getSimMemoryStatus(int slotId) {
        SmsStorageStatus status = new SmsStorageStatus(this);
        status.mUsed = 0;
        status.mTotal = 0;
        try {
            VivoTelephonyApiParams param = new VivoTelephonyApiParams("API_TAG_getStoreMaxSize");
            param.put("slot", Integer.valueOf(slotId));
            VivoTelephonyApiParams ret = getITelephony().vivoTelephonyApi(param);
            if (ret != null) {
                status.mTotal = ret.getAsInteger("recordSize0").intValue();
                status.mUsed = status.mTotal - ret.getAsInteger("recordSize1").intValue();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "getSimMemoryStatus catch RemoteException!");
        }
        return status;
    }

    public void sendMultipartTextMessage(String dstAddress, String scAddress, ArrayList<String> messages, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Bundle extraParams, int subId) {
        if (extraParams != null) {
            int priority = extraParams.getInt("priority", 1);
            boolean isExpectMore = extraParams.getBoolean("is_expect_more", false);
            int validityPeriod = extraParams.getInt("validity_period", 255);
            if (!isMultiSimCard()) {
                SmsManager.getDefault().sendMultipartTextMessage(dstAddress, scAddress, messages, sentIntents, deliveryIntents, priority, isExpectMore, validityPeriod);
            } else if (subId > 0) {
                SmsManager.getSmsManagerForSubscriptionId(subId).sendMultipartTextMessage(dstAddress, scAddress, messages, sentIntents, deliveryIntents, priority, isExpectMore, validityPeriod);
            }
        }
    }

    public void sendMultipartTextMessage(String dstAddress, String scAddress, ArrayList<String> messages, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, int subId) {
        if (!isMultiSimCard()) {
            SmsManager.getDefault().sendMultipartTextMessage(dstAddress, scAddress, messages, sentIntents, deliveryIntents);
        } else if (subId > 0) {
            SmsManager.getSmsManagerForSubscriptionId(subId).sendMultipartTextMessage(dstAddress, scAddress, messages, sentIntents, deliveryIntents);
        }
    }

    public String getApnOperator(int subId) {
        try {
            VivoTelephonyApiParams param = new VivoTelephonyApiParams("API_TAG_getIccOperatorNumericForData");
            param.put("subId", Integer.valueOf(subId));
            VivoTelephonyApiParams ret = getITelephony().vivoTelephonyApi(param);
            if (ret != null) {
                return ret.getAsString("result");
            }
        } catch (RemoteException e) {
            log("subId" + subId + ".getApnOperator catch RemoteException!");
        } catch (NullPointerException e2) {
            log("subId" + subId + ".getApnOperator catch NullPointerException!");
        }
        return null;
    }

    public String getImsStateChangeBroadcast() {
        return "vivo.intent.action.IMS_SERVICE_STATE";
    }

    public void setBandMode(int bandMode, Message response, int phoneId) {
        Phone mPhone = PhoneFactory.getPhone(phoneId);
    }

    public void setDiversityMode(int rat, int operatorCode, Message response) {
        Phone mPhone = PhoneFactory.getDefaultPhone();
    }

    public boolean setDataSub(int subId) {
        if (this.mSubManager != null) {
            this.mSubManager.setDefaultDataSubId(subId);
        }
        return true;
    }

    public int getPhoneId(int subId) {
        if (subId <= 0) {
            return -1;
        }
        int phoneId;
        if (isMultiSimCard()) {
            phoneId = SubscriptionManager.getPhoneId(subId);
        } else {
            phoneId = 0;
        }
        if (DBG) {
            Slog.d(TAG, "getPhoneId(" + subId + ") return phoneId is :" + phoneId);
        }
        return phoneId;
    }

    public int getInvalidSubId() {
        return -1;
    }

    public boolean isValidSubId(int subId) {
        return SubscriptionManager.isValidSubscriptionId(subId);
    }

    public int getDefaultSmsPhoneId() {
        if (this.mSubManager != null) {
            return this.mSubManager.getDefaultSmsPhoneId();
        }
        return SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultSmsSubscriptionId());
    }

    private int[] getSimPbCapabilities(int slotId) {
        int[] capabilities = new int[9];
        try {
            IIccPhoneBook simPhoneBook = getIIccPhoneBook();
            if (simPhoneBook != null) {
                int[] subId = SubscriptionManager.getSubId(slotId);
                if (subId != null && subId.length > 0) {
                    if (subId[0] > 0) {
                        capabilities = simPhoneBook.getQmiPbmCapabilities(subId[0]);
                    }
                }
                if (DBG) {
                    Slog.e(TAG, "sim" + (slotId + 1) + ".getSimPbCapabilities: get subId error");
                }
            } else if (DBG) {
                Slog.e(TAG, "sim" + (slotId + 1) + ".getSimPbCapabilities: simPhoneBook = null");
            }
        } catch (RemoteException ex) {
            if (DBG) {
                Slog.e(TAG, "sim" + (slotId + 1) + ".getSimPbCapabilities: RemoteException = " + ex.getMessage());
            }
        } catch (SecurityException ex2) {
            if (DBG) {
                Slog.e(TAG, "sim" + (slotId + 1) + ".getSimPbCapabilities: SecurityException = " + ex2.getMessage());
            }
        }
        if (DBG) {
            Slog.i(TAG, "sim" + (slotId + 1) + ".getSimPbCapabilities: capabilities = " + Arrays.toString(capabilities));
        }
        return capabilities;
    }

    public boolean isSimPbEmailSupported(int slotId) {
        boolean z = true;
        if (QMI_PBM_ENABLED) {
            if ((getSimPbCapabilities(slotId)[0] & 1) != 1) {
                z = false;
            }
            return z;
        }
        if (getEmailCount(slotId) <= 0) {
            z = false;
        }
        return z;
    }

    public boolean isSimPbAnrSupported(int slotId) {
        boolean z = true;
        if (QMI_PBM_ENABLED) {
            if ((getSimPbCapabilities(slotId)[0] & 2) != 2) {
                z = false;
            }
            return z;
        }
        if (getAnrCount(slotId) <= 0) {
            z = false;
        }
        return z;
    }

    public int getSimPbMaxNameLength(int slotId) {
        if (QMI_PBM_ENABLED) {
            return getSimPbCapabilities(slotId)[3];
        }
        int[] recordSize = new int[3];
        try {
            IIccPhoneBook simPhoneBook = getIIccPhoneBook();
            if (simPhoneBook != null) {
                int[] subId = SubscriptionManager.getSubId(slotId);
                if (subId != null && subId.length > 0) {
                    if (subId[0] > 0) {
                        recordSize = isMultiSimCard() ? simPhoneBook.getAdnRecordsSizeForSubscriber(subId[0], 28474) : simPhoneBook.getAdnRecordsSize(28474);
                    }
                }
                if (DBG) {
                    Slog.e(TAG, "sim" + (slotId + 1) + ".getSimPbMaxNameLength: get subId error");
                }
            }
        } catch (RemoteException e) {
            if (DBG) {
                Slog.e(TAG, "sim" + (slotId + 1) + ".getSimPbMaxNameLength catch RemoteException!");
            }
        } catch (SecurityException e2) {
            if (DBG) {
                Slog.e(TAG, "sim" + (slotId + 1) + ".getSimPbMaxNameLength catch SecurityException!");
            }
        }
        if (recordSize[0] > 14) {
            return recordSize[0] - 14;
        }
        return 0;
    }

    public int getDefaultDataPhoneId() {
        if (this.mSubManager != null) {
            return this.mSubManager.getDefaultDataPhoneId();
        }
        return -1;
    }

    public boolean setPreferredNetworkType(int subId, int networkType) {
        int phoneId = getPhoneId(subId);
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            return false;
        }
        try {
            VivoTelephonyApiParams param = new VivoTelephonyApiParams("API_TAG_setPreferedNetworkType");
            param.put("phoneId", Integer.valueOf(phoneId));
            param.put("networkType", Integer.valueOf(networkType));
            VivoTelephonyApiParams ret = getITelephony().vivoTelephonyApi(param);
            if (ret != null) {
                return ret.getAsBoolean("result").booleanValue();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "subId" + subId + ".setPreferredNetworkType catch RemoteException!");
        } catch (NullPointerException e2) {
            Slog.e(TAG, "subId" + subId + ".setPreferredNetworkType catch NullPointerException!");
        }
        return false;
    }

    public FtTelephonyApiParams commonApi(FtTelephonyApiParams v) {
        if (v == null) {
            Slog.w(TAG, "commonApi() v is null!");
            return null;
        }
        String tag = v.getApiTag();
        if (tag == null || tag.length() <= 0) {
            Slog.w(TAG, "commonApi() tag is empty!");
            return null;
        }
        log("commonApi() tag = " + tag);
        FtTelephonyApiParams ret;
        if ("API_TAG_getIccProviderField".equals(tag)) {
            ret = new FtTelephonyApiParams(tag);
            ret.put("str_tag", IccProvider.STR_TAG);
            ret.put("str_number", IccProvider.STR_NUMBER);
            ret.put("str_emails", IccProvider.STR_EMAILS);
            ret.put("str_anrs", IccProvider.STR_ANRS);
            ret.put("str_new_tag", IccProvider.STR_NEW_TAG);
            ret.put("str_new_number", IccProvider.STR_NEW_NUMBER);
            ret.put("str_new_emails", IccProvider.STR_NEW_EMAILS);
            ret.put("str_new_anrs", IccProvider.STR_NEW_ANRS);
            ret.put("str_index", "index");
            ret.put("str_pin2", IccProvider.STR_PIN2);
            return ret;
        } else if ("API_TAG_getNetworkType".equals(tag)) {
            ret = new FtTelephonyApiParams(tag);
            ret.put("lte_ca", Integer.valueOf(19));
            return ret;
        } else if ("API_TAG_isSupportCA".equals(tag)) {
            ret = new FtTelephonyApiParams(tag);
            String platForm = SystemProperties.get("ro.build.product", "null");
            boolean supportCa = true;
            if (platForm != null && (platForm.contains("msm8937") || platForm.contains("msm8917"))) {
                supportCa = false;
            }
            ret.put("support_ca", Boolean.valueOf(supportCa));
            return ret;
        } else if ("API_TAG_getSimRecordLengthCapacity".equals(tag)) {
            int slotId = 0;
            if (v.containsKey("slot")) {
                slotId = v.getAsInteger("slot").intValue();
            }
            String command = "default";
            if (v.containsKey("sim_command")) {
                command = v.getAsString("sim_command");
            }
            int length = 0;
            if (TextUtils.isEmpty(command) || command.equalsIgnoreCase("default")) {
                length = 0;
            } else if ("adn".equalsIgnoreCase(command)) {
                length = getSimAdnLengthCapacity(slotId);
            } else if ("email".equalsIgnoreCase(command)) {
                length = getSimEmailLengthCapacity(slotId);
            } else if ("anr".equalsIgnoreCase(command)) {
                length = getSimAnrLengthCapacity(slotId);
            }
            ret = new FtTelephonyApiParams(tag);
            ret.put("slot", Integer.valueOf(slotId));
            ret.put("length", Integer.valueOf(length));
            return ret;
        } else if (!"API_TAG_getLine1Number".equals(tag)) {
            return null;
        } else {
            String line1Number;
            Integer subIdVal = v.getAsInteger("subId");
            if (subIdVal != null) {
                line1Number = getTelephonyManager(this.mContext).getLine1Number(subIdVal.intValue());
            } else {
                line1Number = getTelephonyManager(this.mContext).getLine1Number();
            }
            ret = new FtTelephonyApiParams(tag);
            ret.put("line1Number", line1Number);
            return ret;
        }
    }

    private static void log(String msg) {
        if (DBG) {
            Slog.d(TAG, msg);
        }
    }

    private int getSimAdnLengthCapacity(int slotId) {
        int subId = getSubIdBySlot(slotId);
        return 0;
    }

    private int getSimEmailLengthCapacity(int slotId) {
        return 0;
    }

    private int getSimAnrLengthCapacity(int slotId) {
        return 0;
    }
}
