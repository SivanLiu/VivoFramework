package com.vivo.services.motion.gesture.util;

public class Vector3D {
    public static final Vector3D NaN = new Vector3D(Float.NaN, Float.NaN, Float.NaN);
    public static final Vector3D ZERO = new Vector3D(0.0f, 0.0f, 0.0f);
    private final float x;
    private final float y;
    private final float z;

    public Vector3D(float paramFloat1, float paramFloat2, float paramFloat3) {
        this.x = paramFloat1;
        this.y = paramFloat2;
        this.z = paramFloat3;
    }

    public Vector3D add(float paramFloat, Vector3D paramVector3D) {
        Vector3D localVector3D = paramVector3D.scalarMultiply(paramFloat);
        return new Vector3D(this.x + localVector3D.getX(), this.y + localVector3D.getY(), this.z + localVector3D.getZ());
    }

    public Vector3D add(Vector3D paramVector3D) {
        return new Vector3D(this.x + paramVector3D.getX(), this.y + paramVector3D.getY(), this.z + paramVector3D.getZ());
    }

    public float angle(Vector3D paramVector3D) {
        double d1 = norm() * paramVector3D.norm();
        if (d1 == 0.0d) {
            return Float.NaN;
        }
        double d2 = (double) dotProduct(paramVector3D);
        double d3 = d1 * 0.9999d;
        if (d2 >= (-d3) && d2 <= d3) {
            return (float) Math.acos(d2 / d1);
        }
        Vector3D localVector3D = crossProduct(paramVector3D);
        if (d2 >= 0.0d) {
            return (float) Math.asin(localVector3D.norm() / d1);
        }
        return (float) (3.141592653589793d - Math.asin(localVector3D.norm() / d1));
    }

    public float[] asArray() {
        return new float[]{this.x, this.y, this.z};
    }

    public Vector3D crossProduct(Vector3D paramVector3D) {
        float f1 = paramVector3D.getX();
        float f2 = paramVector3D.getY();
        float f3 = paramVector3D.getZ();
        return new Vector3D((this.z * f2) - (this.y * f3), (this.x * f3) - (this.z * f1), (this.y * f1) - (this.x * f2));
    }

    public float distanceFromLine(Vector3D paramVector3D1, Vector3D paramVector3D2) {
        return (float) (subtract(paramVector3D1).crossProduct(subtract(paramVector3D2)).norm() / paramVector3D2.subtract(paramVector3D1).norm());
    }

    public float dotProduct(Vector3D paramVector3D) {
        return ((this.x * paramVector3D.getX()) + (this.y * paramVector3D.getY())) + (this.z * paramVector3D.getZ());
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

    public double norm() {
        return Math.sqrt((double) (((this.x * this.x) + (this.y * this.y)) + (this.z * this.z)));
    }

    public Vector3D normalize() {
        float f1 = (float) norm();
        if (f1 == 0.0f) {
            return NaN;
        }
        float f2 = 1.0f / f1;
        return new Vector3D(this.x * f2, this.y * f2, this.z * f2);
    }

    public Vector3D scalarMultiply(float paramFloat) {
        return new Vector3D(this.x * paramFloat, this.y * paramFloat, this.z * paramFloat);
    }

    public Vector3D subtract(Vector3D paramVector3D) {
        return new Vector3D(this.x - paramVector3D.getX(), this.y - paramVector3D.getY(), this.z - paramVector3D.getZ());
    }

    public String toString() {
        return "Vector3D [x: " + this.x + ", y: " + this.y + ", z: " + this.z + "]";
    }
}
