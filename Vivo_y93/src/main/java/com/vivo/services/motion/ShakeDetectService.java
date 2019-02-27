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
import com.vivo.common.autobrightness.AblConfig;

public final class ShakeDetectService implements IMotionRecognitionService {
    private static final int DATA_NUM = 40;
    private static boolean DBG = true;
    private static final int MM_NUM = 1;
    private static final float MM_THRESHOLD = 20.0f;
    private static final int MSG_SHAKE_DET_START = 1;
    private static final int MSG_SHAKE_DET_STOP = 3;
    private static final int MSG_SHAKE_DET_SWITCH = 2;
    private static final int MSG_SHAKE_DET_TRIGER = 4;
    private static final int STATIC_NUM = 60;
    private static final float STATIC_THRESHOLD = 5.0f;
    private static final String TAG = "ShakeDetectService";
    private static final float THRESHOLD = 8.0f;
    private static ShakeDetectService mSingleShakeDetectService = new ShakeDetectService();
    private long accDataTime;
    private long accTime;
    private boolean enableMode_0;
    private boolean enableMode_1;
    private boolean flag;
    private boolean flagX;
    private boolean isShakeModeWorking;
    private boolean judgeflag;
    private float lastXvalue;
    private SensorEventListener mAccelerometerListener;
    private SensorEventListener mAccelerometerListener1;
    private Handler mCallBackHandler;
    private Context mContext;
    private SensorData[] mData;
    private SensorManager mSensorManager;
    private Handler mServiceHandler;
    private ShakeModeAnalyzer mShakeModeAnalyzer;
    private boolean numflag;
    private float presentXvalue;
    private int snum;
    private int staticnum;
    private boolean staticstate;
    private boolean tileflag;
    private int tiltnum0;
    private int tiltnum1;
    private int timeCount;
    private boolean timeMode;

    private class SensorData {
        public long timestamp;
        public final float[] values;

        SensorData(int size) {
            this.values = new float[size];
        }
    }

    private class ShakeDetectServiceHandler extends Handler {
        public ShakeDetectServiceHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    Log.d(ShakeDetectService.TAG, "MSG_SHAKE_DET_SWITCH");
                    if (!ShakeDetectService.this.enableMode_0) {
                        ShakeDetectService.this.enableAccelerometer(true, 0);
                        return;
                    }
                    return;
                case 3:
                    Log.d(ShakeDetectService.TAG, "MSG_SHAKE_DET_STOP");
                    if (ShakeDetectService.this.enableMode_0) {
                        ShakeDetectService.this.enableAccelerometer(false, 0);
                    }
                    if (ShakeDetectService.this.enableMode_1) {
                        ShakeDetectService.this.enableAccelerometer(false, 1);
                        return;
                    }
                    return;
                case 4:
                    Message smsg = Message.obtain();
                    smsg.what = 16;
                    smsg.obj = new Integer(7);
                    if (ShakeDetectService.this.mCallBackHandler != null) {
                        ShakeDetectService.this.mCallBackHandler.sendMessage(smsg);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class ShakeModeAnalyzer {
        private String MTK_PLATFORM = "MTK";
        private String PLATFORM_TAG = "ro.vivo.product.solution";
        private String QCOM_PLATFORM = "QCOM";
        private float befMaxData;
        private float befMinData;
        public int dataNum;
        private float firMaxData;
        private float firMinData;
        private int firNumMax = 0;
        private int firNumMin = 0;
        private float maxData;
        private float minData;
        private float minMaxData;
        private float minMinData;
        private int mmNum;
        private float mmThreshold = 0.0f;
        private int num1 = 0;
        private int num2 = 0;
        private int nummax = 0;
        private int nummin = 0;
        private Object o = new Object();
        private float secMaxData;
        private float secMinData;
        private int secNumMax = 0;
        private int secNumMin = 0;
        private final float staticThreshold;
        private int value = -1;
        public boolean wavestate = false;
        public float[] xData;

        public ShakeModeAnalyzer(int datanum, int mmnum, float mmthreshold, float staticthreshold) {
            String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            String PLATFORM_INFO = SystemProperties.get(this.PLATFORM_TAG);
            if (AllConfig.mIsArchADSP) {
                this.mmThreshold = mmthreshold + 12.0f;
            } else {
                this.mmThreshold = mmthreshold + 12.0f;
            }
            this.xData = new float[datanum];
            this.dataNum = datanum;
            this.mmNum = mmnum;
            this.staticThreshold = staticthreshold;
        }

        public void reset() {
            for (int i = 0; i < this.dataNum; i++) {
                this.xData[i] = 0.0f;
            }
            ShakeDetectService.this.timeCount = 0;
        }

        public void eat(float xvalue) {
            for (int i = this.dataNum - 1; i > 0; i--) {
                this.xData[i] = this.xData[i - 1];
            }
            this.xData[0] = xvalue;
            if (this.xData[this.dataNum - 1] != 0.0f) {
                wavejudge(this.xData);
            }
        }

        public void wavejudge(float[] mData) {
            int i;
            this.wavestate = false;
            this.minData = mData[mData.length - 1];
            this.maxData = mData[mData.length - 1];
            this.nummin = mData.length - 1;
            this.nummax = mData.length - 1;
            this.firMinData = mData[mData.length - 1];
            this.firMaxData = mData[mData.length - 1];
            this.secMinData = mData[mData.length - 1];
            this.secMaxData = mData[mData.length - 1];
            this.befMinData = mData[mData.length - 1];
            this.befMaxData = mData[mData.length - 1];
            this.firNumMin = mData.length - 1;
            this.firNumMax = mData.length - 1;
            this.secNumMin = mData.length - 1;
            this.secNumMax = mData.length - 1;
            ShakeDetectService.this.numflag = true;
            ShakeDetectService.this.judgeflag = true;
            for (i = mData.length - 2; i >= 0; i--) {
                if (mData[i] < this.minData) {
                    this.minData = mData[i];
                    this.nummin = i;
                }
                if (mData[i] > this.maxData) {
                    this.maxData = mData[i];
                    this.nummax = i;
                }
                Log.d(ShakeDetectService.TAG, "+++++maxData -minData+++++: " + this.maxData + "," + this.minData);
                if (this.maxData - this.minData > this.mmThreshold) {
                    Log.d(ShakeDetectService.TAG, "maxData -minData: " + (this.maxData - this.minData) + "," + this.maxData + "," + this.minData);
                    Log.d(ShakeDetectService.TAG, "nummax: " + this.nummax + "nummin:" + this.nummin);
                    if (this.nummin > this.nummax) {
                        this.minData = this.maxData;
                    } else {
                        this.maxData = this.minData;
                    }
                    String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
                    String PLATFORM_INFO = SystemProperties.get(this.PLATFORM_TAG);
                    if (AllConfig.mIsArchADSP) {
                        if (Math.abs(this.nummax - this.nummin) <= 20) {
                            this.num1++;
                        }
                    } else if (Math.abs(this.nummax - this.nummin) <= 12) {
                        this.num1++;
                    }
                }
                if (mData[i] < this.firMinData) {
                    this.firMinData = mData[i];
                    this.firNumMin = i;
                }
                if (mData[i] > this.firMaxData) {
                    this.firMaxData = mData[i];
                    this.firNumMax = i;
                }
            }
            Log.d(ShakeDetectService.TAG, "num1:" + this.num1);
            if (this.num1 > this.mmNum) {
                this.wavestate = true;
                if (System.getInt(ShakeDetectService.this.mContext.getContentResolver(), "bbk_application_settings", 0) == 3) {
                    this.minMinData = mData[mData.length - 1] - this.firMinData;
                    this.minMaxData = this.firMaxData - mData[mData.length - 1];
                    if (this.num1 == 2) {
                        if (this.firNumMin > this.firNumMax) {
                            i = mData.length - 2;
                            while (i >= 15) {
                                if (!(i == this.firNumMin || i == this.firNumMax)) {
                                    if ((i >= this.firNumMax + 2 || i <= this.firNumMax - 2) && ((i >= this.firNumMin + 2 || i <= this.firNumMin - 2) && mData[i] - this.firMinData < this.minMinData)) {
                                        this.minMinData = mData[i] - this.firMinData;
                                        this.secMinData = mData[i];
                                        this.secNumMin = i;
                                    }
                                    if ((i >= this.firNumMax + 2 || i <= this.firNumMax - 2) && ((i >= this.firNumMin + 2 || i <= this.firNumMin - 2) && this.firMaxData - mData[i] < this.minMaxData)) {
                                        this.minMaxData = this.firMaxData - mData[i];
                                        this.secMaxData = mData[i];
                                        this.secNumMax = i;
                                    }
                                }
                                i--;
                            }
                            if (this.secNumMax < this.firNumMin || this.secMinData > ((float) this.firNumMax)) {
                                ShakeDetectService.this.judgeflag = false;
                            }
                        } else {
                            i = mData.length - 2;
                            while (i >= 15) {
                                if (!(i == this.firNumMin || i == this.firNumMax)) {
                                    if ((i >= this.firNumMax + 2 || i <= this.firNumMax - 2) && ((i >= this.firNumMin + 2 || i <= this.firNumMin - 2) && mData[i] - this.firMinData < this.minMinData)) {
                                        this.minMinData = mData[i] - this.firMinData;
                                        this.secMinData = mData[i];
                                        this.secNumMin = i;
                                    }
                                    if ((i >= this.firNumMax + 2 || i <= this.firNumMax - 2) && ((i >= this.firNumMin + 2 || i <= this.firNumMin - 2) && this.firMaxData - mData[i] < this.minMaxData)) {
                                        this.minMaxData = this.firMaxData - mData[i];
                                        this.secMaxData = mData[i];
                                        this.secNumMax = i;
                                    }
                                }
                                i--;
                            }
                            if (this.secNumMin < this.firNumMax || this.secNumMax > this.firNumMin) {
                                ShakeDetectService.this.judgeflag = false;
                            }
                        }
                        Log.d(ShakeDetectService.TAG, "firMaxData:" + this.firMaxData + "secMaxData:" + this.secMaxData + "firMinData:" + this.firMinData + "secMinData:" + this.secMinData + "judgeflag:" + ShakeDetectService.this.judgeflag);
                        Log.d(ShakeDetectService.TAG, "(firMaxData + secMaxData - 2*firMinData):" + ((this.firMaxData + this.secMaxData) - (this.firMinData * 2.0f)));
                        Log.d(ShakeDetectService.TAG, "(2*firMaxData - (firMinData + secMinData)):" + ((this.firMaxData * 2.0f) - (this.firMinData + this.secMinData)));
                        if (ShakeDetectService.this.judgeflag && (this.firMaxData + this.secMaxData) - (this.firMinData * 2.0f) > ((this.firMaxData * 2.0f) - (this.firMinData + this.secMinData)) + ShakeDetectService.STATIC_THRESHOLD) {
                            this.value = 1;
                        } else if (!ShakeDetectService.this.judgeflag || (this.firMaxData * 2.0f) - (this.firMinData + this.secMinData) <= ((this.firMaxData + this.secMaxData) - (this.firMinData * 2.0f)) + ShakeDetectService.STATIC_THRESHOLD) {
                            Log.d(ShakeDetectService.TAG, "mData[mData.length-1]:" + mData[mData.length - 1]);
                            if (mData[mData.length - 1] > 15.0f || mData[mData.length - 2] > 15.0f) {
                                this.value = 0;
                            } else if (mData[mData.length - 1] < -15.0f || mData[mData.length - 2] < -15.0f) {
                                this.value = 1;
                            } else {
                                for (i = mData.length - 2; i > 36; i--) {
                                    if (mData[i] < this.befMinData) {
                                        this.befMinData = mData[i];
                                    }
                                    if (mData[i] > this.befMaxData) {
                                        this.befMaxData = mData[i];
                                    }
                                }
                                Log.d(ShakeDetectService.TAG, "befMinData:" + this.befMinData + "befMaxData:" + this.befMaxData);
                                if (Math.abs(this.befMinData) > Math.abs(this.befMaxData)) {
                                    this.value = 0;
                                } else {
                                    this.value = 1;
                                }
                            }
                        } else {
                            this.value = 0;
                        }
                    } else if (mData[mData.length - 1] > 15.0f || mData[mData.length - 2] > 15.0f) {
                        this.value = 0;
                    } else if (mData[mData.length - 1] < -15.0f || mData[mData.length - 2] < -15.0f) {
                        this.value = 1;
                    } else {
                        for (i = mData.length - 2; i >= 35; i--) {
                            if (mData[i] < this.befMinData) {
                                this.befMinData = mData[i];
                            }
                            if (mData[i] > this.befMaxData) {
                                this.befMaxData = mData[i];
                            }
                        }
                        Log.d(ShakeDetectService.TAG, "befMinData:" + this.befMinData + "befMaxData:" + this.befMaxData);
                        if (Math.abs(this.befMinData) > Math.abs(this.befMaxData)) {
                            this.value = 0;
                        } else {
                            this.value = 1;
                        }
                    }
                }
                Log.d(ShakeDetectService.TAG, "++++++++++++++++wavestate:" + this.wavestate + "BBK_APPLICATION_SETTINGS:" + System.getInt(ShakeDetectService.this.mContext.getContentResolver(), "bbk_application_settings", 0) + "value:" + this.value);
            }
            this.num1 = 0;
            this.num2 = 0;
        }
    }

    private void WaveAnalysis(float xValue, float yValue, float zValue) {
        Message msg;
        float presentXvalue = xValue;
        if (!(this.flag || (this.mShakeModeAnalyzer.wavestate ^ 1) == 0)) {
            boolean z = xValue > this.lastXvalue + THRESHOLD || xValue < this.lastXvalue - THRESHOLD;
            this.flagX = z;
            if ((((double) xValue) > ((double) this.lastXvalue) + 3.0d || ((double) xValue) < ((double) this.lastXvalue) - 3.0d) && !this.enableMode_1) {
                enableAccelerometer(true, 1);
            }
            if (this.timeMode) {
                this.timeMode = false;
                msg = Message.obtain();
                msg.what = 3;
                if (this.mServiceHandler != null) {
                    this.mServiceHandler.sendMessage(msg);
                }
                if (this.mServiceHandler != null) {
                    this.mServiceHandler.sendEmptyMessageDelayed(2, 100);
                }
            }
        }
        if (this.flagX) {
            this.flag = true;
            this.flagX = false;
            Log.d(TAG, "flag:" + this.flag + "flagX:" + this.flagX);
        }
        if (this.flag) {
            this.mShakeModeAnalyzer.eat(xValue);
            Log.d(TAG, "xValue: " + xValue + " yValue: " + yValue + " zValue: " + zValue + " mShakeModeAnalyzer.xData[mShakeModeAnalyzer.dataNum-1]: " + this.mShakeModeAnalyzer.xData[this.mShakeModeAnalyzer.dataNum - 1]);
        }
        if (this.mShakeModeAnalyzer.wavestate) {
            Log.d(TAG, "xValue1: " + xValue + " yValue: " + yValue);
            if (this.snum <= STATIC_NUM) {
                this.snum++;
                if (Math.abs(this.lastXvalue - xValue) >= 3.0f || yValue <= -8.0f) {
                    this.staticnum = 0;
                } else {
                    this.staticnum++;
                }
                if (xValue < -3.0f) {
                    this.tiltnum0++;
                }
                if (xValue > 3.0f) {
                    this.tiltnum1++;
                }
                Log.d(TAG, "tiltnum0:" + this.tiltnum0 + "tiltnum1:" + this.tiltnum1);
                if (this.tiltnum0 >= 12 && this.tiltnum0 > this.tiltnum1 && this.tileflag) {
                    this.mShakeModeAnalyzer.value = 0;
                    this.tiltnum0 = 0;
                    this.tileflag = false;
                }
                if (this.tiltnum1 >= 12 && this.tiltnum1 > this.tiltnum0 && this.tileflag) {
                    this.mShakeModeAnalyzer.value = 1;
                    this.tiltnum1 = 0;
                    this.tileflag = false;
                }
                if (this.staticnum >= 10) {
                    this.staticstate = true;
                    this.snum = 0;
                    this.staticnum = 0;
                    this.tiltnum0 = 0;
                    this.tiltnum1 = 0;
                    this.tileflag = true;
                    Log.d(TAG, "staticstate:" + this.staticstate + "mShakeModeAnalyzer.value:" + this.mShakeModeAnalyzer.value);
                    if (this.mShakeModeAnalyzer.value < 0) {
                        this.mShakeModeAnalyzer.value = 1;
                    }
                    if (System.getInt(this.mContext.getContentResolver(), "bbk_application_settings", 0) == 3) {
                        Intent intent = new Intent();
                        intent.setAction("android.action.multifloatingtask.showsmallwindowvalue");
                        intent.putExtra("showsmallwindowvalue", this.mShakeModeAnalyzer.value);
                        this.mContext.sendBroadcast(intent);
                        Log.d(TAG, "++++++++++++++++value:" + this.mShakeModeAnalyzer.value);
                    }
                }
            } else {
                this.snum = 0;
                this.staticnum = 0;
                this.tiltnum0 = 0;
                this.tiltnum1 = 0;
                this.tileflag = true;
                this.mShakeModeAnalyzer.wavestate = false;
            }
        }
        this.lastXvalue = xValue;
        if (this.staticstate) {
            this.staticstate = false;
            this.mShakeModeAnalyzer.wavestate = false;
            msg = Message.obtain();
            msg.what = 4;
            if (this.mServiceHandler != null) {
                this.mServiceHandler.sendMessage(msg);
            }
            Log.d(TAG, "staticstate:" + this.staticstate);
        }
        if (this.mShakeModeAnalyzer.xData[this.mShakeModeAnalyzer.dataNum - 1] != 0.0f) {
            this.mShakeModeAnalyzer.reset();
            this.flag = false;
        }
    }

    public static ShakeDetectService getInstance() {
        return mSingleShakeDetectService;
    }

    private ShakeDetectService() {
        this.isShakeModeWorking = false;
        this.mCallBackHandler = null;
        this.mServiceHandler = null;
        this.mContext = null;
        this.lastXvalue = 6.0f;
        this.presentXvalue = 0.0f;
        this.snum = 0;
        this.staticnum = 0;
        this.tiltnum0 = 0;
        this.tiltnum1 = 0;
        this.tileflag = true;
        this.flagX = false;
        this.flag = false;
        this.numflag = true;
        this.judgeflag = true;
        this.timeMode = false;
        this.enableMode_0 = false;
        this.enableMode_1 = false;
        this.timeCount = 0;
        this.accTime = 0;
        this.accDataTime = 0;
        this.staticstate = false;
        this.mAccelerometerListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                if ((event.timestamp / 1000000) - ShakeDetectService.this.accDataTime > 18) {
                    ShakeDetectService.this.WaveAnalysis(event.values[0], event.values[1], event.values[2]);
                    ShakeDetectService.this.accDataTime = event.timestamp / 1000000;
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mAccelerometerListener1 = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                if ((event.timestamp / 1000000) - ShakeDetectService.this.accDataTime > 18) {
                    ShakeDetectService.this.WaveAnalysis(event.values[0], event.values[1], event.values[2]);
                    ShakeDetectService.this.accDataTime = event.timestamp / 1000000;
                }
                if ((event.timestamp / 1000000) - ShakeDetectService.this.accTime < 115) {
                    ShakeDetectService.this.timeCount = ShakeDetectService.this.timeCount + 1;
                } else {
                    ShakeDetectService.this.timeCount = 0;
                }
                if (ShakeDetectService.this.timeCount > 50) {
                    ShakeDetectService.this.timeCount = 0;
                    ShakeDetectService.this.timeMode = true;
                }
                ShakeDetectService.this.accTime = event.timestamp / 1000000;
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mShakeModeAnalyzer = new ShakeModeAnalyzer(40, 1, 20.0f, STATIC_THRESHOLD);
        this.snum = 0;
        this.staticnum = 0;
        this.staticstate = false;
        this.mShakeModeAnalyzer.reset();
        this.mShakeModeAnalyzer.wavestate = false;
    }

    private void enableAccelerometer(boolean enable, int mode) {
        if (enable) {
            if (mode == 0) {
                if (this.mSensorManager != null) {
                    this.mSensorManager.registerListener(this.mAccelerometerListener, this.mSensorManager.getDefaultSensor(1), 66667, this.mServiceHandler);
                    this.enableMode_0 = true;
                }
            } else if (mode == 1 && this.mSensorManager != null) {
                this.mSensorManager.registerListener(this.mAccelerometerListener1, this.mSensorManager.getDefaultSensor(1), 25000, this.mServiceHandler);
                this.enableMode_1 = true;
            }
        } else if (mode == 0) {
            if (this.mSensorManager != null) {
                this.mSensorManager.unregisterListener(this.mAccelerometerListener);
                this.enableMode_0 = false;
            }
        } else if (mode == 1 && this.mSensorManager != null) {
            this.mSensorManager.unregisterListener(this.mAccelerometerListener1);
            this.enableMode_1 = false;
        }
    }

    public boolean startMotionRecognitionService(Context context, Handler handler) {
        Log.d(TAG, "startMotionRecognitionService ");
        if (!this.isShakeModeWorking) {
            this.mContext = context;
            this.isShakeModeWorking = true;
            this.mCallBackHandler = handler;
            this.mServiceHandler = new ShakeDetectServiceHandler(handler.getLooper());
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            if (this.mServiceHandler != null) {
                this.snum = 0;
                this.staticnum = 0;
                this.staticstate = false;
                this.mShakeModeAnalyzer.reset();
                this.mShakeModeAnalyzer.wavestate = false;
                if (!this.enableMode_0) {
                    enableAccelerometer(true, 0);
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
        Log.d(TAG, "stopMotionRecognitionService " + this.isShakeModeWorking);
        if (this.isShakeModeWorking) {
            this.isShakeModeWorking = false;
            if (this.enableMode_0) {
                enableAccelerometer(false, 0);
            }
            if (this.enableMode_1) {
                enableAccelerometer(false, 1);
            }
            if (!this.enableMode_0 && !this.enableMode_1) {
                this.mSensorManager = null;
            } else if (this.enableMode_0) {
                enableAccelerometer(false, 0);
            }
            if (this.enableMode_1) {
                enableAccelerometer(false, 1);
            }
            this.mCallBackHandler = null;
            this.mServiceHandler = null;
            this.mSensorManager = null;
        }
        return true;
    }
}
