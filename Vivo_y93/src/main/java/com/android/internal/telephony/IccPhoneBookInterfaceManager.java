package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.ContentValues;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class IccPhoneBookInterfaceManager {
    /* renamed from: -com-android-internal-telephony-CommandException$ErrorSwitchesValues */
    private static final /* synthetic */ int[] f21xa0f04c1a = null;
    protected static final boolean ALLOW_SIM_OP_IN_UI_THREAD = false;
    protected static final boolean DBG = true;
    protected static final int EVENT_GET_SIZE_DONE = 1;
    protected static final int EVENT_LOAD_DONE = 2;
    protected static final int EVENT_UPDATE_DONE = 3;
    static final String LOG_TAG = "IccPhoneBookIM";
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected static final Object mGetSizeLock = new Object();
    private static final HandlerThread mHandlerThread = new HandlerThread("IccPbHandlerLoader");
    protected AdnRecordCache mAdnCache;
    protected final IccPbHandler mBaseHandler;
    @VivoHook(hookType = VivoHookType.CHANGE_ACCESS)
    protected UiccCardApplication mCurrentApp = null;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int mErrorCause;
    private boolean mIs3gCard = false;
    protected final Object mLock = new Object();
    protected Phone mPhone;
    protected int[] mRecordSize;
    protected List<AdnRecord> mRecords;
    protected boolean mSuccess;

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    protected class IccPbHandler extends Handler {
        public IccPbHandler(Looper looper) {
            super(looper);
        }

        @VivoHook(hookType = VivoHookType.CHANGE_CODE)
        public void handleMessage(Message msg) {
            boolean z = true;
            AsyncResult ar;
            Object obj;
            switch (msg.what) {
                case 1:
                    ar = msg.obj;
                    obj = IccPhoneBookInterfaceManager.mGetSizeLock;
                    synchronized (obj) {
                        if (ar.exception == null) {
                            if (IccPhoneBookInterfaceManager.this.mRecordSize[2] != 0) {
                                int[] ret = ar.result;
                                int[] iArr = IccPhoneBookInterfaceManager.this.mRecordSize;
                                iArr[1] = iArr[1] + ret[1];
                                iArr = IccPhoneBookInterfaceManager.this.mRecordSize;
                                iArr[2] = iArr[2] + ret[2];
                                if (IccPhoneBookInterfaceManager.this.mRecordSize[0] != ret[0]) {
                                    IccPhoneBookInterfaceManager.this.loge("GET_RECORD_SIZE record size is not equal");
                                }
                            } else {
                                IccPhoneBookInterfaceManager.this.mRecordSize = (int[]) ar.result;
                            }
                            IccPhoneBookInterfaceManager.this.logd("GET_RECORD_SIZE Size " + IccPhoneBookInterfaceManager.this.mRecordSize[0] + " total " + IccPhoneBookInterfaceManager.this.mRecordSize[1] + " #record " + IccPhoneBookInterfaceManager.this.mRecordSize[2]);
                        }
                        notifyPendingForGetSize(ar);
                        break;
                    }
                case 2:
                    ar = (AsyncResult) msg.obj;
                    obj = IccPhoneBookInterfaceManager.this.mLock;
                    synchronized (obj) {
                        if (ar.exception == null) {
                            IccPhoneBookInterfaceManager.this.logd("Load ADN records done");
                            IccPhoneBookInterfaceManager.this.mRecords = (List) ar.result;
                        } else {
                            IccPhoneBookInterfaceManager.this.logd("Cannot load ADN records");
                            IccPhoneBookInterfaceManager.this.mRecords = null;
                        }
                        notifyPending(ar);
                        break;
                    }
                case 3:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        IccPhoneBookInterfaceManager.this.logd("exception of EVENT_UPDATE_DONE is" + ar.exception);
                    }
                    synchronized (IccPhoneBookInterfaceManager.this.mLock) {
                        IccPhoneBookInterfaceManager iccPhoneBookInterfaceManager = IccPhoneBookInterfaceManager.this;
                        if (ar.exception != null) {
                            z = false;
                        }
                        iccPhoneBookInterfaceManager.mSuccess = z;
                        if (IccPhoneBookInterfaceManager.this.mSuccess) {
                            AdnRecord newRecord = ar.result;
                            if (newRecord != null) {
                                IccPhoneBookInterfaceManager.this.mErrorCause = newRecord.getRecordNumber();
                            } else {
                                IccPhoneBookInterfaceManager.this.mErrorCause = 1;
                            }
                        } else if (ar.exception instanceof CommandException) {
                            IccPhoneBookInterfaceManager.this.mErrorCause = IccPhoneBookInterfaceManager.this.getErrorCauseFromException((CommandException) ar.exception);
                        } else {
                            IccPhoneBookInterfaceManager.this.mErrorCause = -10;
                        }
                        IccPhoneBookInterfaceManager.this.logd("update done result: " + IccPhoneBookInterfaceManager.this.mErrorCause);
                        notifyPending(ar);
                    }
                    return;
                default:
                    return;
            }
        }

        @VivoHook(hookType = VivoHookType.CHANGE_CODE)
        private void notifyPending(AsyncResult ar) {
            if (ar.userObj != null && (ar.userObj instanceof AtomicBoolean)) {
                ar.userObj.set(true);
            }
            IccPhoneBookInterfaceManager.this.mLock.notifyAll();
            IccPhoneBookInterfaceManager.this.logd("notifyPending: notify end");
        }

        @VivoHook(hookType = VivoHookType.NEW_METHOD)
        private void notifyPendingForGetSize(AsyncResult ar) {
            if (ar.userObj != null && (ar.userObj instanceof AtomicBoolean)) {
                ar.userObj.set(true);
            }
            IccPhoneBookInterfaceManager.mGetSizeLock.notifyAll();
        }
    }

    /* renamed from: -getcom-android-internal-telephony-CommandException$ErrorSwitchesValues */
    private static /* synthetic */ int[] m21x9944abe() {
        if (f21xa0f04c1a != null) {
            return f21xa0f04c1a;
        }
        int[] iArr = new int[Error.values().length];
        try {
            iArr[Error.ABORTED.ordinal()] = 13;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Error.ADDITIONAL_NUMBER_STRING_TOO_LONG.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Error.ADN_LIST_NOT_EXIST.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Error.ANR_SIZE_LIMIT.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Error.DEVICE_IN_USE.ordinal()] = 14;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Error.DIAL_MODIFIED_TO_DIAL.ordinal()] = 15;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Error.DIAL_MODIFIED_TO_SS.ordinal()] = 16;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Error.DIAL_MODIFIED_TO_USSD.ordinal()] = 17;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Error.DIAL_STRING_TOO_LONG.ordinal()] = 4;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Error.EMAIL_NAME_TOOLONG.ordinal()] = 5;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Error.EMAIL_SIZE_LIMIT.ordinal()] = 6;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Error.EMPTY_RECORD.ordinal()] = 18;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Error.ENCODING_ERR.ordinal()] = 19;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Error.FDN_CHECK_FAILURE.ordinal()] = 20;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Error.GENERIC_FAILURE.ordinal()] = 7;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Error.ILLEGAL_SIM_OR_ME.ordinal()] = 21;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Error.INTERNAL_ERR.ordinal()] = 22;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Error.INVALID_ARGUMENTS.ordinal()] = 23;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Error.INVALID_CALL_ID.ordinal()] = 24;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Error.INVALID_MODEM_STATE.ordinal()] = 25;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Error.INVALID_RESPONSE.ordinal()] = 26;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Error.INVALID_SIM_STATE.ordinal()] = 27;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Error.INVALID_SMSC_ADDRESS.ordinal()] = 28;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Error.INVALID_SMS_FORMAT.ordinal()] = 29;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Error.INVALID_STATE.ordinal()] = 30;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Error.LCE_NOT_SUPPORTED.ordinal()] = 31;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Error.MISSING_RESOURCE.ordinal()] = 32;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Error.MODEM_ERR.ordinal()] = 33;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Error.MODE_NOT_SUPPORTED.ordinal()] = 34;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Error.NETWORK_ERR.ordinal()] = 35;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Error.NETWORK_NOT_READY.ordinal()] = 36;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Error.NETWORK_REJECT.ordinal()] = 37;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Error.NOT_PROVISIONED.ordinal()] = 38;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Error.NOT_READY.ordinal()] = 8;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Error.NO_MEMORY.ordinal()] = 39;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Error.NO_NETWORK_FOUND.ordinal()] = 40;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Error.NO_RESOURCES.ordinal()] = 41;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Error.NO_SMS_TO_ACK.ordinal()] = 42;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Error.NO_SUBSCRIPTION.ordinal()] = 43;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Error.NO_SUCH_ELEMENT.ordinal()] = 44;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Error.NO_SUCH_ENTRY.ordinal()] = 45;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Error.OEM_ERROR_1.ordinal()] = 46;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Error.OEM_ERROR_10.ordinal()] = 47;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Error.OEM_ERROR_11.ordinal()] = 48;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Error.OEM_ERROR_12.ordinal()] = 49;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Error.OEM_ERROR_13.ordinal()] = 50;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Error.OEM_ERROR_14.ordinal()] = 51;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Error.OEM_ERROR_15.ordinal()] = 52;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Error.OEM_ERROR_16.ordinal()] = 53;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Error.OEM_ERROR_17.ordinal()] = 54;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Error.OEM_ERROR_18.ordinal()] = 55;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Error.OEM_ERROR_19.ordinal()] = 56;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Error.OEM_ERROR_2.ordinal()] = 57;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Error.OEM_ERROR_20.ordinal()] = 58;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Error.OEM_ERROR_21.ordinal()] = 59;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Error.OEM_ERROR_22.ordinal()] = 60;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Error.OEM_ERROR_23.ordinal()] = 61;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Error.OEM_ERROR_24.ordinal()] = 62;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Error.OEM_ERROR_25.ordinal()] = 63;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Error.OEM_ERROR_3.ordinal()] = 64;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Error.OEM_ERROR_4.ordinal()] = 65;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Error.OEM_ERROR_5.ordinal()] = 66;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Error.OEM_ERROR_6.ordinal()] = 67;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Error.OEM_ERROR_7.ordinal()] = 68;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Error.OEM_ERROR_8.ordinal()] = 69;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Error.OEM_ERROR_9.ordinal()] = 70;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Error.OPERATION_NOT_ALLOWED.ordinal()] = 71;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Error.OP_NOT_ALLOWED_BEFORE_REG_NW.ordinal()] = 72;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Error.OP_NOT_ALLOWED_DURING_VOICE_CALL.ordinal()] = 73;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Error.PASSWORD_INCORRECT.ordinal()] = 9;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Error.QMI_PBM_ERROR.ordinal()] = 74;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Error.QMI_PBM_NOT_READY.ordinal()] = 75;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Error.RADIO_NOT_AVAILABLE.ordinal()] = 76;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Error.REQUEST_NOT_SUPPORTED.ordinal()] = 77;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Error.REQUEST_RATE_LIMITED.ordinal()] = 78;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Error.SIM_ABSENT.ordinal()] = 79;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Error.SIM_ALREADY_POWERED_OFF.ordinal()] = 80;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[Error.SIM_ALREADY_POWERED_ON.ordinal()] = 81;
        } catch (NoSuchFieldError e78) {
        }
        try {
            iArr[Error.SIM_BUSY.ordinal()] = 82;
        } catch (NoSuchFieldError e79) {
        }
        try {
            iArr[Error.SIM_DATA_NOT_AVAILABLE.ordinal()] = 83;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[Error.SIM_ERR.ordinal()] = 84;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[Error.SIM_FULL.ordinal()] = 85;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[Error.SIM_MEM_FULL.ordinal()] = 10;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[Error.SIM_PIN2.ordinal()] = 86;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[Error.SIM_PUK2.ordinal()] = 11;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[Error.SIM_SAP_CONNECT_FAILURE.ordinal()] = 87;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[Error.SIM_SAP_CONNECT_OK_CALL_ONGOING.ordinal()] = 88;
        } catch (NoSuchFieldError e87) {
        }
        try {
            iArr[Error.SIM_SAP_MSG_SIZE_TOO_LARGE.ordinal()] = 89;
        } catch (NoSuchFieldError e88) {
        }
        try {
            iArr[Error.SIM_SAP_MSG_SIZE_TOO_SMALL.ordinal()] = 90;
        } catch (NoSuchFieldError e89) {
        }
        try {
            iArr[Error.SMS_FAIL_RETRY.ordinal()] = 91;
        } catch (NoSuchFieldError e90) {
        }
        try {
            iArr[Error.SS_MODIFIED_TO_DIAL.ordinal()] = 92;
        } catch (NoSuchFieldError e91) {
        }
        try {
            iArr[Error.SS_MODIFIED_TO_DIAL_VIDEO.ordinal()] = 93;
        } catch (NoSuchFieldError e92) {
        }
        try {
            iArr[Error.SS_MODIFIED_TO_SS.ordinal()] = 94;
        } catch (NoSuchFieldError e93) {
        }
        try {
            iArr[Error.SS_MODIFIED_TO_USSD.ordinal()] = 95;
        } catch (NoSuchFieldError e94) {
        }
        try {
            iArr[Error.SUBSCRIPTION_NOT_AVAILABLE.ordinal()] = 96;
        } catch (NoSuchFieldError e95) {
        }
        try {
            iArr[Error.SUBSCRIPTION_NOT_SUPPORTED.ordinal()] = 97;
        } catch (NoSuchFieldError e96) {
        }
        try {
            iArr[Error.SYSTEM_ERR.ordinal()] = 98;
        } catch (NoSuchFieldError e97) {
        }
        try {
            iArr[Error.TEXT_STRING_TOO_LONG.ordinal()] = 12;
        } catch (NoSuchFieldError e98) {
        }
        try {
            iArr[Error.USSD_MODIFIED_TO_DIAL.ordinal()] = 99;
        } catch (NoSuchFieldError e99) {
        }
        try {
            iArr[Error.USSD_MODIFIED_TO_SS.ordinal()] = 100;
        } catch (NoSuchFieldError e100) {
        }
        try {
            iArr[Error.USSD_MODIFIED_TO_USSD.ordinal()] = 101;
        } catch (NoSuchFieldError e101) {
        }
        f21xa0f04c1a = iArr;
        return iArr;
    }

    static {
        mHandlerThread.start();
    }

    public IccPhoneBookInterfaceManager(Phone phone) {
        this.mPhone = phone;
        IccRecords r = phone.getIccRecords();
        if (r != null) {
            this.mAdnCache = r.getAdnCache();
        }
        this.mBaseHandler = new IccPbHandler(mHandlerThread.getLooper());
    }

    public void dispose() {
    }

    public void updateIccRecords(IccRecords iccRecords) {
        if (iccRecords != null) {
            this.mAdnCache = iccRecords.getAdnCache();
        } else {
            this.mAdnCache = null;
        }
    }

    protected void logd(String msg) {
        Rlog.d(LOG_TAG, "[IccPbInterfaceManager] " + msg);
    }

    protected void loge(String msg) {
        Rlog.e(LOG_TAG, "[IccPbInterfaceManager] " + msg);
    }

    public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        }
        logd("updateAdnRecordsInEfBySearch: efid=0x" + Integer.toHexString(efid).toUpperCase() + " (" + Rlog.pii(LOG_TAG, oldTag) + "," + Rlog.pii(LOG_TAG, oldPhoneNumber) + ")" + "==>" + " (" + Rlog.pii(LOG_TAG, newTag) + "," + Rlog.pii(LOG_TAG, newPhoneNumber) + ")" + " pin2=" + Rlog.pii(LOG_TAG, pin2));
        efid = updateEfForIccType(efid);
        synchronized (this.mLock) {
            checkThread();
            this.mSuccess = false;
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(3, status);
            AdnRecord oldAdn = new AdnRecord(oldTag, oldPhoneNumber);
            AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber);
            if (this.mAdnCache != null) {
                this.mAdnCache.updateAdnBySearch(efid, oldAdn, newAdn, pin2, response);
                waitForResult(status);
            } else {
                loge("Failure while trying to update by search due to uninitialised adncache");
            }
        }
        return this.mSuccess;
    }

    public boolean updateAdnRecordsWithContentValuesInEfBySearch(int efid, ContentValues values, String pin2) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        }
        String oldTag = values.getAsString(IccProvider.STR_TAG);
        String newTag = values.getAsString(IccProvider.STR_NEW_TAG);
        String oldPhoneNumber = values.getAsString(IccProvider.STR_NUMBER);
        String newPhoneNumber = values.getAsString(IccProvider.STR_NEW_NUMBER);
        String oldEmail = values.getAsString(IccProvider.STR_EMAILS);
        String newEmail = values.getAsString(IccProvider.STR_NEW_EMAILS);
        String oldAnr = values.getAsString(IccProvider.STR_ANRS);
        String newAnr = values.getAsString(IccProvider.STR_NEW_ANRS);
        String[] oldEmailArray = TextUtils.isEmpty(oldEmail) ? null : getStringArray(oldEmail);
        String[] newEmailArray = TextUtils.isEmpty(newEmail) ? null : getStringArray(newEmail);
        String[] oldAnrArray = TextUtils.isEmpty(oldAnr) ? null : getAnrStringArray(oldAnr);
        String[] newAnrArray = TextUtils.isEmpty(newAnr) ? null : getAnrStringArray(newAnr);
        efid = updateEfForIccType(efid);
        logd("updateAdnRecordsWithContentValuesInEfBySearch: efid=" + efid + ", values = " + values + ", pin2=" + pin2);
        synchronized (this.mLock) {
            checkThread();
            this.mSuccess = false;
            AtomicBoolean atomicBoolean = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(3, atomicBoolean);
            AdnRecord oldAdn = new AdnRecord(oldTag, oldPhoneNumber, oldEmailArray, oldAnrArray);
            AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber, newEmailArray, newAnrArray);
            if (this.mAdnCache != null) {
                this.mAdnCache.updateAdnBySearch(efid, oldAdn, newAdn, pin2, response);
                waitForResult(atomicBoolean);
            } else {
                loge("Failure while trying to update by search due to uninitialised adncache");
            }
        }
        return this.mSuccess;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    @Deprecated
    public boolean updateAdnRecordsInEfByIndex(int efid, String newTag, String newPhoneNumber, int index, String pin2) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        }
        logd("updateAdnRecordsInEfByIndex: efid=0x" + Integer.toHexString(efid).toUpperCase() + " Index=" + index);
        synchronized (this.mLock) {
            checkThread();
            this.mSuccess = false;
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(3, status);
            AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber);
            if (this.mAdnCache != null) {
                this.mAdnCache.updateAdnByIndex(efid, newAdn, index, pin2, response);
                waitForResult(status);
            } else {
                loge("Failure while trying to update by index due to uninitialised adncache");
            }
        }
        return this.mSuccess;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public int[] getAdnRecordsSize(int efid) {
        logd("getAdnRecordsSize: efid=" + efid);
        synchronized (mGetSizeLock) {
            checkThread();
            this.mRecordSize = new int[3];
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(1, status);
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                fh.getEFLinearRecordSize(efid, response);
                waitForResultForGetSize(status);
            }
        }
        return this.mRecordSize;
    }

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
            if (this.mAdnCache != null) {
                this.mAdnCache.requestLoadAllAdnLike(efid, this.mAdnCache.extensionEfForEf(efid), response);
                waitForResult(status);
            } else {
                loge("Failure while trying to load from SIM due to uninitialised adncache");
            }
        }
        return this.mRecords;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    protected void checkThread() {
        logd("To check telephony phonebook deadlock patch valid!");
        if (Looper.getMainLooper().equals(Looper.myLooper())) {
            loge("query() called on the main UI thread!");
            throw new IllegalStateException("You cannot call query on this provder from the main UI thread.");
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    protected void waitForResult(AtomicBoolean status) {
        while (!status.get()) {
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                logd("interrupted while trying to update by search");
            }
        }
        logd("waitForResult: wait end");
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected void waitForResultForGetSize(AtomicBoolean status) {
        while (!status.get()) {
            try {
                mGetSizeLock.wait();
            } catch (InterruptedException e) {
                logd("interrupted while trying to update by search");
            }
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_ACCESS)
    protected int updateEfForIccType(int efid) {
        if (efid == 28474 && this.mPhone.getCurrentUiccAppType() == AppType.APPTYPE_USIM) {
            return IccConstants.EF_PBR;
        }
        return efid;
    }

    protected String[] getStringArray(String str) {
        if (str != null) {
            return str.split(",");
        }
        return null;
    }

    protected String[] getAnrStringArray(String str) {
        if (str != null) {
            return str.split(":");
        }
        return null;
    }

    public int[] getAdnRecordsCapacity() {
        logd("getAdnRecordsCapacity");
        return new int[10];
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public synchronized int updateAdnRecordsInEfByIndexWithError(int efid, String newTag, String newPhoneNumber, String[] anrNumbers, String[] emails, int index, String pin2) {
        return -10;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public synchronized int updateAdnRecordsWithContentValuesInEfBySearchWithError(int efid, ContentValues values, String pin2) {
        return -10;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getAdnCount() {
        return 0;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getAnrCount() {
        return 0;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getEmailCount() {
        return 0;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getSpareAnrCount() {
        return 0;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getSpareEmailCount() {
        return 0;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private int getErrorCauseFromException(CommandException e) {
        if (e == null) {
            return 1;
        }
        int ret;
        switch (m21x9944abe()[e.getCommandError().ordinal()]) {
            case 1:
                ret = -6;
                break;
            case 2:
                ret = -11;
                break;
            case 3:
                ret = -14;
                break;
            case 4:
                ret = -1;
                break;
            case 5:
                ret = -13;
                break;
            case 6:
                ret = -12;
                break;
            case 7:
                ret = -10;
                break;
            case 8:
                ret = -4;
                break;
            case 9:
            case 11:
                ret = -5;
                break;
            case 10:
                ret = -3;
                break;
            case 12:
                ret = -2;
                break;
            default:
                ret = 0;
                break;
        }
        return ret;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public boolean get3gTransferTo2gFlag() {
        return false;
    }
}
