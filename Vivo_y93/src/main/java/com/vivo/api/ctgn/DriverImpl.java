package com.vivo.api.ctgn;

import android.content.Context;
import com.chinatelecom.security.emm.APNControl;
import com.chinatelecom.security.emm.ApplicationControl;
import com.chinatelecom.security.emm.CommControl;
import com.chinatelecom.security.emm.DeviceControl;
import com.chinatelecom.security.emm.Driver;
import com.chinatelecom.security.emm.PerpheralControl;
import com.chinatelecom.security.emm.SecurityControl;

public class DriverImpl implements Driver {
    private APNControl mAPNControl;
    private ApplicationControl mApplicationControl;
    private CommControl mCommControl;
    private DeviceControl mDeviceControl;
    private PerpheralControl mPerpheralControl;
    private SecurityControl mSecurityControl;

    public DeviceControl getDeviceControl(Context p0) {
        if (this.mDeviceControl != null) {
            return this.mDeviceControl;
        }
        this.mDeviceControl = new DeviceManager();
        return this.mDeviceControl;
    }

    public CommControl getCommControl(Context p0) {
        if (this.mCommControl != null) {
            return this.mCommControl;
        }
        this.mCommControl = new CommManager();
        return this.mCommControl;
    }

    public APNControl getAPNControl(Context p0) {
        if (this.mAPNControl != null) {
            return this.mAPNControl;
        }
        this.mAPNControl = new APNManager();
        return this.mAPNControl;
    }

    public ApplicationControl getApplicationControl(Context p0) {
        if (this.mApplicationControl != null) {
            return this.mApplicationControl;
        }
        this.mApplicationControl = new ApplicationManager();
        return this.mApplicationControl;
    }

    public SecurityControl getSecurityControl(Context p0) {
        if (this.mSecurityControl != null) {
            return this.mSecurityControl;
        }
        this.mSecurityControl = new SecurityManager();
        return this.mSecurityControl;
    }

    public PerpheralControl getPerpheralControl(Context p0) {
        if (this.mPerpheralControl != null) {
            return this.mPerpheralControl;
        }
        this.mPerpheralControl = new PerpheralManager();
        return this.mPerpheralControl;
    }
}
