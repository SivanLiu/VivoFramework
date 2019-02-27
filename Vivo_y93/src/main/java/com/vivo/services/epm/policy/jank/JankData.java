package com.vivo.services.epm.policy.jank;

import android.os.SystemClock;
import android.util.Slog;
import com.vivo.services.epm.ExceptionType.PerformanceException;
import com.vivo.services.epm.RuledMap;
import com.vivo.services.epm.util.LogSystemHelper;
import com.vivo.services.epm.util.Utils;

public class JankData {
    public static final String[] LOG_KEYS = new String[]{"extype", "cpu", "io", "mem", "net", "jt", "df", "pn", "otime", "osysversion", "ver"};
    public AppRecord application;
    public int dropFrames;
    public long dropTime;
    public long elapsedRealtime = SystemClock.elapsedRealtime();
    public long lastCost = -1;
    public long pages = -1;
    public int thirdCpuUsage = -1;
    public long timestamp = System.currentTimeMillis();
    public int totalCpuUsage = -1;
    public int type;
    public int uid;

    /* JADX WARNING: Missing block: B:4:0x000a, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isCpuOK(int threshold) {
        return this.totalCpuUsage < 0 || this.thirdCpuUsage < 0 || (this.totalCpuUsage * 8) - this.thirdCpuUsage <= threshold * 8;
    }

    /* JADX WARNING: Missing block: B:4:0x000f, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isMemOK(int threshold) {
        if (this.pages < 0 || this.lastCost < 0 || this.pages * 4000 >= ((long) threshold) * this.lastCost) {
            return true;
        }
        return false;
    }

    public void reportToServer() {
        LogSystemHelper logSystemHelper = LogSystemHelper.getInstance();
        if (logSystemHelper == null) {
            Slog.d(JankExceptionPolicyHandler.TAG, "logSystemHelper not init.");
            return;
        }
        try {
            RuledMap ruledMap = new RuledMap(LOG_KEYS);
            ruledMap.addKeyValue(LOG_KEYS[0], String.valueOf(PerformanceException.EXCEPTION_TYPE_JANK)).addKeyValue(LOG_KEYS[1], "total=" + this.totalCpuUsage + ",third=" + this.thirdCpuUsage).addKeyValue(LOG_KEYS[2], "unknown").addKeyValue(LOG_KEYS[3], "pages=" + this.pages + ",lastCost=" + this.lastCost).addKeyValue(LOG_KEYS[4], "unknown").addKeyValue(LOG_KEYS[5], String.valueOf(this.type)).addKeyValue(LOG_KEYS[6], String.valueOf(this.dropFrames)).addKeyValue(LOG_KEYS[7], this.application.pkgName).addKeyValue(LOG_KEYS[8], String.valueOf(this.timestamp)).addKeyValue(LOG_KEYS[9], Utils.getSystemVersion()).addKeyValue(LOG_KEYS[10], this.application.versionName);
            logSystemHelper.addLog(ruledMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return "JankData{uid=" + this.uid + ", application=" + this.application + ", type=" + this.type + ", dropFrames=" + this.dropFrames + ", dropTime=" + this.dropTime + ", timestamp=" + this.timestamp + ", elapsedRealtime=" + this.elapsedRealtime + ", totalCpuUsage=" + this.totalCpuUsage + ", thirdCpuUsage=" + this.thirdCpuUsage + ", pages=" + this.pages + ", lastCost=" + this.lastCost + '}';
    }
}
