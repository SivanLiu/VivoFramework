package vivo.app.backup;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPackageBackupRestoreObserver extends IInterface {

    public static abstract class Stub extends Binder implements IPackageBackupRestoreObserver {
        private static final String DESCRIPTOR = "vivo.app.backup.IPackageBackupRestoreObserver";
        static final int TRANSACTION_onEnd = 2;
        static final int TRANSACTION_onError = 4;
        static final int TRANSACTION_onProgress = 3;
        static final int TRANSACTION_onStart = 1;

        private static class Proxy implements IPackageBackupRestoreObserver {
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

            public void onStart(String pkgName, int feature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(feature);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onEnd(String pkgName, int feature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(feature);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onProgress(String pkgName, int feature, long complete, long total) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(feature);
                    _data.writeLong(complete);
                    _data.writeLong(total);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onError(String pkgName, int feature, int errno) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(feature);
                    _data.writeInt(errno);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPackageBackupRestoreObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPackageBackupRestoreObserver)) {
                return new Proxy(obj);
            }
            return (IPackageBackupRestoreObserver) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onStart(data.readString(), data.readInt());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onEnd(data.readString(), data.readInt());
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onProgress(data.readString(), data.readInt(), data.readLong(), data.readLong());
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onError(data.readString(), data.readInt(), data.readInt());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onEnd(String str, int i) throws RemoteException;

    void onError(String str, int i, int i2) throws RemoteException;

    void onProgress(String str, int i, long j, long j2) throws RemoteException;

    void onStart(String str, int i) throws RemoteException;
}
