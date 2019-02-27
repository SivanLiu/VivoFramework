package com.vivo.app;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import com.vivo.common.TabSelector;
import com.vivo.common.widget.BBKDatePicker;
import com.vivo.common.widget.BBKVivoGeliDatePicker;
import com.vivo.common.widget.BBKVivoGeliDatePicker.OnDateChangedListener;
import com.vivo.common.widget.BBKVivoLunarDatePicker;
import com.vivo.common.widget.Lunar;
import com.vivo.common.widget.Lunar.LunarDate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class DatePickerDialog extends AlertDialog implements OnClickListener {
    private static final String DAY = "day";
    private static final boolean DEBUG = true;
    private static final String LUNAR = "lunar";
    public static final int MAX_YEAR = 2050;
    public static final int MIN_YEAR = 1901;
    private static final String MONTH = "month";
    private static final String TAG = "DatePickerDialog";
    private static final String YEAR = "year";
    private Calendar mCalendar = null;
    private DateSetCallBack mCallBack = null;
    private Context mContext = null;
    private OnDateChangedListener mDateChangedGeli = new OnDateChangedListener() {
        public void onDateChanged(BBKVivoGeliDatePicker view, int year, int month, int day) {
            DatePickerDialog.this.updateDate(year, month, day);
        }
    };
    private BBKVivoLunarDatePicker.OnDateChangedListener mDateChangedLunar = new BBKVivoLunarDatePicker.OnDateChangedListener() {
        public void onDateChanged(BBKVivoLunarDatePicker view, String LunarDate, int WeekDay) {
            DatePickerDialog.this.updateDate(view.getLunarDate());
        }
    };
    private FrameLayout mExtendContent = null;
    private boolean mIsLunar = false;
    private Lunar mLunar = null;
    private LunarDate mLunarDate = null;
    private BBKVivoLunarDatePicker mLunarDatePicker = null;
    private Time mMaxLunarDate = null;
    private int mMaxYear = 2050;
    private Time mMinLunarDate = null;
    private int mMinYear = 1901;
    private Resources mRes = null;
    private boolean mShowLunar = false;
    private BBKVivoGeliDatePicker mSolarDatePicker = null;
    private int mSolarDay;
    private int mSolarMonth;
    private int mSolarYear;
    private TabSelector mTabSelector = null;
    private DateFormat mTitleDateFormat = null;

    public interface DateSetCallBack {
        void onDateSet(int i, int i2, int i3, boolean z);
    }

    public DatePickerDialog(Context context, DateSetCallBack callBack, int year, int month, int day) {
        super(context);
        this.mContext = context;
        this.mCallBack = callBack;
        this.mLunar = new Lunar(context);
        this.mRes = context.getResources();
        this.mTitleDateFormat = new SimpleDateFormat(BBKDatePicker.getDateFormat(context));
        this.mCalendar = Calendar.getInstance();
        setLunarDateRange();
        updateDate(year, month, day);
        initLayout();
        updateYearRange(1901, 2050);
        showLunar(false);
    }

    private void setLunarDateRange() {
        Lunar lunar = this.mLunar;
        int maxMonth = Lunar.GetLunarLeapMonth(this.mMaxYear) <= 0 ? false : DEBUG ? 13 : 12;
        lunar = this.mLunar;
        this.mMinLunarDate = Lunar.CalendarLundarToSolar(this.mMinYear, 1, 1);
        lunar = this.mLunar;
        this.mMaxLunarDate = Lunar.CalendarLundarToSolar(this.mMaxYear, maxMonth, Lunar.GetLunarMonthDays(this.mMaxYear, maxMonth));
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mCallBack != null) {
            if (this.mIsLunar) {
                Lunar lunar = this.mLunar;
                Time t = Lunar.CalendarLundarToSolar(this.mLunarDate.year, getLunarMonth(this.mLunarDate), this.mLunarDate.dayOfMonth);
                this.mCallBack.onDateSet(t.year, t.month, t.monthDay, this.mIsLunar);
            } else {
                this.mCallBack.onDateSet(this.mSolarYear, this.mSolarMonth, this.mSolarDay, this.mIsLunar);
            }
        }
    }

    private void setSolarDate(int year, int month, int day) {
        if (year > this.mMaxYear || year < this.mMinYear) {
            Log.w(TAG, "setSolarDate overflow : Year=" + year + " Month=" + month + " Day=" + day + " [" + this.mMinYear + " " + this.mMaxYear + "]", new Throwable());
        }
        if (year > this.mMaxYear) {
            this.mSolarYear = this.mMaxYear;
            this.mSolarMonth = 11;
            this.mSolarDay = 31;
        } else if (year < this.mMinYear) {
            this.mSolarYear = this.mMinYear;
            this.mSolarMonth = 0;
            this.mSolarDay = 1;
        } else {
            this.mSolarYear = year;
            this.mSolarMonth = month;
            this.mSolarDay = day;
        }
    }

    private void setLunarDate(LunarDate lunarDate) {
        if (lunarDate.year > this.mMaxYear || lunarDate.year < this.mMinYear) {
            Log.w(TAG, "setLunarDate overflow : Year=" + lunarDate.year + " Month=" + lunarDate.monthOfYear + " Day=" + lunarDate.dayOfMonth + " [" + this.mMinYear + " " + this.mMaxYear + "]", new Throwable());
        }
        if (lunarDate.year > this.mMaxYear) {
            this.mLunarDate = this.mLunar.CalendarSolarToLundar(this.mMaxLunarDate.year, this.mMaxLunarDate.month, this.mMaxLunarDate.monthDay);
        } else if (lunarDate.year < this.mMinYear) {
            this.mLunarDate = this.mLunar.CalendarSolarToLundar(this.mMinLunarDate.year, this.mMinLunarDate.month, this.mMinLunarDate.monthDay);
        } else {
            this.mLunarDate = lunarDate;
        }
    }

    private int getLunarMonth(LunarDate lunarDate) {
        return this.mLunarDate.monthOfYear <= 0 ? this.mLunarDate.LeapMonth : this.mLunarDate.monthOfYear;
    }

    private void updateDate(LunarDate lunarDate) {
        Lunar lunar = this.mLunar;
        Time t = Lunar.CalendarLundarToSolar(lunarDate.year, getLunarMonth(lunarDate), lunarDate.dayOfMonth);
        setSolarDate(t.year, t.month, t.monthDay);
        setLunarDate(lunarDate);
        updateTitle(DEBUG);
    }

    private void updateDate(int year, int month, int day) {
        this.mLunarDate = this.mLunar.CalendarSolarToLundar(year, month, day);
        if (this.mLunarDate == null) {
            Log.w(TAG, "CalendarSolarToLunar overflow : Year=" + year + " Month=" + month + " Day=" + day, new Throwable());
            if (year <= this.mMinYear) {
                this.mLunarDate = this.mLunar.CalendarSolarToLundar(this.mMinLunarDate.year, this.mMinLunarDate.month, this.mMinLunarDate.monthDay);
            } else {
                this.mLunarDate = this.mLunar.CalendarSolarToLundar(this.mMaxLunarDate.year, this.mMaxLunarDate.month, this.mMaxLunarDate.monthDay);
            }
        } else {
            setLunarDate(this.mLunarDate);
        }
        setSolarDate(year, month, day);
        updateTitle(false);
    }

    private void updateTitle(boolean lunar) {
        String title;
        if (lunar) {
            title = this.mLunarDate.mDate;
        } else {
            this.mCalendar.set(1, this.mSolarYear);
            this.mCalendar.set(2, this.mSolarMonth);
            this.mCalendar.set(5, this.mSolarDay);
            title = this.mTitleDateFormat.format(this.mCalendar.getTime());
            if (BBKDatePicker.isThaiCalendar(getContext())) {
                title = title.replace(String.valueOf(this.mCalendar.get(1)), String.valueOf(this.mCalendar.get(1) + 543));
            }
        }
        setTitle(title);
    }

    private void initLayout() {
        setButton(-1, this.mContext.getText(51249411), this);
        setButton(-2, this.mContext.getText(17039360), (OnClickListener) null);
        setIcon(0);
        View view = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(50528293, null);
        setView(view);
        this.mExtendContent = (FrameLayout) view.findViewById(51183711);
        this.mLunarDatePicker = (BBKVivoLunarDatePicker) view.findViewById(51183679);
        this.mLunarDatePicker.init(this.mLunarDate, this.mDateChangedLunar, this.mLunar);
        this.mSolarDatePicker = (BBKVivoGeliDatePicker) view.findViewById(51183710);
        this.mSolarDatePicker.init(this.mSolarYear, this.mSolarMonth, this.mSolarDay, this.mDateChangedGeli);
        this.mTabSelector = (TabSelector) view.findViewById(51183708);
        this.mTabSelector.setTabString(0, this.mRes.getString(51249417));
        this.mTabSelector.setTabString(2, this.mRes.getString(51249416));
        this.mTabSelector.setTabOnClickListener(0, new View.OnClickListener() {
            public void onClick(View v) {
                DatePickerDialog.this.selectLunar(false);
            }
        });
        this.mTabSelector.setTabOnClickListener(2, new View.OnClickListener() {
            public void onClick(View v) {
                DatePickerDialog.this.selectLunar(DatePickerDialog.DEBUG);
            }
        });
    }

    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, this.mSolarYear);
        state.putInt(MONTH, this.mSolarMonth);
        state.putInt(DAY, this.mSolarDay);
        state.putInt(LUNAR, this.mIsLunar ? 1 : 0);
        return state;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        boolean z = DEBUG;
        super.onRestoreInstanceState(savedInstanceState);
        this.mSolarYear = savedInstanceState.getInt(YEAR);
        this.mSolarMonth = savedInstanceState.getInt(MONTH);
        this.mSolarDay = savedInstanceState.getInt(DAY);
        if (savedInstanceState.getInt(LUNAR) != 1) {
            z = false;
        }
        this.mIsLunar = z;
        updateDate(this.mSolarYear, this.mSolarMonth, this.mSolarDay);
        if (this.mIsLunar) {
            this.mLunarDatePicker.init(this.mLunarDate, this.mDateChangedLunar, this.mLunar);
        } else {
            this.mSolarDatePicker.init(this.mSolarYear, this.mSolarMonth, this.mSolarDay, this.mDateChangedGeli);
        }
    }

    public void updateYearRange(int start, int end) {
        if (start < 1901 || end > 2050 || start >= end - 1) {
            Log.w(TAG, "updateYearRange invalidate Range : [" + start + " " + end + "]");
            return;
        }
        this.mMinYear = start;
        this.mMaxYear = end;
        setLunarDateRange();
        setSolarDate(this.mSolarYear, this.mSolarMonth, this.mSolarDay);
        setLunarDate(this.mLunarDate);
        this.mSolarDatePicker.updateYearRange(start, end);
        this.mLunarDatePicker.updateYearRange(start, end);
        if (this.mIsLunar) {
            this.mLunarDatePicker.updateDateAndSpinners(this.mLunarDate.year, this.mLunarDate.monthOfYear, this.mLunarDate.dayOfMonth, this.mLunarDate.LeapMonth, AddorNotMonthValue(this.mLunarDate));
        } else {
            this.mSolarDatePicker.updateDateAndSpinners(this.mSolarYear, this.mSolarMonth, this.mSolarDay);
        }
    }

    public void setBottomContentView(View view) {
        if (this.mExtendContent != null) {
            this.mExtendContent.addView(view);
        }
    }

    public void showLunar(boolean value) {
        this.mShowLunar = value;
        if (this.mShowLunar) {
            this.mTabSelector.setVisibility(0);
            return;
        }
        this.mTabSelector.setVisibility(8);
        this.mSolarDatePicker.setVisibility(0);
        this.mLunarDatePicker.setVisibility(8);
    }

    public void selectLunar(boolean value) {
        if (!this.mShowLunar || this.mIsLunar == value) {
            Log.w(TAG, "selectLunar ignore because lunar is disabled or no changes occured");
            return;
        }
        this.mIsLunar = value;
        if (this.mIsLunar) {
            this.mTabSelector.setSelectorTab(2);
            this.mSolarDatePicker.setVisibility(8);
            this.mLunarDatePicker.setVisibility(0);
            this.mLunarDatePicker.updateDateAndSpinners(this.mLunarDate.year, this.mLunarDate.monthOfYear, this.mLunarDate.dayOfMonth, this.mLunarDate.LeapMonth, AddorNotMonthValue(this.mLunarDate));
            updateTitle(DEBUG);
        } else {
            this.mTabSelector.setSelectorTab(0);
            this.mSolarDatePicker.setVisibility(0);
            this.mLunarDatePicker.setVisibility(4);
            this.mSolarDatePicker.updateDateAndSpinners(this.mSolarYear, this.mSolarMonth, this.mSolarDay);
            updateTitle(false);
        }
    }

    private boolean AddorNotMonthValue(LunarDate lunarDate) {
        if (lunarDate.LeapMonth != 0) {
            Lunar lunar = this.mLunar;
            Time solarTime = Lunar.CalendarLundarToSolar(lunarDate.year, lunarDate.LeapMonth + 2, 1);
            Time LastDayTime = new Time();
            LastDayTime.set(31, 11, lunarDate.year);
            Time NowTime = new Time();
            NowTime.set(this.mSolarDay, this.mSolarMonth, this.mSolarYear);
            long NowDateTime = NowTime.normalize(false);
            return (NowDateTime > LastDayTime.normalize(false) || NowDateTime < solarTime.normalize(false)) ? false : DEBUG;
        }
    }
}
