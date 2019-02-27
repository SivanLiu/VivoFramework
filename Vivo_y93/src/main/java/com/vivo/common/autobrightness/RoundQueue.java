package com.vivo.common.autobrightness;

public class RoundQueue<T> {
    private static final int DEFAULT_BUFFER_LEN = 20;
    private static final String TAG = "RoundQueue";
    private T[] buffer;
    private int cursor;
    private int length;
    private String module;
    private int usedLen;

    public RoundQueue() {
        this.length = 20;
        this.module = TAG;
        this.usedLen = 0;
        this.cursor = 0;
        this.buffer = new Object[20];
    }

    public RoundQueue(String mo, int len) {
        if (len > 0) {
            this.length = len;
        } else {
            this.length = 20;
        }
        if (mo == null || mo.length() == 0) {
            this.module = TAG;
        } else {
            this.module = mo;
        }
        this.usedLen = 0;
        this.cursor = 0;
        this.buffer = new Object[this.length];
    }

    public void reset() {
        for (int i = 0; i < this.usedLen; i++) {
            this.buffer[i] = null;
        }
        this.usedLen = 0;
        this.cursor = 0;
    }

    public boolean isFull() {
        return this.usedLen == this.length;
    }

    public T indexOf(int index) {
        if (index < 0 || index >= this.usedLen) {
            return null;
        }
        return this.buffer[index];
    }

    public int size() {
        return this.usedLen;
    }

    public int length() {
        return this.length;
    }

    public T addToQueue(T object) {
        if (this.usedLen < this.length) {
            this.usedLen++;
        }
        if (this.cursor > this.length - 1) {
            this.cursor = 0;
        }
        T ret = this.buffer[this.cursor];
        this.buffer[this.cursor] = object;
        this.cursor = (this.cursor + 1) % this.length;
        return ret;
    }
}
