package android.view.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.vivo.internal.R;

public class TranslateAnimationRefPos extends TranslateAnimation {
    private final int DEFAULT_REF_POS = 320;
    private float fromOffestY;
    private float mDensity;
    private float mRefPos = 320.0f;
    private float toOffestY;

    public TranslateAnimationRefPos(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray vivoAttr = context.obtainStyledAttributes(attrs, R.styleable.TranslateAnimationRef);
        this.toOffestY = vivoAttr.getDimension(1, 0.0f);
        this.fromOffestY = vivoAttr.getDimension(0, 0.0f);
        vivoAttr.recycle();
        this.mDensity = context.getResources().getDisplayMetrics().density;
    }

    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        if (this.toOffestY != 0.0f) {
            this.mToYDelta += this.toOffestY;
        }
        if (this.fromOffestY != 0.0f) {
            this.mFromYDelta += this.fromOffestY;
        }
        computeNewDuration(height);
    }

    private void computeNewDuration(int height) {
        float pos = this.mRefPos * this.mDensity;
        long oriDuration = getDuration();
        long nDuration = oriDuration;
        if (oriDuration > 0) {
            setDuration((long) (((float) oriDuration) + (((((float) oriDuration) * (((float) height) - pos)) / pos) / 4.0f)));
        }
    }
}
