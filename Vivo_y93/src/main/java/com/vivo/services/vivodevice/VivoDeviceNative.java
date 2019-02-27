package com.vivo.services.vivodevice;

import com.vivo.common.provider.Calendar.Events;

public class VivoDeviceNative {
    public static final String ACCELEROMETER = "accelerometer";
    public static final String CPU = "cpu";
    public static final String CPU_FREQUENCY = "cpu_frequency";
    public static final String GEOMAGNETIC = "geomagnetic";
    public static final String GPIO_KEYS = "gpio-keys";
    public static final String HANDSET = "7k_handset";
    public static final String LCDBACKLIGHT = "lcd-backlight";
    public static final String LCDBACKLIGHT_BRIGHTNESS = "lcdbacklight_brightness";
    public static final String LIGHT = "light";
    public static final String ORIENTATION = "orientation";
    public static final String PROXIMITY = "proximity";
    public static final String TOUCHSCREEN = "AT42QT602240/ATMXT224 Touchscreen";
    public static final String TOUCHSCREEN_COLOR = "touchscreen_color";
    public static final String ULTRAVIOLET = "ultraviolet";
    public static final String ULTRAVIOLET_ENABLE = "ultraviolet_enable";
    private static VivoDeviceNative deviceNative;
    private String accelerometer_inputorder = Events.DEFAULT_SORT_ORDER;
    private String geomagnetic_inputorder = Events.DEFAULT_SORT_ORDER;
    private String gpio_keys_inputorder = Events.DEFAULT_SORT_ORDER;
    private String handset_inputorder = Events.DEFAULT_SORT_ORDER;
    private String light_inputorder = Events.DEFAULT_SORT_ORDER;
    private String orientation_inputorder = Events.DEFAULT_SORT_ORDER;
    private String proximity_inputorder = Events.DEFAULT_SORT_ORDER;
    private String touchscreen_inputorder = Events.DEFAULT_SORT_ORDER;
    private String ultraviolet_inputorder = Events.DEFAULT_SORT_ORDER;

    public native void fileSystemSync();

    public native String getCpuFreq();

    public native String getLcdbacklightBrightness();

    public native String getTouchscreenColor();

    public native String getUltravioletEnable();

    public native void nativeInit();

    public native void nativeTest();

    public native boolean setCpuFreq(String str);

    public native boolean setLcdbacklightBrightness(String str);

    public native boolean setUltravioletEnable(String str);

    private VivoDeviceNative() {
        init();
    }

    public static VivoDeviceNative getDeviceNative() {
        if (deviceNative == null) {
            deviceNative = new VivoDeviceNative();
        }
        return deviceNative;
    }

    private void init() {
        nativeInit();
        test();
    }

    public void test() {
        nativeTest();
        System.out.println("^^^^^^^^^^^[" + this.gpio_keys_inputorder + "]^^^^^^^^^^^^^\n");
        System.out.println("^^^^^^^^^^^[" + this.touchscreen_inputorder + "]^^^^^^^^^^^^^\n");
        System.out.println("^^^^^^^^^^^[" + this.geomagnetic_inputorder + "]^^^^^^^^^^^^^\n");
        System.out.println("^^^^^^^^^^^[" + this.orientation_inputorder + "]^^^^^^^^^^^^^\n");
        System.out.println("^^^^^^^^^^^[" + this.accelerometer_inputorder + "]^^^^^^^^^^^^^\n");
        System.out.println("^^^^^^^^^^^[" + this.proximity_inputorder + "]^^^^^^^^^^^^^\n");
        System.out.println("^^^^^^^^^^^[" + this.light_inputorder + "]^^^^^^^^^^^^^\n");
        System.out.println("^^^^^^^^^^^[" + this.ultraviolet_inputorder + "]^^^^^^^^^^^^^\n");
        System.out.println("^^^^^^^^^^^[" + this.handset_inputorder + "]^^^^^^^^^^^^^\n");
    }

    public static boolean addKernelProperty(String device, String property, String value) {
        return VivoDeviceProxy.getInstance().putDeviceProperty(device, property, value, Events.DEFAULT_SORT_ORDER, Events.DEFAULT_SORT_ORDER, Events.DEFAULT_SORT_ORDER, false);
    }
}
