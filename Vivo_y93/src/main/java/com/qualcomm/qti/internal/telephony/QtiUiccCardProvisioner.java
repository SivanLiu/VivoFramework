package com.qualcomm.qti.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.qualcomm.qcrilhook.IQcRilHook;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;

public class QtiUiccCardProvisioner extends Handler {
    private static final String ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED = "org.codeaurora.intent.action.ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED";
    private static final boolean DBG = true;
    private static final int EVENT_GET_ICCID_DONE = 4;
    private static final int EVENT_ICC_CHANGED = 1;
    private static final int EVENT_OEM_HOOK_SERVICE_READY = 3;
    private static final int EVENT_UNSOL_MANUAL_PROVISION_STATUS_CHANGED = 2;
    private static final String EXTRA_NEW_PROVISION_STATE = "newProvisionState";
    private static final int GENERIC_FAILURE = -1;
    private static final int INVALID_INPUT = -2;
    private static final String LOG_TAG = "QtiUiccCardProvisioner";
    private static final int REQUEST_IN_PROGRESS = -3;
    private static final int SIM_BLOCK_MODE_FORBID_SIM_DUAL = 3;
    private static final int SIM_BLOCK_MODE_FORBID_SIM_ONE = 1;
    private static final int SIM_BLOCK_MODE_FORBID_SIM_TWO = 2;
    private static final int SIM_BLOCK_MODE_NO_FORBID = 0;
    private static final int SUCCESS = 0;
    private static final boolean VDBG = false;
    private static final int mNumPhones = TelephonyManager.getDefault().getPhoneCount();
    private static AtomicBoolean mRequestInProgress = new AtomicBoolean(false);
    private static UiccController mUiccController = null;
    private static QtiUiccCardProvisioner sInstance;
    private static Object sManualProvLock = new Object();
    private CardState[] mCardState;
    private Context mContext;
    private boolean[] mIsIccIdBootUpQuery = new boolean[mNumPhones];
    private boolean[] mIsIccIdQueryPending = new boolean[mNumPhones];
    private RegistrantList mManualProvisionChangedRegistrants = new RegistrantList();
    private UiccProvisionStatus[] mOldProvisionStatus;
    private UiccProvisionStatus[] mProvisionStatus;
    private QtiRilInterface mQtiRilInterface;
    private int mSimBlockMode;
    private ContentObserver mSimBlockModeContentObserver;
    private String[] mSimIccId;

    public static class UiccProvisionStatus {
        public static final int CARD_NOT_PRESENT = -2;
        public static final int INVALID_STATE = -1;
        public static final int NOT_PROVISIONED = 0;
        public static final int PROVISIONED = 1;
        private int currentState = -1;
        private int userPreference = -1;

        UiccProvisionStatus() {
        }

        boolean equals(UiccProvisionStatus provisionStatus) {
            if (provisionStatus.getUserPreference() == getUserPreference() && provisionStatus.getCurrentState() == getCurrentState()) {
                return QtiUiccCardProvisioner.DBG;
            }
            return false;
        }

        int getUserPreference() {
            return this.userPreference;
        }

        void setUserPreference(int pref) {
            this.userPreference = pref;
        }

        int getCurrentState() {
            return this.currentState;
        }

        void setCurrentState(int state) {
            this.currentState = state;
        }

        public String toString() {
            return "User pref " + this.userPreference + " Current pref " + this.currentState;
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public static QtiUiccCardProvisioner make(Context context, Looper looper) {
        if (sInstance == null) {
            sInstance = new QtiUiccCardProvisioner(context, looper);
        } else {
            Log.wtf(LOG_TAG, "QtiUiccCardProvisioner.make() should be called once");
        }
        return sInstance;
    }

    public void dispose() {
        logd(" disposing... ");
        mUiccController.unregisterForIccChanged(this);
        mUiccController = null;
        this.mQtiRilInterface.unRegisterForServiceReadyEvent(this);
        this.mQtiRilInterface.unRegisterForUnsol(this);
        this.mQtiRilInterface = null;
    }

    public static QtiUiccCardProvisioner getInstance() {
        if (sInstance == null) {
            Log.e(LOG_TAG, "QtiUiccCardProvisioner.getInstance called before make");
        }
        return sInstance;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private QtiUiccCardProvisioner(Context context, Looper looper) {
        super(looper);
        logd(" Invoking constructor, no of phones = " + mNumPhones);
        this.mContext = context;
        this.mProvisionStatus = new UiccProvisionStatus[mNumPhones];
        this.mOldProvisionStatus = new UiccProvisionStatus[mNumPhones];
        this.mSimIccId = new String[mNumPhones];
        this.mCardState = new CardState[mNumPhones];
        for (int index = 0; index < mNumPhones; index++) {
            this.mSimIccId[index] = null;
            this.mProvisionStatus[index] = new UiccProvisionStatus();
            this.mCardState[index] = CardState.CARDSTATE_ABSENT;
            this.mIsIccIdQueryPending[index] = false;
            this.mIsIccIdBootUpQuery[index] = DBG;
            this.mOldProvisionStatus[index] = new UiccProvisionStatus();
        }
        mUiccController = UiccController.getInstance();
        mUiccController.registerForIccChanged(this, 1, null);
        this.mQtiRilInterface = QtiRilInterface.getInstance(context);
        this.mQtiRilInterface.registerForServiceReadyEvent(this, 3, null);
        this.mQtiRilInterface.registerForUnsol(this, 2, null);
        this.mSimBlockMode = getSimBlockMode();
        registerSimBlockModeChange();
    }

    public void registerForManualProvisionChanged(Handler handler, int what, Object obj) {
        Registrant r = new Registrant(handler, what, obj);
        synchronized (this.mManualProvisionChangedRegistrants) {
            this.mManualProvisionChangedRegistrants.add(r);
            r.notifyRegistrant();
        }
    }

    public void unregisterForManualProvisionChanged(Handler handler) {
        synchronized (this.mManualProvisionChangedRegistrants) {
            this.mManualProvisionChangedRegistrants.remove(handler);
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case 1:
                ar = msg.obj;
                if (ar == null || ar.result == null) {
                    loge("Error: Invalid card index EVENT_ICC_CHANGED ");
                    return;
                } else {
                    updateIccAvailability(((Integer) ar.result).intValue());
                    return;
                }
            case 2:
                ar = (AsyncResult) msg.obj;
                if (ar == null || ar.result == null) {
                    loge("Error: empty result, UNSOL_MANUAL_PROVISION_STATUS_CHANGED");
                    return;
                } else {
                    handleUnsolManualProvisionEvent((Message) ar.result);
                    return;
                }
            case 3:
                ar = (AsyncResult) msg.obj;
                if (ar == null || ar.result == null) {
                    loge("Error: empty result, EVENT_OEM_HOOK_SERVICE_READY");
                    return;
                } else if (((Boolean) ar.result).booleanValue()) {
                    queryAllUiccProvisionInfo();
                    return;
                } else {
                    return;
                }
            case 4:
                ar = (AsyncResult) msg.obj;
                String iccId = null;
                int phoneId = -1;
                if (ar != null) {
                    phoneId = ((Integer) ar.userObj).intValue();
                    if (ar.result != null) {
                        byte[] data = ar.result;
                        iccId = IccUtils.bchToString(data, 0, data.length);
                    } else {
                        logd("Exception in GET iccId[" + phoneId + "] " + ar.exception);
                    }
                }
                if (phoneId >= 0 && phoneId < mNumPhones) {
                    this.mIsIccIdQueryPending[phoneId] = false;
                    if (!TextUtils.isEmpty(iccId)) {
                        logi("SIM_IO add subInfo record, iccId[" + phoneId + "] = " + iccId);
                        QtiSubscriptionInfoUpdater.getInstance().addSubInfoRecord(phoneId, iccId);
                        this.mSimIccId[phoneId] = iccId;
                        if (this.mSimIccId[phoneId] != null && isAllCardProvisionInfoReceived()) {
                            int[] subIds = QtiSubscriptionController.getInstance().getSubId(phoneId);
                            if (!(subIds == null || subIds.length == 0 || !QtiSubscriptionController.getInstance().isActiveSubId(subIds[0]))) {
                                QtiSubscriptionController.getInstance().updateUserPreferences();
                            }
                        }
                        if (this.mOldProvisionStatus != null && (this.mOldProvisionStatus[phoneId].equals(this.mProvisionStatus[phoneId]) ^ 1) != 0) {
                            logd(" broadcasting ProvisionInfo, phoneId = " + phoneId);
                            broadcastManualProvisionStatusChanged(phoneId, getCurrentProvisioningStatus(phoneId));
                            this.mOldProvisionStatus[phoneId] = this.mProvisionStatus[phoneId];
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            default:
                loge("Error: hit default case " + msg.what);
                return;
        }
    }

    private void handleUnsolManualProvisionEvent(Message msg) {
        if (msg == null || msg.obj == null) {
            loge("Null data received in handleUnsolManualProvisionEvent");
            return;
        }
        ByteBuffer payload = ByteBuffer.wrap((byte[]) msg.obj);
        payload.order(ByteOrder.nativeOrder());
        int rspId = payload.getInt();
        int slotId = msg.arg1;
        if (isValidSlotId(slotId) && rspId == IQcRilHook.QCRILHOOK_UNSOL_UICC_PROVISION_STATUS_CHANGED) {
            logi(" Unsol: rspId " + rspId + " slotId " + msg.arg1);
            queryUiccProvisionInfo(slotId, false);
            int dataSlotId = SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultDataSubscriptionId());
            if (slotId == dataSlotId && getCurrentProvisioningStatus(dataSlotId) == 1) {
                logd("Set dds after SSR");
                QtiRadioCapabilityController.getInstance().setDdsIfRequired(false);
            }
        }
    }

    private void queryAllUiccProvisionInfo() {
        int index = 0;
        while (index < mNumPhones) {
            logd(" query  provision info, card state[" + index + "] = " + this.mCardState[index]);
            if (this.mCardState[index] == CardState.CARDSTATE_PRESENT && !this.mIsIccIdQueryPending[index]) {
                queryUiccProvisionInfo(index, DBG);
            }
            index++;
        }
    }

    public String getUiccIccId(int slotId) {
        return this.mSimIccId[slotId];
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private void queryUiccProvisionInfo(int phoneId, boolean useSimIORequest) {
        boolean z = false;
        if (this.mQtiRilInterface.isServiceReady() && (isValidSlotId(phoneId) ^ 1) == 0) {
            UiccProvisionStatus oldStatus = this.mProvisionStatus[phoneId];
            UiccProvisionStatus subStatus = this.mQtiRilInterface.getUiccProvisionPreference(phoneId);
            if (!(subStatus.getCurrentState() == -1 || subStatus.getUserPreference() == -1)) {
                synchronized (sManualProvLock) {
                    this.mProvisionStatus[phoneId] = subStatus;
                }
            }
            String iccId = this.mQtiRilInterface.getUiccIccId(phoneId);
            if (phoneId == 0 && virtualSIMFlagIsTrue()) {
                logd("queryUiccProvisionInfo sim1 is virtualSim!!! prop");
                iccId = "89860000000000000001";
            }
            if (this.mSimIccId[phoneId] == null) {
                logd(" queryUiccProvisionInfo: useSimIORequest=  " + useSimIORequest);
                if (!useSimIORequest || this.mIsIccIdBootUpQuery[phoneId]) {
                    logd(" queryUiccProvisionInfo: getUiccIccId ");
                    if (this.mIsIccIdBootUpQuery[phoneId]) {
                        this.mIsIccIdBootUpQuery[phoneId] = false;
                    }
                    if (iccId != null) {
                        logi("OEM add subInfo record, iccId[" + phoneId + "] = " + iccId);
                        QtiSubscriptionInfoUpdater.getInstance().addSubInfoRecord(phoneId, iccId);
                        this.mSimIccId[phoneId] = iccId;
                    }
                } else {
                    loadIccId(phoneId);
                }
            }
            logd(" queryUiccProvisionInfo, iccId[" + phoneId + "] = " + this.mSimIccId[phoneId] + " " + this.mProvisionStatus[phoneId]);
            if (!oldStatus.equals(this.mProvisionStatus[phoneId])) {
                if (this.mSimIccId[phoneId] != null && isAllCardProvisionInfoReceived()) {
                    int[] subIds = QtiSubscriptionController.getInstance().getSubId(phoneId);
                    if (!(subIds == null || subIds.length == 0 || !QtiSubscriptionController.getInstance().isActiveSubId(subIds[0]))) {
                        QtiSubscriptionController.getInstance().updateUserPreferences();
                    }
                }
                if (useSimIORequest && this.mSimIccId[phoneId] == null) {
                    z = DBG;
                }
                if (!z) {
                    logd(" broadcasting ProvisionInfo, phoneId = " + phoneId);
                    broadcastManualProvisionStatusChanged(phoneId, getCurrentProvisioningStatus(phoneId));
                    this.mOldProvisionStatus[phoneId] = this.mProvisionStatus[phoneId];
                    broadcastSimModeChanged(phoneId);
                }
            }
            this.mSimBlockMode = getSimBlockMode();
            if (this.mProvisionStatus[phoneId].getCurrentState() == 1) {
                if (phoneId == 0 && (this.mSimBlockMode == 3 || this.mSimBlockMode == 1)) {
                    deactivateUiccCard(phoneId);
                } else if (phoneId == 1 && (this.mSimBlockMode == 3 || this.mSimBlockMode == 2)) {
                    deactivateUiccCard(phoneId);
                }
            }
            return;
        }
        logi("Oem hook service is not ready yet " + phoneId);
    }

    private void loadIccId(int phoneId) {
        UiccCard uiccCard = mUiccController.getUiccCard(phoneId);
        if (uiccCard != null) {
            UiccCardApplication validApp = null;
            int numApps = uiccCard.getNumApplications();
            for (int i = 0; i < numApps; i++) {
                UiccCardApplication app = uiccCard.getApplicationIndex(i);
                if (app != null && app.getType() != AppType.APPTYPE_UNKNOWN) {
                    validApp = app;
                    break;
                }
            }
            if (validApp != null) {
                IccFileHandler fileHandler = validApp.getIccFileHandler();
                if (fileHandler != null) {
                    this.mIsIccIdQueryPending[phoneId] = DBG;
                    fileHandler.loadEFTransparent(12258, obtainMessage(4, Integer.valueOf(phoneId)));
                }
            }
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private boolean virtualSIMFlagIsTrue() {
        if (SystemProperties.getInt("sys.vivo.factory.virtualsim", 9) == 1) {
            return DBG;
        }
        return false;
    }

    private void updateIccAvailability(int slotId) {
        if (isValidSlotId(slotId)) {
            CardState newState = CardState.CARDSTATE_ABSENT;
            UiccCard newCard = mUiccController.getUiccCard(slotId);
            if (newCard != null) {
                newState = newCard.getCardState();
                logd("updateIccAvailability, card state[" + slotId + "] = " + newState);
                this.mCardState[slotId] = newState;
                int currentState = getCurrentProvisioningStatus(slotId);
                if (this.mCardState[slotId] == CardState.CARDSTATE_PRESENT && ((this.mSimIccId[slotId] == null || currentState == -1 || currentState == -2) && !this.mIsIccIdQueryPending[slotId])) {
                    this.mProvisionStatus[slotId].setUserPreference(-1);
                    this.mProvisionStatus[slotId].setCurrentState(-1);
                    queryUiccProvisionInfo(slotId, DBG);
                } else if (this.mCardState[slotId] == CardState.CARDSTATE_ABSENT || this.mCardState[slotId] == CardState.CARDSTATE_ERROR) {
                    synchronized (sManualProvLock) {
                        this.mProvisionStatus[slotId].setUserPreference(-2);
                        this.mProvisionStatus[slotId].setCurrentState(-2);
                        this.mSimIccId[slotId] = null;
                        this.mManualProvisionChangedRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(slotId), null));
                    }
                }
                return;
            }
            logd("updateIccAvailability, uicc card null, ignore " + slotId);
            return;
        }
        loge("Invalid slot Index!!! " + slotId);
    }

    private void broadcastManualProvisionStatusChanged(int phoneId, int newProvisionState) {
        Intent intent = new Intent(ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED);
        intent.putExtra("phone", phoneId);
        intent.putExtra(EXTRA_NEW_PROVISION_STATE, newProvisionState);
        this.mContext.sendBroadcast(intent);
        this.mManualProvisionChangedRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(phoneId), null));
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private void broadcastSimModeChanged(int slotId) {
        Intent intent = new Intent("vivo.intent.action.ACTION_SIM_MODE_CHANGED");
        intent.addFlags(16777216);
        boolean isSub0Active = getCurrentProvisioningStatus(0) == 1 ? DBG : false;
        boolean isSub1Active = getCurrentProvisioningStatus(1) == 1 ? DBG : false;
        int mode = 0;
        if (isSub0Active && (isSub1Active ^ 1) != 0) {
            mode = 1;
        } else if (!isSub0Active && isSub1Active) {
            mode = 2;
        } else if (isSub0Active && isSub1Active) {
            mode = 3;
        }
        intent.putExtra("mode", mode);
        intent.putExtra("slot", slotId);
        this.mContext.sendBroadcast(intent);
    }

    private int getCurrentProvisioningStatus(int slotId) {
        int currentState;
        synchronized (sManualProvLock) {
            currentState = this.mProvisionStatus[slotId].getCurrentState();
        }
        return currentState;
    }

    public int getCurrentUiccCardProvisioningStatus(int slotId) {
        if (mNumPhones == 1 && isValidSlotId(slotId)) {
            return 1;
        }
        if (canProcessRequest(slotId)) {
            return getCurrentProvisioningStatus(slotId);
        }
        return -1;
    }

    public int getUiccCardProvisioningUserPreference(int slotId) {
        if (mNumPhones == 1 && isValidSlotId(slotId)) {
            return 1;
        }
        if (!canProcessRequest(slotId)) {
            return -1;
        }
        int userPref;
        synchronized (sManualProvLock) {
            userPref = this.mProvisionStatus[slotId].getUserPreference();
        }
        return userPref;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public int activateUiccCard(int slotId) {
        logd(" activateUiccCard: phoneId = " + slotId);
        if (this.mQtiRilInterface.isServiceReady()) {
            enforceModifyPhoneState("activateUiccCard");
            int activateStatus = 0;
            if (!canProcessRequest(slotId)) {
                activateStatus = -2;
            } else if (getCurrentProvisioningStatus(slotId) == 1) {
                logd(" Uicc card in slot[" + slotId + "] already activated ");
            } else if (isFlexMapInProgress() || !mRequestInProgress.compareAndSet(false, DBG)) {
                activateStatus = REQUEST_IN_PROGRESS;
            } else {
                boolean retVal = this.mQtiRilInterface.setUiccProvisionPreference(1, slotId);
                if (retVal) {
                    synchronized (sManualProvLock) {
                        this.mProvisionStatus[slotId].setCurrentState(1);
                    }
                    broadcastSimModeChanged(slotId);
                } else {
                    activateStatus = -1;
                }
                logi(" activation result[" + slotId + "] = " + retVal);
                mRequestInProgress.set(false);
            }
            return activateStatus;
        }
        logi("Oem hook service is not ready yet " + slotId);
        return -1;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public int deactivateUiccCard(int slotId) {
        logd(" deactivateUiccCard: phoneId = " + slotId);
        if (this.mQtiRilInterface.isServiceReady()) {
            enforceModifyPhoneState("deactivateUiccCard");
            int deactivateState = 0;
            if (!canProcessRequest(slotId)) {
                deactivateState = -2;
            } else if (getCurrentProvisioningStatus(slotId) == 0) {
                logd(" Uicc card in slot[" + slotId + "] already in deactive state ");
            } else if (isFlexMapInProgress() || !mRequestInProgress.compareAndSet(false, DBG)) {
                deactivateState = REQUEST_IN_PROGRESS;
            } else {
                boolean retVal = this.mQtiRilInterface.setUiccProvisionPreference(0, slotId);
                if (retVal) {
                    synchronized (sManualProvLock) {
                        this.mProvisionStatus[slotId].setCurrentState(0);
                    }
                    broadcastSimModeChanged(slotId);
                } else {
                    deactivateState = -1;
                }
                logi(" deactivation result[" + slotId + "] = " + retVal);
                mRequestInProgress.set(false);
            }
            return deactivateState;
        }
        logi("Oem hook service is not ready yet " + slotId);
        return -1;
    }

    private void enforceModifyPhoneState(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", message);
    }

    private boolean canProcessRequest(int slotId) {
        if (mNumPhones > 1 && isValidSlotId(slotId)) {
            return DBG;
        }
        loge("Request can't be processed, slotId " + slotId + " numPhones " + mNumPhones);
        return false;
    }

    private boolean isValidSlotId(int slotId) {
        if (slotId < 0 || slotId >= mNumPhones) {
            return false;
        }
        return DBG;
    }

    public boolean isFlexMapInProgress() {
        QtiRadioCapabilityController rcController = QtiRadioCapabilityController.getInstance();
        if (rcController == null) {
            return false;
        }
        boolean retVal = rcController.isSetNWModeInProgress();
        logd("isFlexMapInProgress: = " + retVal);
        return retVal;
    }

    public boolean isAnyProvisionRequestInProgress() {
        return mRequestInProgress.get();
    }

    public boolean isAllCardProvisionInfoReceived() {
        int index = 0;
        while (index < mNumPhones) {
            int provPref = getCurrentProvisioningStatus(index);
            if (provPref == -1 || (this.mSimIccId[index] != null && provPref == -2)) {
                logd("isAllCardProvisionInfoReceived, prov pref[" + index + "] = " + provPref);
                return false;
            }
            index++;
        }
        return DBG;
    }

    private void registerSimBlockModeChange() {
        this.mSimBlockModeContentObserver = new ContentObserver(this) {
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                QtiUiccCardProvisioner.this.updateSimBlockMode();
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("ct_network_sim_block"), false, this.mSimBlockModeContentObserver);
    }

    private void updateSimBlockMode() {
        logd("updateSimBlockMode pre simBlockMode = " + this.mSimBlockMode);
        int mode = getSimBlockMode();
        if (this.mCardState[0] == CardState.CARDSTATE_PRESENT) {
            if ((mode == 0 && this.mSimBlockMode == 3) || ((mode == 0 && this.mSimBlockMode == 1) || ((mode == 2 && this.mSimBlockMode == 3) || (mode == 2 && this.mSimBlockMode == 1)))) {
                activateUiccCard(0);
            } else if ((mode == 1 && this.mSimBlockMode == 0) || ((mode == 1 && this.mSimBlockMode == 2) || ((mode == 3 && this.mSimBlockMode == 0) || (mode == 3 && this.mSimBlockMode == 2)))) {
                deactivateUiccCard(0);
            }
        }
        if (this.mCardState[1] == CardState.CARDSTATE_PRESENT) {
            if ((mode == 0 && this.mSimBlockMode == 3) || ((mode == 0 && this.mSimBlockMode == 2) || ((mode == 1 && this.mSimBlockMode == 3) || (mode == 1 && this.mSimBlockMode == 2)))) {
                activateUiccCard(1);
            } else if ((mode == 2 && this.mSimBlockMode == 0) || ((mode == 2 && this.mSimBlockMode == 1) || ((mode == 3 && this.mSimBlockMode == 0) || (mode == 3 && this.mSimBlockMode == 1)))) {
                deactivateUiccCard(1);
            }
        }
        this.mSimBlockMode = mode;
    }

    private int getSimBlockMode() {
        int mode = Secure.getInt(this.mContext.getContentResolver(), "ct_network_sim_block", 0);
        logd("getSimBlockMode mode = " + mode);
        return mode;
    }

    private void logd(String string) {
        Rlog.d(LOG_TAG, string);
    }

    private void logi(String string) {
        Rlog.i(LOG_TAG, string);
    }

    private void loge(String string) {
        Rlog.e(LOG_TAG, string);
    }
}
