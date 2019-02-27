package com.vivo.services.motion.gesture;

import android.content.Context;
import android.graphics.Paint;
import android.hardware.SensorEvent;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.services.motion.gesture.util.Quaternion;
import com.vivo.services.motion.gesture.util.Vector3D;
import java.util.Vector;

public class MotionTestView {
    private static final float NORM_SIZE = 200.0f;
    private static final String TAG = "MotionTest";
    private static GestureUtils mGestureUtils;
    private static MotionTestView singleMotionTestView = null;
    public boolean capturing = false;
    protected boolean draw2d = true;
    protected boolean draw3d = true;
    protected String mGestureStatus = Events.DEFAULT_SORT_ORDER;
    public Vector3D[] mNormal = null;
    private Paint mPaint = new Paint();
    public Vector<Vector3D> mPath2d = null;
    public Vector3D[] mPath2dNomal = null;
    public Vector<Vector3D> mPath3d = null;
    protected Quaternion mRotation;
    protected double velocity;

    private MotionTestView() {
        mGestureUtils = new GestureUtils();
    }

    public static MotionTestView getInstance(Context context) {
        if (singleMotionTestView == null) {
            singleMotionTestView = new MotionTestView();
        }
        return singleMotionTestView;
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == 11) {
            float[] arrayOfFloat1 = event.values;
            float[] arrayOfFloat2 = new float[4];
            if (arrayOfFloat1.length == 3) {
                arrayOfFloat2[0] = arrayOfFloat1[1];
                arrayOfFloat2[1] = arrayOfFloat1[0];
                arrayOfFloat2[2] = -arrayOfFloat1[2];
                this.mRotation = Quaternion.fromUnitVector(arrayOfFloat2[0], arrayOfFloat2[1], arrayOfFloat2[2]);
            }
        }
    }

    public void normalsize() {
        if (this.mPath2d != null && this.mPath2d.size() > 1) {
            int i;
            int max = this.mPath2d.size();
            float maxX = ((Vector3D) this.mPath2d.get(0)).getX();
            float minX = maxX;
            float maxY = ((Vector3D) this.mPath2d.get(0)).getY();
            float minY = maxY;
            for (i = 1; i < max; i++) {
                float VectorX = ((Vector3D) this.mPath2d.get(i)).getX();
                float VectorY = ((Vector3D) this.mPath2d.get(i)).getY();
                if (VectorX > maxX) {
                    maxX = VectorX;
                }
                if (VectorX < minX) {
                    minX = VectorX;
                }
                if (VectorY > maxY) {
                    maxY = VectorY;
                }
                if (VectorY < minY) {
                    minY = VectorY;
                }
            }
            float max_diff_x = maxX - minX;
            float max_diff_y = maxY - minY;
            float ratio = NORM_SIZE / (max_diff_x > max_diff_y ? max_diff_x : max_diff_y);
            float offsetX = NORM_SIZE - maxX;
            float offsetY = 50.0f - minY;
            this.mPath2dNomal = new Vector3D[max];
            if (((double) ratio) < 1.0d) {
                for (i = 0; i < max; i++) {
                    this.mPath2dNomal[i] = new Vector3D((((Vector3D) this.mPath2d.get(i)).getX() * ratio) + offsetX, (((Vector3D) this.mPath2d.get(i)).getY() * ratio) + offsetY, 0.0f);
                }
                return;
            }
            for (i = 0; i < max; i++) {
                this.mPath2dNomal[i] = new Vector3D(((Vector3D) this.mPath2d.get(i)).getX() + offsetX, ((Vector3D) this.mPath2d.get(i)).getY() + offsetY, 0.0f);
            }
        }
    }

    private void dataRotate() {
        int i;
        int num = 0;
        if (!(this.mPath2dNomal == null || this.mPath2d == null || this.mPath2d.size() <= 5)) {
            num = this.mPath2d.size();
        }
        float[] points = new float[(num * 2)];
        for (i = 0; i < num; i++) {
            points[i * 2] = this.mPath2dNomal[i].getX();
            points[(i * 2) + 1] = this.mPath2dNomal[i].getY();
        }
        GestureUtils gestureUtils = mGestureUtils;
        float[] centerData = GestureUtils.computeCentroid(points);
        float centerDataX = centerData[0];
        float centerDataY = centerData[1];
        gestureUtils = mGestureUtils;
        GestureUtils.translate(points, -centerDataX, -centerDataY);
        gestureUtils = mGestureUtils;
        float[][] array = GestureUtils.computeCoVariance(points);
        gestureUtils = mGestureUtils;
        float[] targetVector = GestureUtils.computeOrientation(array);
        if (targetVector[0] != 0.0f || targetVector[1] != 0.0f) {
            float angle = (float) Math.atan2((double) targetVector[1], (double) targetVector[0]);
            gestureUtils = mGestureUtils;
            GestureUtils.rotate(points, -angle);
        }
        for (i = 0; i < num; i++) {
            this.mPath2dNomal[i] = new Vector3D(points[i * 2] + centerDataX, (points[(i * 2) + 1] + centerDataY) + 350.0f, 0.0f);
        }
    }

    private int[] project3D(Vector3D paramVector3D) {
        arrayOfInt = new int[2];
        Vector3D localVector3D = this.mRotation.applyTo(paramVector3D).scalarMultiply(0.7f);
        arrayOfInt[0] = ((int) ((localVector3D.getY() * 2000.0f) / (localVector3D.getZ() + 2000.0f))) + 400;
        arrayOfInt[1] = 1000 - ((int) ((localVector3D.getX() * 2000.0f) / (localVector3D.getZ() + 2000.0f)));
        return arrayOfInt;
    }
}
