package com.android.vendors.bridge.softsim;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;
import java.util.Map;

public interface ISoftSimService extends IInterface {

    public static abstract class Stub extends Binder implements ISoftSimService {
        private static final String DESCRIPTOR = "com.android.vendors.bridge.softsim.ISoftSimService";
        static final int TRANSACTION_configureApn = 8;
        static final int TRANSACTION_configureRat = 9;
        static final int TRANSACTION_deregisterCallback = 2;
        static final int TRANSACTION_getPlatformManufacturer = 3;
        static final int TRANSACTION_getServiceState = 5;
        static final int TRANSACTION_getSupportedSlots = 4;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_sendApdu = 7;
        static final int TRANSACTION_sendEvent = 6;
        static final int TRANSACTION_startSpecificUidToNetwok = 10;
        static final int TRANSACTION_stopSpecificUidToNetwok = 11;
        static final int TRANSACTION_uimRemoteEvent = 12;

        private static class Proxy implements ISoftSimService {
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

            public int registerCallback(ISoftSimServiceCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deregisterCallback(ISoftSimServiceCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_deregisterCallback, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPlatformManufacturer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPlatformManufacturer, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSupportedSlots() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSupportedSlots, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getServiceState(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    this.mRemote.transact(Stub.TRANSACTION_getServiceState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendEvent(int slot, int event, byte[] atr, int errCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(event);
                    _data.writeByteArray(atr);
                    _data.writeInt(errCode);
                    this.mRemote.transact(Stub.TRANSACTION_sendEvent, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendApdu(int slot, int apduStatus, byte[] apduResp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(apduStatus);
                    _data.writeByteArray(apduResp);
                    this.mRemote.transact(Stub.TRANSACTION_sendApdu, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int configureApn(Map apn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeMap(apn);
                    this.mRemote.transact(Stub.TRANSACTION_configureApn, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int configureRat(int rat) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rat);
                    this.mRemote.transact(Stub.TRANSACTION_configureRat, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startSpecificUidToNetwok(List<String> Uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(Uids);
                    this.mRemote.transact(Stub.TRANSACTION_startSpecificUidToNetwok, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int stopSpecificUidToNetwok() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stopSpecificUidToNetwok, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteEvent(int slot, int event, byte[] atr, int errCode, boolean has_transport, int transport, boolean has_usage, int usage, boolean has_apdu_timeout, int apdu_timeout, boolean has_disable_all_polling, int disable_all_polling, boolean has_poll_timer, int poll_timer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(event);
                    _data.writeByteArray(atr);
                    _data.writeInt(errCode);
                    _data.writeInt(has_transport ? Stub.TRANSACTION_registerCallback : 0);
                    _data.writeInt(transport);
                    _data.writeInt(has_usage ? Stub.TRANSACTION_registerCallback : 0);
                    _data.writeInt(usage);
                    _data.writeInt(has_apdu_timeout ? Stub.TRANSACTION_registerCallback : 0);
                    _data.writeInt(apdu_timeout);
                    _data.writeInt(has_disable_all_polling ? Stub.TRANSACTION_registerCallback : 0);
                    _data.writeInt(disable_all_polling);
                    _data.writeInt(has_poll_timer ? Stub.TRANSACTION_registerCallback : 0);
                    _data.writeInt(poll_timer);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteEvent, _data, _reply, 0);
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

        public static ISoftSimService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISoftSimService)) {
                return new Proxy(obj);
            }
            return (ISoftSimService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case TRANSACTION_registerCallback /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = registerCallback(com.android.vendors.bridge.softsim.ISoftSimServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_deregisterCallback /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = deregisterCallback(com.android.vendors.bridge.softsim.ISoftSimServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getPlatformManufacturer /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getPlatformManufacturer();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getSupportedSlots /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSupportedSlots();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getServiceState /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getServiceState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_sendEvent /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = sendEvent(data.readInt(), data.readInt(), data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_sendApdu /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = sendApdu(data.readInt(), data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_configureApn /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = configureApn(data.readHashMap(getClass().getClassLoader()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_configureRat /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = configureRat(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_startSpecificUidToNetwok /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = startSpecificUidToNetwok(data.createStringArrayList());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_stopSpecificUidToNetwok /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = stopSpecificUidToNetwok();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimRemoteEvent /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimRemoteEvent(data.readInt(), data.readInt(), data.createByteArray(), data.readInt(), data.readInt() != 0, data.readInt(), data.readInt() != 0, data.readInt(), data.readInt() != 0, data.readInt(), data.readInt() != 0, data.readInt(), data.readInt() != 0, data.readInt());
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

    int configureApn(Map map) throws RemoteException;

    int configureRat(int i) throws RemoteException;

    int deregisterCallback(ISoftSimServiceCallback iSoftSimServiceCallback) throws RemoteException;

    int getPlatformManufacturer() throws RemoteException;

    int getServiceState(int i) throws RemoteException;

    int getSupportedSlots() throws RemoteException;

    int registerCallback(ISoftSimServiceCallback iSoftSimServiceCallback) throws RemoteException;

    int sendApdu(int i, int i2, byte[] bArr) throws RemoteException;

    int sendEvent(int i, int i2, byte[] bArr, int i3) throws RemoteException;

    int startSpecificUidToNetwok(List<String> list) throws RemoteException;

    int stopSpecificUidToNetwok() throws RemoteException;

    int uimRemoteEvent(int i, int i2, byte[] bArr, int i3, boolean z, int i4, boolean z2, int i5, boolean z3, int i6, boolean z4, int i7, boolean z5, int i8) throws RemoteException;
}
