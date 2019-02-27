package com.vivo.customized.support.inter;

public interface VivoCustomDriver {
    VivoApplicationControl getApplicationManager();

    VivoDeviceInfoControl getDeviceInfoManager();

    VivoDeviceControl getDeviceManager();

    VivoNetworkControl getNetworkManager();

    VivoOperationControl getOperationManager();

    VivoTelecomControl getTelecomManager();
}
