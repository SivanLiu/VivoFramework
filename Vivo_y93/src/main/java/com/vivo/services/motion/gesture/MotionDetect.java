package com.vivo.services.motion.gesture;

import android.content.Context;
import android.hardware.SensorEvent;
import com.vivo.services.motion.gesture.engine.LinearPosition;
import com.vivo.services.motion.gesture.util.Quaternion;
import com.vivo.services.motion.gesture.util.SensorData;
import com.vivo.services.motion.gesture.util.Vector3D;
import java.util.Vector;

public class MotionDetect {
    private static final String TAG = "MotionDetect";
    private static final float TYPE_LINEAR_ACCELERATION = 0.0f;
    private static final float TYPE_ROTATION_VECTOR = 1.0f;
    private static int filterCount = 15;
    private static final int filterNum = 15;
    private static final int linear_acc_mask = 1;
    private static Context mContext = null;
    private static LinearPosition mLinearPosition = null;
    private static MotionAnalysisTraceData mTraceDate = new MotionAnalysisTraceData();
    private static final int rotation_mask = 2;
    private static MotionDetect singleDetect = null;
    private static final int vibStartTime = 4;
    private static final int vibTime = 60;
    public Vector3D[] mNormalVectors = null;
    public Vector<Vector3D> mPathBuffer2D = null;
    public Vector<Vector3D> mPathBuffer3D = null;
    private SensorData mSensorData = new SensorData();
    private int mSensorState;
    public int sample_num = 0;

    public static MotionDetect getInstance(Context context) {
        if (singleDetect == null) {
            mContext = context;
            singleDetect = new MotionDetect();
            mLinearPosition = new LinearPosition();
            mLinearPosition.reset();
        }
        return singleDetect;
    }

    public void onSensorChanged(SensorEvent event) {
        float[] sensor_event = new float[4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                sensor_event[j] = event.values[(i * 4) + j];
            }
            float[] arrayOfFloat = this.mSensorData.remapCoordinatesToMotionEngine(sensor_event);
            float type = sensor_event[3];
            if (type == TYPE_LINEAR_ACCELERATION) {
                this.mSensorData.setLinearAccelerationNoGravity(new Vector3D(arrayOfFloat[0], arrayOfFloat[1], arrayOfFloat[2]));
                this.mSensorState |= 1;
            } else if (type == 1.0f) {
                this.mSensorData.setAngularPosition(Quaternion.fromUnitVector(arrayOfFloat[0], arrayOfFloat[1], arrayOfFloat[2]));
                this.mSensorState |= 2;
            }
            if (this.mSensorState == 3) {
                this.sample_num++;
                this.mSensorState = 0;
                mLinearPosition.onSensorData(this.mSensorData);
            }
        }
    }

    public void result() {
        if (mLinearPosition.PathBuffer2D != null && mLinearPosition.PathBuffer3D != null) {
            this.mPathBuffer2D = mLinearPosition.PathBuffer2D;
            this.mPathBuffer3D = mLinearPosition.PathBuffer3D;
            this.mNormalVectors = mLinearPosition.NormalVectors;
        }
    }

    public void reset() {
        mLinearPosition.reset();
        this.mSensorState = 0;
        this.sample_num = 0;
        filterCount = 15;
    }

    public void start() {
        reset();
        mLinearPosition.setCapturing(true);
    }

    public void stop() {
        mLinearPosition.setCapturing(false);
    }
}
