package com.vivo.framework.facedetect;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.io.FileDescriptor;

public interface IFaceDetectManager extends IInterface {

    public static abstract class Stub extends Binder implements IFaceDetectManager {
        private static final String DESCRIPTOR = "com.vivo.framework.facedetect.IFaceDetectManager";
        static final int TRANSACTION_FaceDetectInit = 5;
        static final int TRANSACTION_enrollFaceWithImage = 4;
        static final int TRANSACTION_faceRename = 12;
        static final int TRANSACTION_getAdjusterParams = 18;
        static final int TRANSACTION_getFaceID = 10;
        static final int TRANSACTION_getFaceRename = 13;
        static final int TRANSACTION_getFastUnlockEnable = 8;
        static final int TRANSACTION_getRetryCount = 15;
        static final int TRANSACTION_getSharedMemoryFD = 25;
        static final int TRANSACTION_hasEnrolledFace = 6;
        static final int TRANSACTION_initEnroll = 26;
        static final int TRANSACTION_isDarkEnvironment = 30;
        static final int TRANSACTION_isFaceUnlockRunning = 16;
        static final int TRANSACTION_isIRLedAvailable = 31;
        static final int TRANSACTION_isSupportFaceDetect = 7;
        static final int TRANSACTION_notifyBrightnessChange = 33;
        static final int TRANSACTION_notifyFaceKeyguardStatus = 34;
        static final int TRANSACTION_notifyFaceUnlockEnable = 23;
        static final int TRANSACTION_notifyFaceUnlockKillEnable = 24;
        static final int TRANSACTION_notifyOtherMessage = 35;
        static final int TRANSACTION_preEnroll = 14;
        static final int TRANSACTION_releaseHandle = 17;
        static final int TRANSACTION_removeEnrolledFace = 11;
        static final int TRANSACTION_setAdjusterParams = 19;
        static final int TRANSACTION_setEnrollPreview = 3;
        static final int TRANSACTION_setFastUnlockEnable = 9;
        static final int TRANSACTION_setParam = 32;
        static final int TRANSACTION_setVerifyTestPreview = 20;
        static final int TRANSACTION_startAuthenticate = 1;
        static final int TRANSACTION_startAuthenticateIRTest = 22;
        static final int TRANSACTION_startAuthenticateModel = 27;
        static final int TRANSACTION_startAuthenticateTest = 21;
        static final int TRANSACTION_startAuthenticateWithBuffer = 29;
        static final int TRANSACTION_stopAuthenticate = 2;
        static final int TRANSACTION_stopAuthenticateModel = 28;

        private static class Proxy implements IFaceDetectManager {
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

            public void startAuthenticate(IFaceDetectClient mFaceDetectClient) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (mFaceDetectClient != null) {
                        iBinder = mFaceDetectClient.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopAuthenticate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setEnrollPreview(FileDescriptor fd, int previewLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeRawFileDescriptor(fd);
                    _data.writeInt(previewLen);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] enrollFaceWithImage(int mPreviewDatalen, int pixelFormat, int mPreviewWidth, int mPreviewHeight, int orientation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mPreviewDatalen);
                    _data.writeInt(pixelFormat);
                    _data.writeInt(mPreviewWidth);
                    _data.writeInt(mPreviewHeight);
                    _data.writeInt(orientation);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void FaceDetectInit() throws RemoteException {
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

            public boolean hasEnrolledFace() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
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

            public boolean isSupportFaceDetect() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
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

            public boolean getFastUnlockEnable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            public void setFastUnlockEnable(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            public int getFaceID() throws RemoteException {
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

            public void removeEnrolledFace() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void faceRename(byte[] faceName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(faceName);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getFaceRename() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long preEnroll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRetryCount() throws RemoteException {
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

            public boolean isFaceUnlockRunning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            public void releaseHandle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public AdjusterParams getAdjusterParams() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    AdjusterParams _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (AdjusterParams) AdjusterParams.CREATOR.createFromParcel(_reply);
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

            public void setAdjusterParams(AdjusterParams params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVerifyTestPreview(FileDescriptor fd, int previewLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeRawFileDescriptor(fd);
                    _data.writeInt(previewLen);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startAuthenticateTest(int mPreviewDatalen, int pixelFormat, int mPreviewWidth, int mPreviewHeight, int orientation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mPreviewDatalen);
                    _data.writeInt(pixelFormat);
                    _data.writeInt(mPreviewWidth);
                    _data.writeInt(mPreviewHeight);
                    _data.writeInt(orientation);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startAuthenticateIRTest(String packageName, IFaceDetectIRClient mFaceDetectIRClient) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (mFaceDetectIRClient != null) {
                        iBinder = mFaceDetectIRClient.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyFaceUnlockEnable(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
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

            public void notifyFaceUnlockKillEnable(boolean killenable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (killenable) {
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

            public FileDescriptor getSharedMemoryFD(int length) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(length);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    FileDescriptor _result = _reply.readRawFileDescriptor();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void initEnroll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startAuthenticateModel(IFaceDetectClient mFaceDetectClient, String model) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (mFaceDetectClient != null) {
                        iBinder = mFaceDetectClient.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(model);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopAuthenticateModel(String model) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(model);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startAuthenticateWithBuffer(IFaceDetectClient mFaceDetectClient, String model, int mPreviewDatalen, int pixelFormat, int mPreviewWidth, int mPreviewHeight, int orientation, boolean livenesscheck) throws RemoteException {
                IBinder iBinder = null;
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (mFaceDetectClient != null) {
                        iBinder = mFaceDetectClient.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(model);
                    _data.writeInt(mPreviewDatalen);
                    _data.writeInt(pixelFormat);
                    _data.writeInt(mPreviewWidth);
                    _data.writeInt(mPreviewHeight);
                    _data.writeInt(orientation);
                    if (livenesscheck) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isDarkEnvironment() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(30, _data, _reply, 0);
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

            public boolean isIRLedAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(31, _data, _reply, 0);
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

            public void setParam(boolean isDarkEnvironment) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (isDarkEnvironment) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyBrightnessChange(boolean changed) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (changed) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_notifyBrightnessChange, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyFaceKeyguardStatus(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    this.mRemote.transact(Stub.TRANSACTION_notifyFaceKeyguardStatus, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyOtherMessage(String message, int param, int extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(message);
                    _data.writeInt(param);
                    _data.writeInt(extra);
                    this.mRemote.transact(Stub.TRANSACTION_notifyOtherMessage, _data, _reply, 0);
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

        public static IFaceDetectManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFaceDetectManager)) {
                return new Proxy(obj);
            }
            return (IFaceDetectManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            byte[] _result;
            boolean _result2;
            int _result3;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    startAuthenticate(com.vivo.framework.facedetect.IFaceDetectClient.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    stopAuthenticate();
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    setEnrollPreview(data.readRawFileDescriptor(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = enrollFaceWithImage(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    FaceDetectInit();
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = hasEnrolledFace();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isSupportFaceDetect();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getFastUnlockEnable();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    setFastUnlockEnable(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getFaceID();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    removeEnrolledFace();
                    reply.writeNoException();
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    faceRename(data.createByteArray());
                    reply.writeNoException();
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getFaceRename();
                    reply.writeNoException();
                    reply.writeByteArray(_result);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    long _result4 = preEnroll();
                    reply.writeNoException();
                    reply.writeLong(_result4);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getRetryCount();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isFaceUnlockRunning();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    releaseHandle();
                    reply.writeNoException();
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    AdjusterParams _result5 = getAdjusterParams();
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(1);
                        _result5.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 19:
                    AdjusterParams _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (AdjusterParams) AdjusterParams.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    setAdjusterParams(_arg0);
                    reply.writeNoException();
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    setVerifyTestPreview(data.readRawFileDescriptor(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = startAuthenticateTest(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    startAuthenticateIRTest(data.readString(), com.vivo.framework.facedetect.IFaceDetectIRClient.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    notifyFaceUnlockEnable(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    notifyFaceUnlockKillEnable(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    FileDescriptor _result6 = getSharedMemoryFD(data.readInt());
                    reply.writeNoException();
                    reply.writeRawFileDescriptor(_result6);
                    return true;
                case 26:
                    data.enforceInterface(DESCRIPTOR);
                    initEnroll();
                    reply.writeNoException();
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    startAuthenticateModel(com.vivo.framework.facedetect.IFaceDetectClient.Stub.asInterface(data.readStrongBinder()), data.readString());
                    reply.writeNoException();
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    stopAuthenticateModel(data.readString());
                    reply.writeNoException();
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = startAuthenticateWithBuffer(com.vivo.framework.facedetect.IFaceDetectClient.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 30:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isDarkEnvironment();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 31:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isIRLedAvailable();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 32:
                    data.enforceInterface(DESCRIPTOR);
                    setParam(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_notifyBrightnessChange /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    notifyBrightnessChange(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_notifyFaceKeyguardStatus /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    notifyFaceKeyguardStatus(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_notifyOtherMessage /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    notifyOtherMessage(data.readString(), data.readInt(), data.readInt());
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

    void FaceDetectInit() throws RemoteException;

    byte[] enrollFaceWithImage(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    void faceRename(byte[] bArr) throws RemoteException;

    AdjusterParams getAdjusterParams() throws RemoteException;

    int getFaceID() throws RemoteException;

    byte[] getFaceRename() throws RemoteException;

    boolean getFastUnlockEnable() throws RemoteException;

    int getRetryCount() throws RemoteException;

    FileDescriptor getSharedMemoryFD(int i) throws RemoteException;

    boolean hasEnrolledFace() throws RemoteException;

    void initEnroll() throws RemoteException;

    boolean isDarkEnvironment() throws RemoteException;

    boolean isFaceUnlockRunning() throws RemoteException;

    boolean isIRLedAvailable() throws RemoteException;

    boolean isSupportFaceDetect() throws RemoteException;

    void notifyBrightnessChange(boolean z) throws RemoteException;

    void notifyFaceKeyguardStatus(int i) throws RemoteException;

    void notifyFaceUnlockEnable(boolean z) throws RemoteException;

    void notifyFaceUnlockKillEnable(boolean z) throws RemoteException;

    void notifyOtherMessage(String str, int i, int i2) throws RemoteException;

    long preEnroll() throws RemoteException;

    void releaseHandle() throws RemoteException;

    void removeEnrolledFace() throws RemoteException;

    void setAdjusterParams(AdjusterParams adjusterParams) throws RemoteException;

    void setEnrollPreview(FileDescriptor fileDescriptor, int i) throws RemoteException;

    void setFastUnlockEnable(boolean z) throws RemoteException;

    void setParam(boolean z) throws RemoteException;

    void setVerifyTestPreview(FileDescriptor fileDescriptor, int i) throws RemoteException;

    void startAuthenticate(IFaceDetectClient iFaceDetectClient) throws RemoteException;

    void startAuthenticateIRTest(String str, IFaceDetectIRClient iFaceDetectIRClient) throws RemoteException;

    void startAuthenticateModel(IFaceDetectClient iFaceDetectClient, String str) throws RemoteException;

    int startAuthenticateTest(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    int startAuthenticateWithBuffer(IFaceDetectClient iFaceDetectClient, String str, int i, int i2, int i3, int i4, int i5, boolean z) throws RemoteException;

    void stopAuthenticate() throws RemoteException;

    void stopAuthenticateModel(String str) throws RemoteException;
}
