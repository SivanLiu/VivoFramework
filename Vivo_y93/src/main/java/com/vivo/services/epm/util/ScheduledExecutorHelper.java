package com.vivo.services.epm.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorHelper {
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final String TAG = "ScheduledExecutor";
    private static ScheduledExecutorHelper sInstance = null;
    private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(CORE_POOL_SIZE);

    private ScheduledExecutorHelper() {
    }

    public static synchronized ScheduledExecutorHelper getDefaultScheduledExecutor() {
        ScheduledExecutorHelper scheduledExecutorHelper;
        synchronized (ScheduledExecutorHelper.class) {
            if (sInstance == null) {
                sInstance = new ScheduledExecutorHelper();
            }
            scheduledExecutorHelper = sInstance;
        }
        return scheduledExecutorHelper;
    }

    public void schedule(ScheduledTask task, long delayedTimeInMillis) {
        task.setScheduleFuture(service.schedule(task, delayedTimeInMillis, TimeUnit.MILLISECONDS));
    }

    public void scheduleAtFixedRate(ScheduledTask task, long firstdelayedTimeInMillis, long periodMillis) {
        task.setScheduleFuture(service.scheduleAtFixedRate(task, firstdelayedTimeInMillis, periodMillis, TimeUnit.MILLISECONDS));
    }

    public void scheduleWithFixedDelay(ScheduledTask task, long firstdelayedTimeInMillis, long periodMillis) {
        task.setScheduleFuture(service.scheduleWithFixedDelay(task, firstdelayedTimeInMillis, periodMillis, TimeUnit.MILLISECONDS));
    }
}
