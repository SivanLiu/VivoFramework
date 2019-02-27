package com.vivo.services.motion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemProperties;
import android.util.Log;
import com.sensoroperate.VivoSensorTest;
import com.vivo.common.autobrightness.AblConfig;
import com.vivo.common.autobrightness.StateInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;

public final class PocketModeService implements IMotionRecognitionService {
    private static boolean DBG = true;
    private static final int MSG_POCKET_FIFO_ENABLE = 5;
    private static final int MSG_POCKET_JUDGE = 6;
    private static final int MSG_POCKET_MODE_DET_START = 1;
    private static final int MSG_POCKET_MODE_DET_STOP = 2;
    private static final int MSG_POCKET_MODE_DET_TRIGER = 3;
    private static final int MSG_POCKET_STOP_ACC_DET = 4;
    private static String MTK_PLATFORM = "MTK";
    private static String PLATFORM_TAG = "ro.vivo.product.solution";
    private static String QCOM_PLATFORM = "QCOM";
    private static final String TAG = "PocketModeService";
    private static final Object mObjectLock = new Object();
    private static PocketModeService mSinglePocketModeService = new PocketModeService();
    private int Totalcnt = 0;
    private float data_x = 0.0f;
    private float data_y = 0.0f;
    private float data_z = 0.0f;
    private int getdataCnt = 0;
    private boolean isAuthWorking = false;
    private boolean isPocketModeWorking = false;
    private int last_prox = -1;
    private int last_prox1 = -1;
    private int last_prox2 = -1;
    private int last_prox3 = -1;
    private int last_prox4 = -1;
    private long last_time;
    private long logic_time;
    private WakeLock mAWakeLock = null;
    private float[] mAccSensorVal = new float[3];
    private MotionSensorEventListener mAcceleromererListener = new MotionSensorEventListener(this, null);
    private Handler mCallBackHandler = null;
    private Context mContext = null;
    private SensorData[] mData;
    private PocketModeAnalyzer mPocketModeAnalyzer = new PocketModeAnalyzer();
    private MotionSensorEventListener mProximityListener = new MotionSensorEventListener(this, null);
    private WakeLock mSWakeLock = null;
    private SensorManager mSensorManager;
    private Handler mServiceHandler = null;
    private VivoSensorTest mVivoSensorTest = null;
    private PowerManager pm = null;

    private class MotionSensorEventListener implements SensorEventListener {
        /* synthetic */ MotionSensorEventListener(PocketModeService this$0, MotionSensorEventListener -this1) {
            this();
        }

        private MotionSensorEventListener() {
        }

        public void onSensorChanged(SensorEvent event) {
            String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            switch (event.sensor.getType()) {
                case 1:
                    if (prop != null && AllConfig.mIsArchADSP) {
                        PocketModeService.this.mPocketModeAnalyzer.judgewithoutfifo(event);
                        break;
                    } else {
                        PocketModeService.this.mPocketModeAnalyzer.judge(event);
                        break;
                    }
                    break;
                case 8:
                case StateInfo.STATE_BIT_LIGHT /*32*/:
                    int logic_value = event.values[0] > 0.0f ? 1 : 0;
                    PocketModeService.this.logic_time = event.timestamp / 1000000;
                    if (PocketModeService.this.last_prox != logic_value) {
                        if (logic_value != 1 || PocketModeService.this.last_prox != 0 || PocketModeService.this.logic_time - PocketModeService.this.last_time <= 1500) {
                            if (!(PocketModeService.this.mAWakeLock == null || (PocketModeService.this.mAWakeLock.isHeld() ^ 1) == 0)) {
                                Log.d(PocketModeService.TAG, "mAWakeLock acquire");
                                PocketModeService.this.mAWakeLock.acquire(2000);
                            }
                            if (PocketModeService.this.mServiceHandler != null && PocketModeService.this.isAuthWorking) {
                                Log.d(PocketModeService.TAG, "no need to set fifomode");
                            }
                        } else if (PocketModeService.this.isAuthWorking) {
                            Log.d(PocketModeService.TAG, "can not clear fifo to isAuthWorking:" + PocketModeService.this.isAuthWorking);
                        } else {
                            if (!(PocketModeService.this.mAWakeLock == null || (PocketModeService.this.mAWakeLock.isHeld() ^ 1) == 0)) {
                                Log.d(PocketModeService.TAG, "mAWakeLock acquire");
                                PocketModeService.this.mAWakeLock.acquire(2500);
                            }
                            if (PocketModeService.this.mSensorManager != null) {
                                Log.d(PocketModeService.TAG, "registerListener mAcceleromererListener");
                                synchronized (PocketModeService.mObjectLock) {
                                    if (PocketModeService.this.mServiceHandler != null) {
                                        PocketModeService.this.mSensorManager.registerListener(PocketModeService.this.mAcceleromererListener, PocketModeService.this.mSensorManager.getDefaultSensor(1), 25000, PocketModeService.this.mServiceHandler);
                                    }
                                }
                                PocketModeService.this.mPocketModeAnalyzer.judge(event);
                            }
                            synchronized (PocketModeService.mObjectLock) {
                                if (PocketModeService.this.mServiceHandler != null) {
                                    Log.d(PocketModeService.TAG, "MSG_POCKET_STOP_ACC_DET,1000");
                                    if (prop == null || !AllConfig.mIsArchADSP) {
                                        PocketModeService.this.mServiceHandler.sendEmptyMessageDelayed(4, 1200);
                                    } else {
                                        PocketModeService.this.mServiceHandler.sendEmptyMessageDelayed(4, 2000);
                                    }
                                    PocketModeService.this.isAuthWorking = true;
                                }
                            }
                        }
                        PocketModeService.this.last_prox = logic_value;
                        PocketModeService.this.last_time = PocketModeService.this.logic_time;
                        break;
                    }
                    PocketModeService.this.last_prox = logic_value;
                    return;
                    break;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private class PocketModeAnalyzer {
        private static final int gsensorNum = 30;
        private int authCnt;
        public int authStep;
        private float countxyz = -4096.0f;
        private int data_debounce;
        private int data_negative = 0;
        private int errNum = 0;
        private float[][] gsensorData = ((float[][]) Array.newInstance(Float.TYPE, new int[]{30, 3}));
        private float[] gsensorDataMax = new float[3];
        private float[] gsensorDataMin = new float[3];
        private boolean judgeflag = true;
        private float lastvalueZ = 0.0f;
        private float lastxyz = -4096.0f;
        private int mFinalStateCnt = 0;
        private float maxX = 0.0f;
        private float maxY = 0.0f;
        private float maxZ = 0.0f;
        private boolean mflag = true;
        private float minX = 0.0f;
        private float minY = 0.0f;
        private float minZ = 0.0f;
        private int phone_negative = 0;
        private int phone_up_count;
        private float xMaxValue = 0.0f;
        private float xMinValue = 0.0f;
        private float yMaxValue = 0.0f;
        private float yMinValue = 0.0f;
        private float zMaxValue = 10.0f;
        private float zMinValue = 10.0f;

        public void reset() {
            this.mFinalStateCnt = 0;
            this.authCnt = 0;
            this.authStep = 0;
            this.mFinalStateCnt = 0;
            PocketModeService.this.isAuthWorking = false;
            this.judgeflag = true;
            this.phone_up_count = 0;
            this.data_debounce = 0;
            this.mflag = true;
            this.lastxyz = -4096.0f;
            this.countxyz = -4096.0f;
            this.minX = 0.0f;
            this.maxX = 0.0f;
            this.minY = 0.0f;
            this.maxY = 0.0f;
            this.minZ = 0.0f;
            this.maxZ = 0.0f;
            this.phone_negative = 0;
            this.data_negative = 0;
            this.errNum = 0;
            for (int i = 0; i < 30; i++) {
                for (int j = 0; j < 3; j++) {
                    this.gsensorData[i][j] = 0.0f;
                }
            }
        }

        private void judge_one(float x, float y, float z) {
            Log.d(PocketModeService.TAG, " xyTotalValue: " + (((x * x) + (y * y)) + (z * z)) + " event.values[0]: " + x + " event.values[1]: " + y + " event.values[2]: " + z);
            if (this.authStep == 2 && this.judgeflag) {
                this.xMaxValue = x;
                this.xMinValue = x;
                this.yMaxValue = y;
                this.yMinValue = y;
                this.zMaxValue = z;
                this.zMinValue = z;
            }
            if (this.errNum < 5) {
                this.errNum++;
            } else {
                for (int i = 29; i > 0; i--) {
                    for (int j = 0; j < 3; j++) {
                        this.gsensorData[i][j] = this.gsensorData[i - 1][j];
                    }
                }
                this.gsensorData[0][0] = x;
                this.gsensorData[0][1] = y;
                this.gsensorData[0][2] = z;
            }
            String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            switch (this.authStep) {
                case 0:
                    this.authStep = 1;
                    break;
                case 1:
                    if (Math.abs(z) < 15.0f) {
                        this.authCnt++;
                    } else {
                        this.authCnt = 0;
                    }
                    if (this.authCnt >= 3) {
                        this.authStep = 2;
                        this.authCnt = 0;
                        break;
                    }
                    break;
                case 2:
                    if (this.authCnt < 3) {
                        if (z > this.lastvalueZ) {
                            this.authCnt++;
                        } else {
                            this.authCnt = 0;
                        }
                    }
                    if (this.xMaxValue < x) {
                        this.xMaxValue = x;
                    }
                    if (this.xMinValue > x) {
                        this.xMinValue = x;
                    }
                    if (this.yMaxValue < y) {
                        this.yMaxValue = y;
                    }
                    if (this.yMinValue > y) {
                        this.yMinValue = y;
                    }
                    if (this.zMaxValue < z) {
                        this.zMaxValue = z;
                    }
                    if (this.zMinValue > z) {
                        this.zMinValue = z;
                    }
                    this.judgeflag = false;
                    if (this.authCnt >= 2 && ((double) (this.zMaxValue - this.zMinValue)) > 0.2d) {
                        this.authStep = 3;
                        this.judgeflag = true;
                        this.authCnt = 0;
                        break;
                    }
                case 3:
                    if (((double) x) > -6.5d && ((double) x) < 6.5d && y > -3.0f && y < 10.0f && z > 2.0f && z < 12.0f) {
                        this.mFinalStateCnt++;
                        if (this.mFinalStateCnt >= 5) {
                            for (int p = 0; p < 3; p++) {
                                this.gsensorDataMin[p] = this.gsensorData[0][p];
                                this.gsensorDataMax[p] = this.gsensorData[0][p];
                            }
                            int m = 1;
                            while (m < 30 && (this.gsensorData[m][0] != 0.0f || this.gsensorData[m][1] != 0.0f || this.gsensorData[m][2] != 0.0f)) {
                                for (int n = 0; n < 3; n++) {
                                    if (this.gsensorData[m][n] < this.gsensorDataMin[n]) {
                                        this.gsensorDataMin[n] = this.gsensorData[m][n];
                                    }
                                    if (this.gsensorData[m][n] > this.gsensorDataMax[n]) {
                                        this.gsensorDataMax[n] = this.gsensorData[m][n];
                                    }
                                }
                                m++;
                            }
                            if (((double) (this.gsensorDataMax[0] - this.gsensorDataMin[0])) > 1.5d || ((double) (this.gsensorDataMax[1] - this.gsensorDataMin[1])) > 1.5d || ((double) (this.gsensorDataMax[2] - this.gsensorDataMin[2])) > 1.5d) {
                                if (PocketModeService.this.mServiceHandler != null) {
                                    PocketModeService.this.mSWakeLock.acquire(3000);
                                    SensorManager -get15 = PocketModeService.this.mSensorManager;
                                    synchronized (PocketModeService.mObjectLock) {
                                        if (PocketModeService.this.mServiceHandler != null) {
                                            PocketModeService.this.mServiceHandler.sendEmptyMessage(3);
                                        }
                                    }
                                }
                                this.authStep = 4;
                                PocketModeService.this.Totalcnt = PocketModeService.this.getdataCnt;
                                break;
                            }
                        }
                    }
                    this.mFinalStateCnt = 0;
                    break;
                    break;
            }
            this.lastvalueZ = z;
            Log.d(PocketModeService.TAG, "fifo auth step" + this.authStep + ",cnt:" + this.authCnt + "mFinalStateCnt:" + this.mFinalStateCnt + "phone_up_count: " + this.phone_up_count);
        }

        private void judge(SensorEvent event) {
            if (event.sensor.getType() == 1) {
                Log.d(PocketModeService.TAG, " xyTotalValue: " + ((event.values[0] * event.values[0]) + (event.values[1] * event.values[1])) + " event.values[0]: " + event.values[0] + " event.values[1]: " + event.values[1] + " event.values[2]: " + event.values[2]);
                if (this.authStep == 2 && this.judgeflag) {
                    this.xMaxValue = event.values[0];
                    this.xMinValue = event.values[0];
                    this.yMaxValue = event.values[1];
                    this.yMinValue = event.values[1];
                    this.zMaxValue = event.values[2];
                    this.zMinValue = event.values[2];
                }
                String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
                switch (this.authStep) {
                    case 0:
                        if (Math.abs(event.values[2]) >= 10.0f || (Math.abs(event.values[0]) <= 5.0f && Math.abs(event.values[1]) <= 5.0f)) {
                            this.authCnt = 0;
                        } else {
                            this.authCnt++;
                        }
                        if (this.authCnt >= 4) {
                            this.authStep = 1;
                            this.authCnt = 0;
                            break;
                        }
                        break;
                    case 1:
                        if (Math.abs(event.values[2]) >= 15.0f || (Math.abs(event.values[0]) <= 1.0f && Math.abs(event.values[1]) <= 1.0f)) {
                            this.authCnt = 0;
                        } else {
                            this.authCnt++;
                        }
                        if (this.authCnt >= 3) {
                            this.authStep = 2;
                            this.authCnt = 0;
                            break;
                        }
                        break;
                    case 2:
                        if (this.authCnt < 3) {
                            if (event.values[2] > this.lastvalueZ) {
                                this.authCnt++;
                            } else {
                                this.authCnt = 0;
                            }
                        }
                        if (this.xMaxValue < event.values[0]) {
                            this.xMaxValue = event.values[0];
                        }
                        if (this.xMinValue > event.values[0]) {
                            this.xMinValue = event.values[0];
                        }
                        if (this.yMaxValue < event.values[1]) {
                            this.yMaxValue = event.values[1];
                        }
                        if (this.yMinValue > event.values[1]) {
                            this.yMinValue = event.values[1];
                        }
                        if (this.zMaxValue < event.values[2]) {
                            this.zMaxValue = event.values[2];
                        }
                        if (this.zMinValue > event.values[2]) {
                            this.zMinValue = event.values[2];
                        }
                        this.judgeflag = false;
                        Log.d(PocketModeService.TAG, "(zMaxValue-zMinValue): " + (this.zMaxValue - this.zMinValue));
                        if (this.authCnt >= 3 && ((double) (this.zMaxValue - this.zMinValue)) > 0.2d) {
                            this.authStep = 3;
                            this.judgeflag = true;
                            this.authCnt = 0;
                            break;
                        }
                    case 3:
                        if (((double) event.values[0]) > -6.5d && ((double) event.values[0]) < 6.5d && event.values[1] > -3.0f && event.values[1] < 10.0f && event.values[2] > 2.0f && event.values[2] < 12.0f) {
                            this.mFinalStateCnt++;
                            if (this.mFinalStateCnt == 5) {
                                if (PocketModeService.this.mServiceHandler != null && this.authStep == 3) {
                                    if (PocketModeService.this.mSensorManager != null) {
                                        PocketModeService.this.mSensorManager.unregisterListener(PocketModeService.this.mAcceleromererListener);
                                    }
                                    synchronized (PocketModeService.mObjectLock) {
                                        if (PocketModeService.this.mServiceHandler != null) {
                                            PocketModeService.this.mServiceHandler.sendEmptyMessage(3);
                                        }
                                    }
                                }
                                this.authStep = 4;
                                break;
                            }
                        }
                        this.mFinalStateCnt = 0;
                        break;
                        break;
                }
                this.lastvalueZ = event.values[2];
                Log.d(PocketModeService.TAG, "judge gsensor data:" + event.values[0] + "," + event.values[1] + "," + event.values[2] + "," + this.mFinalStateCnt);
                Log.d(PocketModeService.TAG, "fifo auth step" + this.authStep + ",cnt:" + this.authCnt + "mFinalStateCnt:" + this.mFinalStateCnt);
            }
        }

        private void judgewithoutfifo(SensorEvent event) {
            float xyz;
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            Log.d(PocketModeService.TAG, "x " + x + " y " + y + " z " + z);
            if (prop == null || !AllConfig.mIsArchADSP) {
                xyz = (float) ((9.81d - Math.sqrt((double) (((x * x) + (y * y)) + (z * z)))) - 0.2d);
            } else {
                xyz = (float) ((9.81d - Math.sqrt((double) (((x * x) + (y * y)) + (z * z)))) - 0.6d);
            }
            if (this.mflag) {
                this.mflag = false;
                this.minX = x;
                this.maxX = x;
                this.minY = y;
                this.maxY = y;
                this.minZ = z;
                this.maxZ = z;
            }
            if (this.minX > x) {
                this.minX = x;
            }
            if (this.minY > y) {
                this.minY = y;
            }
            if (this.minZ > z) {
                this.minZ = z;
            }
            if (this.maxX < x) {
                this.maxX = x;
            }
            if (this.maxY < y) {
                this.maxY = y;
            }
            if (this.maxZ < z) {
                this.maxZ = z;
            }
            if (this.authStep == 0 && (((double) xyz) > 0.3d || ((double) xyz) < -0.3d)) {
                this.authStep = 1;
            }
            if (this.authStep == 1) {
                if (((double) xyz) > 0.0d) {
                    this.phone_up_count++;
                    if (this.phone_up_count >= 3) {
                        this.phone_up_count = 0;
                        this.data_debounce = 0;
                        this.authStep = 2;
                    }
                } else {
                    this.data_debounce++;
                    if (this.data_debounce > 1) {
                        this.phone_up_count = 0;
                        this.data_debounce = 0;
                    }
                }
                if (((double) xyz) < 0.0d) {
                    this.phone_negative++;
                    if (this.phone_negative >= 3) {
                        this.phone_negative = 0;
                        this.data_negative = 0;
                        this.authStep = 3;
                    }
                } else {
                    this.data_negative++;
                    if (this.data_negative > 1) {
                        this.phone_negative = 0;
                        this.data_negative = 0;
                    }
                }
            }
            if (this.authStep == 2) {
                if (((double) xyz) < 0.0d) {
                    this.phone_up_count++;
                    if (this.phone_up_count >= 3) {
                        this.phone_up_count = 0;
                        this.data_debounce = 0;
                        this.authStep = 3;
                    }
                } else {
                    this.data_debounce++;
                    if (this.data_debounce > 1) {
                        this.phone_up_count = 0;
                        this.data_debounce = 0;
                    }
                }
            }
            if (this.authStep == 3) {
                if (((double) x) <= -6.6d || ((double) x) >= 6.6d || ((double) y) <= -3.0d || ((double) y) >= 10.0d || ((double) z) <= 2.0d || ((double) z) >= 12.0d) {
                    this.data_debounce++;
                    if (this.data_debounce > 1) {
                        this.phone_up_count = 0;
                        this.data_debounce = 0;
                    }
                } else {
                    this.phone_up_count++;
                    if (this.phone_up_count >= 3) {
                        this.phone_up_count = 0;
                        this.data_debounce = 0;
                        this.authStep = 4;
                    }
                }
            }
            if (this.authStep == 4 && ((double) (this.maxX - this.minX)) > 0.5d && ((double) (this.maxY - this.minY)) > 0.5d && ((double) (this.maxZ - this.minZ)) > 0.5d && ((((double) (this.maxX - this.minX)) > 1.0d || ((double) (this.maxY - this.minY)) > 1.0d || ((double) (this.maxZ - this.minZ)) > 1.0d) && PocketModeService.this.mServiceHandler != null)) {
                synchronized (PocketModeService.mObjectLock) {
                    if (PocketModeService.this.mServiceHandler != null) {
                        PocketModeService.this.mServiceHandler.sendEmptyMessage(3);
                    }
                }
                if (PocketModeService.this.mSensorManager != null) {
                    PocketModeService.this.mSensorManager.unregisterListener(PocketModeService.this.mAcceleromererListener);
                }
            }
            Log.d(PocketModeService.TAG, "judgewithoutfifo authStep:" + this.authStep + " phone_up_count:" + this.phone_up_count + "xyz " + xyz);
        }
    }

    private class PocketModeServiceHandler extends Handler {
        public PocketModeServiceHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d(PocketModeService.TAG, "MSG_POCKET_MODE_DET_START");
                    return;
                case 2:
                    Log.d(PocketModeService.TAG, "MSG_POCKET_MODE_DET_STOP");
                    return;
                case 3:
                    Message smsg = Message.obtain();
                    smsg.what = 16;
                    smsg.obj = new Integer(6);
                    synchronized (PocketModeService.mObjectLock) {
                        if (PocketModeService.this.mCallBackHandler != null) {
                            PocketModeService.this.mCallBackHandler.sendMessage(smsg);
                        }
                        if (PocketModeService.this.mServiceHandler != null) {
                            Log.d(PocketModeService.TAG, "MSG_POCKET_STOP_ACC_DET");
                            PocketModeService.this.mServiceHandler.removeMessages(4);
                            PocketModeService.this.mServiceHandler.sendEmptyMessage(4);
                        }
                    }
                    Log.d(PocketModeService.TAG, "MSG_POCKET_MODE_DET_TRIGER");
                    return;
                case 4:
                    String prop1 = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
                    if (PocketModeService.this.mSensorManager != null) {
                        PocketModeService.this.mSensorManager.unregisterListener(PocketModeService.this.mAcceleromererListener);
                    }
                    PocketModeService.this.mPocketModeAnalyzer.reset();
                    Log.d(PocketModeService.TAG, "MSG_POCKET_STOP_ACC_DET");
                    return;
                case 6:
                    String proj = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
                    boolean result = PocketModeService.this.getDataFromI2c();
                    if (result) {
                        PocketModeService pocketModeService = PocketModeService.this;
                        pocketModeService.getdataCnt = pocketModeService.getdataCnt + 1;
                        PocketModeService.this.mPocketModeAnalyzer.judge_one(PocketModeService.this.data_x, PocketModeService.this.data_y, PocketModeService.this.data_z);
                        Log.d(PocketModeService.TAG, "getdataCnt:" + PocketModeService.this.getdataCnt);
                        if (PocketModeService.this.getdataCnt == PocketModeService.this.Totalcnt || PocketModeService.this.getdataCnt > 31) {
                            Log.d(PocketModeService.TAG, "Fianl data get:" + PocketModeService.this.getdataCnt);
                            PocketModeService.this.getdataCnt = 0;
                            PocketModeService.this.Totalcnt = 0;
                            return;
                        }
                        synchronized (PocketModeService.mObjectLock) {
                            if (PocketModeService.this.mServiceHandler != null) {
                                if (proj == null || !(proj.equals("PD1227B") || proj.equals("PD1227BT") || proj.equals("PD1227BW") || proj.equals("PD1227T") || proj.equals("TD1305") || proj.equals("PD1401L") || proj.equals("PD1401LG4"))) {
                                    PocketModeService.this.mServiceHandler.sendEmptyMessageDelayed(6, 18);
                                } else {
                                    PocketModeService.this.mServiceHandler.sendEmptyMessageDelayed(6, 25);
                                }
                            }
                        }
                        return;
                    }
                    Log.d(PocketModeService.TAG, "get data error" + result);
                    return;
                default:
                    return;
            }
        }
    }

    private class SensorData {
        public long timestamp;
        public final float[] values;

        SensorData(int size) {
            this.values = new float[size];
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0033 A:{SYNTHETIC, Splitter: B:14:0x0033} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003c A:{SYNTHETIC, Splitter: B:19:0x003c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeFile(String path, String data) {
        Throwable th;
        FileOutputStream fos = null;
        try {
            FileOutputStream fos2 = new FileOutputStream(path);
            try {
                fos2.write(data.getBytes());
                if (fos2 != null) {
                    try {
                        fos2.close();
                    } catch (IOException e) {
                    }
                }
                fos = fos2;
            } catch (IOException e2) {
                fos = fos2;
                try {
                    Log.d(TAG, "Unable to write " + path);
                    if (fos == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e3) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fos = fos2;
                if (fos != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            Log.d(TAG, "Unable to write " + path);
            if (fos == null) {
                try {
                    fos.close();
                } catch (IOException e5) {
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x00a9 A:{SYNTHETIC, Splitter: B:26:0x00a9} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String readFile(String fileName) {
        FileNotFoundException e;
        Throwable th;
        BufferedReader reader = null;
        String tempString = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(fileName)));
            try {
                tempString = reader2.readLine();
                reader2.close();
            } catch (Exception e2) {
                try {
                    Log.d(TAG, "reader.readLine():" + e2.getMessage());
                } catch (FileNotFoundException e3) {
                    e = e3;
                    reader = reader2;
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            }
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e1) {
                    Log.d(TAG, "the readFile is:" + e1.getMessage());
                }
            }
            reader = reader2;
        } catch (FileNotFoundException e4) {
            e = e4;
            try {
                Log.d(TAG, "the readFile is:" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e12) {
                        Log.d(TAG, "the readFile is:" + e12.getMessage());
                    }
                }
                return tempString;
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Log.d(TAG, "the readFile is:" + e122.getMessage());
                    }
                }
                throw th;
            }
        }
        return tempString;
    }

    public static PocketModeService getInstance() {
        return mSinglePocketModeService;
    }

    private PocketModeService() {
        if (SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null) != null && AllConfig.mIsArchADSP) {
            this.mVivoSensorTest = VivoSensorTest.getInstance();
        }
    }

    public boolean startMotionRecognitionService(Context context, Handler handler) {
        Log.d(TAG, "startMotionRecognitionService ");
        if (!this.isPocketModeWorking) {
            this.mContext = context;
            this.isPocketModeWorking = true;
            this.mCallBackHandler = handler;
            this.mPocketModeAnalyzer.reset();
            this.mServiceHandler = new PocketModeServiceHandler(handler.getLooper());
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            this.pm = (PowerManager) this.mContext.getSystemService("power");
            this.mAWakeLock = this.pm.newWakeLock(1, TAG);
            this.mSWakeLock = this.pm.newWakeLock(805306394, TAG);
            if (this.mSensorManager != null) {
                this.mSensorManager.registerListener(this.mProximityListener, this.mSensorManager.getDefaultSensor(8), 100000);
                this.last_prox = -1;
                Log.d(TAG, "startMotionRecognitionService last_prox = " + this.last_prox);
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
        Log.d(TAG, "stopMotionRecognitionService " + this.isPocketModeWorking);
        if (this.isPocketModeWorking) {
            this.isPocketModeWorking = false;
            this.mServiceHandler.removeMessages(4);
            synchronized (mObjectLock) {
                this.mCallBackHandler = null;
                this.mServiceHandler = null;
            }
            if (this.mSensorManager != null) {
                this.mSensorManager.unregisterListener(this.mAcceleromererListener);
                this.mSensorManager.unregisterListener(this.mProximityListener);
            }
            this.mSensorManager = null;
        }
        return true;
    }

    private boolean getDataFromI2c() {
        String data = null;
        float pocke_x = 0.0f;
        float pocke_y = 0.0f;
        float pocke_z = 0.0f;
        try {
            data = readFile("/sys/bus/platform/drivers/gsensor/data");
        } catch (Exception e) {
            Log.d(TAG, "readFileByline:" + e.getMessage());
        }
        if (data != null) {
            String[] out = data.split(" ");
            if (out != null) {
                try {
                    pocke_x = Float.parseFloat(out[0]) / 1000.0f;
                    pocke_y = Float.parseFloat(out[1]) / 1000.0f;
                    pocke_z = Float.parseFloat(out[2]) / 1000.0f;
                    this.data_x = pocke_x;
                    this.data_y = pocke_y;
                    this.data_z = pocke_z;
                } catch (Exception e2) {
                    Log.d(TAG, "pocke_x" + pocke_x + "pocke_y" + pocke_y + "pocke_z" + pocke_z);
                    Log.d(TAG, "data error");
                    return false;
                }
            }
            Log.d(TAG, "out null");
            return false;
        }
        return true;
    }
}
