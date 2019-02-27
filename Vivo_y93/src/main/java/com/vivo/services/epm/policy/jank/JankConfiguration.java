package com.vivo.services.epm.policy.jank;

import android.content.ContentValues;
import android.os.Handler;
import android.util.Slog;
import com.vivo.services.epm.config.ConfigurationObserver;
import com.vivo.services.epm.config.ContentValuesList;
import com.vivo.services.epm.config.DefaultConfigurationManager;
import com.vivo.services.epm.config.StringList;
import com.vivo.services.epm.config.Switch;
import java.util.ArrayList;
import java.util.List;

public class JankConfiguration {
    private static final int CONFIG_APP_JANK_COUNT = 3;
    private static final int CONFIG_APP_JANK_WARM_COUNT = 2;
    private static final long CONFIG_APP_JANK_WARM_INTERVAL = 86400;
    private static final int CONFIG_CPU_THRESHOLD = 50;
    private static final boolean CONFIG_ENABLE = true;
    private static final int CONFIG_IO_THRESHOLD = 0;
    private static final int CONFIG_MEMORY_THRESHOLD = 20;
    private static final int CONFIG_NETWORK_THRESHOLD = 0;
    private static final String KEY_APP_JANK_COUNT = "epm_jank_count";
    private static final String KEY_APP_JANK_WARM_COUNT = "epm_jank_warm_count";
    private static final String KEY_APP_JANK_WARM_INTERVAL = "epm_jank_warm_interval";
    private static final String KEY_BLACKLIST = "epm_jank_blacklist";
    private static final String KEY_CPU_THRESHOLD = "epm_jank_cpu";
    private static final String KEY_IO_THRESHOLD = "epm_jank_io";
    private static final String KEY_JANK_ENABLE = "epm_jank_enable";
    private static final String KEY_JANK_POLICY = "epm_jank_policy";
    private static final String KEY_MEMORY_THRESHOLD = "epm_jank_mem";
    private static final String KEY_NETWORK_THRESHOLD = "epm_jank_network";
    private static final String TAG = "JEPH";
    private List<String> DEFAULT_BLACK_LIST = new ArrayList();
    private List<String> mBlackList = new ArrayList();
    private ContentValuesList mContentValuesList;
    private DefaultConfigurationManager mDefaultConfigurationManager;
    private boolean mEnable;
    private Handler mHandler;
    private int mJankCount;
    private int mJankCpuThreshold;
    private int mJankIoThreshold;
    private int mJankMemoryThreshold;
    private int mJankNetworkThreshold;
    private Switch mJankSwitch;
    private int mJankWarmCount;
    private long mJankWarmInterval;
    private StringList mStringList;

    public boolean isEnable() {
        return this.mEnable;
    }

    public int getJankCount() {
        return this.mJankCount;
    }

    public int getJankWarmCount() {
        return this.mJankWarmCount;
    }

    public long getJankWarmInterval() {
        return this.mJankWarmInterval;
    }

    public int getJankCpuThreshold() {
        return this.mJankCpuThreshold;
    }

    public int getJankMemoryThreshold() {
        return this.mJankMemoryThreshold;
    }

    public int getJankIoThreshold() {
        return this.mJankIoThreshold;
    }

    public int getJankNetworkThreshold() {
        return this.mJankNetworkThreshold;
    }

    public boolean isInBlackList(String pkgName) {
        boolean contains;
        synchronized (this.mBlackList) {
            contains = !this.DEFAULT_BLACK_LIST.contains(pkgName) ? this.mBlackList.contains(pkgName) : CONFIG_ENABLE;
        }
        return contains;
    }

    public JankConfiguration(Handler handler) {
        this.mHandler = handler;
        this.mDefaultConfigurationManager = DefaultConfigurationManager.getInstance();
        try {
            parseSwitch();
            parsePolicy();
            parseList();
            registConfigurationObserver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.DEFAULT_BLACK_LIST.add("com.tencent.tmgp.sgame");
        this.DEFAULT_BLACK_LIST.add("com.tencent.tmgp.speedmobile");
        this.DEFAULT_BLACK_LIST.add("com.tencent.wegame.mangod");
        this.DEFAULT_BLACK_LIST.add("com.netease.hyxd.aligames");
        this.DEFAULT_BLACK_LIST.add("com.tencent.cldts");
        this.DEFAULT_BLACK_LIST.add("com.netease.zjz.uc");
        this.DEFAULT_BLACK_LIST.add("com.tencent.ngame.chty");
    }

    private void parseSwitchInHandler() {
        this.mHandler.post(new Runnable() {
            public void run() {
                JankConfiguration.this.parseSwitch();
            }
        });
    }

    private void parsePolicyInHandler() {
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    JankConfiguration.this.parsePolicy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void parseListInHandler() {
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    JankConfiguration.this.parseList();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void registConfigurationObserver() {
        this.mDefaultConfigurationManager.registerSwitchObserver(this.mJankSwitch, new ConfigurationObserver() {
            public void onConfigChange(String file, String name) {
                Slog.d("JEPH", "file=" + file + " name=" + name + " mEnable=" + JankConfiguration.this.mEnable);
                JankConfiguration.this.parseSwitchInHandler();
            }
        });
        this.mDefaultConfigurationManager.registerContentValuesListObserver(this.mContentValuesList, new ConfigurationObserver() {
            public void onConfigChange(String file, String name) {
                Slog.d("JEPH", "file=" + file + " name=" + name);
                JankConfiguration.this.parsePolicyInHandler();
            }
        });
        this.mDefaultConfigurationManager.registerStringListObserver(this.mStringList, new ConfigurationObserver() {
            public void onConfigChange(String file, String name) {
                Slog.d("JEPH", "file=" + file + " name=" + name);
                JankConfiguration.this.parseListInHandler();
            }
        });
    }

    private void parseSwitch() {
        Slog.d("JEPH", "parseSwitch...");
        this.mJankSwitch = this.mDefaultConfigurationManager.getSwitch(KEY_JANK_ENABLE);
        boolean isOn = (this.mJankSwitch == null || (this.mJankSwitch.isUninitialized() ^ 1) == 0) ? CONFIG_ENABLE : this.mJankSwitch.isOn();
        this.mEnable = isOn;
        Slog.d("JEPH", "parseSwitch =" + this.mEnable);
    }

    private void parsePolicy() {
        Slog.d("JEPH", "parsePolicy...");
        this.mContentValuesList = this.mDefaultConfigurationManager.getContentValuesList(KEY_JANK_POLICY);
        this.mJankCount = 3;
        this.mJankWarmCount = 2;
        this.mJankWarmInterval = 86400000;
        this.mJankCpuThreshold = CONFIG_CPU_THRESHOLD;
        this.mJankMemoryThreshold = 20;
        this.mJankIoThreshold = 0;
        this.mJankNetworkThreshold = 0;
        if (this.mContentValuesList != null && (this.mContentValuesList.isEmpty() ^ 1) != 0) {
            Slog.d("JEPH", "mContentValuesList:" + this.mContentValuesList);
            for (ContentValues cv : this.mContentValuesList.getValues()) {
                if (cv.getAsString("name").equals(KEY_APP_JANK_COUNT)) {
                    try {
                        this.mJankCount = Integer.parseInt(cv.getAsString("value"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (cv.getAsString("name").equals(KEY_APP_JANK_WARM_COUNT)) {
                    try {
                        this.mJankWarmCount = Integer.parseInt(cv.getAsString("value"));
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                } else if (cv.getAsString("name").equals(KEY_APP_JANK_WARM_INTERVAL)) {
                    try {
                        this.mJankWarmInterval = Long.parseLong(cv.getAsString("value")) * 1000;
                    } catch (Exception e22) {
                        e22.printStackTrace();
                    }
                } else if (cv.getAsString("name").equals(KEY_CPU_THRESHOLD)) {
                    try {
                        this.mJankCpuThreshold = Integer.parseInt(cv.getAsString("value"));
                    } catch (Exception e222) {
                        e222.printStackTrace();
                    }
                } else if (cv.getAsString("name").equals(KEY_MEMORY_THRESHOLD)) {
                    try {
                        this.mJankMemoryThreshold = Integer.parseInt(cv.getAsString("value"));
                    } catch (Exception e2222) {
                        e2222.printStackTrace();
                    }
                } else if (cv.getAsString("name").equals(KEY_IO_THRESHOLD)) {
                    try {
                        this.mJankIoThreshold = Integer.parseInt(cv.getAsString("value"));
                    } catch (Exception e22222) {
                        e22222.printStackTrace();
                    }
                } else if (cv.getAsString("name").equals(KEY_NETWORK_THRESHOLD)) {
                    try {
                        this.mJankNetworkThreshold = Integer.parseInt(cv.getAsString("value"));
                    } catch (Exception e222222) {
                        e222222.printStackTrace();
                    }
                }
            }
            Slog.d("JEPH", "mJankCount:" + this.mJankCount + " ,mJankWarmCount:" + this.mJankWarmCount + " ,mJankWarmInterval:" + this.mJankWarmInterval + " ,mJankCpuThreshold:" + this.mJankCpuThreshold + " ,mJankMemoryThreshold:" + this.mJankMemoryThreshold + ",mJankIoThreshold:" + this.mJankIoThreshold + ",mJankNetworkThreshold:" + this.mJankNetworkThreshold);
        }
    }

    private void parseList() {
        Slog.d("JEPH", "parseList...");
        this.mStringList = this.mDefaultConfigurationManager.getStringList(KEY_BLACKLIST);
        if (this.mStringList != null && (this.mStringList.isEmpty() ^ 1) != 0) {
            List<String> list = this.mStringList.getValues();
            Slog.d("JEPH", "parseList =" + list.toString());
            synchronized (this.mBlackList) {
                this.mBlackList.clear();
                this.mBlackList.addAll(list);
            }
        }
    }
}
