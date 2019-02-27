package com.vivo.services.facedetect.analyze;

import android.graphics.Rect;

public class AnalyzeResult {
    public static final int TYPE_DARK = 1;
    public static final int TYPE_HDR = 2;
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_OVERLIGHT = 3;
    public Rect darkestArea = null;
    public int mType = 0;

    public AnalyzeResult(int type, Rect darkestArea) {
        this.mType = type;
        this.darkestArea = darkestArea;
    }

    public String toString() {
        String val = "NORMAL";
        if (this.mType == 2) {
            val = "HDR";
        } else if (this.mType == 1) {
            val = "DARK";
        } else if (this.mType == 3) {
            val = "OVER_LIGHT";
        }
        return val + " Darkest: " + this.darkestArea.toString();
    }
}
