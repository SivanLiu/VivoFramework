package com.vivo.services.facedetect;

import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
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
import android.os.Vibrator;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Calendar;

public class FaceDetectDoze {
    private static final int ICON_LOCKOUT_TIMEOUT_MS = 1800000;
    private static final String INTENT_FACEKEY_MOTION_TIMER = "android.intent.action.ALARM_FACEKEY_MOTION_TIME";
    private static final String INTENT_UD_ALARM_TIMER = "android.intent.action.ALARM_UD_FACE_TIME";
    private static final int MOTION_STATE_MOVE = 2;
    private static final int MOTION_STATE_STILL = 1;
    private static final int MSG_CALLBACK_MOVE_WAKE = 1000;
    private static final int MSG_CALLBACK_TIMEOUT = 1003;
    private static final int MSG_NOTIFY_INFARED = 1002;
    private static final String NODE_HBM = "/sys/lcm/oled_hbm";
    private static final int PROXIMITY_FAR = 2;
    private static final int PROXIMITY_NEAR = 3;
    private static final int PROXIMITY_TIMEOUT = 1;
    private static final int PROXIMITY_TIMEOUT_MS = 7000;
    private static final String TAG = "FaceDetectService";
    private static int TYPE_MOTION_DETECT = 91;
    private boolean isRegisterSensorMotion = false;
    private AlarmManager mAlarmManager;
    private ContentResolver mContentRv = null;
    private Context mContext = null;
    private FaceSensorCallback mFaceSensorCallback;
    private FaceSensorEventListener mFaceSensorEventListener;
    private HandlerThread mHandlerThread;
    private boolean mHasStarted = false;
    private boolean mIsLightLow;
    private PendingIntent mMotionPendingIntent = null;
    private MotionReceiver mMotionReceiver = null;
    private boolean mMotionTimerRegister = false;
    private boolean mMotionTimerStarted = false;
    private PendingIntent mPendingIntent;
    private float mProximityThreshold;
    private UDBroadcastReceiver mReceiver;
    private SensorManager mSensorManager;
    private SensorManagerHandler mSensorManagerHandler;
    private boolean mTimerStarted = false;
    private Vibrator mVibrator = null;

    public static abstract class FaceSensorCallback {
        public void onFaceSensor(int sensorType, int sensorState) {
        }
    }

    private class FaceSensorEventListener implements SensorEventListener {
        /* synthetic */ FaceSensorEventListener(FaceDetectDoze this$0, FaceSensorEventListener -this1) {
            this();
        }

        private FaceSensorEventListener() {
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            int sensorType = event.sensor.getType();
            Log.i(FaceDetectDoze.TAG, "sensorType is: " + sensorType);
            if (sensorType == FaceDetectDoze.TYPE_MOTION_DETECT) {
                int state = (int) Float.valueOf(event.values[0]).floatValue();
                Log.i(FaceDetectDoze.TAG, "motion state changed: " + state);
                FaceDetectDoze.this.sendMessage(FaceDetectDoze.MSG_CALLBACK_MOVE_WAKE, state, 0, null);
            } else if (sensorType == 8) {
                float distance = Float.valueOf(event.values[0]).floatValue();
                Log.i(FaceDetectDoze.TAG, "distance is: " + distance);
                FaceDetectDoze.this.sendMessage(FaceDetectDoze.MSG_NOTIFY_INFARED, 0, 0, Float.valueOf(distance));
            }
        }
    }

    private class MotionReceiver extends BroadcastReceiver {
        /* synthetic */ MotionReceiver(FaceDetectDoze this$0, MotionReceiver -this1) {
            this();
        }

        private MotionReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (FaceDebugConfig.DEBUG) {
                Log.d(FaceDetectDoze.TAG, "motion has received");
            }
            FaceDetectDoze.this.cancelMotionTimer();
            FaceDetectDoze.this.mFaceSensorCallback.onFaceSensor(FaceDetectDoze.TYPE_MOTION_DETECT, 1);
        }
    }

    private class SensorManagerHandler extends Handler {
        public SensorManagerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FaceDetectDoze.MSG_CALLBACK_MOVE_WAKE /*1000*/:
                    FaceDetectDoze.this.handleSensorState(msg.arg1);
                    return;
                case FaceDetectDoze.MSG_NOTIFY_INFARED /*1002*/:
                    FaceDetectDoze.this.handleProximityState(((Float) msg.obj).floatValue());
                    return;
                case FaceDetectDoze.MSG_CALLBACK_TIMEOUT /*1003*/:
                    if (FaceDetectDoze.this.mFaceSensorCallback != null) {
                        FaceDetectDoze.this.mFaceSensorCallback.onFaceSensor(8, 1);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class UDBroadcastReceiver extends BroadcastReceiver {
        /* synthetic */ UDBroadcastReceiver(FaceDetectDoze this$0, UDBroadcastReceiver -this1) {
            this();
        }

        private UDBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Log.i(FaceDetectDoze.TAG, "onReceive: time point reached");
            FaceDetectDoze.this.mFaceSensorCallback.onFaceSensor(8, 1);
            FaceDetectDoze.this.cancelFaceTimer();
        }
    }

    public FaceDetectDoze(Context context) {
        this.mContext = context;
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mSensorManagerHandler = new SensorManagerHandler(this.mHandlerThread.getLooper());
        this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        TYPE_MOTION_DETECT = getSensorType();
        this.mReceiver = new UDBroadcastReceiver(this, null);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(INTENT_UD_ALARM_TIMER), 0);
        this.mMotionPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(INTENT_FACEKEY_MOTION_TIMER), 0);
        this.mMotionReceiver = new MotionReceiver(this, null);
        this.mFaceSensorEventListener = new FaceSensorEventListener(this, null);
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

    private void handleProximityState(float distance) {
        boolean mInfraredPositive = distance >= 0.0f && distance < this.mProximityThreshold;
        if (mInfraredPositive) {
            startFaceTimer();
            return;
        }
        cancelFaceTimer();
        if (this.mFaceSensorCallback != null) {
            this.mFaceSensorCallback.onFaceSensor(8, 2);
        }
    }

    public void registerMotionListener() {
        if (!this.isRegisterSensorMotion) {
            this.mIsLightLow = false;
            Log.i(TAG, "register motion lister TYPE_MOTION_DETECT is: " + TYPE_MOTION_DETECT);
            this.isRegisterSensorMotion = true;
            this.mSensorManager.registerListener(this.mFaceSensorEventListener, this.mSensorManager.getDefaultSensor(TYPE_MOTION_DETECT), 0, this.mSensorManagerHandler);
            Sensor proximitySensor = this.mSensorManager.getDefaultSensor(8);
            if (proximitySensor != null) {
                Log.i(TAG, "register proximitySensor != null");
                this.mProximityThreshold = Math.min(proximitySensor.getMaximumRange(), 5.0f);
                this.mSensorManager.registerListener(this.mFaceSensorEventListener, proximitySensor, 2, this.mSensorManagerHandler);
                Log.i(TAG, "register proximitySensor != null finish");
                return;
            }
            Log.i(TAG, "register proximitySensor == null");
        }
    }

    public void unregisterMotionListener() {
        this.mIsLightLow = false;
        if (this.isRegisterSensorMotion) {
            Log.i(TAG, "unregister motion lister");
            this.isRegisterSensorMotion = false;
            cancelFaceTimer();
            cancelMotionTimer();
            this.mSensorManager.unregisterListener(this.mFaceSensorEventListener);
        }
    }

    private void startFaceTimer() {
        if (!this.mTimerStarted && this.mAlarmManager != null) {
            Log.i(TAG, "start face timer");
            this.mTimerStarted = true;
            registerTimerReceiver();
            this.mAlarmManager.setAlarmClock(new AlarmClockInfo(Calendar.getInstance().getTimeInMillis() + 7000, this.mPendingIntent), this.mPendingIntent);
        }
    }

    private void registerTimerReceiver() {
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter(INTENT_UD_ALARM_TIMER), null, null);
    }

    private void cancelFaceTimer() {
        Log.i(TAG, "cancelFaceTimer: " + this.mTimerStarted);
        if (this.mTimerStarted) {
            this.mTimerStarted = false;
            this.mContext.unregisterReceiver(this.mReceiver);
            if (this.mAlarmManager != null && this.mPendingIntent != null) {
                this.mAlarmManager.cancel(this.mPendingIntent);
            }
        }
    }

    public void setFaceSensorCallback(FaceSensorCallback mFaceSensorCallback) {
        this.mFaceSensorCallback = mFaceSensorCallback;
    }

    private int getSensorType() {
        try {
            Class<?> sensorClass = Class.forName("android.hardware.Sensor");
            return sensorClass.getDeclaredField("TYPE_VIVOMOTION_DETECT").getInt(sensorClass);
        } catch (Exception e) {
            e.printStackTrace();
            return 91;
        }
    }

    private void handleSensorState(int sensorState) {
        Log.d(TAG, "handleSensorState ......");
        if (this.mFaceSensorCallback != null) {
            if (sensorState == 2) {
                cancelMotionTimer();
                this.mFaceSensorCallback.onFaceSensor(TYPE_MOTION_DETECT, sensorState);
            } else {
                startMotionTimer();
            }
        }
    }

    private void sendMessage(int what, int arg1, int arg2, Object obj) {
        this.mSensorManagerHandler.obtainMessage(what, arg1, arg2, obj).sendToTarget();
    }

    private void removeMessage(int what) {
        this.mSensorManagerHandler.removeMessages(what);
    }

    private void startMotionTimer() {
        if (FaceDebugConfig.DEBUG) {
            Log.d(TAG, "motion has started");
        }
        if (!this.mMotionTimerStarted && this.mAlarmManager != null) {
            this.mMotionTimerStarted = true;
            registerMotionReceiver();
            this.mAlarmManager.setAlarmClock(new AlarmClockInfo(Calendar.getInstance().getTimeInMillis() + 7000, this.mMotionPendingIntent), this.mMotionPendingIntent);
        }
    }

    private void registerMotionReceiver() {
        this.mContext.registerReceiver(this.mMotionReceiver, new IntentFilter(INTENT_FACEKEY_MOTION_TIMER), null, null);
    }

    private void cancelMotionTimer() {
        if (FaceDebugConfig.DEBUG) {
            Log.d(TAG, "motion has canceled");
        }
        if (this.mMotionTimerStarted) {
            this.mMotionTimerStarted = false;
            this.mContext.unregisterReceiver(this.mMotionReceiver);
            if (this.mAlarmManager != null && this.mMotionPendingIntent != null) {
                this.mAlarmManager.cancel(this.mMotionPendingIntent);
            }
        }
    }
}
