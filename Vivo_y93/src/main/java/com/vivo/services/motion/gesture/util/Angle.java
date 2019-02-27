package com.vivo.services.motion.gesture.util;

public class Angle {
    /* renamed from: -com-vivo-services-motion-gesture-util-RotationAxisSwitchesValues */
    private static final /* synthetic */ int[] f2x803db61a = null;
    public static final Angle ZERO = new Angle(0.0f, 0.0f, 0.0f);
    private final float pitch;
    private final float roll;
    private final float yaw;

    /* renamed from: -getcom-vivo-services-motion-gesture-util-RotationAxisSwitchesValues */
    private static /* synthetic */ int[] m2xdf46d9f6() {
        if (f2x803db61a != null) {
            return f2x803db61a;
        }
        int[] iArr = new int[RotationAxis.values().length];
        try {
            iArr[RotationAxis.PITCH.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[RotationAxis.ROLL.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[RotationAxis.YAW.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        f2x803db61a = iArr;
        return iArr;
    }

    public Angle(float paramFloat1, float paramFloat2, float paramFloat3) {
        this.yaw = paramFloat1;
        this.pitch = paramFloat2;
        this.roll = paramFloat3;
    }

    public float[] asArray() {
        return new float[]{this.yaw, this.pitch, this.roll};
    }

    public float getAngleByAxis(RotationAxis paramRotationAxis) {
        switch (m2xdf46d9f6()[paramRotationAxis.ordinal()]) {
            case 1:
                return this.pitch;
            case 2:
                return this.roll;
            case 3:
                return this.yaw;
            default:
                return 0.0f;
        }
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getRoll() {
        return this.roll;
    }

    public float getYaw() {
        return this.yaw;
    }

    public String toString() {
        return "Angle [yaw: " + this.yaw + ", pitch: " + this.pitch + ", roll: " + this.roll + "]";
    }
}
