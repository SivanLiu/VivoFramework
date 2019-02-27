package com.vivo.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

public class BBKVivoGeliDatePicker extends BBKDatePicker {
    private OnDateChangedListener mDateChangedListener;

    public interface OnDateChangedListener {
        void onDateChanged(BBKVivoGeliDatePicker bBKVivoGeliDatePicker, int i, int i2, int i3);
    }

    public BBKVivoGeliDatePicker(Context context) {
        this(context, null);
    }

    public BBKVivoGeliDatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BBKVivoGeliDatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDateChangedListener = null;
    }

    public void init(int year, int monthOfYear, int dayOfMonth, OnDateChangedListener onDateChangedListener) {
        this.mDateChangedListener = onDateChangedListener;
        super.init(year, monthOfYear, dayOfMonth, new com.vivo.common.widget.BBKDatePicker.OnDateChangedListener() {
            public void onDateChanged(BBKDatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (BBKVivoGeliDatePicker.this.mDateChangedListener != null) {
                    BBKVivoGeliDatePicker.this.mDateChangedListener.onDateChanged(BBKVivoGeliDatePicker.this, year, monthOfYear, dayOfMonth);
                }
            }
        });
    }

    protected void initLayoutView(Context context) {
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(50528280, this, true);
    }

    public GeliScrollNumberPicker getDayPicker() {
        return toGeliScrollNumberPicker(super.getDayPicker());
    }

    public GeliScrollNumberPicker getMonthPicker() {
        return toGeliScrollNumberPicker(super.getMonthPicker());
    }

    public GeliScrollNumberPicker getYearPicker() {
        return toGeliScrollNumberPicker(super.getYearPicker());
    }

    private GeliScrollNumberPicker toGeliScrollNumberPicker(ScrollNumberPicker picker) {
        if (picker instanceof GeliScrollNumberPicker) {
            return (GeliScrollNumberPicker) picker;
        }
        StringBuilder append = new StringBuilder().append("Can't translate to GeliScrollNumberPicker : ");
        if (picker == null) {
            picker = "null";
        }
        throw new IllegalArgumentException(append.append(picker).toString());
    }
}
