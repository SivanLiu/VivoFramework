package com.vivo.common.animation;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class FakeView extends View {
    private View mFakedView;

    public FakeView(Context context) {
        super(context);
    }

    public FakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setFakedView(View view) {
        this.mFakedView = view;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(this.mFakedView.getMeasuredWidth(), 1073741824), MeasureSpec.makeMeasureSpec(this.mFakedView.getMeasuredHeight(), 1073741824));
    }

    protected void onDraw(Canvas canvas) {
        if (this.mFakedView != null) {
            this.mFakedView.draw(canvas);
        }
    }
}
