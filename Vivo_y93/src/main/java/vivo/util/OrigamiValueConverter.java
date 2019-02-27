package vivo.util;

public class OrigamiValueConverter {
    public static double tensionFromOrigamiValue(double oValue) {
        return oValue == 0.0d ? 0.0d : ((oValue - 30.0d) * 3.62d) + 194.0d;
    }

    public static double origamiValueFromTension(double tension) {
        return tension == 0.0d ? 0.0d : ((tension - 194.0d) / 3.62d) + 30.0d;
    }

    public static double frictionFromOrigamiValue(double oValue) {
        return oValue == 0.0d ? 0.0d : ((oValue - 8.0d) * 3.0d) + 25.0d;
    }

    public static double origamiValueFromFriction(double friction) {
        return friction == 0.0d ? 0.0d : ((friction - 25.0d) / 3.0d) + 8.0d;
    }
}
