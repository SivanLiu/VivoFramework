package com.vivo.common.animation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Interpolator;

public class CustomBounceInterpolator implements Interpolator {
    private double A;
    private double a;
    private double b;
    private double deltaK;
    private double excursion;
    private double k;
    private double k0;

    public CustomBounceInterpolator() {
        setParams();
    }

    public CustomBounceInterpolator(Context context, AttributeSet attrs) {
        setParams();
    }

    private void setParams() {
        this.excursion = Math.asin(0.6d) + 0.13962634015954636d;
        this.k0 = 0.409061543436171d;
        this.deltaK = 0.1636246173744684d;
        this.k = this.k0;
        this.a = (Math.sqrt(322.138333713389d) - 0.2591261478765948d) / 4.0d;
        this.b = 1.0d - (0.6d / this.a);
    }

    public float getInterpolation(float input) {
        this.A = (0.6d / (((((double) input) * this.k) * 32.0d) + this.a)) + this.b;
        this.k = 78.53981633974483d / ((double) (((4.0f * input) * 32.0f) + 192.0f));
        return (float) (1.0d - (this.A * Math.sin(((((double) input) * this.k) * 32.0d) + this.excursion)));
    }
}
