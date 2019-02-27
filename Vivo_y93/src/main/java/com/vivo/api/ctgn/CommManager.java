package com.vivo.api.ctgn;

import android.content.ComponentName;
import android.os.Bundle;
import com.chinatelecom.security.emm.CommControl;
import com.chinatelecom.security.emm.exception.IllegalParamaterException;
import com.vivo.services.cust.VivoCustomManager;

public class CommManager implements CommControl {
    private VivoCustomManager custManager;

    public CommManager() {
        this.custManager = null;
        this.custManager = new VivoCustomManager();
    }

    public void setMobileSettings(ComponentName admin, String busi, Bundle setting) throws SecurityException, IllegalParamaterException {
        try {
            this.custManager.setMobileSettings(admin, busi, setting);
        } catch (IllegalArgumentException e) {
            throw new IllegalParamaterException("setMobileSettings:IllegalParamaterException occur!");
        }
    }

    public Bundle getMobileSettings(ComponentName admin, String busi, String setting) throws SecurityException {
        return this.custManager.getMobileSettings(admin, busi, setting);
    }
}
