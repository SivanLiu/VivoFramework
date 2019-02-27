package com.vivo.common.widget.appwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.common.provider.Weather;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AppWidgetClock {
    private static final int HALFSECONDUPDATE = 2;
    private static final int PERIODUPDATE = 3;
    private static final int SECONDUPDATE = 1;
    private static final String TAG = "AppWidgetClock";
    private ClockDateTime mClockDateTime;
    private Context mContext;
    private HalfSecondUpdateListener mHalfSecondUpdateListener;
    private boolean mIsBroadcastRegistered;
    private int mPeriod;
    private PeriodUpdateListener mPeriodUpdateListener;
    private BroadcastReceiver mReceiver;
    private SecondUpdateListener mSecondUpdateListener;
    private TimeListener mTimeListener;
    private Handler mUpdateHandler;

    public static class ClockDateTime {
        public static final int AM = 0;
        private static final String[] MONTHSHORT = new String[]{"Jan.", "Feb.", "Mar.", "Apr.", "May.", "June.", "July.", "Aug.", "Sept.", "Oct.", "Nov.", "Dec."};
        public static final int PM = 1;
        private int mAmPm;
        private Calendar mCalendar;
        private Context mContext;
        private int mDayOfMonth;
        private int mDayOfWeek;
        private String mDayOfWeekCn;
        private String mDayOfWeekEnLong;
        private String mDayOfWeekEnShort;
        private int mHour;
        private int mHourHigh;
        private int mHourLow;
        private int mHourOfDay;
        private boolean mIs24HourFormat;
        private boolean mIsChina;
        private int mMillisecond;
        private int mMinute;
        private int mMinuteHigh;
        private int mMinuteLow;
        private int mMonth;
        private String mMonthEnLong;
        private String mMonthEnShort;
        private int mSecond;
        private int mYear;

        public ClockDateTime(Context context) {
            this.mContext = context;
            setToNow();
        }

        public Calendar getCalendar(boolean update) {
            if (update) {
                setToNow();
            }
            return this.mCalendar;
        }

        public boolean isChina() {
            return this.mIsChina;
        }

        public int getHour() {
            return this.mHour;
        }

        public int getAmPm() {
            return this.mAmPm;
        }

        public String getAmPmCn() {
            if (this.mAmPm == 0) {
                return "上午";
            }
            return "下午";
        }

        public String getAmPmEn() {
            if (this.mAmPm == 0) {
                return "am";
            }
            return "pm";
        }

        public String getAmPmStr() {
            if (this.mIsChina) {
                return getAmPmCn();
            }
            return getAmPmEn();
        }

        public int getMinute() {
            return this.mMinute;
        }

        public int getSecond() {
            return this.mSecond;
        }

        public int getMillisecond() {
            return this.mMillisecond;
        }

        public int getYear() {
            return this.mYear;
        }

        public int getMonth() {
            return this.mMonth;
        }

        public int getDayOfMonth() {
            return this.mDayOfMonth;
        }

        public int getDayOfWeek() {
            return this.mDayOfWeek;
        }

        public int getHourOfDay() {
            return this.mHourOfDay;
        }

        public boolean is24HourFormat() {
            return this.mIs24HourFormat;
        }

        public int getHourHigh() {
            return this.mHourHigh;
        }

        public int getHourLow() {
            return this.mHourLow;
        }

        public int getMinuteHigh() {
            return this.mMinuteHigh;
        }

        public int getMinuteLow() {
            return this.mMinuteLow;
        }

        public String getHourString() {
            return this.mHourHigh + Events.DEFAULT_SORT_ORDER + this.mHourLow;
        }

        public String getMinuteString() {
            return this.mMinuteHigh + Events.DEFAULT_SORT_ORDER + this.mMinuteLow;
        }

        public String getHourMinuteString() {
            return this.mHourHigh + Events.DEFAULT_SORT_ORDER + this.mHourLow + ":" + this.mMinuteHigh + this.mMinuteLow;
        }

        public String getYearCn() {
            return this.mYear + "年";
        }

        public String getMonthCn() {
            return this.mMonth + "月";
        }

        public String getDayOfMonthCn() {
            return this.mDayOfMonth + "日";
        }

        public String getMonthDayCn() {
            return this.mMonth + "月" + this.mDayOfMonth + "日";
        }

        public String getYearMonthDayCn() {
            return this.mYear + "年" + this.mMonth + "月" + this.mDayOfMonth + "日";
        }

        public String getMonthEnShort() {
            return this.mMonthEnShort;
        }

        public String getMonthEnLong() {
            return this.mMonthEnLong;
        }

        public String getMonthDayEnShort() {
            return this.mMonthEnShort + " " + this.mDayOfMonth;
        }

        public String getMonthDayEnLong() {
            return this.mMonthEnLong + " " + this.mDayOfMonth;
        }

        public String getMonthDay() {
            if (this.mIsChina) {
                return getMonthDayCn();
            }
            return getMonthDayEnShort();
        }

        public String getDayOfWeekCn() {
            return this.mDayOfWeekCn;
        }

        public String getDayOfWeekEnShort() {
            return this.mDayOfWeekEnShort;
        }

        public String getDayOfWeekEnLong() {
            return this.mDayOfWeekEnLong;
        }

        public String getFormatString(String format) {
            return new SimpleDateFormat(format, Locale.getDefault()).format(this.mCalendar.getTime());
        }

        public void setToNow() {
            this.mIsChina = Locale.getDefault().equals(Locale.CHINA);
            this.mCalendar = Calendar.getInstance();
            this.mIs24HourFormat = DateFormat.is24HourFormat(this.mContext);
            this.mHour = this.mCalendar.get(10);
            if (this.mHour == 0) {
                this.mHour = 12;
            }
            this.mHourOfDay = this.mCalendar.get(11);
            this.mMinute = this.mCalendar.get(12);
            this.mSecond = this.mCalendar.get(13);
            this.mMillisecond = this.mCalendar.get(14);
            this.mYear = this.mCalendar.get(1);
            this.mMonth = this.mCalendar.get(2);
            this.mDayOfMonth = this.mCalendar.get(5);
            this.mDayOfWeek = this.mCalendar.get(7);
            this.mMonthEnShort = MONTHSHORT[this.mMonth];
            this.mMonthEnLong = this.mCalendar.getDisplayName(2, 2, Locale.ENGLISH);
            this.mDayOfWeekCn = this.mCalendar.getDisplayName(7, 2, Locale.CHINA);
            this.mDayOfWeekEnShort = this.mCalendar.getDisplayName(7, 1, Locale.ENGLISH);
            this.mDayOfWeekEnLong = this.mCalendar.getDisplayName(7, 2, Locale.ENGLISH);
            this.mAmPm = this.mCalendar.get(9);
            if (this.mIs24HourFormat) {
                this.mHourHigh = this.mHourOfDay / 10;
                this.mHourLow = this.mHourOfDay % 10;
            } else {
                this.mHourHigh = this.mHour / 10;
                this.mHourLow = this.mHour % 10;
            }
            this.mMinuteHigh = this.mMinute / 10;
            this.mMinuteLow = this.mMinute % 10;
        }

        public String toString() {
            return "ClockDateTime [mCalendar=" + this.mCalendar + ", mContext=" + this.mContext + ", mHour=" + this.mHour + ", mHourOfDay=" + this.mHourOfDay + ", mAmPm=" + this.mAmPm + ", mMinute=" + this.mMinute + ", mSecond=" + this.mSecond + ", mMillisecond=" + this.mMillisecond + ", mYear=" + this.mYear + ", mMonth=" + this.mMonth + ", mDayOfMonth=" + this.mDayOfMonth + ", mDayOfWeek=" + this.mDayOfWeek + ", mIs24HourFormat=" + this.mIs24HourFormat + ", mHourHigh=" + this.mHourHigh + ", mHourLow=" + this.mHourLow + ", mMinuteHigh=" + this.mMinuteHigh + ", mMinuteLow=" + this.mMinuteLow + ", mMonthEnShort=" + this.mMonthEnShort + ", mMonthEnLong=" + this.mMonthEnLong + ", mDayOfWeekCn=" + this.mDayOfWeekCn + ", mDayOfWeekEnShort=" + this.mDayOfWeekEnShort + ", mDayOfWeekEnLong=" + this.mDayOfWeekEnLong + ", mIsChina=" + this.mIsChina + ", isChina()=" + isChina() + ", getHour()=" + getHour() + ", getAmPm()=" + getAmPm() + ", getAmPmCn()=" + getAmPmCn() + ", getAmPmEn()=" + getAmPmEn() + ", getAmPmStr()=" + getAmPmStr() + ", getMinute()=" + getMinute() + ", getSecond()=" + getSecond() + ", getMillisecond()=" + getMillisecond() + ", getYear()=" + getYear() + ", getMonth()=" + getMonth() + ", getDayOfMonth()=" + getDayOfMonth() + ", getDayOfWeek()=" + getDayOfWeek() + ", getHourOfDay()=" + getHourOfDay() + ", is24HourFormat()=" + is24HourFormat() + ", getHourHigh()=" + getHourHigh() + ", getHourLow()=" + getHourLow() + ", getMinuteHigh()=" + getMinuteHigh() + ", getMinuteLow()=" + getMinuteLow() + ", getHourString()=" + getHourString() + ", getMinuteString()=" + getMinuteString() + ", getHourMinuteString()=" + getHourMinuteString() + ", getYearCn()=" + getYearCn() + ", getMonthCn()=" + getMonthCn() + ", getDayOfMonthCn()=" + getDayOfMonthCn() + ", getMonthDayCn()=" + getMonthDayCn() + ", getYearMonthDayCn()=" + getYearMonthDayCn() + ", getMonthEnShort()=" + getMonthEnShort() + ", getMonthEnLong()=" + getMonthEnLong() + ", getMonthDayEnShort()=" + getMonthDayEnShort() + ", getMonthDayEnLong()=" + getMonthDayEnLong() + ", getMonthDay()=" + getMonthDay() + ", getDayOfWeekCn()=" + getDayOfWeekCn() + ", getDayOfWeekEnShort()=" + getDayOfWeekEnShort() + ", getDayOfWeekEnLong()=" + getDayOfWeekEnLong() + "]";
        }
    }

    private class ClockWidgetReceiver extends BroadcastReceiver {
        /* synthetic */ ClockWidgetReceiver(AppWidgetClock this$0, ClockWidgetReceiver -this1) {
            this();
        }

        private ClockWidgetReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(AppWidgetClock.TAG, "ClockWidgetReceiver action:" + action);
            if (action.equals("android.intent.action.TIME_TICK")) {
                if (AppWidgetClock.this.mTimeListener != null) {
                    AppWidgetClock.this.mClockDateTime.setToNow();
                    AppWidgetClock.this.mTimeListener.onTimeTick(AppWidgetClock.this.mClockDateTime);
                }
            } else if (action.equals("android.intent.action.TIME_SET")) {
                if (AppWidgetClock.this.mTimeListener != null) {
                    AppWidgetClock.this.mClockDateTime.setToNow();
                    AppWidgetClock.this.mTimeListener.onTimeChanged(AppWidgetClock.this.mClockDateTime);
                }
            } else if (action.equals("android.intent.action.TIMEZONE_CHANGED")) {
                if (AppWidgetClock.this.mTimeListener != null) {
                    AppWidgetClock.this.mClockDateTime.setToNow();
                    AppWidgetClock.this.mTimeListener.onTimezoneChanged(AppWidgetClock.this.mClockDateTime);
                }
            } else if (AppWidgetClock.this.mTimeListener != null) {
                AppWidgetClock.this.mTimeListener.onReceive(context, intent);
            }
        }
    }

    public interface HalfSecondUpdateListener {
        void onHalfSecondUpdate(ClockDateTime clockDateTime, boolean z);
    }

    public interface PeriodUpdateListener {
        void onPeriodUpdate(ClockDateTime clockDateTime);
    }

    public interface SecondUpdateListener {
        void onSecondUpdate(ClockDateTime clockDateTime);
    }

    public interface TimeListener {
        void onReceive(Context context, Intent intent);

        void onTimeChanged(ClockDateTime clockDateTime);

        void onTimeTick(ClockDateTime clockDateTime);

        void onTimezoneChanged(ClockDateTime clockDateTime);
    }

    public AppWidgetClock(Context context, TimeListener timeListener) {
        this(context, timeListener, null, null, null);
    }

    public AppWidgetClock(Context context, TimeListener timeListener, SecondUpdateListener secondUpdateListener) {
        this(context, timeListener, secondUpdateListener, null, null);
    }

    public AppWidgetClock(Context context, TimeListener timeListener, HalfSecondUpdateListener halfSecondUpdateListener) {
        this(context, timeListener, null, halfSecondUpdateListener, null);
    }

    public AppWidgetClock(Context context, TimeListener timeListener, PeriodUpdateListener periodUpdateListener) {
        this(context, timeListener, null, null, periodUpdateListener);
    }

    public AppWidgetClock(Context context, TimeListener timeListener, SecondUpdateListener secondUpdateListener, HalfSecondUpdateListener halfSecondUpdateListener, PeriodUpdateListener periodUpdateListener) {
        this.mReceiver = new ClockWidgetReceiver(this, null);
        this.mIsBroadcastRegistered = false;
        this.mUpdateHandler = new Handler() {
            public void handleMessage(Message msg) {
                boolean z = false;
                switch (msg.what) {
                    case 1:
                        AppWidgetClock.this.mClockDateTime.setToNow();
                        AppWidgetClock.this.mSecondUpdateListener.onSecondUpdate(AppWidgetClock.this.mClockDateTime);
                        AppWidgetClock.this.sendSecondUpdateMessage();
                        return;
                    case 2:
                        AppWidgetClock.this.mClockDateTime.setToNow();
                        int millisecond = AppWidgetClock.this.mClockDateTime.getMillisecond();
                        HalfSecondUpdateListener -get1 = AppWidgetClock.this.mHalfSecondUpdateListener;
                        ClockDateTime -get0 = AppWidgetClock.this.mClockDateTime;
                        if (millisecond >= 500 && millisecond < Weather.WEATHERVERSION_ROM_2_0) {
                            z = true;
                        }
                        -get1.onHalfSecondUpdate(-get0, z);
                        AppWidgetClock.this.sendHalfSecondUpdateMessage();
                        return;
                    case 3:
                        AppWidgetClock.this.mClockDateTime.setToNow();
                        AppWidgetClock.this.mPeriodUpdateListener.onPeriodUpdate(AppWidgetClock.this.mClockDateTime);
                        AppWidgetClock.this.sendPeriodUpdateMessage();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mContext = context;
        this.mTimeListener = timeListener;
        this.mSecondUpdateListener = secondUpdateListener;
        this.mHalfSecondUpdateListener = halfSecondUpdateListener;
        this.mPeriodUpdateListener = periodUpdateListener;
        this.mClockDateTime = new ClockDateTime(context);
    }

    public void setTimeListener(TimeListener timeListener) {
        this.mTimeListener = timeListener;
    }

    public void setSecondUpdateListener(SecondUpdateListener secondUpdateListener) {
        this.mSecondUpdateListener = secondUpdateListener;
    }

    public void setHalfSecondUpdateListener(HalfSecondUpdateListener halfSecondUpdateListener) {
        this.mHalfSecondUpdateListener = halfSecondUpdateListener;
    }

    public void setPeriodUpdateListener(PeriodUpdateListener periodUpdateListener) {
        this.mPeriodUpdateListener = periodUpdateListener;
    }

    public ClockDateTime getClockDateTime(boolean update) {
        if (update) {
            this.mClockDateTime.setToNow();
        }
        return this.mClockDateTime;
    }

    public void registerBroadcastReceiver() {
        registerBroadcastReceiver(null);
    }

    public void registerBroadcastReceiver(String[] broadcasts) {
        if (!this.mIsBroadcastRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.TIME_TICK");
            filter.addAction("android.intent.action.TIME_SET");
            filter.addAction("android.intent.action.TIMEZONE_CHANGED");
            if (!(broadcasts == null || broadcasts.length == 0)) {
                for (String broadcast : broadcasts) {
                    filter.addAction(broadcast);
                }
            }
            this.mContext.registerReceiver(this.mReceiver, filter);
            this.mIsBroadcastRegistered = true;
        }
    }

    public void unregisterReceiver() {
        if (this.mIsBroadcastRegistered) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mIsBroadcastRegistered = false;
        }
    }

    public void startSecondUpdate() {
        if (this.mSecondUpdateListener != null) {
            sendSecondUpdateMessage();
        }
    }

    private void sendSecondUpdateMessage() {
        long delay = 1000 - (System.currentTimeMillis() % 1000);
        this.mUpdateHandler.removeMessages(1);
        this.mUpdateHandler.sendEmptyMessageDelayed(1, delay);
    }

    public void stopSecondUpdate() {
        this.mUpdateHandler.removeMessages(1);
    }

    public void startHalfSecondUpdate() {
        if (this.mHalfSecondUpdateListener != null) {
            sendHalfSecondUpdateMessage();
        }
    }

    private void sendHalfSecondUpdateMessage() {
        long delay = 500 - (System.currentTimeMillis() % 500);
        this.mUpdateHandler.removeMessages(2);
        this.mUpdateHandler.sendEmptyMessageDelayed(2, delay);
    }

    public void stopHalfSecondUpdate() {
        this.mUpdateHandler.removeMessages(2);
    }

    public void startPeriodUpdate(int milliseconds) {
        if (milliseconds > 0 && this.mPeriodUpdateListener != null) {
            this.mPeriod = milliseconds;
            sendPeriodUpdateMessage();
        }
    }

    public void sendPeriodUpdateMessage() {
        long delay = ((long) this.mPeriod) - (System.currentTimeMillis() % ((long) this.mPeriod));
        this.mUpdateHandler.removeMessages(3);
        this.mUpdateHandler.sendEmptyMessageDelayed(3, delay);
    }

    public void stopPeriodUpdate() {
        this.mUpdateHandler.removeMessages(3);
    }
}
