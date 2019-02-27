package com.vivo.framework.popupcamera;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import vivo.app.popupcamera.AbsPopupCameraManager;
import vivo.app.popupcamera.IPopupCameraManager;
import vivo.app.popupcamera.IPopupCameraManager.Stub;

public class PopupCameraManager extends AbsPopupCameraManager {
    private static final String TAG = "PopupCameraManager";
    private static PopupCameraManager sInstance = null;
    private static IPopupCameraManager sService = null;

    private PopupCameraManager() {
        sService = getService();
    }

    public static PopupCameraManager getInstance() {
        if (sInstance == null) {
            sInstance = new PopupCameraManager();
        }
        return sInstance;
    }

    private static IPopupCameraManager getService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService("popup_camera_service");
        if (b == null) {
            return null;
        }
        sService = Stub.asInterface(b);
        return sService;
    }

    public boolean popupFrontCamera() {
        IPopupCameraManager service = getService();
        if (service == null) {
            return false;
        }
        try {
            return service.popupFrontCamera();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in popupFrontCamera", e);
            return false;
        }
    }

    public boolean takeupFrontCamera() {
        IPopupCameraManager service = getService();
        if (service == null) {
            return false;
        }
        try {
            return service.takeupFrontCamera();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in takeupFrontCamera", e);
            return false;
        }
    }

    public int getFrontCameraStatus() {
        IPopupCameraManager service = getService();
        if (service == null) {
            return -1;
        }
        try {
            return service.getFrontCameraStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getFrontCameraStatus", e);
            return -1;
        }
    }

    public boolean notifyCameraStatus(int cameraId, int status, String packageName) {
        IPopupCameraManager service = getService();
        if (service == null) {
            return false;
        }
        try {
            return service.notifyCameraStatus(cameraId, status, packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in notifyCameraStatus", e);
            return false;
        }
    }
}
