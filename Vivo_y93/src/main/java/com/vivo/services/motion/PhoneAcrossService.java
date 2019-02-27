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

public final class PhoneAcrossService implements IMotionRecognitionService {
    private static final int MSG_PHONE_AWAY_DET_TRIGER = 3;
    private static final int MSG_PHONE_CLOSE_DET_TRIGER = 2;
    private static final int MSG_PICK_UP_DET_TRIGER = 1;
    private static final String TAG = "PhoneAcrossService";
    private static PhoneAcrossService singlePhoneAcrossService = new PhoneAcrossService();
    private boolean aFilterFlag = true;
    private boolean aPickUpFlag = false;
    private boolean aPickUpStaticFlag = false;
    private MotionSensorEventListener accelerometerListener = new MotionSensorEventListener(this, null);
    private final float alpha1 = 0.99f;
    private final float alpha2 = 0.0f;
    private boolean gFilterFlag = true;
    private boolean gPickUpFlag = false;
    private float[] gravityValues = new float[3];
    private MotionSensorEventListener gyroscopeListener = new MotionSensorEventListener(this, null);
    private float[] gyroscopeValues = new float[3];
    private boolean isPhoneAcrossServiceWorking = false;
    private MotionSensorEventListener linearAccelerationListener = new MotionSensorEventListener(this, null);
    private Handler mCallBackHandler = null;
    private Context mContext = null;
    private DataAnalysis mDataAnalysis = new DataAnalysis(this, null);
    private MoveAnalyzer mMoveAnalyzer = new MoveAnalyzer();
    private SensorManager mSensorManager;
    private Handler mServiceHandler = null;
    private boolean pickUpTriger = true;

    private class DataAnalysis {
        private static final int GYRO_NUM1 = 15;
        private static final int GYRO_NUM2 = 6;
        private static final int LACC_MOVE_NUM = 5;
        private static final int LACC_STATIC_NUM = 5;
        private static final int MOTION_DET_GYRO_SAMPLE_TOTAL = 100;
        private static final int MOTION_DET_LACC_SAMPLE_TOTAL = 100;
        private double GYRO_THRESHOLD1;
        private double GYRO_THRESHOLD2;
        private int awayErrorNum1;
        private int awayErrorNum2;
        private int closeErrorNum1;
        private int closeErrorNum2;
        public float[][] gyro_data_collect_buf;
        private int gyroscopeAwayDataNum1;
        private int gyroscopeAwayDataNum2;
        private int gyroscopeAwayNumMax1;
        private int gyroscopeAwayNumMax2;
        private int gyroscopeCloseDataNum1;
        private int gyroscopeCloseDataNum2;
        private int gyroscopeCloseNumMax1;
        private int gyroscopeCloseNumMax2;
        private boolean gyroscopeFlag;
        private int gyroscopeState;
        private motionPara gyroscopeX;
        private motionPara gyroscopeY;
        private motionPara gyroscopeZ;
        public float[] lacc_data_collect_buf;
        private motionPara linearAcceleration;
        private int linearAccelerationState;
        Message msg;

        /* synthetic */ DataAnalysis(PhoneAcrossService this$0, DataAnalysis -this1) {
            this();
        }

        private DataAnalysis() {
            this.GYRO_THRESHOLD1 = 0.0d;
            this.GYRO_THRESHOLD2 = 2.0d;
            this.linearAcceleration = new motionPara(PhoneAcrossService.this, null);
            this.gyroscopeX = new motionPara(PhoneAcrossService.this, null);
            this.gyroscopeY = new motionPara(PhoneAcrossService.this, null);
            this.gyroscopeZ = new motionPara(PhoneAcrossService.this, null);
            this.lacc_data_collect_buf = new float[100];
            this.gyro_data_collect_buf = (float[][]) Array.newInstance(Float.TYPE, new int[]{100, 3});
            this.linearAccelerationState = 0;
            this.gyroscopeState = 0;
            this.gyroscopeCloseDataNum1 = 0;
            this.closeErrorNum1 = 0;
            this.gyroscopeAwayDataNum1 = 0;
            this.awayErrorNum1 = 0;
            this.gyroscopeCloseDataNum2 = 0;
            this.closeErrorNum2 = 0;
            this.gyroscopeAwayDataNum2 = 0;
            this.awayErrorNum2 = 0;
            this.gyroscopeCloseNumMax1 = 0;
            this.gyroscopeAwayNumMax1 = 0;
            this.gyroscopeCloseNumMax2 = 0;
            this.gyroscopeAwayNumMax2 = 0;
            this.gyroscopeFlag = true;
        }

        public void linearAccelerationAnalysis(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            int i;
            switch (this.linearAccelerationState) {
                case 0:
                    for (i = 99; i > 0; i--) {
                        this.lacc_data_collect_buf[i] = this.lacc_data_collect_buf[i - 1];
                    }
                    this.lacc_data_collect_buf[0] = (float) Math.sqrt((double) (((x * x) + (y * y)) + (z * z)));
                    this.linearAcceleration.maxVal = this.lacc_data_collect_buf[0];
                    this.linearAcceleration.minVal = this.lacc_data_collect_buf[0];
                    for (i = 1; i < 5; i++) {
                        if (this.linearAcceleration.maxVal < this.lacc_data_collect_buf[i]) {
                            this.linearAcceleration.maxVal = this.lacc_data_collect_buf[i];
                        }
                        if (this.linearAcceleration.minVal > this.lacc_data_collect_buf[i]) {
                            this.linearAcceleration.minVal = this.lacc_data_collect_buf[i];
                        }
                    }
                    Log.d(PhoneAcrossService.TAG, "maxminVal1:" + (this.linearAcceleration.maxVal - this.linearAcceleration.minVal));
                    if (((double) (this.linearAcceleration.maxVal - this.linearAcceleration.minVal)) > 1.3d) {
                        this.linearAccelerationState = 1;
                        Log.d(PhoneAcrossService.TAG, "+++++0+++++linearAccelerationState:" + this.linearAccelerationState);
                        return;
                    }
                    return;
                case 1:
                    for (i = 99; i > 0; i--) {
                        this.lacc_data_collect_buf[i] = this.lacc_data_collect_buf[i - 1];
                    }
                    this.lacc_data_collect_buf[0] = (float) Math.sqrt((double) (((x * x) + (y * y)) + (z * z)));
                    this.linearAcceleration.maxVal = this.lacc_data_collect_buf[0];
                    this.linearAcceleration.minVal = this.lacc_data_collect_buf[0];
                    for (i = 1; i < 5; i++) {
                        if (this.linearAcceleration.maxVal < this.lacc_data_collect_buf[i]) {
                            this.linearAcceleration.maxVal = this.lacc_data_collect_buf[i];
                        }
                        if (this.linearAcceleration.minVal > this.lacc_data_collect_buf[i]) {
                            this.linearAcceleration.minVal = this.lacc_data_collect_buf[i];
                        }
                    }
                    Log.d(PhoneAcrossService.TAG, " linearAcceleration.maxVal: " + this.linearAcceleration.maxVal + " linearAcceleration.minVal: " + this.linearAcceleration.minVal);
                    if (((double) this.linearAcceleration.maxVal) < 1.0d && ((double) this.linearAcceleration.minVal) < 0.5d) {
                        this.linearAccelerationState = 0;
                        Log.d(PhoneAcrossService.TAG, "+++++1+++++linearAccelerationState:" + this.linearAccelerationState);
                    }
                    if (this.linearAccelerationState == 0) {
                        for (i = 1; i < 100; i++) {
                            if (this.linearAcceleration.maxVal < this.lacc_data_collect_buf[i]) {
                                this.linearAcceleration.maxVal = this.lacc_data_collect_buf[i];
                            }
                            if (this.linearAcceleration.minVal > this.lacc_data_collect_buf[i]) {
                                this.linearAcceleration.minVal = this.lacc_data_collect_buf[i];
                            }
                        }
                        Log.d(PhoneAcrossService.TAG, "maxminVal2:" + (this.linearAcceleration.maxVal - this.linearAcceleration.minVal));
                        if (this.linearAcceleration.maxVal - this.linearAcceleration.minVal > 3.0f) {
                            PhoneAcrossService.this.mDataAnalysis.gyroscopeJudge();
                            if (this.gyroscopeState == 1) {
                                this.gyroscopeState = 0;
                                if (PhoneAcrossService.this.mServiceHandler != null) {
                                    PhoneAcrossService.this.mServiceHandler.sendEmptyMessage(2);
                                }
                            } else if (this.gyroscopeState == 2) {
                                this.gyroscopeState = 0;
                                if (PhoneAcrossService.this.mServiceHandler != null) {
                                    PhoneAcrossService.this.mServiceHandler.sendEmptyMessage(3);
                                }
                            }
                        }
                        PhoneAcrossService.this.mDataAnalysis.reset();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        public void gyroscopeAnalysis(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            Log.d(PhoneAcrossService.TAG, " gyroscopeX: " + x + " Y: " + y + " Z: " + z);
            for (int i = 99; i > 0; i--) {
                this.gyro_data_collect_buf[i][0] = this.gyro_data_collect_buf[i - 1][0];
                this.gyro_data_collect_buf[i][1] = this.gyro_data_collect_buf[i - 1][1];
                this.gyro_data_collect_buf[i][2] = this.gyro_data_collect_buf[i - 1][2];
            }
            this.gyro_data_collect_buf[0][0] = x;
            this.gyro_data_collect_buf[0][1] = y;
            this.gyro_data_collect_buf[0][2] = z;
            if (this.gyroscopeFlag) {
                this.gyroscopeX.maxVal = x;
                this.gyroscopeY.maxVal = y;
                this.gyroscopeZ.maxVal = z;
                this.gyroscopeX.minVal = x;
                this.gyroscopeY.minVal = y;
                this.gyroscopeZ.minVal = z;
                this.gyroscopeFlag = false;
            }
            if (this.gyroscopeX.maxVal < x) {
                this.gyroscopeX.maxVal = x;
            }
            if (this.gyroscopeY.maxVal < y) {
                this.gyroscopeY.maxVal = y;
            }
            if (this.gyroscopeZ.maxVal < z) {
                this.gyroscopeZ.maxVal = z;
            }
            if (this.gyroscopeX.minVal > x) {
                this.gyroscopeX.minVal = x;
            }
            if (this.gyroscopeY.minVal > y) {
                this.gyroscopeY.minVal = y;
            }
            if (this.gyroscopeZ.minVal > z) {
                this.gyroscopeZ.minVal = z;
            }
            if ((((double) x) <= this.GYRO_THRESHOLD1 || ((double) z) <= this.GYRO_THRESHOLD1) && (((double) x) <= this.GYRO_THRESHOLD1 || ((double) z) >= (-this.GYRO_THRESHOLD1))) {
                this.closeErrorNum1++;
                if (this.closeErrorNum1 > 1) {
                    this.gyroscopeCloseDataNum1 = 0;
                    this.closeErrorNum1 = 0;
                }
            } else {
                this.gyroscopeCloseDataNum1++;
                if (this.gyroscopeCloseDataNum1 > this.gyroscopeCloseNumMax1) {
                    this.gyroscopeCloseNumMax1 = this.gyroscopeCloseDataNum1;
                }
            }
            if ((((double) x) >= (-this.GYRO_THRESHOLD1) || ((double) z) <= this.GYRO_THRESHOLD1) && (((double) x) >= (-this.GYRO_THRESHOLD1) || ((double) z) >= (-this.GYRO_THRESHOLD1))) {
                this.awayErrorNum1++;
                if (this.awayErrorNum1 > 1) {
                    this.gyroscopeAwayDataNum1 = 0;
                    this.awayErrorNum1 = 0;
                }
            } else {
                this.gyroscopeAwayDataNum1++;
                if (this.gyroscopeAwayDataNum1 > this.gyroscopeAwayNumMax1) {
                    this.gyroscopeAwayNumMax1 = this.gyroscopeAwayDataNum1;
                }
            }
            if ((((double) x) <= this.GYRO_THRESHOLD2 || ((double) z) <= this.GYRO_THRESHOLD2) && (((double) x) <= this.GYRO_THRESHOLD2 || ((double) z) >= (-this.GYRO_THRESHOLD2))) {
                this.closeErrorNum2++;
                if (this.closeErrorNum2 > 1) {
                    this.gyroscopeCloseDataNum2 = 0;
                    this.closeErrorNum2 = 0;
                }
            } else {
                this.gyroscopeCloseDataNum2++;
                if (this.gyroscopeCloseDataNum2 > this.gyroscopeCloseNumMax2) {
                    this.gyroscopeCloseNumMax2 = this.gyroscopeCloseDataNum2;
                }
            }
            if ((((double) x) >= (-this.GYRO_THRESHOLD2) || ((double) z) <= this.GYRO_THRESHOLD2) && (((double) x) >= (-this.GYRO_THRESHOLD2) || ((double) z) >= (-this.GYRO_THRESHOLD2))) {
                this.awayErrorNum2++;
                if (this.awayErrorNum2 > 1) {
                    this.gyroscopeAwayDataNum2 = 0;
                    this.awayErrorNum2 = 0;
                }
            } else {
                this.gyroscopeAwayDataNum2++;
                if (this.gyroscopeAwayDataNum2 > this.gyroscopeAwayNumMax2) {
                    this.gyroscopeAwayNumMax2 = this.gyroscopeAwayDataNum2;
                }
            }
            Log.d(PhoneAcrossService.TAG, " gyroscopeCloseDataNum1: " + this.gyroscopeCloseDataNum1 + " gyroscopeAwayDataNum1: " + this.gyroscopeAwayDataNum1 + " gyroscopeCloseDataNum2: " + this.gyroscopeCloseDataNum2 + " gyroscopeAwayDataNum2: " + this.gyroscopeAwayDataNum2);
        }

        public void gyroscopeJudge() {
            if (this.gyroscopeCloseNumMax1 >= 15 && this.gyroscopeCloseNumMax1 > this.gyroscopeAwayNumMax1) {
                this.gyroscopeState = 1;
            } else if (this.gyroscopeAwayNumMax1 >= 15 && this.gyroscopeAwayNumMax1 > this.gyroscopeCloseNumMax1) {
                this.gyroscopeState = 2;
            }
            if (this.gyroscopeCloseNumMax2 >= 6 && this.gyroscopeState == 0 && this.gyroscopeCloseNumMax2 > this.gyroscopeAwayNumMax2) {
                this.gyroscopeState = 1;
            } else if (this.gyroscopeAwayNumMax2 >= 6 && this.gyroscopeState == 0 && this.gyroscopeAwayNumMax2 > this.gyroscopeCloseNumMax2) {
                this.gyroscopeState = 2;
            }
            Log.d(PhoneAcrossService.TAG, " gyroscopeCloseNumMax1: " + this.gyroscopeCloseNumMax1 + " gyroscopeAwayNumMax1: " + this.gyroscopeAwayNumMax1 + " gyroscopeCloseNumMax2: " + this.gyroscopeCloseNumMax2 + " gyroscopeAwayNumMax2: " + this.gyroscopeAwayNumMax2);
        }

        public void reset() {
            this.linearAccelerationState = 0;
            this.gyroscopeState = 0;
            this.gyroscopeCloseDataNum1 = 0;
            this.closeErrorNum1 = 0;
            this.gyroscopeAwayDataNum1 = 0;
            this.awayErrorNum1 = 0;
            this.gyroscopeCloseDataNum2 = 0;
            this.closeErrorNum2 = 0;
            this.gyroscopeAwayDataNum2 = 0;
            this.awayErrorNum2 = 0;
            this.gyroscopeCloseNumMax1 = 0;
            this.gyroscopeAwayNumMax1 = 0;
            this.gyroscopeCloseNumMax2 = 0;
            this.gyroscopeAwayNumMax2 = 0;
            this.gyroscopeFlag = true;
            Log.d(PhoneAcrossService.TAG, "++++++++++++++++++++++++++++reset+++++++++++++++++++++++++++++++");
        }
    }

    private class MotionSensorEventListener implements SensorEventListener {
        private int pick_up_cnt;

        /* synthetic */ MotionSensorEventListener(PhoneAcrossService this$0, MotionSensorEventListener -this1) {
            this();
        }

        private MotionSensorEventListener() {
            this.pick_up_cnt = 0;
        }

        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case 1:
                    if (PhoneAcrossService.this.aPickUpFlag && PhoneAcrossService.this.gPickUpFlag && PhoneAcrossService.this.pickUpTriger) {
                        if (PhoneAcrossService.this.mServiceHandler != null) {
                            PhoneAcrossService.this.mServiceHandler.sendEmptyMessage(1);
                        }
                        PhoneAcrossService.this.pickUpTriger = false;
                    }
                    if (PhoneAcrossService.this.aFilterFlag) {
                        PhoneAcrossService.this.gravityValues[0] = event.values[0];
                        PhoneAcrossService.this.gravityValues[1] = event.values[1];
                        PhoneAcrossService.this.gravityValues[2] = event.values[2];
                        PhoneAcrossService.this.aFilterFlag = false;
                    }
                    PhoneAcrossService.this.gravityValues[0] = (PhoneAcrossService.this.gravityValues[0] * 0.99f) + (event.values[0] * 0.00999999f);
                    PhoneAcrossService.this.gravityValues[1] = (PhoneAcrossService.this.gravityValues[1] * 0.99f) + (event.values[1] * 0.00999999f);
                    PhoneAcrossService.this.gravityValues[2] = (PhoneAcrossService.this.gravityValues[2] * 0.99f) + (event.values[2] * 0.00999999f);
                    if (!PhoneAcrossService.this.aPickUpFlag || !PhoneAcrossService.this.aPickUpStaticFlag) {
                        PhoneAcrossService.this.mMoveAnalyzer.judge(PhoneAcrossService.this.gravityValues[0], PhoneAcrossService.this.gravityValues[1], PhoneAcrossService.this.gravityValues[2]);
                        return;
                    }
                    return;
                case 4:
                    if (!PhoneAcrossService.this.gPickUpFlag) {
                        if (Math.abs(event.values[0]) <= 1.0f || ((double) Math.abs(event.values[2])) <= 1.5d) {
                            this.pick_up_cnt = 0;
                        } else {
                            this.pick_up_cnt++;
                        }
                        if (this.pick_up_cnt >= 5) {
                            Log.d(PhoneAcrossService.TAG, " gPickUpFlag: true");
                            this.pick_up_cnt = 0;
                            PhoneAcrossService.this.gPickUpFlag = true;
                        }
                    }
                    if (PhoneAcrossService.this.gFilterFlag) {
                        PhoneAcrossService.this.gyroscopeValues[0] = event.values[0];
                        PhoneAcrossService.this.gyroscopeValues[1] = event.values[1];
                        PhoneAcrossService.this.gyroscopeValues[2] = event.values[2];
                        PhoneAcrossService.this.gFilterFlag = false;
                    }
                    PhoneAcrossService.this.gyroscopeValues[0] = (PhoneAcrossService.this.gyroscopeValues[0] * 0.0f) + (event.values[0] * 1.0f);
                    PhoneAcrossService.this.gyroscopeValues[1] = (PhoneAcrossService.this.gyroscopeValues[1] * 0.0f) + (event.values[1] * 1.0f);
                    PhoneAcrossService.this.gyroscopeValues[2] = (PhoneAcrossService.this.gyroscopeValues[2] * 0.0f) + (event.values[2] * 1.0f);
                    event.values[0] = PhoneAcrossService.this.gyroscopeValues[0];
                    event.values[1] = PhoneAcrossService.this.gyroscopeValues[1];
                    event.values[2] = PhoneAcrossService.this.gyroscopeValues[2];
                    if (PhoneAcrossService.this.mDataAnalysis.linearAccelerationState == 1) {
                        PhoneAcrossService.this.mDataAnalysis.gyroscopeAnalysis(event);
                        return;
                    }
                    return;
                case 10:
                    PhoneAcrossService.this.mDataAnalysis.linearAccelerationAnalysis(event);
                    return;
                default:
                    return;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private class MoveAnalyzer {
        private static final int INTERVAL_NUM = 10;
        private static final int SAMPLE_NUM = 3;
        private int countNum;
        private float[] dataAve = new float[3];
        private float[][] dataBuf = ((float[][]) Array.newInstance(Float.TYPE, new int[]{3, 3}));
        private boolean dataFlag = true;
        private float[] dataLasAve = new float[3];
        private float[] dataSum = new float[3];

        public void reset() {
            this.countNum = 0;
            this.dataFlag = true;
        }

        private void judge(float x, float y, float z) {
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
            Log.d(PhoneAcrossService.TAG, " Math.abs(dataAve[0] - dataLasAve[0]): " + Math.abs(this.dataAve[0] - this.dataLasAve[0]) + " Math.abs(dataAve[1] - dataLasAve[1]): " + Math.abs(this.dataAve[1] - this.dataLasAve[1]) + " Math.abs(dataAve[2] - dataLasAve[2]): " + Math.abs(this.dataAve[2] - this.dataLasAve[2]));
            if (((double) Math.abs(this.dataAve[2] - this.dataLasAve[2])) > 0.25d && ((((double) Math.abs(this.dataAve[0] - this.dataLasAve[0])) > 0.3d || ((double) Math.abs(this.dataAve[1] - this.dataLasAve[1])) > 0.3d) && !PhoneAcrossService.this.aPickUpFlag)) {
                reset();
                if (!PhoneAcrossService.this.aPickUpFlag) {
                    Log.d(PhoneAcrossService.TAG, " aPickUpFlag: true");
                    PhoneAcrossService.this.aPickUpFlag = true;
                }
                this.countNum = 0;
                this.dataLasAve[0] = this.dataAve[0];
                this.dataLasAve[1] = this.dataAve[1];
                this.dataLasAve[2] = this.dataAve[2];
                PhoneAcrossService.this.mDataAnalysis.linearAccelerationState = 1;
                Log.d(PhoneAcrossService.TAG, "+++++0+++++to+++++State:" + PhoneAcrossService.this.mDataAnalysis.linearAccelerationState);
            } else if (((double) Math.abs(this.dataAve[0] - this.dataLasAve[0])) < 0.15d && ((double) Math.abs(this.dataAve[1] - this.dataLasAve[1])) < 0.15d && ((double) Math.abs(this.dataAve[2] - this.dataLasAve[2])) < 0.15d) {
                if (!PhoneAcrossService.this.aPickUpStaticFlag) {
                    Log.d(PhoneAcrossService.TAG, " aPickUpStaticFlag: true");
                    PhoneAcrossService.this.aPickUpStaticFlag = true;
                }
                PhoneAcrossService.this.mDataAnalysis.linearAccelerationState = 0;
                Log.d(PhoneAcrossService.TAG, "+++++1+++++to+++++State:" + PhoneAcrossService.this.mDataAnalysis.linearAccelerationState);
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

    private class PhoneAcrossServiceHandler extends Handler {
        public PhoneAcrossServiceHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Message smsg = Message.obtain();
                    smsg.what = 16;
                    smsg.obj = new Integer(5);
                    if (PhoneAcrossService.this.mCallBackHandler != null) {
                        PhoneAcrossService.this.mCallBackHandler.sendMessage(smsg);
                        Log.d(PhoneAcrossService.TAG, "MSG_PICK_UP_DET_TRIGER");
                        return;
                    }
                    return;
                case 2:
                    msg = Message.obtain();
                    msg.what = 16;
                    msg.obj = new Integer(17);
                    if (PhoneAcrossService.this.mCallBackHandler != null) {
                        PhoneAcrossService.this.mCallBackHandler.sendMessage(msg);
                        Log.d(PhoneAcrossService.TAG, "++++++++++++++++++++++++++++PHONEACROSS---+CLOSE+---SUCCESS+++++++++++++++++++++++++++++++");
                        return;
                    }
                    return;
                case 3:
                    msg = Message.obtain();
                    msg.what = 16;
                    msg.obj = new Integer(18);
                    if (PhoneAcrossService.this.mCallBackHandler != null) {
                        PhoneAcrossService.this.mCallBackHandler.sendMessage(msg);
                        Log.d(PhoneAcrossService.TAG, "++++++++++++++++++++++++++++PHONEACROSS---+AWAY+---SUCCESS+++++++++++++++++++++++++++++++");
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class motionPara {
        float curVal;
        float maxVal;
        float minVal;
        float oldVal;
        float temVal;

        /* synthetic */ motionPara(PhoneAcrossService this$0, motionPara -this1) {
            this();
        }

        private motionPara() {
            this.curVal = 0.0f;
            this.oldVal = 0.0f;
            this.temVal = 0.0f;
            this.maxVal = 0.0f;
            this.minVal = 0.0f;
        }
    }

    public static PhoneAcrossService getInstance() {
        return singlePhoneAcrossService;
    }

    private PhoneAcrossService() {
    }

    public boolean startMotionRecognitionService(Context context, Handler handler) {
        if (!this.isPhoneAcrossServiceWorking) {
            this.aFilterFlag = true;
            this.gFilterFlag = true;
            this.aPickUpFlag = false;
            this.aPickUpStaticFlag = false;
            this.gPickUpFlag = false;
            this.pickUpTriger = true;
            this.isPhoneAcrossServiceWorking = true;
            this.mMoveAnalyzer.reset();
            this.mDataAnalysis.reset();
            this.mCallBackHandler = handler;
            this.mContext = context;
            this.mServiceHandler = new PhoneAcrossServiceHandler(handler.getLooper());
            this.mSensorManager = (SensorManager) context.getSystemService("sensor");
            this.mSensorManager.registerListener(this.accelerometerListener, this.mSensorManager.getDefaultSensor(1), 25000);
            this.mSensorManager.registerListener(this.linearAccelerationListener, this.mSensorManager.getDefaultSensor(10), 1);
            this.mSensorManager.registerListener(this.gyroscopeListener, this.mSensorManager.getDefaultSensor(4), 1);
        }
        Log.d(TAG, "startMotionRecognitionService");
        return true;
    }

    public boolean stopMotionRecognitionService() {
        if (this.isPhoneAcrossServiceWorking) {
            this.aFilterFlag = false;
            this.gFilterFlag = false;
            this.mSensorManager.unregisterListener(this.accelerometerListener);
            this.mSensorManager.unregisterListener(this.linearAccelerationListener);
            this.mSensorManager.unregisterListener(this.gyroscopeListener);
            this.isPhoneAcrossServiceWorking = false;
            this.mCallBackHandler = null;
            this.mServiceHandler = null;
            this.mSensorManager = null;
        }
        Log.d(TAG, "stopMotionRecognitionService");
        return true;
    }
}
