package com.vivo.services.epm.policy.cpuinfo;

import android.os.Process;
import android.system.OsConstants;
import com.vivo.common.utils.CpuinfoHelper;
import com.vivo.common.utils.CpuinfoHelper.CpuCoreInfo;
import java.util.List;
import libcore.io.Libcore;

public class CpuInfoUtils {
    private static final int CPU_USAGE_SAMPLE_INTERVAL = 1000;
    private static final int[] LOAD_AVERAGE_FORMAT = new int[]{16416, 16416, 16416};
    private static final int[] SYSTEM_CPU_FORMAT = new int[]{288, 8224, 8224, 8224, 8224, 8224, 8224, 8224};
    private static final long mJiffyMillis = (1000 / Libcore.os.sysconf(OsConstants._SC_CLK_TCK));
    private static final float[] mLoadAverageData = new float[3];
    private static final long[] mSystemCpuData = new long[7];

    private static final class CpuIdleTimeInfo {
        public long idleTime;
        public long sampleTime;
        public long totalTime;

        /* synthetic */ CpuIdleTimeInfo(CpuIdleTimeInfo -this0) {
            this();
        }

        private CpuIdleTimeInfo() {
        }
    }

    public static int getCpuNumbers() {
        return CpuinfoHelper.getCpuNumCores();
    }

    public static int getCpuTotalUsages() {
        CpuIdleTimeInfo t1 = getCpuIdleTimeInfo();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        CpuIdleTimeInfo t2 = getCpuIdleTimeInfo();
        return (int) (((((double) ((t2.totalTime - t2.idleTime) - (t1.totalTime - t1.idleTime))) * 1.0d) / ((double) (t2.totalTime - t1.totalTime))) * 100.0d);
    }

    private static CpuIdleTimeInfo getCpuIdleTimeInfo() {
        CpuIdleTimeInfo ret = new CpuIdleTimeInfo(null);
        ret.sampleTime = System.currentTimeMillis();
        long[] sysCpu = mSystemCpuData;
        if (Process.readProcFile("/proc/stat", SYSTEM_CPU_FORMAT, null, sysCpu, null)) {
            long idletime = sysCpu[3] * mJiffyMillis;
            long iowaittime = sysCpu[4] * mJiffyMillis;
            long irqtime = sysCpu[5] * mJiffyMillis;
            long softirqtime = sysCpu[6] * mJiffyMillis;
            ret.totalTime = ((((((sysCpu[0] + sysCpu[1]) * mJiffyMillis) + (sysCpu[2] * mJiffyMillis)) + iowaittime) + irqtime) + softirqtime) + idletime;
            ret.idleTime = idletime;
        }
        return ret;
    }

    public static float[] getCpuAveragesLoad() {
        float[] loadAverages = mLoadAverageData;
        if (Process.readProcFile("/proc/loadavg", LOAD_AVERAGE_FORMAT, null, null, loadAverages)) {
            float load1 = loadAverages[0];
            float load5 = loadAverages[1];
            float f = loadAverages[2];
        }
        return loadAverages;
    }

    public static List<CpuCoreInfo> getCpuCoreInfos() {
        return CpuinfoHelper.getCpuCoreInfos();
    }
}
