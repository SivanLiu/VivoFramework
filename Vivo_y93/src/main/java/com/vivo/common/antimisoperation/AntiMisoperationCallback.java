package com.vivo.common.antimisoperation;

public interface AntiMisoperationCallback {
    int countBrightFullLocks();

    int getCurrentDisplayPolicy();

    boolean getUseProximity();

    void onLogout();

    void onTriggered(boolean z);
}
