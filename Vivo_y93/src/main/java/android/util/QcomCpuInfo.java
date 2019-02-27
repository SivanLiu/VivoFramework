package android.util;

import android.content.Context;
import com.vivo.internal.R;

public class QcomCpuInfo extends AbsCpuInfo {
    private static final String CPU_FREQ_PATH = "/sys/devices/soc1/cpu_freq";
    private static final String CPU_NUMBER_PATH = "/sys/devices/soc1/core_num";
    private static final String CPU_TYPE_PATH = "/sys/devices/soc1/cpu_type";
    private static final String TAG = "QcomCpuInfo";

    protected void init() {
        if (this.mCpuNumber == 0 || this.mMaxCpuRate == 0 || this.mCpuType == 0) {
            try {
                String cpuFreq = readFileFirstLine(CPU_FREQ_PATH);
                if (cpuFreq == null) {
                    Log.e(TAG, "init get cpuFreq fail");
                    return;
                }
                this.mMaxCpuRate = Long.parseLong(cpuFreq);
                Log.d(TAG, "mMaxCpuRate = " + this.mMaxCpuRate);
                String cpuType = readFileFirstLine(CPU_TYPE_PATH);
                if (cpuType == null) {
                    Log.e(TAG, "init get cpuType fail");
                    return;
                }
                this.mCpuType = Long.parseLong(cpuType);
                Log.d(TAG, "mCpuType = " + this.mCpuType);
                String cpuNumber = readFileFirstLine(CPU_NUMBER_PATH);
                if (cpuNumber == null) {
                    this.mCpuNumber = 8;
                    Log.e(TAG, "init get cpuNumber fail");
                    return;
                }
                this.mCpuNumber = Integer.parseInt(cpuNumber);
                Log.d(TAG, "mCpuNumber = " + this.mCpuNumber);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    public String getCpuModel(Context context) {
        init();
        return context != null ? context.getString(R.string.cpuinfo_qcom) + " " + this.mCpuType : " " + this.mCpuType;
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
