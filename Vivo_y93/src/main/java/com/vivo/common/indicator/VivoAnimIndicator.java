package com.vivo.common.indicator;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class VivoAnimIndicator extends RelativeLayout implements PageIndicator {
    private static final boolean DEBUG = false;
    private static final String TAG = "VivoAnimIndicator";
    private Context mContext = null;
    private VivoCountIndicator mCountIndicator = null;
    private ImageView mIndicatorAnim = null;

    public VivoAnimIndicator(Context context) {
        super(context);
        init(context);
    }

    public VivoAnimIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    protected void onFinishInflate() {
        this.mCountIndicator = (VivoCountIndicator) findViewById(51183656);
        this.mIndicatorAnim = (ImageView) findViewById(51183657);
        this.mCountIndicator.setIndicatorAnim(this.mIndicatorAnim);
    }

    private void init(Context context) {
        this.mContext = context;
    }

    public void setMaxAnalogCount(int maxAnalogCount) {
        this.mCountIndicator.setMaxAnalogCount(maxAnalogCount);
    }

    public void setActiveIndicator(Drawable d) {
        this.mCountIndicator.setActiveIndicator(d);
    }

    public void setNomalIndicator(Drawable d) {
        this.mCountIndicator.setNomalIndicator(d);
    }

    public void setIndicatorDrawable(Drawable nomal, Drawable active, int id) {
        this.mCountIndicator.setNomalIndicator(nomal);
        this.mCountIndicator.setActiveIndicator(active);
        this.mCountIndicator.setIndicatorArray(id);
    }

    public void setTotalLevel(int totalLevel) {
        this.mCountIndicator.setTotalLevel(totalLevel);
    }

    public void setLevel(int currentLevel) {
        this.mCountIndicator.setLevel(currentLevel);
    }

    public void showIndicatorsAnim(Handler handler) {
        this.mCountIndicator.showIndicatorsAnim(handler);
    }

    public boolean setLevel(int totalLevel, int currentLevel) {
        return this.mCountIndicator.setLevel(totalLevel, currentLevel);
    }

    public boolean isIndicatorScrolling() {
        return false;
    }

    public void updateIndicator(int progress, int width) {
        this.mCountIndicator.updateIndicator(progress, width);
    }
}
