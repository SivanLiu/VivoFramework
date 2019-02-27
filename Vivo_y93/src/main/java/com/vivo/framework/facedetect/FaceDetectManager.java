package com.vivo.framework.facedetect;

import android.app.ActivityThread;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import com.vivo.framework.facedetect.IFaceDetectClient.Stub;
import java.io.FileDescriptor;
import java.util.HashMap;

public class FaceDetectManager {
    public static final String CMD_FACE_DETECT_AUTO_TEST = "auto_test";
    public static final String CMD_FACE_DETECT_CAMERA_BEAUTY = "camera_beauty";
    public static final String CMD_FACE_DETECT_IQOOSEC = "iqoosec";
    public static final String CMD_FACE_DETECT_KEYGUARD = "keyguard";
    public static final String CMD_FACE_DETECT_NONE = "none";
    public static final String FACEUNLOCK_ENABLED = "faceunlock_enabled";
    public static final String FACEUNLOCK_START_WHEN_SCREENON = "faceunlock_start_when_screenon";
    public static final int FACE_DETECT_BUSY = -3;
    public static final int FACE_DETECT_FAILED = -1;
    public static final int FACE_DETECT_MSG_RESULT = 100;
    public static final int FACE_DETECT_NO_FACE = -2;
    public static final int FACE_DETECT_SUCEESS = 0;
    public static final int FACE_WHEN_FINGER_FAIL_FIVE_TIMES = -4;
    public static final int FACE_WHEN_PASSWORD_COUNTING = -5;
    public static final int FACE_WHEN_REBOOT = -6;
    private static final String SERVICE = "face_detect_service";
    private static final String TAG = "FaceDetectManager";
    private static final boolean isSupportFaceUnlockKey = "1".equals(SystemProperties.get("persist.facedetect.doze.key", "0"));
    private static boolean isVerifiying;
    private static HashMap<String, FaceAuthenticationCallback> mFaceAuthenticationCallback = new HashMap();
    private static final Object sAuthLock = new Object();
    private static FaceDetectManager sInstance;
    private static final Object sInstanceLock = new Object();
    private volatile long faceUnlockEnd;
    private volatile long faceUnlockStart;
    private final IFaceDetectManager mBinder;
    private ContentResolver mContentResolver;
    private final Context mContext;
    private FaceAuthenticationCallbackTest mFaceAuthenticationCallbackTest;
    private IFaceDetectClient mFaceDetectClient = new Stub() {
        public void onAuthenticationResult(String model, int errorCode, int retry_times) {
            if (FaceDebugConfig.DEBUG) {
                Log.d(FaceDetectManager.TAG, "onAuthenticationResult errorCode = " + errorCode + ", retry_times = " + retry_times);
            }
            Message msg = FaceDetectManager.this.mFaceDetectHandler.obtainMessage();
            msg.what = 100;
            msg.obj = model;
            msg.arg1 = errorCode;
            msg.arg2 = retry_times;
            FaceDetectManager.this.mFaceDetectHandler.sendMessage(msg);
        }
    };
    private Handler mFaceDetectHandler;
    private IFaceDetectIRClient mFaceDetectIRClient = new IFaceDetectIRClient.Stub() {
        public void onAuthenticationResult(int errorCode, float light, float score) {
            if (FaceDebugConfig.DEBUG) {
                Log.d(FaceDetectManager.TAG, "onAuthenticationResult errorCode = " + errorCode + ", light = " + light + ", score = " + score);
            }
            FaceDetectManager.this.mFaceAuthenticationCallbackTest.onFaceAuthenticationResult(errorCode, light, score);
        }
    };
    private HandlerThread mFaceDetectThread;

    public static abstract class FaceAuthenticationCallback {
        public void onFaceAuthenticationResult(int errorCode, int retry_times) {
        }
    }

    public static abstract class FaceAuthenticationCallbackTest {
        public void onFaceAuthenticationResult(int errorCode, float light, float score) {
        }
    }

    private class MyHandler extends Handler {
        /* synthetic */ MyHandler(FaceDetectManager this$0, Looper looper, MyHandler -this2) {
            this(looper);
        }

        private MyHandler() {
        }

        private MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    synchronized (FaceDetectManager.sAuthLock) {
                        String model = msg.obj;
                        if (model != null) {
                            FaceAuthenticationCallback callback = (FaceAuthenticationCallback) FaceDetectManager.mFaceAuthenticationCallback.get(model);
                            if (FaceDebugConfig.DEBUG) {
                                Log.d(FaceDetectManager.TAG, "result model = " + model + ", client = " + callback);
                            }
                            if (callback != null) {
                                FaceDetectManager.this.faceUnlockEnd = System.currentTimeMillis() - FaceDetectManager.this.faceUnlockStart;
                                try {
                                    callback.onFaceAuthenticationResult(msg.arg1, msg.arg2);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (!(msg.arg1 == -1024 || (FaceDetectManager.isSupportFaceUnlockKey && (FaceDetectManager.CMD_FACE_DETECT_KEYGUARD.equals(model) ^ 1) == 0))) {
                                    FaceDetectManager.this.unRegisterFaceAuthenticationCallback(model);
                                }
                                FaceDetectManager.isVerifiying = false;
                            }
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private FaceDetectManager(IFaceDetectManager binder) {
        this.mBinder = binder;
        this.mContext = getApplicationContext();
        getFaceUnlockConfig();
        if (this.mContext != null) {
            this.mContentResolver = this.mContext.getContentResolver();
        } else {
            this.mContentResolver = null;
        }
        mFaceAuthenticationCallback.clear();
        isVerifiying = false;
        this.mFaceDetectThread = new HandlerThread("VivoFaceManager");
        this.mFaceDetectThread.start();
        this.mFaceDetectHandler = new MyHandler(this, this.mFaceDetectThread.getLooper(), null);
    }

    public static FaceDetectManager getInstance() {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new FaceDetectManager(IFaceDetectManager.Stub.asInterface(ServiceManager.getService(SERVICE)));
            }
        }
        return sInstance;
    }

    public static Context getApplicationContext() {
        return ActivityThread.currentApplication();
    }

    public boolean isFaceUnlockEnable() {
        boolean z = false;
        if (this.mContentResolver == null) {
            return false;
        }
        if (System.getInt(this.mContentResolver, FACEUNLOCK_ENABLED, 0) != 0) {
            z = true;
        }
        return z;
    }

    public void setFaceUnlockEnable(boolean enabled) {
        if (this.mContentResolver == null) {
            return;
        }
        if (enabled) {
            System.putInt(this.mContentResolver, FACEUNLOCK_ENABLED, 1);
        } else {
            System.putInt(this.mContentResolver, FACEUNLOCK_ENABLED, 0);
        }
    }

    public boolean faceUnlockStartWhenScreenON() {
        boolean z = false;
        if (this.mContentResolver == null) {
            return false;
        }
        if (System.getInt(this.mContentResolver, FACEUNLOCK_START_WHEN_SCREENON, 1) != 0) {
            z = true;
        }
        return z;
    }

    public void setFaceUnlockStartWhenScreenON(boolean enabled) {
        if (this.mContentResolver == null) {
            return;
        }
        if (enabled) {
            System.putInt(this.mContentResolver, FACEUNLOCK_START_WHEN_SCREENON, 1);
        } else {
            System.putInt(this.mContentResolver, FACEUNLOCK_START_WHEN_SCREENON, 0);
        }
    }

    public boolean isFastUnlockEnable() {
        if (this.mBinder != null) {
            try {
                boolean enable = this.mBinder.getFastUnlockEnable();
                if (FaceDebugConfig.DEBUG) {
                    Log.d(TAG, "isFastUnlockEnable enable = " + enable);
                }
                return enable;
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while face authenticating: ", e);
            }
        }
        return false;
    }

    public void setFastUnlockEnable(boolean enable) {
        if (this.mBinder != null) {
            try {
                this.mBinder.setFastUnlockEnable(enable);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while face authenticating: ", e);
            }
        }
    }

    public boolean hasFaceID() {
        if (this.mBinder != null) {
            try {
                return this.mBinder.hasEnrolledFace();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while face authenticating: ", e);
            }
        }
        return false;
    }

    public boolean removeFaceID() {
        if (this.mBinder != null) {
            try {
                this.mBinder.removeEnrolledFace();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while face authenticating: ", e);
            }
        }
        return false;
    }

    public int enrolledFaceID() {
        return -1;
    }

    private void getFaceUnlockConfig() {
        if (this.mBinder != null) {
            try {
                this.mBinder.FaceDetectInit();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while getFaceUnlockConfig: ", e);
            }
        }
    }

    public void setEnrollPreview(FileDescriptor fd, int previewlen) {
        if (this.mBinder != null) {
            try {
                this.mBinder.setEnrollPreview(fd, previewlen);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while face authenticating: ", e);
            }
        }
    }

    public FaceEnrollResult startEnrollFace(int mPreviewDatalen, int pixelFormat, int mPreviewWidth, int mPreviewHeight, int orientation) {
        if (this.mBinder != null) {
            try {
                byte[] result = this.mBinder.enrollFaceWithImage(mPreviewDatalen, pixelFormat, mPreviewWidth, mPreviewHeight, orientation);
                if (result == null || result.length != 5) {
                    return null;
                }
                int erroCode = result[0];
                boolean enrollFinished = result[1] == (byte) 1;
                int currentEnrollDirect = result[2];
                int enrollFaceStatus = result[3];
                int enrolledDirect = result[4];
                if (FaceDebugConfig.DEBUG) {
                    Log.d(TAG, "erroCode = " + erroCode + ",enrollFinished = " + enrollFinished + ",currentEnrollDirect = " + currentEnrollDirect + ",enrollFaceStatus = " + enrollFaceStatus + ",enrolledDirect = " + enrolledDirect);
                }
                return new FaceEnrollResult(erroCode, enrollFinished, currentEnrollDirect, enrollFaceStatus, enrolledDirect);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while face authenticating: ", e);
            }
        }
        return null;
    }

    public void startFaceUnlock(FaceAuthenticationCallback callback) {
        startFaceUnlock(callback, CMD_FACE_DETECT_KEYGUARD);
    }

    /* JADX WARNING: Missing block: B:2:0x0004, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startFaceUnlock(FaceAuthenticationCallback callback, String model) {
        if (callback != null && model != null && model.length() > 0 && !isVerifiying) {
            isVerifiying = true;
            this.faceUnlockStart = System.currentTimeMillis();
            if (this.mBinder != null) {
                try {
                    synchronized (sAuthLock) {
                        registerFaceAuthenticationCallback(callback, model);
                    }
                    this.mBinder.startAuthenticateModel(this.mFaceDetectClient, model);
                } catch (RemoteException e) {
                    Log.w(TAG, "Remote exception while face authenticating: ", e);
                }
            }
        }
    }

    public void setVerifyTestPreview(FileDescriptor fd, int previewlen) {
        if (this.mBinder != null) {
            try {
                this.mBinder.setVerifyTestPreview(fd, previewlen);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while face authenticating: ", e);
            }
        }
    }

    public int startFaceUnlockTest(int mPreviewDatalen, int pixelFormat, int mPreviewWidth, int mPreviewHeight, int orientation) {
        int result = -1000;
        Log.d(TAG, "startFaceUnlockTest");
        if (this.mBinder == null) {
            return result;
        }
        try {
            return this.mBinder.startAuthenticateTest(mPreviewDatalen, pixelFormat, mPreviewWidth, mPreviewHeight, orientation);
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception while face authenticating: ", e);
            return result;
        }
    }

    public void startIRFaceUnlockTest(String packageName, FaceAuthenticationCallbackTest callback) {
        this.mFaceAuthenticationCallbackTest = callback;
        if (this.mBinder != null) {
            try {
                this.mBinder.startAuthenticateIRTest(packageName, this.mFaceDetectIRClient);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while face authenticating: ", e);
            }
        }
    }

    public void faceRename(byte[] faceName) {
        if (this.mBinder != null) {
            try {
                this.mBinder.faceRename(faceName);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while face authenticating: ", e);
            }
        }
    }

    public byte[] getFaceName() {
        if (this.mBinder != null) {
            try {
                return this.mBinder.getFaceRename();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while face authenticating: ", e);
            }
        }
        return null;
    }

    public void stopFaceUnlock() {
        stopFaceUnlock(CMD_FACE_DETECT_KEYGUARD);
    }

    public void stopFaceUnlock(String model) {
        if (model != null) {
            isVerifiying = false;
            if (this.mBinder != null) {
                try {
                    this.mBinder.stopAuthenticateModel(model);
                    synchronized (sAuthLock) {
                        if (!(isSupportFaceUnlockKey && (CMD_FACE_DETECT_KEYGUARD.equals(model) ^ 1) == 0)) {
                            unRegisterFaceAuthenticationCallback(model);
                        }
                    }
                } catch (RemoteException e) {
                    Log.w(TAG, "Remote exception while face authenticating: ", e);
                }
            }
        }
    }

    public long preEnroll() {
        Log.d(TAG, "preEnroll");
        long random = 0;
        if (this.mBinder == null) {
            return random;
        }
        try {
            return this.mBinder.preEnroll();
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception while face preEnroll: ", e);
            return random;
        }
    }

    public int getRetryCount() {
        int count = 5;
        if (this.mBinder != null) {
            try {
                count = this.mBinder.getRetryCount();
                if (FaceDebugConfig.DEBUG) {
                    Log.d(TAG, "retry count = " + count);
                }
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while face getRetryCount: ", e);
            }
        }
        return count;
    }

    public boolean isFaceUnlockRunning() {
        boolean status = false;
        if (this.mBinder != null) {
            try {
                status = this.mBinder.isFaceUnlockRunning();
                if (FaceDebugConfig.DEBUG) {
                    Log.d(TAG, "retry count = " + status);
                }
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while face isFaceUnlockRunning: ", e);
            }
        }
        return status;
    }

    public void release() {
        if (this.mBinder != null) {
            try {
                this.mBinder.releaseHandle();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while face isFaceUnlockRunning: ", e);
            }
        }
    }

    public AdjusterParams getAdjusterParams() {
        if (this.mBinder == null) {
            return null;
        }
        AdjusterParams params = null;
        try {
            params = this.mBinder.getAdjusterParams();
        } catch (RemoteException e) {
            Log.w(TAG, "getAdjusterParams error ", e);
        }
        return params;
    }

    public void setAdjusterParams(AdjusterParams params) {
        if (this.mBinder != null) {
            try {
                this.mBinder.setAdjusterParams(params);
            } catch (RemoteException e) {
                Log.w(TAG, "set AdjusterParams error ", e);
            }
        }
    }

    public void notifyFaceUnlockEnable(boolean enable) {
        if (this.mBinder != null) {
            try {
                this.mBinder.notifyFaceUnlockEnable(enable);
            } catch (RemoteException e) {
                Log.w(TAG, "notify faceunlock enable error ", e);
            }
        }
    }

    public void notifyFaceUnlockKillEnable(boolean killenable) {
        if (this.mBinder != null) {
            try {
                this.mBinder.notifyFaceUnlockKillEnable(killenable);
            } catch (RemoteException e) {
                Log.w(TAG, "notify faceunlock kill enable error ", e);
            }
        }
    }

    public FileDescriptor getSharedMemoryFD(int length) {
        Log.i(TAG, "getSharedMemoryFD ");
        if (this.mBinder != null) {
            try {
                FileDescriptor fd = this.mBinder.getSharedMemoryFD(length);
                Log.i(TAG, "getSharedMemoryFD success with fd " + fd);
                return fd;
            } catch (RemoteException e) {
                Log.w(TAG, "getSharedMemoryFD", e);
            }
        }
        return null;
    }

    public void initEnroll() {
        Log.i(TAG, "initEnroll ");
        if (this.mBinder != null) {
            try {
                this.mBinder.initEnroll();
                Log.i(TAG, "initEnroll success");
            } catch (RemoteException e) {
                Log.w(TAG, "initEnroll error", e);
            }
        }
    }

    private void registerFaceAuthenticationCallback(FaceAuthenticationCallback callback, String model) {
        if (callback != null && model != null && model.length() > 0) {
            mFaceAuthenticationCallback.put(model, callback);
        }
    }

    private void unRegisterFaceAuthenticationCallback(String model) {
        if (model != null && model.length() > 0) {
            mFaceAuthenticationCallback.remove(model);
        }
    }

    public int startFaceUnlock(FaceAuthenticationCallback callback, String model, int mPreviewDatalen, int pixelFormat, int mPreviewWidth, int mPreviewHeight, int orientation) {
        int result = -1;
        if (model == null) {
            return -1;
        }
        if (this.mBinder != null) {
            try {
                synchronized (sAuthLock) {
                    registerFaceAuthenticationCallback(callback, model);
                }
                result = this.mBinder.startAuthenticateWithBuffer(this.mFaceDetectClient, model, mPreviewDatalen, pixelFormat, mPreviewWidth, mPreviewHeight, orientation, false);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while face authenticating: ", e);
            }
        }
        return result;
    }

    public boolean isDarkEnvironment() {
        try {
            return this.mBinder.isDarkEnvironment();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isIRLedAvailable() {
        try {
            return this.mBinder.isIRLedAvailable();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setParam(boolean isDarkEnvironment) {
        try {
            this.mBinder.setParam(isDarkEnvironment);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void notifyBrightnessChange(boolean changed) {
        try {
            this.mBinder.notifyBrightnessChange(changed);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void notifyFaceKeyguardStatus(int status) {
        if (this.mBinder != null) {
            try {
                this.mBinder.notifyFaceKeyguardStatus(status);
            } catch (RemoteException e) {
                Log.w(TAG, "notify face keyguard is in secure lock page ", e);
            }
        }
    }

    public void notifyOtherMessage(String message, int param, int extra) {
        if (this.mBinder != null) {
            try {
                this.mBinder.notifyOtherMessage(message, param, extra);
            } catch (RemoteException e) {
                Log.w(TAG, "notifyOtherMessage failed ", e);
            }
        }
    }
}
