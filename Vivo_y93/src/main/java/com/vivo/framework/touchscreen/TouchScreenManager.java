package com.vivo.framework.touchscreen;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import vivo.app.touchscreen.AbsTouchScreenManager;
import vivo.app.touchscreen.ITouchScreen;
import vivo.app.touchscreen.ITouchScreen.Stub;

public class TouchScreenManager extends AbsTouchScreenManager {
    private static final String TAG = "TouchScreenManager";
    private static TouchScreenManager sInstance = null;
    private static ITouchScreen sService = null;

    public static TouchScreenManager getInstance() {
        if (sInstance == null) {
            sInstance = new TouchScreenManager();
        }
        return sInstance;
    }

    private static ITouchScreen getService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService("bbk_touch_screen_service");
        if (b == null) {
            return null;
        }
        sService = Stub.asInterface(b);
        return sService;
    }

    public void TouchscreenLcdBacklightStateSet(boolean isScreenOn) {
        ITouchScreen service = getService();
        if (service == null) {
            Slog.d(TAG, "service is NULL");
            return;
        }
        try {
            service.TouchscreenLcdBacklightStateSet(isScreenOn);
        } catch (RemoteException e) {
            Slog.e(TAG, "TouchscreenLcdBacklightStateSet exception", e);
        }
    }

    public int TouchscreenSetFingerGestureSwitch(int state) {
        ITouchScreen service = getService();
        if (service == null) {
            Slog.d(TAG, "service is NULL");
            return 0;
        }
        try {
            return service.TouchscreenSetFingerGestureSwitch(state);
        } catch (RemoteException e) {
            Slog.e(TAG, "TouchscreenSetFingerGestureSwitch exception", e);
            return 0;
        }
    }

    public void touchscreenAccStateSet(int isLandscape) {
        ITouchScreen service = getService();
        if (service == null) {
            Slog.d(TAG, "service is NULL");
            return;
        }
        try {
            service.TouchscreenAccStateSet(isLandscape);
        } catch (RemoteException e) {
            Slog.e(TAG, "TouchscreenAccStateSet exception", e);
        }
    }

    public int touchScreenCallingSwitch(int on) {
        ITouchScreen service = getService();
        if (service == null) {
            Slog.d(TAG, "service is NULL");
            return 0;
        }
        try {
            return service.TouchScreenCallingSwitch(on);
        } catch (RemoteException e) {
            Slog.e(TAG, "TouchScreenCallingSwitch exception", e);
            return 0;
        }
    }

    public int touchScreenGlovesModeSwitch(int on) {
        ITouchScreen service = getService();
        if (service == null) {
            Slog.d(TAG, "service is NULL");
            return 0;
        }
        try {
            return service.TouchScreenGlovesModeSwitch(on);
        } catch (RemoteException e) {
            Slog.e(TAG, "TouchScreenGlovesModeSwitch exception", e);
            return 0;
        }
    }

    private TouchScreenManager() {
        sService = getService();
    }
}
