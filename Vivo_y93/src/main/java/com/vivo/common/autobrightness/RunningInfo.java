package com.vivo.common.autobrightness;

import org.json.JSONException;
import org.json.JSONObject;

public class RunningInfo {
    private static final String KEY_AN = "An";
    private static final String KEY_BACKLIGHT = "Lcm";
    private static final String KEY_CHANGE_BY = "cBy";
    private static final String KEY_LOCATION = "lc";
    private static final String KEY_LUX = "Lux";
    private static final String KEY_MODE = "Mod";
    private static final String KEY_OFF_BY = "oBy";
    private static final String KEY_PKG = "Pkg";
    private static final String KEY_PRE_AN = "pAn";
    private static final String KEY_PRE_BACKLIGHT = "pLcm";
    private static final String KEY_PRE_MODE = "pMod";
    private static final String KEY_PRE_PKG = "pPkg";
    private static final String KEY_PRE_SETTING = "pSet";
    private static final String KEY_PWR_ASSISTANT = "Ass";
    private static final String KEY_PWR_PERCENT = "Pct";
    private static final String KEY_PWR_SAVING = "Sav";
    private static final String KEY_REASON = "Rea";
    private static final String KEY_SETTING = "Set";
    public static final String REASON_BOOT = "boot";
    public static final String REASON_SELF = "self";
    public static final String REASON_USER = "user";
    public String an;
    public int backlight;
    public String changeBy;
    public String location;
    public int lux;
    public int mode;
    public String offBy;
    public String pkg;
    public String preAn;
    public int preBacklight;
    public int preMode;
    public String prePkg;
    public int preSetting;
    public boolean pwrAssistant = false;
    public int pwrPercent = -1;
    public boolean pwrSaving = false;
    public String reason;
    public int setting;

    public RunningInfo(int preBacklight, int preSetting, int preMode, int backlight, int setting, int mode, String offBy, String changeBy, String pkg, String prePkg, String an, String preAn, String location, boolean pwrSaving) {
        this.preBacklight = preBacklight;
        this.preSetting = preSetting;
        this.preMode = preMode;
        this.backlight = backlight;
        this.setting = setting;
        this.mode = mode;
        this.offBy = offBy;
        this.changeBy = changeBy;
        this.pkg = pkg;
        this.prePkg = prePkg;
        this.an = an;
        this.preAn = preAn;
        this.location = location;
        this.pwrSaving = pwrSaving;
    }

    public JSONObject toJsonObject() {
        JSONObject ret = new JSONObject();
        try {
            ret.put(KEY_REASON, this.reason);
            ret.put(KEY_LUX, this.lux);
            ret.put(KEY_PRE_BACKLIGHT, this.preBacklight);
            ret.put(KEY_PRE_SETTING, this.preSetting);
            ret.put(KEY_PRE_MODE, this.preMode);
            ret.put(KEY_BACKLIGHT, this.backlight);
            ret.put(KEY_SETTING, this.setting);
            ret.put(KEY_MODE, this.mode);
            ret.put(KEY_OFF_BY, this.offBy);
            ret.put(KEY_CHANGE_BY, this.changeBy);
            ret.put(KEY_PKG, this.pkg);
            ret.put(KEY_PRE_PKG, this.prePkg);
            ret.put(KEY_AN, this.an);
            ret.put(KEY_PRE_AN, this.preAn);
            ret.put(KEY_LOCATION, this.location);
            ret.put(KEY_PWR_PERCENT, this.pwrPercent);
            ret.put(KEY_PWR_SAVING, this.pwrSaving);
            ret.put(KEY_PWR_ASSISTANT, this.pwrAssistant);
            return ret;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
