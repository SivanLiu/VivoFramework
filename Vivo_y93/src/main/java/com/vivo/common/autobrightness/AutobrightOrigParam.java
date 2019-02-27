package com.vivo.common.autobrightness;

import android.os.SystemProperties;
import android.util.Slog;
import com.vivo.common.provider.Calendar.CalendarsColumns;
import com.vivo.common.provider.Weather;
import java.io.FileInputStream;

public class AutobrightOrigParam {
    private static final String BOARD_VERSION = "/sys/devs_list/board_version";
    private static final String model = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, "unkown").toLowerCase();
    private static final int[][] morig_key_value_PD1610 = new int[][]{new int[]{0, 2}, new int[]{30, 38}, new int[]{Weather.WEATHERVERSION_ROM_2_5_1, 126}, new int[]{Weather.WEATHERVERSION_ROM_3_5, 255}, new int[]{StateInfo.STATE_FINGERPRINT_GOTO_SLEEP, 525}};
    private static final int[][] morig_key_value_PD1616 = new int[][]{new int[]{0, 2}, new int[]{30, 35}, new int[]{Weather.WEATHERVERSION_ROM_2_5_1, 116}, new int[]{4317, 255}, new int[]{StateInfo.STATE_FINGERPRINT_GOTO_SLEEP, 515}};
    private static final int[][] morig_key_value_PD1619 = new int[][]{new int[]{0, 2}, new int[]{30, 35}, new int[]{Weather.WEATHERVERSION_ROM_2_5_1, 130}, new int[]{Weather.WEATHERVERSION_ROM_3_5, 255}, new int[]{5000, CalendarsColumns.RESPOND_ACCESS}};
    private static final int[][] morig_key_value_PD1624 = new int[][]{new int[]{0, 2}, new int[]{30, 35}, new int[]{Weather.WEATHERVERSION_ROM_2_5_1, 106}, new int[]{4239, 255}, new int[]{StateInfo.STATE_FINGERPRINT_GOTO_SLEEP, 514}};
    private static final int[][] morig_key_value_PD1635 = new int[][]{new int[]{0, 2}, new int[]{30, 35}, new int[]{Weather.WEATHERVERSION_ROM_2_5_1, 112}, new int[]{4420, 255}, new int[]{5000, 281}};
    private static final int[][] morig_key_value_PD1705 = new int[][]{new int[]{0, 2}, new int[]{30, 35}, new int[]{Weather.WEATHERVERSION_ROM_2_5_1, 130}, new int[]{Weather.WEATHERVERSION_ROM_3_5, 255}, new int[]{5000, CalendarsColumns.RESPOND_ACCESS}};
    private static final int[][] morig_key_value_PD1708 = new int[][]{new int[]{0, 2}, new int[]{30, 32}, new int[]{Weather.WEATHERVERSION_ROM_2_5_1, 100}, new int[]{4500, 255}, new int[]{5000, 277}};
    private static final int[][] morig_key_value_PD1709 = new int[][]{new int[]{0, 2}, new int[]{30, 35}, new int[]{Weather.WEATHERVERSION_ROM_2_5_1, 112}, new int[]{4420, 255}, new int[]{5000, 281}};
    private static final int[][] morig_key_value_PD1710 = new int[][]{new int[]{0, 2}, new int[]{30, 35}, new int[]{Weather.WEATHERVERSION_ROM_2_5_1, 112}, new int[]{4420, 255}, new int[]{5000, 281}};
    private static final int[][] morig_key_value_PD1728 = new int[][]{new int[]{0, 2}, new int[]{30, 32}, new int[]{Weather.WEATHERVERSION_ROM_2_5_1, 111}, new int[]{4420, 255}, new int[]{5000, 281}};
    private static final int[][] morig_key_value_PD1730 = new int[][]{new int[]{0, 2}, new int[]{30, 32}, new int[]{Weather.WEATHERVERSION_ROM_2_5_1, 111}, new int[]{Weather.WEATHERVERSION_ROM_3_5, 255}, new int[]{5000, CalendarsColumns.RESPOND_ACCESS}};
    private static final int[][] morig_key_value_PD1731 = new int[][]{new int[]{0, 2}, new int[]{30, 32}, new int[]{Weather.WEATHERVERSION_ROM_2_5_1, 111}, new int[]{4420, 255}, new int[]{5000, 281}};
    private static final int[][] morig_key_value_PD1818 = new int[][]{new int[]{0, 2}, new int[]{30, 32}, new int[]{Weather.WEATHERVERSION_ROM_2_5_1, 111}, new int[]{3800, 255}, new int[]{5000, 281}};
    private static final double[][] morig_param_PD1610 = new double[][]{new double[]{1.17d, 2.8d}, new double[]{0.0447d, 36.659d}, new double[]{0.0645d, -3.0d}, new double[]{0.045d, 75.0d}};
    private static final double[][] morig_param_PD1616 = new double[][]{new double[]{1.1d, 2.0d}, new double[]{0.0411d, 33.8d}, new double[]{0.06d, -4.0d}, new double[]{0.045d, 60.735d}};
    private static final double[][] morig_param_PD1619 = new double[][]{new double[]{1.1d, 2.0d}, new double[]{0.0482d, 33.6d}, new double[]{0.0625d, 5.0d}, new double[]{0.045d, 75.0d}};
    private static final double[][] morig_param_PD1624 = new double[][]{new double[]{1.1d, 2.0d}, new double[]{0.0365d, 33.869d}, new double[]{0.065d, -24.065d}, new double[]{0.045d, 64.245d}};
    private static final double[][] morig_param_PD1635 = new double[][]{new double[]{1.1333d, 2.0d}, new double[]{0.0386d, 34.8d}, new double[]{0.059d, -6.0d}, new double[]{0.045d, 56.0d}};
    private static final double[][] morig_param_PD1705 = new double[][]{new double[]{1.1d, 2.0d}, new double[]{0.0482d, 33.6d}, new double[]{0.0625d, 5.0d}, new double[]{0.045d, 75.0d}};
    private static final double[][] morig_param_PD1708 = new double[][]{new double[]{1.0d, 2.0d}, new double[]{0.0345d, 31.0d}, new double[]{0.062d, -24.0d}, new double[]{0.045d, 52.5d}};
    private static final double[][] morig_param_PD1709 = new double[][]{new double[]{1.1333d, 2.0d}, new double[]{0.0386d, 34.8d}, new double[]{0.059d, -6.0d}, new double[]{0.045d, 56.0d}};
    private static final double[][] morig_param_PD1710 = new double[][]{new double[]{1.1333d, 2.0d}, new double[]{0.0386d, 34.8d}, new double[]{0.059d, -6.0d}, new double[]{0.045d, 56.0d}};
    private static final double[][] morig_param_PD1728 = new double[][]{new double[]{1.0d, 2.0d}, new double[]{0.0406d, 30.75d}, new double[]{0.0595d, -7.9d}, new double[]{0.045d, 56.0d}};
    private static final double[][] morig_param_PD1730 = new double[][]{new double[]{1.0d, 2.0d}, new double[]{0.0406d, 30.75d}, new double[]{0.0715d, -31.07d}, new double[]{0.045d, 75.0d}};
    private static final double[][] morig_param_PD1731 = new double[][]{new double[]{1.0d, 2.0d}, new double[]{0.0406d, 30.75d}, new double[]{0.0595d, -7.9d}, new double[]{0.045d, 56.0d}};
    private static final double[][] morig_param_PD1818 = new double[][]{new double[]{1.0d, 2.0d}, new double[]{0.0406d, 30.75d}, new double[]{0.08d, -49.0d}, new double[]{0.045d, 84.0d}};
    private static final double[][] morig_up_down_param_PD1610 = new double[][]{new double[]{2.0d, 9.0d}, new double[]{1.0d, 24.0d}, new double[]{1.25d, 15.0d}, new double[]{1.03d, 32.6d}, new double[]{0.333d, -1.33d}, new double[]{0.5d, -5.5d}, new double[]{0.84d, -17.4d}, new double[]{0.75d, -10.0d}, new double[]{0.29d, 45.2d}};
    private static final double[][] morig_up_down_param_PD1616 = new double[][]{new double[]{2.461d, -0.915d}, new double[]{1.1d, 19.5d}, new double[]{1.25d, 15.0d}, new double[]{1.03d, 32.6d}, new double[]{0.235d, 0.02d}, new double[]{0.6d, -9.0d}, new double[]{0.84d, -17.4d}, new double[]{0.75d, -10.0d}, new double[]{0.29d, 45.2d}};
    private static final double[][] morig_up_down_param_PD1619 = new double[][]{new double[]{2.0769d, -1.153d}, new double[]{1.4d, 9.0d}, new double[]{1.25d, 15.0d}, new double[]{1.03d, 32.6d}, new double[]{0.4211d, -0.5275d}, new double[]{0.5d, 2.5d}, new double[]{0.7778d, -12.223d}, new double[]{0.75d, -10.0d}, new double[]{0.29d, 45.2d}};
    private static final double[][] morig_up_down_param_PD1624 = new double[][]{new double[]{2.0d, 6.0d}, new double[]{1.1d, 19.5d}, new double[]{1.25d, 15.0d}, new double[]{1.03d, 32.6d}, new double[]{0.235d, 0.02d}, new double[]{0.6d, -9.0d}, new double[]{0.84d, -17.4d}, new double[]{0.75d, -10.0d}, new double[]{0.29d, 45.2d}};
    private static final double[][] morig_up_down_param_PD1635 = new double[][]{new double[]{2.0769d, -1.153d}, new double[]{0.75d, 18.75d}, new double[]{1.333d, -1.64d}, new double[]{1.034d, 22.24d}, new double[]{0.4211d, -0.5275d}, new double[]{0.5d, 2.5d}, new double[]{1.0d, -15.0d}, new double[]{0.375d, 35.0d}, new double[]{0.29d, 45.2d}};
    private static final double[][] morig_up_down_param_PD1705 = new double[][]{new double[]{2.0769d, -1.153d}, new double[]{1.4d, 9.0d}, new double[]{1.25d, 15.0d}, new double[]{1.03d, 32.6d}, new double[]{0.4211d, -0.5275d}, new double[]{0.5d, 2.5d}, new double[]{0.7778d, -12.223d}, new double[]{0.75d, -10.0d}, new double[]{0.29d, 45.2d}};
    private static final double[][] morig_up_down_param_PD1708 = new double[][]{new double[]{2.0769d, -1.153d}, new double[]{1.4d, 9.0d}, new double[]{1.25d, 15.0d}, new double[]{1.03d, 32.6d}, new double[]{0.4211d, -0.5275d}, new double[]{0.5d, 2.5d}, new double[]{0.7778d, -12.223d}, new double[]{0.75d, -10.0d}, new double[]{0.29d, 45.2d}};
    private static final double[][] morig_up_down_param_PD1709 = new double[][]{new double[]{2.0769d, -1.153d}, new double[]{0.75d, 18.75d}, new double[]{1.333d, -1.64d}, new double[]{1.034d, 22.24d}, new double[]{0.4211d, -0.5275d}, new double[]{0.5d, 2.5d}, new double[]{1.0d, -15.0d}, new double[]{0.375d, 35.0d}, new double[]{0.29d, 45.2d}};
    private static final double[][] morig_up_down_param_PD1710 = new double[][]{new double[]{2.0769d, -1.153d}, new double[]{0.75d, 18.75d}, new double[]{1.333d, -1.64d}, new double[]{1.034d, 22.24d}, new double[]{0.4211d, -0.5275d}, new double[]{0.5d, 2.5d}, new double[]{1.0d, -15.0d}, new double[]{0.375d, 35.0d}, new double[]{0.29d, 45.2d}};
    private static final double[][] morig_up_down_param_PD1730 = new double[][]{new double[]{2.0769d, -1.153d}, new double[]{1.4d, 9.0d}, new double[]{1.25d, 15.0d}, new double[]{1.03d, 32.6d}, new double[]{0.4211d, -0.5275d}, new double[]{0.5d, 2.5d}, new double[]{0.7778d, -12.223d}, new double[]{0.75d, -10.0d}, new double[]{0.29d, 45.2d}};
    private static final double[][] morig_up_down_param_PD1731 = new double[][]{new double[]{2.0769d, -1.153d}, new double[]{1.4d, 9.0d}, new double[]{1.25d, 15.0d}, new double[]{1.03d, 32.6d}, new double[]{0.4211d, -0.5275d}, new double[]{0.5d, 2.5d}, new double[]{0.7778d, -12.223d}, new double[]{0.75d, -10.0d}, new double[]{0.29d, 45.2d}};
    private static final double[][] morig_up_down_param_PD1806 = new double[][]{new double[]{2.6d, -0.2d}, new double[]{0.0d, 33.0d}, new double[]{0.65d, 22.25d}, new double[]{1.333d, -1.64d}, new double[]{1.034d, 22.24d}, new double[]{0.2d, 0.4d}, new double[]{1.0217d, -23.736d}, new double[]{0.55d, 14.0d}, new double[]{0.29d, 45.2d}};

    private static boolean isPD1732D() {
        try {
            FileInputStream mInputStream = new FileInputStream(BOARD_VERSION);
            byte[] buf = new byte[100];
            int len = mInputStream.read(buf);
            String board_version = new String(buf, 0, len);
            Slog.e("Sensor", "Light sensor board version: " + board_version + " len: " + len);
            char[] temp = board_version.toCharArray();
            mInputStream.close();
            if (temp[2] == '0') {
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public static void getAutoOrigParam(double[][] orig_param, int[][] orig_key_value, double[][] orig_up_down_param) {
        if (model.startsWith("pd1610") || model.startsWith("td1703")) {
            paramCopy(orig_param, morig_param_PD1610);
            paramCopy(orig_key_value, morig_key_value_PD1610);
            paramCopy(orig_up_down_param, morig_up_down_param_PD1610);
        } else if (model.startsWith("pd1616") || model.startsWith("std1616") || model.startsWith("td1608") || model.startsWith("spd1706")) {
            paramCopy(orig_param, morig_param_PD1616);
            paramCopy(orig_key_value, morig_key_value_PD1616);
            paramCopy(orig_up_down_param, morig_up_down_param_PD1616);
        } else if (model.startsWith("pd1624") || model.startsWith("pd1621b") || model.startsWith("vtd1703f_ex") || model.startsWith("vtd1704f_ex")) {
            paramCopy(orig_param, morig_param_PD1624);
            paramCopy(orig_key_value, morig_key_value_PD1624);
            paramCopy(orig_up_down_param, morig_up_down_param_PD1624);
        } else if (model.startsWith("pd1619")) {
            paramCopy(orig_param, morig_param_PD1619);
            paramCopy(orig_key_value, morig_key_value_PD1619);
            paramCopy(orig_up_down_param, morig_up_down_param_PD1619);
        } else if (model.startsWith("pd1635")) {
            paramCopy(orig_param, morig_param_PD1635);
            paramCopy(orig_key_value, morig_key_value_PD1635);
            paramCopy(orig_up_down_param, morig_up_down_param_PD1635);
        } else if (model.startsWith("pd1708") || model.startsWith("td1702") || model.startsWith("pd1718") || model.startsWith("pd1803") || model.startsWith("pd1732")) {
            paramCopy(orig_param, morig_param_PD1708);
            paramCopy(orig_key_value, morig_key_value_PD1708);
            paramCopy(orig_up_down_param, morig_up_down_param_PD1708);
            if (!model.startsWith("pd1732")) {
                return;
            }
            if (model.startsWith("pd1732c") || isPD1732D()) {
                paramCopy(orig_param, morig_param_PD1731);
                paramCopy(orig_key_value, morig_key_value_PD1731);
                paramCopy(orig_up_down_param, morig_up_down_param_PD1731);
            }
        } else if (model.startsWith("pd1709") || model.startsWith("pd1724") || model.startsWith("td1705")) {
            paramCopy(orig_param, morig_param_PD1709);
            paramCopy(orig_key_value, morig_key_value_PD1709);
            paramCopy(orig_up_down_param, morig_up_down_param_PD1709);
        } else if (model.startsWith("pd1710") || model.startsWith("pd1721") || model.startsWith("vtd1702") || model.startsWith("td1704")) {
            paramCopy(orig_param, morig_param_PD1710);
            paramCopy(orig_key_value, morig_key_value_PD1710);
            paramCopy(orig_up_down_param, morig_up_down_param_PD1710);
        } else if (model.startsWith("pd1728") || model.startsWith("pd1729") || model.startsWith("pd1801") || model.startsWith("pd1813")) {
            paramCopy(orig_param, morig_param_PD1728);
            paramCopy(orig_key_value, morig_key_value_PD1728);
            paramCopy(orig_up_down_param, morig_up_down_param_PD1710);
        } else if (model.startsWith("pd1705")) {
            paramCopy(orig_param, morig_param_PD1705);
            paramCopy(orig_key_value, morig_key_value_PD1705);
            paramCopy(orig_up_down_param, morig_up_down_param_PD1705);
        } else if (model.startsWith("pd1730")) {
            paramCopy(orig_param, morig_param_PD1730);
            paramCopy(orig_key_value, morig_key_value_PD1730);
            paramCopy(orig_up_down_param, morig_up_down_param_PD1730);
        } else if (model.startsWith("pd1731")) {
            paramCopy(orig_param, morig_param_PD1731);
            paramCopy(orig_key_value, morig_key_value_PD1731);
            paramCopy(orig_up_down_param, morig_up_down_param_PD1731);
        } else if (model.startsWith("pd1805") || model.startsWith("pd1806") || model.startsWith("pd1809") || model.startsWith("pd1814") || model.startsWith("pd1816")) {
            paramCopy(orig_param, morig_param_PD1728);
            paramCopy(orig_key_value, morig_key_value_PD1728);
            paramCopy(orig_up_down_param, morig_up_down_param_PD1806);
        } else if (model.startsWith("td1803") || model.startsWith("pd1818")) {
            paramCopy(orig_param, morig_param_PD1818);
            paramCopy(orig_key_value, morig_key_value_PD1818);
            paramCopy(orig_up_down_param, morig_up_down_param_PD1731);
        } else {
            paramCopy(orig_param, morig_param_PD1610);
            paramCopy(orig_key_value, morig_key_value_PD1610);
            paramCopy(orig_up_down_param, morig_up_down_param_PD1610);
        }
    }

    private static void paramCopy(double[][] purpose_param, double[][] orig_param) {
        int m = orig_param[0].length;
        int n = orig_param.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                purpose_param[i][j] = orig_param[i][j];
            }
        }
    }

    private static void paramCopy(int[][] purpose_param, int[][] orig_param) {
        int m = orig_param[0].length;
        int n = orig_param.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                purpose_param[i][j] = orig_param[i][j];
            }
        }
    }
}
