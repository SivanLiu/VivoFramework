package android.util;

import android.content.Context;
import java.io.File;
import java.util.regex.Pattern;

public class MTKCpuInfo extends AbsCpuInfo {
    private static final String CPU_FILE_PATH = "/sys/devices/system/cpu";
    private static final String TAG = "MTKCpuInfo";

    protected void init() {
        if (this.mCpuNumber == 0) {
            getCPUCoreNumberInternal();
        }
        if (this.mMaxCpuRate == 0) {
            getMaxCpuRateInternal();
        }
    }

    private int getCPUCoreNumberInternal() {
        if (this.mCpuNumber != 0) {
            return this.mCpuNumber;
        }
        int count = 0;
        File file = new File(CPU_FILE_PATH);
        if (file.exists()) {
            File[] fileList = file.listFiles();
            if (fileList != null) {
                for (File f : fileList) {
                    if (Pattern.matches("cpu[0-9]", f.getName())) {
                        count++;
                    }
                }
            }
            if (count != 0) {
                this.mCpuNumber = count;
            }
            Log.d(TAG, "getCPUCoreNumberInternal:" + this.mCpuNumber);
            return this.mCpuNumber;
        }
        Log.e(TAG, "getCPUCoreNumberInternal:/sys/devices/system/cpu is not exist,just return 0");
        return 0;
    }

    private void getMaxCpuRateInternal() {
        if (this.mMaxCpuRate == 0) {
            long maxRate = 0;
            int cores = getCPUCoreNumberInternal();
            for (int i = 0; i < cores; i++) {
                try {
                    String line = readFileFirstLine("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq");
                    if (line != null) {
                        long tempRate = Long.parseLong(line);
                        if (maxRate < tempRate) {
                            maxRate = tempRate;
                        }
                        Log.d(TAG, "getMaxCpuRate cpu" + i + " rate:" + tempRate);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            if (maxRate != 0) {
                this.mMaxCpuRate = maxRate;
            }
            Log.d(TAG, "getMaxCpuRate:" + maxRate);
        }
    }

    public String getCpuModel(Context context) {
        return "mtk";
    }

    public long getMaxCpuRate() {
        init();
        return this.mMaxCpuRate;
    }

    public int getCPUCoreNumber() {
        init();
        return this.mCpuNumber;
    }
}
