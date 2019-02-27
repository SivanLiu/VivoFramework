package com.vivo.services.facedetect;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class AccelerometerManager {
    private static final String TAG = AccelerometerManager.class.getSimpleName();
    private static AccelerometerManager instance;
    private AccelerometerSensorListener mAccListener;
    private boolean mHasStarted = false;
    private SensorManager mSensorManager;

    private class AccelerometerSensorListener implements SensorEventListener {
        private int dir;

        /* synthetic */ AccelerometerSensorListener(AccelerometerManager this$0, AccelerometerSensorListener -this1) {
            this();
        }

        private AccelerometerSensorListener() {
            this.dir = -1;
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == 1) {
                float x = event.values[0];
                float y = event.values[1];
                if (Math.abs(x) <= 0.5f && Math.abs(y) <= 0.5f) {
                    return;
                }
                if (Math.abs(x) > Math.abs(y)) {
                    if (x > 0.0f) {
                        this.dir = 0;
                    } else {
                        this.dir = 2;
                    }
                } else if (y > 0.0f) {
                    this.dir = 1;
                } else {
                    this.dir = 3;
                }
            }
        }
    }

    private AccelerometerManager() {
    }

    public static AccelerometerManager getInstance() {
        if (instance == null) {
            instance = new AccelerometerManager();
        }
        return instance;
    }

    public static void start(Context context) {
        getInstance().registerListener(context);
    }

    public static void stop() {
        getInstance().unregisterListener();
    }

    public static FaceOrientation getFaceOrientation(boolean isFrontCamera) {
        return getFaceOrientation(getDir(isFrontCamera));
    }

    public static FaceOrientation getFaceOrientation(int dir) {
        Log.d(TAG, "getFaceOrientation  dir = " + dir);
        switch (dir) {
            case 0:
                return FaceOrientation.UP;
            case 1:
                return FaceOrientation.LEFT;
            case 2:
                return FaceOrientation.DOWN;
            case 3:
                return FaceOrientation.RIGHT;
            default:
                return FaceOrientation.RIGHT;
        }
    }

    public static int getDegree(boolean isFrontCamera) {
        return getDir(isFrontCamera) * 90;
    }

    public static int getDegree() {
        return getDir() * 90;
    }

    public static int getDir(boolean isFrontCamera) {
        return getInstance().getDirection(isFrontCamera);
    }

    public static int getDir() {
        return getDir(false);
    }

    private int getDirection(boolean isFrontCamera) {
        if (this.mAccListener == null) {
            return -1;
        }
        int dir = this.mAccListener.dir;
        if (isFrontCamera && (dir & 1) == 1) {
            dir ^= 2;
        }
        return dir;
    }

    private void registerListener(Context context) {
        if (!this.mHasStarted) {
            if (this.mSensorManager == null) {
                this.mSensorManager = (SensorManager) context.getApplicationContext().getSystemService("sensor");
            }
            if (this.mSensorManager != null) {
                Sensor accelerometerSensor = this.mSensorManager.getDefaultSensor(1);
                if (accelerometerSensor != null) {
                    this.mAccListener = new AccelerometerSensorListener(this, null);
                    this.mHasStarted = this.mSensorManager.registerListener(this.mAccListener, accelerometerSensor, 1);
                }
            }
        }
    }

    private void unregisterListener() {
        if (this.mHasStarted && this.mSensorManager != null) {
            this.mHasStarted = false;
            this.mSensorManager.unregisterListener(this.mAccListener);
        }
    }
}
