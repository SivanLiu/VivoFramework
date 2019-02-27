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
import com.vivo.common.widget.BBKDatePicker;
import com.vivo.common.widget.BBKDatePicker.OnDateChangedListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class BBKDatePickerDialog extends AlertDialog implements OnClickListener, OnDateChangedListener {
    private static final String DAY = "day";
    private static final String MONTH = "month";
    private static final String YEAR = "year";
    private final Calendar mCalendar;
    private final OnDateSetListener mCallBack;
    private Context mContext;
    private final BBKDatePicker mDatePicker;
    private int mInitialDay;
    private int mInitialMonth;
    private int mInitialYear;
    private final DateFormat mTitleDateFormat;

    public interface OnDateSetListener {
        void onDateSet(BBKDatePicker bBKDatePicker, int i, int i2, int i3);
    }

    public BBKDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        this(context, 0, callBack, year, monthOfYear, dayOfMonth);
    }

    public BBKDatePickerDialog(Context context, int theme, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, theme);
        this.mContext = context;
        this.mCallBack = callBack;
        this.mInitialYear = year;
        this.mInitialMonth = monthOfYear;
        this.mInitialDay = dayOfMonth;
        this.mTitleDateFormat = new SimpleDateFormat(BBKDatePicker.getDateFormat(context));
        this.mCalendar = Calendar.getInstance();
        updateTitle(this.mInitialYear, this.mInitialMonth, this.mInitialDay);
        Context themeContext = getContext();
        setButton(-1, themeContext.getText(51249411), this);
        setButton(-2, themeContext.getText(17039360), (OnClickListener) null);
        setIcon(0);
        View view = ((LayoutInflater) themeContext.getSystemService("layout_inflater")).inflate(50528275, null);
        setView(view);
        this.mDatePicker = (BBKDatePicker) view.findViewById(51183679);
        this.mDatePicker.init(year, monthOfYear, dayOfMonth, this);
        this.mDatePicker.setDatePickerTopBackgroundResource(50463083);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mCallBack != null) {
            this.mDatePicker.clearFocus();
            this.mCallBack.onDateSet(this.mDatePicker, this.mDatePicker.getYear(), this.mDatePicker.getMonth(), this.mDatePicker.getDayOfMonth());
        }
    }

    public void onDateChanged(BBKDatePicker view, int year, int month, int day) {
        updateTitle(year, month, day);
    }

    private void updateTitle(int year, int month, int day) {
        this.mCalendar.set(1, year);
        this.mCalendar.set(2, month);
        this.mCalendar.set(5, day);
        Date dummyDate = this.mCalendar.getTime();
        String dateStr = this.mTitleDateFormat.format(dummyDate);
        int newYear = dummyDate.getYear() + 1900;
        if (BBKDatePicker.isThaiCalendar(this.mContext)) {
            dateStr = dateStr.replaceFirst(Integer.toString(newYear), Integer.toString(newYear + 543));
            if (Locale.getDefault().getLanguage().equals("th")) {
                dateStr = dateStr.replace("ค.ศ.", "พ.ศ.");
            }
        }
        setTitle(dateStr);
    }

    public BBKDatePicker getDatePicker() {
        return this.mDatePicker;
    }

    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        this.mDatePicker.updateDate(year, monthOfYear, dayOfMonth);
    }

    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, this.mDatePicker.getYear());
        state.putInt(MONTH, this.mDatePicker.getMonth());
        state.putInt(DAY, this.mDatePicker.getDayOfMonth());
        return state;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mDatePicker.init(savedInstanceState.getInt(YEAR), savedInstanceState.getInt(MONTH), savedInstanceState.getInt(DAY), this);
    }

    public void updateYearRange(int start, int end) {
        this.mDatePicker.updateYearRange(start, end);
    }

    public void setYearDisableRange(int start, int end) {
        this.mDatePicker.setYearDisableRange(start, end);
    }
}
