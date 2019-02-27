package com.vivo.common.widget;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class BBKCountIndicator extends LinearLayout {
    private static final boolean DEBUG = true;
    private static final String TAG = "ScreenIndicator";
    private int MAX_ANALOG_COUNT = 10;
    private Drawable mActiveIndicator = null;
    private boolean mAnalogIndicator = DEBUG;
    private LinearLayout mAnalogIndicatorContainer = null;
    private Context mContext = null;
    private int mCurrentLevel = 0;
    private TextView mDigitalIndicator = null;
    private Drawable mNormalIndicator = null;
    private int mTotalLevel = 0;

    public interface OnIndicatorClickListener {
        void onIndicatorClick(int i);
    }

    public BBKCountIndicator(Context context) {
        super(context);
        init(context);
    }

    public BBKCountIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = context.getResources();
        this.mActiveIndicator = res.getDrawable(50463324);
        this.mNormalIndicator = res.getDrawable(50463325);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        this.mAnalogIndicatorContainer = new LinearLayout(context);
        this.mAnalogIndicatorContainer.setOrientation(getOrientation());
        addView(this.mAnalogIndicatorContainer, new LayoutParams(-1, -1));
        this.mDigitalIndicator = new TextView(context);
        this.mDigitalIndicator.setTextColor(-1);
        this.mDigitalIndicator.setShadowLayer(1.5f, 0.0f, 1.5f, -16777216);
        this.mDigitalIndicator.setGravity(17);
        addView(this.mDigitalIndicator, new LayoutParams(-1, -1));
        if (this.mCurrentLevel >= this.mTotalLevel) {
            this.mCurrentLevel = this.mTotalLevel - 1;
        }
        if (this.mCurrentLevel < 0) {
            this.mCurrentLevel = 0;
        }
        if (this.mTotalLevel <= this.MAX_ANALOG_COUNT) {
            ImageView indicator;
            this.mAnalogIndicatorContainer.setVisibility(0);
            this.mDigitalIndicator.setVisibility(8);
            this.mAnalogIndicator = DEBUG;
            for (int i = 0; i < this.mTotalLevel; i++) {
                indicator = new ImageView(this.mContext);
                indicator.setImageDrawable(this.mNormalIndicator);
                LayoutParams lp = new LayoutParams(-2, -2);
                lp.weight = 1.0f;
                lp.gravity = 17;
                this.mAnalogIndicatorContainer.addView(indicator, lp);
            }
            indicator = (ImageView) this.mAnalogIndicatorContainer.getChildAt(this.mCurrentLevel);
            if (indicator != null) {
                if (this.mActiveIndicator instanceof LevelListDrawable) {
                    this.mActiveIndicator.setLevel(this.mCurrentLevel);
                }
                indicator.setImageDrawable(this.mActiveIndicator);
                return;
            }
            return;
        }
        this.mAnalogIndicatorContainer.setVisibility(8);
        this.mDigitalIndicator.setVisibility(0);
        this.mAnalogIndicator = false;
        this.mDigitalIndicator.setText(String.valueOf(this.mCurrentLevel + 1) + "/" + String.valueOf(this.mTotalLevel));
    }

    public void setMaxAnalogCount(int maxAnalogCount) {
        if (maxAnalogCount > 0) {
            this.MAX_ANALOG_COUNT = maxAnalogCount;
        }
    }

    public void setActiveIndicator(Drawable d) {
        if (!d.equals(this.mActiveIndicator)) {
            this.mActiveIndicator.unscheduleSelf(null);
        }
        this.mActiveIndicator = d;
    }

    public void setNomalIndicator(Drawable d) {
        if (!d.equals(this.mNormalIndicator)) {
            this.mNormalIndicator.unscheduleSelf(null);
        }
        this.mNormalIndicator = d;
    }

    public void setTotalLevel(int totalLevel) {
        if (totalLevel != this.mTotalLevel) {
            if (totalLevel <= this.MAX_ANALOG_COUNT) {
                this.mAnalogIndicatorContainer.setVisibility(0);
                this.mDigitalIndicator.setVisibility(8);
                this.mAnalogIndicator = DEBUG;
                int indicatorCount = this.mAnalogIndicatorContainer.getChildCount();
                int gap = Math.abs(indicatorCount - totalLevel);
                if (indicatorCount < totalLevel) {
                    for (int i = 0; i < gap; i++) {
                        ImageView indicator = new ImageView(this.mContext);
                        indicator.setImageDrawable(this.mNormalIndicator);
                        LayoutParams lp = new LayoutParams(-2, -2);
                        lp.weight = 1.0f;
                        lp.gravity = 17;
                        this.mAnalogIndicatorContainer.addView(indicator, lp);
                    }
                } else {
                    this.mAnalogIndicatorContainer.removeViews(this.mAnalogIndicatorContainer.getChildCount() - gap, gap);
                }
            } else {
                this.mAnalogIndicatorContainer.setVisibility(8);
                this.mDigitalIndicator.setVisibility(0);
                this.mAnalogIndicator = false;
            }
            this.mTotalLevel = totalLevel;
            setLevel(this.mCurrentLevel);
        }
    }

    public void setLevel(int currentLevel) {
        if (currentLevel >= this.mTotalLevel) {
            currentLevel = this.mTotalLevel - 1;
        }
        if (currentLevel < 0) {
            currentLevel = 0;
        }
        if (this.mAnalogIndicator) {
            int childCount = this.mAnalogIndicatorContainer.getChildCount();
            for (int i = 0; i < childCount; i++) {
                ImageView imageView = (ImageView) this.mAnalogIndicatorContainer.getChildAt(i);
                if (i == currentLevel) {
                    if (this.mActiveIndicator instanceof LevelListDrawable) {
                        this.mActiveIndicator.setLevel(currentLevel);
                    }
                    imageView.setImageDrawable(this.mActiveIndicator);
                } else {
                    imageView.setImageDrawable(this.mNormalIndicator);
                }
            }
        } else {
            this.mDigitalIndicator.setText(String.valueOf(currentLevel + 1) + "/" + String.valueOf(this.mTotalLevel));
        }
        this.mCurrentLevel = currentLevel;
    }

    public boolean setLevel(int totalLevel, int currentLevel) {
        Log.d("ScreenIndicatorView", "set level new totalLevel" + totalLevel + " new  currentLevel " + currentLevel + " old mTotalLevel = " + this.mTotalLevel + " old mCurrentLevel = " + this.mCurrentLevel);
        if (totalLevel < 0) {
            return false;
        }
        if (currentLevel >= totalLevel) {
            currentLevel = totalLevel - 1;
        }
        if (currentLevel < 0) {
            currentLevel = 0;
        }
        if (totalLevel <= this.MAX_ANALOG_COUNT) {
            this.mAnalogIndicatorContainer.setVisibility(0);
            this.mDigitalIndicator.setVisibility(8);
            this.mAnalogIndicator = DEBUG;
            int indicatorCount = this.mAnalogIndicatorContainer.getChildCount();
            int gap = Math.abs(indicatorCount - totalLevel);
            if (gap != 0) {
                if (indicatorCount < totalLevel) {
                    for (int i = 0; i < gap; i++) {
                        ImageView indicator = new ImageView(this.mContext);
                        indicator.setImageDrawable(this.mNormalIndicator);
                        LayoutParams lp = new LayoutParams(-2, -2);
                        lp.weight = 1.0f;
                        lp.gravity = 17;
                        this.mAnalogIndicatorContainer.addView(indicator, lp);
                    }
                } else {
                    this.mAnalogIndicatorContainer.removeViews(this.mAnalogIndicatorContainer.getChildCount() - gap, gap);
                }
            }
        } else {
            this.mAnalogIndicatorContainer.setVisibility(8);
            this.mDigitalIndicator.setVisibility(0);
            this.mAnalogIndicator = false;
        }
        this.mTotalLevel = totalLevel;
        setLevel(currentLevel);
        requestLayout();
        return DEBUG;
    }
}
