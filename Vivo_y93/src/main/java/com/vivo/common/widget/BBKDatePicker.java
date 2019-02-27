package com.vivo.common.widget;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.Configuration;
import android.os.FtBuild;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.Settings.System;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View.BaseSavedState;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import com.vivo.common.widget.ScrollNumberPicker.OnChangedListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class BBKDatePicker extends FrameLayout {
    private static final boolean DEBUG = true;
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String TAG = "BBKDatePicker";
    private String DATE_FORMAT;
    private final int DEFAULT_MAX_YEAR;
    private final int DEFAULT_MIN_YEAR;
    private final int SPINNER_SCROLL_RANGE;
    private final int THAI_CALENDAR_OFFSET;
    private Calendar mCurrentDate;
    private Locale mCurrentLocale;
    private ScrollNumberPicker mDayPicker;
    private int mEndYear;
    private Calendar mMaxDate;
    private Calendar mMinDate;
    private String[] mMonthName;
    private Map<String, String> mMonthNameToNumber;
    private ScrollNumberPicker mMonthPicker;
    private OnDateChangedListener mOnDateChangedListener;
    private int mStartYear;
    private Calendar mTempDate;
    private ScrollNumberPicker mYearPicker;

    public interface OnDateChangedListener {
        void onDateChanged(BBKDatePicker bBKDatePicker, int i, int i2, int i3);
    }

    private enum DateType {
        YEAR,
        MONTH,
        DAY
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private final int mDay;
        private final int mMonth;
        private final int mYear;

        /* synthetic */ SavedState(Parcel in, SavedState -this1) {
            this(in);
        }

        /* synthetic */ SavedState(Parcelable superState, int year, int month, int day, SavedState -this4) {
            this(superState, year, month, day);
        }

        private SavedState(Parcelable superState, int year, int month, int day) {
            super(superState);
            this.mYear = year;
            this.mMonth = month;
            this.mDay = day;
        }

        private SavedState(Parcel in) {
            super(in);
            this.mYear = in.readInt();
            this.mMonth = in.readInt();
            this.mDay = in.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mYear);
            dest.writeInt(this.mMonth);
            dest.writeInt(this.mDay);
        }
    }

    public BBKDatePicker(Context context) {
        this(context, null);
    }

    public BBKDatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BBKDatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.DEFAULT_MIN_YEAR = 1900;
        this.DEFAULT_MAX_YEAR = 2100;
        this.SPINNER_SCROLL_RANGE = 5;
        this.THAI_CALENDAR_OFFSET = 543;
        this.mTempDate = null;
        this.mMinDate = null;
        this.mMaxDate = null;
        this.mCurrentDate = null;
        this.mStartYear = 1900;
        this.mEndYear = 2100;
        this.mMonthName = new String[12];
        this.mMonthNameToNumber = new HashMap();
        this.DATE_FORMAT = getDateFormat(context);
        setCurrentLocale(Locale.getDefault());
        initBBKDatePick(context, attrs, defStyle);
    }

    public long getMinDate() {
        return this.mMinDate.getTimeInMillis();
    }

    public void setMinDate(long minDate) {
        this.mTempDate.setTimeInMillis(minDate);
        if (this.mTempDate.get(1) != this.mMinDate.get(1) || this.mTempDate.get(6) == this.mMinDate.get(6)) {
            this.mMinDate.setTimeInMillis(minDate);
            if (this.mCurrentDate.before(this.mMinDate)) {
                this.mCurrentDate.setTimeInMillis(this.mMinDate.getTimeInMillis());
            }
            updateSpinners();
        }
    }

    public long getMaxDate() {
        return this.mMaxDate.getTimeInMillis();
    }

    public void setMaxDate(long maxDate) {
        this.mTempDate.setTimeInMillis(maxDate);
        if (this.mTempDate.get(1) != this.mMaxDate.get(1) || this.mTempDate.get(6) == this.mMaxDate.get(6)) {
            this.mMaxDate.setTimeInMillis(maxDate);
            if (this.mCurrentDate.after(this.mMaxDate)) {
                this.mCurrentDate.setTimeInMillis(this.mMaxDate.getTimeInMillis());
            }
            updateSpinners();
        }
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return DEBUG;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);
        event.getText().add(DateUtils.formatDateTime(getContext(), this.mCurrentDate.getTimeInMillis(), 20));
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    private void setCurrentLocale(Locale locale) {
        if (!locale.equals(this.mCurrentLocale)) {
            this.mCurrentLocale = locale;
            this.mTempDate = getCalendarForLocale(this.mTempDate, locale);
            this.mMinDate = getCalendarForLocale(this.mMinDate, locale);
            this.mMaxDate = getCalendarForLocale(this.mMaxDate, locale);
            this.mCurrentDate = getCalendarForLocale(this.mCurrentDate, locale);
        }
    }

    private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
        if (oldCalendar == null) {
            return Calendar.getInstance(locale);
        }
        long currentTimeMillis = oldCalendar.getTimeInMillis();
        Calendar calendar = Calendar.getInstance(locale);
        calendar.setTimeInMillis(currentTimeMillis);
        return calendar;
    }

    public void updateDate(int year, int month, int dayOfMonth) {
        if (isNewDate(year, month, dayOfMonth)) {
            setDate(year, month, dayOfMonth);
            updateSpinners();
            updateCalendarView();
            notifyDateChanged();
        }
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), getYear(), getMonth(), getDayOfMonth(), null);
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setDate(ss.mYear, ss.mMonth, ss.mDay);
        updateSpinners();
        updateCalendarView();
    }

    public void init(int year, int monthOfYear, int dayOfMonth, OnDateChangedListener onDateChangedListener) {
        setDate(year, monthOfYear, dayOfMonth);
        updateSpinners();
        updateCalendarView();
        this.mOnDateChangedListener = onDateChangedListener;
    }

    public static boolean isThaiCalendar(Context context) {
        return "1".equals(System.getString(context.getContentResolver(), "use_thai_calendar"));
    }

    public static String getDateFormat(Context context) {
        boolean useFullFormat;
        DateFormat dateFormat;
        String format = null;
        if (FtBuild.getRomVersion() < 3.6f) {
            useFullFormat = FtBuild.isOverSeas() ^ 1;
        } else {
            useFullFormat = DEBUG;
        }
        if (useFullFormat) {
            dateFormat = DateFormat.getDateInstance(0);
        } else {
            dateFormat = android.text.format.DateFormat.getDateFormat(context);
        }
        if (dateFormat instanceof SimpleDateFormat) {
            format = ((SimpleDateFormat) dateFormat).toPattern();
        } else {
            Log.w(TAG, "can't get DateFormat for SimpleDateFormat");
        }
        if (format == null) {
            format = DEFAULT_DATE_FORMAT;
        }
        Log.d(TAG, "DateFormat : " + format);
        return format;
    }

    private boolean isNewDate(int year, int month, int dayOfMonth) {
        if (this.mCurrentDate.get(1) == year && this.mCurrentDate.get(2) == dayOfMonth && this.mCurrentDate.get(5) == month) {
            return false;
        }
        return DEBUG;
    }

    private void setDate(int year, int month, int dayOfMonth) {
        this.mCurrentDate.set(year, month, dayOfMonth);
        if (this.mCurrentDate.before(this.mMinDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMinDate.getTimeInMillis());
        } else if (this.mCurrentDate.after(this.mMaxDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMaxDate.getTimeInMillis());
        }
    }

    private void updateSpinners() {
        this.mDayPicker.setRange(1, this.mCurrentDate.getActualMaximum(5), 5);
        this.mDayPicker.setScrollItemPositionByRange(this.mCurrentDate.get(5));
        this.mMonthPicker.setScrollItemPositionByRange(this.mMonthName[this.mCurrentDate.get(2)]);
        if (isThaiCalendar(getContext())) {
            this.mYearPicker.setScrollItemPositionByRange(this.mCurrentDate.get(1) + 543);
        } else {
            this.mYearPicker.setScrollItemPositionByRange(this.mCurrentDate.get(1));
        }
    }

    public void updateDateAndSpinners(int year, int month, int DayofMonth) {
        setDate(year, month, DayofMonth);
        updateSpinners();
    }

    private void updateCalendarView() {
    }

    public int getYear() {
        return this.mCurrentDate.get(1);
    }

    public int getMonth() {
        return this.mCurrentDate.get(2);
    }

    public int getDayOfMonth() {
        return this.mCurrentDate.get(5);
    }

    private void notifyDateChanged() {
        sendAccessibilityEvent(4);
        if (this.mOnDateChangedListener != null) {
            this.mOnDateChangedListener.onDateChanged(this, getYear(), getMonth(), getDayOfMonth());
        }
    }

    public void updateYearRange(int start, int end) {
        if (start >= 1900 && start < end) {
            this.mStartYear = start;
            this.mEndYear = end;
            if (isThaiCalendar(getContext())) {
                this.mYearPicker.setRange(this.mStartYear + 543, this.mEndYear + 543, 5);
                this.mYearPicker.setScrollItemPositionByRange(this.mCurrentDate.get(1) + 543);
            } else {
                this.mYearPicker.setRange(this.mStartYear, this.mEndYear, 5);
                this.mYearPicker.setScrollItemPositionByRange(this.mCurrentDate.get(1));
            }
        }
    }

    public void setDatePickerTopBackgroundResource(int resid) {
    }

    public void setYearDisableRange(int start, int end) {
    }

    private void updateBBKDate(String oldString, String newString, DateType type) {
        int oldVal = Integer.valueOf(oldString).intValue();
        int newVal = Integer.valueOf(newString).intValue();
        this.mTempDate.setTimeInMillis(this.mCurrentDate.getTimeInMillis());
        if (type == DateType.DAY) {
            this.mTempDate.set(5, newVal);
        } else if (type == DateType.MONTH) {
            if (oldVal == 11 && newVal == 0) {
                this.mTempDate.add(2, 1);
            } else if (oldVal == 0 && newVal == 11) {
                this.mTempDate.add(2, -1);
            } else {
                this.mTempDate.add(2, newVal - oldVal);
            }
        } else if (type == DateType.YEAR) {
            if (isThaiCalendar(getContext())) {
                this.mTempDate.set(1, newVal - 543);
            } else {
                this.mTempDate.set(1, newVal);
            }
        }
        updateDate(this.mTempDate.get(1), this.mTempDate.get(2), this.mTempDate.get(5));
    }

    private void initBBKDatePickView() {
        this.mDayPicker = (ScrollNumberPicker) findViewById(51183678);
        this.mMonthPicker = (ScrollNumberPicker) findViewById(51183677);
        this.mYearPicker = (ScrollNumberPicker) findViewById(51183676);
        String dateFormat = this.DATE_FORMAT.toUpperCase();
        int dayIndex = dateFormat.indexOf(68);
        int monthIndex = dateFormat.indexOf(77);
        int yearIndex = dateFormat.indexOf(89);
        Log.d(TAG, "dayIndex[" + dayIndex + "] monthIndex[" + monthIndex + "] yearIndex[" + yearIndex + "]");
        if (this.mCurrentLocale.getLanguage().equals("ar")) {
            Log.d(TAG, "revert date sequence anim at Arabic");
            dayIndex = (dateFormat.length() - 1) - dayIndex;
            monthIndex = (dateFormat.length() - 1) - monthIndex;
            yearIndex = (dateFormat.length() - 1) - yearIndex;
        }
        LayoutParams lp1;
        LayoutParams lp2;
        int tmp;
        if (dayIndex >= 0 && dayIndex < monthIndex && monthIndex < yearIndex) {
            this.mDayPicker = (ScrollNumberPicker) findViewById(51183676);
            this.mMonthPicker = (ScrollNumberPicker) findViewById(51183677);
            this.mYearPicker = (ScrollNumberPicker) findViewById(51183678);
            lp1 = this.mDayPicker.getLayoutParams();
            lp2 = this.mYearPicker.getLayoutParams();
            tmp = lp1.width;
            lp1.width = lp2.width;
            lp2.width = tmp;
            this.mDayPicker.setLayoutParams(lp1);
            this.mYearPicker.setLayoutParams(lp2);
        } else if (monthIndex >= 0 && monthIndex < dayIndex && dayIndex < yearIndex) {
            this.mDayPicker = (ScrollNumberPicker) findViewById(51183677);
            this.mMonthPicker = (ScrollNumberPicker) findViewById(51183676);
            this.mYearPicker = (ScrollNumberPicker) findViewById(51183678);
            lp1 = this.mMonthPicker.getLayoutParams();
            lp2 = this.mYearPicker.getLayoutParams();
            tmp = lp1.width;
            lp1.width = lp2.width;
            lp2.width = tmp;
            this.mMonthPicker.setLayoutParams(lp1);
            this.mYearPicker.setLayoutParams(lp2);
        }
    }

    protected void initLayoutView(Context context) {
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(50528274, this, DEBUG);
    }

    private void initDateRange() {
        boolean useLocalNumber = false;
        if (FtBuild.getRomVersion() >= 3.6f && (this.mCurrentLocale.getLanguage().equals("zh") ^ 1) != 0) {
            useLocalNumber = DEBUG;
        }
        Calendar calendar = Calendar.getInstance(this.mCurrentLocale);
        calendar.set(5, 1);
        int month = useLocalNumber ? calendar.getActualMinimum(2) : 1;
        for (int i = 0; i < 12; i++) {
            if (useLocalNumber) {
                calendar.set(2, month);
                this.mMonthName[i] = calendar.getDisplayName(2, 1, this.mCurrentLocale);
            } else {
                this.mMonthName[i] = Integer.toString(month);
            }
            if (this.mMonthName[i] == null) {
                this.mMonthName[i] = Integer.toString(month);
                Log.e(TAG, "get locale name for month " + month + " failed");
            }
            this.mMonthNameToNumber.put(this.mMonthName[i], Integer.toString(i));
            month++;
        }
    }

    private void initBBKDatePick(Context context, AttributeSet attrs, int defStyle) {
        initLayoutView(context);
        initBBKDatePickView();
        initDateRange();
        this.mDayPicker.setRange(1, this.mCurrentDate.getActualMaximum(5), 5);
        this.mDayPicker.setOnSelectChangedListener(new OnChangedListener() {
            public void onChanged(String oldVal, String newVal) {
                BBKDatePicker.this.updateBBKDate(oldVal, newVal, DateType.DAY);
            }
        });
        this.mMonthPicker.setRange(this.mMonthName, 5);
        this.mMonthPicker.setOnSelectChangedListener(new OnChangedListener() {
            public void onChanged(String oldVal, String newVal) {
                BBKDatePicker.this.updateBBKDate((String) BBKDatePicker.this.mMonthNameToNumber.get(oldVal), (String) BBKDatePicker.this.mMonthNameToNumber.get(newVal), DateType.MONTH);
            }
        });
        if (isThaiCalendar(context)) {
            this.mYearPicker.setRange(this.mStartYear + 543, this.mEndYear + 543, 5);
        } else {
            this.mYearPicker.setRange(this.mStartYear, this.mEndYear, 5);
        }
        this.mYearPicker.setOnSelectChangedListener(new OnChangedListener() {
            public void onChanged(String oldVal, String newVal) {
                BBKDatePicker.this.updateBBKDate(oldVal, newVal, DateType.YEAR);
            }
        });
        if (FtBuild.getRomVersion() < 3.6f) {
            if (!FtBuild.isOverSeas()) {
                this.mDayPicker.setPickText(context.getString(51249267));
                this.mMonthPicker.setPickText(context.getString(51249266));
                this.mYearPicker.setPickText(context.getString(51249265));
            }
        } else if (this.mCurrentLocale.getLanguage().equals("zh")) {
            this.mDayPicker.setPickText(context.getString(51249267));
            this.mMonthPicker.setPickText(context.getString(51249266));
            this.mYearPicker.setPickText(context.getString(51249265));
        }
        this.mTempDate.clear();
        this.mTempDate.set(this.mStartYear, 0, 1);
        setMinDate(this.mTempDate.getTimeInMillis());
        this.mTempDate.clear();
        this.mTempDate.set(this.mEndYear, 11, 31);
        setMaxDate(this.mTempDate.getTimeInMillis());
        this.mCurrentDate.setTimeInMillis(System.currentTimeMillis());
        init(this.mCurrentDate.get(1), this.mCurrentDate.get(2), this.mCurrentDate.get(5), null);
    }

    public ScrollNumberPicker getDayPicker() {
        return this.mDayPicker;
    }

    public ScrollNumberPicker getMonthPicker() {
        return this.mMonthPicker;
    }

    public ScrollNumberPicker getYearPicker() {
        return this.mYearPicker;
    }

    public void setSelectedItemTextColor(int selectedItemColor) {
        this.mDayPicker.setSelectedItemTextColor(selectedItemColor);
        this.mMonthPicker.setSelectedItemTextColor(selectedItemColor);
        this.mYearPicker.setSelectedItemTextColor(selectedItemColor);
    }
}
