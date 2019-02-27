package com.vivo.services;

public class DeviceParaProvideService {
    private static int[] als_para = new int[16];
    private static int[] ps_para = new int[16];
    private static int[] tmp_ps_para = new int[16];

    private static native void device_get_als_para(int[] iArr, int i);

    private static native void device_get_ps_para(int[] iArr, int[] iArr2, int i);

    private static native String dump_para_info();

    private static native String jniReadKernelData(String str);

    private static native boolean jniWriteKernelCommand(String str, String str2);

    public DeviceParaProvideService() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
    }

    public int getPsBaseValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return ps_para[0];
    }

    public int getPsBaseMinValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return ps_para[1];
    }

    public int getTmpPsBaseMinValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return tmp_ps_para[1];
    }

    public int getPsBaseMaxValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return ps_para[2];
    }

    public int getTmpPsBaseMaxValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return tmp_ps_para[2];
    }

    public int getAlsBaseMinValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return als_para[1];
    }

    public int getAlsBaseMaxValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return als_para[2];
    }

    public int getPsOneStepMinValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return tmp_ps_para[4];
    }

    public int getPsOneStepMaxValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return tmp_ps_para[5];
    }

    public int getPsOneStepCloseValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return tmp_ps_para[6];
    }

    public int getPsOneStepAwayValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return tmp_ps_para[7];
    }

    public int getPsSecStepMinValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return tmp_ps_para[8];
    }

    public int getPsSecStepMaxValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return tmp_ps_para[9];
    }

    public int getPsSecStepCloseValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return tmp_ps_para[10];
    }

    public int getPsSecStepAwayValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return tmp_ps_para[11];
    }

    public int getPsThrStepMinValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return tmp_ps_para[12];
    }

    public int getPsThrStepMaxValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return tmp_ps_para[13];
    }

    public int getPsThrStepCloseValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return tmp_ps_para[14];
    }

    public int getPsThrStepAwayValue() {
        device_get_ps_para(ps_para, tmp_ps_para, 16);
        device_get_als_para(als_para, 16);
        return tmp_ps_para[15];
    }

    public String dumpParaInfo() {
        return dump_para_info();
    }

    public static boolean writeKernelCommand(String path, String val) {
        return jniWriteKernelCommand(path, val);
    }

    public static String readKernelData(String path) {
        return jniReadKernelData(path);
    }
}
