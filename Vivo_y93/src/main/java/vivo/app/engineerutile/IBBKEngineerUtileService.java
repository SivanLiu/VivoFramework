package vivo.app.engineerutile;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBBKEngineerUtileService extends IInterface {

    public static abstract class Stub extends Binder implements IBBKEngineerUtileService {
        private static final String DESCRIPTOR = "vivo.app.engineerutile.IBBKEngineerUtileService";
        static final int TRANSACTION_checkCameraDeviceConnect = 6;
        static final int TRANSACTION_isServiceWork = 8;
        static final int TRANSACTION_isSetupwizardDisabled = 7;
        static final int TRANSACTION_sendBroadcastFromAtcid = 1;
        static final int TRANSACTION_startActivityFromAtcid = 2;
        static final int TRANSACTION_startServiceFromAtcid = 4;
        static final int TRANSACTION_stopActivityFromAtcid = 3;
        static final int TRANSACTION_stopServiceFromAtcid = 5;

        private static class Proxy implements IBBKEngineerUtileService {
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

            public void sendBroadcastFromAtcid(String name, String action, String extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(action);
                    _data.writeString(extra);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startActivityFromAtcid(String name, String action, String extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(action);
                    _data.writeString(extra);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopActivityFromAtcid(String name, String action, String extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(action);
                    _data.writeString(extra);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startServiceFromAtcid(String name, String action, String extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(action);
                    _data.writeString(extra);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopServiceFromAtcid(String name, String action, String extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(action);
                    _data.writeString(extra);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkCameraDeviceConnect() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int isSetupwizardDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int isServiceWork(String serviceName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(serviceName);
                    this.mRemote.transact(8, _data, _reply, 0);
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

        public static IBBKEngineerUtileService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBBKEngineerUtileService)) {
                return new Proxy(obj);
            }
            return (IBBKEngineerUtileService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    sendBroadcastFromAtcid(data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    startActivityFromAtcid(data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    stopActivityFromAtcid(data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    startServiceFromAtcid(data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    stopServiceFromAtcid(data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _result = checkCameraDeviceConnect();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isSetupwizardDisabled();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isServiceWork(data.readString());
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

    int checkCameraDeviceConnect() throws RemoteException;

    int isServiceWork(String str) throws RemoteException;

    int isSetupwizardDisabled() throws RemoteException;

    void sendBroadcastFromAtcid(String str, String str2, String str3) throws RemoteException;

    void startActivityFromAtcid(String str, String str2, String str3) throws RemoteException;

    void startServiceFromAtcid(String str, String str2, String str3) throws RemoteException;

    void stopActivityFromAtcid(String str, String str2, String str3) throws RemoteException;

    void stopServiceFromAtcid(String str, String str2, String str3) throws RemoteException;
}
