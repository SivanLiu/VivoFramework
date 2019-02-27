package com.vivo.statistics.sdk;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class GatherManager {
    private static final String TAG = "GatherManager";
    private static GatherManager sInstance = null;

    private GatherManager() {
    }

    public static GatherManager getInstance() {
        if (sInstance == null) {
            sInstance = new GatherManager();
        }
        return sInstance;
    }

    public IGather getService() {
        IBinder b = ServiceManager.checkService(IGather.SERVER_NAME);
        if (b == null) {
            return null;
        }
        return GatherProxy.asInterface(b);
    }

    public void gather(String tag, Object... args) {
        IGather service = getService();
        if (service != null) {
            try {
                service.gather(tag, new ArgPack(args));
            } catch (RemoteException e) {
                Log.e(TAG, "gather exception", e);
            }
        }
    }

    public void gathers(String[] tags, ArgPack[] argPacks) {
        IGather service = getService();
        if (service != null) {
            try {
                service.gathers(tags, argPacks);
            } catch (RemoteException e) {
                Log.e(TAG, "gathers exception", e);
            }
        }
    }

    public void beginGather(String tag, Object... args) {
        IGather service = getService();
        if (service != null) {
            try {
                service.beginGather(tag, new ArgPack(args));
            } catch (RemoteException e) {
                Log.e(TAG, "beginGather exception", e);
            }
        }
    }

    public void endGather(String tag, Object... args) {
        IGather service = getService();
        if (service != null) {
            try {
                service.endGather(tag, new ArgPack(args));
            } catch (RemoteException e) {
                Log.e(TAG, "endGather exception", e);
            }
        }
    }
}
