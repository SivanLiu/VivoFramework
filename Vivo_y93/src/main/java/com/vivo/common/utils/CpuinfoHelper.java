package com.vivo.common.utils;

import com.android.internal.util.FastPrintWriter;
import com.vivo.common.autobrightness.StateInfo;
import com.vivo.services.vivodevice.VivoDeviceNative;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CpuinfoHelper {
    private static final String CPU_CORE_INFO_PATTERN = "/sys/devices/system/cpu/cpu";
    private static final String CPU_INFO_BASE_PATH = "/sys/devices/system/cpu/";

    public static class CpuCoreInfo {
        public int core;
        public int cpuinfo_max_freq;
        public int cpuinfo_min_freq;
        public int online;
        public int scaling_cur_freq;

        public String toString() {
            return VivoDeviceNative.CPU + this.core + "{ online=" + this.online + " scaling_cur_freq=" + this.scaling_cur_freq + " cpuinfo_max_freq=" + this.cpuinfo_max_freq + " cpuinfo_min_freq=" + this.cpuinfo_min_freq + " }";
        }
    }

    public static int getCpuNumCores() {
        try {
            return new File(CPU_INFO_BASE_PATH).listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                        return true;
                    }
                    return false;
                }
            }).length;
        } catch (Exception e) {
            return 1;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0028 A:{SYNTHETIC, Splitter: B:20:0x0028} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String readLine(File file) {
        Exception e;
        Throwable th;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(file));
            try {
                String line = reader2.readLine();
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (Exception e2) {
                    }
                }
                return line;
            } catch (Exception e3) {
                e = e3;
                reader = reader2;
            } catch (Throwable th2) {
                th = th2;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            try {
                e.printStackTrace();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e5) {
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e6) {
                    }
                }
                throw th;
            }
        }
    }

    private static int getIntFromFile(String filename) {
        int result = -1;
        try {
            File file = new File(filename);
            if (file.exists()) {
                result = Integer.parseInt(readLine(file), 10);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String printCurrentCpuStatus() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter(sw, false, StateInfo.STATE_BIT_BATTERY);
        for (CpuCoreInfo cpu : getCpuCoreInfos()) {
            pw.print(cpu);
            pw.print("\n");
        }
        pw.flush();
        return sw.toString();
    }

    public static CpuCoreInfo getCpuCoreInfo(int cpuNo) {
        CpuCoreInfo cpu = new CpuCoreInfo();
        if (!new File(CPU_CORE_INFO_PATTERN + cpuNo).exists()) {
            return null;
        }
        cpu.core = cpuNo;
        cpu.online = getIntFromFile(CPU_CORE_INFO_PATTERN + cpuNo + "/online");
        cpu.scaling_cur_freq = getIntFromFile(CPU_CORE_INFO_PATTERN + cpuNo + "/cpufreq/scaling_cur_freq");
        cpu.cpuinfo_max_freq = getIntFromFile(CPU_CORE_INFO_PATTERN + cpuNo + "/cpufreq/cpuinfo_max_freq");
        cpu.cpuinfo_min_freq = getIntFromFile(CPU_CORE_INFO_PATTERN + cpuNo + "/cpufreq/cpuinfo_min_freq");
        return cpu;
    }

    public static List<CpuCoreInfo> getCpuCoreInfos() {
        int coreNum = getCpuNumCores();
        List<CpuCoreInfo> cpus = new ArrayList();
        for (int i = 0; i < coreNum; i++) {
            CpuCoreInfo cpu = getCpuCoreInfo(i);
            if (cpu != null) {
                cpus.add(cpu);
            }
        }
        return cpus;
    }
}
