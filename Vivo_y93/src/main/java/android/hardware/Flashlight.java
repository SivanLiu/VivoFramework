package android.hardware;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.provider.Settings.System;
import android.util.Log;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class Flashlight {
    public static final int FLASHLIGHT_CAMERA_FRONT_ON = 4;
    public static final int FLASHLIGHT_REMIND_BACK_ON = 6;
    public static final int FLASHLIGHT_REMIND_FRONT_ON = 5;
    public static final int FLASHLIGHT_STATE_HIGH = 2;
    public static final int FLASHLIGHT_STATE_LOW = 1;
    public static final int FLASHLIGHT_STATE_OFF = 0;
    public static final int FLASHLIGHT_STATE_SOS = 7;
    public static final int FLASHLIGHT_STATE_STATUSBAR = 8;
    public static final int FRONT_FLASHLIGHT_STATE_HIGH = 12;
    public static final int FRONT_FLASHLIGHT_STATE_LOW = 11;
    public static final int FRONT_FLASHLIGHT_STATE_OFF = 10;
    public static final String KEY_BACK_FLASHLIGHT_STATE = "back_flashlight_state";
    public static final String KEY_FRONT_FLASHLIGHT_STATE = "front_flashlight_state";
    private static final String TAG = "Flashlight";
    private Context mCtx = null;
    private int mFlashState = 0;

    private static native boolean setFlashlightStateNative(int i);

    public Flashlight(Context context) {
        this.mCtx = context;
        this.mFlashState = 0;
    }

    public boolean setFlashlightState(int state) {
        if (state > 12 || state < 0) {
            Log.e(TAG, "invalid state for flashlight.");
            return false;
        }
        Log.i(TAG, "The state[" + state + "] is set.");
        boolean ret = setFlashlightStateNative(state);
        if (ret) {
            this.mFlashState = state;
            saveFlashlightState(state);
        }
        return ret;
    }

    public boolean setFlashlightState(Context context, int state) {
        if (state > 12 || state < 0) {
            Log.e(TAG, "invalid state for flashlight.");
            return false;
        }
        Log.i(TAG, "The only for state[" + state + "] is set.");
        boolean ret = setFlashlightStateNative(state);
        System.putInt(this.mCtx.getContentResolver(), "FlashState", state);
        if (ret) {
            this.mFlashState = state;
            saveFlashlightState(state);
        }
        return ret;
    }

    public boolean setFlashlightState(Context context, int state, int runner) {
        if (state > 12 || state < 0) {
            Log.e(TAG, "invalid state for flashlight.");
            return false;
        }
        Log.i(TAG, "The only for state[" + state + "] is set.");
        boolean ret = setFlashlightStateNative(state);
        System.putInt(this.mCtx.getContentResolver(), "FlashState", state);
        System.putInt(this.mCtx.getContentResolver(), "Flash-State", runner);
        if (ret) {
            this.mFlashState = state;
            saveFlashlightState(state);
        }
        return ret;
    }

    public int getFlashlightState() {
        return System.getInt(this.mCtx.getContentResolver(), "FlashState", 0);
    }

    private void saveFlashlightState(int state) {
        String settingName;
        Log.i(TAG, "saveFlashlightState >> state = " + state);
        if (state < 10 || state > 12) {
            settingName = KEY_BACK_FLASHLIGHT_STATE;
        } else {
            settingName = KEY_FRONT_FLASHLIGHT_STATE;
        }
        System.putInt(this.mCtx.getContentResolver(), settingName, state);
    }
}
