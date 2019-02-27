package com.vivo.services.rms.sdk.args;

import android.os.Parcel;

public class Int3String3Args extends Args {
    public int mInt0;
    public int mInt1;
    public int mInt2;
    public String mString0;
    public String mString1;
    public String mString2;

    public void writeToParcel(Parcel dest) {
        dest.writeInt(this.mInt0);
        dest.writeInt(this.mInt1);
        dest.writeInt(this.mInt2);
        dest.writeString(this.mString0);
        dest.writeString(this.mString1);
        dest.writeString(this.mString2);
    }

    public void readFromParcel(Parcel data) {
        this.mInt0 = data.readInt();
        this.mInt1 = data.readInt();
        this.mInt2 = data.readInt();
        this.mString0 = data.readString();
        this.mString1 = data.readString();
        this.mString2 = data.readString();
    }

    public void recycle() {
        this.mString0 = null;
        this.mString1 = null;
        this.mString2 = null;
    }
}
