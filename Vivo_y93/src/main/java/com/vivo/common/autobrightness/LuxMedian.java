package com.vivo.common.autobrightness;

import android.util.Slog;
import java.util.Arrays;

public class LuxMedian {
    private static final int DEFAULT_BUFFER_LEN = AblConfig.getLuxMedianBufferLen();
    private static final String TAG = "LuxMedian";
    private float average;
    private int[] buffer;
    private int cursor;
    private int length;
    private float max;
    private String module;
    private int usedLen;

    public LuxMedian() {
        this.length = DEFAULT_BUFFER_LEN;
        this.module = TAG;
        this.usedLen = 0;
        this.cursor = 0;
        this.buffer = new int[this.length];
        this.average = 0.0f;
    }

    public LuxMedian(String mo, int len) {
        if (len > 0) {
            this.length = len;
        } else {
            this.length = DEFAULT_BUFFER_LEN;
        }
        if (mo == null || mo.length() == 0) {
            this.module = TAG;
        } else {
            this.module = mo;
        }
        this.usedLen = 0;
        this.cursor = 0;
        this.buffer = new int[this.length];
        this.average = 0.0f;
    }

    public void reset() {
        this.usedLen = 0;
        this.cursor = 0;
        this.average = 0.0f;
    }

    public void dump() {
        String allData = this.module + " : ";
        int i = 0;
        while (i < this.usedLen) {
            this.module += i + ":" + String.valueOf(this.buffer[i]) + ",";
            i++;
        }
        this.module += "average:" + this.average;
        Slog.d(TAG, allData);
    }

    public float getAverage() {
        return this.average;
    }

    public float getMax() {
        return this.max;
    }

    public int getUsedLen() {
        return this.usedLen;
    }

    private int getLightMedian() {
        float luxSum = 0.0f;
        if (this.buffer == null || this.usedLen < 1 || this.buffer.length < 1) {
            Slog.e(TAG, "getLightMedian source is null or length less than 1 or usedLen=" + this.usedLen);
            return -1;
        }
        int len = this.usedLen;
        if (len > this.buffer.length) {
            len = this.buffer.length;
        }
        int[] dest = new int[len];
        if (dest.length == 0) {
            Slog.d(TAG, this.module + " : " + " dest lenght is 0, return -1.");
            return -1;
        }
        for (int i = 0; i < len; i++) {
            dest[i] = this.buffer[i];
            luxSum += (float) this.buffer[i];
        }
        this.average = luxSum / ((float) len);
        Arrays.sort(dest);
        double pos1 = Math.floor((((double) len) - 1.0d) / 2.0d);
        double pos2 = Math.ceil((((double) len) - 1.0d) / 2.0d);
        if (((int) pos1) < 0 || ((int) pos1) >= dest.length || ((int) pos2) < 0 || ((int) pos2) >= dest.length) {
            Slog.d(TAG, "AutoBrightnessOops, index out of bounds. len = " + len + ", dest.length = " + dest.length + ", pos1 = " + pos1 + ", pos2 = " + pos2 + ", (int)pos1 = " + ((int) pos1) + ", (int)pos2 = " + ((int) pos2));
            return dest[0];
        }
        double median;
        if (pos1 == pos2) {
            median = (double) dest[(int) pos1];
        } else {
            median = ((double) (dest[(int) pos1] + dest[(int) pos2])) / 2.0d;
        }
        return (int) median;
    }

    private void putToMedianBuffer(int lux) {
        if (this.usedLen == 0) {
            this.max = (float) lux;
        } else if (((float) lux) > this.max) {
            this.max = (float) lux;
        }
        if (this.usedLen < this.length) {
            this.usedLen++;
        }
        if (this.cursor > this.length - 1) {
            this.cursor = 0;
        }
        this.buffer[this.cursor] = lux;
        this.cursor = (this.cursor + 1) % this.length;
    }

    public int putAndGetLightMedian(int lux) {
        putToMedianBuffer(lux);
        int median = getLightMedian();
        if (AblConfig.isDebug()) {
            Slog.d(TAG, this.module + " : " + "lux = " + lux + " after median=" + median);
        }
        return median;
    }

    public String toString() {
        return "length=" + this.length + ";usedLen=" + this.usedLen + ";average=" + this.average + ";";
    }
}
