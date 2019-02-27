package com.vivo.services.motion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.vivo.services.motion.gesture.GestureDetectCustom;
import com.vivo.services.motion.gesture.GestureDetector;
import com.vivo.services.motion.gesture.MotionDetect;
import com.vivo.services.motion.gesture.MotionTestView;

public class GestureService implements IMotionRecognitionService {
    private static final int DETECT_TYPE_ANDROID = 2;
    private static final int DETECT_TYPE_CUSTOM = 1;
    private static final int MAX_AUTOSTOP_CNT = 10;
    private static final int MAX_SAMPLE = 1000;
    private static final int MSG_GESTURE_ERROR_TRIGER = 0;
    private static final int MSG_GESTURE_M_TRIGER = 1;
    private static final int MSG_GESTURE_S_TRIGER = 2;
    private static final int MSG_GESTURE_V_TRIGER = 3;
    private static final String TAG = "GestureService";
    private static final int TYPE_AIRWAKE_VECTOR = 21;
    private static final float TYPE_AIRWAKE_VECTOR_END = 99.0f;
    private static final float TYPE_AIRWAKE_VECTOR_ERROR = 88.0f;
    private static int detectType = 2;
    private static int letterNum = 0;
    private static Handler mCallBackHandler = null;
    private static MotionDetect mDetect = null;
    private static GestureDetectCustom mGestureDetectCustom = null;
    private static GestureDetector mGestureDetector = null;
    private static GestureService mSingleGestureService = new GestureService();
    private static MotionTestView mTestView = null;
    private boolean isGestureServiceWorking = false;
    private MotionSensorEventListener mAirWakeVector = new MotionSensorEventListener(this, null);
    private int mAutoStopCnt = 0;
    private boolean mAutoStopOn = false;
    private boolean mCapturing = false;
    private Context mContext = null;
    private MotionSensorEventListener mLinearAcceleration;
    private MotionSensorEventListener mRotationVector;
    private SensorManager mSensorManager;
    private Handler mServiceHandler = null;

    private class GestureServiceHandler extends Handler {
        public GestureServiceHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            Message smsg = Message.obtain();
            smsg.what = 16;
            switch (msg.what) {
                case 1:
                    smsg.obj = new Integer(81);
                    if (GestureService.mCallBackHandler != null) {
                        GestureService.mCallBackHandler.sendMessage(smsg);
                    }
                    Log.d(GestureService.TAG, "GESTIRE_TYPE_M");
                    return;
                case 2:
                    smsg.obj = new Integer(82);
                    if (GestureService.mCallBackHandler != null) {
                        GestureService.mCallBackHandler.sendMessage(smsg);
                    }
                    Log.d(GestureService.TAG, "GESTIRE_TYPE_S");
                    return;
                case 3:
                    smsg.obj = new Integer(83);
                    if (GestureService.mCallBackHandler != null) {
                        GestureService.mCallBackHandler.sendMessage(smsg);
                    }
                    Log.d(GestureService.TAG, "GESTIRE_TYPE_V");
                    return;
                default:
                    smsg.obj = new Integer(84);
                    if (GestureService.mCallBackHandler != null) {
                        GestureService.mCallBackHandler.sendMessage(smsg);
                    }
                    Log.d(GestureService.TAG, "GESTURE_TYPE_ERROR");
                    return;
            }
        }
    }

    private class MotionSensorEventListener implements SensorEventListener {
        /* synthetic */ MotionSensorEventListener(GestureService this$0, MotionSensorEventListener -this1) {
            this();
        }

        private MotionSensorEventListener() {
        }

        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case 10:
                case 11:
                case 21:
                    if (!GestureService.this.mCapturing) {
                        return;
                    }
                    if (!GestureService.this.mAutoStopOn) {
                        GestureService.mDetect.onSensorChanged(event);
                        return;
                    } else if (event.values[3] == GestureService.TYPE_AIRWAKE_VECTOR_END || event.values[7] == GestureService.TYPE_AIRWAKE_VECTOR_END || event.values[11] == GestureService.TYPE_AIRWAKE_VECTOR_END || event.values[15] == GestureService.TYPE_AIRWAKE_VECTOR_END) {
                        Log.d(GestureService.TAG, "auto stop nomal");
                        GestureService.this.stopMotionRecognitionServiceInternel();
                        return;
                    } else if (event.values[3] == GestureService.TYPE_AIRWAKE_VECTOR_ERROR && event.values[7] == GestureService.TYPE_AIRWAKE_VECTOR_ERROR && event.values[11] == GestureService.TYPE_AIRWAKE_VECTOR_ERROR && event.values[15] == GestureService.TYPE_AIRWAKE_VECTOR_ERROR) {
                        GestureService gestureService = GestureService.this;
                        gestureService.mAutoStopCnt = gestureService.mAutoStopCnt + 1;
                        if (GestureService.this.mAutoStopCnt > 10) {
                            Log.d(GestureService.TAG, "auto stop time out");
                            GestureService.this.stopMotionRecognitionServiceInternel();
                            return;
                        }
                        return;
                    } else {
                        GestureService.mDetect.onSensorChanged(event);
                        return;
                    }
                default:
                    return;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private void setCapturing(boolean paramBoolean) {
        Log.i(TAG, "setCapturing " + paramBoolean + ":" + this.mCapturing);
        if (this.mCapturing != paramBoolean) {
            this.mCapturing = paramBoolean;
            if (this.mCapturing) {
                mDetect.start();
            } else {
                mDetect.stop();
                mDetect.result();
                mTestView.mPath2d = mDetect.mPathBuffer2D;
                mTestView.mPath3d = mDetect.mPathBuffer3D;
                mTestView.mNormal = mDetect.mNormalVectors;
            }
        }
    }

    private int gestureResult() {
        setCapturing(false);
        if (detectType == 1) {
            mGestureDetectCustom.reset();
            mTestView.normalsize();
            if (mTestView.mPath2dNomal == null || mTestView.mPath2d.size() <= 10) {
                Log.d(TAG, "+++++gesture trace too short,return+++++" + letterNum);
                return letterNum;
            }
            for (int i = 0; i < mTestView.mPath2dNomal.length; i++) {
                Log.d(TAG, " mTestView.mPath2dNomal[i].getX(): " + mTestView.mPath2dNomal[i].getX() + " mTestView.mPath2dNomal[i].getY(): " + mTestView.mPath2dNomal[i].getY());
                mGestureDetectCustom.path2dData(mTestView.mPath2dNomal[i].getX(), mTestView.mPath2dNomal[i].getY(), i);
            }
            letterNum = mGestureDetectCustom.recognition();
        } else {
            mGestureDetector.process(mDetect.mPathBuffer2D);
            letterNum = mGestureDetector.recognition();
        }
        return letterNum;
    }

    public static void loadGestureLib() {
        GestureDetector.loadGestureLib();
    }

    public static GestureService getInstance() {
        return mSingleGestureService;
    }

    private GestureService() {
    }

    public boolean startMotionRecognitionService(Context context, Handler handler) {
        Log.d(TAG, "startMotionRecognitionService " + this.isGestureServiceWorking);
        letterNum = 0;
        this.mAutoStopCnt = 0;
        this.mAutoStopOn = false;
        if (mTestView == null) {
            mTestView = MotionTestView.getInstance(context);
        }
        if (mDetect == null) {
            mDetect = MotionDetect.getInstance(context);
        }
        if (mGestureDetectCustom == null) {
            mGestureDetectCustom = GestureDetectCustom.getInstance(context);
        }
        if (mGestureDetector == null) {
            mGestureDetector = GestureDetector.getInstance(context);
        }
        if (this.mSensorManager == null && context != null) {
            this.mSensorManager = (SensorManager) context.getSystemService("sensor");
        }
        if (!this.isGestureServiceWorking) {
            this.mContext = context;
            mCallBackHandler = handler;
            this.mServiceHandler = new GestureServiceHandler(handler.getLooper());
            this.isGestureServiceWorking = true;
            if (this.mSensorManager != null) {
                this.mSensorManager.registerListener(this.mAirWakeVector, this.mSensorManager.getDefaultSensor(21), 5000);
            }
            setCapturing(true);
        }
        return true;
    }

    private boolean stopMotionRecognitionServiceInternel() {
        Message msg = Message.obtain();
        Log.d(TAG, "stopMotionRecognitionServiceInternel working:" + this.isGestureServiceWorking + " total num:" + mDetect.sample_num);
        this.mAutoStopCnt = 0;
        this.mAutoStopOn = true;
        if (this.isGestureServiceWorking) {
            letterNum = gestureResult();
            if (mDetect.sample_num > 1000) {
                letterNum = 0;
            }
            Log.d(TAG, "letterNum: " + letterNum);
            if (letterNum == 1) {
                msg.what = 1;
            } else if (letterNum == 2) {
                msg.what = 2;
            } else if (letterNum == 3) {
                msg.what = 3;
            } else if (letterNum == 0) {
                msg.what = 0;
            }
            if (this.mServiceHandler != null) {
                this.mServiceHandler.sendMessage(msg);
            }
            this.isGestureServiceWorking = false;
            if (this.mSensorManager != null) {
                this.mSensorManager.unregisterListener(this.mAirWakeVector);
                Log.d(TAG, "sensor unregisterListener");
            }
        }
        this.mSensorManager = null;
        this.mServiceHandler = null;
        return true;
    }

    public boolean stopMotionRecognitionService() {
        Log.d(TAG, "stopMotionRecognitionService");
        if (mDetect.sample_num == 0) {
            stopMotionRecognitionServiceInternel();
        } else {
            this.mAutoStopOn = true;
        }
        return true;
    }
}
