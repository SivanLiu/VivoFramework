package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import com.android.internal.telephony.IccCardConstants.State;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoServiceStateTrackerHelper extends Handler {
    private static final int DELAY_OOS_TIME = 20000;
    private static final int DELAY_TIME = 2000;
    private static final int EVENT_NOTIFY_DELAY = 1;
    private static final int EVENT_NOTIFY_OOS_DELAY = 2;
    private static final int EVENT_SET_DDS_OR_NW = 3;
    static final String TAG = "VivoServiceStateTrackerHelper";
    private BroadcastReceiver mDdsOrNwIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            VivoServiceStateTrackerHelper.this.log("SET_DDS_NW_FLAG,reason:" + intent.getStringExtra("reason"));
            VivoServiceStateTrackerHelper.this.mIsChangingDdsOrNw = true;
            if (VivoServiceStateTrackerHelper.this.hasMessages(3)) {
                VivoServiceStateTrackerHelper.this.removeMessages(3);
            }
            VivoServiceStateTrackerHelper.this.sendMessageDelayed(VivoServiceStateTrackerHelper.this.obtainMessage(3), 20000);
        }
    };
    private boolean mHasDelayNotify;
    private boolean mIsChangingDdsOrNw;
    private GsmCdmaPhone mPhone;
    private ServiceState mSS;
    private Intent mSpnUpdateIntent;
    private long mTime;

    public VivoServiceStateTrackerHelper(GsmCdmaPhone phone) {
        this.mPhone = phone;
        this.mTime = SystemClock.elapsedRealtime();
        this.mSS = new ServiceState();
        this.mHasDelayNotify = false;
        this.mIsChangingDdsOrNw = false;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SET_DDS_NW_FLAG");
        this.mPhone.getContext().registerReceiver(this.mDdsOrNwIntentReceiver, filter);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                if (!hasMessages(2)) {
                    log("EVENT_NOTIFY_DELAY timeout!");
                    resetValue();
                    notifyServiceStateChangedEx(this.mSS);
                    return;
                }
                return;
            case 2:
                log("EVENT_NOTIFY_OOS_DELAY timeout!");
                resetValue();
                notifyServiceStateChangedEx(this.mSS);
                return;
            case 3:
                log("EVENT_SET_DDS_OR_NW timeout!");
                this.mIsChangingDdsOrNw = false;
                return;
            default:
                return;
        }
    }

    public void notifyServiceStateChanged(ServiceState ss) {
        if (ss == null) {
            log("notifyServiceStateChanged ss is null.");
        } else {
            handerDelayUpdateOOS(ss);
        }
    }

    public void updateSpnDisplay(Intent spnUpdateIntent, boolean forceUpdate) {
        if (forceUpdate || (this.mHasDelayNotify ^ 1) != 0) {
            log("updatespndisplay");
            this.mPhone.getContext().sendStickyBroadcastAsUser(spnUpdateIntent, UserHandle.ALL);
            this.mSpnUpdateIntent = null;
            return;
        }
        this.mSpnUpdateIntent = spnUpdateIntent;
    }

    public boolean canUpdateSpnToCurrent() {
        return this.mSpnUpdateIntent == null;
    }

    private void handleServiceStateChanged(ServiceState ss) {
        boolean bNotifyDelay = false;
        if (hasMessages(1)) {
            removeMessages(1);
        }
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - this.mTime < 2000 && this.mSS.getDataNetworkType() == 19 && ss.getDataNetworkType() == 13) {
            bNotifyDelay = true;
        }
        log("handleServiceStateChanged bNotifyDelay = " + bNotifyDelay + " time = " + this.mTime);
        this.mTime = currentTime;
        this.mSS = new ServiceState(ss);
        if (bNotifyDelay) {
            sendMessageDelayed(obtainMessage(1), 2000);
            return;
        }
        resetValue();
        notifyServiceStateChangedEx(this.mSS);
    }

    private void handerDelayUpdateOOS(ServiceState newSs) {
        boolean isSimReady = true;
        IccCard icc = this.mPhone.getIccCard();
        if (icc != null) {
            isSimReady = icc.getState() == State.READY;
        }
        boolean noNeedDaley = (!TelephonyPhoneUtils.sIsCMCCEntry && (this.mPhone.mCi.getRadioState().isOn() ^ 1) == 0 && (isSimReady ^ 1) == 0) ? this.mIsChangingDdsOrNw : true;
        boolean bNotifyDelayOOS = false;
        if (!noNeedDaley) {
            if (getOemRegState(this.mSS) == 0 && getOemRegState(newSs) != 0) {
                bNotifyDelayOOS = true;
            }
            if (!(getOemRegState(this.mSS) == 0 || getOemRegState(newSs) == 0 || !this.mHasDelayNotify)) {
                log("handerDelayUpdateOOS: HasDelayNotify and the DELAY_OOS_TIME has not Timeout!");
                this.mSS = new ServiceState(newSs);
                return;
            }
        }
        log("handerDelayUpdateOOS bNotifyDelayOOS = " + bNotifyDelayOOS + " mHasDelayNotify = " + this.mHasDelayNotify);
        if (bNotifyDelayOOS) {
            this.mHasDelayNotify = true;
            this.mSS = new ServiceState(newSs);
            sendMessageDelayed(obtainMessage(2), 20000);
        } else {
            handleServiceStateChanged(newSs);
        }
    }

    protected int getOemRegState(ServiceState serviceState) {
        int voiceRegState = serviceState.getVoiceRegState();
        int dataRegState = serviceState.getDataRegState();
        int oemRegState = voiceRegState;
        if (voiceRegState == 0 || dataRegState != 0) {
            return oemRegState;
        }
        return dataRegState;
    }

    public void dispose() {
        if (hasMessages(1)) {
            removeMessages(1);
        }
        if (hasMessages(2)) {
            removeMessages(2);
        }
        if (hasMessages(3)) {
            removeMessages(3);
        }
        notifyServiceStateChangedEx(this.mSS);
        this.mSS = new ServiceState();
        this.mHasDelayNotify = false;
        this.mIsChangingDdsOrNw = false;
        this.mSpnUpdateIntent = null;
        this.mPhone.getContext().unregisterReceiver(this.mDdsOrNwIntentReceiver);
    }

    public void resetValue() {
        this.mHasDelayNotify = false;
        if (hasMessages(1)) {
            removeMessages(1);
        }
        if (hasMessages(2)) {
            removeMessages(2);
        }
    }

    private void notifyServiceStateChangedEx(ServiceState ss) {
        log("notifyServiceStateChangedEx, Ss = " + ss);
        this.mPhone.notifyServiceStateChanged(ss);
        if (this.mSpnUpdateIntent != null) {
            log("updatespndisplay");
            this.mPhone.getContext().sendStickyBroadcastAsUser(this.mSpnUpdateIntent, UserHandle.ALL);
            this.mSpnUpdateIntent = null;
        }
    }

    private void resetToDefaultSs(ServiceState state) {
        if (hasMessages(1)) {
            removeMessages(1);
        }
        if (hasMessages(2)) {
            removeMessages(2);
        }
        this.mTime = SystemClock.elapsedRealtime();
        this.mSS = new ServiceState(state);
        this.mHasDelayNotify = false;
    }

    public boolean isShowIroamingDialog(String isoCountryCode) {
        if (!TelephonyPhoneUtils.isAllowIroaming() || TelephonyPhoneUtils.getInsertSimMode() == 0) {
            return false;
        }
        if (TelephonyPhoneUtils.isOnlyChinaCardInsert()) {
            return true;
        }
        if (TelephonyPhoneUtils.hasNoChinaCardInsert()) {
            return (isoCountryCode.equals(MccTable.countryCodeForMcc(TelephonyPhoneUtils.getCardMcc(0))) || (isoCountryCode.equals(MccTable.countryCodeForMcc(TelephonyPhoneUtils.getCardMcc(1))) ^ 1) == 0) ? false : true;
        }
    }

    private void log(String str) {
        Rlog.d(TAG + this.mPhone.getPhoneId(), str);
    }
}
