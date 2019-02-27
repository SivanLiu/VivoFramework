package com.vivo.gamewatch.common;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

public abstract class GameWatchStub extends Binder implements IGameWatch {
    public GameWatchStub() {
        attachInterface(this, IGameWatch.DESCRIPTOR);
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
        return this;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 1:
                gather(data.readString(), (ArgPack) ArgPack.CREATOR.createFromParcel(data));
                reply.writeNoException();
                return true;
            case 2:
                gathers(data.createStringArray(), ArgPack.createArgArray(data));
                return true;
            case 3:
                beginGather(data.readString(), (ArgPack) ArgPack.CREATOR.createFromParcel(data));
                return true;
            case 4:
                endGather(data.readString(), (ArgPack) ArgPack.CREATOR.createFromParcel(data));
                return true;
            case 5:
                ArgPack ret = execute(data.readString(), (ArgPack) ArgPack.CREATOR.createFromParcel(data));
                if (ret != null) {
                    ret.writeToParcel(reply, 0);
                }
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }
}
