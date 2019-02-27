package vivo.app.backup;

import android.util.Slog;
import java.util.Timer;
import java.util.TimerTask;
import vivo.app.backup.utils.DoubleInstanceUtil;

public class BRTimeoutMonitor {
    private final String TAG;
    private BRTimeoutTask mBRTimeoutTask;
    private String mFlag;
    private OnTimeoutListener mListener;
    private volatile long mPulseTime;
    private volatile boolean mRunning;
    private Timer mTimer;
    private int mToken;

    class BRTimeoutTask extends TimerTask {
        private long mInterval;

        public BRTimeoutTask(long interval) {
            this.mInterval = interval;
        }

        public void run() {
            if (System.currentTimeMillis() - BRTimeoutMonitor.this.mPulseTime > this.mInterval) {
                Slog.e(BRTimeoutMonitor.this.TAG, "BRTimeoutMonitor: Timeout , token=" + Integer.toHexString(BRTimeoutMonitor.this.mToken) + " , last flag: " + BRTimeoutMonitor.this.mFlag);
                BRTimeoutMonitor.this.mRunning = false;
                cancel();
                BRTimeoutMonitor.this.handleTimeout();
            }
        }
    }

    public interface OnTimeoutListener {
        void onTimeout(int i, String str, String str2);
    }

    public BRTimeoutMonitor(String tag, int token) {
        this.TAG = tag;
        this.mToken = token;
        init();
    }

    private void init() {
        this.mTimer = new Timer();
        this.mBRTimeoutTask = null;
        this.mRunning = false;
        this.mPulseTime = 0;
        this.mFlag = DoubleInstanceUtil.DEFAULT;
        this.mListener = null;
    }

    public void start(long interval) {
        if (!this.mRunning) {
            try {
                this.mBRTimeoutTask = new BRTimeoutTask(interval);
                this.mRunning = true;
                pulse();
                Slog.i(this.TAG, "start BRTimeoutMonitor ,interval=" + interval + " ,  token=" + Integer.toHexString(this.mToken));
                this.mTimer.schedule(this.mBRTimeoutTask, 0, 1000);
            } catch (Exception e) {
                Slog.e(this.TAG, "Exception at start BRTimeoutMonitor ", e);
            }
        }
    }

    public void cancel() {
        if (this.mRunning) {
            Slog.i(this.TAG, "cancel BRTimeoutMonitor , token=" + Integer.toHexString(this.mToken));
            this.mRunning = false;
            this.mBRTimeoutTask.cancel();
        }
    }

    public void pulse() {
        this.mPulseTime = System.currentTimeMillis();
    }

    public void pulse(String flag) {
        this.mPulseTime = System.currentTimeMillis();
        if (flag != null) {
            this.mFlag = flag;
        }
    }

    public synchronized void setOnTimeoutListener(OnTimeoutListener listener) {
        this.mListener = listener;
    }

    private synchronized void handleTimeout() {
        if (this.mListener != null) {
            this.mListener.onTimeout(this.mToken, this.mFlag, this.TAG);
        }
    }
}
