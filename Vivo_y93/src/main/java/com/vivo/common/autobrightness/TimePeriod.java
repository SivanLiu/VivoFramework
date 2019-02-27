package com.vivo.common.autobrightness;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Slog;
import com.vivo.common.provider.Weather;
import java.util.Calendar;

public class TimePeriod {
    private static final String INTENT_DAY_END_CLOCK_TIMER = "android.intent.action.auto_bright.day.end";
    private static final String INTENT_DAY_MID_CLOCK_TIMER = "android.intent.action.auto_bright.day.mid";
    private static final String INTENT_DAY_START_CLOCK_TIMER = "android.intent.action.auto_bright.day.start";
    private static final long MILLI_OF_HOUR = 3600000;
    private static final String TAG = "TimePeriod";
    private static TimePeriod mInstance = null;
    private static Object mLock = new Object();
    private long DAY_REPEAT_INTERVAL = 86400000;
    private long days = 0;
    private boolean firstSetup = false;
    private Context mContext;
    private final long mDayEndTime = 72000000;
    private final long mDayStartTime = 25200000;
    private Handler mHandler;
    private final long mMorningStartTime = 18000000;
    private final long mNightEndTime = 82800000;
    private PendingIntent mPendingEndIntent = null;
    private PendingIntent mPendingMidIntent = null;
    private PendingIntent mPendingStartIntent = null;
    private PeriodType mPeriodType = PeriodType.NIGHT_TIME;
    private AutoBrightBroadcastReceiver mReceiver = new AutoBrightBroadcastReceiver(this, null);
    private Runnable mRunnable = new Runnable() {
        public void run() {
            TimePeriod.this.mPendingStartIntent = TimePeriod.this.getPendingIntent(TimePeriod.INTENT_DAY_START_CLOCK_TIMER);
            TimePeriod.this.mPendingEndIntent = TimePeriod.this.getPendingIntent(TimePeriod.INTENT_DAY_END_CLOCK_TIMER);
            TimePeriod.this.mPendingMidIntent = TimePeriod.this.getPendingIntent(TimePeriod.INTENT_DAY_MID_CLOCK_TIMER);
            TimePeriod.this.mContext.registerReceiver(TimePeriod.this.mReceiver, new IntentFilter(TimePeriod.INTENT_DAY_START_CLOCK_TIMER), null, TimePeriod.this.mHandler);
            TimePeriod.this.mContext.registerReceiver(TimePeriod.this.mReceiver, new IntentFilter(TimePeriod.INTENT_DAY_END_CLOCK_TIMER), null, TimePeriod.this.mHandler);
            TimePeriod.this.mContext.registerReceiver(TimePeriod.this.mReceiver, new IntentFilter(TimePeriod.INTENT_DAY_MID_CLOCK_TIMER), null, TimePeriod.this.mHandler);
            TimePeriod.this.mContext.registerReceiver(TimePeriod.this.mReceiver, new IntentFilter("android.intent.action.TIME_SET"), null, TimePeriod.this.mHandler);
            TimePeriod.this.mContext.registerReceiver(TimePeriod.this.mReceiver, new IntentFilter("android.intent.action.TIMEZONE_CHANGED"), null, TimePeriod.this.mHandler);
            TimePeriod.this.mContext.registerReceiver(TimePeriod.this.mReceiver, new IntentFilter("android.intent.action.DATE_CHANGED"), null, TimePeriod.this.mHandler);
            TimePeriod.this.setupTimer();
        }
    };
    private WakeLock mWakeLock;
    private boolean waitSunRise = false;

    private class AutoBrightBroadcastReceiver extends BroadcastReceiver {
        /* synthetic */ AutoBrightBroadcastReceiver(TimePeriod this$0, AutoBrightBroadcastReceiver -this1) {
            this();
        }

        private AutoBrightBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (TimePeriod.mLock) {
                if (intent == null) {
                    return;
                }
                TimePeriod.this.acquireWakeLock();
                String action = intent.getAction();
                TimePeriod.this.log("onReceive action=" + action + " , call calcNowPeriodType");
                TimePeriod.this.calcNowPeriodType();
                if (TimePeriod.this.waitSunRise) {
                    if (action.equals(TimePeriod.INTENT_DAY_START_CLOCK_TIMER)) {
                        TimePeriod.this.days = TimePeriod.this.days + 1;
                    }
                } else if (action.equals(TimePeriod.INTENT_DAY_END_CLOCK_TIMER)) {
                    TimePeriod.this.days = TimePeriod.this.days + 1;
                }
                if (TimePeriod.this.firstSetup) {
                    TimePeriod.this.firstSetup = false;
                    TimePeriod.this.days = 0;
                }
                TimePeriod.this.setupTimer();
            }
        }
    }

    public enum PeriodType {
        DAY_TIME,
        NIGHT_TIME,
        DARK_NIGHT_TIME,
        MORNING_TIME
    }

    private PeriodType calcNowPeriodType() {
        long millis = getNowOfDay(Calendar.getInstance());
        if (millis >= 25200000 && millis < 72000000) {
            this.mPeriodType = PeriodType.DAY_TIME;
            log("calcNowPeriodType DAY_TIME");
        } else if (millis >= 18000000 && millis < 25200000) {
            this.mPeriodType = PeriodType.MORNING_TIME;
            log("calcNowPeriodType MORNING_TIME");
        } else if (millis < 72000000 || millis >= 82800000) {
            this.mPeriodType = PeriodType.DARK_NIGHT_TIME;
            log("calcNowPeriodType DARK_NIGHT_TIME");
        } else {
            this.mPeriodType = PeriodType.NIGHT_TIME;
            log("calcNowPeriodType NIGHT_TIME");
        }
        log("calcNowPeriodType");
        return this.mPeriodType;
    }

    private TimePeriod(Context context, Looper looper) {
        this.mContext = context;
        this.mHandler = new Handler(looper);
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        if (powerManager != null) {
            this.mWakeLock = powerManager.newWakeLock(1, TAG);
        }
        this.firstSetup = true;
        calcNowPeriodType();
        this.mHandler.postDelayed(this.mRunnable, 3000);
    }

    public static TimePeriod getInstance(Context context, Looper looper) {
        synchronized (mLock) {
            TimePeriod timePeriod;
            if (mInstance != null) {
                timePeriod = mInstance;
                return timePeriod;
            }
            mInstance = new TimePeriod(context, looper);
            timePeriod = mInstance;
            return timePeriod;
        }
    }

    private void acquireWakeLock() {
        if (this.mWakeLock != null && (this.mWakeLock.isHeld() ^ 1) != 0) {
            this.mWakeLock.acquire(500);
        }
    }

    private PendingIntent getPendingIntent(String action) {
        return PendingIntent.getBroadcast(this.mContext, 0, new Intent(action), 0);
    }

    private long getNowOfDay(Calendar calendar) {
        return (long) ((((((calendar.get(11) * 60) + calendar.get(12)) * 60) + calendar.get(13)) * Weather.WEATHERVERSION_ROM_2_0) + calendar.get(14));
    }

    private boolean setupTimer() {
        try {
            Calendar calendar = Calendar.getInstance();
            long now = calendar.getTimeInMillis();
            long nowOfDay = getNowOfDay(calendar);
            AlarmManager alarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
            alarmManager.cancel(this.mPendingStartIntent);
            alarmManager.cancel(this.mPendingEndIntent);
            alarmManager.cancel(this.mPendingMidIntent);
            long timeToDayStart = 25200000 > nowOfDay ? 25200000 - nowOfDay : (this.DAY_REPEAT_INTERVAL + 25200000) - nowOfDay;
            long timeTodayEnd = 72000000 > nowOfDay ? 72000000 - nowOfDay : (this.DAY_REPEAT_INTERVAL + 72000000) - nowOfDay;
            long timeTodayMid = this.mPeriodType != PeriodType.DARK_NIGHT_TIME ? 82800000 - nowOfDay : 18000000 > nowOfDay ? 18000000 - nowOfDay : (this.DAY_REPEAT_INTERVAL + 18000000) - nowOfDay;
            if (this.firstSetup) {
                this.waitSunRise = timeTodayEnd > timeToDayStart;
            }
            alarmManager.setExact(0, now + timeToDayStart, this.mPendingStartIntent);
            alarmManager.setExact(0, now + timeTodayEnd, this.mPendingEndIntent);
            alarmManager.setExact(0, now + timeTodayMid, this.mPendingMidIntent);
            log("setupTimer timeInMills=" + now + " nowOfDay=" + nowOfDay + " timeToDayStart:" + timeToDayStart + " timeTodayEnd=" + timeTodayEnd + " timeTodayMid=" + timeTodayMid);
            return true;
        } catch (Exception ex) {
            log("setupNextTimer FAIL:" + ex);
            return false;
        }
    }

    private void cancelTimer() {
        log("cancelTimer()");
        AlarmManager alarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        alarmManager.cancel(this.mPendingStartIntent);
        alarmManager.cancel(this.mPendingEndIntent);
        alarmManager.cancel(this.mPendingMidIntent);
    }

    public PeriodType getTimePeriod() {
        return this.mPeriodType;
    }

    public String timePeriodToString() {
        if (this.mPeriodType == PeriodType.DAY_TIME) {
            return "DAY_TIME";
        }
        if (this.mPeriodType == PeriodType.NIGHT_TIME) {
            return "NIGHT_TIME";
        }
        if (this.mPeriodType == PeriodType.MORNING_TIME) {
            return "MORNING_TIME";
        }
        return "DARK_NIGHT_TIME";
    }

    public long getDays() {
        return this.days;
    }

    private void log(String msg) {
        if (AblConfig.isDebug()) {
            Slog.d(TAG, msg);
        }
    }
}
