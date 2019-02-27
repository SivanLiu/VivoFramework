package com.vivo.api.ctgn;

import android.content.ComponentName;
import android.os.Bundle;
import com.chinatelecom.security.emm.ApplicationControl;
import com.chinatelecom.security.emm.exception.IllegalParamaterException;
import com.vivo.services.cust.VivoCustomManager;
import java.util.List;

public class ApplicationManager implements ApplicationControl {
    private VivoCustomManager custManager;

    public ApplicationManager() {
        this.custManager = null;
        this.custManager = new VivoCustomManager();
    }

    public boolean isTrustedAppStoreEnabled(ComponentName admin) throws SecurityException {
        return this.custManager.isTrustedAppStoreEnabled();
    }

    public void enableTrustedAppStore(ComponentName admin, boolean setting) throws SecurityException {
        this.custManager.setTrustedAppStoreState(setting ? 1 : 0);
    }

    public void addTrustedAppStore(ComponentName admin, String packageName) throws SecurityException, IllegalParamaterException {
        try {
            this.custManager.addTrustedAppStore(packageName);
        } catch (IllegalArgumentException e) {
            throw new IllegalParamaterException("addTrustedAppStore:IllegalParamaterException occur!");
        }
    }

    public void deleteTrustedAppStore(ComponentName admin, String packageName) throws SecurityException, IllegalParamaterException {
        try {
            this.custManager.deleteTrustedAppStore(packageName);
        } catch (IllegalArgumentException e) {
            throw new IllegalParamaterException("deleteTrustedAppStore:IllegalParamaterException occur!");
        }
    }

    public List<String> getTrustedAppStore(ComponentName admin) throws SecurityException {
        return this.custManager.getTrustedAppStore();
    }

    public void installPackage(ComponentName admin, String packagePath, String packageName, boolean isOnSDCard) throws SecurityException, IllegalParamaterException {
        int installFlags;
        if (isOnSDCard) {
            installFlags = 8;
        } else {
            installFlags = 16;
        }
        try {
            this.custManager.installPackage(packagePath, ((installFlags | 131072) | 2) | 128, admin.getPackageName());
        } catch (IllegalArgumentException e) {
            throw new IllegalParamaterException("installPackage:IllegalParamaterException occur!");
        }
    }

    public void uninstallPackage(ComponentName admin, String packageName, boolean isRetainAppData) throws SecurityException, IllegalParamaterException {
        int flags = 2;
        if (isRetainAppData) {
            flags = 3;
        }
        try {
            this.custManager.deletePackage(packageName, flags);
        } catch (IllegalArgumentException e) {
            throw new IllegalParamaterException("uninstallPackage:IllegalParamaterException occur!");
        }
    }

    public void setApplicationSettings(ComponentName admin, String busi, Bundle setting) throws SecurityException, IllegalParamaterException {
        try {
            if ("BUSI_APPLICATION".equals(busi) && !setting.containsKey("CLEARCACHE") && !setting.containsKey("RUNNING") && !setting.containsKey("STOP")) {
                boolean containsKey = setting.containsKey("UNINSTALL");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalParamaterException("setApplicationSettings:IllegalParamaterException occur!");
        }
    }

    public Bundle getApplicationSettings(ComponentName admin, String busi, String setting) throws SecurityException, IllegalParamaterException {
        Bundle mBundle = new Bundle();
        if (!(!"BUSI_APPLICATION".equals(busi) || setting.equals("CLEARCACHE") || setting.equals("RUNNING") || setting.equals("STOP"))) {
            boolean equals = setting.equals("UNINSTALL");
        }
        return mBundle;
    }
}
