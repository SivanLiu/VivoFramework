package com.android.internal.os;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.system.OsConstants;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import libcore.io.Libcore;

public class KernelCpuSpeedReader {
    private static final String TAG = "KernelCpuSpeedReader";
    private final long[] mDeltaSpeedTimesMs;
    private final long mJiffyMillis = (1000 / Libcore.os.sysconf(OsConstants._SC_CLK_TCK));
    private final long[] mLastSpeedTimesMs;
    private final String mProcFile;

    public KernelCpuSpeedReader(int cpuNumber, int numSpeedSteps) {
        this.mProcFile = String.format("/sys/devices/system/cpu/cpu%d/cpufreq/stats/time_in_state", new Object[]{Integer.valueOf(cpuNumber)});
        this.mLastSpeedTimesMs = new long[numSpeedSteps];
        this.mDeltaSpeedTimesMs = new long[numSpeedSteps];
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0072 A:{SYNTHETIC, Splitter: B:21:0x0072} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00c5 A:{SYNTHETIC, Splitter: B:50:0x00c5} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0077 A:{SYNTHETIC, Splitter: B:24:0x0077} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long[] readDelta() {
        Throwable th;
        ThreadPolicy policy = StrictMode.allowThreadDiskReads();
        Throwable th2 = null;
        BufferedReader reader = null;
        IOException e;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(this.mProcFile));
            try {
                SimpleStringSplitter splitter = new SimpleStringSplitter(' ');
                for (int speedIndex = 0; speedIndex < this.mLastSpeedTimesMs.length; speedIndex++) {
                    String line = reader2.readLine();
                    if (line == null) {
                        break;
                    }
                    splitter.setString(line);
                    splitter.next();
                    long time = Long.parseLong(splitter.next()) * this.mJiffyMillis;
                    if (time < this.mLastSpeedTimesMs[speedIndex]) {
                        this.mDeltaSpeedTimesMs[speedIndex] = time;
                    } else {
                        this.mDeltaSpeedTimesMs[speedIndex] = time - this.mLastSpeedTimesMs[speedIndex];
                    }
                    this.mLastSpeedTimesMs[speedIndex] = time;
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e2) {
                        e = e2;
                    } catch (Throwable th4) {
                        th = th4;
                        StrictMode.setThreadPolicy(policy);
                        throw th;
                    }
                }
                StrictMode.setThreadPolicy(policy);
                reader = reader2;
                return this.mDeltaSpeedTimesMs;
            } catch (Throwable th5) {
                th = th5;
                reader = reader2;
                if (reader != null) {
                }
                if (th2 == null) {
                }
            }
            try {
                Slog.e(TAG, "Failed to read cpu-freq: " + e.getMessage());
                Arrays.fill(this.mDeltaSpeedTimesMs, 0);
                StrictMode.setThreadPolicy(policy);
                return this.mDeltaSpeedTimesMs;
            } catch (Throwable th6) {
                th = th6;
                StrictMode.setThreadPolicy(policy);
                throw th;
            }
        } catch (Throwable th7) {
            th = th7;
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable th8) {
                    if (th2 == null) {
                        th2 = th8;
                    } else if (th2 != th8) {
                        th2.addSuppressed(th8);
                    }
                }
            }
            if (th2 == null) {
                try {
                    throw th2;
                } catch (IOException e3) {
                    e = e3;
                }
            } else {
                throw th;
            }
        }
    }
}
