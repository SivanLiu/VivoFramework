package vivo.content.res;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoThemeHelper {
    private static final String TAG = "VivoThemeHelper";
    private static final Pattern sFloatPattern = Pattern.compile("(-?[0-9]+(?:\\.[0-9]+)?)(.*)");

    private VivoThemeHelper() {
    }

    public static Integer parseDimension(String dimen) {
        dimen = dimen.trim();
        int len = dimen.length();
        if (len <= 0) {
            return null;
        }
        char[] buf = dimen.toCharArray();
        for (int i = 0; i < len; i++) {
            if (buf[i] > 255) {
                return null;
            }
        }
        if (buf[0] < '0' && buf[0] > '9' && buf[0] != '.' && buf[0] != '-') {
            return null;
        }
        Matcher mMatcher = sFloatPattern.matcher(dimen);
        if (!mMatcher.matches()) {
            return null;
        }
        String f_str = mMatcher.group(1);
        String unitStr = mMatcher.group(2);
        try {
            int tmpDimen = computeTypedValue(Float.parseFloat(f_str));
            byte byte0 = (byte) 0;
            if (unitStr.equals("px")) {
                byte0 = (byte) 0;
            } else if (unitStr.equals("dp") || unitStr.equals("dip")) {
                byte0 = (byte) 1;
            } else if (unitStr.equals("sp")) {
                byte0 = (byte) 2;
            } else if (unitStr.equals("pt")) {
                byte0 = (byte) 3;
            } else if (unitStr.equals("in")) {
                byte0 = (byte) 4;
            } else if (unitStr.equals("mm")) {
                byte0 = (byte) 5;
            }
            return Integer.valueOf(byte0 | tmpDimen);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int computeTypedValue(float value) {
        int radix;
        int shift;
        boolean neg = value < 0.0f;
        if (neg) {
            value = -value;
        }
        long bits = (long) ((8388608.0f * value) + 0.5f);
        if ((8388607 & bits) == 0) {
            radix = 0;
            shift = 23;
        } else if ((-8388608 & bits) == 0) {
            radix = 3;
            shift = 0;
        } else if ((-2147483648L & bits) == 0) {
            radix = 2;
            shift = 8;
        } else if ((-549755813888L & bits) == 0) {
            radix = 1;
            shift = 16;
        } else {
            radix = 0;
            shift = 23;
        }
        int mantissa = (int) ((bits >> shift) & 16777215);
        if (neg) {
            mantissa = (-mantissa) & 16777215;
        }
        return (radix << 4) | (mantissa << 8);
    }
}
