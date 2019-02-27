package com.qualcomm.qti.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.ContentValues;
import android.os.Message;
import android.text.TextUtils;
import com.android.internal.telephony.IccPhoneBookInterfaceManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.qualcomm.qti.internal.telephony.uicccontact.QtiSimPhoneBookAdnRecordCache;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class QtiIccPhoneBookInterfaceManager extends IccPhoneBookInterfaceManager {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "QtiIccPhoneBookInterfaceManager";
    private QtiSimPhoneBookAdnRecordCache mSimPbAdnCache;

    public QtiIccPhoneBookInterfaceManager(Phone phone) {
        super(phone);
        if (isSimPhoneBookEnabled() && this.mSimPbAdnCache == null) {
            this.mSimPbAdnCache = new QtiSimPhoneBookAdnRecordCache(phone.getContext(), phone.getPhoneId(), phone.mCi);
        }
    }

    private boolean isSimPhoneBookEnabled() {
        if (this.mPhone.getContext().getResources().getBoolean(17957013)) {
            return DBG;
        }
        return false;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void dispose() {
        if (this.mRecords != null) {
            this.mRecords.clear();
        }
        if (this.mAdnCache != null) {
            this.mAdnCache.reset();
            this.mAdnCache = null;
        }
        if (this.mSimPbAdnCache != null) {
            this.mSimPbAdnCache.reset();
            this.mSimPbAdnCache = null;
        }
        this.mCurrentApp = null;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    @Deprecated
    public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) {
        if (updateAdnRecordsInEfBySearchWithError(efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2) == 1) {
            return DBG;
        }
        return false;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    @Deprecated
    public synchronized int updateAdnRecordsInEfBySearchWithError(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        }
        logd("updateAdnRecordsInEfBySearch: efid=0x" + Integer.toHexString(efid).toUpperCase());
        efid = updateEfForIccType(efid);
        synchronized (this.mLock) {
            checkThread();
            this.mSuccess = false;
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(3, status);
            AdnRecord oldAdn = new AdnRecord(oldTag, oldPhoneNumber);
            AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber);
            if (this.mAdnCache != null) {
                this.mAdnCache.updateAdnBySearchWithError(efid, oldAdn, newAdn, pin2, response);
                waitForResult(status);
            } else {
                loge("Failure while trying to update by search due to uninitialised adncache");
            }
        }
        if (this.mErrorCause == 1) {
            logd("updateAdnRecordsWithContentValuesInEfBySearch success index is " + -1);
            return -1;
        }
        return this.mErrorCause;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public List<AdnRecord> getAdnRecordsInEf(int efid) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        }
        efid = updateEfForIccType(efid);
        logd("getAdnRecordsInEF: efid=0x" + Integer.toHexString(efid).toUpperCase());
        synchronized (this.mLock) {
            checkThread();
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(2, status);
            if (isSimPhoneBookEnabled() && (efid == 20272 || efid == 28474)) {
                if (this.mSimPbAdnCache != null) {
                    this.mSimPbAdnCache.requestLoadAllAdnLike(response);
                    this.mRecords = this.mSimPbAdnCache.requestLoadAllAdnLike();
                    if (this.mRecords == null) {
                        logd("Cannot load ADN records");
                    } else {
                        logd("Load ADN records done");
                    }
                } else {
                    loge("Failure while trying to load from SIM due to uninit  sim pb adncache");
                }
            } else if (this.mAdnCache != null) {
                this.mAdnCache.requestLoadAllAdnLike(efid, this.mAdnCache.extensionEfForEf(efid), response);
                waitForResult(status);
            } else {
                loge("Failure while trying to load from SIM due to uninitialised adncache");
            }
        }
        return this.mRecords;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    @Deprecated
    public boolean updateAdnRecordsWithContentValuesInEfBySearch(int efid, ContentValues values, String pin2) {
        if (updateAdnRecordsWithContentValuesInEfBySearchWithError(efid, values, pin2) == 1) {
            return DBG;
        }
        return false;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public synchronized int updateAdnRecordsWithContentValuesInEfBySearchWithError(int efid, ContentValues values, String pin2) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        }
        String oldTag = values.getAsString("tag");
        String newTag = values.getAsString("newTag");
        String oldPhoneNumber = values.getAsString("number");
        String newPhoneNumber = values.getAsString("newNumber");
        String oldEmail = values.getAsString("emails");
        String newEmail = values.getAsString("newEmails");
        String oldAnr = values.getAsString("anrs");
        String newAnr = values.getAsString("newAnrs");
        String[] oldEmailArray = TextUtils.isEmpty(oldEmail) ? null : getStringArray(oldEmail);
        String[] newEmailArray = TextUtils.isEmpty(newEmail) ? null : getStringArray(newEmail);
        String[] oldAnrArray = TextUtils.isEmpty(oldAnr) ? null : getStringArray(oldAnr);
        String[] newAnrArray = TextUtils.isEmpty(newAnr) ? null : getStringArray(newAnr);
        efid = updateEfForIccType(efid);
        logd("updateAdnRecordsWithContentValuesInEfBySearchWithError: efid=" + efid);
        synchronized (this.mLock) {
            checkThread();
            this.mSuccess = false;
            AtomicBoolean atomicBoolean = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(3, atomicBoolean);
            AdnRecord oldAdn = new AdnRecord(oldTag, oldPhoneNumber, oldEmailArray, oldAnrArray);
            AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber, newEmailArray, newAnrArray);
            if (isSimPhoneBookEnabled() && (efid == 20272 || efid == 28474)) {
                if (this.mSimPbAdnCache != null) {
                    this.mSimPbAdnCache.updateSimPbAdnBySearchWithError(oldAdn, newAdn, response);
                    logd("updateAdnRecordsInEfByIndex: waitForResult");
                    waitForResult(atomicBoolean);
                } else {
                    loge("Failure while trying to update by search due to uninit sim pb adncache");
                }
            } else if (this.mAdnCache != null) {
                this.mAdnCache.updateAdnBySearchWithError(efid, oldAdn, newAdn, pin2, response);
                waitForResult(atomicBoolean);
            } else {
                loge("Failure while trying to update by search due to uninitialised adncache");
            }
        }
        return this.mErrorCause;
    }

    public int[] getAdnRecordsCapacity() {
        int[] capacity = new int[10];
        if (isSimPhoneBookEnabled()) {
            if (this.mSimPbAdnCache != null) {
                capacity[0] = this.mSimPbAdnCache.getAdnCount();
                capacity[1] = this.mSimPbAdnCache.getUsedAdnCount();
                capacity[2] = this.mSimPbAdnCache.getEmailCount();
                capacity[3] = this.mSimPbAdnCache.getUsedEmailCount();
                capacity[4] = this.mSimPbAdnCache.getAnrCount();
                capacity[5] = this.mSimPbAdnCache.getUsedAnrCount();
                capacity[6] = this.mSimPbAdnCache.getMaxNameLen();
                capacity[7] = this.mSimPbAdnCache.getMaxNumberLen();
                capacity[8] = this.mSimPbAdnCache.getMaxEmailLen();
                capacity[9] = this.mSimPbAdnCache.getMaxAnrLen();
            } else {
                loge("mAdnCache is NULL when getAdnRecordsCapacity.");
            }
        }
        logd("getAdnRecordsCapacity: max adn=" + capacity[0] + ", used adn=" + capacity[1] + ", max email=" + capacity[2] + ", used email=" + capacity[3] + ", max anr=" + capacity[4] + ", used anr=" + capacity[5] + ", max name length =" + capacity[6] + ", max number length =" + capacity[7] + ", max email length =" + capacity[8] + ", max anr length =" + capacity[9]);
        return capacity;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public synchronized int updateAdnRecordsInEfByIndexWithError(int efid, String newTag, String newPhoneNumber, String[] anrNumbers, String[] emails, int index, String pin2) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        }
        logd("updateAdnRecordsInEfByIndex: efid=" + efid + " Index=" + index);
        synchronized (this.mLock) {
            checkThread();
            this.mSuccess = false;
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(3, status);
            AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber, emails, anrNumbers);
            efid = updateEfForIccType(efid);
            if (isSimPhoneBookEnabled() && (efid == 20272 || efid == 28474)) {
                if (this.mSimPbAdnCache != null) {
                    this.mSimPbAdnCache.updateSimPbAdnByIndex(newAdn, index, response);
                    logd("updateAdnRecordsInEfByIndex: waitForResult");
                    waitForResult(status);
                } else {
                    loge("Failure while trying to update by index due to uninitialised adncache");
                }
            } else if (this.mAdnCache != null) {
                this.mAdnCache.updateUsimAdnByIndex(efid, newAdn, index, pin2, response);
                waitForResult(status);
            } else {
                logd("Failure while trying to update by index due to uninitialised adncache");
            }
        }
        return this.mErrorCause;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getAdnCount() {
        int adnCount = 0;
        if (isSimPhoneBookEnabled()) {
            if (this.mSimPbAdnCache != null) {
                adnCount = this.mSimPbAdnCache.getAdnCount();
            } else {
                loge("mAdnCache is NULL when getAdnCount.");
            }
        } else if (this.mAdnCache != null) {
            adnCount = (this.mPhone.getCurrentUiccAppType() == AppType.APPTYPE_USIM || this.mPhone.getCurrentUiccAppType() == AppType.APPTYPE_CSIM) ? this.mAdnCache.getUsimAdnCount() : this.mAdnCache.getAdnCount();
        } else {
            loge("mAdnCache is NULL when getAdnCount.");
        }
        logd("getAdnCount: max adn=" + adnCount);
        return adnCount;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getAnrCount() {
        int anrCount = 0;
        if (isSimPhoneBookEnabled()) {
            if (this.mSimPbAdnCache != null) {
                anrCount = this.mSimPbAdnCache.getAnrCount();
            } else {
                loge("mAdnCache is NULL when getAnrCount.");
            }
        }
        logd("getAnrCount: max anr=" + anrCount);
        return anrCount;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getEmailCount() {
        int emailCount = 0;
        if (isSimPhoneBookEnabled()) {
            if (this.mSimPbAdnCache != null) {
                emailCount = this.mSimPbAdnCache.getEmailCount();
            } else {
                loge("mAdnCache is NULL when getEmailCount.");
            }
        }
        logd("getEmailCount: max email=" + emailCount);
        return emailCount;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getSpareAnrCount() {
        int spareAnrCount = 0;
        if (isSimPhoneBookEnabled()) {
            if (this.mSimPbAdnCache != null) {
                spareAnrCount = this.mSimPbAdnCache.getAnrCount() - this.mSimPbAdnCache.getUsedAnrCount();
            } else {
                loge("mAdnCache is NULL when getSpareAnrCount.");
            }
        }
        logd("getSpareAnrCount: spare anr=" + spareAnrCount);
        return spareAnrCount;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getSpareEmailCount() {
        int spareEmailCount = 0;
        if (isSimPhoneBookEnabled()) {
            if (this.mSimPbAdnCache != null) {
                spareEmailCount = this.mSimPbAdnCache.getEmailCount() - this.mSimPbAdnCache.getUsedEmailCount();
            } else {
                loge("mAdnCache is NULL when getSpareEmailCount.");
            }
        }
        logd("getSpareEmailCount: spare email=" + spareEmailCount);
        return spareEmailCount;
    }
}
