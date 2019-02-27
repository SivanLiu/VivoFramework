package com.vivo.common.indicator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.vivo.internal.R;

public class VivoCountIndicator extends LinearLayout {
    private static final int ANIM_DURATION = 800;
    private static final boolean DEBUG = false;
    private static final String TAG = "VivoCountIndicator";
    private int MAX_ANALOG_COUNT = 10;
    private Drawable mActiveIndicator = null;
    private boolean mAnalogIndicator = true;
    private LinearLayout mAnalogIndicatorContainer = null;
    private Context mContext = null;
    private int mCurrentLevel = 0;
    private TextView mDigitalIndicator = null;
    private ImageView mIndicatorAnim = null;
    private TypedArray mIndicatorArray = null;
    private Drawable mNormalIndicator = null;
    private int mTotalLevel = 0;

    public interface OnIndicatorClickListener {
        void onIndicatorClick(int i);
    }

    public VivoCountIndicator(Context context) {
        super(context);
        init(context);
    }

    public VivoCountIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        TypedValue outValue = new TypedValue();
        this.mContext.getTheme().resolveAttribute(50397305, outValue, true);
        this.mIndicatorArray = getResources().obtainTypedArray(outValue.resourceId);
        Log.d("yang", "mIndicatorArray.length() = " + this.mIndicatorArray.length());
        TypedArray indicatorType = this.mContext.obtainStyledAttributes(null, R.styleable.IndicatorAnim, 50397217, 51314985);
        this.mActiveIndicator = indicatorType.getDrawable(0);
        this.mNormalIndicator = indicatorType.getDrawable(1);
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
            this.mAnalogIndicator = true;
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

    public void setIndicatorAnim(ImageView mIndicatorAnim) {
        this.mIndicatorAnim = mIndicatorAnim;
        mIndicatorAnim.setImageDrawable(this.mIndicatorArray.getDrawable(0));
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

    public void setIndicatorArray(int id) {
    }

    public void setTotalLevel(int totalLevel) {
        if (totalLevel != this.mTotalLevel) {
            if (totalLevel <= this.MAX_ANALOG_COUNT) {
                this.mAnalogIndicatorContainer.setVisibility(0);
                this.mDigitalIndicator.setVisibility(8);
                this.mAnalogIndicator = true;
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
                    if (this.mIndicatorAnim != null && this.mIndicatorAnim.getVisibility() == 0) {
                        this.mIndicatorAnim.setVisibility(8);
                    }
                } else {
                    imageView.setImageDrawable(this.mNormalIndicator);
                }
            }
        } else {
            this.mDigitalIndicator.setText(String.valueOf(currentLevel + 1) + "/" + String.valueOf(this.mTotalLevel));
            if (this.mIndicatorAnim != null) {
                this.mIndicatorAnim.setVisibility(8);
            }
        }
        this.mCurrentLevel = currentLevel;
    }

    public void showIndicatorsAnim(Handler handler) {
        if (this.mTotalLevel > this.MAX_ANALOG_COUNT) {
            handler.sendEmptyMessage(0);
        } else if (this.mAnalogIndicatorContainer != null) {
            boolean isLastIndicator = false;
            int i = this.mAnalogIndicatorContainer.getChildCount() - 1;
            int j = 0;
            while (i >= 0) {
                ImageView imageView = (ImageView) this.mAnalogIndicatorContainer.getChildAt(i);
                imageView.setVisibility(4);
                if (i == 0) {
                    isLastIndicator = true;
                }
                int j2 = j + 1;
                showIndicatorAnim(imageView, j * 50, Boolean.valueOf(isLastIndicator), handler, 800);
                i--;
                j = j2;
            }
        }
    }

    private void showIndicatorAnim(final View indicator, int startDelay, final Boolean isLastIndicator, final Handler handler, int duraiotn) {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", new float[]{0.2f, 1.0f});
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", new float[]{0.2f, 1.0f});
        ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(indicator, new PropertyValuesHolder[]{scaleX, scaleY});
        scale.setDuration((long) (duraiotn / 2));
        scale.setStartDelay((long) startDelay);
        scale.setInterpolator(new OvershootInterpolator(3.5f));
        scale.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                indicator.setVisibility(0);
            }

            public void onAnimationEnd(Animator animation) {
                if (isLastIndicator.booleanValue()) {
                    handler.sendEmptyMessage(0);
                }
            }
        });
        scale.start();
    }

    public boolean setLevel(int totalLevel, int currentLevel) {
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
            this.mAnalogIndicator = true;
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
        return true;
    }

    /* JADX WARNING: Missing block: B:4:0x000a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateIndicator(int progress, int width) {
        if (progress > 0 && progress < (this.mTotalLevel - 1) * width && this.mTotalLevel <= this.MAX_ANALOG_COUNT && this.mIndicatorAnim != null && width != 0) {
            int position = progress / width;
            if (progress % width != 0) {
                ((ImageView) this.mAnalogIndicatorContainer.getChildAt(this.mCurrentLevel)).setImageDrawable(this.mNormalIndicator);
                int id = (int) ((((((float) progress) * 1.0f) % ((float) width)) / ((float) width)) * ((float) (this.mIndicatorArray.length() - 1)));
                this.mIndicatorAnim.setX(this.mAnalogIndicatorContainer.getChildAt(position).getX());
                this.mIndicatorAnim.setImageDrawable(this.mIndicatorArray.getDrawable(id));
                if (this.mIndicatorAnim.getVisibility() != 0) {
                    this.mIndicatorAnim.setVisibility(0);
                }
            }
        }
    }

    public void hideIndicatorAnim() {
        if (this.mIndicatorAnim.getVisibility() == 0) {
            this.mIndicatorAnim.setVisibility(8);
        }
    }
}
