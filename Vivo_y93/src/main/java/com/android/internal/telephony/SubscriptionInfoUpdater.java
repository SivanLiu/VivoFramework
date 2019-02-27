package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.ActivityManager;
import android.app.UserSwitchObserver;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.service.euicc.EuiccProfileInfo;
import android.service.euicc.GetEuiccProfileInfoListResult;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UiccAccessRule;
import android.telephony.euicc.EuiccManager;
import android.text.TextUtils;
import com.android.internal.telephony.FtTelephonyAdapterImpl.SimType;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.euicc.EuiccController;
import com.android.internal.telephony.uicc.IccCardProxy;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionInfoUpdater extends Handler {
    public static final String CURR_SUBID = "curr_subid";
    private static final int EVENT_GET_NETWORK_SELECTION_MODE_DONE = 2;
    private static final int EVENT_REFRESH_EMBEDDED_SUBSCRIPTIONS = 9;
    private static final int EVENT_SIM_ABSENT = 4;
    private static final int EVENT_SIM_ABSENT_DEALY = 200;
    private static final int EVENT_SIM_IO_ERROR = 6;
    private static final int EVENT_SIM_LOADED = 3;
    private static final int EVENT_SIM_LOCKED = 5;
    protected static final int EVENT_SIM_LOCKED_QUERY_ICCID_DONE = 1;
    private static final int EVENT_SIM_NOT_READY = 10;
    private static final int EVENT_SIM_RESTRICTED = 8;
    private static final int EVENT_SIM_UNKNOWN = 7;
    private static final String ICCID_STRING_FOR_NO_SIM = "";
    private static final String LOG_TAG = "SubscriptionInfoUpdater";
    private static final int PROJECT_SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    public static final int SIM_CHANGED = -1;
    public static final int SIM_NEW = -2;
    public static final int SIM_NOT_CHANGE = 0;
    public static final int SIM_NOT_INSERT = -99;
    public static final int SIM_REPOSITION = -3;
    public static final int STATUS_NO_SIM_INSERTED = 0;
    public static final int STATUS_SIM1_INSERTED = 1;
    public static final int STATUS_SIM2_INSERTED = 2;
    public static final int STATUS_SIM3_INSERTED = 4;
    public static final int STATUS_SIM4_INSERTED = 8;
    private static Context mContext = null;
    protected static String[] mIccId = new String[PROJECT_SIM_NUM];
    private static int[] mInsertSimState = new int[PROJECT_SIM_NUM];
    private static Phone[] mPhone;
    private CarrierServiceBindHelper mCarrierServiceBindHelper;
    private int mCurrentlyActiveUserId;
    private EuiccManager mEuiccManager;
    private boolean[] mNotifyPinAgain = new boolean[PROJECT_SIM_NUM];
    private IPackageManager mPackageManager;
    private SubscriptionManager mSubscriptionManager = null;
    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private final BroadcastReceiver sReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int i = 0;
            SubscriptionInfoUpdater.this.logd("[Receiver]+");
            String action = intent.getAction();
            SubscriptionInfoUpdater.this.logd("Action: " + action);
            if (!action.equals("android.intent.action.SIM_STATE_CHANGED") && (action.equals(IccCardProxy.ACTION_INTERNAL_SIM_STATE_CHANGED) ^ 1) != 0 && (action.equals("android.intent.action.LOCALE_CHANGED") ^ 1) != 0) {
                return;
            }
            if (action.equals("android.intent.action.LOCALE_CHANGED")) {
                int[] subIdList = SubscriptionInfoUpdater.this.mSubscriptionManager.getActiveSubscriptionIdList();
                int length = subIdList.length;
                while (i < length) {
                    SubscriptionInfoUpdater.this.updateSubName(subIdList[i]);
                    i++;
                }
                return;
            }
            int slotIndex = intent.getIntExtra("phone", -1);
            SubscriptionInfoUpdater.this.logd("slotIndex: " + slotIndex);
            if (SubscriptionManager.isValidSlotIndex(slotIndex)) {
                String simStatus = intent.getStringExtra("ss");
                SubscriptionInfoUpdater.this.logd("simStatus: " + simStatus);
                if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                    if ("ABSENT".equals(simStatus)) {
                        SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(4, slotIndex, -1));
                    } else if ("UNKNOWN".equals(simStatus)) {
                        SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(7, slotIndex, -1));
                    } else if ("CARD_IO_ERROR".equals(simStatus)) {
                        SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(6, slotIndex, -1));
                    } else if ("CARD_RESTRICTED".equals(simStatus)) {
                        SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(8, slotIndex, -1));
                    } else if ("NOT_READY".equals(simStatus)) {
                        SubscriptionInfoUpdater.this.sendEmptyMessage(9);
                    } else {
                        SubscriptionInfoUpdater.this.logd("Ignoring simStatus: " + simStatus);
                    }
                    SubscriptionInfoUpdater.this.mNotifyPinAgain[slotIndex] = false;
                } else if (action.equals(IccCardProxy.ACTION_INTERNAL_SIM_STATE_CHANGED)) {
                    if ("LOCKED".equals(simStatus)) {
                        SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(5, slotIndex, -1, intent.getStringExtra("reason")));
                    } else if ("LOADED".equals(simStatus)) {
                        SubscriptionInfoUpdater.this.mNotifyPinAgain[slotIndex] = false;
                        SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(3, slotIndex, -1));
                    } else {
                        SubscriptionInfoUpdater.this.logd("Ignoring simStatus: " + simStatus);
                    }
                }
                SubscriptionInfoUpdater.this.logd("[Receiver]-");
                return;
            }
            SubscriptionInfoUpdater.this.logd("ACTION_SIM_STATE_CHANGED contains invalid slotIndex: " + slotIndex);
        }
    };

    protected static class QueryIccIdUserObj {
        public String reason;
        public int slotId;

        QueryIccIdUserObj(String reason, int slotId) {
            this.reason = reason;
            this.slotId = slotId;
        }
    }

    public SubscriptionInfoUpdater(Looper looper, Context context, Phone[] phone, CommandsInterface[] ci) {
        super(looper);
        logd("Constructor invoked");
        mContext = context;
        mPhone = phone;
        this.mSubscriptionManager = SubscriptionManager.from(mContext);
        this.mEuiccManager = (EuiccManager) mContext.getSystemService("euicc_service");
        this.mPackageManager = Stub.asInterface(ServiceManager.getService("package"));
        IntentFilter intentFilter = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction(IccCardProxy.ACTION_INTERNAL_SIM_STATE_CHANGED);
        intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            this.mNotifyPinAgain[i] = false;
        }
        mContext.registerReceiver(this.sReceiver, intentFilter);
        this.mCarrierServiceBindHelper = new CarrierServiceBindHelper(mContext);
        initializeCarrierApps();
    }

    private void initializeCarrierApps() {
        this.mCurrentlyActiveUserId = 0;
        try {
            ActivityManager.getService().registerUserSwitchObserver(new UserSwitchObserver() {
                public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
                    SubscriptionInfoUpdater.this.mCurrentlyActiveUserId = newUserId;
                    CarrierAppUtils.disableCarrierAppsUntilPrivileged(SubscriptionInfoUpdater.mContext.getOpPackageName(), SubscriptionInfoUpdater.this.mPackageManager, TelephonyManager.getDefault(), SubscriptionInfoUpdater.mContext.getContentResolver(), SubscriptionInfoUpdater.this.mCurrentlyActiveUserId);
                    if (reply != null) {
                        try {
                            reply.sendResult(null);
                        } catch (RemoteException e) {
                        }
                    }
                }
            }, LOG_TAG);
            this.mCurrentlyActiveUserId = ActivityManager.getService().getCurrentUser().id;
        } catch (RemoteException e) {
            logd("Couldn't get current user ID; guessing it's 0: " + e.getMessage());
        }
        CarrierAppUtils.disableCarrierAppsUntilPrivileged(mContext.getOpPackageName(), this.mPackageManager, TelephonyManager.getDefault(), mContext.getContentResolver(), this.mCurrentlyActiveUserId);
    }

    protected boolean isAllIccIdQueryDone() {
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            if (mIccId[i] == null) {
                logd("Wait for SIM" + (i + 1) + " IccId");
                return false;
            }
        }
        logd("All IccIds query complete");
        return true;
    }

    public void setDisplayNameForNewSub(String newSubName, int subId, int newNameSource) {
        SubscriptionInfo subInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(subId);
        if (subInfo != null) {
            int oldNameSource = subInfo.getNameSource();
            CharSequence oldSubName = subInfo.getDisplayName();
            logd("[setDisplayNameForNewSub] subId = " + subInfo.getSubscriptionId() + ", oldSimName = " + oldSubName + ", oldNameSource = " + oldNameSource + ", newSubName = " + newSubName + ", newNameSource = " + newNameSource);
            if (oldSubName == null || ((oldNameSource == 0 && newSubName != null) || !(oldNameSource != 1 || newSubName == null || (newSubName.equals(oldSubName) ^ 1) == 0))) {
                this.mSubscriptionManager.setDisplayName(newSubName, subInfo.getSubscriptionId(), (long) newNameSource);
                return;
            }
            return;
        }
        logd("SUB" + (subId + 1) + " SubInfo not created yet");
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private boolean virtualSIMFlagIsTrue() {
        if (SystemProperties.getInt("sys.vivo.factory.virtualsim", 9) == 1) {
            return true;
        }
        return false;
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case 1:
                ar = msg.obj;
                QueryIccIdUserObj uObj = ar.userObj;
                int slotId = uObj.slotId;
                logd("handleMessage : <EVENT_SIM_LOCKED_QUERY_ICCID_DONE> SIM" + (slotId + 1));
                if (ar.exception != null) {
                    mIccId[slotId] = ICCID_STRING_FOR_NO_SIM;
                    logd("Query IccId fail: " + ar.exception);
                } else if (ar.result != null) {
                    byte[] data = ar.result;
                    mIccId[slotId] = IccUtils.bchToString(data, 0, data.length);
                } else {
                    logd("Null ar");
                    mIccId[slotId] = ICCID_STRING_FOR_NO_SIM;
                }
                if (slotId == 0 && virtualSIMFlagIsTrue()) {
                    logd("EVENT_QUERY_ICCID_DONE sim1 is virtualSim!!! prop");
                    mIccId[slotId] = "89860000000000000001";
                }
                logd("sIccId[" + slotId + "] = " + mIccId[slotId]);
                if (isAllIccIdQueryDone()) {
                    updateSubscriptionInfoByIccId();
                }
                broadcastSimStateChanged(slotId, "LOCKED", uObj.reason);
                if (!ICCID_STRING_FOR_NO_SIM.equals(mIccId[slotId])) {
                    updateCarrierServices(slotId, "LOCKED");
                    return;
                }
                return;
            case 2:
                ar = (AsyncResult) msg.obj;
                Integer slotId2 = ar.userObj;
                if (ar.exception != null || ar.result == null) {
                    logd("EVENT_GET_NETWORK_SELECTION_MODE_DONE: error getting network mode.");
                    return;
                } else if (ar.result[0] == 1) {
                    mPhone[slotId2.intValue()].setNetworkSelectionModeAutomatic(null);
                    return;
                } else {
                    return;
                }
            case 3:
                handleSimLoaded(msg.arg1);
                return;
            case 4:
                handleSimAbsentOrError(msg.arg1, "ABSENT");
                return;
            case 5:
                handleSimLocked(msg.arg1, (String) msg.obj);
                return;
            case 6:
                handleSimAbsentOrError(msg.arg1, "CARD_IO_ERROR");
                return;
            case 7:
                updateCarrierServices(msg.arg1, "UNKNOWN");
                return;
            case 8:
                updateCarrierServices(msg.arg1, "CARD_RESTRICTED");
                return;
            case 9:
                if (updateEmbeddedSubscriptions()) {
                    SubscriptionController.getInstance().notifySubscriptionInfoChanged();
                }
                if (msg.obj != null) {
                    ((Runnable) msg.obj).run();
                    return;
                }
                return;
            case 10:
                handleSimNotReady(msg.arg1);
                return;
            case 200:
                if (isAllIccIdQueryDone()) {
                    updateSubscriptionInfoByIccId();
                    return;
                }
                return;
            default:
                logd("Unknown msg:" + msg.what);
                return;
        }
    }

    void requestEmbeddedSubscriptionInfoListRefresh(Runnable callback) {
        sendMessage(obtainMessage(9, callback));
    }

    protected void handleSimLocked(int slotId, String reason) {
        IccFileHandler fileHandler;
        if (mIccId[slotId] != null && mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM)) {
            logd(SimType.SIM_TYPE_SIM_TAG + (slotId + 1) + " hot plug in");
            mIccId[slotId] = null;
        }
        if (mPhone[slotId].getIccCard() == null) {
            fileHandler = null;
        } else {
            fileHandler = mPhone[slotId].getIccCard().getIccFileHandler();
        }
        if (fileHandler != null) {
            String iccId = mIccId[slotId];
            if (iccId == null) {
                logd("Querying IccId");
                fileHandler.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(1, new QueryIccIdUserObj(reason, slotId)));
                return;
            }
            logd("NOT Querying IccId its already set sIccid[" + slotId + "]=" + iccId);
            updateCarrierServices(slotId, "LOCKED");
            broadcastSimStateChanged(slotId, "LOCKED", reason);
            return;
        }
        logd("sFh[" + slotId + "] is null, ignore");
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private String checkOperatorForSimName(String imsi) {
        String defaultSimName = "UNKNOWN";
        if (TelephonyPhoneUtils.sIsOversea) {
            return defaultSimName;
        }
        if (!TextUtils.isEmpty(imsi)) {
            if (TelephonyPhoneUtils.getOperatorTypeByImsi(imsi) == 2) {
                defaultSimName = mContext.getText(51249523).toString();
            } else if (TelephonyPhoneUtils.getOperatorTypeByImsi(imsi) == 0) {
                defaultSimName = mContext.getText(51249521).toString();
            } else if (TelephonyPhoneUtils.getOperatorTypeByImsi(imsi) == 1) {
                defaultSimName = mContext.getText(51249522).toString();
            }
        }
        return defaultSimName;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private String getImsi(int slot) {
        logd("getImsi slot = " + slot);
        String imsi = ICCID_STRING_FOR_NO_SIM;
        if (slot < 0 || slot > 1) {
            return imsi;
        }
        UiccCard uiccCard = mPhone[slot].getUiccCard();
        if (uiccCard == null) {
            return imsi;
        }
        UiccCardApplication app3GPP = uiccCard.getApplication(1);
        UiccCardApplication app3GPP2 = uiccCard.getApplication(2);
        if (!(app3GPP2 == null || app3GPP2.getIccRecords() == null)) {
            imsi = app3GPP2.getIccRecords().getIMSI();
            logd("getImsi app3GPP2 imsi = " + imsi);
            if (!TextUtils.isEmpty(imsi)) {
                return imsi;
            }
        }
        if (app3GPP == null || app3GPP.getIccRecords() == null) {
            logd("getImsi imsi = " + imsi);
            return imsi;
        }
        imsi = app3GPP.getIccRecords().getIMSI();
        logd("getImsi app3GPP imsi = " + imsi);
        return imsi;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private void updateSubName(int subId) {
        SubscriptionInfo subInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(subId);
        logd("updateSubName subId = " + subId);
        if (subInfo != null && subInfo.getNameSource() != 2) {
            String nameToSet = ICCID_STRING_FOR_NO_SIM;
            int slotId = SubscriptionManager.getSlotIndex(subId);
            String imsi = getImsi(slotId);
            logd("updateSubName, imsi = " + imsi + ", slotId = " + slotId);
            if (SubscriptionManager.isValidSlotIndex(slotId) && (TextUtils.isEmpty(imsi) ^ 1) != 0) {
                if (TelephonyPhoneUtils.sIsOversea) {
                    this.mSubscriptionManager.setDisplayName(SimType.SIM_TYPE_SIM_TAG + Integer.toString(slotId + 1), subId);
                    return;
                }
                nameToSet = checkOperatorForSimName(imsi);
                if ("UNKNOWN".equals(nameToSet)) {
                    logd("Operator name is not (cm cu ct)!");
                    this.mSubscriptionManager.setDisplayName(SimType.SIM_TYPE_SIM_TAG + Integer.toString(slotId + 1), subId);
                    return;
                }
                int subId2 = SubscriptionController.getInstance().getSubIdUsingPhoneId(1 - slotId);
                SubscriptionInfo subInfo2 = SubscriptionController.getInstance().getActiveSubscriptionInfo(subId2, mContext.getOpPackageName());
                if (subInfo2 != null) {
                    String nameToSet2 = checkOperatorForSimName(getImsi(1 - slotId));
                    if (nameToSet.equals(nameToSet2)) {
                        nameToSet = nameToSet + (slotId + 1);
                        nameToSet2 = nameToSet2 + ((1 - slotId) + 1);
                        if (!(TextUtils.isEmpty(nameToSet2) || ("UNKNOWN".equals(nameToSet2) ^ 1) == 0 || subInfo2.getNameSource() == 2)) {
                            this.mSubscriptionManager.setDisplayName(nameToSet2, subId2);
                        }
                    }
                }
                this.mSubscriptionManager.setDisplayName(nameToSet, subId);
            }
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    protected void handleSimLoaded(int slotId) {
        logd("handleSimLoaded: slotId: " + slotId);
        int loadedSlotId = slotId;
        IccRecords records = mPhone[slotId].getIccCard().getIccRecords();
        if (records == null) {
            logd("handleSimLoaded: IccRecords null");
        } else if (records.getFullIccId() == null) {
            logd("onRecieve: IccID null");
        } else {
            mIccId[slotId] = records.getFullIccId();
            if (isAllIccIdQueryDone()) {
                updateSubscriptionInfoByIccId();
                for (int subId : this.mSubscriptionManager.getActiveSubscriptionIdList()) {
                    slotId = SubscriptionController.getInstance().getPhoneId(subId);
                    String operator = mPhone[slotId].getOperatorNumeric();
                    if (operator == null || (TextUtils.isEmpty(operator) ^ 1) == 0) {
                        logd("EVENT_RECORDS_LOADED Operator name is null");
                    } else {
                        if (subId == SubscriptionController.getInstance().getDefaultSubId()) {
                            MccTable.updateMccMncConfiguration(mContext, operator, false);
                        }
                        SubscriptionController.getInstance().setMccMnc(operator, subId);
                    }
                    String msisdn = TelephonyManager.getDefault().getLine1Number(subId);
                    ContentResolver contentResolver = mContext.getContentResolver();
                    if (msisdn != null) {
                        SubscriptionController.getInstance().setDisplayNumber(msisdn, subId);
                    }
                    updateSubName(subId);
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                    if (sp.getInt(CURR_SUBID + slotId, -1) != subId) {
                        Editor editor = sp.edit();
                        editor.putInt(CURR_SUBID + slotId, subId);
                        editor.apply();
                    }
                }
            }
            CarrierAppUtils.disableCarrierAppsUntilPrivileged(mContext.getOpPackageName(), this.mPackageManager, TelephonyManager.getDefault(), mContext.getContentResolver(), this.mCurrentlyActiveUserId);
            broadcastSimStateChanged(loadedSlotId, "LOADED", null);
            updateCarrierServices(loadedSlotId, "LOADED");
        }
    }

    private void updateCarrierServices(int slotId, String simState) {
        ((CarrierConfigManager) mContext.getSystemService("carrier_config")).updateConfigForPhoneId(slotId, simState);
        this.mCarrierServiceBindHelper.updateForPhoneId(slotId, simState);
    }

    protected void handleSimAbsentOrError(int slotId, String simState) {
        if (!(mIccId[slotId] == null || (mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM) ^ 1) == 0)) {
            logd(SimType.SIM_TYPE_SIM_TAG + (slotId + 1) + " hot plug out or error.");
        }
        mIccId[slotId] = ICCID_STRING_FOR_NO_SIM;
        if (SubscriptionController.getInstance().isSubInfoReady()) {
            boolean isAllCardAbsent = true;
            for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                if (mIccId[i] != ICCID_STRING_FOR_NO_SIM) {
                    isAllCardAbsent = false;
                }
            }
            logd("isAllCardAbsent:" + isAllCardAbsent);
            if (isAllCardAbsent) {
                if (isAllIccIdQueryDone()) {
                    updateSubscriptionInfoByIccId();
                }
                if (hasMessages(200)) {
                    removeMessages(200);
                }
            } else if (hasMessages(200)) {
                removeMessages(200);
                if (isAllIccIdQueryDone()) {
                    updateSubscriptionInfoByIccId();
                }
            } else {
                sendEmptyMessageDelayed(200, 3000);
            }
        } else if (isAllIccIdQueryDone()) {
            updateSubscriptionInfoByIccId();
        }
        updateCarrierServices(slotId, simState);
    }

    private void handleSimAbsent(int slotId) {
        if (!(mIccId[slotId] == null || (mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM) ^ 1) == 0)) {
            logd(SimType.SIM_TYPE_SIM_TAG + (slotId + 1) + " hot plug out or error.");
        }
        mIccId[slotId] = ICCID_STRING_FOR_NO_SIM;
        if (isAllIccIdQueryDone()) {
            updateSubscriptionInfoByIccId();
        }
        updateCarrierServices(slotId, "ABSENT");
    }

    private void handleSimError(int slotId) {
        if (!(mIccId[slotId] == null || (mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM) ^ 1) == 0)) {
            logd(SimType.SIM_TYPE_SIM_TAG + (slotId + 1) + " Error ");
        }
        mIccId[slotId] = ICCID_STRING_FOR_NO_SIM;
        if (isAllIccIdQueryDone()) {
            updateSubscriptionInfoByIccId();
        }
        updateCarrierServices(slotId, "CARD_IO_ERROR");
    }

    protected void handleSimNotReady(int slotId) {
        logd("handleSimNotReady.");
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    protected synchronized void updateSubscriptionInfoByIccId() {
        int i;
        ContentValues value;
        logd("updateSubscriptionInfoByIccId:+ Start");
        SubscriptionController.getInstance().saveInfoToTemporary();
        for (i = 0; i < PROJECT_SIM_NUM; i++) {
            mInsertSimState[i] = 0;
        }
        int insertedSimCount = PROJECT_SIM_NUM;
        i = 0;
        while (i < PROJECT_SIM_NUM) {
            if (ICCID_STRING_FOR_NO_SIM.equals(mIccId[i]) || mIccId[i] == null) {
                insertedSimCount--;
                mInsertSimState[i] = -99;
            }
            i++;
        }
        logd("insertedSimCount = " + insertedSimCount);
        if (SubscriptionController.getInstance().getActiveSubIdList().length > insertedSimCount) {
            SubscriptionController.getInstance().clearSubInfo();
        }
        i = 0;
        while (i < PROJECT_SIM_NUM) {
            if (mInsertSimState[i] != -99) {
                int index = 2;
                int j = i + 1;
                while (j < PROJECT_SIM_NUM) {
                    if (mInsertSimState[j] == 0 && mIccId[i].equals(mIccId[j])) {
                        mInsertSimState[i] = 1;
                        mInsertSimState[j] = index;
                        index++;
                    }
                    j++;
                }
            }
            i++;
        }
        ContentResolver contentResolver = mContext.getContentResolver();
        String[] oldIccId = new String[PROJECT_SIM_NUM];
        i = 0;
        while (i < PROJECT_SIM_NUM) {
            oldIccId[i] = null;
            List<SubscriptionInfo> oldSubInfo = SubscriptionController.getInstance().getSubInfoUsingSlotIndexWithCheck(i, false, mContext.getOpPackageName());
            if (oldSubInfo == null || oldSubInfo.size() <= 0) {
                if (mInsertSimState[i] == 0) {
                    mInsertSimState[i] = -1;
                }
                oldIccId[i] = ICCID_STRING_FOR_NO_SIM;
                logd("updateSubscriptionInfoByIccId: No SIM in slot " + i + " last time");
            } else {
                oldIccId[i] = ((SubscriptionInfo) oldSubInfo.get(0)).getIccId();
                logd("updateSubscriptionInfoByIccId: oldSubId = " + ((SubscriptionInfo) oldSubInfo.get(0)).getSubscriptionId());
                if (mInsertSimState[i] == 0 && (mIccId[i].equals(oldIccId[i]) ^ 1) != 0) {
                    mInsertSimState[i] = -1;
                }
                if (mInsertSimState[i] != 0) {
                    value = new ContentValues(1);
                    value.put("sim_id", Integer.valueOf(-1));
                    try {
                        contentResolver.update(SubscriptionManager.CONTENT_URI, value, "_id=" + Integer.toString(((SubscriptionInfo) oldSubInfo.get(0)).getSubscriptionId()), null);
                    } catch (Exception e) {
                        logd("update error: " + e.toString());
                    }
                    SubscriptionController.getInstance().refreshCachedActiveSubscriptionInfoList();
                } else {
                    continue;
                }
            }
            i++;
        }
        for (i = 0; i < PROJECT_SIM_NUM; i++) {
            logd("updateSubscriptionInfoByIccId: oldIccId[" + i + "] = " + oldIccId[i] + ", sIccId[" + i + "] = " + mIccId[i]);
        }
        int nNewCardCount = 0;
        int nNewSimStatus = 0;
        for (i = 0; i < PROJECT_SIM_NUM; i++) {
            if (mInsertSimState[i] == -99) {
                logd("updateSubscriptionInfoByIccId: No SIM inserted in slot " + i + " this time");
            } else {
                if (mInsertSimState[i] > 0) {
                    this.mSubscriptionManager.addSubscriptionInfoRecord(mIccId[i] + Integer.toString(mInsertSimState[i]), i);
                    logd("SUB" + (i + 1) + " has invalid IccId");
                } else {
                    this.mSubscriptionManager.addSubscriptionInfoRecord(mIccId[i], i);
                }
                if (isNewSim(mIccId[i], oldIccId)) {
                    nNewCardCount++;
                    switch (i) {
                        case 0:
                            nNewSimStatus |= 1;
                            break;
                        case 1:
                            nNewSimStatus |= 2;
                            break;
                        case 2:
                            nNewSimStatus |= 4;
                            break;
                    }
                    mInsertSimState[i] = -2;
                }
            }
        }
        SubscriptionController.getInstance().clearInfoFromTemporary();
        for (i = 0; i < PROJECT_SIM_NUM; i++) {
            if (mInsertSimState[i] == -1) {
                mInsertSimState[i] = -3;
            }
            logd("updateSubscriptionInfoByIccId: sInsertSimState[" + i + "] = " + mInsertSimState[i]);
        }
        List<SubscriptionInfo> subInfos = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        int nSubCount = subInfos == null ? 0 : subInfos.size();
        logd("updateSubscriptionInfoByIccId: nSubCount = " + nSubCount);
        for (i = 0; i < nSubCount; i++) {
            SubscriptionInfo temp = (SubscriptionInfo) subInfos.get(i);
            String msisdn = TelephonyManager.getDefault().getLine1Number(temp.getSubscriptionId());
            if (msisdn != null) {
                value = new ContentValues(1);
                value.put(IccProvider.STR_NUMBER, msisdn);
                contentResolver.update(SubscriptionManager.CONTENT_URI, value, "_id=" + Integer.toString(temp.getSubscriptionId()), null);
                SubscriptionController.getInstance().refreshCachedActiveSubscriptionInfoList();
            }
        }
        updateEmbeddedSubscriptions();
        SubscriptionController.getInstance().notifySubscriptionInfoChanged();
        tryNotifyPinAgain();
        notifySubInfoReadyForIMS();
        logd("updateSubscriptionInfoByIccId:- SubscriptionInfo update complete");
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private void tryNotifyPinAgain() {
        int i = 0;
        while (i < PROJECT_SIM_NUM) {
            if (this.mNotifyPinAgain[i] && getSimState(i) == State.PIN_REQUIRED) {
                broadcastSimStateChanged(i, "LOCKED", "PIN");
            }
            this.mNotifyPinAgain[i] = false;
            i++;
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private void notifySubInfoReadyForIMS() {
        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
            mPhone[i].notifySubInfoReadyForIMS();
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private State getSimState(int slot) {
        State state = State.UNKNOWN;
        IccCard iccCard = mPhone[slot].getIccCard();
        if (iccCard != null) {
            return iccCard.getState();
        }
        return state;
    }

    public boolean updateEmbeddedSubscriptions() {
        if (!this.mEuiccManager.isEnabled()) {
            return false;
        }
        GetEuiccProfileInfoListResult result = EuiccController.get().blockingGetEuiccProfileInfoList();
        if (result == null) {
            return false;
        }
        EuiccProfileInfo[] embeddedProfiles;
        int i;
        ContentValues values;
        if (result.result == 0) {
            embeddedProfiles = result.profiles;
        } else {
            logd("updatedEmbeddedSubscriptions: error " + result.result + " listing profiles");
            embeddedProfiles = new EuiccProfileInfo[0];
        }
        boolean isRemovable = result.isRemovable;
        String[] embeddedIccids = new String[embeddedProfiles.length];
        for (i = 0; i < embeddedProfiles.length; i++) {
            embeddedIccids[i] = embeddedProfiles[i].iccid;
        }
        boolean hasChanges = false;
        List<SubscriptionInfo> existingSubscriptions = SubscriptionController.getInstance().getSubscriptionInfoListForEmbeddedSubscriptionUpdate(embeddedIccids, isRemovable);
        ContentResolver contentResolver = mContext.getContentResolver();
        int i2 = 0;
        int length = embeddedProfiles.length;
        while (true) {
            int i3 = i2;
            if (i3 >= length) {
                break;
            }
            byte[] bArr;
            EuiccProfileInfo embeddedProfile = embeddedProfiles[i3];
            int index = findSubscriptionInfoForIccid(existingSubscriptions, embeddedProfile.iccid);
            if (index < 0) {
                SubscriptionController.getInstance().insertEmptySubInfoRecord(embeddedProfile.iccid, -1);
            } else {
                existingSubscriptions.remove(index);
            }
            values = new ContentValues();
            values.put("is_embedded", Integer.valueOf(1));
            String str = "access_rules";
            if (embeddedProfile.accessRules == null) {
                bArr = null;
            } else {
                bArr = UiccAccessRule.encodeRules(embeddedProfile.accessRules);
            }
            values.put(str, bArr);
            values.put("is_removable", Boolean.valueOf(isRemovable));
            values.put("display_name", embeddedProfile.nickname);
            values.put("name_source", Integer.valueOf(2));
            hasChanges = true;
            contentResolver.update(SubscriptionManager.CONTENT_URI, values, "icc_id=\"" + embeddedProfile.iccid + "\"", null);
            SubscriptionController.getInstance().refreshCachedActiveSubscriptionInfoList();
            i2 = i3 + 1;
        }
        if (!existingSubscriptions.isEmpty()) {
            List<String> iccidsToRemove = new ArrayList();
            for (i = 0; i < existingSubscriptions.size(); i++) {
                SubscriptionInfo info = (SubscriptionInfo) existingSubscriptions.get(i);
                if (info.isEmbedded()) {
                    iccidsToRemove.add("\"" + info.getIccId() + "\"");
                }
            }
            String whereClause = "icc_id IN (" + TextUtils.join(",", iccidsToRemove) + ")";
            values = new ContentValues();
            values.put("is_embedded", Integer.valueOf(0));
            hasChanges = true;
            contentResolver.update(SubscriptionManager.CONTENT_URI, values, whereClause, null);
            SubscriptionController.getInstance().refreshCachedActiveSubscriptionInfoList();
        }
        return hasChanges;
    }

    private static int findSubscriptionInfoForIccid(List<SubscriptionInfo> list, String iccid) {
        for (int i = 0; i < list.size(); i++) {
            if (TextUtils.equals(iccid, ((SubscriptionInfo) list.get(i)).getIccId())) {
                return i;
            }
        }
        return -1;
    }

    private boolean isNewSim(String iccId, String[] oldIccId) {
        boolean newSim = true;
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            if (iccId.equals(oldIccId[i])) {
                newSim = false;
                break;
            }
        }
        logd("newSim = " + newSim);
        return newSim;
    }

    private void broadcastSimStateChanged(int slotId, String state, String reason) {
        if (!SubscriptionController.getInstance().isSubInfoReady() && "LOCKED".equals(state)) {
            this.mNotifyPinAgain[slotId] = true;
        }
        Intent i = new Intent("android.intent.action.SIM_STATE_CHANGED");
        i.addFlags(67108864);
        i.putExtra("phoneName", "Phone");
        i.putExtra("ss", state);
        i.putExtra("reason", reason);
        SubscriptionManager.putPhoneIdAndSubIdExtra(i, slotId);
        logd("Broadcasting intent ACTION_SIM_STATE_CHANGED " + state + " reason " + reason + " for mCardIndex: " + slotId);
        IntentBroadcaster.getInstance().broadcastStickyIntent(i, slotId);
    }

    public void dispose() {
        logd("[dispose]");
        mContext.unregisterReceiver(this.sReceiver);
    }

    private void logd(String message) {
        Rlog.d(LOG_TAG, message);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("SubscriptionInfoUpdater:");
        this.mCarrierServiceBindHelper.dump(fd, pw, args);
    }

    String getIccIdFromPhoneId(int phoneId) {
        if (phoneId < 0 || phoneId >= PROJECT_SIM_NUM) {
            return null;
        }
        return mIccId[phoneId];
    }
}
