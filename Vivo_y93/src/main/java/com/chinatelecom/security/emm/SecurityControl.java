package com.chinatelecom.security.emm;

import android.content.ComponentName;
import com.chinatelecom.security.emm.exception.IllegalParamaterException;

public interface SecurityControl {
    public static final String TAG = "SecurityControl";

    boolean getDeviceManagerEnable(ComponentName componentName) throws SecurityException, IllegalParamaterException;

    boolean getDeviceManagerUnintall(ComponentName componentName) throws SecurityException, IllegalParamaterException;

    void setDeviceManagerEnable(ComponentName componentName, boolean z) throws SecurityException, IllegalParamaterException;

    void setDeviceManagerUnintall(ComponentName componentName, boolean z) throws SecurityException, IllegalParamaterException;
}
