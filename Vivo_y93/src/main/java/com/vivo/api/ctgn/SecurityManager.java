package com.vivo.api.ctgn;

import android.content.ComponentName;
import android.util.Log;
import com.chinatelecom.security.emm.SecurityControl;
import com.chinatelecom.security.emm.exception.IllegalParamaterException;
import com.vivo.services.cust.VivoCustomManager;
import java.util.ArrayList;
import java.util.List;

public class SecurityManager implements SecurityControl {
    private VivoCustomManager custManager;

    public SecurityManager() {
        this.custManager = null;
        this.custManager = new VivoCustomManager();
    }

    public void setDeviceManagerEnable(ComponentName admin, boolean isActive) throws SecurityException, IllegalParamaterException {
        int i = 1;
        if (admin == null) {
            throw new IllegalParamaterException("setDeviceManagerEnable : IllegalParamaterException occur! admin is null");
        }
        try {
            int i2;
            this.custManager.setDevicePolicyManager(admin, isActive);
            VivoCustomManager vivoCustomManager = this.custManager;
            if (isActive) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            vivoCustomManager.setDevicePolicyManagerUIState(i2);
            this.custManager.setAccessibilityServcie(new ComponentName(admin.getPackageName(), "com.zdk.mg.agent.admin.sensitive.MGAccessibilityService"), isActive);
            VivoCustomManager vivoCustomManager2 = this.custManager;
            if (!isActive) {
                i = 0;
            }
            vivoCustomManager2.setAccessibilityServcieUIState(i);
            if (isActive) {
                this.custManager.setDeviceOwner(admin);
            } else {
                this.custManager.clearDeviceOwner(admin.getPackageName());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalParamaterException("setDeviceManagerEnable:IllegalParamaterException occur!");
        }
    }

    public boolean getDeviceManagerEnable(ComponentName admin) throws SecurityException, IllegalParamaterException {
        if (admin == null) {
            throw new IllegalParamaterException("getDeviceManagerEnable : IllegalParamaterException occur! admin is null");
        }
        try {
            return this.custManager.isDevicePolicyManagerEnable(admin);
        } catch (IllegalArgumentException e) {
            throw new IllegalParamaterException("getDeviceManagerEnable:IllegalParamaterException occur!");
        }
    }

    public void setDeviceManagerUnintall(ComponentName admin, boolean isUninstall) throws SecurityException, IllegalParamaterException {
        if (admin == null) {
            throw new IllegalParamaterException("setDeviceManagerUnintall : IllegalParamaterException occur! admin is null");
        }
        try {
            List<String> str = new ArrayList();
            str.add(admin.getPackageName());
            int pattern = this.custManager.getUninstallPattern();
            if (pattern != 1) {
                this.custManager.setUninstallPattern(1);
            }
            Log.d("SecurityManager", "Device Manager uninstallPattern : " + pattern + "  is set to uninstall: " + isUninstall);
            if (isUninstall) {
                this.custManager.deleteUninstallBlackList(str);
            } else {
                this.custManager.addUninstallBlackList(str);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalParamaterException("setDeviceManagerUnintall:IllegalParamaterException occur!");
        }
    }

    public boolean getDeviceManagerUnintall(ComponentName admin) throws SecurityException, IllegalParamaterException {
        if (admin == null) {
            throw new IllegalParamaterException("getDeviceManagerUnintall : IllegalParamaterException occur! admin is null");
        }
        try {
            int pattern = this.custManager.getUninstallPattern();
            if (pattern != 1) {
                this.custManager.setUninstallPattern(1);
            }
            List<String> str = this.custManager.getUninstallBlackList();
            Log.d("SecurityManager", "Device Manager uninstallPattern : " + pattern + "  uninstalllist size:" + str.size());
            if (str == null || str.size() <= 0) {
                return true;
            }
            Log.d("SecurityManager", "uninstallList is contains EMM :" + str.contains(admin.getPackageName()));
            return str.contains(admin.getPackageName()) ^ 1;
        } catch (IllegalArgumentException e) {
            throw new IllegalParamaterException("getDeviceManagerUnintall:IllegalParamaterException occur!");
        }
    }
}
