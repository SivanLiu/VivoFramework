package com.vivo.statistics.binder;

import android.app.ActivityThread;
import android.os.IBinder;
import android.os.SystemProperties;
import com.vivo.content.Weather;
import com.vivo.statistics.jank.Jank;
import com.vivo.statistics.sdk.GatherManager;

public class TransactTrace {
    private static final int BG_TIMEOUT = SystemProperties.getInt("persist.rms.transact_bg", Weather.WEATHERVERSION_ROM_4_0);
    private static final int FG_TIMEOUT = SystemProperties.getInt("persist.rms.transact_fg", 1000);
    private static final String TAG_TRANSACT_TIMEOUT = "transact_timeout";

    public static boolean isTrace(int flags) {
        return (Jank.DISABLE || flags != 0) ? false : ActivityThread.isSystem() ^ 1;
    }

    /* JADX WARNING: Missing block: B:7:0x0025, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void traceTransact(IBinder binder, int code, int flags, long cost) {
        if (cost >= ((long) (Jank.getInstance().isFg() ? FG_TIMEOUT : BG_TIMEOUT)) && Thread.currentThread().getId() == Jank.getInstance().getMainThreadId() && GatherManager.getInstance().getService() != null) {
            try {
                GatherManager.getInstance().gather(TAG_TRANSACT_TIMEOUT, ActivityThread.currentPackageName(), ActivityThread.currentProcessName(), binder.getInterfaceDescriptor(), Boolean.valueOf(Jank.getInstance().isFg()), Integer.valueOf(code), Integer.valueOf((int) cost), Long.valueOf(System.currentTimeMillis()));
            } catch (Exception e) {
            }
        }
    }
}
