package com.vivo.services.epm.util;

import android.content.Context;
import android.os.BatteryManager;
import android.util.Slog;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class ScheduledTask implements Runnable {
    private static final boolean EXECUTE_TASK_LOW_BATTERY = true;
    public static final int LOW_BATTERY_THRESHOLD = 20;
    private static final boolean NOT_EXECUTE_TASK_LOW_BATTERY = false;
    private static final String TAG = "ScheduledTask";
    protected Context mContext;
    protected ScheduledFuture<?> mFuture;

    public abstract void doTaskOnce();

    public ScheduledTask(Context context) {
        this.mContext = context;
    }

    public void setScheduleFuture(ScheduledFuture<?> future) {
        this.mFuture = future;
    }

    private int getCurrentBatteryLevel() {
        return ((BatteryManager) this.mContext.getSystemService("batterymanager")).getIntProperty(4);
    }

    public boolean onLowBattery(int currentLevel) {
        return false;
    }

    public int getLowBatteryThreshold() {
        return 20;
    }

    public final void run() {
        int curLevel = getCurrentBatteryLevel();
        int threshold = getLowBatteryThreshold();
        if (curLevel >= threshold) {
            doTaskOnce();
        } else if (onLowBattery(curLevel)) {
            doTaskOnce();
        } else {
            Slog.d(TAG, "don't execute the task, because current battery level is " + curLevel + ", but threshold is " + threshold);
        }
    }

    public final synchronized boolean cancel() {
        boolean ret;
        if (this.mFuture != null) {
            ret = this.mFuture.cancel(EXECUTE_TASK_LOW_BATTERY);
            this.mFuture = null;
        } else {
            throw new IllegalStateException("mFuture is null...");
        }
        return ret;
    }

    public final boolean isDone() {
        if (this.mFuture != null) {
            return this.mFuture.isDone();
        }
        throw new IllegalStateException("mFuture is null...");
    }

    public final boolean isCancelled() {
        if (this.mFuture != null) {
            return this.mFuture.isCancelled();
        }
        throw new IllegalStateException("mFuture is null...");
    }

    public final long getDelay() {
        if (this.mFuture != null) {
            return this.mFuture.getDelay(TimeUnit.MILLISECONDS);
        }
        throw new IllegalStateException("mFuture is null...");
    }

    public final synchronized boolean isFutureValid() {
        return this.mFuture != null ? EXECUTE_TASK_LOW_BATTERY : false;
    }
}
