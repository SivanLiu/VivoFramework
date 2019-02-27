package com.vivo.services.motion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephony.Stub;
import com.vivo.common.autobrightness.AblConfig;
import com.vivo.common.autobrightness.StateInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import vivo.app.VivoFrameworkFactory;
import vivo.app.proxcali.AbsVivoProxCaliManager;

public final class ProximityAcrossService implements IMotionRecognitionService {
    private static final int AIROPERATION_TYPE_PROXIMITY_ACROSS = 0;
    private static boolean DBG = true;
    private static final int MSG_COLLECT_DATA = 6;
    private static final int MSG_PROXIMITY_PARAM_DISABLE = 5;
    private static final int MSG_PROXIMITY_PARAM_ENABLE = 4;
    private static final int MSG_PROX_ACROSS_DET_START = 1;
    private static final int MSG_PROX_ACROSS_DET_STOP = 2;
    private static final int MSG_PROX_ACROSS_DET_TRIGER = 3;
    private static final String PROX_ACROSS_PATH = "/sys/bus/platform/drivers/als_ps/prox_across";
    private static final String TAG = "ProximityAcrossService";
    private static final int TRIGGERED = 0;
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final int UNTRIGGERED_FOR_NEAR_TO_FAR_TOO_LONG = 2;
    private static final int UNTRIGGERED_FOR_NOT_STEADY = 1;
    private static final int UNTRIGGERED_FOR_SCREEN_OFF = 3;
    private static float acc_data_x = 0.0f;
    private static float acc_data_y = 0.0f;
    private static float acc_data_z = 9.8f;
    private static long last_acc_x = 65535;
    private static long last_acc_y = 65535;
    private static long last_acc_z = 65535;
    private static final Object mObjectLock = new Object();
    private static ProximityAcrossService mSingleProximityAcrossService = new ProximityAcrossService();
    private int TYPE_PROXIMITY_ACROSS;
    private boolean isACC_z;
    private boolean isProximityAcrossWorking;
    private boolean isSupportAcross;
    private MotionSensorEventListener mAcceleromererListener;
    private AcrossAnalyzer mAcrossAnalyzer;
    private Handler mCallBackHandler;
    private Handler mCollectDataHandler;
    private HandlerThread mCollectDataThread;
    private Context mContext;
    private SensorData[] mData;
    private boolean mNeedUpdateWorkingState;
    private MotionSensorEventListener mProximityAcrossListener;
    private MotionSensorEventListener mProximityListener;
    private Sensor mProximitySensor;
    private SensorManager mSensorManager;
    private Handler mServiceHandler;

    private class AcrossAnalyzer {
        private static final int TRIGGER_COUNT = 10;
        private int mAcrossState;
        private int mCount;
        private SensorData[] mData;
        private float mDownThreshold;
        private long mDownTime;
        private int mNum;
        private int mSteady;
        private long mUpTime;
        private int zCount;

        public AcrossAnalyzer() {
            this.mDownThreshold = 230.0f;
            this.mNum = 0;
            this.mAcrossState = 0;
            this.mSteady = 0;
            this.zCount = 0;
            this.mCount = 0;
            this.mUpTime = 0;
            this.mDownTime = 0;
            this.mNum = 10;
            this.mData = new SensorData[10];
            for (int i = 0; i < this.mNum; i++) {
                this.mData[i] = new SensorData(3);
            }
        }

        public void getThreshold() {
            ProximityThresholdCal mProximityThresholdCal = new ProximityThresholdCal();
            if (mProximityThresholdCal.GetProximityCloseThreshold() >= 0) {
                this.mDownThreshold = (float) mProximityThresholdCal.GetProximityCloseThreshold();
            }
            Log.d(ProximityAcrossService.TAG, "getThreshold: " + this.mDownThreshold);
        }

        /* JADX WARNING: Removed duplicated region for block: B:26:0x00a9 A:{SYNTHETIC, Splitter: B:26:0x00a9} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private String readFile(String fileName) {
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
                        Log.d("TAG", "reader.readLine():" + e2.getMessage());
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
                        Log.d("TAG", "the readFile is:" + e1.getMessage());
                    }
                }
                reader = reader2;
            } catch (FileNotFoundException e4) {
                e = e4;
                try {
                    Log.d("TAG", "the readFile is:" + e.getMessage());
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e12) {
                            Log.d("TAG", "the readFile is:" + e12.getMessage());
                        }
                    }
                    return tempString;
                } catch (Throwable th3) {
                    th = th3;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e122) {
                            Log.d("TAG", "the readFile is:" + e122.getMessage());
                        }
                    }
                    throw th;
                }
            }
            return tempString;
        }

        public void reset() {
            for (int i = 0; i < this.mNum; i++) {
                this.mData[i].values[0] = 65535.0f;
                this.mData[i].values[1] = 65535.0f;
                this.mData[i].values[2] = 65535.0f;
                this.mData[i].timestamp = 0;
            }
            this.mUpTime = 0;
            this.mDownTime = 0;
            this.mAcrossState = 0;
            this.mSteady = 0;
            ProximityAcrossService.this.isACC_z = true;
            this.zCount = 0;
            Log.d(ProximityAcrossService.TAG, "reset ");
        }

        public void pushData(SensorEvent event) {
            for (int i = this.mNum - 1; i > 0; i--) {
                this.mData[i].values[0] = this.mData[i - 1].values[0];
                this.mData[i].values[1] = this.mData[i - 1].values[1];
                this.mData[i].values[2] = this.mData[i - 1].values[2];
                this.mData[i].timestamp = this.mData[i - 1].timestamp;
            }
            this.mData[0].values[0] = event.values[0];
            this.mData[0].values[1] = event.values[1];
            this.mData[0].values[2] = event.values[2];
            this.mData[0].timestamp = event.timestamp;
            Log.d(ProximityAcrossService.TAG, "pushData");
        }

        public void isSteady(float x, float y, float z) {
            float comp_sum = ((x * x) + (y * y)) + (z * z);
            if (z < 0.0f) {
                this.zCount++;
            } else {
                this.zCount = 0;
            }
            if (this.zCount > 2) {
                ProximityAcrossService.this.isACC_z = false;
                this.zCount--;
            } else {
                ProximityAcrossService.this.isACC_z = true;
            }
            if (comp_sum < 110.0f && comp_sum > 70.0f) {
                this.mSteady = 1;
            }
        }

        public boolean IsProxAcrossInFlatState_isSteady(float x, float y, float z) {
            boolean result;
            long acc_x = (long) (1000.0f * x);
            long acc_y = (long) (1000.0f * y);
            long acc_z = (long) (1000.0f * z);
            long comp_sum = ((acc_x * acc_x) + (acc_y * acc_y)) + (acc_z * acc_z);
            if (comp_sum >= 240000000 || comp_sum <= 30000000) {
                ProximityAcrossService.last_acc_x = 65535;
                ProximityAcrossService.last_acc_y = 65535;
                ProximityAcrossService.last_acc_z = 65535;
                result = false;
            } else {
                ProximityAcrossService.last_acc_x = acc_x;
                ProximityAcrossService.last_acc_y = acc_y;
                ProximityAcrossService.last_acc_z = acc_z;
                result = true;
            }
            if (!(!result || ProximityAcrossService.last_acc_x == 65535 || ProximityAcrossService.last_acc_y == 65535 || ProximityAcrossService.last_acc_z == 65535)) {
                if (Math.abs(acc_x - ProximityAcrossService.last_acc_x) > 5000 || Math.abs(acc_y - ProximityAcrossService.last_acc_y) > 5000 || Math.abs(acc_z - ProximityAcrossService.last_acc_z) > 5000) {
                    result = false;
                } else if (acc_y < -8000) {
                    result = false;
                }
            }
            if (!result && Math.abs(acc_x) < 3000 && Math.abs(acc_y) < 3000 && acc_z > -3000) {
                ProximityAcrossService.last_acc_x = 65535;
                ProximityAcrossService.last_acc_y = 65535;
                ProximityAcrossService.last_acc_z = 65535;
                result = true;
            }
            Log.d(ProximityAcrossService.TAG, "IsDevInFlatState--" + acc_x + "," + acc_y + "," + acc_z + "," + comp_sum + "," + ProximityAcrossService.last_acc_x + "," + ProximityAcrossService.last_acc_y + "," + ProximityAcrossService.last_acc_z + "," + result);
            return result;
        }

        private void judge(SensorEvent event) {
            float mProximityThreshold = 0.0f;
            synchronized (ProximityAcrossService.mObjectLock) {
                if (ProximityAcrossService.this.mSensorManager == null && ProximityAcrossService.this.mContext != null) {
                    ProximityAcrossService.this.mSensorManager = (SensorManager) ProximityAcrossService.this.mContext.getSystemService("sensor");
                }
                if (ProximityAcrossService.this.mSensorManager != null) {
                    ProximityAcrossService.this.mProximitySensor = ProximityAcrossService.this.mSensorManager.getDefaultSensor(8);
                }
            }
            if (ProximityAcrossService.this.mProximitySensor != null) {
                mProximityThreshold = Math.min(ProximityAcrossService.this.mProximitySensor.getMaximumRange(), ProximityAcrossService.TYPICAL_PROXIMITY_THRESHOLD);
            }
            int logic_value = (event.values[0] < 0.0f || event.values[0] >= mProximityThreshold) ? 1 : 0;
            switch (this.mAcrossState) {
                case 0:
                    if (logic_value == 1) {
                        this.mAcrossState = 1;
                        Log.d(ProximityAcrossService.TAG, "mAcrossState:" + this.mAcrossState);
                        return;
                    }
                    return;
                case 1:
                    if (logic_value == 0) {
                        this.mAcrossState = 2;
                        this.mDownTime = event.timestamp / 1000000;
                        Log.d(ProximityAcrossService.TAG, "mAcrossState:" + this.mAcrossState);
                        return;
                    }
                    return;
                case 2:
                    if (logic_value == 1) {
                        this.mAcrossState = 3;
                        this.mUpTime = event.timestamp / 1000000;
                        Log.d(ProximityAcrossService.TAG, "mAcrossState:" + this.mAcrossState + "mUpTime - mDownTime:" + (this.mUpTime - this.mDownTime));
                        if (this.mUpTime - this.mDownTime < 500) {
                            int i;
                            if (!ProximityAcrossService.this.callNotice() || ProximityAcrossService.this.isACC_z) {
                                i = 0;
                            } else {
                                i = 1;
                            }
                            if ((i ^ 1) != 0) {
                                Message msg = Message.obtain();
                                msg.what = 3;
                                synchronized (ProximityAcrossService.mObjectLock) {
                                    if (ProximityAcrossService.this.mServiceHandler != null) {
                                        ProximityAcrossService.this.mServiceHandler.sendMessage(msg);
                                    }
                                }
                                reset();
                                this.mAcrossState = 1;
                                return;
                            }
                        }
                        Log.d(ProximityAcrossService.TAG, "judge state 2: down time too long or isACC_z:" + ProximityAcrossService.this.isACC_z);
                        this.mAcrossState = 1;
                        if (ProximityAcrossService.this.mCollectDataHandler != null) {
                            Log.e(ProximityAcrossService.TAG, "0ops proximity across not triggered since down time too long, gotcha");
                            ProximityAcrossService.this.mCollectDataHandler.removeMessages(6);
                            Message collectMessage = ProximityAcrossService.this.mCollectDataHandler.obtainMessage(6);
                            collectMessage.arg1 = 2;
                            ProximityAcrossService.this.mCollectDataHandler.sendMessage(collectMessage);
                            return;
                        }
                        Log.e(ProximityAcrossService.TAG, "0ops proximity across not triggered since down time too long, but handler is null");
                        return;
                    }
                    return;
                default:
                    reset();
                    return;
            }
        }

        private void judge() {
            int i = 1;
            switch (this.mAcrossState) {
                case 0:
                    boolean up = false;
                    Log.d(ProximityAcrossService.TAG, "judge state 0");
                    for (int i2 = this.mNum - 1; i2 > -1; i2--) {
                        if (this.mData[i2].values[0] == 1.0f) {
                            up = true;
                        } else {
                            up = false;
                        }
                    }
                    if (up) {
                        this.mDownTime = 0;
                        this.mAcrossState = 1;
                        Log.d(ProximityAcrossService.TAG, "mAcrossState:" + this.mAcrossState);
                        return;
                    }
                    return;
                case 1:
                    Log.d(ProximityAcrossService.TAG, "judge state 1");
                    if (this.mData[0].values[0] == 0.0f) {
                        this.mAcrossState = 2;
                        if (this.mData[0].timestamp > this.mData[1].timestamp) {
                            this.mDownTime = ((this.mData[0].timestamp - this.mData[1].timestamp) / 1000000) + this.mDownTime;
                            return;
                        }
                        this.mDownTime += 25;
                        return;
                    }
                    return;
                case 2:
                    if (this.mData[0].values[0] == 0.0f) {
                        if (this.mData[0].timestamp > this.mData[1].timestamp) {
                            this.mDownTime += (this.mData[0].timestamp - this.mData[1].timestamp) / 1000000;
                        } else {
                            this.mDownTime += 25;
                        }
                        this.mCount = 0;
                        Log.d(ProximityAcrossService.TAG, "judge state 2: time-" + this.mDownTime);
                        if (this.mDownTime > 400) {
                            Log.d(ProximityAcrossService.TAG, "judge state 2: down time too long");
                            reset();
                            return;
                        }
                        return;
                    }
                    this.mCount++;
                    if (this.mCount >= 6) {
                        this.mAcrossState = 3;
                        Log.d(ProximityAcrossService.TAG, "judge state 3");
                        if (!ProximityAcrossService.this.callNotice() || ProximityAcrossService.this.isACC_z) {
                            i = 0;
                        }
                        if (i == 0) {
                            Log.d(ProximityAcrossService.TAG, "judge send triger message");
                            Message msg = Message.obtain();
                            msg.what = 3;
                            synchronized (ProximityAcrossService.mObjectLock) {
                                if (ProximityAcrossService.this.mServiceHandler != null) {
                                    ProximityAcrossService.this.mServiceHandler.sendMessage(msg);
                                }
                            }
                        }
                        this.mCount = 0;
                        reset();
                        return;
                    }
                    return;
                default:
                    reset();
                    return;
            }
        }
    }

    private class MotionSensorEventListener implements SensorEventListener {
        /* synthetic */ MotionSensorEventListener(ProximityAcrossService this$0, MotionSensorEventListener -this1) {
            this();
        }

        private MotionSensorEventListener() {
        }

        public void onSensorChanged(SensorEvent event) {
            int i = 1;
            if (ProximityAcrossService.this.TYPE_PROXIMITY_ACROSS == -1 || event.sensor.getType() != ProximityAcrossService.this.TYPE_PROXIMITY_ACROSS) {
                switch (event.sensor.getType()) {
                    case 1:
                        ProximityAcrossService.acc_data_x = event.values[0];
                        ProximityAcrossService.acc_data_y = event.values[1];
                        ProximityAcrossService.acc_data_z = event.values[2];
                        ProximityAcrossService.this.mAcrossAnalyzer.isSteady(event.values[0], event.values[1], event.values[2]);
                        return;
                    case 8:
                    case StateInfo.STATE_BIT_LIGHT /*32*/:
                        boolean isSteady = false;
                        if (SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null) == null || !AllConfig.mIsArchADSP) {
                            if (FlatPositionInfo.IsProxAcrossInFlatState()) {
                                isSteady = true;
                                ProximityAcrossService.this.mAcrossAnalyzer.pushData(event);
                                if (ProximityAcrossService.this.isProximityAcrossWorking) {
                                    ProximityAcrossService.this.mAcrossAnalyzer.judge(event);
                                }
                            } else {
                                ProximityAcrossService.this.mAcrossAnalyzer.reset();
                            }
                        } else if (ProximityAcrossService.this.mAcrossAnalyzer.IsProxAcrossInFlatState_isSteady(ProximityAcrossService.acc_data_x, ProximityAcrossService.acc_data_y, ProximityAcrossService.acc_data_z)) {
                            isSteady = true;
                            ProximityAcrossService.this.mAcrossAnalyzer.pushData(event);
                            if (ProximityAcrossService.this.isProximityAcrossWorking) {
                                ProximityAcrossService.this.mAcrossAnalyzer.judge(event);
                            }
                        } else {
                            ProximityAcrossService.this.mAcrossAnalyzer.reset();
                        }
                        if (!isSteady) {
                            if (ProximityAcrossService.this.mCollectDataHandler != null) {
                                Log.e(ProximityAcrossService.TAG, "0ops proximity across not triggered since not steady, gotcha");
                                ProximityAcrossService.this.mCollectDataHandler.removeMessages(6);
                                Message collectMessage = ProximityAcrossService.this.mCollectDataHandler.obtainMessage(6);
                                collectMessage.arg1 = 1;
                                ProximityAcrossService.this.mCollectDataHandler.sendMessage(collectMessage);
                                return;
                            }
                            Log.e(ProximityAcrossService.TAG, "0ops proximity across not triggered since not steady, but handler is null");
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
            Log.d(ProximityAcrossService.TAG, "get prox across data " + event.values[0]);
            if (FlatPositionInfo.IsProxAcrossInFlatState() && event.values[0] == 1.0f) {
                ProximityAcrossService.this.mAcrossAnalyzer.pushData(event);
                Log.d(ProximityAcrossService.TAG, "ready to send a msg MSG_PROX_ACROSS_DET_TRIGER");
                if (ProximityAcrossService.this.isProximityAcrossWorking) {
                    if (!ProximityAcrossService.this.callNotice() || ProximityAcrossService.this.isACC_z) {
                        i = 0;
                    }
                    if ((i ^ 1) != 0) {
                        Message msg = Message.obtain();
                        msg.what = 3;
                        synchronized (ProximityAcrossService.mObjectLock) {
                            if (ProximityAcrossService.this.mServiceHandler != null) {
                                Log.d(ProximityAcrossService.TAG, "send a msg MSG_PROX_ACROSS_DET_TRIGER");
                                ProximityAcrossService.this.mServiceHandler.sendMessage(msg);
                            }
                        }
                        ProximityAcrossService.this.mAcrossAnalyzer.reset();
                        return;
                    }
                }
                ProximityAcrossService.this.mAcrossAnalyzer.reset();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private class ProximityAcrossServiceHandler extends Handler {
        public ProximityAcrossServiceHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            boolean z = false;
            switch (msg.what) {
                case 3:
                    boolean isTriggered = false;
                    if (ProximityAcrossService.this.mContext != null) {
                        Message smsg;
                        if (((PowerManager) ProximityAcrossService.this.mContext.getSystemService("power")).isScreenOn()) {
                            Log.d(ProximityAcrossService.TAG, "MSG_PROX_ACROSS_DET_TRIGER+++");
                            smsg = Message.obtain();
                            smsg.what = 16;
                            smsg.obj = new Integer(4);
                            synchronized (ProximityAcrossService.mObjectLock) {
                                if (ProximityAcrossService.this.mCallBackHandler != null) {
                                    ProximityAcrossService.this.mCallBackHandler.sendMessage(smsg);
                                    isTriggered = true;
                                }
                            }
                            Log.d(ProximityAcrossService.TAG, "MSG_PROX_ACROSS_DET_TRIGER---");
                        } else if (System.getInt(ProximityAcrossService.this.mContext.getContentResolver(), "bbk_smart_touch_setting", 0) == 1 && ProximityAcrossService.this.callNotice()) {
                            smsg = Message.obtain();
                            smsg.what = 16;
                            smsg.obj = new Integer(4);
                            synchronized (ProximityAcrossService.mObjectLock) {
                                if (ProximityAcrossService.this.mCallBackHandler != null) {
                                    ProximityAcrossService.this.mCallBackHandler.sendMessage(smsg);
                                    isTriggered = true;
                                }
                            }
                            Log.d(ProximityAcrossService.TAG, "BBK_SMART_TOUCH_SETTING open triger");
                        } else {
                            Log.d(ProximityAcrossService.TAG, "MSG_PROX_ACROSS_DET_TRIGER, BUT SCREEN IS OFF");
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                }
                            }, 1000);
                            if (((PowerManager) ProximityAcrossService.this.mContext.getSystemService("power")).isScreenOn()) {
                                Log.d(ProximityAcrossService.TAG, "MSG_PROX_ACROSS_DET_TRIGER, AFTER DELAY SCREEN IS ON");
                                smsg = Message.obtain();
                                smsg.what = 16;
                                smsg.obj = new Integer(4);
                                synchronized (ProximityAcrossService.mObjectLock) {
                                    if (ProximityAcrossService.this.mCallBackHandler != null) {
                                        ProximityAcrossService.this.mCallBackHandler.sendMessage(smsg);
                                        isTriggered = true;
                                    }
                                }
                            } else {
                                Log.d(ProximityAcrossService.TAG, "MSG_PROX_ACROSS_DET_TRIGER, AFTER DELAY SCREEN IS STILL OFF");
                            }
                        }
                        Log.d(ProximityAcrossService.TAG, "callNotice():" + ProximityAcrossService.this.callNotice());
                        if (ProximityAcrossService.this.mCollectDataHandler != null) {
                            Log.e(ProximityAcrossService.TAG, "0ops proximity across triggered, isTriggered = " + isTriggered + ", gotcha");
                            ProximityAcrossService.this.mCollectDataHandler.removeMessages(6);
                            Message collectMessage = ProximityAcrossService.this.mCollectDataHandler.obtainMessage(6);
                            collectMessage.arg1 = isTriggered ? 0 : 3;
                            ProximityAcrossService.this.mCollectDataHandler.sendMessage(collectMessage);
                            return;
                        }
                        Log.e(ProximityAcrossService.TAG, "0ops proximity across triggered, isTriggered = " + isTriggered + ", but handler is null");
                        return;
                    }
                    return;
                case 4:
                case 5:
                    try {
                        AbsVivoProxCaliManager mVivoProxCaliManager = VivoFrameworkFactory.getFrameworkFactoryImpl().getVivoProxCaliManager();
                        if (msg.arg1 == 1) {
                            z = true;
                        }
                        mVivoProxCaliManager.changeProximityParam(z, 4);
                        return;
                    } catch (Exception e) {
                        Log.d(ProximityAcrossService.TAG, "Failed in changeProximityParam");
                        return;
                    }
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

    private boolean callNotice() {
        int CallState = 0;
        try {
            ITelephony telephonyService = Stub.asInterface(ServiceManager.getService("phone"));
            if (telephonyService != null) {
                CallState = telephonyService.getCallState();
            }
        } catch (RemoteException ex) {
            Log.w(TAG, "RemoteException from getPhoneInterface()", ex);
        }
        if (CallState == 0) {
            return false;
        }
        Log.d(TAG, "CallState:" + CallState);
        return true;
    }

    public static ProximityAcrossService getInstance() {
        return mSingleProximityAcrossService;
    }

    private ProximityAcrossService() {
        this.isProximityAcrossWorking = false;
        this.isACC_z = true;
        this.mCallBackHandler = null;
        this.mServiceHandler = null;
        this.mContext = null;
        this.mNeedUpdateWorkingState = false;
        this.isSupportAcross = false;
        this.TYPE_PROXIMITY_ACROSS = -1;
        this.mAcceleromererListener = new MotionSensorEventListener(this, null);
        this.mProximityAcrossListener = new MotionSensorEventListener(this, null);
        this.mProximityListener = new MotionSensorEventListener(this, null);
        this.mAcrossAnalyzer = new AcrossAnalyzer();
        this.mNeedUpdateWorkingState = jugdeNeedUpdateWorkingState();
    }

    public boolean startMotionRecognitionService(Context context, Handler handler) {
        Log.d(TAG, "startMotionRecognitionService ");
        if (!this.isProximityAcrossWorking) {
            this.mContext = context;
            this.isProximityAcrossWorking = true;
            this.mCallBackHandler = handler;
            this.mAcrossAnalyzer.reset();
            String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            this.mServiceHandler = new ProximityAcrossServiceHandler(handler.getLooper());
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            this.mCollectDataThread = new HandlerThread("ProximityAcrossCollectData");
            this.mCollectDataThread.start();
            this.mCollectDataHandler = new Handler(this.mCollectDataThread.getLooper()) {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 6:
                            Log.d(ProximityAcrossService.TAG, "collect data, msg.arg1 = " + msg.arg1);
                            AllConfig.collectAirOperationData(ProximityAcrossService.this.mContext, 0, msg.arg1 == 0, msg.arg1);
                            return;
                        default:
                            return;
                    }
                }
            };
            this.isSupportAcross = isSupportProximityAcross();
            Log.d(TAG, "is the project support across " + this.isSupportAcross);
            synchronized (mObjectLock) {
                if (this.mServiceHandler != null) {
                    this.mSensorManager.registerListener(this.mAcceleromererListener, this.mSensorManager.getDefaultSensor(1), 25000, this.mServiceHandler);
                    if (this.isSupportAcross) {
                        this.mSensorManager.registerListener(this.mProximityAcrossListener, this.mSensorManager.getDefaultSensor(this.TYPE_PROXIMITY_ACROSS), StateInfo.STATE_FINGERPRINT_GOTO_SLEEP, this.mServiceHandler);
                    } else {
                        this.mServiceHandler.removeMessages(4);
                        Message message = this.mServiceHandler.obtainMessage(4);
                        message.arg1 = 1;
                        this.mServiceHandler.sendMessage(message);
                        if (prop == null || !(prop.equals("PD1421") || prop.equals("PD1421L") || prop.equals("PD1421D") || prop.equals("PD1421LG4") || prop.equals("PD1421V"))) {
                            this.mSensorManager.registerListener(this.mProximityListener, this.mSensorManager.getDefaultSensor(8), 25000, this.mServiceHandler);
                        } else {
                            this.mSensorManager.registerListener(this.mProximityListener, this.mSensorManager.getDefaultSensor(8), StateInfo.STATE_FINGERPRINT_GOTO_SLEEP, this.mServiceHandler);
                        }
                    }
                }
            }
            if (this.mNeedUpdateWorkingState) {
                updateWorkingState(true);
            } else {
                Log.d(TAG, "updateWorkingState start not need update!");
            }
        }
        Message msg = Message.obtain();
        msg.what = 1;
        synchronized (mObjectLock) {
            if (this.mServiceHandler != null) {
                this.mServiceHandler.sendMessage(msg);
            }
        }
        return true;
    }

    public boolean stopMotionRecognitionService() {
        Message msg = Message.obtain();
        msg.what = 2;
        synchronized (mObjectLock) {
            if (this.mServiceHandler != null) {
                this.mServiceHandler.sendMessage(msg);
            }
        }
        Log.d(TAG, "stopMotionRecognitionService " + this.isProximityAcrossWorking);
        if (this.isProximityAcrossWorking) {
            this.isProximityAcrossWorking = false;
            if (!(this.mServiceHandler == null || this.mSensorManager == null || this.isSupportAcross)) {
                this.mServiceHandler.removeMessages(5);
                Message message = this.mServiceHandler.obtainMessage(5);
                message.arg1 = 0;
                this.mServiceHandler.sendMessage(message);
            }
            if (this.mCollectDataHandler != null) {
                this.mCollectDataHandler.removeMessages(6);
            }
            if (this.mCollectDataThread != null) {
                this.mCollectDataThread.quit();
            }
            this.mCallBackHandler = null;
            this.mServiceHandler = null;
            this.mCollectDataHandler = null;
            this.mCollectDataThread = null;
            synchronized (mObjectLock) {
                if (this.mSensorManager != null) {
                    this.mSensorManager.unregisterListener(this.mAcceleromererListener);
                }
                if (this.isSupportAcross) {
                    this.mSensorManager.unregisterListener(this.mProximityAcrossListener);
                } else {
                    this.mSensorManager.unregisterListener(this.mProximityListener);
                }
                this.mSensorManager = null;
            }
            if (this.mNeedUpdateWorkingState) {
                updateWorkingState(false);
            } else {
                Log.d(TAG, "updateWorkingState stop not need update!");
            }
        }
        return true;
    }

    private static boolean jugdeNeedUpdateWorkingState() {
        String[] mNeedUpdateWorkingStateProjects = new String[]{"PD1421", "PD1421L", "PD1421LG4", "PD1421V"};
        String model = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, "unkown");
        Log.d(TAG, "mNeedUpdateWorkingStateProjects=" + (mNeedUpdateWorkingStateProjects == null ? "null" : "not_Null length:" + mNeedUpdateWorkingStateProjects.length));
        if (mNeedUpdateWorkingStateProjects == null) {
            return false;
        }
        for (int i = 0; i < mNeedUpdateWorkingStateProjects.length; i++) {
            Log.d(TAG, "model:" + model + " i=" + i + mNeedUpdateWorkingStateProjects[i]);
            if (model.equals(mNeedUpdateWorkingStateProjects[i])) {
                return true;
            }
        }
        return false;
    }

    private void updateWorkingState(boolean working) {
        Log.d(TAG, "updateWorkingState " + working);
        if (working) {
            writeFile(PROX_ACROSS_PATH, "1");
        } else {
            writeFile(PROX_ACROSS_PATH, "0");
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

    private boolean isSupportProximityAcross() {
        if (this.mSensorManager != null) {
            for (Sensor i : this.mSensorManager.getSensorList(-1)) {
                if (i.getName().equals("BBK-proxacross")) {
                    this.TYPE_PROXIMITY_ACROSS = i.getType();
                    return true;
                }
            }
        }
        return false;
    }
}
