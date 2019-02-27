package com.qti.location.sdk.collection;

import org.json.JSONArray;

public class CachedInteger {
    private static final int CACHED_BUFFER_MAX = 60;
    private static final int DEFAULT_INVALID_VALUE = -1;
    private int[] buffer = new int[CACHED_BUFFER_MAX];
    private boolean full = false;
    private int index = 0;
    private int mLast = DEFAULT_INVALID_VALUE;

    public void reset() {
        this.index = 0;
        this.full = false;
        this.mLast = DEFAULT_INVALID_VALUE;
        for (int i = 0; i < CACHED_BUFFER_MAX; i++) {
            this.buffer[i] = DEFAULT_INVALID_VALUE;
        }
    }

    public void add(int count) {
        this.buffer[this.index] = count;
        this.mLast = count;
        this.index = (this.index + 1) % CACHED_BUFFER_MAX;
        if (this.index == 0 && (this.full ^ 1) != 0) {
            this.full = true;
        }
    }

    public int getLast() {
        return this.mLast;
    }

    public int[] getBuffer() {
        return this.buffer;
    }

    public int getIndex() {
        return this.index;
    }

    public int getBufferMax() {
        return CACHED_BUFFER_MAX;
    }

    public String getString() {
        int i;
        String str = "";
        if (this.full) {
            i = this.index;
            while (i < CACHED_BUFFER_MAX) {
                str = str + (i == this.index ? "" : ",") + this.buffer[i];
                i++;
            }
        }
        for (i = 0; i < this.index; i++) {
            str = str + (str.isEmpty() ? "" : ",") + this.buffer[i];
        }
        return str;
    }

    public JSONArray toJson() {
        int i;
        JSONArray arr = new JSONArray();
        if (this.full) {
            for (i = this.index; i < CACHED_BUFFER_MAX; i++) {
                arr.put(this.buffer[i]);
            }
        }
        for (i = 0; i < this.index; i++) {
            arr.put(this.buffer[i]);
        }
        return arr;
    }
}
