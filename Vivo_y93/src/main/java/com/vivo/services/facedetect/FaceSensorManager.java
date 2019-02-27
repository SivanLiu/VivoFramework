package com.vivo.services.facedetect;

import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Calendar;

public class FaceSensorManager {
    private static final String ACTION_INFRARED_TIMER = "android.intent.action.ALARM_UD_FACE_TIME";
    private static final int ICON_LOCKOUT_TIMEOUT_MS = 1800000;
    public static final int INFRARED_STATE_FAR = 2;
    public static final int INFRARED_STATE_NEAR = 1;
    private static final String INTENT_FACEKEY_MOTION_TIMER = "android.intent.action.ALARM_FACEKEY_MOTION_TIME";
    public static final int MOTION_STATE_MOVE = 2;
    public static final int MOTION_STATE_STILL = 1;
    private static final int MSG_CALLBACK_INFRARED = 1002;
    private static final int MSG_CALLBACK_MOVE_WAKE = 1001;
    private static final int MSG_NOTIFY_FINGER_STATE = 1005;
    private static final int MSG_NOTIFY_SCREEN_STATE = 1004;
    private static final int MSG_NOTIFY_WORK_STATE = 1003;
    private static final String NODE_HBM = "/sys/lcm/oled_hbm";
    public static final String PRODUCT_MODEL = SystemProperties.get("ro.product.model.bbk", "unkown");
    private static final String TAG = "FaceSensorManager";
    private static final int TIMEOUT_INF_MILLIS = 7000;
    public static final int TYPE_SENSOR_INFRARED = 2;
    public static final int TYPE_SENSOR_MOVE_WAKE = 1;
    private static FaceSensorManager mInstance;
    private static Object mLock = new Object();
    private static int mTypeMotionDetect = 91;
    private AlarmManager mAlarmManager;
    private Context mContext;
    private boolean mEnterWork = false;
    private boolean mFaceDown = false;
    private FaceHandler mFaceHandler;
    private HandlerThread mHandlerThread;
    private boolean mHasStarted = false;
    private boolean mInfTimerOut = false;
    private boolean mInfTimerStarted = false;
    private boolean mInfraredClose = false;
    private boolean mInfraredLisRegistered = false;
    private PendingIntent mInfraredPdIntent;
    private boolean mMotionLisRegistered = false;
    private boolean mMotionTimerRegister = false;
    private boolean mMotionTimerStarted = false;
    private float mProximityThreshold;
    private boolean mReceiverRegistered = false;
    private boolean mScreenOn = true;
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == 8) {
                float distance = Float.valueOf(event.values[0]).floatValue();
                boolean infraredClose = distance >= 0.0f && distance < FaceSensorManager.this.mProximityThreshold;
                Log.i(FaceSensorManager.TAG, "infrared state changed: " + FaceSensorManager.this.mInfraredClose + ":" + distance + ":" + FaceSensorManager.this.mProximityThreshold);
                if (infraredClose != FaceSensorManager.this.mInfraredClose) {
                    FaceSensorManager.this.mInfraredClose = infraredClose;
                    if (FaceSensorManager.this.mInfraredClose) {
                        FaceSensorManager.this.startInfraredTimer();
                    } else {
                        FaceSensorManager.this.cancelInfraredTimer();
                        if (FaceSensorManager.this.mInfTimerOut) {
                            FaceSensorManager.this.mInfTimerOut = false;
                            FaceSensorManager.this.sendMessage(FaceSensorManager.MSG_CALLBACK_INFRARED, 2, 0, null);
                        }
                    }
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private SensorManager mSensorManager;
    private SensorCallback mTimerCallback;
    private BroadcastReceiver mTimerReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                Log.i(FaceSensorManager.TAG, "onReceive: " + action);
                if (!TextUtils.isEmpty(action) && action.equals(FaceSensorManager.ACTION_INFRARED_TIMER)) {
                    FaceSensorManager.this.cancelInfraredTimer();
                    if (FaceSensorManager.this.mInfraredClose) {
                        FaceSensorManager.this.mInfTimerOut = true;
                        FaceSensorManager.this.sendMessage(FaceSensorManager.MSG_CALLBACK_INFRARED, 1, 0, null);
                        if (!FaceSensorManager.this.mInfraredClose) {
                            FaceSensorManager.this.mInfTimerOut = false;
                            Log.i(FaceSensorManager.TAG, "onReceive: infrared away");
                            FaceSensorManager.this.sendMessage(FaceSensorManager.MSG_CALLBACK_INFRARED, 2, 0, null);
                        }
                    }
                }
            }
        }
    };
    private Vibrator mVibrator = null;

    public static abstract class SensorCallback {
        public void onFaceTimer(int sensorType, int sensorState) {
        }
    }

    private class FaceHandler extends Handler {
        public FaceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Log.i(FaceSensorManager.TAG, "handleMessage: " + msg.what + "{ " + FaceSensorManager.this.messageToString(msg) + " }");
            switch (msg.what) {
                case 1001:
                    FaceSensorManager.this.onCallbackEvent(1, msg.arg1);
                    return;
                case FaceSensorManager.MSG_CALLBACK_INFRARED /*1002*/:
                    FaceSensorManager.this.onCallbackEvent(2, msg.arg1);
                    return;
                case FaceSensorManager.MSG_NOTIFY_WORK_STATE /*1003*/:
                    FaceSensorManager.this.notifyWorkStateInternal(((Boolean) msg.obj).booleanValue());
                    return;
                case FaceSensorManager.MSG_NOTIFY_SCREEN_STATE /*1004*/:
                    FaceSensorManager.this.notifyScreenStateInternal(((Boolean) msg.obj).booleanValue());
                    return;
                case FaceSensorManager.MSG_NOTIFY_FINGER_STATE /*1005*/:
                    FaceSensorManager.this.notifyFaceStateInternal(((Boolean) msg.obj).booleanValue());
                    return;
                default:
                    return;
            }
        }
    }

    private FaceSensorManager(Context context) {
        this.mContext = context;
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mFaceHandler = new FaceHandler(this.mHandlerThread.getLooper());
        this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mInfraredPdIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_INFRARED_TIMER), 0);
        mTypeMotionDetect = getSensorType("TYPE_VIVOMOTION_DETECT", 91);
    }

    public static FaceSensorManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (mLock) {
                if (mInstance == null) {
                    mInstance = new FaceSensorManager(context);
                }
            }
        }
        return mInstance;
    }

    public void handleError() {
        startViber();
    }

    private void startViber() {
        if (this.mVibrator != null) {
            try {
                this.mVibrator.vibrate(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void releaseResource() {
        if (this.mVibrator != null) {
            this.mVibrator.cancel();
        }
    }

    public void shutdownLcd() {
        controlHBMInternal("5");
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x008e A:{SYNTHETIC, Splitter: B:22:0x008e} */
    /* JADX WARNING: Removed duplicated region for block: B:52:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0093 A:{SYNTHETIC, Splitter: B:25:0x0093} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x008e A:{SYNTHETIC, Splitter: B:22:0x008e} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0093 A:{SYNTHETIC, Splitter: B:25:0x0093} */
    /* JADX WARNING: Removed duplicated region for block: B:52:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d2 A:{SYNTHETIC, Splitter: B:33:0x00d2} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00d7 A:{SYNTHETIC, Splitter: B:36:0x00d7} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d2 A:{SYNTHETIC, Splitter: B:33:0x00d2} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00d7 A:{SYNTHETIC, Splitter: B:36:0x00d7} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void controlHBMInternal(String value) {
        Exception e;
        Throwable th;
        BufferedWriter bufWriter = null;
        FileWriter fw = null;
        try {
            FileWriter fw2 = new FileWriter(NODE_HBM);
            try {
                BufferedWriter bufWriter2 = new BufferedWriter(fw2);
                try {
                    Log.d(TAG, "controlHBM number: " + value);
                    bufWriter2.write(value);
                    if (bufWriter2 != null) {
                        try {
                            bufWriter2.close();
                        } catch (Exception e2) {
                            Log.e(TAG, "controlHBM close bufferedwriter failed: " + e2);
                        }
                    }
                    if (fw2 != null) {
                        try {
                            fw2.close();
                        } catch (Exception e22) {
                            Log.e(TAG, "controlHBM close file write e: " + e22);
                        }
                    }
                    bufWriter = bufWriter2;
                } catch (Exception e3) {
                    e22 = e3;
                    fw = fw2;
                    bufWriter = bufWriter2;
                    try {
                        Log.e(TAG, "controlHBM e: " + e22);
                        if (bufWriter != null) {
                        }
                        if (fw != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufWriter != null) {
                        }
                        if (fw != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fw = fw2;
                    bufWriter = bufWriter2;
                    if (bufWriter != null) {
                    }
                    if (fw != null) {
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e22 = e4;
                fw = fw2;
                Log.e(TAG, "controlHBM e: " + e22);
                if (bufWriter != null) {
                }
                if (fw != null) {
                }
            } catch (Throwable th4) {
                th = th4;
                fw = fw2;
                if (bufWriter != null) {
                    try {
                        bufWriter.close();
                    } catch (Exception e222) {
                        Log.e(TAG, "controlHBM close bufferedwriter failed: " + e222);
                    }
                }
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (Exception e2222) {
                        Log.e(TAG, "controlHBM close file write e: " + e2222);
                    }
                }
                throw th;
            }
        } catch (Exception e5) {
            e2222 = e5;
            Log.e(TAG, "controlHBM e: " + e2222);
            if (bufWriter != null) {
                try {
                    bufWriter.close();
                } catch (Exception e22222) {
                    Log.e(TAG, "controlHBM close bufferedwriter failed: " + e22222);
                }
            }
            if (fw != null) {
                try {
                    fw.close();
                } catch (Exception e222222) {
                    Log.e(TAG, "controlHBM close file write e: " + e222222);
                }
            }
        }
    }

    private int getSensorType(String type, int defaultValue) {
        try {
            Class<?> sensorClass = Class.forName("android.hardware.Sensor");
            return sensorClass.getDeclaredField(type).getInt(sensorClass);
        } catch (Exception e) {
            Log.e(TAG, "getSensorType: " + e.getMessage());
            return defaultValue;
        }
    }

    private void startInfraredTimer() {
        if (this.mAlarmManager != null && !this.mInfTimerStarted) {
            this.mInfTimerStarted = true;
            setAlarmClock(7000, this.mInfraredPdIntent);
        }
    }

    private void cancelInfraredTimer() {
        if (this.mInfTimerStarted) {
            this.mInfTimerStarted = false;
            cancelPendingIntent(this.mInfraredPdIntent);
        }
    }

    private void handleTimerReceiver(boolean screenOn) {
        if (!screenOn) {
            this.mReceiverRegistered = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_INFRARED_TIMER);
            this.mContext.registerReceiver(this.mTimerReceiver, intentFilter);
        } else if (this.mReceiverRegistered) {
            this.mReceiverRegistered = false;
            this.mContext.unregisterReceiver(this.mTimerReceiver);
        }
    }

    private void setAlarmClock(long startTimeMillis, PendingIntent pendingIntent) {
        AlarmClockInfo aCInfo = new AlarmClockInfo(Calendar.getInstance().getTimeInMillis() + startTimeMillis, pendingIntent);
        if (this.mAlarmManager != null) {
            this.mAlarmManager.setAlarmClock(aCInfo, pendingIntent);
        }
    }

    private void cancelPendingIntent(PendingIntent pendingIntent) {
        if (this.mAlarmManager != null && pendingIntent != null) {
            this.mAlarmManager.cancel(pendingIntent);
        }
    }

    private void notifyWorkStateInternal(boolean enterWork) {
        this.mEnterWork = enterWork;
        if (!this.mEnterWork || (this.mScreenOn ^ 1) == 0) {
            this.mInfraredClose = false;
            cancelInfraredTimer();
            unregisterInfraredListener();
            return;
        }
        this.mInfraredClose = false;
        registerInfraredListener();
    }

    public void notifyWorkState(boolean enterWork) {
        sendMessage(MSG_NOTIFY_WORK_STATE, 0, 0, Boolean.valueOf(enterWork));
    }

    private void notifyScreenStateInternal(boolean screenOn) {
        this.mScreenOn = screenOn;
        this.mFaceDown = false;
        handleTimerReceiver(screenOn);
        if (screenOn) {
            this.mInfraredClose = false;
            cancelInfraredTimer();
            unregisterInfraredListener();
        } else if (!screenOn) {
            this.mInfraredClose = false;
            registerInfraredListener();
        }
    }

    public void notifyScreenState(boolean screenOn) {
        sendMessage(MSG_NOTIFY_SCREEN_STATE, 0, 0, Boolean.valueOf(screenOn));
    }

    private void notifyFaceStateInternal(boolean faceDown) {
        Log.i(TAG, "notifyFaceStateInternal: " + this.mFaceDown + ":" + faceDown);
        if ((this.mFaceDown || !faceDown) && this.mFaceDown) {
            int i = faceDown ^ 1;
        }
        this.mFaceDown = faceDown;
    }

    public void notifyFaceState(boolean faceDown) {
        sendMessage(MSG_NOTIFY_FINGER_STATE, 0, 0, Boolean.valueOf(faceDown));
    }

    private void onCallbackEvent(int type, int state) {
        if (this.mTimerCallback != null) {
            this.mTimerCallback.onFaceTimer(type, state);
        } else {
            Log.w(TAG, "onCallbackEvent failed " + type);
        }
    }

    public void setSensorCallback(SensorCallback timerCallback) {
        this.mTimerCallback = timerCallback;
    }

    private void sendMessage(int what, int arg1, int arg2, Object obj) {
        if (this.mFaceHandler != null) {
            this.mFaceHandler.obtainMessage(what, arg1, arg2, obj).sendToTarget();
        } else {
            Log.w(TAG, "sendMessage failed " + what);
        }
    }

    private void removeMessage(int what) {
        if (this.mFaceHandler != null && this.mFaceHandler.hasMessages(what)) {
            this.mFaceHandler.removeMessages(what);
        }
    }

    private void registerMotionListener() {
        boolean z;
        String str = TAG;
        StringBuilder append = new StringBuilder().append("registerMotionListener: ").append(this.mMotionLisRegistered).append(":");
        if (this.mSensorManager != null) {
            z = true;
        } else {
            z = false;
        }
        Log.i(str, append.append(z).toString());
    }

    private void unregisterMotionListener() {
        Log.i(TAG, "unregisterMotionListener: " + this.mMotionLisRegistered + ":" + (this.mSensorManager != null));
    }

    private void registerInfraredListener() {
        if (!PRODUCT_MODEL.endsWith("LG4")) {
            boolean z;
            String str = TAG;
            StringBuilder append = new StringBuilder().append("registerInfraredListener: ").append(this.mInfraredLisRegistered).append(":");
            if (this.mFaceHandler != null) {
                z = true;
            } else {
                z = false;
            }
            Log.i(str, append.append(z).toString());
            if (!this.mInfraredLisRegistered && this.mSensorManager != null) {
                Sensor proximitySensor = this.mSensorManager.getDefaultSensor(8);
                if (proximitySensor == null) {
                    this.mProximityThreshold = Math.min(1.0f, 5.0f);
                } else {
                    this.mProximityThreshold = Math.min(proximitySensor.getMaximumRange(), 5.0f);
                }
                if (this.mFaceHandler != null) {
                    this.mSensorManager.registerListener(this.mSensorEventListener, proximitySensor, 3, this.mFaceHandler);
                } else {
                    this.mSensorManager.registerListener(this.mSensorEventListener, proximitySensor, 3, new Handler());
                }
                this.mInfraredLisRegistered = true;
                this.mInfTimerOut = false;
            }
        }
    }

    private void unregisterInfraredListener() {
        if (!PRODUCT_MODEL.endsWith("LG4")) {
            Log.i(TAG, "unregisterInfraredListener: " + this.mInfraredLisRegistered);
            if (this.mInfraredLisRegistered && this.mSensorManager != null) {
                this.mSensorManager.unregisterListener(this.mSensorEventListener);
                if (this.mInfTimerOut) {
                    sendMessage(MSG_CALLBACK_INFRARED, 2, 0, null);
                }
                this.mInfraredLisRegistered = false;
                this.mInfTimerOut = false;
            }
        }
    }

    private String messageToString(Message msg) {
        String message = "unknow message";
        switch (msg.what) {
            case 1001:
                return "MSG_CALLBACK_MOVE_WAKE : " + (1 == msg.arg1 ? "sensor still" : "sensor move");
            case MSG_CALLBACK_INFRARED /*1002*/:
                return "MSG_CALLBACK_INFRARED: " + (1 == msg.arg1 ? "InfraredNear" : "InfraredFar");
            case MSG_NOTIFY_WORK_STATE /*1003*/:
                return "MSG_NOTIFY_WORK_STATE : " + (((Boolean) msg.obj).booleanValue() ? "enter work" : "exit work");
            case MSG_NOTIFY_SCREEN_STATE /*1004*/:
                return "MSG_NOTIFY_SCREEN_STATE:" + (((Boolean) msg.obj).booleanValue() ? "screen on" : "screen off");
            case MSG_NOTIFY_FINGER_STATE /*1005*/:
                return "MSG_NOTIFY_FINGER_STATE : " + (((Boolean) msg.obj).booleanValue() ? "fingerprint down" : "fingerprint up");
            default:
                return message;
        }
    }
}
