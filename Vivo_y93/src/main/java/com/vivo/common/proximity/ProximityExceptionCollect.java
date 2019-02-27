package com.vivo.common.proximity;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManagerInternal.DisplayPowerRequest;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.util.Log;
import android.util.Slog;
import com.sensoroperate.SensorTestResult;
import com.sensoroperate.VivoSensorTest;
import com.vivo.common.VivoCollectData;
import com.vivo.common.autobrightness.AutobrightInfo;
import com.vivo.common.provider.Calendar.Events;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class ProximityExceptionCollect {
    private static final int AIROPERATION_EVENT = 2;
    private static final String BASE_THRESHOLD_SENSOR = "persist.sys.base_threshold_prox";
    private static final int CALL_EVENT = 0;
    private static final int CALL_STATE_DIALING = 2;
    private static final int CALL_STATE_IDLE = 0;
    private static final int CALL_STATE_OFFHOOK = 3;
    private static final int CALL_STATE_RINGING = 1;
    private static final String COLLECT_DATA_EVENTID = "1032";
    private static final String COLLECT_DATA_FOR_MISC_AND_MEMS = "10322";
    private static final String COLLECT_DATA_LABLE_FAILED = "10322";
    private static final String COLLECT_DATA_LABLE_SUCCESS = "10321";
    private static final String COLLECT_EXCEPTION_WHEN_CALL = "10323";
    private static final boolean DEBUG = SystemProperties.get("persist.sys.log.ctrl", "no").equals("yes");
    private static final int DETECT_NEAR_FOR_LONG_EVENT = 4;
    private static final String FINGERPRINT_AUTH = "com.vivo.fingerprint.action_authorized";
    private static final int FINGERPRINT_EVENT = 1;
    private static final String KEY_MAG_CALI_STATISTICS = "mgc_statistics";
    private static final String KEY_VIVO_LOG_CTRL = "persist.sys.log.ctrl";
    private static final int MAG_CALI_EVENT = 6;
    private static final int MSG_ACQUIRE_PROXIMITY = 1;
    private static final int MSG_HANDLE_FINGERPRINT_EVENT = 4;
    private static final int MSG_RELEASE_PROXIMITY = 2;
    private static final int MSG_STORE_RECORD = 3;
    private static final int MSG_WAIT_WAKEKEY_AFTER_CALL = 5;
    private static final int PARAMISOLATION_EVENT = 3;
    private static final String[] PHYSIC_KEYS = new String[]{"WakeKey", "WakeKey", "FingerPrint"};
    private static final int PROXIMITY_FAR = 1;
    private static final int PROXIMITY_NEAR = 0;
    private static final int PROXIMITY_UNKOWN = -1;
    private static final int PS_GET_THRES_TEST = 515;
    private static final boolean SCREEN_OFF_STATE = false;
    private static final boolean SCREEN_ON_STATE = true;
    private static final int SHORT_THRES_USED_TIME_EVENT = 5;
    private static final String TAG = ProximityExceptionCollect.class.getSimpleName();
    private static final String TMP_BASE_THRESHOLD_SENSOR = "persist.sys.tmp_base_thres_prox";
    private static final int TYPE_AIR_CHANGE_PARAM = 0;
    private static final int TYPE_CALL_CHANGE_PARAM = 1;
    private static final int TYPE_PROXWAKELOCK_CHANGE_PARAM = 2;
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static ProximityExceptionCollect mInstance = null;
    private static Object mLock = new Object();
    private static SettingsObserver mSettingObserver;
    private float[] DefBase = new float[3];
    private float[] MaxBase = new float[3];
    private float[] MinBase = new float[3];
    private long ONE_DAY_INTERVAL = 86400000;
    private float[] TestVal = new float[3];
    private int[] args = new int[1];
    private boolean mAirOperationChangeParam = false;
    private final IntentFilter mBootCompletedFilter = new IntentFilter("android.intent.action.BOOT_COMPLETED");
    private boolean mCallChangeParam = false;
    private ContentResolver mContentResolver;
    private Context mContext;
    private long mDetectNearStartTime = -1;
    private boolean mEnabled = false;
    private final IntentFilter mFpUnlockFilter = new IntentFilter(FINGERPRINT_AUTH);
    private MyHandler mHandler = null;
    private final IntentFilter mKeyguardUnlockFilter = new IntentFilter("android.intent.action.USER_PRESENT");
    private boolean mLastUnlockByFp = false;
    private SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            int i;
            float distance = event.values[0];
            boolean near = (distance < 0.0f || distance >= ProximityExceptionCollect.this.mProximityThreshold) ? false : ProximityExceptionCollect.SCREEN_ON_STATE;
            ProximityExceptionCollect proximityExceptionCollect = ProximityExceptionCollect.this;
            if (near) {
                i = 0;
            } else {
                i = 1;
            }
            proximityExceptionCollect.mProximityStatus = i;
            if (event.values.length >= 2) {
                ProximityExceptionCollect.this.mProximityData = event.values[1];
            } else {
                ProximityExceptionCollect.this.mProximityData = -1.1f;
            }
            if (ProximityExceptionCollect.this.mProximityStatus == 0) {
                ProximityExceptionCollect.this.mDetectNearStartTime = SystemClock.uptimeMillis();
            } else if (ProximityExceptionCollect.this.mDetectNearStartTime != -1) {
                long current = SystemClock.uptimeMillis();
                if (current - ProximityExceptionCollect.this.mDetectNearStartTime > ProximityExceptionCollect.this.ONE_DAY_INTERVAL) {
                    Slog.e(ProximityExceptionCollect.TAG, "from near to far more than one day, last time:" + (current - ProximityExceptionCollect.this.mDetectNearStartTime));
                    Message msg = ProximityExceptionCollect.this.mHandler.obtainMessage(3);
                    msg.arg1 = 4;
                    msg.obj = new NearForLongStatusSnap(4, 0, 0, ProximityExceptionCollect.this.mPolicy, ProximityExceptionCollect.this.mProximityData);
                    ProximityExceptionCollect.this.mHandler.sendMessage(msg);
                } else {
                    Slog.e(ProximityExceptionCollect.TAG, "from near to far, last time:" + (current - ProximityExceptionCollect.this.mDetectNearStartTime));
                }
                ProximityExceptionCollect.this.mDetectNearStartTime = 0;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private SensorManager mManager = null;
    private int mPhoneState = 0;
    private int mPolicy = 0;
    private float mProximityData = -1.0f;
    private int mProximityStatus = -1;
    private float mProximityThreshold = TYPICAL_PROXIMITY_THRESHOLD;
    private String mProximityVendor = "unkown";
    private boolean mProximityWakelockChangeParam = false;
    private final IntentFilter mRebootFilter = new IntentFilter("android.intent.action.REBOOT");
    private Runnable mRegisterRunnable = new Runnable() {
        public void run() {
            ProximityExceptionCollect.this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(ProximityExceptionCollect.KEY_MAG_CALI_STATISTICS), false, ProximityExceptionCollect.mSettingObserver, -1);
            ProximityExceptionCollect.this.mContext.registerReceiver(ProximityExceptionCollect.this.myBroadcastReceiver, ProximityExceptionCollect.this.mScreenOnFilter);
            ProximityExceptionCollect.this.mContext.registerReceiver(ProximityExceptionCollect.this.myBroadcastReceiver, ProximityExceptionCollect.this.mScreenOffFilter);
            ProximityExceptionCollect.this.mContext.registerReceiver(ProximityExceptionCollect.this.myBroadcastReceiver, ProximityExceptionCollect.this.mKeyguardUnlockFilter);
            ProximityExceptionCollect.this.mContext.registerReceiver(ProximityExceptionCollect.this.myBroadcastReceiver, ProximityExceptionCollect.this.mFpUnlockFilter);
            ProximityExceptionCollect.this.mContext.registerReceiver(ProximityExceptionCollect.this.myBroadcastReceiver, ProximityExceptionCollect.this.mBootCompletedFilter);
            ProximityExceptionCollect.this.mContext.registerReceiver(ProximityExceptionCollect.this.myBroadcastReceiver, ProximityExceptionCollect.this.mShutdownFilter);
            ProximityExceptionCollect.this.mContext.registerReceiver(ProximityExceptionCollect.this.myBroadcastReceiver, ProximityExceptionCollect.this.mRebootFilter);
        }
    };
    private final IntentFilter mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
    private final IntentFilter mScreenOnFilter = new IntentFilter("android.intent.action.SCREEN_ON");
    private boolean mScreenState = SCREEN_ON_STATE;
    private Sensor mSensor = null;
    private long mShortThresStartTime = -1;
    private long mShortThresUsedTime = 0;
    private final IntentFilter mShutdownFilter = new IntentFilter("android.intent.action.ACTION_SHUTDOWN");
    private HandlerThread mThread;
    private boolean mTimeoutSleep = false;
    private VivoCollectData mVivoCollectData = null;
    private VivoSensorTest mVivoSensorTest = null;
    private boolean mWakelockAcquired = false;
    private final BroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver(this, null);
    private SensorTestResult result = new SensorTestResult();
    private boolean waitWakeKeyAfterCall = false;

    private class AirOperationStatusSnap {
        private static final String TAG_ENG_BASE = "eba";
        private static final String TAG_EVENT = "eve";
        private static final String TAG_POLICY = "plc";
        private static final String TAG_PROXIMITY_DATA = "pda";
        private static final String TAG_REASON = "rea";
        private static final String TAG_TEMP_BASE = "tba";
        private static final String TAG_TYPE = "typ";
        private static final String TAG_VENDOR = "ven";
        public int engBase;
        public int event;
        public String policy;
        public double proximityData;
        public int reason;
        public int tmpBase;
        public int type;
        public String vendor;

        public AirOperationStatusSnap(int event, int type, int reason, int policy, float proximityData) {
            this.event = event;
            this.type = type;
            this.reason = reason;
            this.policy = DisplayPowerRequest.policyToString(policy);
            this.proximityData = (double) proximityData;
            this.vendor = ProximityExceptionCollect.this.mProximityVendor;
        }

        public String toString() {
            JSONObject obj = new JSONObject();
            this.engBase = SystemProperties.getInt(ProximityExceptionCollect.BASE_THRESHOLD_SENSOR, -1);
            this.tmpBase = SystemProperties.getInt(ProximityExceptionCollect.TMP_BASE_THRESHOLD_SENSOR, -1);
            try {
                obj.put(TAG_EVENT, this.event);
                obj.put(TAG_TYPE, this.type);
                obj.put("rea", this.reason);
                obj.put(TAG_POLICY, this.policy);
                obj.put(TAG_PROXIMITY_DATA, this.proximityData);
                obj.put(TAG_ENG_BASE, this.engBase);
                obj.put(TAG_TEMP_BASE, this.tmpBase);
                obj.put(TAG_VENDOR, this.vendor);
                return obj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(ProximityExceptionCollect.TAG, "AirOperationStatusSnap toString FAILED");
                return null;
            }
        }
    }

    private class CallStatusSnap {
        public static final String REASON_TIMEOUT = "timeout";
        private static final String TAG_CALL_STATE = "cst";
        private static final String TAG_ENG_BASE = "eba";
        private static final String TAG_EVENT = "eve";
        private static final String TAG_POLICY = "plc";
        private static final String TAG_PROXIMITY_DATA = "pda";
        private static final String TAG_REASON = "rea";
        private static final String TAG_TEMP_BASE = "tba";
        private static final String TAG_TYPE = "typ";
        private static final String TAG_VENDOR = "ven";
        public static final String TYPE_SLEEP = "sleep";
        public static final String TYPE_TIMEOUT_WAKEUP = "timeout_wake";
        public static final String TYPE_WAKEUP = "wake";
        public static final String TYPE_WAKEUP_AFTER_CALL = "wake_ac";
        public String callState;
        public int engBase;
        public int event;
        public String policy;
        public double proximityData;
        public String reason;
        public int tmpBase;
        public String type;
        public String vendor;

        public CallStatusSnap(int event, String type, String reason, int callState, int policy, float proximityData) {
            this.event = event;
            this.type = type;
            this.reason = reason;
            this.callState = ProximityExceptionCollect.this.callStateToString(callState);
            this.policy = DisplayPowerRequest.policyToString(policy);
            this.proximityData = (double) proximityData;
            this.vendor = ProximityExceptionCollect.this.mProximityVendor;
        }

        public String toString() {
            JSONObject obj = new JSONObject();
            this.engBase = SystemProperties.getInt(ProximityExceptionCollect.BASE_THRESHOLD_SENSOR, -1);
            this.tmpBase = SystemProperties.getInt(ProximityExceptionCollect.TMP_BASE_THRESHOLD_SENSOR, -1);
            try {
                obj.put(TAG_EVENT, this.event);
                obj.put(TAG_TYPE, this.type);
                obj.put("rea", this.reason);
                obj.put(TAG_CALL_STATE, this.callState);
                obj.put(TAG_POLICY, this.policy);
                obj.put(TAG_PROXIMITY_DATA, this.proximityData);
                obj.put(TAG_ENG_BASE, this.engBase);
                obj.put(TAG_TEMP_BASE, this.tmpBase);
                obj.put(TAG_VENDOR, this.vendor);
                return obj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(ProximityExceptionCollect.TAG, "CallStatusSnap toString FAILED");
                return null;
            }
        }
    }

    private class FingerprintStatusSnap {
        public static final String REASON_TIMEOUT = "timeout";
        private static final String TAG_ENG_BASE = "eba";
        private static final String TAG_EVENT = "eve";
        private static final String TAG_POLICY = "plc";
        private static final String TAG_PROXIMITY_DATA = "pda";
        private static final String TAG_PROXIMITY_THRES = "pths";
        private static final String TAG_REASON = "rea";
        private static final String TAG_TEMP_BASE = "tba";
        private static final String TAG_TYPE = "typ";
        private static final String TAG_VENDOR = "ven";
        public static final String TYPE_SLEEP = "sleep";
        public static final String TYPE_TIMEOUT_WAKEUP = "timeout_wake";
        public static final String TYPE_WAKEUP = "wake";
        public int engBase;
        public int event;
        public String policy;
        public double proximityData;
        public String proximityThreshold;
        public String reason;
        public int tmpBase;
        public String type;
        public String vendor;

        public FingerprintStatusSnap(int event, String type, String reason, int policy, float proximityData, String proximityThreshold) {
            this.event = event;
            this.type = type;
            this.reason = reason;
            this.policy = DisplayPowerRequest.policyToString(policy);
            this.proximityData = (double) proximityData;
            this.proximityThreshold = proximityThreshold;
            this.vendor = ProximityExceptionCollect.this.mProximityVendor;
        }

        public String toString() {
            JSONObject obj = new JSONObject();
            this.engBase = SystemProperties.getInt(ProximityExceptionCollect.BASE_THRESHOLD_SENSOR, -1);
            this.tmpBase = SystemProperties.getInt(ProximityExceptionCollect.TMP_BASE_THRESHOLD_SENSOR, -1);
            try {
                obj.put(TAG_EVENT, this.event);
                obj.put(TAG_TYPE, this.type);
                obj.put("rea", this.reason);
                obj.put(TAG_POLICY, this.policy);
                obj.put(TAG_PROXIMITY_DATA, this.proximityData);
                obj.put(TAG_PROXIMITY_THRES, this.proximityThreshold);
                obj.put(TAG_ENG_BASE, this.engBase);
                obj.put(TAG_TEMP_BASE, this.tmpBase);
                obj.put(TAG_VENDOR, this.vendor);
                return obj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(ProximityExceptionCollect.TAG, "FingerprintStatusSnap toString FAILED");
                return null;
            }
        }
    }

    private class MagCaliStatusSnap {
        private static final String TAG_EVENT = "eve";
        private static final String TAG_REASON = "rea";
        private static final String TAG_STATISTICS = "sts";
        private static final String TAG_TYPE = "typ";
        public int event;
        public int reason;
        public String statistics;
        public int type;

        public MagCaliStatusSnap(int event, int type, int reason, String statistics) {
            this.event = event;
            this.type = type;
            this.reason = reason;
            this.statistics = statistics;
        }

        public String toString() {
            JSONObject obj = new JSONObject();
            try {
                obj.put(TAG_EVENT, this.event);
                obj.put(TAG_TYPE, this.type);
                obj.put("rea", this.reason);
                obj.put(TAG_STATISTICS, this.statistics);
                return obj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(ProximityExceptionCollect.TAG, "MagCaliStatusSnap toString FAILED");
                return null;
            }
        }
    }

    private final class MyBroadcastReceiver extends BroadcastReceiver {
        /* synthetic */ MyBroadcastReceiver(ProximityExceptionCollect this$0, MyBroadcastReceiver -this1) {
            this();
        }

        private MyBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    ProximityExceptionCollect.this.log("screen on broadcast received");
                    ProximityExceptionCollect.this.mScreenState = ProximityExceptionCollect.SCREEN_ON_STATE;
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    ProximityExceptionCollect.this.log("screen off broadcast received");
                    ProximityExceptionCollect.this.mScreenState = false;
                    ProximityExceptionCollect.this.mLastUnlockByFp = false;
                } else if (action.equals(ProximityExceptionCollect.FINGERPRINT_AUTH)) {
                    ProximityExceptionCollect.this.mLastUnlockByFp = intent.getExtras().getBoolean("isFingerprintSuccess");
                    Slog.e(ProximityExceptionCollect.TAG, "fingerprint unlock broadcast received, extras is " + ProximityExceptionCollect.this.mLastUnlockByFp + ", mProximityStatus = " + ProximityExceptionCollect.this.mProximityStatus);
                    if (ProximityExceptionCollect.this.mLastUnlockByFp && ProximityExceptionCollect.this.mProximityStatus == 0) {
                        ProximityExceptionCollect.this.mHandler.removeMessages(4);
                        ProximityExceptionCollect.this.mHandler.sendEmptyMessage(4);
                        Slog.e(ProximityExceptionCollect.TAG, "fingerprint unlocked when proxmity is near");
                    }
                } else if (action.equals("android.intent.action.USER_PRESENT")) {
                    ProximityExceptionCollect.this.log("keyguard unlocked broadcast received.");
                } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                    Slog.e(ProximityExceptionCollect.TAG, "boot completed broadcast received.");
                    ProximityExceptionCollect.this.mAirOperationChangeParam = false;
                    ProximityExceptionCollect.this.mCallChangeParam = false;
                    ProximityExceptionCollect.this.mProximityWakelockChangeParam = false;
                    ProximityExceptionCollect.this.mShortThresStartTime = SystemClock.uptimeMillis();
                } else if (action.equals("android.intent.action.REBOOT") || action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                    Slog.e(ProximityExceptionCollect.TAG, "reboot or shutdown broadcast received.");
                    long current = SystemClock.uptimeMillis();
                    if (ProximityExceptionCollect.this.mProximityStatus == 0 && ProximityExceptionCollect.this.mDetectNearStartTime != -1) {
                        if (current - ProximityExceptionCollect.this.mDetectNearStartTime > ProximityExceptionCollect.this.ONE_DAY_INTERVAL) {
                            Slog.e(ProximityExceptionCollect.TAG, "from near to far more than one day, last time:" + (current - ProximityExceptionCollect.this.mDetectNearStartTime));
                            Message msg = ProximityExceptionCollect.this.mHandler.obtainMessage(3);
                            msg.arg1 = 4;
                            msg.obj = new NearForLongStatusSnap(4, 0, 0, ProximityExceptionCollect.this.mPolicy, ProximityExceptionCollect.this.mProximityData);
                            ProximityExceptionCollect.this.mHandler.sendMessage(msg);
                        } else {
                            Slog.e(ProximityExceptionCollect.TAG, "from near to far, last time:" + (current - ProximityExceptionCollect.this.mDetectNearStartTime));
                        }
                        ProximityExceptionCollect.this.mDetectNearStartTime = 0;
                    }
                    Slog.e(ProximityExceptionCollect.TAG, "collect short thres used time, last time:" + ProximityExceptionCollect.this.mShortThresUsedTime);
                    Message smsg = ProximityExceptionCollect.this.mHandler.obtainMessage(3);
                    smsg.arg1 = 5;
                    smsg.obj = new ShortThresUsedTimeStatusSnap(5, 0, 0, ProximityExceptionCollect.this.mPolicy, ProximityExceptionCollect.this.mShortThresUsedTime);
                    ProximityExceptionCollect.this.mHandler.sendMessageDelayed(smsg, 100);
                }
            }
        }
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ProximityExceptionCollect.this.enableSensor(ProximityExceptionCollect.SCREEN_ON_STATE);
                    return;
                case 2:
                    ProximityExceptionCollect.this.enableSensor(false);
                    return;
                case 3:
                    Slog.e(ProximityExceptionCollect.TAG, "MSG_STORE_RECORD msg arg1 = " + msg.arg1);
                    switch (msg.arg1) {
                        case 0:
                            ProximityExceptionCollect.this.storeRecord(((CallStatusSnap) msg.obj).toString());
                            return;
                        case 1:
                            ProximityExceptionCollect.this.storeRecord(msg.obj.toString());
                            return;
                        case 2:
                            ProximityExceptionCollect.this.storeRecord(msg.obj.toString());
                            return;
                        case 3:
                            return;
                        case 4:
                            ProximityExceptionCollect.this.storeRecord(msg.obj.toString());
                            return;
                        case 5:
                            ProximityExceptionCollect.this.storeRecord(msg.obj.toString());
                            return;
                        case 6:
                            ProximityExceptionCollect.this.storeRecordForMisc(msg.obj.toString());
                            return;
                        default:
                            ProximityExceptionCollect.this.storeRecord(msg.obj.toString());
                            return;
                    }
                case 4:
                    Slog.e(ProximityExceptionCollect.TAG, "MSG_HANDLE_FINGERPRINT_EVENT get thres test");
                    Message fmsg = ProximityExceptionCollect.this.mHandler.obtainMessage(3);
                    fmsg.arg1 = 1;
                    fmsg.obj = new FingerprintStatusSnap(1, "type", AutobrightInfo.KEY_REASON, ProximityExceptionCollect.this.mPolicy, ProximityExceptionCollect.this.mProximityData, "[0.0,0.0]");
                    ProximityExceptionCollect.this.mHandler.sendMessage(fmsg);
                    return;
                case 5:
                    ProximityExceptionCollect.this.waitWakeKeyAfterCall = false;
                    return;
                default:
                    return;
            }
        }
    }

    private class NearForLongStatusSnap {
        private static final String TAG_ENG_BASE = "eba";
        private static final String TAG_EVENT = "eve";
        private static final String TAG_POLICY = "plc";
        private static final String TAG_PROXIMITY_DATA = "pda";
        private static final String TAG_REASON = "rea";
        private static final String TAG_TEMP_BASE = "tba";
        private static final String TAG_TYPE = "typ";
        private static final String TAG_VENDOR = "ven";
        public int engBase;
        public int event;
        public String policy;
        public double proximityData;
        public int reason;
        public int tmpBase;
        public int type;
        public String vendor;

        public NearForLongStatusSnap(int event, int type, int reason, int policy, float proximityData) {
            this.event = event;
            this.type = type;
            this.reason = reason;
            this.policy = DisplayPowerRequest.policyToString(policy);
            this.proximityData = (double) proximityData;
            this.vendor = ProximityExceptionCollect.this.mProximityVendor;
        }

        public String toString() {
            JSONObject obj = new JSONObject();
            this.engBase = SystemProperties.getInt(ProximityExceptionCollect.BASE_THRESHOLD_SENSOR, -1);
            this.tmpBase = SystemProperties.getInt(ProximityExceptionCollect.TMP_BASE_THRESHOLD_SENSOR, -1);
            try {
                obj.put(TAG_EVENT, this.event);
                obj.put(TAG_TYPE, this.type);
                obj.put("rea", this.reason);
                obj.put(TAG_POLICY, this.policy);
                obj.put(TAG_PROXIMITY_DATA, this.proximityData);
                obj.put(TAG_ENG_BASE, this.engBase);
                obj.put(TAG_TEMP_BASE, this.tmpBase);
                obj.put(TAG_VENDOR, this.vendor);
                return obj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(ProximityExceptionCollect.TAG, "NearForLongStatusSnap toString FAILED");
                return null;
            }
        }
    }

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            String statistics = Events.DEFAULT_SORT_ORDER;
            try {
                statistics = Global.getString(ProximityExceptionCollect.this.mContentResolver, ProximityExceptionCollect.KEY_MAG_CALI_STATISTICS);
                Slog.d(ProximityExceptionCollect.TAG, "get mgc_sta : " + statistics);
                Message msg = ProximityExceptionCollect.this.mHandler.obtainMessage(3);
                msg.arg1 = 6;
                msg.obj = new MagCaliStatusSnap(6, 0, 0, statistics);
                ProximityExceptionCollect.this.mHandler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
                Slog.e(ProximityExceptionCollect.TAG, "get mgc_sta FAILED");
            }
        }
    }

    private class ShortThresUsedTimeStatusSnap {
        private static final String TAG_ENG_BASE = "eba";
        private static final String TAG_EVENT = "eve";
        private static final String TAG_POLICY = "plc";
        private static final String TAG_REASON = "rea";
        private static final String TAG_TEMP_BASE = "tba";
        private static final String TAG_TYPE = "typ";
        private static final String TAG_USED_TIME = "ut";
        private static final String TAG_VENDOR = "ven";
        public int engBase;
        public int event;
        public String policy;
        public int reason;
        public int tmpBase;
        public int type;
        public long usedTime;
        public String vendor;

        public ShortThresUsedTimeStatusSnap(int event, int type, int reason, int policy, long usedTime) {
            this.event = event;
            this.type = type;
            this.reason = reason;
            this.policy = DisplayPowerRequest.policyToString(policy);
            this.usedTime = usedTime;
            this.vendor = ProximityExceptionCollect.this.mProximityVendor;
        }

        public String toString() {
            JSONObject obj = new JSONObject();
            this.engBase = SystemProperties.getInt(ProximityExceptionCollect.BASE_THRESHOLD_SENSOR, -1);
            this.tmpBase = SystemProperties.getInt(ProximityExceptionCollect.TMP_BASE_THRESHOLD_SENSOR, -1);
            try {
                obj.put(TAG_EVENT, this.event);
                obj.put(TAG_TYPE, this.type);
                obj.put("rea", this.reason);
                obj.put(TAG_POLICY, this.policy);
                obj.put(TAG_USED_TIME, this.usedTime);
                obj.put(TAG_ENG_BASE, this.engBase);
                obj.put(TAG_TEMP_BASE, this.tmpBase);
                obj.put(TAG_VENDOR, this.vendor);
                return obj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(ProximityExceptionCollect.TAG, "ShortThresUsedTimeStatusSnap toString FAILED");
                return null;
            }
        }
    }

    public ProximityExceptionCollect(Context context) {
        this.mContext = context;
        this.mVivoCollectData = new VivoCollectData(this.mContext);
        this.mThread = new HandlerThread(TAG);
        this.mThread.start();
        this.mHandler = new MyHandler(this.mThread.getLooper());
        this.mManager = (SensorManager) this.mContext.getSystemService("sensor");
        if (this.mManager != null) {
            this.mSensor = this.mManager.getDefaultSensor(8);
            if (this.mSensor != null) {
                this.mProximityThreshold = Math.min(this.mSensor.getMaximumRange(), TYPICAL_PROXIMITY_THRESHOLD);
                String vendor = this.mSensor.getVendor();
                if (vendor != null) {
                    this.mProximityVendor = vendor.trim();
                }
            }
        }
        mSettingObserver = new SettingsObserver(this.mHandler);
        this.mContentResolver = context.getContentResolver();
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessage(1);
        this.mHandler.post(this.mRegisterRunnable);
    }

    public static ProximityExceptionCollect getInstance(Context context) {
        ProximityExceptionCollect proximityExceptionCollect;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new ProximityExceptionCollect(context);
            }
            proximityExceptionCollect = mInstance;
        }
        return proximityExceptionCollect;
    }

    private void storeRecord(String info) {
        if (info != null) {
            if (this.mVivoCollectData.getControlInfo(COLLECT_DATA_EVENTID)) {
                HashMap<String, String> params = new HashMap();
                params.put("info", info);
                log("storeRecord info:" + info);
                this.mVivoCollectData.writeData(COLLECT_DATA_EVENTID, COLLECT_EXCEPTION_WHEN_CALL, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
            } else {
                log("storeRecord not collect.");
            }
        }
    }

    private void storeRecordForMisc(String info) {
        if (info != null) {
            if (this.mVivoCollectData.getControlInfo(COLLECT_DATA_EVENTID)) {
                HashMap<String, String> params = new HashMap();
                params.put("info", info);
                log("storeRecordForMisc info:" + info);
                this.mVivoCollectData.writeData(COLLECT_DATA_EVENTID, "10322", System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
            } else {
                log("storeRecordForMisc not collect.");
            }
        }
    }

    private void log(String msg) {
        if (DEBUG) {
            Slog.d(TAG, msg);
        }
    }

    private void enableSensor(boolean enable) {
        if (enable != this.mEnabled) {
            if (this.mManager == null) {
                log("enableSensorLock mManager=null");
            } else if (enable) {
                this.mProximityStatus = -1;
                this.mProximityData = -1.0f;
                this.mEnabled = this.mManager.registerListener(this.mListener, this.mSensor, 1, this.mHandler);
            } else {
                this.mManager.unregisterListener(this.mListener);
                this.mEnabled = false;
                this.mProximityStatus = -1;
                this.mProximityData = -1.0f;
            }
        }
    }

    public void onProximityLockChanged(boolean acquired) {
        if (this.mWakelockAcquired != acquired) {
            this.mWakelockAcquired = acquired;
            if (acquired) {
                notifyChangeProximityParam(2, SCREEN_ON_STATE);
            } else {
                notifyChangeProximityParam(2, false);
            }
        }
    }

    public void onPolicyChanged(int policy) {
        this.mPolicy = policy;
    }

    private boolean isPhysicKey(String key) {
        if (key == null) {
            return false;
        }
        for (String k : PHYSIC_KEYS) {
            if (k.equals(key)) {
                return SCREEN_ON_STATE;
            }
        }
        return false;
    }

    public void onWakeupKeyPressd(String key) {
        Message msg;
        if (this.mPhoneState == 0 && isPhysicKey(key) && this.waitWakeKeyAfterCall) {
            msg = this.mHandler.obtainMessage(3);
            msg.arg1 = 0;
            msg.obj = new CallStatusSnap(0, CallStatusSnap.TYPE_WAKEUP_AFTER_CALL, key, this.mPhoneState, this.mPolicy, this.mProximityData);
            this.mHandler.sendMessage(msg);
            Slog.e(TAG, "onWakeupKeyPressd when wait key after call");
            this.mTimeoutSleep = false;
            return;
        }
        if (this.mWakelockAcquired && this.mPhoneState != 0 && isPhysicKey(key)) {
            if (this.mProximityStatus == 0) {
                msg = this.mHandler.obtainMessage(3);
                msg.arg1 = 0;
                msg.obj = new CallStatusSnap(0, "wake", key, this.mPhoneState, this.mPolicy, this.mProximityData);
                this.mHandler.sendMessage(msg);
                log("onWakeupKeyPressd TYPE_WAKEUP");
            } else if (this.mProximityStatus == 1 && this.mTimeoutSleep) {
                msg = this.mHandler.obtainMessage(3);
                msg.arg1 = 0;
                msg.obj = new CallStatusSnap(0, "timeout_wake", key, this.mPhoneState, this.mPolicy, this.mProximityData);
                this.mHandler.sendMessage(msg);
                log("onWakeupKeyPressd TYPE_TIMEOUT_WAKEUP");
            }
        }
        this.mTimeoutSleep = false;
    }

    private String callStateToString(int state) {
        switch (state) {
            case 0:
                return "idle";
            case 1:
                return "ringing";
            case 2:
                return "dialing";
            case 3:
                return "offhook";
            default:
                return "unkown";
        }
    }

    public void onPhoneStateChanged(int state) {
        boolean z = false;
        switch (state) {
            case 0:
                if (this.mPhoneState != 0) {
                    this.waitWakeKeyAfterCall = SCREEN_ON_STATE;
                    this.mHandler.sendEmptyMessageDelayed(5, 1000);
                }
                this.mTimeoutSleep = false;
                break;
            case 1:
            case 2:
            case 3:
                break;
            default:
                return;
        }
        this.mPhoneState = state;
        if (this.mPhoneState != 0) {
            z = SCREEN_ON_STATE;
        }
        notifyChangeProximityParam(1, z);
    }

    public void onGotoSleep(int reason) {
        if (this.mPhoneState != 0 && reason == 2 && this.mWakelockAcquired && this.mProximityStatus == 1) {
            Message msg = this.mHandler.obtainMessage(3);
            msg.arg1 = 0;
            msg.obj = new CallStatusSnap(0, "sleep", "timeout", this.mPhoneState, this.mPolicy, this.mProximityData);
            this.mHandler.sendMessage(msg);
            this.mTimeoutSleep = SCREEN_ON_STATE;
            log("onWakeupKeyPressd TYPE_SLEEP");
        }
    }

    public void onAirOperationTriggered(int type, boolean success, int reason) {
        Slog.e(TAG, "0ops onAirOperationTriggered!!! type = " + type + ", success = " + success + ", reason = " + reason);
        Message msg = this.mHandler.obtainMessage(3);
        msg.arg1 = 2;
        msg.obj = new AirOperationStatusSnap(2, type, reason, this.mPolicy, this.mProximityData);
        this.mHandler.sendMessage(msg);
    }

    public void notifyChangeProximityParam(int type, boolean change) {
        if (type == 0) {
            if (this.mAirOperationChangeParam != change) {
                this.mAirOperationChangeParam = change;
                if (!(!change || (this.mCallChangeParam ^ 1) == 0 || (this.mProximityWakelockChangeParam ^ 1) == 0)) {
                    this.mShortThresUsedTime += SystemClock.uptimeMillis() - this.mShortThresStartTime;
                }
            } else {
                return;
            }
        } else if (type == 1) {
            if (this.mCallChangeParam != change) {
                this.mCallChangeParam = change;
                if (!(!change || (this.mAirOperationChangeParam ^ 1) == 0 || (this.mProximityWakelockChangeParam ^ 1) == 0)) {
                    this.mShortThresUsedTime += SystemClock.uptimeMillis() - this.mShortThresStartTime;
                }
            } else {
                return;
            }
        } else if (type == 2) {
            if (this.mProximityWakelockChangeParam != change) {
                this.mProximityWakelockChangeParam = change;
                if (!(!change || (this.mAirOperationChangeParam ^ 1) == 0 || (this.mCallChangeParam ^ 1) == 0)) {
                    this.mShortThresUsedTime += SystemClock.uptimeMillis() - this.mShortThresStartTime;
                }
            } else {
                return;
            }
        }
        if (!(this.mCallChangeParam || (this.mProximityWakelockChangeParam ^ 1) == 0 || (this.mAirOperationChangeParam ^ 1) == 0)) {
            this.mShortThresStartTime = SystemClock.uptimeMillis();
        }
    }
}
