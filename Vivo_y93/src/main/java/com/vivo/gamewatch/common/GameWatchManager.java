package com.vivo.gamewatch.common;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class GameWatchManager {
    public static final String TAG = "GameWatchManager";
    private static final GameWatchManager sInstance = new GameWatchManager();

    private GameWatchManager() {
    }

    public static GameWatchManager getInstance() {
        return sInstance;
    }

    public IGameWatch getService() {
        IBinder b = ServiceManager.checkService(IGameWatch.SERVER_NAME);
        if (b == null) {
            return null;
        }
        return GameWatchProxy.asInterface(b);
    }

    public void gather(String tag, Object... args) {
        IGameWatch service = getService();
        if (service != null) {
            try {
                service.gather(tag, new ArgPack(args));
            } catch (RemoteException e) {
                Log.e(TAG, "gather exception", e);
            }
        }
    }

    public void gathers(String[] tags, ArgPack[] argPacks) {
        IGameWatch service = getService();
        if (service != null) {
            try {
                service.gathers(tags, argPacks);
            } catch (RemoteException e) {
                Log.e(TAG, "gathers exception", e);
            }
        }
    }

    public void beginGather(String tag, Object... args) {
        IGameWatch service = getService();
        if (service != null) {
            try {
                service.beginGather(tag, new ArgPack(args));
            } catch (RemoteException e) {
                Log.e(TAG, "beginGather exception", e);
            }
        }
    }

    public void endGather(String tag, Object... args) {
        IGameWatch service = getService();
        if (service != null) {
            try {
                service.endGather(tag, new ArgPack(args));
            } catch (RemoteException e) {
                Log.e(TAG, "endGather exception", e);
            }
        }
    }

    public Object[] execute(String tag, Object... args) {
        IGameWatch service = getService();
        if (service != null) {
            try {
                ArgPack argPack = service.execute(tag, new ArgPack(args));
                if (argPack != null) {
                    return argPack.getObjects();
                }
            } catch (RemoteException e) {
                Log.e(TAG, "execute exception", e);
            }
        }
        return null;
    }
}
