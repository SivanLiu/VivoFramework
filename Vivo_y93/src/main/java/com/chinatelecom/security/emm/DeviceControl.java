package com.chinatelecom.security.emm;

import android.content.ComponentName;

public interface DeviceControl {
    public static final String TAG = "DeviceControl";
    public static final String VERSION_INFO = "3.1.4";

    void deviceReboot(ComponentName componentName) throws SecurityException;

    void deviceShutDown(ComponentName componentName) throws SecurityException;

    String getAPIVersion(ComponentName componentName) throws SecurityException;

    Integer getDeviceUpdatePolicy(ComponentName componentName) throws SecurityException;

    boolean isDeviceRoot(ComponentName componentName) throws SecurityException;

    void setDeviceUpdatePolicy(ComponentName componentName, Integer num) throws SecurityException;

    void setSDCardFormatted(ComponentName componentName) throws SecurityException;
}
