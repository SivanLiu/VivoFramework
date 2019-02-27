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
import java.lang.reflect.Array;

public final class PhonePickUpService implements IMotionRecognitionService {
    private static final int GYRO_DATA_BUF = 25;
    private static final int MSG_PHONE_PICK_UP_DET_START = 1;
    private static final int MSG_PHONE_PICK_UP_DET_STOP = 2;
    private static final int MSG_PHONE_PICK_UP_DET_TRIGER = 3;
    private static final String TAG = "PhonePickUpService";
    private static PhonePickUpService mSinglePhonePickUpService = new PhonePickUpService();
    private MotionSensorEventListener accelerometerListener = new MotionSensorEventListener(this, null);
    private final float alpha = 0.99f;
    private boolean filterFlag = true;
    private float[] gravityValues = new float[3];
    private float[][] gyroDataBuf = ((float[][]) Array.newInstance(Float.TYPE, new int[]{4, 25}));
    private MotionSensorEventListener gyroscopeListener = new MotionSensorEventListener(this, null);
    private boolean isPhonePickUpWorking = false;
    private boolean isSensorOn = false;
    private Handler mCallBackHandler = null;
    private Context mContext = null;
    private PhonePickUpAnalyzer mPhonePickUpAnalyzer = new PhonePickUpAnalyzer();
    private SensorManager mSensorManager;
    private Handler mServiceHandler = null;

    private class MotionSensorEventListener implements SensorEventListener {
        /* synthetic */ MotionSensorEventListener(PhonePickUpService this$0, MotionSensorEventListener -this1) {
            this();
        }

        private MotionSensorEventListener() {
        }

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == 4) {
                PhonePickUpService.this.mPhonePickUpAnalyzer.gyroSensorJudge(event.values[0], event.values[1], event.values[2]);
            } else if (event.sensor.getType() == 1) {
                if (PhonePickUpService.this.filterFlag) {
                    PhonePickUpService.this.gravityValues[0] = event.values[0];
                    PhonePickUpService.this.gravityValues[1] = event.values[1];
                    PhonePickUpService.this.gravityValues[2] = event.values[2];
                    PhonePickUpService.this.filterFlag = false;
                }
                PhonePickUpService.this.gravityValues[0] = (PhonePickUpService.this.gravityValues[0] * 0.99f) + (event.values[0] * 0.00999999f);
                PhonePickUpService.this.gravityValues[1] = (PhonePickUpService.this.gravityValues[1] * 0.99f) + (event.values[1] * 0.00999999f);
                PhonePickUpService.this.gravityValues[2] = (PhonePickUpService.this.gravityValues[2] * 0.99f) + (event.values[2] * 0.00999999f);
                Log.d(PhonePickUpService.TAG, " gravityValues[0]: " + PhonePickUpService.this.gravityValues[0] + " gravityValues[1]: " + PhonePickUpService.this.gravityValues[1] + " gravityValues[2]: " + PhonePickUpService.this.gravityValues[2]);
                PhonePickUpService.this.mPhonePickUpAnalyzer.gSensorJudge(PhonePickUpService.this.gravityValues[0], PhonePickUpService.this.gravityValues[1], PhonePickUpService.this.gravityValues[2]);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private class PhonePickUpAnalyzer {
        private static final int INTERVAL_NUM = 10;
        private static final int SAMPLE_NUM = 3;
        private int countNum = 0;
        private float[] dataAve = new float[3];
        private float[][] dataBuf = ((float[][]) Array.newInstance(Float.TYPE, new int[]{3, 3}));
        private boolean dataFlag = true;
        private float[] dataLasAve = new float[3];
        private float[] dataSum = new float[3];
        private int phone_pick_up_count = 0;
        private boolean trigerFlag = true;

        public void reset() {
            this.countNum = 0;
            this.dataFlag = true;
            this.trigerFlag = true;
            this.phone_pick_up_count = 0;
        }

        private void gyroSensorJudge(float x, float y, float z) {
            if (((double) Math.abs(x)) > 10.0d || ((double) Math.abs(y)) > 10.0d || ((double) Math.abs(z)) > 10.0d) {
                this.phone_pick_up_count = 0;
                Log.d(PhonePickUpService.TAG, "gyroscope data error.");
            }
            Log.d(PhonePickUpService.TAG, "gyro_x:" + x + " gyro_y:" + y + " gyro_z:" + z + " phone_pick_up_count:" + this.phone_pick_up_count);
            if (((double) Math.abs(x)) > 1.2d || ((double) Math.abs(y)) > 1.0d || ((double) Math.abs(z)) > 1.0d) {
                this.phone_pick_up_count++;
            } else {
                this.phone_pick_up_count = 0;
            }
            if (this.phone_pick_up_count > 3 && PhonePickUpService.this.mServiceHandler != null) {
                this.phone_pick_up_count = 0;
                PhonePickUpService.this.mServiceHandler.sendEmptyMessage(3);
            }
        }

        private void gSensorJudge(float x, float y, float z) {
            int i;
            for (i = 2; i > 0; i--) {
                this.dataBuf[0][i] = this.dataBuf[0][i - 1];
                this.dataBuf[1][i] = this.dataBuf[1][i - 1];
                this.dataBuf[2][i] = this.dataBuf[2][i - 1];
            }
            this.dataBuf[0][0] = x;
            this.dataBuf[1][0] = y;
            this.dataBuf[2][0] = z;
            this.dataSum[0] = 0.0f;
            this.dataSum[1] = 0.0f;
            this.dataSum[2] = 0.0f;
            if (this.dataFlag) {
                for (i = 0; i < 3; i++) {
                    this.dataBuf[0][i] = x;
                    this.dataBuf[1][i] = y;
                    this.dataBuf[2][i] = z;
                }
                this.dataLasAve[0] = x;
                this.dataLasAve[1] = y;
                this.dataLasAve[2] = z;
                this.dataFlag = false;
            }
            for (i = 0; i < 3; i++) {
                this.dataSum[0] = this.dataSum[0] + this.dataBuf[0][i];
                this.dataSum[1] = this.dataSum[1] + this.dataBuf[1][i];
                this.dataSum[2] = this.dataSum[2] + this.dataBuf[2][i];
            }
            this.dataAve[0] = this.dataSum[0] / 3.0f;
            this.dataAve[1] = this.dataSum[1] / 3.0f;
            this.dataAve[2] = this.dataSum[2] / 3.0f;
            Log.d(PhonePickUpService.TAG, " Math.abs(dataAve[0] - dataLasAve[0]): " + Math.abs(this.dataAve[0] - this.dataLasAve[0]) + " Math.abs(dataAve[1] - dataLasAve[1]): " + Math.abs(this.dataAve[1] - this.dataLasAve[1]) + " Math.abs(dataAve[2] - dataLasAve[2]): " + Math.abs(this.dataAve[2] - this.dataLasAve[2]));
            if (((double) Math.abs(this.dataAve[2] - this.dataLasAve[2])) > 0.35d && (((double) Math.abs(this.dataAve[0] - this.dataLasAve[0])) > 0.4d || ((double) Math.abs(this.dataAve[1] - this.dataLasAve[1])) > 0.4d)) {
                reset();
                Log.d(PhonePickUpService.TAG, " trigerFlag: " + this.trigerFlag);
                if (PhonePickUpService.this.mServiceHandler != null && this.trigerFlag) {
                    PhonePickUpService.this.mServiceHandler.sendEmptyMessage(3);
                    this.trigerFlag = false;
                }
                this.countNum = 0;
                this.dataLasAve[0] = this.dataAve[0];
                this.dataLasAve[1] = this.dataAve[1];
                this.dataLasAve[2] = this.dataAve[2];
            }
            if (this.countNum > 10) {
                this.countNum = 0;
                this.dataLasAve[0] = this.dataAve[0];
                this.dataLasAve[1] = this.dataAve[1];
                this.dataLasAve[2] = this.dataAve[2];
                return;
            }
            this.countNum++;
        }
    }

    private class PhonePickUpServiceHandler extends Handler {
        public PhonePickUpServiceHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d(PhonePickUpService.TAG, "MSG_PHONE_PICK_UP_DET_START");
                    return;
                case 2:
                    Log.d(PhonePickUpService.TAG, "MSG_PHONE_PICK_UP_DET_STOP");
                    return;
                case 3:
                    Message smsg = Message.obtain();
                    smsg.what = 16;
                    smsg.obj = new Integer(20);
                    if (PhonePickUpService.this.mCallBackHandler != null) {
                        PhonePickUpService.this.mCallBackHandler.sendMessage(smsg);
                        Log.d(PhonePickUpService.TAG, "MSG_PHONE_PICK_UP_DET_TRIGER");
                    }
                    if (PhonePickUpService.this.mServiceHandler != null) {
                        PhonePickUpService.this.mServiceHandler.removeMessages(2);
                        PhonePickUpService.this.mServiceHandler.sendEmptyMessage(2);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public static PhonePickUpService getInstance() {
        return mSinglePhonePickUpService;
    }

    private PhonePickUpService() {
    }

    public static boolean isAccelerometerSupported(Context context) {
        if (((SensorManager) context.getSystemService("sensor")).getSensorList(1).size() > 0) {
            return true;
        }
        return false;
    }

    public static boolean isGyroscopeSupported(Context context) {
        if (((SensorManager) context.getSystemService("sensor")).getSensorList(4).size() > 0) {
            return true;
        }
        return false;
    }

    public boolean startMotionRecognitionService(Context context, Handler handler) {
        Log.d(TAG, "startMotionRecognitionService ");
        if (!this.isPhonePickUpWorking) {
            this.mContext = context;
            this.filterFlag = true;
            this.isPhonePickUpWorking = true;
            this.mCallBackHandler = handler;
            this.mPhonePickUpAnalyzer.reset();
            this.mServiceHandler = new PhonePickUpServiceHandler(handler.getLooper());
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            if (this.mSensorManager != null) {
                if (isGyroscopeSupported(this.mContext)) {
                    this.mSensorManager.registerListener(this.gyroscopeListener, this.mSensorManager.getDefaultSensor(4), 1, this.mServiceHandler);
                    this.isSensorOn = true;
                } else if (isAccelerometerSupported(this.mContext)) {
                    this.mSensorManager.registerListener(this.accelerometerListener, this.mSensorManager.getDefaultSensor(1), 1, this.mServiceHandler);
                    this.isSensorOn = true;
                } else {
                    Log.d(TAG, "sensor not support this function");
                    return false;
                }
            }
        }
        Message msg = Message.obtain();
        msg.what = 1;
        if (this.mServiceHandler != null) {
            this.mServiceHandler.sendMessage(msg);
        }
        return true;
    }

    public boolean stopMotionRecognitionService() {
        Message msg = Message.obtain();
        msg.what = 2;
        if (this.mServiceHandler != null) {
            this.mServiceHandler.sendMessage(msg);
        }
        Log.d(TAG, "stopMotionRecognitionService " + this.isPhonePickUpWorking);
        if (this.isPhonePickUpWorking) {
            this.filterFlag = false;
            this.isPhonePickUpWorking = false;
            this.mServiceHandler.removeMessages(2);
            if (this.mSensorManager != null) {
                if (this.isSensorOn) {
                    if (isGyroscopeSupported(this.mContext)) {
                        this.mSensorManager.unregisterListener(this.gyroscopeListener);
                        this.isSensorOn = false;
                    } else if (isAccelerometerSupported(this.mContext)) {
                        this.mSensorManager.unregisterListener(this.accelerometerListener);
                        this.isSensorOn = false;
                    }
                }
                this.mSensorManager = null;
            }
            this.mCallBackHandler = null;
            this.mServiceHandler = null;
        }
        return true;
    }
}
