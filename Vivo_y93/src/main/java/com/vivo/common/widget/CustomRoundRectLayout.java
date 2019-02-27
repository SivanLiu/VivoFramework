package com.vivo.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class CustomRoundRectLayout extends LinearLayout {
    private Paint mPaint = new Paint(1);

    public CustomRoundRectLayout(Context context) {
        super(context);
    }

    public CustomRoundRectLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRoundRectLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomRoundRectLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawRoundRect(canvas);
    }

    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawRoundRect(canvas);
    }

    private void drawRoundRect(Canvas canvas) {
        int cornerRadius = getResources().getDimensionPixelSize(51118354);
        canvas.save();
        Path path = new Path();
        path.addRoundRect(new RectF(0.0f, 0.0f, (float) getWidth(), (float) getHeight()), (float) cornerRadius, (float) cornerRadius, Direction.CW);
        this.mPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        this.mPaint.setColor(-16776961);
        canvas.drawPath(path, this.mPaint);
        canvas.restore();
    }
}
