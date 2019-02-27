package com.vivo.services.motion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.vivo.common.autobrightness.AblConfig;
import com.vivo.common.autobrightness.StateInfo;
import com.vivo.common.provider.Calendar.Events;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import vivo.app.VivoFrameworkFactory;
import vivo.app.proxcali.AbsVivoProxCaliManager;

public final class DirectCallingService implements IMotionRecognitionService {
    private static final int INIT_STATE_DATA_THRESHOLD = 10;
    private static final String KEY_MAX_CREST_CNT = "MaxCrestCnt";
    private static final String KEY_MAX_MOTION_IDX = "MaxMotionIdx";
    private static final String KEY_MOTION_MAX = "MontionMax";
    private static final String KEY_MOTION_MIN = "MontionMin";
    private static final String KEY_WIN_RESAMPLE = "WinReSample";
    private static final int MAG_VIBRATOR_STATIC_THRESHOLD = 3;
    private static final int MOTION_DET_SAMPLE_TOTAL = 64;
    private static final int MOTION_DET_STATE_ANALIST = 2;
    private static final int MOTION_DET_STATE_GETDATA = 1;
    private static final int MOTION_DET_STATE_IDLE = 0;
    private static final int MOTION_DET_STATE_RECOVER = 3;
    private static final int MSG_CONVER_STATE_TO_ANALIST = 2;
    private static final int MSG_CONVER_STATE_TO_GETDATA = 1;
    private static final int MSG_CONVER_STATE_TO_IDLE = 0;
    private static final int MSG_CONVER_STATE_TO_RECOVER = 3;
    private static final int MSG_GYRO_RATE_SWITCH_TO_HIGH = 20;
    private static final int MSG_GYRO_RATE_SWITCH_TO_LOW = 10;
    private static final int MSG_GYRO_STOP = 15;
    private static final int MSG_PICK_UP_ACTION_TRIGER = 4;
    private static final int MSG_PROXIMITY_PARAM_DISABLE = 6;
    private static final int MSG_PROXIMITY_PARAM_ENABLE = 5;
    private static final String TAG = "DirectCallingService";
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static float[] last_mag_data = new float[3];
    private static final String mConfigFile = "/etc/motion_recognition.conf";
    private static final Object mDataLock = new Object();
    private static final Object mHandleLock = new Object();
    private static int mProxyCloseThreshold = 230;
    private static DirectCallingService singleDirectCallingService = new DirectCallingService();
    private MotionSensorEventListener GproScopeListener;
    private int MAX_CREST_CNT;
    private int MAX_CREST_CNT_1222;
    private int MAX_MOTION_IDX;
    private int MAX_MOTION_IDX_1222;
    private float MOTION_AKM_MAX;
    private float MOTION_MAX;
    private float MOTION_MAX_AKM;
    private float MOTION_MIN;
    private float MOTION_MIN_1222;
    private float MOTION_MIN_X_1222;
    private float MOTION_MIN_Y_1222;
    private MotionSensorEventListener MagneticListener;
    private MotionAccPara MotionAccDevParaX;
    private MotionAccPara MotionAccDevParaY;
    private MotionAccPara MotionAccDevParaZ;
    private MotionAngDevPara MotionAngDevParaX;
    private MotionAngDevPara MotionAngDevParaY;
    private MotionAngDevPara MotionAngDevParaZ;
    private SensorManager MotionDetSensorManager;
    private int MotionDetState;
    private int ProximitState;
    private MotionSensorEventListener ProximityListener;
    private MotionSensorEventListener RotationVectorListener;
    private int VibratorState;
    private int VibratorStateCount;
    private int VibratorStaticCount;
    private float WIN_RESAMPLE;
    private MotionSensorEventListener acceleromererListener;
    int callVibrateSetting;
    private int data_abandon;
    float[] degreeSumCall;
    private boolean first_flag;
    private int gyroCount;
    private boolean gyroHighFlag;
    private float[][] gyro_data_buf;
    private boolean isDirectCallingServiceWorking;
    private AudioManager mAudioManager;
    private Handler mCallBackHandler;
    private Context mContext;
    private boolean mDataOperate;
    private boolean mEnabled;
    private Handler mProximityDataHandler;
    private HandlerThread mProximityDataThread;
    private Sensor mProximitySensor;
    private Handler mServiceHandler;
    private AbsVivoProxCaliManager mVivoProxCaliManager;
    private int mag_ab;
    private int mag_vibrator_counter;
    private boolean mag_vibrator_state;
    private int pick_hand;
    private int pick_up_cnt_motion;
    private int pick_up_cnt_threshhold;
    private long pick_up_count;
    private long pick_up_time;
    private long prox_away_time;
    private float prox_close_acc_x;
    private float prox_close_acc_y;
    private float prox_close_acc_z;
    int ringLevel;
    private long[] time_buf;

    private class DirectCallingServiceHandler extends Handler {
        public DirectCallingServiceHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            Log.d(DirectCallingService.TAG, "MotionDet State " + DirectCallingService.this.MotionDetState + " msg " + msg.what);
            switch (DirectCallingService.this.MotionDetState) {
                case 0:
                    Log.d(DirectCallingService.TAG, "MOTION_DET_STATE_IDLE");
                    DirectCallingService.this.MotionDetectIdleStateProcess(msg.what);
                    return;
                case 1:
                    DirectCallingService.this.MotionDetectGetDataStateProcess(msg.what);
                    return;
                case 2:
                    DirectCallingService.this.MotionDetectAnalistStateProcess(msg.what);
                    return;
                case 3:
                    DirectCallingService.this.MotionDetectRecoverStateProcess(msg.what);
                    return;
                case 10:
                    Log.d(DirectCallingService.TAG, "MSG_GYRO_RATE_SWITCH_TO_LOW");
                    if (DirectCallingService.this.MotionDetSensorManager != null) {
                        DirectCallingService.this.first_flag = true;
                        DirectCallingService.this.MotionDetSensorManager.registerListener(DirectCallingService.this.GproScopeListener, DirectCallingService.this.MotionDetSensorManager.getDefaultSensor(4), 66667);
                        return;
                    }
                    return;
                case 15:
                    Log.d(DirectCallingService.TAG, "MSG_GYRO_STOP");
                    if (DirectCallingService.this.MotionDetSensorManager != null) {
                        DirectCallingService.this.MotionDetSensorManager.unregisterListener(DirectCallingService.this.GproScopeListener);
                        return;
                    }
                    return;
                case 20:
                    Log.d(DirectCallingService.TAG, "MSG_GYRO_RATE_SWITCH_TO_HIGH");
                    DirectCallingService.this.gyroHighFlag = true;
                    if (DirectCallingService.this.MotionDetSensorManager != null) {
                        DirectCallingService.this.first_flag = true;
                        DirectCallingService.this.MotionDetSensorManager.registerListener(DirectCallingService.this.GproScopeListener, DirectCallingService.this.MotionDetSensorManager.getDefaultSensor(4), 20000);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class MotionAccPara {
        int acc_idx;
        float acc_value;
        float curr_device_Acc;
        float old_device_Acc;
        float tem_device_Acc;

        /* synthetic */ MotionAccPara(DirectCallingService this$0, MotionAccPara -this1) {
            this();
        }

        private MotionAccPara() {
            this.acc_value = 0.0f;
            this.acc_idx = 0;
            this.curr_device_Acc = 0.0f;
            this.old_device_Acc = 0.0f;
            this.tem_device_Acc = 0.0f;
        }
    }

    private class MotionAngDevPara {
        float curr_device_Ang;
        float old_device_Ang;
        float tem_device_Ang;

        /* synthetic */ MotionAngDevPara(DirectCallingService this$0, MotionAngDevPara -this1) {
            this();
        }

        private MotionAngDevPara() {
            this.curr_device_Ang = 0.0f;
            this.old_device_Ang = 0.0f;
            this.tem_device_Ang = 0.0f;
        }
    }

    private class MotionAngVerPara {
        int gpro_idx;
        float gpro_value;

        /* synthetic */ MotionAngVerPara(DirectCallingService this$0, MotionAngVerPara -this1) {
            this();
        }

        private MotionAngVerPara() {
            this.gpro_value = 0.0f;
            this.gpro_idx = 0;
        }
    }

    private class MotionSensorEventListener implements SensorEventListener {
        private long current;
        private int data_ab;
        public float[][] data_collect_buf;
        public int data_collect_idx;
        private long last;
        private int last_prox;
        private int pick_up_cnt;
        public int stable_cnt;

        /* synthetic */ MotionSensorEventListener(DirectCallingService this$0, MotionSensorEventListener -this1) {
            this();
        }

        private MotionSensorEventListener() {
            this.data_collect_buf = (float[][]) Array.newInstance(Float.TYPE, new int[]{64, 3});
            this.data_collect_idx = 0;
            this.stable_cnt = 0;
            this.last_prox = -1;
            this.pick_up_cnt = 0;
            this.current = 0;
            this.last = 0;
            this.data_ab = 0;
        }

        public void onSensorChanged(SensorEvent event) {
            int m;
            int n;
            if (!(DirectCallingService.this.MotionDetState == 1 || DirectCallingService.this.MotionDetState == 2 || ((AllConfig.mIsAKMVirtGryo && this.data_ab % 2 == 0) || (DirectCallingService.this.first_flag && event.sensor.getType() == 4)))) {
                for (m = 63; m > 0; m--) {
                    for (n = 0; n < 3; n++) {
                        this.data_collect_buf[m][n] = this.data_collect_buf[m - 1][n];
                    }
                }
                this.data_collect_buf[0][0] = event.values[0];
                this.data_collect_buf[0][1] = event.values[1];
                this.data_collect_buf[0][2] = event.values[2];
                this.data_collect_idx++;
                if (this.data_collect_idx == 64) {
                    this.data_collect_idx = 0;
                }
            }
            DirectCallingService directCallingService;
            switch (event.sensor.getType()) {
                case 1:
                    if (!(DirectCallingService.this.MotionDetState == 1 || DirectCallingService.this.MotionDetState == 2)) {
                        if (AllConfig.mIsAKMVirtGryo && DirectCallingService.this.callVibrateSetting == 1) {
                            if (3.5f >= Math.abs(event.values[1]) || 9.0f >= Math.abs(event.values[2]) || 10.6f <= Math.abs(event.values[2])) {
                                DirectCallingService.this.VibratorStateCount = 0;
                                directCallingService = DirectCallingService.this;
                                directCallingService.VibratorStaticCount = directCallingService.VibratorStaticCount + 1;
                                if (3 < DirectCallingService.this.VibratorStaticCount) {
                                    DirectCallingService.this.VibratorState = 0;
                                }
                            } else {
                                directCallingService = DirectCallingService.this;
                                directCallingService.VibratorStateCount = directCallingService.VibratorStateCount + 1;
                                if (DirectCallingService.this.VibratorStateCount > 0) {
                                    DirectCallingService.this.VibratorState = DirectCallingService.this.VibratorStateCount;
                                }
                                DirectCallingService.this.VibratorStaticCount = 0;
                            }
                        }
                        if ((AllConfig.mIsYASVirtGryo || AllConfig.mIsADSPAKMVirtGryo) && DirectCallingService.this.callVibrateSetting == 1) {
                            if (8.5f >= Math.abs(event.values[2]) || 11.0f <= Math.abs(event.values[2])) {
                                DirectCallingService.this.VibratorStateCount = 0;
                                directCallingService = DirectCallingService.this;
                                directCallingService.VibratorStaticCount = directCallingService.VibratorStaticCount + 1;
                                if (3 < DirectCallingService.this.VibratorStaticCount) {
                                    DirectCallingService.this.VibratorState = 0;
                                }
                            } else {
                                directCallingService = DirectCallingService.this;
                                directCallingService.VibratorStateCount = directCallingService.VibratorStateCount + 1;
                                if (DirectCallingService.this.VibratorStateCount > 0) {
                                    DirectCallingService.this.VibratorState = DirectCallingService.this.VibratorStateCount;
                                }
                                DirectCallingService.this.VibratorStaticCount = 0;
                            }
                        }
                        if (!(AllConfig.mIsAKMVirtGryo && this.data_ab % 2 == 0)) {
                            DirectCallingService.this.MotionAccDevParaX.curr_device_Acc = event.values[0];
                            DirectCallingService.this.MotionAccDevParaY.curr_device_Acc = event.values[1];
                            DirectCallingService.this.MotionAccDevParaZ.curr_device_Acc = event.values[2];
                            float diff_acc_x = Math.abs(DirectCallingService.this.MotionAccDevParaX.curr_device_Acc - DirectCallingService.this.MotionAccDevParaX.tem_device_Acc);
                            float diff_acc_y = Math.abs(DirectCallingService.this.MotionAccDevParaY.curr_device_Acc - DirectCallingService.this.MotionAccDevParaY.tem_device_Acc);
                            float diff_acc_z = Math.abs(DirectCallingService.this.MotionAccDevParaZ.curr_device_Acc - DirectCallingService.this.MotionAccDevParaZ.tem_device_Acc);
                            if (diff_acc_x > 2.0f || ((double) diff_acc_y) > 2.0d || ((double) diff_acc_z) > 2.0d) {
                                DirectCallingService.this.MotionAccDevParaX.tem_device_Acc = DirectCallingService.this.MotionAccDevParaX.curr_device_Acc;
                                DirectCallingService.this.MotionAccDevParaY.tem_device_Acc = DirectCallingService.this.MotionAccDevParaY.curr_device_Acc;
                                DirectCallingService.this.MotionAccDevParaZ.tem_device_Acc = DirectCallingService.this.MotionAccDevParaZ.curr_device_Acc;
                                this.stable_cnt = 0;
                            } else {
                                this.stable_cnt++;
                            }
                            if (this.stable_cnt >= 5) {
                                DirectCallingService.this.MotionAccDevParaX.old_device_Acc = DirectCallingService.this.MotionAccDevParaX.curr_device_Acc;
                                DirectCallingService.this.MotionAccDevParaY.old_device_Acc = DirectCallingService.this.MotionAccDevParaY.curr_device_Acc;
                                DirectCallingService.this.MotionAccDevParaZ.old_device_Acc = DirectCallingService.this.MotionAccDevParaZ.curr_device_Acc;
                                this.stable_cnt = 0;
                            }
                            DirectCallingService.this.prox_close_acc_x = event.values[0];
                            DirectCallingService.this.prox_close_acc_y = event.values[1];
                            DirectCallingService.this.prox_close_acc_z = event.values[2];
                            break;
                        }
                    }
                    break;
                case 2:
                    if (10 > DirectCallingService.this.mag_ab) {
                        directCallingService = DirectCallingService.this;
                        directCallingService.mag_ab = directCallingService.mag_ab + 1;
                        Log.d(DirectCallingService.TAG, " TYPE_MAG test 1");
                        DirectCallingService.this.mag_vibrator_state = true;
                    }
                    if (10 == DirectCallingService.this.mag_ab) {
                        DirectCallingService.last_mag_data[0] = event.values[0];
                        DirectCallingService.last_mag_data[1] = event.values[1];
                        DirectCallingService.last_mag_data[2] = event.values[2];
                        directCallingService = DirectCallingService.this;
                        directCallingService.mag_ab = directCallingService.mag_ab + 1;
                        Log.d(DirectCallingService.TAG, " TYPE_MAG test 2 last_mag_data[0]:" + DirectCallingService.last_mag_data[0] + ",last_mag_data[1]:" + DirectCallingService.last_mag_data[1] + ",last_mag_data[2]:" + DirectCallingService.last_mag_data[2]);
                    }
                    if (10 < DirectCallingService.this.mag_ab) {
                        float[] mag_data_delta = new float[]{Math.abs(event.values[0] - DirectCallingService.last_mag_data[0]), Math.abs(event.values[1] - DirectCallingService.last_mag_data[1]), Math.abs(event.values[2] - DirectCallingService.last_mag_data[2])};
                        if (mag_data_delta[0] < 2.0f && mag_data_delta[1] < 2.0f && mag_data_delta[2] < 8.0f) {
                            directCallingService = DirectCallingService.this;
                            directCallingService.mag_vibrator_counter = directCallingService.mag_vibrator_counter + 1;
                            if (3 < DirectCallingService.this.mag_vibrator_counter) {
                                DirectCallingService.this.mag_vibrator_state = true;
                                break;
                            }
                        }
                        DirectCallingService.this.mag_vibrator_state = false;
                        DirectCallingService.this.mag_vibrator_counter = 0;
                        break;
                    }
                    break;
                case 4:
                    String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
                    if (!DirectCallingService.this.first_flag) {
                        this.current = event.timestamp / 1000000;
                        if ((!AllConfig.mIsYASVirtGryo || this.current - this.last >= 15) && !(AllConfig.mIsAKMVirtGryo && this.data_ab % 2 == 0)) {
                            if (!DirectCallingService.this.mDataOperate) {
                                synchronized (DirectCallingService.mDataLock) {
                                    for (m = 64; m > 0; m--) {
                                        for (n = 0; n < 3; n++) {
                                            DirectCallingService.this.gyro_data_buf[m][n] = DirectCallingService.this.gyro_data_buf[m - 1][n];
                                        }
                                        DirectCallingService.this.time_buf[m] = DirectCallingService.this.time_buf[m - 1];
                                    }
                                    if (AllConfig.mIsAKMVirtGryo) {
                                        for (n = 0; n < 3; n++) {
                                            float data_temp = event.values[n] - DirectCallingService.this.gyro_data_buf[1][n];
                                            if (DirectCallingService.this.MOTION_AKM_MAX <= data_temp) {
                                                DirectCallingService.this.gyro_data_buf[0][n] = DirectCallingService.this.gyro_data_buf[1][n] + DirectCallingService.this.MOTION_AKM_MAX;
                                            } else if ((-DirectCallingService.this.MOTION_AKM_MAX) >= data_temp) {
                                                DirectCallingService.this.gyro_data_buf[0][n] = DirectCallingService.this.gyro_data_buf[1][n] - DirectCallingService.this.MOTION_AKM_MAX;
                                            } else {
                                                DirectCallingService.this.gyro_data_buf[0][n] = event.values[n];
                                            }
                                        }
                                    } else {
                                        DirectCallingService.this.gyro_data_buf[0][0] = event.values[0];
                                        DirectCallingService.this.gyro_data_buf[0][1] = event.values[1];
                                        DirectCallingService.this.gyro_data_buf[0][2] = event.values[2];
                                    }
                                    DirectCallingService.this.time_buf[0] = event.timestamp / 1000000;
                                }
                            }
                            if (this.current - this.last > 150 && this.current - this.last < 250) {
                                Log.d(DirectCallingService.TAG, " PG need to regiseter  gyro again");
                                DirectCallingService.this.MotionDetSensorManager.unregisterListener(DirectCallingService.this.GproScopeListener);
                                DirectCallingService.this.first_flag = true;
                                if (AllConfig.mIsAKMVirtGryo) {
                                    DirectCallingService.this.MotionDetSensorManager.registerListener(DirectCallingService.this.GproScopeListener, DirectCallingService.this.MotionDetSensorManager.getDefaultSensor(4), StateInfo.STATE_FINGERPRINT_GOTO_SLEEP, DirectCallingService.this.mCallBackHandler);
                                } else {
                                    DirectCallingService.this.MotionDetSensorManager.registerListener(DirectCallingService.this.GproScopeListener, DirectCallingService.this.MotionDetSensorManager.getDefaultSensor(4), 20000, DirectCallingService.this.mCallBackHandler);
                                }
                            }
                            if (!AllConfig.mIsAKMVirtGryo || (DirectCallingService.this.callVibrateSetting != 1 && (!AllConfig.mIsDoubleloudspeaker || DirectCallingService.this.ringLevel <= 5))) {
                                if ((AllConfig.mIsYASVirtGryo || AllConfig.mIsADSPAKMVirtGryo) && DirectCallingService.this.callVibrateSetting == 1) {
                                    if (((double) Math.abs(event.values[0])) <= 0.8d || ((double) Math.abs(event.values[2])) <= 1.0d || ((double) Math.abs(event.values[1])) <= 0.4d) {
                                        directCallingService = DirectCallingService.this;
                                        this.pick_up_cnt = 0;
                                        directCallingService.pick_up_cnt_motion = 0;
                                    } else {
                                        Log.d(DirectCallingService.TAG, "[PG_Data]" + Math.abs(event.values[0]) + ":" + Math.abs(event.values[2]) + ":" + Math.abs(event.values[1]) + ":pick_cnt" + this.pick_up_cnt + "mag_vibrator_state: " + DirectCallingService.this.mag_vibrator_state + "VibratorState: " + DirectCallingService.this.VibratorState);
                                        if (4 > DirectCallingService.this.VibratorState && (DirectCallingService.this.mag_vibrator_state ^ 1) != 0 && 1 == DirectCallingService.this.ProximitState) {
                                            this.pick_up_cnt++;
                                            DirectCallingService.this.pick_up_cnt_motion = this.pick_up_cnt;
                                        }
                                    }
                                } else if (AllConfig.mIsADSPAKMVirtGryo && DirectCallingService.this.callVibrateSetting == 1) {
                                    if (((double) Math.abs(event.values[0])) <= 0.5d || ((double) Math.abs(event.values[2])) <= 0.5d || ((double) Math.abs(event.values[1])) <= 0.4d) {
                                        directCallingService = DirectCallingService.this;
                                        this.pick_up_cnt = 0;
                                        directCallingService.pick_up_cnt_motion = 0;
                                    } else {
                                        Log.d(DirectCallingService.TAG, "[PG_Data]" + Math.abs(event.values[0]) + ":" + Math.abs(event.values[2]) + ":" + Math.abs(event.values[1]) + ":pick_cnt" + this.pick_up_cnt + "mag_vibrator_state: " + DirectCallingService.this.mag_vibrator_state + "VibratorState: " + DirectCallingService.this.VibratorState);
                                        if (4 > DirectCallingService.this.VibratorState && (DirectCallingService.this.mag_vibrator_state ^ 1) != 0 && 1 == DirectCallingService.this.ProximitState) {
                                            this.pick_up_cnt++;
                                            DirectCallingService.this.pick_up_cnt_motion = this.pick_up_cnt;
                                        }
                                    }
                                } else if (((double) Math.abs(event.values[0])) <= 0.8d || ((double) Math.abs(event.values[2])) <= 0.5d || ((double) Math.abs(event.values[1])) <= 0.4d) {
                                    directCallingService = DirectCallingService.this;
                                    this.pick_up_cnt = 0;
                                    directCallingService.pick_up_cnt_motion = 0;
                                } else {
                                    Log.d(DirectCallingService.TAG, "[PG_Data]" + Math.abs(event.values[0]) + ":" + Math.abs(event.values[2]) + ":" + Math.abs(event.values[1]) + ":pick_cnt" + this.pick_up_cnt);
                                    if (DirectCallingService.this.ProximitState == 1) {
                                        this.pick_up_cnt++;
                                    }
                                    DirectCallingService.this.pick_up_cnt_motion = this.pick_up_cnt;
                                }
                            } else if ((((double) Math.abs(event.values[0])) <= 1.5d || ((double) Math.abs(event.values[2])) <= 1.5d || ((double) Math.abs(event.values[1])) <= 0.4d) && ((double) ((Math.abs(event.values[0]) + Math.abs(event.values[2])) + Math.abs(event.values[1]))) <= 3.0d) {
                                directCallingService = DirectCallingService.this;
                                this.pick_up_cnt = 0;
                                directCallingService.pick_up_cnt_motion = 0;
                            } else {
                                Log.d(DirectCallingService.TAG, "[PG_Data]" + Math.abs(event.values[0]) + ":" + Math.abs(event.values[2]) + ":" + Math.abs(event.values[1]) + ":pick_cnt" + this.pick_up_cnt + "callVibrateSetting " + DirectCallingService.this.callVibrateSetting + " VibratorState:" + DirectCallingService.this.VibratorState + " mag_vibrator_state:" + DirectCallingService.this.mag_vibrator_state);
                                if (5 > DirectCallingService.this.VibratorState && (DirectCallingService.this.mag_vibrator_state ^ 1) != 0 && 1 == DirectCallingService.this.ProximitState) {
                                    this.pick_up_cnt++;
                                    DirectCallingService.this.pick_up_cnt_motion = this.pick_up_cnt;
                                }
                            }
                            if (((double) Math.abs(event.values[0])) > 10.0d || ((double) Math.abs(event.values[1])) > 10.0d || ((double) Math.abs(event.values[2])) > 12.0d) {
                                directCallingService = DirectCallingService.this;
                                this.pick_up_cnt = 0;
                                directCallingService.pick_up_cnt_motion = 0;
                                Log.w(DirectCallingService.TAG, "GYROSCOPE DATA ERROR");
                            }
                            if (this.pick_up_cnt >= DirectCallingService.this.pick_up_cnt_threshhold && DirectCallingService.this.mServiceHandler != null) {
                                DirectCallingService.this.pick_up_count = DirectCallingService.this.pick_up_count + 1;
                                DirectCallingService.this.pick_up_time = event.timestamp / 1000000;
                                if (DirectCallingService.this.pickupDegreeAnalist()) {
                                    directCallingService = DirectCallingService.this;
                                    this.pick_up_cnt = 0;
                                    directCallingService.pick_up_cnt_motion = 0;
                                    if (AllConfig.mVibrate_badly) {
                                        Log.d(DirectCallingService.TAG, "vibrate_badly_project pick up phone");
                                        if (((double) DirectCallingService.this.prox_close_acc_y) > 3.5d || ((double) Math.abs(DirectCallingService.this.prox_close_acc_x)) > 2.5d) {
                                            synchronized (DirectCallingService.mHandleLock) {
                                                if (DirectCallingService.this.mServiceHandler != null) {
                                                    DirectCallingService.this.mServiceHandler.sendEmptyMessage(4);
                                                }
                                            }
                                            Log.d(DirectCallingService.TAG, "vibrate_badly_project the acc value is x: " + DirectCallingService.this.prox_close_acc_x + " y: " + DirectCallingService.this.prox_close_acc_y + " z: " + DirectCallingService.this.prox_close_acc_z);
                                        } else {
                                            Log.d(DirectCallingService.TAG, "vibrate_badly_project wrong judge, real x: " + DirectCallingService.this.prox_close_acc_x + " y: " + DirectCallingService.this.prox_close_acc_y + " z: " + DirectCallingService.this.prox_close_acc_z);
                                        }
                                    } else {
                                        Log.d(DirectCallingService.TAG, "normal_project pick up phone");
                                        synchronized (DirectCallingService.mHandleLock) {
                                            if (DirectCallingService.this.mServiceHandler != null) {
                                                DirectCallingService.this.mServiceHandler.sendEmptyMessage(4);
                                            }
                                        }
                                    }
                                }
                            }
                            this.last = this.current;
                            break;
                        }
                    }
                    directCallingService = DirectCallingService.this;
                    directCallingService.data_abandon = directCallingService.data_abandon + 1;
                    if (3 <= DirectCallingService.this.data_abandon) {
                        DirectCallingService.this.first_flag = false;
                        DirectCallingService.this.data_abandon = 0;
                        break;
                    }
                    break;
                case 8:
                case StateInfo.STATE_BIT_LIGHT /*32*/:
                    float mProximityThreshold = 0.0f;
                    if (DirectCallingService.this.MotionDetSensorManager == null && DirectCallingService.this.mContext != null) {
                        DirectCallingService.this.MotionDetSensorManager = (SensorManager) DirectCallingService.this.mContext.getSystemService("sensor");
                    }
                    if (DirectCallingService.this.MotionDetSensorManager != null) {
                        DirectCallingService.this.mProximitySensor = DirectCallingService.this.MotionDetSensorManager.getDefaultSensor(8);
                    }
                    if (DirectCallingService.this.mProximitySensor != null) {
                        mProximityThreshold = Math.min(DirectCallingService.this.mProximitySensor.getMaximumRange(), DirectCallingService.TYPICAL_PROXIMITY_THRESHOLD);
                    }
                    directCallingService = DirectCallingService.this;
                    int logic_value = (event.values[0] < 0.0f || event.values[0] >= mProximityThreshold) ? 1 : 0;
                    directCallingService.ProximitState = logic_value;
                    Log.d(DirectCallingService.TAG, "logic_value:" + logic_value + "last_prox:" + this.last_prox);
                    if (this.last_prox != logic_value) {
                        Object -get22;
                        if (logic_value != 0 || this.last_prox != 1) {
                            if (logic_value == 1 && this.last_prox == 0) {
                                -get22 = DirectCallingService.mHandleLock;
                                synchronized (-get22) {
                                    if (DirectCallingService.this.mServiceHandler != null) {
                                        DirectCallingService.this.mServiceHandler.removeMessages(3);
                                        DirectCallingService.this.mServiceHandler.sendEmptyMessage(3);
                                    }
                                }
                            }
                            this.last_prox = logic_value;
                            break;
                        }
                        -get22 = DirectCallingService.mHandleLock;
                        synchronized (-get22) {
                            if (DirectCallingService.this.mServiceHandler != null) {
                                DirectCallingService.this.mServiceHandler.sendEmptyMessageDelayed(1, 200);
                                DirectCallingService.this.prox_away_time = event.timestamp / 1000000;
                            }
                        }
                        this.last_prox = logic_value;
                    } else {
                        return;
                    }
                    break;
                case 11:
                    if (!(DirectCallingService.this.MotionDetState == 1 || DirectCallingService.this.MotionDetState == 2)) {
                        DirectCallingService.this.MotionAngDevParaX.curr_device_Ang = event.values[0];
                        DirectCallingService.this.MotionAngDevParaY.curr_device_Ang = event.values[1];
                        DirectCallingService.this.MotionAngDevParaZ.curr_device_Ang = event.values[2];
                        float diff_ang_x = Math.abs(DirectCallingService.this.MotionAngDevParaX.curr_device_Ang - DirectCallingService.this.MotionAngDevParaX.tem_device_Ang);
                        if (diff_ang_x > 1.0f) {
                            diff_ang_x = 2.0f - diff_ang_x;
                        }
                        float diff_ang_y = Math.abs(DirectCallingService.this.MotionAngDevParaY.curr_device_Ang - DirectCallingService.this.MotionAngDevParaY.tem_device_Ang);
                        if (diff_ang_y > 1.0f) {
                            diff_ang_y = 2.0f - diff_ang_y;
                        }
                        float diff_ang_z = Math.abs(DirectCallingService.this.MotionAngDevParaZ.curr_device_Ang - DirectCallingService.this.MotionAngDevParaZ.tem_device_Ang);
                        if (diff_ang_z > 1.0f) {
                            diff_ang_z = 2.0f - diff_ang_z;
                        }
                        if (diff_ang_x > 0.02f || diff_ang_y > 0.02f || diff_ang_z > 0.02f) {
                            DirectCallingService.this.MotionAngDevParaX.tem_device_Ang = DirectCallingService.this.MotionAngDevParaX.curr_device_Ang;
                            DirectCallingService.this.MotionAngDevParaY.tem_device_Ang = DirectCallingService.this.MotionAngDevParaY.curr_device_Ang;
                            DirectCallingService.this.MotionAngDevParaZ.tem_device_Ang = DirectCallingService.this.MotionAngDevParaZ.curr_device_Ang;
                            this.stable_cnt = 0;
                        } else {
                            this.stable_cnt++;
                        }
                        if (this.stable_cnt >= 5) {
                            DirectCallingService.this.MotionAngDevParaX.old_device_Ang = DirectCallingService.this.MotionAngDevParaX.curr_device_Ang;
                            DirectCallingService.this.MotionAngDevParaY.old_device_Ang = DirectCallingService.this.MotionAngDevParaY.curr_device_Ang;
                            DirectCallingService.this.MotionAngDevParaZ.old_device_Ang = DirectCallingService.this.MotionAngDevParaZ.curr_device_Ang;
                            this.stable_cnt = 0;
                            break;
                        }
                    }
                    break;
            }
            this.data_ab++;
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void resetDataBuf() {
            for (int tem_idx = 0; tem_idx < 64; tem_idx++) {
                this.data_collect_buf[tem_idx][0] = 0.0f;
                this.data_collect_buf[tem_idx][1] = 0.0f;
                this.data_collect_buf[tem_idx][2] = 0.0f;
            }
            this.data_collect_idx = 0;
            Log.d(DirectCallingService.TAG, "resetDataBuf");
        }
    }

    private void resetAllDataBuf() {
        this.acceleromererListener.resetDataBuf();
        this.GproScopeListener.resetDataBuf();
        this.RotationVectorListener.resetDataBuf();
        this.ProximityListener.resetDataBuf();
        this.MagneticListener.resetDataBuf();
    }

    private void clearGyrdata() {
        for (int m = 0; m < 64; m++) {
            for (int n = 0; n < 3; n++) {
                this.gyro_data_buf[m][n] = 0.0f;
            }
            this.time_buf[m] = 0;
        }
        float[] fArr = last_mag_data;
        last_mag_data[2] = 0.0f;
        last_mag_data[1] = 0.0f;
        fArr[0] = 0.0f;
    }

    boolean subMotionAccDevAnalist() {
        boolean result = true;
        float accx = this.prox_close_acc_x;
        float DiffAccX = Math.abs(this.MotionAccDevParaX.curr_device_Acc - this.MotionAccDevParaX.old_device_Acc);
        float DiffAccY = Math.abs(this.MotionAccDevParaY.curr_device_Acc - this.MotionAccDevParaY.old_device_Acc);
        float DiffAccZ = Math.abs(this.MotionAccDevParaZ.curr_device_Acc - this.MotionAccDevParaZ.old_device_Acc);
        Log.d("MotionDet_3", "Perhapse acc diff_data:" + DiffAccX + "," + DiffAccY + "," + DiffAccZ);
        Log.d("MotionDet_3", "Perhapse acc curr_data:" + this.MotionAccDevParaX.curr_device_Acc + "," + this.MotionAccDevParaY.curr_device_Acc + "," + this.MotionAccDevParaZ.curr_device_Acc);
        if (DiffAccX < 4.0f && DiffAccY < 4.0f && DiffAccZ < 4.0f && Math.abs(this.MotionAccDevParaX.curr_device_Acc) < 4.0f && Math.abs(this.MotionAccDevParaY.curr_device_Acc) < 4.0f && Math.abs(this.MotionAccDevParaZ.curr_device_Acc) > TYPICAL_PROXIMITY_THRESHOLD) {
            Log.d("MotionDet_3", "Motion Direct FLAT-----");
            result = false;
        }
        String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
        if ((this.MotionAccDevParaY.curr_device_Acc < -5.0f || this.MotionAccDevParaZ.curr_device_Acc < -8.0f) && (DiffAccX > 0.0f || DiffAccY > 0.0f || DiffAccZ > 0.0f)) {
            Log.d("MotionDet_3", "Motion Direct Down  FLAT-----");
            result = false;
        }
        if (AllConfig.mIsYASVirtGryo || AllConfig.mIsADSPAKMVirtGryo) {
            if (this.MotionAccDevParaY.curr_device_Acc > 11.8f) {
                Log.d("MotionDet_acc", "acc_y not satisfy motion");
                result = false;
            }
        } else if (this.MotionAccDevParaY.curr_device_Acc > 15.0f) {
            Log.d("MotionDet_acc", "acc_y not satisfy motion");
            result = false;
        }
        if ((8.0f < Math.abs(this.MotionAccDevParaX.curr_device_Acc) || 6.0f < Math.abs(this.MotionAccDevParaZ.curr_device_Acc)) && -1.5f > this.MotionAccDevParaY.curr_device_Acc) {
            Log.d("MotionDet_acc", "phone is xz big");
            result = false;
        }
        if (this.prox_close_acc_z < -8.0f && this.prox_close_acc_z > -12.0f && this.prox_close_acc_x < 1.0f && this.prox_close_acc_x > -1.0f && this.prox_close_acc_y < 1.0f && this.prox_close_acc_y > -1.0f) {
            Log.d("MotionDet_acc", "phone is over turn not triger");
            result = false;
        }
        if (this.prox_close_acc_y < -6.0f && this.prox_close_acc_y > -12.0f) {
            Log.d("MotionDet_acc", "phone is head down not triger");
            result = false;
        }
        float accsq = ((this.prox_close_acc_x * this.prox_close_acc_x) + (this.prox_close_acc_y * this.prox_close_acc_y)) + (this.prox_close_acc_z * this.prox_close_acc_z);
        if (16.0f > accsq || 260.0f < accsq) {
            Log.d("MotionDet_acc", "phone move");
            result = false;
        }
        if (result) {
            if (0.0f < accx) {
                this.pick_hand = 1;
            }
            if (0.0f > accx) {
                this.pick_hand = 2;
            }
        }
        Log.d("MotionDet_acc", "prox_close_acc_x:" + this.prox_close_acc_x + "prox_close_acc_y:" + this.prox_close_acc_y + "prox_close_acc_z:" + this.prox_close_acc_z + "accsq " + accsq);
        return result;
    }

    boolean subMotionAngVerAnalist() {
        int triger_t_x;
        int triger_t_y;
        int triger_t_z;
        boolean result = true;
        MotionAngVerPara gpro_max_x = new MotionAngVerPara(this, null);
        MotionAngVerPara motionAngVerPara = new MotionAngVerPara(this, null);
        motionAngVerPara = new MotionAngVerPara(this, null);
        motionAngVerPara = new MotionAngVerPara(this, null);
        motionAngVerPara = new MotionAngVerPara(this, null);
        motionAngVerPara = new MotionAngVerPara(this, null);
        MotionAngVerPara gpro_abs_max_x = new MotionAngVerPara(this, null);
        MotionAngVerPara gpro_abs_max_y = new MotionAngVerPara(this, null);
        MotionAngVerPara gpro_abs_max_z = new MotionAngVerPara(this, null);
        int safe_det_idx = this.GproScopeListener.data_collect_idx - 1;
        if (safe_det_idx < 0) {
            safe_det_idx += 64;
        }
        Log.d("GPRO_RAW_DATA", "safe:" + safe_det_idx);
        int loop_idx = 0;
        while (loop_idx < 64) {
            if ((loop_idx - safe_det_idx > 0 ? loop_idx - safe_det_idx : (loop_idx - safe_det_idx) + 64) < 10) {
                this.GproScopeListener.data_collect_buf[loop_idx][0] = 0.0f;
                this.GproScopeListener.data_collect_buf[loop_idx][1] = 0.0f;
                this.GproScopeListener.data_collect_buf[loop_idx][2] = 0.0f;
            }
            Log.d("GPRO_RAW_DATA", "raw_data+" + this.GproScopeListener.data_collect_buf[loop_idx][0] + "," + this.GproScopeListener.data_collect_buf[loop_idx][1] + "," + this.GproScopeListener.data_collect_buf[loop_idx][2] + "," + loop_idx);
            for (int i = 0; i < 3; i++) {
                if (AllConfig.mIsAKMVirtGryo) {
                    int data_id = loop_idx - 1;
                    if (data_id < 0) {
                        data_id = 63;
                    }
                    if (safe_det_idx == data_id) {
                        data_id = loop_idx;
                    }
                    float data_temp = this.GproScopeListener.data_collect_buf[loop_idx][i] - this.GproScopeListener.data_collect_buf[data_id][i];
                    if (this.MOTION_AKM_MAX <= data_temp) {
                        this.GproScopeListener.data_collect_buf[loop_idx][i] = this.GproScopeListener.data_collect_buf[data_id][i] + this.MOTION_AKM_MAX;
                    }
                    if ((-this.MOTION_AKM_MAX) >= data_temp) {
                        this.GproScopeListener.data_collect_buf[loop_idx][i] = this.GproScopeListener.data_collect_buf[data_id][i] - this.MOTION_AKM_MAX;
                    }
                }
                this.GproScopeListener.data_collect_buf[loop_idx][i] = ((float) ((int) (((double) this.GproScopeListener.data_collect_buf[loop_idx][i]) / 0.6d))) * 0.6f;
            }
            loop_idx++;
        }
        gpro_max_x.gpro_value = this.GproScopeListener.data_collect_buf[0][0];
        motionAngVerPara.gpro_value = this.GproScopeListener.data_collect_buf[0][1];
        motionAngVerPara.gpro_value = this.GproScopeListener.data_collect_buf[0][2];
        motionAngVerPara.gpro_value = this.GproScopeListener.data_collect_buf[0][0];
        motionAngVerPara.gpro_value = this.GproScopeListener.data_collect_buf[0][1];
        motionAngVerPara.gpro_value = this.GproScopeListener.data_collect_buf[0][2];
        float prev_x_value = this.GproScopeListener.data_collect_buf[0][0];
        float prev_y_value = this.GproScopeListener.data_collect_buf[0][1];
        float prev_z_value = this.GproScopeListener.data_collect_buf[0][2];
        gpro_max_x.gpro_idx = 0;
        motionAngVerPara.gpro_idx = 0;
        motionAngVerPara.gpro_idx = 0;
        motionAngVerPara.gpro_idx = 0;
        motionAngVerPara.gpro_idx = 0;
        motionAngVerPara.gpro_idx = 0;
        loop_idx = 0;
        int rise_x_cnt = 0;
        int rise_y_cnt = 0;
        int rise_z_cnt = 0;
        int fall_x_cnt = 0;
        int fall_y_cnt = 0;
        int fall_z_cnt = 0;
        int crest_x_cnt = 0;
        int crest_y_cnt = 0;
        int crest_z_cnt = 0;
        int x_rise_crest = 0;
        int y_rise_crest = 0;
        int z_rise_crest = 0;
        while (loop_idx < 64) {
            if (loop_idx >= 8 || AllConfig.mIsAKMVirtGryo || AllConfig.mIsALPSVirtGryo || AllConfig.mIsYASVirtGryo || AllConfig.mIsADSPAKMVirtGryo || (((double) Math.abs(this.GproScopeListener.data_collect_buf[loop_idx][0])) <= 10.0d && ((double) Math.abs(this.GproScopeListener.data_collect_buf[loop_idx][1])) <= 10.0d && ((double) Math.abs(this.GproScopeListener.data_collect_buf[loop_idx][2])) <= 10.0d)) {
                if (gpro_max_x.gpro_value < this.GproScopeListener.data_collect_buf[loop_idx][0]) {
                    gpro_max_x.gpro_value = this.GproScopeListener.data_collect_buf[loop_idx][0];
                    gpro_max_x.gpro_idx = loop_idx;
                }
                if (motionAngVerPara.gpro_value < this.GproScopeListener.data_collect_buf[loop_idx][1]) {
                    motionAngVerPara.gpro_value = this.GproScopeListener.data_collect_buf[loop_idx][1];
                    motionAngVerPara.gpro_idx = loop_idx;
                }
                if (motionAngVerPara.gpro_value < this.GproScopeListener.data_collect_buf[loop_idx][2]) {
                    motionAngVerPara.gpro_value = this.GproScopeListener.data_collect_buf[loop_idx][2];
                    motionAngVerPara.gpro_idx = loop_idx;
                    Log.d("GPRO_MAX_DATA", "gpro_max_z.gpro_value data " + motionAngVerPara.gpro_value + " " + motionAngVerPara.gpro_idx);
                }
                if (motionAngVerPara.gpro_value > this.GproScopeListener.data_collect_buf[loop_idx][0]) {
                    motionAngVerPara.gpro_value = this.GproScopeListener.data_collect_buf[loop_idx][0];
                    motionAngVerPara.gpro_idx = loop_idx;
                }
                if (motionAngVerPara.gpro_value > this.GproScopeListener.data_collect_buf[loop_idx][1]) {
                    motionAngVerPara.gpro_value = this.GproScopeListener.data_collect_buf[loop_idx][1];
                    motionAngVerPara.gpro_idx = loop_idx;
                }
                if (motionAngVerPara.gpro_value > this.GproScopeListener.data_collect_buf[loop_idx][2]) {
                    motionAngVerPara.gpro_value = this.GproScopeListener.data_collect_buf[loop_idx][2];
                    motionAngVerPara.gpro_idx = loop_idx;
                    Log.d("GPRO_MAX_DATA", "gpro_min_z.gpro_value data " + motionAngVerPara.gpro_value + " " + motionAngVerPara.gpro_idx);
                }
                if (prev_x_value < this.GproScopeListener.data_collect_buf[loop_idx][0]) {
                    rise_x_cnt++;
                } else if (prev_x_value > this.GproScopeListener.data_collect_buf[loop_idx][0]) {
                    fall_x_cnt++;
                }
                if (prev_y_value < this.GproScopeListener.data_collect_buf[loop_idx][1]) {
                    rise_y_cnt++;
                } else if (prev_y_value > this.GproScopeListener.data_collect_buf[loop_idx][1]) {
                    fall_y_cnt++;
                }
                if (prev_z_value < this.GproScopeListener.data_collect_buf[loop_idx][2]) {
                    rise_z_cnt++;
                } else if (prev_z_value > this.GproScopeListener.data_collect_buf[loop_idx][2]) {
                    fall_z_cnt++;
                }
                if (rise_x_cnt > 0 && fall_x_cnt == 0) {
                    x_rise_crest = 1;
                } else if (rise_x_cnt == 0 && fall_x_cnt > 0) {
                    x_rise_crest = 0;
                } else if (rise_x_cnt > 0 && fall_x_cnt > 0) {
                    crest_x_cnt++;
                    if (x_rise_crest == 1) {
                        rise_x_cnt = 0;
                    } else {
                        fall_x_cnt = 0;
                    }
                }
                if (rise_y_cnt > 0 && fall_y_cnt == 0) {
                    y_rise_crest = 1;
                } else if (rise_y_cnt == 0 && fall_y_cnt > 0) {
                    y_rise_crest = 2;
                } else if (rise_y_cnt > 0 && fall_y_cnt > 0) {
                    crest_y_cnt++;
                    if (y_rise_crest == 1) {
                        rise_y_cnt = 0;
                    } else {
                        fall_y_cnt = 0;
                    }
                }
                if (rise_z_cnt > 0 && fall_z_cnt == 0) {
                    z_rise_crest = 1;
                } else if (rise_z_cnt == 0 && fall_z_cnt > 0) {
                    z_rise_crest = 2;
                } else if (rise_z_cnt > 0 && fall_z_cnt > 0) {
                    crest_z_cnt++;
                    if (z_rise_crest == 1) {
                        rise_z_cnt = 0;
                    } else {
                        fall_z_cnt = 0;
                    }
                }
                prev_x_value = this.GproScopeListener.data_collect_buf[loop_idx][0];
                prev_y_value = this.GproScopeListener.data_collect_buf[loop_idx][1];
                prev_z_value = this.GproScopeListener.data_collect_buf[loop_idx][2];
                loop_idx++;
            } else {
                loop_idx++;
            }
        }
        int max_t_x = safe_det_idx - gpro_max_x.gpro_idx;
        int max_t_y = safe_det_idx - motionAngVerPara.gpro_idx;
        int max_t_z = safe_det_idx - motionAngVerPara.gpro_idx;
        if (max_t_x < 0) {
            max_t_x += 64;
        }
        if (max_t_y < 0) {
            max_t_y += 64;
        }
        if (max_t_z < 0) {
            max_t_z += 64;
        }
        int min_t_x = safe_det_idx - motionAngVerPara.gpro_idx;
        int min_t_y = safe_det_idx - motionAngVerPara.gpro_idx;
        int min_t_z = safe_det_idx - motionAngVerPara.gpro_idx;
        if (min_t_x < 0) {
            min_t_x += 64;
        }
        if (min_t_y < 0) {
            min_t_y += 64;
        }
        if (min_t_z < 0) {
            min_t_z += 64;
        }
        if (gpro_max_x.gpro_value * motionAngVerPara.gpro_value >= 0.0f || Math.abs(gpro_max_x.gpro_value) <= this.WIN_RESAMPLE || Math.abs(motionAngVerPara.gpro_value) <= this.WIN_RESAMPLE) {
            gpro_abs_max_x = Math.abs(gpro_max_x.gpro_value) > Math.abs(motionAngVerPara.gpro_value) ? gpro_max_x : motionAngVerPara;
            triger_t_x = Math.abs(gpro_max_x.gpro_value) > Math.abs(motionAngVerPara.gpro_value) ? max_t_x : min_t_x;
        } else {
            gpro_abs_max_x = max_t_x < min_t_x ? gpro_max_x : motionAngVerPara;
            triger_t_x = max_t_x < min_t_x ? max_t_x : min_t_x;
        }
        if (motionAngVerPara.gpro_value * motionAngVerPara.gpro_value >= 0.0f || Math.abs(motionAngVerPara.gpro_value) <= this.WIN_RESAMPLE || Math.abs(motionAngVerPara.gpro_value) <= this.WIN_RESAMPLE) {
            gpro_abs_max_y = Math.abs(motionAngVerPara.gpro_value) > Math.abs(motionAngVerPara.gpro_value) ? motionAngVerPara : motionAngVerPara;
            triger_t_y = Math.abs(motionAngVerPara.gpro_value) > Math.abs(motionAngVerPara.gpro_value) ? max_t_y : min_t_y;
        } else {
            gpro_abs_max_y = max_t_y < min_t_y ? motionAngVerPara : motionAngVerPara;
            triger_t_y = max_t_y < min_t_y ? max_t_y : min_t_y;
        }
        if (motionAngVerPara.gpro_value * motionAngVerPara.gpro_value >= 0.0f || Math.abs(motionAngVerPara.gpro_value) <= this.WIN_RESAMPLE || Math.abs(motionAngVerPara.gpro_value) <= this.WIN_RESAMPLE || Math.abs(max_t_z - min_t_z) <= 15) {
            Log.d("GPRO_MAX_DATA", "gpro_abs_max_z 2 " + gpro_abs_max_z.gpro_value);
            gpro_abs_max_z = Math.abs(motionAngVerPara.gpro_value) > Math.abs(motionAngVerPara.gpro_value) ? motionAngVerPara : motionAngVerPara;
            triger_t_z = Math.abs(motionAngVerPara.gpro_value) > Math.abs(motionAngVerPara.gpro_value) ? max_t_z : min_t_z;
        } else {
            gpro_abs_max_z = max_t_z < min_t_z ? motionAngVerPara : motionAngVerPara;
            triger_t_z = max_t_z < min_t_z ? max_t_z : min_t_z;
            Log.d("GPRO_MAX_DATA", "gpro_abs_max_z 1 " + gpro_abs_max_z.gpro_value);
        }
        Log.d("MotionDet", "Perhapse Total crest:" + crest_x_cnt + "," + crest_y_cnt + "," + crest_z_cnt);
        Log.d("MotionDet", "Perhapse triger idx:" + triger_t_x + "," + triger_t_y + "," + triger_t_z);
        Log.d("MotionDet", "Perhapse triger value:" + gpro_abs_max_x.gpro_value + "," + gpro_abs_max_y.gpro_value + "," + gpro_abs_max_z.gpro_value);
        Log.d("MotionDet_1", "x:" + this.GproScopeListener.data_collect_buf[safe_det_idx][0] + "," + "y:" + this.GproScopeListener.data_collect_buf[safe_det_idx][1] + "z:" + this.GproScopeListener.data_collect_buf[safe_det_idx][2]);
        String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
        if (Math.abs(this.GproScopeListener.data_collect_buf[safe_det_idx][1]) > Math.abs(this.GproScopeListener.data_collect_buf[safe_det_idx][0]) + Math.abs(this.GproScopeListener.data_collect_buf[safe_det_idx][2])) {
            if (Math.abs(gpro_abs_max_y.gpro_value) > Math.abs(gpro_abs_max_x.gpro_value) + Math.abs(gpro_abs_max_z.gpro_value)) {
                result = false;
            }
            Log.d("MotionDet", "Angular velocity condition is not satisfied case 5");
        } else if (crest_x_cnt >= this.MAX_CREST_CNT_1222 || crest_y_cnt >= this.MAX_CREST_CNT_1222 || crest_z_cnt >= this.MAX_CREST_CNT_1222) {
            result = false;
            Log.d("MotionDet", "Angular velocity condition is not satisfied case 2");
        } else {
            Log.d("MotionDet", "Angular velocity condition is satisfied");
        }
        Log.d("MotionDet", "max_z/max_y:" + Math.abs(gpro_abs_max_z.gpro_value / gpro_abs_max_y.gpro_value) + "max_z/max_x:" + Math.abs(gpro_abs_max_z.gpro_value / gpro_abs_max_x.gpro_value));
        Log.d("MotionDet_2", "max_x:" + Math.abs(gpro_abs_max_x.gpro_value) + "max_y:" + Math.abs(gpro_abs_max_y.gpro_value) + "max_z:" + Math.abs(gpro_abs_max_z.gpro_value));
        if (Math.abs(gpro_abs_max_z.gpro_value / gpro_abs_max_y.gpro_value) < 0.4f || Math.abs(gpro_abs_max_z.gpro_value / gpro_abs_max_x.gpro_value) < 0.3f) {
            Log.d("MotionDet", "PD1222 Angular velocity condition is not satisfied case 4");
        }
        if (!result) {
            gpro_max_x.gpro_value = 0.0f;
            motionAngVerPara.gpro_value = 0.0f;
            motionAngVerPara.gpro_value = 0.0f;
            motionAngVerPara.gpro_value = 0.0f;
            motionAngVerPara.gpro_value = 0.0f;
            motionAngVerPara.gpro_value = 0.0f;
        }
        if (AllConfig.mIsAKMVirtGryo) {
            if (result && (Math.abs(gpro_abs_max_x.gpro_value) < this.MOTION_MIN_1222 || Math.abs(gpro_abs_max_x.gpro_value) > this.MOTION_MAX_AKM)) {
                Log.d("MotionDet", "MotionDet MAX:X ERROR");
            }
            if (result && (Math.abs(gpro_abs_max_y.gpro_value) < this.MOTION_MIN_Y_1222 || Math.abs(gpro_abs_max_y.gpro_value) > this.MOTION_MAX_AKM)) {
                Log.d("MotionDet", "MotionDet MAX:Y ERROR");
            }
            if (result && (((double) Math.abs(gpro_abs_max_z.gpro_value)) < 1.2d || Math.abs(gpro_abs_max_z.gpro_value) > this.MOTION_MAX_AKM)) {
                Log.d("MotionDet", "MotionDet MAX:Z ERROR");
                result = false;
            }
        } else if (AllConfig.mIsALPSVirtGryo || AllConfig.mIsYASVirtGryo) {
            if (result && (Math.abs(gpro_abs_max_x.gpro_value) < this.MOTION_MIN || Math.abs(gpro_abs_max_x.gpro_value) > this.MOTION_MAX)) {
                Log.d("MotionDet", "MotionDet MAX:X ERROR");
                result = false;
            }
            if (result && (Math.abs(gpro_abs_max_y.gpro_value) < this.MOTION_MIN_Y_1222 || Math.abs(gpro_abs_max_y.gpro_value) > this.MOTION_MAX)) {
                Log.d("MotionDet", "MotionDet MAX:Y ERROR");
                result = false;
            }
            if (result && (Math.abs(gpro_abs_max_z.gpro_value) < this.MOTION_MIN_1222 || ((double) Math.abs(gpro_abs_max_z.gpro_value)) > 30.0d)) {
                Log.d("MotionDet", "MotionDet MAX:Z ERROR");
                result = false;
            }
        } else if (AllConfig.mIsADSPAKMVirtGryo) {
            if (result && (Math.abs(gpro_abs_max_x.gpro_value) < this.MOTION_MIN_Y_1222 || Math.abs(gpro_abs_max_x.gpro_value) > this.MOTION_MAX)) {
                Log.d("MotionDet", "MotionDet MAX:X ERROR");
                result = false;
            }
            if (result && (Math.abs(gpro_abs_max_y.gpro_value) < this.MOTION_MIN_Y_1222 || Math.abs(gpro_abs_max_y.gpro_value) > this.MOTION_MAX)) {
                Log.d("MotionDet", "MotionDet MAX:Y ERROR");
            }
            if (result && (Math.abs(gpro_abs_max_z.gpro_value) < this.MOTION_MIN_Y_1222 || Math.abs(gpro_abs_max_z.gpro_value) > this.MOTION_MAX)) {
                Log.d("MotionDet", "MotionDet MAX:Z ERROR");
                result = false;
            }
        } else {
            if (result && (Math.abs(gpro_abs_max_x.gpro_value) < this.MOTION_MIN || Math.abs(gpro_abs_max_x.gpro_value) > this.MOTION_MAX)) {
                Log.d("MotionDet", "MotionDet MAX:X ERROR");
                result = false;
            }
            if (result && (Math.abs(gpro_abs_max_z.gpro_value) < this.MOTION_MIN || Math.abs(gpro_abs_max_z.gpro_value) > this.MOTION_MAX)) {
                Log.d("MotionDet", "MotionDet MAX:Z ERROR");
                result = false;
            }
        }
        Log.d("MotionDet+++++", "result:" + result);
        return result;
    }

    boolean pickupDegreeAnalist() {
        long timeDelta;
        float[] degreeSum = new float[3];
        long timeSum = 0;
        if (AllConfig.mIsAKMVirtGryo || AllConfig.mIsALPSVirtGryo || AllConfig.mIsYASVirtGryo || AllConfig.mIsADSPAKMVirtGryo) {
            timeDelta = 20;
        } else {
            timeDelta = 40;
        }
        Log.d(TAG, "GYRO_SUM_PICK , pick_up_cnt_motion : " + this.pick_up_cnt_motion + "," + this.pick_up_cnt_threshhold);
        int n;
        if (this.pick_up_cnt_motion == this.pick_up_cnt_threshhold) {
            int integral_count;
            if (AllConfig.mIsAKMVirtGryo || AllConfig.mIsALPSVirtGryo || AllConfig.mIsYASVirtGryo || AllConfig.mIsADSPAKMVirtGryo) {
                integral_count = this.pick_up_cnt_motion + 10;
            } else {
                integral_count = this.pick_up_cnt_motion + 5;
            }
            for (n = 0; n < 3; n++) {
                this.degreeSumCall[n] = 0.0f;
            }
            for (int m = 0; m < integral_count; m++) {
                if (m > 0) {
                    timeDelta = this.time_buf[m - 1] - this.time_buf[m];
                }
                for (n = 0; n < 3; n++) {
                    float[] fArr = this.degreeSumCall;
                    float f = degreeSum[n] + (this.gyro_data_buf[m][n] * ((float) timeDelta));
                    degreeSum[n] = f;
                    fArr[n] = f;
                }
                Log.d(TAG, "GYRO_SUM_PICK , raw_data : " + this.gyro_data_buf[m][0] + "," + this.gyro_data_buf[m][1] + "," + this.gyro_data_buf[m][2] + "," + timeDelta + "," + m);
                timeSum += timeDelta;
                if (timeSum > 1500) {
                    break;
                }
            }
        } else {
            for (n = 0; n < 3; n++) {
                float f2 = this.degreeSumCall[n] + (this.gyro_data_buf[0][n] * ((float) timeDelta));
                this.degreeSumCall[n] = f2;
                degreeSum[n] = f2;
            }
            Log.d(TAG, "GYRO_SUM_PICK , raw_data : " + this.gyro_data_buf[0][0] + "," + this.gyro_data_buf[0][1] + "," + this.gyro_data_buf[0][2] + "," + timeDelta + "," + 0);
        }
        degreeSum[0] = (degreeSum[0] * 180.0f) / 3140.0f;
        degreeSum[1] = (degreeSum[1] * 180.0f) / 3140.0f;
        degreeSum[2] = (degreeSum[2] * 180.0f) / 3140.0f;
        Log.d(TAG, "pickup_degreeSum[0]:" + degreeSum[0] + "degreeSum[1]:" + degreeSum[1] + "degreeSum[2]:" + degreeSum[2]);
        if ((Math.abs(degreeSum[0]) <= 20.0f || Math.abs(degreeSum[1]) <= 16.0f) && ((Math.abs(degreeSum[0]) <= 20.0f || Math.abs(degreeSum[2]) <= 17.0f) && (Math.abs(degreeSum[1]) <= 16.0f || Math.abs(degreeSum[2]) <= 20.0f))) {
            return false;
        }
        return true;
    }

    boolean degreeAnalist() {
        int m_min;
        int m_max;
        boolean degreeFlag = false;
        float[] degreeSum = new float[3];
        float[] degreeSump = new float[3];
        float[] degreeSumn = new float[3];
        float[] dataStore = new float[3];
        float[] degreeFast = new float[3];
        float[] degreeFastp = new float[3];
        float[] degreeFastn = new float[3];
        long timeSum = 0;
        xyz = new int[3];
        boolean max_fast = false;
        xyz[2] = -1;
        xyz[1] = -1;
        xyz[0] = -1;
        dataStore[2] = 0.0f;
        dataStore[1] = 0.0f;
        dataStore[0] = 0.0f;
        degreeFast[2] = 0.0f;
        degreeFast[1] = 0.0f;
        degreeFast[0] = 0.0f;
        degreeFastp[2] = 0.0f;
        degreeFastp[1] = 0.0f;
        degreeFastp[0] = 0.0f;
        degreeFastn[2] = 0.0f;
        degreeFastn[1] = 0.0f;
        degreeFastn[0] = 0.0f;
        if (AllConfig.mIsAKMVirtGryo || AllConfig.mIsADSPAKMVirtGryo) {
            m_min = 0;
            m_max = 60;
        } else {
            m_min = 4;
            m_max = 64;
        }
        synchronized (mDataLock) {
            int n;
            int m = m_min;
            while (m < m_max) {
                long timeDelta;
                boolean z;
                if (m_min != m) {
                    timeDelta = this.time_buf[m - 1] - this.time_buf[m];
                } else if (AllConfig.mIsAKMVirtGryo || AllConfig.mIsALPSVirtGryo || AllConfig.mIsYASVirtGryo || AllConfig.mIsADSPAKMVirtGryo) {
                    timeDelta = 20;
                } else {
                    timeDelta = 40;
                }
                n = 0;
                while (n < 3) {
                    z = (AllConfig.mIsAKMVirtGryo || AllConfig.mIsALPSVirtGryo || AllConfig.mIsYASVirtGryo) ? true : AllConfig.mIsADSPAKMVirtGryo;
                    if (!z && 800 < timeDelta) {
                        break;
                    }
                    degreeSum[n] = degreeSum[n] + (this.gyro_data_buf[m][n] * ((float) timeDelta));
                    if (AllConfig.mIsAKMVirtGryo || AllConfig.mIsALPSVirtGryo || AllConfig.mIsPhyGryo || AllConfig.mIsYASVirtGryo || AllConfig.mIsADSPAKMVirtGryo) {
                        if (0.0f < this.gyro_data_buf[m][n]) {
                            degreeSump[n] = degreeSump[n] + (this.gyro_data_buf[m][n] * ((float) timeDelta));
                        } else {
                            degreeSumn[n] = degreeSumn[n] + (this.gyro_data_buf[m][n] * ((float) timeDelta));
                        }
                        if (30 > m && 8.0f < Math.abs(this.gyro_data_buf[m][n])) {
                            max_fast = true;
                        }
                    }
                    n++;
                }
                if (AllConfig.mIsAKMVirtGryo && max_fast && 30 == m) {
                    degreeFastp[0] = (degreeSump[0] * 180.0f) / 3140.0f;
                    degreeFastp[1] = (degreeSump[1] * 180.0f) / 3140.0f;
                    degreeFastp[2] = (degreeSump[2] * 180.0f) / 3140.0f;
                    degreeFastn[0] = (degreeSumn[0] * 180.0f) / 3140.0f;
                    degreeFastn[1] = (degreeSumn[1] * 180.0f) / 3140.0f;
                    degreeFastn[2] = (degreeSumn[2] * 180.0f) / 3140.0f;
                    degreeFast[0] = Math.abs(degreeFastp[0]) + Math.abs(degreeFastn[0]);
                    degreeFast[1] = Math.abs(degreeFastp[1]) + Math.abs(degreeFastn[1]);
                    degreeFast[2] = Math.abs(degreeFastp[2]) + Math.abs(degreeFastn[2]);
                }
                if (AllConfig.mIsAKMVirtGryo || AllConfig.mIsALPSVirtGryo || AllConfig.mIsYASVirtGryo || AllConfig.mIsADSPAKMVirtGryo) {
                    if (39 < timeDelta) {
                        Log.d("GYRO_SUM ", "timeDelta error : " + timeDelta);
                    }
                } else if (110 < timeDelta) {
                    Log.d("GYRO_SUM ", "timeDelta error : " + timeDelta);
                }
                Log.d(TAG, "GYRO_SUM , raw_data : " + this.gyro_data_buf[m][0] + "," + this.gyro_data_buf[m][1] + "," + this.gyro_data_buf[m][2] + "," + timeDelta + "," + m);
                z = (AllConfig.mIsAKMVirtGryo || AllConfig.mIsALPSVirtGryo || AllConfig.mIsYASVirtGryo) ? true : AllConfig.mIsADSPAKMVirtGryo;
                if (!z) {
                    timeSum += timeDelta;
                    if (timeSum > 2000) {
                        break;
                    } else if (m > 15) {
                        int axis = 0;
                        float degreeSumM = Math.abs(degreeSum[0]);
                        if (Math.abs(degreeSum[1]) > Math.abs(degreeSum[0])) {
                            if (Math.abs(degreeSum[2]) > Math.abs(degreeSum[1])) {
                                axis = 1;
                                degreeSumM = Math.abs(degreeSum[1]);
                            } else if (Math.abs(degreeSum[2]) > Math.abs(degreeSum[0])) {
                                axis = 2;
                                degreeSumM = Math.abs(degreeSum[2]);
                            }
                        } else if (Math.abs(degreeSum[1]) > Math.abs(degreeSum[2])) {
                            axis = 1;
                            degreeSumM = Math.abs(degreeSum[1]);
                        } else if (Math.abs(degreeSum[0]) > Math.abs(degreeSum[2])) {
                            axis = 2;
                            degreeSumM = Math.abs(degreeSum[2]);
                        }
                        degreeSumM = (180.0f * degreeSumM) / 3140.0f;
                        if (degreeSumM > 45.0f && this.gyro_data_buf[m][axis] * this.gyro_data_buf[m - 1][axis] < 0.0f) {
                            Log.d(TAG, "Data has already,degreeSumM:" + degreeSumM);
                            break;
                        }
                    } else {
                        continue;
                    }
                }
                m++;
            }
            if (AllConfig.mIsAKMVirtGryo || AllConfig.mIsPhyGryo || AllConfig.mIsADSPAKMVirtGryo) {
                m = m_min;
                while (m < m_max) {
                    if (-1 == xyz[0] && 1.0f <= this.gyro_data_buf[m][0]) {
                        xyz[0] = 2;
                    } else if (-1 == xyz[0] && -1.0f >= this.gyro_data_buf[m][0]) {
                        xyz[0] = -2;
                    }
                    if (1 == this.pick_hand) {
                        if (-1 == xyz[2] && 1.0f <= this.gyro_data_buf[m][2]) {
                            xyz[2] = 2;
                        } else if (-1 == xyz[2] && -1.0f >= this.gyro_data_buf[m][2]) {
                            xyz[2] = -2;
                        }
                        if (-1 == xyz[1] && 1.0f <= this.gyro_data_buf[m][1]) {
                            xyz[1] = 2;
                        } else if (-1 == xyz[1] && -1.0f >= this.gyro_data_buf[m][1]) {
                            xyz[1] = -2;
                        }
                    } else {
                        if (2 == this.pick_hand) {
                            if (-1 == xyz[2] && 1.0f <= this.gyro_data_buf[m][2]) {
                                xyz[2] = -2;
                            } else if (-1 == xyz[2] && -1.0f >= this.gyro_data_buf[m][2]) {
                                xyz[2] = 2;
                            }
                            if (-1 == xyz[1] && 1.0f <= this.gyro_data_buf[m][1]) {
                                xyz[1] = -2;
                            } else if (-1 == xyz[1] && -1.0f >= this.gyro_data_buf[m][1]) {
                                xyz[1] = 2;
                            }
                        }
                    }
                    if (-1 != xyz[2] && -1 != xyz[1] && -1 != xyz[0]) {
                        break;
                    }
                    m++;
                }
            }
            if (AllConfig.mIsAKMVirtGryo || AllConfig.mIsALPSVirtGryo || AllConfig.mIsYASVirtGryo || AllConfig.mIsADSPAKMVirtGryo) {
                Log.d("VGYRO_SUM", "raw_data : " + this.gyro_data_buf[0][0] + "," + this.gyro_data_buf[0][1] + "," + this.gyro_data_buf[0][2] + ",0");
            }
            if (AllConfig.mIsALPSVirtGryo || AllConfig.mIsYASVirtGryo || AllConfig.mIsADSPAKMVirtGryo || AllConfig.mIsAKMVirtGryo) {
                int[] DataCount = new int[]{0, 0, 0};
                for (m = 0; m < 10; m++) {
                    if (0.0f < this.gyro_data_buf[m][0]) {
                        DataCount[0] = DataCount[0] + 1;
                    } else {
                        DataCount[0] = DataCount[0] - 1;
                    }
                    if (0.0f < this.gyro_data_buf[m][1]) {
                        DataCount[1] = DataCount[1] + 1;
                    } else {
                        DataCount[1] = DataCount[1] - 1;
                    }
                    if (0.0f < this.gyro_data_buf[m][2]) {
                        DataCount[2] = DataCount[2] + 1;
                    } else {
                        DataCount[2] = DataCount[2] - 1;
                    }
                }
                dataStore[2] = 0.0f;
                dataStore[1] = 0.0f;
                dataStore[0] = 0.0f;
                n = (int) (this.gyro_data_buf[0][0] * 10.0f);
                if (10 == Math.abs(DataCount[0])) {
                    if (n > 0) {
                        dataStore[0] = (float) (((n + 1) * n) / 20);
                    } else {
                        dataStore[0] = (float) (((-((-n) + 1)) * (-n)) / 20);
                    }
                }
                n = (int) (this.gyro_data_buf[0][1] * 10.0f);
                if (10 == Math.abs(DataCount[1])) {
                    if (n > 0) {
                        dataStore[1] = (float) (((n + 1) * n) / 20);
                    } else {
                        dataStore[1] = (float) (((-((-n) + 1)) * (-n)) / 20);
                    }
                }
                n = (int) (this.gyro_data_buf[0][2] * 10.0f);
                if (10 == Math.abs(DataCount[2])) {
                    if (n > 0) {
                        dataStore[2] = (float) (((n + 1) * n) / 20);
                    } else {
                        dataStore[2] = (float) (((-((-n) + 1)) * (-n)) / 20);
                    }
                }
                Log.d(TAG, "dataStore[0]:" + dataStore[0] + "dataStore[1]:" + dataStore[1] + "dataStore[2]:" + dataStore[2]);
                dataStore[0] = (float) (((double) dataStore[0]) * 1.146d);
                dataStore[1] = (float) (((double) dataStore[1]) * 1.146d);
                dataStore[2] = (float) (((double) dataStore[2]) * 1.146d);
                Log.d(TAG, "dataStore[0]:" + dataStore[0] + "dataStore[1]:" + dataStore[1] + "dataStore[2]:" + dataStore[2]);
            }
        }
        degreeSum[0] = (degreeSum[0] * 180.0f) / 3140.0f;
        degreeSum[1] = (degreeSum[1] * 180.0f) / 3140.0f;
        degreeSum[2] = (degreeSum[2] * 180.0f) / 3140.0f;
        Log.d(TAG, "degreeSum[0]:" + degreeSum[0] + "degreeSum[1]:" + degreeSum[1] + "degreeSum[2]:" + degreeSum[2]);
        if (AllConfig.mIsAKMVirtGryo || AllConfig.mIsALPSVirtGryo || AllConfig.mIsYASVirtGryo || AllConfig.mIsADSPAKMVirtGryo) {
            degreeSump[0] = (degreeSump[0] * 180.0f) / 3140.0f;
            degreeSump[1] = (degreeSump[1] * 180.0f) / 3140.0f;
            degreeSump[2] = (degreeSump[2] * 180.0f) / 3140.0f;
            degreeSumn[0] = (degreeSumn[0] * 180.0f) / 3140.0f;
            degreeSumn[1] = (degreeSumn[1] * 180.0f) / 3140.0f;
            degreeSumn[2] = (degreeSumn[2] * 180.0f) / 3140.0f;
            Log.d(TAG, "degreeSump[0]: " + degreeSump[0] + "degreeSump[1]: " + degreeSump[1] + "degreeSump[2]:" + degreeSump[2]);
            Log.d(TAG, "degreeSumn[0]: " + degreeSumn[0] + "degreeSumn[1]: " + degreeSumn[1] + "degreeSumn[2]:" + degreeSumn[2]);
        }
        if (AllConfig.mIsAKMVirtGryo || AllConfig.mIsALPSVirtGryo || AllConfig.mIsYASVirtGryo || AllConfig.mIsADSPAKMVirtGryo) {
            if ((Math.abs(degreeSum[0]) > 40.0f && Math.abs(degreeSum[1]) > 30.0f) || ((Math.abs(degreeSum[0]) > 40.0f && Math.abs(degreeSum[2]) > 40.0f) || ((Math.abs(degreeSum[1]) > 30.0f && Math.abs(degreeSum[2]) > 40.0f) || ((Math.abs(degreeSum[2]) > 56.0f && Math.abs(degreeSum[0]) + Math.abs(degreeSum[1]) > 30.0f) || ((Math.abs(degreeSum[0]) > 56.0f && Math.abs(degreeSum[1]) + Math.abs(degreeSum[2]) > 26.0f) || ((Math.abs(degreeSum[0] + dataStore[0]) > 40.0f && Math.abs(degreeSum[1] + dataStore[1]) > 30.0f) || ((Math.abs(degreeSum[0] + dataStore[0]) > 40.0f && Math.abs(degreeSum[2] + dataStore[2]) > 40.0f) || (Math.abs(degreeSum[1] + dataStore[1]) > 30.0f && Math.abs(degreeSum[2] + dataStore[2]) > 40.0f)))))))) {
                degreeFlag = true;
            }
        } else if ((Math.abs(degreeSum[0]) > 45.0f && Math.abs(degreeSum[1]) > 30.0f) || ((Math.abs(degreeSum[0]) > 40.0f && Math.abs(degreeSum[2]) > 45.0f) || (Math.abs(degreeSum[1]) > 30.0f && Math.abs(degreeSum[2]) > 45.0f))) {
            if (((double) Math.abs(this.prox_close_acc_x)) <= 8.0d || Math.abs(degreeSum[0]) + Math.abs(degreeSum[1]) >= 90.0f) {
                degreeFlag = true;
            } else {
                Log.d(TAG, "degree sum is too small for posture: " + this.prox_close_acc_x);
            }
        }
        if (AllConfig.mIsAKMVirtGryo || AllConfig.mIsADSPAKMVirtGryo) {
            if (2 == xyz[2] && 2 == xyz[0]) {
                if (1 == this.pick_hand) {
                    if (2.8f < this.gyro_data_buf[0][0] && 2.8f < this.gyro_data_buf[0][2] && ((Math.abs(degreeSump[0] * 2.0f) > 40.0f && Math.abs(degreeSump[1] * 2.0f) > 30.0f) || ((Math.abs(degreeSump[0] * 2.0f) > 40.0f && Math.abs(degreeSump[2] * 2.0f) > 40.0f) || (Math.abs(degreeSump[1] * 2.0f) > 30.0f && Math.abs(degreeSump[2] * 2.0f) > 40.0f)))) {
                        degreeFlag = true;
                    }
                    if (100.0f < Math.abs(degreeSump[0]) + Math.abs(degreeSump[2]) && 35.0f < Math.abs(degreeSump[2]) && 35.0f < Math.abs(degreeSump[0])) {
                        degreeFlag = true;
                    }
                }
                if (2 == this.pick_hand) {
                    if (2.8f < this.gyro_data_buf[0][0] && -2.8f > this.gyro_data_buf[0][2] && ((Math.abs(degreeSump[0] * 2.0f) > 40.0f && Math.abs(degreeSumn[1] * 2.0f) > 30.0f) || ((Math.abs(degreeSump[0] * 2.0f) > 40.0f && Math.abs(degreeSumn[2] * 2.0f) > 40.0f) || (Math.abs(degreeSumn[1] * 2.0f) > 30.0f && Math.abs(degreeSumn[2] * 2.0f) > 40.0f)))) {
                        degreeFlag = true;
                    }
                    if (100.0f < Math.abs(degreeSump[0]) + Math.abs(degreeSumn[2]) && (35.0f < Math.abs(degreeSumn[2]) || 35.0f < Math.abs(degreeSump[0]))) {
                        degreeFlag = true;
                    }
                }
            }
            Log.d(TAG, "xyz[2]: " + xyz[2] + "xyz[0]: " + xyz[0] + "pick_hand:" + this.pick_hand);
            if (max_fast) {
                if ((Math.abs(degreeFast[0]) > 40.0f && Math.abs(degreeFast[1]) > 30.0f) || ((Math.abs(degreeFast[0]) > 40.0f && Math.abs(degreeFast[2]) > 40.0f) || ((Math.abs(degreeFast[1]) > 30.0f && Math.abs(degreeFast[2]) > 40.0f) || ((Math.abs(degreeFast[0]) > 80.0f && Math.abs(degreeFast[1]) > 80.0f && Math.abs(degreeFast[2]) > 30.0f) || ((Math.abs(degreeFast[0]) > 30.0f && Math.abs(degreeFast[1]) > 80.0f && Math.abs(degreeFast[2]) > 80.0f) || (Math.abs(degreeFast[0]) > 80.0f && Math.abs(degreeFast[1]) > 30.0f && Math.abs(degreeFast[2]) > 80.0f)))))) {
                    degreeFlag = true;
                }
                Log.d(TAG, "degreeFast[0]: " + degreeFast[0] + "degreeFast[1]: " + degreeFast[1] + "degreeFast[2]:" + degreeFast[2]);
                Log.d(TAG, "degreeFastp[0]: " + degreeFastp[0] + "degreeFastp[1]: " + degreeFastp[1] + "degreeFastp[2]:" + degreeFastp[2]);
                Log.d(TAG, "degreeFastn[0]: " + degreeFastn[0] + "degreeFastn[1]: " + degreeFastn[1] + "degreeFastn[2]:" + degreeFastn[2]);
            }
            if (1 == this.pick_hand && ((Math.abs(degreeSump[0]) > 40.0f && Math.abs(degreeSump[1]) > 30.0f) || ((Math.abs(degreeSump[0]) > 40.0f && Math.abs(degreeSump[2]) > 32.0f) || (Math.abs(degreeSump[1]) > 30.0f && Math.abs(degreeSump[2]) > 40.0f)))) {
                degreeFlag = true;
            }
            if (2 == this.pick_hand && ((Math.abs(degreeSump[0]) > 40.0f && Math.abs(degreeSumn[1]) > 30.0f) || ((Math.abs(degreeSump[0]) > 40.0f && Math.abs(degreeSumn[2]) > 40.0f) || (Math.abs(degreeSumn[1]) > 30.0f && Math.abs(degreeSumn[2]) > 40.0f)))) {
                degreeFlag = true;
            }
        } else if (AllConfig.mIsPhyGryo) {
            if (2 == xyz[2] && 2 == xyz[1] && 2 == xyz[0] && 45.0f <= degreeSump[0] && ((1 == this.pick_hand && 45.0f <= degreeSump[1] && 45.0f <= degreeSump[2]) || (2 == this.pick_hand && -45.0f >= degreeSumn[1] && -45.0f >= degreeSumn[2]))) {
                degreeFlag = true;
            }
            Log.d(TAG, "xyz[2]: " + xyz[2] + "xyz[1]: " + xyz[1] + "xyz[0]: " + xyz[0] + "pick_hand:" + this.pick_hand);
        }
        if (degreeFlag) {
            if (AllConfig.mIsAKMVirtGryo) {
                if (160.0f < Math.abs(degreeSum[1]) || 250.0f < Math.abs(degreeSum[0]) || 250.0f < Math.abs(degreeSum[2])) {
                    degreeFlag = false;
                }
            } else if (AllConfig.mIsALPSVirtGryo || AllConfig.mIsYASVirtGryo || AllConfig.mIsADSPAKMVirtGryo) {
                if (150.0f < Math.abs(degreeSum[1]) || 250.0f < Math.abs(degreeSum[0]) || 250.0f < Math.abs(degreeSum[2])) {
                    degreeFlag = false;
                }
            } else if (180.0f < Math.abs(degreeSum[1]) || 270.0f < Math.abs(degreeSum[0]) || 270.0f < Math.abs(degreeSum[2])) {
                degreeFlag = false;
            }
        }
        this.pick_hand = 0;
        return degreeFlag;
    }

    boolean degreeAnalist_2() {
        boolean degreeFlag = false;
        float[] degreeSum = new float[3];
        float[] degreeSump = new float[3];
        degreeSumn = new float[3];
        int dataStore = 0;
        int[] xyz = new int[]{-1, -1, -1};
        Log.d("GYRO_SUM ", "YASVirtGryo style");
        synchronized (mDataLock) {
            int m;
            int n;
            for (m = 0; m < 60; m++) {
                long timeDelta;
                if (m == 0) {
                    timeDelta = 20;
                } else {
                    timeDelta = this.time_buf[m - 1] - this.time_buf[m];
                }
                for (n = 0; n < 3 && 50 >= timeDelta; n++) {
                    degreeSum[n] = degreeSum[n] + (this.gyro_data_buf[m][n] * ((float) timeDelta));
                    if (0.0f < this.gyro_data_buf[m][n]) {
                        degreeSump[n] = degreeSump[n] + (this.gyro_data_buf[m][n] * ((float) timeDelta));
                    } else {
                        degreeSumn[n] = degreeSumn[n] + (this.gyro_data_buf[m][n] * ((float) timeDelta));
                    }
                }
                if (39 < timeDelta) {
                    Log.d("GYRO_SUM ", "timeDelta error : " + timeDelta);
                }
                Log.d(TAG, "GYRO_SUM , raw_data : " + this.gyro_data_buf[m][0] + "," + this.gyro_data_buf[m][1] + "," + this.gyro_data_buf[m][2] + "," + timeDelta + "," + m);
            }
            m = 0;
            while (m < 60) {
                if (-1 == xyz[0] && 1.0f <= this.gyro_data_buf[m][0]) {
                    xyz[0] = 2;
                } else if (-1 == xyz[0] && -1.0f >= this.gyro_data_buf[m][0]) {
                    xyz[0] = -2;
                }
                if (1 == this.pick_hand) {
                    if (-1 == xyz[2] && 1.0f <= this.gyro_data_buf[m][2]) {
                        xyz[2] = 2;
                    } else if (-1 == xyz[2] && -1.0f >= this.gyro_data_buf[m][2]) {
                        xyz[2] = -2;
                    }
                    if (-1 == xyz[1] && 1.0f <= this.gyro_data_buf[m][1]) {
                        xyz[1] = 2;
                    } else if (-1 == xyz[1] && -1.0f >= this.gyro_data_buf[m][1]) {
                        xyz[1] = -2;
                    }
                } else if (2 == this.pick_hand) {
                    if (-1 == xyz[2] && 1.0f <= this.gyro_data_buf[m][2]) {
                        xyz[2] = -2;
                    } else if (-1 == xyz[2] && -1.0f >= this.gyro_data_buf[m][2]) {
                        xyz[2] = 2;
                    }
                    if (-1 == xyz[1] && 1.0f <= this.gyro_data_buf[m][1]) {
                        xyz[1] = -2;
                    } else if (-1 == xyz[1] && -1.0f >= this.gyro_data_buf[m][1]) {
                        xyz[1] = 2;
                    }
                }
                if (-1 != xyz[2] && -1 != xyz[1] && -1 != xyz[0]) {
                    break;
                }
                m++;
            }
            int DataCount = 0;
            for (m = 0; m < 6; m++) {
                if (0.0f < this.gyro_data_buf[m][0]) {
                    DataCount++;
                } else {
                    DataCount--;
                }
            }
            n = (int) this.gyro_data_buf[0][0];
            if (6 == Math.abs(DataCount)) {
                if (n > 0) {
                    dataStore = ((n + 1) * n) / 2;
                } else {
                    dataStore = ((-((-n) + 1)) * (-n)) / 2;
                }
            }
        }
        degreeSum[0] = (degreeSum[0] * 180.0f) / 3140.0f;
        degreeSum[1] = (degreeSum[1] * 180.0f) / 3140.0f;
        degreeSum[2] = (degreeSum[2] * 180.0f) / 3140.0f;
        Log.d(TAG, "degreeSum[0]:" + degreeSum[0] + "degreeSum[1]:" + degreeSum[1] + "degreeSum[2]:" + degreeSum[2]);
        degreeSump[0] = (degreeSump[0] * 180.0f) / 3140.0f;
        degreeSump[1] = (degreeSump[1] * 180.0f) / 3140.0f;
        degreeSump[2] = (degreeSump[2] * 180.0f) / 3140.0f;
        degreeSumn[0] = (degreeSumn[0] * 180.0f) / 3140.0f;
        degreeSumn[1] = (degreeSumn[1] * 180.0f) / 3140.0f;
        degreeSumn[2] = (degreeSumn[2] * 180.0f) / 3140.0f;
        Log.d(TAG, "degreeSump[0]: " + degreeSump[0] + "degreeSump[1]: " + degreeSump[1] + "degreeSump[2]:" + degreeSump[2]);
        Log.d(TAG, "degreeSumn[0]: " + degreeSumn[0] + "degreeSumn[1]: " + degreeSumn[1] + "degreeSumn[2]:" + degreeSumn[2]);
        if ((Math.abs(degreeSum[0]) > 40.0f && Math.abs(degreeSum[1]) > 30.0f) || ((Math.abs(degreeSum[0]) > 40.0f && Math.abs(degreeSum[2]) > 40.0f) || ((Math.abs(degreeSum[1]) > 30.0f && Math.abs(degreeSum[2]) > 40.0f) || ((Math.abs(degreeSum[2]) > 68.0f && Math.abs(degreeSum[0]) + Math.abs(degreeSum[1]) > 30.0f) || ((Math.abs(degreeSum[1]) > 68.0f && Math.abs(degreeSum[0]) + Math.abs(degreeSum[2]) > 28.0f) || (Math.abs(degreeSum[0]) > 68.0f && Math.abs(degreeSum[1]) + Math.abs(degreeSum[2]) > 32.0f)))))) {
            degreeFlag = true;
        }
        if (AllConfig.mIsYASVirtGryo) {
            if (1 == this.pick_hand) {
                if (2.8f < this.gyro_data_buf[0][0] && 2.8f < this.gyro_data_buf[0][2] && ((Math.abs(degreeSump[0] * 2.0f) > 35.0f && Math.abs(degreeSump[1] * 2.0f) > 30.0f) || ((Math.abs(degreeSump[0] * 2.0f) > 35.0f && Math.abs(degreeSump[2] * 2.0f) > 35.0f) || (Math.abs(degreeSump[1] * 2.0f) > 30.0f && Math.abs(degreeSump[2] * 2.0f) > 35.0f)))) {
                    degreeFlag = true;
                }
                if (100.0f < Math.abs(degreeSump[0]) + Math.abs(degreeSump[2]) && 35.0f < Math.abs(degreeSump[2]) && 35.0f < Math.abs(degreeSump[0])) {
                    degreeFlag = true;
                }
                if (100.0f < Math.abs(degreeSumn[2]) && 35.0f < Math.abs(degreeSump[1]) && 60.0f < Math.abs(degreeSump[0]) + Math.abs(degreeSump[1])) {
                    degreeFlag = true;
                }
            }
            if (2 == this.pick_hand) {
                if (2.8f < this.gyro_data_buf[0][0] && -2.8f > this.gyro_data_buf[0][2] && ((Math.abs(degreeSumn[0] * 2.0f) > 35.0f && Math.abs(degreeSumn[1] * 2.0f) > 30.0f) || ((Math.abs(degreeSumn[0] * 2.0f) > 35.0f && Math.abs(degreeSumn[2] * 2.0f) > 35.0f) || (Math.abs(degreeSumn[1] * 2.0f) > 30.0f && Math.abs(degreeSumn[2] * 2.0f) > 35.0f)))) {
                    degreeFlag = true;
                }
                if (100.0f < Math.abs(degreeSump[0]) + Math.abs(degreeSumn[2]) && (35.0f < Math.abs(degreeSumn[2]) || 35.0f < Math.abs(degreeSump[0]))) {
                    degreeFlag = true;
                }
                if (160.0f < Math.abs(degreeSum[2]) && 53.0f < Math.abs(degreeSump[1]) && (30.0f < Math.abs(degreeSump[0]) || 30.0f < Math.abs(degreeSumn[0]))) {
                    degreeFlag = true;
                }
            }
        }
        if (2 == xyz[2] && 2 == xyz[0] && 30.0f <= degreeSump[0] && ((1 == this.pick_hand && 35.0f <= degreeSump[2]) || (2 == this.pick_hand && -35.0f >= degreeSumn[2]))) {
            degreeFlag = true;
        }
        Log.d(TAG, "xyz[2]: " + xyz[2] + "xyz[0]: " + xyz[0] + "pick_hand:" + this.pick_hand);
        if (degreeFlag) {
            if (dataStore != 0) {
                degreeSum[0] = (float) (((double) degreeSum[0]) + (((double) ((float) dataStore)) * 1.146d));
                Log.d(TAG, "dataStore: " + dataStore);
            }
            if (180.0f < Math.abs(degreeSum[1]) || 180.0f < Math.abs(degreeSum[0]) || 240.0f < Math.abs(degreeSum[2])) {
                degreeFlag = false;
            }
        }
        this.pick_hand = 0;
        return degreeFlag;
    }

    private void readConfigFile() {
        File file = new File(mConfigFile);
        Log.d(TAG, "readConfigFile/etc/motion_recognition.conf");
        if (file.exists()) {
            Log.d(TAG, "readConfigFile OK");
            try {
                BufferedReader fin = new BufferedReader(new FileReader(file));
                while (true) {
                    String line = fin.readLine();
                    Log.d(TAG, "readline " + line);
                    if (line == null) {
                        break;
                    }
                    if (!(line.startsWith("#") || line == Events.DEFAULT_SORT_ORDER)) {
                        getValueOfLine(line);
                    }
                    if (line == null) {
                        break;
                    }
                }
                fin.close();
            } catch (Exception e) {
                Log.e(TAG, "fin >> " + e);
            }
        }
    }

    private void getValueOfLine(String line) {
        int index = line.indexOf("=");
        if (index >= 0) {
            String key = line.substring(0, index).trim();
            if (key != null && !key.equals(Events.DEFAULT_SORT_ORDER)) {
                String value = line.substring(index + 1).trim();
                if (value.startsWith("0x") || value.startsWith("0X")) {
                    value = value.substring(2);
                }
                Log.d(TAG, "key = " + key + "; value = " + value);
                if (KEY_MAX_CREST_CNT.equals(key)) {
                    this.MAX_CREST_CNT = Integer.valueOf(value).intValue();
                } else if (KEY_MAX_MOTION_IDX.equals(key)) {
                    this.MAX_MOTION_IDX = Integer.valueOf(value).intValue();
                } else if (KEY_MOTION_MIN.equals(key)) {
                    this.MOTION_MIN = Float.valueOf(value).floatValue();
                } else if (KEY_MOTION_MAX.equals(key)) {
                    this.MOTION_MAX = Float.valueOf(value).floatValue();
                } else if (KEY_WIN_RESAMPLE.equals(key)) {
                    this.WIN_RESAMPLE = Float.valueOf(value).floatValue();
                } else {
                    Log.d(TAG, "unhandled key " + key);
                }
            }
        }
    }

    boolean MotionDetectAnalist() {
        boolean DegreeResult;
        readConfigFile();
        boolean AngVerResult = subMotionAngVerAnalist();
        boolean AccDevResult = subMotionAccDevAnalist();
        if (this.prox_away_time - this.pick_up_time < 1000 && this.pick_up_count > 1) {
        }
        this.pick_up_count = 0;
        this.mDataOperate = true;
        if (AllConfig.mIsYASVirtGryo) {
            DegreeResult = degreeAnalist_2();
        } else {
            DegreeResult = degreeAnalist();
        }
        this.mDataOperate = false;
        Log.d(TAG, "AngVerResult: " + AngVerResult + "AccDevResult: " + AccDevResult + "DegreeResult: " + DegreeResult);
        if (AngVerResult && AccDevResult) {
            return DegreeResult;
        }
        return false;
    }

    private void MotionDetectPickupTriger() {
        Message msg = Message.obtain();
        msg.what = 16;
        msg.obj = new Integer(5);
        Log.d(TAG, "MotionDetectPickupTriger");
        synchronized (mHandleLock) {
            if (this.mCallBackHandler != null) {
                this.mCallBackHandler.sendMessage(msg);
            }
        }
    }

    void MotionDetectIdleStateProcess(int msg_what) {
        Message msg = Message.obtain();
        switch (msg_what) {
            case 1:
                this.MotionDetState = 1;
                synchronized (mHandleLock) {
                    if (this.mServiceHandler != null) {
                        msg.what = 2;
                        this.mServiceHandler.sendMessage(msg);
                    }
                }
                Log.d(TAG, "MotionDetectIdleStateProcess");
                return;
            case 4:
                MotionDetectPickupTriger();
                return;
            case 5:
                AllConfig.changeProximityParam(true, 2);
                return;
            case 6:
                AllConfig.changeProximityParam(false, 2);
                return;
            default:
                return;
        }
    }

    void MotionDetectGetDataStateProcess(int msg_what) {
        Message msg = Message.obtain();
        Object obj;
        switch (msg_what) {
            case 2:
                this.MotionDetState = 2;
                if (MotionDetectAnalist()) {
                    msg.what = 16;
                    msg.obj = new Integer(1);
                    obj = mHandleLock;
                    synchronized (obj) {
                        if (this.mCallBackHandler != null) {
                            try {
                                if (this.mVivoProxCaliManager != null) {
                                    this.mVivoProxCaliManager.onDirectCall(System.currentTimeMillis());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "MotionDetectGetDataStateProcess onDirectCall", e);
                            }
                            this.mCallBackHandler.sendMessage(msg);
                            break;
                        }
                    }
                }
                resetAllDataBuf();
                this.MotionDetState = 0;
                Log.d(TAG, "====MotionDetState = MOTION_DET_STATE_IDLE====");
                Log.d(TAG, "====MotionDetState = MOTION_DET_STATE_IDLE==== delay:1");
                return;
                break;
            case 3:
                this.MotionDetState = 3;
                obj = mHandleLock;
                synchronized (obj) {
                    if (this.mServiceHandler != null) {
                        this.mServiceHandler.sendEmptyMessageDelayed(0, 300);
                        break;
                    }
                }
                break;
            default:
                return;
        }
        return;
    }

    void MotionDetectAnalistStateProcess(int msg_what) {
        switch (msg_what) {
            case 3:
                this.MotionDetState = 3;
                synchronized (mHandleLock) {
                    if (this.mServiceHandler != null) {
                        this.mServiceHandler.sendEmptyMessageDelayed(0, 300);
                    }
                }
                return;
            default:
                return;
        }
    }

    void MotionDetectRecoverStateProcess(int msg_what) {
        switch (msg_what) {
            case 0:
                resetAllDataBuf();
                this.MotionDetState = 0;
                return;
            case 4:
                MotionDetectPickupTriger();
                return;
            default:
                return;
        }
    }

    public static DirectCallingService getInstance() {
        return singleDirectCallingService;
    }

    private DirectCallingService() {
        this.isDirectCallingServiceWorking = false;
        this.mCallBackHandler = null;
        this.mServiceHandler = null;
        this.mAudioManager = null;
        this.callVibrateSetting = 0;
        this.ringLevel = -1;
        this.mVivoProxCaliManager = null;
        this.MotionDetState = 0;
        this.mEnabled = false;
        this.mContext = null;
        this.MAX_CREST_CNT = 15;
        this.MAX_CREST_CNT_1222 = 25;
        this.MAX_MOTION_IDX = 5;
        this.MAX_MOTION_IDX_1222 = 3;
        this.MOTION_MIN = 1.2f;
        this.MOTION_MIN_1222 = 1.5f;
        this.MOTION_MIN_X_1222 = 2.0f;
        this.MOTION_MIN_Y_1222 = 0.5f;
        this.MOTION_MAX = 25.0f;
        this.MOTION_MAX_AKM = 30.0f;
        this.WIN_RESAMPLE = 3.0f;
        this.prox_close_acc_x = 0.0f;
        this.prox_close_acc_y = 0.0f;
        this.prox_close_acc_z = 0.0f;
        this.MOTION_AKM_MAX = 10.0f;
        this.VibratorState = 0;
        this.VibratorStateCount = 0;
        this.VibratorStaticCount = 0;
        this.ProximitState = -1;
        this.pick_up_cnt_threshhold = -1;
        this.pick_up_cnt_motion = -1;
        this.degreeSumCall = new float[3];
        this.mDataOperate = false;
        this.pick_up_count = 0;
        this.pick_up_time = 0;
        this.prox_away_time = 0;
        this.time_buf = new long[100];
        this.gyro_data_buf = (float[][]) Array.newInstance(Float.TYPE, new int[]{100, 3});
        this.gyroCount = 0;
        this.gyroHighFlag = false;
        this.pick_hand = 0;
        this.data_abandon = 0;
        this.first_flag = false;
        this.mag_vibrator_state = false;
        this.mag_vibrator_counter = 0;
        this.mag_ab = 0;
        this.MotionAngDevParaX = new MotionAngDevPara(this, null);
        this.MotionAngDevParaY = new MotionAngDevPara(this, null);
        this.MotionAngDevParaZ = new MotionAngDevPara(this, null);
        this.MotionAccDevParaX = new MotionAccPara(this, null);
        this.MotionAccDevParaY = new MotionAccPara(this, null);
        this.MotionAccDevParaZ = new MotionAccPara(this, null);
        this.acceleromererListener = new MotionSensorEventListener(this, null);
        this.MagneticListener = new MotionSensorEventListener(this, null);
        this.GproScopeListener = new MotionSensorEventListener(this, null);
        this.RotationVectorListener = new MotionSensorEventListener(this, null);
        this.ProximityListener = new MotionSensorEventListener(this, null);
        this.mVivoProxCaliManager = VivoFrameworkFactory.getFrameworkFactoryImpl().getVivoProxCaliManager();
    }

    public boolean startMotionRecognitionService(Context context, Handler handler) {
        Log.d(TAG, "startMotionRecognitionService.");
        if (!this.isDirectCallingServiceWorking) {
            this.isDirectCallingServiceWorking = true;
            resetAllDataBuf();
            clearGyrdata();
            this.MotionDetState = 0;
            this.mCallBackHandler = handler;
            this.mContext = context;
            this.mServiceHandler = new DirectCallingServiceHandler(handler.getLooper());
            this.MotionDetSensorManager = (SensorManager) context.getSystemService("sensor");
            String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            this.mProximityDataThread = new HandlerThread("DirectCallingPro");
            this.mProximityDataThread.start();
            this.mProximityDataHandler = new Handler(this.mProximityDataThread.getLooper());
            this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
            this.callVibrateSetting = this.mAudioManager.getVibrateSetting(0);
            Sensor GyroSensor = this.MotionDetSensorManager.getDefaultSensor(4);
            String GyroName = Events.DEFAULT_SORT_ORDER;
            if (GyroSensor == null) {
                Log.d("MotionDet", "GyroSensor is null!");
            } else {
                GyroName = GyroSensor.getName();
            }
            Log.d("MotionDet", "name " + GyroName);
            Log.d("MotionDet", "tag" + AllConfig.mIsPhyGryo);
            if (GyroName.equals("akm8963-pseudo-gyro") || GyroName.equals("AK8963 Gyroscope") || GyroName.equals("akm09911-pseudo-gyro") || GyroName.equals("AK09911-pseudo-gyro") || GyroName.equals("akm09912-pseudo-gyro") || GyroName.equals("MMC3630KJ-pseudo-gyro") || GyroName.equals("AK09918-pseudo-gyro") || GyroName.equals("pseudo_gyroscope")) {
                if (AllConfig.mIsArchADSP) {
                    AllConfig.mIsAKMVirtGryo = true;
                } else {
                    AllConfig.mIsAKMVirtGryo = true;
                }
                Log.d("MotionDet", "akm pseudo-gyro");
            } else if (GyroName.equals("hscdtd007a-pseudo-gyro")) {
                AllConfig.mIsALPSVirtGryo = true;
                Log.d("MotionDet", "hscdtd007a-pseudo-gyro");
            } else if (GyroName.equals("lsm330_gyr")) {
                AllConfig.mIsPhyGryo = true;
                Log.d("MotionDet", "lsm330_gyr");
            } else if (GyroName.equals("LSM6DS0 Gyroscope") || GyroName.equals("LSM6DS3 Gyroscope") || GyroName.contains("BMI160")) {
                AllConfig.mIsPhyGryo = true;
                Log.d("MotionDet", "physical Gyroscope");
            } else if (GyroName.equals("YAS533 Gyroscope") || GyroName.equals("yas533-pseudo-gyro") || GyroName.equals("yas537-pseudo-gyro") || GyroName.equals("YAS537-pseudo-gyro") || GyroName.equals("YAS533-pseudo-gyro")) {
                if (prop.contains("PD1613")) {
                    AllConfig.mIsAKMVirtGryo = true;
                    Log.d("MotionDet", "yas pseudo-gyro PD1613 force set mIsAKMVirtGryo true");
                } else {
                    AllConfig.mIsYASVirtGryo = true;
                    Log.d("MotionDet", "yas pseudo-gyro");
                }
            }
            float[] fArr = last_mag_data;
            last_mag_data[2] = 0.0f;
            last_mag_data[1] = 0.0f;
            fArr[0] = 0.0f;
            this.mag_ab = 0;
            if (AllConfig.mNeedMag) {
                this.mag_vibrator_state = true;
            } else {
                this.mag_vibrator_state = false;
            }
            this.mag_vibrator_counter = 0;
            Log.d("MotionDet", "tage22222" + AllConfig.mIsPhyGryo);
            if (AllConfig.mIsAKMVirtGryo || AllConfig.mIsYASVirtGryo) {
                this.pick_up_cnt_threshhold = 4;
                this.VibratorState = 0;
                this.VibratorStateCount = 0;
                this.VibratorStaticCount = 0;
            } else {
                this.pick_up_cnt_threshhold = 3;
            }
            this.first_flag = true;
            Log.d("MotionDet", "first_flag " + this.first_flag);
            this.acceleromererListener.last_prox = -1;
            this.MagneticListener.last_prox = -1;
            this.GproScopeListener.last_prox = -1;
            this.RotationVectorListener.last_prox = -1;
            this.ProximityListener.last_prox = -1;
            Log.d("MotionDet", "set init proximity value while regiseter");
            this.mServiceHandler.removeMessages(5);
            this.mServiceHandler.sendMessage(this.mServiceHandler.obtainMessage(5));
            if (AllConfig.mIsArchADSP) {
                if (AllConfig.mIsADSPAKMVirtGryo) {
                    this.MotionDetSensorManager.registerListener(this.acceleromererListener, this.MotionDetSensorManager.getDefaultSensor(1), 25000, handler);
                    if (AllConfig.mNeedMag) {
                        this.MotionDetSensorManager.registerListener(this.MagneticListener, this.MotionDetSensorManager.getDefaultSensor(2), 1, handler);
                    }
                    this.MotionDetSensorManager.registerListener(this.GproScopeListener, this.MotionDetSensorManager.getDefaultSensor(4), 1, handler);
                    this.MotionDetSensorManager.registerListener(this.ProximityListener, this.MotionDetSensorManager.getDefaultSensor(8), 20000, this.mProximityDataHandler);
                    Log.d("MotionDet", "mIsADSPAKMVirtGryo");
                } else if (AllConfig.mIsAKMVirtGryo) {
                    this.MotionDetSensorManager.registerListener(this.acceleromererListener, this.MotionDetSensorManager.getDefaultSensor(1), StateInfo.STATE_FINGERPRINT_GOTO_SLEEP, handler);
                    this.MotionDetSensorManager.registerListener(this.MagneticListener, this.MotionDetSensorManager.getDefaultSensor(2), 1, handler);
                    this.MotionDetSensorManager.registerListener(this.GproScopeListener, this.MotionDetSensorManager.getDefaultSensor(4), StateInfo.STATE_FINGERPRINT_GOTO_SLEEP, handler);
                    this.MotionDetSensorManager.registerListener(this.ProximityListener, this.MotionDetSensorManager.getDefaultSensor(8), 20000, this.mProximityDataHandler);
                } else if (AllConfig.mIsYASVirtGryo) {
                    this.MotionDetSensorManager.registerListener(this.acceleromererListener, this.MotionDetSensorManager.getDefaultSensor(1), 25000, handler);
                    this.MotionDetSensorManager.registerListener(this.MagneticListener, this.MotionDetSensorManager.getDefaultSensor(2), 1, handler);
                    this.MotionDetSensorManager.registerListener(this.GproScopeListener, this.MotionDetSensorManager.getDefaultSensor(4), 1, handler);
                    this.MotionDetSensorManager.registerListener(this.ProximityListener, this.MotionDetSensorManager.getDefaultSensor(8), 20000, this.mProximityDataHandler);
                    Log.d("MotionDet", "is ADSP Arch mIsYASVirtGryo");
                } else {
                    this.MotionDetSensorManager.registerListener(this.acceleromererListener, this.MotionDetSensorManager.getDefaultSensor(1), 25000, handler);
                    this.MotionDetSensorManager.registerListener(this.GproScopeListener, this.MotionDetSensorManager.getDefaultSensor(4), 40000, handler);
                    this.MotionDetSensorManager.registerListener(this.ProximityListener, this.MotionDetSensorManager.getDefaultSensor(8), 20000, this.mProximityDataHandler);
                }
            } else if (AllConfig.mIsAKMVirtGryo) {
                this.MotionDetSensorManager.registerListener(this.acceleromererListener, this.MotionDetSensorManager.getDefaultSensor(1), StateInfo.STATE_FINGERPRINT_GOTO_SLEEP, handler);
                this.MotionDetSensorManager.registerListener(this.MagneticListener, this.MotionDetSensorManager.getDefaultSensor(2), 1, handler);
                this.MotionDetSensorManager.registerListener(this.GproScopeListener, this.MotionDetSensorManager.getDefaultSensor(4), StateInfo.STATE_FINGERPRINT_GOTO_SLEEP, handler);
                this.MotionDetSensorManager.registerListener(this.ProximityListener, this.MotionDetSensorManager.getDefaultSensor(8), 20000, this.mProximityDataHandler);
            } else if (AllConfig.mIsYASVirtGryo) {
                this.MotionDetSensorManager.registerListener(this.acceleromererListener, this.MotionDetSensorManager.getDefaultSensor(1), 25000, handler);
                this.MotionDetSensorManager.registerListener(this.MagneticListener, this.MotionDetSensorManager.getDefaultSensor(2), 1, handler);
                this.MotionDetSensorManager.registerListener(this.GproScopeListener, this.MotionDetSensorManager.getDefaultSensor(4), 1, handler);
                this.MotionDetSensorManager.registerListener(this.ProximityListener, this.MotionDetSensorManager.getDefaultSensor(8), 20000, this.mProximityDataHandler);
                Log.d("MotionDet", "mIsYASVirtGryo");
            } else if (AllConfig.mIsALPSVirtGryo) {
                this.MotionDetSensorManager.registerListener(this.acceleromererListener, this.MotionDetSensorManager.getDefaultSensor(1), 25000, handler);
                this.MotionDetSensorManager.registerListener(this.GproScopeListener, this.MotionDetSensorManager.getDefaultSensor(4), 1, handler);
                this.MotionDetSensorManager.registerListener(this.ProximityListener, this.MotionDetSensorManager.getDefaultSensor(8), 20000, this.mProximityDataHandler);
            } else {
                this.MotionDetSensorManager.registerListener(this.acceleromererListener, this.MotionDetSensorManager.getDefaultSensor(1), 25000, handler);
                this.MotionDetSensorManager.registerListener(this.GproScopeListener, this.MotionDetSensorManager.getDefaultSensor(4), 40000, handler);
                this.MotionDetSensorManager.registerListener(this.ProximityListener, this.MotionDetSensorManager.getDefaultSensor(8), 20000, this.mProximityDataHandler);
            }
        }
        return true;
    }

    public boolean stopMotionRecognitionService() {
        if (this.isDirectCallingServiceWorking) {
            this.MotionDetSensorManager.unregisterListener(this.acceleromererListener);
            this.MotionDetSensorManager.unregisterListener(this.GproScopeListener);
            if (AllConfig.mIsYASVirtGryo || AllConfig.mIsAKMVirtGryo || AllConfig.mIsADSPAKMVirtGryo) {
                this.MotionDetSensorManager.unregisterListener(this.MagneticListener);
            }
            this.MotionDetSensorManager.unregisterListener(this.ProximityListener);
            if (this.mServiceHandler != null) {
                this.mServiceHandler.removeMessages(6);
                this.mServiceHandler.sendMessage(this.mServiceHandler.obtainMessage(6));
            }
            this.isDirectCallingServiceWorking = false;
            if (this.mProximityDataThread != null) {
                this.mProximityDataThread.quit();
            }
            synchronized (mHandleLock) {
                this.mCallBackHandler = null;
                this.mServiceHandler = null;
                this.mProximityDataHandler = null;
                this.mProximityDataThread = null;
            }
        }
        return true;
    }
}
