package com.vivo.common.widget;

import android.content.Context;
import android.os.FtBuild;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class PreferenceRelativeLayout extends RelativeLayout {
    private final boolean DEBUG = false;
    private final String TAG = "PreferenceRelativeLayout";
    private final float TITLE_MAX_LENGTH_PRECENT = 0.7f;

    public PreferenceRelativeLayout(Context context) {
        super(context);
    }

    public PreferenceRelativeLayout(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public PreferenceRelativeLayout(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        boolean needResize = false;
        TextView titleView = (TextView) findViewById(16908310);
        TextView summaryView = (TextView) findViewById(16908304);
        if (!FtBuild.isOverSeas()) {
            if (titleView != null && titleView.getVisibility() != 8 && summaryView != null && summaryView.getVisibility() != 8) {
                needResize = true;
            } else if (titleView != null && titleView.getVisibility() == 0) {
                titleView.setMaxLines(Integer.MAX_VALUE);
            }
        }
        if (needResize) {
            titleView.setMaxWidth(Integer.MAX_VALUE);
            titleView.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(titleView.getMeasuredHeight(), 1073741824));
            summaryView.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(summaryView.getMeasuredHeight(), 0));
            int totalWidth = getMeasuredWidth();
            int extPadding = getPaddingLeft() + getPaddingRight();
            LayoutParams lp = (LayoutParams) summaryView.getLayoutParams();
            int extMargin = (lp.leftMargin + lp.rightMargin) + 0;
            lp = (LayoutParams) titleView.getLayoutParams();
            extMargin += lp.leftMargin + lp.rightMargin;
            int titleWidth = titleView.getMeasuredWidth();
            int summaryWidth = summaryView.getMeasuredWidth();
            if (((titleWidth + summaryWidth) + extPadding) + extMargin > totalWidth) {
                titleView.setMaxWidth(Math.max(((totalWidth - summaryWidth) - extPadding) - extMargin, (int) (((float) totalWidth) * 0.7f)));
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
