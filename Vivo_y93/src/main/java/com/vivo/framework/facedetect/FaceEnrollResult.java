package com.vivo.framework.facedetect;

public class FaceEnrollResult {
    public int mCurrentEnrollDirect;
    public int mEnrollFaceStatus;
    public boolean mEnrollFinished;
    public int mEnrolledDirect;
    public int mErroCode;

    public FaceEnrollResult(int erroCode, boolean enrollFinished, int currentEnrollDirect, int enrollFaceStatus, int enrolledDirect) {
        this.mErroCode = erroCode;
        this.mEnrollFinished = enrollFinished;
        this.mCurrentEnrollDirect = currentEnrollDirect;
        this.mEnrollFaceStatus = enrollFaceStatus;
        this.mEnrolledDirect = enrolledDirect;
    }
}
