package com.vivo.services.rms.sdk;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.ArrayList;

public class RMProxy implements IRM {
    private IBinder mRemote;

    public RMProxy(IBinder remote) {
        this.mRemote = remote;
    }

    public IBinder asBinder() {
        return this.mRemote;
    }

    public String getInterfaceDescriptor() {
        return IRM.DESCRIPTOR;
    }

    public void setAppList(String typeName, ArrayList<String> appList) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeString(typeName);
        data.writeStringList(appList);
        this.mRemote.transact(1, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    public void killProcess(int[] pids, int[] curAdjs, String reason, boolean secure) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeIntArray(pids);
        data.writeIntArray(curAdjs);
        data.writeString(reason);
        data.writeInt(secure ? 1 : 0);
        this.mRemote.transact(2, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    public void stopPackage(String pkg, int userId, String reason) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeString(pkg);
        data.writeInt(userId);
        data.writeString(reason);
        this.mRemote.transact(3, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    public int getPss(int pid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInt(pid);
        this.mRemote.transact(4, data, reply, 0);
        int pss = reply.readInt();
        reply.readException();
        reply.recycle();
        data.recycle();
        return pss;
    }

    public boolean writeSysFs(ArrayList<String> fileNames, ArrayList<String> values) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeStringList(fileNames);
        data.writeStringList(values);
        this.mRemote.transact(5, data, reply, 0);
        int result = reply.readInt();
        reply.readException();
        reply.recycle();
        data.recycle();
        if (result == 1) {
            return true;
        }
        return false;
    }

    public boolean setBundle(String name, Bundle bundle) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeString(name);
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.writeToParcel(data, 0);
        this.mRemote.transact(6, data, reply, 0);
        int result = reply.readInt();
        reply.readException();
        reply.recycle();
        data.recycle();
        if (result == 1) {
            return true;
        }
        return false;
    }

    public Bundle getBundle(String name) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeString(name);
        this.mRemote.transact(7, data, reply, 0);
        Bundle bundle = new Bundle();
        bundle.readFromParcel(reply);
        reply.readException();
        reply.recycle();
        data.recycle();
        return bundle;
    }

    public boolean restoreSysFs(ArrayList<String> fileNames) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeStringList(fileNames);
        this.mRemote.transact(8, data, reply, 0);
        int result = reply.readInt();
        reply.readException();
        reply.recycle();
        data.recycle();
        if (result == 1) {
            return true;
        }
        return false;
    }
}
