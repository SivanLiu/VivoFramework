package com.vivo.services.motion.gesture;

import android.content.Context;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import com.vivo.services.motion.gesture.gesture.Gesture;
import com.vivo.services.motion.gesture.gesture.GestureLibraries;
import com.vivo.services.motion.gesture.gesture.GestureLibrary;
import com.vivo.services.motion.gesture.gesture.GesturePoint;
import com.vivo.services.motion.gesture.gesture.GestureStroke;
import com.vivo.services.motion.gesture.gesture.Prediction;
import com.vivo.services.motion.gesture.util.Vector3D;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.Vector;

public class GestureDetector {
    private static final int GESTURE_ERROR = 0;
    private static final int GESTURE_M = 1;
    private static final int GESTURE_S = 2;
    private static final int GESTURE_V = 3;
    private static final File GesturesFile = new File(Environment.getExternalStorageDirectory(), "AirWake/vivo_gestures");
    private static final double PerfectOrientationChange = 2.5d;
    private static final double PerfectScore = 1.5d;
    private static final double Pi = 3.1415926d;
    private static final String TAG = "GestureDetector";
    private static Context mContext = null;
    private static boolean mDebug = false;
    private static File mDebugFile = null;
    private static final File mDebugFlagFile = new File(Environment.getExternalStorageDirectory(), "AirWake/debug_gestures");
    private static GestureLibrary mDebugStore = null;
    private static GestureLibrary mStore = null;
    private static Time mTime = null;
    private static final double quarterPi = 0.7853982d;
    private static GestureDetector singleGestureDetect = null;
    private Gesture mCurrentGesture = null;
    private Gesture mCurrentMirrorGesture = null;
    private ArrayList<GesturePoint> mStrokeBuffer;

    public static GestureDetector getInstance(Context context) {
        Log.i(TAG, "getInstance: " + mStore);
        if (mContext == null) {
            mContext = context;
        }
        if (mStore == null) {
            loadGestureLib();
        }
        if (singleGestureDetect == null) {
            singleGestureDetect = new GestureDetector();
        }
        return singleGestureDetect;
    }

    public static void loadGestureLib() {
        Log.i(TAG, "loadGestureLib");
        mDebug = mDebugFlagFile.exists();
        if (mStore == null) {
            if (mDebug) {
                mStore = GestureLibraries.fromFile(GesturesFile);
                mTime = new Time();
                mTime.setToNow();
                mDebugFile = new File(Environment.getExternalStorageDirectory(), "AirWake/gestures_" + String.format("%02d", new Object[]{Integer.valueOf(mTime.month)}) + "_" + String.format("%02d", new Object[]{Integer.valueOf(mTime.monthDay)}) + "_" + String.format("%02d", new Object[]{Integer.valueOf(mTime.hour)}) + "_" + String.format("%02d", new Object[]{Integer.valueOf(mTime.minute)}));
                mDebugStore = GestureLibraries.fromFile(mDebugFile);
            } else {
                mStore = GestureLibraries.fromRawResource(mContext, 50790405);
            }
            if (mStore != null) {
                mStore.load();
            }
            if (mDebug && mDebugStore != null) {
                mDebugStore.load();
            }
            mStore.setOrientationStyle(8);
            mStore.setSequenceType(2);
            Set<String> entries = mStore.getGestureEntries();
            Log.i(TAG, "loadGestureLib Patterns " + entries.size());
            if (entries.size() == 0) {
                mStore = null;
            }
        }
    }

    private int getGestureNum(String name) {
        if (name.charAt(0) == 'm' || name.charAt(0) == 'M') {
            return 1;
        }
        if (name.charAt(0) == 's' || name.charAt(0) == 'S') {
            return 2;
        }
        if (name.charAt(0) == 'v' || name.charAt(0) == 'V') {
            return 3;
        }
        return 0;
    }

    private double orientationFromPos(double y, double x) {
        double f = 1.0d + ((Math.atan2(y, x) + Pi) / quarterPi);
        if (f == 9.0d) {
            return 1.0d;
        }
        return f;
    }

    private boolean orientationFilter(Vector<Vector3D> path) {
        int i;
        int size = path.size();
        int start = (int) (((double) size) * 0.25d);
        int stop = (int) (((double) size) * 0.9d);
        double[] Orientation = new double[(stop - start)];
        double preOrientation = 0.0d;
        double maxChange = 0.0d;
        for (i = start; i < stop - 1; i++) {
            Vector3D path2D0 = (Vector3D) path.get(i);
            float x0 = path2D0.getX();
            float y0 = path2D0.getY();
            Vector3D path2D1 = (Vector3D) path.get(i + 1);
            double orientation = orientationFromPos((double) (path2D1.getY() - y0), (double) (path2D1.getX() - x0));
            if (preOrientation == 0.0d) {
                preOrientation = orientation;
            }
            Orientation[i - start] = (0.2d * orientation) + (0.8d * preOrientation);
            preOrientation = Orientation[i - start];
        }
        for (i = 1; i < Orientation.length - 1; i++) {
            double lastOrientation = Orientation[i];
            for (int j = i; j < Orientation.length - 1; j++) {
                double change = Math.abs(Orientation[j] - lastOrientation);
                if (change > 4.0d) {
                    change = 4.0d - (change - 4.0d);
                }
                if (change > maxChange) {
                    maxChange = change;
                }
            }
        }
        Log.i(TAG, "maxChange " + String.format("%.4f", new Object[]{Double.valueOf(maxChange)}));
        if (maxChange > PerfectOrientationChange) {
            return true;
        }
        return false;
    }

    public void process(Vector<Vector3D> path) {
        if (path == null) {
            Log.i(TAG, "process gesture null");
            this.mCurrentGesture = null;
        } else if (orientationFilter(path)) {
            int size = path.size();
            int stop = size;
            Log.i(TAG, "process size:" + size);
            if (size < 5) {
                this.mCurrentGesture = null;
                return;
            }
            int i;
            Vector3D path2D;
            this.mStrokeBuffer = new ArrayList(size);
            for (i = 0; i < size; i++) {
                path2D = (Vector3D) path.get(i);
                this.mStrokeBuffer.add(new GesturePoint(path2D.getX(), path2D.getY(), (long) (100000000 * i)));
            }
            this.mCurrentGesture = new Gesture();
            this.mCurrentGesture.addStroke(new GestureStroke(this.mStrokeBuffer));
            if (mDebug) {
                mTime = new Time();
                mTime.setToNow();
                mDebugStore.addGesture(String.format("%02d", new Object[]{Integer.valueOf(mTime.monthDay)}) + "_" + String.format("%02d", new Object[]{Integer.valueOf(mTime.hour)}) + "_" + String.format("%02d", new Object[]{Integer.valueOf(mTime.minute)}) + "_" + String.format("%02d", new Object[]{Integer.valueOf(mTime.second)}), this.mCurrentGesture);
                mDebugStore.save();
            }
            this.mStrokeBuffer = new ArrayList(size);
            for (i = 0; i < size; i++) {
                path2D = (Vector3D) path.get(i);
                this.mStrokeBuffer.add(new GesturePoint(-path2D.getX(), path2D.getY(), (long) (100000000 * i)));
            }
            this.mCurrentMirrorGesture = new Gesture();
            this.mCurrentMirrorGesture.addStroke(new GestureStroke(this.mStrokeBuffer));
        } else {
            Log.i(TAG, "process gesture is a line");
            this.mCurrentGesture = null;
        }
    }

    public int recognition() {
        if (this.mCurrentGesture == null || this.mCurrentGesture.getStrokesCount() == 0 || ((GestureStroke) this.mCurrentGesture.getStrokes().get(0)).length < 5.0f) {
            Log.e(TAG, "Gesture error");
            return 0;
        } else if (mStore == null) {
            Log.e(TAG, "Patterns error");
            return 0;
        } else {
            double maxScore = 0.0d;
            String maxPredName = null;
            Prediction prediction = null;
            for (Prediction pred : mStore.recognize(this.mCurrentGesture)) {
                if (pred.score > maxScore) {
                    maxScore = pred.score;
                    maxPredName = pred.name;
                    prediction = pred;
                }
            }
            Log.i(TAG, "max score: " + maxScore + " pred:" + maxPredName);
            double maxScoreMirror = 0.0d;
            String maxPredNameMirror = null;
            Prediction predictionMirror = null;
            for (Prediction pred2 : mStore.recognize(this.mCurrentMirrorGesture)) {
                if (pred2.score > maxScoreMirror) {
                    maxScoreMirror = pred2.score;
                    maxPredNameMirror = pred2.name;
                    predictionMirror = pred2;
                }
            }
            Log.i(TAG, "mirror score: " + maxScoreMirror + " pred:" + maxPredNameMirror);
            if (maxScoreMirror <= PerfectScore || maxScore <= PerfectScore || Math.abs(maxScore - maxScoreMirror) >= 0.5d) {
                if (maxScoreMirror > maxScore) {
                    if (maxScoreMirror > PerfectScore) {
                        return getGestureNum(predictionMirror.name);
                    }
                } else if (maxScore > PerfectScore) {
                    return getGestureNum(prediction.name);
                }
                return 0;
            }
            return getGestureNum(prediction.name);
        }
    }
}
