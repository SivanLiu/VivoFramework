package com.vivo.services.motion.gesture.util;

public class SensorData {
    Quaternion angularPosition = Quaternion.IDENTITY;
    Vector3D linearAccelerationNoGravity = Vector3D.ZERO;
    long timestamp;

    public Quaternion getAngularPosition() {
        return this.angularPosition;
    }

    public Vector3D getLinearAccelerationNoGravity() {
        return this.linearAccelerationNoGravity;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setAngularPosition(Quaternion paramQuaternion) {
        this.angularPosition = paramQuaternion;
    }

    public void setLinearAccelerationNoGravity(Vector3D paramVector3D) {
        this.linearAccelerationNoGravity = paramVector3D;
    }

    public void setTimestamp(long paramLong) {
        this.timestamp = paramLong;
    }

    public float[] remapCoordinatesToMotionEngine(float[] paramArrayOfFloat) {
        float[] arrayOfFloat = new float[paramArrayOfFloat.length];
        arrayOfFloat[0] = paramArrayOfFloat[1];
        arrayOfFloat[1] = paramArrayOfFloat[0];
        arrayOfFloat[2] = -paramArrayOfFloat[2];
        return arrayOfFloat;
    }
}
