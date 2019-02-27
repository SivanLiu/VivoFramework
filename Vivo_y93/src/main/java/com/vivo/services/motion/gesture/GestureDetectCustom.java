package com.vivo.services.motion.gesture;

import android.content.Context;
import android.util.Log;

public final class GestureDetectCustom {
    public static final int CODE_LENGTH = 16;
    public static final float GESTURE_LENGTH = 50.0f;
    private static final String LETTER = "LetterGestureRecognition";
    public static final int LETTER_NUM = 3;
    public static final int MAX_DATA_NUM = 5000;
    public static final float RATE = 0.6f;
    private static final String TAG = "GestureDetectCustom";
    private static Context mContext = null;
    private static GestureUtils mGestureUtils = null;
    public static final float mINTERVAL_THS1 = 20.0f;
    public static final float sINTERVAL_THS1 = 20.0f;
    private static GestureDetectCustom singleGestureDetect = null;
    public static final float vINTERVAL_THS1 = 15.0f;
    private int DataSize = 0;
    private float centerDataX;
    private float centerDataY;
    private int[] codeData = new int[5000];
    public float[] dataX = new float[5000];
    public float[] dataY = new float[5000];
    public float maxDataX;
    public float maxDataY;
    public float minDataX;
    public float minDataY;

    public static GestureDetectCustom getInstance(Context context) {
        if (mContext == null) {
            mContext = context;
        }
        if (singleGestureDetect == null) {
            singleGestureDetect = new GestureDetectCustom();
        }
        return singleGestureDetect;
    }

    private GestureDetectCustom() {
        reset();
        mGestureUtils = new GestureUtils();
    }

    public int recognition() {
        int result = 0;
        int[] letter = new int[3];
        extremeData();
        normData();
        centerData();
        letter[0] = vRecognition();
        letter[1] = sRecognition();
        letter[2] = mRecognition();
        dataFlip();
        if (letter[0] == 0) {
            letter[0] = vRecognition();
        }
        if (letter[1] == 0) {
            letter[1] = sRecognition();
        }
        if (letter[2] == 0) {
            letter[2] = mRecognition();
        }
        for (int i = 0; i < 3; i++) {
            if (letter[i] != 0) {
                result = letter[i];
            }
        }
        if (letter[0] != 0) {
            float length = traceLength();
            if (letter[2] != 0) {
                if (length > 75.0f) {
                    result = letter[2];
                } else {
                    result = letter[0];
                }
            }
        }
        Log.d(LETTER, "gesture recognition result:" + result);
        return result;
    }

    public int vRecognition() {
        int[] I = new int[3];
        float[] X = new float[3];
        float minY1 = 0.0f;
        float minY2 = 0.0f;
        float maxY1 = 0.0f;
        float subLength = 0.0f;
        float subLength1 = 0.0f;
        boolean state1 = true;
        boolean state2 = true;
        boolean state3 = true;
        int num = dataSize();
        float length = traceLength();
        if (num < 10) {
            return 0;
        }
        int i;
        boolean z;
        for (i = 1; i < num; i++) {
            subLength1 += (float) Math.sqrt((double) (((this.dataX[i] - this.dataX[i - 1]) * (this.dataX[i] - this.dataX[i - 1])) + ((this.dataY[i] - this.dataY[i - 1]) * (this.dataY[i] - this.dataY[i - 1]))));
            if (subLength1 >= length / 10.0f) {
                break;
            }
            float subLength2 = 0.0f;
            int j = num - 1;
            while (j > 0) {
                subLength2 += (float) Math.sqrt((double) (((this.dataX[j] - this.dataX[j - 1]) * (this.dataX[j] - this.dataX[j - 1])) + ((this.dataY[j] - this.dataY[j - 1]) * (this.dataY[j] - this.dataY[j - 1]))));
                if (subLength2 >= length / 10.0f) {
                    continue;
                    break;
                } else if (((float) Math.sqrt((double) (((this.dataX[i] - this.dataX[j]) * (this.dataX[i] - this.dataX[j])) + ((this.dataY[i] - this.dataY[j]) * (this.dataY[i] - this.dataY[j]))))) < 15.0f) {
                    Log.d(LETTER, "vReturn: too close");
                    return 0;
                } else {
                    j--;
                }
            }
        }
        for (i = 1; i < num; i++) {
            subLength += (float) Math.sqrt((double) (((this.dataX[i] - this.dataX[i - 1]) * (this.dataX[i] - this.dataX[i - 1])) + ((this.dataY[i] - this.dataY[i - 1]) * (this.dataY[i] - this.dataY[i - 1]))));
            if (subLength >= 0.0f && subLength <= length / 8.0f) {
                if (state1) {
                    minY1 = this.dataY[i - 1];
                    X[0] = this.dataX[i - 1];
                    I[0] = i - 1;
                    state1 = false;
                }
                if (this.dataY[i] < minY1) {
                    minY1 = this.dataY[i];
                    X[0] = this.dataX[i];
                    I[0] = i;
                }
            }
            if (subLength > length / 8.0f && subLength < (length / 8.0f) * 7.0f) {
                if (state2) {
                    maxY1 = this.dataY[i - 1];
                    X[1] = this.dataX[i - 1];
                    I[1] = i - 1;
                    state2 = false;
                }
                if (this.dataY[i] > maxY1) {
                    maxY1 = this.dataY[i];
                    X[1] = this.dataX[i];
                    I[1] = i;
                }
            }
            if (subLength >= (length / 8.0f) * 7.0f && subLength <= length) {
                if (state3) {
                    minY2 = this.dataY[i - 1];
                    X[2] = this.dataX[i - 1];
                    I[2] = i - 1;
                    state3 = false;
                }
                if (this.dataY[i] < minY2) {
                    minY2 = this.dataY[i];
                    X[2] = this.dataX[i];
                    I[2] = i;
                }
            }
        }
        float k1 = (maxY1 - minY1) / (X[1] - X[0]);
        float b1 = ((X[0] * maxY1) - (X[1] * minY1)) / (X[0] - X[1]);
        float k2 = (minY2 - maxY1) / (X[2] - X[1]);
        float b2 = ((X[1] * minY2) - (X[2] * maxY1)) / (X[1] - X[2]);
        float y1 = (X[2] * k1) + b1;
        float y2 = (X[0] * k2) + b2;
        String str = LETTER;
        StringBuilder append = new StringBuilder().append(" vdelta11: ").append(maxY1 - minY1 > 16.666666f).append(" vdelta12: ").append(maxY1 - minY2 > 16.666666f).append(" vdelta21: ").append(Math.abs(minY2 - minY1) < 33.333332f).append(" y1: ").append(k1 * y1 > k1 * minY2).append(" y2: ").append(k2 * y2 < k2 * minY1).append(" I01: ").append(I[0] < I[1]).append(" I12: ");
        if (I[1] < I[2]) {
            z = true;
        } else {
            z = false;
        }
        Log.d(str, append.append(z).toString());
        Log.d(LETTER, " vdelta11: " + (maxY1 - minY1) + " vdelta12: " + (maxY1 - minY2) + " vdelta21: " + (minY2 - minY1) + " I0: " + I[0] + " I1: " + I[1] + " I2: " + I[2]);
        Log.d(LETTER, " minY1: " + minY1 + " minY2: " + minY2 + " maxY1: " + maxY1 + " X[0]: " + X[0] + " X[1]: " + X[1] + " X[2]: " + X[2]);
        if (maxY1 - minY1 <= 16.666666f || maxY1 - minY2 <= 16.666666f || Math.abs(minY2 - minY1) >= 33.333332f || k1 * y1 <= k1 * minY2 || k2 * y2 >= k2 * minY1 || I[0] >= I[1] || I[1] >= I[2]) {
            Log.d(LETTER, "vReturn: special point not right");
            return 0;
        }
        int count1 = 0;
        int count2 = 0;
        int num11 = 0;
        int num12 = 0;
        int num21 = 0;
        int num22 = 0;
        for (i = I[0]; i < I[1]; i++) {
            if (this.dataY[i] < this.dataY[i + 1]) {
                count1++;
            }
            if ((this.dataX[i] * k1) + b1 > this.dataY[i]) {
                num11++;
            } else {
                num12++;
            }
        }
        for (i = I[1]; i < I[2] - 1; i++) {
            if (this.dataY[i] > this.dataY[i + 1]) {
                count2++;
            }
            if ((this.dataX[i] * k2) + b2 > this.dataY[i]) {
                num21++;
            } else {
                num22++;
            }
        }
        Log.d(LETTER, " vcount1: " + count1 + " count2: " + count2 + " I0: " + I[0] + " I1: " + I[1] + " I2: " + I[2]);
        Log.d(LETTER, " I10: " + (((I[1] - I[0]) / 5) * 4) + " I21: " + (((I[2] - I[1]) / 5) * 4));
        Log.d(LETTER, " I10: " + (count1 < ((I[1] - I[0]) / 4) * 3) + " I21: " + (count2 < ((I[2] - I[1]) / 4) * 3));
        if (((float) count1) >= ((((float) I[1]) - ((float) I[0])) / 4.0f) * 3.0f && ((float) count2) >= ((((float) I[2]) - ((float) I[1])) / 4.0f) * 3.0f) {
            return 1;
        }
        Log.d(LETTER, "vReturn: gesture maybe others");
        return 0;
    }

    public int sRecognition() {
        int[] I = new int[4];
        float[] Y = new float[4];
        float maxX1 = 0.0f;
        float maxX2 = 0.0f;
        float minX1 = 0.0f;
        float minX2 = 0.0f;
        float subLength = 0.0f;
        float subLength1 = 0.0f;
        boolean state1 = true;
        boolean state2 = true;
        boolean state3 = true;
        boolean state4 = true;
        int num = dataSize();
        float length = traceLength();
        if (num < 10) {
            return 0;
        }
        int i;
        boolean z;
        for (i = 1; i < num; i++) {
            subLength1 += (float) Math.sqrt((double) (((this.dataX[i] - this.dataX[i - 1]) * (this.dataX[i] - this.dataX[i - 1])) + ((this.dataY[i] - this.dataY[i - 1]) * (this.dataY[i] - this.dataY[i - 1]))));
            if (subLength1 >= length / 10.0f) {
                break;
            }
            float subLength2 = 0.0f;
            int j = num - 1;
            while (j > 0) {
                subLength2 += (float) Math.sqrt((double) (((this.dataX[j] - this.dataX[j - 1]) * (this.dataX[j] - this.dataX[j - 1])) + ((this.dataY[j] - this.dataY[j - 1]) * (this.dataY[j] - this.dataY[j - 1]))));
                if (subLength2 >= length / 10.0f) {
                    continue;
                    break;
                } else if (((float) Math.sqrt((double) (((this.dataX[i] - this.dataX[j]) * (this.dataX[i] - this.dataX[j])) + ((this.dataY[i] - this.dataY[j]) * (this.dataY[i] - this.dataY[j]))))) < 20.0f) {
                    Log.d(LETTER, "sReturn: too close");
                    return 0;
                } else {
                    j--;
                }
            }
        }
        for (i = 1; i < num; i++) {
            subLength += (float) Math.sqrt((double) (((this.dataX[i] - this.dataX[i - 1]) * (this.dataX[i] - this.dataX[i - 1])) + ((this.dataY[i] - this.dataY[i - 1]) * (this.dataY[i] - this.dataY[i - 1]))));
            if (subLength >= 0.0f && subLength < length / 8.0f) {
                if (state1) {
                    maxX1 = this.dataX[i - 1];
                    Y[0] = this.dataY[i - 1];
                    I[0] = i - 1;
                    state1 = false;
                }
                if (this.dataX[i] > maxX1) {
                    maxX1 = this.dataX[i];
                    Y[0] = this.dataY[i];
                    I[0] = i;
                }
            }
            if (subLength > 0.0f && subLength < (length / 5.0f) * 3.0f) {
                if (state2) {
                    minX1 = this.dataX[i - 1];
                    Y[1] = this.dataY[i - 1];
                    I[1] = i - 1;
                    state2 = false;
                }
                if (this.dataX[i] < minX1) {
                    minX1 = this.dataX[i];
                    Y[1] = this.dataY[i];
                    I[1] = i;
                }
            }
            if (subLength > (length / 5.0f) * 2.0f && subLength < length) {
                if (state3) {
                    maxX2 = this.dataX[i - 1];
                    Y[2] = this.dataY[i - 1];
                    I[2] = i - 1;
                    state3 = false;
                }
                if (this.dataX[i] > maxX2) {
                    maxX2 = this.dataX[i];
                    Y[2] = this.dataY[i];
                    I[2] = i;
                }
            }
            if (subLength > (length / 8.0f) * 7.0f && subLength <= length) {
                if (state4) {
                    minX2 = this.dataX[i - 1];
                    Y[3] = this.dataY[i - 1];
                    I[3] = i - 1;
                    state4 = false;
                }
                if (this.dataX[i] < minX2) {
                    minX2 = this.dataX[i];
                    Y[3] = this.dataY[i];
                    I[3] = i;
                }
            }
        }
        float k1 = (Y[2] - Y[1]) / (maxX2 - minX1);
        float b1 = ((Y[2] * minX1) - (Y[1] * maxX2)) / (minX1 - maxX2);
        float y11 = (k1 * maxX1) + b1;
        float y12 = (k1 * minX2) + b1;
        float k2 = (Y[3] - Y[0]) / (minX2 - maxX1);
        float b2 = ((Y[3] * maxX1) - (Y[0] * minX2)) / (maxX1 - minX2);
        float y21 = (k2 * minX1) + b2;
        float y22 = (k2 * maxX2) + b2;
        String str = LETTER;
        StringBuilder append = new StringBuilder().append(" sdelta20: ").append(Y[3] - Y[0] > 12.5f).append(" y11: ").append(y11 > Y[0]).append(" y12: ").append(y12 < Y[3]).append(" y21: ").append(y21 > Y[1]).append(" y22: ").append(y22 < Y[2]).append(" I01: ").append(I[0] < I[1]).append(" I12: ");
        if (I[1] < I[2]) {
            z = true;
        } else {
            z = false;
        }
        append = append.append(z).append(" I23: ");
        if (I[2] < I[3]) {
            z = true;
        } else {
            z = false;
        }
        Log.d(str, append.append(z).toString());
        Log.d(LETTER, " sdelta20: " + (Y[3] - Y[0]) + " I0: " + I[0] + " I1: " + I[1] + " I2: " + I[2] + " I3: " + I[3]);
        Log.d(LETTER, " maxX1: " + maxX1 + " maxX2: " + maxX2 + " minX1: " + minX1 + " minX2: " + minX2 + " Y[0]: " + Y[0] + " Y[1]: " + Y[1] + " Y[2]: " + Y[2] + " Y[3]: " + Y[3]);
        if (Y[3] - Y[0] <= 10.0f || I[0] >= I[1] || I[1] >= I[2] || I[2] >= I[3]) {
            Log.d(LETTER, "sReturn: special point not right");
            return 0;
        }
        int count1 = 0;
        int count21 = 0;
        int count22 = 0;
        int count3 = 0;
        for (i = I[0]; i < I[1]; i++) {
            if (this.dataX[i] > this.dataX[i + 1]) {
                count1++;
            }
        }
        i = I[1];
        while (i < I[2]) {
            if (this.dataX[i] < this.dataX[i + 1] && this.dataY[i] < this.dataY[i + 1]) {
                count21++;
            }
            if (this.dataX[i] < this.dataX[i + 1] && this.dataY[i] > this.dataY[i + 1]) {
                count22++;
            }
            i++;
        }
        for (i = I[2]; i < I[3] - 1; i++) {
            if (this.dataX[i] > this.dataX[i + 1]) {
                count3++;
            }
        }
        Log.d(LETTER, " scount1: " + count1 + " count21: " + count21 + " count22: " + count22 + " count3: " + count3 + " I0: " + I[0] + " I1: " + I[1] + " I2: " + I[2] + " I3: " + I[3]);
        Log.d(LETTER, " I10: " + (((I[1] - I[0]) / 4) * 3) + " I21: " + (((I[2] - I[1]) / 4) * 3) + " I32: " + (((I[3] - I[2]) / 4) * 3));
        str = LETTER;
        append = new StringBuilder().append(" I10: ").append(((float) count1) < ((((float) I[1]) - ((float) I[0])) / 4.0f) * 3.0f).append(" I21: ");
        z = ((float) count21) < ((((float) I[2]) - ((float) I[1])) / 4.0f) * 3.0f && ((float) count22) < ((((float) I[2]) - ((float) I[1])) / 4.0f) * 3.0f;
        Log.d(str, append.append(z).append(" I32: ").append(((float) count3) < ((((float) I[3]) - ((float) I[2])) / 4.0f) * 3.0f).toString());
        if (((float) count1) >= ((((float) I[1]) - ((float) I[0])) / 4.0f) * 3.0f && ((float) count3) >= ((((float) I[3]) - ((float) I[2])) / 4.0f) * 3.0f && (((float) count21) >= ((((float) I[2]) - ((float) I[1])) / 4.0f) * 3.0f || ((float) count22) >= ((((float) I[2]) - ((float) I[1])) / 4.0f) * 3.0f)) {
            return 2;
        }
        Log.d(LETTER, "sReturn: gesture maybe others");
        return 0;
    }

    public int mRecognition() {
        int[] I = new int[5];
        float[] X = new float[5];
        float maxY1 = 0.0f;
        float maxY2 = 0.0f;
        float maxY3 = 0.0f;
        float minY1 = 0.0f;
        float minY2 = 0.0f;
        float subLength = 0.0f;
        float subLength1 = 0.0f;
        boolean state1 = true;
        boolean state2 = true;
        boolean state3 = true;
        boolean state4 = true;
        boolean state5 = true;
        int num = dataSize();
        float length = traceLength();
        if (num < 10) {
            return 0;
        }
        int i;
        boolean z;
        for (i = 1; i < num; i++) {
            subLength1 += (float) Math.sqrt((double) (((this.dataX[i] - this.dataX[i - 1]) * (this.dataX[i] - this.dataX[i - 1])) + ((this.dataY[i] - this.dataY[i - 1]) * (this.dataY[i] - this.dataY[i - 1]))));
            if (subLength1 >= length / 10.0f) {
                break;
            }
            float subLength2 = 0.0f;
            int j = num - 1;
            while (j > 0) {
                subLength2 += (float) Math.sqrt((double) (((this.dataX[j] - this.dataX[j - 1]) * (this.dataX[j] - this.dataX[j - 1])) + ((this.dataY[j] - this.dataY[j - 1]) * (this.dataY[j] - this.dataY[j - 1]))));
                if (subLength2 >= length / 10.0f) {
                    continue;
                    break;
                } else if (((float) Math.sqrt((double) (((this.dataX[i] - this.dataX[j]) * (this.dataX[i] - this.dataX[j])) + ((this.dataY[i] - this.dataY[j]) * (this.dataY[i] - this.dataY[j]))))) < 20.0f) {
                    Log.d(LETTER, "mReturn: too close");
                    return 0;
                } else {
                    j--;
                }
            }
        }
        for (i = 1; i < num; i++) {
            subLength += (float) Math.sqrt((double) (((this.dataX[i] - this.dataX[i - 1]) * (this.dataX[i] - this.dataX[i - 1])) + ((this.dataY[i] - this.dataY[i - 1]) * (this.dataY[i] - this.dataY[i - 1]))));
            if (subLength >= 0.0f && subLength <= length / 3.0f) {
                if (state1) {
                    maxY1 = this.dataY[i - 1];
                    X[0] = this.dataX[i - 1];
                    I[0] = i - 1;
                    state1 = false;
                }
                if (this.dataY[i] > maxY1) {
                    maxY1 = this.dataY[i];
                    X[0] = this.dataX[i];
                    I[0] = i;
                }
            }
            if (subLength > length / 5.0f && subLength < (length / 5.0f) * 3.0f) {
                if (state2) {
                    minY1 = this.dataY[i - 1];
                    X[1] = this.dataX[i - 1];
                    I[1] = i - 1;
                    state2 = false;
                }
                if (this.dataY[i] < minY1) {
                    minY1 = this.dataY[i];
                    X[1] = this.dataX[i];
                    I[1] = i;
                }
            }
            if (subLength > length / 3.0f && subLength < (length / 4.0f) * 3.0f) {
                if (state3) {
                    maxY2 = this.dataY[i - 1];
                    X[2] = this.dataX[i - 1];
                    I[2] = i - 1;
                    state3 = false;
                }
                if (this.dataY[i] > maxY2) {
                    maxY2 = this.dataY[i];
                    X[2] = this.dataX[i];
                    I[2] = i;
                }
            }
            if (subLength > (length / 5.0f) * 3.0f && subLength < (length / 6.0f) * 5.0f) {
                if (state4) {
                    minY2 = this.dataY[i - 1];
                    X[3] = this.dataX[i - 1];
                    I[3] = i - 1;
                    state4 = false;
                }
                if (this.dataY[i] < minY2) {
                    minY2 = this.dataY[i];
                    X[3] = this.dataX[i];
                    I[3] = i;
                }
            }
            if (subLength >= (length / 4.0f) * 3.0f && subLength <= length) {
                if (state5) {
                    maxY3 = this.dataY[i - 1];
                    X[4] = this.dataX[i - 1];
                    I[4] = i - 1;
                    state5 = false;
                }
                if (this.dataY[i] > maxY3) {
                    maxY3 = this.dataY[i];
                    X[4] = this.dataX[i];
                    I[4] = i;
                }
            }
        }
        float k1 = (maxY2 - minY1) / (X[2] - X[1]);
        float b1 = ((X[1] * maxY2) - (X[2] * minY1)) / (X[1] - X[2]);
        float y11 = (X[0] * k1) + b1;
        float y12 = (X[3] * k1) + b1;
        float k2 = (minY2 - maxY2) / (X[3] - X[2]);
        float b2 = ((X[2] * minY2) - (X[3] * maxY2)) / (X[2] - X[3]);
        float y21 = (X[1] * k2) + b2;
        float y22 = (X[4] * k2) + b2;
        float k3 = (maxY3 - maxY1) / (X[4] - X[0]);
        float b3 = ((X[0] * maxY3) - (X[4] * maxY1)) / (X[0] - X[4]);
        float y31 = (X[1] * k3) + b3;
        float y32 = (X[3] * k3) + b3;
        float k4 = (minY2 - minY1) / (X[3] - X[1]);
        float b4 = ((X[1] * minY2) - (X[3] * minY1)) / (X[1] - X[3]);
        float y41 = (X[0] * k4) + b4;
        float y42 = (X[2] * k4) + b4;
        float y43 = (X[4] * k4) + b4;
        float abstLength = (float) (((Math.sqrt((double) (((X[1] - X[0]) * (X[1] - X[0])) + ((minY1 - maxY1) * (minY1 - maxY1)))) + Math.sqrt((double) (((X[2] - X[1]) * (X[2] - X[1])) + ((maxY2 - minY1) * (maxY2 - minY1))))) + Math.sqrt((double) (((X[3] - X[2]) * (X[3] - X[2])) + ((minY2 - maxY2) * (minY2 - maxY2))))) + Math.sqrt((double) (((X[4] - X[3]) * (X[4] - X[3])) + ((maxY3 - minY2) * (maxY3 - minY2)))));
        String str = LETTER;
        StringBuilder append = new StringBuilder().append(" mdelta11: ").append(maxY1 - minY1 > 6.25f).append(" mdelta21: ").append(maxY2 - minY1 > 6.25f).append(" mdelta22: ").append(maxY2 - minY2 > 6.25f).append(" mdelta32: ").append(maxY3 - minY2 > 6.25f).append(" X02: ").append(X[0] < X[2]).append(" X24: ").append(X[2] < X[4]).append(" X40: ").append(X[4] - X[0] > 12.5f).append(" X13: ").append(X[1] < X[3]).append(" y11: ").append(k1 * y11 < k1 * maxY1).append(" y12: ").append(k1 * y12 > k1 * minY2).append(" y21: ").append(k2 * y21 < k2 * minY1).append(" y22: ").append(k2 * y22 > k2 * maxY3).append(" y31: ");
        if (k3 * y31 > k3 * minY1) {
            z = true;
        } else {
            z = false;
        }
        append = append.append(z).append(" y32: ");
        if (k3 * y32 > k3 * minY2) {
            z = true;
        } else {
            z = false;
        }
        append = append.append(z).append(" y41: ");
        if (k4 * y41 > k4 * maxY1) {
            z = true;
        } else {
            z = false;
        }
        append = append.append(z).append(" y42: ");
        if (k4 * y42 > k4 * maxY2) {
            z = true;
        } else {
            z = false;
        }
        append = append.append(z).append(" y43: ");
        if (k4 * y43 > k4 * maxY3) {
            z = true;
        } else {
            z = false;
        }
        append = append.append(z).append(" abstLength: ");
        if (abstLength > length / 2.0f) {
            z = true;
        } else {
            z = false;
        }
        append = append.append(z).append(" I01: ");
        if (I[0] < I[1]) {
            z = true;
        } else {
            z = false;
        }
        append = append.append(z).append(" I12: ");
        if (I[1] < I[2]) {
            z = true;
        } else {
            z = false;
        }
        append = append.append(z).append(" I23: ");
        if (I[2] < I[3]) {
            z = true;
        } else {
            z = false;
        }
        append = append.append(z).append(" I34: ");
        if (I[3] < I[4]) {
            z = true;
        } else {
            z = false;
        }
        Log.d(str, append.append(z).toString());
        Log.d(LETTER, " mdelta11: " + (maxY1 - minY1) + " mdelta21: " + (maxY2 - minY1) + " mdelta22: " + (maxY2 - minY2) + " mdelta32: " + (maxY3 - minY2) + " X02: " + (X[0] - X[2]) + " X24: " + (X[2] - X[4]) + " X40: " + (X[4] - X[0]) + " X13: " + (X[1] - X[3]) + " I0: " + I[0] + " I1: " + I[1] + " I2: " + I[2] + " I3: " + I[3] + " I4: " + I[4]);
        Log.d(LETTER, " maxY1: " + maxY1 + " maxY2: " + maxY2 + " maxY3: " + maxY3 + " minY1: " + minY1 + " minY2: " + minY2 + " X[0]: " + X[0] + " X[1]: " + X[1] + " X[2]: " + X[2] + " X[3]: " + X[3] + " X[4]: " + X[4]);
        if (maxY1 - minY1 <= 6.25f || maxY2 - minY1 <= 6.25f || maxY2 - minY2 <= 6.25f || maxY3 - minY2 <= 6.25f || X[4] - X[0] <= 12.5f || k1 * y11 >= k1 * maxY1 || k1 * y12 <= k1 * minY2 || k2 * y21 >= k2 * minY1 || k2 * y22 <= k2 * maxY3 || abstLength <= length / 2.0f || I[0] >= I[1] || I[1] >= I[2] || I[2] >= I[3] || I[3] >= I[4]) {
            Log.d(LETTER, "mReturn: special point not right");
            return 0;
        }
        int count1 = 0;
        int count2 = 0;
        int count3 = 0;
        int count4 = 0;
        for (i = I[0]; i < I[1]; i++) {
            if (this.dataY[i] > this.dataY[i + 1]) {
                count1++;
            }
        }
        for (i = I[1]; i < I[2]; i++) {
            if (this.dataY[i] < this.dataY[i + 1]) {
                count2++;
            }
        }
        for (i = I[2]; i < I[3]; i++) {
            if (this.dataY[i] > this.dataY[i + 1]) {
                count3++;
            }
        }
        for (i = I[3]; i < I[4] - 1; i++) {
            if (this.dataY[i] < this.dataY[i + 1]) {
                count4++;
            }
        }
        Log.d(LETTER, " count1: " + count1 + " count2: " + count2 + " count3: " + count3 + " count4: " + count4);
        Log.d(LETTER, " I10: " + (((I[1] - I[0]) / 4) * 3) + " I21: " + (((I[2] - I[1]) / 4) * 3) + " I32: " + (((I[3] - I[2]) / 4) * 3) + " I43: " + (((I[4] - I[3]) / 4) * 3));
        Log.d(LETTER, " I10: " + (count1 < ((I[1] - I[0]) / 4) * 3) + " I21: " + (count2 < ((I[2] - I[1]) / 4) * 3) + " I32: " + (count3 < ((I[3] - I[2]) / 4) * 3) + " I43: " + (count4 < ((I[4] - I[3]) / 4) * 3));
        if (((float) count2) >= ((((float) I[2]) - ((float) I[1])) / 8.0f) * 5.0f && ((float) count3) >= ((((float) I[3]) - ((float) I[2])) / 8.0f) * 5.0f && ((float) count4) >= ((((float) I[4]) - ((float) I[3])) / 8.0f) * 5.0f) {
            return 3;
        }
        Log.d(LETTER, "mReturn: gesture maybe others");
        return 0;
    }

    public float traceLength() {
        float length = 0.0f;
        for (int i = 1; i < dataSize(); i++) {
            length += (float) Math.sqrt((double) (((this.dataX[i] - this.dataX[i - 1]) * (this.dataX[i] - this.dataX[i - 1])) + ((this.dataY[i] - this.dataY[i - 1]) * (this.dataY[i] - this.dataY[i - 1]))));
        }
        return length;
    }

    public void centerData() {
        float sumX = 0.0f;
        float sumY = 0.0f;
        int num = dataSize();
        for (int i = 1; i < num; i++) {
            sumX += this.dataX[i];
            sumY += this.dataY[i];
        }
        this.centerDataX = sumX / ((float) num);
        this.centerDataY = sumY / ((float) num);
    }

    public void extremeData() {
        int num = dataSize();
        this.minDataX = this.dataX[0];
        this.maxDataX = this.dataX[0];
        this.minDataY = this.dataY[0];
        this.maxDataY = this.dataY[0];
        for (int i = 1; i < num; i++) {
            if (this.dataX[i] < this.minDataX) {
                this.minDataX = this.dataX[i];
            }
            if (this.dataX[i] > this.maxDataX) {
                this.maxDataX = this.dataX[i];
            }
            if (this.dataY[i] < this.minDataY) {
                this.minDataY = this.dataY[i];
            }
            if (this.dataY[i] > this.maxDataY) {
                this.maxDataY = this.dataY[i];
            }
        }
        Log.d(TAG, "minDataX: " + this.minDataX + " maxDataX: " + this.maxDataX + " minDataY: " + this.minDataY + " maxDataY: " + this.maxDataY);
    }

    public int codeFromDeltaPos(float x, float y) {
        int num = ((int) Math.round(((Math.atan2((double) x, (double) y) + 3.141592653589793d) * 4.0d) / 3.141592653589793d)) + 1;
        if (num == 9) {
            return 1;
        }
        return num;
    }

    public void normData() {
        int num = dataSize();
        float deltaY = this.maxDataY - this.minDataY;
        Log.d(TAG, "normdeltaX: " + (this.maxDataX - this.minDataX) + " deltaY: " + deltaY);
        for (int i = 0; i < num; i++) {
            this.dataX[i] = (this.dataX[i] * 50.0f) / deltaY;
            this.dataY[i] = (this.dataY[i] * 50.0f) / deltaY;
        }
    }

    public void dataFlip() {
        int num = dataSize();
        for (int i = 0; i < num; i++) {
            this.dataX[i] = this.dataX[i] + ((this.centerDataX - this.dataX[i]) * 2.0f);
        }
    }

    public void dataRotate() {
        int i;
        int num = dataSize();
        float[] points = new float[(num * 2)];
        for (i = 0; i < num; i++) {
            points[i * 2] = this.dataX[i];
            points[(i * 2) + 1] = this.dataY[i];
        }
        Log.d(TAG, "++++++++step1++++++++");
        GestureUtils gestureUtils = mGestureUtils;
        GestureUtils.translate(points, -this.centerDataX, -this.centerDataY);
        gestureUtils = mGestureUtils;
        float[][] array = GestureUtils.computeCoVariance(points);
        Log.d(TAG, "++++++++step2++++++++");
        gestureUtils = mGestureUtils;
        float[] targetVector = GestureUtils.computeOrientation(array);
        Log.d(TAG, "++++++++step3++++++++");
        if (targetVector[0] != 0.0f || targetVector[1] != 0.0f) {
            float angle = (float) Math.atan2((double) targetVector[1], (double) targetVector[0]);
            gestureUtils = mGestureUtils;
            GestureUtils.rotate(points, -angle);
        }
        Log.d(TAG, "++++++++step4++++++++");
        for (i = 0; i < num; i++) {
            this.dataX[i] = points[i * 2] + this.centerDataX;
            this.dataY[i] = points[(i * 2) + 1] + this.centerDataY;
        }
        Log.d(TAG, "++++++++step5++++++++");
    }

    public void path2dData(float x, float y, int i) {
        this.dataX[i] = x;
        this.dataY[i] = y;
        Log.d(TAG, "orix: " + this.dataX[i] + " y: " + this.dataY[i]);
    }

    public int dataSize() {
        int num = 0;
        int i = 0;
        while (i < 5000) {
            if (this.dataX[i] == 0.0f && this.dataY[i] == 0.0f) {
                return num;
            }
            num++;
            i++;
        }
        return num;
    }

    public void reset() {
        int i;
        for (i = 0; i < 5000; i++) {
            this.dataX[i] = 0.0f;
            this.dataY[i] = 0.0f;
        }
        for (i = 0; i < 16; i++) {
            this.codeData[i] = 0;
        }
    }
}
