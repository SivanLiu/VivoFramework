package com.vivo.services.epm.policy.cpuinfo;

import android.content.Context;
import android.util.Slog;
import com.vivo.common.utils.CpuinfoHelper.CpuCoreInfo;
import com.vivo.services.epm.util.ScheduledTask;
import java.util.List;

public class CpuInfoScheduledTask extends ScheduledTask {
    private static final String TAG = "CpuInfoPolicyHandler";
    private CpuInfo mCurrentCpuInfo;
    private CpuInfo mOldCpuInfo;
    private CpuInfoPolicyHandler mPolicyHandler;

    public CpuInfoScheduledTask(Context context, CpuInfoPolicyHandler handler) {
        super(context);
        this.mPolicyHandler = handler;
    }

    public void doTaskOnce() {
        Slog.d(TAG, "doTaskOnce");
        pollOnce();
    }

    public boolean onLowBattery(int curLevel) {
        Slog.d(TAG, "onLowBattery curLevel=" + curLevel);
        return CpuInfoConfigurationManager.getInstance().isCpuInfoPollWhenLowBattery();
    }

    private synchronized void pollOnce() {
        int coreNumbers = CpuInfoUtils.getCpuNumbers();
        List<CpuCoreInfo> cpuCoreInfos = CpuInfoUtils.getCpuCoreInfos();
        int usagesPercent = CpuInfoUtils.getCpuTotalUsages();
        float[] loads = CpuInfoUtils.getCpuAveragesLoad();
        this.mCurrentCpuInfo = new CpuInfo(coreNumbers, cpuCoreInfos, usagesPercent, loads[0], loads[1], loads[2]);
        Slog.d(TAG, "mOldCpuInfo=" + this.mOldCpuInfo);
        Slog.d(TAG, "mCurrentCpuInfo=" + this.mCurrentCpuInfo);
        if (this.mCurrentCpuInfo.isNeedReportToLogSystem(this.mOldCpuInfo)) {
            this.mOldCpuInfo = this.mCurrentCpuInfo;
            this.mCurrentCpuInfo.reportToLogSystem();
        }
    }
}
