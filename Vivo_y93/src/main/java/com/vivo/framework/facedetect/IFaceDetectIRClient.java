package com.vivo.framework.facedetect;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFaceDetectIRClient extends IInterface {

    public static abstract class Stub extends Binder implements IFaceDetectIRClient {
        private static final String DESCRIPTOR = "com.vivo.framework.facedetect.IFaceDetectIRClient";
        static final int TRANSACTION_onAuthenticationResult = 1;

        private static class Proxy implements IFaceDetectIRClient {
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

            public void onAuthenticationResult(int errorCode, float light, float score) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    _data.writeFloat(light);
                    _data.writeFloat(score);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFaceDetectIRClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFaceDetectIRClient)) {
                return new Proxy(obj);
            }
            return (IFaceDetectIRClient) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onAuthenticationResult(data.readInt(), data.readFloat(), data.readFloat());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onAuthenticationResult(int i, float f, float f2) throws RemoteException;
}
