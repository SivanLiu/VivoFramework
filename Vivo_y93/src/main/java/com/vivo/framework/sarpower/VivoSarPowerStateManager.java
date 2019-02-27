package com.vivo.framework.sarpower;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import vivo.app.sarpower.AbsVivoSarPowerStateManager;
import vivo.app.sarpower.IVivoSarPowerState;
import vivo.app.sarpower.IVivoSarPowerState.Stub;

public class VivoSarPowerStateManager extends AbsVivoSarPowerStateManager {
    private static final String TAG = "VivoSarPowerStateManager";
    private static VivoSarPowerStateManager sInstance = null;
    private static IVivoSarPowerState sService = null;

    private VivoSarPowerStateManager() {
        sService = getService();
    }

    public static VivoSarPowerStateManager getInstance() {
        if (sInstance == null) {
            sInstance = new VivoSarPowerStateManager();
        }
        return sInstance;
    }

    private static IVivoSarPowerState getService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService("vivo_sar_power_state_service");
        if (b == null) {
            Log.e(TAG, "getService vivo_sar_power_state_service return null");
            return null;
        }
        sService = Stub.asInterface(b);
        return sService;
    }

    public int sarPowerSwitchEnable(int enable) {
        IVivoSarPowerState service = getService();
        if (service == null) {
            Log.e(TAG, "sarPowerSwitchEnable service = null");
            return -1;
        }
        try {
            return service.sarPowerSwitchEnable(enable);
        } catch (RemoteException e) {
            Log.e(TAG, "sarPowerSwitchEnable failed", e);
            return -1;
        }
    }
}
