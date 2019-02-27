package vivo.app.backup;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IVivoBackupManager extends IInterface {

    public static abstract class Stub extends Binder implements IVivoBackupManager {
        private static final String DESCRIPTOR = "vivo.app.backup.IVivoBackupManager";
        static final int TRANSACTION_addBackupCompleteSize = 13;
        static final int TRANSACTION_backupPackage = 2;
        static final int TRANSACTION_backupPackageByZip = 4;
        static final int TRANSACTION_checkSupportFeature = 1;
        static final int TRANSACTION_enableDualPackage = 10;
        static final int TRANSACTION_getDualUserId = 11;
        static final int TRANSACTION_getRestoreCmdList = 15;
        static final int TRANSACTION_isDualPackageEnabled = 9;
        static final int TRANSACTION_isRunningFromVivoBackup = 8;
        static final int TRANSACTION_onAgentBackupZipComplete = 6;
        static final int TRANSACTION_onAgentRestoreZipComplete = 7;
        static final int TRANSACTION_postRestoreCompleteSize = 14;
        static final int TRANSACTION_restoreBackupFile = 3;
        static final int TRANSACTION_restorePackageByZip = 5;
        static final int TRANSACTION_startConfirmationUi = 12;

        private static class Proxy implements IVivoBackupManager {
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

            public boolean checkSupportFeature(int feature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean backupPackage(String pkgName, ParcelFileDescriptor fd, IPackageBackupRestoreObserver observer, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, boolean doAllApps, boolean includeSystem, boolean compress, boolean doKeyValue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeInt(includeApks ? 1 : 0);
                    _data.writeInt(includeObbs ? 1 : 0);
                    _data.writeInt(includeShared ? 1 : 0);
                    _data.writeInt(doWidgets ? 1 : 0);
                    _data.writeInt(doAllApps ? 1 : 0);
                    _data.writeInt(includeSystem ? 1 : 0);
                    _data.writeInt(compress ? 1 : 0);
                    _data.writeInt(doKeyValue ? 1 : 0);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean restoreBackupFile(String pkgName, ParcelFileDescriptor fd, String[] cmdList, IPackageBackupRestoreObserver observer) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(cmdList);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean backupPackageByZip(String pkgName, ParcelFileDescriptor writeFile, boolean enableDual, String[] dirs, IPackageBackupRestoreObserver observer) throws RemoteException {
                int i = 1;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (writeFile != null) {
                        _data.writeInt(1);
                        writeFile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!enableDual) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeStringArray(dirs);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean restorePackageByZip(String pkgName, ParcelFileDescriptor readFile, boolean enableDual, String[] cmdList, IPackageBackupRestoreObserver observer) throws RemoteException {
                int i = 1;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (readFile != null) {
                        _data.writeInt(1);
                        readFile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!enableDual) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeStringArray(cmdList);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onAgentBackupZipComplete(int token, long result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeLong(result);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onAgentRestoreZipComplete(int token, long result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeLong(result);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isRunningFromVivoBackup(int fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isDualPackageEnabled(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enableDualPackage(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDualUserId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startConfirmationUi(int token, String action, int fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeString(action);
                    _data.writeInt(fd);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addBackupCompleteSize(long size, int fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(size);
                    _data.writeInt(fd);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void postRestoreCompleteSize(long size, int fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(size);
                    _data.writeInt(fd);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getRestoreCmdList(int fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
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

        public static IVivoBackupManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVivoBackupManager)) {
                return new Proxy(obj);
            }
            return (IVivoBackupManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            String _arg0;
            ParcelFileDescriptor _arg1;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = checkSupportFeature(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    _result = backupPackage(_arg0, _arg1, vivo.app.backup.IPackageBackupRestoreObserver.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    _result = restoreBackupFile(_arg0, _arg1, data.createStringArray(), vivo.app.backup.IPackageBackupRestoreObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    _result = backupPackageByZip(_arg0, _arg1, data.readInt() != 0, data.createStringArray(), vivo.app.backup.IPackageBackupRestoreObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    _result = restorePackageByZip(_arg0, _arg1, data.readInt() != 0, data.createStringArray(), vivo.app.backup.IPackageBackupRestoreObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    onAgentBackupZipComplete(data.readInt(), data.readLong());
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    onAgentRestoreZipComplete(data.readInt(), data.readLong());
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isRunningFromVivoBackup(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isDualPackageEnabled(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _result = enableDualPackage(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    int _result2 = getDualUserId();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    _result = startConfirmationUi(data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    addBackupCompleteSize(data.readLong(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    postRestoreCompleteSize(data.readLong(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    String[] _result3 = getRestoreCmdList(data.readInt());
                    reply.writeNoException();
                    reply.writeStringArray(_result3);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addBackupCompleteSize(long j, int i) throws RemoteException;

    boolean backupPackage(String str, ParcelFileDescriptor parcelFileDescriptor, IPackageBackupRestoreObserver iPackageBackupRestoreObserver, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, boolean z8) throws RemoteException;

    boolean backupPackageByZip(String str, ParcelFileDescriptor parcelFileDescriptor, boolean z, String[] strArr, IPackageBackupRestoreObserver iPackageBackupRestoreObserver) throws RemoteException;

    boolean checkSupportFeature(int i) throws RemoteException;

    boolean enableDualPackage(String str) throws RemoteException;

    int getDualUserId() throws RemoteException;

    String[] getRestoreCmdList(int i) throws RemoteException;

    boolean isDualPackageEnabled(String str) throws RemoteException;

    boolean isRunningFromVivoBackup(int i) throws RemoteException;

    void onAgentBackupZipComplete(int i, long j) throws RemoteException;

    void onAgentRestoreZipComplete(int i, long j) throws RemoteException;

    void postRestoreCompleteSize(long j, int i) throws RemoteException;

    boolean restoreBackupFile(String str, ParcelFileDescriptor parcelFileDescriptor, String[] strArr, IPackageBackupRestoreObserver iPackageBackupRestoreObserver) throws RemoteException;

    boolean restorePackageByZip(String str, ParcelFileDescriptor parcelFileDescriptor, boolean z, String[] strArr, IPackageBackupRestoreObserver iPackageBackupRestoreObserver) throws RemoteException;

    boolean startConfirmationUi(int i, String str, int i2) throws RemoteException;
}
