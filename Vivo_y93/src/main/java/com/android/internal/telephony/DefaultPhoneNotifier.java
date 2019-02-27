package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.CellInfo;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VoLteServiceState;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.ITelephonyRegistry.Stub;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneInternalInterface.DataActivityState;
import java.util.List;

public class DefaultPhoneNotifier implements PhoneNotifier {
    /* renamed from: -com-android-internal-telephony-Call$StateSwitchesValues */
    private static final /* synthetic */ int[] f4-com-android-internal-telephony-Call$StateSwitchesValues = null;
    /* renamed from: -com-android-internal-telephony-PhoneInternalInterface$DataActivityStateSwitchesValues */
    private static final /* synthetic */ int[] f5xb8401fb4 = null;
    private static final boolean DBG = false;
    private static final String LOG_TAG = "DefaultPhoneNotifier";
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private Handler mHandler = new Handler(PhoneFactory.getCommonLooper());
    protected ITelephonyRegistry mRegistry = Stub.asInterface(ServiceManager.getService("telephony.registry"));

    /* renamed from: -getcom-android-internal-telephony-Call$StateSwitchesValues */
    private static /* synthetic */ int[] m4-getcom-android-internal-telephony-Call$StateSwitchesValues() {
        if (f4-com-android-internal-telephony-Call$StateSwitchesValues != null) {
            return f4-com-android-internal-telephony-Call$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.ACTIVE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.ALERTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DIALING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.DISCONNECTED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.DISCONNECTING.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.HOLDING.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.IDLE.ordinal()] = 13;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[State.INCOMING.ordinal()] = 7;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[State.WAITING.ordinal()] = 8;
        } catch (NoSuchFieldError e9) {
        }
        f4-com-android-internal-telephony-Call$StateSwitchesValues = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-internal-telephony-PhoneInternalInterface$DataActivityStateSwitchesValues */
    private static /* synthetic */ int[] m5x2c473d58() {
        if (f5xb8401fb4 != null) {
            return f5xb8401fb4;
        }
        int[] iArr = new int[DataActivityState.values().length];
        try {
            iArr[DataActivityState.DATAIN.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DataActivityState.DATAINANDOUT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DataActivityState.DATAOUT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DataActivityState.DORMANT.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[DataActivityState.NONE.ordinal()] = 13;
        } catch (NoSuchFieldError e5) {
        }
        f5xb8401fb4 = iArr;
        return iArr;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifyPhoneState(final Phone sender) {
        Call ringingCall = sender.getRingingCall();
        int subId = sender.getSubId();
        int phoneId = sender.getPhoneId();
        String incomingNumber = "";
        if (!(ringingCall == null || ringingCall.getEarliestConnection() == null)) {
            incomingNumber = ringingCall.getEarliestConnection().getAddress();
        }
        final int stateNotify = PhoneConstantConversions.convertCallState(sender.getState());
        final String incomingNumberNotify = incomingNumber;
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    if (DefaultPhoneNotifier.this.mRegistry != null) {
                        int phone = sender.getPhoneId();
                        int sub = sender.getSubId();
                        Rlog.d(DefaultPhoneNotifier.LOG_TAG, "notifyPhoneState phone = " + phone + " sub = " + sub);
                        DefaultPhoneNotifier.this.mRegistry.notifyCallStateForPhoneId(phone, sub, stateNotify, incomingNumberNotify);
                    }
                } catch (RemoteException e) {
                }
            }
        });
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifyServiceState(final Phone sender) {
        ServiceState ss = sender.getServiceState();
        int phoneId = sender.getPhoneId();
        Rlog.d(LOG_TAG, "nofityServiceState: mRegistry=" + this.mRegistry + " ss=" + ss + " sender=" + sender + " phondId=" + phoneId + " subId=" + sender.getSubId());
        if (ss == null) {
            ss = new ServiceState();
            ss.setStateOutOfService();
        }
        final ServiceState ssNotify = new ServiceState(ss);
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    if (DefaultPhoneNotifier.this.mRegistry != null) {
                        int phone = sender.getPhoneId();
                        int sub = sender.getSubId();
                        Rlog.d(DefaultPhoneNotifier.LOG_TAG, "nofityServiceState phone = " + phone + " sub = " + sub);
                        DefaultPhoneNotifier.this.mRegistry.notifyServiceStateForPhoneId(phone, sub, ssNotify);
                    }
                } catch (RemoteException e) {
                }
            }
        });
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifySignalStrength(final Phone sender) {
        int phoneId = sender.getPhoneId();
        int subId = sender.getSubId();
        final SignalStrength ssNotify = sender.getSignalStrength();
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    if (DefaultPhoneNotifier.this.mRegistry != null) {
                        int phone = sender.getPhoneId();
                        int sub = sender.getSubId();
                        Rlog.d(DefaultPhoneNotifier.LOG_TAG, "notifySignalStrength phone = " + phone + " sub = " + sub);
                        DefaultPhoneNotifier.this.mRegistry.notifySignalStrengthForPhoneId(phone, sub, ssNotify);
                    }
                } catch (RemoteException e) {
                }
            }
        });
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifyMessageWaitingChanged(final Phone sender) {
        int phoneId = sender.getPhoneId();
        int subId = sender.getSubId();
        final boolean indNotify = sender.getMessageWaitingIndicator();
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    if (DefaultPhoneNotifier.this.mRegistry != null) {
                        int phone = sender.getPhoneId();
                        int sub = sender.getSubId();
                        Rlog.d(DefaultPhoneNotifier.LOG_TAG, "notifyMessageWaitingChanged phone = " + phone + " sub = " + sub);
                        DefaultPhoneNotifier.this.mRegistry.notifyMessageWaitingChangedForPhoneId(phone, sub, indNotify);
                    }
                } catch (RemoteException e) {
                }
            }
        });
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifyCallForwardingChanged(final Phone sender) {
        int subId = sender.getSubId();
        final boolean indNotify = sender.getCallForwardingIndicator();
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    if (DefaultPhoneNotifier.this.mRegistry != null) {
                        int phone = sender.getPhoneId();
                        int sub = sender.getSubId();
                        Rlog.d(DefaultPhoneNotifier.LOG_TAG, "notifyCallForwardingChanged: subId=" + sub + ", isCFActive=" + indNotify);
                        DefaultPhoneNotifier.this.mRegistry.notifyCallForwardingChangedForSubscriber(sub, indNotify);
                    }
                } catch (RemoteException e) {
                }
            }
        });
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifyDataActivity(final Phone sender) {
        int subId = sender.getSubId();
        final int stateNotify = convertDataActivityState(sender.getDataActivityState());
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    if (DefaultPhoneNotifier.this.mRegistry != null) {
                        int phone = sender.getPhoneId();
                        int sub = sender.getSubId();
                        Rlog.d(DefaultPhoneNotifier.LOG_TAG, "notifyDataActivity phone = " + phone + " sub = " + sub);
                        DefaultPhoneNotifier.this.mRegistry.notifyDataActivityForSubscriber(sub, stateNotify);
                    }
                } catch (RemoteException e) {
                }
            }
        });
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifyDataConnection(Phone sender, String reason, String apnType, DataState state) {
        doNotifyDataConnection(sender, reason, apnType, state);
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private void doNotifyDataConnection(Phone sender, String reason, String apnType, DataState state) {
        int dataTypeNotify;
        final int subId = sender.getSubId();
        long dds = (long) SubscriptionManager.getDefaultDataSubscriptionId();
        TelephonyManager telephony = TelephonyManager.getDefault();
        LinkProperties linkProperties = null;
        NetworkCapabilities networkCapabilities = null;
        boolean roaming = false;
        if (state == DataState.CONNECTED) {
            linkProperties = sender.getLinkProperties(apnType);
            networkCapabilities = sender.getNetworkCapabilities(apnType);
        }
        ServiceState ss = sender.getServiceState();
        if (ss != null) {
            roaming = ss.getDataRoaming();
        }
        final boolean dataAllowed = sender.isDataAllowed();
        final String activeApnHost = sender.getActiveApnHost(apnType);
        if (telephony != null) {
            dataTypeNotify = telephony.getDataNetworkType(subId);
        } else {
            dataTypeNotify = 0;
        }
        final int stateNotify = PhoneConstantConversions.convertDataState(state);
        final LinkProperties linkPropertiesNotify = linkProperties;
        final NetworkCapabilities networkCapabilitiesNotify = networkCapabilities;
        final boolean roamingNotify = roaming;
        final String str = reason;
        final String str2 = apnType;
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    if (DefaultPhoneNotifier.this.mRegistry != null) {
                        DefaultPhoneNotifier.this.mRegistry.notifyDataConnectionForSubscriber(subId, stateNotify, dataAllowed, str, activeApnHost, str2, linkPropertiesNotify, networkCapabilitiesNotify, dataTypeNotify, roamingNotify);
                    }
                } catch (RemoteException e) {
                }
            }
        });
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifyDataConnectionFailed(Phone sender, final String reason, final String apnType) {
        final int subId = sender.getSubId();
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    if (DefaultPhoneNotifier.this.mRegistry != null) {
                        DefaultPhoneNotifier.this.mRegistry.notifyDataConnectionFailedForSubscriber(subId, reason, apnType);
                    }
                } catch (RemoteException e) {
                }
            }
        });
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifyCellLocation(final Phone sender) {
        int subId = sender.getSubId();
        final Bundle data = new Bundle();
        sender.getCellLocation().fillInNotifierBundle(data);
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    if (DefaultPhoneNotifier.this.mRegistry != null) {
                        int phone = sender.getPhoneId();
                        DefaultPhoneNotifier.this.mRegistry.notifyCellLocationForSubscriber(sender.getSubId(), data);
                    }
                } catch (RemoteException e) {
                }
            }
        });
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifyCellInfo(final Phone sender, final List<CellInfo> cellInfo) {
        int subId = sender.getSubId();
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    if (DefaultPhoneNotifier.this.mRegistry != null) {
                        int phone = sender.getPhoneId();
                        DefaultPhoneNotifier.this.mRegistry.notifyCellInfoForSubscriber(sender.getSubId(), cellInfo);
                    }
                } catch (RemoteException e) {
                }
            }
        });
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifyOtaspChanged(Phone sender, final int otaspMode) {
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    if (DefaultPhoneNotifier.this.mRegistry != null) {
                        DefaultPhoneNotifier.this.mRegistry.notifyOtaspChanged(otaspMode);
                    }
                } catch (RemoteException e) {
                }
            }
        });
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifyPreciseCallState(Phone sender) {
        Call ringingCall = sender.getRingingCall();
        Call foregroundCall = sender.getForegroundCall();
        Call backgroundCall = sender.getBackgroundCall();
        if (ringingCall != null && foregroundCall != null && backgroundCall != null) {
            final int ringNotify = convertPreciseCallState(ringingCall.getState());
            final int foreNotify = convertPreciseCallState(foregroundCall.getState());
            final int backNotify = convertPreciseCallState(backgroundCall.getState());
            this.mHandler.post(new Runnable() {
                public void run() {
                    try {
                        DefaultPhoneNotifier.this.mRegistry.notifyPreciseCallState(ringNotify, foreNotify, backNotify);
                    } catch (RemoteException e) {
                    }
                }
            });
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifyDisconnectCause(final int cause, final int preciseCause) {
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    DefaultPhoneNotifier.this.mRegistry.notifyDisconnectCause(cause, preciseCause);
                } catch (RemoteException e) {
                }
            }
        });
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifyPreciseDataConnectionFailed(Phone sender, String reason, String apnType, String apn, String failCause) {
        final String str = reason;
        final String str2 = apnType;
        final String str3 = apn;
        final String str4 = failCause;
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    DefaultPhoneNotifier.this.mRegistry.notifyPreciseDataConnectionFailed(str, str2, str3, str4);
                } catch (RemoteException e) {
                }
            }
        });
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifyVoLteServiceStateChanged(Phone sender, final VoLteServiceState lteState) {
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    DefaultPhoneNotifier.this.mRegistry.notifyVoLteServiceStateChanged(lteState);
                } catch (RemoteException e) {
                }
            }
        });
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifyDataActivationStateChanged(Phone sender, final int activationState) {
        final int phoneIdNotify = sender.getPhoneId();
        final int subIdNotify = sender.getSubId();
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    DefaultPhoneNotifier.this.mRegistry.notifySimActivationStateChangedForPhoneId(phoneIdNotify, subIdNotify, 1, activationState);
                } catch (RemoteException e) {
                }
            }
        });
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void notifyVoiceActivationStateChanged(Phone sender, final int activationState) {
        final int phoneIdNotify = sender.getPhoneId();
        final int subIdNotify = sender.getSubId();
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    DefaultPhoneNotifier.this.mRegistry.notifySimActivationStateChangedForPhoneId(phoneIdNotify, subIdNotify, 0, activationState);
                } catch (RemoteException e) {
                }
            }
        });
    }

    public void notifyOemHookRawEventForSubscriber(final int subId, final byte[] rawData) {
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    DefaultPhoneNotifier.this.mRegistry.notifyOemHookRawEventForSubscriber(subId, rawData);
                } catch (RemoteException e) {
                }
            }
        });
    }

    public static int convertDataActivityState(DataActivityState state) {
        switch (m5x2c473d58()[state.ordinal()]) {
            case 1:
                return 1;
            case 2:
                return 3;
            case 3:
                return 2;
            case 4:
                return 4;
            default:
                return 0;
        }
    }

    public static int convertPreciseCallState(State state) {
        switch (m4-getcom-android-internal-telephony-Call$StateSwitchesValues()[state.ordinal()]) {
            case 1:
                return 1;
            case 2:
                return 4;
            case 3:
                return 3;
            case 4:
                return 7;
            case 5:
                return 8;
            case 6:
                return 2;
            case 7:
                return 5;
            case 8:
                return 6;
            default:
                return 0;
        }
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }
}
