package com.vivo.services.motion;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import java.lang.reflect.Array;

public final class ShakeDetectServiceTwo implements IMotionRecognitionService {
    private static boolean Debug = false;
    private static final int MSG_SHAKE_DET_START = 1;
    private static final int MSG_SHAKE_DET_STOP = 2;
    private static final int MSG_SHAKE_DET_TRIGER = 3;
    private static final String TAG = "ShakeDetectServiceTwo";
    private static ShakeDetectServiceTwo mSingleShakeDetectService = new ShakeDetectServiceTwo();
    private long accTime = 0;
    private SensorEventListener accelerometerListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == 1) {
                if (ShakeDetectServiceTwo.this.gravityFlag) {
                    ShakeDetectServiceTwo.this.gravityValues[0] = event.values[0];
                    ShakeDetectServiceTwo.this.gravityValues[1] = event.values[1];
                    ShakeDetectServiceTwo.this.gravityValues[2] = event.values[2];
                    ShakeDetectServiceTwo.this.gravityFlag = true;
                }
                ShakeDetectServiceTwo.this.gravityValues[0] = (ShakeDetectServiceTwo.this.alpha * ShakeDetectServiceTwo.this.gravityValues[0]) + ((1.0f - ShakeDetectServiceTwo.this.alpha) * event.values[0]);
                ShakeDetectServiceTwo.this.gravityValues[1] = (ShakeDetectServiceTwo.this.alpha * ShakeDetectServiceTwo.this.gravityValues[1]) + ((1.0f - ShakeDetectServiceTwo.this.alpha) * event.values[1]);
                ShakeDetectServiceTwo.this.gravityValues[2] = (ShakeDetectServiceTwo.this.alpha * ShakeDetectServiceTwo.this.gravityValues[2]) + ((1.0f - ShakeDetectServiceTwo.this.alpha) * event.values[2]);
                ShakeDetectServiceTwo.this.linearaccelerationValues[0] = event.values[0] - ShakeDetectServiceTwo.this.gravityValues[0];
                ShakeDetectServiceTwo.this.linearaccelerationValues[1] = event.values[1] - ShakeDetectServiceTwo.this.gravityValues[1];
                ShakeDetectServiceTwo.this.linearaccelerationValues[2] = event.values[2] - ShakeDetectServiceTwo.this.gravityValues[2];
                if ((event.timestamp / 1000000) - ShakeDetectServiceTwo.this.accTime > 19) {
                    ShakeDetectServiceTwo.this.mShakeModeAnalyzer.ShakeAnalysis(ShakeDetectServiceTwo.this.linearaccelerationValues[0], ShakeDetectServiceTwo.this.linearaccelerationValues[1], ShakeDetectServiceTwo.this.linearaccelerationValues[2]);
                    ShakeDetectServiceTwo.this.mShakeModeAnalyzer.GravityListenerWindow(ShakeDetectServiceTwo.this.gravityValues[0], ShakeDetectServiceTwo.this.gravityValues[1], ShakeDetectServiceTwo.this.gravityValues[2]);
                    ShakeDetectServiceTwo.this.accTime = event.timestamp / 1000000;
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private float alpha = 0.8f;
    private boolean gravityFlag = true;
    private SensorEventListener gravityListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == 9 && (event.timestamp / 1000000) - ShakeDetectServiceTwo.this.gravityTime > 19) {
                ShakeDetectServiceTwo.this.mShakeModeAnalyzer.GravityListenerWindow(event.values[0], event.values[1], event.values[2]);
                ShakeDetectServiceTwo.this.gravityTime = event.timestamp / 1000000;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private long gravityTime = 0;
    private float[] gravityValues = new float[3];
    private boolean isSensorOn = false;
    private boolean isShakeModeWorking = false;
    private long linearTime = 0;
    private SensorEventListener linearaccelerationListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == 10 && (event.timestamp / 1000000) - ShakeDetectServiceTwo.this.linearTime > 19) {
                ShakeDetectServiceTwo.this.mShakeModeAnalyzer.ShakeAnalysis(event.values[0], event.values[1], event.values[2]);
                ShakeDetectServiceTwo.this.linearTime = event.timestamp / 1000000;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private float[] linearaccelerationValues = new float[3];
    private Handler mCallBackHandler = null;
    private Context mContext = null;
    String mRomVersionSt = SystemProperties.get("ro.vivo.rom.version", "unknown");
    private SensorManager mSensorManager;
    private Handler mServiceHandler = null;
    private ShakeModeAnalyzer mShakeModeAnalyzer = new ShakeModeAnalyzer();
    private boolean trigerFlag = false;

    private class ShakeDetectServiceHandler extends Handler {
        public ShakeDetectServiceHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    ShakeDetectServiceTwo.this.trigerFlag = true;
                    Message smsg = Message.obtain();
                    smsg.what = 16;
                    smsg.obj = new Integer(7);
                    if (ShakeDetectServiceTwo.this.mCallBackHandler != null) {
                        ShakeDetectServiceTwo.this.mCallBackHandler.sendMessage(smsg);
                        Log.d(ShakeDetectServiceTwo.TAG, "MSG_SHAKE_DET_TRIGER");
                    }
                    if (ShakeDetectServiceTwo.this.mShakeModeAnalyzer != null) {
                        ShakeDetectServiceTwo.this.mShakeModeAnalyzer.reset();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class ShakeModeAnalyzer {
        private static final float BALANCE_COEFFICIENT = 0.2f;
        private static final float EXTREME_COEFFICIENT = 0.2f;
        private static final int EXTREME_COUNT = 5;
        private static final int EXTREME_COUNT_THRESHOLD = 3;
        private static final int EXTREME_NUM_THRESHOLD_MODE_0 = 12;
        private static final int EXTREME_NUM_THRESHOLD_MODE_1 = 13;
        private static final int EXTREME_NUM_THRESHOLD_MODE_2 = 11;
        private static final int EXTREME_NUM_THRESHOLD_MODE_A = 25;
        private static final float EXTREME_SPEED_THRESHOLD_MODE_0 = 0.5f;
        private static final float EXTREME_SPEED_THRESHOLD_MODE_1 = 0.8f;
        private static final float EXTREME_SPEED_THRESHOLD_MODE_2 = 1.2f;
        private static final float EXTREME_SPEED_THRESHOLD_MODE_A = 0.2f;
        private static final int EXTREME_ZERO_NUM_THRESHOLD_MODE_0 = 10;
        private static final int EXTREME_ZERO_NUM_THRESHOLD_MODE_1 = 7;
        private static final int EXTREME_ZERO_NUM_THRESHOLD_MODE_2 = 6;
        private static final int EXTREME_ZERO_NUM_THRESHOLD_MODE_A = 10;
        private static final int GRAVITY_M_M_THRESHOLD_MODE_1 = 10;
        private static final int GRAVITY_M_M_THRESHOLD_MODE_2 = 12;
        private static final int NUM_THRESHOLD = 60;
        private static final int POSTURE_EFFECTIVE_THRESHOLD = 5;
        private static final int POSTURE_THRESHOLD = 8;
        private static final float SHAKE_THRESHOLD = 3.0f;
        private static final float SPEED_PROCESS_INTERVAL = 10.0f;
        private static final int STATIC_NUM_THRESHOLD = 10;
        private static final float STATIC_THRESHOLD = 2.0f;
        private static final float TIME_INTERVAL = 25000.0f;
        private int extremeCount = 0;
        private boolean firstFlag = true;
        private float[][] gravityBuf = ((float[][]) Array.newInstance(Float.TYPE, new int[]{3, NUM_THRESHOLD}));
        private float lastXvalue = 0.0f;
        private float[] originalXspeedBuf = new float[NUM_THRESHOLD];
        private int phoneMode = 0;
        private float[][] postureBuf = ((float[][]) Array.newInstance(Float.TYPE, new int[]{3, 8}));
        private float[] postureData = new float[3];
        private float presentXvalue = 0.0f;
        private float[] processXspeedBuf = new float[NUM_THRESHOLD];
        private boolean shakeResult = false;
        private boolean shakeState = false;
        private boolean shakeTag = false;
        private int[] staticInfoBuf = new int[10];
        private boolean staticState = true;
        private float[] xlinearaccBuf = new float[NUM_THRESHOLD];

        public void reset() {
            int i;
            int j;
            this.phoneMode = 0;
            this.extremeCount = 0;
            this.lastXvalue = 0.0f;
            this.presentXvalue = 0.0f;
            this.firstFlag = true;
            this.staticState = true;
            this.shakeState = false;
            this.shakeTag = false;
            this.shakeResult = false;
            for (i = 0; i < NUM_THRESHOLD; i++) {
                this.xlinearaccBuf[i] = 0.0f;
                this.originalXspeedBuf[i] = 0.0f;
                this.processXspeedBuf[i] = 0.0f;
                for (j = 0; j < 3; j++) {
                    this.gravityBuf[j][i] = 0.0f;
                }
            }
            for (i = 0; i < 10; i++) {
                this.staticInfoBuf[i] = 0;
            }
            for (i = 0; i < 7; i++) {
                for (j = 0; j < 3; j++) {
                    this.postureBuf[j][i] = 0.0f;
                }
            }
        }

        private void linearaccelerationListenerWindow(float xValue, float yValue, float zValue) {
            for (int i = 59; i > 0; i--) {
                this.xlinearaccBuf[i] = this.xlinearaccBuf[i - 1];
            }
            this.xlinearaccBuf[0] = xValue;
        }

        private void GravityListenerWindow(float xValue, float yValue, float zValue) {
            int i;
            int j;
            for (i = 59; i > 0; i--) {
                for (j = 0; j < 3; j++) {
                    this.gravityBuf[j][i] = this.gravityBuf[j][i - 1];
                }
            }
            this.gravityBuf[0][0] = xValue;
            this.gravityBuf[1][0] = yValue;
            this.gravityBuf[2][0] = zValue;
            if (this.staticState) {
                for (i = 0; i < 7; i++) {
                    for (j = 0; j < 3; j++) {
                        this.postureBuf[j][i] = this.postureBuf[j][i + 1];
                    }
                    this.postureBuf[0][7] = xValue;
                    this.postureBuf[1][7] = yValue;
                    this.postureBuf[2][7] = zValue;
                }
            }
        }

        private void originalSpeedWindow(float xValue, float yValue, float zValue) {
            for (int i = 59; i > 0; i--) {
                this.originalXspeedBuf[i] = this.originalXspeedBuf[i - 1];
            }
            this.originalXspeedBuf[0] = this.originalXspeedBuf[1] + (((TIME_INTERVAL * xValue) / 1000.0f) / 1000.0f);
        }

        private void processSpeedWindow(float xValue, float yValue, float zValue) {
            int i;
            float sum_speed = 0.0f;
            for (i = 0; ((float) i) < SPEED_PROCESS_INTERVAL; i++) {
                sum_speed += this.originalXspeedBuf[i];
            }
            for (i = 59; i > 0; i--) {
                this.processXspeedBuf[i] = this.processXspeedBuf[i - 1];
            }
            this.processXspeedBuf[0] = this.originalXspeedBuf[0] - (sum_speed / SPEED_PROCESS_INTERVAL);
            Log.d(ShakeDetectServiceTwo.TAG, " processXspeedBuf: " + this.processXspeedBuf[0]);
        }

        private void crestJudge() {
        }

        private void troughJudge() {
        }

        public int getDegree(float x, float y) {
            if (x == 0.0f && y == 0.0f) {
                return -1;
            }
            double angle = Math.toDegrees(Math.atan2((double) y, (double) x));
            if (angle < 0.0d) {
                angle += 360.0d;
            }
            return (int) Math.floor(angle);
        }

        public int transformDegree(int degree) {
            if (degree < 0 || degree >= 90) {
                degree = 450 - degree;
            } else {
                degree = 90 - degree;
            }
            if (degree == 360) {
                return 0;
            }
            return degree;
        }

        private void shakeJudge(float extremeSpeedThreshold, int extremeNumThreshold, int extremeZeroNumThreshold) {
            int i;
            int shakeCount = 0;
            int zeroNum = 0;
            int crestNum = 0;
            int troughNum = 0;
            int extremeNum = 0;
            int crestCount = 0;
            int troughCount = 0;
            float crestSpeed = 0.0f;
            float lastCrestSpeed = 0.0f;
            float troughSpeed = 0.0f;
            float lastTroughSpeed = 0.0f;
            boolean crestFlag = false;
            boolean troughFlag = false;
            boolean thresholdFlag = false;
            for (i = 0; i < 57; i++) {
                if (crestFlag) {
                    if (this.processXspeedBuf[i] < crestSpeed) {
                        shakeCount = 1;
                        break;
                    }
                    crestSpeed = this.processXspeedBuf[i];
                    crestNum = i;
                    extremeNum = i;
                } else if (troughFlag) {
                    if (this.processXspeedBuf[i] > troughSpeed) {
                        shakeCount = 1;
                        break;
                    }
                    troughSpeed = this.processXspeedBuf[i];
                    troughNum = i;
                    extremeNum = i;
                } else if (this.processXspeedBuf[i] > extremeSpeedThreshold - ((this.processXspeedBuf[i] * 2.0f) * 0.2f)) {
                    crestSpeed = this.processXspeedBuf[i];
                    crestNum = i;
                    extremeNum = i;
                    crestFlag = true;
                } else if ((-this.processXspeedBuf[i]) > ((this.processXspeedBuf[i] * 2.0f) * 0.2f) + extremeSpeedThreshold) {
                    troughSpeed = this.processXspeedBuf[i];
                    troughNum = i;
                    extremeNum = i;
                    troughFlag = true;
                } else if (i == 57) {
                    return;
                }
            }
            if (crestFlag) {
                lastCrestSpeed = crestSpeed;
            }
            if (troughFlag) {
                lastTroughSpeed = troughSpeed;
            }
            for (i = extremeNum + 1; i < NUM_THRESHOLD; i++) {
                if (shakeCount >= 3) {
                    this.shakeTag = true;
                    break;
                }
                float zeroSpeed;
                int j;
                if ((-this.processXspeedBuf[i]) > extremeSpeedThreshold - ((crestSpeed - this.processXspeedBuf[i]) * 0.2f)) {
                    crestCount++;
                } else {
                    crestCount = 0;
                }
                if (this.processXspeedBuf[i] > extremeSpeedThreshold - ((this.processXspeedBuf[i] - troughSpeed) * 0.2f)) {
                    troughCount++;
                } else {
                    troughCount = 0;
                }
                if (crestCount > 5) {
                    shakeCount = 1;
                    crestCount = 0;
                    Log.d(ShakeDetectServiceTwo.TAG, " extremeCount crestCount is not satify");
                }
                if (troughCount > 5) {
                    shakeCount = 1;
                    troughCount = 0;
                    Log.d(ShakeDetectServiceTwo.TAG, " extremeCount troughCount is not satify");
                }
                if (crestFlag) {
                    if (thresholdFlag) {
                        if (this.processXspeedBuf[i] > troughSpeed) {
                            troughFlag = true;
                            crestFlag = false;
                            thresholdFlag = false;
                            if (crestSpeed - troughSpeed < 2.0f * extremeSpeedThreshold) {
                                shakeCount = 1;
                                Log.d(ShakeDetectServiceTwo.TAG, " extreme speed threshold is not satify 1");
                            } else {
                                if (shakeCount > 1) {
                                    if (Math.abs(troughSpeed - lastTroughSpeed) > (crestSpeed - troughSpeed) * 0.2f) {
                                        shakeCount = 1;
                                        lastTroughSpeed = troughSpeed;
                                    } else {
                                        lastTroughSpeed = troughSpeed;
                                    }
                                }
                                if (troughNum - crestNum < extremeNumThreshold) {
                                    zeroSpeed = this.processXspeedBuf[troughNum];
                                    for (j = crestNum; j < troughNum; j++) {
                                        if (Math.abs(this.processXspeedBuf[j] - ((crestSpeed + troughSpeed) / 2.0f)) < Math.abs(zeroSpeed - ((crestSpeed + troughSpeed) / 2.0f))) {
                                            zeroSpeed = this.processXspeedBuf[j];
                                            zeroNum = j;
                                        }
                                    }
                                    if (troughNum - zeroNum >= extremeZeroNumThreshold || zeroNum - crestNum >= extremeZeroNumThreshold) {
                                        shakeCount = 1;
                                        Log.d(ShakeDetectServiceTwo.TAG, " extreme speed to zero speed num interval is not satify 1");
                                    } else {
                                        shakeCount++;
                                    }
                                } else {
                                    shakeCount = 1;
                                    Log.d(ShakeDetectServiceTwo.TAG, " extreme speed num interval is not satify 1");
                                }
                            }
                        } else {
                            troughSpeed = this.processXspeedBuf[i];
                            troughNum = i;
                        }
                    } else if ((-this.processXspeedBuf[i]) > extremeSpeedThreshold - ((crestSpeed - this.processXspeedBuf[i]) * 0.2f)) {
                        troughSpeed = this.processXspeedBuf[i];
                        troughNum = i;
                        thresholdFlag = true;
                    }
                }
                if (troughFlag) {
                    if (thresholdFlag) {
                        if (this.processXspeedBuf[i] < crestSpeed) {
                            crestFlag = true;
                            troughFlag = false;
                            thresholdFlag = false;
                            if (crestSpeed - troughSpeed < 2.0f * extremeSpeedThreshold) {
                                shakeCount = 1;
                                Log.d(ShakeDetectServiceTwo.TAG, " extreme speed threshold is not satify 2 ");
                            } else {
                                if (shakeCount > 1) {
                                    if (Math.abs(crestSpeed - lastCrestSpeed) > (crestSpeed - troughSpeed) * 0.2f) {
                                        shakeCount = 1;
                                        lastCrestSpeed = crestSpeed;
                                    } else {
                                        lastCrestSpeed = crestSpeed;
                                    }
                                }
                                if (crestNum - troughNum < extremeNumThreshold) {
                                    zeroSpeed = this.processXspeedBuf[crestNum];
                                    for (j = troughNum; j < crestNum; j++) {
                                        if (Math.abs(this.processXspeedBuf[j] - ((crestSpeed + troughSpeed) / 2.0f)) < Math.abs(zeroSpeed - ((crestSpeed + troughSpeed) / 2.0f))) {
                                            zeroSpeed = this.processXspeedBuf[j];
                                            zeroNum = j;
                                        }
                                    }
                                    if (crestNum - zeroNum >= extremeZeroNumThreshold || zeroNum - troughNum >= extremeZeroNumThreshold) {
                                        shakeCount = 1;
                                        Log.d(ShakeDetectServiceTwo.TAG, " extreme speed to zero speed num interval is not satify 2");
                                    } else {
                                        shakeCount++;
                                    }
                                } else {
                                    shakeCount = 1;
                                    Log.d(ShakeDetectServiceTwo.TAG, " extreme speed num interval is not satify 2");
                                }
                            }
                        } else {
                            crestSpeed = this.processXspeedBuf[i];
                            crestNum = i;
                        }
                    } else if (this.processXspeedBuf[i] > extremeSpeedThreshold - ((this.processXspeedBuf[i] - troughSpeed) * 0.2f)) {
                        crestSpeed = this.processXspeedBuf[i];
                        crestNum = i;
                        thresholdFlag = true;
                    }
                }
            }
            if (this.shakeTag) {
                this.shakeResult = true;
                if (ShakeDetectServiceTwo.Debug) {
                    Log.d(ShakeDetectServiceTwo.TAG, " ++shakeCount:" + shakeCount);
                }
            } else if (ShakeDetectServiceTwo.Debug) {
                Log.d(ShakeDetectServiceTwo.TAG, " --shakeCount:" + shakeCount);
            }
        }

        private void staticJudge(float lastXvalue, float presentXvalue) {
            int i;
            int staticCount = 0;
            if (Math.abs(presentXvalue) < 2.0f) {
                for (i = 9; i > 0; i--) {
                    this.staticInfoBuf[i] = this.staticInfoBuf[i - 1];
                }
                this.staticInfoBuf[0] = 1;
            } else {
                for (i = 9; i > 0; i--) {
                    this.staticInfoBuf[i] = this.staticInfoBuf[i - 1];
                }
                this.staticInfoBuf[0] = 0;
            }
            for (i = 0; i < 10; i++) {
                staticCount += this.staticInfoBuf[i];
            }
            if (ShakeDetectServiceTwo.Debug) {
                Log.d(ShakeDetectServiceTwo.TAG, " staticCount:" + staticCount);
            }
            if (staticCount > 5) {
                ShakeDetectServiceTwo.this.trigerFlag = false;
                reset();
            }
        }

        private int modeJudge() {
            int i;
            int result = 0;
            float gravityMM = 0.0f;
            gravityMin = new float[3];
            float[] gravityMax = new float[]{this.gravityBuf[0][0], this.gravityBuf[1][0], this.gravityBuf[2][0]};
            gravityMax[0] = this.gravityBuf[0][0];
            gravityMax[1] = this.gravityBuf[1][0];
            gravityMax[2] = this.gravityBuf[2][0];
            for (i = 1; i < NUM_THRESHOLD; i++) {
                for (int j = 0; j < 3; j++) {
                    if (this.gravityBuf[j][i] > gravityMax[j]) {
                        gravityMax[j] = this.gravityBuf[j][i];
                    }
                    if (this.gravityBuf[j][i] < gravityMin[j]) {
                        gravityMin[j] = this.gravityBuf[j][i];
                    }
                }
            }
            for (i = 0; i < 3; i++) {
                if (gravityMax[i] - gravityMin[i] > gravityMM) {
                    gravityMM = gravityMax[i] - gravityMin[i];
                }
            }
            if (gravityMM > SPEED_PROCESS_INTERVAL) {
                result = 1;
            }
            if (gravityMM > 12.0f) {
                return 2;
            }
            return result;
        }

        private void getPostureData() {
            int i;
            int standardNum = 0;
            float[] sumValues = new float[3];
            float[] standardValues = new float[3];
            float[] diffValues = new float[5];
            for (i = 0; i < 5; i++) {
                for (int j = 0; j < 3; j++) {
                    sumValues[j] = sumValues[j] + this.postureBuf[j][i];
                }
            }
            for (i = 0; i < 3; i++) {
                standardValues[i] = sumValues[i] / 5.0f;
            }
            for (i = 0; i < 5; i++) {
                diffValues[i] = (((this.postureBuf[0][i] - standardValues[0]) * (this.postureBuf[0][i] - standardValues[0])) + ((this.postureBuf[1][i] - standardValues[1]) * (this.postureBuf[1][i] - standardValues[1]))) + ((this.postureBuf[2][i] - standardValues[2]) * (this.postureBuf[2][i] - standardValues[2]));
            }
            float minValue = diffValues[0];
            for (i = 4; i > 0; i--) {
                if (minValue > diffValues[i]) {
                    minValue = diffValues[i];
                    standardNum = i;
                }
            }
            this.postureData[0] = this.postureBuf[0][standardNum];
            this.postureData[1] = this.postureBuf[1][standardNum];
            this.postureData[2] = this.postureBuf[2][standardNum];
        }

        private boolean phoneDirectionJudge() {
            boolean result = false;
            float gravityYSum = 0.0f;
            int gravityYCount = 0;
            int gravityYZeroCount = 0;
            for (int i = 0; i < 30; i++) {
                if (this.gravityBuf[1][i] != 0.0f) {
                    gravityYZeroCount = 0;
                } else {
                    gravityYZeroCount++;
                }
                gravityYCount++;
                gravityYSum += this.gravityBuf[1][i];
                if (gravityYZeroCount > 1) {
                    break;
                }
            }
            float gravityYAve = gravityYSum / ((float) gravityYCount);
            Log.d(ShakeDetectServiceTwo.TAG, "gravityYAve:" + gravityYAve);
            if (gravityYAve < -5.0f) {
                result = true;
            }
            Log.d(ShakeDetectServiceTwo.TAG, "phone direction is not right,result:" + result);
            return result;
        }

        private int phonePostureJudge() {
            if (Math.abs(this.postureData[0]) > Math.abs(this.postureData[2]) * SHAKE_THRESHOLD || Math.abs(this.postureData[1]) > Math.abs(this.postureData[2]) * SHAKE_THRESHOLD) {
                return 2;
            }
            if (Math.abs(this.postureData[0]) > Math.abs(this.postureData[2]) || Math.abs(this.postureData[1]) > Math.abs(this.postureData[2])) {
                return 1;
            }
            return 0;
        }

        private int tiltModeJudge() {
            if (this.postureData[0] < 0.0f) {
                return 0;
            }
            return 1;
        }

        private void ShakeAnalysis(float xValue, float yValue, float zValue) {
            float extremeSpeedThreshold = 0.0f;
            int extremeNumThreshold = 0;
            int extremeZeroNumThreshold = 0;
            if (ShakeDetectServiceTwo.Debug) {
                Log.d(ShakeDetectServiceTwo.TAG, "gsensor_data_for_shake x: " + xValue + " y: " + yValue + " z: " + zValue);
            }
            this.presentXvalue = xValue;
            if (this.firstFlag) {
                this.lastXvalue = this.presentXvalue;
                this.firstFlag = false;
                return;
            }
            if (!this.staticState || ShakeDetectServiceTwo.this.trigerFlag) {
                staticJudge(this.lastXvalue, this.presentXvalue);
            } else if (Math.abs(this.presentXvalue - this.lastXvalue) > SHAKE_THRESHOLD) {
                this.shakeState = true;
                this.staticState = false;
            }
            this.lastXvalue = this.presentXvalue;
            if (this.shakeState) {
                linearaccelerationListenerWindow(xValue, yValue, zValue);
                originalSpeedWindow(xValue, yValue, zValue);
                processSpeedWindow(xValue, yValue, zValue);
                getPostureData();
                this.phoneMode = phonePostureJudge();
                this.phoneMode = 0;
                if (this.phoneMode == 0) {
                    extremeSpeedThreshold = EXTREME_SPEED_THRESHOLD_MODE_0;
                    extremeNumThreshold = 12;
                    extremeZeroNumThreshold = 10;
                } else if (this.phoneMode == 1) {
                    extremeSpeedThreshold = EXTREME_SPEED_THRESHOLD_MODE_1;
                    extremeNumThreshold = 13;
                    extremeZeroNumThreshold = 7;
                } else if (this.phoneMode == 2) {
                    extremeSpeedThreshold = EXTREME_SPEED_THRESHOLD_MODE_2;
                    extremeNumThreshold = 11;
                    extremeZeroNumThreshold = 6;
                }
                shakeJudge(extremeSpeedThreshold, extremeNumThreshold, extremeZeroNumThreshold);
            } else {
                originalSpeedWindow(xValue, yValue, zValue);
            }
            if (this.shakeResult) {
                this.shakeResult = false;
                if (!phoneDirectionJudge()) {
                    int tiltValue = tiltModeJudge();
                    if (System.getInt(ShakeDetectServiceTwo.this.mContext.getContentResolver(), "bbk_application_settings", 0) == 3) {
                        Intent intent = new Intent();
                        intent.setAction("android.action.multifloatingtask.showsmallwindowvalue");
                        intent.putExtra("showsmallwindowvalue", tiltValue);
                        ShakeDetectServiceTwo.this.mContext.sendBroadcast(intent);
                        Log.d(ShakeDetectServiceTwo.TAG, "tiltValue:" + tiltValue);
                    }
                    Message msg = Message.obtain();
                    msg.what = 3;
                    if (ShakeDetectServiceTwo.this.mServiceHandler != null) {
                        ShakeDetectServiceTwo.this.mServiceHandler.sendMessage(msg);
                    }
                    Log.d(ShakeDetectServiceTwo.TAG, "shake to open apps motion analysis success!");
                }
            }
        }
    }

    public static ShakeDetectServiceTwo getInstance() {
        return mSingleShakeDetectService;
    }

    private ShakeDetectServiceTwo() {
        if (this.mShakeModeAnalyzer != null) {
            this.mShakeModeAnalyzer.reset();
        }
    }

    public static boolean isAccelerometerSupported(Context context) {
        if (((SensorManager) context.getSystemService("sensor")).getSensorList(1).size() > 0) {
            return true;
        }
        return false;
    }

    public static boolean isLinearaccelerationSupported(Context context) {
        if (((SensorManager) context.getSystemService("sensor")).getSensorList(10).size() > 0) {
            return true;
        }
        return false;
    }

    public static boolean isGravitySupported(Context context) {
        if (((SensorManager) context.getSystemService("sensor")).getSensorList(9).size() > 0) {
            return true;
        }
        return false;
    }

    public boolean startMotionRecognitionService(Context context, Handler handler) {
        Log.d(TAG, "startMotionRecognitionService");
        if (!this.isShakeModeWorking) {
            this.mContext = context;
            this.gravityFlag = true;
            this.trigerFlag = false;
            this.isShakeModeWorking = true;
            if (this.mShakeModeAnalyzer != null) {
                this.mShakeModeAnalyzer.reset();
            }
            this.mCallBackHandler = handler;
            this.mServiceHandler = new ShakeDetectServiceHandler(handler.getLooper());
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            if (!(this.mSensorManager == null || this.mShakeModeAnalyzer == null || this.isSensorOn)) {
                if (isLinearaccelerationSupported(this.mContext) && isGravitySupported(this.mContext)) {
                    this.mSensorManager.registerListener(this.linearaccelerationListener, this.mSensorManager.getDefaultSensor(10), 25000, this.mServiceHandler);
                    this.mSensorManager.registerListener(this.gravityListener, this.mSensorManager.getDefaultSensor(9), 25000, this.mServiceHandler);
                    this.isSensorOn = true;
                } else if (isAccelerometerSupported(this.mContext)) {
                    this.mSensorManager.registerListener(this.accelerometerListener, this.mSensorManager.getDefaultSensor(1), 25000, this.mServiceHandler);
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
        Log.d(TAG, "stopMotionRecognitionService,isShakeModeWorking" + this.isShakeModeWorking);
        if (this.isShakeModeWorking) {
            this.gravityFlag = false;
            this.isShakeModeWorking = false;
            this.mCallBackHandler = null;
            this.mServiceHandler = null;
            if (this.mSensorManager != null) {
                if (this.isSensorOn) {
                    if (isLinearaccelerationSupported(this.mContext) && isGravitySupported(this.mContext)) {
                        this.mSensorManager.unregisterListener(this.linearaccelerationListener);
                        this.mSensorManager.unregisterListener(this.gravityListener);
                        this.isSensorOn = false;
                    } else if (isAccelerometerSupported(this.mContext)) {
                        this.mSensorManager.unregisterListener(this.accelerometerListener);
                        this.isSensorOn = false;
                    }
                }
                this.mSensorManager = null;
            }
        }
        return true;
    }
}
