package vivo.app.userprofiling;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVivoUserProfiling extends IInterface {

    public static abstract class Stub extends Binder implements IVivoUserProfiling {
        private static final String DESCRIPTOR = "vivo.app.userprofiling.IVivoUserProfiling";
        static final int TRANSACTION_getApplicationConfig = 2;
        static final int TRANSACTION_getWeChatWebViewValue = 7;
        static final int TRANSACTION_isCaptureOn = 3;
        static final int TRANSACTION_ping = 1;
        static final int TRANSACTION_reportDataToBee = 4;
        static final int TRANSACTION_setWeChatWebViewValue = 6;
        static final int TRANSACTION_updateConfig = 5;
        static final int TRANSACTION_updateWeChatViewId = 8;

        private static class Proxy implements IVivoUserProfiling {
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

            public void ping(String msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(msg);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getApplicationConfig() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isCaptureOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            public int reportDataToBee(String url, String value, String pkgName, String infoType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(url);
                    _data.writeString(value);
                    _data.writeString(pkgName);
                    _data.writeString(infoType);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateConfig() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWeChatWebViewValue(String webViewValue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(webViewValue);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getWeChatWebViewValue() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateWeChatViewId(String textViewId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(textViewId);
                    this.mRemote.transact(8, _data, _reply, 0);
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

        public static IVivoUserProfiling asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVivoUserProfiling)) {
                return new Proxy(obj);
            }
            return (IVivoUserProfiling) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    ping(data.readString());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getApplicationConfig();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result2 = isCaptureOn();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    int _result3 = reportDataToBee(data.readString(), data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    updateConfig();
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    setWeChatWebViewValue(data.readString());
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getWeChatWebViewValue();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    updateWeChatViewId(data.readString());
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

    String getApplicationConfig() throws RemoteException;

    String getWeChatWebViewValue() throws RemoteException;

    boolean isCaptureOn() throws RemoteException;

    void ping(String str) throws RemoteException;

    int reportDataToBee(String str, String str2, String str3, String str4) throws RemoteException;

    void setWeChatWebViewValue(String str) throws RemoteException;

    void updateConfig() throws RemoteException;

    void updateWeChatViewId(String str) throws RemoteException;
}
