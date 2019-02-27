package com.vivo.common.widget;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.Configuration;
import android.os.FtBuild;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View.BaseSavedState;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.common.widget.ScrollNumberPicker.OnChangedListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class BBKTimePicker extends FrameLayout {
    private static final boolean DEFAULT_ENABLED_STATE = true;
    private static final OnTimeChangedListener NO_OP_CHANGE_LISTENER = new OnTimeChangedListener() {
        public void onTimeChanged(BBKTimePicker view, int hourOfDay, int minute) {
        }
    };
    private boolean isExport;
    private int layoutId;
    private ScrollNumberPicker mAmPmPicker;
    private String[] mAmPmStrings;
    private int mCurrentHour;
    private Locale mCurrentLocale;
    private int mCurrentMinute;
    private ScrollNumberPicker mHourPicker;
    private boolean mIs24HourView;
    private boolean mIsAm;
    private boolean mIsEnabled;
    private ScrollNumberPicker mMinutePicker;
    private OnTimeChangedListener mOnTimeChangedListener;
    private Calendar mTempCalendar;

    public interface OnTimeChangedListener {
        void onTimeChanged(BBKTimePicker bBKTimePicker, int i, int i2);
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
        private final int mHour;
        private final int mMinute;

        /* synthetic */ SavedState(Parcel in, SavedState -this1) {
            this(in);
        }

        /* synthetic */ SavedState(Parcelable superState, int hour, int minute, SavedState -this3) {
            this(superState, hour, minute);
        }

        private SavedState(Parcelable superState, int hour, int minute) {
            super(superState);
            this.mHour = hour;
            this.mMinute = minute;
        }

        private SavedState(Parcel in) {
            super(in);
            this.mHour = in.readInt();
            this.mMinute = in.readInt();
        }

        public int getHour() {
            return this.mHour;
        }

        public int getMinute() {
            return this.mMinute;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mHour);
            dest.writeInt(this.mMinute);
        }
    }

    public BBKTimePicker(Context context) {
        this(context, null);
    }

    public BBKTimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BBKTimePicker(Context context, AttributeSet attrs, int defAttr) {
        super(context, attrs, defAttr);
        this.mIs24HourView = false;
        this.mCurrentHour = 0;
        this.mCurrentMinute = 0;
        this.isExport = FtBuild.isOverSeas();
        this.mIsEnabled = DEFAULT_ENABLED_STATE;
        this.layoutId = 0;
        setCurrentLocale(Locale.getDefault());
        initBBKTimePick(context, attrs, defAttr);
        setEnabled(isEnabled());
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mIsEnabled = enabled;
        this.mMinutePicker.setEnabled(enabled);
        this.mHourPicker.setEnabled(enabled);
        this.mAmPmPicker.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    private void setCurrentLocale(Locale locale) {
        if (!locale.equals(this.mCurrentLocale)) {
            this.mCurrentLocale = locale;
            this.mTempCalendar = Calendar.getInstance(locale);
        }
    }

    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        this.mOnTimeChangedListener = onTimeChangedListener;
    }

    public Integer getCurrentHour() {
        return Integer.valueOf(this.mCurrentHour);
    }

    public void setCurrentHour(Integer currentHour) {
        this.mCurrentHour = currentHour.intValue();
        updateHourControl();
        updateAmPmControl();
        onTimeChanged();
    }

    public void setIs24HourView(Boolean is24HourView) {
        if (this.mIs24HourView != is24HourView.booleanValue()) {
            this.mIs24HourView = is24HourView.booleanValue();
            configurePickerRanges();
        }
    }

    public boolean is24HourView() {
        return this.mIs24HourView;
    }

    public Integer getCurrentMinute() {
        return Integer.valueOf(this.mCurrentMinute);
    }

    public void setCurrentMinute(Integer currentMinute) {
        this.mCurrentMinute = currentMinute.intValue();
        this.mMinutePicker.setScrollItemPositionByRange(this.mCurrentMinute);
        onTimeChanged();
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return DEFAULT_ENABLED_STATE;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        int flags;
        super.onPopulateAccessibilityEvent(event);
        if (this.mIs24HourView) {
            flags = 129;
        } else {
            flags = 65;
        }
        this.mTempCalendar.set(11, getCurrentHour().intValue());
        this.mTempCalendar.set(12, getCurrentMinute().intValue());
        event.getText().add(DateUtils.formatDateTime(getContext(), this.mTempCalendar.getTimeInMillis(), flags));
    }

    private void updateHourControl() {
        int currentHour = this.mCurrentHour;
        if (!this.mIs24HourView) {
            if (currentHour > 12) {
                currentHour -= 12;
            } else if (currentHour == 0) {
                currentHour = 12;
            }
        }
        this.mHourPicker.setScrollItemPositionByRange(currentHour);
    }

    private void updateAmPmControl() {
        this.mIsAm = this.mCurrentHour < 12 ? DEFAULT_ENABLED_STATE : false;
        if (this.mIsAm) {
            this.mAmPmPicker.setScrollItemPositionByRange(this.mAmPmStrings[0]);
        } else {
            this.mAmPmPicker.setScrollItemPositionByRange(this.mAmPmStrings[1]);
        }
    }

    private void onTimeChanged() {
        sendAccessibilityEvent(4);
        if (this.mOnTimeChangedListener != null) {
            this.mOnTimeChangedListener.onTimeChanged(this, getCurrentHour().intValue(), getCurrentMinute().intValue());
        }
    }

    public void setTimePickerTopBackgroundResource(int resid) {
    }

    private boolean hidePickerText() {
        return (this.isExport || FtBuild.getRomVersion() >= 3.0f) ? DEFAULT_ENABLED_STATE : false;
    }

    private void initBBKTimePick(Context context, AttributeSet attrs, int defAttr) {
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(50528371, this, DEFAULT_ENABLED_STATE);
        this.mHourPicker = (ScrollNumberPicker) findViewById(51183788);
        this.mMinutePicker = (ScrollNumberPicker) findViewById(51183789);
        this.mAmPmPicker = (ScrollNumberPicker) findViewById(51183790);
        if (hidePickerText()) {
            this.mHourPicker.setPickText(Events.DEFAULT_SORT_ORDER);
        } else {
            this.mHourPicker.setPickText(context.getString(51249268));
        }
        this.mHourPicker.setOnSelectChangedListener(new OnChangedListener() {
            public void onChanged(String oldVal, String newVal) {
                BBKTimePicker.this.mCurrentHour = Integer.valueOf(newVal).intValue();
                if (!BBKTimePicker.this.mIs24HourView) {
                    if (BBKTimePicker.this.mCurrentHour == 12) {
                        BBKTimePicker.this.mCurrentHour = 0;
                    }
                    if (!BBKTimePicker.this.mIsAm) {
                        BBKTimePicker bBKTimePicker = BBKTimePicker.this;
                        bBKTimePicker.mCurrentHour = bBKTimePicker.mCurrentHour + 12;
                    }
                }
                BBKTimePicker.this.onTimeChanged();
            }
        });
        String[] mins = new String[60];
        int i = 0;
        while (i < mins.length) {
            mins[i] = i < 10 ? "0" + i : String.valueOf(i);
            i++;
        }
        this.mMinutePicker.setRange(mins, 5);
        if (hidePickerText()) {
            this.mMinutePicker.setPickText(Events.DEFAULT_SORT_ORDER);
        } else {
            this.mMinutePicker.setPickText(context.getString(51249269));
        }
        this.mMinutePicker.setOnSelectChangedListener(new OnChangedListener() {
            public void onChanged(String oldVal, String newVal) {
                BBKTimePicker.this.mCurrentMinute = Integer.valueOf(newVal).intValue();
                BBKTimePicker.this.onTimeChanged();
            }
        });
        configurePickerRanges();
        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER);
        this.mIsAm = this.mCurrentHour < 12 ? DEFAULT_ENABLED_STATE : false;
        this.mAmPmStrings = new DateFormatSymbols().getAmPmStrings();
        this.mAmPmPicker.setRange(this.mAmPmStrings, 5);
        if (this.mIsAm) {
            this.mAmPmPicker.setScrollItemPositionByRange(this.mAmPmStrings[0]);
        } else {
            this.mAmPmPicker.setScrollItemPositionByRange(this.mAmPmStrings[1]);
        }
        this.mAmPmPicker.setOnSelectChangedListener(new OnChangedListener() {
            public void onChanged(String oldVal, String newVal) {
                BBKTimePicker bBKTimePicker;
                if (BBKTimePicker.this.mIsAm) {
                    if (BBKTimePicker.this.mCurrentHour < 12) {
                        bBKTimePicker = BBKTimePicker.this;
                        bBKTimePicker.mCurrentHour = bBKTimePicker.mCurrentHour + 12;
                    }
                } else if (BBKTimePicker.this.mCurrentHour >= 12) {
                    bBKTimePicker = BBKTimePicker.this;
                    bBKTimePicker.mCurrentHour = bBKTimePicker.mCurrentHour - 12;
                }
                BBKTimePicker.this.mIsAm = BBKTimePicker.this.mIsAm ^ 1;
                BBKTimePicker.this.onTimeChanged();
            }
        });
        setCurrentHour(Integer.valueOf(this.mTempCalendar.get(11)));
        setCurrentMinute(Integer.valueOf(this.mTempCalendar.get(12)));
        setEnabled(isEnabled());
    }

    private void configurePickerRanges() {
        int paddingStart = this.mContext.getResources().getDimensionPixelSize(51118261);
        if (this.mIs24HourView) {
            this.mAmPmPicker.setVisibility(8);
            this.mHourPicker.setPaddingRelative(paddingStart, getPaddingTop(), getPaddingEnd(), getPaddingBottom());
            if (FtBuild.getRomVersion() >= 3.0f) {
                String[] hour = new String[24];
                for (int i = 0; i < 24; i++) {
                    if (i < 10) {
                        hour[i] = "0" + String.valueOf(i);
                    } else {
                        hour[i] = String.valueOf(i);
                    }
                }
                this.mHourPicker.setRange(hour, 5);
                return;
            }
            this.mHourPicker.setRange(0, 23, 5);
            return;
        }
        this.mAmPmPicker.setVisibility(0);
        this.mHourPicker.setRange(1, 12, 5);
        this.mHourPicker.setPaddingRelative(0, getPaddingTop(), getPaddingEnd(), getPaddingBottom());
    }

    public ScrollNumberPicker getHourPicker() {
        return this.mHourPicker;
    }

    public ScrollNumberPicker getMinutePicker() {
        return this.mMinutePicker;
    }

    public ScrollNumberPicker getAmPmPicker() {
        return this.mAmPmPicker;
    }

    public void setSelectedItemTextColor(int selectedItemColor) {
        this.mHourPicker.setSelectedItemTextColor(selectedItemColor);
        this.mMinutePicker.setSelectedItemTextColor(selectedItemColor);
        this.mAmPmPicker.setSelectedItemTextColor(selectedItemColor);
    }

    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), getCurrentHour().intValue(), getCurrentMinute().intValue(), null);
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(Integer.valueOf(ss.getHour()));
        setCurrentMinute(Integer.valueOf(ss.getMinute()));
    }
}
