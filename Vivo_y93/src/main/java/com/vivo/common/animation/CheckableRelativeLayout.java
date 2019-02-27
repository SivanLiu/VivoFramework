package com.vivo.common.animation;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class CheckableRelativeLayout extends RelativeLayout implements Checkable, IListEditControl {
    private ListEditControl mEditControl;

    public CheckableRelativeLayout(Context context) {
        super(context);
        inital(context);
    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        inital(context);
    }

    private void inital(Context context) {
        setWillNotDraw(false);
        this.mEditControl = new ListEditControl(context, this);
    }

    public boolean isChecked() {
        return this.mEditControl.isChecked();
    }

    public void setChecked(boolean checked) {
        this.mEditControl.setChecked(checked);
    }

    public void toggle() {
        this.mEditControl.toggle();
        invalidate();
    }

    public ListEditControl getEditControl() {
        return this.mEditControl;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mEditControl != null) {
            this.mEditControl.draw(canvas);
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        if (this.mEditControl != null && this.mEditControl.verifyDrawable(who)) {
            invalidate();
        }
        return super.verifyDrawable(who);
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mEditControl != null) {
            this.mEditControl.jumpDrawablesToCurrentState();
        }
    }
}
