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

public final class MoveService implements IMotionRecognitionService {
    private static final int MSG_MOVE_DET_START = 1;
    private static final int MSG_MOVE_DET_STOP = 2;
    private static final int MSG_MOVE_DET_TRIGER = 3;
    private static final String TAG = "MoveService";
    private static MoveService mSingleMoveService = new MoveService();
    private MotionSensorEventListener accelerometerListener = new MotionSensorEventListener(this, null);
    private final float alpha = 0.99f;
    private boolean filterFlag = true;
    private float[] gravityValues = new float[3];
    private boolean isMoveWorking = false;
    private Handler mCallBackHandler = null;
    private Context mContext = null;
    private MoveAnalyzer mMoveAnalyzer = new MoveAnalyzer();
    private SensorManager mSensorManager;
    private Handler mServiceHandler = null;

    private class MotionSensorEventListener implements SensorEventListener {
        /* synthetic */ MotionSensorEventListener(MoveService this$0, MotionSensorEventListener -this1) {
            this();
        }

        private MotionSensorEventListener() {
        }

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == 1) {
                if (MoveService.this.filterFlag) {
                    MoveService.this.gravityValues[0] = event.values[0];
                    MoveService.this.gravityValues[1] = event.values[1];
                    MoveService.this.gravityValues[2] = event.values[2];
                    MoveService.this.filterFlag = false;
                }
                MoveService.this.gravityValues[0] = (MoveService.this.gravityValues[0] * 0.99f) + (event.values[0] * 0.00999999f);
                MoveService.this.gravityValues[1] = (MoveService.this.gravityValues[1] * 0.99f) + (event.values[1] * 0.00999999f);
                MoveService.this.gravityValues[2] = (MoveService.this.gravityValues[2] * 0.99f) + (event.values[2] * 0.00999999f);
                Log.d(MoveService.TAG, " gravityValues[0]: " + MoveService.this.gravityValues[0] + " gravityValues[1]: " + MoveService.this.gravityValues[1] + " gravityValues[2]: " + MoveService.this.gravityValues[2]);
                MoveService.this.mMoveAnalyzer.judge(MoveService.this.gravityValues[0], MoveService.this.gravityValues[1], MoveService.this.gravityValues[2]);
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
        private boolean trigerFlag = true;

        public void reset() {
            this.countNum = 0;
            this.dataFlag = true;
            this.trigerFlag = true;
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
            Log.d(MoveService.TAG, " Math.abs(dataAve[0] - dataLasAve[0]): " + Math.abs(this.dataAve[0] - this.dataLasAve[0]) + " Math.abs(dataAve[1] - dataLasAve[1]): " + Math.abs(this.dataAve[1] - this.dataLasAve[1]) + " Math.abs(dataAve[2] - dataLasAve[2]): " + Math.abs(this.dataAve[2] - this.dataLasAve[2]));
            if (((double) Math.abs(this.dataAve[2] - this.dataLasAve[2])) > 0.35d && (((double) Math.abs(this.dataAve[0] - this.dataLasAve[0])) > 0.4d || ((double) Math.abs(this.dataAve[1] - this.dataLasAve[1])) > 0.4d)) {
                reset();
                Log.d(MoveService.TAG, " trigerFlag: " + this.trigerFlag);
                if (MoveService.this.mServiceHandler != null && this.trigerFlag) {
                    MoveService.this.mServiceHandler.sendEmptyMessage(3);
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

    private class MoveServiceHandler extends Handler {
        public MoveServiceHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d(MoveService.TAG, "MSG_MOVE_DET_START");
                    return;
                case 2:
                    Log.d(MoveService.TAG, "MSG_MOVE_DET_STOP");
                    return;
                case 3:
                    Message smsg = Message.obtain();
                    smsg.what = 16;
                    smsg.obj = new Integer(19);
                    if (MoveService.this.mCallBackHandler != null) {
                        MoveService.this.mCallBackHandler.sendMessage(smsg);
                        Log.d(MoveService.TAG, "MSG_MOVE_DET_TRIGER");
                    }
                    if (MoveService.this.mServiceHandler != null) {
                        Log.d(MoveService.TAG, "TRIGER:MSG_MOVE_DET_STOP");
                        MoveService.this.mServiceHandler.removeMessages(2);
                        MoveService.this.mServiceHandler.sendEmptyMessage(2);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public static MoveService getInstance() {
        return mSingleMoveService;
    }

    private MoveService() {
    }

    public boolean startMotionRecognitionService(Context context, Handler handler) {
        Log.d(TAG, "startMotionRecognitionService ");
        if (!this.isMoveWorking) {
            this.mContext = context;
            this.filterFlag = true;
            this.isMoveWorking = true;
            this.mCallBackHandler = handler;
            this.mMoveAnalyzer.reset();
            this.mServiceHandler = new MoveServiceHandler(handler.getLooper());
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            if (this.mSensorManager != null) {
                this.mSensorManager.registerListener(this.accelerometerListener, this.mSensorManager.getDefaultSensor(1), 1, this.mServiceHandler);
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
        Log.d(TAG, "stopMotionRecognitionService " + this.isMoveWorking);
        if (this.isMoveWorking) {
            this.filterFlag = false;
            this.isMoveWorking = false;
            this.mServiceHandler.removeMessages(2);
            this.mCallBackHandler = null;
            this.mServiceHandler = null;
            if (this.mSensorManager != null) {
                this.mSensorManager.unregisterListener(this.accelerometerListener);
            }
            this.mSensorManager = null;
        }
        return true;
    }
}
