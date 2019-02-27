package com.vivo.customized.support;

import com.vivo.customized.support.inter.VivoDeviceInfoControl;
import java.util.List;

class DeviceInfoManager extends BaseManager implements VivoDeviceInfoControl {
    DeviceInfoManager() {
    }

    public boolean isDeviceRoot() {
        return this.custManager.isDeviceRoot();
    }

    public List<String> getPhoneNumbers() {
        return this.custManager.getPhoneNumbers();
    }

    public List<String> getPhoneIccids() {
        return this.custManager.getPhoneIccids();
    }

    public List<String> getPhoneImeis() {
        return this.custManager.getPhoneImeis();
    }

    public long getTrafficBytes(int mode, String packageName) {
        return this.custManager.getTrafficBytes(mode, packageName);
    }
}
