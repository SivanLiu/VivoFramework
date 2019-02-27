package com.vivo.services.motion.gesture.util;

public class Quaternion {
    public static final Quaternion IDENTITY = new Quaternion(1.0f, 0.0f, 0.0f, 0.0f);
    public static final Quaternion NaN = new Quaternion(1.0f, Float.NaN, Float.NaN, Float.NaN);
    private final float w;
    private final float x;
    private final float y;
    private final float z;

    public Quaternion(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4) {
        this.w = paramFloat1;
        this.x = paramFloat2;
        this.y = paramFloat3;
        this.z = paramFloat4;
    }

    public Quaternion(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, boolean paramBoolean) {
        if (paramBoolean) {
            float f1 = (float) Math.sqrt((double) ((((paramFloat1 * paramFloat1) + (paramFloat2 * paramFloat2)) + (paramFloat3 * paramFloat3)) + (paramFloat4 * paramFloat4)));
            if (f1 != 0.0f) {
                float f2 = 1.0f / f1;
                paramFloat1 *= f2;
                paramFloat2 *= f2;
                paramFloat3 *= f2;
                paramFloat4 *= f2;
            }
        }
        this.w = paramFloat1;
        this.x = paramFloat2;
        this.y = paramFloat3;
        this.z = paramFloat4;
    }

    public static Quaternion fromUnitVector(float paramFloat1, float paramFloat2, float paramFloat3) {
        float f1 = ((1.0f - (paramFloat1 * paramFloat1)) - (paramFloat2 * paramFloat2)) - (paramFloat3 * paramFloat3);
        return new Quaternion(f1 > 0.0f ? (float) Math.sqrt((double) f1) : 0.0f, paramFloat1, paramFloat2, paramFloat3);
    }

    public static Quaternion fromVector(float paramFloat1, float paramFloat2, float paramFloat3) {
        float f1 = ((1.0f - (paramFloat1 * paramFloat1)) - (paramFloat2 * paramFloat2)) - (paramFloat3 * paramFloat3);
        return new Quaternion(f1 > 0.0f ? (float) Math.sqrt((double) f1) : 0.0f, paramFloat1, paramFloat2, paramFloat3, true);
    }

    public static Quaternion fromVector(Vector3D paramVector3D) {
        float f1 = paramVector3D.getX();
        float f2 = paramVector3D.getY();
        float f3 = paramVector3D.getZ();
        float f4 = ((1.0f - (f1 * f1)) - (f2 * f2)) - (f3 * f3);
        return new Quaternion(f4 > 0.0f ? (float) Math.sqrt((double) f4) : 0.0f, f1, f2, f3, true);
    }

    public Vector3D applyInverseTo(Vector3D paramVector3D) {
        float f1 = paramVector3D.getX();
        float f2 = paramVector3D.getY();
        float f3 = paramVector3D.getZ();
        float f4 = ((this.x * f1) + (this.y * f2)) + (this.z * f3);
        return new Vector3D(((((-this.w) * (((-this.w) * f1) - ((this.y * f3) - (this.z * f2)))) + (this.x * f4)) * 2.0f) - f1, ((((-this.w) * (((-this.w) * f2) - ((this.z * f1) - (this.x * f3)))) + (this.y * f4)) * 2.0f) - f2, ((((-this.w) * (((-this.w) * f3) - ((this.x * f2) - (this.y * f1)))) + (this.z * f4)) * 2.0f) - f3);
    }

    public Vector3D applyTo(Vector3D paramVector3D) {
        float f1 = paramVector3D.getX();
        float f2 = paramVector3D.getY();
        float f3 = paramVector3D.getZ();
        float f4 = ((this.x * f1) + (this.y * f2)) + (this.z * f3);
        return new Vector3D((((this.w * ((this.w * f1) - ((this.y * f3) - (this.z * f2)))) + (this.x * f4)) * 2.0f) - f1, (((this.w * ((this.w * f2) - ((this.z * f1) - (this.x * f3)))) + (this.y * f4)) * 2.0f) - f2, (((this.w * ((this.w * f3) - ((this.x * f2) - (this.y * f1)))) + (this.z * f4)) * 2.0f) - f3);
    }

    public float[] asArray() {
        return new float[]{this.w, this.x, this.y, this.z};
    }

    public Quaternion composeWith(Quaternion paramQuaternion) {
        float f1 = paramQuaternion.getW();
        float f2 = paramQuaternion.getX();
        float f3 = paramQuaternion.getY();
        float f4 = paramQuaternion.getZ();
        return new Quaternion((this.w * f1) - (((this.x * f2) + (this.y * f3)) + (this.z * f4)), ((this.w * f2) + (this.x * f1)) + ((this.z * f3) - (this.y * f4)), ((this.w * f3) + (this.y * f1)) + ((this.x * f4) - (this.z * f2)), ((this.w * f4) + (this.z * f1)) + ((this.y * f2) - (this.x * f3)));
    }

    public double[][] getMatrix() {
        double[][] arrayOfDouble = new double[][]{new double[3], new double[3], new double[3]};
        double d1 = (double) (this.w * this.w);
        double d3 = (double) (this.y * this.y);
        double d4 = (double) (this.z * this.z);
        double d5 = (double) (this.w * this.x);
        double d6 = (double) (this.w * this.y);
        double d7 = (double) (this.w * this.z);
        double d8 = (double) (this.x * this.y);
        double d9 = (double) (this.x * this.z);
        double d10 = (double) (this.y * this.z);
        arrayOfDouble[0][0] = ((d1 + ((double) (this.x * this.x))) * 2.0d) - 1.0d;
        arrayOfDouble[1][0] = (d8 + d7) * 2.0d;
        arrayOfDouble[2][0] = (d9 + d6) * 2.0d;
        arrayOfDouble[0][1] = (d8 + d7) * 2.0d;
        arrayOfDouble[1][1] = ((d1 + d3) * 2.0d) - 1.0d;
        arrayOfDouble[2][1] = (d10 + d5) * 2.0d;
        arrayOfDouble[0][2] = (d9 + d6) * 2.0d;
        arrayOfDouble[1][2] = (d10 + d5) * 2.0d;
        arrayOfDouble[2][2] = ((d1 + d4) * 2.0d) - 1.0d;
        return arrayOfDouble;
    }

    public Angle getOrientation() {
        return new Angle((float) Math.atan2((double) (((this.w * this.z) + (this.x * this.y)) * 2.0f), (double) (1.0f - (((this.y * this.y) + (this.z * this.z)) * 2.0f))), (float) Math.asin((double) (((this.w * this.y) - (this.x * this.z)) * 2.0f)), (float) Math.atan2((double) (((this.w * this.x) + (this.y * this.z)) * 2.0f), (double) (1.0f - (((this.x * this.x) + (this.y * this.y)) * 2.0f))));
    }

    public float getW() {
        return this.w;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public Quaternion multiply(Quaternion paramQuaternion) {
        float f1 = paramQuaternion.getW();
        float f2 = paramQuaternion.getX();
        float f3 = paramQuaternion.getY();
        float f4 = paramQuaternion.getZ();
        return new Quaternion((((this.w * f1) - (this.x * f2)) - (this.y * f3)) - (this.z * f4), (((this.w * f2) + (this.x * f1)) + (this.y * f4)) - (this.z * f3), (((this.w * f3) - (this.x * f4)) + (this.y * f1)) + (this.z * f2), (((this.w * f4) + (this.x * f3)) - (this.y * f2)) + (this.z * f1));
    }

    public double norm() {
        return Math.sqrt((double) ((((this.w * this.w) + (this.x * this.x)) + (this.y * this.y)) + (this.z * this.z)));
    }

    public Quaternion normalize() {
        float f1 = (float) norm();
        if (f1 == 0.0f) {
            return NaN;
        }
        float f2 = 1.0f / f1;
        return new Quaternion(this.w * f2, this.x * f2, this.y * f2, this.z * f2);
    }

    public String toString() {
        return "Quaternion [w: " + this.w + ", x: " + this.x + ", y: " + this.y + ", z: " + this.z + "]";
    }
}
