package com.vivo.services.rms;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.am.RMAmsHelper;
import com.vivo.services.rms.appmng.AppManager;
import com.vivo.services.rms.sdk.RMNative;
import com.vivo.services.rms.sdk.args.Args;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class RMServer extends RMNative {
    public static final String TAG = "rms";
    private static RMServer sServer;
    private Context mContext;
    private EventNotifier mEventNotifier;
    private Looper mLooper;

    public static RMServer getInstance() {
        return sServer;
    }

    public static void publish(Context context) {
        if (sServer == null) {
            sServer = new RMServer(context);
        }
        try {
            ServiceManager.addService("rms", sServer);
        } catch (Exception e) {
            Log.e("rms", "RMServer addService fail");
        }
    }

    private RMServer(Context context) {
        this.mContext = context;
        HandlerThread thread = new HandlerThread("rms");
        thread.start();
        this.mLooper = thread.getLooper();
        this.mEventNotifier = new EventNotifier(context, this.mLooper);
        SystemProperties.set("sys.rms.is_supported", "true");
    }

    public EventNotifier getEventNotifier() {
        return this.mEventNotifier;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void postProcessEvent(int event, Args args) {
        this.mEventNotifier.postProcessEvent(event, args);
    }

    public void postSystemEvent(int event, Args args) {
        this.mEventNotifier.postSystemEvent(event, args);
    }

    public void setAppList(String typeName, ArrayList<String> arrayList) throws RemoteException {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Permission Denial callingUid=" + Binder.getCallingUid());
        }
    }

    public void killProcess(int[] pids, int[] curAdjs, String reason, boolean secure) throws RemoteException {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Permission Denial callingUid=" + Binder.getCallingUid());
        }
        RMAmsHelper.killProcess(pids, curAdjs, reason, secure);
    }

    public void stopPackage(String pkg, int userId, String reason) throws RemoteException {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Permission Denial callingUid=" + Binder.getCallingUid());
        }
        RMAmsHelper.stopPackage(pkg, userId, reason);
    }

    public boolean writeSysFs(ArrayList<String> fileNames, ArrayList<String> values) throws RemoteException {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Permission Denial callingUid=" + Binder.getCallingUid());
        } else if (SysFsModifier.modify(fileNames, values)) {
            return true;
        } else {
            SysFsModifier.restore(fileNames);
            return false;
        }
    }

    public boolean restoreSysFs(ArrayList<String> fileNames) throws RemoteException {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Permission Denial callingUid=" + Binder.getCallingUid());
        }
        SysFsModifier.restore(fileNames);
        return true;
    }

    public int getPss(int pid) throws RemoteException {
        if (Binder.getCallingUid() == 1000) {
            return (int) Debug.getPss(pid, null, null);
        }
        throw new SecurityException("Permission Denial callingUid=" + Binder.getCallingUid());
    }

    public boolean setBundle(String name, Bundle bundle) throws RemoteException {
        if (Binder.getCallingUid() == 1000) {
            return Config.setBundle(name, bundle);
        }
        throw new SecurityException("Permission Denial callingUid=" + Binder.getCallingUid());
    }

    public Bundle getBundle(String name) throws RemoteException {
        if (Binder.getCallingUid() == 1000) {
            return null;
        }
        throw new SecurityException("Permission Denial callingUid=" + Binder.getCallingUid());
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args.length < 1 || !"--native".equalsIgnoreCase(args[0])) {
            this.mEventNotifier.postDump(fd, pw, args);
        } else {
            AppManager.getInstance().dump(pw);
        }
    }

    public void systemReady() {
        this.mEventNotifier.systemReady();
    }
}
