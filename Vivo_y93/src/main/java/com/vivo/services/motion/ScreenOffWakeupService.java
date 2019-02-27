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
import android.os.PowerManager.WakeLock;
import android.os.SystemProperties;
import android.util.Log;
import com.sensoroperate.SensorTestResult;
import com.sensoroperate.VivoSensorTest;
import com.vivo.common.autobrightness.AblConfig;
import com.vivo.common.autobrightness.StateInfo;
import com.vivo.common.provider.Calendar.Events;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import vivo.app.VivoFrameworkFactory;
import vivo.app.proxcali.AbsVivoProxCaliManager;

public final class ScreenOffWakeupService implements IMotionRecognitionService {
    private static final int AIROPERATION_TYPE_SCREEN_OFF_WAKEUP = 1;
    private static final int DATA_COUNT = 6;
    private static boolean DBG = true;
    private static final String HALL_STATE_PATH = "/sys/class/switch/hall/state";
    private static final int MSG_COLLECT_DATA = 9;
    private static final int MSG_PROXIMITY_PARAM_DISABLE = 8;
    private static final int MSG_PROXIMITY_PARAM_ENABLE = 7;
    private static final int MSG_SCREEN_OFF_WAKEUP_DET_START = 1;
    private static final int MSG_SCREEN_OFF_WAKEUP_DET_STOP = 2;
    private static final int MSG_SCREEN_OFF_WAKEUP_DET_TRIGER = 3;
    private static final int MSG_SCREEN_OFF_WAKEUP_Get_AccData = 5;
    private static final int MSG_SCREEN_OFF_WAKEUP_STOP_ACC_DET = 4;
    private static final int MSG_SCREEN_OFF_WAKEUP_TEM_RESET_DET = 6;
    private static String MTK_PLATFORM = "MTK";
    private static String PLATFORM_TAG = "ro.vivo.product.solution";
    private static String QCOM_PLATFORM = "QCOM";
    private static final String TAG = "ScreenOffWakeupService";
    private static final int TRIGGERED = 0;
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final int UNTRIGGERED_FOR_NEAR_TO_FAR_TOO_LONG = 2;
    private static final int UNTRIGGERED_FOR_NOT_IN_RIGHT_GESTURE = 1;
    private static int datacount = 0;
    private static final Object mObjectLock = new Object();
    private static ScreenOffWakeupService mSingleScreenOffWakeupService = new ScreenOffWakeupService();
    private static int newState = 1;
    private static int registerstate = 0;
    private Handler getAccDataHandler = null;
    private boolean isAuthWorking = false;
    private boolean isScreenOffWakeupWorking = false;
    private WakeLock mAWakeLock = null;
    private MotionSensorEventListener mAcceleromererListener = new MotionSensorEventListener(this, null);
    private WakeLock mBWakeLock = null;
    private Handler mCallBackHandler = null;
    private Handler mCollectDataHandler;
    private HandlerThread mCollectDataThread;
    private Context mContext = null;
    private SensorData[] mData;
    private ProximityAnalyzer mProximityAnalyzer = new ProximityAnalyzer();
    private MotionSensorEventListener mProximityListener = new MotionSensorEventListener(this, null);
    private Sensor mProximitySensor;
    private ScreenOffWakeupAnalyzer mScreenOffWakeupAnalyzer = new ScreenOffWakeupAnalyzer();
    private SensorManager mSensorManager;
    private Handler mServiceHandler = null;
    private VivoSensorTest mVivoSensorTest = null;
    private PowerManager pm = null;

    private class MotionSensorEventListener implements SensorEventListener {
        /* synthetic */ MotionSensorEventListener(ScreenOffWakeupService this$0, MotionSensorEventListener -this1) {
            this();
        }

        private MotionSensorEventListener() {
        }

        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case 1:
                    if (ScreenOffWakeupService.registerstate == 1) {
                        ScreenOffWakeupService.datacount = ScreenOffWakeupService.datacount + 1;
                        if (ScreenOffWakeupService.datacount > 2) {
                            ScreenOffWakeupService.this.mScreenOffWakeupAnalyzer.judge(event);
                        }
                        if (ScreenOffWakeupService.datacount > 6) {
                            if (ScreenOffWakeupService.this.mScreenOffWakeupAnalyzer.authStep != 2) {
                                if (ScreenOffWakeupService.this.mCollectDataHandler != null) {
                                    Log.e(ScreenOffWakeupService.TAG, "0ops screen off wakeup not triggered since not in right gesture, gotcha");
                                    ScreenOffWakeupService.this.mCollectDataHandler.removeMessages(9);
                                    Message collectMessage = ScreenOffWakeupService.this.mCollectDataHandler.obtainMessage(9);
                                    collectMessage.arg1 = 1;
                                    ScreenOffWakeupService.this.mCollectDataHandler.sendMessage(collectMessage);
                                } else {
                                    Log.e(ScreenOffWakeupService.TAG, "0ops screen off wakeup not triggered since not in right gesture, but handler is null");
                                }
                            }
                            synchronized (ScreenOffWakeupService.mObjectLock) {
                                if (ScreenOffWakeupService.this.mServiceHandler != null) {
                                    ScreenOffWakeupService.this.mServiceHandler.sendEmptyMessageDelayed(4, 0);
                                }
                            }
                            return;
                        }
                        return;
                    }
                    return;
                case 8:
                case StateInfo.STATE_BIT_LIGHT /*32*/:
                    ScreenOffWakeupService.this.mProximityAnalyzer.judge(event);
                    return;
                default:
                    return;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private class ProximityAnalyzer {
        private int last_prox = -1;
        private int last_prox1 = -1;
        private int last_prox2 = -1;
        private int last_prox3 = -1;
        private int last_prox4 = -1;
        private int logic_value = -1;
        private long mDownTime = 0;
        private long mDownTime1 = 0;
        private long mStartTime = 0;
        private long mUpTime = 0;
        private long mUpTime1 = 0;
        private int proximityStep = 1;

        public void reset() {
            this.last_prox = -1;
            this.last_prox1 = -1;
            this.last_prox2 = -1;
            this.last_prox3 = -1;
            this.last_prox4 = -1;
            this.logic_value = -1;
            this.proximityStep = 1;
            this.mUpTime = 0;
            this.mUpTime1 = 0;
            this.mDownTime = 0;
            this.mDownTime1 = 0;
            this.mStartTime = 0;
        }

        public void temreset() {
            this.last_prox = 1;
            this.proximityStep = 1;
            this.mUpTime = 0;
            this.mUpTime1 = 0;
            this.mDownTime = 0;
            this.mDownTime1 = 0;
        }

        private void judge(SensorEvent event) {
            if (event.sensor.getType() == 32 || event.sensor.getType() == 8) {
                float mProximityThreshold = 0.0f;
                if (ScreenOffWakeupService.this.mSensorManager == null && ScreenOffWakeupService.this.mContext != null) {
                    ScreenOffWakeupService.this.mSensorManager = (SensorManager) ScreenOffWakeupService.this.mContext.getSystemService("sensor");
                }
                if (ScreenOffWakeupService.this.mSensorManager != null) {
                    ScreenOffWakeupService.this.mProximitySensor = ScreenOffWakeupService.this.mSensorManager.getDefaultSensor(8);
                }
                if (ScreenOffWakeupService.this.mProximitySensor != null) {
                    mProximityThreshold = Math.min(ScreenOffWakeupService.this.mProximitySensor.getMaximumRange(), ScreenOffWakeupService.TYPICAL_PROXIMITY_THRESHOLD);
                }
                int i = (event.values[0] < 0.0f || event.values[0] >= mProximityThreshold) ? 1 : 0;
                this.logic_value = i;
                FileReader file;
                try {
                    char[] buffer = new char[1024];
                    file = new FileReader(ScreenOffWakeupService.HALL_STATE_PATH);
                    ScreenOffWakeupService.newState = Integer.valueOf(new String(buffer, 0, file.read(buffer, 0, 1024)).trim()).intValue();
                    file.close();
                } catch (FileNotFoundException e) {
                    Log.w(ScreenOffWakeupService.TAG, "This kernel does not have hall support");
                } catch (Exception e2) {
                    Log.e(ScreenOffWakeupService.TAG, Events.DEFAULT_SORT_ORDER, e2);
                } catch (Throwable th) {
                    file.close();
                }
                Log.d(ScreenOffWakeupService.TAG, "newState:" + ScreenOffWakeupService.newState);
                if (ScreenOffWakeupService.newState == 0) {
                    this.logic_value = 0;
                    ScreenOffWakeupService.newState = 1;
                }
                if (this.logic_value != this.last_prox) {
                    Log.d("screenoff", "logic_value:" + this.logic_value + "last_prox:" + this.last_prox + "last_prox1:" + this.last_prox1);
                }
                switch (this.proximityStep) {
                    case 1:
                        if (this.logic_value == 0 && this.last_prox == 1) {
                            this.mDownTime = event.timestamp / 1000000;
                            this.proximityStep = 2;
                            break;
                        }
                    case 2:
                        Message collectMessage;
                        boolean isProximityFulfilled = false;
                        if (this.logic_value == 1) {
                            this.mUpTime = event.timestamp / 1000000;
                            Log.d(ScreenOffWakeupService.TAG, "mUpTime:" + this.mUpTime + "mUpTime-mDownTime:" + (this.mUpTime - this.mDownTime) + "proximityStep:" + this.proximityStep);
                            if (this.mUpTime - this.mDownTime < 1000) {
                                this.proximityStep = 3;
                                isProximityFulfilled = true;
                            } else {
                                this.proximityStep = 1;
                                Log.d(ScreenOffWakeupService.TAG, "down time too long, return.");
                                this.last_prox1 = this.last_prox;
                                this.last_prox = this.logic_value;
                                this.mStartTime = event.timestamp / 1000000;
                                if (ScreenOffWakeupService.this.mCollectDataHandler != null) {
                                    Log.e(ScreenOffWakeupService.TAG, "0ops proximity across not triggered since down time too long, gotcha");
                                    ScreenOffWakeupService.this.mCollectDataHandler.removeMessages(9);
                                    collectMessage = ScreenOffWakeupService.this.mCollectDataHandler.obtainMessage(9);
                                    collectMessage.arg1 = 2;
                                    ScreenOffWakeupService.this.mCollectDataHandler.sendMessage(collectMessage);
                                } else {
                                    Log.e(ScreenOffWakeupService.TAG, "0ops proximity across not triggered since down time too long, but handler is null");
                                }
                                return;
                            }
                        } else if (this.logic_value == 0) {
                            this.mDownTime1 = event.timestamp / 1000000;
                            if (this.mDownTime1 - this.mDownTime > 1000) {
                                this.proximityStep = 1;
                                Log.d(ScreenOffWakeupService.TAG, "down time too long, return.");
                            }
                        }
                        if (!isProximityFulfilled) {
                            if (ScreenOffWakeupService.this.mCollectDataHandler == null) {
                                Log.e(ScreenOffWakeupService.TAG, "0ops proximity across not triggered since down time too long, but handler is null");
                                break;
                            }
                            Log.e(ScreenOffWakeupService.TAG, "0ops proximity across not triggered since down time too long, gotcha");
                            ScreenOffWakeupService.this.mCollectDataHandler.removeMessages(9);
                            collectMessage = ScreenOffWakeupService.this.mCollectDataHandler.obtainMessage(9);
                            collectMessage.arg1 = 2;
                            ScreenOffWakeupService.this.mCollectDataHandler.sendMessage(collectMessage);
                            break;
                        }
                        break;
                    case 3:
                        break;
                }
                if (!ScreenOffWakeupService.this.isAuthWorking && this.proximityStep == 3) {
                    if (!(ScreenOffWakeupService.this.mAWakeLock == null || (ScreenOffWakeupService.this.mAWakeLock.isHeld() ^ 1) == 0)) {
                        Log.d(ScreenOffWakeupService.TAG, "mAWakeLock acquire");
                        ScreenOffWakeupService.this.mAWakeLock.acquire(500);
                    }
                    ScreenOffWakeupService.this.isAuthWorking = true;
                    Log.d(ScreenOffWakeupService.TAG, "registerListener mAcceleromererListener");
                    if (AllConfig.mScreenOffWakeupDeviceNode) {
                        Message.obtain().what = 5;
                        synchronized (ScreenOffWakeupService.mObjectLock) {
                            if (ScreenOffWakeupService.this.getAccDataHandler != null) {
                                Log.d(ScreenOffWakeupService.TAG, "MSG_SCREEN_OFF_WAKEUP_Get_AccData,3");
                                for (int i2 = 0; i2 < 4; i2++) {
                                    ScreenOffWakeupService.this.getAccDataHandler.sendEmptyMessageDelayed(5, (long) ((i2 + 1) * 100));
                                }
                            }
                        }
                        synchronized (ScreenOffWakeupService.mObjectLock) {
                            if (ScreenOffWakeupService.this.mServiceHandler != null) {
                                Log.d(ScreenOffWakeupService.TAG, "MSG_SCREEN_OFF_WAKEUP_STOP_ACC_DET,500");
                                ScreenOffWakeupService.this.mServiceHandler.sendEmptyMessageDelayed(4, 450);
                            }
                        }
                    } else if (ScreenOffWakeupService.this.mSensorManager != null && ScreenOffWakeupService.registerstate == 0) {
                        ScreenOffWakeupService.this.mSensorManager.registerListener(ScreenOffWakeupService.this.mAcceleromererListener, ScreenOffWakeupService.this.mSensorManager.getDefaultSensor(1), 25000, ScreenOffWakeupService.this.mServiceHandler);
                        ScreenOffWakeupService.datacount = 0;
                        ScreenOffWakeupService.registerstate = 1;
                    }
                    this.proximityStep = 4;
                }
                this.last_prox4 = this.last_prox3;
                this.last_prox3 = this.last_prox2;
                this.last_prox2 = this.last_prox1;
                this.last_prox1 = this.last_prox;
                this.last_prox = this.logic_value;
                this.mStartTime = event.timestamp / 1000000;
            }
        }
    }

    private class ScreenOffWakeupAnalyzer {
        private int authCnt;
        public int authStep;
        private int mFinalStateCnt = 0;

        public void reset() {
            this.mFinalStateCnt = 0;
            this.authCnt = 0;
            this.authStep = 0;
            ScreenOffWakeupService.this.isAuthWorking = false;
        }

        private void judge() {
            float x = 0.0f;
            float y = 0.0f;
            float z = 0.0f;
            float[] mAccSensorVal = new float[3];
            float[] DefBase_digit = new float[3];
            float[] MinBase_digit = new float[3];
            float[] MaxBase_digit = new float[3];
            String data = null;
            String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            if (AllConfig.mIsArchADSP) {
                SensorTestResult vivo_result = new SensorTestResult();
                int[] arg = new int[]{1};
                if (ScreenOffWakeupService.this.mVivoSensorTest != null && ScreenOffWakeupService.this.mVivoSensorTest.vivoSensorTest(49, vivo_result, arg, 1) == 0) {
                    data = "1";
                    vivo_result.getAllTestResult(mAccSensorVal, DefBase_digit, MinBase_digit, MaxBase_digit);
                }
                if (data != null) {
                    x = mAccSensorVal[0] / 100.0f;
                    y = mAccSensorVal[1] / 100.0f;
                    z = mAccSensorVal[2] / 100.0f;
                }
            } else {
                String PLATFORM_INFO = SystemProperties.get(ScreenOffWakeupService.PLATFORM_TAG);
                try {
                    if (!AllConfig.mIsArchADSP) {
                        data = ScreenOffWakeupService.readFileByline("/sys/bus/platform/drivers/gsensor/data");
                    }
                } catch (Exception e) {
                    Log.d(ScreenOffWakeupService.TAG, "readFileByline:" + e.getMessage());
                }
                if (data != null) {
                    String[] out = data.split(" ");
                    if (AllConfig.mIsArchADSP) {
                        if (out != null) {
                            try {
                                x = Float.parseFloat(out[0].trim()) / 100000.0f;
                                y = Float.parseFloat(out[1].trim()) / 100000.0f;
                                z = Float.parseFloat(out[2].trim()) / 100000.0f;
                            } catch (Exception e2) {
                                Log.d(ScreenOffWakeupService.TAG, "data error");
                            }
                        } else {
                            Log.d(ScreenOffWakeupService.TAG, "out null");
                        }
                    } else if (out != null) {
                        try {
                            x = Float.parseFloat(out[0].trim()) / 1000.0f;
                            y = Float.parseFloat(out[1].trim()) / 1000.0f;
                            z = Float.parseFloat(out[2].trim()) / 1000.0f;
                        } catch (Exception e3) {
                            Log.d(ScreenOffWakeupService.TAG, "data error");
                        }
                    } else {
                        Log.d(ScreenOffWakeupService.TAG, "out null");
                    }
                }
            }
            Log.d(ScreenOffWakeupService.TAG, "x:" + x + "y:" + y + "z:" + z);
            switch (this.authStep) {
                case 0:
                    if (Math.abs(x) >= 4.0f || Math.abs(y) >= 4.0f || Math.abs(z) <= 8.0f) {
                        this.authCnt = 0;
                    } else {
                        this.authCnt++;
                    }
                    Log.d(ScreenOffWakeupService.TAG, "==============authCnt:" + this.authCnt);
                    if (this.authCnt >= 2) {
                        this.authStep = 1;
                        this.authCnt = 0;
                        break;
                    }
                    break;
                case 1:
                    if (ScreenOffWakeupService.this.mServiceHandler != null) {
                        if (!(ScreenOffWakeupService.this.mBWakeLock == null || (ScreenOffWakeupService.this.mBWakeLock.isHeld() ^ 1) == 0)) {
                            Log.d(ScreenOffWakeupService.TAG, "mBWakeLock acquire");
                            ScreenOffWakeupService.this.mBWakeLock.acquire(3000);
                        }
                        if (ScreenOffWakeupService.this.mSensorManager != null && ScreenOffWakeupService.registerstate == 1) {
                            ScreenOffWakeupService.this.mSensorManager.unregisterListener(ScreenOffWakeupService.this.mAcceleromererListener);
                            ScreenOffWakeupService.registerstate = 0;
                        }
                        Log.d(ScreenOffWakeupService.TAG, "isScreenOffWakeupWorking=====1:" + ScreenOffWakeupService.this.isScreenOffWakeupWorking);
                        synchronized (ScreenOffWakeupService.mObjectLock) {
                            if (ScreenOffWakeupService.this.mServiceHandler == null || !ScreenOffWakeupService.this.isScreenOffWakeupWorking) {
                                Log.d(ScreenOffWakeupService.TAG, "mServiceHandler is NULL");
                            } else {
                                Log.d(ScreenOffWakeupService.TAG, "mServiceHandler can  TRIGER");
                                ScreenOffWakeupService.this.mServiceHandler.sendEmptyMessage(3);
                            }
                        }
                    }
                    this.authStep = 2;
                    break;
            }
            Log.d(ScreenOffWakeupService.TAG, "authStep" + this.authStep + ",cnt:" + this.authCnt);
        }

        private void judge(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            Log.d(ScreenOffWakeupService.TAG, "x:" + x + "y:" + y + "z:" + z);
            switch (this.authStep) {
                case 0:
                    if (Math.abs(x) >= 4.0f || Math.abs(y) >= 4.0f || Math.abs(z) <= 8.0f) {
                        this.authCnt = 0;
                    } else {
                        this.authCnt++;
                    }
                    Log.d(ScreenOffWakeupService.TAG, "authCnt:" + this.authCnt);
                    if (this.authCnt >= 2) {
                        this.authStep = 1;
                        this.authCnt = 0;
                        break;
                    }
                    break;
                case 1:
                    if (ScreenOffWakeupService.this.mServiceHandler != null) {
                        if (!(ScreenOffWakeupService.this.mBWakeLock == null || (ScreenOffWakeupService.this.mBWakeLock.isHeld() ^ 1) == 0)) {
                            Log.d(ScreenOffWakeupService.TAG, "mBWakeLock acquire");
                            ScreenOffWakeupService.this.mBWakeLock.acquire(3000);
                        }
                        if (ScreenOffWakeupService.this.mSensorManager != null && ScreenOffWakeupService.registerstate == 1) {
                            ScreenOffWakeupService.this.mSensorManager.unregisterListener(ScreenOffWakeupService.this.mAcceleromererListener);
                            ScreenOffWakeupService.registerstate = 0;
                        }
                        Log.d(ScreenOffWakeupService.TAG, "isScreenOffWakeupWorking:" + ScreenOffWakeupService.this.isScreenOffWakeupWorking);
                        synchronized (ScreenOffWakeupService.mObjectLock) {
                            if (ScreenOffWakeupService.this.mServiceHandler == null || !ScreenOffWakeupService.this.isScreenOffWakeupWorking) {
                                Log.d(ScreenOffWakeupService.TAG, "mServiceHandler is NULL");
                            } else {
                                Log.d(ScreenOffWakeupService.TAG, "mServiceHandler can  TRIGER");
                                ScreenOffWakeupService.this.mServiceHandler.sendEmptyMessage(3);
                            }
                        }
                    }
                    this.authStep = 2;
                    break;
            }
            Log.d(ScreenOffWakeupService.TAG, "authStep" + this.authStep + ",cnt:" + this.authCnt);
        }
    }

    private class ScreenOffWakeupServiceHandler extends Handler {
        public ScreenOffWakeupServiceHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case 1:
                    Log.d(ScreenOffWakeupService.TAG, "MSG_SCREEN_OFF_WAKEUP_DET_START");
                    return;
                case 2:
                    Log.d(ScreenOffWakeupService.TAG, "MSG_SCREEN_OFF_WAKEUP_DET_STOP");
                    return;
                case 3:
                    Message smsg = Message.obtain();
                    smsg.what = 16;
                    smsg.obj = new Integer(8);
                    if (ScreenOffWakeupService.this.mCallBackHandler != null) {
                        ScreenOffWakeupService.this.mCallBackHandler.sendMessage(smsg);
                        if (ScreenOffWakeupService.this.mCollectDataHandler != null) {
                            Log.e(ScreenOffWakeupService.TAG, "0ops screen off wakeup triggered, gotcha");
                            ScreenOffWakeupService.this.mCollectDataHandler.removeMessages(9);
                            Message collectMessage = ScreenOffWakeupService.this.mCollectDataHandler.obtainMessage(9);
                            collectMessage.arg1 = 0;
                            ScreenOffWakeupService.this.mCollectDataHandler.sendMessage(collectMessage);
                        } else {
                            Log.e(ScreenOffWakeupService.TAG, "0ops screen off wakeup triggered, but handler is null");
                        }
                    }
                    Log.d(ScreenOffWakeupService.TAG, "isScreenOffWakeupWorking=====2:" + ScreenOffWakeupService.this.isScreenOffWakeupWorking);
                    synchronized (ScreenOffWakeupService.mObjectLock) {
                        if (ScreenOffWakeupService.this.mServiceHandler != null && ScreenOffWakeupService.this.isScreenOffWakeupWorking) {
                            Log.d(ScreenOffWakeupService.TAG, "MSG_SCREEN_OFF_WAKEUP_DET_TRIGER");
                            if (ScreenOffWakeupService.this.mServiceHandler != null) {
                                ScreenOffWakeupService.this.mServiceHandler.removeMessages(4);
                                ScreenOffWakeupService.this.mServiceHandler.sendEmptyMessage(4);
                            }
                        }
                    }
                    Log.d(ScreenOffWakeupService.TAG, "MSG_SCREEN_OFF_WAKEUP_DET_TRIGER");
                    return;
                case 4:
                    if (ScreenOffWakeupService.this.mSensorManager != null && ScreenOffWakeupService.registerstate == 1) {
                        ScreenOffWakeupService.this.mSensorManager.unregisterListener(ScreenOffWakeupService.this.mAcceleromererListener);
                        ScreenOffWakeupService.registerstate = 0;
                    }
                    ScreenOffWakeupService.this.mScreenOffWakeupAnalyzer.reset();
                    ScreenOffWakeupService.this.mProximityAnalyzer.reset();
                    Log.d(ScreenOffWakeupService.TAG, "MSG_SCREEN_OFF_WAKEUP_STOP_ACC_DET");
                    return;
                case 5:
                    ScreenOffWakeupService.this.mScreenOffWakeupAnalyzer.judge();
                    return;
                case 6:
                    ScreenOffWakeupService.this.mProximityAnalyzer.temreset();
                    return;
                case 7:
                case 8:
                    try {
                        AbsVivoProxCaliManager mVivoProxCaliManager = VivoFrameworkFactory.getFrameworkFactoryImpl().getVivoProxCaliManager();
                        if (msg.arg1 != 1) {
                            z = false;
                        }
                        mVivoProxCaliManager.changeProximityParam(z, 8);
                        return;
                    } catch (Exception e) {
                        Log.d(ScreenOffWakeupService.TAG, "Failed in changeProximityParam");
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

    public static boolean isProximitySupported(Context context) {
        if (((SensorManager) context.getSystemService("sensor")).getSensorList(8).size() > 0) {
            return true;
        }
        return false;
    }

    public static boolean isProximityRawSupported(Context context) {
        if (((SensorManager) context.getSystemService("sensor")).getSensorList(32).size() > 0) {
            return true;
        }
        return false;
    }

    public static ScreenOffWakeupService getInstance() {
        return mSingleScreenOffWakeupService;
    }

    private ScreenOffWakeupService() {
    }

    public boolean startMotionRecognitionService(Context context, Handler handler) {
        Log.d(TAG, "startMotionRecognitionService: " + this.isScreenOffWakeupWorking);
        if (!this.isScreenOffWakeupWorking) {
            this.mContext = context;
            registerstate = 0;
            this.isScreenOffWakeupWorking = true;
            this.mCallBackHandler = handler;
            this.mScreenOffWakeupAnalyzer.reset();
            this.mProximityAnalyzer.reset();
            this.mServiceHandler = new ScreenOffWakeupServiceHandler(handler.getLooper());
            this.getAccDataHandler = new ScreenOffWakeupServiceHandler(handler.getLooper());
            this.mCollectDataThread = new HandlerThread("ScreenOffWakeupCollectData");
            this.mCollectDataThread.start();
            this.mCollectDataHandler = new Handler(this.mCollectDataThread.getLooper()) {
                public void handleMessage(Message msg) {
                    boolean z = false;
                    switch (msg.what) {
                        case 9:
                            Log.d(ScreenOffWakeupService.TAG, "collect data, msg.arg1 = " + msg.arg1);
                            Context -get10 = ScreenOffWakeupService.this.mContext;
                            if (msg.arg1 == 0) {
                                z = true;
                            }
                            AllConfig.collectAirOperationData(-get10, 1, z, msg.arg1);
                            return;
                        default:
                            return;
                    }
                }
            };
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            this.pm = (PowerManager) this.mContext.getSystemService("power");
            this.mAWakeLock = this.pm.newWakeLock(1, TAG);
            this.mBWakeLock = this.pm.newWakeLock(1, TAG);
            if (this.mSensorManager != null) {
                if (isProximitySupported(this.mContext)) {
                    this.mServiceHandler.removeMessages(7);
                    Message message = this.mServiceHandler.obtainMessage(7);
                    message.arg1 = 1;
                    this.mServiceHandler.sendMessage(message);
                    this.mSensorManager.registerListener(this.mProximityListener, this.mSensorManager.getDefaultSensor(8), 100000);
                    Log.d(TAG, "registerListener TYPE_PROXIMITY");
                } else if (isProximityRawSupported(this.mContext)) {
                    this.mSensorManager.registerListener(this.mProximityListener, this.mSensorManager.getDefaultSensor(32), 100000);
                    Log.d(TAG, "registerListener TYPE_PROXIMITY_RAW");
                } else {
                    Log.d(TAG, "no proximity type support!");
                    return false;
                }
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
        Log.d(TAG, "stopMotionRecognitionService " + this.isScreenOffWakeupWorking);
        if (this.isScreenOffWakeupWorking) {
            this.isScreenOffWakeupWorking = false;
            synchronized (mObjectLock) {
                if (this.mServiceHandler != null) {
                    this.mServiceHandler.removeMessages(4);
                }
                if (!(this.mSensorManager == null || this.mServiceHandler == null)) {
                    this.mServiceHandler.removeMessages(8);
                    Message message = this.mServiceHandler.obtainMessage(8);
                    message.arg1 = 0;
                    this.mServiceHandler.sendMessage(message);
                }
            }
            if (this.mCollectDataHandler != null) {
                this.mCollectDataHandler.removeMessages(9);
            }
            if (this.mCollectDataThread != null) {
                this.mCollectDataThread.quit();
            }
            this.mCallBackHandler = null;
            this.mServiceHandler = null;
            this.mCollectDataHandler = null;
            this.mCollectDataThread = null;
            synchronized (mObjectLock) {
                if (this.getAccDataHandler != null) {
                    this.getAccDataHandler.removeMessages(5);
                }
            }
            synchronized (mObjectLock) {
                this.getAccDataHandler = null;
            }
            if (this.mSensorManager != null && registerstate == 1) {
                this.mSensorManager.unregisterListener(this.mAcceleromererListener);
                registerstate = 0;
            }
            if (this.mSensorManager != null) {
                this.mSensorManager.unregisterListener(this.mProximityListener);
            }
            this.mSensorManager = null;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0084 A:{SYNTHETIC, Splitter: B:22:0x0084} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String readFileByline(String fileName) {
        Exception e;
        Throwable th;
        BufferedReader reader = null;
        String tempString = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(fileName)));
            try {
                tempString = reader2.readLine();
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e1) {
                        Log.d("EngineerMode", "the readFileByline is:" + e1.getMessage());
                    }
                }
                reader = reader2;
            } catch (Exception e2) {
                e = e2;
                reader = reader2;
            } catch (Throwable th2) {
                th = th2;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            try {
                Log.d("EngineerMode", "the readFileByline is:" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e12) {
                        Log.d("EngineerMode", "the readFileByline is:" + e12.getMessage());
                    }
                }
                return tempString;
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Log.d("EngineerMode", "the readFileByline is:" + e122.getMessage());
                    }
                }
                throw th;
            }
        }
        return tempString;
    }
}
