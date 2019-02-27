package com.vivo.common.utils;

import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings.System;
import android.util.Log;

public class CrontabGuideUtil {
    private static final String[] countFlag = new String[]{"bbk_crontab_used_poweroff_count", "bbk_crontab_used_poweron_count", "bbk_crontab_used_vibrate_count", "bbk_crontab_used_offline_count", "bbk_crontab_used_systemclean_count"};
    private static final String[] firstTimeFlag = new String[]{"bbk_crontab_used_poweroff_firsttime", "bbk_crontab_used_poweron_firsttime", "bbk_crontab_used_vibrate_firsttime", "bbk_crontab_used_offline_firsttime", "bbk_crontab_used_systemclean_firsttime"};
    private static CrontabGuideUtil mUtil = null;
    private Context mContext = null;

    public CrontabGuideUtil(Context context) {
        this.mContext = context;
    }

    public static CrontabGuideUtil getInstance(Context context) {
        if (mUtil == null) {
            mUtil = new CrontabGuideUtil(context);
        }
        return mUtil;
    }

    private boolean isPhoneVibrate() {
        boolean ret = false;
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        if (audioManager == null) {
            return false;
        }
        if (audioManager.getRingerMode() == 1) {
            ret = true;
        }
        return ret;
    }

    private boolean isPhoneOffline() {
        return System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
    }

    public boolean isMatchPoweroffGuide() {
        boolean ret = false;
        if (haveUsedCrontab() || !isTimeMatch(1)) {
            return false;
        }
        if (isMatchGuide(1)) {
            ret = true;
        }
        return ret;
    }

    public boolean isMatchPoweronGuide() {
        boolean ret = false;
        if (haveUsedCrontab() || !isTimeMatch(2)) {
            return false;
        }
        if (isMatchGuide(2)) {
            ret = true;
        }
        return ret;
    }

    public boolean isMatchVibrateGuide() {
        boolean ret = false;
        if (isPhoneVibrate() || haveUsedCrontab() || !isTimeMatch(3)) {
            return false;
        }
        if (isMatchGuide(3)) {
            ret = true;
        }
        return ret;
    }

    public boolean isMatchOfflineGuide() {
        boolean ret = false;
        if (isPhoneOffline() || haveUsedCrontab() || !isTimeMatch(4)) {
            return false;
        }
        if (isMatchGuide(4)) {
            ret = true;
        }
        return ret;
    }

    public boolean isMatchSystemcleanGuide() {
        return false;
    }

    private boolean haveUsedCrontab() {
        String bbk_crontab_used = System.getString(this.mContext.getContentResolver(), "bbk_crontab_used");
        if (bbk_crontab_used == null || !bbk_crontab_used.equals("1")) {
            return false;
        }
        return true;
    }

    private boolean isTimeMatch(int flag) {
        long now = System.currentTimeMillis();
        int used_count = System.getInt(this.mContext.getContentResolver(), countFlag[flag - 1], 0);
        if (used_count == 3) {
            return false;
        }
        if (used_count == 0) {
            System.putLong(this.mContext.getContentResolver(), firstTimeFlag[flag - 1], now);
            System.putInt(this.mContext.getContentResolver(), countFlag[flag - 1], 1);
            return false;
        }
        boolean ret;
        Log.d("ABC", "used_count=" + used_count);
        if (used_count == 5) {
            long time = System.getLong(this.mContext.getContentResolver(), firstTimeFlag[flag - 1], 0) + 86400000;
            Log.d("ABC", "time match time=" + time + ",now=" + now + ",diff=" + (time - now));
            System.putLong(this.mContext.getContentResolver(), firstTimeFlag[flag - 1], time);
            System.putInt(this.mContext.getContentResolver(), countFlag[flag - 1], 2);
            used_count = System.getInt(this.mContext.getContentResolver(), countFlag[flag - 1], 0);
        }
        long firstTime = System.getLong(this.mContext.getContentResolver(), firstTimeFlag[flag - 1], 0);
        long time1 = (82800000 + firstTime) + ((long) ((used_count - 1) * 86400000));
        long time2 = (90000000 + firstTime) + ((long) ((used_count - 1) * 86400000));
        Log.d("ABC", "time match time1=" + time1 + ",time2=" + time2);
        if (now <= time1 || now >= time2) {
            System.putLong(this.mContext.getContentResolver(), firstTimeFlag[flag - 1], now);
            System.putInt(this.mContext.getContentResolver(), countFlag[flag - 1], 1);
            ret = false;
        } else {
            ret = true;
        }
        return ret;
    }

    private boolean isMatchGuide(int flag) {
        int used_count = System.getInt(this.mContext.getContentResolver(), countFlag[flag - 1], 0);
        if (used_count >= 2) {
            ((StatusBarManager) this.mContext.getSystemService("statusbar")).collapsePanels();
            Intent i = new Intent("bbk.intent.action.CRONTAB.USER_GUIDE");
            i.putExtra("flag", flag);
            this.mContext.sendBroadcast(i);
            return true;
        }
        System.putInt(this.mContext.getContentResolver(), countFlag[flag - 1], used_count + 1);
        return false;
    }
}
