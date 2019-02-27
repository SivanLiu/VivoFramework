package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.dataconnection.DcFailCause;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.vivo.common.VivoCollectData;
import java.util.HashMap;

public class RoamingStatusCollectHelper extends Handler {
    private static int AIRPLAIN_MODULE = 8102;
    private static int BOOTUP_MODULE = 8101;
    private static int DATACALL_MODULE = 8103;
    private static final int EVENT_GET_SMSC_NUMBER0 = 2004;
    private static final int EVENT_GET_SMSC_NUMBER1 = 2005;
    private static final int EVENT_GET_SMSC_NUMBER_DONE0 = 2006;
    private static final int EVENT_GET_SMSC_NUMBER_DONE1 = 2007;
    private static final int EVENT_ICC_CHANGED = 2003;
    private static final int EVENT_UPDATA_STREET_STATUS_DONE0 = 2001;
    private static final int EVENT_UPDATA_STREET_STATUS_DONE1 = 2002;
    private static int OOS_MODULE = 8106;
    private static final int REQUSET_UPDATE_STREET_STATUS0 = 1001;
    private static final int REQUSET_UPDATE_STREET_STATUS1 = 1002;
    private static int SMS_MODULE = 8105;
    private static final String TAG = "RoamingStatusCollectHelper";
    private static int VOICECALL_MODULE = 8104;
    private static Context mContext;
    private static RoamingStatusCollectHelper mRoamingStatusCollectHelper;
    private AirplaneRegisterInfo[] mAirplaneRegisterInfo = new AirplaneRegisterInfo[]{new AirplaneRegisterInfo(this, null), new AirplaneRegisterInfo(this, null)};
    private CallFailedInfo[] mCallFailedInfo = new CallFailedInfo[]{new CallFailedInfo(this, null), new CallFailedInfo(this, null)};
    private CardState mCardState = CardState.CARDSTATE_ABSENT;
    private CommonRegisterStatusInfo[] mCommonRegisterStatusInfo = new CommonRegisterStatusInfo[]{new CommonRegisterStatusInfo(this, null), new CommonRegisterStatusInfo(this, null)};
    private DataCallFailedInfo[] mDataCallFailedInfo = new DataCallFailedInfo[]{new DataCallFailedInfo(this, null), new DataCallFailedInfo(this, null)};
    private boolean mIsAirplaneOn = false;
    private boolean mIsRoaming;
    private String mMcc = "";
    private OosRecorder[] mOosRecorder = new OosRecorder[]{new OosRecorder(this, null), new OosRecorder(this, null)};
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if ("vivo.intent.action.USER_COUNTRY_CHANGE".equals(intent.getAction())) {
                RoamingStatusCollectHelper.this.mMcc = SystemProperties.get("persist.radio.vivo.mcc", "");
            } else if (!"android.intent.action.AIRPLANE_MODE".equals(intent.getAction())) {
            } else {
                if (intent.getBooleanExtra("state", false)) {
                    RoamingStatusCollectHelper.this.mIsAirplaneOn = true;
                    RoamingStatusCollectHelper.this.mAirplaneRegisterInfo[0].mNeedCollect = true;
                    RoamingStatusCollectHelper.this.mAirplaneRegisterInfo[1].mNeedCollect = true;
                    return;
                }
                RoamingStatusCollectHelper.this.mIsAirplaneOn = false;
                RoamingStatusCollectHelper.this.setAirplaneOffTime();
            }
        }
    };
    private RoamingRegisterInfo[] mRoamingRegisterInfo = new RoamingRegisterInfo[]{new RoamingRegisterInfo(this, null), new RoamingRegisterInfo(this, null)};
    private SmsFailedInfo[] mSmsFailedInfo = new SmsFailedInfo[]{new SmsFailedInfo(this, null), new SmsFailedInfo(this, null)};
    private VivoCollectData mVivoCollectData;

    private class AirplaneRegisterInfo {
        boolean mNeedCollect;
        long mTimeAirplaneOff;
        long mTimeRegistered;

        /* synthetic */ AirplaneRegisterInfo(RoamingStatusCollectHelper this$0, AirplaneRegisterInfo -this1) {
            this();
        }

        private AirplaneRegisterInfo() {
        }

        void clearInfo() {
            this.mTimeAirplaneOff = 0;
            this.mTimeRegistered = 0;
        }
    }

    private class CallFailedInfo {
        int mErrorCode;
        String mNumber;
        long mTimeError;

        /* synthetic */ CallFailedInfo(RoamingStatusCollectHelper this$0, CallFailedInfo -this1) {
            this();
        }

        private CallFailedInfo() {
            this.mNumber = "";
        }

        void clearInfo() {
            this.mErrorCode = 0;
            this.mTimeError = 0;
            this.mNumber = "";
        }
    }

    private class CommonRegisterStatusInfo {
        String mCellInfo;
        int mCellInfoRetryTime;
        int mGetSmscRetryTimes;
        String mHplmn;
        int mRat;
        String mSmscNumber;

        /* synthetic */ CommonRegisterStatusInfo(RoamingStatusCollectHelper this$0, CommonRegisterStatusInfo -this1) {
            this();
        }

        private CommonRegisterStatusInfo() {
        }

        void clearInfo() {
            this.mGetSmscRetryTimes = 0;
            this.mCellInfoRetryTime = 0;
            this.mSmscNumber = null;
            this.mHplmn = null;
            this.mRat = 0;
            this.mCellInfo = null;
        }

        void printString() {
            RoamingStatusCollectHelper.this.log("mSmscNumber =" + this.mSmscNumber + " mHplmn=" + this.mHplmn + " mRat=" + this.mRat + " mCellInfo=" + this.mCellInfo);
        }
    }

    private class DataCallFailedInfo {
        String mApnType;
        DcFailCause mCause;
        long mTimeError;

        /* synthetic */ DataCallFailedInfo(RoamingStatusCollectHelper this$0, DataCallFailedInfo -this1) {
            this();
        }

        private DataCallFailedInfo() {
        }

        void clearInfo() {
            this.mCause = null;
            this.mTimeError = 0;
        }
    }

    private class OosRecorder {
        String mLastInServiceCellInfo;
        long mOosTime;
        String mRecentOosCellInfo;
        long mRecoverTime;

        /* synthetic */ OosRecorder(RoamingStatusCollectHelper this$0, OosRecorder -this1) {
            this();
        }

        private OosRecorder() {
        }

        void clearInfo() {
            this.mOosTime = 0;
            this.mRecoverTime = 0;
            this.mLastInServiceCellInfo = null;
        }
    }

    private class RoamingRegisterInfo {
        boolean mIsCollect;
        long mRegDuringTime;
        long mRegFinishedClock;
        long mRegFinishedTime;
        long mRegStartTime;

        /* synthetic */ RoamingRegisterInfo(RoamingStatusCollectHelper this$0, RoamingRegisterInfo -this1) {
            this();
        }

        private RoamingRegisterInfo() {
        }

        void printString() {
            RoamingStatusCollectHelper.this.log(this.mRegStartTime + "," + this.mRegDuringTime + "," + this.mRegFinishedTime + "," + this.mRegFinishedClock);
        }

        void clearInfo() {
            this.mIsCollect = false;
            this.mRegStartTime = 0;
            this.mRegDuringTime = 0;
            this.mRegFinishedTime = 0;
            this.mRegFinishedClock = 0;
        }
    }

    private class SmsFailedInfo {
        int mErrorCode;
        long mTimeError;

        /* synthetic */ SmsFailedInfo(RoamingStatusCollectHelper this$0, SmsFailedInfo -this1) {
            this();
        }

        private SmsFailedInfo() {
        }

        void clearInfo() {
            this.mErrorCode = 0;
            this.mTimeError = 0;
        }
    }

    public void setDataCallFailedReason(int phoneId, DcFailCause cause, String apnType) {
        if ((phoneId == 0 || phoneId == 1) && cause != DcFailCause.NONE) {
            this.mDataCallFailedInfo[phoneId].mApnType = apnType;
            this.mDataCallFailedInfo[phoneId].mCause = cause;
            this.mDataCallFailedInfo[phoneId].mTimeError = System.currentTimeMillis();
            collectDataCallFailedIfNeeded(phoneId);
        }
    }

    private void collectDataCallFailedIfNeeded(int phoneId) {
        if (phoneId == 0 || phoneId == 1) {
            HashMap<String, String> params = new HashMap();
            params.put("cause", this.mDataCallFailedInfo[phoneId].mCause.toString());
            params.put("failed_time", "" + this.mDataCallFailedInfo[phoneId].mTimeError);
            params.put("apn", this.mDataCallFailedInfo[phoneId].mApnType);
            params.put("hplmn", this.mCommonRegisterStatusInfo[phoneId].mHplmn);
            params.put("cell_info", this.mCommonRegisterStatusInfo[phoneId].mCellInfo);
            collectData(phoneId, DATACALL_MODULE, params);
        }
    }

    public void setCallNumber(int phoneId, String address) {
        if (phoneId == 0 || phoneId == 1) {
            this.mCallFailedInfo[phoneId].mNumber = address;
        }
    }

    public void setCallFailedReason(int phoneId, int code) {
        if (phoneId == 0 || phoneId == 1) {
            this.mCallFailedInfo[phoneId].mErrorCode = code;
            this.mCallFailedInfo[phoneId].mTimeError = System.currentTimeMillis();
            collectCallFailedReasonIfNeeded(phoneId);
        }
    }

    private void collectCallFailedReasonIfNeeded(int phoneId) {
        if (phoneId == 0 || phoneId == 1) {
            HashMap<String, String> params = new HashMap();
            params.put("cause", "" + this.mCallFailedInfo[phoneId].mErrorCode);
            params.put("failed_time", "" + this.mCallFailedInfo[phoneId].mTimeError);
            params.put("dial_num", this.mCallFailedInfo[phoneId].mNumber);
            params.put("hplmn", this.mCommonRegisterStatusInfo[phoneId].mHplmn);
            params.put("cell_info", this.mCommonRegisterStatusInfo[phoneId].mCellInfo);
            collectData(phoneId, VOICECALL_MODULE, params);
        }
    }

    public void setSmsErrorCode(int phoneId, int code) {
        if (phoneId == 0 || phoneId == 1) {
            log("setSmsErrorCode " + phoneId + " " + code);
            this.mSmsFailedInfo[phoneId].mErrorCode = code;
            this.mSmsFailedInfo[phoneId].mTimeError = System.currentTimeMillis();
            collectSmsErrorDataIfNeeded(phoneId);
        }
    }

    private void collectSmsErrorDataIfNeeded(int phoneId) {
        if (phoneId == 0 || phoneId == 1) {
            HashMap<String, String> params = new HashMap();
            params.put("cause", "" + this.mCallFailedInfo[phoneId].mErrorCode);
            params.put("failed_time", "" + this.mCallFailedInfo[phoneId].mTimeError);
            params.put("smsc_num", this.mCommonRegisterStatusInfo[phoneId].mSmscNumber);
            params.put("hplmn", this.mCommonRegisterStatusInfo[phoneId].mHplmn);
            params.put("cell_info", this.mCommonRegisterStatusInfo[phoneId].mCellInfo);
            collectData(phoneId, SMS_MODULE, params);
        }
    }

    public static void init(Looper looper, Context context) {
        if (mRoamingStatusCollectHelper == null) {
            mContext = context;
            mRoamingStatusCollectHelper = new RoamingStatusCollectHelper(looper);
        }
    }

    public static RoamingStatusCollectHelper getInstance() {
        return mRoamingStatusCollectHelper;
    }

    public void register() {
        UiccController.getInstance().registerForIccChanged(this, EVENT_ICC_CHANGED, null);
    }

    public void setAirplaneOffRegisterTime() {
        this.mAirplaneRegisterInfo[0].mTimeRegistered = SystemClock.elapsedRealtime();
        this.mAirplaneRegisterInfo[1].mTimeRegistered = SystemClock.elapsedRealtime();
    }

    private void setAirplaneOffTime() {
        this.mAirplaneRegisterInfo[0].mTimeAirplaneOff = SystemClock.elapsedRealtime();
        this.mAirplaneRegisterInfo[1].mTimeAirplaneOff = SystemClock.elapsedRealtime();
    }

    private void collectAirplaneOffInfoIfNeeded(int phoneId) {
        if (phoneId == 0 || phoneId == 1) {
            HashMap<String, String> params = new HashMap();
            params.put("reg_during_time", "" + (this.mAirplaneRegisterInfo[phoneId].mTimeRegistered - this.mAirplaneRegisterInfo[phoneId].mTimeAirplaneOff));
            params.put("hplmn", this.mCommonRegisterStatusInfo[phoneId].mHplmn);
            params.put("cell_info", this.mCommonRegisterStatusInfo[phoneId].mCellInfo);
            collectData(phoneId, AIRPLAIN_MODULE, params);
        }
    }

    public RoamingStatusCollectHelper(Looper looper) {
        super(looper);
        IntentFilter filter = new IntentFilter("android.intent.action.AIRPLANE_MODE");
        filter.addAction("vivo.intent.action.USER_COUNTRY_CHANGE");
        mContext.registerReceiver(this.mReceiver, filter);
        this.mVivoCollectData = new VivoCollectData(mContext);
        if ("1".equals(SystemProperties.get("persist.radio.roaming.test", "1"))) {
            this.mIsRoaming = true;
        }
        register();
    }

    public void onRecordsLoaded(int phoneId) {
        if (phoneId == 0 || phoneId == 1) {
            log("onRecordsLoaded " + phoneId);
            if (TextUtils.isEmpty(this.mCommonRegisterStatusInfo[phoneId].mSmscNumber)) {
                Message msg0 = obtainMessage(EVENT_GET_SMSC_NUMBER0);
                Message msg1 = obtainMessage(EVENT_GET_SMSC_NUMBER1);
                if (phoneId == 0) {
                    if (hasMessages(EVENT_GET_SMSC_NUMBER0)) {
                        removeMessages(EVENT_GET_SMSC_NUMBER0);
                    }
                    sendMessage(msg0);
                } else if (phoneId == 1) {
                    if (hasMessages(EVENT_GET_SMSC_NUMBER1)) {
                        removeMessages(EVENT_GET_SMSC_NUMBER1);
                    }
                    sendMessage(msg1);
                }
            } else {
                log("aleady has valid smsc phoneId=" + phoneId + " number=" + this.mCommonRegisterStatusInfo[phoneId].mSmscNumber);
            }
        }
    }

    public void onImsiGetDone(int phoneId, String plmn) {
        if (phoneId == 0 || phoneId == 1) {
            this.mCommonRegisterStatusInfo[phoneId].mHplmn = plmn;
            if (!(TextUtils.isEmpty(this.mMcc) || plmn == null || plmn.length() < 3)) {
                this.mIsRoaming = this.mCommonRegisterStatusInfo[phoneId].mHplmn.startsWith(this.mMcc);
            }
            log("onImsiGetDone plmn=" + plmn);
            Phone phone = PhoneFactory.getPhone(phoneId);
            if (phone != null && ((GsmCdmaPhone) phone).mSST.mSS.getRilDataRadioTechnology() != 0) {
                collectRoamingRegInfoIfNeeded(phoneId);
            }
        }
    }

    public void updateCurrentStreetStatus(int phoneId) {
        if (phoneId == 0 || phoneId == 1) {
            log("updateCurrentStreetStatus phoneId=" + phoneId);
            Message msg0 = obtainMessage(1001);
            Message msg1 = obtainMessage(1002);
            if (phoneId == 0) {
                if (hasMessages(1001)) {
                    removeMessages(1001);
                }
                sendMessage(msg0);
            } else if (phoneId == 1) {
                if (hasMessages(1002)) {
                    removeMessages(1002);
                }
                sendMessage(msg1);
            }
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        Phone phone;
        GsmCdmaPhone gsmCdmaPhone;
        int rat;
        int lastRat;
        String lastCellInfo;
        String result;
        CommonRegisterStatusInfo commonRegisterStatusInfo;
        String smsc;
        switch (msg.what) {
            case 1001:
                PhoneFactory.getPhone(0).sendMiscInfo(2, VivoNetLowlatency.MISC_INFO_DEFAULT_SEND_BUFFER, obtainMessage(2001));
                log("sendMiscInfo 0");
                return;
            case 1002:
                PhoneFactory.getPhone(1).sendMiscInfo(2, VivoNetLowlatency.MISC_INFO_DEFAULT_SEND_BUFFER, obtainMessage(EVENT_UPDATA_STREET_STATUS_DONE1));
                log("sendMiscInfo 1");
                return;
            case 2001:
                log("EVENT_UPDATA_STREET_STATUS_DONE0");
                ar = msg.obj;
                phone = PhoneFactory.getPhone(0);
                gsmCdmaPhone = (GsmCdmaPhone) phone;
                rat = gsmCdmaPhone.mSST.mSS.getRilDataRadioTechnology();
                if (rat == 0) {
                    rat = gsmCdmaPhone.mSST.mSS.getRilVoiceRadioTechnology();
                }
                lastRat = this.mCommonRegisterStatusInfo[0].mRat;
                lastCellInfo = this.mCommonRegisterStatusInfo[0].mCellInfo;
                if (ar.exception == null) {
                    result = ar.result.replaceAll("\n|\r", "");
                    log("phoneId=0 result = " + result);
                    this.mCommonRegisterStatusInfo[0].mRat = rat;
                    this.mCommonRegisterStatusInfo[0].mHplmn = phone.getOperatorNumeric();
                    this.mRoamingRegisterInfo[0].mRegFinishedTime = SystemClock.elapsedRealtime();
                    this.mRoamingRegisterInfo[0].mRegDuringTime = this.mRoamingRegisterInfo[0].mRegFinishedTime - this.mRoamingRegisterInfo[0].mRegStartTime;
                    this.mRoamingRegisterInfo[0].mRegFinishedClock = System.currentTimeMillis();
                    this.mCommonRegisterStatusInfo[0].mCellInfo = result;
                    this.mRoamingRegisterInfo[0].printString();
                }
                recordRatChanged(0, lastRat, rat, lastCellInfo, this.mCommonRegisterStatusInfo[0].mCellInfo);
                if (isCellInfoValid(0)) {
                    this.mCommonRegisterStatusInfo[0].mCellInfoRetryTime = 0;
                    if (rat != 0) {
                        collectRoamingRegInfoIfNeeded(0);
                    }
                } else {
                    commonRegisterStatusInfo = this.mCommonRegisterStatusInfo[0];
                    commonRegisterStatusInfo.mCellInfoRetryTime++;
                    if (this.mCommonRegisterStatusInfo[0].mCellInfoRetryTime < 10 && !hasMessages(1001)) {
                        sendMessageDelayed(obtainMessage(1001), 5000);
                    }
                }
                log("phone0:");
                this.mCommonRegisterStatusInfo[0].printString();
                return;
            case EVENT_UPDATA_STREET_STATUS_DONE1 /*2002*/:
                log("EVENT_UPDATA_STREET_STATUS_DONE1");
                ar = (AsyncResult) msg.obj;
                phone = PhoneFactory.getPhone(1);
                gsmCdmaPhone = (GsmCdmaPhone) phone;
                rat = gsmCdmaPhone.mSST.mSS.getRilDataRadioTechnology();
                if (rat == 0) {
                    rat = gsmCdmaPhone.mSST.mSS.getRilVoiceRadioTechnology();
                }
                lastRat = this.mCommonRegisterStatusInfo[1].mRat;
                lastCellInfo = this.mCommonRegisterStatusInfo[1].mCellInfo;
                if (ar.exception == null) {
                    result = ((String) ar.result).replaceAll("\n|\r", "");
                    log("phoneId=1 result = " + result);
                    this.mCommonRegisterStatusInfo[1].mRat = rat;
                    this.mCommonRegisterStatusInfo[1].mHplmn = phone.getOperatorNumeric();
                    this.mRoamingRegisterInfo[1].mRegFinishedTime = SystemClock.elapsedRealtime();
                    this.mRoamingRegisterInfo[1].mRegDuringTime = this.mRoamingRegisterInfo[1].mRegFinishedTime - this.mRoamingRegisterInfo[1].mRegStartTime;
                    this.mRoamingRegisterInfo[1].mRegFinishedClock = System.currentTimeMillis();
                    this.mCommonRegisterStatusInfo[1].mCellInfo = result;
                    this.mRoamingRegisterInfo[1].printString();
                }
                recordRatChanged(1, lastRat, rat, lastCellInfo, this.mCommonRegisterStatusInfo[1].mCellInfo);
                if (isCellInfoValid(1)) {
                    this.mCommonRegisterStatusInfo[1].mCellInfoRetryTime = 0;
                    if (rat != 0) {
                        collectRoamingRegInfoIfNeeded(1);
                    }
                } else {
                    commonRegisterStatusInfo = this.mCommonRegisterStatusInfo[1];
                    commonRegisterStatusInfo.mCellInfoRetryTime++;
                    if (this.mCommonRegisterStatusInfo[1].mCellInfoRetryTime < 10 && !hasMessages(1002)) {
                        sendMessageDelayed(obtainMessage(1002), 5000);
                    }
                }
                log("phone1:");
                this.mCommonRegisterStatusInfo[1].printString();
                return;
            case EVENT_ICC_CHANGED /*2003*/:
                ar = (AsyncResult) msg.obj;
                if (ar != null && ar.exception == null && ar.result != null) {
                    int slotId = ((Integer) ar.result).intValue();
                    phone = PhoneFactory.getPhone(slotId);
                    UiccCard card = UiccController.getInstance().getUiccCard(slotId);
                    if (card != null) {
                        CardState cardState = card.getCardState();
                        if (cardState == CardState.CARDSTATE_ABSENT) {
                            this.mCardState = CardState.CARDSTATE_ABSENT;
                            log("EVENT_ICC_CHANGED absent slotId=" + slotId);
                            this.mRoamingRegisterInfo[slotId].clearInfo();
                            this.mCommonRegisterStatusInfo[slotId].clearInfo();
                            return;
                        }
                        log("EVENT_ICC_CHANGED present slotId=" + slotId);
                        if (this.mCardState == CardState.CARDSTATE_ABSENT) {
                            this.mRoamingRegisterInfo[slotId].mRegStartTime = SystemClock.elapsedRealtime();
                        }
                        this.mCardState = cardState;
                        log("EVENT_ICC_CHANGED present slotId=" + slotId);
                        return;
                    }
                    return;
                }
                return;
            case EVENT_GET_SMSC_NUMBER0 /*2004*/:
                PhoneFactory.getPhone(0).getSmscAddress(obtainMessage(EVENT_GET_SMSC_NUMBER_DONE0));
                return;
            case EVENT_GET_SMSC_NUMBER1 /*2005*/:
                PhoneFactory.getPhone(1).getSmscAddress(obtainMessage(EVENT_GET_SMSC_NUMBER_DONE1));
                return;
            case EVENT_GET_SMSC_NUMBER_DONE0 /*2006*/:
                ar = (AsyncResult) msg.obj;
                smsc = "";
                if (!(ar == null || ar.exception != null || ar.result == null)) {
                    smsc = ar.result;
                }
                log("phone0 smsc=" + smsc);
                if (TextUtils.isEmpty(smsc) && this.mCommonRegisterStatusInfo[0].mGetSmscRetryTimes < 5 && !hasMessages(EVENT_GET_SMSC_NUMBER0)) {
                    commonRegisterStatusInfo = this.mCommonRegisterStatusInfo[0];
                    commonRegisterStatusInfo.mGetSmscRetryTimes++;
                    sendMessageDelayed(obtainMessage(EVENT_GET_SMSC_NUMBER0), 5000);
                }
                this.mCommonRegisterStatusInfo[0].mSmscNumber = smsc;
                log("phone0:");
                this.mCommonRegisterStatusInfo[0].printString();
                return;
            case EVENT_GET_SMSC_NUMBER_DONE1 /*2007*/:
                ar = (AsyncResult) msg.obj;
                smsc = "";
                if (!(ar == null || ar.exception != null || ar.result == null)) {
                    smsc = ar.result;
                }
                if (TextUtils.isEmpty(smsc) && this.mCommonRegisterStatusInfo[1].mGetSmscRetryTimes < 5 && !hasMessages(EVENT_GET_SMSC_NUMBER1)) {
                    commonRegisterStatusInfo = this.mCommonRegisterStatusInfo[1];
                    commonRegisterStatusInfo.mGetSmscRetryTimes++;
                    sendMessageDelayed(obtainMessage(EVENT_GET_SMSC_NUMBER1), 5000);
                }
                this.mCommonRegisterStatusInfo[1].mSmscNumber = smsc;
                log("phone1:");
                this.mCommonRegisterStatusInfo[1].printString();
                return;
            default:
                return;
        }
    }

    private void recordRatChanged(int phoneId, int lastRat, int newRat, String lastCellInfo, String newCellInfo) {
        if (phoneId == 0 || phoneId == 1) {
            if (this.mIsAirplaneOn) {
                this.mOosRecorder[0].clearInfo();
                this.mOosRecorder[1].clearInfo();
                return;
            }
            if (lastRat == 0 && newRat != lastRat) {
                this.mOosRecorder[phoneId].mRecoverTime = SystemClock.elapsedRealtime();
            } else if (newRat == 0 && newRat != lastRat) {
                this.mOosRecorder[phoneId].mOosTime = SystemClock.elapsedRealtime();
            }
            if (newRat == 0 && newRat != lastRat) {
                this.mOosRecorder[phoneId].mLastInServiceCellInfo = lastCellInfo;
                this.mOosRecorder[phoneId].mRecentOosCellInfo = newCellInfo;
            }
            log("recordRatChanged mRecoverTime=" + this.mOosRecorder[phoneId].mRecoverTime + " mOosTime=" + this.mOosRecorder[phoneId].mOosTime);
            if (this.mOosRecorder[phoneId].mRecoverTime > 0 && this.mOosRecorder[phoneId].mOosTime > 0 && this.mOosRecorder[phoneId].mRecoverTime - this.mOosRecorder[phoneId].mOosTime > VivoNetLowlatency.LEVEL_SET_MINIMUM_TIME_INTERVAL) {
                collectOosInfo(phoneId);
            }
        }
    }

    private void collectOosInfo(int phoneId) {
        if (phoneId == 0 || phoneId == 1) {
            long oosTime = this.mOosRecorder[phoneId].mRecoverTime - this.mOosRecorder[phoneId].mOosTime;
            if (TextUtils.isEmpty(this.mCommonRegisterStatusInfo[phoneId].mHplmn)) {
                log("we didn't get hplmn, so wo don't collect data");
            } else if (this.mOosRecorder[phoneId].mOosTime != 0 && oosTime > VivoNetLowlatency.LEVEL_SET_MINIMUM_TIME_INTERVAL) {
                HashMap<String, String> params = new HashMap();
                params.put("cell_info_in_service", this.mOosRecorder[phoneId].mLastInServiceCellInfo);
                params.put("cell_info_oos", this.mOosRecorder[phoneId].mRecentOosCellInfo);
                params.put("hplmn", this.mCommonRegisterStatusInfo[phoneId].mHplmn);
                params.put("during_time", "" + oosTime);
                collectData(phoneId, OOS_MODULE, params);
                this.mOosRecorder[phoneId].clearInfo();
            }
        }
    }

    private boolean isCellInfoValid(int phoneId) {
        boolean z = false;
        if (phoneId != 0 && phoneId != 1) {
            return false;
        }
        if (!TextUtils.isEmpty(this.mCommonRegisterStatusInfo[phoneId].mCellInfo)) {
            z = "default null".equals(this.mCommonRegisterStatusInfo[phoneId].mCellInfo) ^ 1;
        }
        return z;
    }

    private void collectRoamingRegInfoIfNeeded(int phoneId) {
        if (phoneId == 0 || phoneId == 1) {
            boolean isHplmnValid = TextUtils.isEmpty(this.mCommonRegisterStatusInfo[phoneId].mHplmn) ^ 1;
            boolean isCellInfoValid = isCellInfoValid(phoneId);
            log("collectRoamingRegInfoIfNeeded mIsCollect=" + this.mRoamingRegisterInfo[phoneId].mIsCollect + " isHplmnValid=" + isHplmnValid + " isCellInfoValid=" + isCellInfoValid + " phoneId=" + phoneId);
            if (!this.mRoamingRegisterInfo[phoneId].mIsCollect && isHplmnValid && isCellInfoValid) {
                collectRoamingRegInfo(phoneId);
                this.mRoamingRegisterInfo[phoneId].mIsCollect = true;
            }
            if (this.mAirplaneRegisterInfo[phoneId].mNeedCollect && (this.mIsAirplaneOn ^ 1) != 0) {
                collectAirplaneOffInfoIfNeeded(phoneId);
                this.mAirplaneRegisterInfo[phoneId].mNeedCollect = false;
            }
        }
    }

    private void collectRoamingRegInfo(int phoneId) {
        if (phoneId == 0 || phoneId == 1) {
            HashMap<String, String> params = new HashMap();
            params.put("hplmn", this.mCommonRegisterStatusInfo[phoneId].mHplmn);
            params.put("rat", "" + this.mCommonRegisterStatusInfo[phoneId].mRat);
            params.put("reg_at_time", "" + this.mRoamingRegisterInfo[phoneId].mRegFinishedTime);
            params.put("reg_during_time", "" + this.mRoamingRegisterInfo[phoneId].mRegDuringTime);
            params.put("cell_info", this.mCommonRegisterStatusInfo[phoneId].mCellInfo);
            collectData(phoneId, BOOTUP_MODULE, params);
        }
    }

    private void collectData(int phoneId, int id, HashMap<String, String> params) {
        if ((phoneId == 0 || phoneId == 1) && this.mIsRoaming) {
            log("collectData------------------------------------ id=" + id + " phoneId=" + phoneId);
            for (String s : params.keySet()) {
                log("collect: " + s + " " + ((String) params.get(s)));
            }
            log("collectData------------------------------------");
            this.mVivoCollectData.writeData("810", "" + id, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
        }
    }

    private void log(String s) {
        Log.v(TAG, s);
    }
}
