package android.view.animation;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.util.FloatMath;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class TweenerInterpolator implements Interpolator {
    /* renamed from: -android-view-animation-TweenerInterpolator$TweenerTypeSwitchesValues */
    private static final /* synthetic */ int[] f101x7174562b = null;
    private TweenerType mTweenerType = null;

    public enum TweenerType {
        easeInQuad,
        easeOutQuad,
        easeInOutQuad,
        easeInCubic,
        easeOutCubic,
        easeInOutCubic,
        easeInQuart,
        easeOutQuart,
        easeInOutQuart,
        easeInQuint,
        easeOutQuint,
        easeInOutQuint,
        easeInSine,
        easeOutSine,
        easeInOutSine,
        easeInExpo,
        easeOutExpo,
        easeInOutExpo,
        easeInCirc,
        easeOutCirc,
        easeInOutCirc,
        easeInBack,
        easeOutBack,
        easeInOutBack,
        bounce,
        bouncePast,
        easeOutBounce,
        easeFromTo,
        easeFrom,
        easeTo,
        swingFromTo,
        swingFrom,
        swingTo,
        elastic,
        sinusoidal,
        reverse,
        flicker,
        wobble,
        spring
    }

    /* renamed from: -getandroid-view-animation-TweenerInterpolator$TweenerTypeSwitchesValues */
    private static /* synthetic */ int[] m34x1d502c07() {
        if (f101x7174562b != null) {
            return f101x7174562b;
        }
        int[] iArr = new int[TweenerType.values().length];
        try {
            iArr[TweenerType.bounce.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[TweenerType.bouncePast.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[TweenerType.easeFrom.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[TweenerType.easeFromTo.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[TweenerType.easeInBack.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[TweenerType.easeInCirc.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[TweenerType.easeInCubic.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[TweenerType.easeInExpo.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[TweenerType.easeInOutBack.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[TweenerType.easeInOutCirc.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[TweenerType.easeInOutCubic.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[TweenerType.easeInOutExpo.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[TweenerType.easeInOutQuad.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[TweenerType.easeInOutQuart.ordinal()] = 14;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[TweenerType.easeInOutQuint.ordinal()] = 15;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[TweenerType.easeInOutSine.ordinal()] = 16;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[TweenerType.easeInQuad.ordinal()] = 17;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[TweenerType.easeInQuart.ordinal()] = 18;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[TweenerType.easeInQuint.ordinal()] = 19;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[TweenerType.easeInSine.ordinal()] = 20;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[TweenerType.easeOutBack.ordinal()] = 21;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[TweenerType.easeOutBounce.ordinal()] = 22;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[TweenerType.easeOutCirc.ordinal()] = 23;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[TweenerType.easeOutCubic.ordinal()] = 24;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[TweenerType.easeOutExpo.ordinal()] = 25;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[TweenerType.easeOutQuad.ordinal()] = 26;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[TweenerType.easeOutQuart.ordinal()] = 27;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[TweenerType.easeOutQuint.ordinal()] = 28;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[TweenerType.easeOutSine.ordinal()] = 29;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[TweenerType.easeTo.ordinal()] = 30;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[TweenerType.elastic.ordinal()] = 31;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[TweenerType.flicker.ordinal()] = 32;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[TweenerType.reverse.ordinal()] = 33;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[TweenerType.sinusoidal.ordinal()] = 34;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[TweenerType.spring.ordinal()] = 35;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[TweenerType.swingFrom.ordinal()] = 36;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[TweenerType.swingFromTo.ordinal()] = 37;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[TweenerType.swingTo.ordinal()] = 38;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[TweenerType.wobble.ordinal()] = 39;
        } catch (NoSuchFieldError e39) {
        }
        f101x7174562b = iArr;
        return iArr;
    }

    public TweenerInterpolator(TweenerType type) {
        this.mTweenerType = type;
    }

    public float getInterpolation(float input) {
        switch (m34x1d502c07()[this.mTweenerType.ordinal()]) {
            case 1:
                return bounce(input);
            case 2:
                return bouncePast(input);
            case 3:
                return easeFrom(input);
            case 4:
                return easeFromTo(input);
            case 5:
                return easeInBack(input);
            case 6:
                return easeInCirc(input);
            case 7:
                return easeInCubic(input);
            case 8:
                return easeInExpo(input);
            case 9:
                return easeInOutBack(input);
            case 10:
                return easeInOutCirc(input);
            case 11:
                easeInOutCubic(input);
                break;
            case 12:
                return easeInOutExpo(input);
            case 13:
                return easeInOutQuad(input);
            case 14:
                return easeInOutQuart(input);
            case 15:
                return easeInOutQuint(input);
            case 16:
                return easeInOutSine(input);
            case 17:
                return easeInQuad(input);
            case 18:
                break;
            case 19:
                return easeInQuint(input);
            case 20:
                return easeInSine(input);
            case 21:
                return easeOutBack(input);
            case 22:
                return easeOutBounce(input);
            case 23:
                return easeOutCirc(input);
            case 24:
                return easeOutCubic(input);
            case 25:
                return easeOutExpo(input);
            case 26:
                return easeOutQuad(input);
            case 27:
                return easeOutQuart(input);
            case 28:
                return easeOutQuint(input);
            case 29:
                return easeOutSine(input);
            case 30:
                return easeTo(input);
            case 31:
                return elastic(input);
            case 32:
                return flicker(input);
            case 33:
                return reverse(input);
            case 34:
                return sinusoidal(input);
            case 35:
                return spring(input);
            case 36:
                return swingFrom(input);
            case 37:
                return swingFromTo(input);
            case 38:
                return swingTo(input);
            case 39:
                return wobble(input);
            default:
                return linear(input);
        }
        return easeInQuart(input);
    }

    private float easeInQuad(float input) {
        return (float) Math.pow((double) input, 2.0d);
    }

    private float easeOutQuad(float input) {
        return (float) (-(Math.pow((double) (input - 1.0f), 2.0d) - 1.0d));
    }

    private float easeInOutQuad(float input) {
        input /= 0.5f;
        if (input < 1.0f) {
            return (float) (Math.pow((double) input, 2.0d) * 0.5d);
        }
        input -= 2.0f;
        return ((input * input) - 2.0f) * -0.5f;
    }

    private float easeInCubic(float input) {
        return (float) Math.pow((double) input, 3.0d);
    }

    private float easeOutCubic(float input) {
        return (float) (Math.pow((double) (input - 1.0f), 3.0d) + 1.0d);
    }

    private float easeInOutCubic(float input) {
        input /= 0.5f;
        if (input < 1.0f) {
            return (float) (Math.pow((double) input, 3.0d) * 0.5d);
        }
        return (float) ((Math.pow((double) (input - 2.0f), 3.0d) + 2.0d) * 0.5d);
    }

    private float easeInQuart(float input) {
        return (float) Math.pow((double) input, 4.0d);
    }

    private float easeOutQuart(float input) {
        return (float) (-(Math.pow((double) (input - 1.0f), 4.0d) - 1.0d));
    }

    private float easeInOutQuart(float input) {
        double d = ((double) input) / 0.5d;
        input = (float) d;
        if (d < 1.0d) {
            return (float) (Math.pow((double) input, 4.0d) * 0.5d);
        }
        input -= 2.0f;
        return (float) (((((double) input) * Math.pow((double) input, 3.0d)) - 2.0d) * -0.5d);
    }

    private float easeInQuint(float input) {
        return (float) Math.pow((double) input, 5.0d);
    }

    private float easeOutQuint(float input) {
        return (float) (Math.pow((double) (input - 1.0f), 5.0d) + 1.0d);
    }

    private float easeInOutQuint(float input) {
        input /= 0.5f;
        if (input < 1.0f) {
            return (float) (Math.pow((double) input, 5.0d) * 0.5d);
        }
        return (float) ((Math.pow((double) (input - 2.0f), 5.0d) + 2.0d) * 0.5d);
    }

    private float easeInSine(float input) {
        return (-FloatMath.cos((float) (((double) input) * 1.5707963267948966d))) + 1.0f;
    }

    private float easeOutSine(float input) {
        return FloatMath.sin((float) (((double) input) * 1.5707963267948966d));
    }

    private float easeInOutSine(float input) {
        return (float) ((Math.cos(((double) input) * 3.141592653589793d) - 1.0d) * -0.5d);
    }

    private float easeInExpo(float input) {
        return (float) (input == 0.0f ? 0.0d : Math.pow(2.0d, (double) ((input - 1.0f) * 10.0f)));
    }

    private float easeOutExpo(float input) {
        double d = 1.0d;
        if (input != 1.0f) {
            d = 1.0d + (-Math.pow(2.0d, (double) (-10.0f * input)));
        }
        return (float) d;
    }

    private float easeInOutExpo(float input) {
        if (input == 0.0f) {
            return 0.0f;
        }
        if (input == 1.0f) {
            return 1.0f;
        }
        input /= 0.5f;
        if (input < 1.0f) {
            return (float) (Math.pow(2.0d, (double) ((input - 1.0f) * 10.0f)) * 0.5d);
        }
        return (float) (((-Math.pow(2.0d, (double) (-10.0f * (input - 1.0f)))) + 2.0d) * 0.5d);
    }

    private float easeInCirc(float input) {
        return -(FloatMath.sqrt(1.0f - (input * input)) - 1.0f);
    }

    private float easeOutCirc(float input) {
        return FloatMath.sqrt((float) (1.0d - Math.pow((double) (input - 1.0f), 2.0d)));
    }

    private float easeInOutCirc(float input) {
        input /= 0.5f;
        if (input < 1.0f) {
            return (FloatMath.sqrt(1.0f - (input * input)) - 1.0f) * -0.5f;
        }
        input -= 2.0f;
        return (FloatMath.sqrt(1.0f - (input * input)) + 1.0f) * 0.5f;
    }

    private float easeInBack(float input) {
        return (input * input) * ((2.70158f * input) - 1.70158f);
    }

    private float easeOutBack(float input) {
        input -= 1.0f;
        return ((input * input) * ((2.70158f * input) + 1.70158f)) + 1.0f;
    }

    private float easeInOutBack(float input) {
        input /= 0.5f;
        if (input < 1.0f) {
            return ((input * input) * (((1.0f + 2.5949094f) * input) - 2.5949094f)) * 0.5f;
        }
        input -= 2.0f;
        return (((input * input) * (((1.0f + 2.5949094f) * input) + 2.5949094f)) + 2.0f) * 0.5f;
    }

    private float bounce(float input) {
        if (input < 0.36363637f) {
            return (7.5625f * input) * input;
        }
        double d;
        if (input < 0.72727275f) {
            d = ((double) input) - 0.5454545454545454d;
            return (float) (((d * 7.5625d) * ((double) ((float) d))) + 0.75d);
        } else if (((double) input) < 0.9090909090909091d) {
            d = ((double) input) - 0.8181818181818182d;
            return (float) (((d * 7.5625d) * ((double) ((float) d))) + 0.9375d);
        } else {
            d = ((double) input) - 0.9545454545454546d;
            return (float) (((d * 7.5625d) * ((double) ((float) d))) + 0.984375d);
        }
    }

    private float bouncePast(float input) {
        if (input < 0.36363637f) {
            return (7.5625f * input) * input;
        }
        double d;
        if (input < 0.72727275f) {
            d = ((double) input) - 0.5454545454545454d;
            return (float) (2.0d - (((d * 7.5625d) * ((double) ((float) d))) + 0.75d));
        } else if (((double) input) < 0.9090909090909091d) {
            d = ((double) input) - 0.8181818181818182d;
            return (float) (2.0d - (((d * 7.5625d) * ((double) ((float) d))) + 0.9375d));
        } else {
            d = ((double) input) - 0.9545454545454546d;
            return (float) (2.0d - (((d * 7.5625d) * ((double) ((float) d))) + 0.984375d));
        }
    }

    private float easeOutBounce(float input) {
        if (((double) input) < 0.36363636363636365d) {
            return (7.5625f * input) * input;
        }
        if (input < 0.72727275f) {
            input -= 0.54545456f;
            return ((7.5625f * input) * input) + 0.75f;
        } else if (((double) input) < 0.9090909090909091d) {
            input -= 0.8181818f;
            return ((7.5625f * input) * input) + 0.9375f;
        } else {
            input -= 0.95454544f;
            return ((7.5625f * input) * input) + 0.984375f;
        }
    }

    private float easeFromTo(float input) {
        input /= 0.5f;
        if (input < 1.0f) {
            return (float) (Math.pow((double) input, 4.0d) * 0.5d);
        }
        input -= 2.0f;
        return (float) (((((double) input) * Math.pow((double) input, 3.0d)) - 2.0d) * -0.5d);
    }

    private float easeFrom(float input) {
        return (float) Math.pow((double) input, 4.0d);
    }

    private float easeTo(float input) {
        return (float) Math.pow((double) input, 0.25d);
    }

    private float swingFromTo(float input) {
        input /= 0.5f;
        if (input < 1.0f) {
            return ((input * input) * (((1.0f + 2.5949094f) * input) - 2.5949094f)) * 0.5f;
        }
        input -= 2.0f;
        return (((input * input) * (((1.0f + 2.5949094f) * input) + 2.5949094f)) + 2.0f) * 0.5f;
    }

    private float swingFrom(float input) {
        return (input * input) * ((2.70158f * input) - 1.70158f);
    }

    private float swingTo(float input) {
        input -= 1.0f;
        return ((input * input) * ((2.70158f * input) + 1.70158f)) + 1.0f;
    }

    private float elastic(float input) {
        return (float) (((Math.pow(4.0d, (double) (-8.0f * input)) * -1.0d) * Math.sin((((double) ((6.0f * input) - 1.0f)) * 6.283185307179586d) / 2.0d)) + 1.0d);
    }

    private float sinusoidal(float input) {
        return (float) (((-Math.cos(((double) input) * 3.141592653589793d)) / 2.0d) + 0.5d);
    }

    private float reverse(float input) {
        return 1.0f - input;
    }

    private float flicker(float input) {
        input = (float) (((double) input) + ((Math.random() - 0.5d) / 5.0d));
        if (input < 0.0f) {
            input = 0.0f;
        } else if (input > 1.0f) {
            input = 1.0f;
        }
        return sinusoidal(input);
    }

    private float wobble(float input) {
        return (float) (((-Math.cos((((double) input) * 3.141592653589793d) * ((double) (9.0f * input)))) / 2.0d) + 0.5d);
    }

    private float spring(float input) {
        return (float) (1.0d - (Math.cos(((double) (4.5f * input)) * 3.141592653589793d) * Math.exp((double) ((-input) * 6.0f))));
    }

    private float linear(float input) {
        return input;
    }
}
