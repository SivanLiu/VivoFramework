package com.vivo.services.epm.policy.cpuinfo;

import android.content.ContentValues;
import android.util.Log;
import com.vivo.services.epm.config.ConfigurationObserver;
import com.vivo.services.epm.config.ContentValuesList;
import com.vivo.services.epm.config.DefaultConfigurationManager;
import com.vivo.services.epm.config.Switch;
import java.util.ArrayList;
import java.util.List;

public class CpuInfoConfigurationManager {
    private static final boolean DEFAULT_CPUINFO_POLICY_STATE = false;
    private static final String EPM_CPUINFO_CV_LIST_NAME = "epm_cpuinfo_policy";
    private static final String EPM_CPUINFO_CV_LIST_NAME_INTERVAL_KEY = "cpuinfo_poll_interval";
    private static final String EPM_CPUINFO_CV_LIST_NAME_LOW_BATTERY_KEY = "cpuinfo_poll_low_battery";
    private static final String EPM_CPUINFO_CV_LIST_NAME_REPORT_THRESHOLD = "cpuinfo_report_min_threshold";
    private static final String EPM_CPUINFO_SWITCH_NAME = "epm_policy_cpuinfo";
    private static final String TAG = "CpuInfoPolicyHandler";
    private static CpuInfoConfigurationManager sIntance;
    private volatile boolean isCpuInfoPolicyEnabled = false;
    private volatile boolean isCpuInfoPollWhenLowBattery = true;
    private Object mConfigChangedLock = new Object();
    private List<ICpuInfoEnabledChangeCallback> mCpuInfoEnabledChangeCallback = new ArrayList();
    private Switch mCpuInfoPolicySwitch = this.mDefaultConfigurationManager.getSwitch(EPM_CPUINFO_SWITCH_NAME);
    private ConfigurationObserver mCpuInfoPolicySwitchObserver = new ConfigurationObserver() {
        /* JADX WARNING: Removed duplicated region for block: B:9:0x004f  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onConfigChange(String file, String name) {
            Log.d(CpuInfoConfigurationManager.TAG, "file=" + file + " name=" + name);
            Switch w = CpuInfoConfigurationManager.this.mDefaultConfigurationManager.getSwitch(CpuInfoConfigurationManager.EPM_CPUINFO_SWITCH_NAME);
            synchronized (CpuInfoConfigurationManager.this.mConfigChangedLock) {
                boolean tmp;
                if (w != null) {
                    if ((w.isUninitialized() ^ 1) != 0) {
                        tmp = w.isOn();
                        if (tmp != CpuInfoConfigurationManager.this.isCpuInfoPolicyEnabled) {
                            CpuInfoConfigurationManager.this.isCpuInfoPolicyEnabled = tmp;
                            CpuInfoConfigurationManager.this.dispatchCpuInfoEnableChanged(CpuInfoConfigurationManager.this.isCpuInfoPolicyEnabled, CpuInfoConfigurationManager.this.mCpuInfoPollMinInterval);
                        }
                    }
                }
                tmp = true;
                if (tmp != CpuInfoConfigurationManager.this.isCpuInfoPolicyEnabled) {
                }
            }
        }
    };
    private volatile long mCpuInfoPollMinInterval = 30000;
    private volatile int mCpuReportMinThreshold = 20;
    private DefaultConfigurationManager mDefaultConfigurationManager = DefaultConfigurationManager.getInstance();
    private ContentValuesList mEpmCpuInfoPolicyList;
    private ConfigurationObserver mEpmCpuInfoPolicyObserver = new ConfigurationObserver() {
        public void onConfigChange(String file, String name) {
            Log.d(CpuInfoConfigurationManager.TAG, "file=" + file + " name=" + name);
            ContentValuesList list = CpuInfoConfigurationManager.this.mDefaultConfigurationManager.getContentValuesList(CpuInfoConfigurationManager.EPM_CPUINFO_CV_LIST_NAME);
            synchronized (CpuInfoConfigurationManager.this.mConfigChangedLock) {
                boolean isPollMinIntervalChanged = false;
                long lastCpuInfoPollMinInterval = CpuInfoConfigurationManager.this.mCpuInfoPollMinInterval;
                boolean lastCpuInfoPollWhenLowBattery = CpuInfoConfigurationManager.this.isCpuInfoPollWhenLowBattery;
                CpuInfoConfigurationManager.this.parseCpuinfoPolicy(list);
                if (lastCpuInfoPollMinInterval != CpuInfoConfigurationManager.this.mCpuInfoPollMinInterval) {
                    isPollMinIntervalChanged = true;
                }
                if (lastCpuInfoPollWhenLowBattery != CpuInfoConfigurationManager.this.isCpuInfoPollWhenLowBattery) {
                }
                if (isPollMinIntervalChanged) {
                    CpuInfoConfigurationManager.this.dispatchCpuInfoEnableChanged(CpuInfoConfigurationManager.this.isCpuInfoPolicyEnabled, CpuInfoConfigurationManager.this.mCpuInfoPollMinInterval);
                }
            }
        }
    };

    public interface ICpuInfoEnabledChangeCallback {
        void onCpuInfoEnableChanged(boolean z, long j);
    }

    private void parseCpuinfoPolicy(ContentValuesList list) {
        if (list != null && (list.isEmpty() ^ 1) != 0) {
            for (ContentValues cv : list.getValues()) {
                if (cv.containsKey(EPM_CPUINFO_CV_LIST_NAME_INTERVAL_KEY)) {
                    this.mCpuInfoPollMinInterval = cv.getAsLong(EPM_CPUINFO_CV_LIST_NAME_INTERVAL_KEY).longValue();
                }
                if (cv.containsKey(EPM_CPUINFO_CV_LIST_NAME_LOW_BATTERY_KEY)) {
                    this.isCpuInfoPollWhenLowBattery = cv.getAsBoolean(EPM_CPUINFO_CV_LIST_NAME_LOW_BATTERY_KEY).booleanValue();
                }
                if (cv.containsKey(EPM_CPUINFO_CV_LIST_NAME_REPORT_THRESHOLD)) {
                    this.mCpuReportMinThreshold = cv.getAsInteger(EPM_CPUINFO_CV_LIST_NAME_REPORT_THRESHOLD).intValue();
                }
            }
        }
    }

    private CpuInfoConfigurationManager() {
        boolean z = false;
        if (!(this.mCpuInfoPolicySwitch == null || (this.mCpuInfoPolicySwitch.isUninitialized() ^ 1) == 0)) {
            z = this.mCpuInfoPolicySwitch.isOn();
        }
        this.isCpuInfoPolicyEnabled = z;
        this.mDefaultConfigurationManager.registerSwitchObserver(this.mCpuInfoPolicySwitch, this.mCpuInfoPolicySwitchObserver);
        this.mEpmCpuInfoPolicyList = this.mDefaultConfigurationManager.getContentValuesList(EPM_CPUINFO_CV_LIST_NAME);
        parseCpuinfoPolicy(this.mEpmCpuInfoPolicyList);
        this.mDefaultConfigurationManager.registerContentValuesListObserver(this.mEpmCpuInfoPolicyList, this.mEpmCpuInfoPolicyObserver);
    }

    public static synchronized CpuInfoConfigurationManager getInstance() {
        CpuInfoConfigurationManager cpuInfoConfigurationManager;
        synchronized (CpuInfoConfigurationManager.class) {
            if (sIntance == null) {
                sIntance = new CpuInfoConfigurationManager();
            }
            cpuInfoConfigurationManager = sIntance;
        }
        return cpuInfoConfigurationManager;
    }

    public boolean isCpuInfoPolicyEnabled() {
        return this.isCpuInfoPolicyEnabled;
    }

    public long getCpuInfoPollMinInterval() {
        return this.mCpuInfoPollMinInterval;
    }

    public boolean isCpuInfoPollWhenLowBattery() {
        return this.isCpuInfoPollWhenLowBattery;
    }

    public int getCpuInfoReportMinThreshold() {
        return this.mCpuReportMinThreshold;
    }

    public void registerCpuInfoEnabledChangeCallback(ICpuInfoEnabledChangeCallback callback) {
        if (!this.mCpuInfoEnabledChangeCallback.contains(callback)) {
            this.mCpuInfoEnabledChangeCallback.add(callback);
        }
    }

    public void unregisterCpuInfoEnabledChangeCallback(ICpuInfoEnabledChangeCallback callback) {
        this.mCpuInfoEnabledChangeCallback.remove(callback);
    }

    private void dispatchCpuInfoEnableChanged(boolean enabled, long pollInterval) {
        for (ICpuInfoEnabledChangeCallback cb : this.mCpuInfoEnabledChangeCallback) {
            cb.onCpuInfoEnableChanged(enabled, pollInterval);
        }
    }

    public String dumpCpuInfoConfiguration() {
        return "{isCpuInfoPolicyEnabled=" + this.isCpuInfoPolicyEnabled + " mCpuInfoPollMinInterval=" + this.mCpuInfoPollMinInterval + " isCpuInfoPollWhenLowBattery=" + this.isCpuInfoPollWhenLowBattery + " mCpuReportMinThreshold=" + this.mCpuReportMinThreshold + "}";
    }
}
