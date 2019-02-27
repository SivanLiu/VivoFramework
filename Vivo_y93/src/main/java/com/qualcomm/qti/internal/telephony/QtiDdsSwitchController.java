package com.qualcomm.qti.internal.telephony;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.GsmCdmaCall;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneCall;
import com.qualcomm.qti.internal.telephony.primarycard.SubsidyLockSettingsObserver;

public class QtiDdsSwitchController {
    public static boolean mTempDdsSwitchRequired = false;
    private final int EVENT_PRECISE_CS_CALL_STATE_CHANGED = SubsidyLockSettingsObserver.SUBSIDY_LOCKED;
    private final int EVENT_PRECISE_IMS_CALL_STATE_CHANGED = SubsidyLockSettingsObserver.AP_LOCKED;
    private final String LOG_TAG = "QtiDdsSwitchController";
    private final String PROPERTY_TEMP_DDSSWITCH = "persist.radio.enable_temp_dds";
    private boolean isLplusLSupported = false;
    private boolean isPropertyEnabled = SystemProperties.getBoolean("persist.radio.enable_temp_dds", false);
    private final GsmCdmaCall[] mBgCsCalls;
    private final ImsPhoneCall[] mBgImsCalls;
    private final Context mContext;
    private final Handler mDdsSwitchHandler;
    private final GsmCdmaCall[] mFgCsCalls;
    private final ImsPhoneCall[] mFgImsCalls;
    private final ImsPhone[] mImsPhones;
    private boolean mIsCallActive = false;
    private boolean mNotifyCallState = false;
    private final int mNumPhones;
    private final Phone[] mPhones;
    private final GsmCdmaCall[] mRiCsCalls;
    private final ImsPhoneCall[] mRiImsCalls;
    private final SubscriptionController mSubscriptionController;
    private int mUserDdsSubId = -1;

    private class DdsSwitchHandler extends Handler {
        public DdsSwitchHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = false;
            int phoneId = ((Integer) msg.obj.userObj).intValue();
            switch (msg.what) {
                case SubsidyLockSettingsObserver.SUBSIDY_LOCKED /*101*/:
                case SubsidyLockSettingsObserver.AP_LOCKED /*102*/:
                    if (!QtiDdsSwitchController.this.mNotifyCallState) {
                        QtiDdsSwitchController.this.mUserDdsSubId = QtiDdsSwitchController.this.mSubscriptionController.getDefaultDataSubId();
                        int voiceRat = QtiDdsSwitchController.this.mPhones[phoneId].getServiceState().getRilVoiceRadioTechnology();
                        QtiDdsSwitchController qtiDdsSwitchController = QtiDdsSwitchController.this;
                        if (!(!QtiDdsSwitchController.this.isFeatureEnabled() || !QtiDdsSwitchController.this.isCallOnNonDds(phoneId) || voiceRat == 0 || voiceRat == 6 || voiceRat == 4)) {
                            boolean z2;
                            if (voiceRat != 5) {
                                z2 = true;
                            } else {
                                z2 = false;
                            }
                            z = z2;
                        }
                        qtiDdsSwitchController.mNotifyCallState = z;
                        QtiDdsSwitchController.this.log("mUserDdsSubId - " + QtiDdsSwitchController.this.mUserDdsSubId + ", voiceRat - " + voiceRat);
                    }
                    if (QtiDdsSwitchController.this.mNotifyCallState) {
                        QtiDdsSwitchController.this.log("EVENT_PRECISE_CALL_STATE_CHANGED");
                        QtiDdsSwitchController.this.processCallStateChanged(phoneId);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public QtiDdsSwitchController(int numPhones, Context context, SubscriptionController subscriptionController, Looper looper, Phone[] phones, boolean status) {
        this.mNumPhones = numPhones;
        this.mContext = context;
        this.mSubscriptionController = subscriptionController;
        this.mDdsSwitchHandler = new DdsSwitchHandler(looper);
        this.mPhones = phones;
        this.mImsPhones = new ImsPhone[this.mNumPhones];
        this.mFgCsCalls = new GsmCdmaCall[this.mNumPhones];
        this.mBgCsCalls = new GsmCdmaCall[this.mNumPhones];
        this.mRiCsCalls = new GsmCdmaCall[this.mNumPhones];
        this.mFgImsCalls = new ImsPhoneCall[this.mNumPhones];
        this.mBgImsCalls = new ImsPhoneCall[this.mNumPhones];
        this.mRiImsCalls = new ImsPhoneCall[this.mNumPhones];
        this.isLplusLSupported = status;
        int i = 0;
        while (i < this.mNumPhones) {
            this.mImsPhones[i] = (ImsPhone) this.mPhones[i].getImsPhone();
            if (!(this.mPhones[i] == null || this.mImsPhones[i] == null)) {
                this.mFgCsCalls[i] = (GsmCdmaCall) this.mPhones[i].getForegroundCall();
                this.mBgCsCalls[i] = (GsmCdmaCall) this.mPhones[i].getBackgroundCall();
                this.mRiCsCalls[i] = (GsmCdmaCall) this.mPhones[i].getRingingCall();
                this.mFgImsCalls[i] = this.mImsPhones[i].getForegroundCall();
                this.mBgImsCalls[i] = this.mImsPhones[i].getBackgroundCall();
                this.mRiImsCalls[i] = this.mImsPhones[i].getRingingCall();
            }
            i++;
        }
    }

    private void onCallStarted(int phoneId) {
        this.mUserDdsSubId = this.mSubscriptionController.getDefaultDataSubId();
        int callSubId = this.mSubscriptionController.getSubId(phoneId)[0];
        mTempDdsSwitchRequired = true;
        log("Trigger temporary DDS switch to sub: " + callSubId);
        this.mSubscriptionController.setDefaultDataSubId(callSubId);
    }

    private void onCallEnded(int phoneId) {
        mTempDdsSwitchRequired = false;
        if (this.mSubscriptionController.isActiveSubId(this.mUserDdsSubId)) {
            log("Set DDS to actual sub: " + this.mUserDdsSubId);
            this.mSubscriptionController.setDefaultDataSubId(this.mUserDdsSubId);
            return;
        }
        log("User dds sub is invalid, skip dds reset");
    }

    public static boolean isTempDdsSwitchRequired() {
        return mTempDdsSwitchRequired;
    }

    public void resetTempDdsSwitchRequired() {
        mTempDdsSwitchRequired = false;
    }

    public void updateLplusLStatus(boolean status) {
        this.isLplusLSupported = status;
        log("updateLplusLStatus - status: " + status);
        int i;
        if (this.isLplusLSupported) {
            for (i = 0; i < this.mNumPhones; i++) {
                log("Register for call state change on phone: " + i);
                this.mPhones[i].registerForPreciseCallStateChanged(this.mDdsSwitchHandler, SubsidyLockSettingsObserver.SUBSIDY_LOCKED, Integer.valueOf(i));
                this.mImsPhones[i].registerForPreciseCallStateChanged(this.mDdsSwitchHandler, SubsidyLockSettingsObserver.AP_LOCKED, Integer.valueOf(i));
            }
            return;
        }
        for (i = 0; i < this.mNumPhones; i++) {
            log("unregister for call state change on phone: " + i);
            this.mPhones[i].unregisterForPreciseCallStateChanged(this.mDdsSwitchHandler);
            this.mImsPhones[i].unregisterForPreciseCallStateChanged(this.mDdsSwitchHandler);
        }
    }

    private boolean isFeatureEnabled() {
        this.isPropertyEnabled = SystemProperties.getBoolean("persist.radio.enable_temp_dds", false);
        log("isPropertyEnabled: " + this.isPropertyEnabled);
        return this.isPropertyEnabled;
    }

    private boolean isCallActive(int phoneId) {
        if (this.mFgCsCalls[phoneId].getState() == State.ACTIVE || this.mBgCsCalls[phoneId].getState() == State.ACTIVE || this.mRiCsCalls[phoneId].getState() == State.ACTIVE || this.mFgImsCalls[phoneId].getState() == State.ACTIVE || this.mBgImsCalls[phoneId].getState() == State.ACTIVE || this.mRiImsCalls[phoneId].getState() == State.ACTIVE) {
            return true;
        }
        return false;
    }

    private boolean isCallIdle(int phoneId) {
        return this.mFgCsCalls[phoneId].isIdle() && this.mBgCsCalls[phoneId].isIdle() && this.mRiCsCalls[phoneId].isIdle() && this.mFgImsCalls[phoneId].getState() == State.IDLE && this.mBgImsCalls[phoneId].getState() == State.IDLE && this.mRiImsCalls[phoneId].getState() == State.IDLE;
    }

    private boolean isCallOnNonDds(int phoneId) {
        return phoneId != this.mSubscriptionController.getPhoneId(this.mUserDdsSubId);
    }

    private void processCallStateChanged(int phoneId) {
        if (!this.mIsCallActive && isCallActive(phoneId)) {
            log("notifyCallStateChanged: call active on non dds");
            this.mIsCallActive = true;
            onCallStarted(phoneId);
        } else if (isCallIdle(phoneId)) {
            this.mNotifyCallState = false;
            if (this.mIsCallActive) {
                log("notifyCallStateChanged: call disconnected on non dds");
                this.mIsCallActive = false;
                onCallEnded(phoneId);
            }
        } else {
            log("ignore call state change");
        }
    }

    protected void log(String l) {
        Rlog.d("QtiDdsSwitchController", l);
    }
}
