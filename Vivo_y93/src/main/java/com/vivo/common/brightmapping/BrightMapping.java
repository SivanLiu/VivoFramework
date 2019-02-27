package com.vivo.common.brightmapping;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Slog;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class BrightMapping {
    private static boolean DEBUG = SystemProperties.getBoolean("debug.bright.mapping", false);
    private static final String TAG = "BrightMapping";
    private Context mContext;
    private final int mMaxSettingBrightness = 255;
    private final int mMinSettingBrightness;

    public BrightMapping(Context contxt) {
        this.mContext = contxt;
        PowerManager mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        if (MappingConfig.isNeedMapping()) {
            this.mMinSettingBrightness = 2;
        } else if (mPowerManager != null) {
            this.mMinSettingBrightness = mPowerManager.getMinimumScreenBrightnessSetting();
        } else {
            this.mMinSettingBrightness = 20;
        }
    }

    public boolean isNeedBrightMapping() {
        return MappingConfig.isNeedMapping();
    }

    public int getBrightProgressMin() {
        if (MappingConfig.isNeedMapping()) {
            return MappingConfig.getProgressMin();
        }
        return this.mMinSettingBrightness;
    }

    public int getBrightProgressMax() {
        if (MappingConfig.isNeedMapping()) {
            return MappingConfig.getProgressMax();
        }
        return 255;
    }

    private int validateProgress(int progress) {
        if (progress > MappingConfig.getProgressMax()) {
            return MappingConfig.getProgressMax();
        }
        if (progress < MappingConfig.getProgressMin()) {
            return MappingConfig.getProgressMin();
        }
        return progress;
    }

    private int validateSetting(int setting) {
        if (setting > 255) {
            return 255;
        }
        if (setting < this.mMinSettingBrightness) {
            return this.mMinSettingBrightness;
        }
        return setting;
    }

    public int progressMappingToSetting(int progress) {
        int pro = progress;
        progress = validateProgress(progress);
        if (pro != progress) {
            Slog.w(TAG, "progressMappingToSetting validate progress from " + pro + " to " + progress);
        }
        double percent = (((double) progress) * 1.0d) / (((double) MappingConfig.getProgressMax()) * 1.0d);
        int setting = validateSetting((int) Math.round(((Math.pow(percent, 2.0d) * 234.0d) + (19.0d * percent)) + 2.0d));
        if (DEBUG) {
            Slog.d(TAG, "progressMappingToSetting progress:" + progress + "to setting:" + setting);
        }
        return setting;
    }

    public int settingMappingToProgress(int setting) {
        int tempSet = setting;
        setting = validateSetting(setting);
        if (tempSet != setting) {
            Slog.w(TAG, "settingMappingToProgress validate setting from " + tempSet + " to " + setting);
        }
        if (setting == this.mMinSettingBrightness) {
            return getBrightProgressMin();
        }
        int progress = (int) Math.round(((double) MappingConfig.getProgressMax()) * ((Math.sqrt((double) ((setting * 936) - 1511)) - 0.2265625d) / 468.0d));
        if (DEBUG) {
            Slog.d(TAG, "settingMappingToProgress setting:" + setting + " to progress:" + progress);
        }
        return progress;
    }
}
