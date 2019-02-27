package com.vivo.services.motion.gesture;

import com.vivo.common.provider.Calendar.CalendarsColumns;

public class MotionAnalysisTraceData {
    public float[] curAcc = new float[3];
    public long curAccTimeStamp;
    public float[] curSpeed = new float[3];
    public float[] lasAcc = new float[3];
    public long lasAccTimeStamp;
    public float[] lasSpeed = new float[3];
    public final int mBufferCount = CalendarsColumns.EDITOR_ACCESS;
    public float[] mRotationMatrix = new float[16];
    public int mTraceCount;
    public float[] mTraceX = new float[CalendarsColumns.EDITOR_ACCESS];
    public float[] mTraceY = new float[CalendarsColumns.EDITOR_ACCESS];
    public float[] mTraceZ = new float[CalendarsColumns.EDITOR_ACCESS];
    public float[] yTraceX = new float[CalendarsColumns.EDITOR_ACCESS];
    public float[] yTraceY = new float[CalendarsColumns.EDITOR_ACCESS];
    public float[] zTraceX = new float[CalendarsColumns.EDITOR_ACCESS];
    public float[] zTraceY = new float[CalendarsColumns.EDITOR_ACCESS];

    public MotionAnalysisTraceData() {
        clearTrace();
    }

    public void clearTrace() {
        int i;
        for (i = 0; i < 3; i++) {
            this.curSpeed[i] = 0.0f;
            this.lasSpeed[i] = 0.0f;
            this.curAcc[i] = 0.0f;
            this.lasAcc[i] = 0.0f;
        }
        for (i = 0; i < CalendarsColumns.EDITOR_ACCESS; i++) {
            this.mTraceX[i] = 0.0f;
            this.mTraceY[i] = 0.0f;
            this.yTraceX[i] = 0.0f;
            this.yTraceY[i] = 0.0f;
        }
        this.curAccTimeStamp = 0;
        this.lasAccTimeStamp = 0;
        this.mTraceX[0] = 0.0f;
        this.mTraceY[0] = 0.0f;
        this.mTraceZ[0] = 0.0f;
        this.mTraceCount = 1;
    }
}
