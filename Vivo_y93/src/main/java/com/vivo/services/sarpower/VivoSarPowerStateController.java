package com.vivo.services.sarpower;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
abstract class VivoSarPowerStateController {
    protected int mCardOneState = -1;
    protected int mCardTwoState = -1;
    protected boolean mForceUpdateState = false;
    protected int mLastCardState = -1;
    protected int mLastSarPowerState = -1;
    protected int mProximityState = -1;
    protected int mSarPowerRfDetectState = -1;
    protected int mScreenState = -1;

    public abstract void handleSarMessage(int i, int i2);

    public abstract boolean initialPowerState();

    public abstract void notifySarPowerTest(int i);

    VivoSarPowerStateController() {
    }

    public void notifyProxmityState(int proximityState) {
        this.mProximityState = proximityState;
    }

    public void notifySarPowerRfDetectState(int sarRfDetectState) {
        this.mSarPowerRfDetectState = sarRfDetectState;
    }

    public void notifyScreenState(int screenState) {
        this.mScreenState = screenState;
    }

    public void notifyCardState(boolean isCardOne, int state) {
        if (isCardOne) {
            this.mCardOneState = state;
        } else {
            this.mCardTwoState = state;
        }
    }

    public void notifyBootCompleted() {
        this.mLastSarPowerState = -1;
    }

    public void notifyForceUpdateState() {
        this.mForceUpdateState = true;
    }
}
