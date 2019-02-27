package com.vivo.services.facedetect;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.vivo.framework.facedetect.FaceEnrollResult;
import java.io.FileDescriptor;
import java.util.Vector;

public class FaceDetectNative {
    private static final int FACE_DETECT_TRANSACT_CHECK_MODEL_UPDATE = 114;
    private static final int FACE_DETECT_TRANSACT_FACE_INIT_ENROLL = 116;
    private static final int FACE_DETECT_TRANSACT_FACE_INIT_VERITY_THREAD = 117;
    private static final int FACE_DETECT_TRANSACT_FACE_RELEASE_VERIFY_THREAD = 118;
    private static final int FACE_ENROLL = 102;
    private static final int FACE_GET_RANDOM = 110;
    private static final int FACE_INIT = 104;
    private static final int FACE_RELEASE = 106;
    private static final int FACE_RELEASE_TA = 115;
    private static final int FACE_REMOVE = 105;
    private static final int FACE_RENAME = 107;
    private static final int FACE_SET_FAST_UNLOCK = 109;
    private static final int FACE_SET_PREVIEW = 108;
    private static final int FACE_VERIFY = 103;
    private static final int FACE_VERIFY_INT = 111;
    private static final int FACE_VERIFY_RELEASE = 112;
    private static final int FACE_VERIFY_RELEASE_ALL = 113;
    private static final String TAG = FaceDetectNative.class.getSimpleName();
    private static IBinder mBinder;
    private static final Object sBindLock = new Object();
    private static FaceDetectNative sInstance;
    private static final Object sInstanceLock = new Object();
    private FaceDetectStatus mFaceDetectStatus = FaceDetectStatus.getInstance();

    private FaceDetectNative(IBinder binder) {
        mBinder = binder;
    }

    public static void resetBind() {
        synchronized (sBindLock) {
            mBinder = null;
        }
    }

    public static FaceDetectNative getInstance() {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new FaceDetectNative(ServiceManager.getService("face_detect"));
            }
        }
        return sInstance;
    }

    private void ensureBinderAlive() {
        synchronized (sBindLock) {
            if (mBinder == null || (mBinder.isBinderAlive() ^ 1) != 0) {
                mBinder = ServiceManager.getService("face_detect");
            }
        }
    }

    public int processFaceDetectInit() {
        try {
            ensureBinderAlive();
            synchronized (sBindLock) {
                if (mBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    mBinder.transact(FACE_INIT, data, reply, 0);
                    int ret = reply.readInt();
                    int result = reply.readInt();
                    int supporFace = reply.readInt();
                    int enrolledFace = reply.readInt();
                    int fastUnlock = reply.readInt();
                    int faceId = reply.readInt();
                    byte[] faceName = reply.createByteArray();
                    if (result == 0 && this.mFaceDetectStatus != null) {
                        this.mFaceDetectStatus.setEnrolledFaceID(faceId);
                        this.mFaceDetectStatus.setFastUnlockStatus(fastUnlock != -1);
                        this.mFaceDetectStatus.setEnrolledFaceStatus(enrolledFace == 1);
                        this.mFaceDetectStatus.setFaceName(faceName);
                    }
                    reply.recycle();
                    data.recycle();
                    return result;
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Failed to call face service", e);
        }
        return -1;
    }

    public FaceEnrollResult processEnroll(int mPreviewDatalen, int pixelFormat, int mPreviewWidth, int mPreviewHeight, int orientation) {
        try {
            ensureBinderAlive();
            synchronized (sBindLock) {
                if (mBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    data.writeInt(mPreviewDatalen);
                    data.writeInt(pixelFormat);
                    data.writeInt(mPreviewWidth);
                    data.writeInt(mPreviewHeight);
                    data.writeInt(orientation);
                    mBinder.transact(FACE_ENROLL, data, reply, 0);
                    int ret = reply.readInt();
                    int erroCode = reply.readInt();
                    int enrollFinished = reply.readInt();
                    int currentEnrollDirect = reply.readInt();
                    int enrollFaceStatus = reply.readInt();
                    int enrolledDirect = reply.readInt();
                    boolean finshed = enrollFinished == 1;
                    if (FaceDebugConfig.DEBUG) {
                        Log.d(TAG, "erroCode = " + erroCode);
                        Log.d(TAG, "enrollFinished = " + enrollFinished);
                        Log.d(TAG, "currentEnrollDirect = " + currentEnrollDirect);
                        Log.d(TAG, "enrollFaceStatus = " + enrollFaceStatus);
                        Log.d(TAG, "enrolledDirect = " + enrolledDirect);
                        Log.d(TAG, "finshed = " + finshed);
                    }
                    reply.recycle();
                    data.recycle();
                    FaceEnrollResult faceEnrollResult = new FaceEnrollResult(erroCode, finshed, currentEnrollDirect, enrollFaceStatus, enrolledDirect);
                    return faceEnrollResult;
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Failed to call face service", e);
        }
        return null;
    }

    public Vector processVerify(int mPreviewDatalen, int pixelFormat, int mPreviewWidth, int mPreviewHeight, int orientation, boolean livenesscheck, boolean isDarkEnvironment, boolean openIrLed) {
        return processVerify(mPreviewDatalen, pixelFormat, mPreviewWidth, mPreviewHeight, orientation, livenesscheck, isDarkEnvironment, openIrLed, null);
    }

    public Vector processVerify(int mPreviewDatalen, int pixelFormat, int mPreviewWidth, int mPreviewHeight, int orientation, boolean livenesscheck, boolean isDarkEnvironment, boolean openIrLed, String packageName) {
        try {
            ensureBinderAlive();
            synchronized (sBindLock) {
                if (mBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    data.writeInt(mPreviewDatalen);
                    data.writeInt(pixelFormat);
                    data.writeInt(mPreviewWidth);
                    data.writeInt(mPreviewHeight);
                    data.writeInt(orientation);
                    data.writeInt(livenesscheck ? 1 : 0);
                    data.writeInt(isDarkEnvironment ? 1 : 0);
                    data.writeInt(openIrLed ? 1 : 0);
                    data.writeString(packageName);
                    mBinder.transact(FACE_VERIFY, data, reply, 0);
                    int ret = reply.readInt();
                    int direct = reply.readInt();
                    float score = reply.readFloat();
                    Vector result = new Vector();
                    result.add(new Integer(direct));
                    result.add(new Float(score));
                    reply.recycle();
                    data.recycle();
                    return result;
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Failed to call face service", e);
        }
        return null;
    }

    public int processFaceDetectRemove() {
        try {
            ensureBinderAlive();
            synchronized (sBindLock) {
                if (mBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    mBinder.transact(FACE_REMOVE, data, reply, 0);
                    int ret = reply.readInt();
                    int result = reply.readInt();
                    reply.recycle();
                    data.recycle();
                    return result;
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Failed to call face service", e);
        }
        return -1;
    }

    public void processFaceSetPreviewBuffer(FileDescriptor fd, int bufferLen, int operateType, boolean is2pd) {
        int i = 0;
        try {
            ensureBinderAlive();
            synchronized (sBindLock) {
                if (mBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    data.writeFileDescriptor(fd);
                    data.writeInt(bufferLen);
                    data.writeInt(operateType);
                    if (is2pd) {
                        i = 1;
                    }
                    data.writeInt(i);
                    mBinder.transact(FACE_SET_PREVIEW, data, reply, 0);
                    int ret = reply.readInt();
                    reply.recycle();
                    data.recycle();
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Failed to call face service", e);
        }
    }

    public int processFaceRename(byte[] faceName) {
        try {
            ensureBinderAlive();
            synchronized (sBindLock) {
                if (mBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    data.writeByteArray(faceName);
                    mBinder.transact(FACE_RENAME, data, reply, 0);
                    int ret = reply.readInt();
                    int result = reply.readInt();
                    reply.recycle();
                    data.recycle();
                    return result;
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Failed to call face service", e);
        }
        return -1;
    }

    public int processFaceSetFastUnlock(boolean enabled) {
        try {
            ensureBinderAlive();
            synchronized (sBindLock) {
                if (mBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    data.writeInt(enabled ? 1 : -1);
                    mBinder.transact(FACE_SET_FAST_UNLOCK, data, reply, 0);
                    int ret = reply.readInt();
                    int result = reply.readInt();
                    reply.recycle();
                    data.recycle();
                    return result;
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Failed to call face service", e);
        }
        return -1;
    }

    public long processPreEnroll() {
        try {
            ensureBinderAlive();
            synchronized (sBindLock) {
                if (mBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    mBinder.transact(FACE_GET_RANDOM, data, reply, 0);
                    int ret = reply.readInt();
                    long result = reply.readLong();
                    reply.recycle();
                    data.recycle();
                    return result;
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Failed to call face service", e);
        }
        return 0;
    }

    public int processFaceDetectRelease() {
        try {
            ensureBinderAlive();
            synchronized (sBindLock) {
                if (mBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    mBinder.transact(FACE_RELEASE, data, reply, 0);
                    int ret = reply.readInt();
                    reply.recycle();
                    data.recycle();
                    return ret;
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Failed to call face service", e);
        }
        return 0;
    }

    public int processFaceVerifyInit() {
        try {
            ensureBinderAlive();
            synchronized (sBindLock) {
                if (mBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    mBinder.transact(FACE_VERIFY_INT, data, reply, 0);
                    int ret = reply.readInt();
                    int result = reply.readInt();
                    reply.recycle();
                    data.recycle();
                    return result;
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Failed to call face service", e);
        }
        return -1;
    }

    public int processFaceVerifyRelease(boolean all) {
        try {
            ensureBinderAlive();
            synchronized (sBindLock) {
                if (mBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    if (all) {
                        mBinder.transact(FACE_VERIFY_RELEASE_ALL, data, reply, 0);
                    } else {
                        mBinder.transact(FACE_VERIFY_RELEASE, data, reply, 0);
                    }
                    int ret = reply.readInt();
                    reply.recycle();
                    data.recycle();
                    return ret;
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Failed to call face service", e);
        }
        return 0;
    }

    public Vector processCheckIfModelUpdate() {
        try {
            ensureBinderAlive();
            synchronized (sBindLock) {
                if (mBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    mBinder.transact(FACE_DETECT_TRANSACT_CHECK_MODEL_UPDATE, data, reply, 0);
                    int ret = reply.readInt();
                    int result = reply.readInt();
                    int errorImageCount = reply.readInt();
                    Vector vResult = new Vector();
                    if (vResult != null) {
                        vResult.add(new Integer(result));
                        vResult.add(new Integer(errorImageCount));
                    }
                    reply.recycle();
                    data.recycle();
                    return vResult;
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Failed to call face service", e);
        }
        return null;
    }

    public void releaseTa() {
        try {
            ensureBinderAlive();
            synchronized (sBindLock) {
                if (mBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    mBinder.transact(FACE_RELEASE_TA, data, reply, 0);
                    int ret = reply.readInt();
                    reply.recycle();
                    data.recycle();
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Failed to call face service", e);
        }
    }

    public void processFaceEnrollInit() {
        try {
            ensureBinderAlive();
            synchronized (sBindLock) {
                if (mBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    mBinder.transact(FACE_DETECT_TRANSACT_FACE_INIT_ENROLL, data, reply, 0);
                    reply.recycle();
                    data.recycle();
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Failed to call face service", e);
        }
    }

    public void prepareVerifyThread() {
        try {
            ensureBinderAlive();
            synchronized (sBindLock) {
                if (mBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    mBinder.transact(FACE_DETECT_TRANSACT_FACE_INIT_VERITY_THREAD, data, reply, 0);
                    reply.recycle();
                    data.recycle();
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Failed to call face service", e);
        }
    }

    public void resetVerifyThread() {
        try {
            ensureBinderAlive();
            synchronized (sBindLock) {
                if (mBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    mBinder.transact(FACE_DETECT_TRANSACT_FACE_RELEASE_VERIFY_THREAD, data, reply, 0);
                    reply.recycle();
                    data.recycle();
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Failed to call face service", e);
        }
    }
}
