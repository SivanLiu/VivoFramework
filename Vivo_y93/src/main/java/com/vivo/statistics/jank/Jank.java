package com.vivo.statistics.jank;

import android.app.ActivityThread;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import com.vivo.content.Weather;
import com.vivo.statistics.sdk.GatherManager;

public class Jank {
    private static final int BG_BROADCAST_TIMEOUT = SystemProperties.getInt("persist.rms.broadcast_bg", Weather.WEATHERVERSION_ROM_4_0);
    private static final int BG_EVENT_TIMEOUT = SystemProperties.getInt("persist.rms.event_bg", Weather.WEATHERVERSION_ROM_4_0);
    private static final int CONTINUOUS_DROP_FRAME_DURARION = 120000;
    private static final int CONTINUOUS_DROP_FRAME_THRESHOLD = 5;
    public static boolean DISABLE = (SystemProperties.getBoolean("persist.rms.bigdata_enable", true) ^ 1);
    private static final int FG_BROADCAST_TIMEOUT = SystemProperties.getInt("persist.rms.broadcast_fg", 1000);
    private static final int FG_EVENT_TIMEOUT = SystemProperties.getInt("persist.rms.event_fg", 1000);
    private static final int OFFSET_TIME = 3000;
    private static final int SKIPPED_FRAME_LIMIT = SystemProperties.getInt("persist.rms.drop_frame_limit", 30);
    private static final String TAG_BROADCAST_TIMEOUT = "broadcast_timeout";
    private static final String TAG_DROP_FRAME = "drop_frame";
    private static final String TAG_DROP_FRAME_SERIES = "drop_frame_continuous";
    private static final String TAG_EVENT_TIMEOUT = "event_timeout";
    private String mClassName;
    private long mLastDropFrameTime;
    private long mLastResetTime;
    private long mMainThreadId;
    private boolean mResumed;
    private long mResumedTime;
    private int mSeriesCount;

    private static class Instance {
        private static final Jank INSTANCE = new Jank();

        private Instance() {
        }
    }

    /* synthetic */ Jank(Jank -this0) {
        this();
    }

    private Jank() {
    }

    public static Jank getInstance() {
        return Instance.INSTANCE;
    }

    public void resumeActivity(String className) {
        this.mClassName = className;
        this.mResumedTime = SystemClock.elapsedRealtime();
        this.mSeriesCount = 0;
        this.mLastResetTime = 0;
        this.mResumed = true;
    }

    public void pauseActivity() {
        if (this.mSeriesCount >= 5) {
            GatherManager.getInstance().gather(TAG_DROP_FRAME_SERIES, ActivityThread.currentPackageName(), ActivityThread.currentProcessName(), "", Integer.valueOf(0), this.mClassName, Integer.valueOf(this.mSeriesCount), Long.valueOf(this.mLastDropFrameTime));
        }
        this.mResumed = false;
        this.mSeriesCount = 0;
        this.mLastResetTime = 0;
    }

    public boolean isFg() {
        return this.mResumed;
    }

    public void handleEventTimeout(int event, long cost) {
        if (this.mMainThreadId == 0) {
            this.mMainThreadId = Thread.currentThread().getId();
        }
        if (!DISABLE) {
            if (cost >= ((long) (isFg() ? FG_BROADCAST_TIMEOUT : BG_BROADCAST_TIMEOUT))) {
                GatherManager.getInstance().gather(TAG_EVENT_TIMEOUT, ActivityThread.currentPackageName(), ActivityThread.currentProcessName(), Integer.valueOf(event), Boolean.valueOf(isFg()), Integer.valueOf((int) cost), Long.valueOf(System.currentTimeMillis()));
            }
        }
    }

    public void handleBroadcastTimeout(String action, boolean order, long cost) {
        if (!DISABLE) {
            if (cost >= ((long) (isFg() ? FG_EVENT_TIMEOUT : BG_EVENT_TIMEOUT))) {
                GatherManager.getInstance().gather(TAG_BROADCAST_TIMEOUT, ActivityThread.currentPackageName(), ActivityThread.currentProcessName(), action, Boolean.valueOf(isFg()), Boolean.valueOf(order), Integer.valueOf((int) cost), Long.valueOf(System.currentTimeMillis()));
            }
        }
    }

    public long getMainThreadId() {
        return this.mMainThreadId;
    }

    /* JADX WARNING: Missing block: B:4:0x0008, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dropFrames(int skippedFrames) {
        if (!DISABLE && skippedFrames >= SKIPPED_FRAME_LIMIT && GatherManager.getInstance().getService() != null) {
            try {
                Trace.traceBegin(8, "dropFrames");
                long now = SystemClock.elapsedRealtime();
                if (!this.mResumed || now - this.mResumedTime < 3000) {
                    Trace.traceEnd(8);
                    return;
                }
                this.mLastDropFrameTime = System.currentTimeMillis();
                String pkgName = ActivityThread.currentPackageName();
                String procName = ActivityThread.currentProcessName();
                int count = this.mSeriesCount;
                this.mSeriesCount = count + 1;
                GatherManager.getInstance().gather(TAG_DROP_FRAME, pkgName, procName, "", Integer.valueOf(0), this.mClassName, Integer.valueOf(skippedFrames), Long.valueOf(this.mLastDropFrameTime));
                if (this.mLastResetTime == 0) {
                    this.mLastResetTime = now;
                    Trace.traceEnd(8);
                    return;
                }
                if (now - this.mLastResetTime > 120000) {
                    if (count >= 5) {
                        GatherManager.getInstance().gather(TAG_DROP_FRAME_SERIES, pkgName, procName, "", Integer.valueOf(0), this.mClassName, Integer.valueOf(count), Long.valueOf(this.mLastDropFrameTime));
                    }
                    this.mLastResetTime = now;
                    this.mSeriesCount = 0;
                }
                Trace.traceEnd(8);
            } catch (Exception e) {
                Trace.traceEnd(8);
            } catch (Throwable th) {
                Trace.traceEnd(8);
                throw th;
            }
        }
    }
}
