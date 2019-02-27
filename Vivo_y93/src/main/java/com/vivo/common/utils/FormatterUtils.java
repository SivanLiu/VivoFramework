package com.vivo.common.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.BidiFormatter;
import android.text.TextUtils;
import android.text.format.Formatter.BytesResult;
import com.vivo.common.provider.Calendar.Events;

public class FormatterUtils {
    public static final int FLAG_CALCULATE_ROUNDED = 2;
    public static final int FLAG_SHORTER = 1;

    public static String formatFileSize(Context context, long sizeBytes) {
        if (context == null) {
            return Events.DEFAULT_SORT_ORDER;
        }
        BytesResult res = formatBytes(context.getResources(), sizeBytes, 0);
        return bidiWrap(context, context.getString(51249589, new Object[]{res.value, res.units}));
    }

    private static BytesResult formatBytes(Resources res, long sizeBytes, int flags) {
        int roundFactor;
        String roundFormat;
        long roundedBytes;
        boolean isNegative = sizeBytes < 0;
        if (isNegative) {
            sizeBytes = -sizeBytes;
        }
        float result = (float) sizeBytes;
        int suffix = 51249583;
        long mult = 1;
        if (result > 900.0f) {
            suffix = 51249584;
            mult = 1024;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = 51249585;
            mult = FileUtils.LOW_STORAGE_THRESHOLD;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = 51249586;
            mult = 1073741824;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = 51249587;
            mult = 1099511627776L;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = 51249588;
            mult = 1125899906842624L;
            result /= 1024.0f;
        }
        if (mult == 1 || result >= 100.0f) {
            roundFactor = 1;
            roundFormat = "%.0f";
        } else if (result < 1.0f) {
            roundFactor = 100;
            roundFormat = "%.2f";
        } else if (result < 10.0f) {
            if ((flags & 1) != 0) {
                roundFactor = 10;
                roundFormat = "%.1f";
            } else {
                roundFactor = 100;
                roundFormat = "%.2f";
            }
        } else if ((flags & 1) != 0) {
            roundFactor = 1;
            roundFormat = "%.0f";
        } else {
            roundFactor = 100;
            roundFormat = "%.2f";
        }
        if (isNegative) {
            result = -result;
        }
        String roundedString = String.format(roundFormat, new Object[]{Float.valueOf(result)});
        if ((flags & 2) == 0) {
            roundedBytes = 0;
        } else {
            roundedBytes = (((long) Math.round(((float) roundFactor) * result)) * mult) / ((long) roundFactor);
        }
        return new BytesResult(roundedString, res.getString(suffix), roundedBytes);
    }

    private static String bidiWrap(Context context, String source) {
        if (TextUtils.getLayoutDirectionFromLocale(context.getResources().getConfiguration().locale) == 1) {
            return BidiFormatter.getInstance(true).unicodeWrap(source);
        }
        return source;
    }
}
