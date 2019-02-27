package com.vivo.services.motion;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import com.vivo.common.autobrightness.AblConfig;
import com.vivo.common.provider.Calendar.Events;

public final class RaiseUpWakeService implements IMotionRecognitionService {
    private static boolean DBG = true;
    private static final String FACEUNLOCK_ENABLED = "faceunlock_enabled";
    private static final String FACEUNLOCK_START_WHEN_SCREENON = "faceunlock_start_when_screenon";
    private static final int MSG_RAISEUP_DET_START = 1;
    private static final int MSG_RAISEUP_DET_STOP = 2;
    private static final int MSG_RAISEUP_DET_TRIGER = 3;
    private static final int MSG_RAISEUP_FIFO_ENABLE = 5;
    private static final int MSG_RAISEUP_JUDGE = 6;
    private static final int MSG_RAISEUP_STOP_RAISEUP_DET = 4;
    private static final int MSG_RAISEUP_WAKEUP = 7;
    private static final String RAISEUP_SETTING_NAME = "bbk_raiseup_wake_enable_setting";
    private static final String TAG = "RaiseUpWakeService";
    private static int logic_value = 1;
    private static final Object mObjectLock = new Object();
    private static String mPlatformName = Events.DEFAULT_SORT_ORDER;
    private static int mRaiseupType = -1;
    private static RaiseUpWakeService mSingleRaiseUpWakeService = new RaiseUpWakeService();
    private static boolean raiseupSettingInit = false;
    private long accWorkingTimeCount = 0;
    private boolean isAccWorking = false;
    private boolean isAuthWorking = false;
    private boolean isRaiseUpWakeWorking = false;
    private boolean isRaiseupDriger = false;
    private boolean isRaiseupSettingInit = false;
    private WakeLock mAWakeLock = null;
    private Handler mCallBackHandler = null;
    private ContentResolver mContentRv = null;
    private Context mContext = null;
    private boolean mFaceUnlockEnable = false;
    private ContentObserver mFaceUnlockObserver;
    private boolean mFaceUnlockWhenScreenOn = false;
    private boolean mHasNotify = false;
    private MotionSensorEventListener mProxListener = new MotionSensorEventListener(this, null);
    private MotionSensorEventListener mRaiseupListener = new MotionSensorEventListener(this, null);
    private SensorManager mSensorManager;
    private Handler mServiceHandler = null;
    private Handler mWakeupHandler;
    private HandlerThread mWakeupThread;
    private long mraiseup_delta_ms = 0;
    private long mraiseup_event_ts = 0;
    private PowerManager pm = null;
    private boolean raiseupRegistered = false;
    private boolean raiseup_collectdata = false;
    private long start_acc = 0;
    private long stop_acc = 0;

    private class MotionSensorEventListener implements SensorEventListener {
        /* synthetic */ MotionSensorEventListener(RaiseUpWakeService this$0, MotionSensorEventListener -this1) {
            this();
        }

        private MotionSensorEventListener() {
        }

        public void onSensorChanged(SensorEvent event) {
            int i = 1;
            if (event.sensor.getType() == RaiseUpWakeService.mRaiseupType) {
                Log.d(RaiseUpWakeService.TAG, "raiseup value " + event.values[0] + " current prox status " + RaiseUpWakeService.logic_value);
                if (event.values[0] == 1.0f && RaiseUpWakeService.logic_value == 0) {
                    RaiseUpWakeService.this.mraiseup_event_ts = System.currentTimeMillis();
                    if (RaiseUpWakeService.this.mContext != null) {
                        AllConfig.collectRaiseUpException(RaiseUpWakeService.this.mContext);
                    }
                }
                if (event.values[0] == 1.0f && RaiseUpWakeService.logic_value == 1) {
                    raiseupWakeupSystem();
                } else if (event.values[0] == 9.0f && RaiseUpWakeService.this.raiseup_collectdata) {
                    if (event.values[2] == 1.0f) {
                        RaiseUpWakeService.this.isAccWorking = true;
                        RaiseUpWakeService.this.start_acc = SystemClock.elapsedRealtime();
                        Log.d(RaiseUpWakeService.TAG, "Raiseup collectdata ACC enable. time is " + RaiseUpWakeService.this.start_acc + " raiseup value " + event.values[0] + " " + event.values[2]);
                    } else if (event.values[2] == 0.0f) {
                        RaiseUpWakeService.this.stop_acc = SystemClock.elapsedRealtime();
                        Log.d(RaiseUpWakeService.TAG, "Raiseup collectdata ACC dsiable. time is " + RaiseUpWakeService.this.stop_acc + " raiseup value " + event.values[0] + " " + event.values[2]);
                        if (RaiseUpWakeService.this.isAccWorking) {
                            RaiseUpWakeService.this.accWorkingTimeCount = RaiseUpWakeService.this.stop_acc - RaiseUpWakeService.this.start_acc;
                            Log.d(RaiseUpWakeService.TAG, "Raiseup collectdata clACC accWorkingTimeCount is " + RaiseUpWakeService.this.accWorkingTimeCount);
                            RaiseUpWakeService.this.isAccWorking = false;
                        }
                    }
                } else if (event.values[0] == 2.0f && (RaiseUpWakeService.this.isRaiseupDriger ^ 1) != 0 && RaiseUpWakeService.this.raiseup_collectdata) {
                    Log.d(RaiseUpWakeService.TAG, "Raiseup collectdata  quicklyRecognition raiseup value " + event.values[0]);
                }
            } else if (event.sensor.getType() == 8) {
                long current_ts = System.currentTimeMillis();
                RaiseUpWakeService.mPlatformName = SystemProperties.get("ro.vivo.product.platform", null);
                Log.d(RaiseUpWakeService.TAG, "mPlatformName is " + RaiseUpWakeService.mPlatformName);
                if (RaiseUpWakeService.mPlatformName.equals("SDM710")) {
                    RaiseUpWakeService.this.mraiseup_delta_ms = 100;
                } else {
                    RaiseUpWakeService.this.mraiseup_delta_ms = 20;
                }
                Log.d(RaiseUpWakeService.TAG, "mraiseup_delta_ms " + RaiseUpWakeService.this.mraiseup_delta_ms + "ms current_ts " + current_ts + " mraiseup_event_ts " + RaiseUpWakeService.this.mraiseup_event_ts + " delta_ms " + (current_ts - RaiseUpWakeService.this.mraiseup_event_ts));
                if (RaiseUpWakeService.logic_value == 0 && event.values[0] > 0.0f && current_ts - RaiseUpWakeService.this.mraiseup_event_ts < RaiseUpWakeService.this.mraiseup_delta_ms) {
                    Log.d(RaiseUpWakeService.TAG, "new prox status " + event.values[0] + " current prox status " + RaiseUpWakeService.logic_value);
                    RaiseUpWakeService.this.mraiseup_event_ts = 0;
                    raiseupWakeupSystem();
                }
                if (event.values[0] <= 0.0f) {
                    i = 0;
                }
                RaiseUpWakeService.logic_value = i;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        private void raiseupWakeupSystem() {
            if (RaiseUpWakeService.this.raiseup_collectdata) {
                Log.d(RaiseUpWakeService.TAG, "Raiseup collectdata MSG_RAISEUP_DET_TRIGER");
                RaiseUpWakeService.this.isRaiseupDriger = true;
            }
            if (RaiseUpWakeService.this.mServiceHandler != null) {
                synchronized (RaiseUpWakeService.mObjectLock) {
                    if (!RaiseUpWakeService.this.mHasNotify) {
                        Log.d(RaiseUpWakeService.TAG, "onSensorChanged delay awake up mHasNotify = " + RaiseUpWakeService.this.mHasNotify + ", mFaceUnlockEnable = " + RaiseUpWakeService.this.mFaceUnlockEnable + ", mFaceUnlockWhenScreenOn = " + RaiseUpWakeService.this.mFaceUnlockWhenScreenOn);
                        RaiseUpWakeService.this.mHasNotify = true;
                        if (RaiseUpWakeService.this.mFaceUnlockEnable && RaiseUpWakeService.this.mFaceUnlockWhenScreenOn) {
                            if (RaiseUpWakeService.this.mWakeupHandler != null) {
                                RaiseUpWakeService.this.mWakeupHandler.sendEmptyMessageDelayed(7, 200);
                            } else {
                                Log.e(RaiseUpWakeService.TAG, "mWakeupHandler is null, unable to wakeup");
                            }
                        } else if (RaiseUpWakeService.this.mWakeupHandler != null) {
                            RaiseUpWakeService.this.mWakeupHandler.sendEmptyMessage(7);
                        } else {
                            Log.e(RaiseUpWakeService.TAG, "mWakeupHandler is null, can not wakeup");
                        }
                    }
                }
            }
        }
    }

    private class RaiseUpWakeServiceHandler extends Handler {
        public RaiseUpWakeServiceHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d(RaiseUpWakeService.TAG, "MSG_RAISEUP_DET_START");
                    return;
                case 2:
                    Log.d(RaiseUpWakeService.TAG, "MSG_RAISEUP_DET_STOP");
                    return;
                case 3:
                    Message smsg = Message.obtain();
                    smsg.what = 16;
                    smsg.obj = new Integer(25);
                    synchronized (RaiseUpWakeService.mObjectLock) {
                        if (RaiseUpWakeService.this.mCallBackHandler != null) {
                            RaiseUpWakeService.this.mCallBackHandler.sendMessage(smsg);
                        }
                        if (RaiseUpWakeService.this.mServiceHandler != null) {
                            Log.d(RaiseUpWakeService.TAG, "MSG_RAISEUP_STOP_RAISEUP_DET");
                            RaiseUpWakeService.this.mServiceHandler.removeMessages(4);
                            RaiseUpWakeService.this.mServiceHandler.sendEmptyMessage(4);
                        }
                    }
                    Log.d(RaiseUpWakeService.TAG, "MSG_RAISEUP_DET_TRIGER");
                    return;
                case 4:
                    String prop1 = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
                    if (RaiseUpWakeService.this.mSensorManager != null && RaiseUpWakeService.this.raiseupRegistered) {
                        RaiseUpWakeService.this.mSensorManager.unregisterListener(RaiseUpWakeService.this.mRaiseupListener);
                        RaiseUpWakeService.this.raiseupRegistered = false;
                    }
                    Log.d(RaiseUpWakeService.TAG, "MSG_RAISEUP_STOP_RAISEUP_DET");
                    return;
                case 7:
                    RaiseUpWakeService.this.handleWakeup();
                    return;
                default:
                    return;
            }
        }
    }

    public static RaiseUpWakeService getInstance() {
        return mSingleRaiseUpWakeService;
    }

    private RaiseUpWakeService() {
    }

    private static int getTypeRaiseupDetectValue() {
        try {
            Class<?> sensorClass = Class.forName("android.hardware.Sensor");
            return sensorClass.getDeclaredField("TYPE_RAISEUP_DETECT").getInt(sensorClass);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean startMotionRecognitionService(Context context, Handler handler) {
        Log.d(TAG, "startMotionRecognitionService RaiseupwakeService");
        if (this.raiseup_collectdata) {
            Log.d(TAG, "Raiseup collectdata startMotionRecognitionService RaiseupwakeService");
        }
        if (!this.isRaiseUpWakeWorking) {
            this.mContext = context;
            this.isRaiseUpWakeWorking = true;
            if (this.raiseup_collectdata) {
                this.accWorkingTimeCount = 0;
                this.isRaiseupDriger = false;
            }
            this.mContentRv = this.mContext.getContentResolver();
            this.mCallBackHandler = handler;
            this.mServiceHandler = new RaiseUpWakeServiceHandler(handler.getLooper());
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            this.pm = (PowerManager) this.mContext.getSystemService("power");
            this.mAWakeLock = this.pm.newWakeLock(805306394, TAG);
            if (this.mSensorManager != null) {
                mRaiseupType = getTypeRaiseupDetectValue();
                Log.i(TAG, "raiseup type = " + mRaiseupType);
                if (mRaiseupType > 0) {
                    this.mSensorManager.registerListener(this.mRaiseupListener, this.mSensorManager.getDefaultSensor(mRaiseupType), 500000);
                    this.raiseupRegistered = true;
                } else {
                    Log.e(TAG, "error raiseup type is not right ! type = " + mRaiseupType);
                    return false;
                }
            }
            this.mSensorManager.registerListener(this.mProxListener, this.mSensorManager.getDefaultSensor(8), 500000);
            updateFaceUnlock();
            this.mFaceUnlockObserver = new ContentObserver(handler) {
                public void onChange(boolean selfChange, Uri uri) {
                    boolean z = true;
                    RaiseUpWakeService raiseUpWakeService;
                    if (System.getUriFor("faceunlock_enabled").equals(uri)) {
                        if (RaiseUpWakeService.this.mContentRv != null) {
                            raiseUpWakeService = RaiseUpWakeService.this;
                            if (System.getInt(RaiseUpWakeService.this.mContentRv, "faceunlock_enabled", 0) != 1) {
                                z = false;
                            }
                            raiseUpWakeService.mFaceUnlockEnable = z;
                        } else {
                            RaiseUpWakeService.this.mFaceUnlockEnable = false;
                        }
                    } else if (System.getUriFor("faceunlock_start_when_screenon").equals(uri)) {
                        if (RaiseUpWakeService.this.mContentRv != null) {
                            raiseUpWakeService = RaiseUpWakeService.this;
                            if (System.getInt(RaiseUpWakeService.this.mContentRv, "faceunlock_start_when_screenon", 1) != 1) {
                                z = false;
                            }
                            raiseUpWakeService.mFaceUnlockWhenScreenOn = z;
                        } else {
                            RaiseUpWakeService.this.mFaceUnlockWhenScreenOn = false;
                        }
                    }
                    Log.d(RaiseUpWakeService.TAG, "face unlock state is faceunlock enable :" + RaiseUpWakeService.this.mFaceUnlockEnable + ", unlock when screenon = " + RaiseUpWakeService.this.mFaceUnlockWhenScreenOn);
                }
            };
            if (this.mContentRv != null) {
                this.mContentRv.registerContentObserver(System.getUriFor("faceunlock_enabled"), true, this.mFaceUnlockObserver);
                this.mContentRv.registerContentObserver(System.getUriFor("faceunlock_start_when_screenon"), true, this.mFaceUnlockObserver);
            }
        }
        Message msg = Message.obtain();
        msg.what = 1;
        if (this.mServiceHandler != null) {
            this.mServiceHandler.sendMessage(msg);
        }
        this.mWakeupThread = new HandlerThread("raiseup_wakeup");
        this.mWakeupThread.start();
        this.mWakeupHandler = new Handler(this.mWakeupThread.getLooper()) {
            public void handleMessage(Message msg) {
                Log.d(RaiseUpWakeService.TAG, "mWakeupThread start " + msg.what);
                switch (msg.what) {
                    case 7:
                        RaiseUpWakeService.this.handleWakeup();
                        break;
                }
                Log.d(RaiseUpWakeService.TAG, "mWakeupThread end " + msg.what);
            }
        };
        return true;
    }

    public boolean stopMotionRecognitionService() {
        Message msg = Message.obtain();
        msg.what = 2;
        if (this.mServiceHandler != null) {
            this.mServiceHandler.sendMessage(msg);
        }
        Log.d(TAG, "stopMotionRecognitionService " + this.isRaiseUpWakeWorking);
        if (this.raiseup_collectdata) {
            Log.d(TAG, "Raiseup collectdata stopMotionRecognitionService " + this.isRaiseUpWakeWorking);
        }
        if (this.isRaiseUpWakeWorking) {
            this.isRaiseUpWakeWorking = false;
            this.mServiceHandler.removeMessages(4);
            synchronized (mObjectLock) {
                this.mCallBackHandler = null;
                this.mServiceHandler = null;
            }
            if (!(this.mFaceUnlockObserver == null || this.mContentRv == null)) {
                this.mContentRv.unregisterContentObserver(this.mFaceUnlockObserver);
            }
            this.mFaceUnlockObserver = null;
            if (this.mSensorManager != null) {
                if (this.raiseupRegistered) {
                    this.mSensorManager.unregisterListener(this.mRaiseupListener);
                    this.raiseupRegistered = false;
                }
                if (this.raiseup_collectdata) {
                    this.stop_acc = SystemClock.elapsedRealtime();
                    Log.d(TAG, "Raiseup collectdata ACC dsiable. time is " + this.stop_acc + " raiseupRegistered " + this.raiseupRegistered);
                    if (this.isAccWorking) {
                        this.accWorkingTimeCount = this.stop_acc - this.start_acc;
                        Log.d(TAG, "Raiseup collectdata ACC accWorkingTimeCount is " + this.accWorkingTimeCount);
                        this.isAccWorking = false;
                    }
                }
                this.mSensorManager.unregisterListener(this.mProxListener);
            }
            this.mSensorManager = null;
            this.mHasNotify = false;
            if (this.mWakeupHandler != null) {
                this.mWakeupHandler.removeMessages(7);
            }
            if (this.mWakeupThread != null) {
                this.mWakeupThread.quitSafely();
            }
            this.mWakeupHandler = null;
            this.mWakeupThread = null;
        }
        return true;
    }

    private void handleWakeup() {
        Log.d(TAG, "handleWakeup send MSG_RAISEUP_DET_TRIGER");
        this.mHasNotify = false;
        synchronized (mObjectLock) {
            if (this.mServiceHandler != null) {
                this.mServiceHandler.sendEmptyMessage(3);
            }
        }
        Log.d(TAG, "handleWakeup");
        this.mAWakeLock.acquire(1000);
        if (this.mSensorManager != null && this.raiseupRegistered) {
            this.mSensorManager.unregisterListener(this.mRaiseupListener);
            this.raiseupRegistered = false;
        }
    }

    private void updateFaceUnlock() {
        boolean z = true;
        if (this.mContext != null && this.mContentRv != null) {
            boolean z2;
            Log.d(TAG, "RAISEUP_SETTING_NAME raiseupSettingInit is " + raiseupSettingInit);
            if (!raiseupSettingInit) {
                raiseupSettingInit = true;
                this.isRaiseupSettingInit = System.getInt(this.mContentRv, RAISEUP_SETTING_NAME, 0) == 0;
                if (this.isRaiseupSettingInit) {
                    System.putInt(this.mContentRv, RAISEUP_SETTING_NAME, 0);
                    Log.d(TAG, "RAISEUP_SETTING_NAME value is " + System.getInt(this.mContentRv, RAISEUP_SETTING_NAME, 1));
                }
            }
            if (System.getInt(this.mContentRv, "faceunlock_enabled", 0) == 1) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.mFaceUnlockEnable = z2;
            if (System.getInt(this.mContentRv, "faceunlock_start_when_screenon", 1) != 1) {
                z = false;
            }
            this.mFaceUnlockWhenScreenOn = z;
            Log.d(TAG, "face unlock state is faceunlock enable :" + this.mFaceUnlockEnable + ", unlock when screenon = " + this.mFaceUnlockWhenScreenOn);
        }
    }
}
