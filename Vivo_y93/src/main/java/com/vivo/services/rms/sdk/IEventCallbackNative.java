package com.vivo.services.rms.sdk;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import com.vivo.services.rms.sdk.args.Args;
import java.io.IOException;

public abstract class IEventCallbackNative extends Binder implements IEventCallback {
    public IEventCallbackNative() {
        attachInterface(this, IEventCallback.DESCRIPTOR);
    }

    public static IEventCallback asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IEventCallback in = (IEventCallback) obj.queryLocalInterface(IEventCallback.DESCRIPTOR);
        if (in != null) {
            return in;
        }
        return new IEventCallbackProxy(obj);
    }

    public IBinder asBinder() {
        return this;
    }

    /* JADX WARNING: Missing block: B:3:0x0008, code:
            return super.onTransact(r9, r10, r11, r12);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 1:
                onProcessEvent(data.readInt(), (Args) Args.CREATOR.createFromParcel(data));
                reply.writeNoException();
                return true;
            case 2:
                onSystemEvent(data.readInt(), (Args) Args.CREATOR.createFromParcel(data));
                reply.writeNoException();
                return true;
            case 3:
                ParcelFileDescriptor fd = data.readFileDescriptor();
                String[] args = data.createStringArray();
                if (fd != null) {
                    try {
                        dumpData(fd.getFileDescriptor(), args);
                        break;
                    } finally {
                        try {
                            fd.close();
                        } catch (IOException e) {
                        }
                    }
                }
                break;
            case 4:
                reply.writeInt(myPid());
                return true;
            case 5:
                Bundle bundle = new Bundle();
                bundle.readFromParcel(data);
                doInit(bundle);
                reply.writeNoException();
                break;
        }
        reply.writeNoException();
        return true;
    }

    public int myPid() throws RemoteException {
        return Process.myPid();
    }
}
