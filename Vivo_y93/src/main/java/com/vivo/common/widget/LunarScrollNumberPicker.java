package com.vivo.common.widget;

import android.content.Context;
import android.util.AttributeSet;

@Deprecated
public class LunarScrollNumberPicker extends ScrollNumberPicker {
    private final boolean DEBUG = false;
    private final String TAG = "LunarScrollNumberPicker";
    private OnChangedListener mListener = null;

    public interface OnChangedListener {
        void onChanged(int i);
    }

    public LunarScrollNumberPicker(Context context) {
        super(context);
    }

    public LunarScrollNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LunarScrollNumberPicker(Context context, AttributeSet attrs, int defAttr) {
        super(context, attrs, defAttr);
    }

    public void setRange(String[] list, int maxLines, int monthdays) {
        if (list != null && monthdays > 0) {
            String[] newList = new String[monthdays];
            for (int i = 0; i < monthdays; i++) {
                newList[i] = list[i];
            }
            setRange(newList, maxLines);
        }
    }

    public void setOnSelectChangedListener(OnChangedListener listener) {
        this.mListener = listener;
    }

    protected void onSelectChanged(int desPos, String curStr, String selectItem) {
        if (this.mListener != null) {
            this.mListener.onChanged(desPos);
        }
    }
}
