package com.vivo.api.fjwj;

import android.content.ComponentName;
import com.vivo.services.cust.VivoCustomManager;

public class FjwjAgent {
    private static final String TAG = "FJWJ";
    private static final boolean system = true;
    private VivoCustomManager custManager;

    public FjwjAgent() {
        this.custManager = null;
        this.custManager = new VivoCustomManager();
    }

    public boolean setDeviceOwner(ComponentName who) {
        try {
            return this.custManager.setDeviceOwner(who);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    public void setFaceLockDisabled(boolean disabled) {
        try {
            this.custManager.setFaceWakeState(disabled ? 0 : 1);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    public void setLanguageChangeDisabled(boolean disabled) {
        try {
            this.custManager.setLanguageChangeDisabled(disabled);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    public void setDeveloperOptionsDisabled(boolean disabled) {
        try {
            this.custManager.setDeveloperOptionsDisabled(disabled);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }
}
