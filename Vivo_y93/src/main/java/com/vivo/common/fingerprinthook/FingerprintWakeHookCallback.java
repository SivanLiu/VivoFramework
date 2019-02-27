package com.vivo.common.fingerprinthook;

public interface FingerprintWakeHookCallback {
    int getAnimatedBrightness();

    int getCurrentDisplayPolicy();

    void goToSleep();

    void hideKeyguardByFingerprint(int i);

    void setFingerFlagDirty();

    void userActivity();

    void wakeUp();
}
