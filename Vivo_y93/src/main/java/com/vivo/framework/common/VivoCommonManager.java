package com.vivo.framework.common;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import vivo.app.common.AbsVivoCommonManager;
import vivo.app.common.IVivoCommon;
import vivo.app.common.IVivoCommon.Stub;

public class VivoCommonManager extends AbsVivoCommonManager {
    private static final String TAG = "VivoCommonManager";
    private static VivoCommonManager sInstance = null;
    private static IVivoCommon sService = null;

    private VivoCommonManager() {
        sService = getService();
    }

    public static VivoCommonManager getInstance() {
        if (sInstance == null) {
            sInstance = new VivoCommonManager();
        }
        return sInstance;
    }

    private static IVivoCommon getService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService("vivo_common_service");
        if (b == null) {
            return null;
        }
        sService = Stub.asInterface(b);
        return sService;
    }

    public void ping(String msg) {
        IVivoCommon service = getService();
        if (service != null) {
            try {
                service.ping(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Dead object in ping", e);
            }
        }
    }

    public String doCommonJob(String msg) {
        IVivoCommon service = getService();
        if (service == null) {
            return null;
        }
        try {
            return service.doCommonJob(msg);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in ping", e);
            return null;
        }
    }
}
