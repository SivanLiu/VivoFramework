package com.vivo.services.rms.sdk;

import android.util.Log;
import java.util.ArrayList;
import java.util.Stack;

public final class ObjectCache<T> {
    private static final String TAG = "ObjectCache";
    private Stack<T> mCache = new Stack();
    private final int mCapacity;
    private final Class<? extends T> mClass;

    public ObjectCache(Class<? extends T> clazz, int capacity) {
        this.mClass = clazz;
        this.mCapacity = capacity;
    }

    public T pop() {
        synchronized (this) {
            if (this.mCache.isEmpty()) {
                try {
                    T o = this.mClass.newInstance();
                    return o;
                } catch (Exception e) {
                    Log.e(TAG, "instance : " + e);
                    return null;
                }
            }
            T pop = this.mCache.pop();
            return pop;
        }
    }

    /* JADX WARNING: Missing block: B:17:0x002c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void put(T cache) {
        synchronized (this) {
            if (cache == null) {
                return;
            }
            for (int i = this.mCache.size() - 1; i >= 0; i--) {
                if (this.mCache.get(i) == cache) {
                    return;
                }
            }
            if (this.mCache.size() < this.mCapacity) {
                this.mCache.push(cache);
            }
        }
    }

    public void put(ArrayList<T> caches) {
        synchronized (this) {
            for (Object cache : caches) {
                if (this.mCache.size() >= this.mCapacity) {
                    break;
                }
                put(cache);
            }
        }
    }

    public void clear() {
        synchronized (this) {
            this.mCache.clear();
        }
    }
}
