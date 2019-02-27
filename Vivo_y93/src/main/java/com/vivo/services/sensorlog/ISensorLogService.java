package com.vivo.services.sensorlog;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISensorLogService extends IInterface {

    public static abstract class Stub extends Binder implements ISensorLogService {
        private static final String DESCRIPTOR = "com.vivo.services.sensorlog.ISensorLogService";
        static final int TRANSACTION_vivoprintinit = 2;
        static final int TRANSACTION_vivoprintmain = 1;

        private static class Proxy implements ISensorLogService {
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

            public int vivoprintmain(int priority, String tag, String msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(priority);
                    _data.writeString(tag);
                    _data.writeString(msg);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int vivoprintinit(int priority, String tag, String msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(priority);
                    _data.writeString(tag);
                    _data.writeString(msg);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISensorLogService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISensorLogService)) {
                return new Proxy(obj);
            }
            return (ISensorLogService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = vivoprintmain(data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = vivoprintinit(data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int vivoprintinit(int i, String str, String str2) throws RemoteException;

    int vivoprintmain(int i, String str, String str2) throws RemoteException;
}
