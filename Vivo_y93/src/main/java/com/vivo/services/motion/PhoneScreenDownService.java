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

public final class PhoneScreenDownService implements IMotionRecognitionService {
    private static final double EARTH_GRAVITY = -9.801d;
    private static final float LENGTH_SIDE_ANGLE = 15.0f;
    private static final int MSG_PHONE_POSTURE_DET_START = 1;
    private static final int MSG_PHONE_POSTURE_DET_STOP = 2;
    private static final int MSG_PHONE_POSTURE_DET_TRIGER_NO = 4;
    private static final int MSG_PHONE_POSTURE_DET_TRIGER_YES = 3;
    private static final String TAG = "PhoneScreenDownService";
    private static final float WIDTH_SIDE_ANGLE = 15.0f;
    private static PhoneScreenDownService mSinglePhoneScreenDownService = new PhoneScreenDownService();
    private MotionSensorEventListener accelerometerListener = new MotionSensorEventListener(this, null);
    private boolean isIdle = true;
    private boolean isPhoneScreenDownWorking = false;
    private Handler mCallBackHandler = null;
    private Context mContext = null;
    private PhonePostureAnalyzer mPhonePostureAnalyzer = new PhonePostureAnalyzer();
    private SensorManager mSensorManager;
    private Handler mServiceHandler = null;

    private class MotionSensorEventListener implements SensorEventListener {
        /* synthetic */ MotionSensorEventListener(PhoneScreenDownService this$0, MotionSensorEventListener -this1) {
            this();
        }

        private MotionSensorEventListener() {
        }

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == 1 && PhoneScreenDownService.this.isIdle) {
                PhoneScreenDownService.this.isIdle = false;
                PhoneScreenDownService.this.isIdle = PhoneScreenDownService.this.mPhonePostureAnalyzer.judge(event);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private class PhonePostureAnalyzer {
        private static final int ANALYSIS_NUM = 5;
        private static final int DISCARDED_NUM = 3;
        private static final int SAMPLE_NUM = 20;
        private static final int SAMPLE_TIME = 150;
        private float[][] cali_data_buf = ((float[][]) Array.newInstance(Float.TYPE, new int[]{20, 3}));
        private long current_time = 0;
        private long last_time = 0;
        private float length_x_th = 0.0f;
        private float length_z_th = 0.0f;
        private int sample_count = 0;
        private boolean start_flag = true;
        private long start_time = 0;
        private boolean state_flag = false;
        private boolean static_flag = false;
        private boolean triger_flag = false;
        private float width_y_th = 0.0f;
        private float width_z_th = 0.0f;
        private float z_th = 0.0f;

        public void reset() {
            this.start_flag = true;
            this.triger_flag = false;
            this.sample_count = 0;
            this.last_time = 0;
            this.width_y_th = ((float) Math.sin((((double) AllConfig.REMIND_ANGLE) * 3.1415926d) / 180.0d)) * -9.801f;
            this.length_x_th = ((float) Math.sin((((double) AllConfig.REMIND_ANGLE) * 3.1415926d) / 180.0d)) * -9.801f;
            this.z_th = (((float) Math.cos((((double) AllConfig.REMIND_ANGLE) * 3.1415926d) / 180.0d)) * -9.801f) * ((float) Math.cos((((double) AllConfig.REMIND_ANGLE) * 3.1415926d) / 180.0d));
            for (int m = 0; m < 20; m++) {
                for (int n = 0; n < 3; n++) {
                    this.cali_data_buf[m][n] = 0.0f;
                }
            }
        }

        private boolean judge(SensorEvent event) {
            float[] analysis_max = new float[3];
            float[] analysis_min = new float[3];
            float[] analysis_sum = new float[3];
            float[] analysis_abs_max = new float[3];
            float[] analysis_posture_x_angle = new float[5];
            float[] analysis_posture_y_angle = new float[5];
            if (this.triger_flag) {
                return true;
            }
            this.state_flag = false;
            this.static_flag = false;
            if (this.start_flag) {
                this.start_time = event.timestamp / 1000000;
                this.start_flag = false;
            }
            this.current_time = event.timestamp / 1000000;
            if (this.current_time - this.last_time < 5) {
                if (this.last_time > this.current_time) {
                    this.last_time = this.current_time;
                    Log.d(PhoneScreenDownService.TAG, "last_time is bigger than current_time.");
                }
                return true;
            }
            this.last_time = this.current_time;
            for (int m = 19; m > 0; m--) {
                for (int n = 0; n < 3; n++) {
                    this.cali_data_buf[m][n] = this.cali_data_buf[m - 1][n];
                }
            }
            this.cali_data_buf[0][0] = event.values[0];
            this.cali_data_buf[0][1] = event.values[1];
            this.cali_data_buf[0][2] = event.values[2];
            this.sample_count++;
            if (this.current_time - this.start_time <= 150 || PhoneScreenDownService.this.mServiceHandler == null) {
                if (this.sample_count > 20) {
                    if (PhoneScreenDownService.this.mServiceHandler != null) {
                        try {
                            PhoneScreenDownService.this.mServiceHandler.sendEmptyMessage(4);
                        } catch (Exception e) {
                        }
                        this.triger_flag = true;
                        Log.d(PhoneScreenDownService.TAG, "sample number is out of threshold");
                        return true;
                    }
                } else if (this.sample_count < 8) {
                    return true;
                }
                for (int j = 0; j < 3; j++) {
                    analysis_max[j] = this.cali_data_buf[0][j];
                    analysis_min[j] = this.cali_data_buf[0][j];
                    analysis_sum[j] = this.cali_data_buf[0][j];
                    for (int i = 1; i < 5; i++) {
                        if (analysis_max[j] < this.cali_data_buf[i][j]) {
                            analysis_max[j] = this.cali_data_buf[i][j];
                        }
                        if (analysis_min[j] > this.cali_data_buf[i][j]) {
                            analysis_min[j] = this.cali_data_buf[i][j];
                        }
                        analysis_sum[j] = analysis_sum[j] + this.cali_data_buf[0][j];
                    }
                }
                if (((double) Math.abs(analysis_max[0] - analysis_min[0])) < 0.5d && ((double) Math.abs(analysis_max[1] - analysis_min[1])) < 0.5d && Math.abs(analysis_max[2] - analysis_min[2]) < 1.0f) {
                    this.static_flag = true;
                }
                if (!this.static_flag) {
                    if (Math.abs(analysis_max[0]) > Math.abs(analysis_min[0])) {
                        analysis_abs_max[0] = Math.abs(analysis_max[0]);
                    } else {
                        analysis_abs_max[0] = Math.abs(analysis_min[0]);
                    }
                    if (Math.abs(analysis_max[1]) > Math.abs(analysis_min[1])) {
                        analysis_abs_max[1] = Math.abs(analysis_max[1]);
                    } else {
                        analysis_abs_max[1] = Math.abs(analysis_min[1]);
                    }
                    if (Math.abs(analysis_max[2]) > Math.abs(analysis_min[2])) {
                        analysis_abs_max[2] = analysis_max[2];
                    } else {
                        analysis_abs_max[2] = analysis_min[2];
                    }
                    if (analysis_abs_max[0] < Math.abs(this.length_x_th) && analysis_abs_max[1] < Math.abs(this.width_y_th) && analysis_abs_max[2] < this.z_th) {
                        for (int a = 0; a < 5; a++) {
                            analysis_posture_x_angle[a] = (float) ((Math.abs(Math.asin((double) (this.cali_data_buf[a][0] / this.cali_data_buf[a][2]))) * 180.0d) / 3.1415926d);
                            analysis_posture_y_angle[a] = (float) ((Math.abs(Math.asin((double) (this.cali_data_buf[a][1] / this.cali_data_buf[a][2]))) * 180.0d) / 3.1415926d);
                        }
                        AllConfig.POSTURE_X_ANGLE = analysis_posture_x_angle[0];
                        AllConfig.POSTURE_Y_ANGLE = analysis_posture_y_angle[0];
                        for (int b = 1; b < 5; b++) {
                            if (AllConfig.POSTURE_X_ANGLE < analysis_posture_x_angle[b]) {
                                AllConfig.POSTURE_X_ANGLE = analysis_posture_x_angle[b];
                            }
                            if (AllConfig.POSTURE_Y_ANGLE < analysis_posture_y_angle[b]) {
                                AllConfig.POSTURE_Y_ANGLE = analysis_posture_y_angle[b];
                            }
                        }
                        if (AllConfig.POSTURE_X_ANGLE > AllConfig.REMIND_ANGLE || AllConfig.POSTURE_Y_ANGLE > AllConfig.REMIND_ANGLE) {
                            this.state_flag = false;
                        } else {
                            this.state_flag = true;
                        }
                        Log.d(PhoneScreenDownService.TAG, "AllConfig.POSTURE_X_ANGLE: " + AllConfig.POSTURE_X_ANGLE + "AllConfig.POSTURE_Y_ANGLE: " + AllConfig.POSTURE_Y_ANGLE + " state_flag:" + this.static_flag);
                    }
                } else if (Math.abs(analysis_sum[0] / 5.0f) < Math.abs(this.length_x_th) && Math.abs(analysis_sum[1] / 5.0f) < Math.abs(this.width_y_th) && analysis_sum[2] / 5.0f < this.z_th) {
                    AllConfig.POSTURE_X_ANGLE = (float) ((Math.abs(Math.asin((double) (analysis_sum[0] / analysis_sum[2]))) * 180.0d) / 3.1415926d);
                    AllConfig.POSTURE_Y_ANGLE = (float) ((Math.abs(Math.asin((double) (analysis_sum[1] / analysis_sum[2]))) * 180.0d) / 3.1415926d);
                    if (AllConfig.POSTURE_X_ANGLE > AllConfig.REMIND_ANGLE || AllConfig.POSTURE_Y_ANGLE > AllConfig.REMIND_ANGLE) {
                        this.state_flag = false;
                    } else {
                        this.state_flag = true;
                    }
                    Log.d(PhoneScreenDownService.TAG, " AllConfig.POSTURE_X_ANGLE:" + AllConfig.POSTURE_X_ANGLE + " AllConfig.POSTURE_Y_ANGLE:" + AllConfig.POSTURE_Y_ANGLE + " state_flag:" + this.static_flag);
                }
                if (!this.state_flag || PhoneScreenDownService.this.mServiceHandler == null) {
                    return true;
                }
                try {
                    PhoneScreenDownService.this.mServiceHandler.sendEmptyMessage(3);
                } catch (Exception e2) {
                }
                this.triger_flag = true;
                Log.d(PhoneScreenDownService.TAG, "phone posture analysis finish.");
                return true;
            }
            try {
                PhoneScreenDownService.this.mServiceHandler.sendEmptyMessage(4);
            } catch (Exception e3) {
            }
            this.triger_flag = true;
            Log.d(PhoneScreenDownService.TAG, "time is out of threshold");
            return true;
        }
    }

    private class PhoneScreenDownServiceHandler extends Handler {
        public PhoneScreenDownServiceHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            Message smsg;
            switch (msg.what) {
                case 1:
                    Log.d(PhoneScreenDownService.TAG, "MSG_PHONE_POSTURE_DET_START");
                    return;
                case 2:
                    Log.d(PhoneScreenDownService.TAG, "MSG_PHONE_POSTURE_DET_STOP");
                    return;
                case 3:
                    smsg = Message.obtain();
                    smsg.what = 16;
                    smsg.obj = new Integer(22);
                    if (PhoneScreenDownService.this.mCallBackHandler != null) {
                        PhoneScreenDownService.this.mCallBackHandler.sendMessage(smsg);
                        Log.d(PhoneScreenDownService.TAG, "MSG_PHONE_POSTURE_DET_TRIGER");
                    }
                    if (PhoneScreenDownService.this.mServiceHandler != null) {
                        try {
                            PhoneScreenDownService.this.mServiceHandler.removeMessages(2);
                            PhoneScreenDownService.this.mServiceHandler.sendEmptyMessage(2);
                            return;
                        } catch (Exception e) {
                            return;
                        }
                    }
                    return;
                case 4:
                    smsg = Message.obtain();
                    smsg.what = 16;
                    smsg.obj = new Integer(23);
                    if (PhoneScreenDownService.this.mCallBackHandler != null) {
                        PhoneScreenDownService.this.mCallBackHandler.sendMessage(smsg);
                        Log.d(PhoneScreenDownService.TAG, "MSG_PHONE_POSTURE_DET_TRIGER");
                    }
                    if (PhoneScreenDownService.this.mServiceHandler != null) {
                        try {
                            PhoneScreenDownService.this.mServiceHandler.removeMessages(2);
                            PhoneScreenDownService.this.mServiceHandler.sendEmptyMessage(2);
                            return;
                        } catch (Exception e2) {
                            return;
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public static PhoneScreenDownService getInstance() {
        return mSinglePhoneScreenDownService;
    }

    private PhoneScreenDownService() {
    }

    public boolean startMotionRecognitionService(Context context, Handler handler) {
        Log.d(TAG, "startMotionRecognitionService");
        if (!this.isPhoneScreenDownWorking) {
            this.mContext = context;
            this.isPhoneScreenDownWorking = true;
            this.mCallBackHandler = handler;
            this.mPhonePostureAnalyzer.reset();
            this.mServiceHandler = new PhoneScreenDownServiceHandler(handler.getLooper());
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            if (this.mSensorManager != null) {
                this.mSensorManager.registerListener(this.accelerometerListener, this.mSensorManager.getDefaultSensor(1), 0, this.mServiceHandler);
            }
        }
        Message msg = Message.obtain();
        msg.what = 1;
        if (this.mServiceHandler != null) {
            try {
                this.mServiceHandler.sendMessage(msg);
            } catch (Exception e) {
            }
        }
        return true;
    }

    public boolean stopMotionRecognitionService() {
        Message msg = Message.obtain();
        msg.what = 2;
        if (this.mServiceHandler != null) {
            try {
                this.mServiceHandler.sendMessage(msg);
            } catch (Exception e) {
            }
        }
        Log.d(TAG, "stopMotionRecognitionService " + this.isPhoneScreenDownWorking);
        if (this.isPhoneScreenDownWorking) {
            this.isPhoneScreenDownWorking = false;
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
