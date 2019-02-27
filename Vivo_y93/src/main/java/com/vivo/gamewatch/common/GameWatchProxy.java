package com.vivo.gamewatch.common;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

public class GameWatchProxy implements IGameWatch {
    private IBinder mRemote;

    public GameWatchProxy(IBinder remote) {
        this.mRemote = remote;
    }

    public static IGameWatch asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IGameWatch in = (IGameWatch) obj.queryLocalInterface(IGameWatch.DESCRIPTOR);
        if (in != null) {
            return in;
        }
        return new GameWatchProxy(obj);
    }

    public IBinder asBinder() {
        return this.mRemote;
    }

    public String getInterfaceDescriptor() {
        return IGameWatch.DESCRIPTOR;
    }

    public void gather(String tag, ArgPack argPack) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeString(tag);
            argPack.writeToParcel(data, 0);
            this.mRemote.transact(1, data, reply, 1);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public void gathers(String[] tags, ArgPack[] argPacks) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeStringArray(tags);
            ArgPack.writeArgArray(argPacks, data);
            this.mRemote.transact(2, data, reply, 1);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public void beginGather(String tag, ArgPack argPack) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeString(tag);
            argPack.writeToParcel(data, 0);
            this.mRemote.transact(3, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public void endGather(String tag, ArgPack argPack) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeString(tag);
            argPack.writeToParcel(data, 0);
            this.mRemote.transact(4, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public ArgPack execute(String tag, ArgPack argPack) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeString(tag);
            argPack.writeToParcel(data, 0);
            this.mRemote.transact(5, data, reply, 0);
            ArgPack createFromParcel = ArgPack.createFromParcel(reply);
            return createFromParcel;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }
}
