package com.vivo.statistics.database;

import android.app.ActivityThread;
import android.os.SystemProperties;
import com.vivo.statistics.jank.Jank;
import com.vivo.statistics.sdk.GatherManager;

public class SQLiteTrace {
    private static final int BG_TIMEOUT = SystemProperties.getInt("persist.rms.sqlite_bg", 2000);
    private static final int FG_TIMEOUT = SystemProperties.getInt("persist.rms.sqlite_fg", 500);
    private static final String TAG_SQLITE_TIMEOUT = "sqlite_timeout";

    /* JADX WARNING: Missing block: B:7:0x0016, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void trace(long cost, String sql) {
        int timeout = Jank.getInstance().isFg() ? FG_TIMEOUT : BG_TIMEOUT;
        if (!Jank.DISABLE && cost >= ((long) timeout) && Thread.currentThread().getId() == Jank.getInstance().getMainThreadId() && GatherManager.getInstance().getService() != null) {
            GatherManager.getInstance().gather(TAG_SQLITE_TIMEOUT, ActivityThread.currentPackageName(), ActivityThread.currentProcessName(), sql, Boolean.valueOf(Jank.getInstance().isFg()), Boolean.valueOf(true), Integer.valueOf((int) cost), Long.valueOf(System.currentTimeMillis()));
        }
    }
}
