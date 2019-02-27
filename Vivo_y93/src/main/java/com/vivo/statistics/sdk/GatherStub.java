package com.vivo.statistics.sdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

public abstract class GatherStub extends Binder implements IGather {
    public GatherStub() {
        attachInterface(this, IGather.DESCRIPTOR);
    }

    public static IGather asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IGather in = (IGather) obj.queryLocalInterface(IGather.DESCRIPTOR);
        if (in != null) {
            return in;
        }
        return new GatherProxy(obj);
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
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }
}
