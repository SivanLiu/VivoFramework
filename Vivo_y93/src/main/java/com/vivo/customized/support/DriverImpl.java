package com.vivo.customized.support;

import com.vivo.customized.support.inter.VivoApplicationControl;
import com.vivo.customized.support.inter.VivoCustomDriver;
import com.vivo.customized.support.inter.VivoDeviceControl;
import com.vivo.customized.support.inter.VivoDeviceInfoControl;
import com.vivo.customized.support.inter.VivoNetworkControl;
import com.vivo.customized.support.inter.VivoOperationControl;
import com.vivo.customized.support.inter.VivoTelecomControl;

public class DriverImpl implements VivoCustomDriver {
    private VivoApplicationControl vac = null;
    private VivoDeviceControl vdc = null;
    private VivoDeviceInfoControl vdi = null;
    private VivoNetworkControl vnc = null;
    private VivoOperationControl voc = null;
    private VivoTelecomControl vtc = null;

    public VivoApplicationControl getApplicationManager() {
        if (this.vac == null) {
            this.vac = new ApplicationManager();
        }
        return this.vac;
    }

    public VivoDeviceControl getDeviceManager() {
        if (this.vdc == null) {
            this.vdc = new DeviceManager();
        }
        return this.vdc;
    }

    public VivoDeviceInfoControl getDeviceInfoManager() {
        if (this.vdi == null) {
            this.vdi = new DeviceInfoManager();
        }
        return this.vdi;
    }

    public VivoOperationControl getOperationManager() {
        if (this.voc == null) {
            this.voc = new OperationManager();
        }
        return this.voc;
    }

    public VivoNetworkControl getNetworkManager() {
        if (this.vnc == null) {
            this.vnc = new NetworkManager();
        }
        return this.vnc;
    }

    public VivoTelecomControl getTelecomManager() {
        if (this.vtc == null) {
            this.vtc = new TelecomManager();
        }
        return this.vtc;
    }
}
