package com.qti.location.sdk.collection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;

public class IZatSensorInfo implements SensorEventListener {
    private static final boolean DEBUG = true;
    private static final int MSG_DISABLE_SENSOR = 2;
    private static final int MSG_ENABLE_SENSOR = 1;
    private String TAG = "IZatSensorInfo";
    private boolean mEnabled = false;
    private float[] mEvent = new float[3];
    private MyHandler mHandler;
    private boolean mHasEvent = false;
    private Object mLock = new Object();
    private Sensor mSensor = null;
    private SensorManager mSensorManager = null;
    private HandlerThread mThread = new HandlerThread(this.TAG);
    private long mTs = 0;

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (!IZatSensorInfo.this.mEnabled) {
                        IZatSensorInfo.this.mHasEvent = false;
                        IZatSensorInfo.this.enableSensorInner();
                        return;
                    }
                    return;
                case 2:
                    if (IZatSensorInfo.this.mEnabled) {
                        IZatSensorInfo.this.disableSensorInner();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public IZatSensorInfo(Context context) {
        this.mThread.start();
        this.mHandler = new MyHandler(this.mThread.getLooper());
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
        if (this.mSensorManager != null) {
            this.mSensor = this.mSensorManager.getDefaultSensor(1);
        }
    }

    private void enableSensorInner() {
        this.mEvent[0] = 0.0f;
        this.mEvent[1] = 0.0f;
        this.mEvent[2] = 0.0f;
        if (this.mSensor == null) {
            Log.w(this.TAG, "Sensors not supported");
        } else {
            this.mEnabled = this.mSensorManager.registerListener(this, this.mSensor, 2, this.mHandler);
        }
    }

    private void disableSensorInner() {
        if (this.mSensorManager != null) {
            this.mSensorManager.unregisterListener(this);
            this.mEnabled = false;
        }
    }

    public void enableSensor() {
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessage(1);
        this.mTs = System.currentTimeMillis();
    }

    public void disableSensor() {
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessage(2);
    }

    public void onSensorChanged(SensorEvent event) {
        if (event != null && !this.mHasEvent) {
            synchronized (this.mLock) {
                this.mHasEvent = DEBUG;
                this.mEvent[0] = event.values[0];
                this.mEvent[1] = event.values[1];
                this.mEvent[2] = event.values[2];
                Log.d(this.TAG, "onSensorChanged tooks:" + (System.currentTimeMillis() - this.mTs));
                this.mLock.notifyAll();
            }
        }
    }

    public void onAccuracyChanged(Sensor s, int accuracy) {
    }

    public JSONArray getJson() {
        JSONArray arr = new JSONArray();
        synchronized (this.mLock) {
            try {
                this.mLock.wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                arr.put((double) this.mEvent[0]);
                arr.put((double) this.mEvent[1]);
                arr.put((double) this.mEvent[2]);
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
        }
        return arr;
    }
}
