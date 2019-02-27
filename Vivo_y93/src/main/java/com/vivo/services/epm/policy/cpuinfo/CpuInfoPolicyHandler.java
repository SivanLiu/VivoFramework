package com.vivo.services.epm.policy.cpuinfo;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Slog;
import com.vivo.services.epm.BaseExceptionPolicyHandler;
import com.vivo.services.epm.EventData;
import com.vivo.services.epm.policy.cpuinfo.CpuInfoConfigurationManager.ICpuInfoEnabledChangeCallback;
import com.vivo.services.epm.util.ScheduledExecutorHelper;

public class CpuInfoPolicyHandler extends BaseExceptionPolicyHandler {
    private static final int MSG_POLL_ENABLE_CHANGED = 1;
    private static final long START_UP_DELAY_MILLIS = 60000;
    static final String TAG = "CpuInfoPolicyHandler";
    private volatile boolean isCpuInfoPolicyEnabled = true;
    private ICpuInfoEnabledChangeCallback mCallback = new ICpuInfoEnabledChangeCallback() {
        public void onCpuInfoEnableChanged(boolean enabled, long pollInterval) {
            Slog.d(CpuInfoPolicyHandler.TAG, "onCpuInfoEnableChanged enabled=" + enabled + " pollInterval=" + pollInterval);
            if ((enabled != CpuInfoPolicyHandler.this.isCpuInfoPolicyEnabled || pollInterval != CpuInfoPolicyHandler.this.mCpuInfoPollMinInterval) && CpuInfoPolicyHandler.this.mMainHandler != null) {
                CpuInfoPolicyHandler.this.isCpuInfoPolicyEnabled = enabled;
                CpuInfoPolicyHandler.this.mCpuInfoPollMinInterval = pollInterval;
                CpuInfoPolicyHandler.this.mMainHandler.sendEmptyMessage(1);
            }
        }
    };
    private volatile long mCpuInfoPollMinInterval;
    private Handler mMainHandler;
    private HandlerThread mMainHandlerThread;
    private ScheduledExecutorHelper mScheduledExecutor;
    private CpuInfoScheduledTask mTask;

    private final class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    try {
                        if (CpuInfoPolicyHandler.this.isCpuInfoPolicyEnabled) {
                            if (!(CpuInfoPolicyHandler.this.mTask == null || !CpuInfoPolicyHandler.this.mTask.isFutureValid() || (CpuInfoPolicyHandler.this.mTask.isCancelled() ^ 1) == 0)) {
                                Slog.d(CpuInfoPolicyHandler.TAG, "cancel the old task");
                                CpuInfoPolicyHandler.this.mTask.cancel();
                            }
                            CpuInfoPolicyHandler.this.mScheduledExecutor.scheduleWithFixedDelay(CpuInfoPolicyHandler.this.mTask, CpuInfoPolicyHandler.START_UP_DELAY_MILLIS, CpuInfoPolicyHandler.this.mCpuInfoPollMinInterval);
                            Slog.d(CpuInfoPolicyHandler.TAG, "reschedule new task");
                            return;
                        } else if (CpuInfoPolicyHandler.this.mTask != null && CpuInfoPolicyHandler.this.mTask.isFutureValid() && (CpuInfoPolicyHandler.this.mTask.isCancelled() ^ 1) != 0) {
                            Slog.d(CpuInfoPolicyHandler.TAG, "cancel the old task");
                            CpuInfoPolicyHandler.this.mTask.cancel();
                            return;
                        } else {
                            return;
                        }
                    } catch (Exception e) {
                        return;
                    }
                default:
                    return;
            }
        }
    }

    public CpuInfoPolicyHandler(Context context) {
        super(context);
        Slog.d(TAG, "CpuInfoPolicyHandler constuct");
        this.mMainHandlerThread = new HandlerThread(TAG);
        this.mMainHandlerThread.start();
        this.mMainHandler = new MainHandler(this.mMainHandlerThread.getLooper());
        this.mTask = new CpuInfoScheduledTask(context, this);
        CpuInfoConfigurationManager.getInstance().registerCpuInfoEnabledChangeCallback(this.mCallback);
        this.mCpuInfoPollMinInterval = CpuInfoConfigurationManager.getInstance().getCpuInfoPollMinInterval();
        this.isCpuInfoPolicyEnabled = CpuInfoConfigurationManager.getInstance().isCpuInfoPolicyEnabled();
        this.mScheduledExecutor = ScheduledExecutorHelper.getDefaultScheduledExecutor();
        this.mMainHandler.sendEmptyMessage(1);
    }

    public void handleExceptionEvent(EventData data) {
    }

    public String onExtraDump() {
        return CpuInfoConfigurationManager.getInstance().dumpCpuInfoConfiguration();
    }
}
