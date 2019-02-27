package com.vivo.services.vivodevice;

import com.vivo.common.provider.Calendar.Events;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VivoDeviceProxy {
    private static final String TAG = "VivoDeviceProxy";
    private static VivoDeviceProxy mInstance;
    private Map<String, VivoDevice> deviceMap = new HashMap();

    private VivoDeviceProxy() {
        init();
    }

    private void init() {
        putDeviceProperty(VivoDeviceNative.ULTRAVIOLET, VivoDeviceNative.ULTRAVIOLET, Events.DEFAULT_SORT_ORDER, "setUltravioletEnable", "getUltravioletEnable", Events.DEFAULT_SORT_ORDER, true);
        putDeviceProperty(VivoDeviceNative.CPU, VivoDeviceNative.CPU_FREQUENCY, Events.DEFAULT_SORT_ORDER, "setCpuFreq", "getCpuFreq", Events.DEFAULT_SORT_ORDER, true);
        putDeviceProperty(VivoDeviceNative.LCDBACKLIGHT, VivoDeviceNative.LCDBACKLIGHT_BRIGHTNESS, Events.DEFAULT_SORT_ORDER, "setLcdbacklightBrightness", "getLcdbacklightBrightness", Events.DEFAULT_SORT_ORDER, true);
        putDeviceProperty(VivoDeviceNative.TOUCHSCREEN, VivoDeviceNative.TOUCHSCREEN_COLOR, Events.DEFAULT_SORT_ORDER, Events.DEFAULT_SORT_ORDER, "getTouchscreenColor", Events.DEFAULT_SORT_ORDER, false);
    }

    public static VivoDeviceProxy getInstance() {
        if (mInstance == null) {
            mInstance = new VivoDeviceProxy();
        }
        return mInstance;
    }

    public boolean putDeviceProperty(String devName, String propName, String propValue, String set, String get, String dataPoll, boolean reloadEachTime) {
        VivoDevice vd = (VivoDevice) this.deviceMap.get(devName);
        if (vd == null) {
            vd = new VivoDevice();
            this.deviceMap.put(devName, vd);
        }
        vd.setPropertyByName(propName, new PropertyData(propValue, set, get, dataPoll, reloadEachTime));
        return true;
    }

    public VivoDevice getDeviceByName(String name) {
        return (VivoDevice) this.deviceMap.get(name);
    }

    public VivoDeviceProxy setDeviceByName(String name, VivoDevice device) {
        if (this.deviceMap.get(name) == null) {
            this.deviceMap.put(name, device);
            return this;
        }
        throw new RuntimeException("The device named " + name + " has been existed.");
    }

    public Set<String> getDeviceNameSet() {
        return this.deviceMap.keySet();
    }
}
