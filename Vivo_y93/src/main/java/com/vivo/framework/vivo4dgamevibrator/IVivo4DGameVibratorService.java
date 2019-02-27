package com.vivo.framework.vivo4dgamevibrator;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVivo4DGameVibratorService extends IInterface {

    public static abstract class Stub extends Binder implements IVivo4DGameVibratorService {
        private static final String DESCRIPTOR = "com.vivo.framework.vivo4dgamevibrator.IVivo4DGameVibratorService";
        static final int TRANSACTION_vibrate = 1;

        private static class Proxy implements IVivo4DGameVibratorService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void vibrate(int mod, long callTimeMillis, long vibrateMillis) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mod);
                    _data.writeLong(callTimeMillis);
                    _data.writeLong(vibrateMillis);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVivo4DGameVibratorService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVivo4DGameVibratorService)) {
                return new Proxy(obj);
            }
            return (IVivo4DGameVibratorService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    vibrate(data.readInt(), data.readLong(), data.readLong());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void vibrate(int i, long j, long j2) throws RemoteException;
}
