package android.app.anr;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IANRManager extends IInterface {

    public static abstract class Stub extends Binder implements IANRManager {
        private static final String DESCRIPTOR = "android.app.anr.IANRManager";
        static final int TRANSACTION_appBinderTimeout = 2;
        static final int TRANSACTION_appLooperBlocked = 1;
        static final int TRANSACTION_clearAllMessages = 5;
        static final int TRANSACTION_getAllBinders = 4;
        static final int TRANSACTION_getAllMessages = 3;

        private static class Proxy implements IANRManager {
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

            public void appLooperBlocked(String pkgName, int pid, String msg, int totaltime, String binderCall) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(pid);
                    _data.writeString(msg);
                    _data.writeInt(totaltime);
                    _data.writeString(binderCall);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void appBinderTimeout(String pkgName, int pid, String service, int totaltime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(pid);
                    _data.writeString(service);
                    _data.writeInt(totaltime);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getAllMessages(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getAllBinders(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearAllMessages(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    this.mRemote.transact(5, _data, _reply, 0);
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

        public static IANRManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IANRManager)) {
                return new Proxy(obj);
            }
            return (IANRManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    appLooperBlocked(data.readString(), data.readInt(), data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    appBinderTimeout(data.readString(), data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    String _result = getAllMessages(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    String[] _result2 = getAllBinders(data.readInt());
                    reply.writeNoException();
                    reply.writeStringArray(_result2);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    clearAllMessages(data.readInt());
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void appBinderTimeout(String str, int i, String str2, int i2) throws RemoteException;

    void appLooperBlocked(String str, int i, String str2, int i2, String str3) throws RemoteException;

    void clearAllMessages(int i) throws RemoteException;

    String[] getAllBinders(int i) throws RemoteException;

    String getAllMessages(int i) throws RemoteException;
}
