package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import com.android.internal.telephony.DctConstants.State;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoCallServiceStateHelper {
    public static final String KEY_PHONE_APN_STATE = "KEY_PHONE_APN_STATE";
    private static final String LOG_TAG = "VivoCallServiceStateHelper";
    public static final String PHONE0_CALL_STATE = "phone0_call_state";
    public static final String PHONE1_CALL_STATE = "phone1_call_state";
    public static final String PHONE_CALL_STATE = "phone_call_state";
    public static int VOICE_CALL_END = 1;
    public static int VOICE_CALL_START = 0;

    public static void updateCallStateSP(Context context, int phoneId, int callState) {
        Editor editor = context.getSharedPreferences(PHONE_CALL_STATE, 0).edit();
        if (phoneId == 0) {
            editor.putInt(PHONE0_CALL_STATE, callState);
        } else if (phoneId == 1) {
            editor.putInt(PHONE1_CALL_STATE, callState);
        } else {
            log("onVoiceCallStarted it will never be happened!");
        }
        editor.apply();
    }

    public static void updateApnStateSP(Context context, int phoneId, String apnType, State state) {
        Editor editor = context.getSharedPreferences(KEY_PHONE_APN_STATE, 0).edit();
        String apnStateKey = new StringBuilder(KEY_PHONE_APN_STATE).append("-").append(apnType).append("-").append(phoneId).toString();
        editor.putInt(apnStateKey, state.ordinal());
        editor.apply();
        log(apnStateKey + " = " + state);
    }

    private static void log(String str) {
        Log.v(LOG_TAG, str);
    }
}
