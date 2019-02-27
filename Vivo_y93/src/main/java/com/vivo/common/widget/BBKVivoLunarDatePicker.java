package com.vivo.common.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View.BaseSavedState;
import android.widget.FrameLayout;
import com.vivo.common.widget.Lunar.LunarDate;
import com.vivo.common.widget.LunarScrollNumberPicker.OnChangedListener;
import java.util.Arrays;
import java.util.Locale;

public class BBKVivoLunarDatePicker extends FrameLayout {
    private static final boolean DEFAULT_ENABLED_STATE = true;
    public static final int MAX_YEAR = 2050;
    public static final int MIN_YEAR = 1901;
    private int OldDay;
    private int OldMonth;
    private int OldYear;
    private final String TAG;
    public String[] iLunarMonth;
    private LunarScrollNumberPicker mBBKDayPicker;
    private LunarScrollNumberPicker mBBKMonthPicker;
    private LunarScrollNumberPicker mBBKYearPicker;
    private int mCurrentDay;
    private int mCurrentMonth;
    private int mCurrentYear;
    private int mEndYear;
    private boolean mIsEnabled;
    private String mLeapMonth;
    private Lunar mLunar;
    private String[] mLunarMonth;
    private String mMonth;
    private OnDateChangedListener mOnDateChangedListener;
    private Resources mRes;
    private int mStartYear;
    private String mYear;

    public interface OnDateChangedListener {
        void onDateChanged(BBKVivoLunarDatePicker bBKVivoLunarDatePicker, String str, int i);
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

    public BBKVivoLunarDatePicker(Context context) {
        this(context, null);
    }

    public BBKVivoLunarDatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BBKVivoLunarDatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.OldYear = 0;
        this.OldMonth = 0;
        this.OldDay = 0;
        this.mStartYear = 1901;
        this.mEndYear = 2050;
        this.TAG = "BBKVivoLunarDatePicker";
        this.mIsEnabled = DEFAULT_ENABLED_STATE;
        this.mCurrentYear = 1901;
        this.mCurrentMonth = 1;
        this.mCurrentDay = 1;
        this.mLunar = null;
        this.mRes = null;
        this.mYear = null;
        this.mMonth = null;
        this.mLeapMonth = null;
        this.mLunarMonth = null;
        this.iLunarMonth = null;
        this.mRes = context.getResources();
        initResourcesForChina(context);
        initBBKDatePick(context, attrs, defStyle);
    }

    private void initResourcesForChina(Context context) {
        Configuration config = this.mRes.getConfiguration();
        Locale originLocale = config.locale;
        config.locale = Locale.CHINA;
        this.mRes.updateConfiguration(config, this.mRes.getDisplayMetrics());
        this.mYear = this.mRes.getString(51249265);
        this.mMonth = this.mRes.getString(51249266);
        this.mLeapMonth = this.mRes.getString(51249415);
        this.mLunarMonth = this.mRes.getStringArray(50921510);
        config.locale = originLocale;
        this.mRes.updateConfiguration(config, this.mRes.getDisplayMetrics());
    }

    public void setEnabled(boolean enabled) {
        if (this.mIsEnabled != enabled) {
            super.setEnabled(enabled);
            this.mIsEnabled = enabled;
        }
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public void updateDate(LunarDate lunarDate) {
        if (isNewDate(lunarDate.year, lunarDate.monthOfYear, lunarDate.dayOfMonth)) {
            setDate(lunarDate.year, lunarDate.monthOfYear, lunarDate.dayOfMonth);
            updateSpinners();
            notifyDateChanged();
            return;
        }
        notifyDateChanged();
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), this.mCurrentYear, this.mCurrentMonth, this.mCurrentDay, null);
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setDate(ss.mYear, ss.mMonth, ss.mDay);
        updateSpinners();
    }

    public void init(LunarDate lunarDate, OnDateChangedListener onDateChangedListener, Lunar lunar) {
        this.mLunar = lunar;
        this.mBBKYearPicker.setRange(this.mLunar.getChineseLunarYear(this.mStartYear, this.mEndYear), 5, (this.mEndYear - this.mStartYear) + 1);
        setDate(lunarDate.year, lunarDate.monthOfYear, lunarDate.dayOfMonth);
        updateSpinners();
        this.mOnDateChangedListener = onDateChangedListener;
    }

    private boolean isNewDate(int year, int month, int dayOfMonth) {
        if (this.mCurrentYear == year && this.mCurrentMonth == dayOfMonth && this.mCurrentDay == month) {
            return false;
        }
        return DEFAULT_ENABLED_STATE;
    }

    private void setDate(int year, int month, int dayOfMonth) {
        int i;
        String mCurrntMonthName = null;
        if (this.iLunarMonth != null) {
            mCurrntMonthName = this.iLunarMonth[this.mCurrentMonth - 1];
        }
        this.iLunarMonth = null;
        this.iLunarMonth = (String[]) Arrays.copyOf(this.mLunarMonth, this.mLunarMonth.length);
        int LeapMonth = Lunar.GetLunarLeapMonth(year);
        if (month == 0) {
            month = LeapMonth + 1;
        }
        this.mCurrentYear = year;
        this.mCurrentMonth = month;
        this.mCurrentDay = dayOfMonth;
        if (this.mCurrentYear > 2037) {
            this.mCurrentYear = 2050;
            this.mCurrentMonth = 12;
            this.mCurrentDay = 30;
        } else if (this.mCurrentYear < 1901) {
            this.mCurrentYear = 1901;
            this.mCurrentMonth = 1;
            this.mCurrentDay = 1;
        }
        if (LeapMonth != 0) {
            for (i = 12; i >= LeapMonth; i--) {
                if (i > LeapMonth) {
                    this.iLunarMonth[i] = this.iLunarMonth[i - 1];
                } else {
                    this.iLunarMonth[i] = this.mLeapMonth + this.iLunarMonth[LeapMonth - 1];
                }
            }
            this.mBBKMonthPicker.setRange(this.iLunarMonth, 5, 13);
        } else {
            this.mBBKMonthPicker.setRange(this.iLunarMonth, 5, 12);
        }
        if (this.OldYear > 1900 && year != this.OldYear) {
            int OldLeapMonth = Lunar.GetLunarLeapMonth(this.OldYear);
            if (OldLeapMonth == 0 && LeapMonth != 0) {
                if (this.OldMonth == month && month > LeapMonth) {
                    month++;
                }
                if (month > 13) {
                    month = 13;
                }
            } else if (OldLeapMonth != 0 && LeapMonth == 0) {
                if (this.OldMonth == month && month > OldLeapMonth) {
                    month--;
                }
                if (month < 0) {
                    month = 1;
                }
            }
        }
        if (LeapMonth == 0 && month > 12) {
            month = 12;
        }
        int monthdays = Lunar.GetLunarMonthDays(year, month);
        if (this.mCurrentDay > monthdays) {
            this.mCurrentDay--;
        }
        this.mBBKDayPicker.setRange(this.mLunar.iLunarDay, 5, monthdays);
        i = 0;
        while (i < this.iLunarMonth.length) {
            if (mCurrntMonthName == null || !mCurrntMonthName.equals(this.iLunarMonth[i])) {
                i++;
            } else {
                this.mCurrentMonth = i + 1;
                return;
            }
        }
    }

    public void updateDateAndSpinners(int LunarYear, int LunarMonth, int LunarDay, int LunarLeapMonth, boolean AddMonth) {
        if (isNewDate(LunarYear, LunarMonth, LunarDay)) {
            if (AddMonth) {
                LunarMonth++;
            }
            if (LunarMonth == 0) {
                LunarMonth = LunarLeapMonth + 1;
            }
            int LeapMonth = Lunar.GetLunarLeapMonth(LunarYear);
            this.mCurrentYear = LunarYear;
            this.mCurrentMonth = LunarMonth;
            this.mCurrentDay = LunarDay;
            this.iLunarMonth = null;
            this.iLunarMonth = (String[]) Arrays.copyOf(this.mLunarMonth, this.mLunarMonth.length);
            if (this.mCurrentYear > 2037) {
                this.mCurrentYear = 2050;
                this.mCurrentMonth = 12;
                this.mCurrentDay = 30;
            } else if (this.mCurrentYear < 1901) {
                this.mCurrentYear = 1901;
                this.mCurrentMonth = 1;
                this.mCurrentDay = 1;
            }
            if (LeapMonth != 0) {
                for (int i = 12; i >= LeapMonth; i--) {
                    if (i > LeapMonth) {
                        this.iLunarMonth[i] = this.iLunarMonth[i - 1];
                    } else {
                        this.iLunarMonth[i] = this.mLeapMonth + this.iLunarMonth[LeapMonth - 1];
                    }
                }
                this.mBBKMonthPicker.setRange(this.iLunarMonth, 5, 13);
            } else {
                this.mBBKMonthPicker.setRange(this.iLunarMonth, 5, 12);
            }
            int monthdays = Lunar.GetLunarMonthDays(LunarYear, LunarMonth);
            if (this.mCurrentDay > monthdays) {
                this.mCurrentDay--;
            }
            this.mBBKDayPicker.setRange(this.mLunar.iLunarDay, 5, monthdays);
            this.OldYear = this.mCurrentYear;
            this.OldMonth = this.mCurrentMonth;
            this.OldDay = this.mCurrentDay;
            updateSpinners();
        }
    }

    private void updateSpinners() {
        int LeapMonth = Lunar.GetLunarLeapMonth(this.mCurrentYear);
        this.mBBKDayPicker.setScrollItemPositionByRange(this.mLunar.iLunarDay[this.mCurrentDay - 1]);
        this.mBBKYearPicker.setScrollItemPositionByRange(this.mLunar.getChineseLunarYear(this.mCurrentYear));
        this.mBBKMonthPicker.setScrollItemPositionByRange(this.iLunarMonth[this.mCurrentMonth - 1]);
    }

    private void notifyDateChanged() {
        if (this.mOnDateChangedListener != null) {
            this.mOnDateChangedListener.onDateChanged(this, getLunarDate().mDate, getsolarDate().weekDay);
        }
    }

    public void updateYearRange(int start, int end) {
        if (start >= 1901 && end <= 2050 && start < end) {
            this.mStartYear = start;
            this.mEndYear = end;
            this.mBBKYearPicker.setRange(this.mLunar.getChineseLunarYear(start, end), 5, (end - start) + 1);
            this.mBBKYearPicker.setScrollItemPositionByRange(this.mLunar.getChineseLunarYear(this.mCurrentYear));
        }
    }

    public void setYearDisableRange(int start, int end) {
        this.mBBKYearPicker.setDisableRang(start, end);
    }

    private void updateBBKDate(int id, int cur) {
        if (cur == 0) {
            this.mCurrentDay = id;
        } else if (cur == 1) {
            this.mCurrentMonth = id;
        } else if (cur == 2) {
            this.mCurrentYear = this.mStartYear + id;
        }
        setDate(this.mCurrentYear, this.mCurrentMonth, this.mCurrentDay);
        this.OldYear = this.mCurrentYear;
        this.OldMonth = this.mCurrentMonth;
        this.OldDay = this.mCurrentDay;
        updateSpinners();
        notifyDateChanged();
    }

    private void initBBKDatePick(Context context, AttributeSet attrs, int defStyle) {
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(50528292, this, DEFAULT_ENABLED_STATE);
        float mDensity = getContext().getResources().getDisplayMetrics().density;
        this.mBBKDayPicker = (LunarScrollNumberPicker) findViewById(51183678);
        this.mBBKDayPicker.setOnSelectChangedListener(new OnChangedListener() {
            public void onChanged(int id) {
                BBKVivoLunarDatePicker.this.updateBBKDate(id + 1, 0);
            }
        });
        this.mBBKMonthPicker = (LunarScrollNumberPicker) findViewById(51183677);
        this.mBBKMonthPicker.setOnSelectChangedListener(new OnChangedListener() {
            public void onChanged(int id) {
                BBKVivoLunarDatePicker.this.updateBBKDate(id + 1, 1);
            }
        });
        this.mBBKYearPicker = (LunarScrollNumberPicker) findViewById(51183676);
        this.mBBKYearPicker.setOnSelectChangedListener(new OnChangedListener() {
            public void onChanged(int id) {
                BBKVivoLunarDatePicker.this.updateBBKDate(id, 2);
            }
        });
    }

    public LunarDate getLunarDate() {
        return this.mLunar.getLunarDate(this.mCurrentYear, this.mCurrentMonth, this.mCurrentDay, Lunar.GetLunarLeapMonth(this.mCurrentYear), this.mLunar.getChineseLunarYear(this.mCurrentYear) + this.mYear + this.iLunarMonth[this.mCurrentMonth - 1] + this.mMonth + this.mLunar.iLunarDay[this.mCurrentDay - 1]);
    }

    public Time getsolarDate() {
        return Lunar.CalendarLundarToSolar(this.mCurrentYear, this.mCurrentMonth, this.mCurrentDay);
    }

    public LunarScrollNumberPicker getDayPicker() {
        return this.mBBKDayPicker;
    }

    public LunarScrollNumberPicker getMonthPicker() {
        return this.mBBKMonthPicker;
    }

    public LunarScrollNumberPicker getYearPicker() {
        return this.mBBKYearPicker;
    }

    public void setSelectedItemTextColor(int selectedItemColor) {
        this.mBBKDayPicker.setSelectedItemTextColor(selectedItemColor);
        this.mBBKMonthPicker.setSelectedItemTextColor(selectedItemColor);
        this.mBBKYearPicker.setSelectedItemTextColor(selectedItemColor);
    }
}
