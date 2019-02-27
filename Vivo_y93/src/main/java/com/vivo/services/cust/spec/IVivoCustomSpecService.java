package com.vivo.services.cust.spec;

import android.content.ComponentName;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVivoCustomSpecService extends IInterface {

    public static abstract class Stub extends Binder implements IVivoCustomSpecService {
        private static final String DESCRIPTOR = "com.vivo.services.cust.spec.IVivoCustomSpecService";
        static final int TRANSACTION_clearDeviceOwner = 8;
        static final int TRANSACTION_disablePackage = 22;
        static final int TRANSACTION_getAPIVersion = 2;
        static final int TRANSACTION_getAccessibilityServcieUIState = 7;
        static final int TRANSACTION_getDataEnabled = 19;
        static final int TRANSACTION_getDevicePolicyManagerUIState = 5;
        static final int TRANSACTION_getFaceWakeState = 13;
        static final int TRANSACTION_getFingerprintState = 17;
        static final int TRANSACTION_getFlightModeStateNormal = 10;
        static final int TRANSACTION_getMobileSettings = 21;
        static final int TRANSACTION_getRomVersion = 3;
        static final int TRANSACTION_getSmartlockState = 15;
        static final int TRANSACTION_isDeveloperOptionsDisabled = 25;
        static final int TRANSACTION_isTrustedAppStoreEnabled = 9;
        static final int TRANSACTION_setAccessibilityServcieUIState = 6;
        static final int TRANSACTION_setDataEnabled = 18;
        static final int TRANSACTION_setDeveloperOptionsDisabled = 24;
        static final int TRANSACTION_setDeviceOwner = 1;
        static final int TRANSACTION_setDevicePolicyManagerUIState = 4;
        static final int TRANSACTION_setFaceWakeState = 12;
        static final int TRANSACTION_setFingerprintState = 16;
        static final int TRANSACTION_setFlightModeStateNormal = 11;
        static final int TRANSACTION_setLanguageChangeDisabled = 23;
        static final int TRANSACTION_setMobileSettings = 20;
        static final int TRANSACTION_setSmartlockState = 14;

        private static class Proxy implements IVivoCustomSpecService {
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

            public boolean setDeviceOwner(ComponentName who) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
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

            public String getAPIVersion() throws RemoteException {
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

            public String getRomVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDevicePolicyManagerUIState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDevicePolicyManagerUIState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAccessibilityServcieUIState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAccessibilityServcieUIState() throws RemoteException {
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

            public void clearDeviceOwner(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isTrustedAppStoreEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            public int getFlightModeStateNormal() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFlightModeStateNormal(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFaceWakeState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getFaceWakeState() throws RemoteException {
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

            public void setSmartlockState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSmartlockState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFingerprintState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getFingerprintState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDataEnabled(boolean value) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (value) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getDataEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(19, _data, _reply, 0);
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

            public void setMobileSettings(ComponentName admin, String busi, Bundle settings) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(busi);
                    if (settings != null) {
                        _data.writeInt(1);
                        settings.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getMobileSettings(ComponentName admin, String busi, String setting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(busi);
                    _data.writeString(setting);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
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

            public void disablePackage(ComponentName admin, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setLanguageChangeDisabled(boolean disabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (disabled) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDeveloperOptionsDisabled(boolean disabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (disabled) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isDeveloperOptionsDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(25, _data, _reply, 0);
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

        public static IVivoCustomSpecService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVivoCustomSpecService)) {
                return new Proxy(obj);
            }
            return (IVivoCustomSpecService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg0;
            boolean _result;
            String _result2;
            int _result3;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    _result = setDeviceOwner(_arg0);
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAPIVersion();
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getRomVersion();
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    setDevicePolicyManagerUIState(data.readInt());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getDevicePolicyManagerUIState();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    setAccessibilityServcieUIState(data.readInt());
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getAccessibilityServcieUIState();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    clearDeviceOwner(data.readString());
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isTrustedAppStoreEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getFlightModeStateNormal();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    setFlightModeStateNormal(data.readInt());
                    reply.writeNoException();
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    setFaceWakeState(data.readInt());
                    reply.writeNoException();
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getFaceWakeState();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    setSmartlockState(data.readInt());
                    reply.writeNoException();
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getSmartlockState();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    setFingerprintState(data.readInt());
                    reply.writeNoException();
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getFingerprintState();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    setDataEnabled(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getDataEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 20:
                    Bundle _arg2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    String _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    setMobileSettings(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    Bundle _result4 = getMobileSettings(_arg0, data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(1);
                        _result4.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    disablePackage(_arg0, data.readString());
                    reply.writeNoException();
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    setLanguageChangeDisabled(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    setDeveloperOptionsDisabled(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isDeveloperOptionsDisabled();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void clearDeviceOwner(String str) throws RemoteException;

    void disablePackage(ComponentName componentName, String str) throws RemoteException;

    String getAPIVersion() throws RemoteException;

    int getAccessibilityServcieUIState() throws RemoteException;

    boolean getDataEnabled() throws RemoteException;

    int getDevicePolicyManagerUIState() throws RemoteException;

    int getFaceWakeState() throws RemoteException;

    int getFingerprintState() throws RemoteException;

    int getFlightModeStateNormal() throws RemoteException;

    Bundle getMobileSettings(ComponentName componentName, String str, String str2) throws RemoteException;

    String getRomVersion() throws RemoteException;

    int getSmartlockState() throws RemoteException;

    boolean isDeveloperOptionsDisabled() throws RemoteException;

    boolean isTrustedAppStoreEnabled() throws RemoteException;

    void setAccessibilityServcieUIState(int i) throws RemoteException;

    void setDataEnabled(boolean z) throws RemoteException;

    void setDeveloperOptionsDisabled(boolean z) throws RemoteException;

    boolean setDeviceOwner(ComponentName componentName) throws RemoteException;

    void setDevicePolicyManagerUIState(int i) throws RemoteException;

    void setFaceWakeState(int i) throws RemoteException;

    void setFingerprintState(int i) throws RemoteException;

    void setFlightModeStateNormal(int i) throws RemoteException;

    void setLanguageChangeDisabled(boolean z) throws RemoteException;

    void setMobileSettings(ComponentName componentName, String str, Bundle bundle) throws RemoteException;

    void setSmartlockState(int i) throws RemoteException;
}
