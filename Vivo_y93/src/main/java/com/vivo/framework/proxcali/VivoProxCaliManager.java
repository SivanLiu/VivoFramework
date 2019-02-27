package com.vivo.framework.proxcali;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import vivo.app.proxcali.AbsVivoProxCaliManager;
import vivo.app.proxcali.IVivoProxCali;
import vivo.app.proxcali.IVivoProxCali.Stub;

public class VivoProxCaliManager extends AbsVivoProxCaliManager {
    private static final String TAG = "VivoProxCaliManager";
    private static VivoProxCaliManager sInstance = null;
    private static IVivoProxCali sService = null;

    private VivoProxCaliManager() {
        sService = getService();
    }

    public static VivoProxCaliManager getInstance() {
        if (sInstance == null) {
            sInstance = new VivoProxCaliManager();
        }
        return sInstance;
    }

    private static IVivoProxCali getService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService("vivo_prox_cali_service");
        if (b == null) {
            Log.e(TAG, "getService vivo_prox_cali_service return null");
            return null;
        }
        sService = Stub.asInterface(b);
        return sService;
    }

    public void startCalibration(int type) {
        IVivoProxCali service = getService();
        if (service == null) {
            Log.e(TAG, "startCalibration service = null");
            return;
        }
        try {
            service.startCalibration(type);
        } catch (RemoteException e) {
            Log.e(TAG, "startCalibration failed", e);
        }
    }

    public void setCrystalAnimStatus(boolean isCrystalAnim) {
        IVivoProxCali service = getService();
        if (service == null) {
            Log.e(TAG, "setCrystalAnimStatus service = null");
            return;
        }
        try {
            service.setCrystalAnimStatus(isCrystalAnim);
        } catch (RemoteException e) {
            Log.e(TAG, "setCrystalAnimStatus failed", e);
        }
    }

    public void onDirectCall(long timestamp) {
        IVivoProxCali service = getService();
        if (service == null) {
            Log.e(TAG, "setCrystalAnimStatus service = null");
            return;
        }
        try {
            service.onDirectCall(timestamp);
        } catch (RemoteException e) {
            Log.e(TAG, "onDirectCall failed", e);
        }
    }

    public void changeProximityParam(boolean change, int state) {
        IVivoProxCali service = getService();
        if (service == null) {
            Log.e(TAG, "setCrystalAnimStatus service = null");
            return;
        }
        try {
            service.changeProximityParam(change, state);
        } catch (RemoteException e) {
            Log.e(TAG, "changeProximityParam failed", e);
        }
    }
}
