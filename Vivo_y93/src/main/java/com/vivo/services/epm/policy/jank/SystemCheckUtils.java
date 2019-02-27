package com.vivo.services.epm.policy.jank;

import android.app.AppGlobals;
import android.content.pm.IPackageManager;
import com.vivo.services.epm.util.MessageCenterHelper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SystemCheckUtils {
    public static final int NUMBER_OF_CORES = 8;
    private static final String PATH_CPU = "/sys/rsc/ktop";
    private static final String PATH_IO = "/proc/iomon/iomon";
    private static final String PATH_IO_ENABLE = "/proc/iomon/enabled";
    private static final String PATH_MEMORY = "/proc/vivo_rsc/mem_lastfg";

    /* JADX WARNING: Removed duplicated region for block: B:19:0x003f A:{SYNTHETIC, Splitter: B:19:0x003f} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004b A:{SYNTHETIC, Splitter: B:25:0x004b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String readFile(String path) {
        IOException e;
        Throwable th;
        BufferedReader reader = null;
        String result = null;
        try {
            StringBuilder output = new StringBuilder();
            BufferedReader reader2 = new BufferedReader(new FileReader(path));
            try {
                String newLine = "";
                for (String line = reader2.readLine(); line != null; line = reader2.readLine()) {
                    output.append(newLine).append(line);
                    newLine = "\n";
                }
                result = output.toString();
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                reader = reader2;
            } catch (IOException e3) {
                e2 = e3;
                reader = reader2;
                try {
                    e2.printStackTrace();
                    if (reader != null) {
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            e22 = e4;
            e22.printStackTrace();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
            }
            return result;
        }
        return result;
    }

    public static JankData collectCpuInfo(JankData jankData) {
        String data = readFile(PATH_CPU);
        if (data == null || data.trim().isEmpty()) {
            return jankData;
        }
        try {
            String[] lines = data.split("\n");
            if (lines != null && lines.length >= 4) {
                String[] line1Array = lines[0].split("\t");
                if (line1Array != null && line1Array.length >= 2) {
                    jankData.totalCpuUsage = Integer.parseInt(line1Array[1].trim());
                }
                int thirdAppUsage = 0;
                for (int i = 3; i < lines.length; i++) {
                    String[] fields = lines[i].split("\t");
                    if (fields != null && fields.length >= 12) {
                        int cpuUid = Integer.parseInt(fields[10].trim());
                        if (cpuUid >= MessageCenterHelper.REBIND_SERVICE_TIME_INTERVAL && cpuUid != 99999) {
                            thirdAppUsage += Integer.parseInt(fields[1].trim());
                        }
                    }
                }
                jankData.thirdCpuUsage = thirdAppUsage;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jankData;
    }

    public static JankData collectMemInfo(JankData jankData) {
        String data = readFile(PATH_MEMORY);
        if (data == null || data.trim().isEmpty()) {
            return jankData;
        }
        try {
            String[] lines = data.split("\n");
            if (lines != null && lines.length >= 3) {
                for (int i = 2; i < lines.length; i++) {
                    String[] fields = lines[i].split("\t");
                    if (fields != null && fields.length >= 27 && Integer.parseInt(fields[1].trim()) == jankData.uid) {
                        long pages = Long.parseLong(fields[26].trim());
                        long lastCost = Long.parseLong(fields[24].trim());
                        jankData.pages = pages;
                        jankData.lastCost = lastCost;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jankData;
    }

    public static final boolean isSystemApp(int uid) {
        if (uid < MessageCenterHelper.REBIND_SERVICE_TIME_INTERVAL) {
            return true;
        }
        try {
            IPackageManager pm = AppGlobals.getPackageManager();
            String[] packageNames = pm.getPackagesForUid(uid);
            return (packageNames == null || packageNames.length <= 0 || (pm.getApplicationInfo(packageNames[0], 1, 0).flags & 1) == 0) ? false : true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
