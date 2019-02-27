package com.vivo.framework.common;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.vivo.services.daemon.VivoDmServiceProxy;

public class VivoDmServiceManager {
    private static final String TAG = "VivoDmServiceManager";
    private static VivoDmServiceManager sInstance = null;
    private static VivoDmServiceProxy sService = null;

    private VivoDmServiceManager() {
        sService = getService();
    }

    public static VivoDmServiceManager getInstance() {
        if (sInstance == null) {
            sInstance = new VivoDmServiceManager();
        }
        return sInstance;
    }

    private static VivoDmServiceProxy getService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService("vivo_daemon.service");
        if (b == null) {
            return null;
        }
        sService = VivoDmServiceProxy.asInterface(b);
        return sService;
    }

    public int runShell(String shell) throws RemoteException {
        int ret = -1;
        VivoDmServiceProxy service = getService();
        if (service == null) {
            return ret;
        }
        try {
            ret = service.runShell(shell);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in runShell", e);
        }
        return ret;
    }

    public String runShellWithResult(String shell) throws RemoteException {
        String ret = null;
        VivoDmServiceProxy service = getService();
        if (service == null) {
            return ret;
        }
        try {
            ret = service.runShellWithResult(shell);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in runShellWithResult", e);
        }
        return ret;
    }

    public int runShellFile(String path) throws RemoteException {
        int ret = -1;
        VivoDmServiceProxy service = getService();
        if (service == null) {
            return ret;
        }
        try {
            ret = service.runShellFile(path);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in runShellFile", e);
        }
        return ret;
    }
}
