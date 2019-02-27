package com.vivo.services.motion.gesture.engine;

import android.util.Log;
import com.vivo.services.motion.gesture.util.Quaternion;
import com.vivo.services.motion.gesture.util.SensorData;
import com.vivo.services.motion.gesture.util.Vector3D;
import java.util.Vector;

public class LinearPosition {
    private static final int MAX_BUFFER_SIZE = 1000;
    private static double PathDistance = 0.0d;
    private static final String TAG = "LinearPosition";
    private static final int effectCont = 40;
    private static final double effectDistance = 260.0d;
    private static final double headOffSet = 0.01d;
    private static final double tailOffSet = 0.1d;
    private static final float triggerLevel = 0.35f;
    public int[] NormalIndex;
    public Vector3D[] NormalVectors;
    public Vector<Vector3D> PathBuffer2D;
    public Vector<Vector3D> PathBuffer3D;
    private boolean capturing = false;
    private DoubleIntegrator3D integrator = null;
    private Vector3D prev3DPonit = null;
    public Vector<Quaternion> rotBuffer;
    private boolean trigger = false;

    private Vector3D[] getDeviceAxes(Vector<Quaternion> paramVector) {
        int size = paramVector.size();
        axesVector3D = new Vector3D[3];
        Quaternion localQuaternion = (Quaternion) paramVector.get(this.NormalIndex[0]);
        axesVector3D[0] = localQuaternion.applyInverseTo(new Vector3D(1.0f, 0.0f, 0.0f));
        axesVector3D[1] = localQuaternion.applyInverseTo(new Vector3D(0.0f, 1.0f, 0.0f));
        axesVector3D[2] = localQuaternion.applyInverseTo(new Vector3D(0.0f, 0.0f, 1.0f));
        return axesVector3D;
    }

    private Vector3D getPlaneNormal(Vector<Vector3D> paramVector) {
        int i;
        int stop = paramVector.size();
        Vector3D localObject1 = null;
        Vector3D localObject2 = null;
        Vector3D localObject3 = null;
        double d1 = -1.0d;
        double d3 = -1.0d;
        this.NormalIndex = new int[3];
        for (i = 0; i < stop; i++) {
            Vector3D localVector3D1 = (Vector3D) paramVector.get(i);
            for (int j = i; j < stop; j++) {
                Vector3D localVector3D2 = (Vector3D) paramVector.get(j);
                double d2 = localVector3D1.subtract(localVector3D2).norm();
                if (d2 > d1) {
                    d1 = d2;
                    localObject1 = localVector3D1;
                    localObject2 = localVector3D2;
                    this.NormalIndex[0] = i;
                    this.NormalIndex[1] = j;
                }
            }
        }
        for (i = 0; i < stop; i++) {
            Vector3D localVector3D3 = (Vector3D) paramVector.get(i);
            double d4 = (double) localVector3D3.distanceFromLine(localObject1, localObject2);
            if (d4 > d3) {
                d3 = d4;
                localObject3 = localVector3D3;
                this.NormalIndex[2] = i;
            }
        }
        this.NormalVectors = new Vector3D[]{localObject2.subtract(localObject1).crossProduct(localObject3.subtract(localObject1)).normalize(), localObject1, localObject2, localObject3};
        return localObject2.subtract(localObject1).crossProduct(localObject3.subtract(localObject1)).normalize();
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isEqual(Quaternion paramQuaternion1, Quaternion paramQuaternion2) {
        if (paramQuaternion1 != null && paramQuaternion2 != null && paramQuaternion1.getW() == paramQuaternion2.getW() && paramQuaternion1.getX() == paramQuaternion2.getX() && paramQuaternion1.getY() == paramQuaternion2.getY() && paramQuaternion1.getZ() == paramQuaternion2.getZ()) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isEqual(Vector3D paramVector3D1, Vector3D paramVector3D2) {
        if (paramVector3D1 != null && paramVector3D2 != null && paramVector3D1.getX() == paramVector3D2.getX() && paramVector3D1.getY() == paramVector3D2.getY() && paramVector3D1.getZ() == paramVector3D2.getZ()) {
            return true;
        }
        return false;
    }

    private void process() {
        Log.d(TAG, "process: distance " + String.format("%.4f", new Object[]{Double.valueOf(PathDistance)}));
        if (this.PathBuffer3D.size() <= 40 || PathDistance <= effectDistance) {
            reset();
            return;
        }
        int i;
        int size = this.PathBuffer3D.size();
        int headCut = (int) (((double) size) * headOffSet);
        int tailCut = (int) (((double) size) * tailOffSet);
        for (i = 0; i < headCut; i++) {
            this.PathBuffer3D.remove(0);
            this.rotBuffer.remove(0);
        }
        for (i = 0; i < tailCut; i++) {
            size = this.PathBuffer3D.size();
            this.PathBuffer3D.remove(size - 1);
            this.rotBuffer.remove(size - 1);
        }
        Vector localVector1 = this.PathBuffer3D;
        get2DPath(localVector1, getDeviceAxes(this.rotBuffer), getPlaneNormal(localVector1));
    }

    private float[][] get2DMatrix(Vector3D[] deviceAxes, Vector3D normal) {
        Vector3D localVector3D1;
        Vector3D localVector3D2;
        double[] arrayOfDouble = new double[3];
        double d1 = -1.0d;
        int i = -1;
        for (int j = 0; j < arrayOfDouble.length; j++) {
            arrayOfDouble[j] = (double) normal.dotProduct(deviceAxes[j]);
            double d2 = Math.abs(arrayOfDouble[j]);
            if (d2 >= d1) {
                d1 = d2;
                i = j;
            }
        }
        double temp = ((arrayOfDouble[0] * arrayOfDouble[0]) + (arrayOfDouble[1] * arrayOfDouble[1])) + (arrayOfDouble[2] * arrayOfDouble[2]);
        if (((double) Math.abs(normal.getZ())) >= 0.9d) {
            Log.d(TAG, "plane is level");
            localVector3D1 = arrayOfDouble[0] < 0.0d ? Vector3D.ZERO.subtract(normal) : normal;
            localVector3D2 = deviceAxes[0];
        } else {
            Log.d(TAG, "plane is not level");
            Vector3D vector3D = new Vector3D(0.0f, 0.0f, -1.0f);
            if (arrayOfDouble[i] <= 0.0d) {
                localVector3D1 = normal;
            } else {
                localVector3D1 = Vector3D.ZERO.subtract(normal);
            }
        }
        Vector3D localVector3D3 = Vector3D.ZERO.subtract(localVector3D1);
        Vector3D localVector3D5 = localVector3D3.crossProduct(localVector3D2.crossProduct(localVector3D3));
        arrayOfFloat = new float[3][];
        arrayOfFloat[0] = new float[]{localVector3D4.getX(), localVector3D4.getY(), localVector3D4.getZ()};
        arrayOfFloat[1] = new float[]{localVector3D5.getX(), localVector3D5.getY(), localVector3D5.getZ()};
        arrayOfFloat[2] = new float[]{localVector3D3.getX(), localVector3D3.getY(), localVector3D3.getZ()};
        return arrayOfFloat;
    }

    private Vector3D get2DSingleVector(Vector3D pathVector, float[][] Matrix) {
        return new Vector3D(((Matrix[0][0] * pathVector.getX()) + (Matrix[0][1] * pathVector.getY())) + (Matrix[0][2] * pathVector.getZ()), ((Matrix[1][0] * pathVector.getX()) + (Matrix[1][1] * pathVector.getY())) + (Matrix[1][2] * pathVector.getZ()), ((Matrix[2][0] * pathVector.getX()) + (Matrix[2][1] * pathVector.getY())) + (Matrix[2][2] * pathVector.getZ()));
    }

    private void get2DPath(Vector<Vector3D> pathVector, Vector3D[] deviceAxes, Vector3D normal) {
        Vector localVector = this.PathBuffer2D;
        float[][] Matrix2D = get2DMatrix(deviceAxes, normal);
        for (int i = 0; i < pathVector.size(); i++) {
            Vector3D point2D = get2DSingleVector((Vector3D) pathVector.get(i), Matrix2D);
            localVector.add(new Vector3D(-point2D.getX(), -point2D.getY(), 0.0f));
        }
    }

    public boolean isCapturing() {
        return this.capturing;
    }

    public void onSensorData(SensorData paramSensorData) {
        Quaternion localQuaternion = paramSensorData.getAngularPosition();
        Vector3D localVector3D1 = paramSensorData.getLinearAccelerationNoGravity();
        if (this.trigger) {
            Vector3D localVector3D3 = this.integrator.process(localQuaternion.applyInverseTo(new Vector3D(localVector3D1.getX(), localVector3D1.getY(), localVector3D1.getZ())));
            Vector3D localVector3D5 = Vector3D.ZERO.add(1200.0f, localVector3D3).add(79.99999f, localQuaternion.applyInverseTo(new Vector3D(1.0f, 0.0f, 0.0f)));
            if (this.capturing) {
                if (this.prev3DPonit != null) {
                    PathDistance += localVector3D5.subtract(this.prev3DPonit).norm();
                }
                this.PathBuffer3D.add(localVector3D5);
                this.rotBuffer.add(localQuaternion);
                if (this.PathBuffer3D.size() > 1000) {
                    this.PathBuffer3D.remove(0);
                    this.rotBuffer.remove(0);
                }
                this.prev3DPonit = localVector3D5;
            }
            return;
        }
        if (localVector3D1.norm() > 0.3499999940395355d) {
            this.trigger = true;
        }
    }

    public void reset() {
        reset(true);
    }

    public void reset(boolean paramBoolean) {
        this.PathBuffer3D = new Vector(1000);
        this.PathBuffer2D = new Vector(1000);
        this.rotBuffer = new Vector(1000);
        this.integrator = new DoubleIntegrator3D(0.01f, 0.008f);
        this.integrator.resetPosition();
        this.prev3DPonit = null;
        PathDistance = 0.0d;
        this.trigger = false;
    }

    public void setCapturing(boolean capture) {
        if (capture) {
            reset(true);
        } else {
            process();
        }
        this.capturing = capture;
    }
}
