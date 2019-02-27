package com.vivo.services.facedetect;

public class FaceDetectStatus {
    private static final int MAX_NAME_LEN = 256;
    private static FaceDetectStatus sInstance;
    private static final Object sInstanceLock = new Object();
    private int enrolledFaceID = -1;
    private boolean isSupportFaceDetect = true;
    private boolean mEnrolledFaceStatus = false;
    private byte[] mFaceName;
    private boolean mFastUnlockStatus = true;

    private FaceDetectStatus() {
    }

    public static FaceDetectStatus getInstance() {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new FaceDetectStatus();
            }
        }
        return sInstance;
    }

    public boolean isSupportFaceDetect() {
        return this.isSupportFaceDetect;
    }

    public boolean getEnrolledFaceStatus() {
        return this.mEnrolledFaceStatus;
    }

    public void setEnrolledFaceStatus(boolean faceStatus) {
        this.mEnrolledFaceStatus = faceStatus;
    }

    public int getEnrolledFaceID() {
        return this.enrolledFaceID;
    }

    public void setEnrolledFaceID(int faceID) {
        this.enrolledFaceID = faceID;
    }

    public boolean getFastUnlockStatus() {
        return this.mFastUnlockStatus;
    }

    public void setFastUnlockStatus(boolean fastUnlockStatus) {
        this.mFastUnlockStatus = fastUnlockStatus;
    }

    public byte[] getFaceName() {
        return this.mFaceName;
    }

    public void setFaceName(byte[] faceName) {
        if (faceName == null || faceName.length <= 0 || faceName.length >= 256) {
            this.mFaceName = null;
            return;
        }
        this.mFaceName = new byte[faceName.length];
        System.arraycopy(faceName, 0, this.mFaceName, 0, faceName.length);
    }
}
