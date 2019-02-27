package com.vivo.services.sarpower;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import com.vivo.services.rms.ProcessList;
import java.util.ArrayList;
import vivo.app.sarpower.IVivoSarPowerState.Stub;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoSarPowerStateService extends Stub {
    private static final String ACTION_BOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED";
    private static final String ACTION_CARD_CHANGE = "android.intent.action.SIM_STATE_CHANGED";
    private static final String ACTION_SAR_POWER_TEST = "android.intent.action.sar_test";
    private static final String ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
    private static final String ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON";
    protected static final int MSG_SAR_BOOT_COMPLETED = 1;
    private static final int MSG_SAR_EVENT_UPDATE_CARD = 0;
    protected static final int MSG_SAR_POWER_CHANGE = 0;
    protected static final int MSG_SAR_UPDATE_PARAM = 2;
    private static final int PLATFORM_MTK = 2;
    private static final int PLATFORM_QCOM = 1;
    private static final int PLATFORM_UNKNOWN = 0;
    private static final String SAR_POWER_STATE_TEST_PARMNAME = "powerState";
    private static final String SAR_POWER_UEVENT_MATCH = "DEVPATH=/devices/virtual/switch/sar-power";
    private static final String SAR_POWER_UEVENT_MATCH_SECOND = "DEVPATH=/devices/virtual/sarpower/sar-power";
    protected static final int SCREEN_STATE_OFF = 0;
    protected static final int SCREEN_STATE_ON = 1;
    private static final int SIM_CARD_ONE_ID = 0;
    private static final int SIM_CARD_TWO_ID = 1;
    private static final boolean SUPPORT_SAR_POWER = SystemProperties.get("persist.vivo.phone.sarpower", "no").equals("Have_sarpower");
    private static final String TAG = "SarPowerStateService";
    private static Context mContext;
    private static final String[] mSarPowerRootChangedList = new String[]{"pd1805", "pd1806", "pd1809", "pd1818", "td1803"};
    private static HandlerThread mThread;
    private static final String model = SystemProperties.get("ro.vivo.product.model", "unkown").toLowerCase();
    private static final String platform = SystemProperties.get("ro.vivo.product.solution", "unkown").toLowerCase();
    private final BroadcastReceiver mBootCompleteReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.d(VivoSarPowerStateService.TAG, "mBootCompleteReceiver action:" + action);
            if (VivoSarPowerStateService.ACTION_BOOT_COMPLETE.equals(action)) {
                VivoSarPowerStateService.this.mSarPowerObserver = new SarPowerObserver();
                VivoSarPowerStateService.this.initialPowerState();
                VivoSarPowerStateService.this.mVivoSarPowerStateController.notifyBootCompleted();
                VivoSarPowerStateService.this.mVivoSarPowerStateController.handleSarMessage(0, 1500);
            }
        }
    };
    private final BroadcastReceiver mCardChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (VivoSarPowerStateService.ACTION_CARD_CHANGE.equals(intent.getAction())) {
                int phoneId = intent.getIntExtra("phone", -1);
                String iccStateExtra = intent.getStringExtra("ss");
                Slog.d(VivoSarPowerStateService.TAG, "mCardChangeReceiver: phoneId " + phoneId + ",iccState:" + iccStateExtra);
                Message msg = VivoSarPowerStateService.this.mVivoSarPowerHandler.obtainMessage();
                msg.what = 0;
                Bundle bundle = new Bundle();
                bundle.putInt("phoneId", phoneId);
                bundle.putString("iccState", iccStateExtra);
                msg.setData(bundle);
                VivoSarPowerStateService.this.mVivoSarPowerHandler.sendMessage(msg);
            }
        }
    };
    private Looper mMainLooper;
    private Sensor mProximitySensor = null;
    private SensorEventListener mProximitySensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            int proximityState = (int) event.values[0];
            Slog.d(VivoSarPowerStateService.TAG, "prox event: " + proximityState);
            VivoSarPowerStateService.this.mVivoSarPowerStateController.notifyProxmityState(proximityState);
            VivoSarPowerStateService.this.mVivoSarPowerStateController.handleSarMessage(0, 0);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private SarPowerObserver mSarPowerObserver;
    private final BroadcastReceiver mSarPowerTestReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.d(VivoSarPowerStateService.TAG, "mSarPowerTestReceiver action:" + action);
            if (VivoSarPowerStateService.ACTION_SAR_POWER_TEST.equals(action)) {
                int tmpPowerState = intent.getIntExtra(VivoSarPowerStateService.SAR_POWER_STATE_TEST_PARMNAME, -1);
                Slog.d(VivoSarPowerStateService.TAG, "receiver the powerState:" + tmpPowerState);
                VivoSarPowerStateService.this.mVivoSarPowerStateController.notifySarPowerTest(tmpPowerState);
            }
        }
    };
    private final BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.d(VivoSarPowerStateService.TAG, "mScreenOffReceiver action:" + action);
            if (VivoSarPowerStateService.ACTION_SCREEN_OFF.equals(action)) {
                VivoSarPowerStateService.this.mVivoSarPowerStateController.notifyScreenState(0);
                VivoSarPowerStateService.this.mVivoSarPowerStateController.handleSarMessage(0, 0);
            }
        }
    };
    private final BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.d(VivoSarPowerStateService.TAG, "mScreenOnReceiver action:" + action);
            if (VivoSarPowerStateService.ACTION_SCREEN_ON.equals(action)) {
                VivoSarPowerStateService.this.mVivoSarPowerStateController.notifyScreenState(1);
                VivoSarPowerStateService.this.mVivoSarPowerStateController.handleSarMessage(0, 0);
            }
        }
    };
    private SensorManager mSensorManager;
    private int mTargetPlatformInfo = 0;
    private TelephonyManager mTelephonyManager;
    private VivoSarPowerHandler mVivoSarPowerHandler;
    private VivoSarPowerStateController mVivoSarPowerStateController = null;

    private class SarPowerObserver extends UEventObserver {
        private static final String TAG = "SarPowerStateService";

        public SarPowerObserver() {
            int mSarPowerRfDetectState = VivoSarPowerStateService.nativeInitSarPowerState();
            VivoSarPowerStateService.this.mVivoSarPowerStateController.notifySarPowerRfDetectState(mSarPowerRfDetectState);
            VivoSarPowerStateService.this.sarPowerSwitchEnable(1);
            Slog.d(TAG, "init mSarPowerRfDetectState = " + mSarPowerRfDetectState);
            if (VivoSarPowerStateService.this.isSarPowerRootChanged()) {
                startObserving(VivoSarPowerStateService.SAR_POWER_UEVENT_MATCH_SECOND);
            } else {
                startObserving(VivoSarPowerStateService.SAR_POWER_UEVENT_MATCH);
            }
        }

        public void onUEvent(UEvent event) {
            try {
                int mSarPowerRfDetectState = Integer.parseInt(event.get("SWITCH_STATE"));
                VivoSarPowerStateService.this.mVivoSarPowerStateController.notifySarPowerRfDetectState(mSarPowerRfDetectState);
                VivoSarPowerStateService.this.mVivoSarPowerStateController.handleSarMessage(0, 0);
                Slog.d(TAG, "onUEvent mSarPowerRfDetectState = " + mSarPowerRfDetectState);
            } catch (NumberFormatException e) {
                Slog.d(TAG, "onUEvent e:" + e);
            }
        }
    }

    private final class VivoSarPowerHandler extends Handler {
        public VivoSarPowerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Bundle bundle = msg.getData();
                    int phoneId = bundle.getInt("phoneId");
                    String iccStateExtra = bundle.getString("iccState");
                    Slog.d(VivoSarPowerStateService.TAG, "VivoSarPowerHandler: phoneId " + phoneId + ",iccState:" + iccStateExtra);
                    if (VivoSarPowerStateService.this.mTargetPlatformInfo == 1 && "IMSI".equals(iccStateExtra)) {
                        if ((VivoSarPowerStateService.this.isWhiteCard(phoneId) ? VivoSarPowerStateService.this.isVSim(phoneId) ^ 1 : 0) != 0) {
                            if (phoneId == 0) {
                                VivoSarPowerStateService.this.mVivoSarPowerStateController.notifyCardState(true, 1);
                            } else if (1 == phoneId) {
                                VivoSarPowerStateService.this.mVivoSarPowerStateController.notifyCardState(false, 1);
                            }
                        } else if (phoneId == 0) {
                            VivoSarPowerStateService.this.mVivoSarPowerStateController.notifyCardState(true, 0);
                        } else if (1 == phoneId) {
                            VivoSarPowerStateService.this.mVivoSarPowerStateController.notifyCardState(false, 0);
                        }
                    } else if (VivoSarPowerStateService.this.mTargetPlatformInfo == 2 && "LOADED".equals(iccStateExtra)) {
                        if ((VivoSarPowerStateService.this.isWhiteCard(phoneId) ? VivoSarPowerStateService.isVSim(VivoSarPowerStateService.this.getSimIccid(phoneId)) ^ 1 : 0) != 0) {
                            if (phoneId == 0) {
                                VivoSarPowerStateService.this.mVivoSarPowerStateController.notifyCardState(true, 1);
                            } else if (1 == phoneId) {
                                VivoSarPowerStateService.this.mVivoSarPowerStateController.notifyCardState(false, 1);
                            }
                        } else if (phoneId == 0) {
                            VivoSarPowerStateService.this.mVivoSarPowerStateController.notifyCardState(true, 0);
                        } else if (1 == phoneId) {
                            VivoSarPowerStateService.this.mVivoSarPowerStateController.notifyCardState(false, 0);
                        }
                    } else if (!VivoSarPowerStateService.this.mTelephonyManager.hasIccCard(phoneId)) {
                        if (phoneId == 0) {
                            VivoSarPowerStateService.this.mVivoSarPowerStateController.notifyCardState(true, 0);
                        } else if (1 == phoneId) {
                            VivoSarPowerStateService.this.mVivoSarPowerStateController.notifyCardState(false, 0);
                        }
                    }
                    if ("LOADED".equals(iccStateExtra) && VivoSarPowerStateService.this.mTargetPlatformInfo == 2) {
                        Slog.d(VivoSarPowerStateService.TAG, "notifyForceUpdateState");
                        VivoSarPowerStateService.this.mVivoSarPowerStateController.notifyForceUpdateState();
                    }
                    VivoSarPowerStateService.this.mVivoSarPowerStateController.handleSarMessage(0, ProcessList.SERVICE_ADJ);
                    return;
                default:
                    Slog.d(VivoSarPowerStateService.TAG, "VivoSarPowerHandler default, msg:" + msg.what);
                    return;
            }
        }
    }

    private static native int nativeHandleSarPowerEnable(int i);

    private static native int nativeInitSarPowerState();

    private String getSimIccid(int phoneId) {
        String iccid = "";
        ArrayList<SubscriptionInfo> activeSubInfoList = (ArrayList) SubscriptionManager.from(mContext).getActiveSubscriptionInfoList();
        String[] iccids = new String[]{"", ""};
        if (activeSubInfoList != null) {
            for (SubscriptionInfo subInfo : activeSubInfoList) {
                if (subInfo != null && subInfo.getSimSlotIndex() == phoneId) {
                    iccid = subInfo.getIccId();
                    break;
                }
            }
        }
        Slog.d(TAG, "iccid read for SubscriptionInfo:" + iccid + ", phoneId = " + phoneId);
        return iccid;
    }

    private static boolean isVSim(String iccid) {
        Slog.d(TAG, "iccid is " + iccid);
        if (iccid == null) {
            return false;
        }
        if (!iccid.startsWith("89860000000000000001") && !iccid.startsWith("89886920556000843550")) {
            return false;
        }
        Slog.d(TAG, "Is virtul Sim");
        return true;
    }

    private boolean isVSim(int phoneId) {
        Slog.d(TAG, "v phoneId is " + phoneId);
        if (phoneId != 0 || !virtualSIMFlagIsTrue()) {
            return false;
        }
        Slog.d(TAG, "Is virtul Sim");
        return true;
    }

    private boolean isWhiteCard(int phoneId) {
        Slog.d(TAG, "phoneId is " + phoneId);
        if (SubscriptionManager.getSubId(phoneId) != null) {
            String imsi = this.mTelephonyManager.getSubscriberId(SubscriptionManager.getSubId(phoneId)[0]);
            if (TextUtils.isEmpty(imsi)) {
                Slog.d(TAG, "imsi is null");
                return false;
            }
            Slog.d(TAG, "imsi is " + imsi);
            if (TextUtils.isEmpty(imsi) || !isTestImsi(imsi)) {
                Slog.d(TAG, "is not white SIM" + phoneId);
                return false;
            }
            Slog.d(TAG, "is white SIM" + phoneId);
            return true;
        }
        Slog.d(TAG, "SubId is null");
        return false;
    }

    private static boolean isTestImsi(String imsi) {
        if (imsi.startsWith("44201") || imsi.startsWith("46099") || imsi.startsWith("001") || imsi.startsWith("002") || imsi.startsWith("003") || imsi.startsWith("004") || imsi.startsWith("005") || imsi.startsWith("006") || imsi.startsWith("007") || imsi.startsWith("008") || imsi.startsWith("009") || imsi.startsWith("010") || imsi.startsWith("011")) {
            return true;
        }
        return imsi.startsWith("012");
    }

    private boolean virtualSIMFlagIsTrue() {
        try {
            int readFlag = SystemProperties.getInt("sys.vivo.factory.virtualsim", 9);
            Slog.d(TAG, "flag = " + readFlag);
            return readFlag == 1;
        } catch (Exception e) {
            Slog.e(TAG, "vs judge throws exception");
            e.printStackTrace();
            return false;
        }
    }

    public VivoSarPowerStateService(Context contxt) {
        mContext = contxt;
        if (SUPPORT_SAR_POWER) {
            this.mSensorManager = (SensorManager) mContext.getSystemService("sensor");
            this.mProximitySensor = this.mSensorManager.getDefaultSensor(8);
            this.mTelephonyManager = TelephonyManager.getDefault();
            if (platform.equals("qcom")) {
                this.mTargetPlatformInfo = 1;
                this.mVivoSarPowerStateController = new VivoQcomSarPowerStateController(contxt);
            } else {
                this.mTargetPlatformInfo = 2;
                this.mVivoSarPowerStateController = new VivoMtkSarPowerStateController(contxt);
            }
            IntentFilter filterBootComplete = new IntentFilter();
            filterBootComplete.addAction(ACTION_BOOT_COMPLETE);
            mContext.registerReceiver(this.mBootCompleteReceiver, filterBootComplete);
            IntentFilter filterScreenOn = new IntentFilter();
            filterScreenOn.addAction(ACTION_SCREEN_ON);
            mContext.registerReceiver(this.mScreenOnReceiver, filterScreenOn);
            IntentFilter filterScreenOff = new IntentFilter();
            filterScreenOff.addAction(ACTION_SCREEN_OFF);
            mContext.registerReceiver(this.mScreenOffReceiver, filterScreenOff);
            IntentFilter filterCardChange = new IntentFilter();
            filterCardChange.addAction(ACTION_CARD_CHANGE);
            mContext.registerReceiver(this.mCardChangeReceiver, filterCardChange);
            mThread = new HandlerThread(TAG);
            mThread.start();
            this.mMainLooper = mThread.getLooper();
            this.mVivoSarPowerHandler = new VivoSarPowerHandler(this.mMainLooper);
            this.mSensorManager.registerListener(this.mProximitySensorListener, this.mProximitySensor, 3);
        }
    }

    private void initialPowerState() {
        if (this.mVivoSarPowerStateController.initialPowerState()) {
            IntentFilter filterSarPowerTest = new IntentFilter();
            filterSarPowerTest.addAction(ACTION_SAR_POWER_TEST);
            mContext.registerReceiver(this.mSarPowerTestReceiver, filterSarPowerTest);
            return;
        }
        sarPowerSwitchEnable(0);
    }

    public int sarPowerSwitchEnable(int enable) {
        int ret = nativeHandleSarPowerEnable(enable);
        if (ret < 0) {
            Slog.d(TAG, "SarPowerSwitchEnable write fail : " + ret);
        }
        Slog.d(TAG, "SarPowerSwitchEnable write enable : " + enable);
        return 0;
    }

    private boolean isSarPowerRootChanged() {
        if (mSarPowerRootChangedList.length > 0) {
            for (String startsWith : mSarPowerRootChangedList) {
                if (model.startsWith(startsWith)) {
                    return true;
                }
            }
        }
        return false;
    }
}
