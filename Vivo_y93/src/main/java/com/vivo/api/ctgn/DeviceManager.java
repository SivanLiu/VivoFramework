package com.vivo.api.ctgn;

import android.content.ComponentName;
import com.chinatelecom.security.emm.DeviceControl;
import com.vivo.services.cust.VivoCustomManager;

public class DeviceManager implements DeviceControl {
    private VivoCustomManager custManager;

    public DeviceManager() {
        this.custManager = null;
        this.custManager = new VivoCustomManager();
    }

    public String getAPIVersion(ComponentName admin) throws SecurityException {
        return this.custManager.getAPIVersion();
    }

    public boolean isDeviceRoot(ComponentName admin) throws SecurityException {
        return this.custManager.isDeviceRoot();
    }

    public void deviceShutDown(ComponentName admin) throws SecurityException {
        this.custManager.shutDown();
    }

    public void deviceReboot(ComponentName admin) throws SecurityException {
        this.custManager.reBoot();
    }

    public void setDeviceUpdatePolicy(ComponentName admin, Integer UpdateFlag) throws SecurityException {
    }

    public Integer getDeviceUpdatePolicy(ComponentName admin) throws SecurityException {
        return Integer.valueOf(-1);
    }

    public void setSDCardFormatted(ComponentName admin) throws SecurityException {
        this.custManager.formatSDCard();
    }

    public String getRomVersion(ComponentName admin) throws SecurityException {
        return this.custManager.getRomVersion();
    }
}
