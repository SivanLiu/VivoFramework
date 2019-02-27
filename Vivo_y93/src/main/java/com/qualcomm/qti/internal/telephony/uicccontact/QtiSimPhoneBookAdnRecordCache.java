package com.qualcomm.qti.internal.telephony.uicccontact;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import android.util.SparseArray;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.qualcomm.qti.internal.telephony.QtiRilInterface;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class QtiSimPhoneBookAdnRecordCache extends Handler {
    private static final boolean DBG = true;
    static final int EVENT_INIT_ADN_DONE = 1;
    static final int EVENT_LOAD_ADN_RECORD_DONE = 3;
    static final int EVENT_LOAD_ALL_ADN_LIKE_DONE = 4;
    static final int EVENT_QUERY_ADN_RECORD_DONE = 2;
    static final int EVENT_SIM_REFRESH = 6;
    static final int EVENT_UPDATE_ADN_RECORD_DONE = 5;
    private static final int EVENT_VIVO_ADD_WAITER = 32;
    private static final int EVENT_VIVO_BASE = 30;
    private static final int EVENT_VIVO_NOTIFY_WAITERS = 31;
    private static final String LOG_TAG = "QtiSimPhoneBookAdnRecordCache";
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static int MAX_PHB_NAME_LENGTH = 60;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static int MAX_PHB_NUMBER_LENGTH = 40;
    SparseArray<int[]> extRecList = new SparseArray();
    private int mAddNumCount = 0;
    private int mAdnCount = 0;
    ArrayList<Message> mAdnLoadingWaiters = new ArrayList();
    Message mAdnUpdatingWaiter = null;
    protected final CommandsInterface mCi;
    protected Context mContext;
    private int mEmailCount = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private boolean mGetAdnRecordsError = false;
    private Object mLock = new Object();
    private int mMaxAnrLen = 0;
    private int mMaxEmailLen = 0;
    private int mMaxNameLen = 0;
    private int mMaxNumberLen = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private boolean mNewAnrNull = false;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private boolean mNewEmailNull = false;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private boolean mOldAnrNull = false;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private boolean mOldEmailNull = false;
    protected int mPhoneId;
    private QtiRilInterface mQtiRilInterface;
    private int mRecCount = 0;
    private boolean mRefreshAdnCache = false;
    private ArrayList<AdnRecord> mSimPbRecords;
    private int mValidAddNumCount = 0;
    private int mValidAdnCount = 0;
    private int mValidEmailCount = 0;
    private final BroadcastReceiver sReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SIM_STATE_CHANGED")) {
                int phoneId = intent.getIntExtra("phone", -1);
                String simStatus = intent.getStringExtra("ss");
                if ("ABSENT".equals(simStatus) && QtiSimPhoneBookAdnRecordCache.this.mPhoneId == phoneId) {
                    QtiSimPhoneBookAdnRecordCache.this.log("ACTION_SIM_STATE_CHANGED intent received simStatus: " + simStatus + "phoneId: " + phoneId);
                    QtiSimPhoneBookAdnRecordCache.this.invalidateAdnCache();
                }
            }
        }
    };

    public QtiSimPhoneBookAdnRecordCache(Context context, int phoneId, CommandsInterface ci) {
        this.mCi = ci;
        this.mSimPbRecords = new ArrayList();
        this.mPhoneId = phoneId;
        this.mContext = context;
        this.mQtiRilInterface = QtiRilInterface.getInstance(context);
        this.mQtiRilInterface.registerForAdnInitDone(this, 1, null);
        this.mCi.registerForIccRefresh(this, 6, null);
        context.registerReceiver(this.sReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
    }

    public void reset() {
        this.mAdnLoadingWaiters.clear();
        clearUpdatingWriter();
        this.mSimPbRecords.clear();
        this.mRecCount = 0;
        this.mRefreshAdnCache = false;
    }

    private void clearUpdatingWriter() {
        sendErrorResponse(this.mAdnUpdatingWaiter, "QtiSimPhoneBookAdnRecordCache reset");
        this.mAdnUpdatingWaiter = null;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private void sendErrorResponse(Message response, String errString) {
        sendErrorResponse(response, errString, 2);
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private void sendErrorResponse(Message response, String errString, int ril_errno) {
        CommandException e = CommandException.fromRilErrno(ril_errno);
        if (response != null) {
            log(errString);
            AsyncResult.forMessage(response).exception = e;
            response.sendToTarget();
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private void notifyAndClearWaiters() {
        if (this.mAdnLoadingWaiters != null) {
            int s = this.mAdnLoadingWaiters.size();
            for (int i = 0; i < s; i++) {
                Message response = (Message) this.mAdnLoadingWaiters.get(i);
                if (!(response == null || response.getTarget() == null)) {
                    Message msg = response.getTarget().obtainMessage(response.what);
                    msg.obj = response.obj;
                    AsyncResult.forMessage(response).result = this.mSimPbRecords;
                    try {
                        response.sendToTarget();
                    } catch (IllegalStateException e) {
                        log("notifyAndClearWaiters Exception : IllegalStateException" + e.toString());
                        sendErrorResponse(msg, "notifyAndClearWaiters Exception : IllegalStateException");
                    }
                }
            }
            this.mAdnLoadingWaiters.clear();
        }
    }

    public void queryAdnRecord() {
        this.mRecCount = 0;
        this.mAdnCount = 0;
        this.mValidAdnCount = 0;
        this.mEmailCount = 0;
        this.mAddNumCount = 0;
        log("start to queryAdnRecord");
        this.mQtiRilInterface.registerForAdnRecordsInfo(this, 3, null);
        this.mQtiRilInterface.getAdnRecord(obtainMessage(2), this.mPhoneId);
        try {
            this.mLock.wait();
        } catch (InterruptedException e) {
            Rlog.e(LOG_TAG, "Interrupted Exception in queryAdnRecord");
        }
        this.mQtiRilInterface.unregisterForAdnRecordsInfo(this);
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    @Deprecated
    public void requestLoadAllAdnLike(Message response) {
        if (this.mQtiRilInterface.isServiceReady()) {
            Message.obtain(this, EVENT_VIVO_ADD_WAITER, response).sendToTarget();
            synchronized (this.mLock) {
                if (this.mRefreshAdnCache) {
                    this.mRefreshAdnCache = false;
                    refreshAdnCache();
                } else if (this.mSimPbRecords.isEmpty()) {
                    queryAdnRecord();
                } else {
                    log("ADN cache has already filled in");
                    sendEmptyMessage(EVENT_VIVO_NOTIFY_WAITERS);
                }
            }
            return;
        }
        log("Oem hook service is not ready yet ");
        sendErrorResponse(response, "Oem hook service is not ready yet");
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    @Deprecated
    public void updateSimPbAdnBySearch(AdnRecord oldAdn, AdnRecord newAdn, Message response) {
        ArrayList<AdnRecord> oldAdnList = this.mSimPbRecords;
        if (this.mQtiRilInterface.isServiceReady()) {
            synchronized (this.mLock) {
                if (this.mRefreshAdnCache) {
                    this.mRefreshAdnCache = false;
                    refreshAdnCache();
                } else if (this.mSimPbRecords.isEmpty()) {
                    queryAdnRecord();
                } else {
                    log("ADN cache has already filled in");
                }
            }
            if (oldAdnList == null) {
                sendErrorResponse(response, "Sim PhoneBook Adn list not exist");
                return;
            }
            int index = -1;
            int count = 1;
            if (!oldAdn.isEmpty() || (newAdn.isEmpty() ^ 1) == 0) {
                Iterator<AdnRecord> it = oldAdnList.iterator();
                while (it.hasNext()) {
                    if (oldAdn.isEqual((AdnRecord) it.next())) {
                        index = count;
                        break;
                    }
                    count++;
                }
            } else {
                index = 0;
            }
            if (index == -1) {
                sendErrorResponse(response, "Sim PhoneBook Adn record don't exist for " + oldAdn);
                return;
            } else if (index == 0 && this.mValidAdnCount == this.mAdnCount) {
                sendErrorResponse(response, "Sim PhoneBook Adn record is full");
                return;
            } else {
                int recordIndex = index == 0 ? 0 : ((AdnRecord) oldAdnList.get(index - 1)).getRecordNumber();
                QtiSimPhoneBookAdnRecord updateAdn = new QtiSimPhoneBookAdnRecord();
                updateAdn.mRecordIndex = recordIndex;
                updateAdn.mAlphaTag = newAdn.getAlphaTag();
                updateAdn.mNumber = newAdn.getNumber();
                if (newAdn.getEmails() != null) {
                    updateAdn.mEmails = newAdn.getEmails();
                    updateAdn.mEmailCount = updateAdn.mEmails.length;
                }
                if (newAdn.getAdditionalNumbers() != null) {
                    updateAdn.mAdNumbers = newAdn.getAdditionalNumbers();
                    updateAdn.mAdNumCount = updateAdn.mAdNumbers.length;
                }
                if (this.mAdnUpdatingWaiter != null) {
                    sendErrorResponse(response, "Have pending update for Sim PhoneBook Adn");
                    return;
                }
                this.mAdnUpdatingWaiter = response;
                this.mQtiRilInterface.updateAdnRecord(updateAdn, obtainMessage(5, index, 0, newAdn), this.mPhoneId);
                return;
            }
        }
        log("Oem hook service is not ready yet ");
        sendErrorResponse(response, "Oem hook service is not ready yet");
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public void updateSimPbAdnBySearchWithError(AdnRecord oldAdn, AdnRecord newAdn, Message response) {
        boolean z = DBG;
        log("updateSimPbAdnBySearch");
        int index = -1;
        ArrayList<AdnRecord> oldAdnList = this.mSimPbRecords;
        if (oldAdnList == null) {
            sendErrorResponse(response, "Sim PhoneBook Adn list not exist", 1011);
        } else if (newAdn.getAlphaTag().length() > MAX_PHB_NAME_LENGTH) {
            sendErrorResponse(response, "the input length of alphaTag is too long: " + newAdn.getAlphaTag(), 1002);
        } else {
            int num_length = newAdn.getNumber().length();
            if (newAdn.getNumber().indexOf(43) != -1) {
                num_length--;
            }
            if (num_length > MAX_PHB_NUMBER_LENGTH) {
                sendErrorResponse(response, "the input length of phoneNumber is too long: " + newAdn.getNumber(), 1001);
                return;
            }
            int count = 1;
            if (!oldAdn.isEmpty() || (newAdn.isEmpty() ^ 1) == 0) {
                Iterator<AdnRecord> it = oldAdnList.iterator();
                while (it.hasNext()) {
                    if (oldAdn.isEqual((AdnRecord) it.next())) {
                        index = count;
                        break;
                    }
                    count++;
                }
            } else {
                index = 0;
            }
            if (index == -1) {
                sendErrorResponse(response, "Sim PhoneBook Adn record don't exist for " + oldAdn);
            } else if (index == 0 && this.mValidAdnCount == this.mAdnCount) {
                sendErrorResponse(response, "Sim PhoneBook Adn record is full", 1003);
            } else {
                this.mOldEmailNull = oldAdn.getEmails() == null ? DBG : false;
                this.mNewEmailNull = newAdn.getEmails() == null ? DBG : false;
                if (this.mValidEmailCount == this.mEmailCount && (this.mNewEmailNull ^ 1) != 0 && this.mOldEmailNull) {
                    sendErrorResponse(response, "Sim PhoneBook email size is full", 1005);
                    return;
                }
                this.mOldAnrNull = oldAdn.getAdditionalNumbers() == null ? DBG : false;
                if (newAdn.getAdditionalNumbers() != null) {
                    z = false;
                }
                this.mNewAnrNull = z;
                if (this.mValidAddNumCount == this.mAddNumCount && (this.mNewAnrNull ^ 1) != 0 && this.mOldAnrNull) {
                    sendErrorResponse(response, "Sim PhoneBook anr size is full", 1007);
                    return;
                }
                int recordIndex = index == 0 ? 0 : ((AdnRecord) oldAdnList.get(index - 1)).getRecordNumber();
                QtiSimPhoneBookAdnRecord updateAdn = new QtiSimPhoneBookAdnRecord();
                updateAdn.mRecordIndex = recordIndex;
                updateAdn.mAlphaTag = newAdn.getAlphaTag();
                updateAdn.mNumber = newAdn.getNumber();
                if (newAdn.getEmails() != null) {
                    updateAdn.mEmails = newAdn.getEmails();
                    updateAdn.mEmailCount = updateAdn.mEmails.length;
                }
                if (newAdn.getAdditionalNumbers() != null) {
                    updateAdn.mAdNumbers = newAdn.getAdditionalNumbers();
                    updateAdn.mAdNumCount = updateAdn.mAdNumbers.length;
                }
                if (this.mAdnUpdatingWaiter != null) {
                    sendErrorResponse(response, "Have pending update for Sim PhoneBook Adn");
                    return;
                }
                this.mAdnUpdatingWaiter = response;
                this.mQtiRilInterface.updateAdnRecord(updateAdn, obtainMessage(5, index, 0, newAdn), this.mPhoneId);
            }
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public void updateSimPbAdnByIndex(AdnRecord newAdn, int index, Message response) {
        boolean z = DBG;
        log("updateSimPbAdnByIndex index:" + index);
        ArrayList<AdnRecord> oldAdnList = this.mSimPbRecords;
        if (oldAdnList == null) {
            sendErrorResponse(response, "Sim PhoneBook Adn list not exist", 1011);
            return;
        }
        int count = 1;
        int recordIndex = index;
        Iterator<AdnRecord> it = oldAdnList.iterator();
        while (it.hasNext()) {
            if (index == ((AdnRecord) it.next()).getRecordNumber()) {
                index = count;
                break;
            }
            count++;
        }
        AdnRecord oldAdn = (AdnRecord) oldAdnList.get(index - 1);
        if (newAdn.getAlphaTag().length() > MAX_PHB_NAME_LENGTH) {
            sendErrorResponse(response, "the input length of alphaTag is too long: " + newAdn.getAlphaTag(), 1002);
            return;
        }
        int num_length = newAdn.getNumber().length();
        if (newAdn.getNumber().indexOf(43) != -1) {
            num_length--;
        }
        if (num_length > MAX_PHB_NUMBER_LENGTH) {
            sendErrorResponse(response, "the input length of phoneNumber is too long: " + newAdn.getNumber(), 1001);
            return;
        }
        this.mOldEmailNull = oldAdn.getEmails() == null ? DBG : false;
        this.mNewEmailNull = newAdn.getEmails() == null ? DBG : false;
        if (this.mValidEmailCount == this.mEmailCount && (this.mNewEmailNull ^ 1) != 0 && this.mOldEmailNull) {
            sendErrorResponse(response, "Sim PhoneBook email size is full", 1005);
            return;
        }
        this.mOldAnrNull = oldAdn.getAdditionalNumbers() == null ? DBG : false;
        if (newAdn.getAdditionalNumbers() != null) {
            z = false;
        }
        this.mNewAnrNull = z;
        if (this.mValidAddNumCount == this.mAddNumCount && (this.mNewAnrNull ^ 1) != 0 && this.mOldAnrNull) {
            sendErrorResponse(response, "Sim PhoneBook anr size is full", 1007);
            return;
        }
        QtiSimPhoneBookAdnRecord updateAdn = new QtiSimPhoneBookAdnRecord();
        updateAdn.mRecordIndex = recordIndex;
        updateAdn.mAlphaTag = newAdn.getAlphaTag();
        updateAdn.mNumber = newAdn.getNumber();
        if (newAdn.getEmails() != null) {
            updateAdn.mEmails = newAdn.getEmails();
            updateAdn.mEmailCount = updateAdn.mEmails.length;
        }
        if (newAdn.getAdditionalNumbers() != null) {
            updateAdn.mAdNumbers = newAdn.getAdditionalNumbers();
            updateAdn.mAdNumCount = updateAdn.mAdNumbers.length;
        }
        if (this.mAdnUpdatingWaiter != null) {
            sendErrorResponse(response, "Have pending update for Sim PhoneBook Adn");
            return;
        }
        this.mAdnUpdatingWaiter = response;
        this.mQtiRilInterface.updateAdnRecord(updateAdn, obtainMessage(5, index, 0, newAdn), this.mPhoneId);
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void handleMessage(Message msg) {
        Message response;
        switch (msg.what) {
            case EVENT_VIVO_NOTIFY_WAITERS /*31*/:
                notifyAndClearWaiters();
                return;
            case EVENT_VIVO_ADD_WAITER /*32*/:
                if (msg.obj instanceof Message) {
                    response = msg.obj;
                    if (this.mAdnLoadingWaiters != null) {
                        this.mAdnLoadingWaiters.add(response);
                    }
                }
                return;
            default:
                AsyncResult ar = msg.obj;
                switch (msg.what) {
                    case 1:
                        ar = msg.obj;
                        log("Initialized ADN done");
                        if (ar.exception != null) {
                            log("Init ADN done Exception: " + ar.exception);
                            break;
                        }
                        invalidateAdnCache();
                        break;
                    case 2:
                        log("Querying ADN record done");
                        if (ar.exception == null) {
                            this.mAdnCount = ((int[]) ar.result)[0];
                            this.mValidAdnCount = ((int[]) ar.result)[1];
                            this.mEmailCount = ((int[]) ar.result)[2];
                            this.mValidEmailCount = ((int[]) ar.result)[3];
                            this.mAddNumCount = ((int[]) ar.result)[4];
                            this.mValidAddNumCount = ((int[]) ar.result)[5];
                            this.mMaxNameLen = ((int[]) ar.result)[6];
                            this.mMaxNumberLen = ((int[]) ar.result)[7];
                            this.mMaxEmailLen = ((int[]) ar.result)[8];
                            this.mMaxAnrLen = ((int[]) ar.result)[9];
                            log("Max ADN count is: " + this.mAdnCount + ", Valid ADN count is: " + this.mValidAdnCount + ", Email count is: " + this.mEmailCount + ", Valid email count is: " + this.mValidEmailCount + ", Add number count is: " + this.mAddNumCount + ", Valid add number count is: " + this.mValidAddNumCount + ", Max name length is: " + this.mMaxNameLen + ", Max number length is: " + this.mMaxNumberLen + ", Max email length is: " + this.mMaxEmailLen + ", Valid anr length is: " + this.mMaxAnrLen);
                            if (this.mValidAdnCount == 0 || this.mRecCount == this.mValidAdnCount) {
                                sendMessage(obtainMessage(4));
                                break;
                            }
                        }
                        synchronized (this.mLock) {
                            Rlog.e(LOG_TAG, "Query adn record failed " + ar.exception);
                            this.mGetAdnRecordsError = DBG;
                            this.mLock.notify();
                        }
                        for (Message response2 : this.mAdnLoadingWaiters) {
                            sendErrorResponse(response2, "Query adn record failed" + ar.exception);
                        }
                        this.mAdnLoadingWaiters.clear();
                        break;
                    case 3:
                        log("Loading ADN record done");
                        if (ar.exception == null) {
                            QtiSimPhoneBookAdnRecord[] AdnRecordsGroup = ar.result;
                            for (int i = 0; i < AdnRecordsGroup.length; i++) {
                                if (AdnRecordsGroup[i] != null) {
                                    int instanceId = AdnRecordsGroup[i].getInstanceId();
                                    if (instanceId == -1 || instanceId != this.mPhoneId) {
                                        log("instanceId = " + instanceId + ",mPhoneId = " + this.mPhoneId);
                                    } else {
                                        this.mSimPbRecords.add(new AdnRecord(0, AdnRecordsGroup[i].getRecordIndex(), AdnRecordsGroup[i].getAlphaTag(), AdnRecordsGroup[i].getNumber(), AdnRecordsGroup[i].getEmails(), AdnRecordsGroup[i].getAdNumbers()));
                                        this.mRecCount++;
                                    }
                                }
                            }
                            if (this.mRecCount == this.mValidAdnCount) {
                                sendMessage(obtainMessage(4));
                                break;
                            }
                        }
                        break;
                    case 4:
                        log("Loading all ADN records done");
                        synchronized (this.mLock) {
                            this.mLock.notify();
                        }
                        notifyAndClearWaiters();
                        break;
                    case 5:
                        log("Update ADN record done");
                        Throwable e = null;
                        AdnRecord adn = null;
                        if (ar.exception == null) {
                            int index = msg.arg1;
                            adn = ar.userObj;
                            int recordIndex = ((int[]) ar.result)[0];
                            int adnRecordIndex;
                            if (index == 0) {
                                log("Record number for added ADN is " + recordIndex);
                                adn.setRecordNumber(recordIndex);
                                this.mSimPbRecords.add(adn);
                                this.mValidAdnCount++;
                                if (!this.mNewEmailNull) {
                                    this.mValidEmailCount++;
                                }
                                if (!this.mNewAnrNull) {
                                    this.mValidAddNumCount++;
                                }
                            } else if (adn.isEmpty()) {
                                adnRecordIndex = ((AdnRecord) this.mSimPbRecords.get(index - 1)).getRecordNumber();
                                log("Record number for deleted ADN is " + adnRecordIndex);
                                adn.setRecordNumber(recordIndex);
                                if (recordIndex == adnRecordIndex) {
                                    this.mSimPbRecords.remove(index - 1);
                                    this.mValidAdnCount--;
                                    if (!this.mOldEmailNull) {
                                        this.mValidEmailCount--;
                                    }
                                    if (!this.mOldAnrNull) {
                                        this.mValidAddNumCount--;
                                    }
                                } else {
                                    e = new RuntimeException("The index for deleted ADN record did not match");
                                }
                            } else {
                                adnRecordIndex = ((AdnRecord) this.mSimPbRecords.get(index - 1)).getRecordNumber();
                                log("Record number for changed ADN is " + adnRecordIndex);
                                if (recordIndex == adnRecordIndex) {
                                    adn.setRecordNumber(recordIndex);
                                    this.mSimPbRecords.set(index - 1, adn);
                                    if (!this.mOldEmailNull && this.mNewEmailNull) {
                                        this.mValidEmailCount--;
                                    } else if (this.mOldEmailNull && (this.mNewEmailNull ^ 1) != 0) {
                                        this.mValidEmailCount++;
                                    }
                                    if (!this.mOldAnrNull && this.mNewAnrNull) {
                                        this.mValidAddNumCount--;
                                    } else if (this.mOldAnrNull && (this.mNewAnrNull ^ 1) != 0) {
                                        this.mValidAddNumCount++;
                                    }
                                } else {
                                    e = new RuntimeException("The index for changed ADN record did not match");
                                }
                            }
                        } else {
                            e = new RuntimeException("Update adn record failed", ar.exception);
                        }
                        if (this.mAdnUpdatingWaiter != null) {
                            AsyncResult.forMessage(this.mAdnUpdatingWaiter, adn, e);
                            this.mAdnUpdatingWaiter.sendToTarget();
                            this.mAdnUpdatingWaiter = null;
                            break;
                        }
                        break;
                    case 6:
                        ar = msg.obj;
                        log("SIM REFRESH occurred");
                        if (ar.exception != null) {
                            log("SIM refresh Exception: " + ar.exception);
                            break;
                        }
                        IccRefreshResponse refreshRsp = ar.result;
                        if (refreshRsp != null) {
                            if (refreshRsp.refreshResult == 0 || refreshRsp.refreshResult == 1) {
                                invalidateAdnCache();
                                break;
                            }
                        }
                        log("IccRefreshResponse received is null");
                        break;
                }
                return;
        }
    }

    public int getAdnCount() {
        return this.mAdnCount;
    }

    public int getUsedAdnCount() {
        return this.mValidAdnCount;
    }

    public int getEmailCount() {
        return this.mEmailCount;
    }

    public int getUsedEmailCount() {
        return this.mValidEmailCount;
    }

    public int getAnrCount() {
        return this.mAddNumCount;
    }

    public int getUsedAnrCount() {
        return this.mValidAddNumCount;
    }

    public int getMaxNameLen() {
        return this.mMaxNameLen;
    }

    public int getMaxNumberLen() {
        return this.mMaxNumberLen;
    }

    public int getMaxEmailLen() {
        return this.mMaxEmailLen;
    }

    public int getMaxAnrLen() {
        return this.mMaxAnrLen;
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    public void invalidateAdnCache() {
        log("invalidateAdnCache");
        this.mRefreshAdnCache = DBG;
    }

    private void refreshAdnCache() {
        log("refreshAdnCache");
        this.mSimPbRecords.clear();
        queryAdnRecord();
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public List<AdnRecord> requestLoadAllAdnLike() {
        synchronized (this.mLock) {
            if (this.mRefreshAdnCache) {
                this.mRefreshAdnCache = false;
                refreshAdnCache();
            } else if (this.mSimPbRecords.isEmpty()) {
                queryAdnRecord();
            } else {
                log("ADN cache has already filled in");
            }
            if (this.mGetAdnRecordsError) {
                this.mGetAdnRecordsError = false;
                return null;
            }
            log("queryAdnRecord done! return mSimPbRecords");
            List list = this.mSimPbRecords;
            return list;
        }
    }
}
