package vivo.util;

public class BouncyConversion {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f0-assertionsDisabled = (BouncyConversion.class.desiredAssertionStatus() ^ 1);
    private final double mBounciness;
    private final double mBouncyFriction;
    private final double mBouncyTension;
    private final double mSpeed;

    public BouncyConversion(double speed, double bounciness) {
        this.mSpeed = speed;
        this.mBounciness = bounciness;
        double b = project_normal(normalize(bounciness / 1.7d, 0.0d, 20.0d), 0.0d, 0.8d);
        this.mBouncyTension = project_normal(normalize(speed / 1.7d, 0.0d, 20.0d), 0.5d, 200.0d);
        double d = b;
        this.mBouncyFriction = quadratic_out_interpolation(d, b3_nobounce(this.mBouncyTension), 0.01d);
    }

    public double getSpeed() {
        return this.mSpeed;
    }

    public double getBounciness() {
        return this.mBounciness;
    }

    public double getBouncyTension() {
        return this.mBouncyTension;
    }

    public double getBouncyFriction() {
        return this.mBouncyFriction;
    }

    private double normalize(double value, double startValue, double endValue) {
        return (value - startValue) / (endValue - startValue);
    }

    private double project_normal(double n, double start, double end) {
        return ((end - start) * n) + start;
    }

    private double linear_interpolation(double t, double start, double end) {
        return (t * end) + ((1.0d - t) * start);
    }

    private double quadratic_out_interpolation(double t, double start, double end) {
        return linear_interpolation((2.0d * t) - (t * t), start, end);
    }

    private double b3_friction1(double x) {
        return (((Math.pow(x, 3.0d) * 7.0E-4d) - (Math.pow(x, 2.0d) * 0.031d)) + (0.64d * x)) + 1.28d;
    }

    private double b3_friction2(double x) {
        return (((Math.pow(x, 3.0d) * 4.4E-5d) - (Math.pow(x, 2.0d) * 0.006d)) + (0.36d * x)) + 2.0d;
    }

    private double b3_friction3(double x) {
        return (((Math.pow(x, 3.0d) * 4.5E-7d) - (Math.pow(x, 2.0d) * 3.32E-4d)) + (0.1078d * x)) + 5.84d;
    }

    private double b3_nobounce(double tension) {
        if (tension <= 18.0d) {
            return b3_friction1(tension);
        }
        if (tension > 18.0d && tension <= 44.0d) {
            return b3_friction2(tension);
        }
        if (tension > 44.0d) {
            return b3_friction3(tension);
        }
        if (f0-assertionsDisabled) {
            return 0.0d;
        }
        throw new AssertionError();
    }
}
