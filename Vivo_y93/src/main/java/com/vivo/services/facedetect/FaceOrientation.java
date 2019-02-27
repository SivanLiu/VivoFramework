package com.vivo.services.facedetect;

public enum FaceOrientation {
    UP(1),
    LEFT(2),
    DOWN(4),
    RIGHT(8),
    UNKNOWN(15);
    
    private static FaceOrientation[] sFaceOrientations;
    final int nativeInt;

    static {
        sFaceOrientations = new FaceOrientation[]{null, UP, LEFT, null, DOWN, null, null, null, RIGHT, null, null, null, null, null, null, UNKNOWN};
    }

    private FaceOrientation(int ni) {
        this.nativeInt = ni;
    }

    public int getValue() {
        return this.nativeInt;
    }

    public static FaceOrientation nativeToOrientation(int ni) {
        return sFaceOrientations[ni];
    }
}
