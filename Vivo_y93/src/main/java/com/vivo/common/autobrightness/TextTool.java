package com.vivo.common.autobrightness;

import com.vivo.common.provider.Calendar.Events;
import java.util.regex.Pattern;

public final class TextTool {
    public static String makeTag(String className) {
        return "Auto" + className;
    }

    public static boolean isEmpty(CharSequence s) {
        boolean z = true;
        if (s == null) {
            return true;
        }
        if (s.length() != 0) {
            z = false;
        }
        return z;
    }

    public static boolean isBlank(CharSequence s) {
        if (s == null) {
            return true;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNull(String s) {
        return (s == null || s.trim().equals(Events.DEFAULT_SORT_ORDER)) ? true : s.trim().equals("null");
    }

    public static String splitDigit(String content) {
        if (content != null) {
            return Pattern.compile("[^0-9]").matcher(content).replaceAll(Events.DEFAULT_SORT_ORDER).trim();
        }
        return Events.DEFAULT_SORT_ORDER;
    }
}
