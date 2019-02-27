package com.qti.snapdragon.sdk.display;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IColorService extends IInterface {

    public static abstract class Stub extends Binder implements IColorService {
        private static final String DESCRIPTOR = "com.qti.snapdragon.sdk.display.IColorService";
        static final int TRANSACTION_createNewMode = 11;
        static final int TRANSACTION_createNewModeAllFeatures = 15;
        static final int TRANSACTION_deleteMode = 9;
        static final int TRANSACTION_disableMemoryColorConfiguration = 28;
        static final int TRANSACTION_getActiveMode = 7;
        static final int TRANSACTION_getAdaptiveBacklightScale = 22;
        static final int TRANSACTION_getBacklightQualityLevel = 20;
        static final int TRANSACTION_getColorBalance = 3;
        static final int TRANSACTION_getDefaultMode = 13;
        static final int TRANSACTION_getMemoryColorParameters = 27;
        static final int TRANSACTION_getModes = 10;
        static final int TRANSACTION_getNumModes = 6;
        static final int TRANSACTION_getPAParameters = 31;
        static final int TRANSACTION_getRangeMemoryColorParameter = 25;
        static final int TRANSACTION_getRangePAParameter = 29;
        static final int TRANSACTION_getRangeSunlightVisibilityStrength = 17;
        static final int TRANSACTION_getSunlightVisibilityStrength = 19;
        static final int TRANSACTION_getUserColorBalance = 5;
        static final int TRANSACTION_isActiveFeatureOn = 23;
        static final int TRANSACTION_isFeatureSupported = 1;
        static final int TRANSACTION_modifyMode = 12;
        static final int TRANSACTION_modifyModeAllFeatures = 16;
        static final int TRANSACTION_release = 32;
        static final int TRANSACTION_setActiveFeatureControl = 24;
        static final int TRANSACTION_setActiveMode = 8;
        static final int TRANSACTION_setBacklightQualityLevel = 21;
        static final int TRANSACTION_setColorBalance = 2;
        static final int TRANSACTION_setDefaultMode = 14;
        static final int TRANSACTION_setMemoryColorParameters = 26;
        static final int TRANSACTION_setPAParameters = 30;
        static final int TRANSACTION_setSunlightVisibilityStrength = 18;
        static final int TRANSACTION_setUserColorBalance = 4;

        private static class Proxy implements IColorService {
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

            public boolean isFeatureSupported(int displayId, int featureId) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(featureId);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setColorBalance(int displayId, int warmth) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(warmth);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getColorBalance(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setUserColorBalance(int displayId, int flags, double red, double green, double blue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(flags);
                    _data.writeDouble(red);
                    _data.writeDouble(green);
                    _data.writeDouble(blue);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public double[] getUserColorBalance(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    double[] _result = _reply.createDoubleArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getNumModes(int displayId, int modeType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(modeType);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long[] getActiveMode(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    long[] _result = _reply.createLongArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setActiveMode(int displayId, int modeId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(modeId);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteMode(int displayId, int modeId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(modeId);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ModeInfo[] getModes(int displayId, int modeType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(modeType);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    ModeInfo[] _result = (ModeInfo[]) _reply.createTypedArray(ModeInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int createNewMode(int displayId, String name, long flag, int cbWarmth) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeString(name);
                    _data.writeLong(flag);
                    _data.writeInt(cbWarmth);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int modifyMode(int displayId, int modeId, String name, long flag, int cbWarmth) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(modeId);
                    _data.writeString(name);
                    _data.writeLong(flag);
                    _data.writeInt(cbWarmth);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDefaultMode(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setDefaultMode(int displayId, int modeId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(modeId);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int createNewModeAllFeatures(int displayId, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeString(name);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int modifyModeAllFeatures(int displayId, int modeId, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(modeId);
                    _data.writeString(name);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRangeSunlightVisibilityStrength(int displayId, int minMax) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(minMax);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setSunlightVisibilityStrength(int displayId, int strengthVal) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(strengthVal);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSunlightVisibilityStrength(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getBacklightQualityLevel(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setBacklightQualityLevel(int displayId, int level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(level);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAdaptiveBacklightScale(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int isActiveFeatureOn(int displayId, int feature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(feature);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setActiveFeatureControl(int displayId, int feature, int request) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(feature);
                    _data.writeInt(request);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getRangeMemoryColorParameter(int displayId, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(type);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setMemoryColorParameters(int displayId, int type, int hue, int saturation, int intensity) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(type);
                    _data.writeInt(hue);
                    _data.writeInt(saturation);
                    _data.writeInt(intensity);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getMemoryColorParameters(int displayId, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(type);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int disableMemoryColorConfiguration(int displayId, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(type);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getRangePAParameter(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setPAParameters(int displayId, int flag, int hue, int saturation, int intensity, int contrast, int satThreshold) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(flag);
                    _data.writeInt(hue);
                    _data.writeInt(saturation);
                    _data.writeInt(intensity);
                    _data.writeInt(contrast);
                    _data.writeInt(satThreshold);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getPAParameters(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void release() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(32, _data, _reply, 0);
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

        public static IColorService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IColorService)) {
                return (IColorService) iin;
            }
            return new Proxy(obj);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            int[] _result2;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result3 = isFeatureSupported(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(!_result3 ? 0 : 1);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setColorBalance(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getColorBalance(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setUserColorBalance(data.readInt(), data.readInt(), data.readDouble(), data.readDouble(), data.readDouble());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    double[] _result4 = getUserColorBalance(data.readInt());
                    reply.writeNoException();
                    reply.writeDoubleArray(_result4);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getNumModes(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    long[] _result5 = getActiveMode(data.readInt());
                    reply.writeNoException();
                    reply.writeLongArray(_result5);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setActiveMode(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result = deleteMode(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    ModeInfo[] _result6 = getModes(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedArray(_result6, 1);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _result = createNewMode(data.readInt(), data.readString(), data.readLong(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    _result = modifyMode(data.readInt(), data.readInt(), data.readString(), data.readLong(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getDefaultMode(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setDefaultMode(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result = createNewModeAllFeatures(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _result = modifyModeAllFeatures(data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getRangeSunlightVisibilityStrength(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setSunlightVisibilityStrength(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSunlightVisibilityStrength(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getBacklightQualityLevel(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setBacklightQualityLevel(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAdaptiveBacklightScale(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isActiveFeatureOn(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setActiveFeatureControl(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getRangeMemoryColorParameter(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case 26:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setMemoryColorParameters(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getMemoryColorParameters(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    _result = disableMemoryColorConfiguration(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getRangePAParameter(data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case 30:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setPAParameters(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 31:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPAParameters(data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case 32:
                    data.enforceInterface(DESCRIPTOR);
                    release();
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

    int createNewMode(int i, String str, long j, int i2) throws RemoteException;

    int createNewModeAllFeatures(int i, String str) throws RemoteException;

    int deleteMode(int i, int i2) throws RemoteException;

    int disableMemoryColorConfiguration(int i, int i2) throws RemoteException;

    long[] getActiveMode(int i) throws RemoteException;

    int getAdaptiveBacklightScale(int i) throws RemoteException;

    int getBacklightQualityLevel(int i) throws RemoteException;

    int getColorBalance(int i) throws RemoteException;

    int getDefaultMode(int i) throws RemoteException;

    int[] getMemoryColorParameters(int i, int i2) throws RemoteException;

    ModeInfo[] getModes(int i, int i2) throws RemoteException;

    int getNumModes(int i, int i2) throws RemoteException;

    int[] getPAParameters(int i) throws RemoteException;

    int[] getRangeMemoryColorParameter(int i, int i2) throws RemoteException;

    int[] getRangePAParameter(int i) throws RemoteException;

    int getRangeSunlightVisibilityStrength(int i, int i2) throws RemoteException;

    int getSunlightVisibilityStrength(int i) throws RemoteException;

    double[] getUserColorBalance(int i) throws RemoteException;

    int isActiveFeatureOn(int i, int i2) throws RemoteException;

    boolean isFeatureSupported(int i, int i2) throws RemoteException;

    int modifyMode(int i, int i2, String str, long j, int i3) throws RemoteException;

    int modifyModeAllFeatures(int i, int i2, String str) throws RemoteException;

    void release() throws RemoteException;

    int setActiveFeatureControl(int i, int i2, int i3) throws RemoteException;

    int setActiveMode(int i, int i2) throws RemoteException;

    int setBacklightQualityLevel(int i, int i2) throws RemoteException;

    int setColorBalance(int i, int i2) throws RemoteException;

    int setDefaultMode(int i, int i2) throws RemoteException;

    int setMemoryColorParameters(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    int setPAParameters(int i, int i2, int i3, int i4, int i5, int i6, int i7) throws RemoteException;

    int setSunlightVisibilityStrength(int i, int i2) throws RemoteException;

    int setUserColorBalance(int i, int i2, double d, double d2, double d3) throws RemoteException;
}
