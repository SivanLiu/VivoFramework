package com.vivo.services.motion;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManagerInternal.DisplayPowerRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.vivo.common.VivoCollectData;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class MotionExceptionCollect {
    private static final int AIROPERATION_EVENT = 2;
    private static final int CALL_EVENT = 0;
    private static final int CALL_STATE_DIALING = 2;
    private static final int CALL_STATE_IDLE = 0;
    private static final int CALL_STATE_OFFHOOK = 3;
    private static final int CALL_STATE_RINGING = 1;
    private static final String COLLECT_DATA_EVENTID = "1032";
    private static final String COLLECT_DATA_LABLE_FAILED = "10322";
    private static final String COLLECT_DATA_LABLE_SUCCESS = "10321";
    private static final String COLLECT_EXCEPTION_WHEN_CALL = "10323";
    private static final String COLLECT_RAISE_UP_DATA_EVENTID = "1008";
    private static final String COLLECT_RAISE_UP_TRIGGER_AND_EXCEPTION = "10081";
    private static final boolean DEBUG = SystemProperties.get("persist.sys.log.ctrl", "no").equals("yes");
    private static final int FINGERPRINT_EVENT = 1;
    private static final String KEY_VIVO_LOG_CTRL = "persist.sys.log.ctrl";
    private static final int MSG_ACQUIRE_PROXIMITY = 1;
    private static final int MSG_HANDLE_FINGERPRINT_EVENT = 4;
    private static final int MSG_RELEASE_PROXIMITY = 2;
    private static final int MSG_STORE_RECORD = 3;
    private static final int PARAMISOLATION_EVENT = 3;
    private static final String[] PHYSIC_KEYS = new String[]{"WakeKey", "WakeKey", "FingerPrint"};
    private static final int PROXIMITY_FAR = 1;
    private static final int PROXIMITY_NEAR = 0;
    private static final int PROXIMITY_UNKOWN = -1;
    private static final int PS_GET_THRES_TEST = 515;
    private static final int RAISE_UP_WHEN_NEAR_EVENT = 4;
    private static final boolean SCREEN_OFF_STATE = false;
    private static final boolean SCREEN_ON_STATE = true;
    private static final String TAG = MotionExceptionCollect.class.getSimpleName();
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static MotionExceptionCollect mInstance = null;
    private static Object mLock = new Object();
    private Context mContext;
    private boolean mEnabled = false;
    private MyHandler mHandler = null;
    private boolean mLastUnlockByFp = false;
    private SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            int i = 0;
            float distance = event.values[0];
            boolean near = (distance < 0.0f || distance >= MotionExceptionCollect.this.mProximityThreshold) ? false : MotionExceptionCollect.SCREEN_ON_STATE;
            MotionExceptionCollect motionExceptionCollect = MotionExceptionCollect.this;
            if (!near) {
                i = 1;
            }
            motionExceptionCollect.mProximityStatus = i;
            MotionExceptionCollect.this.mProximityData = event.values[1];
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
    private boolean mScreenState = SCREEN_ON_STATE;
    private Sensor mSensor = null;
    private HandlerThread mThread;
    private boolean mTimeoutSleep = false;
    private VivoCollectData mVivoCollectData = null;
    private boolean mWakelockAcquired = false;

    private class AirOperationStatusSnap {
        private static final String TAG_ENG_BASE = "eba";
        private static final String TAG_EVENT = "eve";
        private static final String TAG_POLICY = "plc";
        private static final String TAG_PROXIMITY_DATA = "pda";
        private static final String TAG_REASON = "rea";
        private static final String TAG_TEMP_BASE = "tba";
        private static final String TAG_TYPE = "typ";
        private static final String TAG_VENDOR = "ven";
        public int event;
        public String policy;
        public double proximityData;
        public int reason;
        public int type;
        public String vendor;

        public AirOperationStatusSnap(int event, int type, int reason, int policy, float proximityData) {
            this.event = event;
            this.type = type;
            this.reason = reason;
            this.policy = DisplayPowerRequest.policyToString(policy);
            this.proximityData = (double) proximityData;
            this.vendor = MotionExceptionCollect.this.mProximityVendor;
        }

        public String toString() {
            JSONObject obj = new JSONObject();
            try {
                obj.put(TAG_EVENT, this.event);
                obj.put(TAG_TYPE, this.type);
                obj.put("rea", this.reason);
                obj.put(TAG_POLICY, this.policy);
                obj.put(TAG_PROXIMITY_DATA, this.proximityData);
                obj.put(TAG_VENDOR, this.vendor);
                return obj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(MotionExceptionCollect.TAG, "AirOperationStatusSnap toString FAILED");
                return null;
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
                    MotionExceptionCollect.this.enableSensor(MotionExceptionCollect.SCREEN_ON_STATE);
                    return;
                case 2:
                    MotionExceptionCollect.this.enableSensor(false);
                    return;
                case 3:
                    Slog.e(MotionExceptionCollect.TAG, "MSG_STORE_RECORD msg arg1 = " + msg.arg1);
                    switch (msg.arg1) {
                        case 2:
                            MotionExceptionCollect.this.storeRecord(msg.obj.toString());
                            return;
                        case 4:
                            MotionExceptionCollect.this.storeRaiseUpException();
                            return;
                        default:
                            return;
                    }
                default:
                    return;
            }
        }
    }

    public MotionExceptionCollect(Context context) {
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
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessage(1);
    }

    public static MotionExceptionCollect getInstance(Context context) {
        MotionExceptionCollect motionExceptionCollect;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new MotionExceptionCollect(context);
            }
            motionExceptionCollect = mInstance;
        }
        return motionExceptionCollect;
    }

    public static MotionExceptionCollect getInstance() {
        MotionExceptionCollect motionExceptionCollect;
        synchronized (mLock) {
            motionExceptionCollect = mInstance;
        }
        return motionExceptionCollect;
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

    private void storeRaiseUpException() {
        if (this.mVivoCollectData.getControlInfo(COLLECT_RAISE_UP_DATA_EVENTID)) {
            HashMap<String, String> params = new HashMap();
            params.put("info", "rdn");
            log("storeRaiseUpException");
            this.mVivoCollectData.writeData(COLLECT_RAISE_UP_DATA_EVENTID, COLLECT_RAISE_UP_TRIGGER_AND_EXCEPTION, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
            return;
        }
        log("storeRaiseUpException not collect.");
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
                this.mEnabled = this.mManager.registerListener(this.mListener, this.mSensor, 3, this.mHandler);
            } else {
                this.mManager.unregisterListener(this.mListener);
                this.mEnabled = false;
                this.mProximityStatus = -1;
                this.mProximityData = -1.0f;
            }
        }
    }

    public void onAirOperationTriggered(int type, boolean success, int reason) {
        Slog.e(TAG, "0ops onAirOperationTriggered!!! type = " + type + ", success = " + success + ", reason = " + reason);
        Message msg = this.mHandler.obtainMessage(3);
        msg.arg1 = 2;
        msg.obj = new AirOperationStatusSnap(2, type, reason, this.mPolicy, this.mProximityData);
        this.mHandler.sendMessage(msg);
    }

    public void onRaiseUpExceptionTriggered() {
        Slog.e(TAG, "onRaiseUpExceptionTriggered!!!");
        Message msg = this.mHandler.obtainMessage(3);
        msg.arg1 = 4;
        this.mHandler.sendMessage(msg);
    }
}
