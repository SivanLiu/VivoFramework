package com.vivo.services.vivomain;

import android.os.SystemProperties;
import com.vivo.common.fingerprinthook.FingerprintWakeHookCollectData;

public class VivoActionProxy {
    private static final String TAG = "VivoActionProxy";
    private static VivoActionProxy mInstance;

    private VivoActionProxy() {
    }

    public static VivoActionProxy getInstance() {
        if (mInstance == null) {
            mInstance = new VivoActionProxy();
        }
        return mInstance;
    }

    public String startBootAnimation() {
        SystemProperties.set("ctl.start", "bootanim");
        return FingerprintWakeHookCollectData.SUCCESS;
    }

    public String stopBootAnimation() {
        SystemProperties.set("ctl.stop", "bootanim");
        return FingerprintWakeHookCollectData.SUCCESS;
    }
}
