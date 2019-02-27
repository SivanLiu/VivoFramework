package com.vivo.common.autobrightness;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Slog;
import com.vivo.common.autobrightness.CameraLumaCallback.PreLightCallback;

public class PreloadingLightSensor {
    private static final int MSG_DISABLE_PRE_LIGHT_SENSOR = 0;
    private static final int MSG_ENABLE_PRE_LIGHT_SENSOR = 1;
    private static final String TAG = "PreloadingLightSensor";
    private PreLightCallback mCallback;
    private boolean mEanbled = false;
    private PreHandler mHandler = null;
    private long mLightTimeStamp = -1;
    private SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (PreloadingLightSensor.this.mCallback != null) {
                long now = SystemClock.uptimeMillis();
                Slog.e(PreloadingLightSensor.TAG, "onSensorChanged mPreloadingLightSensor lux:" + event.values[0]);
                if (PreloadingLightSensor.this.mLightTimeStamp > 0 && now - PreloadingLightSensor.this.mLightTimeStamp > 60) {
                    PreloadingLightSensor.this.mCallback.notifyBrightnessToUDFinger(event);
                    PreloadingLightSensor.this.mCallback.onSensorChanged(event);
                }
                PreloadingLightSensor preloadingLightSensor = PreloadingLightSensor.this;
                preloadingLightSensor.mValidEventCount = preloadingLightSensor.mValidEventCount + 1;
                if (PreloadingLightSensor.this.mValidEventCount > 20) {
                    PreloadingLightSensor.this.mHandler.sendEmptyMessage(0);
                    return;
                }
                return;
            }
            Slog.e(PreloadingLightSensor.TAG, "onSensorChanged mCallback is null!");
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private SensorManager mSensorManager = null;
    private int mValidEventCount = 0;

    private class PreHandler extends Handler {
        public PreHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Slog.d(PreloadingLightSensor.TAG, "handleMessage MSG_DISABLE_PRE_LIGHT_SENSOR");
                    PreloadingLightSensor.this.enableLightSensor(false);
                    return;
                case 1:
                    Slog.d(PreloadingLightSensor.TAG, "handleMessage MSG_ENABLE_PRE_LIGHT_SENSOR");
                    PreloadingLightSensor.this.enableLightSensor(true);
                    return;
                default:
                    return;
            }
        }
    }

    public PreloadingLightSensor(SensorManager sensormanager, PreLightCallback callback, Looper looper) {
        this.mSensorManager = sensormanager;
        this.mHandler = new PreHandler(looper);
        this.mCallback = callback;
        Slog.d(TAG, "constructor called.");
    }

    public void enablePreLightSensor(boolean enable) {
        if (enable) {
            this.mHandler.removeMessages(0);
            this.mHandler.sendEmptyMessage(1);
            return;
        }
        this.mHandler.sendEmptyMessageDelayed(0, 150);
    }

    private void enableLightSensor(boolean enable) {
        if (this.mEanbled != enable) {
            Slog.d(TAG, "enablePreLightSensor(" + enable + ")");
            if (enable) {
                this.mValidEventCount = 0;
                Sensor mLightSensor = this.mSensorManager.getDefaultSensor(5);
                this.mLightTimeStamp = SystemClock.uptimeMillis();
                this.mEanbled = this.mSensorManager.registerListener(this.mListener, mLightSensor, 1, this.mHandler);
                Slog.d(TAG, "enableLightSensor after register. mEanbled=" + this.mEanbled + " ligntSensor=" + (mLightSensor == null ? "Null" : "NotNull"));
                return;
            }
            this.mLightTimeStamp = -1;
            this.mSensorManager.unregisterListener(this.mListener);
            this.mEanbled = false;
            if (this.mCallback != null) {
                this.mCallback.onSensorChanged(null);
            }
        }
    }
}
