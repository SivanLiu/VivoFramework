package vivo.app.epm;

import android.content.ContentValues;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import vivo.app.epm.IExceptionPolicyManager.Stub;

public class ExceptionPolicyManager {
    private static final String TAG = "ExceptionPolicyManager";
    private static ExceptionPolicyManager sInstance = null;
    private static IExceptionPolicyManager sService = null;

    private ExceptionPolicyManager() {
        sService = getService();
    }

    public static synchronized ExceptionPolicyManager getInstance() {
        ExceptionPolicyManager exceptionPolicyManager;
        synchronized (ExceptionPolicyManager.class) {
            if (sInstance == null) {
                sInstance = new ExceptionPolicyManager();
            }
            exceptionPolicyManager = sInstance;
        }
        return exceptionPolicyManager;
    }

    private static IExceptionPolicyManager getService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService("exception_policy");
        if (b == null) {
            return null;
        }
        sService = Stub.asInterface(b);
        return sService;
    }

    public void reportEvent(int eventType, long timestamp, String message) {
        IExceptionPolicyManager service = getService();
        if (service != null) {
            try {
                service.reportEvent(eventType, timestamp, message);
            } catch (RemoteException e) {
                Log.e(TAG, "Dead object in reportEvent", e);
            }
        }
    }

    public void reportEvent(int eventType, long timestamp, ContentValues content) {
        IExceptionPolicyManager service = getService();
        if (service != null) {
            try {
                service.reportEventWithMap(eventType, timestamp, content);
            } catch (RemoteException e) {
                Log.e(TAG, "Dead object in reportEvent", e);
            }
        }
    }
}
