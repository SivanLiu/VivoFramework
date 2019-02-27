package com.vivo.common.autobrightness;

import android.util.Slog;
import java.util.Arrays;

public class StatisticsUtil {
    private static final String TAG = "StatisticsUtil";

    private static void log(String msg) {
        if (AblConfig.isDebug()) {
            Slog.d(TAG, msg);
        }
    }

    public static int max(int[] array) {
        if (array != null) {
            return max(array, array.length);
        }
        log("max: array is null");
        return 0;
    }

    public static int max(int[] array, int len) {
        if (array == null) {
            log("max: array is null,len=" + len);
            return 0;
        }
        if (array.length < len || len < 1) {
            log("max: array.length=" + array.length + " len=" + len);
            len = array.length;
        }
        int ret = array[0];
        for (int i = 0; i < len; i++) {
            ret = Math.max(ret, array[i]);
        }
        return ret;
    }

    public static int min(int[] array) {
        if (array != null) {
            return min(array, array.length);
        }
        log("min: array is null");
        return 0;
    }

    public static int min(int[] array, int len) {
        if (array == null) {
            log("min: array is null,len=" + len);
            return 0;
        }
        if (array.length < len || len < 1) {
            log("min: array.length=" + array.length + " len=" + len);
            len = array.length;
        }
        int ret = array[0];
        for (int i = 0; i < len; i++) {
            ret = Math.min(ret, array[i]);
        }
        return ret;
    }

    public static double average(int[] array) {
        if (array != null) {
            return average(array, array.length);
        }
        log("average: array is null");
        return 0.0d;
    }

    public static double average(int[] array, int len) {
        double sum = 0.0d;
        double count = (double) len;
        if (array == null) {
            log("average: array is null, len=" + len);
            return 0.0d;
        }
        if (array.length < len) {
            log("average: array.length=" + array.length + " len=" + len);
            len = array.length;
        }
        for (int i = 0; i < len; i++) {
            sum += (double) array[i];
        }
        return sum / count;
    }

    public static int median(int[] array) {
        if (array != null) {
            return median(array, array.length);
        }
        log("median: array is null.");
        return 0;
    }

    public static double variance(int[] array) {
        if (array != null) {
            return variance(array, array.length);
        }
        log("variance: array is null.");
        return 0.0d;
    }

    public static double variance(int[] array, int len) {
        if (array == null) {
            log("variance: array is null,len=" + len);
            return 0.0d;
        } else if (array.length < 1) {
            log("variance: array.length=" + array.length + " len=" + len);
            return 0.0d;
        } else {
            if (len > array.length) {
                len = array.length;
            }
            return variance(array, len, average(array, len));
        }
    }

    public static double variance(int[] array, int len, double average) {
        if (array == null) {
            log("variance: array is null, len=" + len + " average=" + average);
            return 0.0d;
        } else if (array.length < 1) {
            log("variance: array.length=" + array.length + " len=" + len + " average=" + average);
            return 0.0d;
        } else {
            if (len > array.length) {
                log("variance: array.length" + array.length + " len=" + len + " average=" + average);
                len = array.length;
            }
            double var = 0.0d;
            double count = (double) len;
            for (int i = 0; i < len; i++) {
                var += Math.pow(((double) array[i]) - average, 2.0d);
            }
            return var / count;
        }
    }

    public static double standardDeviation(int[] array) {
        if (array != null) {
            return Math.sqrt(variance(array));
        }
        log("standardDeviation: array is null.");
        return 0.0d;
    }

    public static double standardDeviation(int[] array, int len) {
        if (array != null) {
            return Math.sqrt(variance(array, len));
        }
        log("standardDeviation: array is null. len=" + len);
        return 0.0d;
    }

    public static double standardDeviation(int[] array, int len, double avg) {
        if (array != null) {
            return Math.sqrt(variance(array, len, avg));
        }
        log("standardDeviation: array is null. len=" + len + " avg=" + avg);
        return 0.0d;
    }

    public static int median(int[] array, int len) {
        if (array == null) {
            log("median array is null");
            return 0;
        } else if (array.length < 1) {
            log("median array.length is " + array.length);
            return 0;
        } else {
            double median;
            if (len > array.length) {
                len = array.length;
            }
            int[] dest = new int[len];
            for (int i = 0; i < len; i++) {
                dest[i] = array[i];
            }
            Arrays.sort(dest);
            double pos1 = Math.floor((((double) len) - 1.0d) / 2.0d);
            double pos2 = Math.ceil((((double) len) - 1.0d) / 2.0d);
            if (pos1 == pos2) {
                median = (double) dest[(int) pos1];
            } else {
                median = ((double) (dest[(int) pos1] + dest[(int) pos2])) / 2.0d;
            }
            return (int) median;
        }
    }

    public static int countBiggerEqualThen(int[] array, double threshold) {
        int count = 0;
        for (int x : array) {
            if (((double) x) >= threshold) {
                count++;
            }
        }
        return count;
    }
}
