package com.android.internal.telephony;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVivoTelephonyApiCallback extends IInterface {

    public static abstract class Stub extends Binder implements IVivoTelephonyApiCallback {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IVivoTelephonyApiCallback";
        static final int TRANSACTION_onCallback = 1;

        private static class Proxy implements IVivoTelephonyApiCallback {
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

            public VivoTelephonyApiParams onCallback(VivoTelephonyApiParams v) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    VivoTelephonyApiParams _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (v != null) {
                        _data.writeInt(1);
                        v.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (VivoTelephonyApiParams) VivoTelephonyApiParams.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVivoTelephonyApiCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVivoTelephonyApiCallback)) {
                return new Proxy(obj);
            }
            return (IVivoTelephonyApiCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    VivoTelephonyApiParams _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (VivoTelephonyApiParams) VivoTelephonyApiParams.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    VivoTelephonyApiParams _result = onCallback(_arg0);
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    VivoTelephonyApiParams onCallback(VivoTelephonyApiParams vivoTelephonyApiParams) throws RemoteException;
}
