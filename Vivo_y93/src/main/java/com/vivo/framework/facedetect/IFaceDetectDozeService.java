package com.vivo.framework.facedetect;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFaceDetectDozeService extends IInterface {

    public static abstract class Stub extends Binder implements IFaceDetectDozeService {
        private static final String DESCRIPTOR = "com.vivo.framework.facedetect.IFaceDetectDozeService";
        static final int TRANSACTION_onFaceUnlockIconStatus = 1;

        private static class Proxy implements IFaceDetectDozeService {
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

            public void onFaceUnlockIconStatus(int status, int errorcode, int retrytimes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeInt(errorcode);
                    _data.writeInt(retrytimes);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFaceDetectDozeService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFaceDetectDozeService)) {
                return new Proxy(obj);
            }
            return (IFaceDetectDozeService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onFaceUnlockIconStatus(data.readInt(), data.readInt(), data.readInt());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onFaceUnlockIconStatus(int i, int i2, int i3) throws RemoteException;
}
