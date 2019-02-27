package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.ContentValues;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import com.android.internal.telephony.IIccPhoneBook.Stub;
import com.android.internal.telephony.uicc.AdnRecord;
import java.util.List;

public class UiccPhoneBookController extends Stub {
    private static final String TAG = "UiccPhoneBookController";
    private Phone[] mPhone;

    public UiccPhoneBookController(Phone[] phone) {
        if (ServiceManager.getService("simphonebook") == null) {
            ServiceManager.addService("simphonebook", this);
        }
        this.mPhone = phone;
    }

    @Deprecated
    public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
        return updateAdnRecordsInEfBySearchForSubscriber(getDefaultSubscription(), efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2);
    }

    @Deprecated
    public boolean updateAdnRecordsInEfBySearchForSubscriber(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateAdnRecordsInEfBySearch(efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    @Deprecated
    public boolean updateAdnRecordsInEfByIndex(int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
        return updateAdnRecordsInEfByIndexForSubscriber(getDefaultSubscription(), efid, newTag, newPhoneNumber, index, pin2);
    }

    @Deprecated
    public boolean updateAdnRecordsInEfByIndexForSubscriber(int subId, int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateAdnRecordsInEfByIndex(efid, newTag, newPhoneNumber, index, pin2);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfByIndex iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    public int[] getAdnRecordsSize(int efid) throws RemoteException {
        return getAdnRecordsSizeForSubscriber(getDefaultSubscription(), efid);
    }

    public int[] getAdnRecordsSizeForSubscriber(int subId, int efid) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getAdnRecordsSize(efid);
        }
        Rlog.e(TAG, "getAdnRecordsSize iccPbkIntMgr is null for Subscription:" + subId);
        return null;
    }

    public List<AdnRecord> getAdnRecordsInEf(int efid) throws RemoteException {
        return getAdnRecordsInEfForSubscriber(getDefaultSubscription(), efid);
    }

    public List<AdnRecord> getAdnRecordsInEfForSubscriber(int subId, int efid) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getAdnRecordsInEf(efid);
        }
        Rlog.e(TAG, "getAdnRecordsInEf iccPbkIntMgr isnull for Subscription:" + subId);
        return null;
    }

    public int[] getAdnRecordsCapacity() throws RemoteException {
        return getAdnRecordsCapacityForSubscriber(getDefaultSubscription());
    }

    public int[] getAdnRecordsCapacityForSubscriber(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getAdnRecordsCapacity();
        }
        Rlog.e(TAG, "getAdnRecordsCapacity iccPbkIntMgr is null for Subscription:" + subId);
        return null;
    }

    @Deprecated
    public boolean updateAdnRecordsWithContentValuesInEfBySearch(int efid, ContentValues values, String pin2) throws RemoteException {
        return updateAdnRecordsWithContentValuesInEfBySearchUsingSubId(getDefaultSubscription(), efid, values, pin2);
    }

    @Deprecated
    public boolean updateAdnRecordsWithContentValuesInEfBySearchUsingSubId(int subId, int efid, ContentValues values, String pin2) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateAdnRecordsWithContentValuesInEfBySearch(efid, values, pin2);
        }
        Rlog.e(TAG, "updateAdnRecordsWithContentValuesInEfBySearchUsingSubId iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getAdnCount() throws RemoteException {
        return getAdnCountUsingSubId(getDefaultSubscription());
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getAdnCountUsingSubId(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getAdnCount();
        }
        Rlog.e(TAG, "getAdnCount iccPbkIntMgr isnull for Subscription:" + subId);
        return 0;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getAnrCount() throws RemoteException {
        return getAnrCountUsingSubId(getDefaultSubscription());
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getAnrCountUsingSubId(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getAnrCount();
        }
        Rlog.e(TAG, "getAnrCount iccPbkIntMgr isnull for Subscription:" + subId);
        return 0;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getEmailCount() throws RemoteException {
        return getEmailCountUsingSubId(getDefaultSubscription());
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getEmailCountUsingSubId(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getEmailCount();
        }
        Rlog.e(TAG, "getEmailCount iccPbkIntMgr isnull for Subscription:" + subId);
        return 0;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getSpareAnrCount() throws RemoteException {
        return getSpareAnrCountUsingSubId(getDefaultSubscription());
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getSpareAnrCountUsingSubId(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getSpareAnrCount();
        }
        Rlog.e(TAG, "getSpareAnrCount iccPbkIntMgr isnull for Subscription:" + subId);
        return 0;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getSpareEmailCount() throws RemoteException {
        return getSpareEmailCountUsingSubId(getDefaultSubscription());
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getSpareEmailCountUsingSubId(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getSpareEmailCount();
        }
        Rlog.e(TAG, "getSpareEmailCount iccPbkIntMgr isnull for Subscription:" + subId);
        return 0;
    }

    private IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager(int subId) {
        try {
            return this.mPhone[SubscriptionController.getInstance().getPhoneId(subId)].getIccPhoneBookInterfaceManager();
        } catch (NullPointerException e) {
            Rlog.e(TAG, "Exception is :" + e.toString() + " For subscription :" + subId);
            e.printStackTrace();
            return null;
        } catch (ArrayIndexOutOfBoundsException e2) {
            Rlog.e(TAG, "Exception is :" + e2.toString() + " For subscription :" + subId);
            e2.printStackTrace();
            return null;
        }
    }

    private int getDefaultSubscription() {
        return PhoneFactory.getDefaultSubscription();
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int updateAdnRecordsWithContentValuesInEfBySearchWithErrorUsingSubId(int efid, ContentValues values, String pin2, int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateAdnRecordsWithContentValuesInEfBySearchWithError(efid, values, pin2);
        }
        Rlog.e(TAG, "updateAdnRecordsWithContentValuesInEfBySearchWithErrorUsingSubId iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int updateAdnRecordsInEfByIndexWithErrorUsingSubId(int efid, String newTag, String newPhoneNumber, String[] newAnrs, String[] newEmails, int index, String pin2, int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateAdnRecordsInEfByIndexWithError(efid, newTag, newPhoneNumber, newAnrs, newEmails, index, pin2);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfByIndexWithErrorUsingSubId iccPbkIntMgr is null for Subscription:" + subId);
        return 0;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public boolean get3gTransferTo2gFlag(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.get3gTransferTo2gFlag();
        }
        Rlog.e(TAG, "updateUsimPBRecordsInEfByIndexWithError iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int[] getQmiPbmCapabilities(int subId) {
        return null;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public List<AdnRecord> getAdnRecordsFromQmiPbm(int subId) {
        return null;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int updateQmiPbmRecord(int subId, int index, String name, String number, String[] emails, String[] anrs) throws RemoteException {
        return 0;
    }
}
