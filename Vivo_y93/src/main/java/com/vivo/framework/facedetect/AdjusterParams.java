package com.vivo.framework.facedetect;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class AdjusterParams implements Parcelable {
    public static final Creator<AdjusterParams> CREATOR = new Creator<AdjusterParams>() {
        public AdjusterParams createFromParcel(Parcel in) {
            return new AdjusterParams(in);
        }

        public AdjusterParams[] newArray(int size) {
            return new AdjusterParams[size];
        }
    };
    public static final int DEFAULT_EXPOSURE = 0;
    public static final int MAX_EXPOSURE = 40;
    public static final int MEDIUM_EXPOSURE = 20;
    public static final int MINI_EXPOSURE = 1;
    public static final int PIXELS_H = 20;
    public static final int PIXELS_W = 20;
    public static final int SMALL_MEDIUM_EXPOSURE = 10;
    public static final int THRESHOLD_DARK = 50;
    public static final int THRESHOLD_DARKROOM = 50;
    public static final float THRESHOLD_HDR_CONTRAST = 100.0f;
    public static final float THRESHOLD_HDR_DARK_PERCENTAGE = 0.15f;
    public static final float THRESHOLD_HDR_LIGHT_PERCENTAGE = 0.15f;
    public static final int THRESHOLD_LIGHT = 220;
    public static final float THRESHOLD_OVER_LIGHT_PERCENTAGE = 0.4f;
    public int miniExp;
    public float pixelsH;
    public float pixelsW;
    public int smallMediumExp;
    public float thresholdDark;
    public float thresholdDarkRoom;
    public float thresholdHDRContrast;
    public float thresholdHDRDarkPercentage;
    public float thresholdHDRLightPercentage;
    public float thresholdLight;
    public float thresholdOverLightPercentage;

    public AdjusterParams() {
        this.pixelsH = 20.0f;
        this.pixelsW = 20.0f;
        this.thresholdDark = 50.0f;
        this.thresholdLight = 220.0f;
        this.thresholdHDRDarkPercentage = 0.15f;
        this.thresholdHDRLightPercentage = 0.15f;
        this.thresholdOverLightPercentage = 0.4f;
        this.thresholdHDRContrast = 100.0f;
        this.thresholdDarkRoom = 50.0f;
        this.miniExp = 1;
        this.smallMediumExp = 10;
    }

    protected AdjusterParams(Parcel in) {
        this.pixelsH = in.readFloat();
        this.pixelsW = in.readFloat();
        this.thresholdDark = in.readFloat();
        this.thresholdLight = in.readFloat();
        this.thresholdHDRDarkPercentage = in.readFloat();
        this.thresholdHDRLightPercentage = in.readFloat();
        this.thresholdOverLightPercentage = in.readFloat();
        this.thresholdHDRContrast = in.readFloat();
        this.thresholdDarkRoom = in.readFloat();
        this.miniExp = in.readInt();
        this.smallMediumExp = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.pixelsH);
        dest.writeFloat(this.pixelsW);
        dest.writeFloat(this.thresholdDark);
        dest.writeFloat(this.thresholdLight);
        dest.writeFloat(this.thresholdHDRDarkPercentage);
        dest.writeFloat(this.thresholdHDRLightPercentage);
        dest.writeFloat(this.thresholdOverLightPercentage);
        dest.writeFloat(this.thresholdHDRContrast);
        dest.writeFloat(this.thresholdDarkRoom);
        dest.writeInt(this.miniExp);
        dest.writeInt(this.smallMediumExp);
    }
}
