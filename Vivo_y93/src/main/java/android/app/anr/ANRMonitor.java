package android.app.anr;

import android.app.job.JobInfo;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.os.Process;
import android.os.SystemClock;
import android.util.Printer;
import android.util.Slog;

public class ANRMonitor {
    private static final int DURATION_LOOPER_IDLE_TIMEOUT = 2000;
    private static final int DURATION_LOOPER_IDLE_TIMEOUT_STEP = 1000;
    private static final int DURATION_LOOPER_IDLE_TOO_LONG = 30000;
    private static final int MSG_LOOPER_IDLE_TIMEOUT = 1000;
    private static final int MSG_LOOPER_IDLE_TOO_LONG = 1001;
    private static final String TAG = "ANRMonitor";
    private static ANRManager sManager;
    private static ANRMonitor sMonitor;
    private boolean mActiveFromInput = false;
    private long mBeginDispatchTime = 0;
    private Message mDispatchingMessage = null;
    private final Handler mHandler;
    private volatile boolean mIdleNow = true;
    private volatile int mIdleSeq = 0;
    private volatile boolean mLogMessageCost = false;
    private final Looper mMainLooper;
    private final StringBuilder mMessageCost = new StringBuilder();
    private long mStartCostTime = 0;
    private final StringBuilder mStringBuilder = new StringBuilder();
    private final StringBuilder mTempString = new StringBuilder();

    private class MyHandler extends Handler {
        public MyHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    ANRMonitor.this.onBusyInternal(msg.arg1, msg.arg2);
                    return;
                case 1001:
                    ANRMonitor.sManager.clearAllMessages(Process.myPid());
                    return;
                default:
                    return;
            }
        }
    }

    public static void init(Context context, Looper looper) {
        if (sMonitor != null) {
            Slog.w(TAG, "ANRMonitor is already initialized.");
        } else if (looper == null) {
            Slog.w(TAG, "looper is null");
        } else {
            sManager = ANRManager.getDefault(context);
            sMonitor = new ANRMonitor(looper);
            Slog.i(TAG, "ANRMonitor initialized successfully.");
        }
    }

    public static ANRMonitor getInstance() {
        return sMonitor;
    }

    private ANRMonitor(Looper looper) {
        this.mMainLooper = looper;
        this.mMainLooper.setMonitor(this);
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new MyHandler(thread.getLooper());
        Looper looper2 = this.mMainLooper;
        Looper.myQueue().addIdleHandler(new IdleHandler() {
            public boolean queueIdle() {
                ANRMonitor.this.onIdleInternal();
                return true;
            }
        });
    }

    public void beginDispatch(Message msg) {
        onActiveInternal();
        if (this.mLogMessageCost) {
            this.mDispatchingMessage = msg;
            this.mBeginDispatchTime = SystemClock.uptimeMillis();
        }
    }

    public void postDispatch() {
        if (this.mLogMessageCost && this.mDispatchingMessage != null) {
            long now = SystemClock.uptimeMillis();
            synchronized (this.mMessageCost) {
                if (this.mMessageCost.length() == 0) {
                    this.mStartCostTime = now;
                }
                this.mMessageCost.append("  cost: ").append(now - this.mBeginDispatchTime).append("ms").append(this.mDispatchingMessage);
                this.mMessageCost.append("\n");
            }
            this.mDispatchingMessage = null;
        }
    }

    public void beginInputEvent() {
        if (onActiveInternal()) {
            this.mActiveFromInput = true;
        }
    }

    public void postInputEvent() {
        if (this.mActiveFromInput) {
            this.mActiveFromInput = false;
            onIdleInternal();
        }
    }

    private boolean onActiveInternal() {
        if (!this.mIdleNow) {
            return false;
        }
        this.mIdleNow = false;
        this.mIdleSeq++;
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1000, 2000, this.mIdleSeq), 2000);
        return true;
    }

    private boolean onIdleInternal() {
        if (this.mIdleNow) {
            return false;
        }
        this.mIdleNow = true;
        this.mHandler.removeMessages(1000);
        this.mHandler.removeMessages(1001);
        this.mHandler.sendEmptyMessageDelayed(1001, JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS);
        if (this.mLogMessageCost) {
            this.mLogMessageCost = false;
            synchronized (this.mMessageCost) {
                this.mMessageCost.setLength(0);
            }
        }
        return true;
    }

    private void onBusyInternal(int totaltime, int seq) {
        this.mTempString.setLength(0);
        int duration = 0;
        synchronized (this.mMessageCost) {
            if (this.mMessageCost.length() > 0) {
                long now = SystemClock.uptimeMillis();
                duration = (int) (now - this.mStartCostTime);
                this.mTempString.append(this.mMessageCost);
                this.mMessageCost.setLength(0);
                this.mStartCostTime = now;
            }
        }
        logLooperBlocked(totaltime, this.mTempString, duration);
        if (!this.mIdleNow && seq == this.mIdleSeq) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1000, totaltime + 1000, seq), 1000);
            if (!this.mLogMessageCost) {
                this.mLogMessageCost = true;
                synchronized (this.mMessageCost) {
                    this.mMessageCost.setLength(0);
                }
            }
        }
        this.mHandler.removeMessages(1001);
    }

    private void logLooperBlocked(int totaltime, StringBuilder messageCost, int duration) {
        int i = 0;
        this.mStringBuilder.setLength(0);
        this.mStringBuilder.append("Main looper busy for ").append(totaltime).append("ms\n");
        Slog.d(TAG, "Main looper busy for " + totaltime + "ms");
        if (messageCost.length() > 0) {
            this.mStringBuilder.append("Message cost in the past ").append(duration).append("ms");
            this.mStringBuilder.append("\n");
            this.mStringBuilder.append(messageCost.toString());
        }
        this.mStringBuilder.append("Now stack trace:\n");
        StackTraceElement[] stackTrace = this.mMainLooper.getThread().getStackTrace();
        Slog.d(TAG, "getBinderCall = " + getBinderCall(stackTrace));
        Slog.d(TAG, "Now stack trace:");
        int length = stackTrace.length;
        while (i < length) {
            StackTraceElement element = stackTrace[i];
            this.mStringBuilder.append("    at ").append(element.toString()).append("\n");
            Slog.d(TAG, "    at " + element.toString());
            i++;
        }
        this.mMainLooper.dump(new Printer() {
            private static final int MAX_DUMP_LENGTH = 524288;
            private boolean bNeedDumpTotalInfo = true;

            public void println(String x) {
                if (ANRMonitor.this.mStringBuilder.length() < 524288) {
                    ANRMonitor.this.mStringBuilder.append(x).append("\n");
                } else if (this.bNeedDumpTotalInfo && x.contains("(Total messages: ")) {
                    this.bNeedDumpTotalInfo = false;
                    ANRMonitor.this.mStringBuilder.append(x).append("\n");
                    Slog.d(ANRMonitor.TAG, "[[LOOPER DUMP TRUNCATED]]:" + x + "\n");
                }
            }
        }, "");
        sManager.appLooperBlocked(this.mStringBuilder.toString(), totaltime, getBinderCall(stackTrace));
    }

    private String getBinderCall(StackTraceElement[] stackTrace) {
        boolean isBinderCall = false;
        String binderName = null;
        String methodName = null;
        for (StackTraceElement element : stackTrace) {
            String stackString = element.toString();
            if (binderName == null) {
                if (isBinderCall) {
                    try {
                        stackString = stackString.substring(0, stackString.indexOf("("));
                        binderName = stackString.substring(0, stackString.lastIndexOf("."));
                        methodName = stackString.substring(binderName.length() + 1, stackString.length());
                    } catch (Exception e) {
                        binderName = null;
                        methodName = null;
                    }
                } else if (stackString.startsWith("android.os.BinderProxy.transact(")) {
                    isBinderCall = true;
                }
            }
        }
        return (binderName == null || methodName == null) ? binderName : binderName + "." + methodName;
    }
}
