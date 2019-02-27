package com.vivo.framework.motion;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import java.util.List;
import vivo.app.motion.AbsMotionManager;
import vivo.app.motion.IMotionManager;
import vivo.app.motion.IMotionManager.Stub;

public class MotionManager extends AbsMotionManager {
    private static final String TAG = "MotionManger";
    private static MotionManager sInstance = null;
    private static IMotionManager sService = null;

    private MotionManager() {
        sService = getService();
    }

    public static MotionManager getInstance() {
        if (sInstance == null) {
            sInstance = new MotionManager();
        }
        return sInstance;
    }

    private static IMotionManager getService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService("motion_manager");
        if (b == null) {
            return null;
        }
        sService = Stub.asInterface(b);
        return sService;
    }

    public int register(String clientId, String callingPackageName, String type, IBinder cb) {
        try {
            return sService.register(clientId, callingPackageName, type, cb);
        } catch (RemoteException e) {
            Log.e(TAG, "register: error please checkout it!");
            return -1;
        }
    }

    public int unregister(String clientId) {
        try {
            return sService.unregister(clientId);
        } catch (RemoteException e) {
            Log.e(TAG, "unregister: error please checkout it!");
            return -1;
        }
    }

    public List getClients() {
        try {
            return sService.getClients();
        } catch (RemoteException e) {
            Log.e(TAG, "getClients: error please checkout it!");
            return null;
        }
    }
}
