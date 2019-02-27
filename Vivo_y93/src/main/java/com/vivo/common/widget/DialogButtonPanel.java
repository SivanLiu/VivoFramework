package com.vivo.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;

public class DialogButtonPanel extends LinearLayout {
    public DialogButtonPanel(Context context) {
        super(context);
    }

    public DialogButtonPanel(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public DialogButtonPanel(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        View view;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int maxHeight = 0;
        for (i = 0; i < getChildCount(); i++) {
            view = getChildAt(i);
            if (view.getVisibility() == 0 && maxHeight < view.getMeasuredHeight()) {
                maxHeight = view.getMeasuredHeight();
            }
        }
        for (i = 0; i < getChildCount(); i++) {
            view = getChildAt(i);
            if (view.getVisibility() == 0) {
                view.measure(MeasureSpec.makeMeasureSpec(view.getMeasuredWidth(), 1073741824), MeasureSpec.makeMeasureSpec(maxHeight, 1073741824));
            }
        }
    }
}
