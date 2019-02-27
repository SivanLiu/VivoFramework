package com.vivo.mediaplayer.lib;

import android.os.Handler;
import java.lang.ref.WeakReference;

public abstract class WeakHandler<T> extends Handler {
    private WeakReference<T> mOwner;

    public WeakHandler(T owner) {
        this.mOwner = new WeakReference(owner);
    }

    public T getOwner() {
        return this.mOwner.get();
    }
}
