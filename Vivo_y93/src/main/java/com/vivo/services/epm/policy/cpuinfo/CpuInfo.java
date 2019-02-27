package com.vivo.services.epm.policy.cpuinfo;

import android.util.Slog;
import com.vivo.common.utils.CpuinfoHelper.CpuCoreInfo;
import com.vivo.services.epm.ExceptionType.BaseInfoException;
import com.vivo.services.epm.RuledMap;
import com.vivo.services.epm.util.LogSystemHelper;
import com.vivo.services.epm.util.Utils;
import java.util.List;

public class CpuInfo {
    private static final int CORE_STATUS_OFFLINE = 0;
    private static final int CORE_STATUS_ONLINE = 1;
    private static final String[] LOG_KEYS = new String[]{"extype", "otime", "osysversion", "times", "usages", "load1", "load5", "load15", "cs"};
    private static final String TAG = "CpuInfoPolicyHandler";
    public int coreNumbers;
    public List<CpuCoreInfo> cpuCoreInfos;
    public float load1;
    public float load15;
    public float load5;
    public int usagesPercent;

    public CpuInfo(int coreNumbers, List<CpuCoreInfo> cpuCoreInfos, int usagesPercent, float load1, float load5, float load15) {
        this.coreNumbers = coreNumbers;
        this.cpuCoreInfos = cpuCoreInfos;
        this.usagesPercent = usagesPercent;
        this.load1 = load1;
        this.load5 = load5;
        this.load15 = load15;
    }

    private String getCpuCoreOnlineStatus() {
        StringBuffer sb = new StringBuffer();
        for (CpuCoreInfo core : this.cpuCoreInfos) {
            sb.append("{" + core.core + "|" + (core.online == 1 ? 1 : 0) + "|" + core.scaling_cur_freq + "}");
        }
        return sb.toString();
    }

    public boolean isNeedReportToLogSystem(CpuInfo oldOne) {
        if (oldOne != null && Math.abs((this.usagesPercent - oldOne.usagesPercent) * 100) < CpuInfoConfigurationManager.getInstance().getCpuInfoReportMinThreshold()) {
            return false;
        }
        return true;
    }

    public void reportToLogSystem() {
        LogSystemHelper logSystemHelper = LogSystemHelper.getInstance();
        if (logSystemHelper == null) {
            Slog.d(TAG, "logSystemHelper not init.");
            return;
        }
        try {
            RuledMap ruledMap = new RuledMap(LOG_KEYS);
            ruledMap.addKeyValue(LOG_KEYS[0], String.valueOf(BaseInfoException.EXCEPTION_TYPE_CPU_INFO)).addKeyValue(LOG_KEYS[1], String.valueOf(System.currentTimeMillis())).addKeyValue(LOG_KEYS[2], Utils.getSystemVersion()).addKeyValue(LOG_KEYS[3], "0").addKeyValue(LOG_KEYS[4], String.valueOf(this.usagesPercent)).addKeyValue(LOG_KEYS[5], String.valueOf(this.load1)).addKeyValue(LOG_KEYS[6], String.valueOf(this.load5)).addKeyValue(LOG_KEYS[7], String.valueOf(this.load15)).addKeyValue(LOG_KEYS[8], getCpuCoreOnlineStatus());
            logSystemHelper.addLog(ruledMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return "{ usagesPercent=" + this.usagesPercent + " load1=" + this.load1 + " load5=" + this.load5 + " load15=" + this.load15 + "}";
    }
}
