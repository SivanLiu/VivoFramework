package com.vivo.services.rms.sdk;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

public abstract class RMNative extends Binder implements IRM {
    public static final String SERVICE_NAME = "rms";
    public static final String VERSION = "1.0";

    public RMNative() {
        attachInterface(this, IRM.DESCRIPTOR);
    }

    public static IRM asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IRM in = (IRM) obj.queryLocalInterface(IRM.DESCRIPTOR);
        if (in != null) {
            return in;
        }
        return new RMProxy(obj);
    }

    public IBinder asBinder() {
        return this;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        Bundle bundle;
        switch (code) {
            case 1:
                setAppList(data.readString(), data.createStringArrayList());
                reply.writeNoException();
                return true;
            case 2:
                killProcess(data.createIntArray(), data.createIntArray(), data.readString(), data.readInt() == 1);
                reply.writeNoException();
                return true;
            case 3:
                stopPackage(data.readString(), data.readInt(), data.readString());
                reply.writeNoException();
                return true;
            case 4:
                reply.writeInt(getPss(data.readInt()));
                reply.writeNoException();
                return true;
            case 5:
                reply.writeInt(writeSysFs(data.createStringArrayList(), data.createStringArrayList()) ? 1 : 0);
                reply.writeNoException();
                return true;
            case 6:
                String name = data.readString();
                bundle = new Bundle();
                bundle.readFromParcel(data);
                reply.writeInt(setBundle(name, bundle) ? 1 : 0);
                reply.writeNoException();
                return true;
            case 7:
                bundle = getBundle(data.readString());
                if (bundle == null) {
                    bundle = new Bundle();
                }
                bundle.writeToParcel(reply, 0);
                reply.writeNoException();
                return true;
            case 8:
                reply.writeInt(restoreSysFs(data.createStringArrayList()) ? 1 : 0);
                reply.writeNoException();
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }
}
