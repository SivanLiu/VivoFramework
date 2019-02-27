package com.vivo.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.FtBuild;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;

public class SlideDialogLayout extends LinearLayout {
    private Paint mPaint;

    public SlideDialogLayout(Context context) {
        this(context, null);
    }

    public SlideDialogLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideDialogLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mPaint = new Paint(1);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int maxHeight = this.mContext.getResources().getDimensionPixelSize(51118265);
        if (getMeasuredHeight() > maxHeight) {
            onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(maxHeight, 1073741824));
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (FtBuild.getRomVersion() >= 4.0f) {
            drawCircle(canvas);
        }
    }

    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (FtBuild.getRomVersion() >= 4.0f) {
            drawCircle(canvas);
        }
    }

    private void drawCircle(Canvas canvas) {
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
