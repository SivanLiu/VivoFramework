package android.security;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.gatekeeper.IGateKeeperService;
import android.service.gatekeeper.IGateKeeperService.Stub;
import android.util.DoubleAppSwitcher;

public abstract class GateKeeper {
    public static final long INVALID_SECURE_USER_ID = 0;

    private GateKeeper() {
    }

    public static IGateKeeperService getService() {
        IGateKeeperService service = Stub.asInterface(ServiceManager.getService("android.service.gatekeeper.IGateKeeperService"));
        if (service != null) {
            return service;
        }
        throw new IllegalStateException("Gatekeeper service not available");
    }

    public static long getSecureUserId() throws IllegalStateException {
        try {
            return getService().getSecureUserId(UserHandle.myUserId());
        } catch (RemoteException e) {
            throw new IllegalStateException("Failed to obtain secure user ID from gatekeeper", e);
        }
    }

    public static long getSecureUserIdForSoter() throws IllegalStateException {
        int myUserId = UserHandle.myUserId();
        if (DoubleAppSwitcher.sEnabled && myUserId == UserManager.get(KeyStore.getApplicationContext()).getDoubleAppUserId()) {
            myUserId = 0;
        }
        try {
            return getService().getSecureUserId(myUserId);
        } catch (RemoteException e) {
            throw new IllegalStateException("Failed to obtain secure user ID from gatekeeper", e);
        }
    }
}
