package com.vivo.common.utils;

import android.content.Context;
import android.provider.Settings.System;
import android.text.format.DateUtils;
import android.text.format.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtils {
    public static String getDate(long date) {
        DateFormat dateFormat;
        if (isSameYear(date)) {
            dateFormat = new SimpleDateFormat("MM-dd");
        } else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }
        return dateFormat.format(new Date(date));
    }

    public static String getDateTime(Context context, long datetime) {
        boolean is24Hour = System.getString(context.getContentResolver(), "time_12_24").equals("24");
        if (!DateUtils.isToday(datetime)) {
            return getDate(datetime);
        }
        int flags;
        if (is24Hour) {
            flags = 129;
        } else {
            flags = 65;
        }
        return DateUtils.formatDateTime(context, datetime, flags);
    }

    public static String getSimpleDate(long date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(date));
    }

    public static String getSimpleTime(long time) {
        return new SimpleDateFormat("HH:mm:ss").format(new Date(time));
    }

    private static boolean isYesterday(long datetime) {
        boolean z = true;
        Time time = new Time();
        time.set(datetime);
        int year = time.year;
        int month = time.month;
        int monthDay = time.monthDay;
        int yearDay = time.yearDay;
        time.set(System.currentTimeMillis());
        if (year != time.year) {
            if (time.month != 0 || time.monthDay != 1 || month != 11) {
                z = false;
            } else if (monthDay != 31) {
                z = false;
            }
            return z;
        }
        if (yearDay != time.yearDay - 1) {
            z = false;
        }
        return z;
    }

    private static boolean isSameYear(long datetime) {
        Time time = new Time();
        time.set(datetime);
        int year = time.year;
        time.set(System.currentTimeMillis());
        return time.year == year;
    }
}
