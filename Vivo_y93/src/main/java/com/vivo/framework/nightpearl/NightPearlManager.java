package com.vivo.framework.nightpearl;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Slog;
import com.vivo.framework.nightpearl.INightPearlManager.Stub;
import vivo.app.nightpearl.AbsNightPearlManager;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public final class NightPearlManager extends AbsNightPearlManager {
    private static final String TAG = NightPearlManager.class.getSimpleName();
    private static NightPearlManager mNightPearlManager;
    private INightPearlManager mService;

    private NightPearlManager() {
        checkService();
    }

    public static NightPearlManager getInstance() {
        if (mNightPearlManager == null) {
            mNightPearlManager = new NightPearlManager();
        }
        return mNightPearlManager;
    }

    private INightPearlManager checkService() {
        this.mService = Stub.asInterface(ServiceManager.checkService("night_pearl_service"));
        return this.mService;
    }

    public void onBacklightStateChanged(int state, int brightness) {
        checkService();
        if (this.mService != null) {
            try {
                this.mService.onBacklightStateChanged(state, brightness);
                return;
            } catch (RemoteException e) {
                Log.e(TAG, "onBacklightStateChanged :", e);
                return;
            }
        }
        Slog.e(TAG, "onBacklightStateChanged mService is null.");
    }

    public void onShowOff(int reason) {
        checkService();
        if (this.mService != null) {
            try {
                this.mService.onShowOff(reason);
                return;
            } catch (RemoteException e) {
                Log.e(TAG, "onShowOff :", e);
                return;
            }
        }
        Slog.e(TAG, "onShowOff mService is null.");
    }

    public boolean isNightPearlShowing() {
        checkService();
        if (this.mService != null) {
            try {
                return this.mService.isNightPearlShowing();
            } catch (RemoteException e) {
                Log.e(TAG, "isNightPearlShowing :", e);
                return false;
            }
        }
        Slog.e(TAG, "isNightPearlShowing mService is null.");
        return false;
    }

    public void onDrawFinished() {
        checkService();
        if (this.mService != null) {
            try {
                this.mService.onDrawFinished();
                return;
            } catch (RemoteException e) {
                Log.e(TAG, "onDrawFinished :", e);
                return;
            }
        }
        Slog.e(TAG, "onDrawFinished mService is null.");
    }

    public void linkToDeath(DeathRecipient deathRecipient, int flags) {
        try {
            checkService();
            if (this.mService != null) {
                this.mService.asBinder().linkToDeath(deathRecipient, flags);
            }
        } catch (RemoteException e) {
            Log.d(TAG, "linkToDeath RemoteException");
        }
    }

    public boolean unlinkToDeath(DeathRecipient deathRecipient, int flags) {
        checkService();
        if (this.mService != null) {
            return this.mService.asBinder().unlinkToDeath(deathRecipient, flags);
        }
        return false;
    }
}
