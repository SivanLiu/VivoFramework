package vivo.app.touchscreen;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITouchScreen extends IInterface {

    public static abstract class Stub extends Binder implements ITouchScreen {
        private static final String DESCRIPTOR = "vivo.app.touchscreen.ITouchScreen";
        static final int TRANSACTION_TouchScreenCallingSwitch = 5;
        static final int TRANSACTION_TouchScreenDclickSimulateSwitch = 3;
        static final int TRANSACTION_TouchScreenGetDriverICName = 7;
        static final int TRANSACTION_TouchScreenGlovesModeSwitch = 4;
        static final int TRANSACTION_TouchScreenUserDefineGestureGetAttn = 16;
        static final int TRANSACTION_TouchScreenUserDefineGestureGetScore = 18;
        static final int TRANSACTION_TouchScreenUserDefineGestureGetThreshold = 17;
        static final int TRANSACTION_TouchScreenUserDefineGestureSetEnable = 14;
        static final int TRANSACTION_TouchScreenUserDefineGestureSetEnroll = 13;
        static final int TRANSACTION_TouchScreenUserDefineGestureSetMode = 15;
        static final int TRANSACTION_TouchScreenUserDefineGestureSetThreshold = 9;
        static final int TRANSACTION_TouchScreenUserDefineGestureSetgestureEnable = 12;
        static final int TRANSACTION_TouchScreenUserDefineGestureWriteIndex = 10;
        static final int TRANSACTION_TouchScreenUserDefineGestureWriteSignature = 11;
        static final int TRANSACTION_TouchScreenUserDefineGetCoordinates = 25;
        static final int TRANSACTION_TouchScreenUserDefineGetGesturePoints = 26;
        static final int TRANSACTION_TouchScreenUserDefineGetGesturePointsLength = 27;
        static final int TRANSACTION_TouchScreenUserDefineGetMatchScore = 28;
        static final int TRANSACTION_TouchScreenUserDefineGetMaxNumberSigs = 23;
        static final int TRANSACTION_TouchScreenUserDefineGetMaxSigLength = 22;
        static final int TRANSACTION_TouchScreenUserDefineGetgestureEnable = 24;
        static final int TRANSACTION_TouchScreenUserDefineReadDetection = 21;
        static final int TRANSACTION_TouchScreenUserDefineReadIndex = 19;
        static final int TRANSACTION_TouchScreenUserDefineReadSignature = 20;
        static final int TRANSACTION_TouchSensorRxTx = 6;
        static final int TRANSACTION_TouchscreenAccStateSet = 8;
        static final int TRANSACTION_TouchscreenLcdBacklightStateSet = 1;
        static final int TRANSACTION_TouchscreenSetFingerGestureSwitch = 2;
        static final int TRANSACTION_TouchscreenUserDefineGestureClearTemplate = 35;
        static final int TRANSACTION_TouchscreenUserDefineGestureGetDetectionScore = 37;
        static final int TRANSACTION_TouchscreenUserDefineGestureGetRegistrationStatus = 38;
        static final int TRANSACTION_TouchscreenUserDefineGestureGetTemplateData = 45;
        static final int TRANSACTION_TouchscreenUserDefineGestureGetTemplateSize = 39;
        static final int TRANSACTION_TouchscreenUserDefineGestureGetTraceData = 44;
        static final int TRANSACTION_TouchscreenUserDefineGestureGetTraceSize = 43;
        static final int TRANSACTION_TouchscreenUserDefineGestureReadDetectionIndex = 36;
        static final int TRANSACTION_TouchscreenUserDefineGestureReadTemplateDetection = 41;
        static final int TRANSACTION_TouchscreenUserDefineGestureReadTemplateMaxIndex = 40;
        static final int TRANSACTION_TouchscreenUserDefineGestureReadTemplateValid = 42;
        static final int TRANSACTION_TouchscreenUserDefineGestureSetDetectionEnable = 30;
        static final int TRANSACTION_TouchscreenUserDefineGestureSetEngineEnable = 29;
        static final int TRANSACTION_TouchscreenUserDefineGestureSetRegistrationBegin = 32;
        static final int TRANSACTION_TouchscreenUserDefineGestureSetRegistrationEnable = 31;
        static final int TRANSACTION_TouchscreenUserDefineGestureSetTemplateValid = 34;
        static final int TRANSACTION_TouchscreenUserDefineGestureWriteTemplateData = 46;
        static final int TRANSACTION_TouchscreenUserDefineGestureWriteTemplateIndex = 33;

        private static class Proxy implements ITouchScreen {
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

            public void TouchscreenLcdBacklightStateSet(boolean isScreenOn) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!isScreenOn) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenSetFingerGestureSwitch(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenDclickSimulateSwitch(int on) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(on);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenGlovesModeSwitch(int on) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(on);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenCallingSwitch(int on) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(on);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchSensorRxTx() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenGetDriverICName() throws RemoteException {
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

            public void TouchscreenAccStateSet(int isLandscape) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isLandscape);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGestureSetThreshold(int setting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(setting);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGestureWriteIndex(int setting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(setting);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGestureWriteSignature(byte[] signature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(signature);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGestureSetgestureEnable(int setting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(setting);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGestureSetEnroll(int setting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(setting);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGestureSetEnable(int setting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(setting);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGestureSetMode(int setting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(setting);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGestureGetAttn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGestureGetThreshold() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchScreenUserDefineGestureGetThreshold, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGestureGetScore() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchScreenUserDefineGestureGetScore, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineReadIndex() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchScreenUserDefineReadIndex, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineReadSignature(byte[] signature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (signature == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(signature.length);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_TouchScreenUserDefineReadSignature, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(signature);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte TouchScreenUserDefineReadDetection() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchScreenUserDefineReadDetection, _data, _reply, 0);
                    _reply.readException();
                    byte _result = _reply.readByte();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGetMaxSigLength() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchScreenUserDefineGetMaxSigLength, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGetMaxNumberSigs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchScreenUserDefineGetMaxNumberSigs, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGetgestureEnable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchScreenUserDefineGetgestureEnable, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGetCoordinates(byte[] coordinates) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (coordinates == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(coordinates.length);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_TouchScreenUserDefineGetCoordinates, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(coordinates);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGetGesturePoints(byte[] points) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (points == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(points.length);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_TouchScreenUserDefineGetGesturePoints, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(points);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGetGesturePointsLength() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchScreenUserDefineGetGesturePointsLength, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchScreenUserDefineGetMatchScore(byte[] signature1, byte[] signature2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(signature1);
                    _data.writeByteArray(signature2);
                    this.mRemote.transact(Stub.TRANSACTION_TouchScreenUserDefineGetMatchScore, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureSetEngineEnable(int setting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(setting);
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureSetEngineEnable, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureSetDetectionEnable(int setting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(setting);
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureSetDetectionEnable, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureSetRegistrationEnable(int setting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(setting);
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureSetRegistrationEnable, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureSetRegistrationBegin(int setting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(setting);
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureSetRegistrationBegin, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureWriteTemplateIndex(char index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureWriteTemplateIndex, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureSetTemplateValid(int setting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(setting);
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureSetTemplateValid, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureClearTemplate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureClearTemplate, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureReadDetectionIndex() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureReadDetectionIndex, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureGetDetectionScore() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureGetDetectionScore, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureGetRegistrationStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureGetRegistrationStatus, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureGetTemplateSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureGetTemplateSize, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureReadTemplateMaxIndex() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureReadTemplateMaxIndex, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureReadTemplateDetection() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureReadTemplateDetection, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureReadTemplateValid() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureReadTemplateValid, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureGetTraceSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureGetTraceSize, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureGetTraceData(int[] x_trace, int[] y_trace, byte[] segments) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (x_trace == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(x_trace.length);
                    }
                    if (y_trace == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(y_trace.length);
                    }
                    if (segments == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(segments.length);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureGetTraceData, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readIntArray(x_trace);
                    _reply.readIntArray(y_trace);
                    _reply.readByteArray(segments);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureGetTemplateData(float[] data, float[] scalefac, byte[] segments) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(data.length);
                    }
                    if (scalefac == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(scalefac.length);
                    }
                    if (segments == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(segments.length);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureGetTemplateData, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readFloatArray(data);
                    _reply.readFloatArray(scalefac);
                    _reply.readByteArray(segments);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int TouchscreenUserDefineGestureWriteTemplateData(float[] data, float[] scalefac, byte[] segments) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloatArray(data);
                    _data.writeFloatArray(scalefac);
                    _data.writeByteArray(segments);
                    this.mRemote.transact(Stub.TRANSACTION_TouchscreenUserDefineGestureWriteTemplateData, _data, _reply, 0);
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

        public static ITouchScreen asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITouchScreen)) {
                return new Proxy(obj);
            }
            return (ITouchScreen) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            int _arg0_length;
            byte[] bArr;
            int _arg1_length;
            int _arg2_length;
            byte[] _arg2;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    TouchscreenLcdBacklightStateSet(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenSetFingerGestureSwitch(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenDclickSimulateSwitch(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenGlovesModeSwitch(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenCallingSwitch(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchSensorRxTx();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenGetDriverICName();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    TouchscreenAccStateSet(data.readInt());
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenUserDefineGestureSetThreshold(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenUserDefineGestureWriteIndex(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenUserDefineGestureWriteSignature(data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenUserDefineGestureSetgestureEnable(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenUserDefineGestureSetEnroll(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenUserDefineGestureSetEnable(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenUserDefineGestureSetMode(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenUserDefineGestureGetAttn();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchScreenUserDefineGestureGetThreshold /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenUserDefineGestureGetThreshold();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchScreenUserDefineGestureGetScore /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenUserDefineGestureGetScore();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchScreenUserDefineReadIndex /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenUserDefineReadIndex();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchScreenUserDefineReadSignature /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0_length = data.readInt();
                    if (_arg0_length < 0) {
                        bArr = null;
                    } else {
                        bArr = new byte[_arg0_length];
                    }
                    _result = TouchScreenUserDefineReadSignature(bArr);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeByteArray(bArr);
                    return true;
                case TRANSACTION_TouchScreenUserDefineReadDetection /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    byte _result2 = TouchScreenUserDefineReadDetection();
                    reply.writeNoException();
                    reply.writeByte(_result2);
                    return true;
                case TRANSACTION_TouchScreenUserDefineGetMaxSigLength /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenUserDefineGetMaxSigLength();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchScreenUserDefineGetMaxNumberSigs /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenUserDefineGetMaxNumberSigs();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchScreenUserDefineGetgestureEnable /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenUserDefineGetgestureEnable();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchScreenUserDefineGetCoordinates /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0_length = data.readInt();
                    if (_arg0_length < 0) {
                        bArr = null;
                    } else {
                        bArr = new byte[_arg0_length];
                    }
                    _result = TouchScreenUserDefineGetCoordinates(bArr);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeByteArray(bArr);
                    return true;
                case TRANSACTION_TouchScreenUserDefineGetGesturePoints /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0_length = data.readInt();
                    if (_arg0_length < 0) {
                        bArr = null;
                    } else {
                        bArr = new byte[_arg0_length];
                    }
                    _result = TouchScreenUserDefineGetGesturePoints(bArr);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeByteArray(bArr);
                    return true;
                case TRANSACTION_TouchScreenUserDefineGetGesturePointsLength /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenUserDefineGetGesturePointsLength();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchScreenUserDefineGetMatchScore /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchScreenUserDefineGetMatchScore(data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureSetEngineEnable /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenUserDefineGestureSetEngineEnable(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureSetDetectionEnable /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenUserDefineGestureSetDetectionEnable(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureSetRegistrationEnable /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenUserDefineGestureSetRegistrationEnable(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureSetRegistrationBegin /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenUserDefineGestureSetRegistrationBegin(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureWriteTemplateIndex /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenUserDefineGestureWriteTemplateIndex((char) data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureSetTemplateValid /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenUserDefineGestureSetTemplateValid(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureClearTemplate /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenUserDefineGestureClearTemplate();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureReadDetectionIndex /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenUserDefineGestureReadDetectionIndex();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureGetDetectionScore /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenUserDefineGestureGetDetectionScore();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureGetRegistrationStatus /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenUserDefineGestureGetRegistrationStatus();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureGetTemplateSize /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenUserDefineGestureGetTemplateSize();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureReadTemplateMaxIndex /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenUserDefineGestureReadTemplateMaxIndex();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureReadTemplateDetection /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenUserDefineGestureReadTemplateDetection();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureReadTemplateValid /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenUserDefineGestureReadTemplateValid();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureGetTraceSize /*43*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenUserDefineGestureGetTraceSize();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureGetTraceData /*44*/:
                    int[] _arg0;
                    int[] _arg1;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0_length = data.readInt();
                    if (_arg0_length < 0) {
                        _arg0 = null;
                    } else {
                        _arg0 = new int[_arg0_length];
                    }
                    _arg1_length = data.readInt();
                    if (_arg1_length < 0) {
                        _arg1 = null;
                    } else {
                        _arg1 = new int[_arg1_length];
                    }
                    _arg2_length = data.readInt();
                    if (_arg2_length < 0) {
                        _arg2 = null;
                    } else {
                        _arg2 = new byte[_arg2_length];
                    }
                    _result = TouchscreenUserDefineGestureGetTraceData(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeIntArray(_arg0);
                    reply.writeIntArray(_arg1);
                    reply.writeByteArray(_arg2);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureGetTemplateData /*45*/:
                    float[] _arg02;
                    float[] _arg12;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0_length = data.readInt();
                    if (_arg0_length < 0) {
                        _arg02 = null;
                    } else {
                        _arg02 = new float[_arg0_length];
                    }
                    _arg1_length = data.readInt();
                    if (_arg1_length < 0) {
                        _arg12 = null;
                    } else {
                        _arg12 = new float[_arg1_length];
                    }
                    _arg2_length = data.readInt();
                    if (_arg2_length < 0) {
                        _arg2 = null;
                    } else {
                        _arg2 = new byte[_arg2_length];
                    }
                    _result = TouchscreenUserDefineGestureGetTemplateData(_arg02, _arg12, _arg2);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeFloatArray(_arg02);
                    reply.writeFloatArray(_arg12);
                    reply.writeByteArray(_arg2);
                    return true;
                case TRANSACTION_TouchscreenUserDefineGestureWriteTemplateData /*46*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = TouchscreenUserDefineGestureWriteTemplateData(data.createFloatArray(), data.createFloatArray(), data.createByteArray());
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

    int TouchScreenCallingSwitch(int i) throws RemoteException;

    int TouchScreenDclickSimulateSwitch(int i) throws RemoteException;

    int TouchScreenGetDriverICName() throws RemoteException;

    int TouchScreenGlovesModeSwitch(int i) throws RemoteException;

    int TouchScreenUserDefineGestureGetAttn() throws RemoteException;

    int TouchScreenUserDefineGestureGetScore() throws RemoteException;

    int TouchScreenUserDefineGestureGetThreshold() throws RemoteException;

    int TouchScreenUserDefineGestureSetEnable(int i) throws RemoteException;

    int TouchScreenUserDefineGestureSetEnroll(int i) throws RemoteException;

    int TouchScreenUserDefineGestureSetMode(int i) throws RemoteException;

    int TouchScreenUserDefineGestureSetThreshold(int i) throws RemoteException;

    int TouchScreenUserDefineGestureSetgestureEnable(int i) throws RemoteException;

    int TouchScreenUserDefineGestureWriteIndex(int i) throws RemoteException;

    int TouchScreenUserDefineGestureWriteSignature(byte[] bArr) throws RemoteException;

    int TouchScreenUserDefineGetCoordinates(byte[] bArr) throws RemoteException;

    int TouchScreenUserDefineGetGesturePoints(byte[] bArr) throws RemoteException;

    int TouchScreenUserDefineGetGesturePointsLength() throws RemoteException;

    int TouchScreenUserDefineGetMatchScore(byte[] bArr, byte[] bArr2) throws RemoteException;

    int TouchScreenUserDefineGetMaxNumberSigs() throws RemoteException;

    int TouchScreenUserDefineGetMaxSigLength() throws RemoteException;

    int TouchScreenUserDefineGetgestureEnable() throws RemoteException;

    byte TouchScreenUserDefineReadDetection() throws RemoteException;

    int TouchScreenUserDefineReadIndex() throws RemoteException;

    int TouchScreenUserDefineReadSignature(byte[] bArr) throws RemoteException;

    int TouchSensorRxTx() throws RemoteException;

    void TouchscreenAccStateSet(int i) throws RemoteException;

    void TouchscreenLcdBacklightStateSet(boolean z) throws RemoteException;

    int TouchscreenSetFingerGestureSwitch(int i) throws RemoteException;

    int TouchscreenUserDefineGestureClearTemplate() throws RemoteException;

    int TouchscreenUserDefineGestureGetDetectionScore() throws RemoteException;

    int TouchscreenUserDefineGestureGetRegistrationStatus() throws RemoteException;

    int TouchscreenUserDefineGestureGetTemplateData(float[] fArr, float[] fArr2, byte[] bArr) throws RemoteException;

    int TouchscreenUserDefineGestureGetTemplateSize() throws RemoteException;

    int TouchscreenUserDefineGestureGetTraceData(int[] iArr, int[] iArr2, byte[] bArr) throws RemoteException;

    int TouchscreenUserDefineGestureGetTraceSize() throws RemoteException;

    int TouchscreenUserDefineGestureReadDetectionIndex() throws RemoteException;

    int TouchscreenUserDefineGestureReadTemplateDetection() throws RemoteException;

    int TouchscreenUserDefineGestureReadTemplateMaxIndex() throws RemoteException;

    int TouchscreenUserDefineGestureReadTemplateValid() throws RemoteException;

    int TouchscreenUserDefineGestureSetDetectionEnable(int i) throws RemoteException;

    int TouchscreenUserDefineGestureSetEngineEnable(int i) throws RemoteException;

    int TouchscreenUserDefineGestureSetRegistrationBegin(int i) throws RemoteException;

    int TouchscreenUserDefineGestureSetRegistrationEnable(int i) throws RemoteException;

    int TouchscreenUserDefineGestureSetTemplateValid(int i) throws RemoteException;

    int TouchscreenUserDefineGestureWriteTemplateData(float[] fArr, float[] fArr2, byte[] bArr) throws RemoteException;

    int TouchscreenUserDefineGestureWriteTemplateIndex(char c) throws RemoteException;
}
