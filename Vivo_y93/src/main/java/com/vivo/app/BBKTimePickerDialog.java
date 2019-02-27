package com.vivo.app;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.vivo.common.widget.BBKTimePicker;
import com.vivo.common.widget.BBKTimePicker.OnTimeChangedListener;
import java.text.DateFormat;
import java.util.Calendar;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class BBKTimePickerDialog extends AlertDialog implements OnClickListener, OnTimeChangedListener {
    private static final String HOUR = "hour";
    private static final String IS_24_HOUR = "is24hour";
    private static final String MINUTE = "minute";
    private final Calendar mCalendar;
    private final OnTimeSetListener mCallback;
    private final DateFormat mDateFormat;
    private int mInitialHourOfDay;
    private int mInitialMinute;
    private boolean mIs24HourView;
    private final BBKTimePicker mTimePicker;

    public interface OnTimeSetListener {
        void onTimeSet(BBKTimePicker bBKTimePicker, int i, int i2);
    }

    public BBKTimePickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
        this(context, 0, callBack, hourOfDay, minute, is24HourView);
    }

    public BBKTimePickerDialog(Context context, int theme, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
        super(context, theme);
        this.mCallback = callBack;
        this.mInitialHourOfDay = hourOfDay;
        this.mInitialMinute = minute;
        this.mIs24HourView = is24HourView;
        setIcon(0);
        this.mDateFormat = android.text.format.DateFormat.getTimeFormat(context);
        this.mCalendar = Calendar.getInstance();
        updateTitle(this.mInitialHourOfDay, this.mInitialMinute);
        Context themeContext = getContext();
        setButton(-1, themeContext.getText(51249411), this);
        setButton(-2, themeContext.getText(17039360), (OnClickListener) null);
        View view = ((LayoutInflater) themeContext.getSystemService("layout_inflater")).inflate(50528372, null);
        setView(view);
        this.mTimePicker = (BBKTimePicker) view.findViewById(51183791);
        this.mTimePicker.setIs24HourView(Boolean.valueOf(this.mIs24HourView));
        this.mTimePicker.setCurrentHour(Integer.valueOf(this.mInitialHourOfDay));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(this.mInitialMinute));
        this.mTimePicker.setOnTimeChangedListener(this);
        this.mTimePicker.setTimePickerTopBackgroundResource(50463083);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mCallback != null) {
            this.mTimePicker.clearFocus();
            this.mCallback.onTimeSet(this.mTimePicker, this.mTimePicker.getCurrentHour().intValue(), this.mTimePicker.getCurrentMinute().intValue());
        }
    }

    public void updateTime(int hourOfDay, int minutOfHour) {
        this.mTimePicker.setCurrentHour(Integer.valueOf(hourOfDay));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(minutOfHour));
    }

    public void onTimeChanged(BBKTimePicker view, int hourOfDay, int minute) {
        updateTitle(hourOfDay, minute);
    }

    private void updateTitle(int hour, int minute) {
        this.mCalendar.set(11, hour);
        this.mCalendar.set(12, minute);
        setTitle(this.mDateFormat.format(this.mCalendar.getTime()));
    }

    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(HOUR, this.mTimePicker.getCurrentHour().intValue());
        state.putInt(MINUTE, this.mTimePicker.getCurrentMinute().intValue());
        state.putBoolean(IS_24_HOUR, this.mTimePicker.is24HourView());
        return state;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        this.mTimePicker.setIs24HourView(Boolean.valueOf(savedInstanceState.getBoolean(IS_24_HOUR)));
        this.mTimePicker.setCurrentHour(Integer.valueOf(hour));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(minute));
    }
}
