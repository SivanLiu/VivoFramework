package com.vivo.services.daemon;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

public class VivoDmServiceProxy implements IVivoDmService {
    private IBinder mRemote;

    public static VivoDmServiceProxy asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        VivoDmServiceProxy in = (VivoDmServiceProxy) obj.queryLocalInterface(IVivoDmService.descriptor);
        if (in != null) {
            return in;
        }
        return new VivoDmServiceProxy(obj);
    }

    public VivoDmServiceProxy(IBinder remote) {
        this.mRemote = remote;
    }

    public IBinder asBinder() {
        return this.mRemote;
    }

    public int runShell(String shell) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IVivoDmService.descriptor);
        data.writeString(shell);
        this.mRemote.transact(1, data, reply, 0);
        int ret = reply.readInt();
        reply.recycle();
        data.recycle();
        return ret;
    }

    public String runShellWithResult(String shell) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IVivoDmService.descriptor);
        data.writeString(shell);
        this.mRemote.transact(3, data, reply, 0);
        String ret = reply.readString();
        reply.recycle();
        data.recycle();
        return ret;
    }

    public int runShellFile(String path) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IVivoDmService.descriptor);
        data.writeString(path);
        this.mRemote.transact(2, data, reply, 0);
        int ret = reply.readInt();
        reply.recycle();
        data.recycle();
        return ret;
    }
}
