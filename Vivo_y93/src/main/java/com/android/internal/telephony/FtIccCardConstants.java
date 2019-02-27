package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import com.android.internal.telephony.IccCardConstants.State;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class FtIccCardConstants {
    public static final String INTENT_VALUE_LOCKED_NETWORK = "NETWORK";
    public static State NETWORK_LOCKED = State.NETWORK_LOCKED;

    public static State intToState(int state) throws IllegalArgumentException {
        switch (state) {
            case 0:
                return State.UNKNOWN;
            case 1:
                return State.ABSENT;
            case 2:
                return State.PIN_REQUIRED;
            case 3:
                return State.PUK_REQUIRED;
            case 4:
                return State.NETWORK_LOCKED;
            case 5:
                return State.READY;
            case 6:
                return State.NOT_READY;
            case 7:
                return State.PERM_DISABLED;
            case 8:
                return State.CARD_IO_ERROR;
            default:
                throw new IllegalArgumentException();
        }
    }
}
