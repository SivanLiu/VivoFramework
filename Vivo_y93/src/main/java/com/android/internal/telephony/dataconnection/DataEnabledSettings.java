package com.android.internal.telephony.dataconnection;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.os.Handler;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.Pair;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyPhoneUtils;

public class DataEnabledSettings {
    public static final int REASON_DATA_ENABLED_BY_CARRIER = 4;
    public static final int REASON_INTERNAL_DATA_ENABLED = 1;
    public static final int REASON_POLICY_DATA_ENABLED = 3;
    public static final int REASON_REGISTERED = 0;
    public static final int REASON_USER_DATA_ENABLED = 2;
    private boolean mCarrierDataEnabled = true;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private boolean mChildrenModeDataEnable = false;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private boolean mChildrenModeEnable = false;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private int mCtDataBlock = 1;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private int mCtDataConnect = 1;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private int mCtNetworkDataBolckData = 1;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private int mCtNetworkDataBolckSim = 0;
    private final RegistrantList mDataEnabledChangedRegistrants = new RegistrantList();
    private boolean mInternalDataEnabled = true;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private int mNCtDataBlock = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private int mNetworkData = 1;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private Phone mPhone;
    private boolean mPolicyDataEnabled = true;
    private boolean mUserDataEnabled = true;

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public synchronized void setChildrenModeEnabled(boolean enabled) {
        this.mChildrenModeEnable = enabled;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public synchronized boolean getChildrenModeEnabled() {
        return this.mChildrenModeEnable;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public synchronized void setChildrenModeDataEnabled(boolean enabled) {
        this.mChildrenModeDataEnable = enabled;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public synchronized boolean getChildrenModeDataEnabled() {
        return this.mChildrenModeDataEnable;
    }

    public String toString() {
        return "[mInternalDataEnabled=" + this.mInternalDataEnabled + ", mUserDataEnabled=" + this.mUserDataEnabled + ", mPolicyDataEnabled=" + this.mPolicyDataEnabled + ", mCarrierDataEnabled=" + this.mCarrierDataEnabled + "]";
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public synchronized void setPhone(Phone phone) {
        this.mPhone = phone;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public synchronized void setCtNetworkData(int mode) {
        this.mNetworkData = mode;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public synchronized void setCtNetworkDataBolckSim(int mode) {
        this.mCtNetworkDataBolckSim = mode;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public synchronized void setCtNetworkDataBolckData(int mode) {
        this.mCtNetworkDataBolckData = mode;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public synchronized void setCtDataBlock(int mode) {
        this.mCtDataBlock = mode;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public synchronized void setCtDataConnect(int mode) {
        this.mCtDataConnect = mode;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public synchronized void setNCtDataBlock(int mode) {
        this.mNCtDataBlock = mode;
    }

    public synchronized void setInternalDataEnabled(boolean enabled) {
        boolean prevDataEnabled = isDataEnabled();
        this.mInternalDataEnabled = enabled;
        if (prevDataEnabled != isDataEnabled()) {
            notifyDataEnabledChanged(prevDataEnabled ^ 1, 1);
        }
    }

    public synchronized boolean isInternalDataEnabled() {
        return this.mInternalDataEnabled;
    }

    public synchronized void setUserDataEnabled(boolean enabled) {
        boolean prevDataEnabled = isDataEnabled();
        this.mUserDataEnabled = enabled;
        if (prevDataEnabled != isDataEnabled()) {
            notifyDataEnabledChanged(prevDataEnabled ^ 1, 2);
        }
    }

    public synchronized boolean isUserDataEnabled() {
        return this.mUserDataEnabled;
    }

    public synchronized void setPolicyDataEnabled(boolean enabled) {
        boolean prevDataEnabled = isDataEnabled();
        this.mPolicyDataEnabled = enabled;
        if (prevDataEnabled != isDataEnabled()) {
            notifyDataEnabledChanged(prevDataEnabled ^ 1, 3);
        }
    }

    public synchronized boolean isPolicyDataEnabled() {
        return this.mPolicyDataEnabled;
    }

    public synchronized void setCarrierDataEnabled(boolean enabled) {
        boolean prevDataEnabled = isDataEnabled();
        this.mCarrierDataEnabled = enabled;
        if (prevDataEnabled != isDataEnabled()) {
            notifyDataEnabledChanged(prevDataEnabled ^ 1, 4);
        }
    }

    public synchronized boolean isCarrierDataEnabled() {
        return this.mCarrierDataEnabled;
    }

    /* JADX WARNING: Missing block: B:57:0x00ee, code:
            if (r10.mCtNetworkDataBolckSim == 2) goto L_0x00de;
     */
    /* JADX WARNING: Missing block: B:68:0x0104, code:
            return r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public synchronized boolean isDataEnabled() {
        boolean z = false;
        synchronized (this) {
            Rlog.d("DataEnabledSettings", "isDataEnabled mNetworkData = " + this.mNetworkData + " mCtNetworkDataBolckSim = " + this.mCtNetworkDataBolckSim + " mCtNetworkDataBolckData = " + this.mCtNetworkDataBolckData);
            if ("1".equals(SystemProperties.get("ro.build.gn.support", "0")) && this.mPhone != null) {
                String imsi = this.mPhone.getOperatorNumeric();
                Rlog.d("DataEnabledSettings", "isDataAllowedForCtccGN imsi = " + imsi + " mCtDataBlock = " + this.mCtDataBlock + " mCtDataConnect = " + this.mCtDataConnect + " mNCtDataBlock " + this.mNCtDataBlock);
                if (!TextUtils.isEmpty(imsi)) {
                    boolean isCtcc = TelephonyPhoneUtils.getOperatorTypeByImsi(imsi) == 2;
                    if (this.mCtDataBlock == 0 && isCtcc) {
                        return false;
                    } else if (this.mCtDataBlock == 2 && isCtcc) {
                        return true;
                    } else if (this.mNCtDataBlock == 0 && (isCtcc ^ 1) != 0) {
                        return false;
                    }
                }
            } else if (this.mNetworkData == 0) {
                return false;
            } else if (this.mNetworkData == 4) {
                return true;
            } else {
                if (!(this.mCtNetworkDataBolckSim == 0 || (this.mPhone.getPhoneId() == 0 && this.mCtNetworkDataBolckSim == 1))) {
                    if (this.mPhone.getPhoneId() == 1) {
                    }
                }
                if (this.mCtNetworkDataBolckData == 0) {
                    return false;
                }
            }
            boolean dataEnable;
            if (this.mChildrenModeEnable) {
                dataEnable = this.mChildrenModeDataEnable;
            } else {
                dataEnable = this.mUserDataEnabled;
            }
            if (this.mInternalDataEnabled && dataEnable && this.mPolicyDataEnabled) {
                z = this.mCarrierDataEnabled;
            }
        }
    }

    private void notifyDataEnabledChanged(boolean enabled, int reason) {
        this.mDataEnabledChangedRegistrants.notifyResult(new Pair(Boolean.valueOf(enabled), Integer.valueOf(reason)));
    }

    public void registerForDataEnabledChanged(Handler h, int what, Object obj) {
        this.mDataEnabledChangedRegistrants.addUnique(h, what, obj);
        notifyDataEnabledChanged(isDataEnabled(), 0);
    }

    public void unregisterForDataEnabledChanged(Handler h) {
        this.mDataEnabledChangedRegistrants.remove(h);
    }
}
