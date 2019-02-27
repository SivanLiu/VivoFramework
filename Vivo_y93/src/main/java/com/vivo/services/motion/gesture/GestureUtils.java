package com.vivo.services.motion.gesture;

import android.util.Log;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Array;

public final class GestureUtils {
    private static final float NONUNIFORM_SCALE = ((float) Math.sqrt(2.0d));
    private static final float SCALING_THRESHOLD = 0.26f;
    private static final String TAG = "GestureUtils";

    static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close stream", e);
            }
        }
    }

    private static void plot(float x, float y, float[] sample, int sampleSize) {
        if (x < NONUNIFORM_SCALE) {
            x = NONUNIFORM_SCALE;
        }
        if (y < NONUNIFORM_SCALE) {
            y = NONUNIFORM_SCALE;
        }
        int xFloor = (int) Math.floor((double) x);
        int xCeiling = (int) Math.ceil((double) x);
        int yFloor = (int) Math.floor((double) y);
        int yCeiling = (int) Math.ceil((double) y);
        int index;
        if (x == ((float) xFloor) && y == ((float) yFloor)) {
            index = (yCeiling * sampleSize) + xCeiling;
            if (sample[index] < 1.0f) {
                sample[index] = 1.0f;
                return;
            }
            return;
        }
        double xFloorSq = Math.pow((double) (((float) xFloor) - x), 2.0d);
        double yFloorSq = Math.pow((double) (((float) yFloor) - y), 2.0d);
        double xCeilingSq = Math.pow((double) (((float) xCeiling) - x), 2.0d);
        double yCeilingSq = Math.pow((double) (((float) yCeiling) - y), 2.0d);
        float topLeft = (float) Math.sqrt(xFloorSq + yFloorSq);
        float topRight = (float) Math.sqrt(xCeilingSq + yFloorSq);
        float btmLeft = (float) Math.sqrt(xFloorSq + yCeilingSq);
        float btmRight = (float) Math.sqrt(xCeilingSq + yCeilingSq);
        float sum = ((topLeft + topRight) + btmLeft) + btmRight;
        float value = topLeft / sum;
        index = (yFloor * sampleSize) + xFloor;
        if (value > sample[index]) {
            sample[index] = value;
        }
        value = topRight / sum;
        index = (yFloor * sampleSize) + xCeiling;
        if (value > sample[index]) {
            sample[index] = value;
        }
        value = btmLeft / sum;
        index = (yCeiling * sampleSize) + xFloor;
        if (value > sample[index]) {
            sample[index] = value;
        }
        value = btmRight / sum;
        index = (yCeiling * sampleSize) + xCeiling;
        if (value > sample[index]) {
            sample[index] = value;
        }
    }

    static float[] computeCentroid(float[] points) {
        float centerX = NONUNIFORM_SCALE;
        float centerY = NONUNIFORM_SCALE;
        int i = 0;
        while (i < points.length) {
            centerX += points[i];
            i++;
            centerY += points[i];
            i++;
        }
        return new float[]{(2.0f * centerX) / ((float) points.length), (2.0f * centerY) / ((float) points.length)};
    }

    public static float[][] computeCoVariance(float[] points) {
        float[] fArr;
        float[][] array = (float[][]) Array.newInstance(Float.TYPE, new int[]{2, 2});
        array[0][0] = 0.0f;
        array[0][1] = 0.0f;
        array[1][0] = 0.0f;
        array[1][1] = 0.0f;
        int count = points.length;
        int i = 0;
        while (i < count) {
            float x = points[i];
            i++;
            float y = points[i];
            fArr = array[0];
            fArr[0] = fArr[0] + (x * x);
            fArr = array[0];
            fArr[1] = fArr[1] + (x * y);
            array[1][0] = array[0][1];
            fArr = array[1];
            fArr[1] = fArr[1] + (y * y);
            i++;
        }
        fArr = array[0];
        fArr[0] = fArr[0] / ((float) (count / 2));
        fArr = array[0];
        fArr[1] = fArr[1] / ((float) (count / 2));
        fArr = array[1];
        fArr[0] = fArr[0] / ((float) (count / 2));
        fArr = array[1];
        fArr[1] = fArr[1] / ((float) (count / 2));
        return array;
    }

    static float computeTotalLength(float[] points) {
        float sum = NONUNIFORM_SCALE;
        for (int i = 0; i < points.length - 4; i += 2) {
            float dx = points[i + 2] - points[i];
            float dy = points[i + 3] - points[i + 1];
            sum = (float) (((double) sum) + Math.sqrt((double) ((dx * dx) + (dy * dy))));
        }
        return sum;
    }

    static float computeStraightness(float[] points) {
        float dx = points[2] - points[0];
        float dy = points[3] - points[1];
        return ((float) Math.sqrt((double) ((dx * dx) + (dy * dy)))) / computeTotalLength(points);
    }

    static float computeStraightness(float[] points, float totalLen) {
        float dx = points[2] - points[0];
        float dy = points[3] - points[1];
        return ((float) Math.sqrt((double) ((dx * dx) + (dy * dy)))) / totalLen;
    }

    static float squaredEuclideanDistance(float[] vector1, float[] vector2) {
        float squaredDistance = NONUNIFORM_SCALE;
        int size = vector1.length;
        for (int i = 0; i < size; i++) {
            float difference = vector1[i] - vector2[i];
            squaredDistance += difference * difference;
        }
        return squaredDistance / ((float) size);
    }

    static float cosineDistance(float[] vector1, float[] vector2) {
        float sum = NONUNIFORM_SCALE;
        for (int i = 0; i < vector1.length; i++) {
            sum += vector1[i] * vector2[i];
        }
        return (float) Math.acos((double) sum);
    }

    static float minimumCosineDistance(float[] vector1, float[] vector2, int numOrientations) {
        int len = vector1.length;
        float a = NONUNIFORM_SCALE;
        float b = NONUNIFORM_SCALE;
        for (int i = 0; i < len; i += 2) {
            a += (vector1[i] * vector2[i]) + (vector1[i + 1] * vector2[i + 1]);
            b += (vector1[i] * vector2[i + 1]) - (vector1[i + 1] * vector2[i]);
        }
        if (a == NONUNIFORM_SCALE) {
            return 1.5707964f;
        }
        float tan = b / a;
        double angle = Math.atan((double) tan);
        if (numOrientations > 2 && Math.abs(angle) >= 3.141592653589793d / ((double) numOrientations)) {
            return (float) Math.acos((double) a);
        }
        double cosine = Math.cos(angle);
        return (float) Math.acos((((double) a) * cosine) + (((double) b) * (cosine * ((double) tan))));
    }

    public static float[] computeOrientation(float[][] covarianceMatrix) {
        float[] targetVector = new float[2];
        if (covarianceMatrix[0][1] == NONUNIFORM_SCALE || covarianceMatrix[1][0] == NONUNIFORM_SCALE) {
            targetVector[0] = 1.0f;
            targetVector[1] = NONUNIFORM_SCALE;
        }
        float value = ((-covarianceMatrix[0][0]) - covarianceMatrix[1][1]) / 2.0f;
        float rightside = (float) Math.sqrt(Math.pow((double) value, 2.0d) - ((double) ((covarianceMatrix[0][0] * covarianceMatrix[1][1]) - (covarianceMatrix[0][1] * covarianceMatrix[1][0]))));
        float lambda1 = (-value) + rightside;
        float lambda2 = (-value) - rightside;
        if (lambda1 == lambda2) {
            targetVector[0] = NONUNIFORM_SCALE;
            targetVector[1] = NONUNIFORM_SCALE;
        } else {
            float lambda = lambda1 > lambda2 ? lambda1 : lambda2;
            targetVector[0] = 1.0f;
            targetVector[1] = (lambda - covarianceMatrix[0][0]) / covarianceMatrix[0][1];
        }
        return targetVector;
    }

    static float[] rotate(float[] points, float angle) {
        float cos = (float) Math.cos((double) angle);
        float sin = (float) Math.sin((double) angle);
        int size = points.length;
        for (int i = 0; i < size; i += 2) {
            float y = (points[i] * sin) + (points[i + 1] * cos);
            points[i] = (points[i] * cos) - (points[i + 1] * sin);
            points[i + 1] = y;
        }
        return points;
    }

    static float[] translate(float[] points, float dx, float dy) {
        int size = points.length;
        for (int i = 0; i < size; i += 2) {
            points[i] = points[i] + dx;
            int i2 = i + 1;
            points[i2] = points[i2] + dy;
        }
        return points;
    }

    static float[] scale(float[] points, float sx, float sy) {
        int size = points.length;
        for (int i = 0; i < size; i += 2) {
            points[i] = points[i] * sx;
            int i2 = i + 1;
            points[i2] = points[i2] * sy;
        }
        return points;
    }
}
