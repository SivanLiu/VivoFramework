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

public final class PhoneScreenDownPickUpService implements IMotionRecognitionService {
    private static final int MSG_PHONE_SCREEN_DOWN_PICK_UP_DET_START = 1;
    private static final int MSG_PHONE_SCREEN_DOWN_PICK_UP_DET_STOP = 2;
    private static final int MSG_PHONE_SCREEN_DOWN_PICK_UP_DET_TRIGER = 3;
    private static final String TAG = "PhoneScreenDownPickUpService";
    private static PhoneScreenDownPickUpService mSinglePhoneScreenDownPickUpService = new PhoneScreenDownPickUpService();
    private MotionSensorEventListener gyroScopeListener = new MotionSensorEventListener(this, null);
    private boolean isIdle = true;
    private boolean isPhoneScreenDownPickUpWorking = false;
    private Handler mCallBackHandler = null;
    private Context mContext = null;
    private PhonePickUpAnalyzer mPhonePickUpAnalyzer = new PhonePickUpAnalyzer();
    private SensorManager mSensorManager;
    private Handler mServiceHandler = null;

    private class MotionSensorEventListener implements SensorEventListener {
        /* synthetic */ MotionSensorEventListener(PhoneScreenDownPickUpService this$0, MotionSensorEventListener -this1) {
            this();
        }

        private MotionSensorEventListener() {
        }

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == 4 && PhoneScreenDownPickUpService.this.isIdle) {
                PhoneScreenDownPickUpService.this.isIdle = false;
                PhoneScreenDownPickUpService.this.isIdle = PhoneScreenDownPickUpService.this.mPhonePickUpAnalyzer.judge(event);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private class PhonePickUpAnalyzer {
        private static final int DISCARDED_NUM = 5;
        private static final int PICK_UP_ANGLE = 30;
        private static final int SAME_TIME_NUM = 10;
        private static final int SAMPLE_NUM = 600;
        private static final int SAMPLE_TIME = 3000;
        private int analysis_num = 0;
        private float[] angle_sum = new float[3];
        private float current_time = 0.0f;
        private float[][] gyro_data_buf = ((float[][]) Array.newInstance(Float.TYPE, new int[]{600, 3}));
        private float[] gyro_data_time_buf = new float[600];
        private float last_time = 0.0f;
        private int log_count = 0;
        private boolean pick_up_flag = false;
        private float pick_up_x_left_angle = 0.0f;
        private float pick_up_y_left_angle = 0.0f;
        private int same_time_count = 0;
        private int sample_count = 0;
        private float time_sum = 0.0f;
        private boolean triger_flag = false;

        public void reset() {
            this.last_time = 0.0f;
            this.log_count = 0;
            this.same_time_count = 0;
            this.sample_count = 0;
            this.analysis_num = 0;
            this.triger_flag = false;
            this.pick_up_flag = false;
            for (int m = 0; m < 600; m++) {
                for (int n = 0; n < 3; n++) {
                    this.gyro_data_buf[m][n] = 0.0f;
                }
                this.gyro_data_time_buf[m] = 0.0f;
            }
            this.pick_up_x_left_angle = AllConfig.SWITCH_ANGLE - AllConfig.POSTURE_Y_ANGLE;
            if (this.pick_up_x_left_angle < 5.0f) {
                this.pick_up_x_left_angle += 5.0f;
            }
            this.pick_up_y_left_angle = AllConfig.SWITCH_ANGLE - AllConfig.POSTURE_X_ANGLE;
            if (this.pick_up_y_left_angle < 5.0f) {
                this.pick_up_y_left_angle += 5.0f;
            }
        }

        private boolean judge(SensorEvent event) {
            if (this.triger_flag) {
                return true;
            }
            this.current_time = (float) (event.timestamp / 1000000);
            if (this.current_time - this.last_time < 5.0f) {
                if (this.last_time == this.current_time) {
                    this.same_time_count++;
                    Log.d(PhoneScreenDownPickUpService.TAG, "last_time is same as current_time.");
                } else {
                    this.same_time_count = 0;
                }
                if (this.last_time > this.current_time) {
                    this.last_time = this.current_time;
                    Log.d(PhoneScreenDownPickUpService.TAG, "last_time is bigger than current_time.");
                }
                if (this.same_time_count < 10) {
                    return true;
                }
                this.same_time_count = 11;
            }
            this.last_time = this.current_time;
            for (int m = 599; m > 0; m--) {
                for (int n = 0; n < 3; n++) {
                    this.gyro_data_buf[m][n] = this.gyro_data_buf[m - 1][n];
                    this.gyro_data_time_buf[m] = this.gyro_data_time_buf[m - 1];
                }
            }
            this.gyro_data_buf[0][0] = (event.values[0] * 180.0f) / 3.1415925f;
            this.gyro_data_buf[0][1] = (event.values[1] * 180.0f) / 3.1415925f;
            this.gyro_data_buf[0][2] = (event.values[2] * 180.0f) / 3.1415925f;
            this.gyro_data_time_buf[0] = this.current_time;
            if (this.log_count == 0 || this.log_count > 10) {
                this.log_count = 0;
                Log.d(PhoneScreenDownPickUpService.TAG, " x:" + this.gyro_data_buf[0][0] + " y:" + this.gyro_data_buf[0][1] + " z:" + this.gyro_data_buf[0][2]);
            }
            this.log_count++;
            this.sample_count++;
            if (this.sample_count > 600) {
                this.sample_count = 600;
            }
            if (this.sample_count > 5) {
                this.analysis_num = this.sample_count - 5;
            } else {
                this.analysis_num = 0;
            }
            for (int k = 0; k < 3; k++) {
                this.angle_sum[k] = 0.0f;
            }
            this.time_sum = 0.0f;
            int i = 0;
            while (i < this.analysis_num) {
                for (int j = 0; j < 3; j++) {
                    if (this.same_time_count < 10) {
                        this.angle_sum[j] = this.angle_sum[j] + ((this.gyro_data_buf[i][j] * (this.gyro_data_time_buf[i] - this.gyro_data_time_buf[i + 1])) / 1000.0f);
                    } else {
                        this.angle_sum[j] = this.angle_sum[j] + ((this.gyro_data_buf[i][j] * 10.0f) / 1000.0f);
                    }
                }
                if (Math.abs(this.angle_sum[0]) <= this.pick_up_x_left_angle) {
                    if (Math.abs(this.angle_sum[1]) <= this.pick_up_y_left_angle) {
                        if (this.same_time_count < 10) {
                            this.time_sum = (this.time_sum + this.gyro_data_time_buf[i]) - this.gyro_data_time_buf[i + 1];
                        } else {
                            this.time_sum += 10.0f;
                        }
                        if (this.time_sum > 3000.0f) {
                            break;
                        }
                        i++;
                    } else {
                        this.pick_up_flag = true;
                        Log.d(PhoneScreenDownPickUpService.TAG, "y_axis angle_sum[1]:" + this.angle_sum[1]);
                        break;
                    }
                }
                this.pick_up_flag = true;
                Log.d(PhoneScreenDownPickUpService.TAG, "x_axis angle_sum[0]:" + this.angle_sum[0]);
                break;
            }
            if (!this.pick_up_flag || PhoneScreenDownPickUpService.this.mServiceHandler == null) {
                return true;
            }
            PhoneScreenDownPickUpService.this.mServiceHandler.sendEmptyMessage(3);
            this.triger_flag = true;
            Log.d(PhoneScreenDownPickUpService.TAG, "phone screen down pick up analysis finish.");
            return true;
        }
    }

    private class PhoneScreenDownPickUpServiceHandler extends Handler {
        public PhoneScreenDownPickUpServiceHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d(PhoneScreenDownPickUpService.TAG, "MSG_PHONE_SCREEN_DOWN_PICK_UP_DET_START");
                    return;
                case 2:
                    Log.d(PhoneScreenDownPickUpService.TAG, "MSG_PHONE_SCREEN_DOWN_PICK_UP_DET_STOP");
                    return;
                case 3:
                    Message smsg = Message.obtain();
                    smsg.what = 16;
                    smsg.obj = new Integer(24);
                    if (PhoneScreenDownPickUpService.this.mCallBackHandler != null) {
                        PhoneScreenDownPickUpService.this.mCallBackHandler.sendMessage(smsg);
                        Log.d(PhoneScreenDownPickUpService.TAG, "MSG_PHONE_SCREEN_DOWN_PICK_UP_DET_TRIGER");
                    }
                    if (PhoneScreenDownPickUpService.this.mServiceHandler != null) {
                        PhoneScreenDownPickUpService.this.mServiceHandler.removeMessages(2);
                        PhoneScreenDownPickUpService.this.mServiceHandler.sendEmptyMessage(2);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public static PhoneScreenDownPickUpService getInstance() {
        return mSinglePhoneScreenDownPickUpService;
    }

    private PhoneScreenDownPickUpService() {
    }

    public boolean startMotionRecognitionService(Context context, Handler handler) {
        Log.d(TAG, "startMotionRecognitionService");
        if (!this.isPhoneScreenDownPickUpWorking) {
            this.mContext = context;
            this.isPhoneScreenDownPickUpWorking = true;
            this.mCallBackHandler = handler;
            this.mPhonePickUpAnalyzer.reset();
            this.mServiceHandler = new PhoneScreenDownPickUpServiceHandler(handler.getLooper());
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            if (this.mSensorManager != null) {
                this.mSensorManager.registerListener(this.gyroScopeListener, this.mSensorManager.getDefaultSensor(4), 0, this.mServiceHandler);
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
        Log.d(TAG, "stopMotionRecognitionService " + this.isPhoneScreenDownPickUpWorking);
        if (this.isPhoneScreenDownPickUpWorking) {
            this.isPhoneScreenDownPickUpWorking = false;
            this.mServiceHandler.removeMessages(2);
            this.mCallBackHandler = null;
            this.mServiceHandler = null;
            if (this.mSensorManager != null) {
                this.mSensorManager.unregisterListener(this.gyroScopeListener);
            }
            this.mSensorManager = null;
        }
        return true;
    }
}
