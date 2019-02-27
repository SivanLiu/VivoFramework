package com.android.vendors.bridge.softsim;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISoftSimServiceCallback extends IInterface {

    public static abstract class Stub extends Binder implements ISoftSimServiceCallback {
        private static final String DESCRIPTOR = "com.android.vendors.bridge.softsim.ISoftSimServiceCallback";
        static final int TRANSACTION_cardPowerDownIndication = 6;
        static final int TRANSACTION_cardPowerUpIndication = 5;
        static final int TRANSACTION_cardResetIndication = 7;
        static final int TRANSACTION_commandApduIndication = 8;
        static final int TRANSACTION_sendApduResponse = 2;
        static final int TRANSACTION_sendEventResponse = 1;
        static final int TRANSACTION_serviceDisabledIndication = 4;
        static final int TRANSACTION_serviceEnabledIndication = 3;
        static final int TRANSACTION_serviceRebootedIndication = 9;
        static final int TRANSACTION_uimRemoteRadioStateIndication = 10;

        private static class Proxy implements ISoftSimServiceCallback {
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

            public void sendEventResponse(int slot, int responseCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(responseCode);
                    this.mRemote.transact(Stub.TRANSACTION_sendEventResponse, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendApduResponse(int slot, int responseCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(responseCode);
                    this.mRemote.transact(Stub.TRANSACTION_sendApduResponse, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void serviceEnabledIndication(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    this.mRemote.transact(Stub.TRANSACTION_serviceEnabledIndication, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void serviceDisabledIndication(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    this.mRemote.transact(Stub.TRANSACTION_serviceDisabledIndication, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cardPowerUpIndication(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    this.mRemote.transact(Stub.TRANSACTION_cardPowerUpIndication, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cardPowerDownIndication(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    this.mRemote.transact(Stub.TRANSACTION_cardPowerDownIndication, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cardResetIndication(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    this.mRemote.transact(Stub.TRANSACTION_cardResetIndication, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void commandApduIndication(int slot, byte[] apduCmd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeByteArray(apduCmd);
                    this.mRemote.transact(Stub.TRANSACTION_commandApduIndication, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void serviceRebootedIndication() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_serviceRebootedIndication, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteRadioStateIndication(int slot, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(state);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteRadioStateIndication, _data, _reply, 0);
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

        public static ISoftSimServiceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISoftSimServiceCallback)) {
                return new Proxy(obj);
            }
            return (ISoftSimServiceCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_sendEventResponse /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    sendEventResponse(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendApduResponse /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    sendApduResponse(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_serviceEnabledIndication /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    serviceEnabledIndication(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_serviceDisabledIndication /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    serviceDisabledIndication(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cardPowerUpIndication /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    cardPowerUpIndication(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cardPowerDownIndication /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    cardPowerDownIndication(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cardResetIndication /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    cardResetIndication(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_commandApduIndication /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    commandApduIndication(data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_serviceRebootedIndication /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    serviceRebootedIndication();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimRemoteRadioStateIndication /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemoteRadioStateIndication(data.readInt(), data.readInt());
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

    void cardPowerDownIndication(int i) throws RemoteException;

    void cardPowerUpIndication(int i) throws RemoteException;

    void cardResetIndication(int i) throws RemoteException;

    void commandApduIndication(int i, byte[] bArr) throws RemoteException;

    void sendApduResponse(int i, int i2) throws RemoteException;

    void sendEventResponse(int i, int i2) throws RemoteException;

    void serviceDisabledIndication(int i) throws RemoteException;

    void serviceEnabledIndication(int i) throws RemoteException;

    void serviceRebootedIndication() throws RemoteException;

    void uimRemoteRadioStateIndication(int i, int i2) throws RemoteException;
}
