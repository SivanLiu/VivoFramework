package vivo.app.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.vivo.services.security.client.IVivoPermissionCallback;
import com.vivo.services.security.client.VivoPermissionInfo;
import java.util.List;

public interface IVivoPermissionService extends IInterface {

    public static abstract class Stub extends Binder implements IVivoPermissionService {
        private static final String DESCRIPTOR = "vivo.app.security.IVivoPermissionService";
        static final int TRANSACTION_checkDelete = 18;
        static final int TRANSACTION_checkOnePermission = 15;
        static final int TRANSACTION_checkPermission = 1;
        static final int TRANSACTION_getAppPermission = 6;
        static final int TRANSACTION_getMonitorAppList = 5;
        static final int TRANSACTION_getSpecifiedPermAppList = 3;
        static final int TRANSACTION_getTrustedAppList = 4;
        static final int TRANSACTION_getVPMDataBaseState = 14;
        static final int TRANSACTION_getVPMVersion = 13;
        static final int TRANSACTION_isBuildInThirdPartApp = 12;
        static final int TRANSACTION_isCheckingPermission = 2;
        static final int TRANSACTION_isVivoImeiPkg = 19;
        static final int TRANSACTION_noteStartActivityProcess = 11;
        static final int TRANSACTION_setAppPermission = 8;
        static final int TRANSACTION_setAppPermissionExt = 7;
        static final int TRANSACTION_setBlackListApp = 10;
        static final int TRANSACTION_setOnePermission = 16;
        static final int TRANSACTION_setWhiteListApp = 9;
        static final int TRANSACTION_systemReady = 17;

        private static class Proxy implements IVivoPermissionService {
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

            public int checkPermission(String permission, int pid, int uid, IVivoPermissionCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isCheckingPermission(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
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

            public List<VivoPermissionInfo> getSpecifiedPermAppList(int vpTypeId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(vpTypeId);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    List<VivoPermissionInfo> _result = _reply.createTypedArrayList(VivoPermissionInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<VivoPermissionInfo> getTrustedAppList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    List<VivoPermissionInfo> _result = _reply.createTypedArrayList(VivoPermissionInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<VivoPermissionInfo> getMonitorAppList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    List<VivoPermissionInfo> _result = _reply.createTypedArrayList(VivoPermissionInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VivoPermissionInfo getAppPermission(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    VivoPermissionInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (VivoPermissionInfo) VivoPermissionInfo.CREATOR.createFromParcel(_reply);
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

            public void setAppPermissionExt(VivoPermissionInfo vpi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (vpi != null) {
                        _data.writeInt(1);
                        vpi.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAppPermission(String packageName, int vpTypeId, int result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(vpTypeId);
                    _data.writeInt(result);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWhiteListApp(String packageName, boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (enable) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBlackListApp(String packageName, boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (enable) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteStartActivityProcess(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isBuildInThirdPartApp(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
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

            public int getVPMVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVPMDataBaseState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkOnePermission(String packageName, String perm, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(perm);
                    _data.writeInt(uid);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setOnePermission(String packageName, String perm, int uid, boolean granted) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(perm);
                    _data.writeInt(uid);
                    if (granted) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(16, _data, _reply, 0);
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

            public void systemReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_systemReady, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean checkDelete(String path, String pkg, String type, String key, int uid, int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    _data.writeString(pkg);
                    _data.writeString(type);
                    _data.writeString(key);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    this.mRemote.transact(Stub.TRANSACTION_checkDelete, _data, _reply, 0);
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

            public boolean isVivoImeiPkg(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_isVivoImeiPkg, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVivoPermissionService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVivoPermissionService)) {
                return new Proxy(obj);
            }
            return (IVivoPermissionService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            boolean _result2;
            List<VivoPermissionInfo> _result3;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = checkPermission(data.readString(), data.readInt(), data.readInt(), com.vivo.services.security.client.IVivoPermissionCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isCheckingPermission(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getSpecifiedPermAppList(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result3);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getTrustedAppList();
                    reply.writeNoException();
                    reply.writeTypedList(_result3);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getMonitorAppList();
                    reply.writeNoException();
                    reply.writeTypedList(_result3);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    VivoPermissionInfo _result4 = getAppPermission(data.readString());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(1);
                        _result4.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 7:
                    VivoPermissionInfo _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (VivoPermissionInfo) VivoPermissionInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    setAppPermissionExt(_arg0);
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    setAppPermission(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    setWhiteListApp(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    setBlackListApp(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    noteStartActivityProcess(data.readString());
                    reply.writeNoException();
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isBuildInThirdPartApp(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getVPMVersion();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getVPMDataBaseState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result = checkOnePermission(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setOnePermission(data.readString(), data.readString(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case TRANSACTION_systemReady /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    systemReady();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_checkDelete /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = checkDelete(data.readString(), data.readString(), data.readString(), data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case TRANSACTION_isVivoImeiPkg /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isVivoImeiPkg(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean checkDelete(String str, String str2, String str3, String str4, int i, int i2) throws RemoteException;

    int checkOnePermission(String str, String str2, int i) throws RemoteException;

    int checkPermission(String str, int i, int i2, IVivoPermissionCallback iVivoPermissionCallback) throws RemoteException;

    VivoPermissionInfo getAppPermission(String str) throws RemoteException;

    List<VivoPermissionInfo> getMonitorAppList() throws RemoteException;

    List<VivoPermissionInfo> getSpecifiedPermAppList(int i) throws RemoteException;

    List<VivoPermissionInfo> getTrustedAppList() throws RemoteException;

    int getVPMDataBaseState() throws RemoteException;

    int getVPMVersion() throws RemoteException;

    boolean isBuildInThirdPartApp(String str) throws RemoteException;

    boolean isCheckingPermission(int i) throws RemoteException;

    boolean isVivoImeiPkg(String str) throws RemoteException;

    void noteStartActivityProcess(String str) throws RemoteException;

    void setAppPermission(String str, int i, int i2) throws RemoteException;

    void setAppPermissionExt(VivoPermissionInfo vivoPermissionInfo) throws RemoteException;

    void setBlackListApp(String str, boolean z) throws RemoteException;

    boolean setOnePermission(String str, String str2, int i, boolean z) throws RemoteException;

    void setWhiteListApp(String str, boolean z) throws RemoteException;

    void systemReady() throws RemoteException;
}
