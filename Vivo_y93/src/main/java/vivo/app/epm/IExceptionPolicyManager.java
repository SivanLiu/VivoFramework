package vivo.app.epm;

import android.content.ContentValues;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IExceptionPolicyManager extends IInterface {

    public static abstract class Stub extends Binder implements IExceptionPolicyManager {
        private static final String DESCRIPTOR = "vivo.app.epm.IExceptionPolicyManager";
        static final int TRANSACTION_reportEvent = 1;
        static final int TRANSACTION_reportEventWithMap = 2;

        private static class Proxy implements IExceptionPolicyManager {
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

            public void reportEvent(int eventType, long timestamp, String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(eventType);
                    _data.writeLong(timestamp);
                    _data.writeString(message);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void reportEventWithMap(int eventType, long timestamp, ContentValues content) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(eventType);
                    _data.writeLong(timestamp);
                    if (content != null) {
                        _data.writeInt(1);
                        content.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IExceptionPolicyManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IExceptionPolicyManager)) {
                return new Proxy(obj);
            }
            return (IExceptionPolicyManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    reportEvent(data.readInt(), data.readLong(), data.readString());
                    return true;
                case 2:
                    ContentValues _arg2;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    long _arg1 = data.readLong();
                    if (data.readInt() != 0) {
                        _arg2 = (ContentValues) ContentValues.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    reportEventWithMap(_arg0, _arg1, _arg2);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void reportEvent(int i, long j, String str) throws RemoteException;

    void reportEventWithMap(int i, long j, ContentValues contentValues) throws RemoteException;
}
